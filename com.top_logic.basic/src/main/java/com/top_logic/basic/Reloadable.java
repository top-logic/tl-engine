/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;


/**
 * Interface for components that support reloading their configuration or
 * updating their internal state for other sources.
 * 
 * <p>
 * Reloading is triggered from a central administrative interface, where either
 * all {@link Reloadable}s, or a selected subset can be manually reset. To make
 * this central administration of {@link Reloadable}s work, each
 * {@link Reloadable} instance must
 * {@link ReloadableManager#addReloadable(Reloadable) register} with the
 * {@link ReloadableManager}. If the concrete {@link Reloadable} implementation
 * is a singleton, it must register itself after creating its instance object
 * within its <code>getInstance()</code> method. If the concrete
 * {@link Reloadable} is created from a {@link SingletonRegistry} it's the
 * registry's responsibility to register the {@link Reloadable}.
 * </p>
 * 
 * <p>
 * Implementations should extend {@link AbstractReloadable}, if they are free
 * to choose their super-class. This provides convenient access to the classes
 * specific configuration and automatic reload of this configuration.
 * </p>
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Reloadable {

    /**
	 * Hook method that is called by the {@link ReloadableManager} to trigger a
	 * reset of the implementing class (which actually causes an immediate or
	 * deferred reload of the classes configuration).
	 * 
	 * @return <code>true</code>, if the reload succeeded.
	 */
    public boolean reload ();

    /**
	 * Returns an internationalized user-understandable name of the
	 * {@link Reloadable reloadable} class that can be displayed in an
	 * administrative user interface.
	 * 
	 * @return A display name of the implementing class, or <code>null</code>,
	 *     if this {@link Reloadable} should not be displayed in the user
	 *     interface.
	 */
    public String getName ();

    /**
	 * Returns an internationalized user-understandable description of the
	 * functionality of the implementing class.
	 * 
	 * <p>
	 * The returned description can be displayed in an administrative user
	 * interface.
	 * </p>
	 * 
	 * @return A description of the functionality of this class, or
	 *     <code>null</code>, if no such description is available.
	 */
    public String getDescription ();

    /**
	 * Checks, whether a reload of the {@link XMLProperties} is necessary before
	 * a {@link #reload() reload of this instance} can be performed.
	 * 
	 * <p>
	 * The {@link ReloadableManager} decides upon the result of this method, if
	 * it must reload the {@link XMLProperties} before it can trigger a
	 * {@link #reload()} of this instance.
	 * </p>
	 * 
	 * @return <code>true</code>, if a {@link #reload()} of this instance
	 *     requires a reload of the {@link XMLProperties} as prerequisite.
	 */
    public boolean usesXMLProperties ();
}
