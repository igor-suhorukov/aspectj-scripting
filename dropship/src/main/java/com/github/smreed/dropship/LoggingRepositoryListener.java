package com.github.smreed.dropship;

import com.google.common.collect.Maps;
import org.sonatype.aether.AbstractRepositoryListener;
import org.sonatype.aether.RepositoryEvent;
import org.sonatype.aether.artifact.Artifact;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.github.smreed.dropship.NotLogger.debug;
import static com.github.smreed.dropship.NotLogger.info;

class LoggingRepositoryListener extends AbstractRepositoryListener {

  private Map<String , Long> startTimes = Maps.newHashMap();

  private String artifactAsString(Artifact artifact) {
    return Settings.GAV_JOINER.join(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
  }

  @Override
  public void artifactDownloading(RepositoryEvent event) {
    super.artifactDownloading(event);
    Artifact artifact = event.getArtifact();
    String key = artifactAsString(artifact);
    startTimes.put(key, System.nanoTime());
  }

  @Override
  public void artifactDownloaded(RepositoryEvent event) {
    super.artifactDownloaded(event);
    Artifact artifact = event.getArtifact();
    String key = artifactAsString(artifact);
    long downloadTimeNanos = System.nanoTime() - startTimes.remove(key);
    double downloadTimeMs = TimeUnit.NANOSECONDS.toMillis(downloadTimeNanos);
    double downloadTimeSec = TimeUnit.NANOSECONDS.toSeconds(downloadTimeNanos);
    long size = artifact.getFile().length();
    double sizeK = (1 / 1024D) * size;
    double downloadRateKBytesPerSecond = sizeK / downloadTimeSec;
    info("Downloaded %s (%d bytes) in %gms (%g kbytes/sec).", key, size, downloadTimeMs, downloadRateKBytesPerSecond);
  }

  @Override
  public void artifactResolved(RepositoryEvent event) {
    super.artifactResolved(event);
    debug("Resolved %s.", artifactAsString(event.getArtifact()));
  }
}
