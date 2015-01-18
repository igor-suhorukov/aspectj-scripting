package org.aspectj.util;

import com.github.smreed.dropship.MavenClassLoader;
import org.aspectj.configuration.model.Aspect;

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

    public static void prefetch(String[] gavs){
        if(gavs==null) return;
        for(String gav: gavs){
            prefetch(gav);
        }
    }

    public static void prefetch(String gav){
        if(gav==null) return;
        getClassLoader(gav);
    }

    public static void prefetchDependencies(Collection<Aspect> aspects) {
        if(aspects==null) return;
        for(Aspect aspect: aspects){
            prefetch(aspect.getArtifacts());
        }
    }

}
