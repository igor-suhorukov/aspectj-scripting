package org.aspectj.configuration;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.aspectj.configuration.model.Configuration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;

/**
 *
 */
public class ConfigurationLoader {

    public static Configuration fromJson(String json){
        try {
            return new GsonBuilder().create().fromJson(json, Configuration.class);
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Configuration fromXml(String xml){
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (Configuration) jaxbUnmarshaller.unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
