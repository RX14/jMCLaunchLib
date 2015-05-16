package uk.co.rx14.jlaunchlib.auth;

/**
 * Interface used to delay asking for {@link Credentials} until we know that
 * it is required.
 */
public interface CredentialsProvider {
    /**
     * Ask for credentials.
     *
     * @return the {@link Credentials}
     */
    Credentials ask();
}
