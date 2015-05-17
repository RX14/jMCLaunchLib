package uk.co.rx14.jlaunchlib

import groovy.transform.Immutable
import uk.co.rx14.jlaunchlib.caches.EtagCache

@Immutable
class MinecraftVersion {
	String minecraftVersion
	EtagCache versionCache
}
