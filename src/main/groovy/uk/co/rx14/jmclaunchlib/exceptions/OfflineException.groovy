package uk.co.rx14.jmclaunchlib.exceptions

class OfflineException extends RuntimeException {
	OfflineException(String message) {
		super("Can't launch in network offline mode: $message")
	}

	OfflineException(String message, Throwable cause) {
		super("Can't launch in network offline mode: $message", cause)
	}

	OfflineException(Throwable cause) {
		super(cause)
	}

	protected OfflineException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super("Can't launch in network offline mode: $message", cause, enableSuppression, writableStackTrace)
	}
}
