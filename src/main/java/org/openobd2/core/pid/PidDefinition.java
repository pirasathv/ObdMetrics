package org.openobd2.core.pid;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class PidDefinition {

	private int length;

	private String formula;

	private String pid;
	private String mode;
	private String units;

	private String description;
	private String min;
	private String max;
}
