package com.enterprise.shop.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Centralized Logging Aspect for Full Fidelity Request/Response Logging
 * 
 * Captures:
 * - Endpoint URL
 * - HTTP Method
 * - Headers
 * - Request Payload
 * - Response Body
 * - Execution Time
 * - Trace IDs
 * 
 * Log Path: /var/log/shop-app/
 */
@Aspect
@Component
@Order(1)
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);
    private static final Logger requestLogger = LoggerFactory.getLogger("REQUEST_LOGGER");
    private static final Logger responseLogger = LoggerFactory.getLogger("RESPONSE_LOGGER");
    
    private final ObjectMapper objectMapper;
    
    // Headers to mask for security
    private static final Set<String> SENSITIVE_HEADERS = Set.of(
            "authorization", "x-api-key", "api-key", "cookie", "set-cookie"
    );

    public LoggingAspect(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void restControllerPointcut() {}

    @Pointcut("within(@org.springframework.stereotype.Controller *)")
    public void controllerPointcut() {}

    @Pointcut("execution(* com.enterprise.shop.controller..*(..))")
    public void controllerExecutionPointcut() {}

    @Pointcut("@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
              "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
              "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
              "@annotation(org.springframework.web.bind.annotation.PatchMapping) || " +
              "@annotation(org.springframework.web.bind.annotation.DeleteMapping) || " +
              "@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public void httpMethodPointcut() {}

    /**
     * Around advice for all REST controller methods
     * Logs full request/response details with timing
     */
    @Around("(restControllerPointcut() || controllerPointcut()) && httpMethodPointcut()")
    public Object logAroundController(ProceedingJoinPoint joinPoint) throws Throwable {
        String traceId = generateTraceId();
        MDC.put("traceId", traceId);
        
        Instant startTime = Instant.now();
        HttpServletRequest request = getCurrentRequest();
        
        LogContext logContext = new LogContext();
        logContext.setTraceId(traceId);
        logContext.setTimestamp(startTime.toString());
        
        // Log Request
        logRequest(joinPoint, request, logContext);
        
        Object result = null;
        Throwable caughtException = null;
        
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            caughtException = ex;
            throw ex;
        } finally {
            Instant endTime = Instant.now();
            long executionTimeMs = ChronoUnit.MILLIS.between(startTime, endTime);
            
            // Log Response
            logResponse(result, caughtException, executionTimeMs, logContext);
            
            MDC.clear();
        }
    }

    /**
     * Logs incoming request details
     */
    private void logRequest(ProceedingJoinPoint joinPoint, HttpServletRequest request, LogContext logContext) {
        try {
            if (request != null) {
                logContext.setMethod(request.getMethod());
                logContext.setUrl(buildFullUrl(request));
                logContext.setRemoteAddr(request.getRemoteAddr());
                logContext.setHeaders(extractHeaders(request));
                logContext.setQueryParams(extractQueryParams(request));
            }
            
            // Extract method info
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            logContext.setControllerClass(signature.getDeclaringType().getSimpleName());
            logContext.setMethodName(signature.getName());
            
            // Extract request body from method arguments
            Object[] args = joinPoint.getArgs();
            String[] paramNames = signature.getParameterNames();
            Map<String, Object> requestBody = new LinkedHashMap<>();
            
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg != null && !isExcludedType(arg)) {
                    String paramName = (paramNames != null && i < paramNames.length) 
                            ? paramNames[i] : "arg" + i;
                    requestBody.put(paramName, sanitizeForLogging(arg));
                }
            }
            
            logContext.setRequestPayload(requestBody);
            
            // Log as structured JSON
            String requestLogJson = objectMapper.writeValueAsString(Map.of(
                    "type", "REQUEST",
                    "traceId", logContext.getTraceId(),
                    "timestamp", logContext.getTimestamp(),
                    "http", Map.of(
                            "method", logContext.getMethod() != null ? logContext.getMethod() : "UNKNOWN",
                            "url", logContext.getUrl() != null ? logContext.getUrl() : "UNKNOWN",
                            "remoteAddr", logContext.getRemoteAddr() != null ? logContext.getRemoteAddr() : "UNKNOWN"
                    ),
                    "headers", logContext.getHeaders() != null ? logContext.getHeaders() : Collections.emptyMap(),
                    "queryParams", logContext.getQueryParams() != null ? logContext.getQueryParams() : Collections.emptyMap(),
                    "controller", logContext.getControllerClass() + "." + logContext.getMethodName(),
                    "requestPayload", logContext.getRequestPayload()
            ));
            
            requestLogger.info(requestLogJson);
            
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize request log: {}", e.getMessage());
        }
    }

    /**
     * Logs response details
     */
    private void logResponse(Object result, Throwable exception, long executionTimeMs, LogContext logContext) {
        try {
            HttpServletResponse response = getCurrentResponse();
            int statusCode = 200;
            
            if (response != null) {
                statusCode = response.getStatus();
            }
            
            if (exception != null) {
                statusCode = 500;
            }
            
            Map<String, Object> responseLog = new LinkedHashMap<>();
            responseLog.put("type", "RESPONSE");
            responseLog.put("traceId", logContext.getTraceId());
            responseLog.put("timestamp", Instant.now().toString());
            responseLog.put("executionTimeMs", executionTimeMs);
            responseLog.put("statusCode", statusCode);
            responseLog.put("controller", logContext.getControllerClass() + "." + logContext.getMethodName());
            
            if (exception != null) {
                responseLog.put("exception", Map.of(
                        "type", exception.getClass().getSimpleName(),
                        "message", exception.getMessage() != null ? exception.getMessage() : "No message"
                ));
            } else if (result != null) {
                responseLog.put("responseBody", sanitizeForLogging(result));
            }
            
            String responseLogJson = objectMapper.writeValueAsString(responseLog);
            
            if (exception != null) {
                responseLogger.error(responseLogJson);
            } else {
                responseLogger.info(responseLogJson);
            }
            
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize response log: {}", e.getMessage());
        }
    }

    /**
     * Builds full URL including query string
     */
    private String buildFullUrl(HttpServletRequest request) {
        StringBuilder url = new StringBuilder();
        url.append(request.getRequestURL().toString());
        
        String queryString = request.getQueryString();
        if (queryString != null && !queryString.isEmpty()) {
            url.append("?").append(queryString);
        }
        
        return url.toString();
    }

    /**
     * Extracts and masks sensitive headers
     */
    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new LinkedHashMap<>();
        
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames != null && headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            
            // Mask sensitive headers
            if (SENSITIVE_HEADERS.contains(headerName.toLowerCase())) {
                headerValue = "***MASKED***";
            }
            
            headers.put(headerName, headerValue);
        }
        
        return headers;
    }

    /**
     * Extracts query parameters
     */
    private Map<String, String> extractQueryParams(HttpServletRequest request) {
        return request.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> String.join(",", e.getValue()),
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));
    }

    /**
     * Sanitizes objects for logging (handles large payloads)
     */
    private Object sanitizeForLogging(Object obj) {
        try {
            String json = objectMapper.writeValueAsString(obj);
            
            // Truncate large payloads
            if (json.length() > 10000) {
                return json.substring(0, 10000) + "... [TRUNCATED]";
            }
            
            return objectMapper.readValue(json, Object.class);
        } catch (JsonProcessingException e) {
            return obj.toString();
        }
    }

    /**
     * Checks if type should be excluded from logging
     */
    private boolean isExcludedType(Object obj) {
        return obj instanceof HttpServletRequest ||
               obj instanceof HttpServletResponse ||
               obj instanceof org.springframework.web.multipart.MultipartFile ||
               obj instanceof java.io.InputStream ||
               obj instanceof java.io.OutputStream;
    }

    /**
     * Generates unique trace ID
     */
    private String generateTraceId() {
        return "SHOP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Gets current HTTP request from context
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = 
                    (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attributes.getRequest();
        } catch (IllegalStateException e) {
            return null;
        }
    }

    /**
     * Gets current HTTP response from context
     */
    private HttpServletResponse getCurrentResponse() {
        try {
            ServletRequestAttributes attributes = 
                    (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attributes.getResponse();
        } catch (IllegalStateException e) {
            return null;
        }
    }

    /**
     * Internal class to hold logging context
     */
    private static class LogContext {
        private String traceId;
        private String timestamp;
        private String method;
        private String url;
        private String remoteAddr;
        private Map<String, String> headers;
        private Map<String, String> queryParams;
        private String controllerClass;
        private String methodName;
        private Map<String, Object> requestPayload;

        // Getters and Setters
        public String getTraceId() { return traceId; }
        public void setTraceId(String traceId) { this.traceId = traceId; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getRemoteAddr() { return remoteAddr; }
        public void setRemoteAddr(String remoteAddr) { this.remoteAddr = remoteAddr; }
        public Map<String, String> getHeaders() { return headers; }
        public void setHeaders(Map<String, String> headers) { this.headers = headers; }
        public Map<String, String> getQueryParams() { return queryParams; }
        public void setQueryParams(Map<String, String> queryParams) { this.queryParams = queryParams; }
        public String getControllerClass() { return controllerClass; }
        public void setControllerClass(String controllerClass) { this.controllerClass = controllerClass; }
        public String getMethodName() { return methodName; }
        public void setMethodName(String methodName) { this.methodName = methodName; }
        public Map<String, Object> getRequestPayload() { return requestPayload; }
        public void setRequestPayload(Map<String, Object> requestPayload) { this.requestPayload = requestPayload; }
    }
}
