package org.aspectj.util.task;

import org.aspectj.util.Utils;
import org.mvel2.MVEL;
import org.mvel2.integration.VariableResolverFactory;

import java.io.Serializable;

public class MvelRunnable implements Runnable{

    private final Serializable mvelExpression;
    private VariableResolverFactory variableResolverFactory;

    public MvelRunnable(String mvelScript, VariableResolverFactory variableResolverFactory) {
        mvelExpression = Utils.compileMvelExpression(mvelScript);
        this.variableResolverFactory = variableResolverFactory;
    }

    public void run() {
        MVEL.executeExpression(mvelExpression, variableResolverFactory);
    }
}
