/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.gui;

import static com.top_logic.basic.io.FileUtilities.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Logger;
import com.top_logic.basic.ReloadableManager;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.basic.util.ResKey;
import com.top_logic.gui.config.ThemeConfig;
import com.top_logic.gui.config.ThemeSetting;
import com.top_logic.gui.config.ThemeSettings;
import com.top_logic.gui.config.ThemeState;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;


/**
 * An implementation of a ThemeFactory, which is capable of handling multiple themes.
 * 
 * The standard method to get the currently valid theme for a user is to lookup the information
 * stored in the user context.
 * 
 * @author    <a href=mailto:jco@top-logic.com>Jörg Conotte</a>
 */
public class MultiThemeFactory extends ThemeFactory {

	/**
	 * Error message if a theme.xml config file could not be read.
	 */
	private static final String CONFIG_THEME_XML_READING_ERROR = "Could not read theme config file theme.xml";

	/**
	 * Configuration options for {@link MultiThemeFactory}
	 */
	public interface Config extends ThemeFactory.Config {

		/**
		 * @see #getDefaultTheme()
		 */
		String DEFAULT_THEME_PROPERTY = "default-theme";

		/**
		 * {@link ThemeConfig#getId()} of the {@link Theme} a user gets by default.
		 */
		@Name(DEFAULT_THEME_PROPERTY)
		String getDefaultTheme();
		
	}

	/**
	 * Key the current theme in the {@link TLContext}.
	 */
	private static final Property<Theme> CURRENT_THEME_KEY = TypedAnnotatable.property(Theme.class, "currentTheme");

	/**
	 * Key the ID of the personal theme is stored in the {@link PersonalConfiguration}.
	 */
	private static final String PERSONAL_THEME_KEY = "themeID";

	/** Map of themes already loaded. */
	private Map<String, Theme> _themesById = new HashMap<>();

	/** Themes, choosable by user */
	private Collection<Theme> _choosableThemes;

    /** ID of the theme to be used as standard. */
	private Theme _defaultTheme;

	private Map<String, ThemeConfig> _themeConfigsById = new HashMap<>();

	/**
	 * Top level tag name for a theme configuration (theme.xml) file.
	 */
	public static final String TOP_LEVEL_THEME_CONFIG_TAG_NAME = "theme";

	/**
	 * Content of an empty theme.xml configuration.
	 */
	public final static String EMPTY_THEME_CONFIGURATION = "<?xml version=\"1.0\" encoding=\"utf-8\" ?> <theme />";

	/**
	 * Filename for the theme configuration.
	 */
	public static final String THEME_CONFIGURATION_FILENAME = "theme.xml";

	/**
	 * Default folder path for themes.
	 */
	public static final String THEMES_FOLDER_PATH =
		"/WEB-INF" + FileUtilities.PATH_SEPARATOR + "themes" + PATH_SEPARATOR;

	/**
	 * Default folder path for stylesheets.
	 */
	public static final String STYLE_FOLDER_PATH = "/style";

	/**
	 * Creates a {@link MultiThemeFactory} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MultiThemeFactory(InstantiationContext context, Config config) {
		super(context, config);
    }
	
	@Override
	protected void startUp() {
		super.startUp();
		init();

        ReloadableManager.getInstance().addReloadable(this);
	}
	
	@Override
	protected void shutDown() {
		// Ensure that themes are not longer usable.
		invalidateExisting();

		ReloadableManager.getInstance().removeReloadable(this);
		
		super.shutDown();
	}

    @Override
	public boolean reload() {
		init();
		return true;
    }

    /** 
     * @see com.top_logic.basic.Reloadable#getName()
     */
    @Override
	public String getName() {
		return "ThemeFactory";
    }

    /** 
     * @see com.top_logic.basic.Reloadable#getDescription()
     */
    @Override
	public String getDescription() {
		return "Management of all themes in the application";
    }

    /**
     * The Factory gets the information about the available themes from the configuration file.
     * 
     * @see com.top_logic.basic.Reloadable#usesXMLProperties()
     */
    @Override
	public boolean usesXMLProperties() {
        return true;
    }

