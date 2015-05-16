package uk.co.rx14.jlaunchlib.Util

import groovy.transform.CompileStatic

import java.nio.file.Path

@CompileStatic
class Minecraft {
	static Path getMinecraftDirectory()
	{
		String userDir = System.getProperty("user.home");

		switch (OS.CURRENT)
		{
			case OS.LINUX:
				return new File(userDir, ".minecraft").toPath();
			case OS.WINDOWS:
				String appData = System.getenv("APPDATA");
				String folder = appData != null ? appData : userDir;
				return new File(folder, ".minecraft").toPath();
			case OS.OSX:
				return new File(userDir, "Library/Application Support/minecraft").toPath();
			default:
				return new File(userDir, "minecraft").toPath();
		}
	}
}
