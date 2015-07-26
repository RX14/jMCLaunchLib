package uk.co.rx14.jmclaunchlib.version

import groovy.json.JsonSlurper
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import uk.co.rx14.jmclaunchlib.Constants
import uk.co.rx14.jmclaunchlib.caches.EtagCache

class Versions {

	private static final Log LOGGER = LogFactory.getLog(Versions)

	static Map applyParent(Map child, EtagCache cache) {
		if (child.inheritsFrom) {
			LOGGER.debug "[$child.id] Loading parent json $child.inheritsFrom"

			def parent = new MinecraftVersion(child.inheritsFrom, cache)
			child.libraries.addAll(parent.libs)

			parent.json + child
		} else {
			child
		}
	}

	static List<String> minecraftVersions() {
		minecraftVersions(null)
	}

	static List<String> minecraftVersions(EtagCache cache) {
		def URL = "$Constants.MinecraftVersionsBase/versions.json".toURL()

		byte[] data
		if (cache) data = cache.get(URL) else data = URL.bytes

		def list = new JsonSlurper().parse(data)
		list.versions.id
	}
}
