package org.aspectj.aspect.lifecycle;

import org.aspectj.configuration.AspectJDescriptor;
import org.aspectj.configuration.model.Expression;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.util.Utils;
import org.mvel2.MVEL;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.integration.impl.MapVariableResolverFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public abstract class BaseAspectLifecycle {

    public static final String JOIN_POINT_VARIABLE = "joinPoint";

    private final org.aspectj.configuration.model.Aspect aspect;

    protected final Serializable processScript;

    protected VariableResolverFactory resolverFactory = new MapVariableResolverFactory(new HashMap<String, Object>());

    protected BaseAspectLifecycle() {
        String aspectName = getClass().getName();
        aspect = AspectJDescriptor.getConfiguration().getAspect(aspectName);
        if(Expression.isNotEmptyExpression(aspect.getInit())){
            executeLifecycleExpr(aspect.getInit());
        }
        if(Expression.isNotEmptyExpression(aspect.getProcess())){
            processScript = Utils.compileMvelExpression(aspect.getProcess().getExpression());
        } else {
            processScript = null;
        }
        if(Expression.isNotEmptyExpression(aspect.getDispose())){
            Runtime.getRuntime().addShutdownHook(new Thread(){
                @Override
                public void run() {
                    executeLifecycleExpr(aspect.getDispose());
                }
            });
        }
    }

    protected Object processAround(ProceedingJoinPoint pjp) throws Throwable {
        if(processScript ==null){
            return pjp.proceed();
        } else {
            return process(pjp);
        }
    }

    protected Object process(JoinPoint pjp) throws Throwable {
        if(processScript ==null) return null;
        VariableResolverFactory variableResolverFactory = createProcessVariableResolver(pjp);
        return Utils.executeMvelExpression(processScript, variableResolverFactory);
    }

    protected void executeProcessWithException(JoinPoint joinPoint, Throwable exception) throws Throwable {
        if(processScript ==null) return;
        VariableResolverFactory variableResolverFactory = createProcessVariableResolver(joinPoint);
        variableResolverFactory.createVariable("exception", exception);
        Utils.executeMvelExpression(processScript, variableResolverFactory);
    }

    private VariableResolverFactory createProcessVariableResolver(JoinPoint pjp) {
        VariableResolverFactory variableResolverFactory = new MapVariableResolverFactory();
        fillResolveParams(aspect.getProcess().getResultParams(), variableResolverFactory);
        variableResolverFactory.createVariable(JOIN_POINT_VARIABLE, pjp);
        variableResolverFactory.setNextFactory(resolverFactory);
        return variableResolverFactory;
    }

    private void executeLifecycleExpr(Expression expression) {
        fillResolveParams(expression.getResultParams(), resolverFactory);
        MVEL.eval(expression.getExpression(), resolverFactory);
    }

    private void fillResolveParams(Map<String, Object> resultParams, VariableResolverFactory resolverFactory1) {
        if(resultParams!=null && resultParams.size()>0){
            for(Map.Entry<String,Object> entry: resultParams.entrySet()){
                resolverFactory1.createVariable(entry.getKey(), entry.getValue());
            }
        }
    }
}
