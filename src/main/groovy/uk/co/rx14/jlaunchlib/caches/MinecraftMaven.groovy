package uk.co.rx14.jlaunchlib.caches

import groovy.transform.Immutable
import groovy.transform.ToString
import uk.co.rx14.jlaunchlib.Constants
import uk.co.rx14.jlaunchlib.MinecraftVersion
import uk.co.rx14.jlaunchlib.util.OS
import uk.co.rx14.jlaunchlib.util.Strings
import uk.co.rx14.jlaunchlib.util.Zip

import java.nio.file.Path
import java.util.logging.Logger
import java.util.stream.Collectors

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

		def localPath = storage.resolve(id.path)

		if (!localPath.exists()) {

			if (!repo.endsWith("/")) {
				repo += "/"
			}

			def artifactURL = "$repo$id.path".toURL()
			localPath.parentMkdirs()

			LOGGER.info "Downloading $artifactURL"
			localPath.bytes = artifactURL.bytes
		}

		localPath.toFile()
	}

	File[] getLibs(MinecraftVersion version, Path nativesDirectory) {
		List passedLibs = version.libs.stream()
		                         .filter(parseRules)
		                         .collect(Collectors.toList())

		passedLibs.collect { lib ->
			MavenIdentifier id = MavenIdentifier.of(lib.name)

			String repo = lib.url ? lib.url : Constants.MinecraftLibsBase

			if (lib.natives) {
				lib.natives.each { Map.Entry entry ->
					if (OS.fromString(entry.key) == OS.CURRENT) {
						id = id.copyWith(classifier: entry.value.replace('${arch}', System.getProperty("sun.arch.data.model")))
					}
				}
			}

			def file = resolve(id, repo)

			if (lib.extract) {
				LOGGER.info "Extracting $file to natives directory $nativesDirectory"
				Zip.extractWithExclude(file, nativesDirectory, lib.extract.exclude)
			}
			file
		}
	}

	private static parseRules = { lib ->
		if (lib.rules) {
			def download = false
			lib.rules.each { rule ->
				LOGGER.finest "Currently ${download ? "allowing" : "disallowing"} $lib.name"
				if (rule.os) {
					if (OS.fromString(rule.os.name) == OS.CURRENT) {
						LOGGER.finest "Rule OS '$rule.os.name' matched '$OS.CURRENT'"
						LOGGER.finest "Applying rule action $rule.action"
						download = rule.action == "allow"
					} else {
						LOGGER.finest "Rule OS '$rule.os.name' did not match '$OS.CURRENT'"
					}
				} else {
					LOGGER.finest "Applying rule action $rule.action"
					download = rule.action == "allow"
				}
			}
			LOGGER.fine "${download ? "Allowed" : "Disallowed"} $lib.name $lib"
			download
		} else true
	}

	@Immutable(copyWith = true)
	@ToString(includePackage = false, includeNames = true)
	static class MavenIdentifier {
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
					throw new IllegalArgumentException("Failed to parse Maven identifier $identifier: wrong length")
			}

			def mavenIdentifier = new MavenIdentifier(
				group: group,
				artifact: artifact,
				version: version,
				ext: ext,
				classifier: classifier
			)

			LOGGER.finest "Parsed MavenIdentifier $identifier as $mavenIdentifier"

			mavenIdentifier
		}
	}
}
