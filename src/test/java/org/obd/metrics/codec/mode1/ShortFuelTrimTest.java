package org.obd.metrics.codec.mode1;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class ShortFuelTrimTest implements Mode01Test {

	
	@Test
	public void case_01() {
		final Map<String, Number> mappings = new HashMap<String, Number>() {
			private static final long serialVersionUID = 1L;
			{
				put("410680", 0.0);
			}
		};
		mappings.forEach((k, v) -> {
			assertEquals(k, v);
		});
	}
}
