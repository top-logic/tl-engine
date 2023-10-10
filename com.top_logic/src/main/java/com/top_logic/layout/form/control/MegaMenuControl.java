/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.SelectFieldUtils;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DefaultPopupDialogModel;
import com.top_logic.layout.structure.PopupDialogControl;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.layout.tooltip.HtmlToolTip;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * Redraws a {@link SelectField} into a mega menu field and opens the pop up dialog window listing
 * all available options.
 * 
 * @author <a href="mailto:pja@top-logic.com">Petar Janosevic</a>
 *
 */
public class MegaMenuControl extends AbstractFormFieldControl {

	/**
	 * Handling the commands for opening the popup menu and executing the value changing process.
	 */
	@SuppressWarnings("hiding")
	protected static final Map<String, ControlCommand> COMMANDS = createCommandMap(
		AbstractFormFieldControlBase.COMMANDS,
		new ControlCommand[] { OpenPopUpCommand.INSTANCE, MegaMenuSelectionChanged.INSTANCE });

	private static final String MEGAMENU_BUTTONCONTROL = "services.form.MegaMenuButtonControl";

	private PopupDialogControl megaMenu;

	private ThemeImage noOptionIcon = com.top_logic.layout.table.renderer.Icons.SORT_DESC_SMALL;

	private ThemeImage defaultIcon = com.top_logic.layout.tabbar.Icons.TAB_RIGHT_SCROLL_BUTTON;

	/**
	 * @param model
	 *        {@link SelectField} you want to redraw.
	 */
	public MegaMenuControl(SelectField model) {
		super(model, COMMANDS);
		if (model.isMultiple()) {
			throw new IllegalArgumentException(
				"Mega menu can not be a select field with multiple values to choose. Set value of multiple to false.");
		}
	}

