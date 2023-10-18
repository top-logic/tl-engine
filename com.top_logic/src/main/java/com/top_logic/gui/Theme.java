/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.gui;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.apache.commons.collections4.CollectionUtils;

import com.top_logic.basic.AliasManager;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Log;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.io.FileCompiler;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.vars.VariableExpander;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.config.ThemeConfig;
import com.top_logic.gui.config.ThemeSetting;
import com.top_logic.gui.config.ThemeSettings;
import com.top_logic.gui.config.ThemeState;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.html.template.HTMLTemplateUtils;
import com.top_logic.layout.basic.ThemeImage.Img;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.util.TLMimeTypes;

/**
 * Definition of the look'n feel of the application.
 * 
 * A theme defines the used images and the style sheets. By implementing
 * a new theme one is free to customize the look'n feel of the application.
 * A new theme doesn't have to be complete. If there are only parts of
 * new images, the method {@link #getFileLink(String)} will return the
 * correct link to a file (in the current theme or the default one).
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class Theme {

	/**
	 * The file name suffix for a HTML template file.
	 * 
	 * @see HTMLTemplateFragment
	 */
	public static final String TEMPLATE_SUFFIX = ".template.html";

	/**
	 * Path where computed themes are stored.
	 */
	public static final String WORK_STYLE_PATH = "/style";

	/**
	 * Layout name prefix for theme-based layouts.
	 */
	public static final String LAYOUT_PREFIX = "themes";

	/**
	 * Path where icons are stored.
	 */
	public static final String ICONS_DIRECTORY = "themes";

	/**
	 * Filename of the {@link ThemeSettings} configuration.
	 */
	public static final String THEME_SETTINGS_PATH = "theme-settings.xml";

	/** Default image, when there is no image in {@link #getImageByPath(String)}. */
    private static final Image NULL_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

    /** Display name of the theme (taken from the properties). */
	protected static final String THEME_NAME = "THEME_NAME";

	/** The theme directory for style sheet depending things. */
    private static final String CSS_DIR = "/styles";

    /** The theme directory for tabber. */
    private static final String TABBER_DIR = "/tabber";

    /** The theme directory for the colored tabbers. */
    private static final String COLOR_TABBER_DIR = TABBER_DIR + "/color";

    /** The theme directory for spacers. */
    private static final String SPACER_DIR = "/spacer";
    
	/** The theme directory for command buttons. */
	private static final String COMMAND_BUTTON_DIR = "/buttons/command";

    /** The name of the theme. */
	private String _name;

    /** The base directory for the theme. */
    private String baseDir;

	private ThemeSettings _settings;

    /** The properties of this theme. */
    private Properties fileLinks;

	/** The (compiled) style sheet of this theme. */
	private String _styleSheet;

	/** Merge of this.cssNames and parent.getCssNames() */
	private String[] _cssNames;

	private Map<String, String> knownSheets;

	private List<Theme> _parentThemes;

	private final String _themeID;

	private ThemeConfig _config;

	private VariableExpander _expander;

	private boolean _deployed;

	private Map<String, ThemeVar<?>> _variables;

	private boolean _valid;

	/**
	 * Create a {@link Theme} in non-deployed mode.
	 * 
	 * @param themeID
	 *        See {@link #getThemeID()}.
	 */
	public Theme(String themeID) {
		this(themeID, false);
	}

	/**
	 * Creates a {@link Theme}.
	 *
	 * @param themeID
	 *        See {@link #getThemeID()}.
	 * @param deployed
	 *        See {@link ThemeFactory#isDeployed()}.
	 */
	public Theme(String themeID, boolean deployed) {
		_themeID = themeID;
		_deployed = deployed;
	}

	/**
	 * The configuration, this {@link Theme} was created from.
	 */
	public ThemeConfig getConfig() {
		return _config;
	}

	/**
	 * Settings of the underlying {@link Theme}.
	 */
	public ThemeSettings getSettings() {
		return _settings;
	}

	/**
	 * Whether this instance is still part of the current active theme factory.
	 */
	public boolean isValid() {
		return _valid;
	}

	/**
	 * Marks this theme as outdated.
	 */
	void invalidate() {
		_valid = false;
	}

	/**
	 * Setup of theme properties.
	 * 
	 * @param variables
	 *        All declared variables with the built-in default values.
	 * 
	 * @param themeConfig
	 *        The configuration for this theme.
	 */
	public void initializeTheme(Log log, Map<String, ThemeVar<?>> variables, List<Theme> parents,
			ThemeConfig themeConfig, ThemeSettings settings) {
		_variables = variables;
		_parentThemes = parents;
		_config = themeConfig;

		String path = themeConfig.getPathEffective();

		this.baseDir = path;
        this.fileLinks   = new Properties();
		this.knownSheets = new HashMap<>();

		// Note: Looking up the name must be done before adding the parent settings, since the name
		// setting must no be inherited.
		_name = lookupName(settings);
		
		initDefaults(log, settings);
		_settings = settings;

		_expander = AliasManager.getInstance().getExpander().derive();
		for (ThemeSetting setting : _settings.getSettings()) {
			String rawValue = setting.getRawValue();
			if (rawValue != null) {
				AliasManager.putVariable(_expander, setting.getName(), rawValue);
			}
		}
		_expander.resolveRecursion();

		Collection<String> mergedCssNames = getMergedCssNames(log);
		_cssNames = mergedCssNames.toArray(new String[mergedCssNames.size()]);

		if (getConfig().getState() == ThemeState.ENABLED) {
			if (_deployed) {
				_styleSheet = getThemeGlobalStylesheetName();
			} else {
				String[] cssNames = this.getCSSNames();
				String targetName = getThemeGlobalStylesheetName();
				_styleSheet = createStyleSheet(cssNames, targetName);
			}
		}

		_valid = true;
	}

	private Collection<String> getMergedCssNames(Log log) {
		List<String> localCssNames = new ArrayList<>();
		for (ThemeConfig.StyleSheetRef cssConfig : _config.getStyles()) {
			localCssNames.add(FileCompiler.resolveResourcePath(log, cssConfig.getName()));
		}

		Collection<String> mergedCssNames;
		if (!CollectionUtils.isEmpty(_parentThemes)) {
			List<Theme> parent = new ArrayList<>(_parentThemes);

			// Use linked set to suppress duplicates, but preserve order.
			mergedCssNames = new LinkedHashSet<>();

			List<String> parentStyles = new ArrayList<>();

			// Change order of parents to ensure the selectors of highest priority theme
			// are not overwritten
			Collections.reverse(parent);

			for (Theme parentId : parent) {
				parentStyles = Arrays.asList(parentId.getCSSNames());
				mergedCssNames.addAll(parentStyles);
			}

			// Remove local styles and re-add them to ensure that they appear behind parent styles
			// and therefore take precedence.
			mergedCssNames.removeAll(localCssNames);
			mergedCssNames.addAll(localCssNames);
		} else {
			mergedCssNames = localCssNames;
		}
		return mergedCssNames;
	}

	private void initDefaults(Log log, ThemeSettings settings) {
		ThemeSettings parentSettings;
		if (CollectionUtils.isEmpty(_parentThemes)) {
			settings.addDefaults(log, _variables.values());
		} else {
			for(Theme parentThemeId : _parentThemes) {
				parentSettings = parentThemeId.getSettings();
				settings.addDefaults(log, parentSettings);				
			}
		}
		settings.init(log);
	}

	/**
	 * Resets the setting with the given name to it's default value.
	 * 
	 * @return The new active setting, <code>null</code> if the setting was removed, the new default
	 *         value, if it was reset to a default setting.
	 */
	public ThemeSetting removeSetting(String name) {
		ThemeSetting defaultSetting = null;
		List<Theme> parents = getParentThemes();
		if (parents.isEmpty()) {
			ThemeVar<?> declaration = _variables.get(name);
			if (declaration != null) {
				defaultSetting = _settings.createSetting(declaration);
			}
		} else {
			for (Theme parent : parents) {
				defaultSetting = parent.getSetting(name);
				if (!defaultSetting.isAbstract()) {
					break;
				}
			}
			if (defaultSetting != null && defaultSetting != ThemeSetting.NONE) {
				defaultSetting = defaultSetting.copy();
			}
		}

		if (defaultSetting == null || defaultSetting == ThemeSetting.NONE) {
			_settings.remove(name);
		} else {
			_settings.update(defaultSetting);
		}

		return defaultSetting;
	}

	private String lookupName(ThemeSettings settings) {
		String name = null;

		ThemeSetting nameSetting = settings.get(THEME_NAME);
		if (nameSetting != null) {
			name = (String) nameSetting.getValue();
		}
		if (name == null) {
			// Fallback to theme ID.
			name = getThemeID();
		}

		return name;
	}

	/**
	 * Extract the theme variable name form a <code>%VAR_NAME%</code> expression.
	 *
	 * @param varRef
	 *        The variable reference.
	 * @return The variable name.
	 */
	public static String var(String varRef) {
		assert varRef.startsWith("%");
		assert varRef.endsWith("%");
		return varRef.substring(1, varRef.length() - 1);
	}

	/**
	 * The identifier under which this {@link Theme} is registered in the {@link ThemeFactory}.
	 */
	public String getThemeID() {
		return _themeID;
	}

	/**
	 * The {@link Theme} from which this theme inherits its settings.
	 * 
	 * @return The parent Theme, or <code>null</code>, if this Theme has no fall-back.
	 */
	public List<Theme> getParentThemes() {
		return _parentThemes;
	}

	/**
	 * Loads the {@link ThemeSettings} for the {@link Theme} with the given ID.
	 */
	public static ThemeSettings loadLocalSettings(Log log, String themeId) {
		ThemeSettings settings = new ThemeSettings();
		try {
			FileManager fm = FileManager.getInstance();
			String themePrefix = ThemeUtil.getThemePath(themeId);
			List<BinaryData> settingFiles = fm.getDataOverlays(themePrefix + THEME_SETTINGS_PATH);
			for (BinaryData settingFile : settingFiles) {
				settings.load(log, themeId, settingFile);
			}

			// Load templates.
			for (String path : fm.getResourcePaths(themePrefix)) {
				if (path.endsWith(TEMPLATE_SUFFIX)) {
					try (InputStream in = fm.getStream(path)) {
						HTMLTemplateFragment template = HTMLTemplateUtils.parse(path, in);
						settings.addTemplate(log, themeId, path,
							path.substring(themePrefix.length(), path.length() - TEMPLATE_SUFFIX.length()), template);
					}
				}
			}
		} catch (ConfigurationException | IOException ex) {
			Logger.error("Failed to load theme settings for theme '" + themeId + "'.", ex, Theme.class);
		}
		return settings;
	}

    /**
     * Return the string representation of this instance.
     * 
     * @return    The string representation of this instance (for debugging).
     */
    @Override
	public String toString() {
        return (this.getClass().getName() + " [" +
			"name: '" + _name +
                    "', base: '" + this.baseDir +
                    "']");
    }

    /**
     * Return the name for this theme.
     * 
     * @return    The name for this theme.
     */
    public String getName() {
		return _name;
    }

    /**
     * Return the base directory for this theme.
     * 
     * @return    The requested path (without application context).
     */
    public String getBase() {
        return (this.baseDir);
    }

    /**
     * Return the base directory for the CSS of the theme.
     * 
     * @return    The requested path (without application context).
     */
    public String getStylesDir() {
        return (this.getBase() + CSS_DIR);
    }

    /**
     * Return the base directory for the spacers of the theme.
     * 
     * @return    The requested path (without application context).
     */
    public String getSpacerDir() {
        return (this.getBase() + SPACER_DIR);
    }

    /**
     * Return the base directory for the tabber of the theme.
     * 
     * @return    The requested path (without application context).
     */
    public String getTabberDir() {
        return (this.getBase() + TABBER_DIR);
    }

    /**
     * Return the base directory for the tabber of the theme.
     * 
     * @return    The requested path (without application context).
     */
    public String getColorTabberDir() {
        return (this.getBase() + COLOR_TABBER_DIR);
    }
    
	/**
	 * Return the relative link to the directory for the source-parts of commandbuttons of the theme.
	 * 
	 * @return    The requested path (without application context).
	 */
	public String getRelativeCommandButtonDir() {
		return (COMMAND_BUTTON_DIR);
	}    	    

	/**
	 * The {@link VariableExpander} with all variables defined in this {@link Theme}.
	 */
	public VariableExpander getExpander() {
		return _expander;
	}

	/**
	 * The unparsed theme variable value of the variable with the given name.
	 * 
	 * @param key
	 *        The variable name.
	 * @return The raw string value ignoring the variable type.
	 */
	public String getRawValue(String key) {
		return getSetting(key).getRawValue();
	}

	/**
	 * Lookup a theme-local value.
	 * 
	 * @param var
	 *        The {@link ThemeVar} defining the value.
	 * @return The theme-local value assigned to the given variable.
	 */
	public <T> T getValue(ThemeVar<T> var) {
		T value = getValue(var.getName(), var.getType());
		if (value == null) {
			return lookupDefault(var);
		}
		return value;
	}

	private <T> T lookupDefault(ThemeVar<T> var) {
		try {
			return var.defaultValue();
		} catch (RuntimeException ex) {
			Logger.error("Creating default value for theme variable '" + var.getName() + "' failed in theme '"
				+ getName() + "'.", ex, Theme.class);
			return null;
		}
	}

    /**
	 * The (compiled) style-sheet to use.
	 * 
	 * @see #getCSSNames()
	 */
	public String getStyleSheet() {
		return _styleSheet;
    }

	public final String getStyleSheet(String aName) {
		return getStyleSheet(aName, new String[] { aName });
    }

	/**
	 * Merges the given CSS files into one file and stores it under the (individualised) destination
	 * file.
	 * 
	 * @param destinationName
	 *        Name template for the result file.
	 * @param cssFiles
	 *        Files to merge. Must not be empty.
	 * 
	 * @return A global reference to the created CSS file.
	 */
	public synchronized String getStyleSheet(String destinationName, String[] cssFiles) {
		if (cssFiles.length == 0) {
			throw new IllegalArgumentException("No CSS files given.");
		}
		String theSheet = this.knownSheets.get(destinationName);

		String sheetName = getIndividualStylesheetName(destinationName);
		if (theSheet == null) {
			theSheet = this.createStyleSheet(cssFiles, sheetName);

            this.knownSheets.put(destinationName, theSheet);
        }

        return theSheet;
	}

    /**
	 * Generate style sheets for the files defined in the given string array.
	 * 
	 * @param templateNames
	 *        The file names to generate styles for, must not be <code>null</code>.
	 * @param destinationName
	 *        The name of the resulting stylesheet.
	 * @return The names of the resulting CSS files.
	 */
	protected String createStyleSheet(String[] templateNames, String destinationName) {
		int templateCnt = templateNames.length;

		CSSBuffer buffer = new CSSBuffer();
		for (int n = 0; n < templateCnt; n++) {
			buffer.load(templateNames[n]);
		}
		String contextPath = AliasManager.getInstance().getAlias(AliasManager.APP_CONTEXT);
		buffer.replaceVariables(destinationName, contextPath, this);
		buffer.save(destinationName);
		return destinationName + "?t=" + System.currentTimeMillis();
    }

	/**
	 * Write the stylesheets to the given TagWriter.
	 * 
	 * @param out
	 *        The writer to write to.
	 * @throws IOException
	 *         If writing fails.
	 */
    public void writeStyles(String context, TagWriter out) throws IOException { 
		HTMLUtil.writeStylesheetRef(out, context, getStyleSheet());
    }

    /**
	 * Create an image for the given type.
	 * 
	 * <p>
	 * Note: This method only works for mime-types for which a bitmap image is available.
	 * </p>
	 * 
	 * @param aMimeType
	 *        The mime type to get the image for, must not be <code>null</code>.
	 * @return The requested image, never null.
	 */
    public Image getImage(String aMimeType) {
		Img typeIcon = (Img) TLMimeTypes.getInstance().getMimeTypeImage(aMimeType).resolve();
		if (typeIcon == TLMimeTypes.unknownIcon()) {
			return NULL_IMAGE;
		}
		String path = typeIcon.getImagePath();
		return this.getImageByPath(path);
    }

    /** 
     * Create an image out of the given path (which has to be a sub path of the themes.
     * 
     * @param    anImagePath    The path to the image in the themes folder, must not be <code>null</code>.
     * @return   The requested image, never null.
     */
    public Image getImageByPath(String anImagePath) {
        try {
            String theFileName = this.getFileLink(anImagePath);

            if (!StringServices.isEmpty(theFileName)) {
				try (InputStream in = FileManager.getInstance().getStreamOrNull(theFileName)) {
					if (in != null) {
						return ImageIO.read(in);
					}
				}
            }
        }
        catch (IOException ex) {
            Logger.error("Failed to find image for " + anImagePath, ex, this);
        }

		return NULL_IMAGE;
    }

	/**
	 * Return the link to the requested file.
	 * 
	 * This method will lookup for the file in this theme. If there is no such file, this method
	 * will return the location in the default theme.
	 * 
	 * TODO MGA/KHA please clarify what a "link" is: A Link is a valid string to locate a file local
	 * to the web-application, A Link is a valid string to locate a file global to the
	 * web-application, A Link is ...
	 * 
	 * @param aFileName
	 *        The name of the file within the theme, start with "/"
	 * @return The reference to the existing file or <code>null</code>, if there is no file either
	 *         in the current or the default theme.
	 */
	public String getFileLink(String aFileName) {
        if (aFileName == null) {
            return aFileName; // Sorry we cant help you ...
        }
        
        String theResult = this.getCachedFileLink(aFileName);

        if (theResult == null) {
            String resolvedFileName = null;
        
            // the parameter may be an alias so test this!        
            if (aFileName.indexOf('%') > -1) {
				resolvedFileName = this.getRawValue(Theme.var(aFileName));
            }
            if (resolvedFileName == null) {
                resolvedFileName = aFileName;
            }
            
            String theName    = this.getBase() + resolvedFileName;

            if (!this.exists(theName)) {
                theResult = aFileName;

				if (_parentThemes != null) {
					// reverse list of parent themes to ensure that the theme with the highest
					// priority is not overwritten
					List<Theme> parents = new ArrayList<>(_parentThemes);
					Collections.reverse(parents);

					for (Theme parentTheme : parents) {
						theName = parentTheme.getFileLink(aFileName);

						if (this.exists(theName)) {
							theResult = theName;
						}
					}
    	        }
                // return Original name wich may not denote a file in a theme at all.
            }
            else {
                theResult = theName;
            }

            this.addCachedFileLink(aFileName, theResult);
        }

        return (theResult);
    }
    
    protected final String getCachedFileLink(String aFileName) {
        return this.fileLinks.getProperty(aFileName);
    }
    
    protected final void addCachedFileLink(String aFileName, String aResult) {
        this.fileLinks.put(aFileName, aResult);
    }

    /**
     * Check, if the given file exists in the application path.
     * TODO only for current, not for default?!
     * 
     * @param    aPath    The file to be checked.
     * @return   <code>true</code>, if the file exists.
     */
    protected boolean exists(String aPath) {
		return FileManager.getInstance().exists(aPath);
    }

    /**
     * Return the name of the style sheets to be converted.
     * 
     * This method will return the templates to be used for generation.
     * 
     * @return    The full names of the templates.
     */
    protected String[] getCSSNames() {
		return _cssNames;
    }

    private String getThemeGlobalStylesheetName() {
		return getIndividualStylesheetName("theme.css");
    }
    
	private String getIndividualStylesheetName(String templateName) {
		return WORK_STYLE_PATH + "/_" + this.getName() + '_'
				+ templateName.substring(templateName.lastIndexOf('/') + 1);
	}

	public static boolean isThemeValueReference(String value) {
		int length = value.length();
	
		return length > 2 && value.charAt(0) == '%' && value.charAt(length - 1) == '%';
	}

	/**
	 * Typed variant of {@link #getValue(String)}.
	 */
	public int getIntValue(String key) {
		Number result = getValue(key, Number.class);
		if (result == null) {
			throw new IllegalArgumentException("Setting '" + key + "' is not defined in theme '" + getThemeID() + "'.");
		}
		return result.intValue();
	}

	/**
	 * Looks up a theme setting with the given key.
	 */
	public Object getValue(String key) {
		return getSetting(key).getValue();
	}
	
	/**
	 * Looks up a theme setting with the given key.
	 */
	public <T> T getValue(String key, Class<T> type) {
		ThemeSetting setting = getSettingOrNull(key);
		if (setting == null) {
			return null;
		}
		Object value = setting.getValue();
		if (value != null && !type.isInstance(value)) {
			throw new IllegalArgumentException("Setting '" + key + "' in '" + getThemeID()
				+ "' is of incompatible type, expected '" + type.getName() + "' found '" + value.getClass() + "'.");
		}
		return type.cast(value);
	}

	private ThemeSetting getSetting(String key) {
		ThemeSetting result = getSettingOrNull(key);
		if (result == null) {
			return ThemeSetting.NONE;
		}
		return result;
	}

	private ThemeSetting getSettingOrNull(String key) {
		if (key.indexOf('%') >= 0) {
			throw new IllegalArgumentException("A theme variable name must not contain the '%' char: " + key);
		}
		return _settings.get(key);
	}

}
