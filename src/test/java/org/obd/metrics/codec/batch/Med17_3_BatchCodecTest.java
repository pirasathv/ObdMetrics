package org.obd.metrics.codec.batch;

import static org.obd.metrics.codec.batch.BatchMessageBuilder.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.PidRegistryCache;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.raw.RawMessage;

public class Med17_3_BatchCodecTest {
	
	@Test
	public void incorrect_answer() {
		final PidDefinitionRegistry registry = PidRegistryCache.get("mode01.json");
		List<ObdCommand> commands = new ArrayList<>();
		commands.add(new ObdCommand(registry.findBy("15")));
		commands.add(new ObdCommand(registry.findBy("0B")));
		commands.add(new ObdCommand(registry.findBy("0C")));
		commands.add(new ObdCommand(registry.findBy("04")));
		commands.add(new ObdCommand(registry.findBy("11")));
		commands.add(new ObdCommand(registry.findBy("0E")));
		commands.add(new ObdCommand(registry.findBy("0F")));
		commands.add(new ObdCommand(registry.findBy("05")));

		final byte[] message = "01150B0C0411200D0:41155AFF0BFF1:0C000004001100".getBytes();
		final BatchCodec codec = BatchCodec.instance(Adjustments.DEFAULT, null, commands);
		final Map<ObdCommand, RawMessage> values = codec.decode(null, RawMessage.wrap(message));

		final BatchMessage batchMessage = instance(message);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("15")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0B")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0C")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("04")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("11")), batchMessage);

	}

	
	@Test
	public void case_01() {
		final PidDefinitionRegistry registry = PidRegistryCache.get("mode01.json");

		List<ObdCommand> commands = new ArrayList<>();
		commands.add(new ObdCommand(registry.findBy("0C")));
		commands.add(new ObdCommand(registry.findBy("10")));
		commands.add(new ObdCommand(registry.findBy("0B")));
		commands.add(new ObdCommand(registry.findBy("0D")));
		// 00A0:410BFF0C00001:11000D00AAAAAA

		final byte[] message = "00B0:410C000010001:000B660D000000".getBytes();
		final BatchCodec codec = BatchCodec.instance(Adjustments.DEFAULT, null, commands);
		final Map<ObdCommand, RawMessage> values = codec.decode(null, RawMessage.wrap(message));

		final BatchMessage batchMessage = instance(message);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0B")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0C")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0D")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("10")), batchMessage);

	}

	@Test
	public void case_02() {
		final PidDefinitionRegistry registry = PidRegistryCache.get("mode01.json");
		List<ObdCommand> commands = new ArrayList<>();
		commands.add(new ObdCommand(registry.findBy("0C")));
		commands.add(new ObdCommand(registry.findBy("10")));
		commands.add(new ObdCommand(registry.findBy("0B")));
		commands.add(new ObdCommand(registry.findBy("0D")));
		commands.add(new ObdCommand(registry.findBy("05")));
		commands.add(new ObdCommand(registry.findBy("0F")));
		final byte[] message = "00F0:410C000010001:000B660D0005222:0F370000000000".getBytes();
		final BatchCodec codec = BatchCodec.instance(Adjustments.DEFAULT, null, commands);
		final Map<ObdCommand, RawMessage> values = codec.decode(null, RawMessage.wrap(message));

		final BatchMessage batchMessage = instance(message);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0B")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0C")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0F")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("05")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0D")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("10")), batchMessage);
	}

	@Test
	public void case_03() {
		final PidDefinitionRegistry registry = PidRegistryCache.get("mode01.json");
		List<ObdCommand> commands = new ArrayList<>();
		commands.add(new ObdCommand(registry.findBy("0C")));
		commands.add(new ObdCommand(registry.findBy("10")));
		commands.add(new ObdCommand(registry.findBy("0B")));
		commands.add(new ObdCommand(registry.findBy("0D")));
		commands.add(new ObdCommand(registry.findBy("05")));
		commands.add(new ObdCommand(registry.findBy("0F")));

		final byte[] message = "410C0000100000".getBytes();
		final BatchCodec codec = BatchCodec.instance(Adjustments.DEFAULT, null, commands);
		final Map<ObdCommand, RawMessage> values = codec.decode(null, RawMessage.wrap(message));

		final BatchMessage batchMessage = instance(message);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0C")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("10")), batchMessage);

	}

	@Test
	public void case_04() {
		final PidDefinitionRegistry registry = PidRegistryCache.get("mode01.json");
		List<ObdCommand> commands = new ArrayList<>();
		commands.add(new ObdCommand(registry.findBy("0C")));
		commands.add(new ObdCommand(registry.findBy("10")));
		commands.add(new ObdCommand(registry.findBy("0B")));
		commands.add(new ObdCommand(registry.findBy("0D")));
		commands.add(new ObdCommand(registry.findBy("05")));
		commands.add(new ObdCommand(registry.findBy("0F")));

		final byte[] message = "0090:410C000010001:000B6600000000".getBytes();
		final BatchCodec codec = BatchCodec.instance(Adjustments.DEFAULT, null, commands);
		final Map<ObdCommand, RawMessage> values = codec.decode(null, RawMessage.wrap(message));

		
		final BatchMessage batchMessage = instance(message);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0C")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("10")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0B")), batchMessage);

	}

	@Test
	public void case_05() {
		final PidDefinitionRegistry registry = PidRegistryCache.get("mode01.json");
		List<ObdCommand> commands = new ArrayList<>();
		commands.add(new ObdCommand(registry.findBy("0C")));
		commands.add(new ObdCommand(registry.findBy("10")));
		commands.add(new ObdCommand(registry.findBy("0B")));
		commands.add(new ObdCommand(registry.findBy("0D")));
		commands.add(new ObdCommand(registry.findBy("05")));

		final byte[] message = "00D0:410C000010001:000B660D000522".getBytes();
		final BatchCodec codec = BatchCodec.instance(Adjustments.DEFAULT, null, commands);
		final Map<ObdCommand, RawMessage> values = codec.decode(null, RawMessage.wrap(message));

		final BatchMessage batchMessage = instance(message);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0B")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0C")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("05")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0D")), batchMessage);
	}

	@Test
	public void case_06() {
		final PidDefinitionRegistry registry = PidRegistryCache.get("mode01.json");
		List<ObdCommand> commands = new ArrayList<>();
		commands.add(new ObdCommand(registry.findBy("0C")));
		commands.add(new ObdCommand(registry.findBy("10")));
		commands.add(new ObdCommand(registry.findBy("0B")));
		commands.add(new ObdCommand(registry.findBy("0D")));
		commands.add(new ObdCommand(registry.findBy("05")));
		commands.add(new ObdCommand(registry.findBy("11")));

		final byte[] message = "00F0:410C000010001:000B660D0005222:11260000000000".getBytes();
		final BatchCodec codec = BatchCodec.instance(Adjustments.DEFAULT, null, commands);
		final Map<ObdCommand, RawMessage> values = codec.decode(null, RawMessage.wrap(message));

		
		final BatchMessage batchMessage = instance(message);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0B")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0C")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("11")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("05")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0D")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("10")), batchMessage);
	}

	@Test
	public void case_07() {
		final PidDefinitionRegistry registry = PidRegistryCache.get("mode01.json");
		List<ObdCommand> commands = new ArrayList<>();
		commands.add(new ObdCommand(registry.findBy("0B")));
		commands.add(new ObdCommand(registry.findBy("0C")));
		commands.add(new ObdCommand(registry.findBy("0D")));
		commands.add(new ObdCommand(registry.findBy("0E")));
		commands.add(new ObdCommand(registry.findBy("0F")));
		commands.add(new ObdCommand(registry.findBy("10")));

		final byte[] message = "00F0:410B650C00001:0D000E800F2F102:00000000000000".getBytes();
		final BatchCodec codec = BatchCodec.instance(Adjustments.DEFAULT, null, commands);
		final Map<ObdCommand, RawMessage> values = codec.decode(null, RawMessage.wrap(message));


		final BatchMessage batchMessage = instance(message);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0E")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0D")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0C")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0B")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0D")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("10")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0F")), batchMessage);

	}

	@Test
	public void case_08() {
		// 01 03 04 05 06 07
		final PidDefinitionRegistry registry = PidRegistryCache.get("mode01.json","mode01_2.json");
		
		List<ObdCommand> commands = new ArrayList<>();
		commands.add(new ObdCommand(registry.findBy("04")));
		commands.add(new ObdCommand(registry.findBy("05")));
		commands.add(new ObdCommand(registry.findBy("06")));
		commands.add(new ObdCommand(registry.findBy("07")));

		final byte[] message = "0110:4101000771611:0300000400051c2:06800781000000".getBytes();
		final BatchCodec codec = BatchCodec.instance(Adjustments.DEFAULT, null, commands);
		final Map<ObdCommand, RawMessage> values = codec.decode(null, RawMessage.wrap(message));

		final BatchMessage batchMessage = instance(message);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("05")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("04")), batchMessage);
	}
}
