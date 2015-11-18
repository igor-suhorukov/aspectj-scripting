package org.aspectj.weaver.loadtime;

import org.aspectj.util.Utils;

import java.lang.instrument.Instrumentation;

public class AjScriptingAgent {

    public static void premain(String options, Instrumentation instrumentation) {
        setConfigurationProperty(options);
        org.aspectj.weaver.loadtime.Agent.premain(options, instrumentation);
    }

    public static void agentmain(String options, Instrumentation instrumentation) {
        setConfigurationProperty(options);
        org.aspectj.weaver.loadtime.Agent.agentmain(options, instrumentation);
    }

    private static void setConfigurationProperty(String options) {
        if(options!=null && !options.trim().isEmpty()){
            System.setProperty(Utils.CONFIGURATION_PREFIX, options);
        }
    }

    public static Instrumentation getInstrumentation() {
        return org.aspectj.weaver.loadtime.Agent.getInstrumentation();
    }
}
