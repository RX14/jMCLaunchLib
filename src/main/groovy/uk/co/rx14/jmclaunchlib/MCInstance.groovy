package uk.co.rx14.jmclaunchlib

import groovy.transform.CompileStatic
import groovy.transform.Immutable
import groovy.transform.ToString
import groovy.transform.TypeCheckingMode
import uk.co.rx14.jmclaunchlib.auth.Credentials
import uk.co.rx14.jmclaunchlib.auth.MinecraftAuthResult
import uk.co.rx14.jmclaunchlib.auth.YggdrasilAuth
import uk.co.rx14.jmclaunchlib.caches.MinecraftCaches
import uk.co.rx14.jmclaunchlib.util.Zip

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

	static MCInstance create(String MCVersion, Path MCDir, Path cachesDir, Supplier<Credentials> credentialsSupplier) {
		def caches = MinecraftCaches.create(cachesDir)
		def instance = new MCInstance(
			caches: caches,
			minecraftVersion: new MinecraftVersion(MCVersion, caches.versions),
			credentialsSupplier: credentialsSupplier,
			minecraftDirectory: MCDir
		)

		LOGGER.fine "Created $instance"

		instance
	}

	static MCInstance create(String MCVersion, String MCDir, String cachesDir, Supplier<Credentials> credentialsSupplier) {
		create(MCVersion, FileSystems.default.getPath(MCDir), FileSystems.default.getPath(cachesDir), credentialsSupplier)
	}

	static MCInstance createForge(String MCVersion, String forgeVersion, Path MCDir, Path cachesDir, Supplier<Credentials> credentialsSupplier) {
		def caches = MinecraftCaches.create(cachesDir)

		def forgeJson = Zip.extractSingleFile(
			caches.libs.resolve("net.minecraftforge:forge:jar:universal:$forgeVersion", "http://files.minecraftforge.net/maven/"),
			"version.json"
		)

		def instance = new MCInstance(
			caches: caches,
			minecraftVersion: new MinecraftVersion(MCVersion, caches.versions, new String(forgeJson)),
			credentialsSupplier: credentialsSupplier,
			minecraftDirectory: MCDir
		)

		LOGGER.fine "Created $instance"

		instance
	}

	static MCInstance createForge(String MCVersion, String forgeVersion, String MCDir, String cachesDir, Supplier<Credentials> credentialsSupplier) {
		createForge(MCVersion, forgeVersion, FileSystems.default.getPath(MCDir), FileSystems.default.getPath(cachesDir), credentialsSupplier)
	}

	LaunchSpec getOfflineLaunchSpec(String username) {
		def spec = new LaunchSpec()

		spec.offline = true

		spec.auth = new MinecraftAuthResult(
			accessToken: '',
			clientToken: '',
			valid: true,
			selectedProfile: new MinecraftAuthResult.Profile(
				name: username,
				id: ''
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

		spec.minecraftDirectory = minecraftDirectory

		getting "Minecraft Libraries", {
			spec.classpath.addAll(caches.libs.getLibs(minecraftVersion, caches.natives.resolve(minecraftVersion.version)))
		}

		getting "Minecraft Jar", {
			caches.versions.get(minecraftVersion.jarDownloadUrl)
			spec.classpath.add(caches.versions.getPath(minecraftVersion.jarDownloadUrl).toFile())
		}

		getting "Minecraft Assets", {
			spec.assetsPath = caches.assets.getAssets(minecraftVersion)
		}

		spec.launchArgs = getArgs(spec)

		spec.jvmArgs = ["-Djava.library.path=${caches.natives.resolve(minecraftVersion.version).toAbsolutePath()}"].toArray()

		spec.mainClass = minecraftVersion.mainClass
	}

	@CompileStatic(TypeCheckingMode.SKIP)
	private String[] getArgs(LaunchSpec spec) {
		String args = minecraftVersion.minecraftArguments

		args = args.replace('${auth_player_name}', spec.auth.selectedProfile.name)
		args = args.replace('${version_name}', minecraftVersion.version)
		args = args.replace('${game_directory}', "${minecraftDirectory.toAbsolutePath()}")
		args = args.replace('${game_assets}', "${spec.assetsPath.toAbsolutePath()}")
		args = args.replace('${assets_root}', "${caches.assets.storage.toAbsolutePath()}")
		args = args.replace('${assets_index_name}', minecraftVersion.assets)
		args = args.replace('${user_properties}', "{}")
		args = args.replace('--userType ${user_type}', "")
		args = args.replace('${auth_uuid}', spec.auth.clientToken)
		args = args.replace('${auth_access_token}', spec.auth.accessToken)
		args = args.replace('${auth_session}', spec.auth.accessToken)

		args.split(" ")
	}

	private static getting(String name, Closure closure) {
		LOGGER.info "Getting $name"
		def startTime = System.nanoTime()
		closure.call()
		def time = System.nanoTime() - startTime
		LOGGER.info "Got $name in ${time / 1000000000}s"
	}

	static class LaunchSpec {
		Path minecraftDirectory
		List<File> classpath = new ArrayList<>()
		Path assetsPath
		MinecraftAuthResult auth
		String[] launchArgs
		String[] jvmArgs
		boolean offline
		String mainClass

		String getClasspathString() {
			def cp = ""

			classpath.each { File file ->
				cp += "$file.absoluteFile$File.pathSeparatorChar"
			}

			cp = cp.substring(0, cp.length() - 1) //Remove last separator

			cp
		}

		String getClasspathArg() {
			"-cp $classpathString"
		}

		String[] getJavaCommandlineArray() {
			jvmArgs + ["-cp", classpathString, mainClass] + launchArgs
		}

		String getJavaCommandline() {
			"${jvmArgs.join(" ")} $classpathArg $mainClass ${launchArgs.join(" ")}"
		}

		Process run(Path javaExecutable) {
			def bin = [javaExecutable.toString()] as String[]
			(bin + javaCommandlineArray).execute(null as List, minecraftDirectory.toFile())
		}
	}
}
