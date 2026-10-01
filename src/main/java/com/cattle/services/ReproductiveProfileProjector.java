package com.cattle.services;

import com.cattle.config.LambdaContext;
import com.cattle.entities.bovines.ProfileLactancy;
import com.cattle.entities.bovines.ProfilePregnancy;
import com.cattle.entities.bovines.ProfileReproductive;
import com.cattle.enums.BovineEventType;
import com.cattle.enums.LogType;
import com.cattle.events.entities.BovineEventItem;
import com.cattle.repository.BovineEventRepository;
import com.cattle.repository.ProfileLactancyRepository;
import com.cattle.repository.ProfilePregnancyRepository;
import com.cattle.repository.ProfileReproductiveRepository;
import com.cattle.tasks.planner.GestationCalendar;
import com.cattle.tasks.planner.PlannerEvent;
import com.cattle.tasks.service.PlannerEventMapper;
import com.cattle.tasks.service.ReproductiveTaskSettingsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.cattle.services.ReproductiveProfileStatus.LACTATION_CLOSED;
import static com.cattle.services.ReproductiveProfileStatus.LACTATION_DRY;
import static com.cattle.services.ReproductiveProfileStatus.LACTATION_LACTATING;
import static com.cattle.services.ReproductiveProfileStatus.PREGNANCY_ACTIVE;
import static com.cattle.services.ReproductiveProfileStatus.PREGNANCY_CLOSED;

/**
 * Proyecta los eventos reproductivos sobre los perfiles de preñez y lactancia
 * (HU-20260930, reglas PE1–PE5). Incremental por evento, no replay: los perfiles tienen historia
 * cargada a mano que los eventos no conocen (PE8, D2). Mismo patrón que {@link ExitEventProjector}.
 */
@Service
public class ReproductiveProfileProjector {

    public static final Set<BovineEventType> PROFILE_EVENTS = EnumSet.of(
            BovineEventType.DIAGNOSTICO_PRENEZ, BovineEventType.PARTO, BovineEventType.SECADO, BovineEventType.ABORTO);

    private static final String REPRODUCTIVE_SK = "PROFILE#REPRODUCTIVE";
    private static final String RESULT_PREGNANT = "PRENIADA";
    private static final String RESULT_OPEN = "VACIA";

    private final ProfileReproductiveRepository reproductiveRepository;
    private final ProfilePregnancyRepository pregnancyRepository;
    private final ProfileLactancyRepository lactancyRepository;
    private final BovineEventRepository bovineEventRepository;
    private final PlannerEventMapper eventMapper;
    private final ReproductiveTaskSettingsProvider settingsProvider;
    private final LambdaContext lambdaContext;
    /** Sitio de las lactancias ({@code gsi1pk=LACT#FARM#<siteId>}), el que consulta el ordeño (D5). */
    private final String siteId;

    public ReproductiveProfileProjector(ProfileReproductiveRepository reproductiveRepository,
                                        ProfilePregnancyRepository pregnancyRepository,
                                        ProfileLactancyRepository lactancyRepository,
                                        BovineEventRepository bovineEventRepository,
                                        PlannerEventMapper eventMapper,
                                        ReproductiveTaskSettingsProvider settingsProvider,
                                        LambdaContext lambdaContext,
                                        @Value("${reproductive-tasks.site-id:001}") String siteId) {
        this.reproductiveRepository = reproductiveRepository;
        this.pregnancyRepository = pregnancyRepository;
        this.lactancyRepository = lactancyRepository;
        this.bovineEventRepository = bovineEventRepository;
        this.eventMapper = eventMapper;
        this.settingsProvider = settingsProvider;
        this.lambdaContext = lambdaContext;
        this.siteId = siteId;
    }

    /** Aplica el evento ya guardado; no hace nada si no es un evento de perfil. */
    public void project(String bovineId, BovineEventItem item) {
        PlannerEvent event = eventMapper.toPlannerEvent(item);
        if (event == null || !PROFILE_EVENTS.contains(event.type())) {
            return;
        }
        String pk = "BOVINE#" + bovineId;
        switch (event.type()) {
            case DIAGNOSTICO_PRENEZ -> {
                String result = event.payloadString("result");
                if (RESULT_PREGNANT.equalsIgnoreCase(result)) {
                    openPregnancy(bovineId, pk, event);
                } else if (RESULT_OPEN.equalsIgnoreCase(result)) {
                    closePregnancy(pk, null); // PE2
                }
            }
            case ABORTO -> closePregnancy(pk, null); // PE5
            case PARTO -> onCalving(bovineId, pk, event); // PE3
            case SECADO -> onDryOff(pk, event); // PE4
            default -> {
                // PROFILE_EVENTS acota los casos.
            }
        }
    }

