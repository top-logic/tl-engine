/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.layout;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.contact.business.AddressHolder;
import com.top_logic.contact.business.CompanyContact;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.knowledge.gui.WrapperResourceProvider;


/**
 * Generate correct labels for
 * {@link com.top_logic.contact.business.PersonContact person contacts} and for
 * {@link com.top_logic.contact.business.CompanyContact company contacts}.
 *
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class ContactResourceProvider extends WrapperResourceProvider
		implements ConfiguredInstance<ContactResourceProvider.Config> {

	/**
	 * Configuration for the {@link ContactResourceProvider}.
	 *
	 * @author <a href=mailto:tri@top-logic.com>tri</a>
	 */
	public interface Config extends PolymorphicConfiguration<ContactResourceProvider> {

		/**
		 * If <code>true</code>, {@link PersonContact contacts} that belong to an account get
		 * {@link #getMarkerString()} as label suffix.
		 */
		@BooleanDefault(false)
		boolean getMarkAccount();

		/**
		 * Setter for {@link #getMarkAccount()}.
		 */
		void setMarkAccount(boolean flag);

		/**
		 * If <code>true</code>, labels for contacts without account will be marked instead.
		 * 
		 * <p>
		 * This setting is only used when {@link #getMarkAccount() accounts are marked}.
		 * </p>
		 * 
		 * @see #getMarkAccount()
		 */
		@BooleanDefault(false)
		boolean getInverse();

		/**
		 * Setter for {@link #getInverse()}.
		 */
		void setInverse(boolean inv);

		/**
		 * The marker string to be used as label suffix.
		 * 
		 * @see #getMarkAccount()
		 */
		@StringDefault(" *")
		String getMarkerString();
	}

	private final Config _config;

	/**
	 * Create a {@link ContactResourceProvider}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public ContactResourceProvider(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	protected ResKey getTooltipNonNull(Object object) {
		if (object instanceof PersonContact) {
			return getTooltipForPerson((PersonContact) object);
        }
		else if (object instanceof CompanyContact) {
			return getTooltipForCompany((CompanyContact) object);
        }
		return super.getTooltipNonNull(object);
	}

    /** 
     * Return the tool tip Text for a CompanyContact.
     */
	protected ResKey getTooltipForCompany(CompanyContact aCompany) {
		String theText = quote(aCompany.getName());
		String theKey = quote((String) aCompany.getValue(CompanyContact.FKEY2_ATTRIBUTE));

		return I18NConstants.COMPANY_TOOLTIP__NAME_KEY.fill(theText, theKey);
    }

    /** 
     * Return the tool tip Text for a PersonContact.
     */
	protected ResKey getTooltipForPerson(PersonContact aPersonContact) {
		String theText = quote(this.getLabel(aPersonContact));
		String theMail = quote((String) aPersonContact.getValue(PersonContact.EMAIL));
		String thePhone = quote((String) aPersonContact.getValue(PersonContact.PHONE));

		return I18NConstants.PERSON_TOOLTIP__NAME_MAIL_PHONE.fill(theText, theMail, thePhone);
    }

    @Override
	public String getLabel(Object anObject) {
        if (anObject instanceof PersonContact) {
            String theLabel = getLabelForPerson((PersonContact) anObject);
			if (getConfig().getMarkAccount()) {
				String marker = getConfig().getMarkerString();
				if (!getConfig().getInverse()) {
					if (((PersonContact) anObject).getPerson() != null)
						theLabel += marker;
				} else {
					if (((PersonContact) anObject).getPerson() == null)
						theLabel += marker;
				}
            }
            return theLabel;
        }
        if (anObject instanceof CompanyContact) {
            return getLabelForCompany((CompanyContact) anObject);
        }
        return super.getLabel(anObject);
    }

	/** 
     * Return the Label for a CompanyContact.
     */
    protected String getLabelForCompany(CompanyContact aCompany) {
        try {
            return aCompany.getName() + " (" + aCompany.getValue(AddressHolder.CITY) + ")";
        }
        catch (Exception ex) {
            return ex.getMessage();
        }
    }

    /** 
     * Return the Label for a PersonContact.
     */
    protected String getLabelForPerson(PersonContact aPersonContact) {
        return aPersonContact.getValue(PersonContact.NAME_ATTRIBUTE) + ", " + aPersonContact.getValue(PersonContact.FIRST_NAME);
    }

}
