package org.aspectj.aspect;

import org.aspectj.aspect.lifecycle.BaseAspectLifecycle;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 *
 */
@Aspect
public abstract class AbstractAfterThrowingAspect extends BaseAspectLifecycle {

    @Pointcut
    abstract void pointcutExpression();

    @AfterThrowing(pointcut = "pointcutExpression()", throwing = "exception")
    public void afterThrowing(JoinPoint joinPoint, Throwable exception) throws Throwable {
        executeProcessWithException(joinPoint, exception);
    }
}
