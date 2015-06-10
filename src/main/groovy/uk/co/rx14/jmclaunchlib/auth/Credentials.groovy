package uk.co.rx14.jmclaunchlib.auth

import groovy.transform.Immutable

/**
 * Represents a username and password used for authentication purposes.
 */
@Immutable class Credentials {
	String username, password
}
