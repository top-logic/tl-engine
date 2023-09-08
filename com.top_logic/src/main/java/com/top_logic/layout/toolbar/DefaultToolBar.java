/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.toolbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.Decision;
import com.top_logic.basic.listener.PropertyObservableBase;
import com.top_logic.basic.util.Utils;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.form.control.AbstractButtonRenderer;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.ImageButtonRenderer;
import com.top_logic.layout.form.popupmenu.MenuButtonRenderer;
import com.top_logic.layout.structure.Expandable;
import com.top_logic.layout.structure.WindowModel;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;

/**
 * Default {@link ToolBar} implementation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultToolBar extends PropertyObservableBase implements ToolBar {

	/**
	 * Default value for {@link #showMaximize()}.
	 */
	private static final boolean DEFAULT_SHOW_MAXIMIZE = true;

	/**
	 * Default value for {@link #showMinimize()}.
	 */
	private static final boolean DEFAULT_SHOW_MINIMIZE = true;

	private static final int OFFSET = 1024;

	/**
	 * {@link AbstractButtonRenderer} used to display buttons in the toolbar.
	 */
	public static final AbstractButtonRenderer<?> BUTTON_RENDERER = ImageButtonRenderer.INSTANCE;

	private final List<Group> _groups = new ArrayList<>();

	private final List<ToolBarGroup> _groupsUnmodifiable = Collections.<ToolBarGroup> unmodifiableList(_groups);

	private final Object _owner;

	private HTMLFragment _title;

	private boolean _canMaximize;

	private Decision _showMaximize;

	private Decision _showMinimize;

	private boolean _showMinimizeDefault = DEFAULT_SHOW_MINIMIZE;

	private boolean _showMaximizeDefault = DEFAULT_SHOW_MAXIMIZE;

	private Expandable _model;

	/**
	 * Create a {@link DefaultToolBar} for the given owner.
	 * 
	 * @param owner
	 *        Is not allowed to be null.
	 * @param model
	 *        See {@link #getModel()}
	 */
	public DefaultToolBar(Object owner, Expandable model) {
		this(owner, model, Fragments.empty(), true, Decision.DEFAULT, Decision.DEFAULT);
	}

	/**
	 * Create a {@link DefaultToolBar} for the given owner.
	 * 
	 * @param owner
	 *        Is not allowed to be null.
	 * @param model
	 *        See {@link #getModel()}
	 * @param canMaximize
	 *        See {@link #canMaximize()}.
	 * @param showMaximize
	 *        See {@link #showMaximize()}, {@link #setShowMaximizeDefault(boolean)}.
	 * @param showMinimize
	 *        See {@link #showMinimize()}, {@link #setShowMinimizeDefault(boolean)}.
	 */
	public DefaultToolBar(Object owner, Expandable model, HTMLFragment title, boolean canMaximize,
			Decision showMaximize, Decision showMinimize) {
		_model = model;
		_canMaximize = canMaximize;
		_showMaximize = showMaximize;
		_showMinimize = showMinimize;
		_owner = Utils.requireNonNull(owner);
		_title = title;
	}

	@Override
	public Expandable getModel() {
		return _model;
	}

	@Override
	public Object getOwner() {
		return _owner;
	}

	@Override
	public boolean canMaximize() {
		return _canMaximize;
	}

	@Override
	public void setCanMaximize(boolean canMaximize) {
		boolean oldValue = canMaximize();
		_canMaximize = canMaximize;
		boolean newValue = canMaximize();
		if (newValue != oldValue) {
			notifyListeners(ToolBar.OPTIONS, this, oldValue, newValue);
		}
	}

	@Override
	public boolean showMaximize() {
		return _showMaximize.toBoolean(_showMaximizeDefault);
	}

	@Override
	public void setShowMaximize(Decision showMaximize) {
		boolean oldValue = showMaximize();
		_showMaximize = showMaximize;
		boolean newValue = showMaximize();
		if (newValue != oldValue) {
			notifyListeners(ToolBar.OPTIONS, this, oldValue, newValue);
		}
	}

	@Override
	public void setShowMaximizeDefault(boolean showMaximizeDefault) {
		boolean oldValue = showMaximize();
		_showMaximizeDefault = showMaximizeDefault;
		boolean newValue = showMaximize();
		if (newValue != oldValue) {
			notifyListeners(ToolBar.OPTIONS, this, oldValue, newValue);
		}
	}

	@Override
	public boolean showMinimize() {
		return _showMinimize.toBoolean(_showMinimizeDefault);
	}

	@Override
	public void setShowMinimize(Decision showMinimize) {
		boolean oldValue = showMinimize();
		_showMinimize = showMinimize;
		boolean newValue = showMinimize();
		if (newValue != oldValue) {
			notifyListeners(ToolBar.OPTIONS, this, oldValue, newValue);
		}
	}

	@Override
	public void setShowMinimizeDefault(boolean showMinimize) {
		boolean oldValue = showMinimize();
		_showMinimizeDefault = showMinimize;
		boolean newValue = showMinimize();
		if (newValue != oldValue) {
			notifyListeners(ToolBar.OPTIONS, this, oldValue, newValue);
		}
	}

	@Override
	public ToolBarGroup getGroup(String name) {
		int index = getGroupIndex(name);
		if (index < 0) {
			return null;
		}
		return _groups.get(index);
	}

	@Override
	public int getGroupIndex(String name) {
		for (int n = 0, cnt = _groups.size(); n < cnt; n++) {
			ToolBarGroup group = _groups.get(n);
			if (name.equals(group.getName())) {
				return n;
			}
		}
		return -1;
	}

	@Override
	public ToolBarGroup defineGroup(String name) {
		ToolBarGroup result = getGroup(name);
		if (result != null) {
			return result;
		}

		int priority = CommandHandlerFactory.getInstance().getCliqueIndex(name);
		return addGroup(name, priority);
	}

	@Override
	public ToolBarGroup defineGroup(int beforeIndex, String name) {
		if (beforeIndex > _groups.size() || beforeIndex < 0) {
			throw new IllegalArgumentException("Invalid insert position " + beforeIndex + ", only " + _groups.size()
				+ " groups exist.");
		}

		int priority;
		if (beforeIndex == _groups.size()) {
			// add to end of group
			if (beforeIndex > 0) {
				priority = _groups.get(beforeIndex - 1).getPriority() + OFFSET;
			} else {
				priority = OFFSET;
			}
		} else {
			// add to certain position
			if (beforeIndex > 0) {
				priority = (_groups.get(beforeIndex - 1).getPriority() + _groups.get(beforeIndex).getPriority()) / 2;
			} else {
				priority = _groups.get(beforeIndex).getPriority() - OFFSET;
			}
		}

		return addGroup(name, priority);
	}

	private Group addGroup(String name, int priority) {
		boolean inMenu =
			CommandHandlerFactory.getInstance().getDisplay(name) == CommandHandler.Display.MENU;
		Group newGroup = new Group(name, priority, inMenu);
		int index = Collections.binarySearch(_groups, newGroup, GroupComparator.INSTANCE);
		if (index < 0) {
			index = (-index - 1);
		}
		insertGroup(index, newGroup);
		return newGroup;
	}

	private void insertGroup(int index, Group newGroup) {
		if (hasListeners()) {
			List<Group> before = new ArrayList<>(_groups);
			_groups.add(index, newGroup);
			notifyListeners(GROUPS, this, before, _groupsUnmodifiable);
		} else {
			_groups.add(index, newGroup);
		}
	}

	@Override
	public ToolBarGroup removeGroup(String name) {
		int index = groupIndex(name);
		if (index >= 0) {
			if (hasListeners()) {
				List<Group> before = new ArrayList<>(_groups);
				Group result = _groups.remove(index);
				notifyListeners(GROUPS, this, before, _groupsUnmodifiable);
				return result;
			} else {
				Group result = _groups.remove(index);
				return result;
			}
		} else {
			return null;
		}
	}

	private int groupIndex(String name) {
		for (int n = 0, cnt = _groups.size(); n < cnt; n++) {
			ToolBarGroup group = _groups.get(n);
			if (name.equals(group.getName())) {
				return n;
			}
		}
		return -1;
	}

	@Override
	public List<? extends ToolBarGroup> getGroups() {
		return _groupsUnmodifiable;
	}

	@Override
	public boolean isEmpty() {
		for (ToolBarGroup group : _groups) {
			if (!group.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	final void notifyContentChanged(ToolBarGroup sender, List<HTMLFragment> oldValue, List<HTMLFragment> newValue) {
		super.notifyListeners(ToolBarGroup.CONTENTS, sender, oldValue, newValue);
	}

	final boolean hasContentListeners() {
		return hasListeners();
	}

	/**
	 * {@link ToolBarGroup} in a {@link DefaultToolBar}.
	 */
	class Group implements ToolBarGroup {

		private final String _name;

		private final List<HTMLFragment> _views = new ArrayList<>();

		private final List<HTMLFragment> _viewsUnmodifiable = Collections.unmodifiableList(_views);

		private final int _priority;

		private final boolean _menu;

		public Group(String name, int priority, boolean menu) {
			_name = name;
			_priority = priority;
			_menu = menu;
		}

		@Override
		public ToolBar getToolbar() {
			return DefaultToolBar.this;
		}

		@Override
		public String getName() {
			return _name;
		}

		@Override
		public boolean isMenuGroup() {
			return _menu;
		}

		public int getPriority() {
			return _priority;
		}

		@Override
		public List<HTMLFragment> getViews() {
			return _viewsUnmodifiable;
		}

		@Override
		public void addView(HTMLFragment view) {
			if (hasContentListeners()) {
				List<HTMLFragment> before = new ArrayList<>(_views);
				_views.add(view);
				notifyContentChanged(this, before, _viewsUnmodifiable);
			} else {
				_views.add(view);
			}
		}

		@Override
		public void addViews(List<? extends HTMLFragment> buttons) {
			if (hasContentListeners()) {
				List<HTMLFragment> before = new ArrayList<>(_views);
				_views.addAll(buttons);
				notifyContentChanged(this, before, _viewsUnmodifiable);
			} else {
				_views.addAll(buttons);
			}
		}

		@Override
		public boolean removeView(HTMLFragment button) {
			int index = _views.indexOf(button);
			boolean found = index >= 0;
			if (found) {
				removeView(index);
			}
			return found;
		}

		@Override
		public HTMLFragment removeView(int index) {
			HTMLFragment result;
			if (hasContentListeners()) {
				List<HTMLFragment> before = new ArrayList<>(_views);
				result = _views.remove(index);
				notifyContentChanged(this, before, _viewsUnmodifiable);
			} else {
				result = _views.remove(index);
			}
			return result;
		}

		@Override
		public boolean isEmpty() {
			return _views.isEmpty();
		}

		@Override
		public void addButton(CommandModel button) {
			addView(new ButtonControl(button,
				isMenuGroup() ? MenuButtonRenderer.INSTANCE : BUTTON_RENDERER));
		}

		@Override
		public void addButtons(List<? extends CommandModel> buttons) {
			for (CommandModel button : buttons) {
				addButton(button);
			}
		}

		@Override
		public boolean removeButton(CommandModel button) {
			int index = getButtonIndex(button);
			boolean found = index >= 0;
			if (found) {
				removeView(index);
			}
			return found;
		}

		private int getButtonIndex(CommandModel button) {
			for (int n = 0, cnt = _views.size(); n < cnt; n++) {
				HTMLFragment view = _views.get(n);
				
				if (view instanceof ButtonControl) {
					if (((ButtonControl) view).getModel() == button) {
						return n;
					}
				}
			}
			return -1;
		}
	}

	static class GroupComparator implements Comparator<Group> {
		/**
		 * Singleton {@link GroupComparator} instance.
		 */
		public static final GroupComparator INSTANCE = new GroupComparator();

		private GroupComparator() {
			// Singleton constructor.
		}

		@Override
		public int compare(Group g1, Group g2) {
			return CollectionUtil.compareInt(g1.getPriority(), g2.getPriority());
		}
	}

	@Override
	public HTMLFragment getTitle() {
		return _title;
	}

	@Override
	public void setTitle(HTMLFragment title) {
		if (title == null) {
			throw new IllegalArgumentException("Title must not be null.");
		}
		if (Utils.equals(_title, title)) {
			return;
		}
		HTMLFragment oldTitle = _title;
		_title = title;
		notifyListeners(WindowModel.TITLE_PROPERTY, this, oldTitle, _title);
	}

}
