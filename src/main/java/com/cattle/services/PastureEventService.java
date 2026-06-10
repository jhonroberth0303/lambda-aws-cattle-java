package com.cattle.services;

import com.cattle.config.LambdaContext;
import com.cattle.dtos.PastureEventHistoryItemDTO;
import com.cattle.enums.LogType;
import com.cattle.events.entities.PastureEventItem;
import com.cattle.exceptions.ProcessingException;
import com.cattle.exceptions.RepositoryException;
import com.cattle.exceptions.ServiceException;
import com.cattle.repository.PastureEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PastureEventService {
    private final PastureEventRepository pastureEventRepository;
    private final LambdaContext lambdaContext;

    public void save(PastureEventItem item) {
        try {
            pastureEventRepository.save(item);
        } catch (RepositoryException ex) {
            lambdaContext.logException(LogType.SERVICE, "Repository error saving pasture event", ex);
            throw new ServiceException("Repository error saving pasture event", ex);
        } catch (Exception ex) {
            lambdaContext.logException(LogType.SERVICE, "Unexpected error saving pasture event", ex);
            throw new ProcessingException("Unexpected error saving pasture event", ex);
        }
    }

    public List<PastureEventHistoryItemDTO> findByPasture(String farmId, String pastureId, int limit) {
        try {
            return pastureEventRepository.findByPasture(pastureId, limit)
                    .stream()
                    .filter(item -> farmId.equals(item.getFarmId()))
                    .map(item -> PastureEventHistoryItemDTO.builder()
                            .eventId(item.getEventId())
                            .eventType(item.getEventType())
                            .eventAt(item.getEventAt() != null ? item.getEventAt().toString() : null)
                            .createdBy(item.getCreatedBy())
                            .notes(item.getNotes())
                            .payloadJson(item.getPayloadJson())
                            .build())
                    .toList();
        } catch (RepositoryException ex) {
            lambdaContext.logException(LogType.SERVICE, "Repository error querying pasture events", ex);
            throw new ServiceException("Repository error querying pasture events", ex);
        } catch (Exception ex) {
            lambdaContext.logException(LogType.SERVICE, "Unexpected error querying pasture events", ex);
            throw new ProcessingException("Unexpected error querying pasture events", ex);
        }
    }
}