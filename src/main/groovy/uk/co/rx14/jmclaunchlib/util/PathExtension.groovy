package uk.co.rx14.jmclaunchlib.util

import groovy.transform.CompileStatic

import java.nio.file.Path

@CompileStatic
class PathExtension {
	public static boolean exists(Path self) {
		self.toFile().exists()
	}

	public static boolean isFile(Path self) {
		self.toFile().isFile()
	}

	public static boolean isDirectory(Path self) {
		self.toFile().isDirectory()
	}

	public static boolean parentMkdirs(Path self) {
		self.toFile().parentFile.mkdirs()
	}

	public static boolean mkdirs(Path self) {
		self.toFile().mkdirs()
	}

	public static boolean delete(Path self) {
		self.toFile().delete()
	}
}
