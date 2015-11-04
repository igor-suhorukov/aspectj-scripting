package org.aspectj.configuration.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Aspect {
    private String name;
    private Type type;
    private String pointcut;
    private Artifact[] artifacts;
    private Expression init;
    private Expression process;
    private Expression dispose;

    Aspect() {
    }

    public Aspect(String name, Type type, String pointcut, Artifact[] artifacts, Expression init, Expression process, Expression dispose) {
        this.name = name;
        this.type = type;
        this.pointcut = pointcut;
        this.artifacts = artifacts;
        this.init = init;
        this.process = process;
        this.dispose = dispose;
    }

    public String getName() {
        return name;
    }

    public String getPointcut() {
        return pointcut;
    }

    public Type getType() {
        return type;
    }

    public Artifact[] getArtifacts() {
        return artifacts;
    }

    public Expression getInit() {
        return init;
    }

    public Expression getProcess() {
        return process;
    }

    public Expression getDispose() {
        return dispose;
    }
}
