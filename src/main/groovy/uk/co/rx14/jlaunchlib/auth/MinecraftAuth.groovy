package uk.co.rx14.jlaunchlib.auth;

public interface MinecraftAuth {
	/**
	 * Uses the given credentials to generate a new
	 * {@link MinecraftAuthResult}.
	 *
	 * @param credentialsProvider The provider of the credentials to
	 *                            authenticate with.
	 * @return the authentication result to use in the minecraft arguments.
	 * @throws uk.co.rx14.jlaunchlib.exceptions.ForbiddenOperationException when the credentials are invalid.
	 * @throws IllegalArgumentException when something is null.
	 */
	MinecraftAuthResult auth(CredentialsProvider credentialsProvider);

	/**
	 * Refreshes a previously-valid {@link MinecraftAuthResult}. If the
	 * refreshing fails, it returns an auth result where valid is set to false.
	 *
	 * @return the {@link MinecraftAuthResult}.
	 * @throws IllegalArgumentException when something is null.
	 */
	MinecraftAuthResult refresh(MinecraftAuthResult result);
}
