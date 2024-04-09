/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;

import com.top_logic.base.services.simpleajax.JSFunctionCall;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.ExceptionUtil;
import com.top_logic.basic.col.IDBuilder;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ResourceRenderer;
import com.top_logic.layout.basic.contextmenu.ContextMenuProvider;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.FormFieldInternals;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.SelectFieldUtils;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.form.model.utility.OptionModelListener;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;
import com.top_logic.util.css.CssUtil;

/**
 * Drop-down selection list view of a {@link FormField} model.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SelectControl extends AbstractSelectControl implements OptionModelListener {

	private static final String CSS_SELECT = "tl-select";

	private static final String CSS_SELECT_CLASSES =
		CssUtil.joinCssClasses(FormConstants.IS_INPUT_CSS_CLASS, CSS_SELECT);

	private static final String CURRENT_SELECTION_DATA_ATTRIBUTE =
		HTMLConstants.DATA_ATTRIBUTE_PREFIX + "currentselection";

	private static final String CANRESET_DATA_ATTRIBUTE =
		HTMLConstants.DATA_ATTRIBUTE_PREFIX + "canreset";

	private static final String SELECTED_VALUE = "valueDisplay";

	/**
	 * The amount of concurrently displayed options of a multiple selection.
	 */
	private int size;
	
	/**
	 * Whether the {@link #size} property is set.
	 */
	private boolean hasSize;

	private DisplayDimension _width;

	private DisplayDimension _height;

	private final boolean preventClear;

	/** @see #forceDisplayAsList(boolean) */
	private boolean _displayAsList;

	private IDBuilder _idBuilder = new IDBuilder();

	private Renderer<Object> _selectionRenderer;

	private OptionModel<?> _optionModel;

	protected static final Map<String, ControlCommand> COMMANDS = createCommandMap(
		AbstractFormFieldControl.COMMANDS,
		new ControlCommand[] { SelectionChanged.INSTANCE });

	/**
	 * {@link ControlCommand} that deliver the new selection from the UI.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	protected static class SelectionChanged extends ValueChanged {
		/**
		 * Singleton {@link SelectControl.SelectionChanged} instance.
		 */
		public static final SelectControl.SelectionChanged INSTANCE = new SelectControl.SelectionChanged();

		private SelectionChanged() {
			// Singleton constructor.
		}

		@Override
		protected void updateValue(DisplayContext commandContext, AbstractFormFieldControlBase formFieldControl,
				Object newValue, Map<String, Object> arguments) {
			// Prevent mirroring the change back to the UI by eagerly updating the compare value.
			formFieldControl.updateRawValue(newValue);

			SelectControl control = (SelectControl) formFieldControl;
			control.deliverValue(newValue);
		}
	}

	/**
	 * Creates a {@link SelectControl} which can be "cleared".
	 * 
	 * @see #SelectControl(FormField, boolean)
	 */
	public SelectControl(FormField model) {
		this(model, false);
	}

	/**
	 * Creates a {@link SelectControl}.
	 * 
	 * @param model
	 *        The {@link FormField} model.
	 * @param preventClear
	 *        Whether the "no selection choice" should be suppressed for single select fields that
	 *        are non-mandatory.
	 */
	public SelectControl(FormField model, boolean preventClear) {
		super(model, COMMANDS);
		this.preventClear = preventClear;
		LabelProvider optionLabels = SelectFieldUtils.getOptionLabelProvider(model);
		if (optionLabels == null) {
			optionLabels = MetaResourceProvider.INSTANCE;
		}
		ContextMenuProvider contextMenu = SelectFieldUtils.getOptionContextMenu(model);
		setSelectionRenderer(ResourceRenderer.newResourceRenderer(optionLabels, contextMenu));
	}
	
	@Override
	protected void detachInvalidated() {
		super.detachInvalidated();

		_idBuilder.clear();
		resetOptionModel();
	}

	/**
	 * {@link Renderer} that displays the current selection list when the display is immutable.
	 */
	public void setSelectionRenderer(Renderer<Object> selectionRenderer) {
		_selectionRenderer = selectionRenderer;
	}

	/**
	 * Sets the separator that separates selected options in display mode.
	 * 
	 * @param newSeparator
	 *        the new separator
	 */
	public void setSeparator(String newSeparator) {
		if (newSeparator != null) {
			SelectFieldUtils.setMultiSelectionSeparator(getFieldModel(), newSeparator, newSeparator + " ");
		} else {
			SelectFieldUtils.removeMultiSelectionSeparator(getFieldModel());
		}
	}

	/**
	 * Enforces this {@link SelectControl} to render the field as list and not as drop down menu
	 * when field has single selection.
	 * 
	 * @param asList
	 *        whether the field must be rendered as list also if not
	 *        {@link SelectField#isMultiple()}
	 */
	public void forceDisplayAsList(boolean asList) {
		if (this._displayAsList == asList) {
			return;
		}
		this._displayAsList = asList;
		requestRepaint();
	}

	/**
	 * Sets the amount of concurrently displayed options of a multiple selection or the number of
	 * entries shown in a selection drop-down box.
	 * 
	 * @param newSize
	 *        the new number of concurrently displayed options in a multiple selection.
	 */
	public void setSize(int newSize) {
		this.hasSize = true;
		this.size = newSize;
	}
	
	/**
	 * The custom width (as CSS value) of the select field, <code>null</code> to use default values.
	 */
	public DisplayDimension getWidth() {
		return _width;
	}

	/**
	 * The custom width of the select field, <code>null</code> to use default values.
	 * 
	 * @param value
	 *        The CSS width value for the select field.
	 * 
	 * @see #getWidth()
	 */
	public void setWidth(DisplayDimension value) {
		_width = value;
	}

	/**
	 * The custom height (as CSS value) of the select field (only relevant in case of a
	 * multi-selection-box), <code>null</code> to use default values.
	 */
	public DisplayDimension getHeight() {
		return _height;
	}

	/**
	 * @see #getHeight()
	 */
	public void setHeight(DisplayDimension value) {
		_height = value;
	}

	/**
	 * Removes the explicit given number of concurrently displayed options in a multiple selection,
	 * i.e. the browser decides about it.
	 */
	public void removeSize() {
		this.hasSize = false;
		this.size = 0;
	}

	@Override
	protected String getTypeCssClass() {
		return "cDropdownSelect";
	}

	/*
	 * See http://www.w3.org/TR/xhtml1/dtds.html#dtdentry_xhtml1-strict.dtd_select
	 * 
	 * <!ELEMENT select (optgroup|option)+>  <!-- option selector -->
	 * <!ATTLIST select
	 *   %attrs;
	 *   name        CDATA          #IMPLIED
	 *   size        %Number;       #IMPLIED
	 *   multiple    (multiple)     #IMPLIED
	 *   disabled    (disabled)     #IMPLIED
	 *   tabindex    %Number;       #IMPLIED
	 *   onfocus     %Script;       #IMPLIED
	 *   onblur      %Script;       #IMPLIED
	 *   onchange    %Script;       #IMPLIED
	 *   >
	 *   
	 * <!ELEMENT optgroup (option)+>   <!-- option group -->
	 * <!ATTLIST optgroup
	 *   %attrs;
	 *   disabled    (disabled)     #IMPLIED
	 *   label       %Text;         #REQUIRED
	 *   >
	 * 
	 * <!ELEMENT option (#PCDATA)>     <!-- selectable choice -->
	 * <!ATTLIST option
	 *   %attrs;
	 *   selected    (selected)     #IMPLIED
	 *   disabled    (disabled)     #IMPLIED
	 *   label       %Text;         #IMPLIED
	 *   value       CDATA          #IMPLIED
	 *   >
	 */	
	@Override
	protected void writeEditable(DisplayContext context, TagWriter out)	throws IOException {
		List<Throwable> renderingErrors = new ArrayList<>();

		initOptionModel(SelectFieldUtils.getOptionModel(getFieldModel()));

		List<?> options = getDisplayedOptions();
		Set<?> selection = getSelectionSet();

		boolean multiple = isMultiple();
		int optionCount = options.size();

		boolean renderExplicitNoOption = renderExplicitNoOption();
		
		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
		{
			out.beginBeginTag(SPAN);
			out.writeAttribute(CLASS_ATTR, FormConstants.FLEXIBLE_CSS_CLASS);
			out.endBeginTag();

			// Input element
			out.beginBeginTag(SELECT);
			writeInputIdAttr(out);
			out.writeAttribute(CLASS_ATTR, CSS_SELECT_CLASSES);
			writeQualifiedNameAttribute(out);
			
			writeOnChange(out);

			out.beginAttribute(STYLE_ATTR);
			{
				if (_width != null) {
					out.append("width: ");
					out.append(_width.toString());
					out.append(';');
				}
				if (multiple && _height != null) {
					out.append("height: ");
					out.append(_height.toString());
					out.append(';');
				}
				out.append(getInputStyle());
			}
			out.endAttribute();

			if (multiple) {
				out.writeAttribute(MULTIPLE_ATTR, MULTIPLE_MULTIPLE_VALUE);
			} else {
				if (_displayAsList) {
					out.writeAttribute(MULTIPLE_ATTR, MULTIPLE_MULTIPLE_VALUE);
					out.writeAttribute(CANRESET_DATA_ATTRIBUTE, !renderExplicitNoOption && canReset());
				}
				// set the on the client selected element as property
				int clientSideSelection = -1;
				if (!selection.isEmpty()) {
					for (int optionNr = 0; optionNr < optionCount; optionNr++) {
						Object option = options.get(optionNr);
						if (selection.contains(option)) {
							clientSideSelection = optionNr;
							break;
						}
					}
				}
				if (renderExplicitNoOption) {
					// an additional option is rendered
					clientSideSelection++;
				}
				assert _displayAsList || clientSideSelection >= 0 : "There is either an explicit 'noOption' or a selection";
				out.writeAttribute(CURRENT_SELECTION_DATA_ATTRIBUTE, clientSideSelection);
			}

			if (getFieldModel().isDisabled()) {
				out.writeAttribute(DISABLED_ATTR, DISABLED_DISABLED_VALUE);
			}
			
			if (multiple && hasSize) {
				out.writeAttribute(SIZE_ATTR, size);
			}

			if (hasTabIndex()) {
				out.writeAttribute(TABINDEX_ATTR, getTabIndex());
			}
			out.endBeginTag();

			if (renderExplicitNoOption) {
				// Add the implicit option to the option list that represents
				// the empty selection in single selection views.
				out.beginBeginTag(OPTION);
				out.writeAttribute(VALUE_ATTR, makeID(SelectField.NO_OPTION));
				if (selection.isEmpty()) {
					out.writeAttribute(SELECTED_ATTR, SELECTED_SELECTED_VALUE);
				}
				out.endBeginTag();
				out.writeText(getOptionLabel(SelectField.NO_OPTION, renderingErrors));
				out.endTag(OPTION);
			}
			
			for (int optionNr = 0; optionNr < optionCount; optionNr++) {
				Object option = options.get(optionNr);

				out.beginBeginTag(OPTION);
				out.writeAttribute(VALUE_ATTR, makeID(option));
				if (selection.contains(option)) {
					out.writeAttribute(SELECTED_ATTR, SELECTED_SELECTED_VALUE);
				}
				out.endBeginTag();
				out.writeText(getOptionLabel(option, renderingErrors));
				out.endTag(OPTION);
			}

			out.endTag(SELECT);

			out.endTag(SPAN);
		}
		if (!CollectionUtils.isEmpty(renderingErrors)) {
			Throwable labelRenderingError = ExceptionUtil.createException(
				"Error occured during rendering of options of field '" + getFieldModel().getQualifiedName() + "'.",
				renderingErrors);
			produceErrorOutput(context, out, labelRenderingError);
		}
		out.endTag(SPAN);
	}

	private void writeOnChange(TagWriter out) throws IOException {
		// A select element must have registered its onchange handler
		// with the 'onmouseup' and the 'onchange' event.
		//
		// This is necessary to work around a browser bug (at least in Firefox)
		// which prevents the 'onchange' event from being fired in case the
		// first option from such a select box is chosen that has currently
		// no selection (none of its option elements has the selected property
		// set and its selectedIndex is -1).
		//
		// Having handler registered with the 'onmouseup' instead of
		// the 'onchange' event causes the generation of slightly more events
		// than necessary (if a user does open the selection list but does not
		// choose a (new) option). These no-ops are detected and ignored on the
		// server-side. There is one exception: If the first event triggers
		// (through a change listener) a component reload, the second event may
		// hit the same control, but in a different state (newly rendered). The
		// newly rendered control may not be able to process the second event,
		// because its internal ID mapping has changed. Therefore, controls must
		// be enabled to detect and ignore late events.
		//
		// In IE, no 'onmouseup' event is generated, if the user selects a choice
		// by clicking on the pulldown menu. An 'onchange' event is
		// generated, therefore, the 'onchange' handler is also required.

		out.beginAttribute(ONMOUSEUP_ATTR);
		writeOnChangeContent(out);
		out.endAttribute();

		out.beginAttribute(ONCHANGE_ATTR);
		writeOnChangeContent(out);
		out.endAttribute();
	}

	private void writeOnChangeContent(TagWriter out) throws IOException {
		if (isMultiple()) {
			writeHandleOnChangeAction(out, FormConstants.SELECT_CONTROL_CLASS, this, null);
		} else {
			writeJSAction(out, FormConstants.SELECT_CONTROL_CLASS, "handleOnChangeSingleSelect", this, null);
		}
	}

	private boolean renderExplicitNoOption() {
		boolean singleSelection = !isMultiple();
		boolean thereIsCurrentlyNoSelection = getSelectionList().size() == 0;

		// Whether a single selection view renders an explicit "empty selection
		// option" is decided as follows: It is either specified as tag (=view)
		// attribute, that the empty choice is (theoretically) allowed, or the
		// current selection is (de-facto) empty.
		//
		// In the second case, an empty selection option is rendered, even if
		// this view was configured not to provide an empty selection option.
		// The reason for that is as follows:
		//
		// First: Rendering an empty selection option always if the current
		// selection is (de-facto) empty allows to "create" an object from an
		// initially empty form context with all-default values using the same
		// tag (=view) attributes as for editing an already created object.
		//
		// Second: Without an empty selection option, a de-facto empty selection
		// cannot be rendered safely, because browsers tend to "automatically"
		// select some of the options of a single selection view.
		boolean renderExplicitNoOption;
		if (singleSelection && !_displayAsList) {
			if (thereIsCurrentlyNoSelection) {
				renderExplicitNoOption = true;
			} else {
				renderExplicitNoOption = canReset();
			}
		} else {
			renderExplicitNoOption = false;
		}
		return renderExplicitNoOption;
	}

	private boolean canReset() {
		boolean mandatory = getFieldModel().isMandatory();
		return !(mandatory || this.preventClear);
	}

	private String makeID(Object option) {
		return _idBuilder.makeId(option);
	}

	@Override
	protected Object toRawValue(FormField field, Object newValue) {
		// Note: The "raw" value (the value that represents the current application value at the UI)
		// in case of a select control is different from the raw value stored in the field. Both,
		// the field and the control have their ID builders which produce different IDs. Therefore,
		// the raw value that is used for testing, whether a client-side update is required must be
		// computed with the ID builder of the control, not with the one of the field.
		List<String> selectedIDs = new ArrayList<>();
		if (newValue instanceof List<?>) {
			List<?> selectionList = (List<?>) newValue;
			for (Object selection : selectionList) {
				String id = _idBuilder.makeId(selection);
				selectedIDs.add(id);
			}
		} else {
			// Note: In case the field is not a SelectField, the value might not be a list.
			String id = _idBuilder.makeId(newValue);
			selectedIDs.add(id);
		}
		return selectedIDs;
	}

	private String getOptionLabel(Object option, List<Throwable> labelErrors) {
		String optionLabel;
		try {
			optionLabel = SelectFieldUtils.getOptionLabel(getFieldModel(), option);
		} catch (Throwable throwable) {
			labelErrors.add(throwable);
			optionLabel = Resources.getInstance().getString(I18NConstants.RENDERING_ERROR_SELECT_FIELD);
		}
		return optionLabel;
	}

	private boolean isMultiple() {
		FormField field = getFieldModel();
		return SelectFieldUtils.isMultiple(field);
	}

	private List<?> getSelectionListSorted() {
		return SelectFieldUtils.getSelectionListSorted(getFieldModel());
	}

	private List<?> getSelectionList() {
		return SelectFieldUtils.getSelectionList(getFieldModel());
	}

	private Set<?> getSelectionSet() {
		Object value = getFieldModel().getValue();
		if (value instanceof Collection<?>) {
			return CollectionUtil.toSet((Collection<?>) value);
		}
		return CollectionUtilShared.singletonOrEmptySet(value);
	}

	private List<?> getDisplayedOptions() {
		return SelectFieldUtils.joinOrdered(getOptionComparator(), getOptionList(), getSelectionList());
	}

	private void initOptionModel(OptionModel<?> optionModel) {
		resetOptionModel();

		_optionModel = optionModel;
		_optionModel.addOptionListener(this);
	}

	@Override
	public void notifyOptionsChanged(OptionModel<?> sender) {
		resetOptionModel();
		requestRepaint();
	}

	private void resetOptionModel() {
		if (_optionModel != null) {
			_optionModel.removeOptionListener(this);
			_optionModel = null;
		}
	}

	private List<?> getOptionList() {
		return CollectionUtil.toList(_optionModel.iterator());
	}

	private Comparator<? super Object> getOptionComparator() {
		return SelectFieldUtils.getOptionComparator(getFieldModel());
	}

    @Override
    protected void handleInputStyleChange() {
		if (getModel().isActive()) {
    		addUpdate(JSFunctionCall.setStyle(getInputId(), getInputStyle()));
    	} else {
    		super.handleInputStyleChange();
    	}
    }
	
	@Override
	protected void writeImmutable(DisplayContext context, TagWriter out) throws IOException {
		FormField field = getFieldModel();

		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
		{
			// some anchor to define style
		    out.beginBeginTag(SPAN);
		    out.writeAttribute(CLASS_ATTR, SELECTED_VALUE);
		    out.endBeginTag();
			{
				// Content
				if (_selectionRenderer != null) {
					List<?> selectionList = getSelectionListSorted();
					_selectionRenderer.write(context, out, selectionList);
				} else {
					try {
						SelectFieldUtils.writeSelectionImmutable(context, out, field);
					} catch (Throwable throwable) {
						try {
							produceErrorOutput(context, out, throwable);
						} catch (Throwable inner) {
							// In the rare case of catastrophe better throw the original.
							throw throwable;
						}
					}
				}
			}
			out.endTag(SPAN);
		}
		out.endTag(SPAN);
	}

	@Override
	public void internalHandleDisabledEvent(FormMember sender, Boolean oldValue, Boolean newValue) {
		addDisabledUpdate(newValue.booleanValue());
	}

	@Override
	public Bubble handleMandatoryChanged(FormField sender, Boolean oldValue, Boolean newValue) {
		if (!skipEvent(sender)) {
			/* The control renders a "nothing selected" option in not mandatory case but not in
			 * mandatory case. Therefore the option must either be added or removed from the client.
			 * The easiest way is a complete redraw (#19360). */
			requestRepaint();
		}
		return Bubble.BUBBLE;
	}

	@Override
	protected void internalHandleValueChanged(FormField field, Object oldValue, Object newValue) {
		if (field.isImmutable()) {
	        requestRepaint();
		} else if (field.isVisible()) {
			List<?> selectionList = getSelectionList();
			List<Object> selectedIDs = new ArrayList<>(selectionList.size() + 1);
			if (selectionList.isEmpty() && renderExplicitNoOption()) {
				String id = _idBuilder.lookupId(SelectField.NO_OPTION);
				if (id == null) {
					// A value was set that is not within the currently displayed options, need
					// repaint to add the option.
					requestRepaint();
					return;
				}
				selectedIDs.add(id);
			}
			for (Object selection : selectionList) {
				String id = _idBuilder.lookupId(selection);
				if (id == null) {
					// A value was set that is not within the currently displayed options, need
					// repaint to add the option.
					requestRepaint();
					return;
				}
				selectedIDs.add(id);
			}
			addUpdate(
	                new JSFunctionCall(
	                        getInputId(), 
	                        FormConstants.SELECT_CONTROL_CLASS, "setValue", 
				new Object[] { selectedIDs }));
	    }
	}
	
	/**
	 * Parses a new value from the UI and forwards it to the field model.
	 * 
	 * @param newClientValue
	 *        The JavaScript value (ID list) from the UI.
	 */
	protected void deliverValue(Object newClientValue) {
		List<?> newSelectionIDs = (List<?>) newClientValue;
		List<Object> newSelection = new ArrayList<>(newSelectionIDs.size());
		for (Object selectionID : newSelectionIDs) {
			Object option = _idBuilder.getObjectById((String) selectionID);
			if (option != SelectField.NO_OPTION) {
				newSelection.add(option);
			}
		}
		final FormField field = getFieldModel();

		final Object newValue;
		if (field instanceof SelectField) {
			// Always uses list as value.
			newValue = newSelection;
		} else {
			if (isMultiple()) {
				newValue = newSelection;
			} else {
				newValue = CollectionUtil.getSingleValueFromCollection(newSelection);
			}
		}

		try {
			FormFieldInternals.setValue((AbstractFormField) field, newValue);
		} catch (VetoException ex) {
			requestRepaint();
			ex.setContinuationCommand(new Command() {

				@Override
				public HandlerResult executeCommand(DisplayContext context) {
					field.setValue(newValue);
					return HandlerResult.DEFAULT_RESULT;
				}
			});
			ex.process(getWindowScope());
		}
	}
}
