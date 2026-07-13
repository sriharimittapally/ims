package com.infosys.ims.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    /**
     * Pointcut that matches all Spring MVC Controllers in your package.
     */
    @Pointcut("within(com.infosys.ims.controller..*)")
    public void controllerPointcut() {
        // Method is empty as this is just a Pointcut definition
    }

    /**
     * Pointcut that matches all Services in your package.
     */
    @Pointcut("within(com.infosys.ims.service..*)")
    public void servicePointcut() {
        // Method is empty as this is just a Pointcut definition
    }

    /**
     * Around Advice: Logs request execution, method parameters, return values, 
     * and execution duration for all service and controller layer methods.
     */
    @Around("controllerPointcut() || servicePointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String arguments = Arrays.toString(joinPoint.getArgs());

        log.info("Entering: {}.{}() with arguments = {}", className, methodName, arguments);
        
        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - start;
            
            log.info("Exiting: {}.{}() with result = {} [Execution time: {} ms]", 
                    className, methodName, result, executionTime);
            return result;
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument: {} in {}.{}()", arguments, className, methodName);
            throw e;
        }
    }

    /**
     * AfterThrowing Advice: Intercepts and logs any unhandled exceptions 
     * thrown from the controllers or services.
     */
    @AfterThrowing(pointcut = "controllerPointcut() || servicePointcut()", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        log.error("Exception in {}.{}() with cause = {}", 
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(), 
                exception.getCause() != null ? exception.getCause() : "NULL");
    }
}
