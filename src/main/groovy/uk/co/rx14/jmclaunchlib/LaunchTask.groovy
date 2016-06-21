package uk.co.rx14.jmclaunchlib

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import uk.co.rx14.jmclaunchlib.auth.PasswordSupplier
import uk.co.rx14.jmclaunchlib.caches.MinecraftCaches
import uk.co.rx14.jmclaunchlib.tasks.AssetsTask
import uk.co.rx14.jmclaunchlib.tasks.LibsTask
import uk.co.rx14.jmclaunchlib.tasks.LoginTask
import uk.co.rx14.jmclaunchlib.tasks.MinecraftJarTask
import uk.co.rx14.jmclaunchlib.util.Strings
import uk.co.rx14.jmclaunchlib.util.Task
import uk.co.rx14.jmclaunchlib.version.Version

import java.util.concurrent.CopyOnWriteArrayList

@CompileStatic
class LaunchTask implements Task {

	final int weight = 1
	private List<Task> subtasks = new CopyOnWriteArrayList<>()
	final String description = "Prepare to launch Minecraft"

	final LaunchSpec spec

	private MinecraftCaches caches
	private Version version

	private String username
	private PasswordSupplier passwordSupplier

	LaunchTask(LaunchSpec spec, MinecraftCaches caches, Version version, String username, PasswordSupplier passwordSupplier) {
		this.spec = spec
		this.caches = caches
		this.version = version
		this.username = username
		this.passwordSupplier = passwordSupplier
	}

	@Override
	void before() {
		spec.nativesDirectory = caches.natives.resolve(version.MCVersion)

		if (spec.netOffline) spec.offline = true

		subtasks << new LoginTask(username, passwordSupplier, spec.offline, caches.auth, spec)
		subtasks << new LibsTask(caches.libs, version, spec.nativesDirectory, spec)
		subtasks << new MinecraftJarTask(caches.versions, version, spec)
		subtasks << new AssetsTask(version, caches.assets, spec)
	}

	@Override
	void after() {
		spec.launchArgs = getArgs(spec)

		spec.jvmArgs = ["-Djava.library.path=${spec.nativesDirectory.toAbsolutePath()}".toString()]

		spec.mainClass = version.mainClass
	}

	@CompileDynamic
	private List<String> getArgs(LaunchSpec spec) {
		def args = version.minecraftArguments.split(" ")

		args = args.collect { String arg ->
			arg.replace('${auth_player_name}', spec.auth.selectedProfile.name)
			   .replace('${version_name}', version.MCVersion)
			   .replace('${game_directory}', "${spec.minecraftDirectory.toAbsolutePath()}")
			   .replace('${game_assets}', "${spec.assetsPath.toAbsolutePath()}")
			   .replace('${assets_root}', "${caches.assets.storage.toAbsolutePath()}")
			   .replace('${assets_index_name}', version.assetsVersion)
			   .replace('${user_properties}', "{}")
			   .replace('${auth_uuid}', spec.auth.selectedProfile.id)
			   .replace('${auth_access_token}', spec.auth.accessToken)
			   .replace('${auth_session}', spec.auth.accessToken)
		}

		//If the argument flag value is empty remove the flag (set it to empty string as a marker value)
		args = args.eachWithIndex { arg, i ->
			if (Strings.isEmpty(arg) || arg == '${user_type}') {
				args[i - 1] = ""
			}
		}

		//Filter out the empty strings in the args
		args.findAll {
			Strings.isNotEmpty(it) && it != '${user_type}'
		}
	}

	List<Task> getSubtasks() { subtasks.asImmutable() }

	LaunchSpec getSpec() {
		start()
		spec
	}
}
