package uk.co.rx14.jmclaunchlib.version;

import java.net.URL;
import java.util.List;

public interface Version {
	String getMCVersion();

	String getUniqueVersion();

	List getLibs();

	String getAssetsVersion();

	String getMinecraftArguments();

	String getMainClass();

	URL getJarDownloadUrl();
}