	@Override
	public Theme getCurrentTheme(TLSubSessionContext context) {
		if (context == null) {
			return _defaultTheme;
		}

		Theme currentTheme = context.get(CURRENT_THEME_KEY);
		if (currentTheme != null && currentTheme.isValid()) {
			return currentTheme;
		}

		PersonalConfiguration pc = context.getPersonalConfiguration();
		if (pc == null) {
			return _defaultTheme;
		}

		Theme personalTheme;
		String personalThemeId = getPersonalThemeId(pc);
		if (personalThemeId != null) {
			personalTheme = this.getTheme(personalThemeId);
			if (personalTheme == null) {
				personalTheme = _defaultTheme;
			}
		} else {
			personalTheme = _defaultTheme;
		}

		// Remember for later direct use.
		setCurrentTheme(context, personalTheme);

		return personalTheme;
	}

	@Override
	public <T, E1 extends Throwable, E2 extends Throwable> T withTheme(Theme contextTheme,
			ComputationEx2<T, E1, E2> computation) throws E1, E2 {
		TLContext tlContext = TLContext.getContext();
		if (tlContext == null) {
			throw new IllegalStateException("No context installed.");
		}

		Theme before = tlContext.get(CURRENT_THEME_KEY);
		tlContext.set(CURRENT_THEME_KEY, contextTheme);
		try {
			return computation.run();
		} finally {
			tlContext.set(CURRENT_THEME_KEY, before);
		}
	}

	@Override
	public Theme getDefaultTheme() {
		return _defaultTheme;
	}

	/**
	 * The {@link Theme#getThemeID()} stored as default for the given user configuration.
	 * 
	 * @param pc
	 *        The user configuration.
	 */
	public static String getPersonalThemeId(PersonalConfiguration pc) {
		return (String) pc.getValue(PERSONAL_THEME_KEY);
	}

	/**
	 * Sets the given {@link Theme#getThemeID()} as default for the given user configuration.
	 * 
	 * @param pc
	 *        The user configuration.
	 * @param themeId
	 *        The theme ID to use.
	 */
	public static void setPersonalThemeId(PersonalConfiguration pc, String themeId) {
		pc.setValue(PERSONAL_THEME_KEY, themeId);
	}

	/**
	 * Set the given theme as current theme for the given session.
	 * 
	 * @param context
	 *        The session.
	 * @param theme
	 *        The theme to use in that session.
	 */
	private static void setCurrentTheme(TLSubSessionContext context, Theme theme) {
		context.set(CURRENT_THEME_KEY, theme);
	}

	/**
	 * Parse the configuration file (that is the XMLProperties) for the theme section
	 */
	private void init() {
		invalidateExisting();
		initThemeConfigsById();
		initThemesById();
		initChoosableThemes();
		initDefaultTheme();
	}

	/**
	 * Marks currently registered themes as invalid (only relevant during reload).
	 */
	private void invalidateExisting() {
		for (Theme theme : _themesById.values()) {
			theme.invalidate();
		}
	}

	private void initThemeConfigsById() {
		_themeConfigsById = readThemeConfigById();
	}

	private void initThemesById() {
		_themesById = new HashMap<>();
		List<String> themesToRemove = new ArrayList<>();

		for (ThemeConfig themeConfig : _themeConfigsById.values()) {
			try {
				initTheme(themeConfig);
			} catch (ThemeInitializationFailure ex) {
				themesToRemove.add(themeConfig.getId());
				Logger.error("Failed to initialize theme '" + themeConfig.getId() + "': " + ex.getMessage(),
					MultiThemeFactory.class);
			}
		}

		removeUninstantiatedThemes(themesToRemove);
	}

	private void removeUninstantiatedThemes(List<String> themesToRemove) {
		for (String themeId : themesToRemove) {
			_themeConfigsById.remove(themeId);
		}
	}

	private void initChoosableThemes() {
		setChoosableThemes(_themesById.values());
	}

