package com.cattle.config;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.LambdaRuntime;
import com.cattle.enums.LogType;
import org.springframework.stereotype.Component;

@Component
public final class LambdaContext {

    private static final LambdaLogger LOGGER = LambdaRuntime.getLogger();

    public void logInfo(LogType type, String message) {
        LOGGER.log(format("INFO", type.name(), message, null));
    }

    public void logException(LogType type, String message) {
        LOGGER.log(format("ERROR", type.name(), message, null));
    }

    public void logException(LogType type, String message, Exception ex) {
        LOGGER.log(format("ERROR", type.name(), message, ex));
    }

    private String format(String level, String type, String message, Exception ex) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(level).append("] ");
        if (type != null && !type.isEmpty()) {
            sb.append(type).append(": ");
        }
        sb.append(message);
        if (ex != null) {
            sb.append("\n").append(getTrace(ex));
        }
        return sb.toString();
    }

    private String getTrace(Exception ex) {
        StringBuilder traceBuilder = new StringBuilder();
        for (StackTraceElement element : ex.getStackTrace()) {
            traceBuilder.append(element).append("\n");
        }
        return traceBuilder.toString();
    }
}