package uk.co.rx14.jmclaunchlib.caches

import groovy.transform.CompileStatic
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.Path

@CompileStatic
abstract class Cache {

	private final static Log LOGGER = LogFactory.getLog(Cache)

	abstract Path getStorage();
	abstract boolean isOffline();

	/**
	 * Copies all files from another cache to the current cache. The cache
	 * should check the integrity of each file.
	 *
	 * The default implementation simply copies all files not in the current
	 * cache to the relative directory in this cache.
	 *
	 * @param otherCache the path to the cache to copy
	 */
	void copyFrom(Path otherCache) {
		LOGGER.debug "Copying $otherCache to $storage"
		_copyImpl(otherCache)
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
	void copyFromTrusted(Path trustedCache) {
		LOGGER.debug "Copying trusted cache $trustedCache to $storage"
		_copyImpl(trustedCache)
	}

	private void _copyImpl(Path otherCache) {
		Files.walk(otherCache)
		     .filter(Files.&isRegularFile)
		     .forEach { Path path ->
		         try {
			         def destination = storage.resolve(otherCache.relativize(path))
			         destination.toFile().parentFile.mkdirs()
			         Files.copy(path, destination)
		         } catch (FileAlreadyExistsException ignored) {} //Leave it
		}
	}
}
