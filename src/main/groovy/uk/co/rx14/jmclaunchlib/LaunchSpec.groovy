package uk.co.rx14.jmclaunchlib

import groovy.transform.TupleConstructor
import uk.co.rx14.jmclaunchlib.auth.MinecraftAuthResult

import java.nio.file.Path

@TupleConstructor
class LaunchSpec {
	Path minecraftDirectory
	Path assetsPath

	MinecraftAuthResult auth
	boolean offline

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
		jvmArgs + ["-cp", classpathString, mainClass] + launchArgs
	}

	String getJavaCommandline() {
		javaCommandlineArray.join(" ")
	}

	Process run(Path javaExecutable) {
		"$javaExecutable $javaCommandline".execute(null, getMinecraftDirectory().toFile())
	}
}
