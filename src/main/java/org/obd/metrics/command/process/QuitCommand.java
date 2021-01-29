package org.obd.metrics.command.process;

import org.obd.metrics.command.Command;

public final class QuitCommand extends Command implements ProcessCommand {
	public QuitCommand() {
		super("QUIT", "Quit command");
	}
}
