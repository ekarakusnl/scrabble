package com.gamecity.scrabble.aspect;

import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Aspect configuration to monitor controller calls
 * 
 * @author ekarakus
 */
@Aspect
@Component
@Slf4j
public class ControllerAspect {

    /**
     * Defines a pointcut for the given package
     */
    @Pointcut("execution(* com.gamecity.scrabble.controller..*(..))")
    public void controller() {
        // nothing to do
    }

    /**
     * Logs the method invocation
     * 
     * @param joinPoint join point of the method invocation
     * @return the jointPoint
     * @throws Throwable
     */
    @Around("controller()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        long elapsedTime = System.currentTimeMillis() - start;
        log.debug("Method {}.{}({}) -> execution time : {} ms", className, methodName,
                Arrays.toString(joinPoint.getArgs()), elapsedTime);
        return joinPoint.proceed();
    }

    /**
     * Logs the error when an exception is thrown
     * 
     * @param joinPoint join point of the exception
     * @param exception the exception
     */
    @AfterThrowing(pointcut = "controller()", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        log.error("An exception has been thrown in " + joinPoint.getSignature().getName() + " ()");
        log.error("Cause : {}, {}", exception.getMessage(), exception);
    }

}
