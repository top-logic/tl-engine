/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.base.imap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import jakarta.mail.Address;
import jakarta.mail.Message;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.util.Utils;
import com.top_logic.knowledge.objects.DestinationIterator;
import com.top_logic.knowledge.objects.InvalidLinkException;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.knowledge.wrap.AbstractContainerWrapper;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.mail.base.Mail;
import com.top_logic.mail.base.MailFactory;
import com.top_logic.mail.base.MailFolder;
import com.top_logic.mail.proxy.MailReceiverService;
import com.top_logic.mail.proxy.MailServerMessage;
import com.top_logic.mail.proxy.exchange.ExchangeMail;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.util.error.TopLogicException;


/**
 * Wrapper for IMAP based mails.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class IMAPMail extends AbstractContainerWrapper implements Mail {

	private static final AssociationSetQuery<KnowledgeAssociation> DOCUMENTS = AssociationQuery.createOutgoingQuery("documents", ATTACHED_DOCUMENTS_ASSOCIATION);

	private IMAPMailFolder _folder;

    /**
     * Construct an instance wrapped around the specified
     * {@link com.top_logic.knowledge.objects.KnowledgeObject}.
     *
     * This CTor is only for the WrapperFactory! <b>DO NEVER USE THIS
     * CONSTRUCTOR!</b> Use always the getInstance() method of the wrappers.
     *  
     * @param    ko        The KnowledgeObject, must never be <code>null</code>.
     */
    public IMAPMail(KnowledgeObject ko) {
        super(ko);
    }

    @Override
	public Collection<? extends Document> getContent() {
		return CollectionUtil.dynamicCastView(Document.class, this.resolveWrappers(DOCUMENTS));
    }

    @Override
    public String[] getFrom() {
    	return this.toArray(this.getString(ATTR_FROM));
    }
    
    @Override
    public String[] getTo() {
    	return this.toArray(this.getString(ATTR_TO));
    }
    
    @Override
	public String[] getCC() {
    	return this.toArray(this.getString(ATTR_CC));
    }
    
    @Override
	public String[] getBCC() {
    	return this.toArray(this.getString(ATTR_BCC));
    }

    @Override
	public Date getSentDate() {
    	return this.getDate(ATTR_SENT_DATE);
    }

    @Override
	public String getMailID() {
		return this.getString(Mail.MAIL_ID);
    }

    @Override
	public String getMessage() {
        try {
			ExchangeMail exchangeMail = new ExchangeMail(this.getOriginalMail());

			return exchangeMail.getBodyContent();
        }
        catch (Exception ex) {
            return ("**** error in reading mail ****");
        }
    }

    @Override
	public Collection<Document> getAttachements() {
        try {
			List<Document> attachments = new ArrayList<>();

			DestinationIterator destinationIterator = new DestinationIterator(
				this.tHandle(), Mail.ATTACHED_DOCUMENTS_ASSOCIATION);
			while (destinationIterator.hasNext()) {
				Document theDoc = (Document) WrapperFactory.getWrapper(destinationIterator.nextKO());

				attachments.add(theDoc);
			}

			return attachments;
		} catch (Exception ex) {
			Logger.error("Unable to get attachements from " + this, ex, IMAPMail.class);

            return Collections.emptyList();
        }
    }

    @Override
	public boolean hasAttachements() {
		return Utils.isTrue((Boolean) this.getValue(Mail.HAS_ATTACHEMENT));
    }

    @Override
	public MailFolder getFolder() {
		if (this._folder == null) {
            try {
                Iterator<KnowledgeAssociation> theIt = this.tHandle().getIncomingAssociations(MailFolder.CONTENTS_ASSOCIATION);
                KnowledgeAssociation theAss = theIt.hasNext() ? theIt.next() : null;

                if (theAss != null) {
					this._folder = IMAPMailFolder.getInstance(theAss.getSourceObject());
                }
            }
			catch (InvalidLinkException ex) {
				throw new TopLogicException(I18NConstants.ERROR_MAIL_FOLDER_GET, ex);
            }
        }

		return this._folder;
    }

	@Override
	public void setAddress(String aKey, Address[] someAddresses) {
		this.setValue(aKey, MailFactory.toString(someAddresses));
	}

    @Override
	protected boolean _add(TLObject newChild) {
        return false;
    }

    @Override
	public void clear() {
		KBUtils.deleteAll(this.getContent());
    }
    
    @Override
	protected boolean _remove(TLObject oldChild) {
        return false;
    }

    /** 
     * the MailFolder as security relevant object.
     */
    @Override
	public BoundObject getSecurityParent() {
    	MailFolder theFolder = this.getFolder();

    	return (theFolder instanceof BoundObject) ? (BoundObject) theFolder : null;
    }

	/**
	 * Check, if the given mail has a value for BCC, because this information is located in the
	 * original mail.
	 * 
	 * @return <code>true</code> when BCC has been set in original mail.
	 */
	public boolean hasBCC() {
		return this.getBooleanValue(ATTR_BCC);
	}

    /** 
     * Return the mails content by asking the mail server.
     * 
     * @return    The requested content of the mail.
     * @throws    TopLogicException   When accessing the mails fails.
     */
    public Object getMailContent() throws TopLogicException {
        try {
            return this.getOriginalMail().getContent();
        }
        catch (Exception ex) {
			throw new TopLogicException(I18NConstants.ERROR_MAIL_CONTENT_GET, ex);
        }
    }

    /** 
     * Return the original mail message from the mail server.
     * 
     * @return    The requested mail message.
     */
    public Message getOriginalMail() {
        Message theMessage = null;

        try {
            MailFolder theFolder = this.getFolder();
            String     theID     = this.getMailID();

			for (MailServerMessage theMail : MailReceiverService.getMailReceiverInstance().getContent(theFolder.getName())) {
                if (theID.equals(theMail.getID())) {
                    theMessage = theMail.getMessage();
                }
            }
        }
        catch (Exception ex) {
			Logger.error("Unable to get original mail for " + this, ex, IMAPMail.class);
        }

        return theMessage;
    }

    private String[] toArray(String aString) {
		return MailFactory.toArray(aString);
    }
}
