/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.top_logic.base.services.simpleajax.ElementReplacement;
import com.top_logic.base.services.simpleajax.JSFunctionCall;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.IDBuilder;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.FormFieldInternals;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.SelectFieldUtils;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.form.model.utility.TreeOptionModel;
import com.top_logic.layout.form.tag.Icons;
import com.top_logic.layout.provider.LabelResourceProvider;
import com.top_logic.layout.tooltip.HtmlToolTip;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Control for a dropdown view of a {@link FormField} model.
 * 
 * <p>
 * A dropdown can have a tooltip for each item in the list. Furthermore a dropdown that is multiple
 * renders removable tags for all selected items.
 * </p>
 * 
 * @author <a href="mailto:sha@top-logic.com">Simon Haneke</a>
 */
public class DropDownControl extends AbstractSelectControl {

	/**
	 * Commands registered at this control.
	 */
	public static final Map<String, ControlCommand> DD_TOOLTIP_COMMANDS = createCommandMap(DropDownControl.COMMANDS,
		new ControlCommand[] {
			DropDownItemSelected.INSTANCE
		});

	/**
	 * Java-script class of a {@link DropDownControl}.
	 */
	public static final String DROPDOWN_CONTROL_CLASS = FormConstants.FORM_PACKAGE + ".DropDownControl";

	private final boolean _preventClear;

	private final IDBuilder _idBuilder = new IDBuilder();

	private Renderer<Object> _selectionRenderer;

	/**
	 * @param model
	 *        given {@link FormField}
	 */
	public DropDownControl(FormField model) {
		this(model, false);
	}

	/**
	 * Creates a {@link DropDownControl}.
	 * 
	 * @param model
	 *        The {@link FormField} model.
	 * @param preventClear
	 *        Whether the "no selection choice" should be suppressed for single select fields that
	 *        are non-mandatory.
	 */
	public DropDownControl(FormField model, boolean preventClear) {
		this(model, DD_TOOLTIP_COMMANDS, preventClear);
	}

	/**
	 * @param model
	 *        given {@link FormField}
	 * @param command
	 *        all Commands for this Control
	 */
	protected DropDownControl(FormField model, Map<String, ControlCommand> command, boolean preventClear) {
		super(model, command);
		_preventClear = preventClear;
	}

	private String getItemIdPrefix() {
		return getID() + "-Item";
	}

	private String getItemID(Object item) {
		return getItemIdPrefix() + _idBuilder.makeId(item);
	}

	private Object getItemById(String itemID) {
		String id = itemID.replace(getItemIdPrefix(), "");
		return _idBuilder.getObjectById(id);
	}

	private String getItemLabel(FormField dropdown, Object item) {
		if (item != null) {
			return SelectFieldUtils.getOptionLabel(dropdown, item);
		}
		return "";
	}

	private String getSelectionLabel(FormField dropdown) {
		List<?> selection = SelectFieldUtils.getSelectionListSorted(dropdown);
		if (selection.size() <= 0) {
			return SelectFieldUtils.getEmptySelectionLabel(dropdown);
		}
		if (isMultiple()) {
			return SelectFieldUtils.getEmptySelectionLabel(dropdown, false);
		}
		return getItemLabel(dropdown, selection.get(0));
	}

	private String getTagLocID() {
		return getID() + "-tagLoc";
	}

	private boolean isMultiple() {
		return SelectFieldUtils.isMultiple(getFieldModel());
	}

	/**
	 * The CSS-Class prefix <code>ddwtt</code> uniquely identifies this element/control. The prefix
	 * stands for <code>DropDown With ToolTip</code>.
	 */
	@Override
	protected String getTypeCssClass() {
		return "ddwttContainer";
	}

	/**
	 * {@link Renderer} that displays the current selection list when the display is immutable.
	 */
	public void setSelectionRenderer(Renderer<Object> selRenderer) {
		_selectionRenderer = selRenderer;
	}

	@Override
	protected void writeEditable(DisplayContext context, TagWriter out) throws IOException {
		FormField dropdown = getFieldModel();

		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
		{
			if (isMultiple()) {
				renderTags(context, out);
			}
			
			renderDropDownButton(out, dropdown);

			renderDropDownBox(context, out, dropdown);
		}
		out.endTag(SPAN);
	}

	private void renderDropDownButton(TagWriter out, FormField dropdown) throws IOException {
		out.beginBeginTag(BUTTON);
		out.writeAttribute(CLASS_ATTR, "ddwttDropBtn ddwttChevron");
		if (dropdown.isDisabled()) {
			out.writeAttribute(DISABLED_ATTR, DISABLED_DISABLED_VALUE);
		}
		out.writeAttribute(TYPE_ATTR, "button");
		addButtonEvents(out);
		out.writeAttribute(ID, getInputId());
		out.endBeginTag();
		{
			out.beginTag(SPAN);
			{
				out.write(getSelectionLabel(dropdown));
			}
			out.endTag(SPAN);
		}
		out.endTag(BUTTON);
	}

