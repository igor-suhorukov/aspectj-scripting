package org.aspectj.util;

import org.aspectj.configuration.model.Artifact;
import org.aspectj.configuration.model.Aspect;
import org.aspectj.configuration.model.ClassRef;
import org.aspectj.configuration.model.ResourceRef;
import org.mvel2.integration.VariableResolverFactory;

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
        com.github.igorsuhorukov.smreed.dropship.MavenClassLoader.ClassLoaderBuilder classLoaderBuilder;
        if(mavenRepository == null || mavenRepository.isEmpty()){
            LOGGER.info("Using maven central repository");
            classLoaderBuilder = com.github.igorsuhorukov.smreed.dropship.MavenClassLoader.usingCentralRepo();
        } else {
            LOGGER.info("Using maven custom repository: "+mavenRepository);
            classLoaderBuilder = com.github.igorsuhorukov.smreed.dropship.MavenClassLoader.using(mavenRepository);
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
                        String className = classRef.getClassName();
                        try {
                            Class<?> aClass = classLoader.loadClass(className);
                            variableResolverFactory.createVariable(classRef.getVariable(), aClass);
                            variableResolverFactory.createVariable(className, aClass);//fix MVEL class resolution
                        } catch (ClassNotFoundException e) {
                            throw new IllegalArgumentException("Class '"+ className
                                    +"' not found in artifact: "+artifact.getArtifact());
                        }
                    }
                }
                ResourceRef[] resourceRefs = artifact.getResourceRefs();
                if(resourceRefs!=null){
                    for(ResourceRef resourceRef: resourceRefs){
                        Object resourceStream;
                        if(resourceRef.isUseUrl()){
                            resourceStream = classLoader.getResource(resourceRef.getResourceName());
                        } else {
                            resourceStream = classLoader.getResourceAsStream(resourceRef.getResourceName());
                        }
                        if (resourceStream == null) {
                            throw new IllegalArgumentException("Resource " + resourceRef.getResourceName() +
                                    " not found in artifact: " + artifact.getArtifact());
                        }
                        variableResolverFactory.createVariable(resourceRef.getVariable(), resourceStream);
                    }
                }
            }
        }
    }
}
