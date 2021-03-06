package uk.co.rx14.jmclaunchlib.util

import groovy.transform.CompileStatic
import org.apache.commons.logging.LogFactory

import java.nio.file.Path

@CompileStatic
final class Minecraft {

	public static final Path minecraftDirectory = _getMinecraftDirectory()

	static {
		LogFactory.getLog(Minecraft.class).debug "Detected minecraft directory: $minecraftDirectory"
	}

	private static Path _getMinecraftDirectory()
	{
		String userDir = System.getProperty("user.home")

		def file
		switch (OS.CURRENT)
		{
			case OS.LINUX:
				file = new File(userDir, ".minecraft")
				break
			case OS.WINDOWS:
				String appData = System.getenv("APPDATA")
				String folder = appData != null ? appData : userDir
				file = new File(folder, ".minecraft")
				break
			case OS.OSX:
				file = new File(userDir, "Library/Application Support/minecraft")
				break
			default:
				file = new File(userDir, "minecraft")
				break
		}
		file.toPath().toAbsolutePath()
	}
}
