package com.twb.stringtoclass.config.factory;

import com.twb.stringtoclass.ingestion.IngestionService;
import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilationFailedException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

@Component
public class IngestionServiceFactory {

    @SuppressWarnings({"unchecked", "java:S3740"})
    public IngestionService getService(String fileContent) throws Exception {
        try (GroovyClassLoader groovyClassLoader = new GroovyClassLoader()) {
            Class scriptClass = groovyClassLoader.parseClass(fileContent);
            return (IngestionService) scriptClass.getDeclaredConstructor().newInstance();
        } catch (CompilationFailedException
                | IOException
                | NoSuchMethodException
                | InvocationTargetException
                | IllegalAccessException
                | InstantiationException e) {
            throw new Exception("Failed to parse script", e);
        }
    }
}
