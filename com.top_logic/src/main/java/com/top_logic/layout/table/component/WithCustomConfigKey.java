/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.layout.table.component;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.func.IFunction2;
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
	 * model and default key name.
	 *
	 * <p>
	 * The function receives two parameters: the component's model (first) and the default
	 * configuration key name (second). It must return a non-empty string to be used as
	 * configuration key name. The default key name provides context about what key would be used
	 * without customization, allowing the function to build upon or replace it.
	 * </p>
	 *
	 * <p>
	 * If the function returns <code>null</code> or an empty string, no configuration key is used
	 * (no customization).
	 * </p>
	 *
	 * @return The custom key builder function, or <code>null</code> to always use the default
	 *         configuration key.
	 */
	@Name(CUSTOM_CONFIG_KEY)
	ScriptFunction2<String, Object, String> getCustomConfigKey();

	/**
	 * Resolves a custom configuration key for the given component using the provided builder
	 * function.
	 *
	 * <p>
	 * This method invokes the builder function with the component's model and the default key name
	 * to compute a custom configuration key. The default key is used as fallback when no builder is
	 * configured.
	 * </p>
	 *
	 * <p>
	 * If the builder function returns an empty string or <code>null</code>,
	 * {@link ConfigKey#none()} is returned to indicate that no customization is used. If the
	 * builder throws an exception, the error is logged and {@link ConfigKey#none()} is returned.
	 * </p>
	 *
	 * @param component
	 *        The component for which to resolve the configuration key.
	 * @param builder
	 *        The function that computes the custom key name from the model and default key name, or
	 *        <code>null</code> if no custom builder is configured.
	 * @param defaultKey
	 *        The default configuration key to use when no builder is configured. The key's name is
	 *        passed to the builder function as a context parameter.
	 * @return The resolved {@link ConfigKey}: either a named key computed by the builder, the
	 *         default key if no builder is configured, or {@link ConfigKey#none()} if the builder
	 *         returns empty/null or throws an exception.
	 */
	static ConfigKey resolveObjectKey(LayoutComponent component, IFunction2<String, Object, String> builder,
			ConfigKey defaultKey) {
		if (builder == null) {
			return defaultKey;
		}
		Object model = component.getModel();
		String customKey;
		try {
			customKey = builder.apply(model, defaultKey.get());
		} catch (Exception ex) {
			ResKey error = I18NConstants.ERROR_CREATING_CONFIG_KEY__TITLE_NAME_MODEL.fill(
				component.getTitleKey(),
				component.getName(),
				model);
			InfoService.logError(DefaultDisplayContext.getDisplayContext(), error, ex, WithCustomConfigKey.class);
			customKey = null;
		}
		if (StringServices.isEmpty(customKey)) {
			return ConfigKey.none();
		}
		return ConfigKey.named(customKey);
	}

}
