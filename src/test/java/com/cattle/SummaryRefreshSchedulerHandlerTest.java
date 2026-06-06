package com.cattle;

import com.amazonaws.services.lambda.runtime.Context;
import com.cattle.config.LambdaContext;
import com.cattle.enums.LogType;
import com.cattle.services.BovineSummaryService;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SummaryRefreshSchedulerHandlerTest {

//    @Test
//    void handleRequest_returnsCountWhenRefreshSucceeds() {
//        BovineSummaryService bovineSummaryService = mock(BovineSummaryService.class);
//        LambdaContext lambdaContext = mock(LambdaContext.class);
//        Context awsContext = mock(Context.class);
//        when(awsContext.getAwsRequestId()).thenReturn("req-123");
//        when(bovineSummaryService.refreshAllSummaries()).thenReturn(17);
//
//        SummaryRefreshSchedulerHandler handler = new SummaryRefreshSchedulerHandler(bovineSummaryService, lambdaContext);
//
//        Map<String, Object> result = handler.handleRequest(Map.of("source", "aws.scheduler"), awsContext);
//
//        assertEquals("Scheduled summary refresh completed successfully", result.get("message"));
//        assertEquals(17, result.get("count"));
//        assertEquals("req-123", result.get("requestId"));
//        assertEquals("eventbridge-scheduler", result.get("trigger"));
//        verify(lambdaContext).logInfo(eq(LogType.SERVICE), contains("Scheduled summary refresh started"));
//        verify(lambdaContext).logInfo(eq(LogType.SERVICE), contains("updated=17"));
//    }
//
//    @Test
//    void handleRequest_rethrowsWhenRefreshFails() {
//        BovineSummaryService bovineSummaryService = mock(BovineSummaryService.class);
//        LambdaContext lambdaContext = mock(LambdaContext.class);
//        Context awsContext = mock(Context.class);
//        IllegalStateException failure = new IllegalStateException("boom");
//
//        when(awsContext.getAwsRequestId()).thenReturn("req-456");
//        when(bovineSummaryService.refreshAllSummaries()).thenThrow(failure);
//
//        SummaryRefreshSchedulerHandler handler = new SummaryRefreshSchedulerHandler(bovineSummaryService, lambdaContext);
//
//        IllegalStateException exception = assertThrows(IllegalStateException.class,
//                () -> handler.handleRequest(Map.of(), awsContext));
//
//        assertEquals("boom", exception.getMessage());
//        verify(lambdaContext).logException(eq(LogType.SERVICE), contains("Scheduled summary refresh failed"), eq(failure));
//    }
}