package org.obd.metrics.batch;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.command.obd.BatchObdCommand;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinitionRegistry;

public class CacheTest {

	@Test
	public void cacheHitTest() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
		        .getResourceAsStream("mode01.json")) {

			PidDefinitionRegistry registry = PidDefinitionRegistry.builder().source(source).build();
			List<ObdCommand> commands = new ArrayList<>();
			commands.add(new ObdCommand(registry.findBy("0C")));
			commands.add(new ObdCommand(registry.findBy("10")));
			commands.add(new ObdCommand(registry.findBy("0B")));
			commands.add(new ObdCommand(registry.findBy("0D")));
			commands.add(new ObdCommand(registry.findBy("05")));
			String query = "00b0:410c000010001:000b660d000000";
			BatchObdCommand decoder = new BatchObdCommand(query, commands, 0);
			
			for (int i=0; i<10; i++) {
				Map<ObdCommand, String> values = decoder.decode(query);
				Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0B")), "410B66");
				Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0C")), "410C0000");
				Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0D")), "410D00");
				Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("10")), "41100000");
			}
			
			Assertions.assertThat(decoder.getCacheHit(query)).isEqualTo(9);
			
		}
	}
}
