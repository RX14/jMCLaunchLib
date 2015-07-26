package uk.co.rx14.jmclaunchlib

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import uk.co.rx14.jmclaunchlib.auth.MinecraftAuthResult

import java.nio.file.Path

@TupleConstructor
@CompileStatic
class LaunchSpec {
	Path minecraftDirectory
	Path assetsPath
	Path nativesDirectory

	MinecraftAuthResult auth
	boolean offline

	boolean netOffline

	List<File> classpath = [].asSynchronized()
	List<String> launchArgs
	List<String> jvmArgs
	String mainClass

	String getClasspathString() {
		def cp = ""

		classpath.each { File file ->
			cp += "$file.absoluteFile$File.pathSeparatorChar"
		}

		cp = cp.substring(0, cp.length() - 1) //Remove last separator

		cp
	}

	String getClasspathArg() {
		"-cp $classpathString"
	}

	String[] getJavaCommandlineArray() {
		[*jvmArgs, "-cp", classpathString, mainClass, *launchArgs].toArray()
	}

	String getJavaCommandline() {
		javaCommandlineArray.join(" ")
	}

	@CompileDynamic
	Process run(Path javaExecutable) {
		[javaExecutable, *javaCommandlineArray].execute(null, getMinecraftDirectory().toFile())
	}
}
