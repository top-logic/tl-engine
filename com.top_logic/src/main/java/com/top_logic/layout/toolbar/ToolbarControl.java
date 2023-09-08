/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.toolbar;

import java.io.IOException;
import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.AttachedPropertyListener;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.DefaultPopupMenuModel;
import com.top_logic.layout.basic.RenderErrorUtil;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.basic.contextmenu.menu.MenuGroup;
import com.top_logic.layout.form.component.I18NConstants;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.PopupMenuButtonControl;
import com.top_logic.util.ToBeValidated;

/**
 * {@link Control} rendering a {@link ToolBar}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ToolbarControl extends AbstractControlBase implements ToolbarGroupsListener, ToolBarGroupContentsListener {

	/**
	 * CSS class for each toolbar button group.
	 */
	private static final String TOOLBAR_GROUP_CSS_CLASS = "tbGroup";

	private final ToolBar _toolbar;

	/**
	 * Creates a {@link ToolbarControl}.
	 * 
	 * @param toolbar
	 *        The toolbar to render.
	 */
	public ToolbarControl(ToolBar toolbar) {
		_toolbar = toolbar;
	}

	@Override
	public ToolBar getModel() {
		return _toolbar;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	protected boolean hasUpdates() {
		return false;
	}

	@Override
	protected void internalRevalidate(DisplayContext context, UpdateQueue actions) {
		// Ignore.
	}

	@Override
	protected String getTypeCssClass() {
		return "cToolbar";
	}

	@Override
	protected void attachRevalidated() {
		super.attachRevalidated();
		_toolbar.addListener(ToolBar.GROUPS, this);
		_toolbar.addListener(ToolBarGroup.CONTENTS, this);
	}

	@Override
	protected void detachInvalidated() {
		_toolbar.removeListener(ToolBar.GROUPS, this);
		_toolbar.removeListener(ToolBarGroup.CONTENTS, this);
		super.detachInvalidated();
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();

		writeToolbarContents(context, out, SPAN, TOOLBAR_GROUP_CSS_CLASS, _toolbar);

		out.endTag(SPAN);
	}

	@Override
	public Bubble notifyToolbarGroupsChanged(ToolBar sender, List<? extends ToolBarGroup> oldValue,
			List<? extends ToolBarGroup> newValue) {
		requestRepaint();
		return Bubble.BUBBLE;
	}

	@Override
	public Bubble notifyToolBarGroupContentsChanged(ToolBarGroup sender, List<HTMLFragment> oldValue,
			List<HTMLFragment> newValue) {
		requestRepaint();
		return Bubble.BUBBLE;
	}

	/**
	 * Renders a fragment displaying all groups of the given toolbar.
	 * 
	 * @param context
	 *        The rendering context.
	 * @param out
	 *        The writer to write to.
	 * @param groupTag
	 *        The HTML tag to surround each group with.
	 * @param groupClass
	 *        The CSS class to set on each group tag.
	 * @param toolbar
	 *        The {@link ToolBar} to render.
	 * @throws IOException
	 *         If writing fails.
	 */
	public static void writeToolbarContents(DisplayContext context, TagWriter out, String groupTag, String groupClass,
			final ToolBar toolbar)
			throws IOException {
		boolean hasMenu = false;
		for (ToolBarGroup group : toolbar.getGroups()) {
			if (group.isMenuGroup()) {
				hasMenu = true;
				continue;
			}
			out.beginBeginTag(groupTag);
			out.writeAttribute(CLASS_ATTR, groupClass);
			out.endBeginTag();
			int currentDepth = out.getDepth();
			for (HTMLFragment button : group.getViews()) {
				try {
					button.write(context, out);
				} catch (Throwable throwable) {
					out.endAll(currentDepth);
					RenderErrorUtil.produceErrorOutput(context, out, I18NConstants.ERROR_VIEW_CREATION,
						"Rendering error occured during rendering button '" + button + "' of group '" + group.getName()
							+ "' in toolbar '" + toolbar + "' .",
						throwable, ToolbarControl.class);
				}
			}
			out.endTag(groupTag);
		}
	
		if (hasMenu) {
			out.beginBeginTag(groupTag);
			out.writeAttribute(CLASS_ATTR, groupClass);
			out.endBeginTag();

			PopupMenuButtonControl menuControl = createMenuButton(toolbar);
			menuControl.write(context, out);

			out.endTag(groupTag);
		}
	}

	/**
	 * Creates the toolbar's burger menu.
	 */
	public static PopupMenuButtonControl createMenuButton(final ToolBar toolbar) {
		Menu menu = createMenu(toolbar);
		DefaultPopupMenuModel popupMenu = new DefaultPopupMenuModel(Icons.BUTTON_BURGER_MENU, menu);
		class Updater implements AttachedPropertyListener, ToolbarGroupsListener, ToolBarGroupContentsListener,
				ToBeValidated {

			private boolean _attached;

			private boolean _invalid;

			@Override
			public void handleAttachEvent(AbstractControlBase sender, Boolean oldValue, Boolean newValue) {
				if (newValue) {
					attach();
				} else {
					detach();
				}
			}

			private void attach() {
				attachListeners();
				_attached = true;
				_invalid = false;
			}

			private void attachListeners() {
				toolbar.addListener(ToolBar.GROUPS, this);
				toolbar.addListener(ToolBarGroup.CONTENTS, this);
			}

			@Override
			public Bubble notifyToolBarGroupContentsChanged(ToolBarGroup sender, List<HTMLFragment> oldValue,
					List<HTMLFragment> newValue) {
				invalidate();
				return Bubble.BUBBLE;
			}

			private void detach() {
				if (!_attached) {
					return;
				}
				_attached = false;
				_invalid = true;
				detachListeners();
			}

			private void detachListeners() {
				toolbar.removeListener(ToolBar.GROUPS, this);
				toolbar.removeListener(ToolBarGroup.CONTENTS, this);
			}

			@Override
			public Bubble notifyToolbarGroupsChanged(ToolBar sender, List<? extends ToolBarGroup> oldValue,
					List<? extends ToolBarGroup> newValue) {
				invalidate();
				return Bubble.BUBBLE;
			}

			private void invalidate() {
				if (_invalid) {
					return;
				}
				detachListeners();
				_invalid = true;
				DefaultDisplayContext.getDisplayContext().getLayoutContext().notifyInvalid(this);
			}

			@Override
			public void validate(DisplayContext validationContext) {
				if (!_attached) {
					return;
				}
				if (!_invalid) {
					return;
				}
				popupMenu.setCommands(createMenu(toolbar));
				_invalid = false;
				attachListeners();
			}

		}

		Updater updater = new Updater();
		PopupMenuButtonControl menuControl = new PopupMenuButtonControl(popupMenu, DefaultToolBar.BUTTON_RENDERER);
		menuControl.addListener(AbstractControlBase.ATTACHED_PROPERTY, updater);
		return menuControl;
	}

	/**
	 * Creates a {@link Menu} from the commands in the given {@link ToolBar}.
	 */
	public static Menu createMenu(ToolBar toolbar) {
		return createMenu(toolbar, true);
	}

	/**
	 * Creates a {@link Menu} from the commands in the given {@link ToolBar}.
	 */
	public static Menu createMenu(ToolBar toolbar, boolean onlyMenuCommands) {
		Menu menu = new Menu();
		for (ToolBarGroup group : toolbar.getGroups()) {
			if (onlyMenuCommands && !group.isMenuGroup()) {
				continue;
			}

			MenuGroup groupButtons = new MenuGroup();

			for (HTMLFragment view : group.getViews()) {
				// Note: In pop-up menus, only buttons are supported.
				if (view instanceof ButtonControl) {
					groupButtons.add(((ButtonControl) view).getModel());
				}
			}

			if (!groupButtons.isEmpty()) {
				menu.add(groupButtons);
			}
		}
		return menu;
	}
}
