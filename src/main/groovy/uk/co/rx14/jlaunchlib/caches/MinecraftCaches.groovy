package uk.co.rx14.jlaunchlib.caches

import groovy.transform.CompileStatic
import groovy.transform.Immutable

import java.nio.file.Path

@CompileStatic
@Immutable(knownImmutableClasses = [Path.class])
class MinecraftCaches extends Cache {


	Path storage
	AssetsCache assets
	EtagCache versions

		new MinecraftCaches(
	static MinecraftCaches create(Path storage) {
			storage: storage,
			assets: AssetsCache.create(storage.resolve("assets")),
			versions: new EtagCache(storage.resolve("versions"))
		)
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
