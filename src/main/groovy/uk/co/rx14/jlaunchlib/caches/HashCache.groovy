package uk.co.rx14.jlaunchlib.caches

import groovy.transform.CompileStatic
import groovy.transform.Immutable
import groovy.transform.TypeCheckingMode
import org.apache.commons.codec.digest.DigestUtils

import java.nio.file.CopyOption
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/**
 * Implements a hash-addressed filesystem cache.
 *
 * The files are stored by hex-encoded in the format
 * {@code ab/ab615a912fb8ea06648836e0ec1cbeeefe117da6}
 */
@CompileStatic
@Immutable(knownImmutableClasses = [Path.class])
class HashCache {
	Path storage

	/**
	 * Stores the given data in the cache and returns it's hash.
	 *
	 * @param data the data to store.
	 * @return the sha1 hash
	 */
	String store(String data) {
		def hash = DigestUtils.sha1Hex(data)

		def file = getPath(hash).toFile()
		if (!file.exists())
			file.text = data

		hash
	}

	/**
	 * Return the content of the file with the specified hash, or null if not
	 * in the cache.
	 *
	 * @param hash tha sha1 hash to look up.
	 * @return the data.
	 */
	String get(String hash) {
		def file = getPath(hash).toFile()

		if (file.exists())
			file.text
		else
			null
	}

	/**
	 * Returns true if the cache has the given hash
	 *
	 * @param hash
	 * @return
	 */
	boolean has(String hash) {
		getPath(hash).toFile().exists()
	}

	/**
	 * Calculates the path to the file with the given hash. If the path is in
	 * a mirror, that path is returned. Otherwise the path in the main storage
	 * is returned.
	 *
	 * Note that this will return a {@link Path}, whether it exists or not.
	 *
	 * @param hash the sha1 hash to calculate the path for.
	 * @return the path.
	 */
	Path getPath(String hash) {
		def pre = hash.substring(0, 2)
		return storage.resolve("$pre/${hash}")
	}

	/**
	 * Downloads the given URL and places it in the
	 *
	 * @param URL
	 * @return
	 */
	@CompileStatic(TypeCheckingMode.SKIP)
	DataHashPair download(URL URL) {
		new DataHashPair(*_download(URL))
	}

	/**
	 * Gets the contents of the URL given and stores it in the cache while
	 * returning the data. If it already exists in the cache, it uses the
	 * cached value.
	 *
	 * @param hash the hash of the file to download
	 * @param URL
	 * @return
	 */
	@CompileStatic(TypeCheckingMode.SKIP)
	String download(String hash, URL URL) {
		def file = getPath(hash).toFile()
		if (file.exists()) {
			file.text
		} else {
			def (String downloadedHash, String data) = _download(URL)

			if (!downloadedHash.equals(hash))
				throw new InvalidResponseException("$URL did not match has $hash")

			data
		}
	}

	/**
	 * Copies all files from the other cache to the current cache. This method
	 * does not trust the contents of the cache therefore simply uses
	 * {@link #store } to save the contents of each file into the cache.
	 *
	 * @param otherCache the path to the cache to copy
	 */
	void copyFrom(Path otherCache) {
		Files.walk(otherCache)
		     .filter(Files.&isRegularFile)
		     .forEach { Path path ->
		         store(path.text)
		     }
	}

	/**
	 * Copies all files from another cache to this one. This variant is faster
	 * because it trusts the cache to be correct and simply copies the files directory.
	 *
	 * @param otherCache the path to the cache to copy
	 */
	void copyFromTrusted(Path otherCache) {
		Files.walk(otherCache)
		     .filter(Files.&isRegularFile)
		     .forEach { Path path ->
		         Files.copy(path, getPath(path.toFile().name), StandardCopyOption.REPLACE_EXISTING)
		     }
	}

	private _download(URL URL) {
		String data = URL.getText(connectTimeout: 10000, readTimeout: 2000)

		[store(data), data]
	}

	/**
	 * Indicates that the response received from an external source did not
	 * match the expected hash.
	 */
	static class InvalidResponseException extends RuntimeException {
		InvalidResponseException() {
			super()
		}

		InvalidResponseException(String message) {
			super(message)
		}
	}

	static @Immutable
	class DataHashPair {
		String hash, data

		boolean verify() {
			DigestUtils.sha1Hex(data) == hash
		}
	}
}
