package org.aspectj.aspect;

import org.aspectj.aspect.lifecycle.BaseAspectLifecycle;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 *
 */
@Aspect
public abstract class AbstractAfterAspect extends BaseAspectLifecycle {

    @Pointcut
    abstract void pointcutExpression();

    @After("pointcutExpression()")
    public void after(JoinPoint joinPoint) throws Throwable {
        process(joinPoint);
    }
}
