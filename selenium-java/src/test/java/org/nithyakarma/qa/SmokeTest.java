package org.nithyakarma.qa;

import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class SmokeTest {
    @Test
    public void runnerIsWried() {
        assertThat(1 + 1).isEqualTo(2);
    }

}
