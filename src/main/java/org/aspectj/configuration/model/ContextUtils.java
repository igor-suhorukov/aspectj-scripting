package org.aspectj.configuration.model;

import org.aspectj.util.MavenLoader;
import org.aspectj.util.Utils;
import org.aspectj.util.task.MvelTimerTask;
import org.mvel2.integration.impl.MapVariableResolverFactory;

import java.util.HashMap;
import java.util.Timer;

/**
 */
public class ContextUtils {

    public static final String VARIABLE_RESOLVER = "variableResolver";
    public static final String VARIABLE_GLOBAL_TIMER = "globalTimer";

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
                    initTimers(globalContext, globalResolver);
                    configuration.setGlobalResolver(globalResolver);
                }
            }
        }
    }

    private static void initTimers(GlobalContext globalContext, MapVariableResolverFactory globalResolver) {
        TimerTask[] timerTasks = globalContext.getTimerTasks();
        if(timerTasks!=null && timerTasks.length>0){
            Timer timer = new Timer();
            globalResolver.createVariable(VARIABLE_GLOBAL_TIMER, timer);
            for(TimerTask timerTask: timerTasks){
                MvelTimerTask mvelTimerTask = new MvelTimerTask(timerTask.getJobExpression(), globalResolver);
                if(timerTask.getFirstTime()!=null && timerTask.getPeriod()!=null){
                    timer.schedule(mvelTimerTask, timerTask.getFirstTime(), timerTask.getPeriod());
                } else if(timerTask.getFirstTime()!=null && timerTask.getPeriod()==null){
                    timer.schedule(mvelTimerTask, timerTask.getFirstTime());
                } else if(timerTask.getDelay()!=null && timerTask.getPeriod()!=null){
                    timer.schedule(mvelTimerTask, timerTask.getDelay(), timerTask.getPeriod());
                } else if(timerTask.getDelay()!=null){
                    timer.schedule(mvelTimerTask, timerTask.getDelay());
                }
            }
        }
    }
}
