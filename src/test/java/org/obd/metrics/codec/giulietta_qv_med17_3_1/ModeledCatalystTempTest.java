package org.obd.metrics.codec.giulietta_qv_med17_3_1;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class ModeledCatalystTempTest implements Giulietta_QV_Med_17_3_1_Test {
	// 18370E = 20
	
	@ParameterizedTest
	@CsvSource(value = { 
//			"6218371E=60.0",
	        "6218370E=20.0",
			}, delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertEquals(input, Double.parseDouble(expected));
	}
}
