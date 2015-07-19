package uk.co.rx14.jmclaunchlib.tasks

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import uk.co.rx14.jmclaunchlib.LaunchSpec
import uk.co.rx14.jmclaunchlib.auth.MinecraftAuthResult
import uk.co.rx14.jmclaunchlib.auth.PasswordSupplier
import uk.co.rx14.jmclaunchlib.auth.YggdrasilAuth
import uk.co.rx14.jmclaunchlib.util.Task

class LoginTask implements Task {

	final int weight
	List<Task> subtasks = [].asImmutable()

	String username
	PasswordSupplier passwordSupplier
	boolean offline
	File cacheFile
	LaunchSpec spec

	LoginTask(String username, PasswordSupplier passwordSupplier, boolean offline, File cacheFile, LaunchSpec spec) {
		this.username = username
		this.passwordSupplier = passwordSupplier
		this.offline = offline
		this.cacheFile = cacheFile
		this.spec = spec
		this.weight = offline ? 0 : 5
	}

	@Override
	void before() { }

	@Override
	void after() {
		if (offline) {
			spec.auth = new MinecraftAuthResult(
				accessToken: 'fakeAccessToken',
				clientToken: '',
				valid: true,
				selectedProfile: new MinecraftAuthResult.Profile(
					name: username,
					id: ''
				)
			)
		} else {
			Map<String, MinecraftAuthResult> tokens = [:]
			if (cacheFile.exists()) {
				try {
					tokens = new JsonSlurper().parse(cacheFile) as Map<String, MinecraftAuthResult>
				} catch (ClassCastException e) {
					throw e //TODO remove this once tested
				}
			}

			def authResult = tokens.get(username)

			if (authResult) {
				new YggdrasilAuth().refresh(authResult)
			} else {
				authResult = new YggdrasilAuth().auth(username, passwordSupplier.getPassword(username))
			}

			tokens.put(username, authResult)

			cacheFile.text = JsonOutput.toJson(tokens)

			spec.auth = authResult
		}
	}
}
