/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.i18n;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.basic.util.ResKey4;
import com.top_logic.basic.util.ResKey5;
import com.top_logic.basic.util.ResKeyN;

/**
 * Common base class for all <code>I18NConstants</code> classes.
 * 
 * <p>
 * This class automatically creates I18N keys for "constants" declared by subclasses. This
 * initialization process must be triggered by including the following code snippet into each
 * <code>I18NConstants</code> subclass:
 * </p>
 * 
 * <pre>
 *   static {
 *       {@link #initConstants(Class) initConstants(I18NConstants.class)};
 *   }
 * </pre>
 * 
 * <p>
 * Each subclass must be named <code>I18NConstants</code> and it must be located in the package for
 * which it defines I18N key constants.
 * </p>
 * 
 * <p>
 * Besides the static initializer shown above, subclasses define <tt>static non-final</tt> members
 * of one of the following types:
 * <dl>
 * <dt>{@link ResKey}</dt>
 * <dd>A I18N key without any arguments.</dd>
 * <dt>{@link ResKey1}</dt>
 * <dd>A I18N key which needs one argument.</dd>
 * <dt>{@link ResKey2}</dt>
 * <dd>A I18N key which needs two arguments.</dd>
 * <dt>{@link ResKey3}</dt>
 * <dd>A I18N key which needs three arguments.</dd>
 * <dt>{@link ResKey4}</dt>
 * <dd>A I18N key which needs four arguments.</dd>
 * <dt>{@link ResKey5}</dt>
 * <dd>A I18N key which needs five arguments.</dd>
 * <dt>{@link ResKeyN}</dt>
 * <dd>A I18N key which needs more than five arguments. The user is responsible to deliver the
 * correct number of arguments.</dd>
 * </dl>
 * The members adhere to the following naming convention:
 * </p>
 * 
 * &lt;DESCRIPTION_OF_THE_MESSAGE> ( "__" &lt;ARGUMENT> ("_" &lt;ARGUMENT>)* )?
 * 
 * <p>
 * The resulting I18N is derived from the full-qualified name of the <code>I18NConstants</code>
 * class, the name of the constant and the prefix "class", e.g. if the {@link ResKey} constant with
 * name <tt>MY_MESSAGE</tt> is located in the class <tt>com.my.app.messages.I18NConstants</tt>, the
 * translation for that constant is searched under
 * <tt>class.com.my.app.messages.I18NConstants.MY_MESSAGE</tt>. A {@link ResKey2} constant with name
 * <tt>MY_MESSAGE__ARG1_ARG2</tt> is accordingly searched for under
 * <tt>class.com.my.app.messages.I18NConstants.MY_MESSAGE__ARG1_ARG2</tt>
 * </p>
 * 
 * @implNote These "constants" cannot be declared <code>final</code>, because their values are
 *           initialized reflectively.
 * 
 * @see ResKey
 * @see ResKey1
 * @see ResKey2
 * @see ResKey3
 * @see ResKey4
 * @see ResKey5
 * @see ResKeyN
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class I18NConstantsBase {

	private static final String I18NCONSTANTS_CLASS_NAME = "I18NConstants";

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
	protected static void initConstants(Class<? extends I18NConstantsBase> i18nClass) {
		internalInitConstants(FieldInitializer.INSTANCE, i18nClass);
	}

	/**
	 * Processes all declared fields of the given class with the given {@link FieldInitializer}
	 */
	protected static void internalInitConstants(FieldInitializer initializer, Class<? extends I18NConstantsBase> i18nClass) {
		String className = i18nClass.getName();
		int indexOfLastDot = className.lastIndexOf('.');

		assert className.substring(indexOfLastDot + 1).equals(I18NCONSTANTS_CLASS_NAME) : 
			"I18N class is named " + I18NCONSTANTS_CLASS_NAME;
		assert I18NConstantsBase.class.isAssignableFrom(i18nClass) : 
			"I18N class extends I18N base";
		
		Field[] allFields = i18nClass.getDeclaredFields();

		for (int cnt = allFields.length, n = 0; n < cnt; n++) {
			initializer.initField(i18nClass, allFields[n]);
		}
	}
	
	/**
	 * Iterates all {@link ResKey}, {@link ResKey1}, {@link ResKey2}, {@link ResKey3},
	 * {@link ResKey4}, {@link ResKey5}, or {@link ResKeyN} properties of a
	 * {@link I18NConstantsBase} class.
	 * 
	 * @param filter
	 *        An optional filter to return only those constants that match the given filter. May be
	 *        <code>null</code>.
	 */
	public static Iterator<Field> getI18NConstants(final Class<? extends I18NConstantsBase> i18nClass,
			Predicate<? super Field> filter) {
		return new Iterator<>() {
			Field[] allFields = i18nClass.getDeclaredFields();
			int n = 0;
			
			/**
			 * Whether {@link #hasResult} and {@link #nextResult} contain valid
			 * values. If not, {@link #findNext()} must be called to obtain the
			 * next iterator result.
			 */
			boolean resultValid = false;
			
			/**
			 * Whether this iterator has a next result.
			 */
			boolean hasResult;
			
			/**
			 * The next result returned form this iterator.
			 */
			Field nextResult;
			
			private void findNext() {
				for ( ;n < allFields.length; n++) {
					Field constantField = allFields[n];
					int constantModifiers = constantField.getModifiers();
					if (! Modifier.isStatic(constantModifiers)) continue;
					if (! Modifier.isPublic(constantModifiers)) continue;
					if (!I18NConstantsBase.isResKeyLike(constantField.getType()))
						continue;
					if (filter != null && !filter.test(constantField)) {
						continue;
					}
					hasResult = true;
					nextResult = constantField;
					
					// Make sure, to advance the cursor just before the next
					// call to findNext().
					n++;
					return;
				}
				
				hasResult = false;
				nextResult = null;
				return;
			}
			
			@Override
			public boolean hasNext() {
				if (! resultValid) {
					findNext();
					resultValid = true;
				}
				return hasResult;
			}

			@Override
			public Field next() {
				if (! hasNext()) {
					throw new NoSuchElementException();
				}
				
				resultValid = false;
				return nextResult;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	} 

	/**
	 * Creates a {@link ResKey} with a custom value.
	 * 
	 * @deprecated Just leave the value unassigned and let the generic assign a value.
	 */
	@Deprecated
	protected static ResKey legacyKey(String key) {
		return ResKey.internalCreate(key);
	}

	/**
	 * Creates a {@link ResKey1} with a custom value.
	 * 
	 * @deprecated Just leave the value unassigned and let the generic assign a value.
	 */
	@Deprecated
	protected static ResKey1 legacyKey1(String key) {
		return (ResKey1) legacyKey(key);
	}

	/**
	 * Creates a {@link ResKey2} with a custom value.
	 * 
	 * @deprecated Just leave the value unassigned and let the generic assign a value.
	 */
	@Deprecated
	protected static ResKey2 legacyKey2(String key) {
		return (ResKey2) legacyKey(key);
	}

	/**
	 * Creates a {@link ResKey3} with a custom value.
	 * 
	 * @deprecated Just leave the value unassigned and let the generic assign a value.
	 */
	@Deprecated
	protected static ResKey3 legacyKey3(String key) {
		return (ResKey3) legacyKey(key);
	}

	/**
	 * Creates a {@link ResKey4} with a custom value.
	 * 
	 * @deprecated Just leave the value unassigned and let the generic assign a value.
	 */
	@Deprecated
	protected static ResKey4 legacyKey4(String key) {
		return (ResKey4) legacyKey(key);
	}

	/**
	 * Creates a {@link ResKey5} with a custom value.
	 * 
	 * @deprecated Just leave the value unassigned and let the generic assign a value.
	 */
	@Deprecated
	protected static ResKey5 legacyKey5(String key) {
		return (ResKey5) legacyKey(key);
	}

	/**
	 * Algorithm for initializing a declared field of a I18N class.
	 */
	protected static class FieldInitializer {

		/**
		 * Singleton {@link I18NConstantsBase.FieldInitializer} instance.
		 */
		public static final I18NConstantsBase.FieldInitializer INSTANCE = new I18NConstantsBase.FieldInitializer();

		/**
		 * Creates a {@link FieldInitializer}.
		 */
		protected FieldInitializer() {
			// Singleton constructor.
		}

		/**
		 * Initializes the given field of the given class.
		 */
		public final void initField(Class<? extends I18NConstantsBase> i18nClass, Field constantField) {
			int constantModifiers = constantField.getModifiers();
			if (!Modifier.isStatic(constantModifiers) // ignore all none (public static String)
				|| !Modifier.isPublic(constantModifiers) || !canHandle(constantField)) {
				return;
			}

			try {
				Object formerValue = constantField.get(null);
				if (formerValue == null) {
					String constantFieldName = constantField.getName();
					StringBuilder qualifiedKeyBuilder = new StringBuilder();
					qualifiedKeyBuilder.append("class.");
					qualifiedKeyBuilder.append(i18nClass.getName());
					qualifiedKeyBuilder.append('.');
					qualifiedKeyBuilder.append(constantFieldName);

					init(constantField, qualifiedKeyBuilder);
				}
			} catch (IllegalArgumentException e) {
				throw new UnreachableAssertion(e);
			} catch (IllegalAccessException e) {
				throw new UnreachableAssertion(e);
			}
		}

		/**
		 * Whether this algorithm can handle the given {@link Field}.
		 */
		protected boolean canHandle(Field constantField) {
			Class<?> type = constantField.getType();
			return isResKeyLike(type);
		}

		/**
		 * Actually initializes the given field that is accepted by {@link #canHandle(Field)}.
		 * 
		 * @param constantField
		 *        The {@link Field} to initialize.
		 * @param qualifiedKeyBuilder
		 *        A {@link StringBuilder} filled with the qualified name of the given field.
		 */
		protected void init(Field constantField, StringBuilder qualifiedKeyBuilder) throws IllegalAccessException {
			String qualifiedKey = qualifiedKeyBuilder.toString();
			constantField.set(null, ResKey.internalCreate(qualifiedKey));
		}

	}

	static boolean isResKeyLike(Class<?> type) {
		return type == ResKey.class || type == ResKey1.class || type == ResKey2.class || type == ResKey3.class
			|| type == ResKey4.class || type == ResKey5.class || type == ResKeyN.class;
	}

}
