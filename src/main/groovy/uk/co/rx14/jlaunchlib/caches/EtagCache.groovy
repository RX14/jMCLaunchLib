package uk.co.rx14.jlaunchlib.caches

import com.mashape.unirest.http.Unirest
import groovy.transform.CompileStatic
import groovy.transform.Immutable

import javax.xml.ws.http.HTTPException
import java.nio.file.Path

@CompileStatic
@Immutable(knownImmutableClasses = [Path.class])
class EtagCache extends Cache {
	Path storage

	String getLocalEtag(URL URL) {
		def etagFile = getFilenames(URL)[1].toFile()

		etagFile.exists() ? etagFile.text : null
	}

	String get(URL URL) {
		def localEtag = getLocalEtag(URL)
		def request = Unirest.get(URL.toString())

		if (localEtag) {
			request.header("If-None-Match", localEtag)
		}

		def response = request.asString()

		println "$URL: $response.status"

		if (response.status == 304) { //Return from cache
			getFilenames(URL)[0].text
		} else if (response.status == 200) { //Return from response
			response.body
		} else {
			throw new HTTPException(response.status)
		}
	}

	private List<Path> getFilenames(URL URL) {
		if (URL.file.endsWith("/")) return null

		def file = storage.resolve(URL.path.substring(URL.path.lastIndexOf("/") + 1))
		[storage.resolve(file), storage.resolve("${file}.etag")]
	}
}
