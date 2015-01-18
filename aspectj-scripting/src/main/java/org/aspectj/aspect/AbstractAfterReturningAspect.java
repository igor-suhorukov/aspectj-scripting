package org.aspectj.aspect;

import org.aspectj.aspect.lifecycle.BaseAspectLifecycle;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 *
 */
@Aspect
public abstract class AbstractAfterReturningAspect extends BaseAspectLifecycle {

    @Pointcut
    abstract void pointcutExpression();

    @AfterReturning("pointcutExpression()")
    public void afterReturning(JoinPoint joinPoint) throws Throwable {
        process(joinPoint);
    }
}
