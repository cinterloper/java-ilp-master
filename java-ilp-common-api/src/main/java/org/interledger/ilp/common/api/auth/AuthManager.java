package org.interledger.ilp.common.api.auth;

import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthHandler;
import io.vertx.ext.web.handler.BasicAuthHandler;
import org.interledger.ilp.common.api.auth.impl.BasicAuthInfoBuilder;
import org.interledger.ilp.common.api.auth.impl.SimpleAuthProvider;
import org.interledger.ilp.common.config.Config;
import org.interledger.ilp.common.config.core.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AuthManager
 *
 * @author mrmx
 */
public class AuthManager {

    public static final String DEFAULT_BASIC_REALM = "ILP Ledger API";

    private static final Logger log = LoggerFactory.getLogger(AuthManager.class);

    private static AuthManager instance;

    enum Provider {
        Basic,
        Jdbc,
        Shiro;
    }

    enum Auth {
        realm
    }

    private final Provider provider;
    private final AuthProvider authProvider;
    private final AuthHandler authHandler;
    private final AuthInfoBuilder authInfoBuilder;

    public AuthManager(Provider provider, AuthProvider authProvider, AuthHandler authHandler, AuthInfoBuilder authInfoBuilder) {
        this.provider = provider;
        this.authProvider = authProvider;
        this.authHandler = authHandler;
        this.authInfoBuilder = authInfoBuilder;
    }

    public AuthProvider getAuthProvider() {
        return authProvider;
    }

    public AuthHandler getAuthHandler() {
        return authHandler;
    }

    public AuthInfoBuilder getAuthInfoBuilder() {
        return authInfoBuilder;
    }

    public AuthInfo getAuthInfo(RoutingContext context) {
        return authInfoBuilder.build(context);
    }

    public static AuthManager getInstance() {
        return instance;
    }

    public static AuthManager getInstance(Config config) {
        Config authConfig = config.getConfig(Auth.class);
        authConfig.debug();
        Provider provider = authConfig.getEnum(Provider.class);
        AuthProvider authProvider = null;
        AuthHandler authHandler = null;
        AuthInfoBuilder authInfoBuilder = null;
        switch (provider) {
            case Basic:
                authProvider = new SimpleAuthProvider();
                String realm = authConfig.getString(DEFAULT_BASIC_REALM, Auth.realm);
                authHandler = BasicAuthHandler.create(authProvider, realm);
                authInfoBuilder = new BasicAuthInfoBuilder();
                break;
            default: // Jdbc, Shiro, break;
                throw new ConfigurationException("authProvider not defined for provider " + provider);
        }
        authConfig.apply(authProvider);
        log.debug("Created {} authProvider using {} impl", provider, authProvider.getClass().getSimpleName());
        return instance = new AuthManager(provider, authProvider, authHandler, authInfoBuilder);
    }

}
