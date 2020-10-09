package com.revature.aspects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AspectLogger {
    private static final Logger log = LogManager.getLogger(AspectLogger.class);
    
    @AfterThrowing(pointcut = "within(com.revature.*)", throwing = "e")
    public void logException(JoinPoint jp, Exception e) throws Throwable {
        log.trace("EXCEPTION THROWN FOR {} ON {}", jp.getTarget(), jp.getSignature());
        log.trace("STACK TRACE", e);
    }
    
    // ------------------ LOG CRUD METHODS -------------------------
    
    // add methods that use save are set to return a copy of the persistent object
    // logging for those not done with aspects
    
    @AfterReturning(pointcut = "execution(* *..update(..))", returning = "ret")
    public void logInsert(JoinPoint jp, boolean ret) {
        log.info("{} CALLED {} FOR {}", jp.getTarget(), jp.getSignature(), jp.getArgs()[0]);
        log.info("RETURNED: {}", ret);
    }
    
    @AfterReturning(pointcut = "execution(* *..delete(..))", returning = "ret")
    public void logDelete(JoinPoint jp, boolean ret) {
        log.info("{} CALLED {} FOR {}", jp.getTarget(), jp.getSignature(), jp.getArgs()[0]);
        log.info("RETURNED: {}", ret);
    }
    
}
