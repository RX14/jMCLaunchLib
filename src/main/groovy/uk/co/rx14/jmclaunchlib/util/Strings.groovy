package uk.co.rx14.jmclaunchlib.util

import groovy.transform.CompileStatic

@CompileStatic
class Strings {
	static boolean isEmpty(String str) {
		str == null || str.length() == 0
	}

	static boolean isNotEmpty(String str) {
		!isEmpty(str)
	}
}
