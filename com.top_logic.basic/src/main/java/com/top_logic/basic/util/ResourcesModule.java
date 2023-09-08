/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.list;
import static java.util.Collections.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.LocaleValueProvider;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.core.workspace.ModuleLayoutConstants;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.tools.resources.ResourceFile;

/**
 * The class {@link ResourcesModule} manages {@link I18NBundle}s.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ResourcesModule extends ConfiguredManagedClass<ResourcesModule.Config> {

	/** Flag indicating that the key was found and no default was given. */
	public static final char FLAG_KEY_FOUND = '*';

	/** Flag indicating that the key was not found and no default was given. */
	public static final char FLAG_KEY_NOT_FOUND = '-';

	/**
	 * Resource base name of dynamic storage resources.
	 * 
	 * @see Config#getDynamicStorage()
	 * @see #startResourceTransaction()
	 */
	public static final String DYNAMIC_BUNDLE = "dynamicMessages";

	/**
	 * Extension of resource files.
	 */
	public static final String EXT = ".properties";

	/**
	 * Configuration of the {@link ResourcesModule}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ConfiguredManagedClass.Config<ResourcesModule>, Flags {

		/** @see #getSupportedLocales() */
		String SUPPORTED_LOCALES_ATTRIBUTE = "supported-locales";

		/** @see #getDefaultLocale() */
		String DEFAULT_LOCALE_ATTRIBUTE = "default-locale";

		/** @see #getFallbackLocale() */
		String FALLBACK_LOCALE_ATTRIBUTE = "fallback-locale";

		/** @see #isDisableSystemMessages() */
		String DISABLE_SYSTEM_MESSAGES = "disable-system-messages";

		/** @see #getBundles() */
		String BUNDLES_ATTRIBUTE = "bundles";

		/** @see #getDynamicStorage() */
		String DYNAMIC_STORAGE = "dynamic-storage";

		/**
		 * Name of the directory, where in-app dynamically created resources are stored.
		 */
		@Name(DYNAMIC_STORAGE)
		@Nullable
		String getDynamicStorage();

		/**
		 * Names of the supported {@link Locale}s.
		 * 
		 * @see ResourcesModule#localeFromString(String) The locale format.
		 */
		@Name(SUPPORTED_LOCALES_ATTRIBUTE)
		@ListBinding(tag = "locale", attribute = "name")
		List<String> getSupportedLocales();

		/**
		 * @see #getSupportedLocales()
		 */
		void setSupportedLocales(List<String> value);

		/**
		 * System locale used in contexts that are not related to a specific user.
		 */
		@Format(LocaleValueProvider.class)
		@Name(DEFAULT_LOCALE_ATTRIBUTE)
		Locale getDefaultLocale();

		/**
		 * @see #getDefaultLocale()
		 */
		void setDefaultLocale(Locale value);

		/**
		 * Locale that is used as fallback locale, if the value for a key in the requested locale
		 * can not be found.
		 */
		@Format(LocaleValueProvider.class)
		@Name(FALLBACK_LOCALE_ATTRIBUTE)
		Locale getFallbackLocale();

		/**
		 * @see #getFallbackLocale()
		 */
		void setFallbackLocale(Locale value);

		/**
		 * Whether code-derived resources should not be loaded.
		 * 
		 * <p>
		 * A resource is code-derived, if the English label is taken from the name of the
		 * implementing field, method or class and the tooltip is taken from the JavaDoc of it's
		 * declaration.
		 * </p>
		 */
		@Name(DISABLE_SYSTEM_MESSAGES)
		boolean isDisableSystemMessages();

		/**
		 * Names of the bundles to read I18N from.
		 */
		@Name(BUNDLES_ATTRIBUTE)
		@ListBinding(attribute = "name")
		List<String> getBundles();

		/**
		 * @see #getBundles()
		 */
		void setBundles(List<String> value);

	}

	/**
	 * @since 5.7.5
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Flags extends ConfigurationItem {

		/** @see #isAlwaysShowKeys() */
		String ALWAYS_SHOW_KEYS_ATTRIBUTE = "always-show-keys";

		/** @see #isLogMissingKeys() */
		String LOG_MISSING_KEYS_ATTRIBUTE = "log-missing-keys";

		/** @see #isLogDeprecatedKeys() */
		String LOG_DEPRECATED_KEYS_ATTRIBUTE = "log-deprecated-keys";

		/** @see #isErrorOnMissingKey() */
		String ERROR_ON_MISSING_KEY_ATTRIBUTE = "error-on-missing-key";

		/** @see #isDisableFallback() */
		String DISABLE_FALLBACK_ATTRIBUTE = "disable-fallback-to-default-language";

		/**
		 * Whether to show always the key, regardless if an entry was found or not. The keys are
		 * prefixed with '*', if an entry was found, prefixed with '!' if no entry was found but a
		 * default value is used.
		 */
		@Name(ALWAYS_SHOW_KEYS_ATTRIBUTE)
		boolean isAlwaysShowKeys();

		/**
		 * Whether a log entry is written, when a translation for a key is requested, but there is
		 * no translation.
		 **/
		@BooleanDefault(true)
		@Name(LOG_MISSING_KEYS_ATTRIBUTE)
		boolean isLogMissingKeys();

		/**
		 * Whether an error is logged, when a deprecated resource key is successfully resolved.
		 **/
		@BooleanDefault(false)
		@Name(LOG_DEPRECATED_KEYS_ATTRIBUTE)
		boolean isLogDeprecatedKeys();
		
		/**
		 * Whether to log missing keys with error level.
		 * 
		 * @see #isLogMissingKeys()
		 */
		@Name(ERROR_ON_MISSING_KEY_ATTRIBUTE)
		boolean isErrorOnMissingKey();

		/**
		 * Whether fallback to default language is disabled, if a key was not found.
		 */
		@Name(DISABLE_FALLBACK_ATTRIBUTE)
		boolean isDisableFallback();
	}

	private Map<Locale, I18NBundleSPI> _bundles;

	private final Set<String> _unknownKeys = Collections.synchronizedSet(new TreeSet<>());

	private final Set<String> _unknownKeysWithDefault = Collections.synchronizedSet(new TreeSet<>());

	private volatile boolean _logMissingKeys;

	private volatile boolean _logDeprecatedKeys;

	private volatile boolean _alwaysShowKeys;

	private volatile boolean _disableFallbackToDefLang;

	private final List<String> _bundleNames;

	private final String[] _supportedLocaleNames;

	private final List<Locale> _supportedLocales;

	private final Locale _defaultLocale;

	/** @see #getFallbackLocale() */
	private final Locale _fallbackLocale;

	/**
	 * Creates a new {@link ResourcesModule} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link ResourcesModule}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public ResourcesModule(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		_logMissingKeys = config.isLogMissingKeys();
		_logDeprecatedKeys = config.isLogDeprecatedKeys();
		_alwaysShowKeys = config.isAlwaysShowKeys();
		_disableFallbackToDefLang = config.isDisableFallback();
		_bundleNames = getBundleNames(config);
		_supportedLocaleNames = getSupportedLocaleNames(config);
		_supportedLocales = getSupportedLocales(config);
		_defaultLocale = getDefaultLocale(context, config, _supportedLocaleNames);
		_fallbackLocale = getFallbackLocale(context, config, _supportedLocaleNames);
		checkDynamicStorage(config);
	}

	private void checkDynamicStorage(Config config) {
		String storage = config.getDynamicStorage();
		if (storage == null) {
			return;
		}

		File dir = new File(storage);
		if (!dir.exists()) {
			Logger.info("Directory for dynamic resources '" + storage + "' does not exist, creating.",
				ResourcesModule.class);

			boolean success = dir.mkdirs();
			if (!success) {
				Logger.error("Directory for dynamic resources '" + storage + "' cannot be created.",
					ResourcesModule.class);
			}
		}
	}

	private Locale getFallbackLocale(InstantiationContext context, Config config, String[] supportedLocaleNames) {
		Locale fallbackLanguage = config.getFallbackLocale();
		if (fallbackLanguage == null) {
			StringBuilder noFallbackLanguage = new StringBuilder();
			noFallbackLanguage.append("No '");
			noFallbackLanguage.append(Config.FALLBACK_LOCALE_ATTRIBUTE);
			noFallbackLanguage.append("' configured: Use no fallback.");
			context.info(noFallbackLanguage.toString());
			return null;
		}
		if (!isLanguageSupported(context, fallbackLanguage.getLanguage(), supportedLocaleNames)) {
			StringBuilder unsupportedFallbackLanguage = new StringBuilder();
			unsupportedFallbackLanguage.append("Fallback language '");
			unsupportedFallbackLanguage.append(fallbackLanguage);
			unsupportedFallbackLanguage.append("' for attribute '");
			unsupportedFallbackLanguage.append(Config.FALLBACK_LOCALE_ATTRIBUTE);
			unsupportedFallbackLanguage.append("' is not supported. Supported: '");
			unsupportedFallbackLanguage.append(Arrays.toString(supportedLocaleNames));
			unsupportedFallbackLanguage.append("'.");
			context.error(unsupportedFallbackLanguage.toString());
		}
		return fallbackLanguage;
	}

	private Locale getDefaultLocale(InstantiationContext context, Config config, String[] supportedLocaleNames) {
		Locale defaultLocale = config.getDefaultLocale();
		if (defaultLocale == null) {
			return Locale.GERMANY;
		}
		String configuredLanguage = defaultLocale.getLanguage();
		if (configuredLanguage.isEmpty()) {
			StringBuilder emptyLanguage = new StringBuilder();
			emptyLanguage.append("Empty language is not supported as '");
			emptyLanguage.append(Config.DEFAULT_LOCALE_ATTRIBUTE);
			emptyLanguage.append("': ");
			emptyLanguage.append(defaultLocale);
			emptyLanguage.append(".");
			context.error(emptyLanguage.toString());
		} else {
			boolean isSupported = isLanguageSupported(context, configuredLanguage, supportedLocaleNames);
			if (!isSupported) {
				StringBuilder unsupportedLanguage = new StringBuilder();
				unsupportedLanguage.append("Language '");
				unsupportedLanguage.append(configuredLanguage);
				unsupportedLanguage.append("' in configured default locale '");
				unsupportedLanguage.append(defaultLocale);
				unsupportedLanguage.append("' is not supported. Supported: ");
				unsupportedLanguage.append(Arrays.asList(supportedLocaleNames));
				context.error(unsupportedLanguage.toString());
			}
		}
		if (defaultLocale.getCountry().isEmpty()) {
			StringBuilder emptyCountry = new StringBuilder();
			emptyCountry.append("Empty country is not supported as '");
			emptyCountry.append(Config.DEFAULT_LOCALE_ATTRIBUTE);
			emptyCountry.append("': ");
			emptyCountry.append(defaultLocale);
			emptyCountry.append(".");
			context.error(emptyCountry.toString());
		}
		return defaultLocale;
	}

	private boolean isLanguageSupported(InstantiationContext context, String language, String[] supportedLocaleNames) {
		boolean isSupported = false;
		for (String supportedLocale : supportedLocaleNames) {
			// see Locale.getLanguage() for reason of safeSupportedLanguage
			Locale languageLocale;
			try {
				languageLocale =
					LocaleValueProvider.INSTANCE.getValue(Config.SUPPORTED_LOCALES_ATTRIBUTE, supportedLocale);
			} catch (ConfigurationException ex) {
				context.error("Illegal supported locale: " + supportedLocale, ex);
				continue;
			}
			String safeSupportedLanguage = languageLocale.getLanguage();
			if (language.equals(safeSupportedLanguage)) {
				isSupported = true;
				break;
			}
		}
		return isSupported;
	}

	private String[] getSupportedLocaleNames(Config config) {
		List<String> locales = config.getSupportedLocales();
		String[] supported = new String[locales.size()];
		for (int i = 0; i < supported.length; i++) {
			supported[i] = locales.get(i);
		}
		return supported;
	}

	private List<Locale> getSupportedLocales(Config config) {
		List<Locale> result = list();
		for (String localeConfig : config.getSupportedLocales()) {
			result.add(localeFromString(localeConfig));
		}
		return unmodifiableList(result);
	}

	private List<String> getBundleNames(Config config) {
		List<String> staticBundles = config.getBundles();

		List<String> bundleNames = new ArrayList<>();
		for (String name : staticBundles) {
			bundleNames.add(ModuleLayoutConstants.RESOURCES_RESOURCE_PREFIX + '/' + name);
		}

		String dynamicStorage = config.getDynamicStorage();
		if (dynamicStorage != null) {
			bundleNames.add(FileManager.markDirect(dynamicStorage + '/' + DYNAMIC_BUNDLE));
		}
		
		return Collections.unmodifiableList(bundleNames);
	}

	/**
	 * The configuration of the {@link ResourcesModule}.
	 */
	public final Config getConfiguration() {
		return getConfig();
	}

	/**
	 * Returns the resource for the given {@link Locale}.
	 * 
	 * @param requestedLocale
	 *        The {@link Locale} to get resources for.
	 * 
	 * @return May be <code>null</code> in case there was no resource registered for the given
	 *         locale.
	 */
	public I18NBundle getBundle(Locale requestedLocale) {
		I18NBundleSPI result = _bundles.get(requestedLocale);
		if (result != null) {
			return result;
		}

		Locale lookup = fallback(requestedLocale);
		while (lookup != null) {
			result = _bundles.get(lookup);
			if (result != null) {
				return result;
			}

			lookup = fallback(lookup);
		}

		throw new IllegalStateException("No resource found for locale: " + requestedLocale);
	}

	@Override
	protected void startUp() {
		super.startUp();

		_bundles = loadBundles();
	}

	private Map<Locale, I18NBundleSPI> loadBundles() {
		return new BundleLoader().load();
	}

	@Override
	protected void shutDown() {
		super.shutDown();

		_unknownKeys.clear();
		_unknownKeysWithDefault.clear();
		_bundles = null;
	}

	/**
	 * Creates a new bundle for the given {@link Locale} fallback chain.
	 * 
	 * @param locale
	 *        The {@link Locale}s in descending precedence order to create the {@link I18NBundle}
	 *        for.
	 */
	protected I18NBundleSPI createBundle(Locale locale, Map<String, String> bundle, I18NBundleSPI fallback) {
		return new DefaultBundle(this, locale, bundle, fallback);
	}

	private Locale fallback(Locale locale) {
		boolean hasLanguage = !locale.getLanguage().isEmpty();
		if (hasLanguage) {
			boolean hasVariant = !locale.getVariant().isEmpty();
			if (hasVariant) {
				// Fallback to country.
				return new Locale(locale.getLanguage(), locale.getCountry());
			}

			boolean hasCountry = !locale.getCountry().isEmpty();
			if (hasCountry) {
				// Fallback to language.
				return new Locale(locale.getLanguage());
			}
		}

		Locale fallbackLocale = getFallbackLocale();
		if (fallbackLocale != null && !sameLanguage(locale, fallbackLocale)) {
			return fallbackLocale;
		}

		return null;
	}

	private boolean sameLanguage(Locale l1, Locale l2) {
		return l1.getLanguage().equals(l2.getLanguage());
	}

	/**
	 * A collection containing all known resources, i.e. all resources that may be return by
	 * {@link #getBundle(Locale)}.
	 */
	public Collection<? extends I18NBundle> getBundles() {
		return _bundles.values();
	}

	/**
	 * Name of the directory with dynamic resources.
	 */
	public String getDynamicStorage() {
		return getConfig().getDynamicStorage();
	}

	/**
	 * The set of keys that are requested but not known by some {@link I18NBundle}.
	 */
	public Set<String> getUnknownKeys() {
		return _unknownKeys;
	}

	/**
	 * Marks the given key as unknown.
	 * 
	 * @param key
	 *        The unknown key.
	 */
	public void handleUnknownKey(I18NBundleSPI bundle, ResKey key) {
		boolean newlyAdded = _unknownKeys.add(key.getKey());
		if (newlyAdded) {
			newUnknownKey(bundle, key);
		}
	}

	/**
	 * Marks the given key as unknwon.
	 */
	protected void newUnknownKey(I18NBundleSPI bundle, ResKey key) {
		if (isLogMissingKeys()) {
			String message = "Missing resource '" + key.unknown(bundle) + "'.";

			boolean logError = getConfig().isErrorOnMissingKey();
			if (logError) {
				Logger.error(message, new Exception("Stacktrace"), ResourcesModule.class);
			} else {
				Logger.warn(message, null, ResourcesModule.class);
			}
		}
	}

	/**
	 * Marks the given key as deprecated.
	 */
	@FrameworkInternal
	public void handleDeprecatedKey(I18NBundleSPI bundle, ResKey deprecatedKey, ResKey origKey) {
		// Note: The key in question might be a literal.
		if (deprecatedKey.hasKey()) {
			// Reuse the unknown keys map also for remembering deprecated ones.
			boolean newlyAdded = _unknownKeys.add(deprecatedKey.getKey());
			if (newlyAdded) {
				newDeprecatedKey(bundle, deprecatedKey, origKey);
			}
		}
	}

	private void newDeprecatedKey(I18NBundleSPI bundle, ResKey deprecatedKey, ResKey origKey) {
		if (isLogDeprecatedKeys()) {
			String message =
				"Deprecated resource key " + deprecatedKey.unknown(bundle) + " was resolved, "
					+ origKey.unknown(bundle) + " should be used instead.";

			Logger.error(message, new Exception("Stacktrace"), ResourcesModule.class);
		}
	}

	/**
	 * The set of keys that are requested with a given default but not known by some
	 * {@link I18NBundle}.
	 */
	public Set<String> getUnknownKeysWithDefault() {
		return _unknownKeysWithDefault;
	}

	/**
	 * Marks the given key as unknown.
	 * 
	 * @param key
	 *        The unknown key requested with a given default.
	 * 
	 * @return Whether the given key was added before.
	 */
	public boolean addUnknownKeyWithDefault(String key) {
		return _unknownKeysWithDefault.add(key);
	}

	/**
	 * @see Flags#isLogMissingKeys()
	 */
	public boolean isLogMissingKeys() {
		return _logMissingKeys;
	}

	/**
	 * Sets value of {@link #isLogMissingKeys()}
	 * 
	 * @param logMissingKeys
	 *        New value of {@link #isLogMissingKeys()}.
	 */
	public void setLogMissingKeys(boolean logMissingKeys) {
		resetUnknownKeys();
		_unknownKeysWithDefault.clear();
		_logMissingKeys = logMissingKeys;
	}

	/**
	 * @see Flags#isLogDeprecatedKeys()
	 */
	public boolean isLogDeprecatedKeys() {
		return _logDeprecatedKeys;
	}

	/**
	 * @see #isLogDeprecatedKeys()
	 */
	public void setLogDeprecatedKeys(boolean logDeprecatedKeys) {
		_logDeprecatedKeys = logDeprecatedKeys;
	}

	/**
	 * Drops all recorded unknown keys.
	 */
	protected final void resetUnknownKeys() {
		_unknownKeys.clear();
	}

	/**
	 * @see Flags#isAlwaysShowKeys()
	 */
	public boolean isAlwaysShowKeys() {
		return _alwaysShowKeys;
	}

	/**
	 * Sets value of {@link #isAlwaysShowKeys()}.
	 * 
	 * @param alwaysShowKeys
	 *        New value of {@link #isAlwaysShowKeys()}
	 */
	public void setAlwaysShowKeys(boolean alwaysShowKeys) {
		_alwaysShowKeys = alwaysShowKeys;
	}

	/**
	 * @see Flags#isDisableFallback()
	 */
	public boolean isDisableFallbackToDefLang() {
		return _disableFallbackToDefLang;
	}

	/**
	 * Sets value of {@link #isDisableFallbackToDefLang()}
	 * 
	 * @param disableFallbackToDefLang
	 *        New value of {@link #isDisableFallbackToDefLang()}
	 */
	public void setDisableFallbackToDefLang(boolean disableFallbackToDefLang) {
		_disableFallbackToDefLang = disableFallbackToDefLang;
	}

	/**
	 * Reloads all {@link I18NBundle} of the {@link ResourcesModule}.
	 */
	@FrameworkInternal
	public void internalReload() {
		updateBundles(loadBundles());
		_unknownKeys.clear();
		_unknownKeysWithDefault.clear();
	}

	private void updateBundles(Map<Locale, I18NBundleSPI> newBundles) {
		Map<Locale, I18NBundleSPI> oldBundles = _bundles;

		_bundles = newBundles;

		if (oldBundles != null) {
			for (I18NBundleSPI oldBundle : oldBundles.values()) {
				oldBundle.invalidate();
			}
		}
	}

	/**
	 * Returns the locales supported by the application, e.g "en", "de".
	 */
	public String[] getSupportedLocaleNames() {
		return _supportedLocaleNames;
	}

	/**
	 * An unmodifiable view of the supported {@link Locale}s.
	 * <p>
	 * This is the always the same as {@link #getSupportedLocaleNames()}.
	 * </p>
	 */
	public List<Locale> getSupportedLocales() {
		return _supportedLocales;
	}

	/**
	 * The singleton instance of {@link ResourcesModule}.
	 */
	public static ResourcesModule getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}
	
	/**
	 * Return the names of the bundles to read internationalization from.
	 * 
	 * <p>
	 * Resources in later bundles override resources in former.
	 * </p>
	 */
	public List<String> getBundleNames() {
		return _bundleNames;
	}

	/**
	 * System locale used in contexts that are not related to a specific user.
	 */
	public Locale getDefaultLocale() {
		return _defaultLocale;
	}

	/**
	 * The locale to use when a resource key is not translated in the requested language. May be
	 * <code>null</code>. In such case no fallback is used.
	 */
	public Locale getFallbackLocale() {
		return _fallbackLocale;
	}

	/**
	 * The {@link Locale} for log statements.
	 * <p>
	 * Logs are written in English. This cannot be changed by a setter or even configured, as almost
	 * every log statement contains a hard coded English text.
	 * </p>
	 */
	public static Locale getLogLocale() {
		return Locale.ENGLISH;
	}

	/**
	 * Starts a transaction updating dynamic resources.
	 */
	public ResourceTransaction startResourceTransaction() {
		return new ResourceTransaction() {

			private Map<Locale, Map<ResKey, String>> _changes = new HashMap<>();

			@Override
			public void saveI18N(Locale locale, ResKey key, String translation) {
				_changes.computeIfAbsent(locale, x -> new HashMap<>()).put(key, translation);
			}

			@Override
			public void commit() {
				if (!_changes.isEmpty()) {
					updateDynamicResources(_changes);
				}
			}

			@Override
			public void close() {
				// Ignore.
			}
		};
	}

	/**
	 * Stores an update to the dynamic resources.
	 *
	 * @param changes
	 *        New (or deleted) translations per {@link Locale} and {@link ResKey}. A
	 *        <code>null</code> value means removing the translation for the corresponding key.
	 */
	protected void updateDynamicResources(Map<Locale, Map<ResKey, String>> changes) {
		File storage = new File(getDynamicStorage());
		for (Entry<Locale, Map<ResKey, String>> entry : changes.entrySet()) {
			Locale locale = entry.getKey();
			I18NBundleSPI bundle = _bundles.get(locale);
			if (bundle == null) {
				Logger.error("Tried to store resource for unsupported locale: " + locale,
					ResourcesModule.class);
				continue;
			}

			String fileName = DYNAMIC_BUNDLE + suffix(locale) + EXT;
			File file = new File(storage, fileName);
			ResourceFile resourceFile;
			if (file.exists()) {
				resourceFile = new ResourceFile(file);
			} else {
				resourceFile = new ResourceFile();
			}

			for (Entry<ResKey, String> resource : entry.getValue().entrySet()) {
				ResKey resKey = resource.getKey();
				if (!resKey.hasKey()) {
					continue;
				}

				String key = resKey.getKey();
				String value = resource.getValue();

				if (value != null) {
					resourceFile.setProperty(key, value);
				} else {
					resourceFile.removeProperty(key);
				}
			}

			try {
				resourceFile.saveAs(file);
			} catch (IOException ex) {
				Logger.error("Cannot store dynamic resources: " + file.getAbsolutePath(), ex,
					ResourcesModule.class);
			}
		}
	}

	private String suffix(Locale locale) {
		StringBuilder result = new StringBuilder();

		result.append('_');
		result.append(locale.getLanguage());
		String country = locale.getCountry();
		if (!country.isEmpty()) {
			result.append('_');
			result.append(country);

			String variant = locale.getVariant();
			if (!variant.isEmpty()) {
				result.append('_');
				result.append(variant);
			}
		}

		return result.toString();
	}

	/**
	 * Parses a string as <code><b>language</b>_<b>COUNTRY</b>[_<b>Variant</b>]</code> code and
	 * creates a matching locale.
	 *
	 * @param localeCode
	 *        The language.
	 * @return Default locale in case of any error (can't live without one :-)).
	 */
	public static Locale localeFromString(String localeCode) {
		int eol = localeCode.indexOf('_');
		if (eol < 0) {
			return new Locale(localeCode);
		} else {
			int eoc = localeCode.indexOf('_', eol + 1);
			if (eoc < 0) {
				return new Locale(localeCode.substring(0, eol), localeCode.substring(eol + 1));
			} else {
				return new Locale(localeCode.substring(0, eol), localeCode.substring(eol + 1, eoc),
					localeCode.substring(eoc + 1));
			}
		}
	}

	private class BundleLoader {
		Map<Locale, I18NBundleSPI> _loading;

		public Map<Locale, I18NBundleSPI> load() {
			_loading = new HashMap<>();
			for (Locale locale : getSupportedLocales()) {
				mkBundle(locale);
			}
			return _loading;
		}

		private I18NBundleSPI mkBundle(Locale locale) {
			I18NBundleSPI existing = _loading.get(locale);
			if (existing != null) {
				return existing;
			}

			I18NBundleSPI fallback = mkFallback(locale);
			I18NBundleSPI result = createBundle(locale, loadResources(locale), fallback);

			_loading.put(locale, result);
			return result;
		}

		private I18NBundleSPI mkFallback(Locale locale) {
			Locale fallbackLocale = fallback(locale);
			if (fallbackLocale == null) {
				return null;
			}
			return mkBundle(fallbackLocale);
		}

		private Map<String, String> loadResources(Locale locale) {
			Properties result = new Properties();
			String suffix = suffix(locale);
			addSystemResources(result, suffix);
			addAppResources(result, suffix);
			return toMap(result);
		}

		private void addAppResources(Properties result, String suffix) {
			for (String bundleName : getBundleNames()) {
				addAppBundle(result, bundleName + suffix + EXT);
			}
		}

		private void addAppBundle(Properties result, String resourceName) {
			try {
				// Workaround for problem that Tomcat caches resources
				// and doesn't check that the file has been modified
				BinaryData propertiesData = FileManager.getInstance().getDataOrNull(resourceName);
				if (propertiesData != null) {
					try (InputStream in = propertiesData.getStream()) {
						if (in != null) {
							result.load(in);
							Logger.debug("Loading I18N properties " + resourceName + ".", DefaultBundle.class);
						}
					}
				}
			} catch (IOException ex) {
				Logger.error("Unable to load resources '" + resourceName + "'.", ex, DefaultBundle.class);
			}
		}

		private void addSystemResources(Properties result, String suffix) {
			if (getConfig().isDisableSystemMessages()) {
				return;
			}

			String resourceName = "META-INF/messages" + suffix + EXT;
			try {
				Enumeration<URL> messages =
					getClass().getClassLoader().getResources(resourceName);
				while (messages.hasMoreElements()) {
					URL url = messages.nextElement();
					try (InputStream in = url.openStream()) {
						result.load(in);
					}
				}
			} catch (IOException ex) {
				Logger.error("Unable to load system resources '" + resourceName + "'.", ex, DefaultBundle.class);
			}
		}

		private Map<String, String> toMap(Properties properties) {
			HashMap<String, String> result = new HashMap<>();
			for (Entry<Object, Object> entry : properties.entrySet()) {
				result.put((String) entry.getKey(), (String) entry.getValue());
			}
			return result;
		}
	}

	/**
	 * Module for the {@link ResourcesModule} service.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class Module extends TypedRuntimeModule<ResourcesModule> {

		/** Singleton {@link ResourcesModule.Module} instance. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// singleton instance
		}

		@Override
		public Class<ResourcesModule> getImplementation() {
			return ResourcesModule.class;
		}

	}

}
