/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.authentication;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.config.constraint.algorithm.GenericValueDependency2;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.algorithm.ValueDependency;

/**
 * {@link ValueDependency} asserting that a {@link SecretConfiguration} for a configured
 * authentication exists.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class SecretAvailableConstraint<T>
		extends GenericValueDependency2<List<String>, T, Map<String, ? extends AuthenticationConfig>> {

	/**
	 * Creates a new {@link SecretAvailableConstraint}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public SecretAvailableConstraint(Class<T> tType) {
		super((Class) List.class, tType, (Class) Map.class);
	}

	/**
	 * Checks that for each configured authentication a {@link SecretConfiguration} is available.
	 */
	protected void checkSecretAvailable(PropertyModel<List<String>> authentication,
			Collection<? extends SecretConfiguration> availableSecrets,
			Map<String, ? extends AuthenticationConfig> availableAuthentications) {
		List<String> selectedAuthentication = authentication.getValue();
		if (selectedAuthentication.isEmpty()) {
			return;
		}
		Set<String> availableSecretDomains = availableSecrets
			.stream()
			.map(SecretConfiguration::getDomain)
			.collect(Collectors.toSet());
		for (String auth : selectedAuthentication) {
			AuthenticationConfig authenticationConfig = availableAuthentications.get(auth);
			if (authenticationConfig == null) {
				authentication.setProblemDescription(
					I18NConstants.ERROR_NO_AUTHENTICATION_AVAILABLE__DOMAIN.fill(selectedAuthentication));
				break;
			}
			if (!authenticationConfig.isSeparateSecretNeeded()) {
				continue;
			}

			if (!availableSecretDomains.contains(auth)) {
				authentication.setProblemDescription(
					I18NConstants.ERROR_NO_SECRET_AVAILABLE__DOMAIN.fill(selectedAuthentication));
				break;
			}
		}
	}

}
