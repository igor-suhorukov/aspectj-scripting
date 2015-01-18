package org.aspectj.aspect;

import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.transform.FastFourierTransformer;

import static junit.framework.Assert.assertNotNull;

public class AgentTest {

    private FastFourierTransformer fourierTransformer  = new FastFourierTransformer();

    @org.junit.Test
    public void testInstrumentation() throws Exception {
        Complex[] transform = fourierTransformer.transform(new double[]{235, 32626, 7347, 267, 2, 990, 5, 8535});
        assertNotNull(transform);

    }

    @org.junit.Test(expected = java.lang.IllegalArgumentException.class)
    public void testException() throws Exception {
        Complex[] transform2 = fourierTransformer.transform(new double[]{235, 32626, 7347, 267, 2, 990, 5,});
    }
}
