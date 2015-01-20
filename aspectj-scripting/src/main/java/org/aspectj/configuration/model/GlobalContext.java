package org.aspectj.configuration.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class GlobalContext {
    private Artifact[] artifacts;
    private Expression init;
    private Expression dispose;

    GlobalContext() {
    }

    public GlobalContext(Artifact[] artifacts, Expression init, Expression dispose) {
        this.artifacts = artifacts;
        this.init = init;
        this.dispose = dispose;
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
}
