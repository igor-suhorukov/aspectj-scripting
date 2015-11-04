package org.aspectj.configuration.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class ResourceRef {
    private String variable;
    private String resourceName;
    private boolean useUrl = true;

    ResourceRef() {
    }

    public ResourceRef(String variable, String resourceName, boolean useUrl) {
        this.variable = variable;
        this.resourceName = resourceName;
        this.useUrl = useUrl;
    }

    public String getVariable() {
        return variable;
    }
    
    public String getResourceName() {
        return resourceName;
    }

    public boolean isUseUrl() {
        return useUrl;
    }
}
