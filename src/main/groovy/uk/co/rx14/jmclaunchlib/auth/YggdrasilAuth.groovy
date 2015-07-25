package uk.co.rx14.jmclaunchlib.auth

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.Unirest
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import uk.co.rx14.jmclaunchlib.exceptions.ForbiddenOperationException

import static uk.co.rx14.jmclaunchlib.auth.MinecraftAuthResult.Profile

public class YggdrasilAuth {

	private final static Log LOGGER = LogFactory.getLog(YggdrasilAuth)

	/**
	 * Uses the given credentials to generate a new
	 * {@link MinecraftAuthResult}.
	 *
	 * @param username the username
	 * @param password the password
	 * @return the authentication result to use in the minecraft arguments.
	 * @throws uk.co.rx14.jmclaunchlib.exceptions.ForbiddenOperationException when
	 * the credentials are invalid.
	 * @throws IllegalArgumentException when something is null.
	 */
	static MinecraftAuthResult auth(String username, String password) {
		LOGGER.trace "Starting Minecraft authentication"
		if (!username) throw new IllegalArgumentException("Username is null")
		if (!password) throw new IllegalArgumentException("Password is null")

		def res = request("authenticate", [
			agent   : [
				name   : "Minecraft",
				version: 1
			],
			username: username,
			password: password
		])

		if (res.error) {
			def exception
			if (res.error == "ForbiddenOperationException") {
				exception = new ForbiddenOperationException(res.errorMessage)
			} else if (res.error == "IllegalArgumentException") {
				exception = new IllegalArgumentException(res.errorMessage)
			} else {
				exception = new RuntimeException(res.errorMessage)
			}
			LOGGER.warn "Minecraft authentication failed with: $res", exception
			throw exception
		}

		LOGGER.debug "Authenticated successfully"

		new MinecraftAuthResult(
			accessToken: res.accessToken,
			clientToken: res.clientToken,
			selectedProfile: new Profile(
				name: res.selectedProfile.name,
				id: res.selectedProfile.id
			),
			valid: true
		)
	}

	/**
	 * Refreshes a previously-valid {@link MinecraftAuthResult}. If the
	 * refreshing fails, it returns an auth result where valid is set to false.
	 *
	 * @return the {@link MinecraftAuthResult}.
	 * @throws IllegalArgumentException when something is null.
	 */
	static MinecraftAuthResult refresh(MinecraftAuthResult previous) {
		LOGGER.trace "Starting Minecraft token refresh"

		def res = request("refresh", [
			accessToken: previous.accessToken,
			clientToken: previous.clientToken
		])

		if (res.error) {
			if (res.error == "ForbiddenOperationException" && res.errorMessage == "Invalid token.") {
				LOGGER.debug "Token is invalid"
				return previous.copyWith(valid: false)
			}

			def exception
			if (res.error == "ForbiddenOperationException") {
				exception = new ForbiddenOperationException((String) res.errorMessage)
			} else if (res.error == "IllegalArgumentException") {
				exception = new IllegalArgumentException((String) res.errorMessage)
			} else {
				exception = new RuntimeException((String) res.errorMessage)
			}
			LOGGER.warn "Minecraft token refresh failed with: $res", exception
			throw exception
		}

		LOGGER.debug "Refreshed token successfully"

		new MinecraftAuthResult(
			accessToken: res.accessToken,
			clientToken: res.clientToken,
			selectedProfile: new Profile(
				name: res.selectedProfile.name,
				id: res.selectedProfile.id
			),
			valid: true
		)
	}

	private static request(String path, body) {
		def jsonBody = JsonOutput.toJson(body)

		def startTime = System.nanoTime()
		HttpResponse<String> response =
			Unirest.post("https://authserver.mojang.com/$path")
			       .header("Content-Type", "application/json")
			       .body(jsonBody)
			       .asString();

		def time = System.nanoTime() - startTime
		LOGGER.trace "Completed $path request in ${time / 1000000000}s"

		new JsonSlurper().parseText(response.body)
	}
}
