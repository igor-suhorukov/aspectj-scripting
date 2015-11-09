package org.aspectj.configuration.model;

import org.aspectj.util.MavenLoader;
import org.aspectj.util.Utils;
import org.mvel2.integration.impl.MapVariableResolverFactory;

import java.util.HashMap;

/**
 */
public class ContextUtils {

    public static final String VARIABLE_RESOLVER = "variableResolver";

    public static void initGlobalContext(Configuration configuration) {
        if(configuration.getGlobalContext()!=null && configuration.getGlobalResolver()==null) {
            synchronized (Configuration.class) {
                if(configuration.getGlobalResolver()==null) {
                    MavenLoader.prefetch(configuration.getArtifacts());
                    GlobalContext globalContext = configuration.getGlobalContext();
                    MapVariableResolverFactory globalResolver = new MapVariableResolverFactory(new HashMap<String, Object>());
                    globalResolver.createVariable(VARIABLE_RESOLVER, globalResolver);
                    MavenLoader.loadArtifact(globalContext.getArtifacts(), globalResolver);
                    Utils.executeExpression(globalContext.getInit(), globalResolver);
                    Utils.registerDisposeExpression(globalContext.getDispose(), globalResolver);
                    configuration.setGlobalResolver(globalResolver);
                }
            }
        }
    }
}
