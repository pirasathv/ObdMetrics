package org.obd.metrics.connection;

final class CharacterFilter {
	private static final String FILTER_PATTERN = "SEARCHING...";

	boolean isCharacterAllowed(char character) {
		return character != '\t' && character != '\n' && character != '\r' && character != ' ';
	}

	String filterOut(StringBuilder message) {
		return message.toString().replace(FILTER_PATTERN, "").toLowerCase();
	}
}