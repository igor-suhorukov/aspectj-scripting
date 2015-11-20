package org.aspectj.weaver.loadtime;

import org.aspectj.util.Utils;
import org.aspectj.weaver.bcel.BcelWeakClassLoaderReference;
import org.aspectj.weaver.loadtime.definition.Definition;
import org.aspectj.weaver.tools.Trace;
import org.aspectj.weaver.tools.TraceFactory;
import org.aspectj.weaver.tools.WeavingAdaptor;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Use in non-OSGi environment
 *
 * @author David Knibb
 */
public class DefaultWeavingContext implements IWeavingContext {

    protected BcelWeakClassLoaderReference loaderRef;
    private String shortName;
    private static Trace trace = TraceFactory.getTraceFactory().getTrace(DefaultWeavingContext.class);

    /**
     * Construct a new WeavingContext to use the specified ClassLoader This is the constructor which should be used.
     *
     * @param loader
     */
    public DefaultWeavingContext(ClassLoader loader) {
        super();
        this.loaderRef = new BcelWeakClassLoaderReference(loader);
    }

    /**
     * Same as ClassLoader.getResources()
     */
    public Enumeration getResources(String name) throws IOException {
        if(name!=null && !name.isEmpty()){
            if(Utils.isSkippedClassLoader()){
                return java.util.Collections.emptyEnumeration();
            }
            String resource = Utils.checkMvelExpression(name).toString();
            URL url = null;
            if(resource.startsWith("http")){
                url = new URL(resource);
            } else {
                try {
                    url = ConfigurationHolder.configuration.get(resource);
                } catch (ExecutionException e) {
                    throw new IOException(e);
                }
            }
            if(url!=null) return Collections.enumeration(Arrays.asList(url));
        }
        return getClassLoader().getResources(name);
    }

    /**
     * @return null as we are not in an OSGi environment (therefore no bundles)
     */
    public String getBundleIdFromURL(URL url) {
        return "";
    }

    /**
     * @return classname@hashcode
     */
    public String getClassLoaderName() {
        ClassLoader loader = getClassLoader();
        return ((loader != null) ? loader.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(loader))
                : "null");
    }

    public ClassLoader getClassLoader() {
        return loaderRef.getClassLoader();
    }

    /**
     * @return filename
     */
    public String getFile(URL url) {
        return url.getFile();
    }

    /**
     * @return unqualifiedclassname@hashcode
     */
    public String getId() {
        if (shortName == null) {
            shortName = getClassLoaderName().replace('$', '.');
            int index = shortName.lastIndexOf(".");
            if (index != -1) {
                shortName = shortName.substring(index + 1);
            }
        }
        return shortName;
    }

    public String getSuffix() {
        return getClassLoaderName();
    }

    public boolean isLocallyDefined(String classname) {
        String asResource = classname.replace('.', '/').concat(".class");
        ClassLoader loader = getClassLoader();
        URL localURL = loader.getResource(asResource);
        if (localURL == null) {
            return false;
        }

        boolean isLocallyDefined = true;

        ClassLoader parent = loader.getParent();
        if (parent != null) {
            URL parentURL = parent.getResource(asResource);
            if (localURL.equals(parentURL)) {
                isLocallyDefined = false;
            }
        }
        return isLocallyDefined;
    }

    /**
     * Simply call weaving adaptor back to parse aop.xml
     *
     * @param adaptor
     * @param loader
     */
    public List<Definition> getDefinitions(final ClassLoader loader, final WeavingAdaptor adaptor) {
        if (trace.isTraceEnabled()) {
            trace.enter("getDefinitions", this, new Object[] { "goo", adaptor });
        }

        List<Definition> definitions = ((ClassLoaderWeavingAdaptor) adaptor).parseDefinitions(loader);

        if (trace.isTraceEnabled()) {
            trace.exit("getDefinitions", definitions);
        }
        return definitions;
    }
}
