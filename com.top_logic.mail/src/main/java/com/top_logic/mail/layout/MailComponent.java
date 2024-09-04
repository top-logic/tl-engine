/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.layout;

import java.util.Arrays;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.DataField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.mail.base.Mail;

/**
 * Display component for a single mail.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class MailComponent extends FormComponent {

	/** The name of the mail. */
    public static final String FIELD_NAME = "name";

    /** The sent date of the mail. */
    public static final String FIELD_SEND_DATE = "sendDate";

    /** The sender of the mail. */
    public static final String FIELD_FROM = "from";

    /** The receiver of the mail. */
    public static final String FIELD_TO = "to";

    /** The CCC of the mail. */
    public static final String FIELD_CC = "cc";

    /** The attachments of the mail. */
    public static final String FIELD_ATTACHMENTS = "attachments";

    /** 
     * Create a new instance of this class.
     */
    public MailComponent(InstantiationContext context, Config someAttrs) throws ConfigurationException {
        super(context, someAttrs);
    }

    @Override
	public FormContext createFormContext() {
    	FormContext theContext = new FormContext(this);
        Mail        theMail    = (Mail) this.getModel();

        if (theMail != null) {
        	FormGroup theGroup = new FormGroup(FIELD_ATTACHMENTS, this.getResPrefix());

        	theContext.addMember(this.createNameField(theMail));
	        theContext.addMember(this.createSentDateField(theMail));
	        theContext.addMember(this.createAddressField(FIELD_FROM, theMail.getFrom()));
	        theContext.addMember(this.createAddressField(FIELD_TO, theMail.getTo()));
	        theContext.addMember(this.createAddressField(FIELD_CC, theMail.getCC()));
	        theContext.addMember(theGroup);

	        if (theMail.hasAttachments()) {
	        	for (Document theAttachment : theMail.getAttachments()) {
					theGroup.addMember(this.createAttachmentField(theAttachment));
	            }
	        }
        }

        return theContext;
    }
    
    @Override
	protected boolean supportsInternalModel(Object anObject) {
        return anObject instanceof Mail;
    }
    
	@Override
	protected void afterModelSet(Object oldModel, Object newModel) {
		super.afterModelSet(oldModel, newModel);
		this.removeFormContext();
    }

	private DataField createAttachmentField(Document anAttachment) {
		String    theName  = IdentifierUtil.toExternalForm(KBUtils.getWrappedObjectName(anAttachment)).replaceAll("[^\\w]", "");
		DataField theField = FormFactory.newDataField(theName);

		theField.initializeField(anAttachment);
		theField.setImmutable(true);

		return theField;
	}

	private StringField createNameField(Mail aMail) {
		return FormFactory.newStringField(FIELD_NAME, aMail.getName(), true);
	}

	private ComplexField createSentDateField(Mail aMail) {
		return FormFactory.newDateField(FIELD_SEND_DATE, aMail.getSentDate(), true);
	}

    private FormField createAddressField(String aName, String[] someAddresses) {
        StringField theField = FormFactory.newStringField(aName, true);

        if (someAddresses != null) {
            theField.setAsString(StringServices.join(Arrays.asList(someAddresses), ", "));
        }

        return theField;
    }
}
