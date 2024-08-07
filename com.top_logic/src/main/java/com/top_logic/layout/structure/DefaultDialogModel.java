/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;


import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Default {@link DialogModel}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultDialogModel extends AbstractDialogModel {

	/**
	 * Constant to use when no help ID is given
	 * 
	 * @see DialogModel#getHelpID()
	 */
	public static final String NO_HELP_ID = null;

	/**
	 * Constant to determine that the dialog should be resizable
	 * 
	 * @see DialogModel#isResizable()
	 */
	public static final boolean RESIZABLE = true;

	/**
	 * Constant to determine that the dialog should have a close button.
	 * 
	 * @see DialogModel#hasCloseButton()
	 */
	public static final boolean CLOSE_BUTTON = true;

	private boolean closed;

	private final Command closeAction;

	/**
	 * Create a new DefaultDialogModel with a default close action
	 * 
	 * @param layoutData - the dialog layout data
	 * @param title - the dialog title
	 * @param resizable - whether the dialog is resizable, or not
	 * @param closable - whether the dialog is closable, or not
	 * @param helpID - the help id
	 */
	public DefaultDialogModel(LayoutData layoutData, HTMLFragment title, boolean resizable, boolean closable,
			String helpID) {
		super(layoutData, title, resizable, closable, helpID);
		this.closeAction = createCloseAction();
	}
	

	/**
	 * Create a new DefaultDialogModel with a user defined close action
	 * 
	 * @param layoutData - the dialog layout data
	 * @param title - the dialog title
	 * @param resizable - whether the dialog is resizable, or not
	 * @param closable - whether the dialog is closable, or not
	 * @param helpID - the help id
	 * @param closeAction - the user defined close action, must not be null
	 */
	public DefaultDialogModel(LayoutData layoutData, HTMLFragment title, boolean resizable, boolean closable,
			String helpID, Command closeAction) {
		super(layoutData, title, resizable, closable, helpID);
		
		assert closeAction != null : "Close action must not be null!";
		this.closeAction = closeAction;

	}

	/**
	 * Create a new DefaultDialogModel with a default close action and a {@link ConfigKey} for
	 * {@link PersonalConfiguration}a.
	 * 
	 * @param layoutData
	 *        - the dialog layout data
	 * @param title
	 *        - the dialog title
	 * @param resizable
	 *        - whether the dialog is resizable, or not
	 * @param closable
	 *        - whether the dialog is closable, or not
	 * @param helpID
	 *        - the help id
	 * @param configKey
	 *        - A unique attribute name. <code>null</code> will prevent personal configurations.
	 */
	public DefaultDialogModel(LayoutData layoutData, HTMLFragment title, boolean resizable, boolean closable,
			String helpID, ConfigKey configKey) {
		super(layoutData, title, resizable, closable, helpID, configKey);
		this.closeAction = createCloseAction();
	}
	
	/**
	 * Create a new DefaultDialogModel with a user defined close action
	 * 
	 * @param layoutData
	 *        - the dialog layout data
	 * @param title
	 *        - the dialog title
	 * @param resizable
	 *        - whether the dialog is resizable, or not
	 * @param closable
	 *        - whether the dialog is closable, or not
	 * @param helpID
	 *        - the help id
	 * @param closeAction
	 *        - the user defined close action, must not be null
	 * @param configKey
	 *        - A unique attribute name. <code>null</code> will prevent personal configurations.
	 */
	public DefaultDialogModel(LayoutData layoutData, HTMLFragment title, boolean resizable, boolean closable,
			String helpID, Command closeAction, ConfigKey configKey) {
		super(layoutData, title, resizable, closable, helpID, configKey);
		
		assert closeAction != null : "Close action must not be null!";
		this.closeAction = closeAction;
		
	}

	/** Creates a default close action */
	private Command createCloseAction() {
		return new Command() {

			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				if (!DefaultDialogModel.this.closed) {
					DefaultDialogModel.this.closed = true;
					notifyListeners(CLOSED_PROPERTY, DefaultDialogModel.this, false, true);
				}
				return HandlerResult.DEFAULT_RESULT;
			}
		};
	}

	@Override
	public Command getCloseAction() {
		return closeAction;
	}
	
	@Override
	public boolean isClosed() {
		return closed;
	}


	/**
	 * Utility for creating a {@link DialogModel} from title {@link ResPrefix} and dimensions.
	 */
	public static DefaultDialogModel dialogModel(ResPrefix resourcePrefix, DisplayDimension width,
			DisplayDimension height) {
		return dialogModel(resourcePrefix.key("title"), width, height);
	}

	/**
	 * Utility for creating a {@link DialogModel} from title {@link ResPrefix} and dimensions.
	 */
	public static DefaultDialogModel dialogModel(ResPrefix resourcePrefix, DisplayDimension width,
			DisplayDimension height, boolean closable) {
		return dialogModel(resourcePrefix.key("title"), width, height, closable);
	}

	/**
	 * Utility for creating a {@link DialogModel} from title {@link ResKey} and dimensions.
	 */
	public static DefaultDialogModel dialogModel(ResKey titleKey, DisplayDimension width, DisplayDimension height) {
		return dialogModel(new ResourceText(titleKey), width, height);
	}

	/**
	 * Utility for creating a {@link DialogModel} from title {@link ResKey} and dimensions.
	 */
	public static DefaultDialogModel dialogModel(ResKey titleKey, DisplayDimension width, DisplayDimension height,
			boolean closable) {
		return dialogModel(new ResourceText(titleKey), width, height, closable);
	}

	/**
	 * Utility for creating a {@link DialogModel} from title and dimensions.
	 */
	public static DefaultDialogModel dialogModel(HTMLFragment title, DisplayDimension width,
			DisplayDimension height) {
		return dialogModel(title, width, height, true);
	}

	/**
	 * Utility for creating a {@link DialogModel} from title, dimensions and closable.
	 * 
	 * @param closable
	 *        - if a dialog has a closing button and closes by clicking in the background
	 */
	public static DefaultDialogModel dialogModel(HTMLFragment title, DisplayDimension width, DisplayDimension height,
			boolean closable) {
		return new DefaultDialogModel(
			new DefaultLayoutData(width, 100, height, 100, Scrolling.NO), title, true, closable, null);
	}
}
