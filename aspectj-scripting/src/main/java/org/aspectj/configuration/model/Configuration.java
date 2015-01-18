package org.aspectj.configuration.model;

import org.aspectj.util.Utils;

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

    Configuration() {
    }

    public Configuration(Map<String, String[]> aspectByInstance, Collection<Aspect> aspects) {
        this.aspectByInstance = aspectByInstance;
        this.aspects = aspects;
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

    public Aspect getAspect(String name){
        for(Aspect aspect: aspects){
            if(name.equals(aspect.getName())){
                return aspect;
            }
        }
        throw new IllegalArgumentException("Aspect configuration '"+name+"' not found");
    }

    public static void validateConfiguration(Configuration configuration) {
        Collection<Aspect> aspects = configuration.getAllAspects();
        Set<String> uniqAspectNames = new HashSet<String>(aspects.size());
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
        }
        if(uniqAspectNames.size()!=aspects.size()){
            throw new IllegalArgumentException("Unique aspect names: "
                    + uniqAspectNames.size() + ", total aspect count "+aspects.size());
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
