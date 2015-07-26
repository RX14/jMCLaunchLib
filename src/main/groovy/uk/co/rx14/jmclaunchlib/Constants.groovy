package uk.co.rx14.jmclaunchlib

import groovy.transform.CompileStatic
import uk.co.rx14.jmclaunchlib.util.NamedThreadFactory

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@CompileStatic
class Constants {
	public static final String MinecraftDownload = "https://s3.amazonaws.com/Minecraft.Download"
	public static final String MinecraftVersionsBase = "$MinecraftDownload/versions"
	public static final String MinecraftIndexesBase = "$MinecraftDownload/indexes"
	public static final String MinecraftAssetsBase = "http://resources.download.minecraft.net"
	public static final String MinecraftLibsBase = "https://libraries.minecraft.net/"

	public static final String[] XZLibs = [/org\.scala-lang.*/, /com\.typesafe.*/]

	public static final ExecutorService executor = Executors.newFixedThreadPool(10, new NamedThreadFactory("jMCLaunchLib taskpool"))
}
