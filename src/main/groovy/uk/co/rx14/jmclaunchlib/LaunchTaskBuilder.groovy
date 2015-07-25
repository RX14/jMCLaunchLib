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
import java.util.function.Function

@CompileStatic
@Immutable(copyWith = true, knownImmutableClasses = [Path, PasswordSupplier, Function])
class LaunchTaskBuilder {
	Function<LaunchTaskBuilder, Version> versionSupplier

	Path instanceDir
	MinecraftCaches caches

	PasswordSupplier passwordSupplier = NullPasswordSupplier.INSTANCE

	String username
	boolean offline

	boolean netOffline

	LaunchTask build() {
		Objects.requireNonNull(versionSupplier, "Version should be set")
		Objects.requireNonNull(instanceDir, "Instance directory should be set")
		Objects.requireNonNull(caches, "Caches should be set")
		Objects.requireNonNull(username, "Username should be set")

		def version = versionSupplier.apply(this)

		Objects.requireNonNull(version, "Version supplier returned null")

		def spec = new LaunchSpec(minecraftDirectory: instanceDir, offline: offline)

		new LaunchTask(spec, caches, version, username, passwordSupplier)
	}

	LaunchTaskBuilder setVersion(Version version) {
		this.copyWith versionSupplier: { version }
	}

	LaunchTaskBuilder setMinecraftVersion(String minecraftVersion) {
		this.copyWith versionSupplier: { LaunchTaskBuilder it -> new MinecraftVersion(minecraftVersion, it.caches.versions) }
	}

	LaunchTaskBuilder setForgeVersion(String minecraftVersion, String forgeVersion) {
		this.copyWith versionSupplier: { LaunchTaskBuilder it -> new ForgeVersion(minecraftVersion, forgeVersion, it.caches) }
	}


	LaunchTaskBuilder setInstanceDir(Path instanceDir) {
		this.copyWith instanceDir: instanceDir
	}

	LaunchTaskBuilder setInstanceDir(String instanceDir) {
		this.copyWith instanceDir: FileSystems.default.getPath(instanceDir)
	}


	LaunchTaskBuilder setCachesDir(Path cachesDir) {
		this.copyWith caches: MinecraftCaches.create(cachesDir)
	}

	LaunchTaskBuilder setCachesDir(String cachesDir) {
		this.copyWith caches: MinecraftCaches.create(FileSystems.default.getPath(cachesDir))
	}

	LaunchTaskBuilder setCaches(MinecraftCaches caches) {
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
		this.copyWith netOffline: netOffline
	}

	LaunchTaskBuilder setNetOffline() {
		this.copyWith netOffline: true
	}
}
