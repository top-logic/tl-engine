/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.ElementReplacement;
import com.top_logic.base.services.simpleajax.PropertyUpdate;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DynamicText;
import com.top_logic.layout.basic.ConstantDisplayValue;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.constraints.RangeConstraint;
import com.top_logic.layout.form.tag.Icons;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.mig.html.HTMLConstants;

/**
 * The IntegerInputControl is a server-side {@link Control} implementation of
 * input fields for integers providing support for range constraints and buttons
 * for incrementing and decrementing the value of the field.
 * 
 * @author    <a href="mailto:TEH@top-logic.com">Tobias Ehrler</a>
 */
public class IntegerInputControl extends AbstractFormFieldControl {

	/**
	 * {@link ControlProvider} creating {@link IntegerInputControl}s.
	 */
	public static final class Provider implements ControlProvider {

		/**
		 * Singleton {@link IntegerInputControl.Provider} instance.
		 */
		public static final Provider INSTANCE = new Provider();

		private Provider() {
			// Singleton constructor.
		}

		@Override
		public Control createControl(Object model, String style) {
			if (model instanceof FormField) {
				return new IntegerInputControl((FormField) model);
			} else {
				return null;
			}
		}

	}
    
	private final TextInputControl textInput;
    private String incButtonId;
    private String decButtonId;
    private Comparable min;
    private Comparable max;

    /** 
     * Creates a {@link IntegerInputControl}.
     * 
     * @param aModel The model object behind this control.
     */
    public IntegerInputControl(FormField aModel) {
		super(aModel);
        textInput = new TextInputControl(aModel);
        textInput.setMultiLine(false);
        textInput.setRows(1);
    }

    @Override
	public void valueChanged(FormField field, Object oldValue, Object newValue) {
		super.valueChanged(field, oldValue, newValue);
		if (field.isVisible()) {
			requestRepaint();
    	}
    }

	@Override
	public void internalHandleDisabledEvent(FormMember sender, Boolean oldValue, Boolean newValue) {
		if (!sender.isImmutable()) {
			updateDisabledState(newValue.booleanValue());
		}
	}

	/**
	 * Updates the disabled state of the increment and decrement buttons, and updates the images of
	 * these buttons. The disabled state of the input field must not be updated as this is done by
	 * {@link #textInput} itself.
	 * 
	 * @param disabled
	 *        whether the buttons must be disabled or not.
	 */
	private void updateDisabledState(boolean disabled) {
		addDisabledUpdate(disabled);
		/* It is not necessary to update the disabled state of the text field since this is done by
		 * the TextInputControl */
		addUpdate(new ElementReplacement(buttonsId(), this::writeButtons));
	}

	@Override
	protected String getTypeCssClass() {
		return "cIntegerInput";
	}

    @Override
	protected void writeEditable(DisplayContext context, TagWriter out)
            throws IOException {
		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
        // In this case getInputStyle will apply so we can ignore the normal getStyle()
        // out.writeAttribute(STYLE_ATTR, getStyle());
		out.endBeginTag();
        {
			textInput.write(context, out);
			writeButtons(context, out);
        }
		out.endTag(SPAN);
    }

    @Override
	protected void writeImmutable(DisplayContext context, TagWriter out)
            throws IOException {
		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
        // In this case getInputStyle will apply so we can ignore the normal getStyle()
        // out.writeAttribute(STYLE_ATTR, getStyle());
		out.endBeginTag();
        {
			textInput.write(context, out);
        }
		out.endTag(SPAN);
    }

	@Override
	protected void internalHandleValueChanged(FormField field, Object oldValue, Object newValue) {
		if (field.isImmutable()) {
            requestRepaint();
		} else if (field.isVisible()) {
            addUpdate( new PropertyUpdate(
                    getInputId(),
                    HTMLConstants.VALUE_ATTR,
				new ConstantDisplayValue((String) newValue)));
        }
    }
    
	private String buttonsId(){
		return getID() + "_buttons";
    }

