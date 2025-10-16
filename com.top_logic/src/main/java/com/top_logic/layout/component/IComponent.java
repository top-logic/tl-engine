/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.List;

import com.top_logic.basic.Log;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;

/**
 * Common interface of components for composing <i>TopLogic</i> applications.
 *
 * @see LayoutComponent
 */
public interface IComponent extends TypedAnnotatable, ConfiguredInstance<LayoutComponent.Config> {

	/**
	 * Configuration options for {@link IComponent}.
	 */
	public interface ComponentConfig extends ConfigurationItem {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/**
		 * Registers the {@link CommandHandler#getID() ids} of the {@link CommandHandler} that are
		 * intrinsic for the configured component, i.e. the component would not work correctly
		 * without these commands.
		 * 
		 * <p>
		 * The commands must be known in the {@link CommandHandlerFactory}.
		 * </p>
		 * 
		 * @param registry
		 *        {@link CommandRegistry} to register commands.
		 */
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			// By default none.
		}

	}

	/**
	 * The name of this component.
	 *
	 * <p>
	 * If nothing is configured a (unique) synthetic name is returned.
	 * </p>
	 */
	ComponentName getName();

	/**
	 * A user-readable reason, why {@link #canShow()} is <code>false</code>.
	 * 
	 * @return A reason why {@link #canShow()} is <code>false</code>, or <code>null</code> if
	 *         {@link #canShow()} should be <code>true</code>.
	 */
	ResKey hideReason();

	/**
	 * Check if <em>default</em> {@link com.top_logic.tool.boundsec.BoundCommandGroup} is allowed
	 * for the current {@link com.top_logic.knowledge.wrap.person.Person}.
	 * 
	 * This will usually check sub checkers too.
	 * 
	 * @return false, if default command cannot be performed on this object. The default command is
	 *         usually "VIEW"
	 */
	boolean canShow();

	/** The root component of this layout. */
	MainLayout getMainLayout();

	/**
	 * Access to the internal command map.
	 *
	 * @param anID
	 *        The ID of the requested command.
	 * @return The requested command or <code>null</code>, if no such command registered.
	 */
	CommandHandler getCommandById(String anID);

	/**
	 * Model channel with the given channel kind.
	 * 
	 * @param kind
	 *        Kind of channel to retrieve. See
	 *        {@link com.top_logic.layout.channel.ChannelSPI#getName()}.
	 * 
	 * @return The {@link ComponentChannel} of this component with the given name.
	 * 
	 * @throws IllegalArgumentException
	 *         If this component does not support a channel with the given name.
	 */
	ComponentChannel getChannel(String kind);

	/**
	 * Hook that is called during component instantiation for establishing links between component
	 * channels.
	 * 
	 * <p>
	 * If a custom component defines a {@link ComponentChannel} of a custom kind, the component must
	 * override this method to link its custom channel according to its configuration.
	 * </p>
	 * 
	 * <p>
	 * This method can is called more than once, i.e. the component must ensure that potential
	 * listeners have a correct {@link Object#equals(Object)} implementation to avoid accumulation
	 * of listeners with same functionality.
	 * </p>
	 * 
	 * @param log
	 *        Log to write messages and problems to.
	 */
	void linkChannels(Log log);

	/**
	 * Return the model of this component.
	 * 
	 * <p>
	 * If this component is able to edit an object, this method has to return a model. If it returns
	 * no model, a user can only create a new object using this component.
	 * </p>
	 * 
	 * @return The used model, may be <code>null</code>.
	 */
	Object getModel();

	/**
	 * This as {@link LayoutComponent} for compatibility.
	 */
	default LayoutComponent self() {
		return (LayoutComponent) this;
	}

	/**
	 * <code>true</code> if this component was opened as a dialog.
	 */
	boolean openedAsDialog();

	/**
	 * Closes the dialog, this {@link LayoutComponent} is part of.
	 * 
	 * <p>
	 * If this component is not part of an open dialog, nothing happens.
	 * </p>
	 */
	void closeDialog();

	/**
	 * Check if this component handles the given type.
	 *
	 * The type may either be a classname or (in TopLogic) some other meta-type. When this function
	 * returns true, supports Objects shoud be true, too.
	 * 
	 * @param type
	 *        the type. If <code>null</code> or empty false is returned
	 * @return true if the component handles the type
	 */
	boolean isDefaultFor(String type);

	/**
	 * The container, this component is part of.
	 * 
	 * <p>
	 * The top-level component is of type {@link MainLayout} and has no parent.
	 * </p>
	 */
	IComponent getParent();

	/**
	 * The dialogs registered at this component.
	 */
	List<? extends IComponent> getDialogs();

}
