package uk.co.rx14.jmclaunchlib.caches

import groovy.transform.Immutable
import groovy.transform.ToString
import uk.co.rx14.jmclaunchlib.util.MavenIdentifier

import java.nio.file.Path
import java.util.logging.Logger

@Immutable(knownImmutableClasses = [Path.class])
@ToString(includePackage = false, includeNames = true)
class MinecraftMaven extends Cache {

	private final static Logger LOGGER = Logger.getLogger(MinecraftMaven.class.name)

	Path storage

	File resolve(String identifier, String repo) {
		resolve(MavenIdentifier.of(identifier), repo)
	}

	File resolve(MavenIdentifier id, String repo) {
		LOGGER.finer "Resolving dependency: $id.identifier in repo $repo"

		def localPath = storage.resolve(id.path).toFile()

		//Check if non pack.xz file exists
		if (id.ext.endsWith(".pack.xz")) {
			def jarPath = storage.resolve(id.copyWith(ext: id.ext.replaceAll('\\.pack\\.xz$', "")).path).toFile()
			if (jarPath.exists()) {
				return jarPath
			}
		}

		if (!localPath.exists()) {

			if (!repo.endsWith("/")) {
				repo += "/"
			}

			def artifactURL = "$repo$id.path".toURL()
			localPath.parentFile.mkdirs()

			LOGGER.fine "Downloading $artifactURL"
			localPath.bytes = artifactURL.bytes
		}

		localPath
	}

	boolean exists(MavenIdentifier id) {
		localPath(id).exists()
	}

	File localPath(MavenIdentifier id) {
		storage.resolve(id.path).toFile()
	}
}
