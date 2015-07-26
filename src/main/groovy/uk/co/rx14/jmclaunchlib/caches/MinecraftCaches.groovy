package uk.co.rx14.jmclaunchlib.caches

import groovy.transform.CompileStatic
import groovy.transform.Immutable
import groovy.transform.ToString
import org.apache.commons.io.FileUtils
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import java.nio.file.Path

import static uk.co.rx14.jmclaunchlib.util.Minecraft.minecraftDirectory

@CompileStatic
@ToString(includePackage = false, includeNames = true)
@Immutable(knownImmutableClasses = [Path.class, MinecraftMaven.class])
class MinecraftCaches extends Cache {

	private final static Log LOGGER = LogFactory.getLog(MinecraftCaches)

	Path storage
	boolean offline

	AssetsCache assets
	MinecraftMaven libs
	EtagCache versions
	Path natives

	static MinecraftCaches create(Path storage, boolean offline = false) {
		def cache = new MinecraftCaches(
			storage: storage,
			assets: AssetsCache.create(storage.resolve("assets"), offline),
			libs: new MinecraftMaven(storage.resolve("libs"), offline),
			versions: new EtagCache(storage.resolve("versions"), offline),
			natives: storage.resolve("natives"),
			offline: offline
		)

		LOGGER.trace "Created $cache"

		cache.copyFromMinecraftDirectory()

		cache
	}

	@Override
	void copyFrom(Path otherCache) {
		LOGGER.trace "Copying $otherCache to $storage"
		assets.copyFrom(otherCache.resolve(storage.relativize(assets.storage)))
		versions.copyFrom(otherCache.resolve(storage.relativize(versions.storage)))
		libs.copyFrom(otherCache.resolve(storage.relativize(libs.storage)))
	}

	@Override
	void copyFromTrusted(Path trustedCache) {
		LOGGER.trace "Copying trusted cache $trustedCache to $storage"
		assets.copyFromTrusted(trustedCache.resolve(storage.relativize(assets.storage)))
		versions.copyFromTrusted(trustedCache.resolve(storage.relativize(versions.storage)))
		libs.copyFromTrusted(trustedCache.resolve(storage.relativize(libs.storage)))
	}

	void copyFromMinecraftDirectory() {
		def marker = storage.resolve("hasCopiedMinecraftDirectory").toFile()
		if (marker.exists()) return

		def libsPath = minecraftDirectory.resolve("libraries")
		if (libsPath.toFile().isDirectory()) libs.copyFrom(libsPath)

		def assetsPath = minecraftDirectory.resolve("assets")
		if (assetsPath.toFile().isDirectory()) assets.copyFrom(assetsPath)

		def versionsDirectory = minecraftDirectory.resolve("versions").toFile()
		if (versionsDirectory.isDirectory()) {
			versionsDirectory.listFiles().each { File subdirectory ->
				if (subdirectory.isDirectory()) {
					FileUtils.copyDirectory(subdirectory, versions.storage.toFile())
				}
			}
		}

		marker.parentFile.mkdirs()
		marker.createNewFile()
	}
}
