/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.person;

import static java.util.Objects.*;

import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.json.JSON.ParseException;
import com.top_logic.basic.translation.TranslationService;
import com.top_logic.knowledge.wrap.ValueProvider;
import com.top_logic.knowledge.wrap.WrapperValueAnalyzer;
import com.top_logic.knowledge.wrap.WrapperValueFactory;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.util.TLContext;

/**
 * Interface for accessing and modifying the personal configurations for the currently logged in
 * user.
 * 
 * <p>
 * The personal configuration consists of the start page, GUI preferences like column order, table
 * sorting, etc.
 * </p>
 * 
 * <p>
 * You can access the {@link PersonalConfiguration} for the currently active user through
 * {@link #getPersonalConfiguration()}.
 * </p>
 * 
 * @see #getPersonalConfiguration()
 * 
 * @author <a href=mailto:fma@top-logic.com>fma</a>
 */
public interface PersonalConfiguration extends ValueProvider {

	/**
	 * Looks up the {@link PersonalConfiguration} for the currently active user.
	 * 
	 * <p>
	 * The resulting object is a transient cache that can be modified without a commit context. The
	 * configuration is made persistent when the current sub-session terminates.
	 * </p>
	 * 
	 * @return The {@link PersonalConfiguration} for the currently active user, or
	 *         <code>null</code>, if the call was made from a thread without {@link TLContext user
	 *         context}.
	 * 
	 * @see TLSubSessionContext#getPersonalConfiguration()
	 */
	static PersonalConfiguration getPersonalConfiguration() {
		TLContext context = TLContext.getContext();
		if (context == null) {
			return NoPersonalConfig.INSTANCE;
		}
		return context.getPersonalConfiguration();
	}

	/**
	 * Stores changes to the personal configuration.
	 * 
	 * @see #getPersonalConfiguration()
	 */
	static void storePersonalConfiguration() {
		TLContext context = TLContext.getContext();
		if (context == null) {
			return;
		}
		context.storePersonalConfiguration();
	}

	/**
	 * The {@link ConfigurationItem} for the features of the {@link PersonalConfiguration}.
	 */
	public interface Config extends ConfigurationItem {

		/** Property name of {@link #getDefaultStartPageAutomatism()}. */
		String DEFAULT_START_PAGE_AUTOMATISM = "default-start-page-automatism";

		/** Property name of {@link #getHideOptionStartPageAutomatism()}. */
		String HIDE_OPTION_START_PAGE_AUTOMATISM = "hide-option-start-page-automatism";

		/**
		 * Whether the start page should be stored when the user logs out.
		 * 
		 * <p>
		 * The user my customize this setting in its account profile, if not
		 * {@link #getHideOptionStartPageAutomatism()} is set.
		 * </p>
		 */
		@BooleanDefault(false)
		boolean getDefaultStartPageAutomatism();

		/**
		 * Whether the option to customize the {@link #getDefaultStartPageAutomatism()} setting
		 * should be hidden from the user.
		 */
		@BooleanDefault(false)
		boolean getHideOptionStartPageAutomatism();

		/**
		 * Whether automatic translation for I18N fields is enabled by default.
		 * 
		 * <p>
		 * This setting only applies, if the {@link TranslationService} is activated in an
		 * application.
		 * </p>
		 */
		boolean getAutoTranslate();

	}

	/**
	 * Default key to store whether the short help must be hide.
	 * 
	 * @see #hideShortHelp()
	 */
	String HIDE_HELP = "hideShortHelp";
    
	/** @see #getStartPageAutomatism() */
	@FrameworkInternal
	String KEY_STORE_START_PAGE_ON_LOGOUT = "store-start-page-on-logout";

	/** @see #getAutoTranslate() */
	@FrameworkInternal
	String KEY_AUTO_TRANSLATE = "auto-translate";

    /** The lag for showing the short help. */
    public static final boolean SHOW_SORT_HELP = true;

	/**
	 * @param layout
	 *        The {@link MainLayout} to set homepage for.
	 * @return The homepage formerly set. May be <code>null</code> when no homepage was set.
	 * 
	 * @throws ConfigurationException
	 *         iff the formerly stored {@link Homepage} has not longer a valid format.
	 */
	default Homepage getHomepage(MainLayout layout) throws ConfigurationException {
		return PersonalConfigurationUtil.getHomepage(this, layout);
	}

	/**
	 * Removes the formerly stored homepage for the given {@link MainLayout}.
	 * 
	 * @param layout
	 *        The {@link MainLayout} to remove homepage for.
	 */
	default void removeHomepage(MainLayout layout) {
		setHomepage(layout, null);
	}

