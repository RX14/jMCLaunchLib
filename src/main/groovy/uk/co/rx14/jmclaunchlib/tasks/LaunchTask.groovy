package uk.co.rx14.jmclaunchlib.tasks

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import uk.co.rx14.jmclaunchlib.LaunchSpec
import uk.co.rx14.jmclaunchlib.MinecraftVersion
import uk.co.rx14.jmclaunchlib.auth.PasswordSupplier
import uk.co.rx14.jmclaunchlib.caches.MinecraftCaches
import uk.co.rx14.jmclaunchlib.util.Strings
import uk.co.rx14.jmclaunchlib.util.Task

import java.util.concurrent.CopyOnWriteArrayList

@CompileStatic
class LaunchTask implements Task {

	final int weight = 1
	private List<Task> subtasks = new CopyOnWriteArrayList<>()
	final String description = "Prepare to launch Minecraft"

	LaunchSpec spec

	private MinecraftCaches caches
	private MinecraftVersion minecraftVersion

	private String username
	private PasswordSupplier passwordSupplier

	LaunchTask(LaunchSpec spec, MinecraftCaches caches, MinecraftVersion minecraftVersion, String username, PasswordSupplier passwordSupplier) {
		this.spec = spec
		this.caches = caches
		this.minecraftVersion = minecraftVersion
		this.username = username
		this.passwordSupplier = passwordSupplier
	}

	@Override
	void before() {
		subtasks << new LoginTask(username, passwordSupplier, spec.offline, caches.storage.resolve("auth.json").toFile(), spec)
		subtasks << new LibsTask(caches.libs, minecraftVersion, caches.natives.resolve(minecraftVersion.version), spec)
		subtasks << new MinecraftJarTask(caches.versions, minecraftVersion, spec)
		subtasks << new AssetsTask(minecraftVersion, caches.assets, spec)
	}

	@Override
	void after() {
		spec.launchArgs = getArgs(spec)

		spec.jvmArgs = ["-Djava.library.path=${caches.natives.resolve(minecraftVersion.version).toAbsolutePath()}".toString()]

		spec.mainClass = minecraftVersion.mainClass
	}

	@CompileDynamic
	private List<String> getArgs(LaunchSpec spec) {
		def args = minecraftVersion.minecraftArguments.split(" ")

		args = args.collect { String arg ->
			arg.replace('${auth_player_name}', spec.auth.selectedProfile.name)
			   .replace('${version_name}', minecraftVersion.version)
			   .replace('${game_directory}', "${spec.minecraftDirectory.toAbsolutePath()}")
			   .replace('${game_assets}', "${spec.assetsPath.toAbsolutePath()}")
			   .replace('${assets_root}', "${caches.assets.storage.toAbsolutePath()}")
			   .replace('${assets_index_name}', minecraftVersion.assets)
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
}
