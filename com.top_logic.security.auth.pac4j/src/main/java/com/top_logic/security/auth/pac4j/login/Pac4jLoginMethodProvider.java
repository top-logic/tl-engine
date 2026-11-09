/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.auth.pac4j.login;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.base.accesscontrol.loginmethod.LoginMethod;
import com.top_logic.base.accesscontrol.loginmethod.LoginMethodProvider;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.security.auth.pac4j.config.ClientConfigurator;
import com.top_logic.security.auth.pac4j.config.Pac4jConfigFactory;

/**
 * {@link LoginMethodProvider} yielding one {@link Pac4jLoginMethod} per configured pac4j SSO
 * client.
 *
 * <p>
 * Reads the static client configuration
 * ({@link com.top_logic.security.auth.pac4j.config.Pac4jConfigFactory.Config#getClients()}), so it
 * works regardless of whether the runtime clients have been instantiated, and yields nothing when
 * the pac4j module is inactive. Each method asks the {@link ClientConfigurator} of its client
 * whether it is available.
 * </p>
 */
public class Pac4jLoginMethodProvider implements LoginMethodProvider {

	/**
	 * Creates a new {@link Pac4jLoginMethodProvider}.
	 */
	public Pac4jLoginMethodProvider() {
		// No configuration needed.
	}

	@Override
	public List<? extends LoginMethod> getLoginMethods() {
		if (!Pac4jConfigFactory.Module.INSTANCE.isActive()) {
			return Collections.emptyList();
		}
		Pac4jConfigFactory<?> factory = Pac4jConfigFactory.getInstance();
		Map<String, ClientConfigurator.Config<? extends ClientConfigurator>> clients = factory.getConfig().getClients();
		List<LoginMethod> result = new ArrayList<>(clients.size());
		for (ClientConfigurator.Config<? extends ClientConfigurator> client : clients.values()) {
			String name = client.getName();
			result.add(new Pac4jLoginMethod(name, buttonLabel(client.getLabel()), client.getIcon(),
				factory.getClientConfigurator(name)));
		}
		return result;
	}

	/**
	 * The label of the login button of a client.
	 *
	 * @param label
	 *        The {@link ClientConfigurator.Config#getLabel() configured label}, or
	 *        <code>null</code>.
	 * @return The given label, or {@link I18NConstants#LOGIN_WITH_SSO}, if the given label is
	 *         missing or {@link #isEmptyLabel(ResKey) empty}.
	 */
	public static ResKey buttonLabel(ResKey label) {
		return isEmptyLabel(label) ? I18NConstants.LOGIN_WITH_SSO : label;
	}

	/**
	 * Whether the given label shows no text.
	 *
	 * <p>
	 * A label whose text comes from an environment variable through an alias is empty in all
	 * languages while the variable is not set, e.g. <code>&lt;label&gt;&lt;en&gt;%ALIAS%&lt;/en&gt;&lt;/label&gt;</code>.
	 * A label that is a resource key counts as not empty.
	 * </p>
	 */
	public static boolean isEmptyLabel(ResKey label) {
		if (label == null || label == ResKey.NONE || label.equals(ResKey.text(""))) {
			return true;
		}
		if (label instanceof ResKey.LiteralKey literal) {
			for (String translation : literal.getTranslations().values()) {
				if (!StringServices.isEmpty(translation)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

}
