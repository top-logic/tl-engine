/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.gui;

import java.awt.Color;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.col.Provider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultConfigConstructorScheme;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.basic.IconsBase;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.ThemeImage.Img;
import com.top_logic.layout.form.format.ColorFormat;
import com.top_logic.layout.template.WithProperties;

/**
 * {@link Theme}-local variable.
 * 
 * <p>
 * <b>Note:</b> A theme variable should by declared as pseudo-constant in an {@link IconsBase}
 * subclass.
 * </p>
 * 
 * @param <T>
 *        The value type of the variable content.
 * 
 * @see Theme#getValue(ThemeVar)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ThemeVar<T> implements Provider<T> {

	private final String _name;

	private Class<T> _type;

	/**
	 * Creates a {@link ThemeVar}.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 */
	protected ThemeVar(String name, Class<T> type) {
		_type = type;
		_name = check(name);
	}

	static String check(String name) {
		if (name.indexOf('%') >= 0) {
			throw new IllegalArgumentException("A theme variable name must not contain the '%' char: " + name);
		}
		return name;
	}

	/**
	 * The value type of this variable.
	 */
	public Class<T> getType() {
		return _type;
	}

	/**
	 * The name of the variable under which the value is defined in the theme properties.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * The value to use, if the {@link Theme} does not define a custom value.
	 */
	public abstract T defaultValue();

	/**
	 * Looks up the corresponding value in the current {@link Theme}.
	 */
	@Override
	public T get() {
		return ThemeFactory.getTheme().getValue(this);
	}

	@Override
	public String toString() {
		return getName();
	}

	/**
	 * Creates a {@link ThemeVar} of {@link String} type with the default value <code>null</code>.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 * @deprecated Declare a theme setting in a {@link IconsBase} class.
	 */
	@Deprecated
	public static ThemeVar<String> stringVar(String name) {
		return stringVar(name, null);
	}

	/**
	 * Creates a {@link ThemeVar} of {@link String} type.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 * @param defaultValue
	 *        See {@link #defaultValue()}.
	 * @deprecated Declare a theme setting in a {@link IconsBase} class.
	 */
	@Deprecated
	public static ThemeVar<String> stringVar(String name, String defaultValue) {
		return new StringVar(name, defaultValue);
	}

	/**
	 * Creates a {@link ThemeVar} of {@link Integer} type with the default value <code>0</code>.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 * @deprecated Declare a theme setting in a {@link IconsBase} class.
	 */
	@Deprecated
	public static ThemeVar<Integer> intVar(String name) {
		return intVar(name, 0);
	}

	/**
	 * Creates a {@link ThemeVar} of {@link Integer} type.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 * @param defaultValue
	 *        See {@link #defaultValue()}.
	 * @deprecated Declare a theme setting in a {@link IconsBase} class.
	 */
	@Deprecated
	public static ThemeVar<Integer> intVar(String name, int defaultValue) {
		return new IntVar(name, Integer.valueOf(defaultValue));
	}

	/**
	 * Creates a {@link ThemeVar} of {@link Float} type with default value <code>0.0F</code>.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 * @deprecated Declare a theme setting in a {@link IconsBase} class.
	 */
	@Deprecated
	public static ThemeVar<Float> floatVar(String name) {
		return floatVar(name, 0.0f);
	}

	/**
	 * Creates a {@link ThemeVar} of {@link Float} type.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 * @param defaultValue
	 *        See {@link #defaultValue()}.
	 * @deprecated Declare a theme setting in a {@link IconsBase} class.
	 */
	@Deprecated
	public static ThemeVar<Float> floatVar(String name, float defaultValue) {
		return new FloatVar(name, Float.valueOf(defaultValue));
	}

	/**
	 * Creates a {@link ThemeVar} of {@link Boolean} type with default value <code>false</code>.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 * @deprecated Declare a theme setting in a {@link IconsBase} class.
	 */
	@Deprecated
	public static ThemeVar<Boolean> booleanVar(String name) {
		return booleanVar(name, Boolean.FALSE);
	}

	/**
	 * Creates a {@link ThemeVar} of {@link Boolean} type.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 * @param defaultValue
	 *        See {@link #defaultValue()}.
	 * @deprecated Declare a theme setting in a {@link IconsBase} class.
	 */
	@Deprecated
	public static ThemeVar<Boolean> booleanVar(String name, boolean defaultValue) {
		return new BooleanVar(name, Boolean.valueOf(defaultValue));
	}

	/**
	 * Creates a {@link ThemeVar} of {@link Color} type with default value <code>null</code>.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 * @deprecated Declare a theme setting in a {@link IconsBase} class.
	 */
	@Deprecated
	public static ThemeVar<Color> colorVar(String name) {
		return colorVar(name, (Color) null);
	}

	/**
	 * Creates a {@link ThemeVar} of {@link Color} type.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 * @param defaultValue
	 *        See {@link #defaultValue()}.
	 * @deprecated Declare a theme setting in a {@link IconsBase} class.
	 */
	@Deprecated
	public static ThemeVar<Color> colorVar(String name, Color defaultValue) {
		return new ColorVar(name, defaultValue);
	}

	/**
	 * Creates a {@link ThemeVar} of {@link ThemeImage} type.
	 *
	 * @param name
	 *        See {@link #getName()}.
	 * @deprecated Declare a theme setting in a {@link IconsBase} class.
	 */
	@Deprecated
	public static ThemeVar<ThemeImage> iconVar(String name) {
		return genericVar(name, ThemeImage.class, null);
	}

	/**
	 * Creates a {@link ThemeVar} of {@link Img} type.
	 *
	 * @param name
	 *        See {@link #getName()}.
	 * @deprecated Declare a theme setting in a {@link IconsBase} class.
	 */
	@Deprecated
	public static ThemeVar<ThemeImage.Img> imgVar(String name) {
		return genericVar(name, ThemeImage.Img.class, null);
	}

	/**
	 * Creates a {@link ThemeVar} of {@link Color} type.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 * @param defaultValue
	 *        The textual representation of the default value, see {@link #defaultValue()}.
	 *        <code>null</code> or empty means a default value of <code>null</code>.
	 * @deprecated Declare a theme setting in a {@link IconsBase} class.
	 */
	@Deprecated
	public static ThemeVar<Color> colorVar(String name, String defaultValue) {
		return new ColorVar(name, StringServices.isEmpty(defaultValue) ? null
			: ColorFormat.parseColor(defaultValue));
	}

	/**
	 * Creates a {@link ThemeVar} of {@link DisplayDimension} type with default value
	 * <code>null</code>.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 * @deprecated Declare a theme setting in a {@link IconsBase} class.
	 */
	@Deprecated
	public static ThemeVar<DisplayDimension> dimVar(String name) {
		return dimVar(name, null);
	}

	/**
	 * Creates a {@link ThemeVar} of {@link DisplayDimension} type.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 * @param defaultValue
	 *        See {@link #defaultValue()}.
	 * @deprecated Declare a theme setting in a {@link IconsBase} class.
	 */
	@Deprecated
	public static ThemeVar<DisplayDimension> dimVar(String name, DisplayDimension defaultValue) {
		return new DimensionVar(name, defaultValue);
	}

	/**
	 * Creates a {@link ThemeVar} for a {@link HTMLTemplateFragment template}.
	 *
	 * @param name
	 *        See {@link #getName()}.
	 * @param modelType
	 *        The type of object that is expected to be passed to the template for rendering.
	 * @param defaultValue
	 *        See {@link #defaultValue()}.
	 */
	@FrameworkInternal
	public static ThemeVar<HTMLTemplateFragment> templateVar(String name, Class<? extends WithProperties> modelType,
			HTMLTemplateFragment defaultValue) {
		return new TemplateVar(name, modelType, defaultValue);
	}

	/**
	 * Creates a {@link ThemeVar} of {@link Object} type with default value <code>null</code>.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 * @param type
	 *        The expected super-type of the class value configured as value.
	 * @deprecated Declare a theme setting in a {@link IconsBase} class.
	 */
	@Deprecated
	public static <T> ThemeVar<T> instanceVar(String name, Class<T> type) {
		return instanceVar(name, type, null);
	}

	/**
	 * Creates a {@link ThemeVar} of {@link Object} type.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 * @param type
	 *        The expected super-type of the class value configured as value.
	 * @param defaultValue
	 *        See {@link #defaultValue()}.
	 * @deprecated Declare a theme setting in a {@link IconsBase} class.
	 */
	@Deprecated
	public static <T> ThemeVar<T> instanceVar(String name, Class<T> type, T defaultValue) {
		return new InstanceVarWithInstanceDefault<>(name, type, defaultValue);
	}
	
	/**
	 * Creates a {@link ThemeVar} of {@link Object} type.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 * @param type
	 *        The expected super-type of the class value configured as value.
	 * @param defaultValue
	 *        The class to instantiate, if no custom value is defined by the {@link Theme}, see
	 *        {@link #defaultValue()}.
	 * @deprecated Declare a theme setting in a {@link IconsBase} class.
	 */
	@Deprecated
	public static <T> ThemeVar<T> instanceVarWithClassDefault(String name, Class<T> type,
			Class<? extends T> defaultValue) {
		return new InstanceVarWithClassDefault<>(name, type, defaultValue);
	}

	/**
	 * Creates a {@link ThemeVar} of {@link Class} type with default value <code>null</code>.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 * @param type
	 *        The expected super-type of the class value configured as value.
	 * @deprecated Declare a theme setting in a {@link IconsBase} class.
	 */
	@Deprecated
	public static <T> ThemeVar<Class<? extends T>> classVar(String name, Class<T> type) {
		return classVar(name, type, null);
	}

	/**
	 * Creates a {@link ThemeVar} of {@link Class} type.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 * @param type
	 *        The expected super-type of the class value configured as value.
	 * @param defaultValue
	 *        See {@link #defaultValue()}.
	 * @deprecated Declare a theme setting in a {@link IconsBase} class.
	 */
	@Deprecated
	public static <T> ThemeVar<Class<? extends T>> classVar(String name, Class<T> type,
			Class<? extends T> defaultValue) {
		return new ClassVar<>(name, type, defaultValue);
	}

	/**
	 * Creates a generic {@link ThemeVar} of given type.
	 *
	 * @param name
	 *        See {@link #getName()}.
	 * @param type
	 *        The type of the theme setting to access. Supported types are {@link String},
	 *        {@link Integer}, {@link Float}, {@link ThemeImage}, {@link Color}, {@link Class}.
	 */
	@FrameworkInternal
	public static <T> ThemeVar<T> genericVar(String name, Class<T> type, T defaultValue) {
		return new StaticDefaultVar<>(name, type, defaultValue);
	}

	private static class StaticDefaultVar<T> extends ThemeVar<T> {

		private final T _defaultValue;

		public StaticDefaultVar(String name, Class<T> type, T defaultValue) {
			super(name, type);
			_defaultValue = defaultValue;
		}

		@Override
		public T defaultValue() {
			return _defaultValue;
		}
	}

	private static class StringVar extends StaticDefaultVar<String> {

		public StringVar(String name, String defaultValue) {
			super(name, String.class, defaultValue);
		}

	}

	private static class IntVar extends StaticDefaultVar<Integer> {

		public IntVar(String name, Integer defaultValue) {
			super(name, Integer.class, defaultValue);
		}

	}

	private static class FloatVar extends StaticDefaultVar<Float> {

		public FloatVar(String name, Float defaultValue) {
			super(name, Float.class, defaultValue);
		}

	}

	private static class BooleanVar extends StaticDefaultVar<Boolean> {

		public BooleanVar(String name, Boolean defaultValue) {
			super(name, Boolean.class, defaultValue);
		}

	}

	private static class ColorVar extends StaticDefaultVar<Color> {

		public ColorVar(String name, Color defaultValue) {
			super(name, Color.class, defaultValue);
		}

	}

	private static class DimensionVar extends StaticDefaultVar<DisplayDimension> {

		public DimensionVar(String name, DisplayDimension defaultValue) {
			super(name, DisplayDimension.class, defaultValue);
		}

	}

	/**
	 * A {@link ThemeVar} containing a rendering template.
	 */
	@FrameworkInternal
	public static class TemplateVar extends ThemeVar<HTMLTemplateFragment> {
		private HTMLTemplateFragment _defaultValue;

		private Class<? extends WithProperties> _modelType;

		/**
		 * Creates a {@link TemplateVar}.
		 *
		 * @param name
		 *        See {@link #getName()}.
		 * @param modelType
		 *        See {@link #getModelType()}.
		 * @param defaultValue
		 *        See {@link #defaultValue()}.
		 */
		public TemplateVar(String name, Class<? extends WithProperties> modelType, HTMLTemplateFragment defaultValue) {
			super(name, HTMLTemplateFragment.class);
			_modelType = modelType;
			_defaultValue = defaultValue;
		}

		/**
		 * The type of object that is used as model for the rendered template.
		 */
		public Class<? extends WithProperties> getModelType() {
			return _modelType;
		}

		@Override
		public HTMLTemplateFragment defaultValue() {
			return _defaultValue;
		}
	}

	private abstract static class AbstractClassVar<T, V> extends ThemeVar<V> {

		public AbstractClassVar(String name, Class<V> type) {
			super(name, type);
		}
	}

	private abstract static class AbstractInstanceVar<T> extends AbstractClassVar<T, T> {
		public AbstractInstanceVar(String name, Class<T> type) {
			super(name, type);
		}

		protected final T createInstance(Class<?> clazz) {
			try {
				@SuppressWarnings("unchecked")
				T result = (T) DefaultConfigConstructorScheme.getFactory(clazz).createDefaultInstance();
				return result;
			} catch (ConfigurationException ex) {
				Logger.error("Invalid configured class referenced in theme variable '" + getName() + "' not found.",
					ex, ThemeVar.class);
				return defaultValue();
			}
		}
	}

	private static class InstanceVarWithClassDefault<T> extends AbstractInstanceVar<T> {

		private final Class<? extends T> _defaultType;

		public InstanceVarWithClassDefault(String name, Class<T> type, Class<? extends T> defaultType) {
			super(name, type);
			_defaultType = defaultType;
		}

		@Override
		public T defaultValue() {
			return createInstance(_defaultType);
		}
	}

	private static class InstanceVarWithInstanceDefault<T> extends AbstractInstanceVar<T> {

		private final T _defaultValue;

		public InstanceVarWithInstanceDefault(String name, Class<T> type, T defaultValue) {
			super(name, type);
			_defaultValue = defaultValue;
		}

		@Override
		public T defaultValue() {
			return _defaultValue;
		}
	}

	private static class ClassVar<T> extends AbstractClassVar<T, Class<? extends T>> {

		private final Class<? extends T> _defaultValue;

		public ClassVar(String name, Class<T> type, Class<? extends T> defaultValue) {
			super(name, classType(type));
			_defaultValue = defaultValue;
		}

		private static <T> Class<T> classType(Class<?> type) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Class<T> result = (Class) type.getClass();
			return result;
		}

		@Override
		public Class<? extends T> defaultValue() {
			return _defaultValue;
		}

	}
}
