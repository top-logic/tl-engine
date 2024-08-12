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
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.base.services.simpleajax.JSFunctionCall;
import com.top_logic.base.services.simpleajax.PropertyUpdate;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.Utils;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DynamicText;
import com.top_logic.layout.basic.ConstantDisplayValue;
import com.top_logic.layout.basic.TemplateVariable;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.constraints.StringLengthConstraint;
import com.top_logic.layout.form.format.WikiWrite;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.tooltip.ToolTip;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Server-side {@link Control} implementation of text input fields. 
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TextInputControl extends AbstractFormFieldControl implements WithPlaceHolder {

	/**
	 * {@link TemplateVariable} for rendering additional buttons.
	 * 
	 * @see #writeButtons(DisplayContext, TagWriter)
	 */
	public static final String BUTTONS_PROPERTY = "buttons";

	/**
	 * Value for {@link #setColumns(int)} that prevents a <code>size</code> attribute from being
	 * written.
	 */
	public static final int NO_COLUMNS = 0;

	/**
	 * Value for {@link #setRows(int)} to use the client-side default value.
	 */
	public static final int NO_ROWS = 0;

	private boolean multiLine;

	/**
	 * Number of the input columns, <code>0</code> means no attribute set.
	 */
	private int columns = NO_COLUMNS;

	private int rows = NO_ROWS;

	private DynamicText onFocus;

	private DynamicText onInput;

    private String type;

    private int maxLengthShown = -1;

	private String _placeHolder;

	private int _maxLength;

	private int _minLength;

	/**
	 * Create a new {@link TextInputControl} with the default {@link #COMMANDS} map.
	 */
    public TextInputControl(FormField model) {
		this(model, COMMANDS);
    }
    
	/** Create a new {@link TextInputControl}. */
    protected TextInputControl(FormField model, Map aCommands) {
        super(model, aCommands);
		type = TEXT_TYPE_VALUE;
    }

	@Override
	public String getPlaceHolder() {
		return _placeHolder != null ? _placeHolder : getModelPlaceholder();
	}

	/**
	 * The placeholder value retrieved from the field model.
	 */
	private String getModelPlaceholder() {
		Object placeholder = getFieldModel().getPlaceholder();
		if (placeholder == null) {
			return null;
		}
		return formatPlaceholder(placeholder);
	}

	/**
	 * Converts the placeholder value from the field model to a string being displayed.
	 */
	protected String formatPlaceholder(Object placeholder) {
		FormField field = getFieldModel();
		if (field instanceof ComplexField complexField) {
			return complexField.getFormat().format(placeholder);
		} else {
			return (String) placeholder;
		}
	}

	@Override
	public void setPlaceHolder(String newValue) {
		// Note: Must be set unconditionally, since the placeholder override could be set to the
		// value provided by the field model.
		_placeHolder = newValue;

		String oldValue = getPlaceHolder();
		if (!Utils.equals(newValue, oldValue)) {
			internalHandlePlaceholderChanged(newValue);
		}
	}

	@Override
	protected void internalHandlePlaceholderChanged() {
		internalHandlePlaceholderChanged(getPlaceHolder());
	}

	/**
	 * Updates the UI with a new placeholder value.
	 */
	protected void internalHandlePlaceholderChanged(String newValue) {
		if (isAttached()) {
			addUpdate(
				new PropertyUpdate(getInputId(), HTMLConstants.PLACEHOLDER_ATTR,
					new ConstantDisplayValue(StringServices.nonNull(newValue))));
		}
	}

	/** @see #isMultiLine() */
    public void setMultiLine(boolean multiLine) {
        this.multiLine = multiLine;
        requestRepaint();
    }

	/** Whether multiple lines of text can be input with this control. */
    public boolean isMultiLine() {
        return multiLine;
    }

	/**
	 * The client-side field type.
	 * 
	 * @see HTMLConstants#TEXT_TYPE_VALUE
	 */
	@TemplateVariable("type")
	public final String getType() {
		return type;
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

	/** The number of columns to display. */
	@TemplateVariable("columns")
	public final int getColumns() {
        return this.columns;
    }
    
	/** Undo {@link #setColumns(int) setting the columns property}. */
    public void resetColumns() {
		setColumns(0);
    }
    
    /**
	 * The number of rows to display, or <code>0</code> for using the default value.
	 */
	@TemplateVariable("rows")
	public final int getRows() {
		return rows;
	}

	/**
	 * The number of rows that are displayed without scrolling the contents.
	 * 
	 * <p>
	 * The value of this property is only relevant, if {@link #isMultiLine()} is <code>true</code>.
	 * </p>
	 */
    public void setRows(int rows) {
        this.rows = rows;
        requestRepaint();
    }
    
    /**
     * Undo {@link #setRows(int) setting the row property}.
     */
    public void resetRows() {
		rows = NO_ROWS;
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

	@Override
	protected String getTypeCssClass() {
		return "cTextInput";
	}

	@Override
    protected void writeEditable(DisplayContext context, TagWriter out) throws IOException {
		updateMinMaxLength();

		if (multiLine) {
			Icons.TEXT_INPUT_EDIT_MULTI_TEMPLATE.get().write(context, out, this);
		} else {
			Icons.TEXT_INPUT_EDIT_SINGLE_TEMPLATE.get().write(context, out, this);
		}
    }

	private void updateMinMaxLength() {
		int maxLength = Integer.MAX_VALUE;
		int minLength = -1;
		List<Constraint> constraints = field().getConstraints();
		for (Constraint constraint : constraints) {
            if (constraint instanceof StringLengthConstraint) {
				StringLengthConstraint lengthConstraint = (StringLengthConstraint) constraint;
				if (lengthConstraint.hasMaxLength()) {
					maxLength = Math.min(maxLength, lengthConstraint.getMaxLength());
				}
				if (lengthConstraint.hasMinLength()) {
					minLength = Math.max(minLength, lengthConstraint.getMinLength());
				}
            }
        }
		if (maxLength == Integer.MAX_VALUE) {
			maxLength = -1;
		}

		_maxLength = maxLength;
		_minLength = minLength;
	}

	/**
	 * The client-side value of the <code>disabled</code> attribute.
	 */
	@TemplateVariable("disabled")
	public CharSequence disabledValue() {
		if (field().isDisabled()) {
			return DISABLED_DISABLED_VALUE;
		} else {
			return null;
		}
	}

	/**
	 * The client-side representation of the current value.
	 */
	@TemplateVariable("value")
	public String currentValue() {
		String currentValue = (String) field().getRawValue();
		if (isPasswordInput()) {
			currentValue = passwordReplacement(currentValue);
		}
		return currentValue;
	}

	private FormField field() {
		return (FormField) getModel();
	}

	/**
	 * The JavaScript function to invoke when the value of the field changes.
	 */
	@TemplateVariable("onchange")
	public void writeOnChangeContent(TagWriter out) throws IOException {
		writeHandleOnChangeAction(out, getJsClass(), this, null);
	}

	/** Returns the js object. */
	protected String getJsClass() {
		return FormConstants.TEXT_INPUT_CONTROL_CLASS;
	}

	/**
	 * Specifies a JavaScript command that is executed when the input field loses focus (onblur
	 * event).
	 */
	@TemplateVariable("onblur")
	public void writeOnBlurContent(TagWriter out) throws IOException {
		if (hasConstraint()) {
			out.append("return services.form.TextInputControl.handleOnBlur(this, ");
			out.writeInt(_maxLength);
			out.append(',');
			out.writeInt(_minLength);
			out.append(");");
		}
	}

	/**
	 * Creates a JavaScript command that is executed when a key is pressed in the input field.
	 */
	@TemplateVariable("oninput")
	public void writeOnInputContent(DisplayContext context, TagWriter out) throws IOException {
		boolean hasConstraint = hasConstraint();
		if (hasConstraint) {
			out.append("return services.form.TextInputControl.handleOnInput(this,'");
			out.append(getID());
			out.append("',");
			out.writeInt(_maxLength);
			out.append(',');
			out.writeInt(_minLength);
			if (showWait(this)) {
				out.append(",true");
			}
			out.append(')');
		}
		boolean hasHandler = this.onInput != null;
		if (hasHandler) {
			if (hasConstraint) {
				out.append(" && ");
			} else {
				out.append("return ");
			}
			this.onInput.append(context, out);
		}
		if (hasConstraint || hasHandler) {
			out.append(';');
		}
	}

	/**
	 * Specifies a JavaScript command that is executed when the input field receives focus.
	 */
	@TemplateVariable("onfocus")
	public void writeOnFocusContent(DisplayContext context, TagWriter out) throws IOException {
		boolean hasConstraint = hasConstraint();
		if (hasConstraint) {
			out.append("return services.form.TextInputControl.handleOnFocus(this, ");
			out.writeInt(_maxLength);
			out.append(',');
			out.writeInt(_minLength);
			out.append(')');
		}
		boolean hasHandler = this.onFocus != null;
		if (hasHandler) {
			if (hasConstraint) {
				out.append(" && ");
			} else {
				out.append("return ");
			}
			this.onFocus.append(context, out);
		}
		if (hasConstraint || hasHandler) {
			out.append(';');
		}
	}

	private boolean hasConstraint() {
		return _maxLength >= 0 || _minLength >= 0;
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
		if (multiLine) {
			Icons.TEXT_INPUT_IMMUTABLE_MULTI_TEMPLATE.get().write(context, out, this);
		} else {
			Icons.TEXT_INPUT_IMMUTABLE_SINGLE_TEMPLATE.get().write(context, out, this);
		}
    }

	/**
	 * Generates the tooltip contents (for immutable display).
	 * 
	 * <p>
	 * The value must be embedded into a {@value HTMLConstants#TL_TOOLTIP_ATTR} attribute.
	 * </p>
	 */
	@TemplateVariable("tooltip")
	public HTMLFragment getTooltipContents() {
		String value = (String) field().getRawValue();
		if (this.maxLengthShown > -1 && (value.length() >= this.maxLengthShown)) {
			return new ToolTip() {
				@Override
				public void writeContent(DisplayContext context, TagWriter out) throws IOException {
					out.writeText(value);
				}
			};
		}
		return null;
	}

	/**
	 * Generates the immutable value display.
	 */
	@TemplateVariable("immutableValue")
	public void writeImmutableValue(TagWriter out) throws IOException {
		String value = (String) field().getRawValue();
		if (this.maxLengthShown > -1 && (value.length() >= this.maxLengthShown)) {
			value = StringServices.minimizeString(value, maxLengthShown, maxLengthShown - 3);
		}
		if (multiLine) {
			writeMultiLineTextContent(out, value);
		} else {
			writeValueText(out, value);
		}
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

	private void writeValueText(TagWriter out, String value) {
		if (isPasswordInput()) {
			out.writeText(passwordReplacement(value));
			return;
		}
		out.writeText(value);
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

	/**
	 * Writes additional buttons.
	 * 
	 * <p>
	 * This property is only relevant for subclasses that provide additional functionality (like a
	 * calendar pop-up).
	 * </p>
	 * 
	 * @param context
	 *        The current rendering context
	 * @param out
	 *        The HTML output.
	 * @throws IOException
	 *         If writing fails.
	 */
	@TemplateVariable(BUTTONS_PROPERTY)
	public void writeButtons(DisplayContext context, TagWriter out) throws IOException {
		// Only relevant in sub-classes
	}
}
