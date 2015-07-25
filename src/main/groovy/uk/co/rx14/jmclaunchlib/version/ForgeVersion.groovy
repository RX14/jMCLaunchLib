package uk.co.rx14.jmclaunchlib.version

import groovy.json.JsonSlurper
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.Synchronized
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import uk.co.rx14.jmclaunchlib.Constants
import uk.co.rx14.jmclaunchlib.caches.MinecraftCaches
import uk.co.rx14.jmclaunchlib.util.Compression

@CompileStatic
class ForgeVersion implements Version {

	private static final Log LOGGER = LogFactory.getLog(ForgeVersion)

	final String MCVersion
	final String uniqueVersion

	final MinecraftCaches caches
	private Map json

	ForgeVersion(String MCVersion, String ForgeVersion, MinecraftCaches caches) {
		this.MCVersion = MCVersion
		this.uniqueVersion = ForgeVersion
		this.caches = caches
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

		def data = Compression.extractZipSingleFile(
			caches.libs.resolve("net.minecraftforge:forge:jar:universal:$uniqueVersion", "http://files.minecraftforge.net/maven/"),
			"version.json"
		)

		this.json = Versions.applyParent(new JsonSlurper().parse(data) as Map, caches.versions)
	}
}
