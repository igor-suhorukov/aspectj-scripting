package org.aspectj.configuration.model;

import org.aspectj.aspect.*;

/**
 *
 */
public enum Type {

    AROUND(AbstractAroundAspect.class),
    BEFORE(AbstractBeforeAspect.class),
    AFTER(AbstractAfterAspect.class),
    AFTER_RETURNING(AbstractAfterReturningAspect.class),
    AFTER_THROWING(AbstractAfterThrowingAspect.class);

    private Type(Class clazz) {
        this.aspectName = clazz.getName();
    }

    private String aspectName;

    public String getAspectName() {
        return aspectName;
    }
}
