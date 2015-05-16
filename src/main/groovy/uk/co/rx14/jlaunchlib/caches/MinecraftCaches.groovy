package uk.co.rx14.jlaunchlib.caches

import groovy.transform.CompileStatic
import groovy.transform.Immutable

import java.nio.file.Path

@CompileStatic
@Immutable
class MinecraftCaches {
	AssetsCache assets
	MavenCache libs
	EtagCache versions

	static MinecraftCaches create(Path directory) {
		new MinecraftCaches(
			assets: AssetsCache.create(directory.resolve("assets")),
			libs: new MavenCache(directory.resolve("libs")),
			versions: new EtagCache(directory.resolve("versions"))
		)
	}
}
