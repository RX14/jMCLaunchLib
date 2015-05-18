package uk.co.rx14.jlaunchlib

import groovy.transform.CompileStatic
import groovy.transform.Immutable
import groovy.transform.ToString
import groovy.transform.TypeCheckingMode
import uk.co.rx14.jlaunchlib.auth.Credentials
import uk.co.rx14.jlaunchlib.auth.MinecraftAuthResult
import uk.co.rx14.jlaunchlib.auth.YggdrasilAuth
import uk.co.rx14.jlaunchlib.caches.MinecraftCaches

import java.nio.file.FileSystems
import java.nio.file.Path
import java.util.function.Supplier
import java.util.logging.Logger

@CompileStatic
@ToString(includePackage = false, includeNames = true)
@Immutable(knownImmutableClasses = [Path.class, MinecraftVersion.class])
class MCInstance {

	private static final Logger LOGGER = Logger.getLogger(MCInstance.class.getName())

	Path minecraftDirectory
	MinecraftCaches caches
	MinecraftVersion minecraftVersion
	private Supplier<Credentials> credentialsSupplier

	static MCInstance create(String MCVersion, Path minecraftDirectory, Path cachesDir, Supplier<Credentials> credentialsSupplier) {
		def caches = MinecraftCaches.create(cachesDir)
		def instance = new MCInstance(
			caches: caches,
			minecraftVersion: new MinecraftVersion(MCVersion, caches.versions),
			credentialsSupplier: credentialsSupplier,
			minecraftDirectory: minecraftDirectory
		)

		LOGGER.fine "Created $instance"

		instance
	}

	static MCInstance create(String MCVersion, String minecraftDirectory, String cachesDir, Supplier<Credentials> credentialsSupplier) {
		create(MCVersion, FileSystems.default.getPath(minecraftDirectory), FileSystems.default.getPath(cachesDir), credentialsSupplier)
	}

	LaunchSpec getOfflineLaunchSpec(String username) {
		def spec = new LaunchSpec()

		spec.offline = true

		spec.auth = new MinecraftAuthResult(
			accessToken: null,
			clientToken: null,
			valid: true,
			selectedProfile: new MinecraftAuthResult.Profile(
				name: username,
				id: null
			)
		)

		_commonTasks(spec)

		spec
	}

	LaunchSpec getLaunchSpec() {
		def spec = new LaunchSpec()

		spec.offline = false

		LOGGER.info "Logging in..."
		spec.auth = new YggdrasilAuth().auth(credentialsSupplier)

		_commonTasks(spec)

		spec
	}

	@CompileStatic(TypeCheckingMode.SKIP)
	private void _commonTasks(LaunchSpec spec) {
		getting "Minecraft Libraries", {
			spec.classpath.addAll(caches.libs.getLibs(minecraftVersion, caches.natives.resolve(minecraftVersion.version)))
		}

		getting "Minecraft Jar", {
			caches.versions.get(minecraftVersion.downloadUrl)
			spec.classpath.add(caches.versions.getPath(minecraftVersion.downloadUrl).toFile())
		}

		getting "Minecraft Assets", {
			spec.assetsPath = caches.assets.getAssets(minecraftVersion)
		}

		spec.launchArgs = getArgs(spec)

		spec.jvmArgs = "-Djava.library.path=\"${caches.natives.resolve(minecraftVersion.version).toAbsolutePath()}\""

		spec.mainClass = minecraftVersion.get().mainClass
	}

	@CompileStatic(TypeCheckingMode.SKIP)
	private String getArgs(LaunchSpec spec) {
		String args = minecraftVersion.get().minecraftArguments

		if (spec.offline) {
			args.replaceFirst('--username $\\{.*\\}', "")
		} else {
			args = args.replace('${auth_uuid}', spec.auth.clientToken)
			args = args.replace('${auth_access_token}', spec.auth.accessToken)
			args = args.replace('${auth_session}', spec.auth.accessToken)
		}

		args = args.replace('${auth_player_name}', spec.auth.selectedProfile.name)
		args = args.replace('${version_name}', minecraftVersion.version)
		args = args.replace('${game_directory}', "\"${minecraftDirectory.toAbsolutePath()}\"")
		args = args.replace('${game_assets}', "\"${spec.assetsPath.toAbsolutePath()}\"")
		args = args.replace('${assets_root}', "\"${caches.assets.storage.toAbsolutePath()}\"")
		args = args.replace('${assets_index_name}', minecraftVersion.get().assets)
		args = args.replace('${user_properties}', "{}")
		args = args.replace('--userType ${user_type}', "")

		args
	}

	private static getting(String name, Closure closure) {
		LOGGER.info "Getting $name"
		def startTime = System.nanoTime()
		closure.call()
		def time = System.nanoTime() - startTime
		LOGGER.info "Got $name in ${time / 1000000000}s"
	}

	static class LaunchSpec {
		List<File> classpath = new ArrayList<>()
		Path assetsPath
		MinecraftAuthResult auth
		String launchArgs
		String jvmArgs
		boolean offline
		String mainClass

		String getClasspathArg() {
			def cp = "-cp "

			classpath.each { File file ->
				cp += "\"$file.absoluteFile\";"
			}

			cp
		}

		String getJavaCommandline() {
			"$jvmArgs $classpathArg $mainClass $launchArgs"
		}
	}
}
