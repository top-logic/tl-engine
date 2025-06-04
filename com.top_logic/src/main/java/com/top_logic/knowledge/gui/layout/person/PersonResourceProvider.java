/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.person;

import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.gui.WrapperResourceProvider;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.util.Resources;

/**
 * {@link ResourceProvider} for {@link Person}s.
 * 
 * @author <a href="mailto:kbu@top-logic.com"></a>
 */
public class PersonResourceProvider extends WrapperResourceProvider
		implements ConfiguredInstance<PersonResourceProvider.Config> {

	private Config _config;

	/**
	 * Configuration for the {@link PersonResourceProvider}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<PersonResourceProvider> {

		/**
		 * If <code>true</code>, the login name will be included in the label.
		 */
		@BooleanDefault(false)
		boolean getShowLoginName();

		/**
		 * Setter for {@link #getShowLoginName()}.
		 */
		void setShowLoginName(boolean showLoginName);
	}

	/**
	 * Create a {@link PersonResourceProvider}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public PersonResourceProvider(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	/**
	 * Creates a {@link Config} with default values.
	 * 
	 * @see #createConfig(boolean)
	 */
	public static Config createConfig() {
		return createConfig(false);
	}

	/**
	 * Creates a {@link Config} to instantiate {@link PersonResourceProvider} programmatically.
	 * 
	 * @param showLoginName
	 *        Value of {@link Config#getShowLoginName()}.
	 */
	public static Config createConfig(boolean showLoginName) {
		Config item = TypedConfiguration.newConfigItem(Config.class);
		item.setShowLoginName(showLoginName);
		return item;
	}

    @Override
	public String getLabel(Object anObject) {
        if (anObject instanceof Person) {
			Person account = (Person) anObject;

			UserInterface user = account.getUser();
			if (user == null) {
				// No additional information available.
				return account.getName();
			}

			String loginName = account.getName();
			String lastName = user.getName();
			String title = user.getTitle();
			if (title != null) {
				lastName = title + " " + lastName;
			}
			String firstName = user.getFirstName();
			ResKey key;
			if (getConfig().getShowLoginName()) {
				if (StringServices.isEmpty(firstName)) {
					key = I18NConstants.ACCOUNT_LABEL__LAST_LOGIN.fill(lastName, loginName);
				} else {
					key = I18NConstants.ACCOUNT_LABEL__FIRST_LAST_LOGIN.fill(firstName, lastName, loginName);
				}
			} else {
				if (StringServices.isEmpty(firstName)) {
					key = I18NConstants.ACCOUNT_LABEL__LAST.fill(lastName);
				} else {
					key = I18NConstants.ACCOUNT_LABEL__FIRST_LAST.fill(firstName, lastName);
				}
			}
			return Resources.getInstance().getString(key);
		} else {
            return super.getLabel(anObject);
        }
    }

	@Override
	protected ResKey getTooltipNonNull(Object object) {
		Person account = (Person) object;

		UserInterface user = account.getUser();
		if (user == null) {
			// No additional information available.
			return null;
		}

		String label = this.getLabel(account);
		String mail = user.getEMail();
		String phone = user.getPhone();

		return I18NConstants.ACCOUNT_TOOLTIP__LABEL_MAIL_PHONE.fill(
			quote(label),
			quote(mail),
			quote(phone));
	}
}
