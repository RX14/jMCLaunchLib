package uk.co.rx14.jlaunchlib.caches

import groovy.transform.CompileStatic
import groovy.transform.Immutable

import java.nio.file.Path
import java.util.logging.Logger

@CompileStatic
@Immutable(knownImmutableClasses = [Path.class])
class MinecraftCaches extends Cache {

	private final static Logger LOGGER = Logger.getLogger(MinecraftCaches.class.name)

	Path storage
	AssetsCache assets
	EtagCache versions

	static MinecraftCaches create(Path storage) {
		def cache = new MinecraftCaches(
			storage: storage,
			assets: AssetsCache.create(storage.resolve("assets")),
			versions: new EtagCache(storage.resolve("versions"))
		)

		LOGGER.fine "Created $cache"

		cache
	}

	@Override
	void copyFrom(Path otherCache) {
		LOGGER.info "Copying $otherCache to $storage"
		assets.copyFrom(otherCache.resolve(storage.relativize(assets.storage)))
		versions.copyFrom(otherCache.resolve(storage.relativize(versions.storage)))
	}

	@Override
	void copyFromTrusted(Path trustedCache) {
		LOGGER.info "Copying trusted cache $trustedCache to $storage"
		assets.copyFromTrusted(trustedCache.resolve(storage.relativize(assets.storage)))
		versions.copyFromTrusted(trustedCache.resolve(storage.relativize(versions.storage)))
	}
}
