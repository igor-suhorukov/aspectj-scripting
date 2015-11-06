package org.aspectj.util.task;

import org.mvel2.integration.VariableResolverFactory;

import java.util.TimerTask;

public class MvelTimerTask extends TimerTask {

    private MvelRunnable mvelRunnable;

    public MvelTimerTask(String mvelScript, VariableResolverFactory variableResolverFactory) {
        this.mvelRunnable = new MvelRunnable(mvelScript, variableResolverFactory);
    }

    @Override
    public void run() {
        mvelRunnable.run();
    }
}
