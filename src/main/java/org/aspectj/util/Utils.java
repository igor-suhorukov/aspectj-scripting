package org.aspectj.util;

import org.aspectj.configuration.model.Expression;
import org.mvel2.MVEL;
import org.mvel2.integration.VariableResolverFactory;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 *
 */
public class Utils {

    public static final String CONFIGURATION_PREFIX = "org.aspectj.weaver.loadtime.configuration";
    public static final String DEBUG_OPTION = CONFIGURATION_PREFIX + ".debug";
    public static final String CONFIGURATION_FILTER = CONFIGURATION_PREFIX + ".filter";
    public static final String CONFIGURATION_MAVEN_REPOSITORY = "repo.remote.url";

    public static final String MVEL_PREFIX = "mvel:";
    public static final String MVEL_PACKAGE_PREFIX = "org.mvel2";
    
    public static final String JAVAX_MANAGEMENT_REMOTE_RMI_PACKAGE = "javax.management.remote.rmi";

    public static Object checkMvelExpression(String source){
        if(source.startsWith(MVEL_PREFIX)){
            String expression = source.substring(MVEL_PREFIX.length());
            return MVEL.eval(expression);
        } else {
            return source;
        }
    }

    public static Throwable unwrapMvelException(RuntimeException exception) {
        Throwable resultException = exception;
        while (resultException!=null && resultException.getStackTrace()!=null
                && resultException.getStackTrace().length>0 &&
                (resultException.getStackTrace()[0].getClassName().startsWith(MVEL_PACKAGE_PREFIX)
                    || resultException.getClass().isAssignableFrom(InvocationTargetException.class))){
            if(resultException.getCause()==null) break;
            resultException = resultException.getCause();
        }
        return resultException;
    }

    public static Object executeMvelExpression(Serializable compiledScript, VariableResolverFactory variableResolverFactory) throws Throwable {
        try {
            return MVEL.executeExpression(compiledScript, variableResolverFactory);
        } catch (RuntimeException exception) {
            throw unwrapMvelException(exception);
        }
    }

    public static boolean isEmpty(String string) {
        return string == null || string.trim().isEmpty();
    }

    public static Serializable compileMvelExpression(String expression) {
        return MVEL.compileExpression(expression);
    }

    public static boolean isSkippedClassLoader() {
        boolean isMavenClassLoader = false;
        StackTraceElement[] stackTrace = new Exception().getStackTrace();
        for(StackTraceElement traceElement: stackTrace){
            if(MavenLoader.class.getName().equals(traceElement.getClassName()) ||
                    traceElement.getClassName().startsWith(JAVAX_MANAGEMENT_REMOTE_RMI_PACKAGE)){
                isMavenClassLoader = true;
                break;
            }
        }
        return isMavenClassLoader;
    }

    public static void fillResolveParams(Map<String, Object> resultParams, VariableResolverFactory resolverFactory1) {
        if(resultParams!=null && resultParams.size()>0){
            for(Map.Entry<String,Object> entry: resultParams.entrySet()){
                resolverFactory1.createVariable(entry.getKey(), entry.getValue());
            }
        }
    }

    public static void executeExpression(Expression expression, VariableResolverFactory variableResolverFactory) {
        if(expression!=null && expression.isNotEmpty()) {
            fillResolveParams(expression.getResultParams(), variableResolverFactory);
            MVEL.eval(expression.getExpression(), variableResolverFactory);
        }
    }

    public static void registerDisposeExpression(final Expression expression, final VariableResolverFactory resolverFactory) {
        if(expression!=null && expression.isNotEmpty()){
            Runtime.getRuntime().addShutdownHook(new Thread(){
                @Override
                public void run() {
                    executeExpression(expression, resolverFactory);
                }
            });
        }
    }
}
