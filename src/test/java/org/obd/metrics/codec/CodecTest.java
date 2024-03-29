package org.obd.metrics.codec;

import java.util.Collection;

import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;
import org.obd.metrics.PidRegistryCache;
import org.obd.metrics.codec.formula.FormulaEvaluatorConfig;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.raw.RawMessage;

public interface CodecTest {

	default void assertEquals(String pid, String pidSource, String rawData, Object expectedValue) {
		assertEquals(false, pid, null, pidSource, rawData, expectedValue);
	}

	default void assertEquals(boolean debug, String pid, Long id, String pidSource, String rawData,
			Object expectedValue) {

		Assertions.assertThat(pid).isNotNull();
		Assertions.assertThat(pidSource).isNotNull();
		Assertions.assertThat(rawData).isNotNull();

		final CodecRegistry codecRegistry = CodecRegistry.builder().formulaEvaluatorConfig(
				FormulaEvaluatorConfig.builder().debug(debug).scriptEngine("JavaScript").build()).build();

		PidDefinition pidDef = null;
		if (id == null) {
			pidDef = PidRegistryCache.get(pidSource).findBy(pid);
		} else {
			Collection<PidDefinition> findAllBy = PidRegistryCache.get(pidSource)
					.findAllBy(PidRegistryCache.get(pidSource).findBy(pid));
			pidDef = findAllBy.stream().filter(p -> p.getId().equals(id)).findFirst().get();
		}

		Assertions.assertThat(pidDef).isNotNull();
		final Codec<?> codec = codecRegistry.findCodec(pidDef);

		if (codec == null) {
			Assertions.fail("No codec available for PID: {}", pid);
		} else {
			final Object actualValue = codec.decode(pidDef, RawMessage.wrap(rawData.getBytes()));
			Assertions.assertThat(actualValue).isEqualTo(expectedValue);
		}
	}

	default void assertCloseTo(boolean debug, String pid, String pidSource, String rawData, float expectedValue,
			float offset) {

		Assertions.assertThat(pid).isNotNull();
		Assertions.assertThat(pidSource).isNotNull();
		Assertions.assertThat(rawData).isNotNull();

		final CodecRegistry codecRegistry = CodecRegistry.builder().formulaEvaluatorConfig(
				FormulaEvaluatorConfig.builder().debug(debug).scriptEngine("JavaScript").build()).build();

		final PidDefinition pidDef = PidRegistryCache.get(pidSource).findBy(pid);
		Assertions.assertThat(pidDef).isNotNull();
		final Codec<?> codec = codecRegistry.findCodec(pidDef);

		if (codec == null) {
			Assertions.fail("No codec available for PID: {}", pid);
		} else {
			final Float actualValue = ((Number) codec.decode(pidDef, RawMessage.wrap(rawData.getBytes()))).floatValue();
			Assertions.assertThat(actualValue).isCloseTo(expectedValue, Offset.offset(offset));
		}
	}
}