    /** 
     * This method writes out buttons appropriately enabled or disabled
     * according to the value of the field.
     */
	private void writeButtons(DisplayContext aContext, TagWriter anOut) throws IOException {
		FormField field = getFieldModel();
        // determine minimum and maximum values as applicable
		setMinMax(field);
		boolean isDisabled = field.isDisabled();
		Object theObj = field.hasValue() ? field.getValue() : null;
        Comparable theValue = null;
        if (theObj != null && isApplicableType(theObj)) {
            theValue = (Comparable)theObj;
        }
        // span to position the plus/minus buttons
        anOut.beginBeginTag(SPAN);
		anOut.writeAttribute(ID_ATTR, buttonsId());
		anOut.writeAttribute(CLASS_ATTR, FormConstants.FIXED_RIGHT_CSS_CLASS);
        anOut.writeAttribute(STYLE_ATTR, "position: relative;");
        anOut.endBeginTag();
		{
			writeIncrementButton(aContext, anOut, isDisabled, theValue);

			writeDecrementButton(aContext, anOut, isDisabled, theValue);
		}

		anOut.endTag(SPAN);
	}

	private void writeDecrementButton(DisplayContext context, TagWriter out, boolean disabled, Comparable value)
			throws IOException {
		boolean disabledState = disabled || isLessThanMin(value);

		ThemeImage enabledImagePath = Icons.DECREMENT_BUTTON_IMAGE;
		ThemeImage disabledImagePath = Icons.DECREMENT_BUTTON_DISABLED_IMAGE;
		XMLTag tag = getButtonIcon(disabledState, enabledImagePath, disabledImagePath).toButton();

		// Render the decrement button
		tag.beginBeginTag(context, out);
		out.beginCssClasses();
		out.append(FormConstants.INPUT_IMAGE_ACTION_CSS_CLASS);
		out.append(FormConstants.BUTTON_MINUS_CSS_CLASS);
		if (disabledState) {
			out.append(FormConstants.DISABLED_CSS_CLASS);
		}
		out.endCssClasses();

		writeButtonState(out, disabledState);

		if (!disabledState) {
			writeOnClickAttribute(context, out, true);
		}

		tag.endEmptyTag(context, out);
	}

	private void writeIncrementButton(DisplayContext context, TagWriter out, boolean disabled, Comparable value)
			throws IOException {
		boolean disabledState = disabled || isGreaterThanMax(value);

		ThemeImage enabledImagePath = Icons.INCREMENT_BUTTON_IMAGE;
		ThemeImage disabledImagePath = Icons.INCREMENT_BUTTON_DISABLED_IMAGE;
		XMLTag tag = getButtonIcon(disabledState, enabledImagePath, disabledImagePath).toButton();

		// Render the increment button
		tag.beginBeginTag(context, out);
		out.beginCssClasses();
		out.append(FormConstants.INPUT_IMAGE_ACTION_CSS_CLASS);
		out.append(FormConstants.BUTTON_PLUS_CSS_CLASS);
		if (disabledState) {
			out.append(FormConstants.DISABLED_CSS_CLASS);
		}
		out.endCssClasses();

		writeButtonState(out, disabledState);

		writeOnClickAttribute(context, out, false);
		tag.endEmptyTag(context, out);
	}

	private void writeOnClickAttribute(DisplayContext context, TagWriter out, boolean decrement) throws IOException {
		out.beginAttribute(ONCLICK_ATTR);
		writeOnClickContent(context, out, decrement);
		out.endAttribute();
	}

	private void writeOnClickContent(DisplayContext context, TagWriter out, boolean decrement) throws IOException {
		final String cmd = decrement ? "handleDecrement" : "handleIncrement";

		out.append("return ");
		out.append(FormConstants.INTEGER_INPUT_CONTROL_CLASS);
		out.append('.');
		out.append(cmd);
		out.append("(");
		writeIdJsString(out);
		out.append(",");
		textInput.writeInputIdJsString(out);
		out.append(", ");
		out.writeJsLiteral(min);
		out.append(", ");
		out.writeJsLiteral(max);
		if (showWait(this)) {
			out.append(",true");
		}
		out.append(");");
	}

