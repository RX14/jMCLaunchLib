package uk.co.rx14.jmclaunchlib

import groovy.transform.CompileStatic
import groovy.transform.Immutable
import groovy.transform.ToString
import uk.co.rx14.jmclaunchlib.auth.PasswordSupplier
import uk.co.rx14.jmclaunchlib.caches.MinecraftCaches
import uk.co.rx14.jmclaunchlib.tasks.LaunchTask
import uk.co.rx14.jmclaunchlib.util.Compression

import java.nio.file.FileSystems
import java.nio.file.Path
import java.util.logging.Logger

@CompileStatic
@ToString(includePackage = false, includeNames = true)
@Immutable(knownImmutableClasses = [Path.class, MinecraftVersion.class])
class MCInstance {

	private static final Logger LOGGER = Logger.getLogger(MCInstance.class.getName())

	Path minecraftDirectory
	MinecraftCaches caches
	MinecraftVersion minecraftVersion
	private PasswordSupplier passwordSupplier

	static MCInstance create(String MCVersion, Path MCDir, Path cachesDir, PasswordSupplier passwordSupplier) {
		def caches = MinecraftCaches.create(cachesDir)
		def instance = new MCInstance(
			caches: caches,
			minecraftVersion: new MinecraftVersion(MCVersion, caches.versions),
			passwordSupplier: passwordSupplier,
			minecraftDirectory: MCDir,
		)

		LOGGER.fine "Created $instance"

		instance
	}

	static MCInstance create(String MCVersion, String MCDir, String cachesDir, PasswordSupplier passwordSupplier) {
		create(MCVersion, FileSystems.default.getPath(MCDir), FileSystems.default.getPath(cachesDir), passwordSupplier)
	}

	static MCInstance createForge(String MCVersion, String forgeVersion, Path MCDir, Path cachesDir, PasswordSupplier passwordSupplier) {
		def caches = MinecraftCaches.create(cachesDir)

		def forgeJson = Compression.extractZipSingleFile(
			caches.libs.resolve("net.minecraftforge:forge:jar:universal:$forgeVersion", "http://files.minecraftforge.net/maven/"),
			"version.json"
		)

		def instance = new MCInstance(
			caches: caches,
			minecraftVersion: new MinecraftVersion(MCVersion, caches.versions, new String(forgeJson)),
			passwordSupplier: passwordSupplier,
			minecraftDirectory: MCDir
		)

		LOGGER.fine "Created $instance"

		instance
	}

	static MCInstance createForge(String MCVersion, String forgeVersion, String MCDir, String cachesDir, PasswordSupplier passwordSupplier) {
		createForge(MCVersion, forgeVersion, FileSystems.default.getPath(MCDir), FileSystems.default.getPath(cachesDir), passwordSupplier)
	}

	LaunchTask getTask(String username, boolean offline) {
		def spec = new LaunchSpec(minecraftDirectory: minecraftDirectory, offline: offline)

		new LaunchTask(spec, caches, minecraftVersion, username, passwordSupplier)
	}
}
