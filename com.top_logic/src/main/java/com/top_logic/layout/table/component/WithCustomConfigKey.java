/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.layout.table.component;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.function.Function;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.ResKey;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.ScriptFunction2;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Configuration interface for components that support custom configuration keys.
 *
 * <p>
 * This interface allows components to define a custom script-based function for computing their
 * configuration key dynamically based on the component's model, instead of using the default
 * component-based key. Configuration keys are used to store and retrieve component-specific
 * settings such as table configurations (column order, visibility, filters, etc.).
 * </p>
 */
@Abstract
public interface WithCustomConfigKey extends ConfigurationItem {

	/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
	Lookup LOOKUP = MethodHandles.lookup();

	/**
	 * Configuration property name for {@link #getCustomConfigKey()}.
	 */
	String CUSTOM_CONFIG_KEY = "custom-config-key";

	/**
	 * Optional script function that computes a custom configuration key based on the component's
	 * model and name.
	 *
	 * <p>
	 * The function receives two parameters: the component's model and the component's qualified
	 * name. It must return a non-empty string to be used as configuration key name. The qualified
	 * name allows creating different configuration keys for different components even when they
	 * share the same model.
	 * </p>
	 * 
	 * <p>
	 * If the function returns <code>null</code>, an empty string, or throws an exception, the
	 * default component-based configuration key is used as fallback.
	 * </p>
	 *
	 * @return The custom key builder function, or <code>null</code> to always use the default
	 *         component-based key.
	 */
	@Name(CUSTOM_CONFIG_KEY)
	ScriptFunction2<String, Object, String> getCustomConfigKey();

	/**
	 * Resolves the configuration key for the given component.
	 *
	 * <p>
	 * If a {@link #getCustomConfigKey() custom config key function} is configured, it is invoked
	 * with the component's model to compute a custom key name. The default key provider function is
	 * used as fallback if:
	 * </p>
	 * <ul>
	 * <li>No custom function is configured</li>
	 * <li>The function returns <code>null</code> or an empty string</li>
	 * <li>The function throws an exception (error is logged)</li>
	 * </ul>
	 *
	 * @param table
	 *        The component for which to resolve the configuration key.
	 * @param defaultKey
	 *        Function that computes the default configuration key when no custom key is configured
	 *        or the custom key function returns an empty result.
	 * @return The resolved {@link ConfigKey}, never <code>null</code>.
	 */
	default ConfigKey resolveConfigKey(LayoutComponent table, Function<LayoutComponent, ConfigKey> defaultKey) {
		ScriptFunction2<String, Object, String> keyBuilder = getCustomConfigKey();
		ConfigKey key;
		if (keyBuilder != null) {
			Object model = table.getModel();
			String customKey;
			try {
				customKey = keyBuilder.impl().apply(model, table.getName().qualifiedName());
			} catch (Exception ex) {
				ResKey error = I18NConstants.ERROR_CREATING_CONFIG_KEY__TITLE_NAME_MODEL.fill(
					table.getTitleKey(),
					table.getName(),
					model);
				InfoService.logError(DefaultDisplayContext.getDisplayContext(), error, ex, WithCustomConfigKey.class);
				customKey = null;
			}
			if (!StringServices.isEmpty(customKey)) {
				key = ConfigKey.named(customKey);
			} else {
				key = defaultKey.apply(table);
			}
		} else {
			key = defaultKey.apply(table);
		}
		return key;
	}

}
