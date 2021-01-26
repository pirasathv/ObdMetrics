package org.obd.metrics.integration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections4.MultiValuedMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.CommandExecutor;
import org.obd.metrics.CommandsBuffer;
import org.obd.metrics.DataCollector;
import org.obd.metrics.ExecutorPolicy;
import org.obd.metrics.Metric;
import org.obd.metrics.StatusObserver;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.group.Mode1CommandGroup;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.connection.Connection;
import org.obd.metrics.pid.PidRegistry;

//its not really a test ;)
public class IntegrationTest extends IntegrationTestBase {

	@Test
	public void t1() throws IOException, InterruptedException, ExecutionException {
		
		final Connection connection = openConnection();
		
		final InputStream source = Thread.currentThread().getContextClassLoader().getResourceAsStream("mode01.json");

		final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();

		final CommandsBuffer buffer = CommandsBuffer.instance(); // Define command buffer
		buffer.add(Mode1CommandGroup.INIT); // Add protocol initialization AT commands
		buffer.add(Mode1CommandGroup.SUPPORTED_PIDS); // Request for supported PID's

		// Read signals from the device
		final ObdCommand intakeAirTempCommand = new ObdCommand(pidRegistry.findBy("01", "0F"));// Intake air temperature
		buffer.add(intakeAirTempCommand)
			.add(new ObdCommand(pidRegistry.findBy("01", "0C"))) // Engine rpm
			.add(new ObdCommand(pidRegistry.findBy("01", "10"))) // Maf
			.add(new ObdCommand(pidRegistry.findBy("01", "0B"))) // Intake manifold pressure
			.add(new ObdCommand(pidRegistry.findBy("01", "0D"))) // Vehicle speed
			.add(new ObdCommand(pidRegistry.findBy("01", "05"))) // Engine temp
			.add(new QuitCommand());// Last command that will close the communication

		final DataCollector collector = new DataCollector(); // It collects the

		final CodecRegistry codecRegistry = CodecRegistry.builder().equationEngine("JavaScript").pids(pidRegistry)
				.build();

		final CommandExecutor executor = CommandExecutor
				.builder()
				.connection(connection)
				.buffer(buffer)
				.subscribe(collector)
				.policy(ExecutorPolicy.DEFAULT)
				.codecRegistry(codecRegistry)
				.statusObserver(StatusObserver.DEFAULT).build();

		final ExecutorService executorService = Executors.newFixedThreadPool(1);
		executorService.invokeAll(Arrays.asList(executor));

		final MultiValuedMap<Command, Metric<?>> data = collector.getData();
		Assertions.assertThat(data.containsKey(intakeAirTempCommand));

		final Collection<Metric<?>> collection = data.get(intakeAirTempCommand);
		Assertions.assertThat(collection.iterator().hasNext()).isTrue();

		// 133 ??
		Assertions.assertThat(collection.iterator().next().getValue()).isEqualTo(133.0);

		executorService.shutdown();
		source.close();
	}

	@Test
	public void t2() throws IOException, InterruptedException, ExecutionException {
		for (int i = 0; i < 5; i++) {

			final Connection connection = openConnection();
			Assertions.assertThat(connection).isNotNull();

			final InputStream source = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream("mode01.json");

			final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();

			final CommandsBuffer buffer = CommandsBuffer.instance(); // Define command buffer
			buffer.add(Mode1CommandGroup.INIT); // Add protocol initialization AT commands
			buffer.add(Mode1CommandGroup.SUPPORTED_PIDS); // Request for supported PID's

			// Read signals from the device
			final ObdCommand intakeAirTempCommand = new ObdCommand(pidRegistry.findBy("01", "0F"));// Intake air
																									// temperature
			buffer.add(intakeAirTempCommand).add(new ObdCommand(pidRegistry.findBy("01", "0C"))) // Engine rpm
					.add(new ObdCommand(pidRegistry.findBy("01", "10"))) // Maf
					.add(new ObdCommand(pidRegistry.findBy("01", "0B"))) // Intake manifold pressure
					.add(new ObdCommand(pidRegistry.findBy("01", "0D"))) // Behicle speed
					.add(new ObdCommand(pidRegistry.findBy("01", "05"))) // Engine temp
					.add(new QuitCommand());// Last command that will close the communication

			final DataCollector collector = new DataCollector();

			final CodecRegistry codecRegistry = CodecRegistry.builder().equationEngine("JavaScript").pids(pidRegistry)
					.build();

			final CommandExecutor executor = CommandExecutor.builder().connection(connection).buffer(buffer)
					.subscribe(collector).policy(ExecutorPolicy.DEFAULT)
					.codecRegistry(codecRegistry).statusObserver(StatusObserver.DEFAULT).build();

			final ExecutorService executorService = Executors.newFixedThreadPool(1);
			executorService.invokeAll(Arrays.asList(executor));

			final MultiValuedMap<Command, Metric<?>> data = collector.getData();
			Assertions.assertThat(data.containsKey(intakeAirTempCommand));

			final Collection<Metric<?>> collection = data.get(intakeAirTempCommand);
			Assertions.assertThat(collection.iterator().hasNext()).isTrue();

			// 133 ??
			Assertions.assertThat(collection.iterator().next().getValue()).isEqualTo(133.0);

			executorService.shutdown();
			source.close();
		}
	}
}