	private void addButtonEvents(TagWriter out) throws IOException {
		out.beginAttribute(ONCLICK_ATTR);
		addJSFunction(out, "buttonDrop", "this");
		out.endAttribute();
		out.beginAttribute(ONKEYDOWN_ATTR);
		addJSFunction(out, "keyPressed", "event, " + isMultiple());
		out.endAttribute();
	}

	private void renderSearch(TagWriter out) throws IOException {
		out.beginBeginTag(INPUT);
		out.writeAttribute(CLASS_ATTR, "ddwttSearch ddwttHide");
		out.writeAttribute(TYPE_ATTR, "search");
		addSearchEvents(out);
		out.endEmptyTag();
	}

	private void addSearchEvents(TagWriter out) throws IOException {
		out.beginAttribute(ONFOCUSOUT_ATTR);
		addJSFunction(out, "lostFocus", null);
		out.endAttribute();
		out.beginAttribute(ONKEYDOWN_ATTR);
		addJSFunction(out, "keyPressed", "event, " + isMultiple());
		out.endAttribute();
		out.beginAttribute(ONINPUT_ATTR);
		addJSFunction(out, "search", "this");
		out.endAttribute();
	}

	private void renderDropDownBox(DisplayContext context, TagWriter out, FormField dropdown) throws IOException {
		String listID = getID() + "-ddList";

		out.beginBeginTag(SPAN);
		out.writeAttribute(CLASS_ATTR, "ddwttDDBox");
		out.writeAttribute("data-ctrlID", getID());
		out.endBeginTag();
		{
			renderSearch(out);

			out.beginBeginTag(SPAN);
			out.writeAttribute(CLASS_ATTR, "ddwttDDList");
			out.writeAttribute(ID, listID);
			out.endBeginTag();
			{
				fillDropDownList(context, out, dropdown);
			}
			out.endTag(SPAN);
		}
		out.endTag(SPAN);
	}

	private void fillDropDownList(DisplayContext context, TagWriter out, FormField dropdown) throws IOException {
		if (!dropdown.isMandatory() && !isMultiple() && !_preventClear) {
			renderItem(context, out, dropdown, SelectField.NO_OPTION);
		}

		OptionModel<?> _optionModel = SelectFieldUtils.getOptionModel(dropdown);
		if (isInfiniteTree(_optionModel)) {
			throw new RuntimeException("Optionlist is infinite for field " + dropdown.getLabel());
		}
		Comparator<? super Object> comp = SelectFieldUtils.getOptionComparator(dropdown);
		List<?> options = CollectionUtil.toList(_optionModel.iterator());
		Collections.sort(options, comp);
		for (Object item : options) {
			renderItem(context, out, dropdown, item);
		}
	}

	private boolean isInfiniteTree(OptionModel<?> _optionModel) {
		if (_optionModel instanceof TreeOptionModel) {
			return !((TreeOptionModel) _optionModel).getBaseModel().isFinite();
		}
		return false;
	}

	private void renderItem(DisplayContext context, TagWriter out, FormField dropdown, Object item)
			throws IOException {
		String classes = "ddwttItem tooltipHorizontal";
		boolean useDropDownTooltip = false;
		
		if (isMultiple() && SelectFieldUtils.getSelectionList(dropdown).contains(item)) {
			classes += " ddwttSelectedItem";
		}

		out.beginBeginTag(SPAN);
		out.writeAttribute(CLASS_ATTR, classes);
		out.writeAttribute(ID, getItemID(item));
		addItemEvents(out);
		if (!useDropDownTooltip) {
			renderTooltip(context, out, dropdown, item, false);
		}
		out.writeAttribute(TABINDEX_ATTR, "-1");
		out.endBeginTag();
		{
			renderItemLabel(out, dropdown, item);

			if (useDropDownTooltip) {
				renderTooltip(context, out, dropdown, item, true);
			}
		}
		out.endTag(SPAN);
	}

	private void addItemEvents(TagWriter out) throws IOException {
		out.beginAttribute(ONMOUSEOVER_ATTR);
		addJSFunction(out, "positionTt", "this, true");
		out.endAttribute();
		out.beginAttribute(ONFOCUSOUT_ATTR);
		addJSFunction(out, "lostFocus", null);
		out.endAttribute();
		out.beginAttribute(ONKEYDOWN_ATTR);
		addJSFunction(out, "keyPressed", "event, " + isMultiple());
		out.endAttribute();
	}

