/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.basic.col.MapUtil;

/**
 * {@link TypeKeyProvider} that uses the Java class hierarchy for providing
 * {@link TypeKeyProvider.Key} instances.
 * 
 * @see TypeKeyRegistry for getting access to the functionality of this class.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultTypeKeyProvider implements TypeKeyProvider {

	/** The prefix to indicate that the key is a Java {@link Class}. */
	public static final String CLASS_NAME_PREFIX = "class:";
	
	private final ConcurrentHashMap<Class<?>, Key> classKeyCache =
		new ConcurrentHashMap<>();
	
	@Override
	public Key lookupTypeKey(Object obj) {
		return getClassKey(obj.getClass());
	}
	
	public Key getClassKey(Class<?> keyClass) {
		Key result = classKeyCache.get(keyClass);
		if (result == null) {
			result = MapUtil.putIfAbsent(classKeyCache, keyClass, new ClassKey(keyClass));
		}
		return result;
	}
	
	/**
	 * Wrapper for a {@link Class} to use as {@link TypeKeyProvider.Key} to a
	 * {@link SingletonRegistry}.
	 * 
	 * <p>
	 * The default for a classes key is the key corresponding to its
	 * super-class. The only key without default is the key corresponding to
	 * {@link Object}.
	 * </p>
	 */
	public static final class ClassKey implements Key {
		
		private Class<?> keyClass;

		private String _configName;

		/**
		 * A key corresponding to the given class.
		 * 
		 * <p>
		 * Class keys use their super-classes key as default. 
		 * </p>
		 */
		public ClassKey(Class<?> keyClass) {
			this.keyClass = keyClass;
			_configName = CLASS_NAME_PREFIX + keyClass.getName();
		}

		@Override
		public String getConfigurationName() {
			return _configName;
		}
		
		@Override
		public Key getDefaultKey() {
			Class<?> superclass = keyClass.getSuperclass();
			if (superclass != null) {
				return new ClassKey(superclass);
			} else {
				return null;
			}
		}
        
        @Override
		public String toString() {
            return "ClassKey: " + keyClass.getName();
        }
		
		@Override
		public boolean equals(Object obj) {
	    	if (obj == this) {
	    		return true;
	    	}
			if (obj instanceof ClassKey) {
				ClassKey other = (ClassKey) obj;

				return keyClass.equals(other.keyClass);
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return keyClass.hashCode();
		}
		
		@Override
		public boolean isHistoric() {
			return false;
		}
	}
	
	/**
	 * Key implementation which holds a formerly created {@link #getDefaultKey()}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public abstract static class KeyWithDefault implements Key {

		private Key _defaultKey;

		/**
		 * Creates a new {@link KeyWithDefault}.
		 * 
		 * @param defaultKey
		 *        Value of {@link #getDefaultKey()}
		 */
		public KeyWithDefault(Key defaultKey) {
			_defaultKey = defaultKey;
		}

		@Override
		public Key getDefaultKey() {
			return _defaultKey;
		}
	}

	@Override
	public String getDescription() {
		//  TODO BHU Automatically created
		return null;
	}

	@Override
	public String getName() {
		//  TODO BHU Automatically created
		return null;
	}

	@Override
	public boolean usesXMLProperties() {
		//  TODO BHU Automatically created
		return false;
	}

	@Override
	public boolean reload() {
		classKeyCache.clear();
		
		return true;
	}
	
}
