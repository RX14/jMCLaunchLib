package uk.co.rx14.jmclaunchlib.util

import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

class NamedThreadFactory implements ThreadFactory {

	private String name
	private int count = 0

	NamedThreadFactory(String name) {
		this.name = name
	}

	@Override
	Thread newThread(Runnable r) {
		def t = Executors.defaultThreadFactory().newThread(r)
		t.setName("$name-thread-${count++}")
		t
	}
}
