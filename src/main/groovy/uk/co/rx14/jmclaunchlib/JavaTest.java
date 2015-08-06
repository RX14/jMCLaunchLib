package uk.co.rx14.jmclaunchlib;

import uk.co.rx14.jmclaunchlib.util.ChangePrinter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Supplier;

public class JavaTest {
	public static void main(String[] args) {
		final LaunchTask task = new LaunchTaskBuilder()
			.setCachesDir("test/caches")
			.setMinecraftVersion("1.7.10")
//			.setForgeVersion()
			.setInstanceDir("test/instance")
			.setUsername("RX14")
			.setOffline()
			.build();

		new ChangePrinter(
			new Supplier<String>() {
				public String get() {return "" + task.getCompletedPercentage();}
			}
			, 100
		).start();

		LaunchSpec spec = task.getSpec();

		Process run = spec.run(new File("/usr/bin/java").toPath());

		BufferedReader stdout = new BufferedReader(new InputStreamReader(run.getInputStream()));
		String line = null;
		try {
			while ((line = stdout.readLine()) != null) {
				System.out.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
