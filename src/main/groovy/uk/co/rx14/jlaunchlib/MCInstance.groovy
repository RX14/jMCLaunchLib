package uk.co.rx14.jlaunchlib

import groovy.transform.CompileStatic
import groovy.transform.Immutable
import uk.co.rx14.jlaunchlib.auth.Credentials

import uk.co.rx14.jlaunchlib.caches.MinecraftCaches

import java.nio.file.Path
import java.util.function.Supplier

@CompileStatic
@Immutable(knownImmutableClasses = [Path.class])
class MCInstance {
	Path directory
	MinecraftCaches caches
	MinecraftVersion minecraftVersion
	private Supplier<Credentials> credentialsSupplier
}
