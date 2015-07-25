package uk.co.rx14.jmclaunchlib.tasks

import groovy.json.JsonSlurper
import uk.co.rx14.jmclaunchlib.Constants
import uk.co.rx14.jmclaunchlib.LaunchSpec
import uk.co.rx14.jmclaunchlib.caches.AssetsCache
import uk.co.rx14.jmclaunchlib.caches.HashCache
import uk.co.rx14.jmclaunchlib.util.Task
import uk.co.rx14.jmclaunchlib.version.Version

import java.util.concurrent.CopyOnWriteArrayList

class AssetsTask implements Task {
	final int weight = 5
	private final List<Task> _subtasks = new CopyOnWriteArrayList<>()
	final String description = "Download assets listing"

	Version version
	AssetsCache cache
	LaunchSpec spec

	AssetsTask(Version version, AssetsCache cache, LaunchSpec spec) {
		this.version = version
		this.cache = cache
		this.spec = spec
	}

	@Override
	void before() {
		if (version.assetsVersion) {
			def index = new JsonSlurper().parseText(
				new String(cache.indexes.get("$Constants.MinecraftIndexesBase/${version.assetsVersion}.json".toURL()))
			)

			index.objects.each {
				if (!cache.objects.has(it.value.hash)) {
					_subtasks << new AssetDownloadTask(it)
				}
			}

			spec.assetsPath = cache.storage
		} else {
			throw new UnsupportedOperationException("Legacy assets not supported... for now :3")
		}
	}

	@Override
	void after() {
		//TODO remove this and make it a verify method
		Thread.start {
			cache.objects.verify(HashCache.VerificationAction.DELETE)
		}
	}

	class AssetDownloadTask implements Task {
		final int weight = 5
		final List<Task> subtasks = [].asImmutable()
		final String description

		def asset

		AssetDownloadTask(asset) {
			this.asset = asset
			this.description = "Download $asset.key"
		}

		@Override
		void before() { }

		@Override
		void after() {
			String hash = asset.value.hash
			def URL = "$Constants.MinecraftAssetsBase/${hash.substring(0, 2)}/$hash".toURL()
			cache.objects.preDownload(hash, URL)
		}
	}

	List<Task> getSubtasks() { _subtasks.asImmutable() }
}
