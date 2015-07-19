package uk.co.rx14.jmclaunchlib.auth;

/**
 * Due to Minecraft's access token system, the password is not required at every login attempt.
 * When the password is required, it is requested via this interface.
 * This allows launchers to never store passwords, asking the user every time it is required.
 */
public interface PasswordSupplier {
	abstract String getPassword(String username);
}
