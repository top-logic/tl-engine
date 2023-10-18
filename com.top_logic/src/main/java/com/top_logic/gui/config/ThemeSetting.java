/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.gui.config;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Log;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.constraint.annotation.RegexpConstraint;
import com.top_logic.basic.config.format.ClassFormat;
import com.top_logic.gui.Theme;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.ThemeImageConfigFormat;
import com.top_logic.layout.form.control.ColorControlProvider;
import com.top_logic.layout.form.format.ColorConfigFormat;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;
import com.top_logic.layout.form.values.edit.annotation.RenderWholeLine;

/**
 * Base class for a setting.
 */
public abstract class ThemeSetting extends AbstractConfiguredInstance<ThemeSetting.Config<?>> {

	private static final Map<Class<?>, Class<? extends ThemeSetting.Config<?>>> SETTING_TYPES;

	static {
		Map<Class<?>, Class<? extends ThemeSetting.Config<?>>> map = new HashMap<>();
		map.put(String.class, StringSetting.Config.class);
		map.put(Color.class, ColorSetting.Config.class);
		map.put(Integer.class, IntSetting.Config.class);
		map.put(Float.class, FloatSetting.Config.class);
		map.put(DisplayDimension.class, SizeSetting.Config.class);
		map.put(Boolean.class, BooleanSetting.Config.class);
		map.put(ThemeImage.class, IconSetting.Config.class);
		map.put(ThemeImage.Img.class, ImgSetting.Config.class);
		map.put(HTMLTemplateFragment.class, TemplateSetting.Config.class);
		map.put(Class.class, TypeSetting.Config.class);
		SETTING_TYPES = map;
	}

	/**
	 * The {@link ThemeSetting} configuration type for a theme variables content type.
	 */
	public static Class<? extends ThemeSetting.Config<?>> configType(Class<?> valueType) {
		return SETTING_TYPES.getOrDefault(valueType, InstanceSetting.Config.class);
	}

	private static final String EXPR_START = "%";

	private static final String VAR_NAME = "([A-Za-z_][-A-Za-z_0-9\\.@/]*)|(<!--)";

	private static final String EXPR_END = "(" + VAR_NAME + ")%";

	/**
	 * Regular expression describing a reference to another setting in a theme expression.
	 * 
	 * @see #REF_PATTERN
	 */
	public static final String REF_EXPR = EXPR_START + EXPR_END;

	/**
	 * {@link Pattern} describing a reference to another setting in a theme expression.
	 */
	public static final Pattern REF_PATTERN = Pattern.compile(REF_EXPR);

	/**
	 * Common configuration interface for all {@link ThemeSetting}s.
	 */
	@Abstract
	public interface Config<I extends ThemeSetting> extends PolymorphicConfiguration<I> {

		/**
		 * @see #isAbstract()
		 */
		String ABSTRACT = "abstract";

		/**
		 * Configuration name of the {@link #getExpr()} property.
		 */
		String EXPRESSION_ATTRIBUTE = "expr";

		/**
		 * Common name of all typed value properties in {@link ThemeSetting} configurations.
		 */
		String VALUE = "value";

		/** Configuration name of the {@link #getName()} property. */
		String NAME_ATTRIBUTE = "name";

		/**
		 * Name of the configured object.
		 */
		@Name(NAME_ATTRIBUTE)
		@Mandatory
		@RegexpConstraint(VAR_NAME)
		@RenderWholeLine
		String getName();

		/**
		 * Setter for {@link #getName()}.
		 */
		void setName(String name);

		/**
		 * The application value of this setting.
		 */
		@Abstract
		@Name(VALUE)
		@Nullable
		@RenderWholeLine
		Object getValue();

		/**
		 * @see #getValue()
		 */
		void setValue(Object value);

		/**
		 * Theme expression to construct the value from, if no concrete value is given.
		 * 
		 * <p>
		 * This theme expression may reference other settings with the <code>%OTHER_NAME%</code>
		 * syntax.
		 * </p>
		 */
		@Nullable
		@Name(EXPRESSION_ATTRIBUTE)
		@RenderWholeLine
		String getExpr();

		/** @see #getExpr() */
		void setExpr(String value);

		/**
		 * Whether this setting does not provide a {@link #getValue()}.
		 * 
		 * <p>
		 * An abstract setting is only there to define the type of a theme variable.
		 * </p>
		 */
		@Name(ABSTRACT)
		boolean isAbstract();

