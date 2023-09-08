/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.util.Iterator;

import com.top_logic.basic.Configuration.IterableConfiguration;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.DefaultConfigConstructorScheme;
import com.top_logic.basic.config.DefaultConfigConstructorScheme.Factory;

/**
 * Base class for a configurable registry of singleton objects.
 * 
 * <p>
 * The singletons in a {@link SingletonRegistry} can be configured through the
 * global {@link Configuration}. The registry provides lookup of singletons
 * associated to a {@link TypeKeyProvider.Key} in the registry. If there is no singleton defined
 * for some key, there is a multi-level fall-back mechanism to find a
 * best-matching default.
 * </p>
 * 
 * <p>
 * For an example usage see: <code>LabelProviderRegistry</code>.
 * </p>
 * 
 * @see TypeKeyProvider.Key for the default lookup mechanism.
 * @see Configuration
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class SingletonRegistry<T> extends KeyStorage<T> {
    
    public SingletonRegistry() {
    	// Formulated as assertion to disable this potentially costly test in
		// production environments.
    	assert checkConfiguration() : "Not all configured classes could be loaded.";
    }
    
    protected boolean checkConfiguration() {
		boolean result = true;

		IterableConfiguration configuration = getConfiguration();
        for (Iterator<Object> it = configuration.getProperties().values().iterator(); it.hasNext(); ) {
        	String configuredClassName = (String) it.next();
			if (isDeactivated(configuredClassName)) {
				continue;
			}
        	try {
        		// Try to load the configured singleton class to make sure that
				// there are not outdated configuration entries left.
				Class.forName(configuredClassName);
			} catch (ClassNotFoundException ex) {
				Logger.error("Invalid configuration entry for " + this.getClass().getName(), ex, this);
				result = false;
			}
        }

		return result;
	}

	@Override
	protected T newEntry(String propertyName, String configurationValue) throws Exception {
		Factory factory = DefaultConfigConstructorScheme
			.getFactory(ConfigUtil.getClassForNameMandatory(Object.class, propertyName, configurationValue));
		return wrap(factory.createDefaultInstance());
	}

	/**
	 * Returns a T for an object constructed from informations in the configuration, e.g. if the
	 * registry postulates that the configured entries are of type T and an object of type S is
	 * actually configured, then this method produces an object of type T from the configured object
	 * and returns it.
	 * 
	 * @param newEntry
	 *        the object created by definition in the configuration
	 * @return the T which is associated to the configuration the <code>newEntry</code> is
	 *         constructed from
	 * 
	 * @throws ClassCastException
	 *         if it is not possible to create an object of type T from the configured object
	 */
	protected abstract T wrap(Object newEntry) throws ClassCastException;

	private RuntimeException createSecurityError(SecurityException ex) {
		return new RuntimeException(
			"Looking up singleton forbidden due to security policy.", ex);
	}

}
