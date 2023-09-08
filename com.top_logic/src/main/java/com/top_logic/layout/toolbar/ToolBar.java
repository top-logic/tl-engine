/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.toolbar;

import java.util.List;
import java.util.Map;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.Decision;
import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.PropertyObservable;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.control.PopupMenuButtonControl;
import com.top_logic.layout.structure.CollapsibleControl;
import com.top_logic.layout.structure.Expandable;
import com.top_logic.layout.structure.Expandable.ExpansionState;
import com.top_logic.layout.template.NoSuchPropertyException;
import com.top_logic.layout.template.WithProperties;

/**
 * A collection of {@link CommandModel buttons} organized in groups.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ToolBar extends PropertyObservable, WithProperties {

	/** @see #getTitle() */
	public static final String TITLE = "title";

	/** @see #getGroups() */
	public static final String GROUPS_PROPERTY = "groups";

	/** @see #maximized() */
	public static final String MAXIMIZED = "maximized";

	/** @see #minimized() */
	public static final String MINIMIZED = "minimized";

	/** @see #menuButton() */
	public static final String MENU_BUTTON = "menuButton";

	/**
	 * Property key for observing the options {@link #canMaximize()}, {@link #showMaximize()}, and
	 * {@link #showMinimize()}.
	 */
	static final EventType<ToolbarOptionsListener, ToolBar, Boolean> OPTIONS =
		new EventType<>("options") {
			@Override
			public com.top_logic.basic.listener.EventType.Bubble dispatch(ToolbarOptionsListener listener,
					ToolBar sender, Boolean oldValue, Boolean newValue) {
				listener.notifyToolbarOptionsChanged(sender);
				return Bubble.BUBBLE;
			}
		};

	/**
	 * Property key for observing {@link #getGroups()}.
	 */
	static final EventType<ToolbarGroupsListener, ToolBar, List<? extends ToolBarGroup>> GROUPS =
		new EventType<>(GROUPS_PROPERTY) {
			@Override
			public com.top_logic.basic.listener.EventType.Bubble dispatch(ToolbarGroupsListener listener,
					ToolBar sender, List<? extends ToolBarGroup> oldValue, List<? extends ToolBarGroup> newValue) {
				return listener.notifyToolbarGroupsChanged(sender, oldValue, newValue);
			}
		};

	/**
	 * The owner of this {@link ToolBar}.
	 * 
	 * @return Never null.
	 */
	Object getOwner();

	/**
	 * The {@link ToolBarGroup} with the given name.
	 * 
	 * @param name
	 *        The {@link ToolBarGroup#getName() name} of the group to look up.
	 * @return The {@link ToolBarGroup} with the given name, or <code>null</code> if no such group
	 *         exists in this {@link ToolBar}.
	 * 
	 * @see #defineGroup(String)
	 */
	ToolBarGroup getGroup(String name);

	/**
	 * Finds the index of the {@link ToolBarGroup} with the given name.
	 * 
	 * @see #defineGroup(int, String)
	 */
	int getGroupIndex(String name);

	/**
	 * Defines a {@link ToolBarGroup} in this {@link ToolBar} with the given name.
	 * 
	 * @param name
	 *        The name of the {@link ToolBarGroup} to define.
	 * @return The {@link ToolBarGroup} with the given name. If the group existed before, it is
	 *         simply returned. If the group does not yet exist, it is newly created.
	 */
	ToolBarGroup defineGroup(String name);

	/**
	 * Defines a {@link ToolBarGroup} in this {@link ToolBar} with the given name at the given
	 * position.
	 * 
	 * @param beforeIndex
	 *        The index in the {@link #getGroups()} list before which to insert the new group, see
	 *        {@link #getGroupIndex(String)}.
	 * @param name
	 *        The name of the {@link ToolBarGroup} to define.
	 * @return The {@link ToolBarGroup} with the given name. If the group existed before, it is
	 *         simply returned. If the group does not yet exist, it is newly created.
	 */
	ToolBarGroup defineGroup(int beforeIndex, String name);

	/**
	 * Removes the {@link ToolBarGroup} with the given name.
	 * 
	 * @param name
	 *        The name of the {@link ToolBarGroup} to remove.
	 * @return The removed group, or <code>null</code> if no such group did exist in this
	 *         {@link ToolBar}.
	 */
	ToolBarGroup removeGroup(String name);

	/**
	 * All groups currently defined in this {@link ToolBar}.
	 * 
	 * <p>
	 * This property is observable through the {@link #GROUPS} property key.
	 * </p>
	 * 
	 * @see #addListener(EventType, com.top_logic.basic.listener.PropertyListener)
	 */
	List<? extends ToolBarGroup> getGroups();

	/**
	 * Whether there are only empty {@link #getGroups() groups} in this {@link ToolBar}.
	 */
	boolean isEmpty();

	/**
	 * The title for this {@link ToolBar}.
	 */
	HTMLFragment getTitle();

	/**
	 * Setter for {@link #getTitle()}
	 * 
	 * @param title
	 *        New value of {@link #getTitle()}
	 */
	void setTitle(HTMLFragment title);

	/**
	 * The state model storing the expansion state.
	 */
	Expandable getModel();

	/**
	 * Whether this toolbar can be maximized.
	 */
	boolean canMaximize();

	/**
	 * @see #canMaximize()
	 */
	void setCanMaximize(boolean canMaximize);

	/**
	 * Whether the maximize button is shown in the toolbar.
	 * 
	 * <p>
	 * This setting does not influence the ability of this control to actually react on the maximize
	 * event of its {@link #getModel()}. Even if no maximize button is shown in the toolbar, this
	 * control may react on the maximize event of a model displayed elsewhere.
	 * </p>
	 * 
	 * @see #canMaximize()
	 */
	boolean showMaximize();

	/**
	 * @see #showMaximize()
	 */
	void setShowMaximize(Decision showMaximize);

	/**
	 * The default value for the {@link #showMaximize()} Decision.
	 * 
	 * @see CollapsibleControl#CollapsibleControl(ResKey, Expandable, Map, boolean, Decision,
	 *      Decision)
	 */
	void setShowMaximizeDefault(boolean showMaximizeDefault);

	/**
	 * Whether the minimize button is shown in the toolbar.
	 */
	boolean showMinimize();

	/**
	 * @see #showMinimize()
	 */
	void setShowMinimize(Decision showMinimize);

	/**
	 * The default value for the {@link #showMinimize()} {@link Decision}.
	 * 
	 * @see CollapsibleControl#CollapsibleControl(ResKey, Expandable, Map, boolean, Decision,
	 *      Decision)
	 */
	void setShowMinimizeDefault(boolean showMinimize);

	@Override
	default Object getPropertyValue(String propertyName) throws NoSuchPropertyException {
		switch (propertyName) {
			case TITLE:
				return getTitle();
			case GROUPS_PROPERTY:
				return getGroups();
			case MAXIMIZED:
				return maximized();
			case MINIMIZED:
				return minimized();
			case MENU_BUTTON:
				return menuButton();
		}
		return WithProperties.super.getPropertyValue(propertyName);
	}

	/**
	 * Whether the toolbar's owner is maximized.
	 */
	default boolean maximized() {
		return getModel().getExpansionState() == ExpansionState.MAXIMIZED;
	}

	/**
	 * Whether the toolbar's owner is minimized.
	 */
	default boolean minimized() {
		return getModel().getExpansionState() == ExpansionState.MINIMIZED;
	}

	/**
	 * Button opening the toolbar's burger menu.
	 */
	default PopupMenuButtonControl menuButton() {
		return ToolbarControl.createMenuButton(this);
	}
}
