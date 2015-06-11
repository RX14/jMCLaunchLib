package uk.co.rx14.jmclaunchlib.util

import groovy.transform.CompileStatic

import java.nio.file.Path
import java.util.logging.Logger

@CompileStatic
final class Minecraft {

	public static final Path minecraftDirectory = _getMinecraftDirectory()

	static {
		Logger.getLogger(Minecraft.class.name).info "Detected minecraft directory: $minecraftDirectory"
	}

	private static Path _getMinecraftDirectory()
	{
		String userDir = System.getProperty("user.home")

		switch (OS.CURRENT)
		{
			case OS.LINUX:
				return new File(userDir, ".minecraft").toPath()
			case OS.WINDOWS:
				String appData = System.getenv("APPDATA")
				String folder = appData != null ? appData : userDir
				return new File(folder, ".minecraft").toPath()
			case OS.OSX:
				return new File(userDir, "Library/Application Support/minecraft").toPath()
			default:
				return new File(userDir, "minecraft").toPath()
		}
	}
}