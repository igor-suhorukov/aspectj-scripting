package org.aspectj.configuration.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class ClassRef {
    
    private String variable;
    private String className;

    ClassRef() {
    }

    public ClassRef(String className, String variable) {
        this.className = className;
        this.variable = variable;
    }

    public String getVariable() {
        return variable;
    }

    public String getClassName() {
        return className;
    }
}
