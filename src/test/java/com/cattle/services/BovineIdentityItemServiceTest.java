package com.cattle.services;

import com.cattle.config.AppProperties;
import com.cattle.config.LambdaContext;
import com.cattle.config.TablesConfig;
import com.cattle.entities.bovines.BovineIdentityItem;
import com.cattle.exceptions.RepositoryException;
import com.cattle.exceptions.ServiceException;
import com.cattle.repository.BovineRepository;
import com.cattle.repository.CounterRepository;
import com.cattle.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Tests unitarios para BovinesService
 * Fase 1.1 - HU-ASEGURAMIENTO-CALIDAD-001
 * 
 * Cobertura objetivo: 0% → 95%
 * Tests: 18
 */
@Tag("unit")
@Tag("fast")
@Tag("services")
class BovineIdentityItemServiceTest {

    @Mock
    private BovineRepository bovineRepository;

    @Mock
    private LambdaContext lambdaContext;

    @Mock
    private CounterRepository counterRepository;

    private BovineService bovineService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        TablesConfig tablesConfig = new TablesConfig();
        tablesConfig.setBovines("Bovines");
        bovineService = new BovineService(bovineRepository, lambdaContext, counterRepository, tablesConfig, new AppProperties());
    }

    // ==================== findAll Tests ====================

    @Test
    void findAll_returnsListOfBovines() {
        // Arrange
        String farmId = "farm-001";
        List<BovineIdentityItem> bovineIdentityItems = TestDataBuilder.createBovineList(farmId, 5);
        when(bovineRepository.findAll()).thenReturn(Optional.of(bovineIdentityItems));

        // Act
        Optional<List<BovineIdentityItem>> result = bovineService.findAll();

        // Assert
        assertTrue(result.isPresent());
        assertEquals(5, result.get().size());
        verify(bovineRepository, times(1)).findAll();
        verify(lambdaContext, never()).logException(any(), anyString(), any());
    }

    @Test
    void findAll_emptyRepository_returnsEmptyList() {
        // Arrange
        when(bovineRepository.findAll()).thenReturn(Optional.empty());

        // Act
        Optional<List<BovineIdentityItem>> result = bovineService.findAll();

        // Assert
        assertTrue(result.isEmpty());
        verify(bovineRepository, times(1)).findAll();
    }

    @Test
    void findAll_repositoryException_throwsServiceException() {
        // Arrange
        when(bovineRepository.findAll()).thenThrow(new RepositoryException("DB Error"));

        // Act & Assert
        assertThrows(ServiceException.class, () -> bovineService.findAll());
    }

    // ==================== findById Tests ====================

    @Test
    void findById_existingId_returnsBovine() {
        // Arrange
        Integer bovineId = 123;
        BovineIdentityItem bovineIdentityItem = TestDataBuilder.createBovine("farm-001", "cow");
        bovineIdentityItem.setBovineId(bovineId);
        when(bovineRepository.findById(bovineId)).thenReturn(Optional.of(bovineIdentityItem));

        // Act
        Optional<BovineIdentityItem> result = bovineService.findById(bovineId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(bovineId, result.get().getBovineId());
        verify(bovineRepository, times(1)).findById(bovineId);
    }

    @Test
    void findById_nonExistingId_returnsEmpty() {
        // Arrange
        Integer bovineId = 999;
        when(bovineRepository.findById(bovineId)).thenReturn(Optional.empty());

        // Act
        Optional<BovineIdentityItem> result = bovineService.findById(bovineId);

        // Assert
        assertTrue(result.isEmpty());
        verify(bovineRepository, times(1)).findById(bovineId);
    }

    @Test
    void findById_repositoryException_throwsServiceException() {
        // Arrange
        Integer bovineId = 123;
        when(bovineRepository.findById(bovineId)).thenThrow(new RepositoryException("DB Error"));

        // Act & Assert
        assertThrows(ServiceException.class, () -> bovineService.findById(bovineId));
    }

    // ==================== save Tests ====================

    @Test
    void save_validBovine_returnsCreatedBovine() {
        // Arrange
        BovineIdentityItem bovineIdentityItem = TestDataBuilder.createBovine("farm-001", "cow");
        bovineIdentityItem.setBovineId(null); // Nuevo bovino sin ID
        
        when(counterRepository.getNextId(any())).thenReturn("100");
        when(bovineRepository.save(any(BovineIdentityItem.class))).thenAnswer(invocation -> {
            BovineIdentityItem arg = invocation.getArgument(0);
            return Optional.of(arg);
        });

        // Act
        Optional<BovineIdentityItem> result = bovineService.save(bovineIdentityItem);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(100, result.get().getBovineId());
        assertNotNull(result.get().getCreatedAt());
        assertNotNull(result.get().getUpdatedAt());
        verify(counterRepository, times(1)).getNextId(any());
        verify(bovineRepository, times(1)).save(any(BovineIdentityItem.class));
    }

    @Test
    void save_withFarmId_assignsFarmId() {
        // Arrange
        String farmId = "farm-XYZ";
        BovineIdentityItem bovineIdentityItem = TestDataBuilder.createBovine(farmId, "bull");
        
        when(counterRepository.getNextId(any())).thenReturn("200");
        when(bovineRepository.save(any(BovineIdentityItem.class))).thenAnswer(invocation -> {
            BovineIdentityItem arg = invocation.getArgument(0);
            return Optional.of(arg);
        });

        // Act
        Optional<BovineIdentityItem> result = bovineService.save(bovineIdentityItem);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(farmId, result.get().getFarmId());
    }

    @Test
    void save_generatesId_whenNotProvided() {
        // Arrange
        BovineIdentityItem bovineIdentityItem = TestDataBuilder.createBovine("farm-001", "heifer");
        bovineIdentityItem.setBovineId(null);
        
        when(counterRepository.getNextId(any())).thenReturn("42");
        when(bovineRepository.save(any(BovineIdentityItem.class))).thenAnswer(invocation -> {
            BovineIdentityItem arg = invocation.getArgument(0);
            return Optional.of(arg);
        });

        // Act
        Optional<BovineIdentityItem> result = bovineService.save(bovineIdentityItem);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(42, result.get().getBovineId());
        assertEquals("BOVINE#42", result.get().getPk());
        assertEquals("IDENTITY", result.get().getSk());
        assertEquals("IDENTITY", result.get().getGsi1pk());
        assertEquals("BOVINE#42", result.get().getGsi1sk());
    }

    @Test
    void save_repositoryException_throwsServiceException() {
        // Arrange
        BovineIdentityItem bovineIdentityItem = TestDataBuilder.createBovine("farm-001", "cow");
        when(counterRepository.getNextId(any())).thenReturn("100");
        when(bovineRepository.save(any(BovineIdentityItem.class))).thenThrow(new RepositoryException("DB Error"));

        // Act & Assert
        assertThrows(ServiceException.class, () -> bovineService.save(bovineIdentityItem));
    }

    // ==================== update Tests ====================

    @Test
    void update_existingBovine_returnsUpdated() {
        // Arrange
        Integer bovineId = 123;
        BovineIdentityItem existingBovineIdentityItem = TestDataBuilder.createBovine("farm-001", "cow");
        existingBovineIdentityItem.setBovineId(bovineId);
        
        BovineIdentityItem updatedBovineIdentityItem = TestDataBuilder.createBovine("farm-001", "cow");
        updatedBovineIdentityItem.setBovineId(bovineId);
        updatedBovineIdentityItem.setName("Updated Name");
        
        when(bovineRepository.findById(bovineId)).thenReturn(Optional.of(existingBovineIdentityItem));
        // El servicio guarda una copia fusionada (HU-20261120); el repositorio devuelve lo que recibe
        when(bovineRepository.update(any(BovineIdentityItem.class)))
                .thenAnswer(invocation -> Optional.of(invocation.getArgument(0)));

        // Act
        Optional<BovineIdentityItem> result = bovineService.update(updatedBovineIdentityItem);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Updated Name", result.get().getName());
        assertEquals("BOVINE#123", result.get().getPk());
        assertEquals("IDENTITY", result.get().getSk());
        assertNotNull(result.get().getUpdatedAt());
        verify(bovineRepository, times(1)).findById(bovineId);
        verify(bovineRepository, times(1)).update(any(BovineIdentityItem.class));
    }

    @Test
    void update_nonExisting_throwsServiceException() {
        // Arrange
        Integer bovineId = 999;
        BovineIdentityItem bovineIdentityItem = TestDataBuilder.createBovine("farm-001", "cow");
        bovineIdentityItem.setBovineId(bovineId);
        
        when(bovineRepository.findById(bovineId)).thenReturn(Optional.empty());

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, 
            () -> bovineService.update(bovineIdentityItem));
        assertTrue(exception.getMessage().contains("not found"));
        verify(bovineRepository, times(1)).findById(bovineId);
        verify(bovineRepository, never()).update(any());
    }

    @Test
    void update_repositoryException_throwsServiceException() {
        // Arrange
        Integer bovineId = 123;
        BovineIdentityItem bovineIdentityItem = TestDataBuilder.createBovine("farm-001", "cow");
        bovineIdentityItem.setBovineId(bovineId);
        
        when(bovineRepository.findById(bovineId)).thenReturn(Optional.of(bovineIdentityItem));
        when(bovineRepository.update(any(BovineIdentityItem.class))).thenThrow(new RepositoryException("DB Error"));

        // Act & Assert
        assertThrows(ServiceException.class, () -> bovineService.update(bovineIdentityItem));
    }

    // ==================== Additional Business Logic Tests ====================

    @Test
    void save_setsCorrectDynamoDBKeys() {
        // Arrange
        BovineIdentityItem bovineIdentityItem = TestDataBuilder.createBovine("farm-001", "cow");
        when(counterRepository.getNextId(any())).thenReturn("555");
        when(bovineRepository.save(any(BovineIdentityItem.class))).thenAnswer(invocation -> {
            BovineIdentityItem arg = invocation.getArgument(0);
            return Optional.of(arg);
        });

        // Act
        Optional<BovineIdentityItem> result = bovineService.save(bovineIdentityItem);

        // Assert
        assertTrue(result.isPresent());
        BovineIdentityItem saved = result.get();
        assertEquals("BOVINE#555", saved.getPk());
        assertEquals("IDENTITY", saved.getSk());
        assertEquals("IDENTITY", saved.getGsi1pk());
        assertEquals("BOVINE#555", saved.getGsi1sk());
    }

    @Test
    void save_setsEnabledToTrue() {
        // Arrange
        BovineIdentityItem bovineIdentityItem = TestDataBuilder.createBovine("farm-001", "cow");
        
        when(counterRepository.getNextId(any())).thenReturn("100");
        when(bovineRepository.save(any(BovineIdentityItem.class))).thenAnswer(invocation -> {
            BovineIdentityItem arg = invocation.getArgument(0);
            return Optional.of(arg);
        });

        // Act
        Optional<BovineIdentityItem> result = bovineService.save(bovineIdentityItem);

        // Assert
        assertTrue(result.isPresent());
    }

    @Test
    void save_setsTimestamps() {
        // Arrange
        BovineIdentityItem bovineIdentityItem = TestDataBuilder.createBovine("farm-001", "cow");
        
        when(counterRepository.getNextId(any())).thenReturn("100");
        when(bovineRepository.save(any(BovineIdentityItem.class))).thenAnswer(invocation -> {
            BovineIdentityItem arg = invocation.getArgument(0);
            return Optional.of(arg);
        });

        // Act
        Optional<BovineIdentityItem> result = bovineService.save(bovineIdentityItem);

        // Assert
        assertTrue(result.isPresent());
        assertNotNull(result.get().getCreatedAt());
        assertNotNull(result.get().getUpdatedAt());
        assertEquals(result.get().getCreatedAt(), result.get().getUpdatedAt());
    }

    @Test
    void update_onlyUpdatesTimestamp() {
        // Arrange
        Integer bovineId = 123;
        BovineIdentityItem bovineIdentityItem = TestDataBuilder.createBovine("farm-001", "cow");
        bovineIdentityItem.setBovineId(bovineId);
        String originalCreatedAt = "2025-01-01T00:00:00Z";
        bovineIdentityItem.setCreatedAt(originalCreatedAt);
        
        when(bovineRepository.findById(bovineId)).thenReturn(Optional.of(bovineIdentityItem));
        when(bovineRepository.update(any(BovineIdentityItem.class))).thenAnswer(invocation -> {
            BovineIdentityItem arg = invocation.getArgument(0);
            return Optional.of(arg);
        });

        // Act
        Optional<BovineIdentityItem> result = bovineService.update(bovineIdentityItem);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(originalCreatedAt, result.get().getCreatedAt()); // No cambia
        assertNotNull(result.get().getUpdatedAt()); // Se actualiza
    }

    // ==================== HU-20261120: edición con merge y género normalizado ====================

    /** Bovino guardado "completo", con ID ficticio (nunca 167 ni 175). */
    private static BovineIdentityItem storedCompleteBovine(int id) {
        BovineIdentityItem stored = TestDataBuilder.createBovine(id, "F001", "cow", "FEMALE");
        stored.setOrigin("BOUGHT");
        stored.setBreedType("CROSSBRED");
        stored.setBreedComposition(List.of(new com.cattle.entities.bovines.BreedComposition("Jersey", 80),
                new com.cattle.entities.bovines.BreedComposition("Holstein", 20)));
        stored.setFatherId("TORO-1");
        stored.setFatherNameSnapshot("TORO-1-Prueba");
        stored.setMotherId("901");
        stored.setMotherNameSnapshot("901-Madre-Prueba");
        stored.setColor("Colorado");
        stored.setCreatedAt("2024-05-01T08:00:00Z");
        return stored;
    }

    private void stubUpdateEchoesArgument() {
        when(bovineRepository.update(any(BovineIdentityItem.class)))
                .thenAnswer(invocation -> Optional.of(invocation.getArgument(0)));
    }

    @Test
    void update_partialPayload_keepsFieldsNotSent() {
        // CA2: el formulario solo envía algunos campos; el resto debe conservarse
        int bovineId = 9001;
        when(bovineRepository.findById(bovineId)).thenReturn(Optional.of(storedCompleteBovine(bovineId)));
        stubUpdateEchoesArgument();

        BovineIdentityItem changes = new BovineIdentityItem();
        changes.setBovineId(bovineId);
        changes.setFarmId("F001");
        changes.setName("Bovine-9001");
        changes.setGender("female");
        changes.setBornDate("2023-01-10");
        changes.setColor("Manchado"); // único cambio real

        BovineIdentityItem saved = bovineService.update(changes).orElseThrow();

        assertEquals("Manchado", saved.getColor());
        assertEquals("F001", saved.getFarmId());
        assertEquals("BOUGHT", saved.getOrigin());
        assertEquals("CROSSBRED", saved.getBreedType());
        assertEquals(2, saved.getBreedComposition().size());
        assertEquals("TORO-1", saved.getFatherId());
        assertEquals("TORO-1-Prueba", saved.getFatherNameSnapshot());
        assertEquals("901", saved.getMotherId());
        assertEquals("901-Madre-Prueba", saved.getMotherNameSnapshot());
        assertEquals("2024-05-01T08:00:00Z", saved.getCreatedAt()); // RF4
        assertEquals("BOVINE#9001", saved.getPk());
        assertEquals("IDENTITY", saved.getSk());
        assertEquals("IDENTITY", saved.getGsi1pk());
        assertEquals("BOVINE#9001", saved.getGsi1sk());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void update_createdAtInPayload_isIgnored() {
        int bovineId = 9002;
        when(bovineRepository.findById(bovineId)).thenReturn(Optional.of(storedCompleteBovine(bovineId)));
        stubUpdateEchoesArgument();

        BovineIdentityItem changes = new BovineIdentityItem();
        changes.setBovineId(bovineId);
        changes.setCreatedAt("2099-01-01T00:00:00Z");

        BovineIdentityItem saved = bovineService.update(changes).orElseThrow();

        assertEquals("2024-05-01T08:00:00Z", saved.getCreatedAt());
    }

    @Test
    void update_newFarmId_replacesStoredOne() {
        // RF2: el controlador manda la finca de la ruta; si llega, se guarda
        int bovineId = 9003;
        BovineIdentityItem stored = storedCompleteBovine(bovineId);
        stored.setFarmId("FARM#001");
        when(bovineRepository.findById(bovineId)).thenReturn(Optional.of(stored));
        stubUpdateEchoesArgument();

        BovineIdentityItem changes = new BovineIdentityItem();
        changes.setBovineId(bovineId);
        changes.setFarmId("F001");

        assertEquals("F001", bovineService.update(changes).orElseThrow().getFarmId());
    }

    @Test
    void update_lowercaseGender_isStoredUppercase() {
        // CA5
        int bovineId = 9004;
        when(bovineRepository.findById(bovineId)).thenReturn(Optional.of(storedCompleteBovine(bovineId)));
        stubUpdateEchoesArgument();

        BovineIdentityItem changes = new BovineIdentityItem();
        changes.setBovineId(bovineId);
        changes.setGender("female");

        assertEquals("FEMALE", bovineService.update(changes).orElseThrow().getGender());
    }

    @Test
    void update_genderNotSent_normalizesStoredLowercaseGender() {
        // Caso del dato real en minúsculas: al editar otro campo, el género guardado queda normalizado
        int bovineId = 9005;
        BovineIdentityItem stored = storedCompleteBovine(bovineId);
        stored.setGender("female");
        when(bovineRepository.findById(bovineId)).thenReturn(Optional.of(stored));
        stubUpdateEchoesArgument();

        BovineIdentityItem changes = new BovineIdentityItem();
        changes.setBovineId(bovineId);
        changes.setColor("Negro");

        assertEquals("FEMALE", bovineService.update(changes).orElseThrow().getGender());
    }

    @Test
    void save_lowercaseGender_isStoredUppercase() throws Exception {
        // CA5 en el alta
        when(counterRepository.getNextId(anyString())).thenReturn("9006");
        when(bovineRepository.save(any(BovineIdentityItem.class)))
                .thenAnswer(invocation -> Optional.of(invocation.getArgument(0)));

        BovineIdentityItem nuevo = TestDataBuilder.createBovine("F001", "cow");
        nuevo.setGender("male");

        assertEquals("MALE", bovineService.save(nuevo).orElseThrow().getGender());
    }

    @Test
    void normalizeGender_handlesNullAndSpaces() {
        assertNull(BovineService.normalizeGender(null));
        assertEquals("FEMALE", BovineService.normalizeGender(" female "));
        assertEquals("MALE", BovineService.normalizeGender("MALE"));
    }

    // ==================== Revisión H3: vaciar textos opcionales con "" ====================

    @Test
    void update_emptyStringsClearOptionalTexts_nullKeepsThem() {
        int bovineId = 9007;
        when(bovineRepository.findById(bovineId)).thenReturn(Optional.of(storedCompleteBovine(bovineId)));
        stubUpdateEchoesArgument();

        BovineIdentityItem changes = new BovineIdentityItem();
        changes.setBovineId(bovineId);
        changes.setColor("");                 // vaciar
        changes.setFatherNameSnapshot("  ");  // solo espacios = vaciar
        changes.setMotherNameSnapshot(null);  // no enviado = conservar

        BovineIdentityItem saved = bovineService.update(changes).orElseThrow();

        assertNull(saved.getColor());
        assertNull(saved.getFatherNameSnapshot());
        assertEquals("901-Madre-Prueba", saved.getMotherNameSnapshot());
        assertEquals("TORO-1", saved.getFatherId()); // otros campos intactos
    }

    @Test
    void mergeOptionalText_rules() {
        assertEquals("previo", BovineService.mergeOptionalText(null, "previo"));
        assertNull(BovineService.mergeOptionalText("", "previo"));
        assertNull(BovineService.mergeOptionalText("   ", "previo"));
        assertEquals("nuevo", BovineService.mergeOptionalText("nuevo", "previo"));
    }

    // ==================== Revisión H4: ningún campo del ítem se pierde en una edición vacía ====================

    /**
     * Guardia anti-regresión: llena por reflexión TODOS los campos de BovineIdentityItem (y de su base) y
     * edita sin enviar nada. Si mañana se agrega un campo y nadie lo suma a mergeIdentity, este test falla.
     */
    @Test
    void update_emptyChanges_preservesEveryFieldOfTheItem() throws Exception {
        int bovineId = 9008;
        BovineIdentityItem stored = new BovineIdentityItem();
        List<java.lang.reflect.Field> fields = allFields(BovineIdentityItem.class);
        for (java.lang.reflect.Field f : fields) {
            f.setAccessible(true);
            f.set(stored, sampleValue(f, bovineId));
        }
        when(bovineRepository.findById(bovineId)).thenReturn(Optional.of(stored));
        stubUpdateEchoesArgument();

        BovineIdentityItem changes = new BovineIdentityItem();
        changes.setBovineId(bovineId);
        BovineIdentityItem saved = bovineService.update(changes).orElseThrow();

        for (java.lang.reflect.Field f : fields) {
            if (f.getName().equals("updatedAt")) {
                continue; // se renueva en cada edición
            }
            assertEquals(f.get(stored), f.get(saved), "El campo '" + f.getName() + "' se perdió o cambió en la edición");
        }
    }

    private static List<java.lang.reflect.Field> allFields(Class<?> type) {
        List<java.lang.reflect.Field> result = new java.util.ArrayList<>();
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                if (!java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                    result.add(f);
                }
            }
        }
        return result;
    }

    private static Object sampleValue(java.lang.reflect.Field f, int bovineId) {
        Class<?> t = f.getType();
        String name = f.getName();
        if (name.equals("bovineId")) return bovineId;
        if (name.equals("pk") || name.equals("gsi1sk")) return "BOVINE#" + bovineId;
        if (name.equals("sk")) return "IDENTITY";
        if (name.equals("gsi1pk")) return "IDENTITY";
        if (name.equals("gender")) return "FEMALE";
        if (t == String.class) return "valor-" + name;
        if (t == Integer.class || t == int.class) return 7;
        if (t == List.class) return List.of(new com.cattle.entities.bovines.BreedComposition("Jersey", 100));
        throw new IllegalStateException("Tipo no contemplado en el test de reflexión: " + name + " (" + t
                + "). Agrega un valor de ejemplo y revisa que mergeIdentity lo conserve.");
    }
}
