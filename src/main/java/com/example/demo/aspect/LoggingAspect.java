package com.example.demo.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    /**
     * Pointcut for all service layer methods
     */
    @Pointcut("within(com.example.demo.service..*)")
    public void serviceLayer() {
    }

    /**
     * Pointcut for all controller layer methods
     */
    @Pointcut("within(com.example.demo.controller..*)")
    public void controllerLayer() {
    }

    /**
     * Pointcut for all repository layer methods
     */
    @Pointcut("within(com.example.demo.repository..*)")
    public void repositoryLayer() {
    }

    /**
     * Around advice for service layer methods
     */
    @Around("serviceLayer()")
    public Object logServiceLayer(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        log.info("Entering {}.{}()", className, methodName);

        try {
            Object result = joinPoint.proceed();
            stopWatch.stop();
            log.info("Exiting {}.{}() - Execution time: {} ms", className, methodName, stopWatch.getTotalTimeMillis());
            return result;
        } catch (IllegalArgumentException e) {
            stopWatch.stop();
            log.error("Illegal argument in {}.{}(): {}", className, methodName, e.getMessage());
            throw e;
        } catch (Exception e) {
            stopWatch.stop();
            log.error("Exception in {}.{}(): {} - Execution time: {} ms",
                    className, methodName, e.getMessage(), stopWatch.getTotalTimeMillis());
            throw e;
        }
    }

    /**
     * Around advice for controller layer methods
     */
    @Around("controllerLayer()")
    public Object logControllerLayer(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        // Log request details
        Object[] args = joinPoint.getArgs();
        log.info("REST Request: {}.{}[args={}]", className, methodName, args);

        try {
            Object result = joinPoint.proceed();
            stopWatch.stop();
            log.info("REST Response: {}.{}[status=OK, time={} ms]",
                    className, methodName, stopWatch.getTotalTimeMillis());
            return result;
        } catch (Exception e) {
            stopWatch.stop();
            log.error("REST Exception: {}.{}[exception={}, time={} ms]",
                    className, methodName, e.getMessage(), stopWatch.getTotalTimeMillis());
            throw e;
        }
    }

    /**
     * Around advice for repository layer methods
     */
    @Around("repositoryLayer()")
    public Object logRepositoryLayer(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        log.debug("DB Operation: {}.{}[args={}]", className, methodName, joinPoint.getArgs());

        try {
            Object result = joinPoint.proceed();
            stopWatch.stop();
            log.debug("DB Operation Completed: {}.{}()[return={}, time={} ms]",
                    className, methodName, result, stopWatch.getTotalTimeMillis());
            return result;
        } catch (Exception e) {
            stopWatch.stop();
            log.error("DB Exception: {}.{}[exception={}, time={} ms]",
                    className, methodName, e.getMessage(), stopWatch.getTotalTimeMillis());
            throw e;
        }
    }
}