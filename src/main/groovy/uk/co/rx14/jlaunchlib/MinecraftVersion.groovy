package uk.co.rx14.jlaunchlib

import groovy.json.JsonSlurper
import uk.co.rx14.jlaunchlib.caches.EtagCache

class MinecraftVersion {
	final String version
	final EtagCache versionCache
	private versionJson

	MinecraftVersion(String minecraftVersion, EtagCache versionCache) {
		this.version = minecraftVersion
		this.versionCache = versionCache
	}

	def get() {
		if (!versionJson) {
			versionJson = new JsonSlurper().parseText(
				new String(versionCache.get("$Constants.MinecraftVersionsBase/$version/${version}.json".toURL()))
			)
		}
		versionJson
	}

	List getLibs() {
		get().libraries
	}

	URL getDownloadUrl() {
		"$Constants.MinecraftVersionsBase/$version/${version}.jar".toURL()
	}
}
