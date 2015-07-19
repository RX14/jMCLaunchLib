package uk.co.rx14.jmclaunchlib.tasks

import uk.co.rx14.jmclaunchlib.LaunchSpec
import uk.co.rx14.jmclaunchlib.MinecraftVersion
import uk.co.rx14.jmclaunchlib.caches.EtagCache
import uk.co.rx14.jmclaunchlib.util.Task

class MinecraftJarTask implements Task {
	final int weight = 5
	final List<Task> subtasks = [].asImmutable()

	EtagCache versionCache
	MinecraftVersion minecraftVersion
	LaunchSpec spec

	MinecraftJarTask(EtagCache versionCache, MinecraftVersion minecraftVersion, LaunchSpec spec) {
		this.versionCache = versionCache
		this.minecraftVersion = minecraftVersion
		this.spec = spec
	}

	@Override
	void before() { }

	@Override
	void after() {
		versionCache.get(minecraftVersion.jarDownloadUrl)
		spec.classpath.add(versionCache.getPath(minecraftVersion.jarDownloadUrl).toFile())
	}
}
