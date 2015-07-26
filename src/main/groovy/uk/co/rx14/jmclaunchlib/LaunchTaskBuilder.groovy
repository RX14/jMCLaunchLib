package uk.co.rx14.jmclaunchlib

import groovy.transform.CompileStatic
import groovy.transform.Immutable
import uk.co.rx14.jmclaunchlib.auth.PasswordSupplier
import uk.co.rx14.jmclaunchlib.caches.MinecraftCaches
import uk.co.rx14.jmclaunchlib.util.NullPasswordSupplier
import uk.co.rx14.jmclaunchlib.version.ForgeVersion
import uk.co.rx14.jmclaunchlib.version.MinecraftVersion
import uk.co.rx14.jmclaunchlib.version.Version

import java.nio.file.FileSystems
import java.nio.file.Path

@CompileStatic
@Immutable(copyWith = true, knownImmutableClasses = [Path, PasswordSupplier, Version])
class LaunchTaskBuilder {
	Version version

	Path instanceDir

	MinecraftCaches caches

	PasswordSupplier passwordSupplier = NullPasswordSupplier.INSTANCE

	String username
	boolean offline

	boolean netOffline

	LaunchTask build() {
		Objects.requireNonNull(version, "Version should be set")
		Objects.requireNonNull(caches, "Caches should be set")
		Objects.requireNonNull(instanceDir, "Instance directory should be set")
		Objects.requireNonNull(username, "Username should be set")

		def spec = new LaunchSpec(minecraftDirectory: instanceDir, offline: offline, netOffline: netOffline)

		new LaunchTask(spec, caches, version, username, passwordSupplier)
	}

	LaunchTaskBuilder setVersion(Version version) {
		this.copyWith version: version
	}

	LaunchTaskBuilder setMinecraftVersion(String minecraftVersion) {
		if (!caches) throw new IllegalStateException("Caches must be set before setting the Minecraft Version")
		this.copyWith version: new MinecraftVersion(minecraftVersion, caches.versions)
	}

	LaunchTaskBuilder setForgeVersion(String minecraftVersion, String forgeVersion) {
		if (!caches) throw new IllegalStateException("Caches must be set before setting the Minecraft Version")
		this.copyWith version: new ForgeVersion(minecraftVersion, forgeVersion, caches)
	}


	LaunchTaskBuilder setInstanceDir(Path instanceDir) {
		this.copyWith instanceDir: instanceDir
	}

	LaunchTaskBuilder setInstanceDir(String instanceDir) {
		this.copyWith instanceDir: FileSystems.default.getPath(instanceDir)
	}


	LaunchTaskBuilder setCachesDir(Path cachesDir) {
		if (version) throw new IllegalStateException("Caches must not be re-set after setting the Minecraft version")
		this.copyWith caches: MinecraftCaches.create(cachesDir, netOffline)
	}

	LaunchTaskBuilder setCachesDir(String cachesDir) {
		if (version) throw new IllegalStateException("Caches must not be re-set after setting the Minecraft version")
		this.copyWith caches: MinecraftCaches.create(FileSystems.default.getPath(cachesDir), netOffline)
	}

	LaunchTaskBuilder setCaches(MinecraftCaches caches) {
		if (version) throw new IllegalStateException("Caches must not be re-set after setting the Minecraft version")
		this.copyWith caches: caches
	}


	LaunchTaskBuilder setPasswordSupplier(PasswordSupplier passwordSupplier) {
		this.copyWith passwordSupplier: passwordSupplier
	}

	LaunchTaskBuilder setUsername(String username) {
		this.copyWith username: username
	}

	LaunchTaskBuilder setOffline(boolean offline) {
		this.copyWith offline: offline
	}

	LaunchTaskBuilder setOffline() {
		this.copyWith offline: true
	}

	LaunchTaskBuilder setNetOffline(boolean netOffline) {
		if (caches) throw new IllegalStateException("netOffline must be set before caches")
		this.copyWith netOffline: netOffline
	}

	LaunchTaskBuilder setNetOffline() {
		if (caches) throw new IllegalStateException("netOffline must be set before caches")
		this.copyWith netOffline: true
	}
}