		/**
		 * @see #isAbstract()
		 */
		void setAbstract(boolean b);
	}

	private final boolean _abstract;

	private Object _value;

	private boolean _initializing;
	private boolean _initialized;

	/**
	 * Creates a {@link ThemeSetting}.
	 */
	public ThemeSetting(InstantiationContext context, ThemeSetting.Config<?> config, Object value) {
		super(context, config);
		_value = value;
		_abstract = config == null || config.isAbstract();
	}

	/**
	 * The name of this setting.
	 * 
	 * @see Config#getName()
	 */
	public String getName() {
		return getConfig().getName();
	}

	/**
	 * Returns the local name, name without its package prefix.
	 */
	public String getLocalName() {
		String name = getName();

		if (name.startsWith("mime.")) {
			return name.substring("mime.".length());
		}

		int sep = name.lastIndexOf('.');

		if (sep < 0) {
			return name;
		}

		return name.substring(sep + 1);
	}

	/**
	 * Whether this setting does not define a {@link #getValue()}.
	 * 
	 * @see Config#isAbstract()
	 */
	public boolean isAbstract() {
		return _abstract;
	}

	/**
	 * Whether this setting should produce a CSS variable.
	 */
	public boolean isCssRelevant() {
		return true;
	}

	/**
	 * The theme expression that is evaluated producing the {@link #getRawValue()}.
	 */
	public String getExpr() {
		return getConfig().getExpr();
	}

	/**
	 * The concrete value configured for the given setting {@link #getName()}.
	 */
	public Object getValue() {
		return _value;
	}

	/**
	 * The raw configuration value for this setting.
	 * 
	 * @throws IllegalStateException
	 *         If the setting has not yet been initialized, or the setting has no primitive type.
	 */
	public String getRawValue() {
		if (!_initialized) {
			throw new IllegalStateException("No value available for uninitialized setting '" + getName() + "'.");
		}

		@SuppressWarnings("rawtypes")
		ConfigurationValueProvider valueProvider = valueProvider();
		if (valueProvider == null) {
			return null;
		}

		@SuppressWarnings("unchecked")
		String result = valueProvider.getSpecification(_value);
		return result;
	}

	/**
	 * The format of a primitive setting.
	 */
	@SuppressWarnings({ "unchecked" })
	private ConfigurationValueProvider<Object> valueProvider() {
		return property().getValueProvider();
	}

	/**
	 * The type of this setting.
	 */
	public Class<?> getType() {
		return property().getType();
	}

	private PropertyDescriptor property() {
		return getConfig().descriptor().getProperty(Config.VALUE);
	}

	/**
	 * Initializes this setting by evaluating a potential {@link Config#getExpr() expression}.
	 * 
	 * @param log
	 *        For error reporting.
	 * @param settings
	 *        All settings that could be referred to during initialization.
	 */
	protected void init(Log log, ThemeSettings settings) {
		if (_initialized) {
			// Has already a concrete value.
			return;
		}
		try {
			String expr = getConfig().getExpr();

			if (getConfig().getValue() == null && expr != null) {
				if (_initializing) {
					log.error("Cyclic reference in property '" + getName() + "'.");
					return;
				}

				_initializing = true;
				initValue(log, expand(log, settings, expr));
			}
		} finally {
			_initializing = false;
			_initialized = true;
		}
	}

	private String expand(Log log, ThemeSettings settings, String expr) {
		Matcher matcher = REF_PATTERN.matcher(expr);
		int pos = 0;
		if (matcher.find()) {
			StringBuffer buffer = new StringBuffer();
			do {
				buffer.append(expr, pos, matcher.start());
	
				String key = matcher.group(1);
				ThemeSetting other = settings.get(key);
				if (other == null) {
					log.error("Reference to undefined setting '" + key + "' in '" + getName() + "'.");
				} else {
					// Make sure that no references are added to the result.
					other.init(log, settings);
	
					String expansion = other.getRawValue();
					if (expansion != null) {
						buffer.append(expansion);
					}
				}
	
				pos = matcher.end();
			} while (matcher.find(pos));
			buffer.append(expr, pos, expr.length());
			return buffer.toString();
		} else {
			return expr;
		}
	}

