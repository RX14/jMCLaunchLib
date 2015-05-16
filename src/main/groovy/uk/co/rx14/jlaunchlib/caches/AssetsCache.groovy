package uk.co.rx14.jlaunchlib.caches

import groovy.transform.CompileStatic
import groovy.transform.Immutable
import uk.co.rx14.jlaunchlib.Util.Minecraft

import java.nio.file.Path

@CompileStatic
@Immutable
class AssetsCache {
	HashCache objects
	EtagCache indexes

	static AssetsCache create(Path directory, Path... others) {
		def objectsCache = new HashCache(directory.resolve("objects"))
		def indexesCache = new EtagCache(directory.resolve("indexes"))

		others.each { Path path ->
			objectsCache.copyFromTrusted(path.resolve("objects"))
//			indexesCache.copyFrom()
		}

		new AssetsCache(
			objects: objectsCache,
			indexes: indexesCache
		)
	}

	void getAssets() {

	}
}
