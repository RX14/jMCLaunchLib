package uk.co.rx14.jmclaunchlib.util

import groovy.transform.CompileStatic
import uk.co.rx14.jmclaunchlib.auth.PasswordSupplier

@CompileStatic
class NullPasswordSupplier implements PasswordSupplier {

	public static final NullPasswordSupplier INSTANCE = new NullPasswordSupplier()

	@Override
	String getPassword(String username, boolean retry, String failureMessage) { null }
}
