package uk.co.rx14.jlaunchlib

import groovy.transform.CompileStatic

@CompileStatic
class Constants {
	public static final String MinecraftDownload = "https://s3.amazonaws.com/Minecraft.Download"
	public static final String MinecraftVersionsBase = "$MinecraftDownload/versions"
	public static final String MinecraftIndexesBase = "$MinecraftDownload/indexes"
	public static final String MinecraftAssetsBase = "http://resources.download.minecraft.net"
	public static final String MinecraftLibsBase = "https://libraries.minecraft.net/"
}
