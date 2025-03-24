/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.api;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.func.misc.AlwaysFalse;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.service.openapi.common.authentication.SecretConfiguration;
import com.top_logic.service.openapi.common.authentication.apikey.APIKeyPosition;
import com.top_logic.service.openapi.common.document.SecuritySchemeObject;
import com.top_logic.service.openapi.common.document.SecuritySchemeType;
import com.top_logic.service.openapi.common.util.OpenAPIConfigs;
import com.top_logic.service.openapi.server.authentication.AuthenticationFailure;
import com.top_logic.service.openapi.server.authentication.Authenticator;
import com.top_logic.service.openapi.server.authentication.conf.ServerAuthentication;
import com.top_logic.service.openapi.server.authentication.http.basic.I18NConstants;
import com.top_logic.util.TLContext;

/**
 * Demo for a custom authentication mechanism for an <i>OpenAPI</i> interface.
 */
public class CustomAuthentication extends AbstractConfiguredInstance<CustomAuthentication.Config<?>>
		implements ServerAuthentication<CustomAuthentication.Config<?>> {

	/**
	 * Configuration options for {@link CustomAuthentication}.
	 */
	public interface Config<I extends CustomAuthentication> extends ServerAuthentication.Config<I> {
		/**
		 * The header that contains the secret.
		 */
		@Mandatory
		String getHeader();

		/**
		 * A function looking up a user for a given secret.
		 */
		@Mandatory
		Expr getLookupUser();

		@Override
		@Derived(fun = AlwaysFalse.class, args = {})
		boolean isSeparateSecretNeeded();
	}

	private QueryExecutor _lookupUser;

	/**
	 * Creates a {@link CustomAuthentication} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CustomAuthentication(InstantiationContext context, Config<?> config) {
		super(context, config);

		_lookupUser = QueryExecutor.compile(config.getLookupUser());
	}

	@Override
	public SecuritySchemeObject createSchemaObject(String schemaName) {
		SecuritySchemeObject securityScheme = OpenAPIConfigs.newSecuritySchema(schemaName);
		securityScheme.setType(SecuritySchemeType.API_KEY);
		securityScheme.setIn(APIKeyPosition.HEADER);
		securityScheme.setName(getConfig().getHeader());
		return securityScheme;
	}

	@Override
	public Authenticator createAuthenticator(List<? extends SecretConfiguration> availableSecrets) {
		return new CustomAuthenticator(getConfig().getHeader(), _lookupUser);
	}

	static class CustomAuthenticator implements Authenticator {

		private String _header;

		private QueryExecutor _lookupUser;

		public CustomAuthenticator(String header, QueryExecutor lookupUser) {
			_header = header;
			_lookupUser = lookupUser;
		}

		@Override
		public Person authenticate(HttpServletRequest req, HttpServletResponse resp)
				throws AuthenticationFailure {
			return TLContext.inSystemContext(CustomAuthenticator.class, () -> {
				String header = req.getHeader(_header);
				Person account =
					(Person) SearchExpression.asSingleElement(_lookupUser.getSearch(), _lookupUser.execute(header));
				if (account == null) {
					throw new AuthenticationFailure(I18NConstants.AUTH_FAILED_WRONG_AUTHENTICATION_DATA);
				}
				return account;
			});
		}

	}

}
