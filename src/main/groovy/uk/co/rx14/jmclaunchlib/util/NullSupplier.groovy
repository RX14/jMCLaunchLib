package uk.co.rx14.jmclaunchlib.util

import groovy.transform.CompileStatic

import java.util.function.Supplier

@CompileStatic
class NullSupplier implements Supplier {

	public static final NullSupplier INSTANCE = new NullSupplier()

	@Override
	def get() {
		return null
	}
}
