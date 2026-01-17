package com.enterprise.shop.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Service Call Logging Aspect
 * Logs all outgoing calls from shop-management to product-stock service
 */
@Aspect
@Component
@Order(2)
public class ServiceCallLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger("SERVICE_CALL_LOGGER");
    private final ObjectMapper objectMapper;

    public ServiceCallLoggingAspect(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Pointcut("execution(* com.enterprise.shop.client..*(..))")
    public void clientMethodPointcut() {}

    @Pointcut("execution(* com.enterprise.shop.service..*Client*.*(..))")
    public void serviceClientPointcut() {}

    /**
     * Logs all outgoing service calls
     */
    @Around("clientMethodPointcut() || serviceClientPointcut()")
    public Object logServiceCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String traceId = MDC.get("traceId");
        if (traceId == null) {
            traceId = "SVC-" + System.currentTimeMillis();
        }

        Instant startTime = Instant.now();
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();

        // Log outgoing request
        Map<String, Object> requestLog = new LinkedHashMap<>();
        requestLog.put("type", "SERVICE_REQUEST");
        requestLog.put("traceId", traceId);
        requestLog.put("timestamp", startTime.toString());
        requestLog.put("target", "product-stock");
        requestLog.put("method", methodName);
        requestLog.put("arguments", sanitizeArgs(args));

        logger.info(objectMapper.writeValueAsString(requestLog));

        Object result = null;
        Throwable exception = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            exception = ex;
            throw ex;
        } finally {
            Instant endTime = Instant.now();
            long executionTimeMs = ChronoUnit.MILLIS.between(startTime, endTime);

            // Log response
            Map<String, Object> responseLog = new LinkedHashMap<>();
            responseLog.put("type", "SERVICE_RESPONSE");
            responseLog.put("traceId", traceId);
            responseLog.put("timestamp", endTime.toString());
            responseLog.put("target", "product-stock");
            responseLog.put("method", methodName);
            responseLog.put("executionTimeMs", executionTimeMs);

            if (exception != null) {
                responseLog.put("status", "ERROR");
                responseLog.put("exception", Map.of(
                        "type", exception.getClass().getSimpleName(),
                        "message", exception.getMessage() != null ? exception.getMessage() : "No message"
                ));
                logger.error(objectMapper.writeValueAsString(responseLog));
            } else {
                responseLog.put("status", "SUCCESS");
                responseLog.put("response", sanitizeResult(result));
                logger.info(objectMapper.writeValueAsString(responseLog));
            }
        }
    }

    private Object sanitizeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "none";
        }
        try {
            String json = objectMapper.writeValueAsString(args);
            if (json.length() > 5000) {
                return json.substring(0, 5000) + "... [TRUNCATED]";
            }
            return objectMapper.readValue(json, Object.class);
        } catch (Exception e) {
            return "Unable to serialize";
        }
    }

    private Object sanitizeResult(Object result) {
        if (result == null) {
            return "null";
        }
        try {
            String json = objectMapper.writeValueAsString(result);
            if (json.length() > 5000) {
                return json.substring(0, 5000) + "... [TRUNCATED]";
            }
            return objectMapper.readValue(json, Object.class);
        } catch (Exception e) {
            return result.toString();
        }
    }
}