    /** 
     * This method gets the ID of the increment button input.
     * 
     * @return Returns the ID of the increment button input.
     */
    private String getIncButtonId() {
        if (incButtonId == null) {
            incButtonId = getID() + "-inc";
        }
        return this.incButtonId;
    }
    
    /** 
     * This method gets the ID of the decrement button input.
     * 
     * @return Returns the ID of the decrement button input.
     */
    private String getDecButtonId() {
        if (decButtonId == null) {
            decButtonId = getID() + "-dec";
        }
        return this.decButtonId;
    }
    
	/**
	 * This method writes the disabled attribute and an image attribute of a tag to the given
	 * writer. It is a utility method for use with the plus/minus buttons of this control.
	 */
	private void writeButtonState(TagWriter out, boolean disabled) {
		if (disabled) {
			out.writeAttribute(DISABLED_ATTR, DISABLED_DISABLED_VALUE);
		}
	}

	private ThemeImage getButtonIcon(boolean disabled, ThemeImage enabledImage, ThemeImage disabledImage) {
		return disabled ? disabledImage : enabledImage;
	}
    
    /** 
     * This method checks whether the value held in the {@link FormField} is of a type
     * that this control can process.
     * At the moment these types are Integer and Long.
     * 
     * @param anObject The value to check.
     */
    private static boolean isApplicableType(Object anObject) {
        return anObject instanceof Integer || anObject instanceof Long;
    }
    
    /** 
     * This method checks if the given value is less than the configured minimum.
     * 
     * @param aValue The value to check.
     * @return Returns true if the value is less than the minimum or, if the value
     * is empty, i.e. none has been entered, if the minimum is greater than the
     * implicit default value zero.
     */
    private boolean isLessThanMin(Comparable aValue) {
        boolean theResult = false;
        if (min != null) {
            if (aValue != null) {
                theResult = aValue.compareTo(min) <= 0;
            }
            else {
                theResult = Integer.valueOf(0).compareTo((Integer)min) <= 0;
            }
        }
        return theResult;
    }
    
    /** 
     * This method checks if the given value is less than the configured maximum.
     * 
     * @param aValue The value to check.
     * @return Returns true if the value is greater than the maximum or, if the value
     * is empty, i.e. none has been entered, if the maximum is less than the
     * implicit default value zero.
     */
    private boolean isGreaterThanMax(Comparable aValue) {
        boolean theResult = false;
        if (max != null) {
            if (aValue != null) {
                theResult = aValue.compareTo(max) >= 0;
            }
            else {
                theResult = Integer.valueOf(0).compareTo((Integer)max) >= 0;
            }
        }
        return theResult;
    }
    
    /** 
     * This method sets the minimum and maximum constraints of this control according
     * to any {@link RangeConstraint} attached to the model's {@link FormField}.
     * 
     * @param aField The model field to get the constraint from.
     */
    private void setMinMax(FormField aField) {
		for (Constraint constraint : aField.getConstraints()) {
            if (constraint instanceof RangeConstraint) {
                RangeConstraint rangeConstraint = (RangeConstraint) constraint;
                Comparable theLowerComp = rangeConstraint.getLower();
                Comparable theUpperComp = rangeConstraint.getUpper();
                if (theLowerComp == null || isApplicableType(theLowerComp)) {
                    min = theLowerComp;
                }
                if (theUpperComp == null || isApplicableType(theUpperComp)) {
                    max = theUpperComp;
                }
            }
        }
    }

    /** 
     * This method sets the onFocus for this control's text input.
     */
	public void setOnFocus(DynamicText anOnFocus) {
        this.textInput.setOnFocus(anOnFocus);
    }

    /** 
     * This method sets the onInput for this control's text input.
     */
	public void setOnInput(DynamicText anOnInput) {
		this.textInput.setOnInput(anOnInput);
    }

	/**
	 * This method sets the columns for this control's text input.
	 * 
	 * @see TextInputControl#setColumns(int)
	 */
    public void setColumns(int aColumns) {
        this.textInput.setColumns(aColumns);
    }
}