	private void initValue(Log log, String rawValue) {
		Object value;
		ConfigurationValueProvider<Object> valueProvider = valueProvider();
		if (valueProvider == null) {
			log.error("Property '" + getName() + "' is not primitive and cannot be initialized with an expression.");
			return;
		}
		try {
			value = valueProvider.getValue(getName(), rawValue);
		} catch (ConfigurationException ex) {
			log.error(ex.getMessage());
			return;
		}
		_value = value;
	}

	/**
	 * Creates a new {@link #init(Log, ThemeSettings) uninitialized} copy of this setting.
	 */
	public ThemeSetting copy() {
		if (getConfig().getExpr() == null) {
			// Not dependent on the context, can be shared.
			return this;
		} else {
			return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(getConfig())
				.initThemeId(_themeId);
		}
	}

	/**
	 * ID of the {@link Theme} that defines this setting.
	 * 
	 * @see Theme#getThemeID()
	 */
	public String getThemeId() {
		return _themeId;
	}

	/**
	 * Initializes {@link #getThemeId()}
	 */
	@FrameworkInternal
	public ThemeSetting initThemeId(String themeId) {
		assert _themeId == null : "Duplicate initialization of theme Id.";
		_themeId = themeId;
		return this;
	}

	/**
	 * Apply the definition of this setting.
	 * 
	 * @param log
	 *        For error reporting.
	 * @param originalSource
	 *        Where the base setting was defined.
	 * @param original
	 *        The base setting, this one is overriding.
	 */
	protected void checkOverride(Log log, String originalSource, ThemeSetting original) {
		if (!original.getType().isAssignableFrom(getType())) {
			// Invalid override.
			String location = originalSource != null ? " in '" + originalSource + "'" : " of base theme";
			log.error("Invalid override of setting '" + original.getName() + "'" + location + ": Was defined as '"
				+ original.getType().getSimpleName() + "', overridden as '"
				+ getType().getSimpleName() + "'.");
		}
	}

