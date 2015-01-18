package com.github.smreed.dropship;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyFilter;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.repository.RepositoryPolicy;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.filter.ScopeDependencyFilter;
import org.sonatype.aether.util.graph.PreorderNodeListGenerator;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import static com.github.smreed.dropship.NotLogger.info;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;

public final class MavenClassLoader {

  public static class ClassLoaderBuilder {

    private static final String COMPILE_SCOPE = "compile";
    private static final ClassLoader SHARE_NOTHING = null;

    private final List<RemoteRepository> repositories;
    private final File localRepositoryDirectory;

    private ClassLoaderBuilder(RemoteRepository... repositories) {
      checkNotNull(repositories);
      checkArgument(repositories.length > 0, "Must specify at least one remote repository.");

      this.repositories = ImmutableList.copyOf(repositories);
      this.localRepositoryDirectory = new File(Settings.localRepoPath());
    }

    public URLClassLoader forMavenCoordinates(String groupArtifactVersion) {
      try {
        info("Collecting maven metadata.");
        CollectRequest collectRequest = createCollectRequestForGAV(groupArtifactVersion);

        info("Resolving dependencies.");
        List<Artifact> artifacts = collectDependenciesIntoArtifacts(collectRequest);

        info("Building classpath for %s from %d URLs.", groupArtifactVersion, artifacts.size());
        List<URL> urls = Lists.newArrayListWithExpectedSize(artifacts.size());
        for (Artifact artifact : artifacts) {
          urls.add(artifact.getFile().toURI().toURL());
        }

        for (String path : Settings.additionalClasspathPaths()) {
          info("Adding \"%s\" to classpath.", path);
          urls.add(new File(path).toURI().toURL());
        }

        return new URLClassLoader(Iterables.toArray(urls, URL.class), SHARE_NOTHING);
      } catch (Exception e) {
        throw propagate(e);
      }
    }

    private CollectRequest createCollectRequestForGAV(String gav) {
      DefaultArtifact artifact = new DefaultArtifact(gav);
      Dependency dependency = new Dependency(artifact, COMPILE_SCOPE);

      CollectRequest collectRequest = new CollectRequest();
      collectRequest.setRoot(dependency);
      for (RemoteRepository repository : repositories) {
        collectRequest.addRepository(repository);
      }

      return collectRequest;
    }

    private List<Artifact> collectDependenciesIntoArtifacts(CollectRequest collectRequest)
      throws PlexusContainerException, ComponentLookupException, DependencyCollectionException, ArtifactResolutionException, DependencyResolutionException {

      RepositorySystem repositorySystem = newRepositorySystem();
      RepositorySystemSession session = newSession(repositorySystem);
      DependencyNode node = repositorySystem.collectDependencies(session, collectRequest).getRoot();

      DependencyFilter filter = new ScopeDependencyFilter();

      DependencyRequest request = new DependencyRequest(node, filter);

      repositorySystem.resolveDependencies(session, request);

      PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();
      node.accept(nlg);

      return nlg.getArtifacts(false);
    }

    private RepositorySystem newRepositorySystem() throws PlexusContainerException, ComponentLookupException {
      return new DefaultPlexusContainer().lookup(RepositorySystem.class);
    }

    private RepositorySystemSession newSession(RepositorySystem system) {
      MavenRepositorySystemSession session = new MavenRepositorySystemSession();

      session.setRepositoryListener(new LoggingRepositoryListener());
      session.setChecksumPolicy(RepositoryPolicy.CHECKSUM_POLICY_FAIL);
      session.setIgnoreInvalidArtifactDescriptor(false);
      session.setIgnoreMissingArtifactDescriptor(false);
      session.setNotFoundCachingEnabled(false);
      session.setTransferErrorCachingEnabled(false);
      session.setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_ALWAYS);

      LocalRepository localRepo = new LocalRepository(localRepositoryDirectory);
      session.setLocalRepositoryManager(system.newLocalRepositoryManager(localRepo));

      return session;
    }

  }

  /**
   * Creates a classloader that will resolve artifacts against the default "central" repository. Throws
   * {@link IllegalArgumentException} if the GAV is invalid, {@link NullPointerException} if the GAV is null.
   *
   * @param gav artifact group:artifact:version, i.e. joda-time:joda-time:1.6.2
   * @return a classloader that can be used to load classes from the given artifact
   */
  public static URLClassLoader forMavenCoordinates(String gav) {
    return usingCentralRepo().forMavenCoordinates(checkNotNull(gav));
  }

  public static ClassLoaderBuilder using(String url) {
    RemoteRepository custom = new RemoteRepository("custom", "default", url);
    return new ClassLoaderBuilder(custom);
  }

  public static ClassLoaderBuilder usingCentralRepo() {
    RemoteRepository central = new RemoteRepository("central", "default", "http://repo1.maven.org/maven2/");
    return new ClassLoaderBuilder(central);
  }

}
