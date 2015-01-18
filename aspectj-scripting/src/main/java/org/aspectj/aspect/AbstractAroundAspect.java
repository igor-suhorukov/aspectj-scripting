package org.aspectj.aspect;

import org.aspectj.aspect.lifecycle.BaseAspectLifecycle;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 *
 */
@Aspect
public abstract class AbstractAroundAspect extends BaseAspectLifecycle {

    @Pointcut
    abstract void pointcutExpression();

    @Around("pointcutExpression()")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        return processAround(proceedingJoinPoint);
    }
}
