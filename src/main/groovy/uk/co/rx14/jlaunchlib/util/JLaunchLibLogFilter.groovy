package uk.co.rx14.jlaunchlib.util

import groovy.transform.CompileStatic

import java.util.logging.Filter
import java.util.logging.LogRecord

@CompileStatic
class JLaunchLibLogFilter implements Filter {
	@Override
	boolean isLoggable(LogRecord record) {
		!(record.loggerName == "org.jboss.shrinkwrap.resolver.impl.maven.logging.LogTransferListener" && !record.message.contains("Downloading:"))
	}
}
