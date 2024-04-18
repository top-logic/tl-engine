/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ButtonUIModel;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelAdapter;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.PopupMenuModel;
import com.top_logic.layout.basic.contextmenu.menu.AbstractMenuContents;
import com.top_logic.layout.basic.contextmenu.menu.CommandItem;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.basic.contextmenu.menu.MenuGroup;
import com.top_logic.layout.basic.contextmenu.menu.MenuItem;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.component.configuration.GuiInspectorCommand;
import com.top_logic.layout.form.popupmenu.I18NConstants;
import com.top_logic.layout.form.popupmenu.MenuButtonRenderer;
import com.top_logic.layout.form.popupmenu.PopupCommandNaming;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DefaultPopupDialogModel;
import com.top_logic.layout.structure.DialogClosedListener;
import com.top_logic.layout.structure.PopupDialogControl;
import com.top_logic.layout.structure.PopupDialogControl.HorizontalPopupPosition;
import com.top_logic.layout.structure.PopupDialogControl.VerticalPopupPosition;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link Control}, used to display and handle menus, displayed in a {@link PopupDialogControl}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class PopupMenuButtonControl extends AbstractButtonControl<PopupMenuModel> implements DialogClosedListener {

	private static final String POPUP_MENU_CLASS = "popupMenu";

	private static final String MENU_TITLE_CLASS = "menuTitle";

	private static final DefaultSimpleCompositeControlRenderer POPUP_MENU_RENDERER =
		DefaultSimpleCompositeControlRenderer.spanWithCSSClass(POPUP_MENU_CLASS);

	private static final Map<String, ControlCommand> CONTROL_COMMANDS = buildCommands();

	/**
	 * Whether the pop-up is currently open.
	 */
	private boolean _active;

	private HorizontalPopupPosition _horizontalPosition = HorizontalPopupPosition.LEFT_ALIGNED;

	private VerticalPopupPosition _verticalPosition = VerticalPopupPosition.BELOW;

	/**
	 * Creates a {@link PopupMenuButtonControl}.
	 */
	public PopupMenuButtonControl(PopupMenuModel model, IButtonRenderer renderer) {
		super(model, renderer, CONTROL_COMMANDS);
	}

	/**
	 * Horizontal position where the pop-up opens relative to the button.
	 */
	public HorizontalPopupPosition getHorizontalPosition() {
		return _horizontalPosition;
	}

	/**
	 * @see #getHorizontalPosition()
	 */
	public void setHorizontalPosition(HorizontalPopupPosition horizontalPosition) {
		_horizontalPosition = horizontalPosition;
	}

	/**
	 * Vertical position where the pop-up opens relative to the button.
	 */
	public VerticalPopupPosition getVerticalPosition() {
		return _verticalPosition;
	}

	/**
	 * @see #getVerticalPosition()
	 */
	public void setVerticalPosition(VerticalPopupPosition verticalPosition) {
		_verticalPosition = verticalPosition;
	}

	/**
	 * Creates a {@link PopupMenuButtonControl} for the given {@link ButtonUIModel
	 * model}. The renderer will be the renderer set as property of the key {@link #BUTTON_RENDERER}
	 * . If no such renderer was set a {@link ButtonRenderer} is used.
	 */
	public PopupMenuButtonControl(PopupMenuModel model) {
		super(model, CONTROL_COMMANDS);
	}

	private static final Map<String, ControlCommand> buildCommands() {
		return addCommands(Collections.<String, ControlCommand> emptyMap(), OpenPopupMenuCommand.INSTANCE);
	}

	/**
	 * Opens the pop-up menu.
	 * 
	 * @param commandContext
	 *        The {@link DisplayContext} of the open command.
	 */
	public HandlerResult openPopupMenu(DisplayContext commandContext) {
		final DefaultPopupDialogModel dialogModel =
			new DefaultPopupDialogModel(DefaultLayoutData.scrollingLayout(0, 0));
	
		PopupDialogControl dialogControl =
			new PopupDialogControl(getFrameScope(), dialogModel, getID(),
				_horizontalPosition, _verticalPosition);
	
		BlockControl contentControl = createPopupMenuContent(getModel(), dialogModel.getCloseAction());
		dialogControl.setContent(contentControl);

		dialogModel.addListener(DefaultPopupDialogModel.POPUP_DIALOG_CLOSED_PROPERTY, this);
	
		if (!contentControl.getChildren().isEmpty()) {
			getFrameScope().getWindowScope().openPopupDialog(dialogControl);
			setActive(true);
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	@Override
	public void handleDialogClosed(Object sender, Boolean oldValue, Boolean newValue) {
		setActive(false);
	}

	/**
	 * Marks the popup as being open or closed.
	 */
	private void setActive(boolean active) {
		_active = active;
		requestRepaint();
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		if (_active) {
			out.append("tlPopupOpen");
		}
	}

	/**
	 * Creates the content for a popup with the given {@link PopupMenuModel popup menu}.
	 * 
	 * @param popupModel
	 *        The supplier for the commands to display in the popup.
	 * @param dialogCloseAction
	 *        {@link Command} that actually closes the popup dialog.
	 */
	public static BlockControl createPopupMenuContent(PopupMenuModel popupModel, Command dialogCloseAction) {
		BlockControl contentControl = new BlockControl();
		contentControl.setRenderer(DefaultSimpleCompositeControlRenderer.DIV_INSTANCE);
		{
			Menu menu = popupModel.getMenu();
			HTMLFragment title = menu.getTitle();
			if (title != null) {
				contentControl.addChild(Fragments.div(MENU_TITLE_CLASS, Fragments.div("menuTitleContents", title)));
			}

			addGroup(popupModel, dialogCloseAction, contentControl, menu);

			if (ScriptingRecorder.isEnabled()) {
				BlockControl groupControl = new BlockControl();
				groupControl.setRenderer(POPUP_MENU_RENDERER);
				contentControl.addChild(groupControl);

				groupControl.addChild(
					new ButtonControl(
						new CloseCommandAdapter(GuiInspectorCommand.INSTANCE, dialogCloseAction),
						MenuButtonRenderer.INSTANCE));
			}
		}
		return contentControl;
	}

	private static void addGroup(PopupMenuModel popupModel, Command dialogCloseAction,
			BlockControl contentControl, AbstractMenuContents group) {
		BlockControl groupControl = null;
		for (MenuItem item : group) {
			groupControl = addItem(popupModel, dialogCloseAction, contentControl, groupControl, item);
		}
	}

	private static BlockControl addItem(PopupMenuModel popupModel, Command dialogCloseAction,
			BlockControl contentControl, BlockControl groupControl, MenuItem item) {
		if (item instanceof MenuGroup) {
			MenuGroup group = (MenuGroup) item;
			addGroup(popupModel, dialogCloseAction, contentControl, group);

			// There is (no longer) a current group to insert plain command items to.
			return null;
		} else if (item instanceof CommandItem) {
			return addCommand(popupModel, dialogCloseAction, contentControl, groupControl, (CommandItem) item);
		} else {
			// Unsupported, ignore.
			return groupControl;
		}
	}

	private static BlockControl addCommand(PopupMenuModel popupModel, Command dialogCloseAction,
			BlockControl contentControl, BlockControl groupControl, CommandItem command) {
		CommandModel button = command.getButton();
		button.set(PopupCommandNaming.POPUP, popupModel);

		if (groupControl == null) {
			groupControl = new BlockControl();
			groupControl.setRenderer(POPUP_MENU_RENDERER);
			contentControl.addChild(groupControl);
		}

		groupControl.addChild(
			new ButtonControl(
				new CloseCommandAdapter(button, dialogCloseAction),
				MenuButtonRenderer.INSTANCE));
		return groupControl;
	}

	static final class CloseCommandAdapter extends CommandModelAdapter {
		private final Command _dialogCloseAction;

		public CloseCommandAdapter(CommandModel impl, Command dialogCloseAction) {
			super(impl);
			_dialogCloseAction = dialogCloseAction;
		}

		@Override
		public HandlerResult executeCommand(DisplayContext context) {
			_dialogCloseAction.executeCommand(context);
			return super.executeCommand(context);
		}
	}

	/**
	 * Opens a popup dialog for a command menu. Used in combination with a
	 * {@link PopupMenuButtonControl}.
	 */
	public static class OpenPopupMenuCommand extends ControlCommand {
		/** Default instance */
		public static final OpenPopupMenuCommand INSTANCE = new OpenPopupMenuCommand("activate");

		private OpenPopupMenuCommand(String commandName) {
			super(commandName);
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			PopupMenuButtonControl buttonControl = (PopupMenuButtonControl) control;
			return buttonControl.openPopupMenu(commandContext);
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.OPEN_POPUP_MENU;
		}
	}
}
