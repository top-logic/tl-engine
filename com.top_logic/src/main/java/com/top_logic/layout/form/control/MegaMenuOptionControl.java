/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.AbstractConstantControl;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.TemplateVariable;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.SelectFieldUtils;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.structure.PopupDialogControl;
import com.top_logic.layout.tooltip.HtmlToolTip;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Is one single option for a mega menu popup window.
 * 
 * <p>
 * Being used to display a option for a mega menu in {@MegaMenuTabConfig} which acts as a navigation
 * bar button (main tab bar button).
 * </p>
 * 
 * @author <a href="mailto:pja@top-logic.com">Petar Janosevic</a>
 *
 */
public class MegaMenuOptionControl extends AbstractConstantControl {

	/**
	 * Handling commands for closing pop up window and setting the selection value.
	 */
	protected static final Map<String, ControlCommand> COMMANDS = createCommandMap(
		new ControlCommand[] { ValueChange.INSTANCE });

	private LayoutComponent _option;

	private SelectField _megaMenu;

	private PopupDialogControl _popupDialog;

	/**
	 * @param option
	 *        Any kind of option for a mega menu, which should be drawn.
	 * @param megaMenu
	 *        {@link SelectField} for which this option is meant.
	 * @param popupDialog
	 *        to be able to close the popup window after a option was selected.
	 */
	public MegaMenuOptionControl(LayoutComponent option, SelectField megaMenu, PopupDialogControl popupDialog) {
		super(COMMANDS);
		_option = option;
		_megaMenu = megaMenu;
		_popupDialog = popupDialog;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		Icons.MEGA_MENU_OPTION_TEMPLATE.get().write(context, out, this);
	}

	private ResourceProvider toResourceProvider(LabelProvider optionLabelProvider) {
		ResourceProvider optionResourceProvider;
		if (optionLabelProvider instanceof ResourceProvider) {
			optionResourceProvider = (ResourceProvider) optionLabelProvider;
		} else {
			optionResourceProvider = MetaResourceProvider.INSTANCE;
		}
		return optionResourceProvider;
	}

	/** CSS class for the option. */
	@TemplateVariable("isSelected")
	public boolean writeTabbarContainerClass() {
		List<?> selection = SelectFieldUtils.getSelectionList(_megaMenu);
		return selection.contains(_option);
	}

	/** Selects the option. */
	@TemplateVariable("onclick")
	public void writeOnClick(TagWriter out) throws IOException {
		out.append("return ");
		ValueChange.INSTANCE.writeInvokeExpression(out, this);
	}

	/** Selects the option. */
	@TemplateVariable("dataValue")
	public void writeDataValue(TagWriter out) throws IOException {
		out.append(_megaMenu.getOptionID(_option));
	}

	/** Name of the option. */
	@TemplateVariable("label")
	public void writeLabel(TagWriter out) throws IOException {
		ResKey currOption = _option.getConfig().getTabInfo().getLabel();
		LabelProvider optionLabelProvider = _megaMenu.getOptionLabelProvider();
		String optionLabel = optionLabelProvider.getLabel(currOption);
		out.append(optionLabel);
	}

	/** Tooltip of the option. */
	@TemplateVariable("tooltip")
	public void writeTooltip(TagWriter out) throws IOException {
		ResKey currOption = _option.getConfig().getTabInfo().getLabel();
		LabelProvider optionLabelProvider = _megaMenu.getOptionLabelProvider();
		ResourceProvider optionResourceProvider = toResourceProvider(optionLabelProvider);
		String tooltip = optionResourceProvider.getTooltip(currOption);
		// Using something else and not writeContent() would lead to tooltip
		// being displayed together with HTML tags written as text,
		// f.e: "Test <strong>tooltip</strong>"
		if (tooltip != null) {
			String saveTooltip = HtmlToolTip.ensureSafeHTMLTooltip(tooltip);
			out.writeContent(saveTooltip);
		}
	}

	/**
	 * Command class for this option control.
	 * 
	 * Sets the option as selected option in mega menu and closes pop up.
	 * 
	 * @author <a href="mailto:pja@top-logic.com">Petar Janosevic</a>
	 *
	 */
	public static class ValueChange extends ControlCommand {

		/**
		 * COMMAND_ID for {@link ValueChange#INSTANCE}.
		 */
		public static final String COMMAND_ID = "closeMegaMenu";

		/**
		 * INSTANCE for {@link #COMMANDS}.
		 */
		public static final ValueChange INSTANCE = new ValueChange(COMMAND_ID);

		/**
		 * @param aCommand
		 *        ID to instantiate a {@ControlCommand}.
		 */
		public ValueChange(String aCommand) {
			super(aCommand);
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			MegaMenuOptionControl megaMenuOptionC = (MegaMenuOptionControl) control;
			SelectField megaMenu = megaMenuOptionC._megaMenu;
			megaMenu.setAsSingleSelection(megaMenuOptionC._option);
			megaMenuOptionC._popupDialog.getModel().getCloseAction().executeCommand(commandContext);
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.SET_VALUE_CLOSE_POPUP;
		}
	}

}
