package org.aspectj.configuration.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class GlobalContext {
    private Artifact[] artifacts;
    private Expression init;
    private Expression dispose;
    private TimerTask[] timerTasks;

    GlobalContext() {
    }

    public GlobalContext(Artifact[] artifacts, Expression init, Expression dispose, TimerTask[] timerTasks) {
        this.artifacts = artifacts;
        this.init = init;
        this.dispose = dispose;
        this.timerTasks = timerTasks;
    }

    public Artifact[] getArtifacts() {
        return artifacts;
    }

    public Expression getInit() {
        return init;
    }

    public Expression getDispose() {
        return dispose;
    }

    public TimerTask[] getTimerTasks() {
        return timerTasks;
    }
}