	private void renderItemLabel(TagWriter out, FormField dropdown, Object item) throws IOException {
		out.beginBeginTag(SPAN);
		out.writeAttribute(CLASS_ATTR, "ddwttItemLabel");
		out.beginAttribute(ONCLICK_ATTR);
		addJSFunction(out, "selectItem", "this.parentElement");
		out.endAttribute();
		out.endBeginTag();
		{
			out.write(getItemLabel(dropdown, item));
		}
		out.endTag(SPAN);
	}

	private void renderTooltip(DisplayContext context, TagWriter out, FormField dropdown, Object item,
			boolean useDropDownTooltip) throws IOException {
		LabelProvider lprovider = SelectFieldUtils.getOptionLabelProvider(dropdown);
		ResourceProvider rprovider = LabelResourceProvider.toResourceProvider(lprovider);
		String tooltip = item == SelectField.NO_OPTION ? null : rprovider.getTooltip(item);
		if (tooltip == null) {
			return;
		}

		if (useDropDownTooltip) {
			out.beginBeginTag(SPAN);
			out.writeAttribute(CLASS_ATTR, "ddwttTooltip");
			out.endBeginTag();
			{
				out.beginBeginTag(SPAN);
				out.writeAttribute(CLASS_ATTR, "ddwttTooltipContent");
				out.endBeginTag();
				{
					// We have to use this deprecated method so tooltips support HTML input
					out.writeContent(HtmlToolTip.ensureSafeHTMLTooltip(tooltip));
				}
				out.endTag(SPAN);
			}
			out.endTag(SPAN);
		} else {
			HTMLUtil.writeImageTooltipHtml(context, out, tooltip);
		}
	}

	private void renderTags(DisplayContext context, TagWriter out) throws IOException {
		FormField dropdown = getFieldModel();

		out.beginBeginTag(SPAN);
		out.writeAttribute(ID, getTagLocID());
		out.writeAttribute(CLASS_ATTR, "ddwttTagLoc");
		out.endBeginTag();
		{
			for (Object selectedItem : SelectFieldUtils.getSelectionListSorted(dropdown)) {
				String itemID = getItemID(selectedItem);

				out.beginBeginTag(SPAN);
				out.writeAttribute(ID, itemID + "-tag");
				out.writeAttribute(CLASS_ATTR, "ddwttTag");
				renderTooltip(context, out, dropdown, selectedItem, false);
				out.endBeginTag();
				{
					out.write(getItemLabel(dropdown, selectedItem));

					renderXButton(context, out, itemID);
				}
				out.endTag(SPAN);
			}
		}
		out.endTag(SPAN);
	}

	private void renderXButton(DisplayContext context, TagWriter out, String itemID) throws IOException {
		XMLTag xButton = Icons.DELETE_BUTTON.toButton();
		xButton.beginBeginTag(context, out);
		out.beginCssClasses();
		out.append(FormConstants.INPUT_IMAGE_ACTION_CSS_CLASS);
		out.append(FormConstants.CLEAR_BUTTON_CSS_CLASS);
		out.append("ddwttTagX");
		out.endCssClasses();
		out.beginAttribute(ONCLICK_ATTR);
		addJSFunction(out, "removeTag", "this.parentElement, '" + itemID + "'");
		out.endAttribute();
		xButton.endEmptyTag(context, out);
	}

	private void addJSFunction(TagWriter out, String function, String custom) throws IOException {
		String jsClass = DROPDOWN_CONTROL_CLASS;
		out.append("return ");
		out.append(jsClass);
		out.append('.');
		out.append(function);
		out.append("(");

		if (custom != null) {
			out.append(custom);
		}
		if (showWait(this)) {
			if ((custom != null) && (custom != "")) {
				out.append(", ");
			}
			out.append("true);");
		} else {
			out.append(");");
		}
	}

	@Override
	protected void writeImmutable(DisplayContext context, TagWriter out) throws IOException {
		FormField dropdown = getFieldModel();
		String separator = SelectFieldUtils.getCollectionSeparator(dropdown);
		List<?> selection = SelectFieldUtils.getSelectionList(dropdown);
		boolean useDropDownTooltip = false;
		boolean first = true;

		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
		{
			if (selection.isEmpty()) {
				out.append(SelectFieldUtils.getEmptySelectionLabelImmutable(dropdown));
			}
			
			if (_selectionRenderer != null) {
				_selectionRenderer.write(context, out, selection);
			} else {
				for (Object item : selection) {
					if (first) {
						first = false;
					} else {
						out.append(separator);
					}
					renderImmutableItem(context, out, dropdown, useDropDownTooltip, item);
				}
			}
		}
		out.endTag(SPAN);
	}

