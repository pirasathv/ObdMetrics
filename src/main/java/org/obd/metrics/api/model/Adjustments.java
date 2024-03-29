package org.obd.metrics.api.model;

import org.obd.metrics.codec.GeneratorSpec;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.ToString;


/**
 * It contains an additional settings used by {@link CommandProducer}
 * 
 * @since 0.6.0
 * @author tomasz.zebrowski
 */
@ToString
@Builder
public final class Adjustments {

	public static Adjustments DEFAULT = Adjustments.builder().build();


	/**
	 * Enables batch queries so that multiple PIDSs are read within single request/response to the ECU.
	 */
	@Getter
	@Default
	private final boolean batchEnabled = Boolean.FALSE;

	/**
	 * Add number of lines expected to return by Adapter which speedups the communication between Lib->Adapter.
	 */
	@Getter
	@Default
	private final boolean responseLengthEnabled = Boolean.TRUE;
	

	@Getter
	@Default
	private final GeneratorSpec generator = GeneratorSpec.DEFAULT;

	@Getter
	@Default
	private final AdaptiveTimeoutPolicy adaptiveTiming = AdaptiveTimeoutPolicy.DEFAULT;

	@Getter
	@Default
	private final ProducerPolicy producerPolicy = ProducerPolicy.DEFAULT;

	@Getter
	@Default
	private final CacheConfig cacheConfig = CacheConfig.DEFAULT;
}
