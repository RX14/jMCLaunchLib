package uk.co.rx14.jlaunchlib.util

import groovy.transform.CompileStatic

import java.nio.file.Path
import java.util.logging.Logger
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

@CompileStatic
class Zip {

	private final static Logger LOGGER = Logger.getLogger(Zip.class.name)

	public static void extractWithExclude(File zipFile, Path outPath, List<String> exclusions) {
		def startTime = System.nanoTime()

		def zip = new ZipFile(zipFile)
		try {
			outer:
			for (entry in (Enumeration<ZipEntry>) zip.entries()) {
				for (exclusion in exclusions) {
					if (entry.name.startsWith(exclusion))
						continue outer
				}
				def entryFile = outPath.resolve(entry.name).toFile()
				if (entry.isDirectory()) {
					entryFile.mkdirs()
				} else {
					entryFile.parentFile.mkdirs()

					def stream = zip.getInputStream(entry)
					entryFile.bytes = stream.getBytes()
				}
			}
		} finally {
			zip.close()
		}

		def time = System.nanoTime() - startTime

		LOGGER.finer "Extracted $zipFile to $outPath in ${time / 1000000000}s"
		LOGGER.finer "Exclusions: $exclusions"
	}
}