	/**
	 * Sets the collection of themes the user can select.
	 */
	public void setChoosableThemes(Collection<Theme> choosableThemes) {
		if (choosableThemes.isEmpty()) {
			Logger.warn("No valid themes found.", MultiThemeFactory.class);
		}

		_choosableThemes = new HashSet<>();

		for (Theme theme : choosableThemes) {
			addChoosableTheme(theme);
		}
	}

	private void initDefaultTheme() {
		String configuredDefaultThemeId = getConfig().getDefaultTheme();
		
		_defaultTheme = getDefaultTheme(configuredDefaultThemeId);
	}

	private Theme getDefaultTheme(String configuredDefaultThemeId) {
		return _themesById.getOrDefault(configuredDefaultThemeId, createDefaultThemeFallback());
	}

	private Theme createDefaultThemeFallback() {
		Optional<Theme> firstTheme = findFirstTheme(_choosableThemes);
		
		if(firstTheme.isPresent()) {
			return firstTheme.get();
		} else {
			throw new ConfigurationError(ResKey.text("Can't create a default fallback theme."));
		}
	}

	private Optional<Theme> findFirstTheme(Collection<Theme> themes) {
		return themes.stream().findFirst();
	}

	private Map<String, ThemeConfig> readThemeConfigById() {
		try {
			return ThemeUtil.readApplicationThemeConfigs();
		} catch (IOException exception) {
			throw new TopLogicException(ResKey.text(CONFIG_THEME_XML_READING_ERROR), exception);
		}
	}

	/**
	 * Creates a Theme for the given {@link ThemeConfig} if the theme is not already created.
	 */
	public void initTheme(ThemeConfig themeConfiguration) throws ThemeInitializationFailure {
		String themeId = themeConfiguration.getId();

		if (!isInitialized(themeId)) {
			initTheme(themeId, themeConfiguration);
		}
	}

	private Theme initTheme(String themeId, ThemeConfig themeConfiguration) throws ThemeInitializationFailure {
		Theme theme = createTheme(themeConfiguration);

		checkVariableNames(theme);

		return _themesById.put(themeId, theme);
	}

	private void checkVariableNames(Theme theme) {
		Map<String, List<ThemeSetting>> settingsByLocalName = getVariablesByLocalName(theme);

		settingsByLocalName.entrySet().forEach(entry -> {
			List<ThemeSetting> settings = entry.getValue();

			if (settings.size() > 1) {
				String localName = entry.getKey();
				String names = settings.stream().map(ThemeSetting::getName).collect(Collectors.joining(", "));

				Logger.error("Multiple variables with name '" + localName + "': " + names, MultiThemeFactory.class);
			}
		});
	}

	private Map<String, List<ThemeSetting>> getVariablesByLocalName(Theme theme) {
		Collection<ThemeSetting> settings = theme.getSettings().getSettings();

		return settings.stream().collect(Collectors.groupingBy(setting -> setting.getLocalName()));
	}

	private Theme createTheme(ThemeConfig themeConfiguration) throws ThemeInitializationFailure {
		String themeId = themeConfiguration.getId();

		Log log = new LogProtocol(MultiThemeFactory.class) {
			@Override
			protected String enhance(String message) {
				return "Theme '" + themeId + "': " + super.enhance(message);
			}
		};

		ThemeSettings settings = Theme.loadLocalSettings(log, themeId);

		List<Theme> parentTheme = getParentThemes(themeConfiguration);

		return createTheme(themeConfiguration, log, settings, parentTheme);
	}

	private Theme getInitializedTheme(String themeId) throws ThemeInitializationFailure {
		if (!isInitialized(themeId)) {
			initTheme(themeId);
		}

		return getTheme(themeId);
	}

	private List<Theme> getParentThemes(ThemeConfig themeConfiguration) throws ThemeInitializationFailure {
		List<String> parentThemeIds = themeConfiguration.getExtends();
		List<Theme> initializedParentThemes = new ArrayList<>();

		if (parentThemeIds != null) {
			for (String parentThemeId : parentThemeIds) {
				if (themeConfiguration.getId().equals(parentThemeId)) {
					throw new ThemeInitializationFailure(
						I18NConstants.ERROR_CYCLIC_THEME_HIERARCHY__ID.fill(themeConfiguration.getId()));
				}
				initializedParentThemes.add(getInitializedTheme(parentThemeId));
			}
		}

		return initializedParentThemes;
	}

