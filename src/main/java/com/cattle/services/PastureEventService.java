package com.cattle.services;

import com.cattle.config.LambdaContext;
import com.cattle.enums.LogType;
import com.cattle.events.entities.PastureEventItem;
import com.cattle.exceptions.ProcessingException;
import com.cattle.exceptions.RepositoryException;
import com.cattle.exceptions.ServiceException;
import com.cattle.repository.PastureEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}