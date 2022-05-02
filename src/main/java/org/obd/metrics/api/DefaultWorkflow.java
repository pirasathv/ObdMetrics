package org.obd.metrics.api;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.obd.metrics.CommandLoop;
import org.obd.metrics.Lifecycle;
import org.obd.metrics.Reply;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.command.ATCommand;
import org.obd.metrics.command.group.DefaultCommandGroup;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.DelayCommand;
import org.obd.metrics.command.process.InitCompletedCommand;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.diagnostic.Diagnostics;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.AdapterConnection;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class DefaultWorkflow implements Workflow {

	private CommandProducer commandProducer;
	private final CommandsBuffer commandsBuffer = CommandsBuffer.instance();

	@Getter
	private Diagnostics diagnostics = Diagnostics.instance();

	@Getter
	private final PidDefinitionRegistry pidRegistry;

	private CodecRegistry codecRegistry;
	private ReplyObserver<Reply<?>> externalEventsObserver;
	private final String equationEngine;
	private final Lifecycle.Subscription subscription = Lifecycle.subscription;
	private final Lifecycle externalSubsciber;

	// just a single thread in a pool
	private static final ExecutorService singleTaskPool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
	        new LinkedBlockingQueue<Runnable>(1), new ThreadPoolExecutor.DiscardPolicy());

	protected DefaultWorkflow(
	        Pids pids,
	        String equationEngine,
	        ReplyObserver<Reply<?>> eventsObserver,
	        Lifecycle lifecycle) {

		log.info("Creating an instance of the '{}' workflow", getClass().getSimpleName());
		this.equationEngine = equationEngine;
		this.externalEventsObserver = eventsObserver;
		this.externalSubsciber = lifecycle;

		try (final Resources sources = Resources.convert(pids)) {
			this.pidRegistry = PidDefinitionRegistry.builder().sources(sources.getResources()).build();
		}
	}

	@Override
	public void stop() {
		log.info("Stopping the workflow...");
		commandsBuffer.clear();
		commandsBuffer.addFirst(new QuitCommand());
		log.info("Publishing lifecycle changes");
		subscription.onStopping();
	}

	@Override
	public void start(@NonNull AdapterConnection connection, @NonNull Query query,
	        @NonNull Init init, @NonNull Adjustments adjustements) {

		final Runnable task = () -> {
			final ExecutorService executorService = Executors.newFixedThreadPool(2);

			try {
				subscription.unregisterAll();
				
				codecRegistry = buildCodecRegistry(adjustements);
				commandProducer = buildCommandProducer(adjustements, getCommandsSupplier(adjustements,
				        query), init);

				initCommandBuffer(init);
				initLifecycleSubscribtion();

				log.info("Starting the workflow. Protocol: {}, headers: {}, adjustements: {}, selected PID's: {}",
				        init.getProtocol(), init.getHeaders(), adjustements, query.getPids());

				diagnostics.reset();

				
				@SuppressWarnings("unchecked")
				final CommandLoop commandLoop = CommandLoop
				        .builder()
				        .connection(connection)
				        .buffer(commandsBuffer)
				        .observer(externalEventsObserver)
				        .observer((ReplyObserver<Reply<?>>) diagnostics)
				        .pids(pidRegistry)
				        .codecs(codecRegistry)
				        
				        .lifecycle(subscription).build();

				executorService.invokeAll(Arrays.asList(commandLoop, commandProducer));

			} catch (Throwable e) {
				log.error("Failed to initialize the framework.", e);
			} finally {
				log.info("Stopping the Workflow.");
				subscription.onStopped();
				executorService.shutdown();
			}
		};

		singleTaskPool.submit(task);
	}

	private CommandProducer buildCommandProducer(Adjustments adjustements, Supplier<List<ObdCommand>> supplier, Init init) {
		return new CommandProducer(diagnostics, commandsBuffer, supplier, adjustements, init);
	}

	private CodecRegistry buildCodecRegistry(Adjustments adjustments) {
		return CodecRegistry.builder().equationEngine(getEquationEngine(equationEngine)).adjustments(adjustments)
		        .build();
	}

	private @NonNull String getEquationEngine(String equationEngine) {
		return equationEngine == null || equationEngine.length() == 0 ? "JavaScript" : equationEngine;
	}

	private void initLifecycleSubscribtion() {
		
		subscription.subscribe(externalSubsciber);
		subscription.subscribe(commandProducer);
		subscription.onConnecting();
	}

	private void initCommandBuffer(Init initConfiguration) {
		DefaultCommandGroup.SUPPORTED_PIDS.getCommands().forEach(p -> {
			codecRegistry.register(p.getPid(), p);
		});
		
		commandsBuffer.clear();
		commandsBuffer.add(initConfiguration.getSequence());
		// Protocol
		commandsBuffer.addLast(new ATCommand("SP" + initConfiguration.getProtocol().getType()));
		commandsBuffer.add(DefaultCommandGroup.SUPPORTED_PIDS);
		commandsBuffer.addLast(new DelayCommand(initConfiguration.getDelay()));
		commandsBuffer.addLast(new InitCompletedCommand());
	}

	private Supplier<List<ObdCommand>> getCommandsSupplier(Adjustments adjustements, Query query) {
		return new CommandsSuplier(pidRegistry, adjustements.isBatchEnabled(),
		        query);
	}
}
