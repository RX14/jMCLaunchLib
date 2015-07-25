package uk.co.rx14.jmclaunchlib.tasks

import uk.co.rx14.jmclaunchlib.LaunchSpec
import uk.co.rx14.jmclaunchlib.caches.EtagCache
import uk.co.rx14.jmclaunchlib.util.Task
import uk.co.rx14.jmclaunchlib.version.Version

class MinecraftJarTask implements Task {
	final int weight = 5
	final List<Task> subtasks = [].asImmutable()
	final String description = "Download minecraft.jar"

	EtagCache versionCache
	Version version
	LaunchSpec spec

	MinecraftJarTask(EtagCache versionCache, Version version, LaunchSpec spec) {
		this.versionCache = versionCache
		this.version = version
		this.spec = spec
	}

	@Override
	void before() { }

	@Override
	void after() {
		versionCache.get(version.jarDownloadUrl)
		spec.classpath.add(versionCache.getPath(version.jarDownloadUrl).toFile())
	}
}
