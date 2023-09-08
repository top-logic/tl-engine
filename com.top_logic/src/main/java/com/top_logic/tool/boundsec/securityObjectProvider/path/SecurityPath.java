/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.securityObjectProvider.path;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.securityObjectProvider.PathSecurityObjectProvider;

/**
 * Path step in the path chain of a {@link PathSecurityObjectProvider} to finally compute a security
 * object.
 * 
 * <p>
 * An {@link SecurityPath} may either delegate to a different component for the next path step
 * ({@link #nextComponent(LayoutComponent, int, int)}) or computes the model for a component
 * ({@link #getModel(LayoutComponent, Object, BoundCommandGroup, int, int)}).
 * </p>
 * 
 * <p>
 * First the {@link SecurityPath} is asked for the next component of component "A". If the result is
 * <code>null</code>, it is responsible to determine the security object for component "A".
 * </p>
 * 
 * @see PathSecurityObjectProvider
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class SecurityPath
		implements ConfiguredInstance<PolymorphicConfiguration<? extends SecurityPath>> {

	/**
	 * Computes the component to use as base component in the next path step.
	 * 
	 * @param component
	 *        The component to compute next component from.
	 * @param pathIndex
	 *        The index of this {@link SecurityPath} in the path chain.
	 * @param size
	 *        The size of the whole chain.
	 * 
	 * @return Returns the base component for the next path step. May be <code>null</code> which
	 *         means that this {@link SecurityPath} determines the actual model of the path chain.
	 */
	public abstract LayoutComponent nextComponent(LayoutComponent component, int pathIndex, int size);

	/**
	 * Computes the actual value of
	 * {@link PathSecurityObjectProvider#getModel(BoundChecker, Object, BoundCommandGroup)}
	 * 
	 * @param component
	 *        The component to get model for.
	 * @param model
	 *        The model of the given component or a potential model.
	 * @param group
	 *        The {@link BoundCommandGroup} to get security object for.
	 * @param pathIndex
	 *        The index of this {@link SecurityPath} in the path chain
	 * @param size
	 *        The size of the whole chain.
	 * 
	 * @return The security object for the given component.
	 */
	public abstract Object getModel(LayoutComponent component, Object model, BoundCommandGroup group, int pathIndex, int size);

	/**
	 * New {@link Component.Config} for the given component name.
	 */
	public static final Component.Config componentConfig(ComponentName name) {
		Component.Config config = TypedConfiguration.newConfigItem(Component.Config.class);
		config.setName(name);
		return config;
	}

	/** Singleton {@link CurrentObject} instance. */
	public static final SecurityPath currentObject() {
		return CurrentObject.INSTANCE;
	}

	/** Singleton {@link Window} instance. */
	public static final SecurityPath window() {
		return Window.INSTANCE;
	}

	/** Singleton {@link Selection} instance. */
	public static final SecurityPath selection() {
		return Selection.INSTANCE;
	}

	/** Singleton {@link SelectableMaster} instance. */
	public static final SecurityPath selectablemaster() {
		return SelectableMaster.INSTANCE;
	}

	/** Singleton {@link Parent} instance. */
	public static final SecurityPath parent() {
		return Parent.INSTANCE;
	}

	/** Singleton {@link Opener} instance. */
	public static final SecurityPath opener() {
		return Opener.INSTANCE;
	}

	/** Singleton {@link Model} instance. */
	public static final SecurityPath model() {
		return Model.INSTANCE;
	}

	/** Singleton {@link Master} instance. */
	public static final SecurityPath master() {
		return Master.INSTANCE;
	}

}
