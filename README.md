jMCLaunchLib
============
A library to make minecraft launcher development with JVM languages easier by providing a library to do the hard work: actually launching minecraft.

Although this library is written in groovy, java support **IS** a priority for me, some java-based launchers are already using it.

If you require help and support see me in the #RX14 channel on EsperNet.
**Do not hesitate to report bugs and feature requests in the github issues.**
Even if you decide not to use this library, please drop a note as to why so I can improve, for the good of the community.

How do I get it?
----------------
The library is available on the jcenter maven repository.

Using Gradle:
```groovy
repositories {
    jcenter()
}

dependencies {
    compile "uk.co.rx14.jmclaunchlib:jMCLaunchLib:$version"
}
```

Usage
-----
First step in launching Minecraft is to get a `MCInstance`.
This class holds information like Minecraft version, where to cache data, where the minecraft directory is etc.
To obtain a `MCInstance` you call the `create` or `createForge` static methods on `MCInstance`.

```java
MCInstance instance = MCInstance.createForge(
	"1.7.10", //Minecraft Version
	"1.7.10-10.13.3.1408-1.7.10", //Forge version
	"test/caches", //Caches directory
	"test/instance/1.7.10-forge", //Minecraft directory
	new YourPasswordSupplier() //Where to request passwords. Details below.
);
```

This should be mostly self-explanitory apart from the requirement for a `PasswordSupplier`.
`PasswordSupplier` is an interface with one method, `String getPassword(String username)`.
This is required because my library stores authentication tokens but does not store passwords,
using this interface as a callback to request the password for a specific username enables the launcher to only ask the user for a password when absolutely needed.

Now you have your `MCInstance`, what can you do with it?
The next step to launching Minecraft is getting the task which you will start to launch the game,
the reason you get a Task object THEN launch it is that it allows you to monitor the launching progress from another thread and provide feedback to the user.

```java
LaunchTask task = instance.getLaunchTask("user@email.address"); //Or username if not Mojang account
//OR
LaunchTask task = instance.getOfflineLaunchTask("username");

//Open a dialog box to report the percentage with
task.getCompletedPercentage()

task.start()

LaunchSpec spec = task.getSpec()
```

You should reuse the MCInstance object as much as possible, it can be expensive to create (also never make it on a GUI thread).
You cannot, however, reuse Task objects.

LaunchSpec is a class which contains the information you need to launch minecraft: classpath, launchargs, jvmArgs and mainclass.
You can use `getLaunchArgs().add(...)` and `getJvmArgs().add(...)` to customise the launch parameters.
Use `LaunchSpec.getJavaCommandline()` to get the arguments to pass to the java executable as a String,
or use `LaunchSpec.run(java.nio.file.Path javaExecutable)` to let jMCLaunchLib run Minecraft.
The `run()` method returns a standard java Process object so you can control minecraft and route it's standard output streams.
If you pester me enough I might write something cool to redirect stdout/err from a Process to useful places.

Customisation
-------------
Constructing a custom `MCInstance` object is possibe, and it allows you to provide your own version JSON file with the `MinecraftVersion` class.
It also allows you to customise the layout of the caches directory.


