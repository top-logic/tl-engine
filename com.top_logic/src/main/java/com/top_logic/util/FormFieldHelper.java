/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.awt.Color;
import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.format.IdentityFormat;
import com.top_logic.basic.format.configured.Formatter;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.ResourceRenderer;
import com.top_logic.layout.basic.component.ControlComponent;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.control.AbstractCompositeControl;
import com.top_logic.layout.form.control.AbstractFormFieldControlBase;
import com.top_logic.layout.form.control.AbstractFormMemberControl;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.CheckboxControl;
import com.top_logic.layout.form.control.ColorChooserControl;
import com.top_logic.layout.form.control.DateInputControl;
import com.top_logic.layout.form.control.DropDownControl;
import com.top_logic.layout.form.control.ErrorControl;
import com.top_logic.layout.form.control.FormMemberControl;
import com.top_logic.layout.form.control.SelectionControl;
import com.top_logic.layout.form.control.TextInputControl;
import com.top_logic.layout.form.format.ColorFormat;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.ExpandableStringField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.table.CellObject;
import com.top_logic.mig.html.HTMLFormatter;

/**
 * The FormFieldHelper supplies methods for creating form fields and controls and methods to
 * get values from them.<br/>
 * Currently supported field types with corresponding controls:<br/>
 * {@link BooleanField} ({@link CheckboxControl})<br/>
 * {@link ComplexField} ({@link TextInputControl})<br/>
 * {@link SelectField} ({@link SelectionControl})<br/>
 * {@link StringField} ({@link TextInputControl})<br/>
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class FormFieldHelper {

    /** Suffix for the error control IDs. */
    public static final String ERROR_CONTROL_SUFFIX = "_errorID";

    /** Prefix for generated field names. */
    public static final String FIELDNAME_PREFIX = "GenField_";

	private final Formatter formatter = HTMLFormatter.getInstance();
    private DateFormat dateFormat;
    private NumberFormat numberFormat;
    private ColorFormat colorFormat;

	private ResPrefix resPrefix;

    /** Saves the initial values of the created fields for later use. */
    private Map data;

    /** Saves the created fields. */
    private Map fields;

    /** Saves the created controls. */
    private Map controls;

    private int nameIndex = 0;



    /**
     * Creates a {@link FormFieldHelper}.
     */
    public FormFieldHelper() {
        clear();
    }

    /**
     * Creates a {@link FormFieldHelper} and sets the given resource prefix for
     * calls of the {@link #getResString(String)} method.
     *
     * @param aResPrefix
     *        the resource prefix to set
     */
	public FormFieldHelper(ResPrefix aResPrefix) {
        clear();
        setResPrefix(aResPrefix);
    }



    /**
     * This method clears all data, fields and controls stored by this helper.
     * It also resets the date and number format setting to default.<br/>
     *
     * Use {@link #clearFieldsAndControls()}, if you want to preserve data for later use.
     */
    public void clear() {
        clearData();
        clearFieldsAndControls();
        dateFormat = null;
        numberFormat = formatter.getNumberFormat();
        colorFormat = ColorFormat.INSTANCE;
    }

    /**
     * This method clears all data in this helpers data map.
     */
    public void clearData() {
        data = new LinkedHashMap();
    }

    /**
     * This method clears all fields and controls stored by this helper.
     */
    public void clearFieldsAndControls() {
        fields = new LinkedHashMap();
        controls = new LinkedHashMap();
    }

    /**
     * Gets the data map with the stored initial values of the created FormFields.
     *
     * @return the map with the stored values of the created FormFields
     */
    public Map getData() {
        return data;
    }

    /**
     * Gets the fields map with the created FormFields.
     *
     * @return the map with the created FormFields
     */
    public Map getFields() {
        return fields;
    }

    /**
     * Gets the controls map with the created Controls.
     *
     * @return the map with the created Controls
     */
    public Map getControls() {
        return controls;
    }

    /**
     * Gets the initial value of the created FormField with the given name.
     *
     * @param aKey
     *            the name of the data
     * @return the initial value of the created FormField with the given name
     */
    public Object getData(String aKey) {
        return data.get(aKey);
    }

    /**
     * Stores a value in the helpers data map with the stored initial values of the created
     * FormFields. Use this to store additional application data if required.
     *
     * @param aKey
     *            the name of the data
     * @param aValue
     *            the value to store
     */
    public void putData(String aKey, Object aValue) {
        data.put(aKey, aValue);
    }

    /**
     * Gets the field with the given name.
     *
     * @param aKey
     *            the name of the field
     * @return the field with the given name
     */
    public FormField getField(String aKey) {
        return (FormField)fields.get(aKey);
    }

    /**
     * Gets the control with the given name.
     *
     * @param aKey
     *            the name of the control
     * @return the control with the given name
     */
    public Control getControl(String aKey) {
        return (Control)controls.get(aKey);
    }



    /**
     * This method returns the date format for new date fields.
     *
     * @return the dateFormat
     */
    public DateFormat getDateFormat() {
        return this.dateFormat;
    }

    /**
     * This method sets the date format for new date fields.
     *
     * @param aDateFormat
     *            the date format to set
     */
    public void setDateFormat(DateFormat aDateFormat) {
        this.dateFormat = aDateFormat;
    }



    /**
     * This method returns the number format for new number fields.
     *
     * @return the number format for new number fields
     */
    public NumberFormat getNumberFormat() {
        return (this.numberFormat);
    }

    /**
     * This method sets the number format for new number fields.
     *
     * @param aNumberFormat
     *            the number format to set
     */
    public void setNumberFormat(NumberFormat aNumberFormat) {
        this.numberFormat = aNumberFormat;
    }



    /**
     * This method returns the color format for new color fields.
     *
     * @return the color format for new color fields
     */
    public ColorFormat getColorFormat() {
        return (this.colorFormat);
    }

    /**
     * This method sets the color format for new color fields.
     *
     * @param aColorFormat
     *            the color format to set
     */
    public void setColorFormat(ColorFormat aColorFormat) {
        this.colorFormat = aColorFormat;
    }



    /**
     * This method returns the resource prefix for internationalization.
     *
     * @return the number format for new date fields
     */
	public ResPrefix getResPrefix() {
        return this.resPrefix;
    }

    /**
     * This method sets the resource prefix for internationalization.
     *
     * @param aResPrefix
     *        the resource prefix to set
     */
	public void setResPrefix(ResPrefix aResPrefix) {
		this.resPrefix = aResPrefix == null ? ResPrefix.GLOBAL : aResPrefix;
    }



    /**
     * Inserts the resource prefix to the given key and gets the string from the
     * ResourceBundle.
     *
     * @param aKey
     *            the lookup key
     * @return the internationalized string for the given key
     */
    public String getResString(String aKey) {
		return Resources.getInstance().getString(getResPrefix().key(aKey));
    }

    /**
     * Inserts the resource prefix to the given key and gets the message from the
     * ResourceBundle.
     *
     * @param aKey
     *            the lookup key
     * @return the internationalized string for the given key
	 * 
	 * @deprecated Use {@link #getResString(String)}, {@link #getResMessage(String, Object[])}, or
	 *             {@link ResKey#decode(String)}, if really
	 *             required.
	 */
    @Deprecated
    public String getResMessage(String aKey) {
		return Resources.getInstance().getString(getResPrefix().key(aKey));
    }

    /**
     * Inserts the resource prefix to the given key and gets the message from the
     * ResourceBundle.
     *
     * @param aKey
     *            the lookup key
     * @return the internationalized string for the given key
     */
    public String getResMessage(String aKey, Object[] values) {
		return Resources.getInstance().getMessage(getResPrefix().key(aKey), values);
    }

    /**
     * Inserts the resource prefix to the given key and gets the message from the
     * ResourceBundle.
     *
     * @param aKey
     *            the lookup key
     * @return the internationalized string for the given key
     */
    public String getResMessage(String aKey, Object value0) {
		return Resources.getInstance().getMessage(getResPrefix().key(aKey), value0);
    }

    /**
     * Inserts the resource prefix to the given key and gets the message from the
     * ResourceBundle.
     *
     * @param aKey
     *            the lookup key
     * @return the internationalized string for the given key
     */
    public String getResMessage(String aKey, Object value0, Object value1) {
		return Resources.getInstance().getMessage(getResPrefix().key(aKey), value0, value1);
    }

    /**
     * Inserts the resource prefix to the given key and gets the message from the
     * ResourceBundle.
     *
     * @param aKey
     *            the lookup key
     * @return the internationalized string for the given key
     */
    public String getResMessage(String aKey, Object value0, Object value1, Object value2) {
		return Resources.getInstance().getMessage(getResPrefix().key(aKey), value0, value1, value2);
    }

    /**
     * Generates a unique name for FormFields. Use {@link #createName(FormContainer)} if possible.
     *
     * @return a unique name for FormFields
     */
    public String createName() {
        return FIELDNAME_PREFIX + nameIndex++;
    }

    /**
     * Generates a unique name for FormFields. In addition, this method check whether the
     * FormContainer contains a member with the generated name already.
     *
     * @param aContext
     *            the form context to check whether a field with the generated name already
     *            exists
     *
     * @return a unique name for FormFields;
     */
    public String createName(FormContainer aContext) {
        String theName = FIELDNAME_PREFIX + nameIndex++;
        while (aContext.hasMember(theName)) {
            theName = FIELDNAME_PREFIX + nameIndex++;
        }
        return theName;
    }



    /**
     * Creates a FormField with the given name and the given object as value and adds it to
     * the given FormContainer. The kind of FormField is determined automatically. <br/>
     * <br/>
     * Note: Beware of <code>null</code> object! If the given object is <code>null</code>,
     * a StringField will be created! Use the create*Field methods instead to ensure the
     * right field type!<br/>
     * <br/>
     * The initial value of the field is stored in the helpers data map, and the field
     * itself is stored in the helpers fields map. Both can be accessed by the
     * {@link #getData(String)} or {@link #getField(String)} methods with the name as key
     * later.
     *
     * @param aName
     *            the name of the FormField (may be empty or <code>null</code>; in this
     *            case a unique name will be created)
     * @param aValue
     *            the value of the FormField to set (may be <code>null</code>)
     * @param aLabel
     *            the label of the FormField to set (may be <code>null</code>)
     * @param aContext
     *            the FormContainer to add the field
     * @param isImmutable
     *            the immutable state of the field
     * @param isMandatory
     *            the mandatory state of the field
     * @return the created FormField (SelectField or ComplexField or StringField)
     * @deprecated Use the create*Field methods instead to ensure the right field type
     */
    @Deprecated
    public FormField createField(String aName, Object aValue, String aLabel, FormContainer aContext, boolean isImmutable, boolean isMandatory) {
        String theName = StringServices.isEmpty(aName) ? createName(aContext) : aName;
        Object theValue = aValue;
        FormField theField;
        if (theValue instanceof Wrapper) {
            theField = FormFactory.newSelectField(theName, CollectionUtil.intoList(theValue));
            theValue = CollectionUtil.intoList(theValue);
        }
        else if (theValue instanceof List) {
            theField = FormFactory.newSelectField(theName, (List)theValue, true, true);
        }
        else if (theValue instanceof Date) {
            theField = FormFactory.newDateField(theName, null, false);
            if (dateFormat != null) ((ComplexField) theField).setFormat(dateFormat);
        }
        else if (theValue instanceof Double) {
            theField = FormFactory.newComplexField(theName, numberFormat);
        }
        else if (theValue instanceof Boolean) {
            theField = FormFactory.newBooleanField(theName);
        }
        else {
            theField = FormFactory.newStringField(theName);
            if (theValue != null) {
                theValue = theValue.toString();
            }
        }
        setFieldParameters(theField, theName, theValue, aLabel, aContext, isImmutable, isMandatory);
        return theField;
    }

    /**
     * Creates a BooleanField with the given name and the given boolean as value and adds it
     * to the given FormContainer.
     *
     * @see #createField(String, Object, String, FormContainer, boolean, boolean) for parameters
     */
    public BooleanField createBooleanField(String aName, boolean aValue, String aLabel, FormContainer aContext, boolean isImmutable, boolean isMandatory) {
        return createBooleanField(aName, Boolean.valueOf(aValue), aLabel, aContext, isImmutable, isMandatory);
    }

    /**
     * Creates a BooleanField with the given name and the given object as value and adds it
     * to the given FormContainer.
     *
     * @see #createField(String, Object, String, FormContainer, boolean, boolean) for parameters
     */
    public BooleanField createBooleanField(String aName, Object aValue, String aLabel, FormContainer aContext, boolean isImmutable, boolean isMandatory) {
        String theName = StringServices.isEmpty(aName) ? createName(aContext) : aName;
        Object theValue = aValue;
        BooleanField theField = FormFactory.newBooleanField(theName);
        if (!(theValue instanceof Boolean)) {
            theValue = getBooleanValue(theValue);
        }
        setFieldParameters(theField, theName, theValue, aLabel, aContext, isImmutable, isMandatory);
        return theField;
    }

    /**
     * Creates a ComplexField with the given name and the given object as value and adds it
     * to the given FormContainer.
     *
     * @see #createField(String, Object, String, FormContainer, boolean, boolean) for parameters
     */
    public ComplexField createComplexField(String aName, Object aValue, String aLabel, FormContainer aContext, boolean isImmutable, boolean isMandatory) {
        String theName = StringServices.isEmpty(aName) ? createName(aContext) : aName;
        Object theValue = aValue;
        ComplexField theField;
        if (theValue instanceof Date) {
            theField = FormFactory.newDateField(theName, null, false);
            if (dateFormat != null) theField.setFormat(dateFormat);
        }
        else if (theValue instanceof Number) {
            theField = FormFactory.newComplexField(theName, numberFormat);
            theValue = getDoubleValue(theValue);
        }
        else if (theValue instanceof Color) {
            theField = FormFactory.newComplexField(theName, colorFormat);
        }
        else {
            theField = FormFactory.newComplexField(theName, IdentityFormat.INSTANCE);
        }
        setFieldParameters(theField, theName, theValue, aLabel, aContext, isImmutable, isMandatory);
        return theField;
    }

    /**
     * Creates a multiple selection SelectField with the given name and aValue as option
     * list and adds it to the given FormContainer. All options gets selected initially.
     *
     * @see #createField(String, Object, String, FormContainer, boolean, boolean) for
     *      parameters
     */
    public SelectField createSelectField(String aName, Object aValue, String aLabel, FormContainer aContext, boolean isImmutable, boolean isMandatory) {
        String theName = StringServices.isEmpty(aName) ? createName(aContext) : aName;
        Object theValue = aValue;
        SelectField theField;
        if (theValue instanceof List) {
            theField = FormFactory.newSelectField(theName, (List)theValue, true, isImmutable);
        }
        else {
            theField = FormFactory.newSelectField(theName, CollectionUtil.intoList(theValue));
            theValue = CollectionUtil.intoList(theValue);
        }
        setFieldParameters(theField, theName, theValue, aLabel, aContext, isImmutable, isMandatory);
        return theField;
    }

    /**
     * Creates a multiple selection SelectField with the given name and the given object
     * list and adds it to the given FormContainer. Selects initially aValue, which may be a
     * list of options to select.
     *
     * @param aOptionList
     *            the options of the SelectField
     * @param aValue
     *            the initially selected option(s) of the SelectField
     * @see #createField(String, Object, String, FormContainer, boolean, boolean) for the
     *      other parameters
     */
    public SelectField createSelectField(String aName, List aOptionList, Object aValue, String aLabel, FormContainer aContext, boolean isImmutable, boolean isMandatory) {
        String theName = StringServices.isEmpty(aName) ? createName(aContext) : aName;
        Object theValue = aValue;
        SelectField theField = FormFactory.newSelectField(theName, aOptionList, true, isImmutable);
        if (!(theValue instanceof List)) {
            theValue = CollectionUtil.intoList(theValue);
        }
        setFieldParameters(theField, theName, theValue, aLabel, aContext, isImmutable, isMandatory);
        return theField;
    }

    /**
     * Creates a single selection SelectField with the given name and the given object list and adds it to the given FormContainer.
     * Selects the given object initially
     *
     * @param aOptionList
     *            the options of the SelectField
     * @param aValue
     *            the selected option of the SelectField
     * @see #createField(String, Object, String, FormContainer, boolean, boolean) for
     *      parameters
     */
    public SelectField createSingleSelectField(String aName, List aOptionList, Object aValue, String aLabel, FormContainer aContext, boolean isImmutable, boolean isMandatory) {
        String theName = StringServices.isEmpty(aName) ? createName(aContext) : aName;
        Object theValue = aValue;
        SelectField theField = FormFactory.newSelectField(theName, aOptionList, false, isImmutable);
        if (theValue == null) {
            theValue = new ArrayList(0);
        }
        else if (theValue instanceof List) {
            if (((List)theValue).size() > 1) {
                theValue = CollectionUtil.intoList(((List)theValue).get(0));
            }
        }
        else {
            theValue = CollectionUtil.intoList(theValue);
        }
        setFieldParameters(theField, theName, theValue, aLabel, aContext, isImmutable, isMandatory);
        return theField;
    }

    /**
     * Creates a StringField with the given name and the given object as value and adds it
     * to the given FormContainer.
     *
     * @see #createField(String, Object, String, FormContainer, boolean, boolean) for parameters
     */
    public StringField createStringField(String aName, Object aValue, String aLabel, FormContainer aContext, boolean isImmutable, boolean isMandatory) {
        String theName = StringServices.isEmpty(aName) ? createName(aContext) : aName;
        Object theValue = aValue;
        StringField theField = FormFactory.newStringField(theName);
        if (theValue != null) {
            theValue = theValue.toString();
        }
        setFieldParameters(theField, theName, theValue, aLabel, aContext, isImmutable, isMandatory);
        return theField;
    }

	/**
	 * Creates an {@link ExpandableStringField} with the given name and the given Object as value
	 * and adds it to the given FormContainer.
	 * 
	 * @see #createField(String, Object, String, FormContainer, boolean, boolean) for parameters
	 */
	public ExpandableStringField createExpandableStringField(String aName, Object aValue, String aLabel,
			FormContainer aContext, boolean isImmutable, boolean isMandatory) {
		String theName = StringServices.isEmpty(aName) ? createName(aContext) : aName;
		Object theValue = aValue;
		ExpandableStringField theField = FormFactory.newExpandableStringField(aName);
		if (theValue != null) {
			theValue = theValue.toString();
		}
		setFieldParameters(theField, theName, theValue, aLabel, aContext, isImmutable, isMandatory);
		return theField;
	}

    /**
     * Sets the fields parameter.
     *
     * @see #createField(String, Object, String, FormContainer, boolean, boolean) for the parameters
     */
    private void setFieldParameters(FormField aField, String aName, Object aValue, String aLabel, FormContainer aContext, boolean isImmutable, boolean isMandatory) {
        aField.initializeField(aValue);
        if (aField instanceof SelectField) {
            ((SelectField)aField).setOptionLabelProvider(MetaResourceProvider.INSTANCE);
        }
        if (aLabel != null) {
            aField.setLabel(aLabel);
        }
        aField.setImmutable(isImmutable);
        aField.setMandatory(isMandatory);
        if (aContext != null) aContext.addMember(aField);
        data.put(aName, aValue);
        fields.put(aName, aField);
    }

    /**
     * Parameter: isMandatory = false
     * @see #createField(String, Object, String, FormContainer, boolean, boolean)
     * @deprecated Use the create*Field methods instead to ensure the right field type
     */
    @Deprecated
    public FormField createField(String aName, Object aValue, String aLabel, FormContainer aContext, boolean isImmutable) {
        return createField(aName, aValue, aLabel, aContext, isImmutable, false);
    }

    /**
     * Parameter: isImmutable = false; isMandatory = false
     * @see #createField(String, Object, String, FormContainer, boolean, boolean)
     * @deprecated Use the create*Field methods instead to ensure the right field type
     */
    @Deprecated
    public FormField createField(String aName, Object aValue, String aLabel, FormContainer aContext) {
        return createField(aName, aValue, aLabel, aContext, false, false);
    }

    /**
     * Parameter: isMandatory = false
     * @see #createField(String, Object, String, FormContainer, boolean, boolean)
     */
    public BooleanField createBooleanField(String aName, Object aValue, String aLabel, FormContainer aContext, boolean isImmutable) {
        return createBooleanField(aName, aValue, aLabel, aContext, isImmutable, false);
    }

    /**
     * Parameter: isImmutable = false; isMandatory = false
     * @see #createField(String, Object, String, FormContainer, boolean, boolean)
     */
    public BooleanField createBooleanField(String aName, Object aValue, String aLabel, FormContainer aContext) {
        return createBooleanField(aName, aValue, aLabel, aContext, false, false);
    }

    /**
     * Parameter: isMandatory = false
     * @see #createField(String, Object, String, FormContainer, boolean, boolean)
     */
    public BooleanField createBooleanField(String aName, boolean aValue, String aLabel, FormContainer aContext, boolean isImmutable) {
        return createBooleanField(aName, aValue, aLabel, aContext, isImmutable, false);
    }

    /**
     * Parameter: isImmutable = false; isMandatory = false
     * @see #createField(String, Object, String, FormContainer, boolean, boolean)
     */
    public BooleanField createBooleanField(String aName, boolean aValue, String aLabel, FormContainer aContext) {
        return createBooleanField(aName, aValue, aLabel, aContext, false, false);
    }

    /**
     * Parameter: isMandatory = false
     * @see #createField(String, Object, String, FormContainer, boolean, boolean)
     */
    public ComplexField createComplexField(String aName, Object aValue, String aLabel, FormContainer aContext, boolean isImmutable) {
        return createComplexField(aName, aValue, aLabel, aContext, isImmutable, false);
    }

    /**
     * Parameter: isImmutable = false; isMandatory = false
     * @see #createField(String, Object, String, FormContainer, boolean, boolean)
     */
    public ComplexField createComplexField(String aName, Object aValue, String aLabel, FormContainer aContext) {
        return createComplexField(aName, aValue, aLabel, aContext, false, false);
    }

    /**
     * Parameter: isMandatory = false
     * @see #createField(String, Object, String, FormContainer, boolean, boolean)
     */
    public SelectField createSelectField(String aName, Object aValue, String aLabel, FormContainer aContext, boolean isImmutable) {
        return createSelectField(aName, aValue, aLabel, aContext, isImmutable, false);
    }

    /**
     * Parameter: isImmutable = false; isMandatory = false
     * @see #createField(String, Object, String, FormContainer, boolean, boolean)
     */
    public SelectField createSelectField(String aName, Object aValue, String aLabel, FormContainer aContext) {
        return createSelectField(aName, aValue, aLabel, aContext, false, false);
    }

    /**
     * Parameter: isMandatory = false
     * @see #createField(String, Object, String, FormContainer, boolean, boolean)
     */
    public SelectField createSelectField(String aName, List aOptionList, Object aValue, String aLabel, FormContainer aContext, boolean isImmutable) {
        return createSelectField(aName, aOptionList, aValue, aLabel, aContext, isImmutable, false);
    }

    /**
     * Parameter: isImmutable = false; isMandatory = false
     * @see #createField(String, Object, String, FormContainer, boolean, boolean)
     */
    public SelectField createSelectField(String aName, List aOptionList, Object aValue, String aLabel, FormContainer aContext) {
        return createSelectField(aName, aOptionList, aValue, aLabel, aContext, false, false);
    }

    /**
     * Parameter: isMandatory = false
     * @see #createField(String, Object, String, FormContainer, boolean, boolean)
     */
    public SelectField createSingleSelectField(String aName, List aOptionList, Object aValue, String aLabel, FormContainer aContext, boolean isImmutable) {
        return createSingleSelectField(aName, aOptionList, aValue, aLabel, aContext, isImmutable, false);
    }

    /**
     * Parameter: isImmutable = false; isMandatory = false
     * @see #createField(String, Object, String, FormContainer, boolean, boolean)
     */
    public SelectField createSingleSelectField(String aName, List aOptionList, Object aValue, String aLabel, FormContainer aContext) {
        return createSingleSelectField(aName, aOptionList, aValue, aLabel, aContext, false, false);
    }

    /**
     * Parameter: isMandatory = false
     * @see #createField(String, Object, String, FormContainer, boolean, boolean)
     */
    public StringField createStringField(String aName, Object aValue, String aLabel, FormContainer aContext, boolean isImmutable) {
        return createStringField(aName, aValue, aLabel, aContext, isImmutable, false);
    }

    /**
     * Parameter: isImmutable = false; isMandatory = false
     * @see #createField(String, Object, String, FormContainer, boolean, boolean)
     */
    public StringField createStringField(String aName, Object aValue, String aLabel, FormContainer aContext) {
        return createStringField(aName, aValue, aLabel, aContext, false, false);
    }

    /**
     * Creates the corresponding control for a FormField.<br/>
     * Use this method only if you don't want to get an ErrorControl, too.<br/>
     * Else use {@link #createControls(FormField, ControlComponent)} instead.
     *
     * Currently supported field types with corresponding controls:<br/>
     * {@link BooleanField} ({@link CheckboxControl})<br/>
     * {@link ComplexField} ({@link TextInputControl})<br/>
     * {@link SelectField} ({@link SelectionControl})<br/>
     * {@link StringField} ({@link TextInputControl})<br/>
     *
     * @param aField
     *            the field to create the control for
     * @param aComponent
     *            the component to add the created control
     * @return the created control
     */
    public AbstractFormFieldControlBase createControl(FormField aField, ControlComponent aComponent) {
        return createControl(aField, aComponent, true);
    }

    public AbstractFormFieldControlBase createControl(FormField aField, ControlComponent aComponent, boolean usePopupIfPossible) {
        return createControl(aField, aComponent, usePopupIfPossible, -1);
    }

    public AbstractFormFieldControlBase createControl(FormField aField, ControlComponent aComponent, boolean usePopupIfPossible, int aSize) {
        if (aField == null || aComponent == null) return null;
        String theName = aField.getQualifiedName();
        AbstractFormFieldControlBase theControl;
        if (aField instanceof SelectField) {
            if(usePopupIfPossible) {
                theControl = new SelectionControl(((SelectField)aField));
            }
            else {
				theControl = new DropDownControl(((SelectField) aField));
            }
        }
        else if (aField instanceof BooleanField) {
            theControl = new CheckboxControl((BooleanField)aField);
        }
        else if (aField instanceof ComplexField) {
            ComplexField theField = (ComplexField)aField;
            if (theField.getFormat() instanceof DateFormat) {
                theControl = new DateInputControl(theField);
            }
            else if (theField.getFormat() instanceof ColorFormat) {
                theControl = new ColorChooserControl(theField);
            }
            else {
                theControl = new TextInputControl(aField);
            }
        }
        else {
            theControl = new TextInputControl(aField);
        }
        if(aSize != -1 && theControl instanceof TextInputControl) {
            ((TextInputControl)theControl).setColumns(aSize);
        }
        aComponent.addControl(theName, theControl);
        controls.put(theName, theControl);
        return theControl;
    }

    /**
     * Creates a CheckboxControl for a BooleanField.<br/>
     *
     * @param aField
     *            the field to create the control for
     * @param aComponent
     *            the component to add the created control
     * @return the created control
     * @see #appendErrorControl(AbstractFormFieldControlBase, ControlComponent)
     */
    public CheckboxControl createCheckboxControl(BooleanField aField, ControlComponent aComponent) {
        if (aField == null || aComponent == null) return null;
        String theName = aField.getQualifiedName();
        CheckboxControl theControl = new CheckboxControl(aField);
        aComponent.addControl(theName, theControl);
        controls.put(theName, theControl);
        return theControl;
    }

	/**
	 * Creates a SelectionControl (Dialog) for a SelectField.<br/>
	 * 
	 * @param aField
	 *        the field to create the control for
	 * @param aComponent
	 *        the component to add the created control
	 * @return the created control
	 * @see #appendErrorControl(AbstractFormFieldControlBase, ControlComponent)
	 */
    public SelectionControl createSelectionControl(SelectField aField, ControlComponent aComponent) {
        if (aField == null || aComponent == null) return null;
        String theName = aField.getQualifiedName();
        SelectionControl theControl = new SelectionControl(aField);
        aComponent.addControl(theName, theControl);
        controls.put(theName, theControl);
        return theControl;
    }

    /**
     * Creates a SelectControl (drop-down list) for a SelectField.<br/>
     *
     * @param aField
     *            the field to create the control for
     * @param aComponent
     *            the component to add the created control
     * @return the created control
     * @see #appendErrorControl(AbstractFormFieldControlBase, ControlComponent)
     */
	public DropDownControl createSelectControl(SelectField aField, ControlComponent aComponent) {
        if (aField == null || aComponent == null) return null;
        String theName = aField.getQualifiedName();
		DropDownControl theControl = new DropDownControl(aField);
        aComponent.addControl(theName, theControl);
        controls.put(theName, theControl);
        return theControl;
    }

    /**
     * Creates a TextInputControl for a FormField.<br/>
     *
     * @param aField
     *            the field to create the control for
     * @param aComponent
     *            the component to add the created control
     * @return the created control
     * @see #appendErrorControl(AbstractFormFieldControlBase, ControlComponent)
     */
    public TextInputControl createTextInputControl(FormField aField, ControlComponent aComponent) {
        if (aField == null || aComponent == null) return null;
        String theName = aField.getQualifiedName();
        TextInputControl theControl = new TextInputControl(aField);
        aComponent.addControl(theName, theControl);
        controls.put(theName, theControl);
        return theControl;
    }

    /**
     * Creates an ErrorControl for a FormField.<br/>
     * Use this method only if you don't want to get an proper control, too.<br/>
     * Else use {@link #createControls(FormField, ControlComponent)} instead.
     *
     * @param aField
     *            the field to create the ErrorControl for
     * @param aComponent
     *            the component to add the created ErrorControl
     * @return the created control
     * @see #createControls(FormField, ControlComponent)
     */
	public ErrorControl createErrorControl(FormMember aField, ControlComponent aComponent) {
        if (aField == null || aComponent == null) return null;
        String theName = aField.getQualifiedName() + ERROR_CONTROL_SUFFIX;
        ErrorControl theControl = new ErrorControl(aField, true);
        aComponent.addControl(theName, theControl);
        controls.put(theName, theControl);
        return theControl;
    }

    /**
     * Puts the given control into a list and creates and adds an ErrorControl for the
     * FormField of the given control. Use this if you create your own control instead of
     * using the {@link #createControls(FormField, ControlComponent)} method.<br/>
     *
     * @param aControl
     *            the control to create an ErrorControl for
     * @param aComponent
     *            the component to add the created ErrorControl
     * @return a list with two controls, the given control and the create ErrorControl. In
     *         case that the Control cannot be created (should never happen),
     *         <code>null</code> is returned.
     */
    public List appendErrorControl(AbstractFormFieldControlBase aControl, ControlComponent aComponent) {
        ArrayList theList = new ArrayList(2);
        if (aControl == null) return null;
        theList.add(aControl);
        Control theErrorControl = createErrorControl(aControl.getFieldModel(), aComponent);
        if (theErrorControl != null) {
            theList.add(theErrorControl);
        }
        return theList;
    }

    /**
     * Creates the corresponding Control and ErrorControl for a FormField. <br/>
     *
     * @param aField
     *            the field to create the controls for
     * @param aComponent
     *            the component to add the created controls
     * @return a list with two controls, the proper Control and the ErrorControl. In case
     *         that the Control cannot be created (should never happen), <code>null</code>
     *         is returned.
     */
    public List createControls(FormField aField, ControlComponent aComponent) {
        return createControls(aField,aComponent, true);
    }

    public List createControls(FormField aField, ControlComponent aComponent, boolean usePopupIfPossible) {
        return createControls(aField, aComponent, usePopupIfPossible, -1);
    }
    public List createControls(FormField aField, ControlComponent aComponent, boolean usePopupIfPossible, int aSize) {
        ArrayList theList = new ArrayList(2);
        Control theControl = createControl(aField, aComponent, usePopupIfPossible, aSize);
        if (theControl == null) return null;
        theList.add(theControl);
        theControl = createErrorControl(aField, aComponent);
        if (theControl != null) {
            theList.add(theControl);
        }
        return theList;
    }

    /**
     * Creates the corresponding Control and ErrorControl for a FormField and returns them
     * in reversed Order.<br/>
     *
     * @param aField
     *            the field to create the controls for
     * @param aComponent
     *            the component to add the created controls
     * @return a list with two controls, the ErrorControl and the proper Control. In case
     *         that the Control cannot be created (should never happen), <code>null</code>
     *         is returned.
     */
    public List createControlsReversed(FormField aField, ControlComponent aComponent) {
        List theList = createControls(aField, aComponent);
        Collections.reverse(theList);
        return theList;
    }

    /**
     * Writes the control or field or data with the given name to the given TagWriter.
     *
     * @param aKey
     *            the name of the given control or field or data
     * @param aRenderer
     *            the renderer to use to write the content if there exists no control
     */
	public void write(String aKey, DisplayContext aContext, TagWriter aOut, Renderer<Object> aRenderer)
			throws IOException {
        Control theControl = getControl(aKey);
        if (theControl != null) {
            writeObject(theControl, aContext, aOut, aRenderer);
        }
        else {
            FormField theField = getField(aKey);
            if (theField != null) {
                writeObject(theField, aContext, aOut, aRenderer);
            }
            else {
                writeObject(getData(aKey), aContext, aOut, aRenderer);
            }
        }
    }

    /**
     * Writes the control or field or data with the given name to the given TagWriter.
     *
     * @param aKey
     *            the name of the given control or field or data
     */
    public void write(String aKey, DisplayContext aContext, TagWriter aOut) throws IOException {
        write(aKey, aContext, aOut, ResourceRenderer.INSTANCE);
    }


    /**
     * Writes a Object to the given TagWriter. This method can handle controls and fields
     * and asks for their values.
     *
     * @param aObject
     *            the object to write
     * @param aRenderer
     *            the renderer to use to write the content if there exists no control
     */
	public void writeObject(Object aObject, DisplayContext aContext, TagWriter aOut, Renderer<Object> aRenderer)
			throws IOException {
        if (aObject instanceof Control) {
            ((Control)aObject).write(aContext, aOut);
            return;
        }
        Object theValue = aObject;
        if (theValue instanceof FormField) {
            theValue = ((FormField)aObject).hasValue() ? ((FormField)aObject).getValue() : null;
        }
        if (theValue != null) {
            if (aRenderer == null) {
                ResourceRenderer.INSTANCE.write(aContext, aOut, theValue);
            }
            else {
                aRenderer.write(aContext, aOut, theValue);
            }
        }
    }

    /**
     * Writes a Object to the given TagWriter. This method can handle controls and fields
     * and asks for their values.
     *
     * @param aObject
     *            the object to write
     */
    public void writeObject(Object aObject, DisplayContext aContext, TagWriter aOut) throws IOException {
        writeObject(aObject, aContext, aOut, ResourceRenderer.INSTANCE);
    }



    /**
     * Removes the field, the controls and the data entry with the given name. Necessary
     * cleanup work is done automatically.
     *
     * @param aKey
     *            the name of the field or control or data to remove
     * @param aComponent
     *            the component to remove the controls from
     */
    public void remove(String aKey, ControlComponent aComponent) {
        List toRemove = new ArrayList();
        toRemove.add(controls.remove(aKey));
        toRemove.add(controls.remove(aKey + ERROR_CONTROL_SUFFIX));
        toRemove.add(fields.remove(aKey));
        toRemove.add(data.remove(aKey));
        internalRemoveObjects(toRemove, aComponent);
    }



    /**
     * Removes the field, the controls and the data entry with the given name. Necessary
     * cleanup work is done automatically.<br/>
     * Use {@link #remove(String, ControlComponent)} if possible!
     *
     * @param aKey
     *            the name of the field or control or data to remove
     */
    public void remove(String aKey) {
        remove(aKey, null);
    }



    /**
     * Removes form fields or controls. Necessary cleanup work is done
     * automatically.<br/>
     *
     * @param aObject
     *            the field or control or data to remove
     * @param aComponent
     *            the component to remove the controls from
     */
    public void removeObject(Object aObject, ControlComponent aComponent) {
        if (aObject == null) return; // aObject doesn't exist - well done!
        Collection theCollection = aObject instanceof Collection ? (Collection)aObject :
        	aObject instanceof AbstractCompositeControl ? ((AbstractCompositeControl)aObject).getChildren() : 
            CollectionUtil.intoList(aObject);
        for (Iterator it = theCollection.iterator(); it.hasNext();) {
            Object aElement = it.next();
            String theKey = null;
            if (aElement instanceof AbstractFormMemberControl) {
                theKey = ((AbstractFormMemberControl)aElement).getModel().getQualifiedName();
            }
			else if (aElement instanceof ErrorControl) {
				Set<? extends FormMember> models = ((ErrorControl) aElement).getModels();
				if (models.size() == 1) {
					theKey = (models.iterator().next()).getQualifiedName();
					theKey = theKey + ERROR_CONTROL_SUFFIX;
				}
			}
            else if (aElement instanceof FormMember) {
                theKey = ((FormMember)aElement).getName();
            }
            if (theKey != null) {
                Control theDeleted = (Control) controls.remove(theKey);
                if(theDeleted != null) {
                    theDeleted.detach();
                }
                fields.remove(theKey);
                data.remove(theKey);
            }
        }
        internalRemoveObjects(theCollection, aComponent);
    }



    /**
     * Removes form fields or controls. Necessary cleanup work is done
     * automatically. Note that controls have to be detached before a form field
     * is removed.
     *
     * @param aCollection
     *            the collection of fields or controls or data to remove
     * @param aComponent
     *            the component to remove the controls from
     */
    private void internalRemoveObjects(Collection aCollection, ControlComponent aComponent) {
        LinkedHashSet theSet = new LinkedHashSet();

        // Remove Controls
        Iterator it = aCollection.iterator();
        while (it.hasNext()) {
            Object theElement = it.next();
            if (theElement instanceof AbstractFormMemberControl) {
                FormMember theModel = ((AbstractFormMemberControl)theElement).getModel();
                theSet.add(theModel);
                String theControlKey = theModel.getQualifiedName();
                Control theControl = aComponent == null ? null : aComponent.getControl(theControlKey);
                if (theControl != null && aComponent != null) {
                    aComponent.removeControl(theControlKey);
                }
                if (((Control)theElement).isAttached()) {
                    try {
                        ((Control)theElement).detach();
                    }
                    catch (Exception e) {
                        // ignore
                    }
                }
            }
            else if (theElement instanceof FormMember) {
                theSet.add(theElement);
            }
            // else don't remove an unremovable object
        }

        // Remove FormMembers
        if (theSet.contains(null)) theSet.remove(null);
        it = theSet.iterator();
        while (it.hasNext()) {
            FormMember theMember = (FormMember)it.next();
            FormContainer theParent = theMember.getParent();
            if (theParent != null) theParent.removeMember(theMember);
        }
    }



    /**
     * Updates the data entry with the actual value of the corresponding field. If no field
     * with the given name exists, the data entry will be set to <code>null</code>.
     *
     * @param aKey
     *            the field to save the value from
     */
    public void updateDataFromField(String aKey) {
        data.put(aKey, getProperValueFrom(aKey));
    }

    /**
     * Updates the data entries of all fields with the field's actual values.
     */
    public void updateAllDataFromFields() {
        for (Iterator it = fields.keySet().iterator(); it.hasNext();) {
            updateDataFromField((String)it.next());
        }
    }

    /**
     * Gets the value of the field or control or data with the given name as an Object.
     *
     * @param aKey
     *            the name of the given field or control or data
     * @return the actual value of the given field or control or data
     */
    public Object getProperValueFrom(String aKey) {
        Object theObject = getField(aKey);
        if (theObject == null) theObject = getControl(aKey);
        if (theObject == null) theObject = getData(aKey);
        return getProperValue(theObject);
    }

    /**
     * Gets the value of the field or control or data with the given name as a String.
     *
     * @param aKey
     *            the name of the given field or control or data
     * @return the actual value of the given field or control or data as String
     */
    public String getStringValueFrom(String aKey) {
        Object theObject = getField(aKey);
        if (theObject == null) theObject = getControl(aKey);
        if (theObject == null) theObject = getData(aKey);
        return getStringValue(theObject);
    }

    /**
     * Gets the Double value of the field or control or data with the given name.
     *
     * @param aKey
     *            the name of the given field or control or data
     * @return the actual Double value of the given field or control or data
     */
    public Double getDoubleValueFrom(String aKey) {
        Object theObject = getField(aKey);
        if (theObject == null) theObject = getControl(aKey);
        if (theObject == null) theObject = getData(aKey);
        return getDoubleValue(theObject);
    }

    /**
     * Gets the double value of the field or control or data with the given name.
     *
     * @param aKey
     *            the name of the given field or control or data
     * @return the actual double value of the given field or control or data
     */
    public double getdoubleValueFrom(String aKey) {
        Object theObject = getField(aKey);
        if (theObject == null) theObject = getControl(aKey);
        if (theObject == null) theObject = getData(aKey);
        return getdoubleValue(theObject);
    }

    /**
     * Gets the Integer value of the field or control or data with the given name.
     *
     * @param aKey
     *            the name of the given field or control or data
     * @return the actual Integer value of the given field or control or data
     */
    public Integer getIntegerValueFrom(String aKey) {
        Object theObject = getField(aKey);
        if (theObject == null) theObject = getControl(aKey);
        if (theObject == null) theObject = getData(aKey);
        return getIntegerValue(theObject);
    }

    /**
     * Gets the int value of the field or control or data with the given name.
     *
     * @param aKey
     *            the name of the given field or control or data
     * @return the actual int value of the given field or control or data
     */
    public int getintValueFrom(String aKey) {
        Object theObject = getField(aKey);
        if (theObject == null) theObject = getControl(aKey);
        if (theObject == null) theObject = getData(aKey);
        return getintValue(theObject);
    }

    /**
     * Gets the Long value of the field or control or data with the given name.
     *
     * @param aKey
     *            the name of the given field or control or data
     * @return the actual Long value of the given field or control or data
     */
    public Long getLongValueFrom(String aKey) {
        Object theObject = getField(aKey);
        if (theObject == null) theObject = getControl(aKey);
        if (theObject == null) theObject = getData(aKey);
        return getLongValue(theObject);
    }

    /**
     * Gets the long value of the field or control or data with the given name.
     *
     * @param aKey
     *            the name of the given field or control or data
     * @return the actual long value of the given field or control or data
     */
    public long getlongValueFrom(String aKey) {
        Object theObject = getField(aKey);
        if (theObject == null) theObject = getControl(aKey);
        if (theObject == null) theObject = getData(aKey);
        return getlongValue(theObject);
    }

    /**
     * Gets the Boolean value of the field or control or data with the given name.
     *
     * @param aKey
     *            the name of the given field or control or data
     * @return the actual Boolean value of the given field or control or data
     */
    public Boolean getBooleanValueFrom(String aKey) {
        Object theObject = getField(aKey);
        if (theObject == null) theObject = getControl(aKey);
        if (theObject == null) theObject = getData(aKey);
        return getBooleanValue(theObject);
    }

    /**
     * Gets the boolean value of the field or control or data with the given name.
     *
     * @param aKey
     *            the name of the given field or control or data
     * @return the actual boolean value of the given field or control or data
     */
    public boolean getbooleanValueFrom(String aKey) {
        Object theObject = getField(aKey);
        if (theObject == null) theObject = getControl(aKey);
        if (theObject == null) theObject = getData(aKey);
        return getbooleanValue(theObject);
    }

    /**
     * Gets the Date value of the field or control or data with the given name.
     *
     * @param aKey
     *            the name of the given field or control or data
     * @return the actual Date value of the given field or control or data
     */
    public Date getDateValueFrom(String aKey) {
        Object theObject = getField(aKey);
        if (theObject == null) theObject = getControl(aKey);
        if (theObject == null) theObject = getData(aKey);
        return getDateValue(theObject);
    }

    /**
     * Gets the value of the field or control or data with the given name as an Object. In
     * addition to {@link #getProperValueFrom(String)}, the first element is extracted, if
     * the value is a collection of objects. Useful for getting the selected element of a
     * single selection field.
     *
     * @param aKey
     *            the name of the given field or control or data
     * @return the actual extracted value of the given field or control or data
     */
    public Object getSingleProperValueFrom(String aKey) {
        Object theObject = getField(aKey);
        if (theObject == null) theObject = getControl(aKey);
        if (theObject == null) theObject = getData(aKey);
        return getSingleProperValue(theObject);
    }



    /**
     * Gets the proper value from an object. This method can handle
     * controls and fields and asks for their values.
     *
     * @param aObject
     *            the object to get the proper value from
     * @return the value as a Object
     */
    public static Object getProperValue(Object aObject) {
        Object theObject = aObject;
        if (theObject == null) return null;
        theObject = resolveCollection(theObject);
        if (theObject instanceof AbstractFormMemberControl) {
            theObject = ((AbstractFormMemberControl)theObject).getModel();
        } else if (theObject instanceof ButtonControl) {
            theObject = ((ButtonControl) theObject).getLabel();
        }
        if (theObject instanceof AbstractFormField) {
            theObject = ((AbstractFormField)theObject).hasValue() ? ((AbstractFormField)theObject).getValue() : null;
        }
        return theObject;
    }

    /**
     * Gets a String from an object. This method can handle
     * controls and fields and asks for their values.
     *
     * @param aObject
     *            the object to get the string value from
     * @return the value as a String
     */
    public static String getStringValue(Object aObject) {
        Object theObject = getSingleProperValue(aObject);
        return theObject == null ? null : theObject.toString();
    }

    /**
	 * Gets a Float from an object. This method can handle controls and fields and asks for their
	 * values.
	 * 
	 * @param aObject
	 *        the object to get the Float value from
	 * @return the value as a Float or <code>null</code>, if the value is no Number
	 */
	public static Float getFloatValue(Object aObject) {
        Object theObject = getSingleProperValue(aObject);
        if (theObject == null) return null;
		if (theObject instanceof Float)
			return (Float) theObject;
		if (theObject instanceof Number)
			return Float.valueOf(((Number) theObject).floatValue());
        try {
			return Float.valueOf(theObject.toString());
        }
        catch (Exception e) {
            // The objects seems to be anything but a Double
            return null;
        }
    }

    /**
	 * Gets a float from an object. This method can handle controls and fields and asks for their
	 * values.
	 * 
	 * @param aObject
	 *        the object to get the float value from
	 * @return the value as a float or <code>null</code>, if the value is no Number
	 */
	public static float getfloatValue(Object aObject) {
        Object theObject = getSingleProperValue(aObject);
		if (theObject == null)
			return 0.0f;
		if (theObject instanceof Number)
			return ((Number) theObject).floatValue();
        try {
			return Float.parseFloat(theObject.toString());
        }
        catch (Exception e) {
            // The objects seems to be anything but a double
			return 0.0f;
        }
    }

    /**
	 * Gets a Double from an object. This method can handle controls and fields and asks for their
	 * values.
	 * 
	 * @param aObject
	 *        the object to get the Double value from
	 * @return the value as a Double or <code>null</code>, if the value is no Number
	 */
	public static Double getDoubleValue(Object aObject) {
		Object theObject = getSingleProperValue(aObject);
		if (theObject == null)
			return null;
		if (theObject instanceof Double)
			return (Double) theObject;
		if (theObject instanceof Number)
			return Double.valueOf(((Number) theObject).doubleValue());
		try {
			return Double.valueOf(theObject.toString());
		} catch (Exception e) {
			// The objects seems to be anything but a Double
			return null;
		}
	}

	/**
	 * Gets a double from an object. This method can handle controls and fields and asks for their
	 * values.
	 * 
	 * @param aObject
	 *        the object to get the double value from
	 * @return the value as a double or <code>null</code>, if the value is no Number
	 */
	public static double getdoubleValue(Object aObject) {
		Object theObject = getSingleProperValue(aObject);
		if (theObject == null)
			return 0.0;
		if (theObject instanceof Number)
			return ((Number) theObject).doubleValue();
		try {
			return Double.parseDouble(theObject.toString());
		} catch (Exception e) {
			// The objects seems to be anything but a double
			return 0.0;
		}
	}

	/**
	 * Gets an Integer from an object. This method can handle controls and fields and asks for their
	 * values.
	 * 
	 * @param aObject
	 *        the object to get the Integer value from
	 * @return the value as an Integer or <code>null</code>, if the value is no Number
	 */
    public static Integer getIntegerValue(Object aObject) {
        Object theObject = getSingleProperValue(aObject);
        if (theObject == null) return null;
        if (theObject instanceof Integer) return (Integer)theObject;
        if (theObject instanceof Number)
            return Integer.valueOf(((Number)theObject).intValue());
        try {
            return Integer.valueOf(theObject.toString());
        }
        catch (Exception e) {
            // The objects seems to be anything but an Integer
            return null;
        }
    }

    /**
     * Gets an int from an object. This method can handle
     * controls and fields and asks for their values.
     *
     * @param aObject
     *            the object to get the int value from
     * @return the value as an int or <code>null</code>, if the value is no Number
     */
    public static int getintValue(Object aObject) {
        Object theObject = getSingleProperValue(aObject);
        if (theObject == null) return 0;
        if (theObject instanceof Number) return ((Number)theObject).intValue();
        try {
            return Integer.parseInt(theObject.toString());
        }
        catch (Exception e) {
            // The objects seems to be anything but an int
            return 0;
        }
    }

    /**
     * Gets a Long from an object. This method can handle
     * controls and fields and asks for their values.
     *
     * @param aObject
     *            the object to get the Long value from
     * @return the value as a Long or <code>null</code>, if the value is no Number
     */
    public static Long getLongValue(Object aObject) {
        Object theObject = getSingleProperValue(aObject);
        if (theObject == null) return null;
        if (theObject instanceof Long) return (Long)theObject;
		if (theObject instanceof Number)
			return Long.valueOf(((Number) theObject).longValue());
        try {
            return Long.valueOf(theObject.toString());
        }
        catch (Exception e) {
            // The objects seems to be anything but a Long
            return null;
        }
    }

    /**
     * Gets a long from an object. This method can handle
     * controls and fields and asks for their values.
     *
     * @param aObject
     *            the object to get the long value from
     * @return the value as a long or <code>null</code>, if the value is no Number
     */
    public static long getlongValue(Object aObject) {
        Object theObject = getSingleProperValue(aObject);
        if (theObject == null) return 0;
        if (theObject instanceof Number) return ((Number)theObject).longValue();
        try {
            return Long.parseLong(theObject.toString());
        }
        catch (Exception e) {
            // The objects seems to be anything but a long
            return 0;
        }
    }

    /**
     * Gets a Boolean from an object. This method can handle
     * controls and fields and asks for their values.
     *
     * @param aObject
     *            the object to get the Boolean value from
     * @return the value as a Boolean
     */
    public static Boolean getBooleanValue(Object aObject) {
        Object theObject = getSingleProperValue(aObject);
        if (theObject instanceof Boolean) return (Boolean)theObject;
        return ((theObject != null) && theObject.toString().trim().equalsIgnoreCase("true")) ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * Gets a boolean from an object. This method can handle
     * controls and fields and asks for their values.
     *
     * @param aObject
     *            the object to get the boolean value from
     * @return the value as a boolean
     */
    public static boolean getbooleanValue(Object aObject) {
        Object theObject = getSingleProperValue(aObject);
        if (theObject instanceof Boolean) return ((Boolean)theObject).booleanValue();
        return ((theObject != null) && theObject.toString().trim().equalsIgnoreCase("true"));
    }

    /**
     * Gets a Date from an object. This method can handle
     * controls and fields and asks for their values.
     *
     * @param aObject
     *            the object to get the date value from
     * @return the value as a Date or <code>null</code>, if the value is no Date
     */
    public static Date getDateValue(Object aObject) {
        Object theObject = getSingleProperValue(aObject);
        if (theObject == null) return null;
        if (theObject instanceof Date) return (Date)theObject;
        if (theObject instanceof Calendar) return ((Calendar)theObject).getTime();
        try {
            return HTMLFormatter.getInstance().getDateFormat().parse(theObject.toString());
        }
        catch (Exception e) {
            // The objects seems to be anything but a Date
            return null;
        }
    }

    /**
     * Gets the proper value from an object. This method can handle controls and fields and
     * asks for their values. In addition to {@link #getProperValue(Object)}, the first
     * element is extracted, if the value is a collection of objects. Useful for getting the
     * selected element of a single selection field.
     *
     * @param aObject
     *            the object to get the proper value from
     * @return the value as a Object
     */
    public static Object getSingleProperValue(Object aObject) {
        Object theValue = getProperValue(aObject);
        while (theValue instanceof Collection) {
            theValue = CollectionUtil.getFirst((Collection)theValue);
        }
        return theValue;
    }

    /**
     * Gets the form field from an object. This method can handle collections, controls and
     * fields.
     *
     * @param aObject
     *        the object to get the form field from
     * @return the form field or <code>null</code>, if the given object isn't or doesn't
     *         contain a form field
     */
    public static FormField getField(Object aObject) {
        Object theObject = aObject;
        if (theObject == null) return null;
        theObject = resolveCollection(theObject);
        if (theObject instanceof AbstractFormMemberControl) {
            theObject = ((AbstractFormMemberControl)theObject).getModel();
        }
        return theObject instanceof FormField ? (FormField)theObject : null;
    }

    /**
     * Gets the control from an object. This method can handle collections as well.
     *
     * @param aObject
     *        the object to get the form field from
     * @return the form field or <code>null</code>, if the given object isn't or doesn't
     *         contain a form field
     */
    public static Control getControlValue(Object aObject) {
        Object theObject = aObject;
        if (theObject == null) return null;
        theObject = resolveCollection(theObject);
        return theObject instanceof AbstractFormMemberControl ? (AbstractFormMemberControl)theObject : null;
    }

    /**
     * Resolves values from collections. In addition, this method respects Lists created by
     * the {@link #createControls(FormField, ControlComponent)} and
     * {@link #createControlsReversed(FormField, ControlComponent)} methods, trying to get
     * the AbstractFormMemberControl primarily instead of the ErrorControls.
     *
     * @param aObject
     *        the object which might be a collection
     * @return the object which was contained in the collection, or the given object itself,
     *         if it wasn't a collection; if the collection contains more than one element,
     *         the first element is returned except it is an error control, then the the
     *         second element is returned
     */
    private static Object resolveCollection(Object aObject) {
    	boolean done;
    	do {
    		done = false;
    		if (aObject instanceof Collection) {
    			Collection collection = (Collection)aObject;
				Object theFirst = CollectionUtil.getFirst(collection);
    			if (theFirst instanceof ErrorControl) {
    				aObject = CollectionUtil.getSecond(collection);
    				if (aObject == null) aObject = theFirst;
    			}
    			else {
    				aObject = theFirst;
    			}
    			done = true;
    		}
    		if (aObject instanceof AbstractCompositeControl) {
				List<? extends HTMLFragment> children = ((AbstractCompositeControl) aObject).getChildren();
				Object theFirst = CollectionUtil.getFirst(children);
    			if (theFirst instanceof ErrorControl) {
    				aObject = CollectionUtil.getSecond(children);
    				if (aObject == null) aObject = theFirst;
    			}
    			else {
    				aObject = theFirst;
    			}
    			done = true;
    		}
    		if (aObject instanceof CellObject) {
    			aObject = ((CellObject)aObject).getValue();
    			done = true;
    		}
    	}
    	while (done);
        return aObject;
    }



    /**
     * Gets the proper value from an object via accessor. This method can handle
     * controls and fields and asks for their values.
     *
     * @param aRow
     *            the RowObject
     * @param aColumn
     *            the ColumName
     * @param aAccessor
     *            the accessor to use to get the cell content.
     * @return the proper value of a cell
     */
    public static Object getProperValue(Object aRow, String aColumn, Accessor aAccessor) {
        return getProperValue(aAccessor.getValue(aRow, aColumn));
    }

    /**
     * Gets a string from an object via accessor. This method can handle
     * controls and fields and asks for their values.
     *
     * @param aRow
     *            the RowObject
     * @param aColumn
     *            the ColumName
     * @param aAccessor
     *            the accessor to use to get the cell content.
     * @return the value of a cell as a string
     */
    public static String getStringValue(Object aRow, String aColumn, Accessor aAccessor) {
        return getStringValue(aAccessor.getValue(aRow, aColumn));
    }

    /**
     * Gets a Double from an object via accessor. This method can handle
     * controls and fields and asks for their values.
     *
     * @param aRow
     *            the RowObject
     * @param aColumn
     *            the ColumName
     * @param aAccessor
     *            the accessor to use to get the cell content.
     * @return the value of a cell as a Double
     */
    public static Double getDoubleValue(Object aRow, String aColumn, Accessor aAccessor) {
        return getDoubleValue(aAccessor.getValue(aRow, aColumn));
    }

    /**
     * Gets a double from an object via accessor. This method can handle
     * controls and fields and asks for their values.
     *
     * @param aRow
     *            the RowObject
     * @param aColumn
     *            the ColumName
     * @param aAccessor
     *            the accessor to use to get the cell content.
     * @return the value of a cell as a double
     */
    public static double getdoubleValue(Object aRow, String aColumn, Accessor aAccessor) {
        return getdoubleValue(aAccessor.getValue(aRow, aColumn));
    }

    /**
     * Gets an Integer from an object via accessor. This method can handle
     * controls and fields and asks for their values.
     *
     * @param aRow
     *            the RowObject
     * @param aColumn
     *            the ColumName
     * @param aAccessor
     *            the accessor to use to get the cell content.
     * @return the value of a cell as an Integer
     */
    public static Integer getIntegerValue(Object aRow, String aColumn, Accessor aAccessor) {
        return getIntegerValue(aAccessor.getValue(aRow, aColumn));
    }

    /**
     * Gets an int from an object via accessor. This method can handle
     * controls and fields and asks for their values.
     *
     * @param aRow
     *            the RowObject
     * @param aColumn
     *            the ColumName
     * @param aAccessor
     *            the accessor to use to get the cell content.
     * @return the value of a cell as an int
     */
    public static double getintValue(Object aRow, String aColumn, Accessor aAccessor) {
        return getintValue(aAccessor.getValue(aRow, aColumn));
    }

    /**
     * Gets a Long from an object via accessor. This method can handle
     * controls and fields and asks for their values.
     *
     * @param aRow
     *            the RowObject
     * @param aColumn
     *            the ColumName
     * @param aAccessor
     *            the accessor to use to get the cell content.
     * @return the value of a cell as a Long
     */
    public static Long getLongValue(Object aRow, String aColumn, Accessor aAccessor) {
        return getLongValue(aAccessor.getValue(aRow, aColumn));
    }

    /**
     * Gets a long from an object via accessor. This method can handle
     * controls and fields and asks for their values.
     *
     * @param aRow
     *            the RowObject
     * @param aColumn
     *            the ColumName
     * @param aAccessor
     *            the accessor to use to get the cell content.
     * @return the value of a cell as a long
     */
    public static double getlongValue(Object aRow, String aColumn, Accessor aAccessor) {
        return getlongValue(aAccessor.getValue(aRow, aColumn));
    }

    /**
     * Gets a Boolean from an object via accessor. This method can handle
     * controls and fields and asks for their values.
     *
     * @param aRow
     *            the RowObject
     * @param aColumn
     *            the ColumName
     * @param aAccessor
     *            the accessor to use to get the cell content.
     * @return the value of a cell as a Boolean
     */
    public static Boolean getBooleanValue(Object aRow, String aColumn, Accessor aAccessor) {
        return getBooleanValue(aAccessor.getValue(aRow, aColumn));
    }

    /**
     * Gets a boolean from an object via accessor. This method can handle
     * controls and fields and asks for their values.
     *
     * @param aRow
     *            the RowObject
     * @param aColumn
     *            the ColumName
     * @param aAccessor
     *            the accessor to use to get the cell content.
     * @return the value of a cell as a boolean
     */
    public static boolean getbooleanValue(Object aRow, String aColumn, Accessor aAccessor) {
        return getbooleanValue(aAccessor.getValue(aRow, aColumn));
    }

    /**
     * Gets a Date from an object via accessor. This method can handle
     * controls and fields and asks for their values.
     *
     * @param aRow
     *            the RowObject
     * @param aColumn
     *            the ColumName
     * @param aAccessor
     *            the accessor to use to get the cell content.
     * @return the value of a cell as a Date
     */
    public static Date getDateValue(Object aRow, String aColumn, Accessor aAccessor) {
        return getDateValue(aAccessor.getValue(aRow, aColumn));
    }

    /**
     * Gets the proper value from an object via accessor. This method can handle controls
     * and fields and asks for their values.In addition to
     * {@link #getProperValue(Object, String, Accessor)}, the first element is extracted,
     * if the value is a collection of objects. Useful for getting the selected element of a
     * single selection field.
     *
     * @param aRow
     *            the RowObject
     * @param aColumn
     *            the ColumName
     * @param aAccessor
     *            the accessor to use to get the cell content.
     * @return the proper value of a cell
     */
    public static Object getSingleProperValue(Object aRow, String aColumn, Accessor aAccessor) {
        return getSingleProperValue(aAccessor.getValue(aRow, aColumn));
    }

    /**
     * Gets the form field from an object via accessor. This method can handle collections,
     * controls and fields.
     *
     * @param aRow
     *            the RowObject
     * @param aColumn
     *            the ColumName
     * @param aAccessor
     *            the accessor to use to get the cell content.
     * @return the form field of a cell
     */
    public static FormField getField(Object aRow, String aColumn, Accessor aAccessor) {
        return getField(aAccessor.getValue(aRow, aColumn));
    }

    /**
     * Gets the control from an object via accessor. This method can handle collections as
     * well.
     *
     * @param aRow
     *        the RowObject
     * @param aColumn
     *        the ColumName
     * @param aAccessor
     *        the accessor to use to get the cell content.
     * @return the control of a cell
     */
    public static Control getControlValue(Object aRow, String aColumn, Accessor aAccessor) {
        return getControlValue(aAccessor.getValue(aRow, aColumn));
    }


    /**
     * This methods adds a {@link ValueListener} to some {@link FormField}, and
     * triggers the listener with the current value of the field as oldValue as
     * well as newValue.
     *
     * @param field
     *        some {@link FormField} must not be null and must
     *        {@link FormField#hasValue() have a value}.
     * @param dependency
     *        some {@link ValueListener} to add to the field.
     */
    public static void initDependency(FormField field, ValueListener dependency) {
        field.addValueListener(dependency);
        dependency.valueChanged(field, field.getValue(), field.getValue());
    }

	/**
	 * {@link Mapping} of {@link FormField}s and {@link FormMemberControl}s to their value. (see
	 * {@link FormFieldHelper#getSingleProperValue(Object)})
	 * 
	 * @author <a href="mailto:jes@top-logic.com">jes</a>
	 */
	public static class SingleProperValueMapping implements Mapping<Object, Object> {

		/**
		 * Singleton {@link SingleProperValueMapping} instance.
		 */
		public static final SingleProperValueMapping INSTANCE = new SingleProperValueMapping();

		private SingleProperValueMapping() {
			// Singleton constructor.
		}

		@Override
		public Object map(Object input) {
			return FormFieldHelper.getSingleProperValue(input);
		}

	}

}
