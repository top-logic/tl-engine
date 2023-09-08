/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.base.services.simpleajax.ContentReplacement;
import com.top_logic.base.services.simpleajax.ElementReplacement;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.base.services.simpleajax.PropertyUpdate;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.ExceptionUtil;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.GenericPropertyListener;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.AbstractDisplayValue;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ResourceRenderer;
import com.top_logic.layout.buttonbar.Icons;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.decorator.DecorateInfo;
import com.top_logic.layout.form.decorator.DecorateService;
import com.top_logic.layout.form.dnd.FieldDrop;
import com.top_logic.layout.form.dnd.FieldDropTarget;
import com.top_logic.layout.form.model.FormFieldInternals;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.SelectFieldUtils;
import com.top_logic.layout.form.selection.AbstractSelectDialog;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * A {@link com.top_logic.layout.Control} that controls the view of a {@link SelectField}.
 * 
 * <p>
 * A {@link SelectionControl} displays itself a an input field with a button to open a dialog for
 * choosing options and optionally a button for clearing the current selection.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SelectionControl extends AbstractFormFieldControlBase {

	/**
	 * Value for {@link #setColumns(int)} that prevents a size attribute from being written.
	 */
	public static final int NO_COLUMNS = 0;

	private static final String STATUS_DATA_ATTRIBUTE = HTMLConstants.DATA_ATTRIBUTE_PREFIX + "status";

	private static final String	FORM_INPUT_CONSTANT	= "services.form.SelectionControl";

	public static final Map<String, ControlCommand> COMMANDS = createCommandMap(
		AbstractFormFieldControlBase.COMMANDS,
		new ControlCommand[] {
   			ClearSelection.INSTANCE,
   			OpenSelector.INSTANCE,
   			AutoCompletion.INSTANCE,
			FieldDrop.INSTANCE,
		});
	
	private String openButtonID;
	
	private String clearButtonID;

	private int columns = NO_COLUMNS;

	private boolean hasClearButton;
	
	private boolean useAutoCompletion;
	
	private Renderer<Object> optionRenderer;
	
	private boolean	hasInputField;
	
	private boolean hasPopupOpener;

	private List sendOptions;
	
	public SelectionControl(SelectField model) {
		this(model, COMMANDS);
	}
	
	protected SelectionControl(SelectField model, Map<String, ControlCommand> commandsByName) {
	   super(model, commandsByName);
	   
	   this.hasClearButton    = !model.isMandatory();
	   this.hasInputField     = true;
	   this.useAutoCompletion = true;
	   this.hasPopupOpener = true;
	   this.sendOptions       = Collections.EMPTY_LIST;
   }
   
	/**
	 * The number of text columns to display.
	 *
	 * <p>
	 * A value of {@link #NO_COLUMNS} means not to use a size attribute.
	 * </p>
	 */
	public void setColumns(int value) {
		this.columns = value;
	}

	public void setClearButton(boolean value) {
		this.hasClearButton = value;
	}

	public boolean getClearButton() {
		return this.hasClearButton;
	}

    public boolean hasInputField() {
    	return this.hasInputField;
    }
    
    public void setInputField(boolean value) {
    	this.hasInputField = value;
    }
    
	/** Whether the {@link SelectField} has a button opening selection popup. */
	public boolean hasPopupOpener() {
		return hasPopupOpener;
	}

	/** Whether the {@link SelectField} has a button clearing the current selection. */
	public boolean hasClearButton() {
		return hasClearButton;
	}

	public void setPopupOpener(boolean showPopupOpener) {
		hasPopupOpener = showPopupOpener;
	}

    public void setUseAutoCompletion(boolean value) {
    	this.useAutoCompletion = value;
    }
    
    public boolean useAutoCompletion() {
    	return this.useAutoCompletion;
    }
    
	public void setOptionRenderer(Renderer<Object> optionRenderer) {
		this.optionRenderer = optionRenderer;
	}

	public Renderer<Object> getOptionRenderer() {
		return this.optionRenderer;
	}

	public SelectField getSelectionModel() {
		return (SelectField) super.getModel();
	}
	
	/**
	 * Necessary to keep track on the last send entries that matched the user input. 
	 */
	public List getSendOptions() {
		return this.sendOptions;
	}
	
	public void setSendOptions(List someOptions) {
		this.sendOptions = someOptions;
	}

	/**
	 * Translates value changes of this {@link com.top_logic.layout.Control}'s field into update or
	 * re-draw actions.
	 * 
	 * <p>
	 * Note: Observing the value of a {@link SelectField} must be done as {@link ValueListener}
	 * instead of as {@link GenericPropertyListener}. The reason is that the field does not fire a property
	 * event if it is updated with client-side values , because it assumes that these values were
	 * transmitted from the client in response to a user interaction (which already would have set
	 * the corresponding value into the fields view).
	 * </p>
	 * 
	 * @see ValueListener#valueChanged(FormField, Object, Object)
	 */
	@Override
	protected void internalHandleValueChanged(FormField field, Object oldValue, Object newValue) {
		final SelectField theModel = getSelectionModel();

		if (isRepaintRequested())
			return;

		if (theModel.isImmutable()) {
			requestRepaint();
		} else if (theModel.isVisible()) {
			if (hasInputField()) {
				addUpdate(new PropertyUpdate(getInputId(), VALUE_ATTR, new AbstractDisplayValue() {

					@Override
					public void append(DisplayContext context, Appendable out) throws IOException {
						SelectFieldUtils.writeSelectionAsTextEditable(out, theModel);
					}

				}));
			}
			if (hasClearButton) {
				addClearButtonUpdates();
			}
		}
	}

	private void addClearButtonUpdates() {
		addUpdate(new ElementReplacement(getClearButtonSpanID(), new HTMLFragment() {
			@Override
			public void write(DisplayContext context, TagWriter out) throws IOException {
				writeClearButton(context, out);
			}
		}));
	}

	@Override
	public void internalHandleDisabledEvent(FormMember sender, Boolean oldValue, Boolean newValue) {
		requestRepaint();
	}

	@Override
	protected String getTypeCssClass() {
		return "cPopupSelect";
	}

	@Override
	protected void doInternalWrite(DisplayContext context, TagWriter out) throws IOException {
		Throwable renderingError = null;
		SelectField theModel = getSelectionModel();

		if (theModel.isBlocked()) {
			AbstractFormFieldControl.writeBlocked(context, out, this,
				com.top_logic.layout.form.I18NConstants.BLOCKED_SELECTION_TEXT);
			return;
		}
		
		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
		{
			if (theModel.isImmutable()) {
				if (theModel.hasValue()&& hasInputField()) {
					List<Throwable> labelErrors = new ArrayList<>();
					Renderer<Object> theRenderer = getRenderer(theModel);
					List sortedSelection = theModel.getSelection();
					if (!theModel.hasCustomOrder()) {
						sortedSelection = new ArrayList(sortedSelection);
						Collections.sort(sortedSelection, theModel.getOptionComparator());
					}
					DecorateInfo decorateInfo = DecorateService.start(context, out, theModel, getLabelProvider(theModel));
					int itemContainerDepth = out.getDepth();
					for (Iterator it = sortedSelection.iterator(); it.hasNext();) {
						try {
							theRenderer.write(context, out, it.next());
						} catch (Throwable throwable) {
							out.endAll(itemContainerDepth);
							labelErrors.add(throwable);
							out.writeText(
								Resources.getInstance().getString(I18NConstants.RENDERING_ERROR_SELECT_FIELD));
						}
						if (it.hasNext()) {
							out.writeText(SelectFieldUtils.getCollectionSeparator(theModel));
						}
					}
					DecorateService.end(context, out, theModel, decorateInfo);
					if (!CollectionUtil.isEmpty(labelErrors)) {
						renderingError = ExceptionUtil.createException(
							"Error occured during rendering of options of field '" + getFieldModel().getQualifiedName()
								+ "'.",
							labelErrors);
					}
				}
			} else if (theModel.isVisible()) {
				/* 1. Render the input field. */
				if (hasInputField()) {
					out.beginBeginTag(SPAN);
					out.writeAttribute(CLASS_ATTR, FormConstants.FLEXIBLE_CSS_CLASS);
					out.endBeginTag();

					out.beginBeginTag(INPUT);
					writeInputIdAttr(out);

					FieldDropTarget dropTarget = FieldDrop.getDropTarget(theModel);
					if (dropTarget.dropEnabled(theModel)) {
						out.writeAttribute(ONDROP_ATTR,
							"return services.form.SelectionControl.handleOnDrop(event, this);");
						out.writeAttribute(ONDRAGOVER_ATTR,
							"return services.form.SelectionControl.handleOnDragOver(event, this);");
						out.writeAttribute(ONDRAGENTER_ATTR,
							"return services.form.SelectionControl.handleOnDragEnter(event, this);");
						out.writeAttribute(ONDRAGLEAVE_ATTR,
							"return services.form.SelectionControl.handleOnDragLeave(event, this);");
					}

					out.writeAttribute(TYPE_ATTR, TEXT_TYPE_VALUE);
					out.writeAttribute(CLASS_ATTR, FormConstants.IS_POPUP_CSS_CLASS);
					out.writeAttribute(STYLE_ATTR, getInputStyle());
					if (columns > NO_COLUMNS) {
						out.writeAttribute(SIZE_ATTR, columns);
					}
					out.writeAttribute(NAME_ATTR, theModel.getQualifiedName());
					
					if (isTextSelectionInputDisabled(theModel)) {
						out.writeAttribute(DISABLED_ATTR, DISABLED_DISABLED_VALUE);
					}
					else if(useAutoCompletion){
						writeOnKeyDown(out);
						writeOnFocus(context, out);
						writeOnChange(out);
						writeOnBlur(out);

						out.writeAttribute( "autocomplete", "off" );
					} else {
						writeOnChange(out);
					}
					if (theModel.hasValue()) {
						out.beginAttribute(VALUE_ATTR);
						try {
							SelectFieldUtils.writeSelectionAsTextEditable(out, theModel);
						} catch (Throwable throwable) {
							renderingError = throwable;
						}
						out.endAttribute();
					} else {
						Object rawValue = theModel.getRawValue();
						if (rawValue instanceof String) {
							out.writeAttribute(VALUE_ATTR, (String)rawValue);
						}
					}
					out.endEmptyTag();

					out.endTag(SPAN);
				}

				out.beginBeginTag(SPAN);
				out.writeAttribute(CLASS_ATTR, FormConstants.FIXED_RIGHT_CSS_CLASS);
				out.endBeginTag();

				writeButtons(context, out, theModel);

				out.endTag(SPAN);
			}
		}

		if (renderingError != null) {
			produceErrorOutput(context, out, renderingError);
		}
		out.endTag(SPAN);
	}

	/**
	 * Writes the buttons on the right-hand side of the input field.
	 */
	protected void writeButtons(DisplayContext context, TagWriter out, SelectField model) throws IOException {
		if (hasPopupOpener()) {
			boolean isDisabled = model.isDisabled();
			String id = getOpenButtonID();
			ControlCommand command = getOpenSelectorCommand();
			String label = model.getLabel();

			writePopupButton(context, out, this, isDisabled, id, command, label);
		}

		/* 4. Render clear image. */
		if (hasClearButton()) {
			writeClearButton(context, out);
		}

	}

	void writeClearButton(DisplayContext context, TagWriter out) throws IOException {
		boolean emptySelection = hasEmptySelection();
		boolean onlyFixedSelection = emptySelection ? false : hasOnlyFixedSelection();

		boolean clearButtonActive = isClearButtonActive();

		out.beginBeginTag(SPAN);
		out.writeAttribute(ID_ATTR, getClearButtonSpanID());
		if (onlyFixedSelection) {
			OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out,
				Resources.getInstance().getString(
					com.top_logic.layout.form.I18NConstants.ONLY_FIXED_OPTIONS_SELETED_MESSAGE));
		}
		out.endBeginTag();

		ButtonWriter buttonWriter = new ButtonWriter(this, com.top_logic.layout.form.tag.Icons.DELETE_BUTTON,
			com.top_logic.layout.form.tag.Icons.DELETE_BUTTON_DISABLED, getClearSelectionCommand());
		buttonWriter.setID(getClearButtonID());
		buttonWriter.setCss(FormConstants.CLEAR_BUTTON_CSS_CLASS);
		buttonWriter.setTooltip(
			com.top_logic.layout.form.I18NConstants.CLEAR_CHOOSER__LABEL.fill(getSelectionModel().getLabel()));
		
		if (clearButtonActive) {
			buttonWriter.writeButton(context, out);
		} else {
			buttonWriter.writeDisabledButton(context, out);
		}

		out.endTag(SPAN);
	}

	private LabelProvider getLabelProvider(SelectField selectField) {
		LabelProvider labels;
		if (optionRenderer instanceof ResourceRenderer) {
			labels = ((ResourceRenderer<?>) optionRenderer).getResourceProvider();
		} else {
			labels = selectField.getOptionLabelProvider();
		}
		return labels;
	}

	private Renderer<Object> getRenderer(SelectField selectField) {
		if (optionRenderer != null) {
			return optionRenderer;
		}
		
		return SelectFieldUtils.getOptionRenderer(selectField);
	}

	private boolean isClearButtonActive() {
		SelectField selectField = getSelectionModel();
		boolean isDisabled = selectField.isDisabled();
		boolean emptySelection = hasEmptySelection();
		boolean onlyFixedSelection = emptySelection ? false : hasOnlyFixedSelection();
		return (!isDisabled && !emptySelection && !onlyFixedSelection) || selectField.hasError();
	}

	private boolean isTextSelectionInputDisabled(SelectField theModel) {
		boolean isDisabled = theModel.isDisabled();
		return isDisabled || (theModel.isMultiple() && !SelectFieldUtils.isMultiSelectionTextInputEnabled(theModel));
	}

	private void writeOnClick(TagWriter out) throws IOException {
		out.beginAttribute(ONCLICK_ATTR);
		out.append("return ");
		getClearSelectionCommand().writeInvokeExpression(out, this);
		out.append(';');
		out.endAttribute();
	}

	@Override
	protected void writeControlAttributes(DisplayContext context, TagWriter out) throws IOException {
		super.writeControlAttributes(context, out);
		if (!getSelectionModel().isImmutable()) {
			out.writeAttribute(STYLE_ATTR, "white-space: nowrap;");
		}
	}

	private void writeOnBlur(TagWriter out) throws IOException {
		out.beginAttribute(ONBLUR_ATTR);
		writeOnBlurContent(out);
		out.endAttribute();
	}

	private void writeOnBlurContent(TagWriter out) throws IOException {
		out.append("return ");
		out.append(FORM_INPUT_CONSTANT);
		out.append(".handleOnBlur(");
		writeIdJsString(out);
		out.append(", ");
		writeInputIdJsString(out);
		if (showWait(SelectionControl.this)) {
			out.append(", true");
		}

		out.append(");");
	}

	private void writeOnChange(TagWriter out) throws IOException {
		out.beginAttribute(ONCHANGE_ATTR);
		writeOnChangeContent(out);
		out.endAttribute();
	}

	private void writeOnChangeContent(TagWriter out) throws IOException {
		out.append("return ");
		out.append(FORM_INPUT_CONSTANT);
		out.append(".handleOnChange(this, ");
		writeIdJsString(out);
		if (showWait(SelectionControl.this)) {
			out.append(", true");
		}
		out.append(");");
	}

	private void writeOnKeyDown(TagWriter out) throws IOException {
		out.beginAttribute(ONKEYDOWN_ATTR);
		writeKeyDownContent(out);
		out.endAttribute();
	}

	private void writeKeyDownContent(TagWriter out) throws IOException {
		out.append("return ");
		out.append(FORM_INPUT_CONSTANT);
		out.append(".handleOnKeyDown(this, ");
		writeIdJsString(out);
		out.append(", arguments[0]");
		out.append(", '");
		out.append(SelectFieldUtils.getMultiSelectionSeparator(getSelectionModel()));
		out.append("', '");
		out.append(SelectFieldUtils.getMultiSelectionSeparatorFormat(getSelectionModel()));
		out.append("', ");
		if (getSelectionModel().isMultiple()) {
			out.append("true");
		} else {
			out.append("false");
		}
		out.append(");");
	}

	private void writeOnFocus(DisplayContext context, TagWriter out) throws IOException {
		out.beginAttribute(ONFOCUS_ATTR);
		writeOnFocusContent(context, out);
		out.endAttribute();
	}

	private void writeOnFocusContent(DisplayContext context, TagWriter out) throws IOException {
		out.append(FORM_INPUT_CONSTANT);
		out.append(".image=");
		out.beginJsString();
		Icons.SLIDER_IMG.get().appendUrl(context, out);
		out.endJsString();
		out.append(";");
	}

    public static void writePopupButton(DisplayContext aContext, TagWriter anOut, Control aControl, boolean isDisabled, String anId, ControlCommand aCommand, String aLabel) throws IOException {
        /* 2. Render the dialog popup button. */

		ButtonWriter buttonWriter =
			new ButtonWriter(aControl, com.top_logic.layout.form.tag.Icons.OPEN_CHOOSER, aCommand);
		buttonWriter.setID(anId);
		buttonWriter.setCss(FormConstants.CHOOSE_BUTTON);
		buttonWriter.setTooltip(com.top_logic.layout.form.I18NConstants.OPEN_CHOOSER__LABEL.fill(aLabel));

		if (isDisabled) {
			buttonWriter.writeDisabledButton(aContext, anOut);
		} else {
			buttonWriter.writeButton(aContext, anOut);
		}
    }

	private static void writeOnClick(TagWriter out, Control aControl, ControlCommand aCommand) throws IOException {
		out.beginAttribute(ONCLICK_ATTR);
		out.append("return ");
		aCommand.writeInvokeExpression(out, aControl);
		out.append(';');
		out.endAttribute();
	}
    
	final ControlCommand getClearSelectionCommand() {
		return getCommand(ClearSelection.COMMAND);
	}

	final ControlCommand getOpenSelectorCommand() {
		return getCommand(OpenSelector.COMMAND);
	}

	private boolean hasEmptySelection() {
		SelectField theModel = getSelectionModel();
		if (theModel.hasValue()) {
			return theModel.isSelectionEmpty();
		} else {
			return true;
		}
	}
	
	private boolean hasOnlyFixedSelection() {
		SelectField theModel = getSelectionModel();
		
		Filter filter = theModel.getFixedOptions();
		if (filter != null) {
			if (theModel.hasValue()) {
				List selection = theModel.getSelection();
				for (Object object : selection) {
					if (!filter.accept(object)) {
						return false;
					}
				}
				return true;
			}
			else {
				return false;
				}
		}
		else {
			return false;
		}
	}
	
	public static final ResKey RES_KEY_MORE_ELEMENTS  = I18NConstants.COMPLETIOIN_MORE_ELEMENTS__DISPLAYED;
	public static final ResKey RES_KEY_NO_ELEMENTS    = I18NConstants.COMPLETION_NO_ELEMENTS;
	
	private class AutoCompletionFloater implements HTMLFragment{

		public static final String CLASS_ELEMENT          = "sifSearchElement";
		public static final String CLASS_ELEMENT_UNSELECTABLE = "sifSearchElementUnselectable";
		public static final String CLASS_ELEMENT_SELECTED = "sifSearchElementSelected";
		
		private List options;
		private String id;
		private LabelProvider provider;
		
		public AutoCompletionFloater(List someOptions, String anID, LabelProvider aProvider) {
			this.options = someOptions;
			this.id = anID;
			this.provider = aProvider;
        }
		
		@Override
		public void write(DisplayContext context, TagWriter out) throws IOException {
			int index = 0;
			if(this.options.size() == 0 ) {
				out.beginBeginTag(DIV);
				out.writeAttribute(ID_ATTR, id + "_more_elements");
				out.writeAttribute(CLASS_ATTR, CLASS_ELEMENT_UNSELECTABLE);
				writeNOOPClickAttribute(out);
				out.endBeginTag();
				out.writeText(Resources.getInstance().getString(RES_KEY_NO_ELEMENTS));
				out.endTag(DIV);
				return;
			}
			for(Iterator theIt = this.options.iterator(); theIt.hasNext();) {
				if(index >= AutoCompletion.MAX_DISPLAYED_ENTRIES) {
					out.beginBeginTag(DIV);
					out.writeAttribute(ID_ATTR, id + "_more_elements");
					out.writeAttribute(CLASS_ATTR, CLASS_ELEMENT_UNSELECTABLE);
					writeNOOPClickAttribute(out);
					out.endBeginTag();
					out.writeText(Resources.getInstance().getMessage(RES_KEY_MORE_ELEMENTS,
						Integer.valueOf(AutoCompletion.MAX_DISPLAYED_ENTRIES)));
					out.endTag(DIV);
					break;
				}
				Object theSel = theIt.next();
				String theLabel = this.provider.getLabel(theSel);
				String theDivID = id + "_" + index;
				out.beginTag(DIV);
				out.beginBeginTag(DIV);
				out.writeAttribute(ID_ATTR, theDivID);
				if(index == 0) {
					out.writeAttribute(CLASS_ATTR, CLASS_ELEMENT_SELECTED);
				}
				else {
					out.writeAttribute(CLASS_ATTR, CLASS_ELEMENT);
				}
				writeMouseOver(out, index);
				writeOnClickAttribute(out, index);
				out.endBeginTag();
				out.writeText(theLabel);
				out.endTag(DIV);
				out.endTag(DIV);
				index ++;
			}
        }

		private void writeOnClickAttribute(TagWriter out, int index) throws IOException {
			out.beginAttribute(ONCLICK_ATTR);
			appendOnClickContent(out, index);
			out.endAttribute();
		}

		private void writeNOOPClickAttribute(TagWriter out) throws IOException {
			out.beginAttribute(ONCLICK_ATTR);
			appendNOOPOnClickContent(out);
			out.endAttribute();
		}

		private void appendNOOPOnClickContent(TagWriter out) throws IOException {
			out.append("return ");
			out.append(FORM_INPUT_CONSTANT);
			out.append(".handleNOOPClick(");
			writeIdJsString(out);
			out.append(");");
		}

		private void appendOnClickContent(TagWriter out, int index) throws IOException {
			out.append("return ");
			out.append(FORM_INPUT_CONSTANT);
			out.append(".handleOnClick(");
			writeIdJsString(out);
			out.append(", arguments[0], ");
			out.writeInt(index);
			out.append(", ");
			writeInputIdJsString(out);
			out.append(", '");
			out.append(SelectFieldUtils.getMultiSelectionSeparator(getSelectionModel()));
			out.append("', '");
			out.append(SelectFieldUtils.getMultiSelectionSeparatorFormat(getSelectionModel()));
			out.append("', ");
			if (SelectionControl.this.getSelectionModel().isMultiple()) {
				out.append("true");
			} else {
				out.append("false");
			}
			out.append(");");
		}

		private void writeMouseOver(TagWriter out, int index) throws IOException {
			out.beginAttribute(ONMOUSEOVER_ATTR);
			writeMouseOverContent(out, index);
			out.endAttribute();
		}

		private void writeMouseOverContent(TagWriter out, int index) throws IOException {
			out.append("return ");
			out.append(FORM_INPUT_CONSTANT);
			out.append(".handleOnMouseOver(");
			out.writeJsString(getID());
			out.append(", arguments[0], ");
			out.writeInt(index);
			out.append(");");
		}
	}
	
	public static class AutoCompletion extends ControlCommand {
		
		public static final SelectionControl.AutoCompletion INSTANCE = new AutoCompletion();
		public static final String COMMAND                   = "autoCompletion";
		
		public static final String JS_CONST_MATCHING_ENTRIES = "services.form.SelectionControl.matchingEntries=";
		public static final String ENTERED_VALUE             = "enteredValue";
		public static final String ELEMENT_ID                = "elementID";
		public static final int    MAX_DISPLAYED_ENTRIES     = 7;
		
		public AutoCompletion() {
			super(COMMAND);
		}
		
		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			HandlerResult result = HandlerResult.DEFAULT_RESULT;
			String elementID = (String) arguments.get(ELEMENT_ID);
			
			String theVal = (String) arguments.get(ENTERED_VALUE);
			
			// get the underlying Formfield
			SelectionControl theControl = (SelectionControl) control;
			SelectField theField = (SelectField) theControl.getModel();
			LabelProvider theProvider = theField.getOptionLabelProvider();
			List matchingOptions = theField.getMatchingOptions(theVal, false);
			
			HTMLFragment theFragment = theControl.new AutoCompletionFloater(matchingOptions, elementID, theProvider);
			ContentReplacement contentReplacement = new ContentReplacement(elementID, theFragment);
			/* The floater of a SelectionControl is a Popup window. Therefore it is placed in the
			 * top level document of the browser window in which the control is displayed. */
			theControl.getScope().getFrameScope().getWindowScope().getTopLevelFrameScope()
				.addClientAction(contentReplacement);
			
			int size = matchingOptions.size() > MAX_DISPLAYED_ENTRIES ? MAX_DISPLAYED_ENTRIES : matchingOptions.size();
			theControl.addUpdate(new JSSnipplet(JS_CONST_MATCHING_ENTRIES + size + ";"));
			theControl.setSendOptions(matchingOptions);
			return result;
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.SELECTION_AUTO_COMPLETION;
		}
	}

	public static class ClearSelection extends ControlCommand {

		public static final String COMMAND = "clearSelection";
		public static final SelectionControl.ClearSelection INSTANCE = new ClearSelection();
		
		/**
		 * Singleton constructor.
		 */
		protected ClearSelection() {
			super(COMMAND);
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			SelectionControl selectionControl = (SelectionControl) control;
			final SelectField selectField = selectionControl.getSelectionModel();
			
			Filter fixedOptions = selectField.getFixedOptions();
			final Object newValue;
			 // No  fixed options, clear field completely 
			if (fixedOptions == null) {
				newValue = Collections.emptyList();
			} else {
				newValue = new ArrayList(getSelection(selectField));
				FilterUtil.filterInline(fixedOptions, (Collection) newValue);
			}

			try {
				FormFieldInternals.setValue(selectField, newValue);
			} catch (VetoException ex) {
				ex.setContinuationCommand(new Command() {

					@Override
					public HandlerResult executeCommand(DisplayContext context) {
						selectField.setValue(newValue);
						return HandlerResult.DEFAULT_RESULT;
					}
				});
				ex.process(selectionControl.getWindowScope());
			}
		
			if (ScriptingRecorder.isRecordingActive()) {
				ScriptingRecorder.recordFieldInput(selectField, newValue);
			}
			
            return HandlerResult.DEFAULT_RESULT;
		}

		private List<?> getSelection(SelectField targetSelectField) {
			Object storedValue = FormFieldInternals.getStoredValue(targetSelectField);
			if (storedValue != null) {
				return (List<?>) storedValue;
			} else {
				return Collections.emptyList();
			}
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.CLEAR_SELECTION;
		}
	}
	
	public static class OpenSelector extends ControlCommand {

		public static final String COMMAND = "openSelector";
		public static final SelectionControl.OpenSelector INSTANCE = new OpenSelector();
		
		/**
		 * Singleton constructor.
		 */
		protected OpenSelector() {
			super(COMMAND);
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			SelectField selectField = (SelectField) control.getModel();
			
			return openSelectorPopup(commandContext,
				((AbstractControlBase) control).getScope().getFrameScope().getWindowScope(), selectField);
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.OPEN_SELECTION_DIALOG;
		}
	}
	
	public static HandlerResult openSelectorPopup(DisplayContext commandContext, WindowScope windowScope, SelectField targetField) {
		AbstractSelectDialog selectDialog = targetField.getSelectDialogProvider().createSelectDialog(targetField);
		selectDialog.open(windowScope);
		return HandlerResult.DEFAULT_RESULT;
	}

	private String getOpenButtonID() {
		if (openButtonID == null) {
			openButtonID = getID() + "-open";
		}
		return this.openButtonID;
	}

	private String getClearButtonID() {
		if (clearButtonID == null) {
			clearButtonID = getID() + "-clear";
		}
		return this.clearButtonID;
	}

	private String getClearButtonSpanID() {
		return getClearButtonID() + "-span";
	}

	@Override
	public Bubble hasErrorChanged(FormField sender, Boolean oldError, Boolean newError) {
		if (!skipEvent(sender)) {
			if (hasClearButton) {
				addClearButtonUpdates();
			}
		}
		return super.hasErrorChanged(sender, oldError, newError);
	}

}