	@Override
	public boolean equals(Object object) {
		if (object != null) {
			if (object instanceof ThemeSetting) {
				ThemeSetting other = (ThemeSetting) object;
				return getName().equals(other.getName()) && CollectionUtil.equals(getThemeId(), other.getThemeId());
			}
		}

		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		String name = getName();
		result = prime * result;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/**
	 * The serialized value to be used in CSS.
	 * 
	 * <p>
	 * Note: Must only be called for settings that are {@link #isCssRelevant() CSS-relevant}.
	 * </p>
	 */
	public String getCssValue() {
		return getFormat().getSpecification(getValue());
	}

	private ConfigurationValueProvider<Object> getFormat() {
		PropertyDescriptor valueProperty = getConfig().descriptor().getProperty(Config.VALUE);
		@SuppressWarnings("unchecked")
		ConfigurationValueProvider<Object> valueProvider = valueProperty.getValueProvider();
		return valueProvider;
	}

	/**
	 * Guard for a non-existing setting.
	 */
	public static final ThemeSetting NONE = new ThemeSetting(null, null, null) {
		@Override
		public String getRawValue() {
			return null;
		}

		@Override
		protected void init(Log log, ThemeSettings settings) {
			// Ignore.
		}

		@Override
		public ThemeSetting copy() {
			return this;
		}

	};

	private String _themeId;

	/**
	 * A generic string value that does not impose any constraints on its value.
	 */
	public static final class StringSetting extends ThemeSetting {

		/**
		 * Configuration options for {@link ThemeSetting.StringSetting}.
		 */
		@TagName("string")
		public interface Config extends ThemeSetting.Config<StringSetting> {
			/**
			 * A plain string without {@link ThemeSetting#REF_EXPR variable expressions}.
			 */
			String NO_VAR_PATTERN =
				"(?:" + "[^" + EXPR_START + "]+" + "(?:" + EXPR_START + "(?!" + EXPR_END + ")" + ")?" + ")*";

			@Override
			@RegexpConstraint(NO_VAR_PATTERN)
			String getValue();

			/** @see #getValue() */
			void setValue(String value);
		}

		/**
		 * Creates a {@link StringSetting} from configuration.
		 */
		@CalledByReflection
		public StringSetting(InstantiationContext context, Config config) {
			super(context, config, config.getValue());
		}

		/**
		 * Creates a new {@link StringSetting}.
		 */
		public static StringSetting newInstance(String themeId, String key, String value) {
			Config config = TypedConfiguration.newConfigItem(StringSetting.Config.class);
			config.setName(key);
			config.setValue(value);
			StringSetting result = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
			result.initThemeId(themeId);
			return result;
		}

		/**
		 * Creates a new {@link StringSetting} that evaluates the given theme expression.
		 */
		public static ThemeSetting newExpressionInstance(String key, String expr) {
			Config config = TypedConfiguration.newConfigItem(StringSetting.Config.class);
			config.setName(key);
			config.setExpr(expr);
			return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
		}
	}

	/**
	 * A {@link Color} value.
	 */
	public static final class ColorSetting extends ThemeSetting {
		/**
		 * Configuration options for {@link ThemeSetting.ColorSetting}.
		 */
		@TagName("color")
		public interface Config extends ThemeSetting.Config<ColorSetting> {
			@Override
			@Format(ColorConfigFormat.class)
			@ControlProvider(ColorControlProvider.class)
			Color getValue();
		}

		/**
		 * Creates a {@link ColorSetting} from configuration.
		 */
		@CalledByReflection
		public ColorSetting(InstantiationContext context, Config config) {
			super(context, config, config.getValue());
		}
	}

	/**
	 * An {@link Integer} value.
	 */
	public static final class IntSetting extends ThemeSetting {

		/**
		 * Configuration options for {@link ThemeSetting.IntSetting}.
		 */
		@TagName("int")
		public interface Config extends ThemeSetting.Config<IntSetting> {
			@Name(VALUE)
			@Override
			Integer getValue();
		}

		/**
		 * Creates a {@link IntSetting} from configuration.
		 */
		@CalledByReflection
		public IntSetting(InstantiationContext context, Config config) {
			super(context, config, config.getValue());
		}
	}

	/**
	 * An {@link Float} value.
	 */
	public static final class FloatSetting extends ThemeSetting {

		/**
		 * Configuration options for {@link ThemeSetting.FloatSetting}.
		 */
		@TagName("float")
		public interface Config extends ThemeSetting.Config<FloatSetting> {
			@Name(VALUE)
			@Override
			Float getValue();
		}

		/**
		 * Creates a {@link FloatSetting} from configuration.
		 */
		@CalledByReflection
		public FloatSetting(InstantiationContext context, Config config) {
			super(context, config, config.getValue());
		}
	}

	/**
	 * A dimension e.g. <code>42em</code>.
	 */
	public static final class DimSetting extends ThemeSetting {

		/**
		 * Configuration options for {@link ThemeSetting.DimSetting}.
		 */
		@TagName("dim")
		public interface Config extends ThemeSetting.Config<DimSetting> {
			/**
			 * Regular expression for an integer.
			 */
			String INT_PATTERN = "-?[1-9][0-9]*|0";

			/**
			 * Regular expression describing {@link #getValue()}.
			 */
			String DIM_PATTERN =
				"(?:" + INT_PATTERN + ")" + "(?:" + "cm|mm|in|px|pt|pc|em|ex|ch|rem|vw|vh|vmin|vmax|%" + ")?";

			@Override
			@RegexpConstraint(value = DIM_PATTERN)
			String getValue();
		}

		/**
		 * Creates a {@link DimSetting} from configuration.
		 */
		@CalledByReflection
		public DimSetting(InstantiationContext context, Config config) {
			super(context, config, config.getValue());
		}
	}

	/**
	 * A size e.g. <code>42px</code> or <code>50%</code>.
	 * 
	 * <p>
	 * This is a specialized form of a {@link DimSetting} that only allows pixel or percentage as
	 * unit.
	 * </p>
	 */
	public static final class SizeSetting extends ThemeSetting {

		/**
		 * Configuration options for {@link ThemeSetting.SizeSetting}.
		 */
		@TagName("size")
		public interface Config extends ThemeSetting.Config<SizeSetting> {
			@Override
			DisplayDimension getValue();
		}

		/**
		 * Creates a {@link SizeSetting} from configuration.
		 */
		@CalledByReflection
		public SizeSetting(InstantiationContext context, Config config) {
			super(context, config, config.getValue());
		}
	}

	/**
	 * A margin or padding value e.g. <code>3px 5px</code>.
	 */
	public static final class BoxSetting extends ThemeSetting {

		/**
		 * Configuration options for {@link ThemeSetting.BoxSetting}.
		 */
		@TagName("box")
		public interface Config extends ThemeSetting.Config<BoxSetting> {
			/**
			 * Regular expression for (non-optional) white space.
			 */
			String S = "\\s+";

			/**
			 * Regular expression describing {@link #getValue()}.
			 */
			String BOX_PATTERN =
				DimSetting.Config.DIM_PATTERN + "(?:" + S + DimSetting.Config.DIM_PATTERN +
					"(?:" + S + DimSetting.Config.DIM_PATTERN + S + DimSetting.Config.DIM_PATTERN + ")?" + ")?";

			@Override
			@RegexpConstraint(BOX_PATTERN)
			String getValue();
		}

		/**
		 * Creates a {@link BoxSetting} from configuration.
		 */
		@CalledByReflection
		public BoxSetting(InstantiationContext context, Config config) {
			super(context, config, config.getValue());
		}
	}

	/**
	 * A {@link Boolean} value.
	 */
	public static final class BooleanSetting extends ServerSetting {

		/**
		 * Configuration options for {@link ThemeSetting.BooleanSetting}.
		 */
		@TagName("boolean")
		public interface Config extends ThemeSetting.Config<BooleanSetting> {
			@Override
			@Name(VALUE)
			Boolean getValue();
		}

		/**
		 * Creates a {@link BooleanSetting} from configuration.
		 */
		@CalledByReflection
		public BooleanSetting(InstantiationContext context, Config config) {
			super(context, config, config.getValue());
		}
	}

	/**
	 * A {@link ThemeImage} value.
	 */
	public static final class IconSetting extends ServerSetting {

		/**
		 * Configuration options for {@link ThemeSetting.IconSetting}.
		 */
		@TagName("icon")
		public interface Config extends ThemeSetting.Config<IconSetting> {
			@Override
			@Format(ThemeImageConfigFormat.class)
			ThemeImage getValue();

			/**
			 * @see #getValue()
			 */
			void setValue(ThemeImage value);
		}

		/**
		 * Creates a {@link IconSetting} from configuration.
		 */
		@CalledByReflection
		public IconSetting(InstantiationContext context, Config config) {
			super(context, config, config.getValue());
		}

		/**
		 * Creates a new {@link IconSetting} programatically.
		 */
		@FrameworkInternal
		public static ThemeSetting newInstance(String name, ThemeImage value) {
			Config config = TypedConfiguration.newConfigItem(Config.class);
			config.setName(name);
			config.setValue(value);
			return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
		}
	}

	/**
	 * A theme resource {@link ThemeImage} value.
	 */
	public static final class ImgSetting extends ServerSetting {

		/**
		 * Configuration options for {@link ThemeSetting.IconSetting}.
		 */
		@TagName("img")
		public interface Config extends ThemeSetting.Config<ImgSetting> {
			@Override
			@Format(ImgFormat.class)
			ThemeImage.Img getValue();

			/**
			 * {@link ConfigurationValueProvider} for {@link ThemeImage} that only accepts resource
			 * icons.
			 */
			class ImgFormat extends AbstractConfigurationValueProvider<ThemeImage.Img> {

				/**
				 * Singleton {@link ThemeSetting.ImgSetting.Config.ImgFormat} instance.
				 */
				public static final ImgFormat INSTANCE = new ImgFormat();

				private ImgFormat() {
					super(ThemeImage.Img.class);
				}

				@Override
				protected ThemeImage.Img getValueNonEmpty(String propertyName, CharSequence propertyValue)
						throws ConfigurationException {
					String value = propertyValue.toString();
					return ThemeImage.internalImg(value);
				}

				@Override
				protected String getSpecificationNonNull(ThemeImage.Img configValue) {
					return configValue.toEncodedForm();
				}
			}
		}

		/**
		 * Creates a {@link IconSetting} from configuration.
		 */
		@CalledByReflection
		public ImgSetting(InstantiationContext context, Config config) {
			super(context, config, config.getValue());
		}
	}

	/**
	 * {@link ThemeSetting} only relevant on the server.
	 * 
	 * @see #isCssRelevant()
	 */
	public abstract static class ServerSetting extends ThemeSetting {

		/**
		 * Creates a {@link ServerSetting}.
		 */
		public ServerSetting(InstantiationContext context, Config<?> config, Object value) {
			super(context, config, value);
		}

		@Override
		public boolean isCssRelevant() {
			return false;
		}

	}

	/**
	 * A {@link HTMLTemplateFragment template} theme resource.
	 */
	public static class TemplateSetting extends ServerSetting {
		/**
		 * Configuration options for {@link ThemeSetting.IconSetting}.
		 */
		@TagName("template")
		public interface Config extends ThemeSetting.Config<TemplateSetting> {
			@Override
			HTMLTemplateFragment getValue();
		}

		/**
		 * Creates a {@link TemplateSetting} from configuration.
		 */
		@CalledByReflection
		public TemplateSetting(InstantiationContext context, Config config) {
			super(context, config, config.getValue());
		}
	}

	/**
	 * Base class for setting that are configured with implementation classes.
	 */
	protected static abstract class ImplSetting extends ServerSetting {

		private Class<?> _interfaceType;

		/**
		 * Configuration options for {@link ThemeSetting.ImplSetting}.
		 */
		@Abstract
		public interface Config<T extends ThemeSetting.ImplSetting> extends ThemeSetting.Config<T> {
			/**
			 * The interface type, the {@link #getValue() given value} must be compatible with.
			 */
			@Hidden
			@Format(ClassFormat.class)
			Class<?> getInterface();
		}

		/**
		 * Creates a {@link ImplSetting} from configuration.
		 */
		@CalledByReflection
		public ImplSetting(InstantiationContext context, Config<?> config, Object value) {
			super(context, config, value);

			_interfaceType = config.getInterface();

			if (_interfaceType != null) {
				checkValue(context);
			}
		}

		@Override
		public Class<?> getType() {
			return getInterfaceType() != null ? getInterfaceType() : Object.class;
		}

		/**
		 * The implementation interface that is required.
		 */
		public final Class<?> getInterfaceType() {
			return _interfaceType;
		}

		private void checkValue(Log log) {
			Class<?> valueType = valueType();
			if (valueType != null) {
				if (!_interfaceType.isAssignableFrom(valueType)) {
					log.error("Invalid value '" + valueType.getName() + "' in setting '" + getName()
					+ "', expected a subtype of '" + _interfaceType + "'.");
				}
			}
		}

		/**
		 * The type of {@link #getValue()} (for checking against {@link #getInterfaceType()}).
		 */
		protected abstract Class<?> valueType();

		@Override
		protected void checkOverride(Log log, String originalSource, ThemeSetting original) {
			if (original instanceof ImplSetting) {
				Class<?> originalInterface = ((ImplSetting) original).getInterfaceType();
				if (originalInterface != null) {
					Class<?> myInterface = getInterfaceType();
					if (myInterface != null) {
						if (!originalInterface.isAssignableFrom(myInterface)) {
							log.error("The interface '" + myInterface.getName() + "' of setting '" + getName()
								+ "' is not compatible with inherited interface '" + originalInterface.getName()
								+ "'.");
						}
					} else {
						// Note: A settings override is not required to repeat the interface
						// declaration.
						// This declaration is adjusted from the base setting here.
						_interfaceType = originalInterface;
						checkValue(log);
					}
				}
			}

			// Note: Super check must be last after the interface setting has been inherited.
			super.checkOverride(log, originalSource, original);
		}
	}

	/**
	 * A {@link Class} value.
	 */
	public static final class TypeSetting extends ImplSetting {

		/**
		 * Configuration options for {@link ThemeSetting.TypeSetting}.
		 */
		@TagName("type")
		public interface Config extends ImplSetting.Config<TypeSetting> {
			@Override
			Class<?> getValue();
		}

		/**
		 * Creates a {@link TypeSetting} from configuration.
		 */
		@CalledByReflection
		public TypeSetting(InstantiationContext context, Config config) {
			super(context, config, config.getValue());
		}

		@Override
		protected Class<?> valueType() {
			return (Class<?>) getValue();
		}

	}

	/**
	 * An instance value.
	 */
	public static final class InstanceSetting extends ImplSetting {

		/**
		 * Configuration options for {@link ThemeSetting.InstanceSetting}.
		 */
		@TagName("instance")
		public interface Config extends ImplSetting.Config<InstanceSetting> {
			@Override
			@InstanceFormat
			@Subtypes({})
			@ItemDisplay(ItemDisplayType.VALUE)
			Object getValue();
		}

		/**
		 * Creates a {@link InstanceSetting} from configuration.
		 */
		@CalledByReflection
		public InstanceSetting(InstantiationContext context, Config config) {
			super(context, config, config.getValue());
		}

		@Override
		protected Class<?> valueType() {
			Object value = getValue();
			return value != null ? value.getClass() : null;
		}

	}

}