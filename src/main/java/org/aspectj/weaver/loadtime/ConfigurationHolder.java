package org.aspectj.weaver.loadtime;

import com.github.igorsuhorukov.google.common.cache.CacheBuilder;
import com.github.igorsuhorukov.google.common.cache.CacheLoader;
import com.github.igorsuhorukov.google.common.cache.LoadingCache;
import org.aspectj.configuration.AspectJDescriptor;
import org.aspectj.util.Utils;

import java.io.IOException;
import java.net.URL;

/**
 */
public class ConfigurationHolder {

    private static final String AOP_FROM_CONFIGURATION_PREFIX = "config:";

    public static LoadingCache<String, URL> configuration = CacheBuilder.newBuilder().maximumSize(Integer.MAX_VALUE)
            .build(
                    new CacheLoader<String, URL>() {
                        public URL load(String resource) throws IOException {
                            if(isAspectJScriptingManagedResource(resource)){
                                String configuration = resource.substring(AOP_FROM_CONFIGURATION_PREFIX.length());
                                String configurationLocation = Utils.checkMvelExpression(configuration).toString();
                                return AspectJDescriptor.renderConfigurationToTemporaryFile(configurationLocation);
                            }
                            return null;
                        }
                    });

    public static boolean isAspectJScriptingManagedResource(String resource) {
        return resource.startsWith(AOP_FROM_CONFIGURATION_PREFIX);
    }
}
