/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.top_logic.base.security.util.Password;
import com.top_logic.base.services.simpleajax.JSFunctionCall;
import com.top_logic.base.services.simpleajax.PropertyUpdate;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DynamicText;
import com.top_logic.layout.basic.ConstantDisplayValue;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.constraints.StringLengthConstraint;
import com.top_logic.layout.form.format.WikiWrite;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.mig.html.UserAgent;

/**
 * Server-side {@link Control} implementation of text input fields. 
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TextInputControl extends AbstractFormFieldControl implements WithPlaceHolder {

	/**
	 * Value for {@link #setColumns(int)} that prevents a <code>size</code> attribute from being
	 * written.
	 */
	public static final int NO_COLUMNS = 0;

	private boolean multiLine;

	/**
	 * Number of the input columns, <code>0</code> means no attribute set.
	 */
	private int columns = NO_COLUMNS;

    private boolean hasRows;
    private int rows;

	private DynamicText onFocus;

	private DynamicText onInput;

    private boolean unsafeHTML;
    
    private String type;

    private int maxLengthShown = -1;

	private String _placeHolder;

    public TextInputControl(FormField model) {
		this(model, COMMANDS);
    }
    
    protected TextInputControl(FormField model, Map aCommands) {
        super(model, aCommands);
		type = TEXT_TYPE_VALUE;
    }

	@Override
	public String getPlaceHolder() {
		return _placeHolder;
	}

	@Override
	public void setPlaceHolder(String placeHolder) {
		_placeHolder = placeHolder;
	}

    /**
     * @see #isMultiLine()
     */
    public void setMultiLine(boolean multiLine) {
        this.multiLine = multiLine;
        requestRepaint();
    }

    /**
     * Whether multiple lines of text can be input with this control.
     */
    public boolean isMultiLine() {
        return multiLine;
    }

    
	/**
	 * Sets the type of a text input field.
	 * 
	 * @param aType
	 *        The type of the field (e.g.x {@link HTMLConstants#TEXT_TYPE_VALUE}, or
	 *        {@link HTMLConstants#PASSWORD_TYPE_VALUE}).
	 */
    public void setType(String aType) {
		if (aType != null) {
			type = aType;
		} else {
			type = TEXT_TYPE_VALUE;
		}
    }
    
    /**
	 * The number of text columns that are displayed without scrolling the contents.
	 * 
	 * <p>
	 * A value of {@link #NO_COLUMNS} means not to use a <code>size</code> attribute.
	 * </p>
	 */
    public void setColumns(int columns) {
        this.columns = columns;
        requestRepaint();
    }

    protected int getColumns() {
        return this.columns;
    }
    
    /**
     * Undo {@link #setColumns(int) setting the columns property}.
     */
    public void resetColumns() {
		setColumns(0);
    }
    
    /**
     * The number of rows that are displayed without scrolling the contents.
     * 
     * <p>
     * The value of this property is only relevant, if {@link #isMultiLine()} is
     * <code>true</code>.
     * </p>
     */
    public void setRows(int rows) {
        this.hasRows = true;
        this.rows = rows;
        requestRepaint();
    }
    
    /**
     * Undo {@link #setRows(int) setting the row property}.
     */
    public void resetRows() {
        hasRows = false;
        requestRepaint();
    }
    
    /**
     * The {@link HTMLConstants#ONFOCUS_ATTR} of this control's input element.
     */
	public void setOnFocus(DynamicText onFocus) {
        this.onFocus = onFocus;
        requestRepaint();
    }

    /**
	 * The {@link HTMLConstants#ONINPUT_ATTR} of this control's input element.
	 */
	public void setOnInput(DynamicText onKeyUp) {
        this.onInput = onKeyUp;
        requestRepaint();
    }

    /**
     * Requests to output the input text as unquoted HTML fragment.
     * 
     * @deprecated This opens a cross site scripting vulnerability in the
     *             application and should not/must not be used. This property is
     *             currently only used for displaying FCKEditor output. Whether
     *             this is safe is not yet clear.
     */
    @Deprecated
	public void setUnsafeHTML(boolean unsafeHTML) {
        this.unsafeHTML = unsafeHTML;
        requestRepaint();
    }
    
	@Override
	protected String getTypeCssClass() {
		return "cTextInput";
	}

	@Override
    protected void writeEditable(DisplayContext context, TagWriter out) throws IOException {
        out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
        // In this case getInputStyle will apply so we can ignore the normal getStyle()
        // out.writeAttribute(STYLE_ATTR, getStyle());
        out.endBeginTag();
        {
            writeEditableContents(context, out);
        }
        out.endTag(SPAN);
    }

    protected void writeEditableContents(DisplayContext context, TagWriter out) throws IOException {
		FormField field = (FormField) getModel();
        
		out.beginBeginTag(SPAN);
		out.writeAttribute(CLASS_ATTR, FormConstants.FLEXIBLE_CSS_CLASS);
		out.endBeginTag();

        if (multiLine) {
            out.beginBeginTag(TEXTAREA);
        } else {
            out.beginBeginTag(INPUT);
        }
        
        // Functional attributes
		writeInputIdAttr(out);
		writeQualifiedNameAttribute(out);
        out.writeAttribute(CLASS_ATTR, FormConstants.IS_INPUT_CSS_CLASS);
		writeOnChange(out);
        out.writeAttribute(STYLE_ATTR, getInputStyle());
		out.writeAttribute(PLACEHOLDER_ATTR, _placeHolder);
        
        // Switch off autocompletion offered by browser per default. The reason
        // is that this will not fire any event so that the application is not
        // aware of any changes and will therfore ignore the entered values.
        out.writeAttribute( "autocomplete", "off" );
        
        if (hasTabIndex()) {
            out.writeAttribute(TABINDEX_ATTR, getTabIndex());
        }

		// Compute the maximal and minimal length of the most restrictive string length constraint.
        int minMaxLength = Integer.MAX_VALUE;
		int maxMinLength = Integer.MIN_VALUE;
		List<Constraint> constraints = field.getConstraints();
		for (Constraint constraint : constraints) {
            if (constraint instanceof StringLengthConstraint) {
				StringLengthConstraint lengthConstraint = (StringLengthConstraint) constraint;
				if (lengthConstraint.hasMaxLength()) {
					minMaxLength = Math.min(minMaxLength, lengthConstraint.getMaxLength());
				}
				if (lengthConstraint.hasMinLength()) {
					maxMinLength = Math.max(maxMinLength, lengthConstraint.getMinLength());
				}
            }
        }
        
		if (minMaxLength < Integer.MAX_VALUE || maxMinLength > Integer.MIN_VALUE) {
			int javascriptMinLength;
			if (maxMinLength == Integer.MIN_VALUE) {
				javascriptMinLength = -1;
			} else {
				javascriptMinLength = maxMinLength;
			}
			int javascriptMaxLength;
			if (minMaxLength == Integer.MAX_VALUE) {
				javascriptMaxLength = -1;
			} else {
				javascriptMaxLength = minMaxLength;
			}
			writeOnFocus(context, out, javascriptMinLength, javascriptMaxLength);
			writeOnInput(context, out, javascriptMinLength, javascriptMaxLength);
			writeOnBlur(out, javascriptMinLength, javascriptMaxLength);
        } else {
        	if (this.onFocus != null) {
				HTMLUtil.writeAttribute(context, out, ONFOCUS_ATTR, this.onFocus);
        	}
        	if (this.onInput != null) {
				if (UserAgent.is_ie9down(context.getUserAgent())) {
					HTMLUtil.writeAttribute(context, out, ONKEYUP_ATTR, this.onInput);
				} else {
					HTMLUtil.writeAttribute(context, out, ONINPUT_ATTR, this.onInput);
				}
            }
        }

		if (field.isDisabled()) {
            out.writeAttribute(DISABLED_ATTR, DISABLED_DISABLED_VALUE);
        }

		String currentValue = (String) field.getRawValue();
		if (isPasswordInput()) {
			currentValue = passwordReplacement(currentValue);
		}

		if (multiLine) {
			if (columns > NO_COLUMNS) {
                out.writeAttribute(COLS_ATTR, columns);
            }
            if (hasRows) {
                out.writeAttribute(ROWS_ATTR, rows);
            }
            out.endBeginTag();
            out.writeText(currentValue);
            out.endTag(TEXTAREA);
        } else {
			out.writeAttribute(TYPE_ATTR, type);

            // Content attributes
            out.writeAttribute(VALUE_ATTR, currentValue);

            // Visual attributes.
			if (columns > NO_COLUMNS) {
                out.writeAttribute(SIZE_ATTR, columns);
            }
            out.endEmptyTag();
        }
		out.endTag(SPAN);
    }

	private void writeOnChange(TagWriter out) throws IOException {
		out.beginAttribute(ONCHANGE_ATTR);
		writeHandleOnChangeAction(out, getJsClass(), this, null);
		out.endAttribute();
	}

	protected String getJsClass() {
		return FormConstants.TEXT_INPUT_CONTROL_CLASS;
	}

	private void writeOnBlur(TagWriter out, int javascriptMinLength, int javascriptMaxLength) throws IOException {
		out.beginAttribute(ONBLUR_ATTR);
		writeOnBlurContent(out, javascriptMaxLength, javascriptMinLength);
		out.endAttribute();
	}

	/**
	 * Create JavaScript command to execute on blur event in input field
	 * 
	 * @param maxLength
	 *        the maximal allowed length or -1 for no restriction.
	 * @param minLength
	 *        the minimal allowed length or -1 for no restriction.
	 */
	private void writeOnBlurContent(TagWriter out, int maxLength, int minLength) throws IOException {
		out.append("return services.form.TextInputControl.handleOnBlur(this, ");
		out.writeInt(maxLength);
		out.append(',');
		out.writeInt(minLength);
		out.append(");");
	}

	private void writeOnInput(DisplayContext context, TagWriter out, int javascriptMinLength, int javascriptMaxLength)
			throws IOException {
		if (UserAgent.is_ie9down(context.getUserAgent())) {
			out.beginAttribute(ONKEYUP_ATTR);
		} else {
			out.beginAttribute(ONINPUT_ATTR);
		}
		writeOnInputContent(context, out, javascriptMaxLength, javascriptMinLength);
		out.endAttribute();
	}

	/**
	 * Create JavaScript command to execute on key up event in input field
	 * 
	 * @param maxLength
	 *        the maximal allowed length or -1 for no restriction.
	 * @param minLength
	 *        the minimal allowed length or -1 for no restriction.
	 */
	private void writeOnInputContent(DisplayContext context, TagWriter out, int maxLength, int minLength)
			throws IOException {
		out.append("return services.form.TextInputControl.handleOnInput(this,'");
		out.append(getID());
		out.append("',");
		out.writeInt(maxLength);
		out.append(',');
		out.writeInt(minLength);
		if (showWait(this)) {
			out.append(",true");
		}
		out.append(')');
		if (this.onInput != null) {
			out.append(" && ");
			this.onInput.append(context, out);
		}
		out.append(';');
	}

	private void writeOnFocus(DisplayContext context, TagWriter out, int javascriptMinLength, int javascriptMaxLength)
			throws IOException {
		out.beginAttribute(ONFOCUS_ATTR);
		writeOnFocusContent(context, out, javascriptMaxLength, javascriptMinLength);
		out.endAttribute();
	}

	/**
	 * Create JavaScript command to execute on focus event in input field
	 * 
	 * @param maxLength
	 *        the maximal allowed length or -1 for no restriction.
	 * @param minLength
	 *        the minimal allowed length or -1 for no restriction.
	 */
	private void writeOnFocusContent(DisplayContext context, TagWriter out, int maxLength, int minLength)
			throws IOException {
		out.append("return services.form.TextInputControl.handleOnFocus(this, ");
		out.writeInt(maxLength);
		out.append(',');
		out.writeInt(minLength);
		out.append(')');
		if (this.onFocus != null) {
			out.append(" && ");
			this.onFocus.append(context, out);
		}
		out.append(';');
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
		String value = (String) ((FormField) getModel()).getRawValue();

        out.beginBeginTag((multiLine ? DIV : SPAN));
        
        // Functional attributes
		writeControlAttributes(context, out);
		writeStyle(out);

        if (this.maxLengthShown > -1 && (value.length() >= this.maxLengthShown)) {
			String encoded = TagUtil.encodeXML(value);
			OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, encoded);
            value = StringServices.minimizeString(value, maxLengthShown, maxLengthShown-3);
        }
        out.endBeginTag();
        {
            // Content
            if (multiLine) {
				writeMultiLineTextContent(out, value);
            } else {
                writeValueText(out, value);
            }
        }
        out.endTag((multiLine ? DIV : SPAN));
    }

	/**
	 * Writer the given multi-line value to the given {@link TagWriter}.
	 * 
	 * @param out
	 *        The {@link TagWriter} to write to.
	 * @param value
	 *        The text to write.
	 * @throws IOException
	 *         When writing to {@link TagWriter} failed.
	 */
	protected void writeMultiLineTextContent(TagWriter out, String value) throws IOException {
		// Wikify the output to prevent loosing newlines.
		WikiWrite.wikiWrite(out, value);
	}

    private void writeValueText(TagWriter out, String value) throws IOException {
		if (isPasswordInput()) {
			out.writeText(passwordReplacement(value));
			return;
		}
        if (unsafeHTML) {
            out.writeContent(value);
        } else {
            out.writeText(value);
        }
    }

	private boolean isPasswordInput() {
		return HTMLConstants.PASSWORD_TYPE_VALUE.equals(type);
	}

	private String passwordReplacement(String currentValue) {
		return StringServices.isEmpty(currentValue) ? "" : Password.PASSWORD_REPLACEMENT;
	}

	@Override
	protected void internalHandleValueChanged(FormField field, Object oldValue, Object newValue) {
        FormField fieldModel = getFieldModel();
        if (getModel().isImmutable()) {
            requestRepaint();
        } else if (fieldModel.isVisible()) {
			if (isMultiLine()) {
				requestRepaint();
			} else {
				DynamicText newDisplayValue = ConstantDisplayValue.valueOf((String) newValue);
				PropertyUpdate updateEvent =
					new PropertyUpdate(getInputId(), HTMLConstants.VALUE_ATTR, newDisplayValue);
				addUpdate(updateEvent);
			}
        }
    }
    
	@Override
	public void internalHandleDisabledEvent(FormMember sender, Boolean oldValue, Boolean newValue) {
		if (!sender.isImmutable()) {
			addDisabledUpdate(newValue.booleanValue());
		}
	}
    
    /**
     * Returns the maximum number of characters shown, when the field is
     * immutable. If the field value is longer than this number it will be cut
     * and "..." is appended. In addition a tooltip with the whole value is
     * added.
     */
    public int getMaxLengthShown() {
        return (maxLengthShown);
    }

    /**
     * Sets the maximum number of characters shown when the field is immutable.
     * Per default the value is reduced to <code>maxLengthShown - 3</code> and
     * then "..." is appended.
     * 
     * @param maxLengthShown
     *            the maximum length visible in immutable state.
     */
    public void setMaxLengthShown(int maxLengthShown) {
        this.maxLengthShown = maxLengthShown;
        requestRepaint();
    }
}
