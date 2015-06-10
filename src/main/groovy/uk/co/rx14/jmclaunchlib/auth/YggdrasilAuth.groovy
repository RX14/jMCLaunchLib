package uk.co.rx14.jmclaunchlib.auth

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.Unirest
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import uk.co.rx14.jmclaunchlib.exceptions.ForbiddenOperationException

import java.util.function.Supplier
import java.util.logging.Logger

import static uk.co.rx14.jmclaunchlib.auth.MinecraftAuthResult.Profile

public class YggdrasilAuth implements MinecraftAuth {

	private final static Logger LOGGER = Logger.getLogger(YggdrasilAuth.class.name)

	@Override
	MinecraftAuthResult auth(Supplier<Credentials> provider) {
		LOGGER.info "Starting Minecraft authentication"
		if (!provider) throw new IllegalArgumentException("CredentialsProvider is null")
		Credentials credentials = provider.get();
		if (!credentials) throw new IllegalArgumentException("Credentials are null")

		def res = request("authenticate", [
			agent   : [
				name   : "Minecraft",
				version: 1
			],
			username: credentials.username,
			password: credentials.password
		])

		if (res.error) {
			LOGGER.warning "Minecraft authentication failed with: $res"
			if (res.error == "ForbiddenOperationException") {
				throw new ForbiddenOperationException(res.errorMessage)
			} else if (res.error == "IllegalArgumentException") {
				throw new IllegalArgumentException(res.errorMessage)
			} else {
				throw new RuntimeException(res.errorMessage)
			}
		}

		LOGGER.info "Authenticated successfully"

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

	@Override
	MinecraftAuthResult refresh(MinecraftAuthResult previous) {
		LOGGER.info "Starting Minecraft token refresh"
		if (!previous.valid) throw new IllegalArgumentException("MinecraftAuthResult is not valid")

		def res = request("refresh", [
			accessToken: previous.accessToken,
			clientToken: previous.clientToken
		])

		if (res.error) {
			if (res.error == "ForbiddenOperationException" && res.errorMessage == "Invalid token.") {
				return new MinecraftAuthResult(
					accessToken: previous.accessToken,
					clientToken: previous.clientToken,
					selectedProfile: previous.selectedProfile,
					valid: false
				)
			}

			LOGGER.warning "Minecraft token refresh failed with: $res"

			if (res.error == "ForbiddenOperationException") {
				throw new ForbiddenOperationException((String) res.errorMessage)
			} else if (res.error == "IllegalArgumentException") {
				throw new IllegalArgumentException((String) res.errorMessage)
			} else {
				throw new RuntimeException((String) res.errorMessage)
			}
		}

		LOGGER.info "Refreshed token successfully"

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

	def request(String path, body) {
		def jsonBody = JsonOutput.toJson(body)

		def startTime = System.nanoTime()
		HttpResponse<String> response =
			Unirest.post("https://authserver.mojang.com/$path")
			       .header("Content-Type", "application/json")
			       .body(jsonBody)
			       .asString();

		def time = System.nanoTime() - startTime
		LOGGER.fine "Completed $path request in ${time / 1000000000}s"

		new JsonSlurper().parseText(response.body)
	}
}
