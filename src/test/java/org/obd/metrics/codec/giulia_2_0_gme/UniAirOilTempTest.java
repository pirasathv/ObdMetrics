package org.obd.metrics.codec.giulia_2_0_gme;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class UniAirOilTempTest implements Giulia_2_0_GME_Test {

	@ParameterizedTest
	@CsvSource(value = { 
			"62198E0861=89.0",
			"62198E0649=67.0"
			}, delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertEquals(Boolean.TRUE,input, Double.parseDouble(expected));
	}
}
