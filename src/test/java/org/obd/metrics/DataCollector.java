package org.obd.metrics;

import java.util.List;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.obd.metrics.command.Command;
import org.obd.metrics.pid.PidDefinition;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public final class DataCollector extends ReplyObserver<Reply<?>> {

	@Getter
	private final MultiValuedMap<Command, Reply<?>> data = new ArrayListValuedHashMap<Command, Reply<?>>();

	private final MultiValuedMap<PidDefinition, ObdMetric> metrics = new ArrayListValuedHashMap<PidDefinition, ObdMetric>();

	public List<ObdMetric> findMetricsBy(PidDefinition pidDefinition) {
		return (List<ObdMetric>) metrics.get(pidDefinition);
	}

	@Override
	public void onNext(Reply<?> reply) {
		log.trace("Receive data: {}", reply.toString());
		data.put(reply.getCommand(), reply);

		if (reply instanceof ObdMetric) {
			metrics.put(((ObdMetric) reply).getCommand().getPid(), (ObdMetric) reply);
		}
	}
}
