package uk.co.rx14.jlaunchlib.auth;

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.Unirest
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

public class YggdrasilAuth implements MinecraftAuth {

	@Override
	MinecraftAuthResult auth() {
		HttpResponse<String> reponse = Unirest.post("https://authserver.mojang.com/authenticate")
			.body(JsonOutput.toJson([
				agent: [
					name: "Minecraft",
					version: 1
				],
				username: username,
				password: password
			]))
			.asString();

		def res = new JsonSlurper().parseText(reponse.body)

		if (res.error) {
			new MinecraftAuthResult(null, null, null, false)
		} else {
			new MinecraftAuthResult(res.accessToken, res.clientToken, new Profile(res.selectedProfile.id, res.selectedProfile.name), true)
		}
	}

	@Override
	MinecraftAuthResult refresh(MinecraftAuthResult previous) {
		HttpResponse<String> reponse = Unirest.post("https://authserver.mojang.com/refresh")
			.body(JsonOutput.toJson([
				accessToken: previous.accessToken,
				clientToken: previous.clientToken
			]))
			.asString();

		def res = new JsonSlurper().parseText(reponse.body)

		if (res.error == "ForbiddenOperationException") {
			new MinecraftAuthResult(previous.accessToken, previous.clientToken, previous.selectedProfile, false)
		} else {
			def profile = new Profile(res.selectedProfile.id, res.selectedProfile.name)
			new MinecraftAuthResult(res.accessToken, res.clientToken, profile, true)
		}
	}
}
