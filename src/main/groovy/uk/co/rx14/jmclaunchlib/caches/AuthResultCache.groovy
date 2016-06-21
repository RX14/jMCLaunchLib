package uk.co.rx14.jmclaunchlib.caches

import groovy.json.JsonOutput
import groovy.json.JsonSlurperClassic
import groovy.transform.Immutable
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import uk.co.rx14.jmclaunchlib.auth.MinecraftAuthResult
import uk.co.rx14.jmclaunchlib.auth.PasswordSupplier
import uk.co.rx14.jmclaunchlib.auth.YggdrasilAuth
import uk.co.rx14.jmclaunchlib.exceptions.ForbiddenOperationException

@Immutable(knownImmutableClasses = [File])
class AuthResultCache {

	private final static Log LOGGER = LogFactory.getLog(AuthResultCache)

	File cacheFile

	MinecraftAuthResult get(String username) {
		def authResultMap = this.cache.get(username)
		if (authResultMap == null) return null

		try {
			authResultMap.selectedProfile = authResultMap.selectedProfile as MinecraftAuthResult.Profile //why :(
			return authResultMap as MinecraftAuthResult
		} catch (Exception e) {
			LOGGER.warn "Failed to parse auth.json: Invalid authResultMap", e
			return null
		}
	}

	MinecraftAuthResult getValid(String username, PasswordSupplier passwordSupplier) {
		def authResult = get(username)

		switch (authResult) {
			case { authResult != null }:
				try {
					authResult = YggdrasilAuth.refresh(authResult)
					if (authResult.valid) break
				} catch (Exception ignored) {}
				// Fallthrough!

			case { authResult == null }:
				String password = passwordSupplier.getPassword(username, false, null)
				while (true) {
					try {
						authResult = YggdrasilAuth.auth(username, password)
						break
					} catch (ForbiddenOperationException ex) {
						//Try again
						password = passwordSupplier.getPassword(username, true, ex.message)
					}
				}
		}

		put(username, authResult)

		authResult
	}

	void put(String username, MinecraftAuthResult authResult) {
		def cache = this.cache
		cache.put(username, authResult)
		cacheFile.text = JsonOutput.toJson(cache)
	}

	private getCache() {
		if (!cacheFile.exists()) return [:]
		try {
			def cache = new JsonSlurperClassic().parse(cacheFile)
			if (!(cache instanceof Map)) {
				LOGGER.warn "Failed to parse auth.json: Was not an object"
				return [:]
			}
			return cache
		} catch (Exception e) {
			LOGGER.warn "Failed to parse auth.json", e
			return [:]
		}
	}
}
