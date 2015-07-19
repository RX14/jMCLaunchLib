package uk.co.rx14.jmclaunchlib.util

import groovy.transform.CompileStatic
import org.apache.commons.io.IOUtils
import org.apache.commons.io.input.BoundedInputStream
import org.tukaani.xz.XZInputStream

import java.nio.file.Path
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.jar.Pack200
import java.util.logging.Logger
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

@CompileStatic
class Compression {

	private final static Logger LOGGER = Logger.getLogger(Compression.class.name)

	static void extractZipWithExclude(File zipFile, Path outPath, List<String> exclusions) {
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

	static byte[] extractZipSingleFile(File zipFile, String filePath) {
		def startTime = System.nanoTime()

		def zip = new ZipFile(zipFile)
		try {
			for (entry in (Enumeration<ZipEntry>) zip.entries()) {
				if (entry.name == filePath && !entry.isDirectory()) {
					def time = System.nanoTime() - startTime
					LOGGER.finer "Extracted $filePath from $zipFile in ${time / 1000000000}s"
					return zip.getInputStream(entry).bytes
				}
			}
		} finally {
			zip.close()
		}
		throw new FileNotFoundException("Could not find $filePath in $zipFile")
	}

	static void extractPackXz(File pack, File jar) {
		LOGGER.fine "Extracting pack $pack"

		def packFile = new File(pack.path.replaceFirst('\\.xz$', ""))

		def packFileStream = packFile.newOutputStream()
		try {
			LOGGER.fine "Unpacking xz..."
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
			LOGGER.severe "Unpacking $pack failed: Signature missing."
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

		LOGGER.fine "Unpacking pack200..."
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
	}
}
