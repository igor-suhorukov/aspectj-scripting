package org.aspectj.configuration;

import org.apache.commons.io.IOUtils;
import org.aspectj.configuration.model.Aspect;
import org.aspectj.configuration.model.Configuration;
import org.aspectj.configuration.model.ContextUtils;
import org.aspectj.util.Utils;
import org.mvel2.optimizers.OptimizerFactory;
import org.mvel2.templates.TemplateRuntime;

import java.io.*;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;

/**
 *
 */
public class AspectJDescriptor {

    public static final String DEFAULT_FILTER = "ALL_ASPECT";

    private static Logger LOGGER = Logger.getLogger(AspectJDescriptor.class.getName());

    public static String generate(Collection<Aspect> aspects){
        return TemplateRuntime.eval(
        "<aspectj>\n\t<aspects>\n" +
            "@if{aspects!=null}@foreach{aspect : aspects} " +
                "\t\t<concrete-aspect name=\"@{aspect.name}\" extends=\"@{aspect.type.aspectName}\">" +
                "<pointcut name=\"pointcutExpression\" expression=\"@{aspect.pointcut}\"/>" +
                "</concrete-aspect>\n" +
            "@end{} @end{}" +
            "\t</aspects>\n" +
            "@if{Boolean.getBoolean(\""+ Utils.DEBUG_OPTION +"\")}\t<weaver options=\"-verbose  -showWeaveInfo\"/>\n@end{}" +
        "</aspectj>\n",
            Collections.singletonMap("aspects",aspects)).toString();
    }

    public static URL renderConfigurationToTemporaryFile(String configurationLocation) throws IOException {

        String instanceFilter = System.getProperty(Utils.CONFIGURATION_FILTER, DEFAULT_FILTER);

        Configuration configuration = loadConfiguration(configurationLocation);
        final Collection<Aspect> aspects;
        if(DEFAULT_FILTER.equals(instanceFilter)){
            aspects = configuration.getAllAspects();
        } else {
            aspects = configuration.currentAspects(Utils.checkMvelExpression(instanceFilter).toString());
        }
        String aopXmlContent = generate(aspects);
        return writeAopXml(aopXmlContent);
    }

    private static URL writeAopXml(String aopXmlContent) throws IOException {
        URL url;File aopXmlTempFile = File.createTempFile("aop", ".xml");
        if(!Boolean.getBoolean(Utils.DEBUG_OPTION)){
            aopXmlTempFile.deleteOnExit();
        } else {
            LOGGER.info("AspectJ aop.xml descriptor: " + aopXmlTempFile.getAbsolutePath());
        }
        FileOutputStream outputStream = new FileOutputStream(aopXmlTempFile);
        try {
            IOUtils.copy(new StringReader(aopXmlContent), outputStream);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }

        url = aopXmlTempFile.toURI().toURL();
        return url;
    }

    private volatile static Configuration configuration;

    public static synchronized Configuration getConfiguration(){
        return configuration;
    }

    static synchronized Configuration loadConfiguration(String configurationLocation) throws IOException {
        if(configuration==null){
            LOGGER.info("Fetch configuration from "+configurationLocation);
            InputStream configStream = new URL(configurationLocation).openStream();
            String configurationContent;
            try {
                configurationContent = IOUtils.toString(configStream);
            } finally {
                IOUtils.closeQuietly(configStream);
            }
            Configuration configuration;
            if(configurationContent.startsWith("{")){
                LOGGER.info("Load configuration as JSON file: configuration start with '{'");
                configuration = ConfigurationLoader.fromJson(configurationContent);
            } else {
                configuration = ConfigurationLoader.fromXml(configurationContent);
            }
            Configuration.validateConfiguration(configuration);
            AspectJDescriptor.configuration = configuration;
            OptimizerFactory.setDefaultOptimizer(OptimizerFactory.SAFE_REFLECTIVE); // to avoid ClassNotFoundException in maven classes

            ContextUtils.initGlobalContext(configuration);
            LOGGER.info("Shared configuration class hash: " + System.identityHashCode(AspectJDescriptor.class));
        }
        return configuration;
    }
}
