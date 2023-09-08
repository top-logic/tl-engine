/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.toolbar;

import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.template.NoSuchPropertyException;
import com.top_logic.layout.template.WithProperties;

/**
 * A group of commands in a {@link ToolBar}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ToolBarGroup extends WithProperties {

	/** @see #getViews() */
	public static final String VIEWS = "views";

	/** @see #isMenuGroup() */
	public static final String IS_MENU = "isMenu";

	/**
	 * Property key for observing {@link #getViews()}.
	 */
	static final EventType<ToolBarGroupContentsListener, ToolBarGroup, List<HTMLFragment>> CONTENTS =
		new EventType<>("contents") {
			@Override
			public com.top_logic.basic.listener.EventType.Bubble dispatch(ToolBarGroupContentsListener listener,
					ToolBarGroup sender, List<HTMLFragment> oldValue, List<HTMLFragment> newValue) {
				return listener.notifyToolBarGroupContentsChanged(sender, oldValue, newValue);
			}
		};

	/**
	 * The owning {@link ToolBar}.
	 */
	ToolBar getToolbar();

	/**
	 * The identifier of this group.
	 * 
	 * @see ToolBar#getGroup(String)
	 */
	String getName();

	/**
	 * Whether this group is displayed in the toolbar's burger menu.
	 */
	boolean isMenuGroup();

	/**
	 * Appends a view to this {@link ToolBarGroup}.
	 * 
	 * @param view
	 *        The new view to add.
	 */
	void addView(HTMLFragment view);

	/**
	 * Convenience method adding all given views.
	 * 
	 * @see #addView(HTMLFragment)
	 */
	void addViews(List<? extends HTMLFragment> views);

	/**
	 * Removes a view from this {@link ToolBarGroup}.
	 * 
	 * @param view
	 *        The view to remove.
	 * @return Whether the given view was previously a member of this group.
	 */
	boolean removeView(HTMLFragment view);

	/**
	 * Removes a button from this {@link ToolBarGroup}.
	 * 
	 * @param viewIndex
	 *        The index of the button to remove.
	 * @return The removed button.
	 */
	HTMLFragment removeView(int viewIndex);

	/**
	 * List of all views in this group.
	 * 
	 * <p>
	 * This property is observable through the {@link #CONTENTS} property key by adding a
	 * {@link PropertyListener} to the owning {@link #getToolbar()}.
	 * </p>
	 */
	List<HTMLFragment> getViews();

	/**
	 * Whether this group contains no tools.
	 */
	boolean isEmpty();

	/**
	 * Convenience method to add a {@link ButtonControl} view.
	 * 
	 * @see #addView(HTMLFragment)
	 */
	void addButton(CommandModel button);

	/**
	 * Convenience method to add several {@link ButtonControl} views.
	 * 
	 * @see #addViews(List)
	 */
	void addButtons(List<? extends CommandModel> buttons);

	/**
	 * Convenience method to remove a {@link ButtonControl} view identified by its
	 * {@link ButtonControl#getModel() model}.
	 * 
	 * @see #removeView(HTMLFragment)
	 */
	boolean removeButton(CommandModel button);

	@Override
	default Object getPropertyValue(String propertyName) throws NoSuchPropertyException {
		switch (propertyName) {
			case IS_MENU:
				return isMenuGroup();
			case VIEWS:
				return getViews();
		}
		return WithProperties.super.getPropertyValue(propertyName);
	}
}
