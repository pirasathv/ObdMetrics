package org.obd.metrics;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.command.process.DelayCommand;
import org.obd.metrics.command.process.InitCompletedCommand;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.connection.Connection;
import org.obd.metrics.connection.Connections;
import org.obd.metrics.pid.PidRegistry;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;
import rx.subjects.PublishSubject;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommandLoop implements Callable<String> {

	private Connection connection;
	private CommandsBuffer buffer;
	private PublishSubject<Reply> publisher = PublishSubject.create();
	private CommandLoopPolicy policy;
	private CodecRegistry codecRegistry;
	private StatusObserver statusObserver;
	private PidRegistry pids;

	@Builder
	static CommandLoop build(@NonNull Connection connection, @NonNull CommandsBuffer buffer,
			@Singular("observer") List<ReplyObserver> observers, @NonNull CommandLoopPolicy policy,
			@NonNull CodecRegistry codecRegistry, @NonNull StatusObserver statusObserver,@NonNull PidRegistry pids) {

		var loop = new CommandLoop();
		loop.connection = connection;
		loop.buffer = buffer;
		loop.policy = policy;
		loop.codecRegistry = codecRegistry;
		loop.statusObserver = statusObserver;
		loop.pids = pids;
		
		if (null == observers || observers.isEmpty()) {
			log.info("No subscriber specified.");
		} else {
			observers.forEach(s -> loop.publisher.subscribe(s));
		}
		return loop;
	}

	@Override
	public String call() throws Exception {

		log.info("Starting command executor thread..");

		try (final Connections conn = Connections.builder().connection(connection).build()) {
			final CommandExecutor commandExecutor = CommandExecutor
					.builder()
					.codecRegistry(codecRegistry)
					.connections(conn)
					.pids(pids)
					.publisher(publisher)
					.statusObserver(statusObserver).build();

			while (true) {
				while (!buffer.isEmpty()) {
					Thread.sleep(policy.getFrequency());
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
							commandExecutor.execute(command);
						}
					}
				}
			
				Thread.sleep(policy.getFrequency());
			}
		} catch (Throwable e) {
			publishQuitCommand();
			var message = String.format("Command executor failed: %s", e.getMessage());
			log.trace(message, e);
			statusObserver.onError(message, e);
		}

		return null;
	}

	private void publishQuitCommand() {
		publisher.onNext(Reply.builder().command(new QuitCommand()).build());
	}
}