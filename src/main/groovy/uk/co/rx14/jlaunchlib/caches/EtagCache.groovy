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
		def etagFile = getEtagPath(URL).toFile()

		etagFile.exists() ? etagFile.text : null
	}

	String get(URL URL) {
		getPath(URL).toFile().parentFile.mkdirs()

		if (getEtagPath(URL).toFile().exists() && !getPath(URL).toFile().exists())
			getEtagPath(URL).toFile().delete()

		def localEtag = getLocalEtag(URL)
		def request = Unirest.get(URL.toString())

		if (localEtag) {
			request.header("If-None-Match", localEtag)
		}

		def response = request.asString()

		println "$URL: $response.status"

		if (response.status == 304) { //Return from cache
			getPath(URL).text
		} else if (response.status == 200) { //Return from response
			getEtagPath(URL).text = response.headers.getFirst("etag")
			getPath(URL).text = response.body
			response.body
		} else {
			throw new HTTPException(response.status)
		}
	}

	private Path getPath(URL URL) {
		if (URL.file.endsWith("/")) return null
		storage.resolve(URL.path.substring(URL.path.lastIndexOf("/") + 1))
	}

	private Path getEtagPath(URL URL) {
		if (URL.file.endsWith("/")) return null
		new File("${getPath(URL)}.etag").toPath()
	}
}
