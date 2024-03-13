/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.mail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Named;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.util.ResKey;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.contact.external.ExternalContact;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.dsa.util.DataAccessProxyDataSource;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.form.format.MailAddressFormat;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * The MailHelper provides useful methods for sending mails.
 * 
 * Instead of building up {@link Mail} objects yourself, you are encouraged to use this helper.
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class MailHelper extends ConfiguredManagedClass<ConfiguredManagedClass.Config<MailHelper>> {

    /** Attribute - subject of the e-mail */
	public static final String SUBJECT = "subject";

    /** Attribute - body of the e-mail */
	public static final String BODY = "body";

    /** Attribute - comma separated e-mail addresses */
	public static final String EMAIL_ADDRESSES = "eMailAddresses";
    
	/** Content type - text. */
	public static final String CONTENT_TYPE_TEXT = "text/plain";

	/** Content type - text UTF8. */
	public static final String CONTENT_TYPE_TEXT_UTF8 = "text/plain" + "; charset=utf-8";

	/** Content type - HTML. */
	public static final String CONTENT_TYPE_HTML = "text/html";

	/** Content type - HTML UTF8. */
	public static final String CONTENT_TYPE_HTML_UTF8 = CONTENT_TYPE_HTML + "; charset=utf-8";

	/**
	 * Creates a new {@link MailHelper}.
	 */
	public MailHelper(InstantiationContext context, Config<MailHelper> config) {
		super(context, config);
	}

	/**
	 * Provides the singleton instance of {@link MailHelper}.
	 */
    public static final MailHelper getInstance() {
        return Module.INSTANCE.getImplementationInstance();
    }
    
    /**
     * Send a mail. The sender address will be extracted from the current user
     * returned by {@link Mail#getCurrentUserAddress()}
     * 
     * @see #sendMail(Object, List, List, List, String, String, List, String)
     */
    public SendMailResult sendUserMail(List aTO, String aSubject, String aContent, String aContentType) {
        return this.sendUserMail(aTO, null, null, aSubject, aContent, null, aContentType); 
    }

    /**
     * Send a mail. The sender address will be extracted from the current user
     * returned by {@link Mail#getCurrentUserAddress()}
     * 
     * @see #sendMail(Object, List, List, List, String, String, List, String)
     */
    public SendMailResult sendUserMail(List aTO, List aCC, List aBCC, String aSubject, String aContent, List someDocs, String aContentType) {
        return this.sendMail(Mail.getCurrentUserAddress(), aTO, aCC, aBCC, aSubject, aContent, someDocs, aContentType);
    }

    /**
     * Send a mail. The system wide {@link MailSenderService#getFromAddress()} is used as sender.
     * 
     * @see #sendMail(Object, List, List, List, String, String, List, String)
     */
    public SendMailResult sendSystemMail(List aTO, String aSubject, String aContent, String aContentType) {
        return this.sendSystemMail(aTO, null, null, aSubject, aContent, null, aContentType); 
    }

    /**
     * Send a mail. The system wide {@link MailSenderService#getFromAddress()} is used as sender.
     * 
     * @see #sendMail(Object, List, List, List, String, String, List, String)
     */
    public SendMailResult sendSystemMail(List aTO, List aCC, List aBCC, String aSubject, String aContent, List someDocs, String aContentType) {
        return this.sendMail(MailSenderService.getFromAddress(), aTO, aCC, aBCC, aSubject, aContent, someDocs, aContentType);
    }
    
    /**
     * @see #sendMail(Object, List, List, List, String, String, List, String)
     */
    public SendMailResult sendMail(Object aSender, List aTO, String aSubject, String aContent, String aContentType) {
        return this.sendMail(aSender, aTO, null, null, aSubject, aContent, null, aContentType);
    }
    
    /**
     * Send a mail. Sender and receivers addesses are resolved using {@link #getEmailAddresses(Collection, List)}. 
     * 
     * @param aSender  the sender of the mail, must not be <code>null</code>
     * @param aTO      collection of receivers, must not be <code>null</code> or empty
     * @param aCC      collection of receivers of copies
     * @param aBCC     collection of receivers of blind copies
     * @param aSubject the subject of the mail, must not be <code>null</code>
     * @param aContent the body of the mail, must not be <code>null</code>
     * @param someDocs a list of {@link Document}, {@link DataAccessProxy} or {@link DataSource}
     * @param aContentType the content type, if <code>null</code> {@link #CONTENT_TYPE_TEXT} is used
     * @return {@link SendMailResult} that contains information about errors
     * 
     * TODO FSC: throw exception if mail format is not correct.
     */
    public SendMailResult sendMail(Object aSender, List aTO, List aCC, List aBCC, String aSubject, String aContent, List someDocs, String aContentType) {
		return sendMail(aSender, aTO, aCC, aBCC, null, aSubject, aContent, someDocs, aContentType);
    }
    
	/**
	 * Send a mail. Sender and receivers addresses are resolved using
	 * {@link #getEmailAddresses(Collection, List)}.
	 * 
	 * @param aSender
	 *        The sender of the mail. Must not be <code>null</code>.
	 * @param aTO
	 *        Collection of receivers. Must not be <code>null</code> or empty.
	 * @param aCC
	 *        Collection of receivers of copies. May be <code>null</code>.
	 * @param aBCC
	 *        Collection of receivers of blind copies. May be <code>null</code>.
	 * @param reply
	 *        Collection of reply addresses. May be <code>null</code>.
	 * @param aSubject
	 *        The subject of the mail. Must not be <code>null</code> or empty.
	 * @param aContent
	 *        The body of the mail. Must not be <code>null</code> or empty.
	 * @param someDocs
	 *        A list of {@link Document}, {@link DataAccessProxy} or {@link DataSource}.
	 * @param aContentType
	 *        The content type of the mail. If <code>null</code> {@link #CONTENT_TYPE_TEXT} is used
	 * 
	 * @return {@link SendMailResult} that contains information about errors
	 */
	public SendMailResult sendMail(Object aSender, List aTO, List aCC, List aBCC, List<?> reply, String aSubject,
			String aContent, List someDocs, String aContentType) {
        
        if ( ! MailSenderService.isConfigured()) {
			return SendMailResult.createErrorResult(I18NConstants.ERROR_NO_EMAIL_SUPPORT);
        }
            
		List<?> theInvalid = new ArrayList<>();
		List<?> theSender = this.getEmailAddresses(Collections.singleton(aSender), theInvalid);
        if (theSender.isEmpty() || ! theInvalid.isEmpty()) {
			return SendMailResult.createErrorResult(I18NConstants.ERROR_NO_SENDER);
        }
        
        List<String>  theTo      = this.getEmailAddresses(aTO,  theInvalid);
        List<String>  theCC      = this.getEmailAddresses(aCC,  theInvalid);
        List<String>  theBCC     = this.getEmailAddresses(aBCC, theInvalid);
		List<String> replyAddresses = getEmailAddresses(reply, theInvalid);
        
        if (theTo.isEmpty() && theCC.isEmpty() && theBCC.isEmpty()) {
			return SendMailResult.createErrorResult(I18NConstants.ERROR_NO_RECEIVER);
        }
        
        Mail theMail = new Mail(String.valueOf(CollectionUtil.getFirst(theSender)));
        
        theMail.setType(StringServices.isEmpty(aContent) ? CONTENT_TYPE_TEXT : aContentType);
        theMail.setTitle(aSubject);
        theMail.setContent(aContent);
        
        for (String theAddress : theTo) {
            theMail.addReceiver(theAddress);
        }
        
        for (String theAddress : theCC) {
            theMail.addCcReceiver(theAddress);
        }
        
        for (String theAddress : theBCC) {
            theMail.addBccReceiver(theAddress);
        }
        
		for (String replyAddress : replyAddresses) {
			theMail.addReplyToReceiver(replyAddress);
        }
        
        this.addAttachments(theMail, someDocs);
        
        return MailSenderService.getInstance().sendMail(theMail);
    }
    
    /**
     * Append documents from various sources to the mail
     */
    protected void addAttachments(Mail aMail, List someDocs) {
        if ((someDocs != null) && !someDocs.isEmpty()) {
			for (Iterator<?> theIt = someDocs.iterator(); theIt.hasNext();) {
                Object theObject = theIt.next();
                
                if (theObject instanceof Document) {
					DataAccessProxy theDAP = ((Document) theObject).getDAP();

					aMail.addAttachment(theDAP);
                }
                else if (theObject instanceof DataAccessProxy) {
                    aMail.addAttachment((DataAccessProxy) theObject);
                }
                else if (theObject instanceof DataSource) {
                    aMail.addAttachment((DataSource) theObject);
                }
                else if (theObject instanceof Wrapper) {
					{
						aMail.addAttachment(new DataAccessProxyDataSource(((Wrapper) theObject).getDAP(),
							((Named) theObject).getName()));
                    }
                }
            }
        }
    }
    
    /** 
     * Add email address to a receiver set of strings if not empty (after trimming).
     * 
     * @param anAddress the email address.
     * @param aRcvSet   the email address set. Must no be <code>null</code>
     * 
     * @return true when email was added (even when supressed as duplicate)
     */
    protected boolean addAddress(String anAddress, Set<String> aRcvSet) {
        if (this.checkAddressFormat(anAddress)) {
            aRcvSet.add(anAddress);
            return true;
        }
        return false; // was not added
    }
    
    /**
     * Get an email addres for an object
     */
    protected String internalGetEmailAddress(Object anObject) {
		if (anObject instanceof PersonContact) {
			return ((PersonContact) anObject).getEMail();
		} else if (anObject instanceof ExternalContact) {
			return ((ExternalContact) anObject).getEMail();
		} else if (anObject instanceof String) {
            return (String) anObject;
        }
        else if (anObject instanceof UserInterface) {
			return this.getEmailAddress(((UserInterface) anObject).getEMail());
        }
        else if (anObject instanceof Person) {
			Person account = (Person) anObject;
			UserInterface user = account.getUser();
			return user == null ? null : this.getEmailAddress(user.getEMail());
        }
        return null;
    }
    
    private final String getEmailAddress(Object anObject) {
        String theAddress = this.internalGetEmailAddress(anObject);
        return Mail.appendDomain(theAddress);
    }
    
    /**
     * This method returns whether the given e-mail address is valid.
     * 
     * @param  anAddress A e-mail address.
     * @return Returns <code>true</code> if the e-mail address is valid,
     *         <code>false</code> otherwise.
     */
    public final boolean checkAddressFormat(String anAddress) {
        return ! StringServices.isEmpty(anAddress)
               && MailAddressFormat.MAIL_ADDRESS_PATTERN.matcher(anAddress).matches();
    }
    
    /** 
     * Get the email addresses from the given receivers.
     * 
     * @param aRcvList  the receiver list. May contain email addresses (Strings), 
     *                  {@link com.top_logic.knowledge.wrap.person.Person}s, 
     *                  and {@link com.top_logic.tool.boundsec.wrap.Group}s
     * @param invalid   A List to store Strings describing invalid addresses
     * 
     * @return the receivers email address set. May be empty but not <code>null</code>
     */
    public final List<String> getEmailAddresses (Collection aRcvList, List invalid) {
        
        if (CollectionUtil.isEmptyOrNull(aRcvList)) {
            return Collections.EMPTY_LIST;
        }
        
        Set<String> theRcvSet = new LinkedHashSet<>(aRcvList.size());
		Iterator<?> theRcvIt = aRcvList.iterator();
        while (theRcvIt.hasNext()) {
            Object theRcv =  theRcvIt.next();
            if (theRcv != null) {
                try {
                    if (theRcv instanceof Group) {
                        Group theGroup = (Group) theRcv;
                        theRcvSet.addAll(this.getEmailAddresses(theGroup.getMembers(), invalid));
                    }
                    else {
                        String theAddress = this.getEmailAddress(theRcv);
                        if (! this.addAddress(theAddress, theRcvSet) && ! StringServices.isEmpty(theAddress)) {
                            invalid.add(theAddress);
                        }
                    }
                }
                catch (Exception ex) {
                    Logger.error("MailHelper - getEmailAddresses ", ex,
                            MailHelper.class);
                }
            }
        }
		return new ArrayList<>(theRcvSet);
    }

    public static String extractEmailAddress(Address anAddress) {
        String theString = anAddress.toString();
        
        // an email address may be formatted like 
        // "Hr. Mustermann <m.mustermann@example.com>"
        int theIdx = theString.indexOf("<");
        if (theIdx > -1) {
            int theEnd = theString.indexOf(">");
            if (theEnd < theIdx) {
                throw new IllegalArgumentException("Missmatch of '<' and '>' in " + theString);
            }
            
            theString = theString.substring(theIdx + 1, theEnd);
        }
        
        // remove all whitespaces
        theString = theString.replaceAll("\\s*", "");
        return theString;
    }
    
    /**
     * The SendMailResult provides information about the send process of a mail 
     * 
     * @author    <a href=mailto:fsc@top-logic.com>fsc</a>
     */
    public static class SendMailResult extends HandlerResult {
        
        private Mail    mail;
        private List<Address>    invalidAddresses;
        private Date    sendDate;
        
        /**
         * @see com.top_logic.tool.boundsec.HandlerResult#isSuccess()
         */
        @Override
		public boolean isSuccess() {
            return super.isSuccess() && this.mail != null && CollectionUtil.isEmptyOrNull(this.invalidAddresses);
        }
        
        /**
         * Set a list of {@link InternetAddress}es that have not retrieved the mail
         */
        public void setInvalidAddresses(List<Address> someAddresses) {
            this.invalidAddresses = someAddresses;
        }
        
        /**
         * Return a list of {@link InternetAddress}es that have not retrieved the mail
         */
        public List<Address> getInvalidAddresses() {
            return this.invalidAddresses;
        }

        /**
         * Get the {@link Mail} of this result
         * @return may be <code>null</code> if no {@link Mail} object was created
         */
        public Mail getMail() {
            return this.mail;
        }
        
		/**
		 * Set the date when this mail was send.
		 */
        public void setSendDate(Date aDate) {
            this.sendDate = aDate;
        }
        
        /**
         * Get the date when this mail was send
         */
        public Date getSendDate() {
            return this.sendDate;
        }
        
		/**
		 * Set the {@link Mail} of this result and add to the processed ones.
		 */
        protected void setMail(Mail aMail) {
            this.mail = aMail;
            this.addProcessed(aMail);
        }

        /**
		 * Log a detailed report of this result to {@link Logger}.
		 */
        public void logErrorResult(Object aCaller) {
            if (! this.isSuccess()) {
                StringBuffer theError = this.getErrorResultString();
                
                TopLogicException theEx = this.getException();
                if (theEx != null) {
                    Logger.error(theError.toString(), theEx.getCause(), aCaller);
                }
                else {
                    Logger.error(theError.toString(), aCaller);
                }
            }
        }
        
		/**
		 * Provides the {@link StringBuffer} containing error result.
		 */
        public StringBuffer getErrorResultString() {
            if (! this.isSuccess()) {
                Resources theRes = Resources.getInstance();
                StringBuffer theBuff = new StringBuffer(128);
                theBuff.append("Sending of mail");
                if (this.mail != null) {
                    theBuff.append(" '").append(this.mail.getTitle());
                }
				theBuff.append("' failed: ");

				List<ResKey> encodedErrors = this.getEncodedErrors();
				boolean hasErrorMessages = !encodedErrors.isEmpty();
				if (hasErrorMessages) {
					Iterator<ResKey> theIter = encodedErrors.iterator();
                    while (theIter.hasNext()) {
						ResKey encodedError = theIter.next();
						theBuff.append(theRes.decodeMessageFromKeyWithEncodedArguments(encodedError));
                        if (theIter.hasNext()) {
                            theBuff.append(", ");
                        }
                    }
                }

				if (this.getException() != null) {
					if (hasErrorMessages) {
						theBuff.append(", ");
					}
					theBuff.append(this.getException().getMessage());
				}
                
                if (this.getInvalidAddresses() != null) {
					if ((this.getException() != null) || hasErrorMessages) {
						theBuff.append("; ");
					}
					theBuff.append("Invalid Addresses: ");
                    theBuff.append(StringServices.join(this.getInvalidAddresses(), ", "));
                }
                return theBuff;
            }
            return new StringBuffer(1);
        }
        
		/**
		 * Creates an error-valued {@link SendMailResult} for the given {@link ResKey}.
		 */
		public static SendMailResult createErrorResult(ResKey anI18nErrorKey) {
            SendMailResult theResult = new SendMailResult();
            theResult.addErrorMessage(anI18nErrorKey);
            return theResult;
        }
        
		/**
		 * Creates an error-valued {@link SendMailResult} for the given {@link Mail} and {@link ResKey}.
		 */
		public static SendMailResult createErrorResult(Mail aMail, ResKey anI18nErrorKey) {
            SendMailResult theResult = createErrorResult(anI18nErrorKey);
            theResult.setMail(aMail);
            theResult.setSendDate(new Date());
            return theResult;
        }
    }

	/**
	 * Module for instantiation of the {@link MailHelper}.
	 */
	public static class Module extends TypedRuntimeModule<MailHelper> {

		/** Singleton for this module. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<MailHelper> getImplementation() {
			return MailHelper.class;
		}

	}
}
