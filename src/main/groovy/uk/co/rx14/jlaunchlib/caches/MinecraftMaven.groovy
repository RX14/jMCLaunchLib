package uk.co.rx14.jlaunchlib.caches

import groovy.transform.Immutable
import groovy.transform.ToString
import org.apache.commons.io.IOUtils
import org.apache.commons.io.input.BoundedInputStream
import org.tukaani.xz.XZInputStream
import uk.co.rx14.jlaunchlib.Constants
import uk.co.rx14.jlaunchlib.MinecraftVersion
import uk.co.rx14.jlaunchlib.util.OS
import uk.co.rx14.jlaunchlib.util.Strings
import uk.co.rx14.jlaunchlib.util.Zip

import java.nio.file.Path
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.jar.Pack200
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

		//Check if non pack.xz file exists
		if (id.ext.endsWith(".pack.xz")) {
			Path jarPath = storage.resolve(id.copyWith(ext: id.ext.replaceAll('\\.pack\\.xz$', "")).path)
			if (jarPath.exists()) {
				return jarPath.toFile()
			}
		}

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

			//Hacks
			switch (id.artifact) {
				case "forge":
					id = id.copyWith(classifier: "universal")
					break
				case "minecraftforge":
					id = new MavenIdentifier(group: "net.minecraftforge", artifact: "forge", version: id.version, classifier: "universal")
					break
			}

			Constants.XZLibs.each { XZGroup ->
				if (id.group.startsWith(XZGroup)) {
					id = id.copyWith(ext: "jar.pack.xz")
				}
			}

			if (lib.natives) {
				lib.natives.each { Map.Entry entry ->
					if (OS.fromString(entry.key) == OS.CURRENT) {
						id = id.copyWith(classifier: entry.value.replace('${arch}', System.getProperty("sun.arch.data.model")))
					}
				}
			}

			def file = resolve(id, repo)

			def jarFile = new File(file.path.replaceAll('\\.pack\\.xz$', ""))
			if (file.name.endsWith(".pack.xz") && !jarFile.exists()) {
				extractPackXz(file, jarFile)
				file = jarFile
			}

			if (lib.extract) {
				LOGGER.info "Extracting $file to natives directory $nativesDirectory"
				Zip.extractWithExclude(file, nativesDirectory, lib.extract.exclude)
			}

			file
		}
	}

	private void extractPackXz(File pack, File jar) {
		LOGGER.info "Extracting pack $pack"

		def packFile = new File(pack.path.replaceFirst('\\.xz$', ""))

		def packFileStream = packFile.newOutputStream()
		try {
			LOGGER.fine "Unpacking xz..."
			IOUtils.copy(new XZInputStream(pack.newInputStream()), packFileStream)
		} finally {
			packFileStream.close()
		}

		def raf = new RandomAccessFile(packFile, "r")
		int length = packFile.length()

		byte[] sum = new byte[8]

		raf.seek(length - 8)
		raf.readFully(sum)

		if (!new String(sum, 4, 4).equals("SIGN")) {
			LOGGER.severe "Unpacking $pack failed: Signature missing."
		}

		def len =
			((sum[0] & 0xFF)) |
			((sum[1] & 0xFF) << 8) |
			((sum[2] & 0xFF) << 16) |
			((sum[3] & 0xFF) << 24);

		def checksums = new byte[len]
		raf.seek(length - len - 8)
		raf.readFully(checksums)
		raf.close()

		def packStream = new BoundedInputStream(packFile.newInputStream(), length - len - 8)
		def jarStream = new JarOutputStream(new FileOutputStream(jar))

		LOGGER.fine "Unpacking pack200..."
		Pack200.newUnpacker().unpack(packStream, jarStream)

		JarEntry checksumsFile = new JarEntry("checksums.sha1");
		checksumsFile.setTime(0);
		jarStream.putNextEntry(checksumsFile);
		jarStream.write(checksums);
		jarStream.closeEntry();

		jarStream.close();
		packStream.close();

		pack.delete()
		packFile.delete()
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
