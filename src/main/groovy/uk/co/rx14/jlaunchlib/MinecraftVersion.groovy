package uk.co.rx14.jlaunchlib

import groovy.json.JsonSlurper
import uk.co.rx14.jlaunchlib.caches.EtagCache

import java.util.logging.Logger


class MinecraftVersion {

	private static final Logger LOGGER = Logger.getLogger(MinecraftVersion.class.getName())

	final String version
	final EtagCache versionCache
	final private versionJson

	MinecraftVersion(String minecraftVersion, EtagCache versionCache, String versionJson) {
		this.version = minecraftVersion
		this.versionCache = versionCache

		this.versionJson = applyParent(
			new JsonSlurper().parseText(
				versionJson
			)
		)
	}

	MinecraftVersion(String minecraftVersion, EtagCache versionCache, URL jsonURL) {
		this(minecraftVersion, versionCache, new String(versionCache.get(jsonURL)))
	}

	MinecraftVersion(String version, EtagCache versionCache) {
		this(version, versionCache, "$Constants.MinecraftVersionsBase/$version/${version}.json".toURL())
	}

	def get() {
		versionJson
	}

	List getLibs() {
		get().libraries
	}

	URL getJarDownloadUrl() {
		"$Constants.MinecraftVersionsBase/$version/${version}.jar".toURL()
	}

	private applyParent(versionJson) {
		if (versionJson.inheritsFrom) {
			LOGGER.info "[$version] Loading parent json $versionJson.inheritsFrom"

			def parent = new MinecraftVersion(versionJson.inheritsFrom, versionCache)
			versionJson.libs << parent.get().libs

			parent + versionJson
		} else {
			versionJson
		}
	}
}
