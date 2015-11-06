package org.aspectj.aspect;

import com.google.gson.GsonBuilder;
import org.aspectj.configuration.model.Artifact;
import org.aspectj.configuration.model.ClassRef;
import org.aspectj.configuration.model.ResourceRef;
import org.aspectj.util.MavenLoader;
import org.aspectj.util.task.MvelTimerTask;
import org.mvel2.integration.impl.MapVariableResolverFactory;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.util.HashMap;
import java.util.Timer;

public class ExecuteTimerTaskTest {

    public static final int TIMER_DELAY = 500;

    private static String jsonResult = null;

    public static void setJsonResult(String jsonResult) {
        ExecuteTimerTaskTest.jsonResult = jsonResult;
    }

    @org.junit.Test
    public void mvelScheduler() throws Exception{
        Timer timer = new Timer();
        MapVariableResolverFactory variableResolverFactory = new MapVariableResolverFactory();
        ClassRef[] classRefs = {new ClassRef("org.github.suhorukov.SigarCollect", "SigarCollect")};
        ResourceRef[] resourceRefs = new ResourceRef[0];
        Artifact[] artifacts = {new Artifact("com.github.igor-suhorukov:jvm-metrics:1.1", classRefs, resourceRefs)};
        MavenLoader.loadArtifact(artifacts, variableResolverFactory);
        String mvelScript = "java.lang.String metrics = new SigarCollect().getJsonFullInfo();" +
                            "org.aspectj.aspect.ExecuteTimerTaskTest.setJsonResult(metrics);";
        timer.schedule(new MvelTimerTask(mvelScript, variableResolverFactory), TIMER_DELAY);
        Thread.sleep(TIMER_DELAY*5);
        HashMap metrics = new GsonBuilder().create().fromJson(jsonResult, HashMap.class);
        assertNotNull(jsonResult);
        assertTrue(metrics.containsKey("ProcCpu"));
        assertTrue(metrics.containsKey("Pid"));
        assertTrue(metrics.containsKey("Mem"));
    }
}
