package org.aspectj.aspect;

import org.aspectj.aspect.lifecycle.BaseAspectLifecycle;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

/**
 *
 */
@Aspect
public abstract class AbstractBeforeAspect extends BaseAspectLifecycle {

    @Pointcut
    abstract void pointcutExpression();

    @Before("pointcutExpression()")
    public void before(JoinPoint joinPoint) throws Throwable {
        process(joinPoint);
    }
}
