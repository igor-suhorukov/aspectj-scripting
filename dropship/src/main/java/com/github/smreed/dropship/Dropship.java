package com.github.smreed.dropship;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.Properties;

import static com.github.smreed.dropship.NotLogger.info;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class Dropship {

  private static MavenClassLoader.ClassLoaderBuilder classLoaderBuilder() {
    Optional<String> override = Settings.mavenRepoUrl();
    if (override.isPresent()) {
      info("Will load artifacts from %s", override);
      return MavenClassLoader.using(override.get());
    } else {
      return MavenClassLoader.usingCentralRepo();
    }
  }

  private static String resolveGav(String gav) {
    ImmutableList<String> tokens = ImmutableList.copyOf(Settings.GAV_SPLITTER.split(gav));

    checkArgument(tokens.size() > 1, "Require groupId:artifactId[:version]");
    checkArgument(tokens.size() < 4, "Require groupId:artifactId[:version]");

    if (tokens.size() > 2) {
      return gav;
    }

    Properties settings = Settings.loadBootstrapPropertiesUnchecked();

    if (settings.containsKey(gav)) {
      return Settings.GAV_JOINER.join(tokens.get(0), tokens.get(1), settings.getProperty(gav));
    } else {
      return Settings.GAV_JOINER.join(tokens.get(0), tokens.get(1), "[0,)");
    }
  }

  public static void main(String[] args) throws Exception {
    args = checkNotNull(args);
    checkArgument(args.length >= 2, "Must specify groupId:artifactId[:version] and classname!");

    info("Starting Dropship v%s", Settings.dropshipVersion());

    String gav = resolveGav(args[0]);

    info("Requested %s, will load artifact and dependencies for %s.", args[0], gav);

    URLClassLoader loader = classLoaderBuilder().forMavenCoordinates(gav);

    System.setProperty("dropship.running", "true");

    Class<?> mainClass = loader.loadClass(args[1]);

    Thread.currentThread().setContextClassLoader(loader);

    Method mainMethod = mainClass.getMethod("main", String[].class);

    Iterable<String> mainArgs = Iterables.skip(ImmutableList.copyOf(args), 2);
    mainMethod.invoke(null, (Object) Iterables.toArray(mainArgs, String.class));
  }
}
