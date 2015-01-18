package com.github.smreed.dropship;

import com.google.common.base.CaseFormat;
import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Manifest;

import static com.github.smreed.dropship.NotLogger.debug;
import static com.github.smreed.dropship.NotLogger.warn;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;

class Settings {

  static final Joiner GAV_JOINER = Joiner.on(':');
  static final boolean DEBUG = System.getProperty("verbose") != null;
  static final CharMatcher GAV_DELIMITER = CharMatcher.is(':');
  static final Splitter GAV_SPLITTER = Splitter.on(GAV_DELIMITER).trimResults().omitEmptyStrings();

  private static final String DEFAULT_CONFIG_FILE_NAME = "dropship.properties";
  private static final Properties CACHE = new Properties();
  private static final Splitter CSV = Splitter.on(',').trimResults().omitEmptyStrings();

  private static volatile boolean loaded = false;

  static Optional<String> mavenRepoUrl() {
    return loadProperty("repo.remote.url");
  }

  static String localRepoPath() {
    return loadProperty("repo.local.path", ".m2/repository");
  }

  static String dropshipVersion() {
    return loadProperty("dropship.xArtifactVersion", "0.0");
  }

  static List<String> additionalClasspathPaths() {
    Optional<String> additionalClasspathPathsString = loadProperty("dropship.additional.paths");
    if (additionalClasspathPathsString.isPresent()) {
      return ImmutableList.copyOf(CSV.split(additionalClasspathPathsString.get()));
    } else {
      return ImmutableList.of();
    }
  }

  private static String loadProperty(String name, String defaultValue) {
    checkNotNull(defaultValue);

    return loadProperty(name).or(defaultValue);
  }

  private static Optional<String> loadProperty(String name) {
    return Optional.fromNullable(loadBootstrapPropertiesUnchecked().getProperty(name));
  }

  static synchronized Properties loadBootstrapProperties() throws IOException {
    if (loaded) {
      return CACHE;
    }

    URL url = Dropship.class.getClassLoader().getResource(DEFAULT_CONFIG_FILE_NAME);
    if (url == null) {
      warn("No dropship.properties found! Using .dropship-prefixed system properties (-D)");
      for(Object key: System.getProperties().keySet()) {
    	  String skey = String.valueOf(key);
    	  if(skey.startsWith("dropship.")) {
    		  String kkey = skey.substring("dropship.".length());
    		  warn("Using " + kkey + "=" + System.getProperty(skey));
    		  CACHE.put(kkey, System.getProperty(skey));
    	  }
      }
    } else {
      debug("Loading configuration from %s.", url);
      CACHE.load(Dropship.class.getClassLoader().getResourceAsStream(DEFAULT_CONFIG_FILE_NAME));
      for (Map.Entry<Object, Object> entry : CACHE.entrySet()) {
        debug("  %s: %s = %s", DEFAULT_CONFIG_FILE_NAME, entry.getKey(), entry.getValue());
      }
    }
    for (Map.Entry<Object, Object> entry : loadPackageInformation().entrySet()) {
      //noinspection UseOfPropertiesAsHashtable
      CACHE.put(entry.getKey(), entry.getValue());
      debug("  MANIFEST: %s = %s", entry.getKey(), entry.getValue());
    }
    loaded = true;
    return CACHE;
  }

  static Properties loadBootstrapPropertiesUnchecked() {
    try {
      return loadBootstrapProperties();
    } catch (Exception e) {
      throw propagate(e);
    }
  }

  private static Properties loadPackageInformation() {
    Properties versionProperties = new Properties();
    Optional<Manifest> manifest = loadManifest();
    if (manifest.isPresent()) {
      for (Map.Entry<Object, Object> attributeEntry : manifest.get().getMainAttributes().entrySet()) {
        String manifestEntryKey = attributeEntry.getKey().toString().toLowerCase();
        if (manifestEntryKey.startsWith("X-")) {
          manifestEntryKey = manifestEntryKey.substring(2);
        }
        String versionPropertiesKey = "dropship." + CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, manifestEntryKey);
        System.setProperty(versionPropertiesKey, attributeEntry.getValue().toString());
        versionProperties.setProperty(versionPropertiesKey, attributeEntry.getValue().toString());
      }
    }
    return versionProperties;
  }

  private static Optional<Manifest> loadManifest() {
    Optional<URL> location = Optional.fromNullable(Resources.getResource(CharMatcher.is('.').replaceFrom(Dropship.class.getName(), '/') + ".class"));
    if (!location.isPresent()) {
      return Optional.absent();
    }

    try {
      String classPath = location.get().toString();
      if (!classPath.startsWith("jar")) {
        // Class not from JAR
        return Optional.absent();
      }
      String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
      return Optional.of(new Manifest(new URL(manifestPath).openStream()));
    } catch (MalformedURLException e) {
      return Optional.absent();
    } catch (IOException e) {
      return Optional.absent();
    }
  }
}
