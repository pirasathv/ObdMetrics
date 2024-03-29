package org.obd.metrics.buffer;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingDeque;

import org.obd.metrics.command.Command;
import org.obd.metrics.command.group.CommandGroup;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class DefaultCommandsBuffer implements CommandsBuffer {

	// no synchronization need, already synchronized
	private volatile LinkedBlockingDeque<Command> deque = new LinkedBlockingDeque<>();

	@Override
	public CommandsBuffer clear() {
		log.info("Invaldiating {} commands in the queue.", deque.size());
		deque.clear();
		return this;
	}

	@Override
	public long size() {
		return deque.size();
	}

	@Override
	public DefaultCommandsBuffer add(final CommandGroup<?> group) {
		addAll(group.getCommands());
		return this;
	}

	@Override
	public CommandsBuffer addAll(final Collection<? extends Command> commands) {
		commands.forEach(this::addLast);
		return this;
	}

	@Override
	public <T extends Command> CommandsBuffer addFirst(final T command) {
		try {
			deque.putFirst(command);
		} catch (final InterruptedException e) {
			log.warn("Failed to add command to the queue", e);
		}
		return this;
	}

	@Override
	public <T extends Command> CommandsBuffer addLast(final T command) {
		try {
			deque.putLast(command);
		} catch (final InterruptedException e) {
			log.warn("Failed to add command to the queue", e);
		}
		return this;
	}

	@Override
	public Command get() throws InterruptedException {
		return deque.takeFirst();
	}
}
