package org.obd.metrics.command.obd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.obd.metrics.codec.AnswerCodeDecoder;
import org.obd.metrics.codec.batch.Batchable;
import org.obd.metrics.pid.PidDefinition;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BatchObdCommand extends ObdCommand implements Batchable {

	private final List<ObdCommand> commands;
	private final String predictedAnswerCode;
	private final Map<String, Pattern> queryPatterns = new HashMap<>();

	@Getter
	private final int priority;

	public BatchObdCommand(String query, List<ObdCommand> commands, int priority) {
		super(query);
		this.commands = commands;
		this.priority = priority;
		this.predictedAnswerCode = new AnswerCodeDecoder()
		        .getPredictedAnswerCode(commands.iterator().next().getPid().getMode());
	}
	
	public int getCacheHit(String query) {
		return queryPatterns.get(query).hit;
	}
	
	@Override
	public Map<ObdCommand, String> decode(String message) {
		final Map<ObdCommand, String> values = new HashMap<ObdCommand, String>();

		final String normalized = message.replaceAll("[a-zA-Z0-9]{1}\\:", "");
		int indexOfAnswerCode = normalized.indexOf(predictedAnswerCode);

		if (indexOfAnswerCode == 0 || indexOfAnswerCode == 3) {
			if (queryPatterns.containsKey(query)) {
				final Pattern pattern = queryPatterns.get(query);
				pattern.hit++;
				pattern.entries.forEach(p -> {
					final String pidValue = normalized.substring(p.start, p.end);
					if (log.isTraceEnabled()) {
						log.info("Cache: {} = {} : {} : {}", p.command.pid.getPid(), p.start, p.end, pidValue);
					}
					values.put(p.command, predictedAnswerCode + p.command.pid.getPid() + pidValue);
				});
			} else {
				int messageIndex = indexOfAnswerCode + 2;
				final Pattern cache = new Pattern();
				for (final ObdCommand command : commands) {

					if (messageIndex == normalized.length()) {
						break;
					}

					final PidDefinition pid = command.pid;
					final int sizeOfPid = messageIndex + 2;
					final String pidSeq = normalized.substring(messageIndex, sizeOfPid).toUpperCase();
					if (pidSeq.equalsIgnoreCase(pid.getPid())) {

						final int pidLength = pid.getLength() * 2;
						final String pidValue = normalized.substring(sizeOfPid, sizeOfPid + pidLength);

						if (log.isTraceEnabled()) {
							log.trace("Init: {} =  {} : {} : {}", pidSeq, sizeOfPid, (sizeOfPid + pidLength), pidValue);
						}

						values.put(command, predictedAnswerCode + pid.getPid() + pidValue);
						cache.entries.add(new PatternEntry(command, sizeOfPid, (sizeOfPid + pidLength)));
						messageIndex += pidLength + 2;
						continue;
					}
				}
				queryPatterns.put(query, cache);
			}
		} else {
			log.warn("Answer code was not correct for message: {}. Query: {}", message, query);
		}

		return values;
	}

	@Override
	public String toString() {
		return "[priority=" + priority + ", query=" + query + "]";
	}
}
