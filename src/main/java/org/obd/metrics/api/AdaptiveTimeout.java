package org.obd.metrics.api;

import java.time.Duration;
import java.time.Instant;

import org.obd.metrics.AdaptiveTimeoutPolicy;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class AdaptiveTimeout {

	private Instant start = Instant.now();

	@Getter
	private long currentTimeout;

	private final AdaptiveTimeoutPolicy policy;

	AdaptiveTimeout(final AdaptiveTimeoutPolicy policy) {

		this.policy = policy;
		this.currentTimeout = 1000 / policy.getCommandFrequency();

		if (this.currentTimeout < policy.getMinimumTimeout()) {
			this.currentTimeout = policy.getMinimumTimeout();
		}

		log.info("Timeout: {}ms for expected command frequency: {}, "
		        + "adaptive timing enabled: {}, check interval: {}",
		        currentTimeout,
		        policy.getCommandFrequency(),
		        policy.isEnabled(),
		        policy.getCheckInterval());

	}

	void update(double currentCommandFrequency) {

		if (Duration.between(start, Instant.now()).toMillis() >= policy
		        .getCheckInterval()) {

			log.info("Current RPS: {},requested RPS: {}, current timeout: {}",
			        currentCommandFrequency, policy.getCommandFrequency(),
			        currentTimeout);

			if (policy.isEnabled()) {
				if (currentCommandFrequency < policy.getCommandFrequency()) {
					if (currentTimeout > policy.getMinimumTimeout()) {
						long newTimeout = currentTimeout - 10;
						if (newTimeout < policy.getMinimumTimeout()) {
							newTimeout = policy.getMinimumTimeout();
						}
						log.info("Current RPS: {} is bellow requested: {}. Decreasing timeout to: {}",
						        currentCommandFrequency, policy.getCommandFrequency(), newTimeout);
						currentTimeout = newTimeout;
					} else {
						log.debug("Current timeout is bellow minimum value which is {}",
						        policy.getMinimumTimeout());
					}
				} else {
					// increase timeout if it is highly above expected throughput
				}
			}
			start = Instant.now();
		}
	}
}