	/**
	 * Sets the home page. That is the page displayed hen the user logs in.
	 * 
	 * @param layout
	 *        The {@link MainLayout} to set homepage for.
	 * @param homepage
	 *        The home page to show. May be <code>null</code>.
	 */
	default void setHomepage(MainLayout layout, Homepage homepage) {
		PersonalConfigurationUtil.setHomepage(this, layout, homepage);
	}

	/**
	 * Whether the start page should be stored when the user logs out.
	 */
	default boolean getStartPageAutomatism() {
		Config config = getConfig();
		if (config.getHideOptionStartPageAutomatism()) {
			return config.getDefaultStartPageAutomatism();
		}
		Boolean entry = getBoolean(KEY_STORE_START_PAGE_ON_LOGOUT);
		if (entry == null) {
			return config.getDefaultStartPageAutomatism();
		}
		return entry;
	}

	/** @see #getStartPageAutomatism() */
	default void setStartPageAutomatism(boolean value) {
		setBooleanValue(KEY_STORE_START_PAGE_ON_LOGOUT, value);
	}

	/**
	 * Whether the user has enabled auto-translation for I18N fields.
	 * 
	 * @see Config#getAutoTranslate()
	 */
	default boolean getAutoTranslate() {
		Boolean entry = getBoolean(KEY_AUTO_TRANSLATE);
		if (entry != null) {
			return entry;
		}

		Config config = getConfig();
		return config.getAutoTranslate();
	}

	/**
	 * @see #getAutoTranslate()
	 */
	default void setAutoTranslate(boolean value) {
		setBooleanValue(KEY_AUTO_TRANSLATE, value);
	}

	/**
	 * @param aValue
	 *        Flag, if the short help is visible or not.
	 */   
	default void setHideShortHelp(boolean aValue) {
		setBooleanValue(HIDE_HELP, aValue);
	}

    /**
     * Flag, if the short help is hidden or not.
     */   
	default boolean hideShortHelp() {
		return getBooleanValue(HIDE_HELP);
	}
    
    /**
     * Get a user specific boolean value, default is false  
     */
	default boolean getBooleanValue(String aKey) {
		Boolean storedBoolean = getBoolean(aKey);
		if (storedBoolean != null) {
			return storedBoolean.booleanValue();
		}
		return false;
	}

    /**
     * Set a user specific boolean value, default is false  
     */
	default void setBooleanValue(String aKey, boolean aValue) {
		setBoolean(aKey, Boolean.valueOf(aValue));
	}
    
    /**
	 * true, if a Boolean value is referenced by the given key, false otherwise.
	 */
	default boolean hasBoolean(String key) {
		return getBoolean(key) != null;
	}

	/**
	 * whether a Boolean value, if any is referenced by the given key, null otherwise
	 */
	default Boolean getBoolean(String key) {
		return (Boolean) getValue(key);
	}

	/**
	 * Store a Boolean value, referenced by the given key. If value is null, the reference will be
	 * removed.
	 */
	default void setBoolean(String key, Boolean value) {
		setValue(key, value);
	}

	/**
	 * Retrieves a stored complex configuration consisting of primitive values and collections
	 * thereof.
	 * 
	 * @see JSON Supported values.
	 */
	default Object getJSONValue(String key) {
		String value = (String) getValue(key);
		if (value == null) {
			return null;
		}
		try {
			return JSON.fromString(value, WrapperValueFactory.WRAPPER_INSTANCE);
		} catch (ParseException ex) {
			// Legacy configuration format.
			Logger.info("Ignoring legacy personal configuration format: " + value, ex, PersonalConfiguration.class);

			// Drop/repair configuration.
			setValue(key, null);

			return null;
		}

	}

	/**
	 * Stores a complex configuration consisting of primitive values and
	 * collections thereof.
	 * 
	 * @see JSON Supported values.
	 */
	default void setJSONValue(String key, Object value) {
		if (value == null) {
			setValue(key, null);
			return;
		}
		try {
			String jsonString = JSON.toString(WrapperValueAnalyzer.WRAPPER_INSTANCE, value);
			setValue(key, jsonString);
		} catch (Exception ex) {
			Logger.warn("Data could not be stored in personal configuration!", ex, PersonalConfiguration.class);
		}
	}

	/** Getter for the {@link Config}. */
	static Config getConfig() {
		return requireNonNull(ApplicationConfig.getInstance().getConfig(Config.class));
	}

}
