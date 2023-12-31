/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.person;

import java.util.ArrayList;
import java.util.Collection;

import com.top_logic.base.security.attributes.PersonAttributes;
import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.security.device.db.DBUserRepository;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.MOStructureImpl;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.knowledge.wrap.person.infouser.InfoUserConstraint;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.component.AbstractCreateComponent;
import com.top_logic.layout.form.constraints.SelectionSizeConstraint;
import com.top_logic.layout.form.constraints.StringLengthConstraint;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.util.Resources;


/**
 * Create, edit, and delete Persons
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class NewPersonComponent extends AbstractCreateComponent {

	/**
	 * Configuration of a {@link NewPersonComponent}
	 * 
	 * @since 5.7.5
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractCreateComponent.Config {

		@FormattedDefault("null()")
		@Override
		public ModelSpec getModelSpec();

		@StringDefault(NewPersonCommandHandler.COMMAND_ID)
		@Override
		String getCreateHandler();

	}

	final PersonManager pManager = PersonManager.getManager();

	private final MOStructureImpl _userMO;

    public NewPersonComponent(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
		MOStructureImpl userMetaObject;
		try {
			userMetaObject = DBUserRepository.getUserMetaObject(PersistencyLayer.getKnowledgeBase());
		} catch (UnknownTypeException ex) {
			context.error("Unable to get meta object for user data.", ex);
			userMetaObject = null;
		}
		_userMO = userMetaObject;
    }

    @Override
	protected boolean supportsInternalModel(Object anObject) {
		return anObject == null;
    }

    @Override
	public FormContext createFormContext() {
        FormContext formContext = new FormContext("NewPersonFormContext", getResPrefix());
		StringField userField = newStringField(PersonAttributes.USER_NAME, 1, _userMO);
        userField.setMandatory(true);
        userField.addConstraint(new Constraint() {
            @Override
			public boolean check(Object aValue) throws CheckException {
                if (!pManager.validatePersonName((String) aValue)) {
					throw new CheckException(Resources.getInstance().getString(getResPrefix().key("error.invalidName")));
                }
                
                return true;
            }
        
        });
        userField.addConstraint(new Constraint() {
            @Override
			public boolean check(Object aValue) throws CheckException {

				if (pManager.personNameAlreadyUsed((String) aValue)) {
					throw new CheckException(Resources.getInstance().getString(getResPrefix().key("error.exists")));
                }
                
                return true;
            }
        
        });
        
        formContext.addMember(userField);
		StringField nameField = newStringField(PersonAttributes.GIVEN_NAME, 0, _userMO);
		nameField.setMandatory(true);
		formContext.addMember(nameField);
		StringField surField = newStringField(PersonAttributes.SUR_NAME, 1, _userMO);
        surField.setMandatory(true);
        formContext.addMember(surField);
		formContext.addMember(newStringField(PersonAttributes.TITLE, 0, _userMO));
		formContext.addMember(newBooleanField(PersonAttributes.RESTRICTED_USER));

		Collection<?> allWritableDataDevices = TLSecurityDeviceManager.getInstance().getWritableSecurityDeviceIDs();
        SelectField dataDevicesField = FormFactory.newSelectField(PersonAttributes.DATA_ACCESS_DEVICE_ID, new ArrayList(allWritableDataDevices), false, true, false, new SelectionSizeConstraint(1, 1));
        formContext.addMember(dataDevicesField);
	    if (allWritableDataDevices.size() == 1) {
	        dataDevicesField.setAsSingleSelection(allWritableDataDevices.iterator().next());
	        dataDevicesField.setVisible(false);
	    }
		return formContext;
    }

	private StringField newStringField(String personAttribute, int minLength, MOStructureImpl personType) {
		int maxLength = EditPersonComponent.getSizeForMOAttribute(personType, personAttribute, false);
		StringLengthConstraint constraint = new StringLengthConstraint(minLength, maxLength);
		return FormFactory.newStringField(personAttribute, "", false, false, constraint);
	}

	private BooleanField newBooleanField(String personAttribute) {
		return FormFactory.newBooleanField(personAttribute, false, false, false, InfoUserConstraint.INSTANCE);
	}

}
