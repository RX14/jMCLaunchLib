package uk.co.rx14.jlaunchlib.caches

import groovy.transform.CompileStatic

import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.Path

@CompileStatic
abstract class Cache {
	abstract Path getStorage();

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
		Files.walk(otherCache)
		     .filter(Files.&isRegularFile)
		     .forEach { Path path ->
		         try {
			         Files.copy(path, path.relativize(otherCache).resolve(storage))
		         } catch (FileAlreadyExistsException ignored) {} //Leave it
		     }
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
		copyFrom(trustedCache)
	}
}
