package org.obd.metrics.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Executors;

import org.obd.metrics.CommandLoop;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.Lifecycle;
import org.obd.metrics.command.group.Mode1CommandGroup;
import org.obd.metrics.command.process.InitCompletedCommand;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class Mode1Workflow extends AbstractWorkflow {

	Mode1Workflow(@NonNull EcuSpecific ecuSpecific, String equationEngine, @NonNull ReplyObserver observer,
	        Lifecycle lifecycle, Long commandFrequency, GeneratorSpec generator) throws IOException {
		super(ecuSpecific, equationEngine, observer, lifecycle, commandFrequency, generator);
	}

	@Override
	public void start(WorkflowContext ctx) {
	
		final Runnable task = () -> {

			lifecycle.onConnecting();
			comandsBuffer.clear();
			ecuSpecific.getSequences().forEach(comandsBuffer::add);
			comandsBuffer.add(Mode1CommandGroup.SUPPORTED_PIDS);
			comandsBuffer.add(new InitCompletedCommand());

			log.info("Starting the workflow: {}. Selected PID's: {}", getClass().getSimpleName(), ctx.filter);

			var producer = new Mode1Producer(comandsBuffer, producerPolicy, pids, ctx.filter, ctx.batchEnabled);
			
			var executor = CommandLoop
					.builder()
					.connection(ctx.connection)
					.buffer(comandsBuffer)
					.observer(producer)
					.observer(replyObserver)
					.observer(statistics)
					.pids(pids)
					.policy(executorPolicy)
					.codecRegistry(codec)
					.lifecycle(lifecycle)
					.build();

			var executorService = Executors.newFixedThreadPool(2);

			try {
				executorService.invokeAll(Arrays.asList(executor, producer));
			} catch (InterruptedException e) {
				log.error("Failed to schedule workers.", e);
			} finally {
				lifecycle.onStopped();
				executorService.shutdown();
			}
		};

		singleTaskPool.submit(task);
	}
}
