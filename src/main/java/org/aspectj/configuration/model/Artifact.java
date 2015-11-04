package org.aspectj.configuration.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class Artifact {
    private String artifact;
    private ClassRef[] classRefs;
    private ResourceRef[] resourceRefs;

    Artifact() {
    }

    public Artifact(String artifact, ClassRef[] classRefs, ResourceRef[] resourceRefs) {
        this.artifact = artifact;
        this.classRefs = classRefs;
        this.resourceRefs = resourceRefs;
    }

    public String getArtifact() {
        return artifact;
    }

    public ClassRef[] getClassRefs() {
        return classRefs;
    }

    public ResourceRef[] getResourceRefs() {
        return resourceRefs;
    }
}
