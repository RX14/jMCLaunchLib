package uk.co.rx14.jlaunchlib.caches

import org.apache.commons.io.IOUtils
import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem
import org.jboss.shrinkwrap.resolver.api.maven.Maven
import uk.co.rx14.jlaunchlib.Constants
import uk.co.rx14.jlaunchlib.MinecraftVersion
import uk.co.rx14.jlaunchlib.util.Minecraft
import uk.co.rx14.jlaunchlib.util.OS
import uk.co.rx14.jlaunchlib.util.Zip

import java.nio.file.Path
import java.util.logging.Logger
import java.util.stream.Collectors
import java.util.zip.ZipFile

class MinecraftMaven {

	private final static Logger LOGGER = Logger.getLogger(MinecraftMaven.class.name)

	static ConfigurableMavenResolverSystem RESOLVER =
		Maven.configureResolver()
		     .withClassPathResolution(false)
		     .withMavenCentralRepo(true)
		     .withRemoteRepo("minecraft-local", Minecraft.minecraftDirectory.resolve("libraries").toUri().toString(), "default")
		     .withRemoteRepo("minecraft-libs", Constants.MinecraftLibsBase, "default")


	static File resolve(String identifier) {
		LOGGER.info "Resolving dependency: $identifier"
		RESOLVER.resolve(identifier).withoutTransitivity().asSingleFile()
	}

	File[] getLibs(MinecraftVersion version, Path nativesDirectory) {
		List passedLibs = version.libs.stream()
		                         .filter(parseRules)
		                         .collect(Collectors.toList())

		passedLibs.collect { lib ->
			String specifier = lib.name
			if (lib.natives) {
				def (String G, String A, String V) = specifier.split(":")
				String C
				lib.natives.each { Map.Entry entry ->
					if (OS.fromString(entry.key) == OS.currentPlatform) {
						C = entry.value.replace('${arch}', System.getProperty("sun.arch.data.model"))
					}
				}
				def file = resolve("$G:$A:jar:$C:$V")
				if (lib.extract) {
					LOGGER.info "Extracting $file to natives directory $nativesDirectory with exclusions: $lib.extract.exclude"
					Zip.extractWithExclude(file, nativesDirectory, lib.extract.exclude)
				}
				file
			} else {
				resolve(specifier)
			}
		}
	}

	def parseRules = { lib ->
		if (lib.rules) {
			def download = false
			lib.rules.each { rule ->
				LOGGER.finest "Currently ${download ? "allowing" : "disallowing"} $lib.name"
				if (rule.os) {
					if (OS.fromString(rule.os.name) == OS.currentPlatform) {
						LOGGER.finest "Rule OS '$rule.os.name' matched '$OS.currentPlatform'"
						LOGGER.finest "Applying rule action $rule.action"
						download = rule.action == "allow"
					} else {
						LOGGER.finest "Rule OS '$rule.os.name' did not match '$OS.currentPlatform'"
					}
				} else {
					LOGGER.finest "Applying rule action $rule.action"
					download = rule.action == "allow"
				}
			}
			LOGGER.info "${download ? "Allowed" : "Disallowed"} $lib.name $lib"
			download
		} else true
	}
}
