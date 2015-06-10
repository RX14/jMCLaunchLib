package uk.co.rx14.jmclaunchlib.caches

import com.mashape.unirest.http.Unirest
import groovy.transform.Immutable
import groovy.transform.ToString

import javax.xml.ws.http.HTTPException
import java.nio.file.Path
import java.util.logging.Logger

@ToString(includePackage = false, includeNames = true)
@Immutable(knownImmutableClasses = [Path.class])
class EtagCache extends Cache {

	private final static Logger LOGGER = Logger.getLogger(EtagCache.class.name)

	Path storage

	String getLocalEtag(URL URL) {
		def path = getEtagPath(URL)
		path.exists() ? path.text : null
	}

	byte[] get(URL URL) {
		LOGGER.fine "Getting $URL"

		def filePath = getPath(URL)
		def etagPath = getEtagPath(URL)

		filePath.parentMkdirs()

		if (etagPath.exists() && !filePath.exists()) {
			LOGGER.info "Etag file $etagPath exists but file $filePath does not: Deleting etag file."
			etagPath.delete()
		}

		def localEtag = getLocalEtag(URL)
		def request = Unirest.get(URL.toString())

		if (localEtag) {
			LOGGER.fine "Etag exists, adding header If-None-Match: $localEtag"
			request.header("If-None-Match", localEtag)
		}

		def startTime = System.nanoTime()
		def response = request.asBinary()
		def time = System.nanoTime() - startTime

		if (response.status == 304) { //Return from cache
			LOGGER.info "$URL returned 304 in ${time / 1000000000}s: using cache"
			filePath.bytes
		} else if (response.status == 200) { //Return from response
			LOGGER.info "$URL returned 200 in ${time / 1000000000}s: caching"
			LOGGER.fine "Etag was ${response.headers.getFirst("etag")}"
			etagPath.text = response.headers.getFirst("etag")
			filePath.bytes = response.body.bytes
			filePath.bytes
		} else {
			LOGGER.warning "$URL returned $response.status in ${time / 1000000000}s: error"
			throw new HTTPException(response.status)
		}
	}

	Path getPath(URL URL) {
		if (URL.file.endsWith("/")) return null
		storage.resolve(URL.path.substring(URL.path.lastIndexOf("/") + 1))
	}

	Path getEtagPath(URL URL) {
		if (URL.file.endsWith("/")) return null
		new File("${getPath(URL)}.etag").toPath()
	}
}