	@Override
	protected void writeEditable(DisplayContext context, TagWriter out) throws IOException {
		SelectField megaMenuSelectField = (SelectField) getModel();
		out.beginBeginTag(HTMLConstants.SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
		out.beginBeginTag(HTMLConstants.BUTTON);
		writeInputIdAttr(out);
		out.writeAttribute(HTMLConstants.CLASS_ATTR, "megaMenuButton");
		if (megaMenuSelectField.isDisabled()) {
			out.writeAttribute(DISABLED_ATTR, DISABLED_DISABLED_VALUE);
		}
		writeOnClick(out, "handleButtonClick");
		Object singleSelection = megaMenuSelectField.getSingleSelection();
		if (singleSelection == null) {
			out.endBeginTag();
			out.writeText(SelectFieldUtils.getOptionLabel(megaMenuSelectField, SelectField.NO_OPTION));
		} else {
			LabelProvider optionLabelProvider = megaMenuSelectField.getOptionLabelProvider();
			ResourceProvider optionResourceProvider = toResourceProvider(optionLabelProvider);

			String tooltip = optionResourceProvider.getTooltip(singleSelection);
			if (tooltip != null) {
				// add tooltip caption for the mouse hover effect.
				OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, tooltip);
			}
			out.endBeginTag();
			String optionLabel = optionLabelProvider.getLabel(singleSelection);
			out.writeText(optionLabel);

		}
		out.endTag(HTMLConstants.BUTTON);
		out.endTag(HTMLConstants.SPAN);
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

	/**
	 * Opens a pop up window.
	 * 
	 * @param context
	 *        from {@link OpenPopUpCommand#execute}.
	 */
	@SuppressWarnings("deprecation")
	protected void openPopUp(DisplayContext context) {
		SelectField megaMenuSelectField = (SelectField) getModel();
		List<?> options = megaMenuSelectField.getOptions();
		List<?> selection = SelectFieldUtils.getSelectionList(getFieldModel());
		int nbrOfOptionElements = megaMenuSelectField.getOptionCount();
		boolean isGridNeeded = nbrOfOptionElements > 4;
		boolean hasListIcons = doesListHaveIcons(megaMenuSelectField, options, nbrOfOptionElements);

		if (isGridNeeded) {
			megaMenu = createMegaMenuPopUpDialogWindow(context, 600, Scrolling.AUTO);
		} else {
			megaMenu = createMegaMenuPopUpDialogWindow(context, 300, Scrolling.AUTO);
		}

		megaMenu.setContent(new HTMLFragment() {
			@Override
			public void write(DisplayContext context1, TagWriter out) throws IOException {
				LabelProvider optionLabelProvider = megaMenuSelectField.getOptionLabelProvider();
				ResourceProvider optionResourceProvider = toResourceProvider(optionLabelProvider);
				out.beginBeginTag(HTMLConstants.DIV);
				if (isGridNeeded) {
					out.writeAttribute(HTMLConstants.CLASS_ATTR, "megaMenuGridContainer");
				} else {
					out.writeAttribute(HTMLConstants.CLASS_ATTR, "megaMenuFlexContainer");
				}
				out.endBeginTag();

				// User should not be able to choose the no option option if field is mandatory
				// and has options to chose.
				boolean isNoOptionNotSelectable = megaMenuSelectField.isMandatory() && !options.isEmpty();
				if (!isNoOptionNotSelectable) {
					String emptyLabel = getEmptyLabel();
					String emptyTooltip = getEmptyTooltip();
					if (!options.isEmpty()) {
						displayOption(out, emptyLabel, emptyTooltip, null, true);
					} else {
						displayOption(out, emptyLabel, null, null, true);
					}
				}
				if (megaMenuSelectField.getFixedOptions() == null) {
					for (int i = 0; i < nbrOfOptionElements; i++) {
						Object currOption = options.get(i);
						String tooltip = optionResourceProvider.getTooltip(currOption);
						String optionLabel = optionLabelProvider.getLabel(currOption);
						displayOption(out, optionLabel, tooltip, currOption, false);
					}
				} else {
					// Displays a mega menu which has fixed options.
					for (int i = 0; i < nbrOfOptionElements; i++) {
						Object currOption = options.get(i);
						String tooltip = optionResourceProvider.getTooltip(currOption);
						String optionLabel = optionLabelProvider.getLabel(currOption);
						displayFixedOption(out, optionLabel, tooltip, currOption, false);
					}
				}
				out.endTag(HTMLConstants.DIV);
			}

			private void displayOption(TagWriter out, String optionLabel, String tooltip, Object currOption,
					boolean noOptionIsCallingThisMethod)
					throws IOException {
				out.beginBeginTag(HTMLConstants.ANCHOR);
				out.writeAttribute(HTMLConstants.HREF_ATTR, HTMLConstants.HREF_EMPTY_LINK);
				writeOnClick(out, "handleOnOptionClick");
				// First condition checks if no option is calling this method and if it
				// is the currently selected value, which is the case if selection is empty.
				if ((noOptionIsCallingThisMethod && selection.isEmpty()) || selection.contains(currOption)) {
					out.writeAttribute(HTMLConstants.CLASS_ATTR, "megaMenuItem megaMenuItemSelected");
				} else {
					out.writeAttribute(HTMLConstants.CLASS_ATTR, "megaMenuItem");
				}
				displayOptionContainer(out, optionLabel, tooltip, currOption, noOptionIsCallingThisMethod);
				out.endTag(HTMLConstants.ANCHOR);
			}

			private void displayFixedOption(TagWriter out, String optionLabel, String tooltip,
					Object currOption, boolean noOptionIsCallingThisMethod) throws IOException {
				// Display the fixed option gray and unable to be chosen, else display option as
				// usual.
				if (megaMenuSelectField.getFixedOptions().accept(currOption)) {
					out.beginBeginTag(HTMLConstants.SPAN);
					out.writeAttribute(HTMLConstants.CLASS_ATTR, "megaMenuFixedItem");
					displayOptionContainer(out, optionLabel, tooltip, currOption, noOptionIsCallingThisMethod);
					out.endTag(HTMLConstants.SPAN);
				} else {
					displayOption(out, optionLabel, tooltip, currOption, noOptionIsCallingThisMethod);
				}
			}

			private void displayOptionContainer(TagWriter out, String optionLabel,
					String tooltip, Object currOption, boolean noOptionIsCallingThisMethod) throws IOException {
				out.writeAttribute(HTMLConstants.DATA_ATTRIBUTE_PREFIX + "value",
					megaMenuSelectField.getOptionID(currOption));
				// Makes sure you cant right click on the option elements and do things like
				// "open in new tab"
				out.writeAttribute(HTMLConstants.ONCONTEXTMENU_ATTR, "return false;");
				out.endBeginTag();

				LabelProvider optionLabelProvider = megaMenuSelectField.getOptionLabelProvider();
				ResourceProvider optionResourceProvider = toResourceProvider(optionLabelProvider);

				ThemeImage image = optionResourceProvider.getImage(currOption, null);
				if (hasListIcons) {
					XMLTag icon;
					if (noOptionIsCallingThisMethod) {
						icon = noOptionIcon.toIcon();
					} else if (image != null) {
						icon = image.toIcon();
					} else {
						icon = defaultIcon.toIcon();
					}
					out.beginBeginTag(HTMLConstants.DIV);
					out.writeAttribute(HTMLConstants.CLASS_ATTR, "itemContainer");
					out.endBeginTag();

					out.beginBeginTag(HTMLConstants.DIV);
					out.writeAttribute(HTMLConstants.CLASS_ATTR, "megaMenuIconContainer");
					out.endBeginTag();

					icon.beginBeginTag(context, out);
					out.writeAttribute(HTMLConstants.CLASS_ATTR, "megaMenuIcon");
					icon.endBeginTag(context, out);
					icon.endTag(context, out);
					out.endTag(HTMLConstants.DIV);

					out.beginBeginTag(HTMLConstants.DIV);
					out.writeAttribute(HTMLConstants.CLASS_ATTR, "textContainer");
					out.endBeginTag();
					displayOptionText(out, optionLabel, tooltip);
					out.endTag(HTMLConstants.DIV);

					out.endTag(HTMLConstants.DIV);
				} else {
					displayOptionText(out, optionLabel, tooltip);
				}

			}

			private void displayOptionText(TagWriter out, String optionLabel, String tooltip) throws IOException {
				out.beginBeginTag(HTMLConstants.SPAN);
				out.writeAttribute(HTMLConstants.CLASS_ATTR, "strong");
				out.endBeginTag();
				out.writeText(optionLabel);
				out.endTag(HTMLConstants.SPAN);
				out.emptyTag(HTMLConstants.BR);
				out.beginTag(HTMLConstants.SPAN);

				// Using something else and not writeContent() would lead to tooltip
				// being displayed together with HTML tags written as text,
				// f.e: "Test <strong>tooltip</strong>"
				if (tooltip != null) {
					String saveTooltip = HtmlToolTip.ensureSafeHTMLTooltip(tooltip);
					out.writeContent(saveTooltip);
				}
				out.endTag(HTMLConstants.SPAN);
				out.emptyTag(HTMLConstants.BR);
			}

		});
		getWindowScope().openPopupDialog(megaMenu);
	}

	private boolean doesListHaveIcons(SelectField megaMenuSelectField, List<?> options, int nbrOfOptionElements) {
		for (int i = 0; i < nbrOfOptionElements; i++) {
			Object currOption = options.get(i);
			LabelProvider optionLabelProvider = megaMenuSelectField.getOptionLabelProvider();
			ResourceProvider optionResourceProvider = toResourceProvider(optionLabelProvider);
			ThemeImage image = optionResourceProvider.getImage(currOption, null);
			if (image != null) {
				XMLTag icon = image.toIcon();
				if (icon == null) {
					throw new IllegalArgumentException("Only icons are accepted as part of an option.");
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * Creates the dialog and layout for the pop up window.
	 * 
	 * @param context
	 *        from {@link #openPopUp}.
	 * @param aWidth
	 *        value for {@link DefaultLayoutData#getWidth()}.
	 * @param aScrollable
	 *        value of {@link DefaultLayoutData#getScrollable()}.
	 * @return {@link PopupDialogControl}.
	 */
	public PopupDialogControl createMegaMenuPopUpDialogWindow(DisplayContext context, int aWidth,
			Scrolling aScrollable) {
		int borderWidth = Icons.MEGA_MENU_BORDER_WIDTH.get();
		DefaultLayoutData popupLayout =
			new DefaultLayoutData(DisplayDimension.dim(aWidth, DisplayUnit.PIXEL), 100, DisplayDimension.dim(0,
				DisplayUnit.PIXEL), 100, aScrollable);
		DefaultPopupDialogModel defaultPopupModel = new DefaultPopupDialogModel(null, popupLayout, borderWidth);

		PopupDialogControl popupDialogControl = new PopupDialogControl(getFrameScope(), defaultPopupModel, getID());
		return popupDialogControl;
	}

	private void writeOnClick(TagWriter out, String methodName) throws IOException {
		out.beginAttribute(HTMLConstants.ONCLICK_ATTR);
		writeJSAction(out, MEGAMENU_BUTTONCONTROL, methodName, this, null);
		out.endAttribute();
	}

	/**
	 * Getter method for the no selection label in the mega menu.
	 * 
	 * @return the no selection label {@link ResKey} as a String.
	 */
	public String getEmptyLabel() {
		return Resources.getInstance().getString(getEmptyLabelResKey());
	}

	/**
	 * Setter method for the no selection label in the mega menu.
	 * 
	 * @return the no selection tooltip {@link ResKey} as a String.
	 */
	public String getEmptyTooltip() {
		return Resources.getInstance().getString(getEmptyLabelResKey().tooltip());
	}

	private ResKey getEmptyLabelResKey() {
		SelectField megaMenuSelectField = (SelectField) getModel();
		ResKey noOptionResKey = megaMenuSelectField.getEmptySelectionMegaMenu();
		if (noOptionResKey == null && !megaMenuSelectField.isMandatory()) {
			noOptionResKey = megaMenuSelectField.getOptionCount() == 0
				? I18NConstants.EMPTY_SINGLE_SELECTION_WITH_NO_OPTIONS_MEGA_MENU
				: I18NConstants.MEGAMENU_NO_OPTION_LABEL;
			return noOptionResKey;
		}
		if (megaMenuSelectField.isMandatory()) {
			noOptionResKey = I18NConstants.MANDATORY_EMPTY_SINGLE_SELECTION_WITH_NO_OPTIONS_MEGA_MENU;
		}

		return noOptionResKey;
	}

	@Override
	public Bubble handleMandatoryChanged(FormField sender, Boolean oldValue, Boolean newValue) {
		if (!skipEvent(sender)) {
			requestRepaint();
		}
		return Bubble.BUBBLE;
	}

	@Override
	protected void internalHandleValueChanged(FormField field, Object oldValue, Object newValue) {
		requestRepaint();
	}

	@Override
	protected void writeImmutable(DisplayContext context, TagWriter out) throws IOException {
		SelectField megaMenuSelectField = (SelectField) getModel();
		LabelProvider optionLabelProvider = megaMenuSelectField.getOptionLabelProvider();
		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
		out.beginBeginTag(SPAN);
		out.writeAttribute(HTMLConstants.CLASS_ATTR, "megaMenuImmutableLabel");
		out.endBeginTag();
		Object singleSelection = megaMenuSelectField.getSingleSelection();
		String labelToDisplay;
		if (singleSelection != null) {
			labelToDisplay = optionLabelProvider.getLabel(singleSelection);
		} else {
			labelToDisplay = SelectFieldUtils.getOptionLabel(getFieldModel(), SelectField.NO_OPTION);
		}
		out.writeText(labelToDisplay);
		out.endTag(SPAN);
		out.endTag(SPAN);
	}

	@Override
	protected void internalHandleDisabledEvent(FormMember sender, Boolean oldValue, Boolean newValue) {
		addDisabledUpdate(newValue.booleanValue());
	}

	/**
	 * We have to do this otherwise the mega menu button won't get updated with the selected option.
	 * The problem lies in this method: {@link AbstractFormFieldControlBase#valueChanged},
	 * variables: current raw value and last raw value. Both variables are equal in the if condition
	 * while in reality they are not equal.
	 */
	@Override
	protected void updateRawValue(Object nextRawValue) {
	}

	@Override
	protected String getTypeCssClass() {
		return "cMegaMenu";
	}

	/**
	 * Class which opens a popup dialog window for the {@link MegaMenuControl}.
	 * 
	 * @author <a href="mailto:pja@top-logic.com">Petar Janosevic</a>
	 *
	 */
	public static class OpenPopUpCommand extends ControlCommand {

		/**
		 * COMMAND_ID for {@link OpenPopUpCommand#INSTANCE}.
		 */
		public static final String COMMAND_ID = "megaMenuActive";

		/**
		 * INSTANCE for {@link #COMMANDS}.
		 */
		public static final OpenPopUpCommand INSTANCE = new OpenPopUpCommand(COMMAND_ID);

		/**
		 * @param aCommand
		 *        instantiation of an {@link OpenPopUpCommand} object with
		 *        {@link OpenPopUpCommand#COMMAND_ID} "megaMenuActive". Clicking on the mega menu
		 *        button triggers a JS Method which leads back to {@link OpenPopUpCommand#execute},
		 *        because of this command.
		 */
		public OpenPopUpCommand(String aCommand) {
			super(aCommand);
		}

		// Called from file: ajax-form.js - MegaMenuButtonControl: handleClick
		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			MegaMenuControl megaMenuControl = (MegaMenuControl) control;
			megaMenuControl.openPopUp(commandContext);
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.OPEN_MEGA_MENU;
		}
	}

	/**
	 * Class for closing the pop up window and update values.
	 * 
	 * @author <a href="mailto:pja@top-logic.com">Petar Janosevic</a>
	 *
	 */
	protected static class MegaMenuSelectionChanged extends ValueChanged {
		/**
		 * INSTANCE for {@link #COMMANDS}.
		 */
		public static final MegaMenuControl.MegaMenuSelectionChanged INSTANCE =
			new MegaMenuControl.MegaMenuSelectionChanged();

		/**
		 * Singleton constructor.
		 */
		public MegaMenuSelectionChanged() {
		}

		@Override
		protected void updateValue(DisplayContext commandContext, AbstractFormFieldControlBase formFieldControl,
				Object newValue, Map<String, Object> arguments) {
			super.updateValue(commandContext, formFieldControl, newValue, arguments);
			MegaMenuControl megaMenuControlField = (MegaMenuControl) formFieldControl;
			megaMenuControlField.megaMenu.getModel().getCloseAction().executeCommand(commandContext);
		}
	}

	/**
	 * Class used as a link between the jsp file of the page where the mega menu is to be displayed
	 * and the select field that is to be converted into a mega menu.
	 * 
	 * @author <a href="mailto:pja@top-logic.com">Petar Janosevic</a>
	 *
	 */
	public static class CP implements ControlProvider {

		/**
		 * Singleton {@link CP} instance for the control provider variable in the jsp file.
		 */
		public static final CP INSTANCE = new CP();

		/**
		 * Singleton constructor.
		 */
		public CP() {
		}

		@Override
		public Control createControl(Object model, String style) {
			return new MegaMenuControl((SelectField) model);
		}
	}

}
