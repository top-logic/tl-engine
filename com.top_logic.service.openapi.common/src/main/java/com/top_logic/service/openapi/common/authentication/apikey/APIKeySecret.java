/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.authentication.apikey;

import java.util.function.Predicate;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.form.values.MultiLineText;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.service.openapi.common.authentication.AllAuthenticationDomains;
import com.top_logic.service.openapi.common.authentication.AuthenticationConfig;
import com.top_logic.service.openapi.common.authentication.SecretConfiguration;

/**
 * {@link SecretConfiguration} for authentication using an API key.
 * 
 * @see APIKeyAuthentication
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface APIKeySecret extends SecretConfiguration {

	/** Configuration name for {@link #getAPIKey()}. */
	String API_KEY = "api-key";

	/** Configuration name for {@link #getDescription()}. */
	String DESCRIPTION = "description";

	/**
	 * The API key to authenticate.
	 */
	@Mandatory
	@Name(API_KEY)
	String getAPIKey();

	/**
	 * Optional description about the usage of the API key.
	 */
	@Name(DESCRIPTION)
	@Nullable
	@ControlProvider(MultiLineText.class)
	String getDescription();

	/**
	 * All domains of configured {@link APIKeyAuthentication}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class AllAPIKeyDomains extends AllAuthenticationDomains {

		/**
		 * Creates a new {@link AllAPIKeyDomains}.
		 */
		public AllAPIKeyDomains(DeclarativeFormOptions options) {
			super(options);
		}

		@Override
		protected Predicate<? super AuthenticationConfig> filter() {
			return APIKeyAuthentication.class::isInstance;
		}

	}

}

