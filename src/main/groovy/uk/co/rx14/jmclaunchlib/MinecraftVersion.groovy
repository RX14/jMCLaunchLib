package uk.co.rx14.jmclaunchlib

import groovy.json.JsonSlurper
import uk.co.rx14.jmclaunchlib.caches.EtagCache

import java.util.logging.Logger


class MinecraftVersion {

	private static final Logger LOGGER = Logger.getLogger(MinecraftVersion.class.getName())

	final String version
	final EtagCache versionCache
	final private Map json

	MinecraftVersion(String minecraftVersion, EtagCache versionCache, String versionJson) {
		this.version = minecraftVersion
		this.versionCache = versionCache

		this.json = applyParent(
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

	List getLibs() {
		json.libraries.clone()
	}

	URL getJarDownloadUrl() {
		"$Constants.MinecraftVersionsBase/$version/${version}.jar".toURL()
	}

	String getAssets() {
		json.assets
	}

	String getMinecraftArguments() {
		json.minecraftArguments
	}

	String getMainClass() {
		json.mainClass
	}

	private Map applyParent(versionJson) {
		if (versionJson.inheritsFrom) {
			LOGGER.info "[$versionJson.id] Loading parent json $versionJson.inheritsFrom"

			def parent = new MinecraftVersion(versionJson.inheritsFrom, versionCache)
			versionJson.libraries.addAll(parent.libs)

			parent.json + versionJson
		} else {
			versionJson
		}
	}
}
