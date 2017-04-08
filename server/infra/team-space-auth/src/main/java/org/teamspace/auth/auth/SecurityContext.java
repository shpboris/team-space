package org.teamspace.auth.auth;


import org.teamspace.auth.domain.AccessToken;

/**
 * Created by shpilb on 18/03/2017.
 */
public class SecurityContext {
    private static final ThreadLocal<AccessToken> context = new ThreadLocal<AccessToken>();
    public static AccessToken getContext() {
        return context.get();
    }
    public static void setContext(AccessToken accessToken) {
        context.set(accessToken);
    }
}
