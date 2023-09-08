/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;


/**
 * A {@link TypeKeyProvider} looks up {@link Key}s for arbitrary object
 * instances.
 * 
 * <p>
 * A {@link TypeKeyProvider} implementation encapsulates the mechanism for
 * identifying the "type" of an object.
 * </p>
 * 
 * @see DefaultTypeKeyProvider for an implementation that uses the Java class of
 *      an object to find a Key for an instance.
 * 
 * @see TypeKeyRegistry for getting static access to a configured
 *      {@link TypeKeyProvider} implementation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TypeKeyProvider extends Reloadable {
	
	/**
	 * Find and return the {@link Key} instance associated with the given
	 * object.
	 * 
	 * <p>
	 * Two consecutive calls with the same object return an identical key
	 * instance.
	 * </p>
	 * 
	 * @return A {@link Key} identifying the type of the given object. Must not
	 *         be <code>null</code>.
	 */
	Key lookupTypeKey(Object obj);
	
	/**
	 * Key to lookup a value in the registry.
	 * 
	 * <p>
	 * Besides the name, the {@link TypeKeyProvider.Key} interface defines the fallback
	 * mechanism to find a best-matching default, if nothing is configured in
	 * the registry (see {@link #getDefaultKey()}).
	 * </p>
	 */
	public interface Key {
		
		/**
		 * The name of the key.
		 * 
		 * <p>
		 * This value is expected to be found as key in the
		 * {@link Configuration}.
		 * </p>
		 */
		String getConfigurationName();
		
		/**
		 * The default key for this key.
		 * 
		 * <p>
		 * The default key is used, if nothing is configured for some key. This
		 * fallback mechanism is multi-leveled. If nothing is configured for a
		 * default key, the default's default key is used and so on.
		 * </p>
		 * 
		 * @return A default to use for this key, or <code>null</code>, if
		 *     there is no default. In this case, the
		 *     {@link SingletonRegistry#getGlobalDefault()} is used.
		 */
		Key getDefaultKey();
		
		/**
		 * Check if the key is for historic data which may be treated differently
		 * 
		 * @return true if this key is for historic data
		 */
		boolean isHistoric();
	}

}
