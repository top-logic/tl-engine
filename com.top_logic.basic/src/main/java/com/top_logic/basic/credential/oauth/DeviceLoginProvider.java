/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential.oauth;

import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.RequestAuthentication;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKeyUtil;
import com.top_logic.basic.util.ResourcesModule;

/**
 * {@link CredentialProvider} authenticating requests with a personal access token of a user, who
 * confirms the login once in a browser (OAuth 2.0 device authorization grant, RFC 8628).
 *
 * <p>
 * Meant for development: a developer's application calls an API gateway with a token of the
 * developer instead of holding a key of the external service. The client is typically a public
 * client without client authentication.
 * </p>
 *
 * <p>
 * A token is obtained in this order:
 * </p>
 * <ol>
 * <li>The cached access token, while it is valid.</li>
 * <li>A new access token for the refresh token received last, or the refresh token in the
 * configured store after a restart.</li>
 * <li>A device login: the first request without a usable token starts the login in the background
 * and fails with a message naming the URL to open and the code to confirm. The same instruction is
 * logged as warning. Further requests fail with the same message until the user has confirmed the
 * login; then the next request is authenticated. A denied or expired login is followed by a new one
 * on the next request.</li>
 * </ol>
 *
 * <p>
 * Each token response with a refresh token updates the store, since the authorization server may
 * issue a new refresh token with each use. A refresh token rejected by the server is removed from
 * the store and a new device login starts.
 * </p>
 *
 * <p>
 * The scope <code>offline_access</code> lets the authorization server issue an offline refresh
 * token. A regular refresh token is bound to the user's login session at the server and becomes
 * invalid with it, e.g. after a few hours or when the user logs out in the browser. An offline
 * token stays valid until it is unused for a longer period or revoked, so restarts of the
 * application need no new login for days or weeks.
 * </p>
 *
 * <p>
 * A developer's configuration against a Keycloak realm, keeping the refresh token in the home
 * directory of the developer:
 * </p>
 *
 * <pre>
 * &lt;credentials class="com.top_logic.basic.credential.oauth.DeviceLoginProvider"
 *   issuer="https://login.example.com/realms/my-realm"
 *   client-id="dev-gateway"
 *   scope="openid offline_access"
 * &gt;
 *   &lt;refresh-token-store class="com.top_logic.basic.credential.oauth.FileRefreshTokenStore"/&gt;
 * &lt;/credentials&gt;
 * </pre>
 *
 * <p>
 * The device code, access token and refresh token never appear in a log message or exception
 * message, only the URL to open and the code the user confirms there.
 * </p>
 */
