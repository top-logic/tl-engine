/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.base.accesscontrol;

import com.top_logic.base.accesscontrol.Login.LoginDeniedException;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.util.Utils;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * {@link ExternalUserMapping} checking the the {@link Person} is known in the LDAP system.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LDAPUserMapping extends AbstractConfiguredInstance<LDAPUserMapping.Config> implements ExternalUserMapping {

	/**
	 * Typed configuration interface definition for {@link LDAPUserMapping}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends PolymorphicConfiguration<LDAPUserMapping> {

		/**
		 * Name of the LDAP in which the {@link Person} must be known.
		 */
		@Mandatory
		String getLDAP();

	}

	/**
	 * Create a {@link LDAPUserMapping}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public LDAPUserMapping(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Person findAccountForExternalName(String externalUserName) throws LoginDeniedException {
		Person authenticatedAccount = ExternalUserMapping.findAccount(externalUserName);
		if (!Utils.equals(getConfig().getLDAP(), authenticatedAccount.getAuthenticationDeviceID())) {
			throw new LoginDeniedException(
				"Person '" + externalUserName + "' is not known in LDAP '" + getConfig().getLDAP() + "'.");
		}
		return authenticatedAccount;
	}

}

