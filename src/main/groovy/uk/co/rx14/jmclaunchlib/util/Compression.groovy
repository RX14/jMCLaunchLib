package uk.co.rx14.jmclaunchlib.util

import groovy.transform.CompileStatic
import org.apache.commons.io.IOUtils
import org.apache.commons.io.input.BoundedInputStream
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.tukaani.xz.XZInputStream

import java.nio.file.Path
import java.security.SignatureException
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.jar.Pack200
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

@CompileStatic
class Compression {

	private final static Log LOGGER = LogFactory.getLog(Compression)

	static void extractZipWithExclude(File zipFile, Path outPath, List<String> exclusions) {
		LOGGER.trace "Extracting $zipFile to $outPath"
		def startTime = System.nanoTime()

		def zip = new ZipFile(zipFile)
		try {
			zip.entries().each { ZipEntry entry ->
				if (exclusions.any { entry.name.startsWith(it) }) return

				def entryFile = outPath.resolve(entry.name).toFile()
				if (entry.isDirectory()) {
					entryFile.mkdirs()
				} else {
					entryFile.parentFile.mkdirs()

					entryFile.bytes = zip.getInputStream(entry).bytes
				}
			}
		} finally {
			zip.close()
		}

		def time = System.nanoTime() - startTime

		LOGGER.debug "Extracted $zipFile to $outPath in ${time / 1000000000}s"
		LOGGER.trace "Exclusions: $exclusions"
	}

	static byte[] extractZipSingleFile(File zipFile, String filePath) {
		def startTime = System.nanoTime()

		def zip = new ZipFile(zipFile)
		try {
			for (ZipEntry entry in (List<ZipEntry>) zip.entries().toList()) {
				if (entry.name == filePath && !entry.isDirectory()) {
					def time = System.nanoTime() - startTime
					LOGGER.debug "Extracted $filePath from $zipFile in ${time / 1000000000}s"
					return zip.getInputStream(entry).bytes
				}
			}
		} finally {
			zip.close()
		}


		def e = new FileNotFoundException("Could not find $filePath in $zipFile")
		LOGGER.debug "", e
		throw e
	}

	static void extractPackXz(File pack, File jar) {
		LOGGER.debug "Extracting pack $pack"
		def startTime = System.nanoTime()

		def packFile = new File(pack.path.replaceFirst('\\.xz$', ""))

		def packFileStream = packFile.newOutputStream()
		try {
			LOGGER.trace "Unpacking xz..."
			IOUtils.copy(new XZInputStream(pack.newInputStream()), packFileStream)
		} finally {
			packFileStream.close()
		}

		def raf = new RandomAccessFile(packFile, "r")
		int length = packFile.length() as int

		byte[] sum = new byte[8]

		raf.seek(length - 8)
		raf.readFully(sum)

		if (!new String(sum, 4, 4).equals("SIGN")) {
			def e = new SignatureException("Unpacking $pack failed: Signature missing.")
			LOGGER.warn "", e
			throw e
		}

		def len =
			((sum[0] & 0xFF)) |
			((sum[1] & 0xFF) << 8) |
			((sum[2] & 0xFF) << 16) |
			((sum[3] & 0xFF) << 24);

		def checksums = new byte[len]
		raf.seek(length - len - 8)
		raf.readFully(checksums)
		raf.close()

		def packStream = new BoundedInputStream(packFile.newInputStream(), length - len - 8)
		def jarStream = new JarOutputStream(new FileOutputStream(jar))

		LOGGER.trace "Unpacking pack200..."
		Pack200.newUnpacker().unpack(packStream, jarStream)

		JarEntry checksumsFile = new JarEntry("checksums.sha1");
		checksumsFile.setTime(0);
		jarStream.putNextEntry(checksumsFile);
		jarStream.write(checksums);
		jarStream.closeEntry();

		jarStream.close();
		packStream.close();

		pack.delete()
		packFile.delete()

		def time = System.nanoTime() - startTime
		LOGGER.debug "Extracted $pack to $jar in ${time / 1000000000}s"
	}
}
