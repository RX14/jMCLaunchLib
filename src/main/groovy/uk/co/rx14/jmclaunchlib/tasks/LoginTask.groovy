package uk.co.rx14.jmclaunchlib.tasks

import groovy.json.JsonOutput
import groovy.json.JsonSlurperClassic
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import uk.co.rx14.jmclaunchlib.LaunchSpec
import uk.co.rx14.jmclaunchlib.auth.MinecraftAuthResult
import uk.co.rx14.jmclaunchlib.auth.PasswordSupplier
import uk.co.rx14.jmclaunchlib.auth.YggdrasilAuth
import uk.co.rx14.jmclaunchlib.util.Task

class LoginTask implements Task {

	private final static Log LOGGER = LogFactory.getLog(LoginTask)

	final int weight
	List<Task> subtasks = [].asImmutable()
	final String description = "Log in"

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
	void before() {}

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
			def tokens = [:]
			if (cacheFile.exists()) {
				try {
					tokens = new JsonSlurperClassic().parse(cacheFile)
				} catch (Exception e) {
					LOGGER.warn "Failed to parse auth.json", e
				}
			}

			def authResult = tokens.get(username)

			switch (authResult) {
				case { authResult != null }:
					try {
						authResult.selectedProfile = authResult.selectedProfile as MinecraftAuthResult.Profile //why :(
						authResult = authResult as MinecraftAuthResult
						authResult = YggdrasilAuth.refresh(authResult)
						if (authResult.valid) break
					} catch (Exception ignored) {}

				case { authResult == null }:
					authResult = YggdrasilAuth.auth(username, passwordSupplier.getPassword(username))
			}

			tokens.put(username, authResult)

			cacheFile.text = JsonOutput.toJson(tokens)

			spec.auth = authResult
		}
	}
}
