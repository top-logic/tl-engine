/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.util.ResKey;
import com.top_logic.util.Resources;

/**
 * Factory for creating resouce keys with a common prefix (namespace).
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Label("Resource prefix")
@Format(ResPrefix.ValueFormat.class)
public abstract class ResPrefix implements ResourceView {
	
	/**
	 * Central lock for updating / extending cached resources. After some warmup, no further
	 * modifications to the cached structure should occur and all operations should perform fully
	 * concurrent.
	 */
	private static final ReentrantReadWriteLock LOCK = new ReentrantReadWriteLock();

	static final ReadLock READ_LOCK = LOCK.readLock();

	static final WriteLock WRITE_LOCK = LOCK.writeLock();

	private static final Map<String, ResPrefix> PREFIXES = new HashMap<>(4096);

	/**
	 * The global resouce namespace (without prefix).
	 */
	public static final ResPrefix GLOBAL = internalCreate("");

	/**
	 * A namespace that must not be used.
	 * 
	 * <p>
	 * This represents an assertion that no resources are ever created from a certain namespace.
	 * </p>
	 */
	public static final ResPrefix NONE = none("unknown");

	/**
	 * {@link ConfigurationValueProvider} to read {@link ResPrefix} instances in typed
	 * configurations.
	 */
	public static class ValueFormat extends AbstractConfigurationValueProvider<ResPrefix> {

		/**
		 * Singleton {@link ResPrefix.ValueFormat} instance.
		 */
		public static final ResPrefix.ValueFormat INSTANCE = new ResPrefix.ValueFormat();

		private ValueFormat() {
			super(ResPrefix.class);
		}

		@Override
		protected ResPrefix getValueNonEmpty(String propertyName, CharSequence propertyValue)
				throws ConfigurationException {
			return ResPrefix.internalCreate(propertyValue.toString());
		}

		@Override
		protected String getSpecificationNonNull(ResPrefix configValue) {
			return configValue.toPrefix();
		}

	}

	@Override
	public ResKey getStringResource(String suffix) {
		return key(suffix);
	}

	@Override
	public ResKey getStringResource(String suffix, ResKey defaultValue) {
		return key(suffix).fallback(defaultValue);
	}

	@Override
	public boolean hasStringResource(String suffix) {
		return Resources.getInstance().getString(key(suffix), null) != null;
	}

	/**
	 * Creates sub-namespace with the given suffix.
	 * 
	 * @param suffix
	 *        The suffix to append to this {@link ResPrefix}.
	 * @return The sub-namespace that generates keys with an enlarged prefix.
	 */
	public abstract ResPrefix append(String suffix);

	/**
	 * Creates a resouce key with the given local name.
	 * 
	 * @param suffix
	 *        The key suffix to append to this namespace.
	 * @return A resouce key that can be used for resolving a resource.
	 * 
	 * @see Resources#getString(ResKey)
	 */
	public final ResKey key(String suffix) {
		if (suffix == null || suffix.isEmpty()) {
			return null;
		}

		return internalKey(suffix);
	}

	/**
	 * Allocates a key from the given suffix.
	 * 
	 * @param suffix
	 *        The suffix to append to this prefix.
	 * @return The complete key.
	 */
	protected abstract ResKey internalKey(String suffix);

	/**
	 * Whether this is the global namespace.
	 */
	public abstract boolean isEmpty();

	/**
	 * The internal {@link String} representation of the prefix.
	 */
	@FrameworkInternal
	public abstract String toPrefix();

	/**
	 * Compatibility for the legacy method of generating resource keys (by just appending some
	 * suffix to the prefix string).
	 * 
	 * @return The raw prefix.
	 */
	@Override
	public String toString() {
		return toPrefix();
	}

	/**
	 * Creates a {@link ResPrefix} for a Java class.
	 */
	public static ResPrefix forClass(Class<?> clazz) {
		return internalCreate("class." + clazz.getName() + ".");
	}

	public static ResPrefix none(Object id) {
		return new NoPrefix(id);
	}

	/**
	 * Creates a dummy (testing only) {@link ResPrefix} with the given raw prefix string.
	 * 
	 * <p>
	 * Note: This method must only be referenced from test code!
	 * </p>
	 */
	public static ResPrefix forTest(String prefix) {
		// Uncached for testing only.
		return allocatePrefix(prefix);
	}

	/**
	 * Creates a {@link ResPrefix} from a prefix string.
	 * 
	 * @deprecated Use {@link #forClass(Class)}
	 */
	@Deprecated
	public static ResPrefix legacyString(String prefix) {
		return internalCreate(prefix);
	}

	/**
	 * Creates a {@link ResPrefix} for a Java package in legacy stripped form (excluding a
	 * <code>com.top_logic.</code> prefix).
	 * 
	 * @deprecated Use {@link #forClass(Class)}
	 */
	@Deprecated
	public static ResPrefix legacyPackage(Class<?> clazz) {
		return internalCreate(I18NConstantsBase.i18nPrefix(clazz));
	}

	/**
	 * Creates a {@link ResPrefix} for a Java class in legacy full form (excluding the the
	 * <code>com.top_logic.</code> prefix).
	 * 
	 * @deprecated Use {@link #forClass(Class)}
	 */
	@Deprecated
	public static ResPrefix legacyClassLocal(Class<?> clazz) {
		return internalCreate(I18NConstantsBase.i18nPrefix(clazz) + '.' + clazz.getSimpleName() + '.');
	}

	/**
	 * Creates a {@link ResPrefix} for a Java class in legacy full form (including the the
	 * <code>com.top_logic.</code> prefix) without a "class." prefix.
	 * 
	 * @deprecated Use {@link #forClass(Class)}
	 */
	@Deprecated
	public static ResPrefix legacyClass(Class<?> clazz) {
		return internalCreate(clazz.getName());
	}

	static ResPrefix internalCreate(String prefix) {
		READ_LOCK.lock();
		try {
			ResPrefix result = PREFIXES.get(prefix);
			if (result != null) {
				return result;
			}
		} finally {
			READ_LOCK.unlock();
		}
	
		WRITE_LOCK.lock();
		try {
			ResPrefix newPrefix = allocatePrefix(prefix);
			ResPrefix existing = PREFIXES.put(prefix, newPrefix);
			if (existing != null) {
				// Concurrent creation.
				return existing;
			}
			return newPrefix;
		} finally {
			WRITE_LOCK.unlock();
		}
	}

	private static ResPrefix allocatePrefix(String prefix) {
		if (prefix.endsWith(".") || prefix.isEmpty()) {
			return new DefaultPrefix(prefix);
		} else {
			return new DefaultPrefix(prefix + ".");
		}
	}

	static String normalizeSuffix(String suffix) {
		String normalizedSuffix;
		if (suffix.startsWith(".")) {
			normalizedSuffix = suffix.substring(1);
		} else {
			normalizedSuffix = suffix;
		}
		return normalizedSuffix;
	}

	private static class DefaultPrefix extends ResPrefix {

		private final String _prefix;

		private Map<String, ResKey> _keys = new HashMap<>(4);

		DefaultPrefix(String prefix) {
			_prefix = prefix;
		}

		@Override
		public ResPrefix append(String suffix) {
			String prefix;
			if (suffix.endsWith(".")) {
				if (suffix.startsWith(".")) {
					prefix = _prefix + suffix.substring(1);
				} else {
					prefix = _prefix + suffix;
				}
			} else {
				if (suffix.startsWith(".")) {
					prefix = _prefix + suffix.substring(1) + ".";
				} else {
					prefix = _prefix + suffix + ".";
				}
			}
			return internalCreate(prefix);
		}

		@Override
		protected ResKey internalKey(String suffix) {
			READ_LOCK.lock();
			try {
				ResKey result = _keys.get(suffix);
				if (result != null) {
					return result;
				}
			} finally {
				READ_LOCK.unlock();
			}

			WRITE_LOCK.lock();
			try {
				ResKey newKey = allocateKey(suffix);
				ResKey existing = _keys.put(suffix, newKey);
				if (existing != null) {
					// Concurrent creation.
					return existing;
				}
				return newKey;
			} finally {
				WRITE_LOCK.unlock();
			}
		}

		private ResKey allocateKey(String suffix) {
			return ResKey.internalCreate(_prefix + normalizeSuffix(suffix));
		}

		@Override
		public boolean isEmpty() {
			return _prefix.isEmpty();
		}

		@Override
		public String toPrefix() {
			return _prefix;
		}
	}

	private static class NoPrefix extends ResPrefix {

		private final Object _id;

		NoPrefix(Object id) {
			_id = id;
		}

		@Override
		public ResPrefix append(String suffix) {
			return this;
		}

		@Override
		protected ResKey internalKey(String suffix) {
			return ResKey.none(_id, normalizeSuffix(suffix));
		}

		@Override
		public boolean isEmpty() {
			return true;
		}

		@Override
		public String toPrefix() {
			return "none(" + _id + ").";
		}

	}

}
