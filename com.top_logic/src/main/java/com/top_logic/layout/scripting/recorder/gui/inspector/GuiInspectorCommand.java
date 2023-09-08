/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector;

import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.DisplayValue;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.ConstantDisplayValue;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.structure.DefaultDialogModel;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Opens the gui inspector, which is used to inspect an gui element and create assertions for it.
 * 
 * @param <C>
 *        The control implementation.
 * @param <M>
 *        The model type.
 * 
 * @author <a href="mailto:Jan.Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class GuiInspectorCommand<C extends Control, M> extends ControlCommand {

	/**
	 * The command name, under which the inspect fuctionality is reachable within a control.
	 */
	public static final String COMMAND_NAME = "openGuiInspector";

	private static final DisplayValue POPUP_TITLE = new ConstantDisplayValue("Gui Inspector");

	private static final DisplayDimension WIDTH = DisplayDimension.dim(640, DisplayUnit.PIXEL);

	private static final DisplayDimension HEIGHT = DisplayDimension.dim(480, DisplayUnit.PIXEL);

	private static final int MAX_WIDTH_IN_PERCENT = 100;
	private static final int MAX_HEIGHT_IN_PERCENT = 100;
	private static final boolean RESIZABLE = true;
	private static final boolean CLOSABLE = true;
	private static final String HELP_ID = null;

	private static final DefaultLayoutData LAYOUT =
		new DefaultLayoutData(WIDTH, MAX_WIDTH_IN_PERCENT, HEIGHT, MAX_HEIGHT_IN_PERCENT, Scrolling.AUTO);

	protected GuiInspectorCommand() {
		super(COMMAND_NAME);
	}

	@Override
	protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
		if (!GuiInspectorUtil.isGuiInspectorEnabled()) {
			String warning =
				"The gui inspector was requested, but it's disabled. The client should have checked and prevented that!";
			Logger.warn(warning, GuiInspectorCommand.class);
			return HandlerResult.DEFAULT_RESULT;
		}
		assert arguments.containsKey(CONTROL_ID_PARAM);
		assert arguments.get(CONTROL_ID_PARAM) instanceof String;
		assert arguments.get(CONTROL_ID_PARAM).equals(control.getID());
		assert control instanceof AbstractControlBase;

		@SuppressWarnings("unchecked")
		M inspectedGuiElement = findInspectedGuiElement((C) control, arguments);

		return openDialog(commandContext, inspectedGuiElement);
	}

	/**
	 * Extracts the model to reason about from the given {@link Control}
	 * 
	 * @param control
	 *        The control being inspected.
	 * @param arguments
	 *        The custom arguments passed by the inspector from the client-side.
	 * @return The control's model.
	 */
	protected abstract M findInspectedGuiElement(C control, Map<String, Object> arguments) throws AssertionError;

	private HandlerResult openDialog(DisplayContext displayContext, M inspectedGuiElement) {
		InspectorModel model = createInspector(inspectedGuiElement);
		if (model.isEmtpy()) {
			return HandlerResult.DEFAULT_RESULT;
		}

		DialogModel dialogModel = new DefaultDialogModel(LAYOUT, POPUP_TITLE, RESIZABLE, CLOSABLE, HELP_ID);
		return new GuiInspectorControl(dialogModel, model).open(displayContext);
	}

	private InspectorModel createInspector(M model) {
		InspectorModel inspector = new InspectorModel(model);
		buildInspector(inspector, model);
		return inspector;
	}

	@Override
	public ResKey getI18NKey() {
		return I18NConstants.OPEN_GUI_INSPECTOR;
	}

	/**
	 * Builds the assertions of the {@link InspectorModel}.
	 * 
	 * @param inspector
	 *        The {@link InspectorModel} to fill with assertions.
	 * @param model
	 *        The control's model, see {@link #findInspectedGuiElement(Control, Map)}
	 */
	protected abstract void buildInspector(InspectorModel inspector, M model);

}
