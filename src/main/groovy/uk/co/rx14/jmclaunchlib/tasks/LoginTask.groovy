package uk.co.rx14.jmclaunchlib.tasks

import groovy.json.JsonOutput
import groovy.json.JsonSlurperClassic
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import uk.co.rx14.jmclaunchlib.LaunchSpec
import uk.co.rx14.jmclaunchlib.auth.MinecraftAuthResult
import uk.co.rx14.jmclaunchlib.auth.PasswordSupplier
import uk.co.rx14.jmclaunchlib.auth.YggdrasilAuth
import uk.co.rx14.jmclaunchlib.caches.AuthResultCache
import uk.co.rx14.jmclaunchlib.exceptions.ForbiddenOperationException
import uk.co.rx14.jmclaunchlib.util.Task

class LoginTask implements Task {

	private final static Log LOGGER = LogFactory.getLog(LoginTask)

	final int weight
	List<Task> subtasks = [].asImmutable()
	final String description = "Log in"

	String username
	PasswordSupplier passwordSupplier
	boolean offline
	AuthResultCache cache
	LaunchSpec spec

	LoginTask(String username, PasswordSupplier passwordSupplier, boolean offline, AuthResultCache cache, LaunchSpec spec) {
		this.username = username
		this.passwordSupplier = passwordSupplier
		this.offline = offline
		this.cache = cache
		this.spec = spec
		this.weight = offline ? 0 : 5
	}

	@Override
	void before() {}

	@Override
	void after() {
		if (offline) {
			spec.auth = new MinecraftAuthResult(
				accessToken: '-',
				clientToken: '',
				valid: true,
				selectedProfile: new MinecraftAuthResult.Profile(
					name: username,
					id: ''
				)
			)
		} else {
			spec.auth = cache.getValid(username, passwordSupplier)
		}
	}
}
