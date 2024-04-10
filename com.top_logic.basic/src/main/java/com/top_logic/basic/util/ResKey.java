/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.col.TupleFactory;
import com.top_logic.basic.config.AbstractConfigurationValueBinding;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationValueBinding;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.LocaleValueProvider;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Binding;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.text.TLMessageFormat;
import com.top_logic.basic.xml.XMLStreamUtil;

/**
 * Representation of a resource key.
 * 
 * @see I18NBundle#getString(ResKey)
 * @see I18NConstantsBase
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Format(ResKey.ValueFormat.class)
@Binding(ResKey.ValueBinding.class)
@Label("Resource key")
public abstract class ResKey {

	/** {@link ResKey} suffix to get derived tooltip resource. */
	public static final String TOOLTIP = ".tooltip";

	static final LiteralText NO_TEXT = new LiteralText(null);

	private static final LiteralText EMPTY_TEXT = new LiteralText("");

	/**
	 * The {@link ResKey} that must not be resolved.
	 * 
	 * <p>
	 * Use this as assertion that a certain key is never accessed.
	 * </p>
	 */
	public static final ResKey NONE = none("", null);

	/**
	 * @see #message(ResKey, Object...)
	 * @see #text(String)
	 */
	ResKey() {
		// Must only be created by factory methods, or I18NConstants classes.
	}

	/**
	 * Whether {@link #getKey()} returns a valid key.
	 */
	@FrameworkInternal
	public abstract boolean hasKey();

	/**
	 * The internal key representation if {@link #hasKey()}. Undefined otherwise.
	 */
	@FrameworkInternal
	public abstract String getKey();

	/**
	 * Encodes this {@link ResKey} into an external form.
	 * 
	 * @see #decode(String)
	 */
	protected abstract String internalEncode();

	/**
	 * Resolves this key relative to a given {@link ResourceBundle}.
	 * 
	 * @param bundle
	 *        The context bundle to use for resource resolution.
	 * @param origKey
	 *        The initially resolved key (e.g. if a key chain with fall-back keys is being resolved,
	 *        the original key is the key that was tried to resolve first.)
	 * @param withFallbackBundle
	 *        Whether the key must resolve relative to the {@link I18NBundleSPI#getFallback()
	 *        fallback bundle} if no value can be found.
	 */
	@FrameworkInternal
	public final String resolve(I18NBundleSPI bundle, ResKey origKey, boolean withFallbackBundle) {
		DirectKey directKey = directInternal();
		ResKey defaultKey = fallback();

		String value = directKey.lookupDirect(bundle, origKey, withFallbackBundle);
		if (value != null || !directKey.nullMeansMissing(bundle, origKey)) {
			return value;
		} else if (defaultKey != null) {
			return defaultKey.resolve(bundle, origKey, withFallbackBundle);
		} else if (!withFallbackBundle) {
			return value;
		} else {
			bundle.handleUnknownKey(origKey);
			return origKey.unknown(bundle);
		}
	}

	@Override
	public String toString() {
		return ResKey.encode(this);
	}

	/**
	 * The plain key (without potential arguments).
	 */
	public abstract ResKey plain();
	
	/**
	 * The key to resolve excluding any fall-backs.
	 */
	public final ResKey direct() {
		return directInternal();
	}

	/**
	 * Implementation of {@link #direct()}.
	 */
	abstract DirectKey directInternal();

	/**
	 * The key to resolve, if no resources are found for this key.
	 * 
	 * @return The fall-back key, or <code>null</code>, if this key has no fall-back.
	 */
	public abstract ResKey fallback();

	/**
	 * Creates a key chain, where the given key is the {@link #fallback()} of the this one.
	 * 
	 * @param fallback
	 *        The key to use, if no resources is found for this one. No fall-backs are resolved, if
	 *        the given fall-back key is <code>null</code>.
	 * @return A new key with {@link #fallback()}, or this one if the given key is null.
	 */
	public ResKey fallback(ResKey fallback) {
		return fallback(this, fallback);
	}

	/**
	 * Potential dynamic arguments to this internationalization.
	 */
	public abstract Object[] arguments();

	/**
	 * Sets or appends the given arguments to the message.
	 * 
	 * <p>
	 * If this key already has arguments, the given arguments are appended to the list of arguments.
	 * </p>
	 * 
	 * @param arguments
	 *        The arguments to pass to the internationalization process.
	 * @return The {@link ResKey} that encapsulates the given arguments.
	 */
	abstract ResKey internalFill(Object... arguments);

	/**
	 * Whether this is a {@link #literal(Iterable)} resource.
	 */
	public boolean isLiteral() {
		return false;
	}

	/**
	 * Builds a complex key with arguments.
	 * 
	 * @deprecated The key should define the concrete number of arguments. See {@link #asResKeyN()}.
	 */
	@Deprecated
	public static ResKey message(ResKey messageKey, Object... arguments) {
		return messageKey.asResKeyN().fill(arguments);
	}

	/**
	 * Builds a complex key with one argument.
	 * 
	 * @deprecated The key should define the concrete number of arguments. See {@link #asResKey1()}.
	 */
	@Deprecated
	public static ResKey message(ResKey messageKey, Object argument) {
		return messageKey.asResKey1().fill(argument);
	}

	/**
	 * Builds a complex key with two arguments.
	 * 
	 * @deprecated The key should define the concrete number of arguments. See {@link #asResKey2()}.
	 */
	@Deprecated
	public static ResKey message(ResKey messageKey, Object arg1, Object arg2) {
		return messageKey.asResKey2().fill(arg1, arg2);
	}

	/**
	 * Builds a complex key with three arguments.
	 * 
	 * @deprecated The key should define the concrete number of arguments. See {@link #asResKey3()}.
	 */
	@Deprecated
	public static ResKey message(ResKey messageKey, Object arg1, Object arg2, Object arg3) {
		return messageKey.asResKey3().fill(arg1, arg2, arg3);
	}

	/**
	 * Builds a complex key with four arguments.
	 * 
	 * @deprecated The key should define the concrete number of arguments. See {@link #asResKey4()}.
	 */
	@Deprecated
	public static ResKey message(ResKey messageKey, Object arg1, Object arg2, Object arg3, Object arg4) {
		return messageKey.asResKey4().fill(arg1, arg2, arg3, arg4);
	}

	/**
	 * Builds a complex key with five arguments.
	 * 
	 * @deprecated The key should define the concrete number of arguments. See {@link #asResKey5()}.
	 */
	@Deprecated
	public static ResKey message(ResKey messageKey, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
		return messageKey.asResKey5().fill(arg1, arg2, arg3, arg4, arg5);
	}

	static ResKey setArguments(ResKey messageKey, Object[] arguments) {
		if (arguments == null || arguments.length == 0) {
			return messageKey;
		}
		return new MessageKey(messageKey.plain(), arguments);
	}

	/**
	 * Encodes literal text as resource key.
	 */
	public static ResKey text(String literalText) {
		if (literalText == null) {
			// Optimization for frequent "default" value during resource lookup.
			return NO_TEXT;
		}
		else if (literalText.isEmpty()) {
			return EMPTY_TEXT;
		}
		else {
			return new LiteralText(literalText);
		}
	}

	/**
	 * A literal {@link ResKey} build from its translations.
	 *
	 * @param strings
	 *        The values in all supported languages.
	 * 
	 * @see #builder()
	 */
	public static ResKey literal(Iterable<? extends LangString> strings) {
		Builder result = ResKey.builder();
		for (LangString string : strings) {
			result.add(string.getLang(), string.getText());
		}
		return result.build();
	}

	/**
	 * A literal {@link ResKey} build from its translations.
	 *
	 * @param strings
	 *        The values in all supported languages.
	 * 
	 * @see #builder()
	 */
	public static ResKey literal(LangString... strings) {
		Builder result = ResKey.builder();
		for (LangString string : strings) {
			result.add(string.getLang(), string.getText());
		}
		return result.build();
	}

	/**
	 * A language tagged string for building {@link #literal(LangString...)} keys.
	 * 
	 * @param lang
	 *        The string's language.
	 * @param value
	 *        The string's values.
	 * @return A string that knows it's language.
	 * 
	 * @see #literal(LangString...)
	 */
	public static LangString langString(Locale lang, String value) {
		return new LangString() {
			@Override
			public Locale getLang() {
				return lang;
			}
	
			@Override
			public String getText() {
				return value;
			}
		};
	}

	/**
	 * Creates a {@link Builder} for replacing the given {@link ResKey} with a new
	 * {@link LiteralKey}.
	 * 
	 * @param literal
	 *        The {@link ResKey} to take the {@link #getKey()} from.
	 * @return A new Builder that builds a {@link ResKey} with the same {@link #getKey()} value as
	 *         the given {@link ResKey}.
	 */
	public static Builder builder(ResKey literal) {
		Builder builder;
		if (literal.hasKey()) {
			builder = builder(literal.getKey());
		} else {
			builder = builder();
		}
		return builder;
	}

	/**
	 * Creates a {@link Builder} for creating a literal {@link ResKey} that contains its
	 * translations.
	 * 
	 * <p>
	 * Note: At least the translation in the {@link ResourcesModule#getFallbackLocale() the system's
	 * fallback locale} must be passed to the resulting builder.
	 * </p>
	 * 
	 * @see #literal(LangString...)
	 */
	public static Builder builder() {
		return builder((String) null);
	}

	/**
	 * Creates a {@link Builder} for creating a literal {@link ResKey} that contains its
	 * translations.
	 * 
	 * <p>
	 * Note: At least the translation in the {@link ResourcesModule#getFallbackLocale() the system's
	 * fallback locale} must be passed to the resulting builder.
	 * </p>
	 * 
	 * @param key
	 *        The {@link #getKey() key} to which the literal translations belong to. This
	 *        information is purely informative and not used for resolving internationalization
	 *        texts.
	 * @see #literal(LangString...)
	 */
	public static Builder builder(String key) {
		return new Builder() {
			Map<Locale, String> _translations = new LinkedHashMap<>();

			Map<String, Builder> _builderBySuffix = Collections.emptyMap();
	
			@Override
			public Builder add(Locale locale, String value) {
				Map<Locale, String> translations = _translations;
				addTranslation(translations, locale, value);
				return this;
			}

			@Override
			public boolean has(Locale locale) {
				return _translations.containsKey(locale);
			}

			@Override
			public String get(Locale locale) {
				return _translations.get(locale);
			}

			private void addTranslation(Map<Locale, String> translations, Locale locale, String value) {
				translations.put(locale, value);

				// Fill fallback locales with the same value to allow looking up the value with more
				// general locales.
				while (true) {
					locale = fallback(locale);
					if (locale == null) {
						break;
					}
					if (translations.get(locale) != null) {
						break;
					}
					translations.put(locale, value);
				}
			}

			private Locale fallback(Locale locale) {
				if (!locale.getVariant().isEmpty()) {
					return new Locale(locale.getLanguage(), locale.getCountry());
				}
				if (!locale.getCountry().isEmpty()) {
					return new Locale(locale.getLanguage());
				}
				return null;
			}

			@Override
			public Builder suffix(String suffix) {
				if (StringServices.isEmpty(suffix)) {
					return this;
				}
				String suffixWithoutDot;
				if (suffix.charAt(0) == '.') {
					suffixWithoutDot = suffix.substring(1);
				} else {
					suffixWithoutDot = suffix;
				}
				Builder suffixBuilder = _builderBySuffix.get(suffixWithoutDot);
				if (suffixBuilder != null) {
					return suffixBuilder;
				}
				String keyWithSuffix;
				if (StringServices.isEmpty(key)) {
					keyWithSuffix = suffixWithoutDot;
				} else if (suffix.charAt(0) == '.') {
					keyWithSuffix = key + suffix;
				} else {
					keyWithSuffix = key + '.' + suffix;
				}
				suffixBuilder = builder(keyWithSuffix);
				if (_builderBySuffix.isEmpty()) {
					_builderBySuffix = new HashMap<>();
				}
				_builderBySuffix.put(suffixWithoutDot, suffixBuilder);
				return suffixBuilder;
			}

			@Override
			public LiteralKey build() {
				if (_builderBySuffix.isEmpty()) {
					if (_translations.isEmpty()) {
						return null;
					} else {
						return new LiteralKey(key, _translations, Collections.emptyMap());
					}
				}
				Map<String, LiteralKey> suffixKeys = new HashMap<>(_builderBySuffix.size());
				for (Entry<String, Builder> entry : _builderBySuffix.entrySet()) {
					LiteralKey suffixKey = (LiteralKey) entry.getValue().build();
					if (suffixKey == null) {
						continue;
					}
					suffixKeys.put(entry.getKey(), suffixKey);
				}
				return new LiteralKey(key, _translations, suffixKeys);
			}
		};
	}

	/**
	 * Builder for pseudo {@link ResKey}s that literally contain their translations.
	 */
	public interface Builder {
		/**
		 * Adds the given language tagged string to this builder.
		 * 
		 * @return This {@link Builder} for call chaining.
		 */
		Builder add(Locale locale, String text);

		/**
		 * The translation already added to this builder, <code>null</code> if no translation was
		 * {@link #add(Locale, String) added} for the given locale.
		 */
		String get(Locale locale);

		/**
		 * Whether a translation was already added for the given locale.
		 */
		boolean has(Locale locale);

		/**
		 * Creates a builder for a given suffix.
		 * 
		 * @param suffix
		 *        Name of the suffix to create builder for. If <code>null</code> or empty, this
		 *        builder is returned. The suffix may or may not start with '.', both cases are
		 *        treated equally.
		 * @return A {@link Builder} to create a derived key with the given suffix.
		 */
		Builder suffix(String suffix);

		/**
		 * Creates the {@link ResKey} with translations passed to {@link #add(Locale, String)}
		 * before and suffix keys created with {@link #suffix(String)}.
		 */
		ResKey build();

		/**
		 * Adds all translations from the given {@link LiteralKey}.
		 */
		public default void addAll(LiteralKey literal) {
			for (Entry<Locale, String> entry : literal.getTranslations().entrySet()) {
				add(entry.getKey(), entry.getValue());
			}
			for (Entry<String, LiteralKey> entry : literal._suffixKeys.entrySet()) {
				suffix(entry.getKey()).addAll(entry.getValue());
			}
		}
	}

	/**
	 * A language tagged string.
	 */
	public interface LangString {
		/**
		 * @see #getLang()
		 */
		String LANG = "lang";
	
		/**
		 * The string's {@link Locale}.
		 * 
		 * <p>
		 * Even if this interface is no configuration interface, it is required to have annotations
		 * for the format, to allow creating configuration sub-interfaces.
		 * </p>
		 */
		@Name(LANG)
		@Mandatory
		@Format(LocaleValueProvider.class)
		Locale getLang();
	
		/**
		 * The string's value.
		 */
		String getText();
	
	}

	/**
	 * Creates key chain, where the second key is the {@link #fallback()} of the first.
	 * 
	 * @param key
	 *        The key to resolve first. If <code>null</code>, the fall-back key is used.
	 * @param fallback
	 *        The key to use, if no resources are found for the first one. No fall-backs are
	 *        resolved, if the given fall-back key is <code>null</code>.
	 * @return A new key with {@link #fallback()}, or <code>null</code>, if both keys given are
	 *         <code>null</code>.
	 */
	public static ResKey fallback(ResKey key, ResKey fallback) {
		if (key == null) {
			return fallback;
		}
		if (key instanceof LiteralText) {
			return key;
		}
		if (fallback instanceof NoKey) {
			return key;
		}
		if (fallback == null) {
			return key;
		}
		if (key instanceof NoKey) {
			return fallback;
		}
		return new FallbackKey(key.directInternal(), fallback(key.fallback(), fallback));
	}

	/**
	 * Marks a resource key as deprecated.
	 * 
	 * <p>
	 * A deprecated key is not listed in the debug display to make it less probable that is it newly
	 * inserted into the resources.
	 * </p>
	 * 
	 * @param key
	 *        The key to deprecate.
	 * @return The key that resolves to the same as the given key but that is hidden from resource
	 *         debugging.
	 */
	public static ResKey deprecated(ResKey key) {
		if (key == null) {
			return null;
		}
		if (key instanceof DeprecatedKey) {
			return key;
		}
		
		ResKey directDeprecated = new DeprecatedKey(key.directInternal());
		if (key.fallback() != null) {
			// Move the deprecation to the innermost key to ensure that correct deprecations can be
			// reported.
			return fallback(directDeprecated, deprecated(key.fallback()));
		}
		return directDeprecated;
	}

	/**
	 * Encodes the given {@link ResKey} into an external form.
	 * 
	 * @see #decode(String)
	 */
	public static String encode(ResKey resKey) {
		if (resKey == null) {
			return null;
		}

		return resKey.internalEncode();
	}

	/**
	 * Retrieves the {@link ResKey} from the result of {@link #internalEncode()}.
	 */
	public static ResKey decode(String encodedForm) {
		return ResKeyEncoding.decode(encodedForm);
	}

	/**
	 * Derives a suffix key form this {@link ResKey}
	 * 
	 * @param suffix
	 *        The suffix to appen to the internal representation.
	 * @return The new key.
	 */
	public final ResKey suffix(String suffix) {
		if (suffix == null || suffix.isEmpty()) {
			return this;
		}
		
		if (suffix.charAt(0) != '.') {
			suffix = "." + suffix;
		}
		
		return internalSuffix(suffix);
	}

	/**
	 * Derives a "tooltip" key form this {@link ResKey}.
	 * 
	 * @return A {@link ResKey} which {@link #suffix(String) suffixed} with {@value #TOOLTIP}.
	 * 
	 * @see #tooltipOptional() Key that resolves to <code>null</code> if no tooltip can be found.
	 */
	public final ResKey tooltip() {
		return suffix(TOOLTIP);
	}

	/**
	 * Derives an optional "tooltip" key form this {@link ResKey}, i.e. if no value is given for the
	 * tooltip, the key resolves to <code>null</code>.
	 * 
	 * @return A {@link ResKey} which {@link #suffix(String) suffixed} with {@value #TOOLTIP} and
	 *         <code>text(null)</code> as fallback.
	 * 
	 * @see #tooltip()
	 */
	public final ResKey tooltipOptional() {
		return tooltip().fallback(text(null));
	}

	/**
	 * Actually creates a suffix key.
	 * 
	 * @param suffix
	 *        The suffix starting with a '.' character.
	 * @return The suffix. key.
	 */
	protected ResKey internalSuffix(String suffix) {
		return create(getKey() + suffix);
	}

	/**
	 * Internationalization of a Java class name.
	 * 
	 * @param clazz
	 *        The class to internationalize.
	 * @return The {@link ResKey} pointing to the class name.
	 */
	public static ResKey forClass(Class<?> clazz) {
		return create(clazz.getName());
	}

	/**
	 * Internationalization key for a {@link ConfigurationItem}.
	 * 
	 * @param model
	 *        The configuration to internationalize.
	 * @return The {@link ResKey} pointing to the class name.
	 */
	public static ResKey forConfig(ConfigurationItem model) {
		ConfigurationItem config = model;
		if (config instanceof PolymorphicConfiguration<?>) {
			Class<?> implementationClass = ((PolymorphicConfiguration<?>) config).getImplementationClass();
			if (implementationClass != null) {
				return forClass(implementationClass)
					.fallback(forClass(model.descriptor().getConfigurationInterface()));
			} else {
				return forClass(model.descriptor().getConfigurationInterface());
			}
		} else {
			return forClass(model.descriptor().getConfigurationInterface());
		}
	}

	/**
	 * Internationalization of a Java enum constant.
	 * 
	 * @param enumInstance
	 *        The constant to internationalize.
	 * @return The {@link ResKey} pointing to the classifier name.
	 */
	public static ResKey forEnum(Enum<?> enumInstance) {
		ResKey enumKey =
			create(enumInstance.getDeclaringClass().getName().replace('$', '.') + "." + enumInstance.name());
		if (enumInstance instanceof ExternallyNamed) {
			enumKey = fallback(enumKey, text(((ExternallyNamed) enumInstance).getExternalName()));
		}
		return enumKey;
	}

	/**
	 * Creates a {@link ResKey} for test cases.
	 * 
	 * <p>
	 * <b>Note:</b> This method must only be invoked from test code. It is not resolved using
	 * message properties, but always resolves to the string "<code>[key passed to method]</code>".
	 * </p>
	 */
	public static ResKey forTest(String key) {
		return forTest(key, false);
	}

	/**
	 * Creates an unknown {@link ResKey} for test cases.
	 * 
	 * <p>
	 * <b>Note:</b> This method must only be invoked from test code. It is not resolved using
	 * message properties, but always resolves to the string "<code>[key passed to method]</code>".
	 * </p>
	 */
	public static ResKey forTestUnknown(String key) {
		return forTest(key, true);
	}

	/**
	 * Creates a {@link ResKey} for test cases.
	 * 
	 * <p>
	 * <b>Note:</b> This method must only be invoked from test code. It is not resolved using
	 * message properties, but always resolves to the string "<code>[key passed to method]</code>".
	 * </p>
	 * 
	 * @param key
	 *        The plain key.
	 * @param unknown
	 *        Whether the key is expected to actually resolve to some value.
	 */
	public static ResKey forTest(String key, boolean unknown) {
		if (key == null || key.isEmpty()) {
			return null;
		}

		return new TestKey(key, unknown);
	}

	/**
	 * Creates a key that cannot be resolved.
	 * 
	 * @param id
	 *        An identifier that is included in the missing resource message, if the resulting key
	 *        is resolved.
	 * @param local
	 *        A local part of the unresolvable key. This is also included as suffix to the missing
	 *        resource message.
	 * @return An key that produces a missing resource error when being resolved.
	 */
	public static ResKey none(Object id, String local) {
		return new NoKey(id, local);
	}

	/**
	 * Creates a key defined in some JSP.
	 * 
	 * <p>
	 * <b>Note:</b> This method must only be invoked from JSP tag implementations!
	 * </p>
	 */
	@FrameworkInternal
	public static ResKey internalJsp(String key) {
		return create(key);
	}

	/**
	 * Creates a key defined in layout XML.
	 * 
	 * <p>
	 * <b>Note:</b> This method must only be invoked from Layout XML parsing code!
	 * </p>
	 */
	@FrameworkInternal
	public static ResKey internalLayout(String key) {
		return create(key);
	}

	/**
	 * Creates a key defined in the application model.
	 * 
	 * <p>
	 * <b>Note:</b> This method must only be invoked from Layout XML parsing code!
	 * </p>
	 */
	@FrameworkInternal
	public static ResKey internalModel(String key) {
		return create(key);
	}

	/**
	 * Internal access, must not be called by the application.
	 */
	@FrameworkInternal
	public static ResKey internalCreate(String key) {
		return create(key);
	}

	static ResKey create(String key) {
		if (key == null || key.isEmpty()) {
			return null;
		}

		return new PlainKey(key);
	}

	/**
	 * Directly creates a new {@link ResKey} from its internal representation.
	 * 
	 * @param key
	 *        See {@link #getKey()}.
	 * @return The new {@link ResKey}.
	 * 
	 * @deprecated Declare a pseudo constant in an <code>I18NConstants</code> class (preferred), or
	 *             create it locally to a <code>ResPrefix</code> (only if the contents is dynamic).
	 */
	@Deprecated
	public static ResKey legacy(String key) {
		if (key == null || key.isEmpty()) {
			return null;
		}
		return new LegacyKey(key);
	}

	private enum Mode {
		UNKNOWN_KEY,
		INSPECT,
	}

	private enum Level {
		DEFAULT,
		FULL,
		DEPRECATION
	}

	private static class Representation {

		private final Mode _mode;

		private final Level _level;

		private Representation(Mode mode, Level level) {
			_mode = mode;
			_level = level;
		}

		public static Representation representation(Mode mode, Level level) {
			return new Representation(mode, level);
		}

		public Mode mode() {
			return _mode;
		}

		public Level level() {
			return _level;
		}

	}

	static final Object[] NOARGS = new Object[0];

	private static abstract class AbstractResKey extends ResKey
			implements ResKey1, ResKey2, ResKey3, ResKey4, ResKey5, ResKeyN {

		@Override
		public ResKey1 asResKey1() {
			return this;
		}

		@Override
		public ResKey2 asResKey2() {
			return this;
		}

		@Override
		public ResKey3 asResKey3() {
			return this;
		}

		@Override
		public ResKey4 asResKey4() {
			return this;
		}

		@Override
		public ResKey5 asResKey5() {
			return this;
		}

		@Override
		public ResKeyN asResKeyN() {
			return this;
		}

		@Override
		public ResKey fill(Object arg) {
			return internalFill(new Object[] { arg });
		}

		@Override
		public ResKey fill(Object arg1, Object arg2) {
			return internalFill(new Object[] { arg1, arg2 });
		}

		@Override
		public ResKey fill(Object arg1, Object arg2, Object arg3) {
			return internalFill(new Object[] { arg1, arg2, arg3 });
		}

		@Override
		public ResKey fill(Object arg1, Object arg2, Object arg3, Object arg4) {
			return internalFill(new Object[] { arg1, arg2, arg3, arg4 });
		}

		@Override
		public ResKey fill(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
			return internalFill(new Object[] { arg1, arg2, arg3, arg4, arg5 });
		}

		@Override
		public ResKey fill(Object... arguments) {
			return internalFill(arguments);
		}

	}

	/**
	 * A {@link ResKey} without any {@link #fallback()} keys.
	 */
	private static abstract class DirectKey extends AbstractResKey {
		DirectKey() {
			super();
		}
		
		@Override
		final DirectKey directInternal() {
			return this;
		}
		
		@Override
		public final ResKey fallback() {
			return null;
		}

		@Override
		protected String lookupDirect(I18NBundleSPI bundle, ResKey origKey, boolean withFallback) {
			String key = getKey();
			return bundle.lookup(key, withFallback);
		}
		
		/**
		 * Checks whether a null value of this key means that there is no resource for the key..
		 * 
		 * <p>
		 * This method can be used to determine whether the <code>null</code> result of
		 * {@link #lookupDirect(I18NBundleSPI, ResKey, boolean)} means that there is no resource for the key.
		 * </p>
		 * 
		 * @param bundle
		 *        The resource bundle to use.
		 * @param origKey
		 *        The top-most key that is currently being resolved.
		 * @return <code>true</code> iff a null value in
		 *         {@link #lookupDirect(I18NBundleSPI, ResKey, boolean)} means that there is no resource.
		 */
		protected boolean nullMeansMissing(I18NBundleSPI bundle, ResKey origKey) {
			return true;
		}

	}
	
	private static class PlainKey extends DirectKey {

		private final String _key;

		PlainKey(String key) {
			checkFormat(key);

			_key = key;
		}

		private static void checkFormat(String key) {
			if (key == null) {
				throw new IllegalArgumentException("Key must not be null.");
			}
			if (key.isEmpty()) {
				throw new IllegalArgumentException("Key must not be empty.");
			}
			char firstChar = key.charAt(0);
			if (firstChar == '.') {
				throw new IllegalArgumentException("Key must not start with a '.' character: " + key);
			}
			char lastChar = key.charAt(key.length() - 1);
			if (lastChar == '.') {
				throw new IllegalArgumentException("Key must not end with a '.' character: " + key);
			}
		}

		@Override
		public ResKey plain() {
			return this;
		}
		
		@Override
		ResKey internalFill(Object... arguments) {
			return setArguments(this, arguments);
		}

		@Override
		public Object[] arguments() {
			return NOARGS;
		}

		@Override
		public boolean hasKey() {
			return true;
		}

		@Override
		public String getKey() {
			return _key;
		}

		@Override
		protected String internalEncode() {
			return _key;
		}

		@Override
		void appendDebugLocal(I18NBundleSPI bundle, StringBuilder buffer, Representation representation) {
			buffer.append(getKey());
		}
	}

	private static final class TestKey extends PlainKey {

		private boolean _unknown;

		TestKey(String key, boolean unknown) {
			super(key);
			_unknown = unknown;
		}

		@Override
		protected String lookupDirect(I18NBundleSPI bundle, ResKey origKey, boolean withFallback) {
			if (_unknown) {
				return null;
			}
			return "(" + getKey() + ")";
		}

		@Override
		protected boolean nullMeansMissing(I18NBundleSPI bundle, ResKey origKey) {
			return true;
		}

		@Override
		protected ResKey internalSuffix(String suffix) {
			return forTest(getKey() + suffix);
		}

	}

	private static final class FallbackKey extends AbstractResKey {

		private final DirectKey _direct;

		private final ResKey _fallback;

		public FallbackKey(DirectKey direct, ResKey fallback) {
			if (direct == null) {
				throw new IllegalArgumentException("Fallback key without first alternative.");
			}
			_direct = direct;
			_fallback = fallback;
		}

		@Override
		protected String lookupDirect(I18NBundleSPI bundle, ResKey origKey, boolean withFallbackBundle) {
			String directResult = _direct.lookupDirect(bundle, origKey, withFallbackBundle);
			if (directResult != null || !_direct.nullMeansMissing(bundle, origKey)) {
				return directResult;
			}

			return _fallback.lookupDirect(bundle, origKey, withFallbackBundle);
		}

		@Override
		public boolean hasKey() {
			return _direct.hasKey();
		}

		@Override
		public String getKey() {
			return _direct.getKey();
		}

		@Override
		protected ResKey internalSuffix(String suffix) {
			return fallback(directInternal().internalSuffix(suffix), fallback().internalSuffix(suffix));
		}

		@Override
		protected String internalEncode() {
			return _direct.internalEncode() + ResKeyEncoding.FALLBACK_SEPARATOR + _fallback.internalEncode();
		}

		@Override
		public ResKey fallback() {
			return _fallback;
		}

		@Override
		DirectKey directInternal() {
			return _direct;
		}
		
		@Override
		public ResKey plain() {
			return this;
		}

		@Override
		public Object[] arguments() {
			return NOARGS;
		}
		
		@Override
		ResKey internalFill(Object... arguments) {
			return fallback(directInternal().internalFill(arguments), fallback().internalFill(arguments));
		}

		@Override
		void appendDebug(I18NBundleSPI bundle, StringBuilder buffer, Representation representation) {
			int lengthStart = buffer.length();
			_direct.appendDebug(bundle, buffer, representation);

			int lengthDirect = buffer.length();
			if (lengthDirect > lengthStart) {
				buffer.append(ResKeyEncoding.FALLBACK_SEPARATOR);
			}
			int lengthBefore = buffer.length();
			_fallback.appendDebug(bundle, buffer, representation);
			if (buffer.length() == lengthBefore) {
				// The fallback key produced no output.
				buffer.setLength(lengthDirect);
			}
		}

		@Override
		void appendDebugLocal(I18NBundleSPI bundle, StringBuilder buffer, Representation representation) {
			_direct.appendDebugLocal(bundle, buffer, representation);
		}
	}

	private static final class LegacyKey extends PlainKey {

		LegacyKey(String key) {
			super(key);
		}

	}

	private static final class DeprecatedKey extends DirectKey {

		private final DirectKey _key;

		public DeprecatedKey(DirectKey key) {
			_key = key;
		}

		@Override
		public boolean hasKey() {
			return _key.hasKey();
		}

		@Override
		public String getKey() {
			return _key.getKey();
		}

		@Override
		protected String internalEncode() {
			return _key.internalEncode();
		}

		@Override
		protected String lookupDirect(I18NBundleSPI bundle, ResKey origKey, boolean withFallback) {
			String result = _key.lookupDirect(bundle, origKey, withFallback);
			if (result != null || !_key.nullMeansMissing(bundle, origKey)) {
				bundle.handleDeprecatedKey(_key, origKey);
			}
			return result;
		}

		@Override
		protected ResKey internalSuffix(String suffix) {
			return deprecated(super.internalSuffix(suffix));
		}

		@Override
		protected boolean nullMeansMissing(I18NBundleSPI bundle, ResKey origKey) {
			return _key.nullMeansMissing(bundle, origKey);
		}

		@Override
		public ResKey plain() {
			ResKey plain = _key.plain();
			if (plain == this) {
				return plain;
			}
			return deprecated(plain);
		}

		@Override
		public Object[] arguments() {
			return _key.arguments();
		}

		@Override
		ResKey internalFill(Object... arguments) {
			return deprecated(_key.internalFill(arguments));
		}

		@Override
		void appendDebug(I18NBundleSPI bundle, StringBuilder buffer, Representation representation) {
			if (showDeprecation(bundle, representation)) {
				super.appendDebug(bundle, buffer, representation);
			}
		}

		private boolean showDeprecation(I18NBundleSPI bundle, Representation representation) {
			switch (representation.mode()) {
				case INSPECT:
					// Existing keys are always shown.
					return exists(bundle);
				case UNKNOWN_KEY:
					return representation.level() == Level.DEPRECATION;
			}
			return false;
		}

		@Override
		void appendDebugLocal(I18NBundleSPI bundle, StringBuilder buffer, Representation representation) {
			buffer.append("deprecated:");
			_key.appendDebugLocal(bundle, buffer, representation);
		}

	}

	private static final class NoKey extends DirectKey {

		private final Object _id;

		private final String _local;

		NoKey(Object id, String local) {
			_id = id;
			_local = local;
		}

		@Override
		protected String lookupDirect(I18NBundleSPI bundle, ResKey origKey, boolean withFallback) {
			return null;
		}

		@Override
		public ResKey plain() {
			return this;
		}

		@Override
		public Object[] arguments() {
			return NOARGS;
		}

		@Override
		ResKey internalFill(Object... arguments) {
			return this;
		}

		@Override
		protected ResKey internalSuffix(String suffix) {
			return none(_id, StringServices.isEmpty(_local) ? suffix.substring(1) : _local + suffix);
		}

		@Override
		public boolean hasKey() {
			return true;
		}

		@Override
		public String getKey() {
			return name();
		}

		@Override
		protected String internalEncode() {
			return name();
		}
		
		@Override
		public String toString() {
			return name();
		}

		@Override
		protected boolean exists(I18NBundleSPI bundle) {
			return false;
		}

		@Override
		void appendDebugLocal(I18NBundleSPI bundle, StringBuilder buffer, Representation representation) {
			switch (representation.level()) {
				case FULL:
				case DEPRECATION:
					buffer.append(name());
					break;

				case DEFAULT:
					// Skip.
			}
		}

		private String name() {
			StringBuilder result = new StringBuilder();
			result.append("none(");
			result.append(_id);
			result.append(")");
			if (_local != null) {
				result.append(".");
				result.append(_local);
			}
			return result.toString();
		}
	}

	private static final class MessageKey extends DirectKey {

		private final ResKey _messageKey;

		private final Object[] _arguments;

		MessageKey(ResKey messageKey, Object[] arguments) {
			_messageKey = messageKey;
			_arguments = arguments;
		}

		@Override
		protected String internalEncode() {
			return ResKeyEncoding.encodeMessage(_messageKey, _arguments);
		}

		@Override
		public boolean hasKey() {
			return _messageKey.hasKey();
		}

		@Override
		public String getKey() {
			return _messageKey.getKey();
		}

		@Override
		protected String lookupDirect(I18NBundleSPI bundle, ResKey origKey, boolean withFallback) {
			String value = _messageKey.lookupDirect(bundle, origKey, withFallback);
			if (value != null) {
				// if a value for the key exists: format as usual
				TLMessageFormat theFormat = new TLMessageFormat(value, bundle.getLocale());

				// Thread.yield(); // Use together with the Testcase
				try {
					return theFormat.format(localize(bundle, _arguments));
				} catch (IllegalArgumentException ex) {
					return null;
				}
			} else {
				return null;
			}
		}

		private static Object[] localize(I18NBundleSPI bundle, Object[] arguments) {
			// Allocated on demand.
			Object[] result = null;
			for (int n = 0, cnt = arguments.length; n < cnt; n++) {
				Object argument = arguments[n];
				Object localized = localizeArgument(bundle, argument);
				if (localized == argument) {
					// No need to allocate new array
					continue;
				}
				result = allocateResult(arguments, result);
				result[n] = localized;
			}
			if (result == null) {
				// Nothing to localize.
				return arguments;
			}
			return result;
		}

		private static Object localizeArgument(I18NBundleSPI bundle, Object argument) {
			Object localized;
			if (argument == null) {
				localized = argument;
			} else if (argument instanceof ResKey) {
				ResKey keyArgument = (ResKey) argument;
				localized = keyArgument.resolve(bundle, keyArgument, true);
			} else if (argument instanceof Enum<?>) {
				ResKey keyArgument = forEnum((Enum<?>) argument);
				localized = keyArgument.resolve(bundle, keyArgument, true);
			} else if (argument instanceof Collection<?>) {
				localized = localizeCollection(bundle, argument);
			} else if (argument instanceof TupleFactory.Tuple) {
				localized =
					localizeCollection(bundle, Arrays.asList(TupleFactory.toArray((TupleFactory.Tuple) argument)));
			} else if (argument.getClass().isArray()) {
				localized = localizeCollection(bundle, Arrays.asList((Object[]) argument));
			} else {
				localized = bundle.transformMessageArgument(argument);
			}
			return localized;
		}

		private static Object localizeCollection(I18NBundleSPI bundle, Object argument) {
			Object localized;
			List<Object> result = new ArrayList<>(((Collection<?>) argument).size());
			boolean changed = false;
			for (Object argContent : (Iterable<?>) argument) {
				Object localizedContent = localizeArgument(bundle, argContent);
				result.add(localizedContent);
				if (localizedContent != argContent) {
					changed = true;
				}
			}
			if (changed) {
				localized = result;
			} else {
				localized = argument;
			}
			return localized;
		}

		private static Object[] allocateResult(Object[] arguments, Object[] result) {
			if (result == null) {
				result = new Object[arguments.length];
				System.arraycopy(arguments, 0, result, 0, arguments.length);
			}
			return result;
		}
		
		@Override
		public ResKey plain() {
			return _messageKey;
		}

		@Override
		public Object[] arguments() {
			return _arguments;
		}
		
		@Override
		ResKey internalFill(Object... arguments) {
			if (arguments == null) {
				return this;
			}
			int addedLength = arguments.length;
			if (addedLength == 0) {
				return this;
			}
			int oldLength = _arguments.length;
			Object[] appendedArguments = new Object[oldLength + addedLength];
			System.arraycopy(_arguments, 0, appendedArguments, 0, oldLength);
			System.arraycopy(arguments, 0, appendedArguments, oldLength, addedLength);
			return new MessageKey(_messageKey, appendedArguments);
		}

		@Override
		protected ResKey internalSuffix(String suffix) {
			return _messageKey.suffix(suffix).internalFill(_arguments);
		}

		@Override
		void appendDebugLocal(I18NBundleSPI bundle, StringBuilder buffer, Representation representation) {
			buffer.append(_messageKey);
			buffer.append('(');
			for (int n = 0, cnt = _arguments.length; n < cnt; n++) {
				if (n > 0) {
					buffer.append(", ");
				}
				appendArgument(bundle, buffer, _arguments[n], representation);
			}
			buffer.append(')');
		}

		private void appendArgument(I18NBundleSPI bundle, StringBuilder buffer, Object argument,
				Representation representation) {
			if (argument instanceof String) {
				buffer.append('"');
				buffer.append(argument);
				buffer.append('"');
			} else if (argument instanceof ResKey) {
				((ResKey) argument).appendDebug(bundle, buffer, representation);
			} else {
				buffer.append(argument);
			}
		}
	}

	private static abstract class AbstractLiteral extends DirectKey {

		public AbstractLiteral() {
			super();
		}

		@Override
		public boolean hasKey() {
			return false;
		}

		@Override
		public String getKey() {
			throw new UnsupportedOperationException("Must not resolve literal text as key.");
		}

		@Override
		protected boolean nullMeansMissing(I18NBundleSPI bundle, ResKey origKey) {
			return false;
		}

		@Override
		protected ResKey internalSuffix(String suffix) {
			return NONE;
		}

		@Override
		public ResKey plain() {
			return this;
		}

		@Override
		public Object[] arguments() {
			return NOARGS;
		}

		@Override
		ResKey internalFill(Object... arguments) {
			return this;
		}

		@Override
		protected boolean exists(I18NBundleSPI bundle) {
			return true;
		}

	}

	private static final class LiteralText extends AbstractLiteral {

		private final String _literalText;

		LiteralText(String literalText) {
			_literalText = literalText;
		}

		@Override
		protected String internalEncode() {
			return ResKeyEncoding.encodeLiteralText(_literalText);
		}

		@Override
		protected String lookupDirect(I18NBundleSPI bundle, ResKey origKey, boolean withFallback) {
			return _literalText;
		}

		@Override
		public Object[] arguments() {
			return new Object[] { _literalText };
		}

		@Override
		void appendDebug(I18NBundleSPI bundle, StringBuilder buffer, Representation representation) {
			appendDebugLocal(bundle, buffer, representation);
		}

		@Override
		void appendDebugLocal(I18NBundleSPI bundle, StringBuilder buffer, Representation representation) {
			if (_literalText == null) {
				buffer.append("null");
			} else {
				buffer.append('"');
				buffer.append(_literalText);
				buffer.append('"');
			}
		}
	}

	/**
	 * A {@link ResKey} that stores the {@link Map} of translations.
	 */
	public static class LiteralKey extends AbstractLiteral {

		private final Map<Locale, String> _translations;

		final Map<String, LiteralKey> _suffixKeys;

		private final String _key;

		LiteralKey(String key, Map<Locale, String> translations, Map<String, LiteralKey> suffixKeys) {
			_key = key;
			_translations = Collections.unmodifiableMap(translations);
			_suffixKeys = suffixKeys;
		}

		/** An unmodifiable view of the internal {@link Map} of translations. */
		public Map<Locale, String> getTranslations() {
			return _translations;
		}

		/** The translation for exactly the given {@link Locale}, otherwise null. */
		public String getTranslationWithoutFallbacks(Locale locale) {
			return _translations.get(locale);
		}

		@Override
		public boolean isLiteral() {
			return true;
		}

		@Override
		public boolean hasKey() {
			return _key != null;
		}

		@Override
		public String getKey() {
			return hasKey() ? _key : super.getKey();
		}

		@Override
		ResKey internalFill(Object... arguments) {
			return setArguments(this, arguments);
		}

		@Override
		protected String lookupDirect(I18NBundleSPI bundle, ResKey origKey, boolean withFallback) {
			I18NBundleSPI lookup = bundle;
			do {
				String result = _translations.get(lookup.getLocale());
				if (result != null || !withFallback) {
					return result;
				}

				lookup = lookup.getFallback();
			} while (lookup != null);

			// No translation for requested language or fallback language found.
			// Get translation for any language as fallback
			ResourcesModule res = ResourcesModule.getInstance();
			for (Locale locale : res.getSupportedLocales()) {
				String result = _translations.get(locale);
				if (result != null) {
					return result;
				}
			}

			return null;
		}

		@Override
		protected ResKey internalSuffix(String suffix) {
			LiteralKey suffixKey = _suffixKeys.get(suffix.substring(1));
			if (suffixKey != null) {
				return suffixKey;
			}
			return super.internalSuffix(suffix);
		}

		@Override
		protected String internalEncode() {
			StringBuilder buffer = new StringBuilder();
			appendToString(buffer);
			return buffer.toString();
		}

		@Override
		void appendDebugLocal(I18NBundleSPI bundle, StringBuilder buffer, Representation representation) {
			appendToString(buffer);
		}

		private void appendToString(StringBuilder buffer) {
			buffer.append("#(");
			appendContents(buffer, this);
			buffer.append(")");
		}

		private void appendContents(StringBuilder buffer, LiteralKey literal) {
			boolean first = true;
			ArrayList<Locale> locales = new ArrayList<>(literal._translations.keySet());
			Collections.sort(locales, (l1, l2) -> l1.toLanguageTag().compareTo(l2.toLanguageTag()));
			for (Locale locale : locales) {
				if (first) {
					first = false;
				} else {
					buffer.append(", ");
				}
				buffer.append('"');
				buffer.append(encode(literal._translations.get(locale)));
				buffer.append('"');
				buffer.append('@');
				buffer.append(locale.toLanguageTag());
			}
			for (Entry<String, LiteralKey> suffix : literal._suffixKeys.entrySet()) {
				if (first) {
					first = false;
				} else {
					buffer.append(", ");
				}
				buffer.append(suffix.getKey());
				buffer.append(": {");
				appendContents(buffer, suffix.getValue());
				buffer.append("}");
			}
		}

		private Object encode(String value) {
			return value == null ? "null" : value.replace("\\", "\\\\").replace("\"", "\\\"");
		}
	}

	/**
	 * {@link ConfigurationValueProvider} for reading {@link ResKey}s from configuration.
	 */
	public static class ValueFormat extends AbstractConfigurationValueProvider<ResKey> {
	
		/**
		 * Singleton {@link ResKey.ValueFormat} instance.
		 */
		public static final ResKey.ValueFormat INSTANCE = new ValueFormat();
	
		private ValueFormat() {
			super(ResKey.class);
		}
	
		@Override
		protected ResKey getValueNonEmpty(String propertyName, CharSequence propertyValue)
				throws ConfigurationException {
			try {
				return decode(propertyValue.toString());
			} catch (IllegalArgumentException ex) {
				throw new ConfigurationException("Invalid resource key: " + ex.getMessage(), ex);
			}
		}
	
		@Override
		protected String getSpecificationNonNull(ResKey configValue) {
			return ResKey.encode(configValue);
		}

		@Override
		public boolean isLegalValue(Object value) {
			if (value == null) {
				return true;
			}
			if (!(value instanceof ResKey)) {
				return false;
			}
			ResKey key = (ResKey) value;

			// Literals are better serialized using the binding.
			return !key.isLiteral();
		}
	}

	/**
	 * {@link ConfigurationValueBinding} for (potentially literal) {@link ResKey}s.
	 */
	public static class ValueBinding extends AbstractConfigurationValueBinding<ResKey> {

		/**
		 * Default {@link ValueBinding} instance.
		 */
		public static final ValueBinding INSTANCE = new ValueBinding(false);

		private final boolean _cData;

		/**
		 * Creates a {@link ValueBinding}.
		 *
		 * @param cdataContent
		 *        Whether the content must be written and read as CDATA.
		 */
		public ValueBinding(boolean cdataContent) {
			_cData = cdataContent;
		}

		@Override
		public ResKey loadConfigItem(XMLStreamReader in, ResKey baseValue)
				throws XMLStreamException, ConfigurationException {
			String key = in.getAttributeValue(null, "key");

			String text = XMLStreamUtil.nextText(in).trim();
			int next = in.getEventType();
			if (text.length() > 0) {
				if (key != null) {
					throw new XMLStreamException("Resource key must not be given as key attribute and text content.",
						in.getLocation());
				}
				if (next != XMLStreamConstants.END_ELEMENT) {
					throw new XMLStreamException("Expected end of resouce key with encoded text content.",
						in.getLocation());
				}
				return decode(text);
			}

			ResKey result;
			if (next == XMLStreamConstants.END_ELEMENT) {
				// Not a literal.
				result = decode(key);
			} else {
				Builder builder = ResKey.builder(key);
				fillBuilder(in, builder);
				result = builder.build();
			}
			return result;
		}

		private void fillBuilder(XMLStreamReader in, Builder builder) throws XMLStreamException {
			while (in.getEventType() == XMLStreamConstants.START_ELEMENT) {
				String localeOrSuffixName = in.getLocalName();
				String tmp = XMLStreamUtil.nextText(in);
				switch (in.getEventType()) {
					case XMLStreamConstants.END_ELEMENT: {
						builder.add(new Locale(localeOrSuffixName), tmp);
						break;
					}
					case XMLStreamConstants.START_ELEMENT: {
						Builder suffixBuilder = builder.suffix(localeOrSuffixName);
						fillBuilder(in, suffixBuilder);
						break;
					}
					default:
						throw new XMLStreamException("Expected end of locale element '" + localeOrSuffixName
							+ "' or start of locales for suffix " + localeOrSuffixName + ".", in.getLocation());
				}

				in.nextTag();
			}

			if (in.getEventType() != XMLStreamConstants.END_ELEMENT) {
				throw new XMLStreamException("Expected end of resource element.", in.getLocation());
			}
		}

		@Override
		public void saveConfigItem(XMLStreamWriter out, ResKey item) throws XMLStreamException {
			if (item.isLiteral()) {
				if (item.hasKey()) {
					out.writeAttribute("key", item.getKey());
				}
				if (item instanceof LiteralKey) {
					writeLiteralKeyContent(out, (LiteralKey) item);
				} else {
					writeLocales(out, item);
				}
			} else {
				out.writeAttribute("key", ResKey.encode(item));
			}
		}

		private void writeLiteralKeyContent(XMLStreamWriter out, LiteralKey literal) throws XMLStreamException {
			writeLocales(out, literal);
			for (Entry<String, LiteralKey> entry : literal._suffixKeys.entrySet()) {
				out.writeStartElement(entry.getKey());
				writeLiteralKeyContent(out, entry.getValue());
				out.writeEndElement();
			}
		}

		private void writeLocales(XMLStreamWriter out, ResKey item) throws XMLStreamException {
			for (Locale locale : ResourcesModule.getInstance().getSupportedLocales()) {
				String translation = ResKeyUtil.getTranslation(item, locale);

				if (!StringServices.isEmpty(translation)) {
					out.writeStartElement(locale.toString());
					if (_cData) {
						out.writeCData(translation);
					} else {
						out.writeCharacters(translation);
					}
					out.writeEndElement();
				}
			}
		}

	}

	/**
	 * Looks up the value of this key in the given bundle.
	 * 
	 * @param bundle
	 *        The resource bundle to use.
	 * @param origKey
	 *        The top-most key that is currently being resolved.
	 * @param withFallback
	 *        Whether the {@link I18NBundleSPI#getFallback() fallback bundle} is used when no
	 *        translation can be found.
	 * @return The value for this key in the given bundle. A value <code>null</code> means either
	 *         that the given bundle has no resource for the given key, or the value of the given
	 *         key is <code>null</code>.
	 * 
	 * @see DirectKey#nullMeansMissing(I18NBundleSPI, ResKey)
	 */
	protected abstract String lookupDirect(I18NBundleSPI bundle, ResKey origKey, boolean withFallback);

	/**
	 * Creates a resource debugging representation of this {@link ResKey}.
	 * 
	 * <p>
	 * The format of the debugging representation is the following:
	 * </p>
	 * 
	 * <pre>
	 * flag '[' key '(' optional-arguments ')' | optional-fallback ']'
	 * </pre>
	 * 
	 * <p>
	 * The flag may either be '*' to indicate an existing resource, or '-' to indicate a missing
	 * resource.
	 * </p>
	 * 
	 * <p>
	 * Literal text (that is not defined as resource) is printed in double quotes: "some text".
	 * </p>
	 * 
	 * @param bundle
	 *        The {@link I18NBundle} for which the representation should be created.
	 * @return A debug representation of this key.
	 */
	public final String debug(I18NBundleSPI bundle) {
		return debug(bundle, Mode.INSPECT);
	}

	private String debug(I18NBundleSPI bundle, Mode mode) {
		for (Level level : Level.values()) {
			String result = debug(bundle, Representation.representation(mode, level));
			if (!result.isEmpty()) {
				return result;
			}
		}
		return toString();
	}

	/**
	 * Create a debug representation of this key.
	 * 
	 * @param bundle
	 *        The {@link I18NBundle} for which the representation should be created.
	 * @param representation
	 *        Whether a key inspection representation should be generated (in contrast to a
	 *        simplified "unknown key" representation).
	 * @return The debug representation of this key.
	 */
	private final String debug(I18NBundleSPI bundle, Representation representation) {
		StringBuilder buffer = new StringBuilder();
		appendDebug(bundle, buffer, representation);
		return buffer.toString();
	}

	/**
	 * Representation of a missing key.
	 * 
	 * @param bundle
	 *        The {@link I18NBundle} for which the representation should be created.
	 */
	@FrameworkInternal
	public final String unknown(I18NBundleSPI bundle) {
		return debug(bundle, Mode.UNKNOWN_KEY);
	}

	/**
	 * Appends the debug representation of this key and potential fallbacks to the given buffer.
	 * 
	 * @param bundle
	 *        The {@link I18NBundle} for which the representation should be created.
	 * @param buffer
	 *        The buffer to fill with the debug representation.
	 * @param representation
	 *        Whether a key inspection representation should be generated (in contrast to a
	 *        simplified "unknown key" representation).
	 */
	void appendDebug(I18NBundleSPI bundle, StringBuilder buffer, Representation representation) {
		int start = buffer.length();
		if (representation.mode() == Mode.INSPECT) {
			buffer.append(debugFlag(bundle));
		}
		buffer.append('[');
		int innerStart = buffer.length();
		appendDebugLocal(bundle, buffer, representation);
		if (buffer.length() == innerStart) {
			buffer.setLength(start);
		} else {
			buffer.append(']');
		}
	}

	/**
	 * One of {@link ResourcesModule#FLAG_KEY_FOUND}, or {@link ResourcesModule#FLAG_KEY_NOT_FOUND}
	 * 
	 * @param bundle
	 *        The context bundle to check for existence.
	 * @return The debug flag for this resource.
	 */
	private char debugFlag(I18NBundleSPI bundle) {
		if (exists(bundle)) {
			return ResourcesModule.FLAG_KEY_FOUND;
		} else {
			return ResourcesModule.FLAG_KEY_NOT_FOUND;
		}
	}

	/**
	 * Whether this key can be resolved in the given bundle.
	 */
	protected boolean exists(I18NBundleSPI bundle) {
		return bundle.existsResource(getKey());
	}

	/**
	 * Appends the debug representation of this key excluding debug flags and other decoration.
	 * 
	 * @param representation
	 *        See {@link #appendDebug(I18NBundleSPI, StringBuilder, Representation)}.
	 */
	abstract void appendDebugLocal(I18NBundleSPI bundle, StringBuilder buffer, Representation representation);

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof ResKey) {
			return encode(this).equals(encode((ResKey) obj));
		}
		return false;
	}

	@Override
	public int hashCode() {
		return encode(this).hashCode();
	}

	/**
	 * Transforms this {@link ResKey} to a {@link ResKey1}. Arguments delivered to the returned
	 * {@link ResKey1} are ignored.
	 * 
	 * @return This {@link ResKey} acting as {@link ResKey1}.
	 * 
	 * @deprecated Define a {@link ResKey1} in an "I18NConstants" class. (See
	 *             {@link I18NConstantsBase})
	 */
	@Deprecated
	public abstract ResKey1 asResKey1();

	/**
	 * Transforms this {@link ResKey} to a {@link ResKey2}. Arguments delivered to the returned
	 * {@link ResKey2} are ignored.
	 * 
	 * @return This {@link ResKey} acting as {@link ResKey2}
	 * 
	 * @deprecated Define a {@link ResKey2} in an "I18NConstants" class. (See
	 *             {@link I18NConstantsBase})
	 */
	@Deprecated
	public abstract ResKey2 asResKey2();

	/**
	 * Transforms this {@link ResKey} to a {@link ResKey3}. Arguments delivered to the returned
	 * {@link ResKey3} are ignored.
	 * 
	 * @return This {@link ResKey} acting as {@link ResKey3}
	 * 
	 * @deprecated Define a {@link ResKey3} in an "I18NConstants" class. (See
	 *             {@link I18NConstantsBase})
	 */
	@Deprecated
	public abstract ResKey3 asResKey3();

	/**
	 * Transforms this {@link ResKey} to a {@link ResKey4}. Arguments delivered to the returned
	 * {@link ResKey4} are ignored.
	 * 
	 * @return This {@link ResKey} acting as {@link ResKey4}
	 * 
	 * @deprecated Define a {@link ResKey4} in an "I18NConstants" class. (See
	 *             {@link I18NConstantsBase})
	 */
	@Deprecated
	public abstract ResKey4 asResKey4();

	/**
	 * Transforms this {@link ResKey} to a {@link ResKey5}. Arguments delivered to the returned
	 * {@link ResKey5} are ignored.
	 * 
	 * @return This {@link ResKey} acting as {@link ResKey5}.
	 * 
	 * @deprecated Define a {@link ResKey5} in an "I18NConstants" class. (See
	 *             {@link I18NConstantsBase})
	 */
	@Deprecated
	public abstract ResKey5 asResKey5();

	/**
	 * Transforms this {@link ResKey} to a {@link ResKeyN}.
	 * 
	 * @return This {@link ResKey} acting as {@link ResKeyN}.
	 * 
	 * @deprecated Define a {@link ResKeyN} in an "I18NConstants" class. (See
	 *             {@link I18NConstantsBase})
	 */
	@Deprecated
	public abstract ResKeyN asResKeyN();

}
