/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import com.top_logic.basic.Configuration.IterableConfiguration;

/**
 * Implementation of the {@link Reloadable} interface for all reloadable classes
 * that are not otherwise bound in the choice of their super-class (e.g. system
 * services).
 * 
 * <p>
 * This class provides access to the configuration section applicable to the
 * concrete extending sub-class (see {@link #getConfiguration()}). This
 * implementation cares about invalidating and reloading this configuration
 * section, whenever a {@link #reload()} was triggered by the
 * {@link ReloadableManager}. If a concrete subclass has additional state that
 * must be reset upon reload, it must override the {@link #reload()} method,
 * call the respective super-implementation and perform additional cleanup or
 * re-initialization afterwards.
 * </p>
 * 
 * @author <a href="mailto:mga@top-logic.com"">Michael G&auml;nsler</a>
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractReloadable implements Reloadable {    

    /**
	 * The configuration of this {@link Reloadable}.
	 * 
	 * <p>
	 * Note: Lazily initialized. Subclasses must access their configuration with
	 * {@link #getConfiguration()}.
	 * </p>
	 */
    private IterableConfiguration configuration;

    /**
	 * Creates a new {@link Reloadable} and registers it with the
	 * {@link ReloadableManager}.
	 * 
	 * <p>
	 * Only a {@link Reloadable} that is
	 * {@link ReloadableManager#addReloadable(Reloadable) registered} with the
	 * {@link ReloadableManager} can be
	 * {@link ReloadableManager#reload() reloaded by an administrator}.
	 * </p>
	 */
    protected AbstractReloadable() {
    	// If an instance of Reloadable would register itself with the
		// ReloadableManager, the reload() method could be called, before the
    	// Reloadable instance is completely initialized. This problem can
		// occur in a sub-class of the reloadable class that registers itself 
    	// from within its constructor at the ReloadableManager. If the reload() 
    	// event occurs, after the super-class has registered the new instance with
		// the ReloadableManager, but before the constructor of the instance in
		// question has finished, the reload() method of the new instance is
		// executed before its constructor has completed. 
    	assert ! ReloadableManager.getInstance().isReloadableRegistered(this) : 
    		"A Reloadable instance must not register itself from within its " + 
    		"constructor with the ReloadableManager. ";
    }

    /**
	 * Registers this instance with the {@link ReloadableManager} to make it
	 * listen for future {@link #reload()} events. 
	 */
	public final void registerForReload() {
		ReloadableManager.getInstance().addReloadable(this);
	}

    /**
	 * Unregisters this instance with the {@link ReloadableManager} to prevent
	 * future {@link #reload()} events.
	 */
	public final void unregisterFromReload() {
		ReloadableManager.getInstance().removeReloadable(this);
	}

    /**
	 * Invalidates the
	 * {@link #getConfiguration() cached view of the configuration}.
	 * 
	 * <p>
	 * Subclasses can override this method to perform additional cleanups on
	 * reload, but overriding implementations must call their
	 * super-implementation.
	 * </p>
	 * 
	 * @see Reloadable#reload()
	 */
    @Override
	public synchronized boolean reload () {
        this.configuration = null;
        return true;
    }

    /**
	 * Returns the value from the {@link #getConfiguration() configuration} for
	 * the given configuration key.
	 * 
	 * <p>
	 * The configurable property is looked up with the given key from the
	 * {@link #getConfiguration() configuration} of this instance and returned.
	 * If there is no property with the given key, <code>null</code> is
	 * returned.
	 * </p>
	 * 
	 * @param aConfigurationKey
	 *     The name of the requested property.
	 * @return The configured property value or null, if the property is not
	 *     configured.
	 */
    public final String getProperty (String aConfigurationKey) {
        return this.getConfiguration ().getValue (aConfigurationKey);
    }

    /**
	 * Returns the value from the {@link #getConfiguration() configuration} for
	 * the given configuration key.
	 * 
	 * <p>
	 * The configurable property is looked up with the given key from the
	 * {@link #getConfiguration() configuration} of this instance and returned.
	 * If there is no property with the given key, <code>null</code> is
	 * returned.
	 * </p>
	 * 
	 * @param aConfigurationKey
	 *     The name of the requested property.
	 * @param aDefault
	 *     A default value to return, if the requested property is not 
	 *     configured.
	 * @return The configured property value or the given default, if the
	 *     property is not configured.
	 */
    public final String getProperty (String aConfigurationKey, String aDefault) {
        return this.getConfiguration ().getValue (aConfigurationKey, aDefault);
    }

    /**
	 * Return the {@link Configuration} of this instance.
	 * 
	 * @return The section of the {@link Configuration} applicable to this
	 *     instance.
	 */
    public synchronized final IterableConfiguration getConfiguration () {
        if (this.configuration == null) {
            this.configuration = createConfiguration();
			checkConfigurationInternal(configuration);
        }
        return this.configuration;
    }

	private void checkConfigurationInternal(IterableConfiguration config) {
		try {
			checkConfiguration(config);
		} catch (RuntimeException ex) {
			String message = "Checking the configuration of " + getClass().getSimpleName() + " failed."
				+ " Cause: " + ex.getMessage();
			Logger.error(message, new RuntimeException(message, ex));
		}
	}

	/**
	 * Hook for subclasses to check the configuration.
	 * <p>
	 * This is called immediately after the configuration has been created. It is called only once.
	 * As the configuration is created lazily, if it is never accessed, this method is never called.
	 * </p>
	 * <p>
	 * {@link RuntimeException}s throw by subclasses are caught and logged. {@link Error}s are not
	 * caught.
	 * </p>
	 * 
	 * @param config
	 *        The config to check.
	 */
	protected void checkConfiguration(IterableConfiguration config) {
		// Nothing to check, this is only a hook for subclasses.
	}

	/**
	 * Creates a configuration for this {@link AbstractReloadable}.
	 * 
	 * @return the {@link IterableConfiguration} which will be returned by
	 *         {@link #getConfiguration()}
	 */
	protected synchronized IterableConfiguration createConfiguration() {
		return Configuration.getConfiguration(getClass());
	}

    /**
	 * Reports, whether the {@link #getConfiguration() configuration} was
	 * accessed since the last call to {@link #reload()}.
	 * 
	 * @see Reloadable#usesXMLProperties() 
	 */
    @Override
	public boolean usesXMLProperties () {
        return this.configuration != null;
    }
}
