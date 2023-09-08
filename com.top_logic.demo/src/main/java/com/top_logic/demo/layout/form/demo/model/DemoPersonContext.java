/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.demo.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.top_logic.demo.layout.form.demo.DemoOrderForm;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.constraints.AbstractConstraint;
import com.top_logic.layout.form.constraints.OneFilledFieldDependency;
import com.top_logic.layout.form.constraints.SelectionSizeConstraint;
import com.top_logic.layout.form.constraints.StringLengthConstraint;
import com.top_logic.layout.form.format.MailAddressFormat;
import com.top_logic.layout.form.format.StringTokenFormat;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.util.Resources;

/**
 * A {@link FormContext} that represents a person (for the AJAX-enabled form
 * demo {@link DemoOrderForm}).
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DemoPersonContext extends FormContext {
    
    /** The name of the {@link #mailAddresses} member. */
    public static final String MAIL_ADDRESSES = "mailAddresses";
    /** The name of the {@link #mailAddress1} member. */
    public static final String MAIL_ADDRESS_1 = "mailAddress1";
    /** The name of the {@link #mailAddress2} member. */
    public static final String MAIL_ADDRESS_2 = "mailAddress2";
    /** The name of the {@link #mailAddress3} member. */
    public static final String MAIL_ADDRESS_3 = "mailAddress3";

    /** The options for the {@link #title} {@link SelectField}. */
    private static final List TITLES = Arrays.asList(new String[] 
      { "Prof.", "Dr. h.c.", "Dr. ing.", "Dr. med.", "Dr. phil.", "Dr. rer. nat."});

    /** The list of options for the {@link #gender} {@link SelectField}. */
    private static final List GENDERS = Arrays.asList(new String[] 
      { "Frau", "Herr"} );

	public final SelectField title = 
		FormFactory.newSelectField("title", TITLES, true, SelectField.NO_SELECTION, false, false, new SelectionSizeConstraint(-1, 3));

	public final SelectField gender = 
		FormFactory.newSelectField("gender", GENDERS, !SelectField.MULTIPLE, SelectField.NO_SELECTION, true, false, new SelectionSizeConstraint(1, 1));
	
	public final SelectField title2 = 
		FormFactory.newSelectField("title2", TITLES, true, SelectField.NO_SELECTION, false, false, new SelectionSizeConstraint(-1, 3));
	
	public final SelectField gender2 = 
		FormFactory.newSelectField("gender2", GENDERS, !SelectField.MULTIPLE, SelectField.NO_SELECTION, true, false, new SelectionSizeConstraint(1, 1));
	
	public final StringField surname = FormFactory.newStringField("surname");
	{
		surname.addConstraint(new StartsWithUppercaseLetterConstraint("surname"));
		surname.addConstraint(new StringLengthConstraint(-1, 15));
	}
	
	public final StringField givenName = FormFactory.newStringField("givenName");
	{
		givenName.addConstraint(new StringLengthConstraint(-1, 12));
        givenName.addConstraint(new StartsWithUppercaseLetterConstraint("given name"));
	}
    
    public final ComplexField dateOfBirth = FormFactory.newDateField("dateOfBirth", null, false);

    public ComplexField mailAddresses = FormFactory.newComplexField(MAIL_ADDRESSES, new StringTokenFormat(new MailAddressFormat(), ",; ", null, true));
    
    public ComplexField mailAddress1  = FormFactory.newComplexField(MAIL_ADDRESS_1, new MailAddressFormat());
    
    public ComplexField mailAddress2  = FormFactory.newComplexField(MAIL_ADDRESS_2, new MailAddressFormat());
    
    public ComplexField mailAddress3  = FormFactory.newComplexField(MAIL_ADDRESS_3, new MailAddressFormat());
    
    
    public DemoPersonContext(String name) {
		this(name, ResPrefix.legacyClass(DemoPersonContext.class));
    }
	
	public DemoPersonContext(String name, ResPrefix i18nPrefix) {
        super(name, i18nPrefix);
        
        MailAddressComposer listener = new MailAddressComposer(mailAddresses);
        mailAddress1.addValueListener(listener);
        mailAddress2.addValueListener(listener);
        mailAddress3.addValueListener(listener);
        
        FormField[] fields = new FormField[] {mailAddress1, mailAddress2, mailAddress3};
        OneFilledFieldDependency dependency = new OneFilledFieldDependency(fields);
        dependency.attach();
        
        addMembers(new FormMember[] { 
        	title, gender, title2, gender2, surname, givenName, dateOfBirth,
            mailAddresses, mailAddress1, mailAddress2, mailAddress3});
    }
    
    public void loadWith(DemoPerson person) {
    	// TODO BHU: missing title and gender.
        surname.setValue(person.getSurname()); 
        givenName.setValue(person.getGivenName()); 
    }
    
    public void saveIn(DemoPerson person) {
    	// TODO BHU: missing title and gender.
        person.surname   = surname.getAsString();
        person.givenName = givenName.getAsString();
    }

    private final static class StartsWithUppercaseLetterConstraint extends AbstractConstraint {
        String thingName;

        public StartsWithUppercaseLetterConstraint(String thingName) {
            this.thingName = thingName;
        }

        @Override
		public boolean check(Object value) throws CheckException {
            String stringValue = (String) value;
            int    theLength   = (stringValue == null) ? 0 : stringValue.length();

            if (theLength == 0) {
				throw new CheckException(
					Resources.getInstance().getString(I18NConstants.NOT_EMPTY__TARGET.fill(
						Character.toUpperCase(thingName.charAt(0)) + thingName.substring(1))));
            }
            
            if (! Character.isUpperCase(stringValue.charAt(0))) {
				throw new CheckException(
					Resources.getInstance().getString(I18NConstants.START_WITH_UPPERCASE_LETTER__TARGET.fill(
						Character.toUpperCase(thingName.charAt(0)) + thingName.substring(1))));
            }
            return true;
        }

    }

    /**
     * A MailAddressComposer listens on changes in a list of fields and modifies 
	 * the value of a dependant field accordingly. 
     * 
     * The MailAddressComposer adds new e-mail addresses to the intern field or
     * removes e-mail addresses from it.
     */
    private static class MailAddressComposer implements ValueListener {

        /** The complex field for e-mail addresses. */
        private ComplexField field;
        
        /**
         * Creates a {@link MailAddressComposer} with the given
         * parameters.
         * 
         * @param aField A {@link ComplexField} for e-mail addresses. Must NOT
         *        be <code>null</code>.
         */
        public MailAddressComposer(ComplexField aField) {
            this.field = aField;
        }
        
        /**
         * This method adds the new e-mail address to the intern e-mail field 
         * ({@link #field}) and removes the old e-mail address.
         * 
         * @param aField    The {@link FormField}.
         * @param aOldValue The old e-mail address from the field.
         * @param aNewValue The new e-mail address for the field.
         */
        @Override
		public void valueChanged(FormField aField, Object aOldValue, Object aNewValue) {
            List list = (List)field.getValue();
            if (list == null) {
                list = new ArrayList();
            }
            if (list.contains(aOldValue)) {
                list.remove(aOldValue);
                
            }
            if (aNewValue != null && !list.contains(aNewValue)) {
                list.add(aNewValue);
            }
            field.setValue(list);
        }
    }
    
}
