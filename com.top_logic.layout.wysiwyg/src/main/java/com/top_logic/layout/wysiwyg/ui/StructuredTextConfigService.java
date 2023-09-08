/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

import com.google.common.collect.Lists;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.json.JSON.ParseException;
import com.top_logic.basic.json.JSON.ValueAnalyzer;
import com.top_logic.basic.json.config.JSONConfigValueAnalyzer;
import com.top_logic.basic.json.config.JSONObject;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.tool.boundsec.securityObjectProvider.path.Component;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.error.TopLogicException;

/**
 * Configuration Service for the StructuredText.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class StructuredTextConfigService extends ManagedClass {
	
	/** Path of the ckeditor in webapp */
	private static final String CKEDITOR_PATH = "/script/ckeditor/";

	/**
	 * Key for configurations that are not internationalized.
	 * 
	 * <p>
	 * Configurations have paths to template files that can be in different languages. To create
	 * internationalized paths the not internationalized configs stored with this key will be used.
	 * </p>
	 * 
	 * @see #resolveConfigForLang(String, String)
	 * @see #resolveTemplatePathsForLanguage(String, List)
	 */
	private static final String BASE_TEMPLATE_PATH = "base";

	/**
	 * The name of the default feature set for an HTML editor.
	 * 
	 * <p>
	 * The properties of this feature set are implicitly defined by
	 * {@link Config#getDefaultEditorConfig()}.
	 * </p>
	 * 
	 * @see #getEditorConfig(String, String, List, String)
	 */
	public static final String FEATURE_SET_DEFAULT = "default";

	/**
	 * Name for the feature set containing list of {@link #getFeatures() feature} names to select
	 * for a default HTML editor.
	 * 
	 * @see #getEditorConfig(String, String, List, String)
	 */
	public static final String FEATURE_SET_HTML = "html";

	/**
	 * Name for the feature set containing list of {@link #getFeatures() feature} names to select
	 * for an I18N HTML editor.
	 * 
	 * @see #getEditorConfig(String, String, List, String)
	 */
	public static final String FEATURE_SET_I18N_HTML = "i18n-html";

	private static final String ITEMS_NAME_ATTRIBUTE = "items";

	private static final String FEATURE_NAME_ATTRIBUTE = "name";

	private static final String TEMPLATES_FILES_EDITOR_CONFIG = "templates_files";

	private static final String TEMPLATES_EDITOR_CONFIG = "templates";
	
	/**
	 * Constant used to annotate the currently defined {@link Locale} of the {@link Component} which
	 * is independent from the system {@link Locale}.
	 */
	public static final Property<Locale> LOCALE = TypedAnnotatable.property(Locale.class, "locale");

	/**
	 * Map of html configurations in different languages.
	 * 
	 * <p>
	 * The language is relevant for the locations of template files. The map contains the language
	 * as a key ({@link #BASE_TEMPLATE_PATH} for a not internationalized config) and the config with
	 * the template files of the language.
	 * </p>
	 */
	private Map<String, String> _htmlConfig = new HashMap<>();

	/**
	 * Map of i18nhtml configurations in different languages.
	 * 
	 * <p>
	 * The language is relevant for the locations of template files. The map contains the language
	 * as a key ({@link #BASE_TEMPLATE_PATH} for a not internationalized config) and the config with
	 * the template files of the language.
	 * </p>
	 */
	private Map<String, String> _i18nHTMLConfig = new HashMap<>();

	private final Map<String, Object> _features;

	/**
	 * Map of editor configurations in different languages.
	 * 
	 * <p>
	 * The language is relevant for the locations of template files. The map contains the language
	 * as a key ({@link #BASE_TEMPLATE_PATH} for a not internationalized config) and a map of
	 * configs with the feature set as a key.
	 * </p>
	 */
	private final Map<String, Map<String, String>> _editorConfigs = new HashMap<>();

	private final Object _defaultEditorConfigProperties;

	/** Default editor config without internationalization */
	private String _defaultEditorConfig;

	/**
	 * Configuration for the {@link StructuredTextConfigService} defining the client-side
	 * configuration of the CKEditor 4 toolbar.
	 * 
	 * <p>
	 * The CKEditor is configured using a JavaScript object. For direct JavaScript usage, such
	 * native JSON configuration object could be created using the <a href=
	 * "https://ckeditor.com/latest/samples/toolbarconfigurator/index.html#advanced">CKEditor
	 * Configurator</a> tool. However, <i>TopLogic</i> uses a modular approach for configuring different
	 * occurrences of the editor with different configuration objects without repeating the complete
	 * configuration over and over.
	 * </p>
	 * 
	 * <p>
	 * In <i>TopLogic</i>, an editor configuration is generated by combining a
	 * {@link #getDefaultEditorConfig()} with several features from the {@link #getFeatures()}
	 * section. The features to use are selected by name.
	 * </p>
	 * 
	 * <p>
	 * A native JSON string is canonically constructed from a {@link JSONObject} configuration item.
	 * But before the final JSON string for the editor configuration is created, several
	 * {@link JSONObject} configurations are merged together by combining their properties. The
	 * {@link #getDefaultEditorConfig()} is combined with those {@link #getFeatures() feature
	 * configuration objects} that are selected by name in the feature sets.
	 * </p>
	 * 
	 * <p>
	 * For example, the styles available in the editor toolbar are configured by adding a toolbar
	 * group struct with name "styles" to the "toolbar" list:
	 * </p>
	 * 
	 * <pre>
	 * {@code
	 *  <feature name="styles">
	 *     <property key="toolbar">
	 *        <list>
	 *           <struct>
	 *              <property key="name">
	 *                 <string value="styles" />
	 *              </property>
	 *              <property key="items">
	 *                 <list>
	 *                    <string value="Format" />
	 *                    <string value="Font" />
	 *                    <string value="FontSize" />
	 *                 </list>
	 *              </property>
	 *           </struct>
	 *        </list>
	 *     </property>
	 *  </feature>
	 * }
	 * </pre>
	 *
	 * <p>
	 * The "styles" feature is then selected in a feature set, e.g. for the regular editor by
	 * referencing this feature in the feature set "html":
	 * </p>
	 * 
	 * <pre>
	 * {@code
	 * <feature-sets>
	 *    <feature-set name="html">
	 *       <feature-ref name="styles"/>
	 *    </featureSet>
	 * </feature-sets>
	 * }
	 * </pre>
	 * 
	 * @see StructuredTextConfigService#getHtmlConfig(List, String)
	 * @see StructuredTextConfigService#getI18nHTMLConfig(List, String)
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Config extends ServiceConfiguration<StructuredTextConfigService> {

		/** @see #getDefaultEditorConfig() */
		String DEFAULT_CONFIG = "default-config";

		/** @see #getFeatures() */
		String FEATURES = "features";

		/** @see #getTemplates() */
		String TEMPLATES = "templates";

		/** @see #getTemplateFiles() */
		String TEMPLATE_FILES = "templates_files";

		/**
		 * Default settings for every editor instance.
		 */
		@Name(DEFAULT_CONFIG)
		@Mandatory
		JSONObject getDefaultEditorConfig();

		/**
		 * Named {@link JSONObject} fragments each describing the configuration options to activate
		 * an editor feature.
		 * 
		 * <p>
		 * Features are selected in a {@link #getFeatureSets() feature set} and combined with the
		 * {@link #getDefaultEditorConfig()} to create a final JSON configuration object.
		 * </p>
		 * 
		 * @see #getFeatureSets()
		 */
		@Name(FEATURES)
		@EntryTag("feature")
		@Key(NamedConfigMandatory.NAME_ATTRIBUTE)
		List<EditorConfigFeature> getFeatures();

		/**
		 * Named feature sets.
		 * 
		 * <p>
		 * The features defined in the {@link TextEditorConfig} are added to the
		 * {@link #getDefaultEditorConfig()}.
		 * </p>
		 */
		@Key(TextEditorConfig.NAME_ATTRIBUTE)
		Map<String, TextEditorConfig> getFeatureSets();

		/**
		 * Paths to template files.
		 */
		@Name(TEMPLATE_FILES)
		List<String> getTemplateFiles();

		/**
		 * Comma separated list of templates. The templates are defined in the
		 * {@link #getTemplateFiles()}.
		 */
		@Name(TEMPLATES)
		String getTemplates();
	}

	/**
	 * Configuration options for an HTML editor feature.
	 */
	public interface EditorConfigFeature extends JSONObject, NamedConfigMandatory {
		// Sum interface.
	}

	/**
	 * Creates a {@link StructuredTextConfigService}.
	 *
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        {@link StructuredTextConfigService} configuration.
	 */
	public StructuredTextConfigService(InstantiationContext context, Config config) {
		super(context, config);

		ValueAnalyzer instance = JSONConfigValueAnalyzer.INSTANCE;

		try {
			_features = getFeatures(config.getFeatures(), instance);
			_defaultEditorConfigProperties = JSON.fromString(JSON.toString(instance, config.getDefaultEditorConfig()));
		} catch (ParseException ex) {
			throw new TopLogicException(I18NConstants.JSON_PARSING_ERROR, ex);
		}
		_editorConfigs.put(BASE_TEMPLATE_PATH, getEditorConfigs(context, config));

		_defaultEditorConfig = getEditorConfig(Collections.emptyList(), null, null);
		_editorConfigs.get(BASE_TEMPLATE_PATH).put(FEATURE_SET_DEFAULT, _defaultEditorConfig);

		_htmlConfig.put(BASE_TEMPLATE_PATH, resolveFeatureSet(BASE_TEMPLATE_PATH, FEATURE_SET_HTML));
		if (_htmlConfig == null) {
			_i18nHTMLConfig.put(BASE_TEMPLATE_PATH, _defaultEditorConfig);
			context.info("No HTML editor feature set '" + FEATURE_SET_HTML + "' defined, using defaults.");
		}
		_i18nHTMLConfig.put(BASE_TEMPLATE_PATH, resolveFeatureSet(BASE_TEMPLATE_PATH, FEATURE_SET_I18N_HTML));
		if (_i18nHTMLConfig == null) {
			_i18nHTMLConfig = _htmlConfig;
			context.info("No HTML editor feature set '" + FEATURE_SET_I18N_HTML + "' defined, using defaults.");
		}
	}

	private String getTemplateLanguage() {
		Locale documentationLanguage = TLContextManager.getSubSession().get(StructuredTextConfigService.LOCALE);
		return (documentationLanguage != null) ? documentationLanguage.getLanguage()
			: TLContext.getLocale().getLanguage();
	}

	private Map<String, String> getEditorConfigs(InstantiationContext context, Config config) {
		Map<String, TextEditorConfig> featureSets = config.getFeatureSets();
		List<TextEditorConfig> sortedSets = CollectionUtil.topsort(featureSet -> {
			String baseName = featureSet.getBase();
			if (baseName == null) {
				return Collections.emptyList();
			} else {
				TextEditorConfig baseConfig = featureSets.get(baseName);
				if (baseConfig == null) {
					context.error("Base configuration '" + baseName + "' for feature set '"
						+ featureSet.getName() + "' does not exist.");
					return Collections.emptyList();
				} else {
					return Collections.singletonList(baseConfig);
				}
			}
		}, featureSets.values(), false);

		List<String> templateFiles = config.getTemplateFiles();
		String templates = config.getTemplates();
		Map<String, String> editorConfigs = new HashMap<>();
		for (TextEditorConfig featureSet : sortedSets) {
			List<? extends NamedConfiguration> features = getFeatures(featureSet, featureSets);
			String editorConfig =
				getEditorConfig(getFeatureNames(features), BASE_TEMPLATE_PATH, templateFiles, templates);
			editorConfigs.put(featureSet.getName(), editorConfig);
		}
		return editorConfigs;
	}

	private List<? extends NamedConfiguration> getFeatures(TextEditorConfig featureSet,
			Map<String, TextEditorConfig> featureSets) {
		String baseConfig = featureSet.getBase();
		if (baseConfig == null) {
			return featureSet.getEditorConfig();
		}
		List<NamedConfigMandatory> features = new ArrayList<>();
		addFeaturesRecursive(features, featureSet, featureSets);
		return features;
	}

	private void addFeaturesRecursive(List<NamedConfigMandatory> features, TextEditorConfig featureSet,
			Map<String, TextEditorConfig> featureSets) {
		TextEditorConfig baseConfig = featureSets.get(featureSet.getBase());
		if (baseConfig == null) {
			// Error already reported
		} else {
			addFeaturesRecursive(features, baseConfig, featureSets);
		}
		features.addAll(featureSet.getEditorConfig());
	}

	private Map<String, Object> getFeatures(List<EditorConfigFeature> config, ValueAnalyzer instance)
			throws ParseException {
		Map<String, Object> features = new HashMap<>();

		for (EditorConfigFeature configFeature : config) {
			features.put(configFeature.getName(), JSON.fromString(JSON.toString(instance, configFeature)));
		}

		return features;
	}

	private List<String> getFeatureNames(List<? extends NamedConfiguration> features) {
		return Lists.transform(features, NamedConfiguration::getName);
	}

	/**
	 * Returns the editor configuration which supports all features in the given feature set and all
	 * defined templates.
	 * 
	 * @param featureSetName
	 *        Name of a feature set. The configured set defined the list of features that should be
	 *        used.
	 * @param templateFiles
	 *        {@link List} of paths to template files. If null only the default templates will be
	 *        offered.
	 * @param templates
	 *        Comma separated list of template names that are defined in the template files. If null
	 *        only the default templates will be offered.
	 * @return Editor configuration for the given feature set name with the defined templates.
	 */
	public String getEditorConfig(String featureSetName, List<String> templateFiles, String templates) {
		return getEditorConfig(featureSetName, null, templateFiles, templates);
	}

	/**
	 * Returns the editor configuration which supports all features in the given feature set and all
	 * defined templates.
	 * 
	 * @param featureSetName
	 *        Name of a feature set. The configured set defined the list of features that should be
	 *        used.
	 * @param language
	 *        The language of the template files. If <code>null</code> the language will be
	 *        determined by {@link #getTemplateLanguage()}
	 * @param templateFiles
	 *        {@link List} of paths to template files. If null only the default templates will be
	 *        offered.
	 * @param templates
	 *        Comma separated list of template names that are defined in the template files. If null
	 *        only the default templates will be offered.
	 * @return Editor configuration for the given feature set name with the defined templates.
	 */
	public String getEditorConfig(String featureSetName, String language, List<String> templateFiles, String templates) {
		String config = resolveFeatureSet(language, featureSetName);
		if (config == null) {
			Logger.warn("No HTML editor feature set '" + featureSetName + "' defined.",
				StructuredTextConfigService.class);
			config = _defaultEditorConfig;
		}
		Map<Object, Object> configJson = getConfigJsonCopy(config);
		mergeTemplateFiles(configJson, language, templateFiles);
		mergeTemplates(configJson, language, templates);
		return JSON.toString(configJson);
	}

	/**
	 * Converts a config string to a map representing a {@link JSONObject}.
	 * 
	 * @param config
	 *        The string representation of the HTML Editor config.
	 * @return Map representing the {@link JSONObject}
	 */
	@SuppressWarnings("unchecked")
	public Map<Object, Object> getConfigJsonCopy(String config) {
		try {
			Object configJson = JSON.fromString(config);
			return (Map<Object, Object>) deepCopy(configJson);
		} catch (ParseException ex) {
			throw new TopLogicException(I18NConstants.JSON_PARSING_ERROR, ex);
		}
	}

	private String resolveFeatureSet(String language, String featureSetName) {
		language = (language == null) ? getTemplateLanguage() : language;
		Map<String, String> languageConfig = _editorConfigs.get(language);
		if(languageConfig == null) {
			return addEditorsConfigLang(language).get(featureSetName);
		} else {
			return languageConfig.get(featureSetName);
		}
	}

	/**
	 * Available feature sets.
	 */
	public Set<String> getFeatureSets() {
		return _editorConfigs.get(BASE_TEMPLATE_PATH).keySet();
	}

	/**
	 * Creates an editor configuration which supports all mentioned features and contains all
	 * defined templates.
	 * 
	 * @param featureNames
	 *        List of features that should be used.
	 * @param templateFiles
	 *        {@link List} of paths to template files. If null only the default templates will be
	 *        offered.
	 * @param templates
	 *        Comma separated list of template names that are defined in the template files. If null
	 *        only the default templates will be offered.
	 * 
	 * @return Editor Configuration with all desired features and templates.
	 */
	public String getEditorConfig(List<String> featureNames, List<String> templateFiles, String templates) {
		return getEditorConfig(featureNames, null, templateFiles, templates);
	}

	/**
	 * Creates an editor configuration which supports all mentioned features and contains all
	 * defined templates.
	 * 
	 * @param featureNames
	 *        List of features that should be used.
	 * @param language
	 *        The language of the templates. If <code>null</code> the language will be determined by
	 *        {@link #getTemplateLanguage()}
	 * @param templateFiles
	 *        {@link List} of paths to template files. If null only the default templates will be
	 *        offered.
	 * @param templates
	 *        Comma separated list of template names that are defined in the template files. If null
	 *        only the default templates will be offered.
	 * 
	 * @return Editor Configuration with all desired features and templates.
	 */
	public String getEditorConfig(List<String> featureNames, String language, List<String> templateFiles, String templates) {
		Map<?, ?> config = (Map<?, ?>) deepCopy(_defaultEditorConfigProperties);

		mergeFeatures(config, featureNames);
		mergeTemplateFiles(config, language, templateFiles);
		mergeTemplates(config, language, templates);

		return JSON.toString(config);
	}

	/**
	 * Merges the given feature names into the editor config.
	 * 
	 * @param config
	 *        Editor config to expand.
	 * @param featureNames
	 *        List of features that should be used.
	 */
	private void mergeFeatures(Map<?, ?> config, List<String> featureNames) {
		for (String featureName : featureNames) {
			Object properties = getFeatures().get(featureName);
			if (properties == null) {
				Logger.error("No such WYSIWYG feature set '" + featureName + "'.", StructuredTextControl.class);
				continue;
			}

			Map<?, ?> feature = (Map<?, ?>) deepCopy(properties);

			merge(config, feature);
		}
	}

	private void mergeTemplateFiles(Map<?, ?> config, String language, List<String> templateFiles) {
		if (templateFiles == null) {
			return;
		}

		if (language.equals(BASE_TEMPLATE_PATH)) {
			merge(config.get(TEMPLATES_FILES_EDITOR_CONFIG), templateFiles);
		} else {
			List<String> files = resolveTemplatePathsForLanguage(language, templateFiles);
			merge(config.get(TEMPLATES_FILES_EDITOR_CONFIG), files);
		}
	}

	/**
	 * Resolves the paths of the template files with the given language.
	 * 
	 * <p>
	 * The names of the files will get a prefix with the language.
	 * </p>
	 * 
	 * @param language
	 *        The language of the template files.
	 * @param templateFiles
	 *        The paths of the template files without a language prefix.
	 * @return A list of all template paths with the language prefix.
	 */
	private List<String> resolveTemplatePathsForLanguage(String language, List<String> templateFiles) {
		List<String> files = new ArrayList<>();
		if (templateFiles == null || templateFiles.isEmpty()) {
			return files;
		}
		for (String templateFile : templateFiles) {
			String languagePath = getLanguagePath(language, templateFile);
			if (languagePath != null) {
				files.add(languagePath);
			}
		}
		return files;
	}

	/**
	 * Returns the given path with a file name with language prefix if the file exists.
	 * 
	 * <p>
	 * Checks if the file with the given language or a fallback language exists. E.g. if the given
	 * base path looks like "plugins/templates/templates/templateFiles/default.js" and the path to
	 * the English template file is wanted, this method will look for a file in
	 * {@link #CKEDITOR_PATH} + "plugins/templates/templates/templateFiles/en_default.js".
	 * 
	 * @param language
	 *        The language for which the paths have to be resolved.
	 * @param basePath
	 *        The path to the template without a language prefix.
	 *
	 * 
	 * @return The basePath with a language prefix in the file name or <code>null</code> if the
	 *         template of the language can't be found.
	 */
	private String getLanguagePath(String language, String basePath) {
		String filePath = FilenameUtils.getFullPath(basePath);
		String fileName = FilenameUtils.getName(basePath);
		String intFileName = language + "_" + fileName;
		String languagePath = filePath + intFileName;
		try {
			URL resourceUrl = FileManager.getInstance().getResourceUrl(CKEDITOR_PATH + languagePath);
			if (resourceUrl == null) {
				String fallbackLang = ResourcesModule.getInstance().getFallbackLocale().getLanguage();
				if (fallbackLang.equals(language)) {
					return null;
				}
				intFileName = fallbackLang + "_" + fileName;
				languagePath = filePath + intFileName;
				resourceUrl = FileManager.getInstance().getResourceUrl(CKEDITOR_PATH + languagePath);
				if (resourceUrl == null) {
					return null;
				}
			}
		} catch (MalformedURLException ex) {
			Logger.error("Failed loading file " + basePath, ex, this);
			return null;
		}
		return languagePath;
	}

	/**
	 * Modifies the template files paths and templates stored in the given config in
	 * {@link #TEMPLATES_FILES_EDITOR_CONFIG} and {@link #TEMPLATES_EDITOR_CONFIG} by adding a
	 * language prefix to the file names and definition names.
	 * 
	 * @param language
	 *        The language of the templates.
	 * @param config
	 *        The configuration string whose paths shall be modified.
	 * 
	 * @return The internationalized config.
	 */
	@SuppressWarnings("unchecked")
	private String resolveConfigForLang(String language, String config) {
		Map<Object, Object> configJson = getConfigJsonCopy(config);
		List<String> oldFiles = (List<String>) configJson.get(TEMPLATES_FILES_EDITOR_CONFIG);
		List<String> newFiles = resolveTemplatePathsForLanguage(language, oldFiles);
		configJson.put(TEMPLATES_FILES_EDITOR_CONFIG, newFiles);
		String oldTemplates = (String) configJson.get(TEMPLATES_EDITOR_CONFIG);
		String newTemplates = resolveTemplatesForLanguage(language, oldTemplates);
		configJson.put(TEMPLATES_EDITOR_CONFIG, newTemplates);
		return JSON.toString(configJson);
	}

	/**
	 * Adds an internationalized editor config with the given language.
	 * 
	 * <p>
	 * Uses the base config stored in {@link #_editorConfigs}
	 * (_editorConfigs.get(BASE_TEMPLATE_PATH)) to create the internationalized config.
	 * </p>
	 * 
	 * @param language
	 *        The language of the template files of the new config.
	 * @return The new config in the given language that was added to the editor configs.
	 */
	private Map<String, String> addEditorsConfigLang(String language) {
		Map<String, String> baseConfigs = _editorConfigs.get(BASE_TEMPLATE_PATH);
		Map<String, String> langConfigs = new HashMap<>();
		for (Map.Entry<String, String> entry : baseConfigs.entrySet()) {
			String langConfig = resolveConfigForLang(language, entry.getValue());
			langConfigs.put(entry.getKey(), langConfig);
		}
		_editorConfigs.put(language, langConfigs);
		return langConfigs;
	}

	@SuppressWarnings("unchecked")
	private void mergeTemplates(Map<?, ?> config, String language, String templates) {
		if (StringServices.isEmpty(templates)) {
			return;
		}

		if (!language.equals(BASE_TEMPLATE_PATH)) {
			templates = resolveTemplatesForLanguage(language, templates);
		}

		Map<Object, Object> target = (Map<Object, Object>) config;

		String oldTemplates = (String) config.get(TEMPLATES_EDITOR_CONFIG);
		target.put(TEMPLATES_EDITOR_CONFIG, String.join(",", oldTemplates, templates));
	}

	private String resolveTemplatesForLanguage(String language, String templates) {
		String[] languageTemplates = templates.split(",");
		for (int i = 0; i < languageTemplates.length; i++) {
			languageTemplates[i] = language + "_" + languageTemplates[i];
		}

		return String.join(",", languageTemplates);
	}

	private Object deepCopy(Object jsonObject) {
		if (jsonObject instanceof Map) {
			return deepCopyMap((Map<?, ?>) jsonObject);
		} else if (jsonObject instanceof List) {
			return deepCopyList((List<?>) jsonObject);
		} else if (jsonObject instanceof Number || jsonObject instanceof String || jsonObject instanceof Boolean) {
			return jsonObject;
		} else if (jsonObject == null) {
			return jsonObject;
		} else {
			throw new TopLogicException(I18NConstants.JSON_DEEP_COPY_ERROR__OBJ.fill(jsonObject));
		}
	}

	private Object deepCopyList(List<?> list) {
		List<Object> listCopy = new LinkedList<>();
		Iterator<?> iterator = list.iterator();
		
		while (iterator.hasNext()) {
			listCopy.add(deepCopy(iterator.next()));
		}

		return listCopy;
	}

	private Object deepCopyMap(Map<?, ?> map) {
		Map<Object, Object> mapCopy = new HashMap<>();

		for (Object key : map.keySet()) {
			mapCopy.put(deepCopy(key), deepCopy(map.get(key)));
		}

		return mapCopy;
	}

	@SuppressWarnings("unchecked")
	private void merge(Object target, Object item) {
		if (target instanceof Map && item instanceof Map) {
			merge((Map<Object, Object>) target, (Map<?, ?>) item);
		} else if (target instanceof List && item instanceof List) {
			merge((List<Object>) target, (List<?>) item);
		} else {
			Logger.error(Resources.getInstance().getString(I18NConstants.EDITOR_MERGE_ERROR), this);
		}
	}

	private void merge(Map<Object, Object> target, Map<?, ?> item) {
		for (Object key : item.keySet()) {
			if (target.get(key) == null) {
				target.put(key, item.get(key));
			} else {
				merge(target.get(key), item.get(key));
			}
		}
	}

	private void merge(List<Object> target, List<?> item) {
		for (Object listItem : item) {
			if (listItem instanceof Map) {
				/* Special case for a StructuredText configuration where two features should share
				 * the same toolbar group. Then we have to merge to the existant toolbar group
				 * another items. */
				mergeIndexedListItems(target, (Map<?, ?>) listItem);
			} else {
				target.add(listItem);
			}
		}
	}

	private void mergeIndexedListItems(List<Object> target, Map<?, ?> mapItem) {
		Map<?, ?> searchedItem = searchListByItemKey(target, mapItem);

		if (searchedItem != null) {
			merge(getItem(searchedItem), getItem(mapItem));
		} else {
			target.add(mapItem);
		}
	}

	private Object getItem(Map<?, ?> map) {
		return map.get(ITEMS_NAME_ATTRIBUTE);
	}

	private String getFeatureName(Map<?, ?> map) {
		return (String) map.get(FEATURE_NAME_ATTRIBUTE);
	}

	private Map<?, ?> searchListByItemKey(List<Object> features, Map<?, ?> item) {
		return searchListByFeatureName(features, getFeatureName(item));
	}

	private Map<?, ?> searchListByFeatureName(List<Object> features, String featureName) {
		for (Object item : features) {
			Map<?, ?> mapItem = (Map<?, ?>) item;

			if (featureName.equals(getFeatureName(mapItem))) {
				return mapItem;
			}
		}

		return null;
	}

	/**
	 * String representation of an html attribute configuration.
	 */
	public String getHtmlConfig(List<String> templateFiles, String templates) {
		String language = getTemplateLanguage();
		String langConfig = _htmlConfig.get(language);
		if (langConfig == null) {
			langConfig = resolveConfigForLang(language, _htmlConfig.get(BASE_TEMPLATE_PATH));
		}
		if (templateFiles == null) {
			return langConfig;
		}
		Map<Object, Object> configJson = getConfigJsonCopy(langConfig);
		mergeTemplateFiles(configJson, language, templateFiles);
		mergeTemplates(configJson, language, templates);
		return JSON.toString(configJson);
	}

	/**
	 * String representation of an I18N html attribute configuration.
	 */
	public String getI18nHTMLConfig(List<String> templateFiles, String templates) {
		String language = getTemplateLanguage();
		String langConfig = _i18nHTMLConfig.get(language);
		if (langConfig == null) {
			langConfig = resolveConfigForLang(language, _i18nHTMLConfig.get(BASE_TEMPLATE_PATH));
		}
		if (templateFiles == null) {
			return langConfig;
		}
		Map<Object, Object> configJson = getConfigJsonCopy(langConfig);
		mergeTemplateFiles(configJson, language, templateFiles);
		mergeTemplates(configJson, language, templates);
		return JSON.toString(configJson);
	}

	/**
	 * The {@link StructuredTextConfigService} instance.
	 */
	public static StructuredTextConfigService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * the features
	 */
	public Map<String, Object> getFeatures() {
		return _features;
	}

	/**
	 * {@link BasicRuntimeModule} for {@link StructuredTextConfigService}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static final class Module extends TypedRuntimeModule<StructuredTextConfigService> {

		/**
		 * Singleton {@link StructuredTextConfigService.Module} instance.
		 */
		public static final StructuredTextConfigService.Module INSTANCE = new StructuredTextConfigService.Module();

		@Override
		public Class<StructuredTextConfigService> getImplementation() {
			return StructuredTextConfigService.class;
		}

	}
}
