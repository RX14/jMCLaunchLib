package uk.co.rx14.jmclaunchlib.util

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

class NamedThreadFactory implements ThreadFactory {

	private final static Log LOGGER = LogFactory.getLog(NamedThreadFactory)

	private String name
	private int count = 0

	NamedThreadFactory(String name) {
		this.name = name
	}

	@Override
	Thread newThread(Runnable r) {
		def t = Executors.defaultThreadFactory().newThread(r)
		t.daemon = true
		t.name = "$name-thread-${count++}"

		LOGGER.trace "Created thread $t.name"
		t
	}
}
