package org.obd.metrics.codec;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.obd.metrics.codec.Codec;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidRegistry;

public interface PidTest {

	default void mode01Test(String rawData, Object expectedValue) throws IOException {
		modeTest("01", rawData.substring(2, 4), "mode01.json", rawData, expectedValue);
	}

	default void mode22Test(String rawData, Object expectedValue) throws IOException {
		modeTest("22", rawData.substring(2, 6), "alfa.json", rawData, expectedValue);
	}

	default void modeTest(String mode, String pid, String pidSource, String rawData, Object expectedValue)
			throws IOException {

		Assertions.assertThat(mode).isNotNull();
		Assertions.assertThat(pid).isNotNull();
		Assertions.assertThat(pidSource).isNotNull();
		Assertions.assertThat(rawData).isNotNull();

		try (final InputStream source = Thread.currentThread().getContextClassLoader().getResourceAsStream(pidSource)) {
			final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();
			
			final CodecRegistry codecRegistry = CodecRegistry.builder().pids(pidRegistry).equationEngine("JavaScript").build();
			final PidDefinition pidDef = pidRegistry.findBy(mode, pid);
			Assertions.assertThat(pidDef).isNotNull();
			final Optional<Codec<?>> codec = codecRegistry.findCodec(new ObdCommand(pidDef));

			if (codec.isPresent()) {
				final Object value = codec.get().decode(rawData);
				Assertions.assertThat(value).isEqualTo(expectedValue);
			} else {
				Assertions.fail("No codec available for PID: {}", pid);
			}
		}
	}
}