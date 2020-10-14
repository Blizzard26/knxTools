package org.openhab.support.knx2openhab.velocity;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

class VelocityToolsTest
{

    @Test
    void testContainsPrefix()
    {
        assertThat(true, equalTo(VelocityTools.containsPrefix(Arrays.asList("1.001", "1."), "1.001")));
        assertThat(true, equalTo(VelocityTools.containsPrefix(Arrays.asList("1.001", "1."), "1.005")));
        assertThat(false, equalTo(VelocityTools.containsPrefix(Arrays.asList("1.001", "1."), "2.001")));
    }

}
