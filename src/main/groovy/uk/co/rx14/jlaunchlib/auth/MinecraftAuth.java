package uk.co.rx14.jlaunchlib.auth;

public interface MinecraftAuth {
	/**
	 * Authenticates to provide a
	 *
	 * @return
	 * @throws AuthenticationFailedException when the API
	 */
	MinecraftAuthResult auth() throws AuthenticationFailedException;

	/**
	 * Refreshes a previously-valid {@link MinecraftAuthResult}. If the
	 * refreshing fails, it returns an auth result where valid is set to false.
	 *
	 * @return the {@link MinecraftAuthResult}.
	 */
	MinecraftAuthResult refresh(MinecraftAuthResult result);
}
