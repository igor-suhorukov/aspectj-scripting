package org.aspectj.configuration;

import org.junit.Test;

public class ArtifactConfigurationTest {
    /**
     * https://github.com/igor-suhorukov/aspectj-scripting/issues/2
     */
    @Test(expected = IllegalArgumentException.class)
    public void testEmptyArtifact() throws Exception {
        AspectJDescriptor.loadConfiguration(ArtifactConfigurationTest.class.getResource("/emptyArtifacts.xml").toString());
    }
    @Test(expected = IllegalArgumentException.class)
    public void testEmptyGlobalArtifact() throws Exception {
        AspectJDescriptor.loadConfiguration(ArtifactConfigurationTest.class.getResource("/emptyGlobalArtifacts.xml").toString());
    }
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidArtifactValue() throws Exception {
        AspectJDescriptor.loadConfiguration(ArtifactConfigurationTest.class.getResource("/invalidArtifactsValue.xml").toString());
    }
}
