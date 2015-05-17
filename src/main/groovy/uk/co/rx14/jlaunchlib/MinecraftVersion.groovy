package uk.co.rx14.jlaunchlib

import groovy.json.JsonSlurper
import uk.co.rx14.jlaunchlib.caches.EtagCache

class MinecraftVersion {
	final String minecraftVersion
	final EtagCache versionCache
	private versionJson

	MinecraftVersion(String minecraftVersion, EtagCache versionCache) {
		this.minecraftVersion = minecraftVersion
		this.versionCache = versionCache
	}

	def get() {
		if (!versionJson) {
			versionJson = new JsonSlurper().parseText(
				versionCache.get("$Constants.MinecraftVersionsBase/$minecraftVersion/${minecraftVersion}.json".toURL())
			)
		}
		versionJson
	}

	List getLibs() {
		get().libraries
	}
}
