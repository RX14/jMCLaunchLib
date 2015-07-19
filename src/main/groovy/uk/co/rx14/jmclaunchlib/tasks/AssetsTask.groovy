package uk.co.rx14.jmclaunchlib.tasks

import groovy.json.JsonSlurper
import uk.co.rx14.jmclaunchlib.Constants
import uk.co.rx14.jmclaunchlib.LaunchSpec
import uk.co.rx14.jmclaunchlib.MinecraftVersion
import uk.co.rx14.jmclaunchlib.caches.AssetsCache
import uk.co.rx14.jmclaunchlib.caches.HashCache
import uk.co.rx14.jmclaunchlib.util.Task

import java.util.concurrent.CopyOnWriteArrayList

class AssetsTask implements Task {
	final int weight = 5
	private final List<Task> _subtasks = new CopyOnWriteArrayList<>()

	MinecraftVersion minecraftVersion
	AssetsCache cache
	LaunchSpec spec

	AssetsTask(MinecraftVersion minecraftVersion, AssetsCache cache, LaunchSpec spec) {
		this.minecraftVersion = minecraftVersion
		this.cache = cache
		this.spec = spec
	}

	@Override
	void before() {
		if (minecraftVersion.assets) {
			def index = new JsonSlurper().parseText(
				new String(cache.indexes.get("$Constants.MinecraftIndexesBase/${minecraftVersion.assets}.json".toURL()))
			)

			index.objects.each {
				String hash = it.value.hash
				def URL = "$Constants.MinecraftAssetsBase/${hash.substring(0, 2)}/$hash".toURL()
				if (!cache.objects.has(hash)) {
					_subtasks << new AssetDownloadTask(hash, URL)
				}
			}

			spec.assetsPath = cache.storage
		} else {
			throw new UnsupportedOperationException("Legacy assets not supported... for now :3")
		}
	}

	@Override
	void after() {
		Thread.start {
			cache.objects.verify(HashCache.VerificationAction.DELETE)
		}
	}

	class AssetDownloadTask implements Task {
		final int weight = 5
		final List<Task> subtasks = [].asImmutable()

		String hash
		URL URL

		AssetDownloadTask(String hash, URL URL) {
			this.hash = hash
			this.URL = URL
		}

		@Override
		void before() { }

		@Override
		void after() {
			cache.objects.preDownload(hash, URL)
		}
	}

	List<Task> getSubtasks() { _subtasks.asImmutable() }
}
