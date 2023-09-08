/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.mail;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import com.top_logic.base.mail.MailHelper.SendMailResult;
import com.top_logic.basic.AliasManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Reloadable;
import com.top_logic.basic.ReloadableManager;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.func.IfTrue;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.version.Version;
import com.top_logic.layout.form.values.MultiLineText;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.DynamicMandatory;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ManagedClass} for sending e-mail via SMTP.
 * 
 * @see #sendMail(Mail) To send mails, create a new mail via {@link Mail} and pass it to the
 *      {@link #sendMail(Mail)} method.
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public final class MailSenderService extends ConfiguredManagedClass<MailSenderService.Config> implements Reloadable {

	private Config _config;

	private ObjectPool _transports;
    
	private Session _session;

	private String _server;

	private boolean _activated;

	/**
	 * Configuration for {@link MailSenderService}.
	 */
	@DisplayOrder({
		Config.ACTIVATED,
		Config.SERVER,
		Config.LOOKUP_MX_RECORD,
		Config.PORT,
		Config.USER,
		Config.PASSWORD,
		Config.MAIL_DOMAIN,
		Config.FROM_ADDRESS,
		Config.STARTUP_NOTIFICATION_RECEIVERS,
		Config.STARTUP_NOTIFICATION_SUBJECT,
		Config.STARTUP_NOTIFICATION_BODY,
		Config.OPTIONS,
	})
	public interface Config extends ConfiguredManagedClass.Config<MailSenderService> {

		/** @see #getActivated() */
		String ACTIVATED = "activated";

		/** @see #getServer() */
		String SERVER = "server";

		/** @see #getLookupMX() */
		String LOOKUP_MX_RECORD = "lookup-mx-record";

		/** @see #getPort() */
		String PORT = "port";

		/** @see #getUser() */
		String USER = "user";

		/** @see #getPassword() */
		String PASSWORD = "password";

		/** @see #getStartUpNotificationReceivers() */
		String STARTUP_NOTIFICATION_RECEIVERS = "startup-notification-receivers";

		/** @see #getStartUpNotificationSubject() */
		String STARTUP_NOTIFICATION_SUBJECT = "startup-notification-subject";

		/** @see #getStartUpNotificationBody() */
		String STARTUP_NOTIFICATION_BODY = "startup-notification-body";

		/** @see #getMailDomain() */
		String MAIL_DOMAIN = "mail-domain";

		/** @see #getFromAddress() */
		String FROM_ADDRESS = "from-address";

		/** @see #getOptions() */
		String OPTIONS = "options";

		/**
		 * Status of the transport.
		 */
		@Name(ACTIVATED)
		@BooleanDefault(true)
		boolean getActivated();

		/**
		 * The e-mail domain name of the company.
		 * 
		 * <p>
		 * The value is used to turn local accounts into e-mail addresses by prefixing the domain
		 * name and a '@' character to an account name. If account names do not correspond to e-mail
		 * addresses, this value must be empty.
		 * </p>
		 */
		@Name(MAIL_DOMAIN)
		@Nullable
		String getMailDomain();

		/**
		 * The address this system sends e-mails from.
		 */
		@Name(FROM_ADDRESS)
		@Nullable
		@DynamicMandatory(fun = IfTrue.class, args = @Ref(ACTIVATED))
		String getFromAddress();

		/**
		 * The port of the SMTP server.
		 */
		@Name(SERVER)
		@DynamicMandatory(fun = IfTrue.class, args = @Ref(ACTIVATED))
		String getServer();

		/**
		 * Whether {@link #getServer()} is a domain name that should be resolved to a MX record
		 * through DNS.
		 */
		@Name(LOOKUP_MX_RECORD)
		@BooleanDefault(false)
		boolean getLookupMX();

		/**
		 * The port of the SMTP server.
		 */
		@Name(PORT)
		@IntDefault(-1)
		int getPort();

		/**
		 * The user name used to login to the SMTP server.
		 */
		@Name(USER)
		@Nullable
		String getUser();

		/**
		 * The password to authenticate this system to the SMTP server.
		 */
		@Name(PASSWORD)
		@Encrypted
		@Nullable
		String getPassword();
		
		/**
		 * A comma separated list of e-mail address to send a startup notification mail to.
		 */
		@Name(STARTUP_NOTIFICATION_RECEIVERS)
		@Format(CommaSeparatedStrings.class)
		List<String> getStartUpNotificationReceivers();

		/**
		 * The subject of the startup notification e-mail.
		 * 
		 * <p>
		 * The value is only relevant, if {@link #getStartUpNotificationReceivers()} is set. If no
		 * subject is given, a generic message subject is generated.
		 * </p>
		 */
		@Name(STARTUP_NOTIFICATION_SUBJECT)
		@Nullable
		String getStartUpNotificationSubject();

		/**
		 * The body of the startup notification e-mail.
		 * 
		 * <p>
		 * The value is only relevant, if {@link #getStartUpNotificationReceivers()} is set. If no
		 * message body is given, a generic message body is generated.
		 * </p>
		 */
		@Name(STARTUP_NOTIFICATION_BODY)
		@ControlProvider(MultiLineText.class)
		@Nullable
		String getStartUpNotificationBody();

		/**
		 * Mail parameters, for instance socket and connection timeout or the transport protocol.
		 */
		@Name(OPTIONS)
		@MapBinding(key = "name", attribute = "value")
		Map<String, String> getOptions();
	}

	/**
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        Configuration for {@link MailSenderService}.
	 * @throws ConfigurationException
	 *         Exception if no system address is configured.
	 */
	public MailSenderService(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		_config = config;

		GenericObjectPool.Config poolConfig = new GenericObjectPool.Config();

		/**
		 * This configuration ensures one single connection to an SMPT-Server The connection will be opened
		 * after creation. The connection will be tested to be opened before usage. The connection will be
		 * closed after being one minute idle.
		 */
		poolConfig.maxActive = 1; // only one connection in the pool
		poolConfig.testOnBorrow = false;
		poolConfig.testOnReturn = false;
		// remove idle connections after one minute
		poolConfig.timeBetweenEvictionRunsMillis = 60000;
		// remove idle connections only if they were in idle for at least this time
		poolConfig.minEvictableIdleTimeMillis = 5000;
		poolConfig.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_BLOCK;
		// block borrow connection for a maximum of 10 seconds
		poolConfig.maxWait = 10000;
		_transports = new GenericObjectPool(new PooledTransportFactory(), poolConfig);
	}

	@Override
	public String getDescription() {
        return "Reloads the configuration for SMTP transport and tries to connect to the configured server!";
    }

    @Override
	public String getName() {
        return "SMTP Mail Transport";
    }

	@Override
	protected void startUp() {
		super.startUp();

		Config config = getConfig();
		boolean shouldActivate = config.getActivated();
		if (shouldActivate) {
			if (config.getFromAddress() == null) {
				Logger.error("No system address configured, unable to send mails.", MailSenderService.class);
				_activated = false;
			} else {
				_activated = true;

				_server = resolveServer();
				ReloadableManager.getInstance().addReloadable(this);
				sendStartupNotification(config);
			}
		} else {
			_activated = false;
		}
	}

	/**
	 * Sends a mail to configured receivers for a system-startup notification.
	 */
	private void sendStartupNotification(Config config) {
		List<String> receivers = config.getStartUpNotificationReceivers();
		if (!receivers.isEmpty()) {
			Mail mail = new Mail(config.getFromAddress());
			
			for (String receiver : receivers) {
				mail.addReceiver(receiver);
			}
		    
			String subject = config.getStartUpNotificationSubject();
			if (StringServices.isEmpty(subject)) {
				subject = Version.getApplicationName() + " startup notification";
		    }
			mail.setTitle(subject);
		    
			String body = config.getStartUpNotificationBody();
			if (StringServices.isEmpty(body)) {
				StringBuilder buffer = new StringBuilder();
				buffer.append(Version.getApplicationName());
				buffer.append(" startup");
				buffer.append("\nDate: ");
				buffer.append(CalendarUtil.getDateTimeInstance().format(new Date()));
				String host = AliasManager.getInstance().getAlias(AliasManager.HOST);
				if (host != null) {
					buffer.append("\nURL: ");
					buffer.append(host);
					buffer.append(AliasManager.getInstance().getAlias(AliasManager.APP_CONTEXT));
				}
	
				try {
					InetAddress localhost = InetAddress.getLocalHost();
					buffer.append("\nHost: ");
					buffer.append(localhost.getHostName());
					buffer.append(" / ");
					buffer.append(localhost.getHostAddress());
				} catch (UnknownHostException n) {
					// ignore
				}
		        
				body = buffer.toString();
		    }
		    
			mail.setContent(body);
			mail.setType(MailHelper.CONTENT_TYPE_TEXT);
		    
			SendMailResult theResult = sendMail(mail);
			if (theResult.isSuccess()) {
				Logger.info("Startup notification sent to: " + receivers, MailSenderService.class);
			} else {
		        theResult.logErrorResult(MailSenderService.class);
		    }
		}
	}

	@Override
	protected void shutDown() {
		ReloadableManager.getInstance().removeReloadable(this);
		_server = null;

		super.shutDown();
	}

    @Override
	public boolean reload() {
		if (!isActivated()) {
			return true;
		}

		_session = null;
        
        try {
			_transports.clear();
        } catch (Exception ex) {
            Logger.error("Cannot reset transport connection pool", ex, MailSenderService.class);
        }
        
        try {
            if (this.isActivated()) {
				// Reconnect
				this.releaseTransport(this.getTransport(), true);
            }
        } catch (MessagingException mex) {
            Logger.error("Unable to load!", mex, this);
        }
        return true;
    }

    @Override
	public boolean usesXMLProperties() {
        return true;
    }

	/**
	 * Sends the given {@link Mail}.
	 */
    public SendMailResult sendMail(Mail aMail) {
        if ( ! isActivated()) {
            return SendMailResult.createErrorResult(aMail, I18NConstants.ERROR_NO_EMAIL_SUPPORT);
        }
        
        boolean isDebug = Logger.isDebugEnabled(this);
        
		MimeMessage theMessage = this.createEmptyMessage();
        
        Address theSender   = aMail.getSender();
        String  theTitle    = aMail.getTitle();
        String  theContent  = aMail.getContent();
        Date    theSendDate = new Date();
        String  theType     = aMail.getType();
        
        if (theSender == null) {
            return SendMailResult.createErrorResult(aMail, I18NConstants.ERROR_NO_SENDER);
        }
        else if (theTitle == null) {
            if (isDebug) {
                Logger.debug("No subject!", this);
            }
            return SendMailResult.createErrorResult(aMail, I18NConstants.ERROR_NO_TITLE);
        } 
        else if (theContent == null) {
            if (isDebug) {
                Logger.debug("No content!", this);
            }
            return SendMailResult.createErrorResult(aMail, I18NConstants.ERROR_NO_CONTENT);
        } 
        else {
            try {
                Address[] theArray = null;
                Address[] theRecv = null;                
                Address[] theCcRecv = null;
                Address[] theBccRecv = null;
                
                // set sender of the mail
                theMessage.setFrom(theSender);

                // add primary receivers to mail (mandatory)
                List<Address> theToList = aMail.getReceiverList();
				List<Address> theCcList = aMail.getCcReceiverList();
				List<Address> theBccList = aMail.getBccReceiverList();
                
				if (hasNoRecipients(aMail)) {
                    return SendMailResult.createErrorResult(aMail, I18NConstants.ERROR_NO_RECEIVER);
                }
                
                theArray = new Address[theToList.size()];
                theRecv = theToList.toArray(theArray);
                theMessage.addRecipients(Message.RecipientType.TO, theRecv);

                // add "cc" receivers to mail (optional)
                theArray = new Address[theCcList.size()];
                theCcRecv = theCcList.toArray(theArray);
                theMessage.addRecipients(Message.RecipientType.CC, theCcRecv);
                
                // add "bcc" receivers to mail (optional)
                theArray = new Address[theBccList.size()];
                theBccRecv = theBccList.toArray(theArray);
                theMessage.addRecipients(Message.RecipientType.BCC, theBccRecv);
                
                // add "replyTo" receivers to mail (optional)
                List<Address> theReplyToList = aMail.getReplyToReceiverList();
                theArray = new Address[theReplyToList.size()];
                theRecv = theReplyToList.toArray(theArray);
                theMessage.setReplyTo(theRecv);
                
                // set subject and data of mail
				theMessage.setSubject(theTitle, "UTF-8");
                theMessage.setSentDate(theSendDate);

                if (MailHelper.CONTENT_TYPE_TEXT.equals(theType)) {
                	theType = MailHelper.CONTENT_TYPE_TEXT_UTF8;  // Avoid problems with encoding here.
                }

				if (aMail.getAttachments().size() == 0) { // mail without attachments.
                    theMessage.setContent(theContent, theType);
                } 
                else {            // mail with one or more attachments
                    BodyPart theTextBodyPart = new MimeBodyPart();
                    theTextBodyPart.setContent(theContent, theType);
                    
                    Multipart theMultipart = new MimeMultipart();
                    theMultipart.addBodyPart(theTextBodyPart);
                    aMail.addAttachments(theMultipart);
                    theMessage.setContent(theMultipart);
                }
            
                List<Address>  theInvalid = new ArrayList<>();
                
                if (isDebug) {
                    List<Address> theRes = new ArrayList<>(theToList);
                    theRes.addAll(theCcList);
                    theRes.addAll(theBccList);
                    Logger.debug("Try sending message " + theTitle + " from " + theSender + " to " + StringServices.join(theRes, ", "), this);
                }
                
                try {
                    
                    SendMailResult theResult = new SendMailResult();
                    theResult.setMail(aMail);
                    theResult.setSendDate(theSendDate);
                    
                    if (! this.send(theMessage, theInvalid, isDebug)) {
                        if (! theInvalid.isEmpty()) {
                            theResult.addErrorMessage(I18NConstants.ERROR_INVALID_ADDRESSES);
                            theResult.setInvalidAddresses(theInvalid);
                        }
                        else {
                            theResult.addErrorMessage(I18NConstants.ERROR_UNKNOWN_REASON);
                        }
                        if (isDebug) {
                            theResult.logErrorResult(this);
                        }
                    }
                    return theResult;
                    
                } catch (MessagingException mex) {
                    TopLogicException theEx = new TopLogicException(MailSenderService.class, "mail.send.failed.messageException", mex);
                    SendMailResult theResult = SendMailResult.createErrorResult(aMail, I18NConstants.ERROR_MESSAGE_EXCEPTION);
                    theResult.setException(theEx);
                    return theResult;
                }
            } 
            catch (MessagingException msgEx) {
                TopLogicException theEx = new TopLogicException(MailSenderService.class, "mail.send.failed.formatException", msgEx);
                SendMailResult theResult = SendMailResult.createErrorResult(aMail, I18NConstants.ERROR_FORMAT_EXCEPTION);
                theResult.setException(theEx);
                
                Logger.error("Sending of mail with title " + theTitle + "failed", msgEx, this);
                return theResult;
            }
        }
        
        // never reached?
        //return SendMailResult.createErrorResult(aMail, "mail.send.failed.unknown");
    }

	private boolean hasNoRecipients(Mail mail) {
		return mail.getReceiverList().isEmpty()
			&& mail.getCcReceiverList().isEmpty()
			&& mail.getBccReceiverList().isEmpty();
	}
    
    /**
     * Send a Message on the actual transport. Before sending the message 
     * {@link Message#saveChanges()} will be called.
     * 
     * @param aMessage to be send
     * @param invalidAddresses 
     *        will contain all {@link Address} to which this message was not send.
     *        @see SendFailedException
     * @return <code>true</code> if all receivers had valid addresses
     */
    public boolean send(Message aMessage, List<Address> invalidAddresses, boolean logDebug) throws MessagingException {
        if ( ! this.isActivated()) {
            throw new IllegalStateException("Mail transport is not activated! Check your configuration.");
        }

        aMessage.saveChanges();  // throws MessagingException, cannot be handled with retry

		// don't do too much
        int maxRetries = 3; //Math.max(maxRetries, 3);
        MessagingException theLastException = null;
        boolean doRetry = true;
        
        while (doRetry) {
			boolean success = false;
			Transport theTransport = this.getTransport();
            try {
				aMessage.setFrom(new InternetAddress(getFromAddress()));
                theTransport.sendMessage(aMessage, aMessage.getAllRecipients());
                if (logDebug) {
                    Logger.debug("Send message '" + aMessage.getSubject() + "' to " + StringServices.toString(aMessage.getAllRecipients()), this);
                }
				success = true;
                return true;
            } catch (SendFailedException sfex) {
				// It seems that invalid addresses are only reported
                // if the server is not able to relay for their domain.
                // Invalid addresses will not be reported if the domain is relayed.
                // but a user/mailbox is not known to the server.
                // Example: Our server relays for "top-logic.com".
                // Mails to unknown@top-logic.com will accepted by the server.
                // Mails to fsc@mailtest.com will be rejected with an invalid address error.
                if (invalidAddresses == null) {
                    invalidAddresses = new ArrayList<>();
                }
                
                Address[] theFailed = sfex.getInvalidAddresses();
                if (theFailed != null) {
                    invalidAddresses.addAll(Arrays.asList(theFailed));
                }
                
                theFailed = sfex.getValidUnsentAddresses();
                if (theFailed != null) {
                    invalidAddresses.addAll(Arrays.asList(theFailed));
                }
                
                if (invalidAddresses.isEmpty()) {
                    // retry only if the SendMailException was NOT caused by invalid addresses
                    // this is if the exception does not contain any invalid addresses
                    theLastException = sfex;
                    doRetry = (--maxRetries > 0);
                    Logger.warn("Sending of mail with subject '"+ aMessage.getSubject() + "' failed. Retry " + maxRetries, sfex, this);
                }
                else {
                    // sending failed for at least one receiver 
                    return false;
                }
            } catch (MessagingException mex) {
                // retry and log
                theLastException = mex;
                doRetry = (--maxRetries > 0);
                Logger.warn("Sending of mail with subject '"+ aMessage.getSubject() + "' failed. Retry " + maxRetries, mex, this);
            } finally {
				this.releaseTransport(theTransport, success);
            }
        }// end retry

        // sending failed after all
        if (theLastException != null) {
            throw theLastException;
        }
        else {
            return false;
        }
    }
    
    /**
     * Returns the Transport of the actual session. 
     * After usage {@link #releaseTransport(Transport, boolean)} must be called 
     * 
     * @throws MessagingException if connecting to the SMTP server fails for some reason.
     */
    private Transport getTransport() throws MessagingException {
        try {
			return (Transport) _transports.borrowObject();
        } catch (Exception ex) {
            throw new MessagingException("Cannot get transport from connection pool", ex);
        }
    }

	/**
	 * Release a Transport after usage
	 * 
	 * @param success
	 *        Whether the given {@link Transport} was used successfully.
	 */
    private void releaseTransport(Transport aTransport, boolean success) throws MessagingException {
        try {
			if (success) {
				_transports.returnObject(aTransport);
			} else {
				_transports.invalidateObject(aTransport);
			}
        } catch (Exception ex) {
            throw new MessagingException("Cannot return transsport from connection pool", ex);
        }
    }
    
    /**
     * Returns the actual session.
     */
    private Session getSession() {
		if (_session == null) {
			_session = Session.getInstance(createProperties(), null);
        }
		return _session;
    }
    
    /**
     * Reads the configuration and resolves the servers name.
     */
	private Properties createProperties() {
		Config config = getConfig();

		Properties properties = new Properties();
		properties.put("mail.smtp.host", _server);
		properties.put("mail.transport.protocol", "smtp");

		String user = config.getUser();
		if (!StringServices.isEmpty(user)) {
			properties.put("mail.smtp.user", user);
		}
		properties.put("mail.smtp.port", Integer.toString(config.getPort()));

		for (Entry<String, String> entry : config.getOptions().entrySet()) {
			String value = entry.getValue();
			if (!StringServices.isEmpty(value)) {
				properties.put(entry.getKey(), value);
			}
		}

		return properties;
    }
    
    /**
	 * Creates an empty {@link MimeMessage} for the current mail session.
	 */
    public MimeMessage createEmptyMessage() {
        return new MimeMessage(getSession());
    }
    
    /**
	 * Whether this {@link MailSenderService} is set active.
	 */
	public final boolean isActivated() {
		return _activated;
    }
    
    /**
	 * The {@link MailSenderService} instance.
	 */
    public static synchronized MailSenderService getInstance() {
    	return Module.INSTANCE.getImplementationInstance();
    }
    
    /**
	 * Resolves the server name. Depending on {@link Config#getLookupMX()}, a DNS MX record is
	 * searched.
	 */
	private String resolveServer() {
		Config config = getConfig();

		String hostname = config.getServer();
        if (config.getLookupMX()) {
            try {
				List<String> hostnames = lookupMxRecord(hostname);
				if (!hostnames.isEmpty()) {
					return hostnames.get(0);
                }
				Logger.error("No MX record for " + hostname + ". Assuming " + hostname + " is valid.",
					MailSenderService.class);
			} catch (NamingException ex) {
				Logger.error("Unable to resolve MX record for " + hostname + ". Assuming " + hostname + " is valid.",
					ex, MailSenderService.class);
            }
        }

		return hostname;
    }
    
    /**
	 * @see Config#getFromAddress()
	 */
	public static String getFromAddress() {
		try {
			return serviceConfig().getFromAddress();
		} catch (ConfigurationException ex) {
			return null;
		}
	}

	/**
	 * The Internet domain name of the company.
	 * 
	 * <p>
	 * The value is used to turn a local account name into an e-mail address by prefixing the domain
	 * name and a '@' character.
	 * </p>
	 * 
	 * @return The companies domain name, or <code>null</code>, if local account names do not
	 *         correspond to e-mail addresses.
	 */
	protected static String getMailDomain() {
		try {
			return serviceConfig().getMailDomain();
		} catch (ConfigurationException ex) {
			return null;
		}
	}

	private static Config serviceConfig() throws ConfigurationException {
		return (Config) ApplicationConfig.getInstance().getServiceConfiguration(MailSenderService.class);
	}

	/**
	 * This method tries to lookup MX or A records for a domain-name.
	 * 
	 * @param domain
	 *        (the part after the @)
	 * @return a list of hostnames or ip addresses
	 * @throws NamingException
	 *         if the lookup fails for some reason
	 */
	public static List<String> lookupMxRecord(String domain) throws NamingException {
		Hashtable<String, String> theEnv = new Hashtable<>();
        theEnv.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
		DirContext dirContext = new InitialDirContext(theEnv);
        
        // try to get MX records
		Attributes attributes = dirContext.getAttributes(domain, new String[] { "MX" });
		Attribute mxAttr = attributes.get("MX");
        
		if (mxAttr != null) {
			// Got MX records
			return splitMXRecord(mxAttr);
		} else {
			// No MX, try to get A record.
			attributes = dirContext.getAttributes(domain, new String[] { "A" });
			Attribute aAttr = attributes.get("A");
			if (aAttr == null) { // got pissed
				throw new NamingException("No dns entry found for: " + domain);
			} else {
				return splitARecord(aAttr);
            }
        }
    }
    
    // Note the "." at the end of each entry
	// MX: 100 www.top-logix.com., 120 www.tcom.com., 140 www.heise.de. (= anAttr)
    // 100 www.top-logic.com.                                           (= NamingEnum)
    // 120 www.tcom.com.
    // 140 www.heise.de.
	private static List<String> splitMXRecord(Attribute mxAttr) throws NamingException {
		NamingEnumeration<?> it = mxAttr.getAll();
		List<String> hostNames = new ArrayList<>();
		while (it.hasMore()) {
			String record = (String) it.next();
			String parts[] = record.split(" ");
			if (parts.length == 2) {
				// Got a hostname.
				String hostname = parts[1];
				if (hostname.endsWith(".")) {
					hostname = hostname.substring(0, hostname.length() - 1);
				}
				hostNames.add(hostname);
			}
		}
		return hostNames;
    }
    
    // A:  192.168.0.1, 192.168.0.15 (= anAttr)
    // 192.168.0.1                   (= NamingEnum)
    // 192.168.0.15
	private static List<String> splitARecord(Attribute aAttr) throws NamingException {
		List<String> hostNames = new ArrayList<>();

		NamingEnumeration<?> theEnum = aAttr.getAll();
        while ( theEnum.hasMore() ) {
            String theRecord = (String) theEnum.next();
            if ( ! StringServices.isEmpty(theRecord)) {
				hostNames.add(theRecord);
            }
         }
			return hostNames;
    }

    /**
     * Check if the mail transport is available
     */
    public static boolean isConfigured () {
		return MailSenderService.Module.INSTANCE.isActive() && MailSenderService.getInstance().isActivated();
    }
    
    /**
     * The PooledTransportFactory will create, connect and disconnect a {@link Transport} 
     * 
     * @author    <a href=mailto:fsc@top-logic.com>fsc</a>
     */
    class PooledTransportFactory implements PoolableObjectFactory {
        
        @Override
		public void activateObject(Object aObj) throws Exception {
			// Ignore.
        }
        
        /**
         * Close the {@link Transport}
         */
        @Override
		public void destroyObject(Object aObj) throws Exception {
            ((Transport) aObj).close(); 
        }
        
        /**
		 * Creates a connected {@link Transport} of {@link MailSenderService#getSession()}.
		 */
        @Override
		public Object makeObject() throws Exception {
			Transport result = getSession().getTransport();
			result.connect(_server, _config.getPort(), _config.getUser(), _config.getPassword());
			return result;
        }
        
        @Override
		public void passivateObject(Object aObj) throws Exception {
			// Ignore.
        }
        
        /**
         * Checks if the {@link Transport#isConnected()}
         */
        @Override
		public boolean validateObject(Object aObj) {
            return ((Transport) aObj).isConnected();
        }
    }

	/**
	 * Module for {@link MailSenderService}.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public static final class Module extends TypedRuntimeModule<MailSenderService> {

		/**
		 * Module instance.
		 */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<MailSenderService> getImplementation() {
			return MailSenderService.class;
		}
	}
}

