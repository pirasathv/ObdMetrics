package org.obd.metrics.api.integration;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.DataCollector;
import org.obd.metrics.api.EcuSpecific;
import org.obd.metrics.api.Workflow;
import org.obd.metrics.api.WorkflowFactory;
import org.obd.metrics.command.group.Mode1CommandGroup;
import org.obd.metrics.connection.Connection;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PerformanceTest extends IntegrationTestBase {

	@Test
	public void t0() throws IOException, InterruptedException, ExecutionException {
		final Connection connection = openConnection();
		final DataCollector collector = new DataCollector();

		final Workflow workflow = WorkflowFactory
				.mode1()
				.ecuSpecific(EcuSpecific
						.builder()
						.initSequence(Mode1CommandGroup.INIT)
						.pidFile("mode01.json").build())
				.observer(collector).initialize();
		
		final Set<Long> ids = new HashSet<>();
		ids.add(6l);  // Engine coolant temperature
		ids.add(12l); // Intake manifold absolute pressure
		ids.add(13l); // Engine RPM
		ids.add(16l); // Intake air temperature
		ids.add(18l); // Throttle position
		ids.add(14l); // Vehicle speed

		workflow.filter(ids).batch(true).start(connection);
		final Callable<String> end = () -> {
			Thread.sleep(1 * 60000);
			log.info("Ending the process of collecting the data");
			workflow.stop();
			return "end";
		};

		final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(1);
		newFixedThreadPool.invokeAll(Arrays.asList(end));
		
		final PidRegistry pids = workflow.getPids();
		
		final PidDefinition engineTemp = pids.findBy(6l);
		Assertions.assertThat(engineTemp.getPid()).isEqualTo("05");

		double ratePerSec05 = workflow.getStatistics().getRatePerSec(engineTemp);
		double ratePerSec0C = workflow.getStatistics().getRatePerSec(pids.findBy(12l));

		log.info("Rate: 0105: {}", ratePerSec05);
		log.info("Rate: 010C: {}", ratePerSec0C);

		Assertions.assertThat(ratePerSec05).isGreaterThan(10d);
		Assertions.assertThat(ratePerSec0C).isGreaterThan(10d);

		newFixedThreadPool.shutdown();
	}
}