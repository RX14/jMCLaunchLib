package uk.co.rx14.jlaunchlib.caches

import groovy.transform.CompileStatic
import groovy.transform.Immutable

import java.nio.file.Path

@CompileStatic
@Immutable(knownImmutableClasses = [Path.class])
class EtagCache {
	Path storage

	String download(URL URL) {
		def file = storage.resolve(URL.path.substring(URL.path.lastIndexOf("/")) + 1)
	}
}
