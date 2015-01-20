package org.aspectj.configuration.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class Artifact {
    private String artifact;
    private ClassRef[] classRefs;

    Artifact() {
    }

    public Artifact(String artifact, ClassRef[] classRefs) {
        this.artifact = artifact;
        this.classRefs = classRefs;
    }

    public String getArtifact() {
        return artifact;
    }

    public ClassRef[] getClassRefs() {
        return classRefs;
    }
}
