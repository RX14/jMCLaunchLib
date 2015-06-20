package uk.co.rx14.jmclaunchlib.caches

import groovy.transform.CompileStatic
import groovy.transform.Immutable
import groovy.transform.ToString

import java.nio.file.Path
import java.util.logging.Logger

@CompileStatic
@ToString(includePackage = false, includeNames = true)
@Immutable(knownImmutableClasses = [Path.class, MinecraftMaven.class])
class MinecraftCaches extends Cache {

	private final static Logger LOGGER = Logger.getLogger(MinecraftCaches.class.name)

	Path storage
	AssetsCache assets
	MinecraftMaven libs
	EtagCache versions
	Path natives

	static MinecraftCaches create(Path storage) {
		def cache = new MinecraftCaches(
			storage: storage,
			assets: AssetsCache.create(storage.resolve("assets")),
			libs: new MinecraftMaven(storage.resolve("libs")),
			versions: new EtagCache(storage.resolve("versions")),
			natives: storage.resolve("natives")
		)

		LOGGER.fine "Created $cache"

		cache
	}

	@Override
	void copyFrom(Path otherCache) {
		LOGGER.info "Copying $otherCache to $storage"
		assets.copyFrom(otherCache.resolve(storage.relativize(assets.storage)))
		versions.copyFrom(otherCache.resolve(storage.relativize(versions.storage)))
		libs.copyFrom(otherCache.resolve(storage.relativize(libs.storage)))
	}

	@Override
	void copyFromTrusted(Path trustedCache) {
		LOGGER.info "Copying trusted cache $trustedCache to $storage"
		assets.copyFromTrusted(trustedCache.resolve(storage.relativize(assets.storage)))
		versions.copyFromTrusted(trustedCache.resolve(storage.relativize(versions.storage)))
		libs.copyFromTrusted(trustedCache.resolve(storage.relativize(libs.storage)))
	}
}