public class DeviceLoginProvider extends AbstractConfiguredInstance<DeviceLoginProvider.Config>
		implements CredentialProvider {

	/**
	 * Configuration options for {@link DeviceLoginProvider}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<DeviceLoginProvider>, OAuthClientConfig {

		/**
		 * Tag name of this configuration in a list of {@link CredentialProvider}s.
		 */
		String TAG_NAME = "device-login";

		/**
		 * @see #getDeviceAuthorizationUrl()
		 */
		String DEVICE_AUTHORIZATION_URL = "device-authorization-url";

		/**
		 * @see #getRefreshTokenStore()
		 */
		String REFRESH_TOKEN_STORE = "refresh-token-store";

		/**
		 * URL of the device authorization endpoint of the authorization server.
		 *
		 * <p>
		 * If empty, the endpoint is read from the discovery document of the issuer.
		 * </p>
		 */
		@Name(DEVICE_AUTHORIZATION_URL)
		String getDeviceAuthorizationUrl();

		/**
		 * @see #getDeviceAuthorizationUrl()
		 */
		void setDeviceAuthorizationUrl(String value);

		/**
		 * Storage of the refresh token, so that a restarted application needs no new login.
		 *
		 * <p>
		 * If not given, the refresh token is kept in memory only.
		 * </p>
		 */
		@Name(REFRESH_TOKEN_STORE)
		PolymorphicConfiguration<? extends RefreshTokenStore> getRefreshTokenStore();

		/**
		 * @see #getRefreshTokenStore()
		 */
		void setRefreshTokenStore(PolymorphicConfiguration<? extends RefreshTokenStore> value);

	}

	/**
	 * Polling interval in milliseconds if the authorization server specifies none (RFC 8628 §3.2).
	 */
	private static final long DEFAULT_INTERVAL = 5000;

	/**
	 * Increase of the polling interval in milliseconds on {@link OAuthConstants#ERROR_SLOW_DOWN}
	 * (RFC 8628 §3.5).
	 */
	private static final long SLOW_DOWN_INCREMENT = 5000;

	private static final String THREAD_NAME_PREFIX = "DeviceLogin-";

	private final OAuthClient _client;

	private final TokenCache _tokens;

	private final RefreshTokenStore _store;

	private final String _server;

	private final String _deviceAuthorizationUrl;

	private final Object _refreshLock = new Object();

	/**
	 * The current refresh token, guarded by {@link #_refreshLock}.
	 */
	private String _refreshToken;

	/**
	 * Whether {@link #_refreshToken} has been read from the {@link #_store}, guarded by
	 * {@link #_refreshLock}.
	 */
	private boolean _loaded;

	/**
	 * The device login started last, <code>null</code> if none is running or its result has been
	 * taken.
	 *
	 * <p>
	 * Only accessed while fetching a token, which the {@link TokenCache} never does concurrently.
	 * </p>
	 */
	private DeviceLogin _login;

	/**
	 * Creates a {@link DeviceLoginProvider} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public DeviceLoginProvider(InstantiationContext context, Config config) {
		super(context, config);
		_client = new OAuthClient(context, config);
		_tokens = _client.newTokenCache();
		_store = context.getInstance(config.getRefreshTokenStore());
		_server = StringServices.isEmpty(config.getIssuer()) ? config.getTokenUrl() : config.getIssuer();
		_deviceAuthorizationUrl = StringServices.nonEmpty(config.getDeviceAuthorizationUrl());
		if (_deviceAuthorizationUrl == null && StringServices.isEmpty(config.getIssuer())) {
			I18NRuntimeException error = new I18NRuntimeException(I18NConstants.ERROR_NO_DEVICE_AUTHORIZATION_ENDPOINT);
			context.error(error.getMessage(), error);
		}
	}

	@Override
	public void authenticate(RequestAuthentication request) {
		AccessToken token = _tokens.getToken(last -> fetchToken());
		request.setHeader(AUTHORIZATION, token.getAuthorization());
	}

	/**
	 * Drops the cached access token. The refresh token is kept, so that the next request obtains a
	 * new access token without a new login.
	 */
	@Override
	public void invalidate() {
		_tokens.invalidate();
	}

	/**
	 * Waits before the next poll of the token endpoint during a device login.
	 *
	 * @param millis
	 *        The polling interval in milliseconds requested by the authorization server.
	 *
	 * @throws InterruptedException
	 *         If the polling thread has been interrupted.
	 */
	protected void waitBeforePoll(long millis) throws InterruptedException {
		Thread.sleep(millis);
	}

	private AccessToken fetchToken() {
		DeviceLogin login = _login;
		if (login != null) {
			switch (login.getState()) {
				case PENDING:
					throw new I18NRuntimeException(login.getInstruction());
				case AUTHORIZED:
					_login = null;
					AccessToken token = login.getToken();
					if (!token.isExpired(System.currentTimeMillis())) {
						return token;
					}
					// Use its refresh token.
					break;
				case FAILED:
					_login = null;
					break;
			}
		}

		String refreshToken = getRefreshToken();
		if (refreshToken != null) {
			try {
				return refresh(refreshToken);
			} catch (OAuthErrorException ex) {
				if (!OAuthConstants.ERROR_INVALID_GRANT.equals(ex.getError())) {
					throw ex;
				}
				log(I18NConstants.REFRESH_TOKEN_REJECTED__CLIENT.fill(_client.getClientId()), null);
				forgetRefreshToken(refreshToken);
			}
		}

		login = startDeviceLogin();
		_login = login;
		throw new I18NRuntimeException(login.getInstruction());
	}

	private AccessToken refresh(String refreshToken) {
		OAuthRequest request = _client.newTokenRequest(OAuthConstants.GRANT_TYPE_REFRESH_TOKEN);
		request.setParameter(OAuthConstants.REFRESH_TOKEN, refreshToken);
		AccessToken token = _client.requestToken(request);
		updateRefreshToken(token.getRefreshToken());
		return token;
	}

	private DeviceLogin startDeviceLogin() {
		String endpoint = getDeviceAuthorizationEndpoint();
		OAuthRequest request = _client.newRequest(endpoint);
		String scope = getConfig().getScope();
		if (!StringServices.isEmpty(scope)) {
			request.setParameter(OAuthConstants.SCOPE, scope);
		}
		Map<String, Object> response = _client.getServer().post(request);

		String deviceCode = requireString(endpoint, response, OAuthConstants.DEVICE_CODE);
		String userCode = requireString(endpoint, response, OAuthConstants.USER_CODE);
		Object completeUri = response.get(OAuthConstants.VERIFICATION_URI_COMPLETE);
		String uri = completeUri instanceof String && !((String) completeUri).isEmpty() ? (String) completeUri
			: requireString(endpoint, response, OAuthConstants.VERIFICATION_URI);
		Long expiresIn = OAuthServer.parseSeconds(endpoint, response.get(OAuthConstants.EXPIRES_IN));
		if (expiresIn == null) {
			throw malformed(endpoint);
		}
		Long interval = OAuthServer.parseSeconds(endpoint, response.get(OAuthConstants.INTERVAL));

		ResKey instruction =
			I18NConstants.ERROR_DEVICE_LOGIN_REQUIRED__URL_CODE_CLIENT.fill(uri, userCode, _client.getClientId());
		DeviceLogin login = new DeviceLogin(deviceCode, instruction,
			System.currentTimeMillis() + expiresIn.longValue() * 1000,
			interval == null ? DEFAULT_INTERVAL : interval.longValue() * 1000);
		Thread thread = new Thread(login, THREAD_NAME_PREFIX + _client.getClientId());
		thread.setDaemon(true);
		thread.start();
		log(instruction, null);
		return login;
	}

	private String getDeviceAuthorizationEndpoint() {
		if (_deviceAuthorizationUrl != null) {
			return _deviceAuthorizationUrl;
		}
		return _client.getServer().getMetadataEndpoint(OAuthConstants.DEVICE_AUTHORIZATION_ENDPOINT);
	}

	private static String requireString(String endpoint, Map<String, Object> response, String field) {
		Object value = response.get(field);
		if (!(value instanceof String) || ((String) value).isEmpty()) {
			throw malformed(endpoint);
		}
		return (String) value;
	}

	private static I18NRuntimeException malformed(String endpoint) {
		return new I18NRuntimeException(I18NConstants.ERROR_MALFORMED_RESPONSE__URL.fill(endpoint));
	}

	private String getRefreshToken() {
		synchronized (_refreshLock) {
			if (!_loaded) {
				_loaded = true;
				if (_store != null) {
					try {
						_refreshToken = _store.load(_server, _client.getClientId());
					} catch (I18NRuntimeException ex) {
						Logger.warn(ex.getMessage(), ex, DeviceLoginProvider.class);
					}
				}
			}
			return _refreshToken;
		}
	}

	/**
	 * Keeps and stores the refresh token of a token response.
	 *
	 * @param refreshToken
	 *        The refresh token of the response, <code>null</code> if the response contains none, in
	 *        which case the refresh token received before stays valid.
	 */
	private void updateRefreshToken(String refreshToken) {
		if (refreshToken == null) {
			return;
		}
		synchronized (_refreshLock) {
			_loaded = true;
			if (refreshToken.equals(_refreshToken)) {
				return;
			}
			_refreshToken = refreshToken;
			if (_store != null) {
				try {
					_store.store(_server, _client.getClientId(), refreshToken);
				} catch (I18NRuntimeException ex) {
					Logger.warn(ex.getMessage(), ex, DeviceLoginProvider.class);
				}
			}
		}
	}

	private void forgetRefreshToken(String refreshToken) {
		synchronized (_refreshLock) {
			if (!refreshToken.equals(_refreshToken)) {
				// Replaced in the meantime.
				return;
			}
			_refreshToken = null;
			if (_store != null) {
				try {
					_store.clear(_server, _client.getClientId());
				} catch (I18NRuntimeException ex) {
					Logger.warn(ex.getMessage(), ex, DeviceLoginProvider.class);
				}
			}
		}
	}

	private static void log(ResKey message, Throwable cause) {
		Logger.warn(ResKeyUtil.getTranslation(message, ResourcesModule.getLogLocale()), cause,
			DeviceLoginProvider.class);
	}

	private enum State {
		PENDING, AUTHORIZED, FAILED;
	}

	/**
	 * A device login in progress: polls the token endpoint until the user has confirmed or denied
	 * the login, or the device code has expired.
	 */
	private final class DeviceLogin implements Runnable {

		private final String _deviceCode;

		private final ResKey _instruction;

		private final long _deadline;

		private long _interval;

		private volatile AccessToken _token;

		private volatile State _state = State.PENDING;

		DeviceLogin(String deviceCode, ResKey instruction, long deadline, long interval) {
			_deviceCode = deviceCode;
			_instruction = instruction;
			_deadline = deadline;
			_interval = interval;
		}

		State getState() {
			return _state;
		}

		ResKey getInstruction() {
			return _instruction;
		}

		AccessToken getToken() {
			return _token;
		}

		@Override
		public void run() {
			String clientId = _client.getClientId();
			try {
				while (true) {
					waitBeforePoll(_interval);
					if (System.currentTimeMillis() >= _deadline) {
						fail(I18NConstants.DEVICE_LOGIN_EXPIRED__CLIENT.fill(clientId), null);
						return;
					}
					try {
						OAuthRequest request = _client.newTokenRequest(OAuthConstants.GRANT_TYPE_DEVICE_CODE);
						request.setParameter(OAuthConstants.DEVICE_CODE, _deviceCode);
						AccessToken token = _client.requestToken(request);
						updateRefreshToken(token.getRefreshToken());
						_token = token;
						_state = State.AUTHORIZED;
						return;
					} catch (OAuthErrorException ex) {
						String error = ex.getError();
						if (OAuthConstants.ERROR_AUTHORIZATION_PENDING.equals(error)) {
							continue;
						}
						if (OAuthConstants.ERROR_SLOW_DOWN.equals(error)) {
							_interval += SLOW_DOWN_INCREMENT;
							continue;
						}
						if (OAuthConstants.ERROR_ACCESS_DENIED.equals(error)) {
							fail(I18NConstants.DEVICE_LOGIN_DENIED__CLIENT.fill(clientId), null);
						} else if (OAuthConstants.ERROR_EXPIRED_TOKEN.equals(error)) {
							fail(I18NConstants.DEVICE_LOGIN_EXPIRED__CLIENT.fill(clientId), null);
						} else {
							fail(I18NConstants.DEVICE_LOGIN_FAILED__CLIENT.fill(clientId), ex);
						}
						return;
					}
				}
			} catch (InterruptedException ex) {
				fail(I18NConstants.DEVICE_LOGIN_FAILED__CLIENT.fill(clientId), ex);
			} catch (RuntimeException ex) {
				fail(I18NConstants.DEVICE_LOGIN_FAILED__CLIENT.fill(clientId), ex);
			}
		}

		private void fail(ResKey message, Throwable cause) {
			_state = State.FAILED;
			log(message, cause);
		}

	}

}
