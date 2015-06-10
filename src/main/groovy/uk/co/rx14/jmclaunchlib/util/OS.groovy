package uk.co.rx14.jmclaunchlib.util

import groovy.transform.CompileStatic

import java.util.logging.Logger

@CompileStatic
enum OS
{
	LINUX("linux", "bsd", "unix"),
	WINDOWS("windows", "win"),
	OSX("osx", "mac"),
	UNKNOWN("unknown")

	private String name
	private String[] aliases

	public static final OS CURRENT = _getCurrentPlatform()
	public static final String VERSION = System.getProperty("os.version")

	static {
		Logger.getLogger(OS.class.getName()).info "Detected OS '$CURRENT' version '$VERSION'"
	}

	private OS(String name, String... aliases) {
		this.name = name
		this.aliases = aliases
	}

	private static OS _getCurrentPlatform() {
		fromString(System.getProperty("os.name").toLowerCase(Locale.US))
	}

	public static OS fromString(String osName) {
		for (OS os : values()) {
			if (osName.contains(os.name)) return os
			for (String alias : os.aliases) {
				if (osName.contains(alias)) return os
			}
		}
		return UNKNOWN
	}

	@Override
	public String toString() {
		return name
	}
}