    /** PE1: abre preñez activa, o actualiza el método si ya hay una (reconfirmación, CA5). */
    private void openPregnancy(String bovineId, String pk, PlannerEvent diagnosis) {
        ProfileReproductive reproductive = findOrCreateReproductive(bovineId, pk);
        String method = confirmationMethod(diagnosis.payloadString("method"));

        Optional<ProfilePregnancy> current = currentPregnancy(pk, reproductive);
        if (current.isPresent() && PREGNANCY_ACTIVE.equalsIgnoreCase(current.get().getStatus())) {
            if (method != null) {
                current.get().setConfirmationMethod(method);
                current.get().setUpdatedAt(Instant.now().toString());
                pregnancyRepository.save(current.get());
            }
            return;
        }

        int gestationDays = settingsProvider.current().gestationDays();
        List<PlannerEvent> history = eventMapper.toPlannerEvents(bovineEventRepository.findAllByBovine(bovineId));
        Optional<PlannerEvent> service = GestationCalendar.lastServiceBefore(history, diagnosis);
        LocalDate serviceDate = service.map(PlannerEvent::date)
                .orElseGet(() -> GestationCalendar.estimatedServiceDate(diagnosis));
        LocalDate expectedDue = service.map(s -> GestationCalendar.expectedCalving(s, gestationDays))
                .orElseGet(() -> GestationCalendar.calvingFromGestationEstimate(diagnosis, gestationDays));

        LocalDate keyDate = serviceDate != null ? serviceDate : diagnosis.date();
        ProfilePregnancy pregnancy = ProfilePregnancy.builder()
                .pk(pk)
                .sk("PREG#" + keyDate)
                .gsi1pk("PREG#" + PREGNANCY_ACTIVE)
                .gsi1sk(keyDate + "#BOVINE#" + bovineId)
                .build();
        pregnancy.setStatus(PREGNANCY_ACTIVE);
        pregnancy.setServiceDate(serviceDate != null ? serviceDate.toString() : null);
        pregnancy.setExpectedDueDate(expectedDue != null ? expectedDue.toString() : null);
        pregnancy.setConfirmationMethod(method);
        pregnancy.setCreatedAt(diagnosis.date().toString());
        pregnancy.setUpdatedAt(Instant.now().toString());
        pregnancy.setNotes("Registrada por evento DIAGNOSTICO_PRENEZ " + diagnosis.eventId());
        pregnancyRepository.save(pregnancy);

        reproductive.setCurrentPregnancyId(pregnancy.getSk());
        saveReproductive(reproductive);
    }

    /** PE2, PE3, PE5: cierra la preñez activa (con fecha de parto si la hubo). */
    private void closePregnancy(String pk, LocalDate calvingDate) {
        reproductiveRepository.findById(pk, REPRODUCTIVE_SK)
                .flatMap(reproductive -> currentPregnancy(pk, reproductive))
                .filter(pregnancy -> PREGNANCY_ACTIVE.equalsIgnoreCase(pregnancy.getStatus()))
                .ifPresent(pregnancy -> {
                    pregnancy.setStatus(PREGNANCY_CLOSED);
                    pregnancy.setGsi1pk("PREG#" + PREGNANCY_CLOSED);
                    if (calvingDate != null) {
                        pregnancy.setCalvingDate(calvingDate.toString());
                    }
                    pregnancy.setUpdatedAt(Instant.now().toString());
                    pregnancyRepository.save(pregnancy);
                });
    }

