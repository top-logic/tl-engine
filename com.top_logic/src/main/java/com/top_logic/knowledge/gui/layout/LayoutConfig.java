/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.List;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;

/**
 * Global configuration options.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface LayoutConfig extends ConfigurationItem {

	/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
	Lookup LOOKUP = MethodHandles.lookup();

	/** Configuration name for the value of {@link #getLayouts()}. */
	String LAYOUTS_NAME = "layouts";

	/**
	 * The maximum number of browser windows, a user can open in the same session.
	 * 
	 * <p>
	 * A value of <code>0</code> means "no limit".
	 * </p>
	 */
	int getMaxSubsessionCount();

	/**
	 * Name of the first application window.
	 * 
	 * <p>
	 * If not set, all window names are randomly chosen (if not provided by the client).
	 * </p>
	 */
	@Format(WindowNameFormat.class)
	String getStableWindowName();

	/**
	 * Layout names of the top-level component declarations.
	 * 
	 * <p>
	 * The first layout is treated as the default layout.
	 * </p>
	 *
	 * @see #getAvailableLayouts()
	 * @see #getDefaultLayout()
	 */
	@FormattedDefault("masterFrame.layout.xml")
	@Format(CommaSeparatedStrings.class)
	@Name(LAYOUTS_NAME)
	List<String> getLayouts();

	/**
	 * Service method to get {@link #getLayouts()} from the {@link ApplicationConfig}.
	 * 
	 * @return The layouts defined in the {@link ApplicationConfig}.
	 */
	static List<String> getAvailableLayouts() {
		return ApplicationConfig.getInstance().getConfig(LayoutConfig.class).getLayouts();
	}

	/**
	 * Service method to get the "default" layout of the application.
	 * 
	 * <p>
	 * The method returns the first entry in {@link #getAvailableLayouts()}, if not empty. Otherwise
	 * a {@link ConfigurationException} is thrown.
	 * </p>
	 * 
	 * @return The layouts defined in the {@link ApplicationConfig}.
	 * 
	 * @throws ConfigurationException
	 *         When no layouts are available.
	 */
	static String getDefaultLayout() throws ConfigurationException {
		List<String> allLayouts = getAvailableLayouts();
		if (allLayouts.isEmpty()) {
			throw new ConfigurationException(I18NConstants.NO_LAYOUT_CONFIGURED, LAYOUTS_NAME,
				CommaSeparatedStrings.INSTANCE.getSpecification(allLayouts));
		}
		return allLayouts.get(0);
	}

	/**
	 * Location of the page that is displayed, if cloning of the main application window is
	 * detected.
	 */
	@StringDefault("subsession.jsp")
	String getSubsessionLocation();

	/**
	 * Location of the page that is displayed, if cloning of an external application window is
	 * detected.
	 */
	@StringDefault("subsession.jsp")
	String getClonedExternalWindowLocation();

	/**
	 * The maximum time in milliseconds a layout load operation may take before issuing a warning.
	 */
	@LongDefault(30000L)
	long getLoadLayoutTime();

	/**
	 * Separator to separate a collection of objects.
	 */
	@StringDefault(" ")
	String getCollectionSeparator();

	/**
	 * When <code>true</code> this will result in some extra comments written to the HTML-header.
	 */
	boolean isDebugHeadersEnabled();

	/**
	 * Global flag for enabling or disabling client-side logging.
	 */
	@BooleanDefault(true)
	boolean isClientSideLoggingEnabled();
}
