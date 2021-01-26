package org.obd.metrics;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.codec.batch.Batchable;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.process.DelayCommand;
import org.obd.metrics.command.process.InitCompletedCommand;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.connection.Connection;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;
import rx.subjects.PublishSubject;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommandExecutor implements Callable<String> {

	private static final String NO_DATA = "no data";
	private static final String STOPPED = "stopped";
	private static final String UNABLE_TO_CONNECT = "unable to connect";

	private Connection connection;
	private CommandsBuffer buffer;
	private PublishSubject<Metric<?>> publisher = PublishSubject.create();
	private ExecutorPolicy policy;
	private CodecRegistry codecRegistry;
	private StatusObserver statusObserver;

	@Builder
	static CommandExecutor build(@NonNull Connection connection, @NonNull CommandsBuffer buffer,
			@Singular("subscribe") List<MetricsObserver> subscribe, @NonNull ExecutorPolicy policy,
			@NonNull CodecRegistry codecRegistry, @NonNull StatusObserver statusObserver) {

		var commandExecutor = new CommandExecutor();
		commandExecutor.connection = connection;
		commandExecutor.buffer = buffer;
		commandExecutor.policy = policy;
		commandExecutor.codecRegistry = codecRegistry;
		commandExecutor.statusObserver = statusObserver;

		if (null == subscribe || subscribe.isEmpty()) {
			log.info("No subscriber specified.");
		} else {
			subscribe.forEach(s -> commandExecutor.publisher.subscribe(s));
		}
		return commandExecutor;
	}

	@Override
	public String call() throws Exception {

		log.info("Starting command executor thread..");

		try (final Connections conn = Connections.builder().connection(connection).build()) {
			while (true) {
				Thread.sleep(policy.getFrequency());
				while (!buffer.isEmpty()) {

					if (conn.isFaulty()) {
						var message = "Device connection is faulty. Finishing communication.";
						log.error(message);
						publishQuitCommand();
						statusObserver.onError(message, null);
						return null;
					} else {

						var command = buffer.get();
						if (command instanceof DelayCommand) {
							final DelayCommand delayCommand = (DelayCommand) command;
							TimeUnit.MILLISECONDS.sleep(delayCommand.getDelay());

						} else if (command instanceof QuitCommand) {
							log.info("Stopping command executor thread. Finishing communication.");
							publishQuitCommand();
							return null;

						} else if (command instanceof InitCompletedCommand) {
							log.info("Initialization is completed.");
							statusObserver.onConnected();
						} else {

							var data = conn.transmit(command).receive();

							if (null == data || data.length() == 0) {
								log.debug("Recieved no data.");
								continue;
							} else if (data.contains(STOPPED) || data.contains(UNABLE_TO_CONNECT)) {
								statusObserver.onError(data, null);
							} else if (data.contains(NO_DATA)) {
								log.debug("Recieved no data.");
							} else if (command instanceof Batchable) {
								((Batchable) command).decode(data).forEach(this::decodeAndPublish);
								continue;
							}
							decodeAndPublish(command, data);
						}
					}
				}
			}
		} catch (Throwable e) {
			publishQuitCommand();
			var message = String.format("Command executor failed: %s", e.getMessage());
			log.error(message, e);
			statusObserver.onError(message, e);
		}

		return null;
	}

	private void decodeAndPublish(final Command command, final String data) {
		var decoded = codecRegistry.findCodec(command).map(p -> p.decode(data)).orElse(null);
		publisher.onNext(Metric.builder().command(command).raw(data).value(decoded).build());
	}

	private void publishQuitCommand() {
		publisher.onNext(Metric.builder().command(new QuitCommand()).build());
	}
}