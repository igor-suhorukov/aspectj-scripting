package org.aspectj.aspect.lifecycle;

import org.aspectj.configuration.AspectJDescriptor;
import org.aspectj.configuration.model.Configuration;
import org.aspectj.configuration.model.Expression;
import org.aspectj.configuration.model.GlobalContext;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.util.MavenLoader;
import org.aspectj.util.Utils;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.integration.impl.MapVariableResolverFactory;

import java.io.Serializable;
import java.util.HashMap;

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
        Configuration configuration = AspectJDescriptor.getConfiguration();
        if(configuration.getGlobalContext()!=null && configuration.getGlobalResolver()==null) {
            synchronized (Configuration.class) {
                if(configuration.getGlobalResolver()==null) {
                    MavenLoader.prefetchDependencies(configuration.getAllAspects());
                    if (configuration.getGlobalContext() != null) {
                        MavenLoader.prefetch(configuration.getGlobalContext().getArtifacts());
                    }
                    GlobalContext globalContext = configuration.getGlobalContext();
                    MapVariableResolverFactory globalResolver = new MapVariableResolverFactory(new HashMap<String, Object>());
                    MavenLoader.loadArtifact(globalContext.getArtifacts(), globalResolver);
                    Utils.executeExpression(globalContext.getInit(), globalResolver);
                    Utils.registerDisposeExpression(globalContext.getDispose(), globalResolver);
                    configuration.setGlobalResolver(globalResolver);
                }
            }
        }
        aspect = configuration.getAspect(aspectName);
        registerGlobalResolverContext(configuration);
        MavenLoader.loadArtifact(aspect.getArtifacts(), resolverFactory);
        Utils.executeExpression(aspect.getInit(), resolverFactory);
        if(Expression.isNotEmptyExpression(aspect.getProcess())){
            processScript = Utils.compileMvelExpression(aspect.getProcess().getExpression());
        } else {
            processScript = null;
        }
        Utils.registerDisposeExpression(aspect.getDispose(), this.resolverFactory);
    }

    private void registerGlobalResolverContext(Configuration configuration) {
        VariableResolverFactory globalResolver = configuration.getGlobalResolver();
        if(globalResolver!=null) {
            resolverFactory.setNextFactory(globalResolver);
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
        Utils.fillResolveParams(aspect.getProcess().getResultParams(), variableResolverFactory);
        variableResolverFactory.createVariable(JOIN_POINT_VARIABLE, pjp);
        variableResolverFactory.setNextFactory(resolverFactory);
        return variableResolverFactory;
    }
}
