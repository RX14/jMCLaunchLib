package uk.co.rx14.jmclaunchlib.auth

import groovy.transform.Immutable
import groovy.transform.ToString

@ToString(includePackage = false, includeNames = true)
@Immutable(copyWith = true)
class MinecraftAuthResult {
	String accessToken, clientToken
	Profile selectedProfile
	boolean valid

	@ToString(includePackage = false, includeNames = true)
	@Immutable(copyWith = true)
	static class Profile {
		String name, id
	}
}
