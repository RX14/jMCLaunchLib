package uk.co.rx14.jmclaunchlib.version

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import uk.co.rx14.jmclaunchlib.caches.EtagCache
import uk.co.rx14.jmclaunchlib.caches.HashCache

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
}
