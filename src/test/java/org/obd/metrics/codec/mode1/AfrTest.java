package org.obd.metrics.codec.mode1;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.obd.metrics.codec.PidTest;

public class AfrTest implements PidTest {
	@Test
	public void t1() throws IOException {
		mode01Test("41155aff", 10.01);
	}
}
