package uk.co.rx14.jmclaunchlib.caches

import groovy.transform.Immutable
import groovy.transform.ToString
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import uk.co.rx14.jmclaunchlib.exceptions.OfflineException
import uk.co.rx14.jmclaunchlib.util.MavenIdentifier

import java.nio.file.Path

@Immutable(knownImmutableClasses = [Path.class])
@ToString(includePackage = false, includeNames = true)
class MinecraftMaven extends Cache {

	private final static Log LOGGER = LogFactory.getLog(MinecraftMaven)

	Path storage
	boolean offline

	File resolve(String identifier, String repo) {
		resolve(MavenIdentifier.of(identifier), repo)
	}

	File resolve(MavenIdentifier id, String repo) {
		LOGGER.trace "Resolving dependency: $id.identifier in repo $repo"

		def localPath = storage.resolve(id.path).toFile()

		//Check if non pack.xz file exists
		if (id.ext.endsWith(".pack.xz")) {
			def jarPath = storage.resolve(id.copyWith(ext: id.ext.replaceAll('\\.pack\\.xz$', "")).path).toFile()
			if (jarPath.exists()) {
				return jarPath
			}
		}

		if (!localPath.exists()) {

			if (offline) throw new OfflineException("Cannot download $id.identifier")

			if (!repo.endsWith("/")) {
				repo += "/"
			}

			def artifactURL = "$repo$id.path".toURL()
			localPath.parentFile.mkdirs()

			LOGGER.info "Downloading $artifactURL"
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
