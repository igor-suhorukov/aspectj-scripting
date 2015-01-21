package org.aspectj.configuration.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class ResourceRef {
    private String variable;
    private String resourceName;

    ResourceRef() {
    }

    public ResourceRef(String variable, String resourceName) {
        this.variable = variable;
        this.resourceName = resourceName;
    }

    public String getVariable() {
        return variable;
    }

    public String getResourceName() {
        return resourceName;
    }
}
