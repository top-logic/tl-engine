/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.TemplateVariable;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.form.CollapsedListener;
import com.top_logic.layout.form.Collapsible;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.format.WikiWrite;
import com.top_logic.layout.form.model.ExpandableStringField;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;
import com.top_logic.util.Utils;
import com.top_logic.util.css.CssUtil;

/**
 * The TextInputEditControl extends {@link TextInputControl} by adding a single button to
 * toggle from a single and multiline version of the input field.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ExpandableTextInputControl extends TextInputControl implements CollapsedListener {

    // private static final String EDITOR_POPUP = "SimpleEditorPopup";

    public static final Map COMMANDS = createCommandMap(TextInputControl.COMMANDS, new ControlCommand[] { ToggleFieldMode.INSTANCE });

	private static final ResKey TOGGLE_BUTTON_TOOLTIP = I18NConstants.TOGGLE_EXPAND_TOOLTIP;

    private int singleLineColumns;
    private int multiLineColumns;
    private int multiLineRows;
    private String openButtonID;
    private int startCutPosition;

    /**
	 * Creates a {@link ExpandableTextInputControl}.
	 */
    public ExpandableTextInputControl(FormField aModel) {
    	super(aModel, COMMANDS);
    	this.setSingleLineColumns(20);
    	this.setMultiLineColumns(60);
    	this.setMultiLineRows(5);
    	this.setStartCutPosition(-1);
    	this.setMultiLine(!((ExpandableStringField) aModel).isCollapsed());
    }
    
    /**
	 * Creates a {@link ExpandableTextInputControl}.
	 */
    public ExpandableTextInputControl(FormField aModel, int aMaxLengthShown) {
    	this(aModel);
    	this.setMaxLengthShown(aMaxLengthShown);
    }
    
	@Override
	protected void registerListener(FormMember member) {
		super.registerListener(member);
		member.addListener(Collapsible.COLLAPSED_PROPERTY, this);
	}

	@Override
	protected void deregisterListener(FormMember member) {
		member.removeListener(Collapsible.COLLAPSED_PROPERTY, this);
		super.deregisterListener(member);
	}
    
    protected String getOpenButtonID() {
    	if (this.openButtonID != null) {
    		this.openButtonID = getID() + "-open";
    	}
    	return openButtonID;
    }

	@Override
	protected String getTypeCssClass() {
		return "cExpandableTextInput";
	}

	@Override
	protected void writeEditable(DisplayContext context, TagWriter out) throws IOException {
		Icons.TEXT_INPUT_WITH_BUTTONS_EDIT_TEMPLATE.get().write(context, out, this);
	}
    
	@TemplateVariable("buttons")
	public void writeToggleModeButton(DisplayContext context, TagWriter out) throws IOException {
		String anId = getOpenButtonID();
		ControlCommand aCommand = getToggleFieldModeCommand();

		out.beginBeginTag(SPAN);
		out.writeAttribute(CLASS_ATTR, FormConstants.FIXED_RIGHT_CSS_CLASS);
		out.endBeginTag();
        
        /* 2. Render the dialog popup button. */
		String buttonTooltip = Resources.getInstance().getString(TOGGLE_BUTTON_TOOLTIP);
		
		XMLTag tag = getButton();		
		tag.beginBeginTag(context, out);
		out.writeAttribute(ID_ATTR, anId);
        
        if (buttonTooltip != null)
			OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, buttonTooltip);
        
		CssUtil.writeCombinedCssClasses(out,
			FormConstants.INPUT_IMAGE_ACTION_CSS_CLASS,
			FormConstants.TOGGLE_BUTTON_CSS_CLASS);
		writeOnClick(out, aCommand);

		tag.endEmptyTag(context, out);

		out.endTag(SPAN);
    }

	private XMLTag getButton() {
		if (((ExpandableStringField) getModel()).isCollapsed()) {
			return Icons.EDIT_MAXIMIZE.toButton();
		} else {
			return Icons.EDIT_MINIMIZE.toButton();
		}
	}

	private void writeOnClick(TagWriter out, ControlCommand aCommand) throws IOException {
		out.beginAttribute(ONCLICK_ATTR);
		out.append("return ");
		aCommand.writeInvokeExpression(out, this);
		out.append(';');
		out.endAttribute();
	}

    @Override
	protected void writeImmutable(DisplayContext context, TagWriter out) throws IOException {
        ExpandableStringField field   = (ExpandableStringField) getModel();
        String                value   = field.getRawString();
        boolean               isExpanded = !field.isCollapsed();

		out.beginBeginTag((isExpanded ? DIV : SPAN));
    
        // Functional attributes
		writeControlAttributes(context, out);
		out.writeAttribute(STYLE_ATTR, getInputStyle());
    
        if (!isExpanded && (this.getMaxLengthShown() > -1)) {
			String encoded = TagUtil.encodeXML(value);
			OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, encoded);
            value = StringServices.minimizeString(value, this.getMaxLengthShown(), getStartCutPosition());
        }

		out.endBeginTag();
        {
            if (isExpanded) {
                // Wikify the output to prevent loosing newlines.
				WikiWrite.wikiWrite(out, value);
            }
            else {
				out.writeText(value);
            }
        }
		out.endTag((isExpanded ? DIV : SPAN));
    }

    public final ControlCommand getToggleFieldModeCommand() {
        return getCommand(ToggleFieldMode.COMMAND);
    }

    /**
     * This method returns the maxLengthView.
     * 
     * @return Returns the maxLengthView.
     * 
     * @deprecated use {@link #getMaxLengthShown()} directly.
     */
    @Deprecated
	public int getMaxLengthView() {
        return this.getMaxLengthShown();
    }

    public int getSingleLineColumns() {
        return this.singleLineColumns;
    }

    public void setSingleLineColumns(int aSingleLineColumns) {
        this.singleLineColumns = aSingleLineColumns;
        
        this.internalUpdate();
    }

    public int getMultiLineColumns() {
        return (this.multiLineColumns);
    }

    public void setMultiLineColumns(int aMultiLineColumns) {
        this.multiLineColumns = aMultiLineColumns;
        
        this.internalUpdate();
    }

    public int getMultiLineRows() {
        return (this.multiLineRows);
    }

    public void setMultiLineRows(int aMultiLineRows) {
        this.multiLineRows = aMultiLineRows;
        
        this.internalUpdate();
    }

    private void internalUpdate() {
        if (this.isMultiLine()) {
            this.setColumns(this.getMultiLineColumns());
            this.setRows(this.getMultiLineRows());
        }
        else {
            this.setColumns(this.getSingleLineColumns());
            this.setRows(1);
        }
    }
    
    @Override
    public void setMultiLine(boolean aMultiLine) {
        super.setMultiLine(aMultiLine);
        
        internalUpdate();
    }

    public void setStartCutPosition(int startCutPosition) {
        this.startCutPosition = startCutPosition;
        
        internalUpdate();
    }

    public int getStartCutPosition() {
        return (startCutPosition);
    }

	@Override
	public Bubble handleCollapsed(Collapsible collapsible, Boolean oldValue, Boolean newValue) {
		if (!skipEvent(collapsible)) {
			boolean isExpanded = Utils.isFalse(newValue);
			setMultiLine(isExpanded);
		}
		return Bubble.BUBBLE;
	}

    /**
     * @author    <a href=mailto:mga@top-logic.com>Michael Gänsler</a>
     */
    public static class ToggleFieldMode extends ControlCommand {
    
        public static final String COMMAND = "toggleFieldMode";
        public static final ToggleFieldMode INSTANCE = new ToggleFieldMode();
    
        /**
         * Singleton constructor.
         */
        protected ToggleFieldMode() {
            super(COMMAND);
        }
    
        @Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
            ExpandableTextInputControl theControl = (ExpandableTextInputControl) control;
            
            ExpandableStringField      theField   = (ExpandableStringField) theControl.getModel();
            theField.toggleExpandedState();
            
            return HandlerResult.DEFAULT_RESULT;
        }

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.TOGGLE_TEXT_FIELD_MODE;
		}
    }
}
