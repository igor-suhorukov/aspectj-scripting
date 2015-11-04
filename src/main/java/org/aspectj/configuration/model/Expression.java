package org.aspectj.configuration.model;

import org.aspectj.util.Utils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Expression {
    private String expression;
    private Map<String, String> params;


    Expression() {
    }

    public Expression(String expression) {
        this.expression = expression;
    }

    public Expression(String expression, Map<String, String> params) {
        this.expression = expression;
        this.params = params;
    }
    
    public boolean isNotEmpty() {
        return isNotEmptyExpression(this);
    }
    
    public static boolean isNotEmptyExpression(Expression expression) {
        return expression !=null && expression.getExpression()!=null && !expression.getExpression().trim().isEmpty();
    }

    public String getExpression() {
        return expression;
    }

    public Map<String, Object> getResultParams() {
        if(params == null || params.size() == 0) return null;
        Map<String, Object> resultParams = new HashMap<String, Object>(params.size());
        Set<Map.Entry<String,String>> entries = params.entrySet();
        for(Map.Entry<String,String> entry: entries){
            String sourceValue = entry.getValue();
            String key = entry.getKey();
            try {
                Object value = Utils.checkMvelExpression(sourceValue);
                resultParams.put(key, value);
            } catch (Exception e) {
                throw new IllegalArgumentException("Parameter name '"+key+"'", e);
            }
        }
        return resultParams;
    }
}
