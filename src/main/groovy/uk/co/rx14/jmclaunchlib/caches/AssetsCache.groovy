package uk.co.rx14.jmclaunchlib.caches

import groovy.transform.CompileStatic
import groovy.transform.Immutable
import groovy.transform.ToString

import java.nio.file.Path
import java.util.logging.Logger

@CompileStatic
@ToString(includePackage = false, includeNames = true)
@Immutable(knownImmutableClasses = [Path.class])
class AssetsCache extends Cache {

	private final static Logger LOGGER = Logger.getLogger(AssetsCache.class.name)

	Path storage
	HashCache objects
	EtagCache indexes

	/**
	 * Creates an AssetsCache from a single path with a default structure.
	 *
	 * Takes a list of other caches to copy into this cache.
	 *
	 * @param storage where to construct the cache.
	 * @param others other assets caches such as {@code ~/.minecraft}.
	 * @return
	 */
	static AssetsCache create(Path storage, Path... others) {
		storage = storage.toAbsolutePath()
		def objectsCache = new HashCache(storage.resolve("objects"))
		def indexesCache = new EtagCache(storage.resolve("indexes"))

		def cache = new AssetsCache(
			storage: storage,
			objects: objectsCache,
			indexes: indexesCache
		)

		LOGGER.fine "Created $cache"

		others.each(cache.&copyFromTrusted)

		cache
	}

	/**
	 * Copies all files from another cache to the current cache. Checks the
	 * integrity of each file.
	 *
	 * The other cache should have exactly the same layout as this cache.
	 *
	 * @param otherCache the path to the cache to copy
	 */
	@Override
	void copyFrom(Path otherCache) {
		LOGGER.fine "Copying cache $otherCache to $storage"
		objects.copyFrom(otherCache.resolve(storage.relativize(objects.storage)))
		indexes.copyFrom(otherCache.resolve(storage.relativize(indexes.storage)))
	}

	/**
	 * Copies all files from another cache to the current cache. This variant
	 * is faster because it trusts the cache to be correct and simply copies the
	 * files directory.
	 *
	 * Default implementation is the same as {@link #copyFrom}.
	 *
	 * @param trustedCache the path to the cache to copy
	 */
	@Override
	void copyFromTrusted(Path trustedCache) {
		LOGGER.fine "Copying trusted cache $trustedCache to $storage"

		objects.copyFromTrusted(trustedCache.resolve(storage.relativize(objects.storage)))
		indexes.copyFromTrusted(trustedCache.resolve(storage.relativize(indexes.storage)))
	}
}
