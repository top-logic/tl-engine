/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.gui;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Reloadable;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.format.BuiltInFormats;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.RuntimeModule;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.Computation;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.html.template.EmptyTemplate;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.html.template.HTMLTemplateUtils;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.basic.DefaultValue;
import com.top_logic.layout.basic.IconsBase;
import com.top_logic.layout.basic.TemplateType;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.ThemeImage.CachedThemeIcon;
import com.top_logic.layout.basic.ThemeImageConfigFormat;
import com.top_logic.layout.form.format.ColorConfigFormat;
import com.top_logic.layout.template.WithProperties;
import com.top_logic.util.TLContext;

/**
 * Registry of {@link Theme}s.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public abstract class ThemeFactory extends ConfiguredManagedClass<ThemeFactory.Config> implements Reloadable {

	/**
	 * Configuration options for {@link ThemeFactory}
	 */
	public interface Config extends ConfiguredManagedClass.Config<ThemeFactory> {
		
		/** Name of property {@link #isDeployed()}. */
		String DEPLOYED_NAME = "is-deployed";

		/** Flag if we are in a deployed state (= styles where pre-created) */
		@Name(DEPLOYED_NAME)
		boolean isDeployed();
	}

	private static final String CLASS_NAME = "Icons";

	private static final Map<Class<?>, ConfigurationValueProvider<?>> DEFAULT_VALUE_PARSERS =
		new MapBuilder<Class<?>, ConfigurationValueProvider<?>>()
			.put(DisplayDimension.class, DisplayDimension.ValueProvider.INSTANCE)
			.put(ThemeImage.class, ThemeImageConfigFormat.INSTANCE)
			.put(ThemeImage.Img.class, ThemeImageConfigFormat.INSTANCE)
			.put(Color.class, ColorConfigFormat.INSTANCE)
			.toMap();

	private Map<String, ThemeVar<?>> _declaredVars = new HashMap<>();

	/**
	 * Creates a {@link ThemeFactory} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ThemeFactory(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * @see Config#isDeployed()
	 */
	public boolean isDeployed() {
		return getConfig().isDeployed();
	}

    /**
	 * The theme for the current user.
	 * 
	 * @see #getCurrentTheme(TLSubSessionContext)
	 */
	public final Theme getCurrentTheme() {
		return getCurrentTheme(TLContext.getContext());
	}

	/**
	 * The {@link Theme} for the given context.
	 */
	public abstract Theme getCurrentTheme(TLSubSessionContext context);

    /**
     * Return the theme to be used by the current user.
     * 
     * @return    The current theme for the user.
     */
    public static Theme getTheme() {
        return (ThemeFactory.getInstance().getCurrentTheme());
    }

    /**
     * Return the only instance of this class.
     * 
     * @return    The only instance of this class.
     */
    public static ThemeFactory getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * {@link RuntimeModule} managing the {@link ThemeFactory} service.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class Module extends TypedRuntimeModule<ThemeFactory> {

		/**
		 * Singleton {@link ThemeFactory.Module} instance.
		 */
		public static final ThemeFactory.Module INSTANCE = new ThemeFactory.Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<ThemeFactory> getImplementation() {
			return ThemeFactory.class;
		}

    }

	/**
	 * Return all themes, which are choosable by user, never <code>null</code>.
	 * 
	 * <p>
	 * By default, every defined theme is choosable by user. This can be restricted by
	 * <code>&#60;section name="ChoosableThemes"&#62;...&#60;/section&#62;</code> within application
	 * xml.
	 * </p>
	 */
	public abstract Collection<Theme> getChoosableThemes();

	/**
	 * All defined themes abstract and concrete ones.
	 * 
	 * @see #getChoosableThemes()
	 */
	public abstract Collection<Theme> getAllThemes();

	/**
	 * The {@link Theme} with the given theme ID.
	 */
	public abstract Theme getTheme(String themeId);

	/**
	 * The {@link Theme} that is used, if there is no personal configuration.
	 */
	public abstract Theme getDefaultTheme();

	/**
	 * Executes the given {@link Computation} with the given {@link Theme} installed.
	 * 
	 * @return The result of the given {@link Computation}.
	 */
	public abstract <T, E1 extends Throwable, E2 extends Throwable> T withTheme(Theme theme,
			ComputationEx2<T, E1, E2> computation) throws E1, E2;

	/**
	 * All declared {@link ThemeVar variables}.
	 */
	public Map<String, ThemeVar<?>> getDeclaredVars() {
		return Collections.unmodifiableMap(_declaredVars);
	}

	@Override
	protected void startUp() {
		super.startUp();

		Collection<Class<?>> themeDeclarations =
			TypeIndex.getInstance().getSpecializations(IconsBase.class, true, false, false);
		for (Class<?> themeDeclaration : themeDeclarations) {
			initDeclarations(themeDeclaration);
		}
	}

	@Override
	protected void shutDown() {
		_declaredVars.clear();

		super.shutDown();
	}

	/**
	 * This method must be called from the static initializer of an <code>Icons</code> sub-class.
	 * 
	 * @param iconsClass
	 *        The `Icons` class declaring static fields of type {@link ThemeImage} being filled
	 *        automatically.
	 */
	private void initDeclarations(Class<?> iconsClass) {
		assert iconsClass.getSimpleName().equals(CLASS_NAME) : "Icon set class must be named '" + CLASS_NAME + "'.";
		assert IconsBase.class.isAssignableFrom(iconsClass) : "Icon set class must extend IconsBase";

		Field[] allFields = iconsClass.getDeclaredFields();

		for (int cnt = allFields.length, n = 0; n < cnt; n++) {
			initField(iconsClass, allFields[n]);
		}
	}

	/**
	 * Initializes the given field of the given class.
	 */
	private final void initField(Class<?> iconsClass, Field constantField) {
		int constantModifiers = constantField.getModifiers();
		if (!Modifier.isStatic(constantModifiers) || !Modifier.isPublic(constantModifiers)
			|| Modifier.isFinal(constantModifiers)) {
			return;
		}

		try {
			Class<?> fieldType = constantField.getType();

			if (fieldType == ThemeImage.class) {
				initImageField(iconsClass, constantField);
			} else if (fieldType == ThemeVar.class) {
				initThemeVarField(iconsClass, constantField);
			} else {
				throw new IllegalArgumentException(
					"Unsupported field type '" + fieldType.getName() + "' in icons class '" + iconsClass.getName()
						+ "'.");
			}
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		} catch (IllegalAccessException ex) {
			throw new UnreachableAssertion(ex);
		}
	}

	private void initImageField(Class<?> iconsClass, Field constantField) throws IllegalAccessException {
		String name = varName(iconsClass, constantField);
		ThemeImage currentValue = (ThemeImage) constantField.get(null);
		if (currentValue instanceof CachedThemeIcon) {
			// Already initialized.
			ThemeImage defaultImage = ((CachedThemeIcon) currentValue).defaultValue();
			_declaredVars.put(name, ThemeVar.genericVar(name, ThemeImage.class, defaultImage));
			return;
		}
	
		ThemeImage defaultValue;
		DefaultValue defaultAnnotation = constantField.getAnnotation(DefaultValue.class);
		if (defaultAnnotation != null) {
			defaultValue = ThemeImage.internalDecode(defaultAnnotation.value());
		} else {
			defaultValue = currentValue;
		}
		_declaredVars.put(name, ThemeVar.genericVar(name, ThemeImage.class, defaultValue));
		ThemeImage themeIcon = ThemeImage.singletonThemeIcon(name, defaultValue);
		constantField.set(null, themeIcon);
	}

	private void initThemeVarField(Class<?> iconsClass, Field constantField)
			throws IllegalAccessException, ConfigurationException {
		String name = varName(iconsClass, constantField);
		ThemeVar<?> existingVar = (ThemeVar<?>) constantField.get(null);
		if (existingVar != null) {
			// Already initialized.
			_declaredVars.put(name, existingVar);
			return;
		}

		Class<?> contentType = getFieldType(constantField);
		String defaultSpec = lookupDefaultSpec(constantField);
		Object defaultValue = parseDefaultSpec(name, contentType, defaultSpec);

		ThemeVar<?> themeVar;
		if (contentType == HTMLTemplateFragment.class) {
			themeVar = createTemplateVar(iconsClass, constantField, name, defaultValue);
		} else {
			themeVar = createGenericVar(contentType, name, defaultValue);
		}
		_declaredVars.put(name, themeVar);
		constantField.set(null, themeVar);
	}

	private ThemeVar<?> createTemplateVar(Class<?> iconsClass, Field constantField, String name,
			Object defaultValue) {
		HTMLTemplateFragment defaultFragment;
		if (defaultValue == null) {
			defaultFragment = loadDefaultTemplate(iconsClass, constantField, name);
		} else {
			defaultFragment = (HTMLTemplateFragment) defaultValue;
		}

		Class<? extends WithProperties> modelType;
		TemplateType typeAnnotation = constantField.getAnnotation(TemplateType.class);
		if (typeAnnotation != null) {
			modelType = typeAnnotation.value();
		} else {
			Logger.warn(
				"No template type defined for template variable, add a @"
					+ TemplateType.class.getSimpleName()
					+ " annotation: " + constantField,
				ThemeFactory.class);
			modelType = null;
		}
		return ThemeVar.templateVar(name, modelType, defaultFragment);
	}

	private HTMLTemplateFragment loadDefaultTemplate(Class<?> iconsClass, Field constantField, String name) {
		HTMLTemplateFragment defaultFragment;
		// Load built-in default from class path.
		String resourceName = constantField.getName() + Theme.TEMPLATE_SUFFIX;
		try {
			defaultFragment = HTMLTemplateUtils.parse(iconsClass, resourceName);
		} catch (Throwable ex) {
			Logger.error(
				"Cannot parse built-in template: " + iconsClass.getPackageName() + "." + resourceName,
				ex, ThemeFactory.class);
			defaultFragment = null;
		}
		if (defaultFragment == null) {
			try {
				defaultFragment = HTMLTemplateUtils
					.parse("Error-template", "<span>[No template '" + resourceName + "' found for '" + name + "']</span>");
			} catch (ConfigurationException ex) {
				Logger.error("Cannot parse default template.", ex, ThemeFactory.class);
				defaultFragment = EmptyTemplate.INSTANCE;
			}
		}
		return defaultFragment;
	}

	private Class<?> getFieldType(Field constantField) {
		Type genericType = constantField.getGenericType();
		Class<?> contentType;
		if (genericType instanceof ParameterizedType) {
			// The content type is the first type parameter of ThemeVar.
			contentType = rawType(((ParameterizedType) genericType).getActualTypeArguments()[0]);
		} else {
			contentType = Object.class;
		}
		return contentType;
	}

	private String lookupDefaultSpec(Field constantField) {
		ClassDefault classAnnotation = constantField.getAnnotation(ClassDefault.class);
		if (classAnnotation != null) {
			return classAnnotation.value().getName();
		}
		DefaultValue defaultAnnotation = constantField.getAnnotation(DefaultValue.class);
		if (defaultAnnotation != null) {
			return defaultAnnotation.value();
		}
		return null;
	}

	private Object parseDefaultSpec(String name, Class<?> contentType, String defaultSpec)
			throws ConfigurationException {
		if (defaultSpec != null) {
			ConfigurationValueProvider<?> parser = DEFAULT_VALUE_PARSERS.get(contentType);
			if (parser == null) {
				parser = BuiltInFormats.getPrimitiveValueProvider(contentType, true);
			}
			if (parser != null) {
				return parser.getValue(name, defaultSpec);
			} else {
				// Assume an instance variable.
				return ConfigUtil.getInstanceMandatory(name,
					ConfigUtil.getClassForNameMandatory(contentType, name, defaultSpec));
			}
		} else {
			return null;
		}
	}

	private static <T> ThemeVar<T> createGenericVar(Class<T> contentType, String name, Object defaultValue) {
		return ThemeVar.genericVar(name, contentType, contentType.cast(defaultValue));
	}

	private static Class<?> rawType(Type bound) {
		if (bound instanceof Class<?>) {
			return (Class<?>) bound;
		} else if (bound instanceof ParameterizedType) {
			return rawType(((ParameterizedType) bound).getRawType());
		} else {
			return Object.class;
		}
	}

	private static String varName(Class<?> iconsClass, Field constantField) {
		return iconsClass.getName() + '.' + constantField.getName();
	}

}
