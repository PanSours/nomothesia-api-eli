//package com.di.nomothesia.util;
//
//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.annotation.AfterReturning;
//import org.aspectj.lang.annotation.AfterThrowing;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Before;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//
//import java.util.Arrays;
//
///**
// * Defines the tracing aspect which will be activated on entrance/exit/exception of all
// * <b>Spring-managed</b> class methods which have the {@link Loggable} annotation declared.
// *
// * Created by psour on 16/11/2016.
// */
//@Aspect
//@Component
//public class LogAspect {
//
//    private static final String MSG_BEGIN = "() BEGIN ";
//    private static final String MSG_END = "() END ";
//    private static final String MSG_EXCEPTION = "() EXCEPTION ";
//
//    /**
//     * Invoked at the beginning of the logged method.
//     *
//     * @param joinPoint Join point information
//     * @param loggable Loggable annotation
//     */
//    @Before (value="@annotation(loggable)", argNames= "joinPoint,loggable")
//    public void before(JoinPoint joinPoint, Loggable loggable) {
//        Class<? extends Object> clazz = joinPoint.getTarget().getClass();
//        Logger log = LoggerFactory.getLogger(clazz);
//
//        if (log.isInfoEnabled()) {
//            StringBuffer logMsg = new StringBuffer(clazz.getSimpleName())
//                    .append(".")
//                    .append(joinPoint.getSignature().getName())
//                    .append(MSG_BEGIN);
//
//            if (loggable.arguments()) {
//                logMsg.append(Arrays.deepToString(joinPoint.getArgs()));
//            }
//
//            log.info(logMsg.toString());
//        }
//    }
//
//    /**
//     * Invoked at the end of the logged method.
//     *
//     * @param joinPoint Join point information
//     * @param loggable Loggable annotation
//     * @param returnValue Value being returned by the logged method
//     */
//    @AfterReturning (pointcut="@annotation(loggable)", returning="returnValue")
//    public void afterReturning(JoinPoint joinPoint, Loggable loggable, Object returnValue) {
//        Class<? extends Object> clazz = joinPoint.getTarget().getClass();
//        Logger log = LoggerFactory.getLogger(clazz);
//
//        if (log.isInfoEnabled()) {
//            StringBuffer logMsg = new StringBuffer(clazz.getSimpleName())
//                    .append(".")
//                    .append(joinPoint.getSignature().getName())
//                    .append(MSG_END);
//
//            if (loggable.result()) {
//                logMsg.append(returnValue);
//            }
//
//            log.info(logMsg.toString());
//        }
//    }
//
//    /**
//     * Invoked when the traced method throws an exception.
//     *
//     * @param joinPoint Join point information
//     * @param loggable Loggable annotation
//     * @param throwable Value being returned by the logged method
//     */
//    @AfterThrowing (pointcut="@annotation(loggable)", throwing="throwable")
//    public void afterThrowing(JoinPoint joinPoint, Loggable loggable, Throwable throwable) {
//        Class<? extends Object> clazz = joinPoint.getTarget().getClass();
//        Logger log = LoggerFactory.getLogger(clazz);
//
//        if (log.isInfoEnabled()) {
//            StringBuffer logMsg = new StringBuffer(clazz.getSimpleName())
//                    .append(".")
//                    .append(joinPoint.getSignature().getName())
//                    .append(MSG_EXCEPTION);
//
//            log.error(logMsg.toString(), throwable);
//        }
//    }
//}