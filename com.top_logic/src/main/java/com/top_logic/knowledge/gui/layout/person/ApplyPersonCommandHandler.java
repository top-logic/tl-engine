/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.person;

import static com.top_logic.basic.shared.string.StringServicesShared.*;

import java.util.Locale;
import java.util.TimeZone;

import com.top_logic.base.security.device.SecurityDeviceFactory;
import com.top_logic.base.security.device.interfaces.PersonDataAccessDevice;
import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.event.ModelTrackingService;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.gui.MultiThemeFactory;
import com.top_logic.gui.Theme;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.knowledge.wrap.person.PersonalConfigurationWrapper;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.TLContext;
import com.top_logic.util.Utils;
import com.top_logic.util.error.TopLogicException;

/**
 * Store the changes made to a person.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
@Deprecated
public class ApplyPersonCommandHandler extends AbstractApplyCommandHandler {

    /** ID of this handler. */
    public static final String COMMAND_ID = "applyPerson";

    public ApplyPersonCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

    protected boolean updataAuthenticationDeviceID(Person aPerson, String newDeviceID, FormContext aContext){
    	try{
    	    aPerson.setAuthenticationDeviceID(newDeviceID);
    	    return true;
    	} catch(Exception e) {
			throw new TopLogicException(ApplyPersonCommandHandler.class, "update.authenticationdevice", e);
    	}
    }

    @Override
	protected boolean storeChanges(LayoutComponent aComponent, FormContext aContext, Object aModel) {
        boolean hasSuccess = (aModel instanceof Person) && aContext.checkAll();

        if (hasSuccess) {
            Person  thePerson               = (Person) aModel;
            String  thePersonsDadID         = thePerson.getDataAccessDeviceID();
            String  newDataAccessDevice     = (String) ((SelectField)aContext.getField(Person.DATA_ACCESS_DEVICE_ID)).getSingleSelection();
            String  newAuthenticationDevice = (String) ((SelectField)aContext.getField(Person.AUTHENTICATION_DEVICE_ID)).getSingleSelection();
            PersonDataAccessDevice theDevice = SecurityDeviceFactory.getPersonAccessDevice(thePersonsDadID);

			if (!thePersonsDadID.equals(newDataAccessDevice)) {
				throw new UnsupportedOperationException("Cannot change device.");
			}
            this.updataAuthenticationDeviceID(thePerson,newAuthenticationDevice,aContext);
            // Store changes to person 
            this.updateLocale(thePerson,
                              (String) ((SelectField)aContext.getField(EditPersonComponent.LANGUAGE)).getSingleSelection(),
                              (String) ((SelectField)aContext.getField(EditPersonComponent.COUNTRY)).getSingleSelection());
			SelectField timeZoneField = (SelectField) aContext.getField(EditPersonComponent.TIME_ZONE);
			if (timeZoneField.isChanged()) {
				thePerson.setTimeZone((TimeZone) timeZoneField.getSingleSelection());
			}
            this.updateNonUserFields(thePerson, aContext);       

            if(theDevice != null && !theDevice.isReadOnly()){

                // Store insecure changes to model 
                this.updateUserFields(thePerson, aContext);       
                this.updateAdminFields(thePerson, aContext);

                // Store admin-accessible changes to model
				if (TLContext.isAdmin()) {
                    this.updateRoles(thePerson, aContext);
                }
    
                try {
                    if(!SecurityDeviceFactory.getPersonAccessDevice(thePerson.getDataAccessDeviceID()).isReadOnly()){
                        thePerson.updateUserData();
                    }
                }
                catch (Exception ex) {
					throw new TopLogicException(ApplyPersonCommandHandler.class, "update.userdata", ex);
                }
            }

            if (hasSuccess) {
                sendEvent(thePerson);
            }
        }
        return (hasSuccess);
    }
    
    /**
     * Hook for subclasses. This method sends a MonitorEvent on the ModelTrackingService.
     * Overwrite this method if you want to deactivate or send special MonitorEvents.
     * Use this method if you want to generate MonitorEvents outside a GUI-Context.
     * MonitorEvents are generated by default. 
     * 
     * @param aPerson the changed person
     */
    protected void sendEvent(Person aPerson) {
       ModelTrackingService.sendModifyEvent(aPerson, TLContext.getContext().getCurrentPersonWrapper());
    }

    /**
     * Update the Locale for the current Person.
     * 
     * @param    aPerson      The person to set the locale to, must not be <code>null</code>.
     * @param    aLanguage    The new language, may be <code>null</code> or empty.
     * @param    aCountry     The new country, may be <code>null</code> or empty.
     */
	public void updateLocale(Person aPerson, String aLanguage, String aCountry) {
		aPerson.setLocale(new Locale(nonNull(aLanguage), nonNull(aCountry)));
    }

    /**
     * Update the roles of the user based on the given FormContext.
     * 
     * @param    aPerson     The person to change the roles for.
     * @param    aContext    To fetch the <code>roleString</code> from.
     */
	protected void updateRoles(Person aPerson, FormContext aContext) {
		if (!TLContext.isAdmin()) {
			return;
		}

		if (aContext.hasMember(EditPersonComponent.SUPER_USER_FIELD)) {
			FormField theField = aContext.getField(EditPersonComponent.SUPER_USER_FIELD);
            if (theField.isChanged()) {
				boolean isSuperUser = ((Boolean) theField.getValue()).booleanValue();
            	
				aPerson.setAdmin(isSuperUser);
            }
        }
    }

    /**
	 * Update the admin fields of the given person.
	 * 
	 * The admin fields are {@link UserInterface#FIRST_NAME}, {@link UserInterface#NAME},
	 * {@link UserInterface#TITLE}.
	 * 
	 * @param aPerson
	 *        The person to be changed.
	 * @param aContext
	 *        The form context containing the values to be set.
	 */
	protected void updateAdminFields(Person aPerson, FormContext aContext) {
		UserInterface user = aPerson.getUser();
		user.setName((String) this.getChangedValue(aContext, UserInterface.FIRST_NAME));
		user.setFirstName((String) this.getChangedValue(aContext, UserInterface.NAME));
		user.setTitle((String) this.getChangedValue(aContext, UserInterface.TITLE));
    }

    /**
     * Update the fields that a user may change for himself.    
     * 
     * @param    aPerson     The person to be changed.
     * @param    aContext    The form context containing the values to be set.
     */
	protected void updateUserFields(Person aPerson, FormContext aContext) {
		UserInterface user = aPerson.getUser();
		user.setPhone((String) this.getChangedValue(aContext, UserInterface.PHONE));
		user.setEMail((String) this.getChangedValue(aContext, UserInterface.EMAIL));
    }
    
    /**
     * Update the fields that are independend of the data access device.    
     * 
     * @param    aPerson     The person to be changed.
     * @param    aContext    The form context containing the values to be set.
     */
	protected void updateNonUserFields(Person aPerson, FormContext aContext) {
        Boolean changedValue = (Boolean) getChangedValue(aContext, Person.RESTRICTED_USER);
        if (changedValue != null) {
            aPerson.setRestrictedUser(changedValue);
        }
        SelectField themeSelector = (SelectField) aContext.getField(EditPersonComponent.THEME_SELECTOR);
		Theme theme = (Theme) themeSelector.getSingleSelection();
		if (themeSelector.isChanged() && theme != null) {
			String themeID = theme.getThemeID();
			PersonalConfigurationWrapper pcw = PersonalConfigurationWrapper.createPersonalConfiguration(aPerson);
			MultiThemeFactory.setPersonalThemeId(pcw, themeID);
			TLContext currentContext = TLContext.getContext();
			if (Utils.equals(aPerson, currentContext.getCurrentPersonWrapper())) {
				PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
				MultiThemeFactory.setPersonalThemeId(pc, themeID);

				showChangedThemeInfo(theme);

			}
		}
        
    }

	/**
	 * Show an info box to the user that the theme has been changed and a new login is necessary.
	 * 
	 * @param theme
	 *        The new theme of the user.
	 */
	protected void showChangedThemeInfo(Theme theme) {
		String themeString = MetaLabelProvider.INSTANCE.getLabel(theme);
		InfoService.showInfo(I18NConstants.CHANGED_THEME_RELOGIN_NECESSARY__THEME.fill(themeString));
	}

}
