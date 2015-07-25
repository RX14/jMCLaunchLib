package uk.co.rx14.jmclaunchlib.util

import groovy.transform.Immutable
import groovy.transform.ToString
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

@Immutable(copyWith = true)
@ToString(includePackage = false, includeNames = true)
class MavenIdentifier {

	private final static Log LOGGER = LogFactory.getLog(MavenIdentifier)

	String group, artifact, version, classifier, ext

	String getGroupPath() {
		group.replaceAll("\\.", "/")
	}

	String getArtifactPath() {
		"$groupPath/$artifact"
	}

	String getVersionPath() {
		"$artifactPath/$version"
	}

	String getFilename() {
		def classifier = Strings.isEmpty(this.classifier) ? "" : "-${this.classifier}"
		"$artifact-$version$classifier.$ext"
	}

	String getPath() {
		"$versionPath/$filename"
	}

	String getIdentifier() {
		"$group:$artifact${Strings.isEmpty(ext) ? "" : ":$ext"}${Strings.isEmpty(classifier) ? "" : ":$classifier"}:$version"
	}

	static MavenIdentifier of(String identifier) {
		String[] parts = identifier.split(":")

		String group, artifact, version
		def classifier = ""
		def ext = "jar"

		switch (parts.length) {
			case 5:
				classifier = parts[3]
			case 4:
				ext = parts[2]
			case 3:
				group = parts[0]
				artifact = parts[1]
				version = parts[parts.length - 1]
				break
			default:
				def e = new IllegalArgumentException("Failed to parse Maven identifier $identifier: wrong length")
				LOGGER.debug "", e
				throw e
		}

		def mavenIdentifier = new MavenIdentifier(
			group: group,
			artifact: artifact,
			version: version,
			ext: ext,
			classifier: classifier
		)

		LOGGER.trace "Parsed MavenIdentifier $identifier as $mavenIdentifier"

		mavenIdentifier
	}
}
