package com.twb.stringtoclass.ingestion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ScriptInfo {
    String name();

    String email();

    int version();

    int maxTries() default 1;
}
