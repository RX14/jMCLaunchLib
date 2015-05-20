package uk.co.rx14.jlaunchlib.caches

import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.transform.Immutable
import groovy.transform.ToString
import groovy.transform.TypeCheckingMode
import uk.co.rx14.jlaunchlib.Constants
import uk.co.rx14.jlaunchlib.MinecraftVersion

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

	@CompileStatic(TypeCheckingMode.SKIP)
	Path getAssets(MinecraftVersion version) {
		if (version.get().assets) {
			_getAssets(version.get().assets)
			storage
		} else {
			_getLegacyAssets(version.version)
		}
	}

	@CompileStatic(TypeCheckingMode.SKIP)
	private void _getAssets(String assetsVersion) {
		def index = new JsonSlurper().parseText(
			new String(indexes.get("$Constants.MinecraftIndexesBase/${assetsVersion}.json".toURL()))
		)

		index.objects.each {
			String hash = it.value.hash
			def URL = "$Constants.MinecraftAssetsBase/${hash.substring(0, 2)}/$hash".toURL()
			if (!objects.has(hash)) {
				LOGGER.info "Downloading $it.key from $URL"
				objects.preDownload(hash, URL)
			} else {
				LOGGER.finest "Not Downloading $hash: in cache ($URL)"
			}
		}

		Thread.start {
			objects.verify(HashCache.VerificationAction.DELETE)
		}
	}

	private Path _getLegacyAssets(String minecraftVersion) {
		throw new UnsupportedOperationException("Legacy assets not supported... for now :3")
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
		LOGGER.info "Copying cache $otherCache to $storage"
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
		LOGGER.info "Copying trusted cache $trustedCache to $storage"

		objects.copyFromTrusted(trustedCache.resolve(storage.relativize(objects.storage)))
		indexes.copyFromTrusted(trustedCache.resolve(storage.relativize(indexes.storage)))
	}
}
