package uk.co.rx14.jmclaunchlib.util

import java.util.function.Supplier

class ChangePrinter {
	final Supplier<String> valueSupplier
	final int pollingMilliseconds
	private final Thread thread

	ChangePrinter(Supplier<String> valueSupplier, int pollingTime) {
		this.valueSupplier = valueSupplier
		this.pollingMilliseconds = pollingTime

		thread = new Thread({
			def value
			while (true) {
				def newValue = valueSupplier.get()
				if (value != newValue) {
					println value
					value = newValue
					Thread.sleep(pollingTime)
				}
			}
		})

		thread.daemon = true
	}

	ChangePrinter(Supplier<String> valueSupplier) {
		this(valueSupplier, 100)
	}

	void start() {
		thread.start()
	}
}
