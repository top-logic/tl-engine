/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.IDBuilder;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.FormFieldInternals;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.SelectFieldUtils;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.provider.LabelResourceProvider;
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
		new ControlCommand[] { OpenPopUpCommand.INSTANCE, MegaMenuOptionSelected.INSTANCE });

	private static final String MEGAMENU_CONTROL = FormConstants.FORM_PACKAGE + ".MegaMenuControl";

	private static final String BUTTON_CLICK_FUNCTION = "MegaMenuButtonControl.handleButtonClick";

	private static final String OPTION_CLICK_FUNCTION = "handleOnOptionClick";

	private final IDBuilder _idBuilder = new IDBuilder();

	private PopupDialogControl _megaMenuPopupDialogControl;

	private ThemeImage _noOptionIcon = com.top_logic.layout.table.renderer.Icons.SORT_DESC_SMALL;

	private ThemeImage _defaultIcon = com.top_logic.layout.tabbar.Icons.TAB_RIGHT_SCROLL_BUTTON;

	/**
	 * ResKey for storing an explicit empty label for the mega menu.
	 */
	public static final Property<ResKey> EMPTY_LABEL_PROPERTY_MEGAMENU =
		TypedAnnotatable.property(ResKey.class, "noOptionSelectedLabelMegaMenu");

	/**
	 * @param model
	 *        {@link SelectField} you want to redraw.
	 */
	public MegaMenuControl(FormField model) {
		super(model, COMMANDS);
		if (model instanceof SelectField && ((SelectField) model).isMultiple()) {
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
		out.writeAttribute(HTMLConstants.CLASS_ATTR, "tl-mega-menu-button");
		if (megaMenuSelectField.isDisabled()) {
			out.writeAttribute(DISABLED_ATTR, DISABLED_DISABLED_VALUE);
		}
		writeOnClick(out, BUTTON_CLICK_FUNCTION, null);
		Object singleSelection = megaMenuSelectField.getSingleSelection();
		if (singleSelection == null) {
			out.endBeginTag();
			out.writeText(SelectFieldUtils.getOptionLabel(megaMenuSelectField, SelectField.NO_OPTION));
		} else {
			LabelProvider optionLabelProvider = megaMenuSelectField.getOptionLabelProvider();
			ResourceProvider optionResourceProvider = LabelResourceProvider.toResourceProvider(optionLabelProvider);
			String tooltip = optionResourceProvider.getTooltip(singleSelection);

			if (tooltip != null) {
				// add tooltip caption for the mouse hover effect.
				OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, tooltip);
			}
			out.endBeginTag();

			ThemeImage image = optionResourceProvider.getImage(singleSelection, null);
			if (image != null) {
				XMLTag icon = image.toIcon();
				icon.beginBeginTag(context, out);
				out.writeAttribute(HTMLConstants.CLASS_ATTR, "tl-mega-menu-button-icon");
				icon.endBeginTag(context, out);
				icon.endTag(context, out);
			}

			out.beginBeginTag(HTMLConstants.SPAN);
			out.writeAttribute(HTMLConstants.CLASS_ATTR, "tl-mega-menu-button-label");
			out.endBeginTag();
			String optionLabel = optionLabelProvider.getLabel(singleSelection);
			out.writeText(optionLabel);
			out.endTag(HTMLConstants.SPAN);
		}
		XMLTag arrowDownIcon = com.top_logic.layout.table.control.Icons.MOVE_ROW_DOWN.toIcon();
		arrowDownIcon.beginBeginTag(context, out);
		out.writeAttribute(HTMLConstants.CLASS_ATTR, "tl-mega-menu-button-arrow-down");
		arrowDownIcon.endBeginTag(context, out);
		arrowDownIcon.endTag(context, out);
		out.endTag(HTMLConstants.BUTTON);
		out.endTag(HTMLConstants.SPAN);
	}

	/**
	 * Opens a pop up window.
	 * 
	 * @param context
	 *        from {@link OpenPopUpCommand#execute}.
	 */
	@SuppressWarnings("deprecation")
	protected void openPopUp(DisplayContext context) {
		FormField megaMenu = getFieldModel();
		OptionModel<?> optionModel = SelectFieldUtils.getOptionModel(megaMenu);
		List<Object> options = createOptionList(optionModel);

		List<?> selection = SelectFieldUtils.getSelectionList(getFieldModel());
		int nbrOfOptionElements = optionModel.getOptionCount();
		boolean hasListIcons = doesListHaveIcons(megaMenu, options, nbrOfOptionElements);
		boolean isGridNeeded = nbrOfOptionElements > 4;
		_megaMenuPopupDialogControl =
			MegaMenuPopupDialogCreater.createPopupDialog(context, isGridNeeded, getFrameScope(), getID());

		_megaMenuPopupDialogControl.setContent(new HTMLFragment() {
			@Override
			public void write(DisplayContext context1, TagWriter out) throws IOException {
				LabelProvider optionLabelProvider = SelectFieldUtils.getOptionLabelProvider(megaMenu);
				ResourceProvider optionResourceProvider = LabelResourceProvider.toResourceProvider(optionLabelProvider);
				out.beginBeginTag(HTMLConstants.DIV);
				out.writeAttribute(HTMLConstants.CLASS_ATTR, "tl-mega-menu-container");
				writeOnKeyDown(isGridNeeded, out);
				out.endBeginTag();

				// User should not be able to choose the no option option if field is mandatory
				// and has options to chose.
				boolean isNoOptionNotSelectable = megaMenu.isMandatory() && !options.isEmpty();
				if (!isNoOptionNotSelectable) {
					String emptyLabel = getEmptyLabel();
					String emptyTooltip = getEmptyTooltip();
					if (!options.isEmpty()) {
						displayOption(out, emptyLabel, emptyTooltip, null, true);
					} else {
						displayOption(out, emptyLabel, null, null, true);
					}
				}

				boolean isSelectField = megaMenu instanceof SelectField;
				Filter fixedOptions = isSelectField ? ((SelectField) megaMenu).getFixedOptions() : null;

				for (int i = 0; i < nbrOfOptionElements; i++) {
					Object currOption = options.get(i);
					String optionLabel = optionLabelProvider.getLabel(currOption);
					String tooltip = optionResourceProvider.getTooltip(currOption);

					if (isSelectField && fixedOptions != null && fixedOptions.accept(currOption)) {
						displayFixedOption(out, optionLabel, tooltip, currOption);
					} else {
						displayOption(out, optionLabel, tooltip, currOption, false);
					}
				}

				out.endTag(HTMLConstants.DIV);
			}

			private void displayOption(TagWriter out, String optionLabel, String tooltip, Object currOption,
					boolean noOptionIsCallingThisMethod)
					throws IOException {
				out.beginBeginTag(HTMLConstants.ANCHOR);
				out.writeAttribute(HTMLConstants.HREF_ATTR, HTMLConstants.HREF_EMPTY_LINK);
				writeOnClick(out, OPTION_CLICK_FUNCTION, null);
				writeOnMouseOver(out);
				// First condition checks if no option is calling this method and if it
				// is the currently selected value, which is the case if selection is empty.
				if ((noOptionIsCallingThisMethod && selection.isEmpty()) || selection.contains(currOption)) {
					out.writeAttribute(HTMLConstants.CLASS_ATTR, "tl-mega-menu__option tl-mega-menu__option--selected");
				} else {
					out.writeAttribute(HTMLConstants.CLASS_ATTR, "tl-mega-menu__option");
				}
				displayOptionContainer(out, optionLabel, tooltip, currOption, noOptionIsCallingThisMethod);
				out.endTag(HTMLConstants.ANCHOR);
			}

			private void displayFixedOption(TagWriter out, String optionLabel, String tooltip,
					Object currOption) throws IOException {
				out.beginBeginTag(HTMLConstants.SPAN);
				out.writeAttribute(HTMLConstants.CLASS_ATTR, "tl-mega-menu__option--fixed");
				displayOptionContainer(out, optionLabel, tooltip, currOption, false);
				out.endTag(HTMLConstants.SPAN);
			}

			private void displayOptionContainer(TagWriter out, String optionLabel,
					String tooltip, Object currOption, boolean noOptionIsCallingThisMethod) throws IOException {
				out.writeAttribute(HTMLConstants.DATA_ATTRIBUTE_PREFIX + "value", getItemID(currOption));
				// Makes sure you cant right click on the option elements and do things like
				// "open in new tab"
				out.writeAttribute(HTMLConstants.ONCONTEXTMENU_ATTR, "return false;");
				out.endBeginTag();

				LabelProvider optionLabelProvider = SelectFieldUtils.getOptionLabelProvider(megaMenu);
				ResourceProvider optionResourceProvider = LabelResourceProvider.toResourceProvider(optionLabelProvider);

				ThemeImage image = optionResourceProvider.getImage(currOption, null);
				if (hasListIcons) {
					XMLTag icon;
					if (noOptionIsCallingThisMethod) {
						icon = _noOptionIcon.toIcon();
					} else if (image != null) {
						icon = image.toIcon();
					} else {
						icon = _defaultIcon.toIcon();
					}
					out.beginBeginTag(HTMLConstants.DIV);
					out.writeAttribute(HTMLConstants.CLASS_ATTR, "tl-mega-menu__option-container");
					out.endBeginTag();

					displayOptionIcon(context, out, icon);
					displayOptionText(out, optionLabel, tooltip);

					out.endTag(HTMLConstants.DIV);
				} else {
					displayOptionText(out, optionLabel, tooltip);
				}

			}

			private void displayOptionIcon(DisplayContext context, TagWriter out, XMLTag icon) throws IOException {
				out.beginBeginTag(HTMLConstants.DIV);
				out.writeAttribute(HTMLConstants.CLASS_ATTR, "tl-mega-menu__option-icon-container");
				out.endBeginTag();
				icon.beginBeginTag(context, out);
				out.writeAttribute(HTMLConstants.CLASS_ATTR, "tl-mega-menu__option-icon");
				icon.endBeginTag(context, out);
				icon.endTag(context, out);
				out.endTag(HTMLConstants.DIV);
			}

			private void displayOptionText(TagWriter out, String optionLabel, String tooltip) throws IOException {
				out.beginBeginTag(HTMLConstants.DIV);
				out.writeAttribute(HTMLConstants.CLASS_ATTR, "tl-mega-menu__option-text-container");
				out.endBeginTag();
				out.beginBeginTag(HTMLConstants.SPAN);
				out.writeAttribute(HTMLConstants.CLASS_ATTR, "strong tl-mega-menu__option-label");
				out.endBeginTag();
				out.writeText(optionLabel);
				out.endTag(HTMLConstants.SPAN);
				out.beginBeginTag(HTMLConstants.SPAN);
				out.writeAttribute(HTMLConstants.CLASS_ATTR, "tl-mega-menu__option-tooltip");
				out.endBeginTag();
				// Using something else and not writeContent() would lead to tooltip
				// being displayed together with HTML tags written as text,
				// f.e: "Test <strong>tooltip</strong>"
				if (tooltip != null) {
					String saveTooltip = HtmlToolTip.ensureSafeHTMLTooltip(tooltip);
					out.writeContent(saveTooltip);
				}
				out.endTag(HTMLConstants.SPAN);
				out.endTag(HTMLConstants.DIV);
			}

		});
		getWindowScope().openPopupDialog(_megaMenuPopupDialogControl);
	}

	private List<Object> createOptionList(OptionModel<?> optionModel) {
		List<Object> options = new ArrayList<>();
		for (Object option : optionModel) {
			options.add(option);
		}
		return options;
	}

	private boolean doesListHaveIcons(FormField megaMenu, List<?> options, int nbrOfOptionElements) {
		for (int i = 0; i < nbrOfOptionElements; i++) {
			Object currOption = options.get(i);
			LabelProvider optionLabelProvider = SelectFieldUtils.getOptionLabelProvider(megaMenu);
			ResourceProvider optionResourceProvider = LabelResourceProvider.toResourceProvider(optionLabelProvider);
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

	private void writeOnKeyDown(boolean isGridNeeded, TagWriter out) throws IOException {
		out.beginAttribute(HTMLConstants.ONKEYDOWN_ATTR);
		out.write("services.form.MegaMenuControl.handleArrowKeyNavigation(event, " + isGridNeeded + ")");
		out.endAttribute();
	}

	private void writeOnClick(TagWriter out, String methodName, String additional) throws IOException {
		out.beginAttribute(HTMLConstants.ONCLICK_ATTR);
		writeJSAction(out, MEGAMENU_CONTROL, methodName, this, additional);
		out.endAttribute();
	}

	private void writeOnMouseOver(TagWriter out) throws IOException {
		out.beginAttribute(HTMLConstants.ONMOUSEOVER_ATTR);
		out.write("this.focus();");
		out.endAttribute();
	}

	private String getItemID(Object item) {
		return _idBuilder.makeId(item);
	}

	private Object getItemById(String itemID) {
		return _idBuilder.getObjectById(itemID);
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
		ResKey noOptionResKey = SelectFieldUtils.getEmptySelectionMegaMenu(megaMenuSelectField);
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
		ResourceProvider optionResourceProvider = LabelResourceProvider.toResourceProvider(optionLabelProvider);
		Object singleSelection = megaMenuSelectField.getSingleSelection();

		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();

		ThemeImage image = optionResourceProvider.getImage(singleSelection, null);
		if (image != null) {
			XMLTag icon = image.toIcon();
			icon.beginBeginTag(context, out);
			out.writeAttribute(HTMLConstants.CLASS_ATTR, "tl-mega-menu__option-icon--immutable");
			icon.endBeginTag(context, out);
			icon.endTag(context, out);
		}

		out.beginBeginTag(SPAN);
		out.writeAttribute(HTMLConstants.CLASS_ATTR, "tl-mega-menu__option-label--immutable");
		out.endBeginTag();
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

	@Override
	protected String getTypeCssClass() {
		return "tl-mega-menu";
	}

	/**
	 * Creates the pop up dialog for a mega menu.
	 *
	 * @author <a href="mailto:pja@top-logic.com">Petar Janosevic</a>
	 */
	public static class MegaMenuPopupDialogCreater {

		/**
		 * Creates the dialog and layout for the pop up window.
		 * 
		 * @param context
		 *        from {@link #openPopUp}.
		 * @param frameScope
		 *        the control's {@link FrameScope}.
		 * @param id
		 *        the control's id.
		 * @return {@link PopupDialogControl}.
		 */
		public static PopupDialogControl createPopupDialog(DisplayContext context, boolean isGridNeeded,
				FrameScope frameScope, String id) {
			int width;
			String CssClass;
			if (isGridNeeded) {
				width = 600;
				CssClass = "tl-mega-menu-grid-container";
			} else {
				width = 300;
				CssClass = "tl-mega-menu-flex-container";
			}
			int borderWidth = Icons.MEGA_MENU_BORDER_WIDTH.get();

			DefaultLayoutData popupLayout =
				new DefaultLayoutData(DisplayDimension.dim(width, DisplayUnit.PIXEL), 100, DisplayDimension.dim(0,
					DisplayUnit.PIXEL), 100, Scrolling.AUTO);
			DefaultPopupDialogModel defaultPopupModel =
				new DefaultPopupDialogModel(null, popupLayout, borderWidth, CssClass);

			PopupDialogControl popupDialogControl = new PopupDialogControl(frameScope, defaultPopupModel, id);
			return popupDialogControl;
		}
	}

	/**
	 * Class which opens a popup dialog window for the {@link MegaMenuControl}.
	 * 
	 * @author <a href="mailto:pja@top-logic.com">Petar Janosevic</a>
	 *
	 */
	public static class OpenPopUpCommand extends ControlCommand {

		/** COMMAND_ID for {@link OpenPopUpCommand#INSTANCE}. */
		public static final String COMMAND_ID = "megaMenuActive";

		/** INSTANCE for {@link #COMMANDS}. */
		public static final OpenPopUpCommand INSTANCE = new OpenPopUpCommand();

		/** Constructor for opening the pop up window command. */
		public OpenPopUpCommand() {
			super(COMMAND_ID);
		}

		// Called from file: ajax-form.js - MegaMenuButtonControl: handleButtonClick
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
	 * Command to save selected option.
	 * 
	 * @author <a href="mailto:pja@top-logic.com">Petar Janosevic</a>
	 */
	protected static class MegaMenuOptionSelected extends ControlCommand {

		/** ID of the command */
		public static final String COMMAND_ID = "megaMenuOptionSelected";

		/** Single instance of the {@link MegaMenuOptionSelected} */
		public static final ControlCommand INSTANCE = new MegaMenuOptionSelected();

		/** Constructor for the item selected command */
		public MegaMenuOptionSelected() {
			super(COMMAND_ID);
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			AbstractFormField megaMenu = (AbstractFormField) control.getModel();
			Object selectedOption = ((MegaMenuControl) control).getItemById((String) arguments.get("dataValue"));

			if (selectedOption == SelectField.NO_OPTION) {
				deliverValue(commandContext, megaMenu, Collections.emptyList());
			} else {
				deliverValue(commandContext, megaMenu, Collections.singletonList(selectedOption));
			}
			MegaMenuControl megaMenuControlField = (MegaMenuControl) control;
			megaMenuControlField._megaMenuPopupDialogControl.getModel().getCloseAction().executeCommand(commandContext);
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.MEGAMENU_OPTION_SELECTED;
		}
	}

	private static void deliverValue(DisplayContext commandContext, AbstractFormField megaMenu,
			List<Object> newSelection) {
		final Object newValue;
		if (megaMenu instanceof SelectField) {
			// Always uses list as value.
			newValue = newSelection;
		} else {
			newValue = CollectionUtil.getSingleValueFromCollection(newSelection);
		}

		try {
			FormFieldInternals.setValue(megaMenu, newValue);
		} catch (VetoException ex) {
			ex.setContinuationCommand(context -> {
				megaMenu.setValue(newValue);
				return HandlerResult.DEFAULT_RESULT;
			});
			ex.process(commandContext.getWindowScope());
		}
	}


	/**
	 * {@link ControlProvider} to instantiate a {@link MegaMenuControl} for a {@link SelectField}.
	 * 
	 * @author <a href="mailto:pja@top-logic.com">Petar Janosevic</a>
	 *
	 */
	public static class CP implements ControlProvider {

		/**
		 * Singleton {@link CP} instance.
		 */
		public static final CP INSTANCE = new CP();

		/** Singleton constructor. */
		private CP() {
		}

		@Override
		public Control createControl(Object model, String style) {
			return new MegaMenuControl((FormField) model);
		}
	}

}
