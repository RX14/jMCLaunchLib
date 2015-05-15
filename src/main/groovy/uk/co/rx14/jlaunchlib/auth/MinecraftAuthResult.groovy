package uk.co.rx14.jlaunchlib.auth

import groovy.transform.Immutable;

@Immutable class MinecraftAuthResult {
	String accessToken, clientToken
	Profile selectedProfile
	boolean valid
}
