package org.obd.metrics.api;

import java.io.IOException;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.DataCollector;
import org.obd.metrics.ObdMetric;
import org.obd.metrics.WorkflowFinalizer;

public class Mode01Test {

	@Test
	public void nonBatchTest() throws IOException, InterruptedException {

		final DataCollector collector = new DataCollector();
		final Workflow workflow = SimpleWorkflowFactory.getMode01Workflow(collector);

		final Query query = Query.builder()
		        .pid(6l) // Engine coolant temperature
		        .pid(12l) // Intake manifold absolute pressure
		        .pid(13l) // Engine RPM
		        .pid(16l) // Intake air temperature
		        .pid(18l) // Throttle position
		        .pid(14l) // Vehicle speed
		        .build();

		final MockConnection connection = MockConnection.builder()
		        .commandReply("0100", "4100be3ea813")
		        .commandReply("0200", "4140fed00400")
		        .commandReply("0105", "410522")
		        .commandReply("010C", "410c541B")
		        .commandReply("010D", "")
		        .commandReply("0111", "no data")
		        .commandReply("010B", "410b35")
		        .readTimeout(0)
		        .build();

		workflow.start(connection, query);

		WorkflowFinalizer.finalizeAfter500ms(workflow);

		// Ensure we receive AT command as well
		Assertions.assertThat(collector.findATResetCommand()).isNotNull();

		final List<ObdMetric> collection = collector.findMetricsBy(workflow.getPidRegistry().findBy(6l));
		Assertions.assertThat(collection.isEmpty()).isFalse();
		final ObdMetric metric = collection.iterator().next();

		Assertions.assertThat(metric.getValue()).isInstanceOf(Integer.class);
		Assertions.assertThat(metric.getValue()).isEqualTo(-6);
		Assertions.assertThat(metric.valueToDouble()).isEqualTo(-6.0);
		Assertions.assertThat(metric.valueToString()).isEqualTo("-6");
	}

	@Test
	public void batchTest() throws IOException, InterruptedException {
		
		//Create an instance of DataCollector that receives the OBD Metrics
		var collector = new DataCollector();

		//Getting the Workflow instance for mode 01
		var workflow = SimpleWorkflowFactory.getMode01Workflow(collector);
		
		//Query for specified PID's like: Engine coolant temperature
		var query = Query.builder()
		        .pid(6l) // Engine coolant temperature
		        .pid(12l) // Intake manifold absolute pressure
		        .pid(13l) // Engine RPM
		        .pid(16l) // Intake air temperature
		        .pid(18l) // Throttle position
		        .pid(14l) // Vehicle speed
		        .build();
		
		//Create an instance of mock connection with additional commands and replies 
		var connection = MockConnection.builder()
		        .commandReply("0100", "4100be3ea813")
		        .commandReply("0200", "4140fed00400")
		        .commandReply("01 0B 0C 11 0D 0F 05", "00e0:410bff0c00001:11000d000f00052:00aaaaaaaaaaaa").build();

		//Enabling batch commands
		var optional = Adjustments
		        .builder()
		        .batchEnabled(true)
		        .build();

		//Start background threads, that call the adapter,decode the raw data, and populates OBD metrics
		workflow.start(connection, query, optional);
		
		// Starting the workflow completion job, it will end workflow after some period of time (helper method)
		WorkflowFinalizer.finalizeAfter500ms(workflow);

		// Ensure we receive AT commands
		Assertions.assertThat(collector.findATResetCommand()).isNotNull();

		var coolant = workflow.getPidRegistry().findBy(6l);
		
		// Ensure we receive Coolant temperatur metric
		var metrics = collector.findMetricsBy(coolant);
		Assertions.assertThat(metrics.isEmpty()).isFalse();
		var metric = metrics.iterator().next();

		Assertions.assertThat(metric.getValue()).isInstanceOf(Integer.class);
		Assertions.assertThat(metric.getValue()).isEqualTo(-40);
	}

	@Test
	public void batchLessThan6Test() throws IOException, InterruptedException {

		final DataCollector collector = new DataCollector();
		final Workflow workflow = SimpleWorkflowFactory.getMode01Workflow(collector);

		final Query query = Query.builder()
		        .pid(6l) // Engine coolant temperature
		        .pid(12l)// Intake manifold absolute pressure
		        .build();

		final MockConnection connection = MockConnection.builder()
		        .commandReply("0100", "4100be3ea813")
		        .commandReply("0200", "4140fed00400")
		        .commandReply("01 0B 05", "410bff0500").build();

		Adjustments optional = Adjustments
		        .builder()
		        .batchEnabled(true).build();
		
		
		workflow.start(connection, query, optional);

		WorkflowFinalizer.finalizeAfter500ms(workflow);

		// Ensure we receive AT command as well
		Assertions.assertThat(collector.findATResetCommand()).isNotNull();

		final List<ObdMetric> collection = collector.findMetricsBy(workflow.getPidRegistry().findBy(6l));
		Assertions.assertThat(collection.isEmpty()).isFalse();
		final ObdMetric metric = collection.iterator().next();

		Assertions.assertThat(metric.getValue()).isInstanceOf(Integer.class);
		Assertions.assertThat(metric.getValue()).isEqualTo(-40);
	}
}
