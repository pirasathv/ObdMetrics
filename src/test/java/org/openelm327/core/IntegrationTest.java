package org.openelm327.core;

import java.io.IOException;

import org.openelm327.core.command.CustomCommand;
import org.openelm327.core.command.DescribeProtocolCommand;
import org.openelm327.core.command.EchoCommand;
import org.openelm327.core.command.HeadersCommand;
import org.openelm327.core.command.ProtocolCloseCommand;
import org.openelm327.core.command.QuitCommand;
import org.openelm327.core.command.ReadVoltagetCommand;
import org.openelm327.core.command.ResetCommand;
import org.openelm327.core.command.SelectProtocolCommand;
import org.openelm327.core.streams.StreamFactory;
import org.openelm327.core.streams.Streams;

//its not really a test ;)
public class IntegrationTest {

	public static void main(String[] args) throws IOException, InterruptedException {

		final Commands commands = new Commands();
		commands.add(new ResetCommand());// reset
		commands.add(new ReadVoltagetCommand());
		commands.add(new CustomCommand("AT L0"));
		commands.add(new HeadersCommand(0));// headers off
		commands.add(new EchoCommand(0));// echo off
		commands.add(new SelectProtocolCommand(0)); // protocol default
		commands.add(new DescribeProtocolCommand());

		commands.add(new CustomCommand("01 00")); //get supported pids
		commands.add(new CustomCommand("01 20")); //get supported pids
		commands.add(new CustomCommand("01 40")); //get supported pids
		commands.add(new CustomCommand("01 0C")); // engine rpm
		commands.add(new CustomCommand("01 0F")); // air intake
		commands.add(new CustomCommand("01 10")); // maf
		commands.add(new CustomCommand("01 0B")); // intake manifold pressure
		commands.add(new CustomCommand("01 0D")); // vehicle speed
		
		
		
		
		commands.add(new ProtocolCloseCommand()); // quit
		commands.add(new QuitCommand());// end the process

		final String obdDongleId = "AABBCC112233";
		final Streams streams = StreamFactory.bt(obdDongleId);

		final CommandResultSubscriber result = new CommandResultSubscriber();

		final CommandExecutor commandExecutor = CommandExecutor.builder().streams(streams).commands(commands).build();
		commandExecutor.subscribe(result);

		commandExecutor.start();
	}
}
