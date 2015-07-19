package uk.co.rx14.jmclaunchlib.tasks

import uk.co.rx14.jmclaunchlib.Constants
import uk.co.rx14.jmclaunchlib.LaunchSpec
import uk.co.rx14.jmclaunchlib.MinecraftVersion
import uk.co.rx14.jmclaunchlib.caches.MinecraftMaven
import uk.co.rx14.jmclaunchlib.util.Compression
import uk.co.rx14.jmclaunchlib.util.MavenIdentifier
import uk.co.rx14.jmclaunchlib.util.OS
import uk.co.rx14.jmclaunchlib.util.Task

import java.nio.file.Path
import java.util.logging.Logger
import java.util.stream.Collectors

class LibsTask implements Task {

	private final static Logger LOGGER = Logger.getLogger(LibsTask.class.name)

	final int weight = 0
	private List<Task> subtasks = [].asImmutable()
	final String description = "Download Libraries"

	MinecraftMaven mavenCache
	MinecraftVersion version
	Path nativesDirectory
	LaunchSpec spec

	LibsTask(MinecraftMaven mavenCache, MinecraftVersion version, Path nativesDirectory, LaunchSpec spec) {
		this.mavenCache = mavenCache
		this.version = version
		this.nativesDirectory = nativesDirectory
		this.spec = spec
	}

	@Override
	void before() {
		subtasks = version.libs.stream()
		                       .filter(parseRules)
		                       .map { new LibTask(it) }
		                       .collect(Collectors.toList())
		                       .asImmutable()
	}

	@Override
	void after() { }

	class LibTask implements Task {
		int weight = 0
		final List<Task> subtasks = [].asImmutable()
		final String description

		def lib

		LibTask(lib) {
			this.lib = lib
			this.description = "Download $lib.name"
		}

		@Override
		void before() { }

		@Override
		void after() {
			MavenIdentifier id = MavenIdentifier.of(lib.name)

			String repo = lib.url ?: Constants.MinecraftLibsBase

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

			//Calculate weight
			if (!mavenCache.exists(id)) {
				weight += 5
				if (id.ext == "jar.pack.xz") weight += 10
			}
			if (lib.extract) weight += 5

			def file = mavenCache.resolve(id, repo)

			def jarFile = new File(file.path.replaceAll('\\.pack\\.xz$', ""))
			if (file.name.endsWith(".pack.xz") && !jarFile.exists()) {
				Compression.extractPackXz(file, jarFile)
				file = jarFile
			}

			if (lib.extract) {
				LOGGER.fine "Extracting $file to natives directory $nativesDirectory"
				Compression.extractZipWithExclude(file, nativesDirectory, lib.extract.exclude)
			}

			spec.classpath << file
		}
	}

	List<Task> getSubtasks() { subtasks.asImmutable() }

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
}