    /** PE3: cierra preñez y lactancia abierta; abre la lactancia n+1 (la vaca entra a ordeño, PE9). */
    private void onCalving(String bovineId, String pk, PlannerEvent calving) {
        ProfileReproductive reproductive = findOrCreateReproductive(bovineId, pk);
        Optional<ProfileLactancy> current = currentLactation(pk, reproductive);
        if (current.isPresent() && current.get().getStartDate() != null
                && calving.date().toString().compareTo(current.get().getStartDate()) <= 0) {
            // Guarda RT2: parto anterior o igual al inicio de la lactancia vigente (fuera de orden o repetido).
            lambdaContext.logInfo(LogType.SERVICE, "PARTO ignorado para perfiles: no es posterior a la lactancia vigente. bovineId="
                    + bovineId + ", parto=" + calving.date() + ", lactancia=" + current.get().getSk());
            return;
        }

        closePregnancy(pk, calving.date());
        current.filter(lactation -> !ReproductiveProfileStatus.isClosed(lactation.getStatus())
                        && lactation.getEndDate() == null)
                .ifPresent(lactation -> {
                    lactation.setStatus(LACTATION_CLOSED);
                    lactation.setEndDate(calving.date().toString());
                    lactation.setUpdatedAt(Instant.now().toString());
                    lactancyRepository.save(lactation);
                });

        int number = nextLactationNumber(pk);
        String sk = String.format("LACT#%03d", number);
        String start = calving.date().toString();
        ProfileLactancy lactation = ProfileLactancy.builder()
                .pk(pk)
                .sk(sk)
                .gsi1pk("LACT#FARM#" + siteId)
                .gsi1sk(start + "#" + LACTATION_LACTATING + "#BOVINE#" + bovineId + "#" + sk)
                .build();
        lactation.setLactationNumber(String.valueOf(number));
        lactation.setStatus(LACTATION_LACTATING);
        lactation.setStartDate(start);
        lactation.setCreatedAt(start);
        lactation.setUpdatedAt(Instant.now().toString());
        lactation.setNotes("Abierta por evento PARTO " + calving.eventId());
        lactancyRepository.save(lactation);

        reproductive.setCurrentLactationId(sk);
        saveReproductive(reproductive);
    }

    /** PE4: la lactancia vigente pasa a seca; se cierra con el siguiente parto. */
    private void onDryOff(String pk, PlannerEvent dryOff) {
        reproductiveRepository.findById(pk, REPRODUCTIVE_SK)
                .flatMap(reproductive -> currentLactation(pk, reproductive))
                .filter(lactation -> LACTATION_LACTATING.equalsIgnoreCase(lactation.getStatus())
                        && lactation.getEndDate() == null)
                .ifPresent(lactation -> {
                    if (lactation.getStartDate() != null
                            && dryOff.date().toString().compareTo(lactation.getStartDate()) < 0) {
                        lambdaContext.logInfo(LogType.SERVICE, "SECADO ignorado para perfiles: anterior al inicio de la lactancia. pk="
                                + pk + ", secado=" + dryOff.date() + ", lactancia=" + lactation.getSk());
                        return;
                    }
                    lactation.setStatus(LACTATION_DRY);
                    lactation.setDryDate(dryOff.date().toString());
                    lactation.setUpdatedAt(Instant.now().toString());
                    lactancyRepository.save(lactation);
                });
    }

    private int nextLactationNumber(String pk) {
        return lactancyRepository.findAllLactationsByBovine(pk).orElseGet(List::of).stream()
                .map(ProfileLactancy::getLactationNumber)
                .mapToInt(ReproductiveProfileProjector::parseNumber)
                .max()
                .orElse(0) + 1;
    }

    private static int parseNumber(String value) {
        try {
            return value == null ? 0 : Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private Optional<ProfilePregnancy> currentPregnancy(String pk, ProfileReproductive reproductive) {
        String id = reproductive.getCurrentPregnancyId();
        return id == null || id.isBlank() ? Optional.empty() : pregnancyRepository.findById(pk, id);
    }

    private Optional<ProfileLactancy> currentLactation(String pk, ProfileReproductive reproductive) {
        String id = reproductive.getCurrentLactationId();
        return id == null || id.isBlank() ? Optional.empty() : lactancyRepository.findById(pk, id);
    }

    /** Formato real del perfil reproductivo cuando la vaca aún no tiene uno. */
    private ProfileReproductive findOrCreateReproductive(String bovineId, String pk) {
        return reproductiveRepository.findById(pk, REPRODUCTIVE_SK).orElseGet(() -> ProfileReproductive.builder()
                .pk(pk)
                .sk(REPRODUCTIVE_SK)
                .gsi1pk("PROFILE")
                .gsi1sk("BOVINE#" + bovineId + "#REPRODUCTIVE")
                .build());
    }

    private void saveReproductive(ProfileReproductive reproductive) {
        reproductive.setUpdatedAt(Instant.now().toString());
        reproductiveRepository.save(reproductive);
    }

    /** Payload del formulario ({@code PALPA}/{@code ECOGRAFIA}) al vocabulario histórico del perfil. */
    private static String confirmationMethod(String method) {
        if (method == null) {
            return null;
        }
        return switch (method.toUpperCase()) {
            case "PALPA" -> "palpation";
            case "ECOGRAFIA" -> "ultrasound";
            default -> null;
        };
    }
}
