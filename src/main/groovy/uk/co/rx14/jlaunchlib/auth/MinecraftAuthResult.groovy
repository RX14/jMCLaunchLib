package uk.co.rx14.jlaunchlib.auth

import groovy.transform.Immutable

@Immutable class MinecraftAuthResult {
	String accessToken, clientToken
	Profile selectedProfile
	boolean valid

	@Override
	String toString() {
		"${getClass().getName()}(accessToken: $accessToken, clientToken: $clientToken, selectedProfile: $selectedProfile, valid: $valid)"
	}

	public static @Immutable class Profile {
		String name, id
	}
}
