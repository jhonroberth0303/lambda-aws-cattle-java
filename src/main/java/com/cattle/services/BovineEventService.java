package com.cattle.services;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.BovineEventHistoryItemDTO;
import com.cattle.events.entities.BovineEventItem;
import com.cattle.enums.LogType;
import com.cattle.exceptions.ProcessingException;
import com.cattle.exceptions.RepositoryException;
import com.cattle.exceptions.ServiceException;
import com.cattle.repository.BovineEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BovineEventService {

    private final BovineEventRepository bovineEventRepository;
    private final LambdaContext lambdaContext;

    public void save(BovineEventItem item) {
        try {
            bovineEventRepository.save(item);
        } catch (RepositoryException ex) {
            lambdaContext.logException(LogType.SERVICE, "Repository error saving bovine event", ex);
            throw new ServiceException("Repository error saving bovine event", ex);
        } catch (Exception ex) {
            lambdaContext.logException(LogType.SERVICE, "Unexpected error saving bovine event", ex);
            throw new ProcessingException("Unexpected error saving bovine event", ex);
        }
    }

    public List<BovineEventHistoryItemDTO> findByBovine(String farmId, String bovineId, int limit) {
        try {
            return bovineEventRepository.findByBovine(bovineId, limit)
                    .stream()
                    .filter(item -> farmId.equals(item.getFarmId()))
                    .map(item -> BovineEventHistoryItemDTO.builder()
                            .eventId(item.getEventId())
                            .eventType(item.getEventType())
                            .eventAt(item.getEventAt() != null ? item.getEventAt().toString() : null)
                            .createdBy(item.getCreatedBy())
                            .notes(item.getNotes())
                            .payloadJson(item.getPayloadJson())
                            .build())
                    .toList();
        } catch (RepositoryException ex) {
            lambdaContext.logException(LogType.SERVICE, "Repository error querying bovine events", ex);
            throw new ServiceException("Repository error querying bovine events", ex);
        } catch (Exception ex) {
            lambdaContext.logException(LogType.SERVICE, "Unexpected error querying bovine events", ex);
            throw new ProcessingException("Unexpected error querying bovine events", ex);
        }
    }
}
