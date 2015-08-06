package uk.co.rx14.jmclaunchlib.version

import groovy.json.JsonSlurper
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.Synchronized
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import uk.co.rx14.jmclaunchlib.Constants
import uk.co.rx14.jmclaunchlib.caches.EtagCache

@CompileStatic
class MinecraftVersion implements Version {

	private static final Log LOGGER = LogFactory.getLog(MinecraftVersion)

	final String MCVersion
	final String uniqueVersion

	final EtagCache cache
	private Map json

	MinecraftVersion(String MCVersion, EtagCache versionsCache) {
		this.MCVersion = MCVersion
		this.uniqueVersion = MCVersion
		this.cache = versionsCache
	}

	@Override
	@CompileDynamic //Groovy bugs
	List getLibs() {
		ensureJson()
		json.libraries.clone() as List
	}

	@Override
	URL getJarDownloadUrl() {
		"$Constants.MinecraftVersionsBase/$MCVersion/${MCVersion}.jar".toURL()
	}

	@Override
	String getAssetsVersion() {
		ensureJson()
		json.assets
	}

	@Override
	String getMinecraftArguments() {
		ensureJson()
		json.minecraftArguments
	}

	@Override
	String getMainClass() {
		ensureJson()
		json.mainClass
	}

	@Synchronized
	private ensureJson() {
		if (json) return

		this.json = Versions.applyParent(
			new JsonSlurper().parse(new String(
				cache.get("$Constants.MinecraftVersionsBase/$MCVersion/${MCVersion}.json".toURL())
			).chars) as Map,
			cache
		)
	}
}
