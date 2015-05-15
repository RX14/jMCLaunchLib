package uk.co.rx14.jlaunchlib;

import java.nio.file.Path;

class MCInstance {
	private final String directory
	private MinecraftVersion minecraftVersion

	private Path assetCache
	private Path libraryCache
	private Path nativesDirectory

	public MCInstance(String directory) {
		this.directory = directory
	}

	/*
	 * Builder-style chainable methods
	 */

	//---- minecraftVersion ----
	MCInstance setMinecraftVersion(MinecraftVersion version) {
		this.minecraftVersion = version
		this
	}

	MCInstance setMinecraftVersion(String minecraftVersion) {
		this.minecraftVersion = new MinecraftVersion(minecraftVersion)
		this
	}

	//---- assetCache ----
	MCInstance setAssetsPath(Path path) {
		this.assetCache = path
		this
	}

	MCInstance setAssetsPath(File path) {
		this.assetCache = path.toPath()
		this
	}

	MCInstance setAssetsPath(String path) {
		this.assetCache = new File(path).toPath()
		this
	}

	//---- libraryCache ----
	MCInstance setLibrariesPath(Path path) {
		this.libraryCache = path
		this
	}

	MCInstance setLibrariesPath(File path) {
		this.libraryCache = path.toPath()
		this
	}

	MCInstance setLibrariesPath(String path) {
		this.libraryCache = new File(path).toPath()
		this
	}

	//---- nativesDirectory ----
	MCInstance setNativesPath(Path path) {
		this.nativesDirectory = path
		this
	}

	MCInstance setNativesPath(File path) {
		this.nativesDirectory = path.toPath()
		this
	}

	MCInstance setNativesPath(String path) {
		this.nativesDirectory = new File(path).toPath()
		this
	}
}
