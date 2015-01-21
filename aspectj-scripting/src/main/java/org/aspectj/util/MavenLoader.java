package org.aspectj.util;

import com.github.smreed.dropship.MavenClassLoader;
import org.aspectj.configuration.model.Artifact;
import org.aspectj.configuration.model.Aspect;
import org.aspectj.configuration.model.ClassRef;
import org.aspectj.configuration.model.ResourceRef;
import org.mvel2.integration.VariableResolverFactory;

import java.io.InputStream;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.logging.Logger;

/**
 *
 */
public class MavenLoader {

    private static Logger LOGGER = Logger.getLogger(MavenLoader.class.getName());

    /*
    -Drepo.remote.url=https://repo.spring.io/snapshot/
    -Drepo.local.path=.m2/repository
     */
    public static URLClassLoader getClassLoader(String gav) {
        String mavenRepository = System.getProperty(Utils.CONFIGURATION_MAVEN_REPOSITORY);
        MavenClassLoader.ClassLoaderBuilder classLoaderBuilder;
        if(mavenRepository == null || mavenRepository.isEmpty()){
            LOGGER.info("Using maven central repository");
            classLoaderBuilder = MavenClassLoader.usingCentralRepo();
        } else {
            LOGGER.info("Using maven custom repository: "+mavenRepository);
            classLoaderBuilder = MavenClassLoader.using(mavenRepository);
        }
        return classLoaderBuilder.forMavenCoordinates(gav);
    }

    public static Class<?> loadClass(String gav, String className) throws ClassNotFoundException {
        URLClassLoader classLoader = getClassLoader(gav);
        return classLoader.loadClass(className);
    }

    public static void prefetch(Artifact[] artifacts){
        if(artifacts==null) return;
        for(Artifact artifact: artifacts){
            prefetch(artifact);
        }
    }

    public static void prefetch(Artifact artifact){
        if(artifact==null) return;
        getClassLoader(artifact.getArtifact());
    }

    public static void prefetchDependencies(Collection<Aspect> aspects) {
        if(aspects==null) return;
        for(Aspect aspect: aspects){
            prefetch(aspect.getArtifacts());
        }
    }

    public static void loadArtifact(Artifact[] artifacts, VariableResolverFactory variableResolverFactory) {
        if(artifacts!=null){
            for(Artifact artifact: artifacts){
                URLClassLoader classLoader = getClassLoader(artifact.getArtifact());
                ClassRef[] classRefs = artifact.getClassRefs();
                if(classRefs!=null){
                    for(ClassRef classRef: classRefs){
                        try {
                            Class<?> aClass = classLoader.loadClass(classRef.getClassName());
                            variableResolverFactory.createVariable(classRef.getVariable(), aClass);
                        } catch (ClassNotFoundException e) {
                            throw new IllegalArgumentException("Class '"+classRef.getClassName()
                                    +"' not found in artifact: "+artifact.getArtifact());
                        }
                    }
                }
                ResourceRef[] resourceRefs = artifact.getResourceRefs();
                if(resourceRefs!=null){
                    for(ResourceRef resourceRef: resourceRefs){
                        InputStream resourceStream = classLoader.getResourceAsStream(resourceRef.getResourceName());
                        if(resourceStream==null){
                            throw new IllegalArgumentException("Resource "+resourceRef.getResourceName()+
                                    " not found in artifact: " + artifact.getArtifact());
                        }
                        variableResolverFactory.createVariable(resourceRef.getVariable(), resourceStream);
                    }
                }
            }
        }
    }
}
