/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.messagebox;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.layout.structure.LayoutControl;
import com.top_logic.layout.structure.MediaQueryControl;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Base class for implementing a dialog.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractDialog {

	private static final Property<AbstractDialog> DIALOG = TypedAnnotatable.property(AbstractDialog.class, "dialog");

	private final DialogModel _dialogModel;

	/**
	 * Creates an {@link AbstractDialog}.
	 */
	public AbstractDialog(DialogModel dialogModel) {
		_dialogModel = dialogModel;
		_dialogModel.set(DIALOG, this);
	}

	/**
	 * The {@link AbstractDialog} that controls the given {@link DialogModel}, or <code>null</code>,
	 * if there is no such instance.
	 */
	public static AbstractDialog getDialog(DialogModel dialogModel) {
		return dialogModel.get(DIALOG);
	}

	/**
	 * The {@link DialogModel} of this dialog.
	 */
	public DialogModel getDialogModel() {
		return _dialogModel;
	}

	/**
	 * {@link Command} that closes this dialog.
	 */
	public Command getDiscardClosure() {
		return _dialogModel.getCloseAction();
	}

	/**
	 * @see #open(WindowScope)
	 */
	public HandlerResult open(DisplayContext context) {
		return open(context.getWindowScope());
	}

	/**
	 * Opens the dialog in the window scope.
	 * 
	 * @return See {@link MessageBox#confirm(WindowScope, MessageType, String, CommandModel...)}
	 */
	public HandlerResult open(WindowScope windowScope) {
		return MessageBox.open(windowScope, _dialogModel, createLayout(), getButtons());
	}

	/**
	 * Constructs the content display of this dialog.
	 */
	protected abstract HTMLFragment createView();

	/**
	 * Creates the dialog layout wrapping the content {@link #createView() view} into a
	 * {@link MediaQueryControl}.
	 * 
	 * @see MediaQueryControl
	 */
	protected HTMLFragment createLayout() {
		return createMediaControl(DisplayDimension.HUNDERED_PERCENT, DisplayDimension.HUNDERED_PERCENT, createView());
	}

	/**
	 * Creates a {@link MediaQueryControl} with given content view and layout dimensions.
	 * 
	 * @see MediaQueryControl
	 */
	protected LayoutControl createMediaControl(DisplayDimension width, DisplayDimension height, HTMLFragment view) {
		MediaQueryControl mediaControl = new MediaQueryControl(view);
		mediaControl.setConstraint(new DefaultLayoutData(width, 100, height, 100, Scrolling.AUTO));
		return mediaControl;
	}

	private final List<CommandModel> getButtons() {
		ArrayList<CommandModel> buttons = new ArrayList<>();

		fillButtons(buttons);

		return buttons;
	}

	/**
	 * Creates the list of buttons to display.
	 * 
	 * <p>
	 * Note: call {@link #addCancel(List)}, if this dialog should have a cancel button.
	 * </p>
	 */
	protected abstract void fillButtons(List<CommandModel> buttons);

	/**
	 * Utility for adding the OK button to the given list of button models.
	 * 
	 * @param buttons
	 *        The buttons to add a cancel button to.
	 * @return The created button.
	 */
	protected CommandModel addOk(List<CommandModel> buttons) {
		return addClose(buttons, ButtonType.OK);
	}

	/**
	 * Utility for adding a cancel button to the given list of button models.
	 * 
	 * @param buttons
	 *        The buttons to add a cancel button to.
	 * @return The created button.
	 */
	protected CommandModel addCancel(List<CommandModel> buttons) {
		return addClose(buttons, ButtonType.CANCEL);
	}

	/**
	 * Adds a button of the given type, which closes this dialog
	 * 
	 * @return The created button.
	 */
	protected CommandModel addClose(List<CommandModel> buttons, ButtonType closeType) {
		CommandModel result;
		buttons.add(result = MessageBox.button(closeType, getDiscardClosure()));
		return result;
	}

}