	private void renderImmutableItem(DisplayContext context, TagWriter out, FormField dropdown,
			boolean useDropDownTooltip, Object item) throws IOException {
		out.beginBeginTag(SPAN);
		out.writeAttribute(CLASS_ATTR, "ddwttImmutableItem");
		out.beginAttribute(ONMOUSEOVER_ATTR);
		addJSFunction(out, "positionTt", "this, false");
		out.endAttribute();
		out.beginAttribute(ONMOUSEOUT_ATTR);
		addJSFunction(out, "setItemInactive", "this");
		out.endAttribute();
		if (!useDropDownTooltip) {
			renderTooltip(context, out, dropdown, item, useDropDownTooltip);
		}
		out.endBeginTag();
		{
			out.write(getItemLabel(dropdown, item));
			if (useDropDownTooltip) {
				renderTooltip(context, out, dropdown, item, useDropDownTooltip);
			}
		}
		out.endTag(SPAN);
	}

	@Override
	protected void internalHandleDisabledEvent(FormMember sender, Boolean oldValue, Boolean newValue) {
		addDisabledUpdate(newValue.booleanValue());
	}

	@Override
	public void valueChanged(FormField field, Object oldValue, Object newValue) {
		if (!skipEvent(field) && isAttached()) {
			if (isMultiple()) {
				addUpdate(new ElementReplacement(getTagLocID(), this::renderTags));

				List<Object> changes = new ArrayList<>((List<?>) oldValue);
				List<Object> additions = new ArrayList<>((List<?>) newValue);
				changes.removeAll(additions);
				additions.removeAll((List<?>) oldValue);
				changes.addAll(additions);

				for (Object item : changes) {
					if (CollectionUtil.toList(SelectFieldUtils.getOptionModel(field).iterator()).contains(item)) {
						addUpdate(
							new JSFunctionCall(
								getItemID(item),
								DROPDOWN_CONTROL_CLASS,
								"changeSelectedState"));
					}
				}
			}
			addUpdate(
				new JSFunctionCall(
					getInputId(),
					DROPDOWN_CONTROL_CLASS, "setSelectedLabel",
					getSelectionLabel(field)));
		}
	}

	@Override
	protected void internalHandleValueChanged(FormField field, Object oldValue, Object newValue) {
		// Implemented valueChanged() directly, because we need the actual objects of the selection.
	}

	@Override
	public Bubble handleMandatoryChanged(FormField sender, Boolean oldValue, Boolean newValue) {
		if (!skipEvent(sender)) {
			requestRepaint();
		}
		return Bubble.BUBBLE;
	}

	/**
	 * Command to save selected item, so this will be set on reload.
	 * 
	 * @author sha
	 */
	protected static class DropDownItemSelected extends ControlCommand {

		/**
		 * ID of the command
		 */
		public static final String COMMAND_ID = "ddItemSelected";

		/**
		 * Single instance of the {@link DropDownItemSelected}
		 */
		public static final ControlCommand INSTANCE = new DropDownItemSelected();

		/**
		 * Constructor for the item selected command
		 */
		public DropDownItemSelected() {
			super(COMMAND_ID);
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			AbstractFormField dropdown = (AbstractFormField) control.getModel();
			Object selItem = ((DropDownControl) control).getItemById((String) arguments.get("itemID"));

			if (selItem == SelectField.NO_OPTION) {
				deliverValue(commandContext, dropdown, Collections.emptyList());
			} else if (SelectFieldUtils.isMultiple(dropdown)) {
				List<Object> selection = new ArrayList<>(SelectFieldUtils.getSelectionList(dropdown));
				if (selection.contains(selItem)) {
					selection.remove(selItem);
				} else {
					selection.add(selItem);
				}
				deliverValue(commandContext, dropdown, selection);
			} else {
				deliverValue(commandContext, dropdown, Collections.singletonList(selItem));
			}
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.DD_ITEM_SELECTED;
		}
	}

	private static void deliverValue(DisplayContext commandContext, AbstractFormField dropdown,
			List<Object> newSelection) {
		final Object newValue;
		if (dropdown instanceof SelectField) {
			// Always uses list as value.
			newValue = newSelection;
		} else {
			if (SelectFieldUtils.isMultiple(dropdown)) {
				newValue = newSelection;
			} else {
				newValue = CollectionUtil.getSingleValueFromCollection(newSelection);
			}
		}

		try {
			FormFieldInternals.setValue(dropdown, newValue);
		} catch (VetoException ex) {
			ex.setContinuationCommand(context -> {
				dropdown.setValue(newValue);
				return HandlerResult.DEFAULT_RESULT;
			});
			ex.process(commandContext.getWindowScope());
		}
	}
}