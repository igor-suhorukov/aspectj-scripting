package org.aspectj.configuration.model;

import com.github.igorsuhorukov.eclipse.aether.artifact.DefaultArtifact;
import org.aspectj.util.Utils;
import org.mvel2.integration.VariableResolverFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

/**
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Configuration {
    private Map<String,String[]> aspectByInstance;
    private Collection<Aspect> aspects;
    Artifact[] artifacts;
    private GlobalContext globalContext;
    private volatile transient VariableResolverFactory globalResolver;

    Configuration() {
    }

    public Configuration(Map<String, String[]> aspectByInstance, Collection<Aspect> aspects, GlobalContext globalContext) {
        this.aspectByInstance = aspectByInstance;
        this.aspects = aspects;
        this.globalContext = globalContext;
    }

    public Collection<Aspect> currentAspects(String filter){
        if(filter==null || filter.isEmpty()){
            return aspects;
        }
        String[] aspectsRefs = aspectByInstance.get(filter);
        if(aspectsRefs==null || aspectsRefs.length==0){
            return Collections.emptyList();
        }
        Collection<Aspect> filtredAspects = new ArrayList<Aspect>();
        for(Aspect aspect: aspects){
            String aspectName = aspect.getName();
            if(aspectName == null || aspectName.isEmpty()) continue;
            for(String name: aspectsRefs){
                if(name == null || name.isEmpty()) continue;
                if(name.equals(aspectName)){
                    filtredAspects.add(aspect);
                }
            }
        }
        if(filtredAspects.size()!=aspectsRefs.length){
            throw new IllegalArgumentException("Filtred aspect count "+filtredAspects.size()+", but reference count "+aspectsRefs.length);
        }
        return filtredAspects;
    }

    public Collection<Aspect> getAllAspects() {
        return aspects;
    }

    Artifact[] getArtifacts() {
        return artifacts;
    }

    public Aspect getAspect(String name){
        for(Aspect aspect: aspects){
            if(name.equals(aspect.getName())){
                return aspect;
            }
        }
        throw new IllegalArgumentException("Aspect configuration '"+name+"' not found");
    }

    public GlobalContext getGlobalContext() {
        return globalContext;
    }

    public VariableResolverFactory getGlobalResolver() {
        return globalResolver;
    }

    public void setGlobalResolver(VariableResolverFactory globalResolver) {
        this.globalResolver = globalResolver;
    }

    public static void validateConfiguration(Configuration configuration) {
        Collection<Artifact> artifacts = new ArrayList<Artifact>();
        Collection<Aspect> aspects = configuration.getAllAspects();
        Set<String> uniqAspectNames = new HashSet<String>(aspects==null ? 0 : aspects.size());
        if(aspects!=null && aspects.size() > 0){
            for(Aspect aspect: aspects){
                if(Utils.isEmpty(aspect.getName())){
                    throw new IllegalArgumentException("Aspect name is empty");
                }
                uniqAspectNames.add(aspect.getName());
                if(Utils.isEmpty(aspect.getPointcut())){
                    throw new IllegalArgumentException("Pointcut expression is empty. Aspect: " + aspect.getName());
                }
                if(aspect.getType()==null){
                    throw new IllegalArgumentException("Type is empty. Aspect: " + aspect.getName());
                }
                validateExpression(aspect, aspect.getInit(), "init");
                validateExpression(aspect, aspect.getProcess(), "process");
                validateExpression(aspect, aspect.getDispose(), "dispose");
                validateArtifact("Aspect: " + aspect.getName(), aspect.getArtifacts());
                addArtifacts(artifacts, aspect.getArtifacts());
            }
            if(uniqAspectNames.size()!=aspects.size()){
                throw new IllegalArgumentException("Unique aspect names: "
                        + uniqAspectNames.size() + ", total aspect count "+aspects.size());
            }
        }
        Map<String,String[]> aspectByInstance = configuration.getAspectByInstance();
        Set<Map.Entry<String, String[]>> entries = aspectByInstance.entrySet();
        for(Map.Entry<String, String[]> entry: entries){
            String key = entry.getKey();
            if(Utils.isEmpty(key)){
                throw new IllegalArgumentException("Key is empty inside aspectByInstance");
            }
            String[] aspectsRef = entry.getValue();
            if(aspectsRef==null || aspectsRef.length==0){
                throw new IllegalArgumentException("Value is empty inside aspectByInstance. Key " + key);
            }
            for(String aspect: aspectsRef){
                if(Utils.isEmpty(aspect)){
                    throw new IllegalArgumentException("Aspect reference is empty inside aspectByInstance. Key " + key);
                }
                if(!uniqAspectNames.contains(aspect)){
                    throw new IllegalArgumentException("Aspect not found by by reference '" + aspect + "'");
                }
            }
        }
        GlobalContext globalContext = configuration.getGlobalContext();
        if(globalContext!=null){
            validateTimerTasks(globalContext);
            validateArtifact("Global context", globalContext.getArtifacts());
            addArtifacts(artifacts, globalContext.getArtifacts());
        }
        if(!artifacts.isEmpty()){
            configuration.artifacts = artifacts.toArray(new Artifact[artifacts.size()]);
        }
        //TODO validate artifact not null, variable name uniq on aspect level
        //TODO validate globalContext
    }

    private static void validateTimerTasks(GlobalContext globalContext) {
        TimerTask[] timerTasks = globalContext.getTimerTasks();
        if(timerTasks !=null && timerTasks.length>0){
            for (int idx = 0; idx < timerTasks.length; idx++) {
                TimerTask timerTask = timerTasks[idx];
                if(timerTask.getJobExpression()==null || timerTask.getJobExpression().trim().isEmpty()){
                    throw new IllegalArgumentException(String.format("TimerTask[%d] with empty jobExpression", idx));
                }
                if(timerTask.getFirstTime()==null && timerTask.getDelay()==null && timerTask.getPeriod()==null){
                    String msg = String.format("TimerTask[%d] with empty parameters firstTime, delay, period", idx);
                    throw new IllegalArgumentException(msg);
                }
                if(timerTask.getFirstTime()==null && timerTask.getDelay()==null){
                    String msg = String.format(
                            "TimerTask[%d]. Property must be defined with value: either 'firstTime' or 'delay' ", idx);
                    throw new IllegalArgumentException(msg);
                }
            }
        }
    }

    private static void validateArtifact(String context, Artifact[] artifacts) {
        if(artifacts!=null && artifacts.length>0){
            for (int idx = 0; idx < artifacts.length; idx++) {
                Artifact artifact = artifacts[idx];
                String gav = artifact.getArtifact();
                if (gav == null || gav.trim().isEmpty()) {
                    throw new IllegalArgumentException(String.format("Empty artifact value (group:artifact:value) at %s[%d]", context, idx));
                }
                try {
                    new DefaultArtifact(gav);
                } catch (IllegalArgumentException e){
                    throw new IllegalArgumentException(String.format("%s at %s[%d] ", e.getMessage(), context, idx));
                }
            }
        }
    }

    private static void addArtifacts(Collection<Artifact> resultArtifacts, Artifact[] sourceArtifacts) {
        if(sourceArtifacts !=null){
            Collections.addAll(resultArtifacts, sourceArtifacts);
        }
    }

    private static void validateExpression(Aspect aspect, Expression expression, String expressionName) {
        if(expression!=null){
            if(!Expression.isNotEmptyExpression(expression)){
                throw new IllegalArgumentException("Empty expression in '"+expressionName+"' Aspect: "+aspect.getName());
            }
            try {
                Utils.compileMvelExpression(expression.getExpression());
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid MVEL expression. Aspect '" + aspect.getName()
                        + "', expression: " + expressionName, e);
            }
            try {
                expression.getResultParams();
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid parameter value. Aspect '" + aspect.getName()
                        + "', expression: " + expressionName, e);
            }
        }
    }

    public Map<String, String[]> getAspectByInstance() {
        if(aspectByInstance==null) return Collections.emptyMap();
        return Collections.unmodifiableMap(aspectByInstance);
    }
}