	@Override
	public Theme getTheme(String themeId) {
		return _themesById.get(themeId);
	}

	@Override
	public Collection<Theme> getAllThemes() {
		return _themesById.values();
	}

	private void initTheme(String themeId) throws ThemeInitializationFailure {
		ThemeConfig themeConfig = _themeConfigsById.get(themeId);

		if (themeConfig != null) {
			initTheme(themeId, themeConfig);
		} else {
			throw new ThemeInitializationFailure(I18NConstants.ERROR_REFERENCED_THEME_NOT_DEFINED__ID.fill(themeId));
		}
	}

	private boolean isInitialized(String themeId) {
		return _themesById.containsKey(themeId);
	}

	private Theme createTheme(ThemeConfig themeConfig, Log log, ThemeSettings settings, List<Theme> parents) {
		Theme newTheme = new Theme(themeConfig.getId(), isDeployed());

		newTheme.initializeTheme(log, getDeclaredVars(), parents, themeConfig, settings);

		return newTheme;
	}

	@Override
	public final Collection<Theme> getChoosableThemes() {
		return _choosableThemes;
	}

	private void updateChoosableTheme(String themeID) throws ThemeInitializationFailure {
		removeChoosableTheme(themeID);
		addChoosableTheme(themeID);
	}

	private void addChoosableTheme(Theme theme) {
		if (theme.getConfig().getState() == ThemeState.ENABLED) {
			_choosableThemes.add(theme);
		}
	}

	/**
	 * Add the theme with the given id to the choosables themes for the user.
	 */
	public void addChoosableTheme(String themeId) throws ThemeInitializationFailure {
		addChoosableTheme(getInitializedTheme(themeId));
	}

	/**
	 * Removes the theme with the given id from all choosables themes.
	 */
	public boolean removeChoosableTheme(String themeID) {
		return _choosableThemes.removeIf(theme -> theme.getThemeID().equals(themeID));
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	/**
	 * A Map containing all configured themes.
	 */
	public Map<String, ThemeConfig> getThemeConfigsById() {
		return _themeConfigsById;
	}

	/**
	 * All configured theme ids.
	 */
	public Collection<String> getThemeConfigIds() {
		return _themeConfigsById.values().stream().map(themeConfig -> themeConfig.getId()).collect(Collectors.toSet());
	}

	/**
	 * Theme configuration for the given name.
	 */
	public ThemeConfig getThemeConfig(String name) {
		return _themeConfigsById.get(name);
	}

	/**
	 * Replaces the given transient {@link ThemeConfig} with the given theme id.
	 */
	public void replaceThemeConfig(String themeId, ThemeConfig themeConfig) throws ThemeInitializationFailure {
		_themeConfigsById.replace(themeId, themeConfig);
		dropTheme(themeId);
		updateChoosableTheme(themeId);
	}

	private void dropTheme(String themeId) {
		Theme old = _themesById.remove(themeId);
		if (old != null) {
			old.invalidate();
		}
	}

	/**
	 * @see #getThemeConfigsById()
	 */
	public void setThemeConfigsById(Map<String, ThemeConfig> themeConfigsById) {
		_themeConfigsById = themeConfigsById;
	}

	/**
	 * Adds a {@link ThemeConfig} to the transient loaded themes configs.
	 */
	public void putThemeConfig(String themeId, ThemeConfig themeConfig) throws ThemeInitializationFailure {
		_themeConfigsById.put(themeId, themeConfig);

		addChoosableTheme(themeId);
	}

	/**
	 * Removes the theme with the given id. I.e. the transient theme, theme config and updates the
	 * choosables themes.
	 */
	public void removeTheme(String themeId) {
		_themeConfigsById.remove(themeId);
		dropTheme(themeId);

		removeChoosableTheme(themeId);
	}

}
