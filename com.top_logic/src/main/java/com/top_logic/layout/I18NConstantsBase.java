/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import com.top_logic.html.i18n.DefaultHtmlResKey;
import com.top_logic.html.i18n.HtmlResKey;
import com.top_logic.html.i18n.HtmlResKey1;
import com.top_logic.html.i18n.HtmlResKey2;
import com.top_logic.html.i18n.HtmlResKey3;
import com.top_logic.html.i18n.HtmlResKey4;
import com.top_logic.html.i18n.HtmlResKey5;
import com.top_logic.html.i18n.HtmlResKeyN;

/**
 * Common base class for all I18NConstant classes.
 * 
 * <p>
 * This class automatically creates I18N keys for all string "constants"
 * declared by subclasses. This initialization process must be triggered by
 * including the following code snippet into each <code>I18NConstants</code>
 * subclass:
 * </p>
 * 
 * <pre>
 *   static {
 *       {@link #initConstants(Class) initConstants(I18NConstants.class)};
 *   }
 * </pre>
 * 
 * <p>
 * Each subclass must be named <code>I18NConstants</code> and it must be
 * located in the package for which it defines I18N key constants. For
 * brevity the common prefix "com.top_logic." is dropped.
 * </p>
 * 
 * <p>
 * Besides the static initializer shown above, subclasses define
 * <code>static non-final</code> {@link String} members that adhere to the
 * following naming convention:
 * </p>
 * 
 * &lt;DESCRIPTION_OF_THE_MESSAGE> "_" ("SINGULAR" | "PLURAL")? ( "__"
 * &lt;ARGUMENT> ("_" &lt;ARGUMENT>)* )?
 *
 * <p>
 * <b>Note:</b> These "constants" cannot be declared <code>final</code>,
 * because their values are initialized reflectively.
 * </p>
 * 
 * @see com.top_logic.layout.form.I18NConstants for a concrete example.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class I18NConstantsBase extends com.top_logic.basic.i18n.I18NConstantsBase {

	/**
	 * Marker annotation to identify elements in extensions of {@link I18NConstantsBase} which will
	 * serve as prefix for the real resources. These constants ends with '.'.
	 * 
	 * @since 5.8.0
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 * 
	 * @deprecated Is no longer neccessary, since resource prefixes now are represented by a
	 *             separate type {@link ResPrefix}.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@Deprecated
	public static @interface ResourcePrefix {
		// no arguments. Just marker annotation
	}

	private static final String OMITTED_COMMON_PREFIX = "com.top_logic.";

	/** This class (and all of its subclasses) may not be instantiated. */
	protected I18NConstantsBase() {
		throw new AssertionError();
	}
	
	/**
	 * Initializes all <code>public static</code> fields of type
	 * {@link String} that are declared in the given class with a value suitable
	 * as property key.
	 * 
	 * <p>
	 * This method must be called from the static initializer of an
	 * <code>I18NConstants</code> class.
	 * </p>
	 * 
	 * @param i18nClass
	 *     The subclass of {@link I18NConstantsBase} that should be
	 *     initialized.
	 */
	protected static void initConstants(Class<? extends com.top_logic.basic.i18n.I18NConstantsBase> i18nClass) {
		internalInitConstants(FieldInitializer.INSTANCE, i18nClass);
	}

	/**
	 * Legacy resource prefix derived from a classes package and name.
	 */
	public static String i18nPrefix(Class<?> i18nClass) {
		return getLegacyResPrefix(i18nClass.getName());
	}

	/**
	 * Legacy resource prefix derived from a classes package and name.
	 */
	public static String getLegacyResPrefix(String className) {
		int indexOfLastDot = className.lastIndexOf('.');
		
		assert indexOfLastDot > 0 : "not a top-level class";
		
		String prefix = className.substring(
			className.startsWith(OMITTED_COMMON_PREFIX) ? 
				OMITTED_COMMON_PREFIX.length() : 0, indexOfLastDot);
		
		return prefix;
	}
	
	/**
	 * Creates a {@link ResPrefix} with a custom value.
	 * 
	 * @deprecated Just leave the value unassigned and let the generic assign a value.
	 */
	@Deprecated
	protected static ResPrefix legacyPrefix(String prefix) {
		return ResPrefix.internalCreate(prefix);
	}

	private static class FieldInitializer extends com.top_logic.basic.i18n.I18NConstantsBase.FieldInitializer {

		/**
		 * Singleton {@link I18NConstantsBase.FieldInitializer} instance.
		 */
		public static final I18NConstantsBase.FieldInitializer INSTANCE = new I18NConstantsBase.FieldInitializer();

		private FieldInitializer() {
			// Singleton constructor.
		}

		@Override
		protected boolean canHandle(Field constantField) {
			return super.canHandle(constantField) || constantField.getType() == ResPrefix.class
					|| isHtmlResKeyLike(constantField.getType());
		}

		private boolean isHtmlResKeyLike(Class<?> type) {
			return type == HtmlResKey.class || type == HtmlResKey1.class || type == HtmlResKey2.class
					|| type == HtmlResKey3.class || type == HtmlResKey4.class || type == HtmlResKey5.class
					|| type == HtmlResKeyN.class;
		}

		@Override
		protected void init(Field constantField, StringBuilder qualifiedKeyBuilder) throws IllegalAccessException {
			Class<?> fieldType = constantField.getType();
			boolean resourcePrefix = fieldType == ResPrefix.class;
			if (resourcePrefix) {
				qualifiedKeyBuilder.append('.');
				String qualifiedKey = qualifiedKeyBuilder.toString();
				constantField.set(null, ResPrefix.internalCreate(qualifiedKey));
			} else if (isHtmlResKeyLike(fieldType)) {
				constantField.set(null, new DefaultHtmlResKey(toResKey(qualifiedKeyBuilder)));
			} else {
				super.init(constantField, qualifiedKeyBuilder);
			}
		}
	}

}
