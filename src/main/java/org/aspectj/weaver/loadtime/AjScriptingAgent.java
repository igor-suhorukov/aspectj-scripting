package org.aspectj.weaver.loadtime;

import org.aspectj.util.Utils;

import java.lang.instrument.Instrumentation;
import java.util.concurrent.ExecutionException;

public class AjScriptingAgent {

    public static void premain(String options, Instrumentation instrumentation) {
        prepareConfiguration(options);
        org.aspectj.weaver.loadtime.Agent.premain(options, instrumentation);
    }

    public static void agentmain(String options, Instrumentation instrumentation) {
        prepareConfiguration(options);
        org.aspectj.weaver.loadtime.Agent.agentmain(options, instrumentation);
    }

    private static void prepareConfiguration(String options) {
        if(options!=null && !options.trim().isEmpty()){
            System.setProperty(Utils.CONFIGURATION_PREFIX, options);
            if(ConfigurationHolder.isAspectJScriptingManagedResource(options)){
                try {
                    ConfigurationHolder.configuration.get(options);
                } catch (ExecutionException e) {
                    throw new IllegalArgumentException(e.getCause());
                }
            }
        }
    }

    public static Instrumentation getInstrumentation() {
        return org.aspectj.weaver.loadtime.Agent.getInstrumentation();
    }
}
