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

import com.top_logic.base.accesscontrol.login.LoginMethod;
import com.top_logic.base.accesscontrol.login.LoginMethodProvider;
import com.top_logic.basic.util.ResKey;
import com.top_logic.security.auth.pac4j.config.ClientConfigurator;
import com.top_logic.security.auth.pac4j.config.Pac4jConfigFactory;

/**
 * {@link LoginMethodProvider} yielding one {@link Pac4jLoginMethod} per configured pac4j SSO client.
 *
 * <p>
 * Reads the static client configuration ({@link Pac4jConfigFactory.Config#getClients()}), so it
 * works regardless of whether the runtime clients have been instantiated, and yields nothing when
 * the pac4j module is inactive.
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
		Map<String, ClientConfigurator.Config<? extends ClientConfigurator>> clients =
			Pac4jConfigFactory.getInstance().getConfig().getClients();
		List<LoginMethod> result = new ArrayList<>(clients.size());
		for (ClientConfigurator.Config<? extends ClientConfigurator> client : clients.values()) {
			String name = client.getName();
			ResKey label = client.getLabel() != null ? client.getLabel() : ResKey.text(name);
			result.add(new Pac4jLoginMethod(name, label, client.getIcon()));
		}
		return result;
	}

}
