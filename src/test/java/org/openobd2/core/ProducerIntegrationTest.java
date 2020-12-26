package org.openobd2.core;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections4.MultiValuedMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.command.Command;
import org.openobd2.core.command.CommandReply;
import org.openobd2.core.command.obd.mode1.EngineTempCommand;
import org.openobd2.core.command.process.QuitCommand;
import org.openobd2.core.streams.StreamFactory;
import org.openobd2.core.streams.Streams;

import lombok.extern.slf4j.Slf4j;

//its not really a test ;)
@Slf4j
public class ProducerIntegrationTest {

	@Test
	public void producerTest() throws IOException, InterruptedException, ExecutionException {
		final CommandsBuffer buffer = new CommandsBuffer();
		final Streams streams = StreamFactory.builder().btId("AABBCC112233").build();

		
		final ProducerPolicy policy = ProducerPolicy.builder().frequency(50).build();
		final CommandsProducer producer = CommandsProducer.builder().buffer(buffer).policy(policy).build();
		//collects obd data
		final DataCollector collector = new DataCollector();
		final ExecutorPolicy executorPolicy  = ExecutorPolicy.builder().frequency(100).build();
		
		final CommandExecutor executor = CommandExecutor
				.builder()
				.streams(streams)
				.buffer(buffer)
				.subscribe(collector)
				.subscribe(producer)
				.policy(executorPolicy)
				.build();
		
		
		///finish after 15s from now on
		final Callable<String> end = () -> {
		
			Thread.sleep(15000);
			log.info("Thats the end.....");
			//end interaction with the dongle
			buffer.addFirst(new QuitCommand());// quite the CommandExecutor
			return "end";
		};
		
		final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(3);
		newFixedThreadPool.invokeAll(Arrays.asList(executor, producer,end));

		final MultiValuedMap<Command, CommandReply<?>> data = collector.getData();
		Assertions.assertThat(data.containsKey(new EngineTempCommand()));

		final Collection<CommandReply<?>> collection = data.get(new EngineTempCommand());
		Assertions.assertThat(collection).isNotEmpty();

		newFixedThreadPool.shutdown();
	}

}
