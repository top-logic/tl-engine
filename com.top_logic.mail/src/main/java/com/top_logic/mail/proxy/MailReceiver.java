/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.proxy;

import java.io.IOException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.StoreClosedException;
import javax.mail.event.ConnectionEvent;
import javax.mail.event.ConnectionListener;
import javax.mail.search.SearchTerm;

import com.top_logic.base.mail.MailSenderService;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.func.IfTrue;
import com.top_logic.layout.form.values.edit.annotation.DynamicMandatory;
import com.top_logic.mail.base.imap.IMAPMailFolder;
import com.top_logic.mail.proxy.exchange.ExchangeMail;
import com.top_logic.mail.proxy.exchange.ExchangeMeeting;
import com.top_logic.util.error.TopLogicException;

/**
 * Connection to a IMAP server for receiving mails.
 * 
 * @see MailReceiverService#getMailReceiverInstance()
 */
public class MailReceiver implements ConnectionListener {

	/**
	 * Configuration options of {@link MailReceiver}.
	 */
	@DisplayOrder({
		ServerConfig.ACTIVATED,
		ServerConfig.SERVER,
		ServerConfig.PORT,
		ServerConfig.USER,
		ServerConfig.PASSWORD,
		ServerConfig.OPTIONS,
	})
	public interface ServerConfig extends ConfigurationItem {
	
		/**
		 * @see #isActivated()
		 */
		String ACTIVATED = "activated";

		/** @see #getServer() */
		String SERVER = "server";

		/** @see #getPort() */
		String PORT = "port";

		/** @see #getUser() */
		String USER = "user";

		/** @see #getPassword() */
		String PASSWORD = "password";

		/** @see #getOptions() */
		String OPTIONS = "options";

		/**
		 * Whether receiving mails is activated.
		 */
		@Name(ACTIVATED)
		@BooleanDefault(true)
		boolean isActivated();

		/**
		 * The server's host name.
		 */
		@Name(SERVER)
		@Nullable
		@DynamicMandatory(fun = IfTrue.class, args = @Ref(ACTIVATED))
		String getServer();

		/**
		 * The server port to access.
		 * 
		 * <p>
		 * By default the standard port for the configured protocol is used.
		 * </p>
		 */
		@Name(PORT)
		@IntDefault(-1)
		int getPort();

		/**
		 * The user name for logging in to the {@link #getServer()}.
		 */
		@Name(USER)
		@Nullable
		@DynamicMandatory(fun = IfTrue.class, args = @Ref(ACTIVATED))
		String getUser();

		/**
		 * The password for {@link #getUser()}.
		 */
		@Name(PASSWORD)
		@Encrypted
		@Nullable
		@DynamicMandatory(fun = IfTrue.class, args = @Ref(ACTIVATED))
		String getPassword();

		/**
		 * Additional options to pass when opening a connection to the mail server.
		 */
		@Name(OPTIONS)
		@MapBinding(key = "name", attribute = "value")
		Map<String, String> getOptions();
	}

	/** Standard name of mail server in box. */
    public static final String INBOX = "INBOX";
    
    /** Connection to the mail server. */
	private Session _session;
    
    /** Used mail box on IMAP server. */
	private Store _store;

    /** 
     * This cache holds the inbox only 
     * other folders are cache in {@link IMAPMailFolder} itself
     */
	private Map<String, Folder> _folders = new HashMap<>();

	private ServerConfig _config;

	MailReceiver(ServerConfig config) {
		_config = config;
	}

    /** 
     * Debugging representation of this instance.
     * 
     * @return    Debugging string.
     */
    @Override
	public String toString() {
		return (getClass().getName() + " [" + toStringValues() + "]");
    }

    /** 
     * Reset all references to mail server.
     * 
     * @param    anEvent    The event notifying the closing of the connection.
     * @see      javax.mail.event.ConnectionListener#closed(javax.mail.event.ConnectionEvent)
     */
    @Override
	public void closed(ConnectionEvent anEvent) {
        Object theSource = anEvent.getSource();
        if (theSource instanceof Store) {
			_session = null;
			_store = null;
			_folders.clear();
            Logger.debug("Store '" + theSource + "' closed! "  + Thread.currentThread().toString(), this);
        }
        else if (theSource instanceof Folder && INBOX.equals(((Folder) theSource).getName())) {
			_folders.clear();
            Logger.debug("Folder '" + theSource + "' closed! "  + Thread.currentThread().toString(), this);
        }
    }

    /** 
     * Reset all references to mail server.
     * 
     * @param    anEvent    The event notifying the disconnecting from mail server.
     * @see      javax.mail.event.ConnectionListener#disconnected(javax.mail.event.ConnectionEvent)
     */
    @Override
	public void disconnected(ConnectionEvent anEvent) {
        Object theSource = anEvent.getSource();
        if (theSource instanceof Store) {
			_session = null;
			_store = null;
			_folders.clear();
            Logger.debug("Store '" + theSource + "' disconnected! "  + Thread.currentThread().toString(), this);
        }
    }

    @Override
	public void opened(ConnectionEvent anEvent) {
		// No additional operation needed when server connection opened.
    }

    /** 
     * Connect to the store, if there is no connection.
     * 
     * @return    The requested store or <code>null</code>, if connecting fails for a reason.
     */
    public Store connect() {
		if (isConnected()) {
			return _store;
        }
        else {
			if (login()) {
				return _store;
            }
            else {
                return (null);
            }
        }
    }

    /** 
     * Disconnect the proxy from the mail server.
     * 
     * @return    If disconnecting succeeds.
     */
    public boolean disconnect() {
        boolean isConnect = this.isConnected();

        if (isConnect) {
            this.logout(true);
        }

        return (isConnect);
    }

    /**
	 * Return the default folder of the mail server (the root folder).
	 * 
	 * @return The root folder of the mail server, never <code>null</code>.
	 * 
	 * @throws IllegalStateException
	 *         if this Store is not connected.
	 * @throws MessagingException
	 *         getting information from server fails.
	 */
    public Folder getDefaultFolder() throws IllegalStateException, MessagingException {
        return (this.connect().getDefaultFolder());
    }

    /**
	 * Return the inbox of the connected server in {@link Folder#READ_WRITE READ_WRITE mode}.
	 * 
	 * @return The requested in box, never <code>null</code>.
	 * @throws IllegalStateException
	 *         If was no login before.
	 * @throws TopLogicException
	 *         If opening the inbox fails for a reason.
	 * @see #login()
	 */
    public Folder getInbox() throws IllegalStateException, TopLogicException {
        return (this.getFolder(MailReceiver.INBOX));
    }

    /** 
     * Return the folder identified by its name.
     * 
     * @param    aName    The name of the requested folder, must not be <code>null</code>.
     * @return   The requested folder, never <code>null</code>.
     * @throws   TopLogicException    If getting the folder fails for a reason.
     */
    public Folder getFolder(String aName) throws TopLogicException {
        try {
            try {
                return (this.getFolderWithoutRetry(aName));
            }
            catch (StoreClosedException ex) {
                this.reconnect();

                return (this.getFolderWithoutRetry(aName));
            }
        }
        catch (MessagingException ex) {
			throw new TopLogicException(I18NConstants.FOLDER_OPEN.fill(aName), ex);
        }
    }

    /** 
     * Return all meetings located in the inbox.
     * 
     * This method will only collect meetings ("text/calendar") objects from the messages.
     * 
     * @return    The collection of all meetings, never <code>null</code>, but may be emty.
     * @throws    TopLogicException    If getting the messages from inbox fails.
     */
	public Collection<MailMeeting> getMeetings() {
		Collection<MailMeeting> theResult = new ArrayList<>();

        try {
            Folder    theFolder   = this.getInbox();
            Message[] theMessages = theFolder.getMessages();

            for (int thePos = 0; thePos < theMessages.length; thePos++) {
                Message theMessage = theMessages[thePos];

                try {
                    if (!theMessage.isExpunged()) {
                        MailMeeting theMeeting = this.message2Meeting(theMessage);

                        if (theMeeting != null) {
                            theResult.add(theMeeting);
                        }
                    }
                }
                catch (Exception ex) {
                    Logger.error("Unable to inspect message #" + thePos, ex, this);
                }
            }
        }
        catch (MessagingException ex) {
			throw new TopLogicException(I18NConstants.MEETINGS_GET, ex);
        }

        return (theResult);
    }

    /** 
     * Return all meetings located in the inbox.
     * 
     * This method will only collect meetings ("text/calendar") objects from the messages.
     * 
     * @return    The collection of all meetings, never <code>null</code>, but may be empty.
     * @throws    TopLogicException    If getting the messages from inbox fails.
     */
    public Collection<MailServerMessage> getContent() {
        return (this.getContent(MailReceiver.INBOX));
    }

    /**
	 * Return the meetings and mails matching to the given search term.
	 * 
	 * @param aTerm
	 *        The term to be used by search, may be <code>null</code>
	 * @return The collection of all meetings, never <code>null</code>, but may be empty.
	 * @throws TopLogicException
	 *         If getting the messages from inbox fails.
	 */
    public Collection<MailServerMessage> getContent(SearchTerm aTerm) {
        Collection<MailServerMessage> theResult = new ArrayList<>();

        try {
            Folder    theFolder   = this.getInbox();
            Message[] theMessages = (aTerm == null) ? theFolder.getMessages() 
                                                    : theFolder.search(aTerm);

            for (int thePos = 0; thePos < theMessages.length; thePos++) {
                Message theMessage = theMessages[thePos];

                try {
                    if (this.isMeeting(theMessage)) {
                        MailMeeting theMeeting = this.message2Meeting(theMessage);
    
                        if (theMeeting != null) {
                            theResult.add(theMeeting);
                        }
                    }
                    else {
                        MailMessage theMail = this.message2Mail(theMessage);

                        if (theMail != null) {
                            theResult.add(theMail);
                        }
                    }
                }
                catch (Exception ex) {
                    Logger.error("Unable to inspect message #" + thePos, ex, this);
                }
            }
        }
        catch (MessagingException ex) {
			throw new TopLogicException(I18NConstants.MEETINGS_GET, ex);
        }

        return (theResult);
    }

    /** 
     * Return all meetings located in the inbox.
     * 
     * This method will only collect meetings ("text/calendar") objects from the messages.
     *
     * @param    aFolder    The name of the folder to get the content for.
     * @return   The collection of all meetings, never <code>null</code>, but may be emty.
     * @throws   TopLogicException    If getting the messages from inbox fails.
     */
	public List<MailServerMessage> getContent(String aFolder) throws TopLogicException {
        try {
			Folder theFolder = this.getFolder(aFolder);

            if (theFolder != null) {
                return this.convertMessages(theFolder.getMessages());
            }
            else {
				return Collections.emptyList();
            }
        }
        catch (MessagingException ex) {
			throw new TopLogicException(I18NConstants.MEETINGS_GET, ex);
        }
    }

	/** 
	 * Convert the given messages into internal {@link MailServerMessage}s.
	 * 
	 * @param someMessages   The messages to be converted.
	 * @return   The collection of messages.
	 */
	public List<MailServerMessage> convertMessages(Message[] someMessages) {
		List<MailServerMessage> theResult = new ArrayList<>();

        for (int thePos = 0; thePos < someMessages.length; thePos++) {
            Message theMessage = someMessages[thePos];

            try {
                if (!theMessage.isExpunged()) {
                    if (this.isMeeting(theMessage)) {
                        MailMeeting theMeeting = this.message2Meeting(theMessage);

                        if (theMeeting != null) {
                            theResult.add(theMeeting);
                        }
                    }
                    else {
                        MailMessage theMail = this.message2Mail(theMessage);

                        if (theMail != null) {
                            theResult.add(theMail);
                        }
                    }
                }
            }
            catch (Exception ex) {
                Logger.error("Unable to inspect message #" + thePos, ex, this);
            }
        }

        return (theResult);
    }

    /** 
     * Return the session to communicate with the mail server.
     * 
     * @return    The requested session to the mail server.
     */
    public Session getSession() {
		if (_session == null) {
			_session = Session.getInstance(createProperties(), null);
        }

		return _session;
    }
    
    /**
	 * Convert a {@link Message message} to a {@link MailMeeting meeting object}.
	 * 
	 * @param aMessage
	 *        The message to be converted, may be <code>null</code>.
	 * @return The converted meeting, may be <code>null</code>.
	 * 
	 * @throws MessagingException
	 *         If getting further information from message fails.
	 * @throws IOException
	 *         If reading the properties fails for a communication error.
	 */
    protected MailMeeting message2Meeting(Message aMessage) throws IOException, MessagingException {
		if (isMeeting(aMessage)) {
			return (new ExchangeMeeting(aMessage, _config.getUser()));
		} else {
            return (null);
        }
    }

    /**
	 * Convert a {@link Message message} to a {@link MailMessage mail object}.
	 * 
	 * @param aMessage
	 *        The message to be converted, may be <code>null</code>.
	 * @return The converted mail, may be <code>null</code>.
	 * 
	 * @throws MessagingException
	 *         If getting further information from message fails.
	 * @throws IOException
	 *         If reading the properties fails for a communication error.
	 */
    protected MailMessage message2Mail(Message aMessage) throws IOException, MessagingException {
		if (!isMeeting(aMessage)) {
            return (new ExchangeMail(aMessage));
        }
        else {
            return (null);
        }
    }

    /** 
     * Return the folder defined by the given name.
     * 
     * The folder will be looked up in the mail server by {@link Store#getFolder(String)}.
     * 
     * @param    aName          Name of the requested folder, must not be <code>null</code>.
     * @param    aFolderFlag    Flag for opening {@link Folder#open(int)}.
     * @return   The requested folder, never <code>null</code>.
     * @throws   MessagingException    If requesting the folder fails for a reason. 
     */
    protected Folder getFolder(String aName, int aFolderFlag) throws MessagingException {
		Store theStore = getStore();
        Folder theFolder = theStore.getFolder(aName);

        if(!theFolder.isOpen()){
            theFolder.open(aFolderFlag);
    	}

        return (theFolder);
    }

    /**
     * Check, if the given message is a meeting.
     * 
     * @param    aMessage   The message to be checked.
     * @return   <code>true</code>, if it is a meeting.
     * @throws   MessagingException    If getting further information from message fails.
     */
    protected boolean isMeeting(Message aMessage) throws MessagingException {
        boolean isDate = false;
        
        if (!aMessage.isSet(Flags.Flag.DELETED)) {
            // MS Exchange Server
            String[] theValues = aMessage.getHeader("Content-class");

            if (theValues != null) {
                for (int j = 0; j < theValues.length; j++) {
                    isDate = ("urn:content-classes:calendarmessage".equals(theValues[j]));
                }
                return isDate;
            }
            
            // others
            theValues = aMessage.getHeader("Content-Type");
            if (theValues != null) {
                for (int j = 0; j < theValues.length; j++) {
                    isDate = theValues[j].startsWith("text/calendar");
                }
            }
        }

        return isDate;
    }

    /**
	 * Return the connection to the mail server (the store).
	 * 
	 * @return The requested connection, never <code>null</code>.
	 * @throws IllegalStateException
	 *         If instance is {@link #isConnected() not connected}.
	 * @see #login()
	 */
    protected Store getStore() throws IllegalStateException {
        if (_store == null || !_store.isConnected()) {
        	Logger.info("Reconnecting to mail server.",MailReceiver.class);
			login();
        }

		return _store;
    }

    /** 
     * Return the requested folder.
     * 
     * This method will not perform a retry logic in case the mail store has been closed.
     * This function is realised in {@link #getFolder(String)}.
     * 
     * @param    aName    The name of the folder requested, must not be <code>null</code>.
     * @return   The requested folder, never <code>null</code>.
     * @throws   MessagingException    If accessing the folder fails for a reason. 
     * @see      #getFolder(String)
     */
    protected Folder getFolderWithoutRetry(String aName) throws MessagingException {
		Folder theFolder = _folders.get(aName);

        if (theFolder != null) {
            if (theFolder.exists()) {
                if (!theFolder.isOpen()) {
                    theFolder.open(Folder.READ_WRITE);
                    Logger.debug("(re)opening cached folder "+aName, this);
                }
            }
            else {
                theFolder = null;
            }
        }
   
        if (theFolder == null) {
            if (MailReceiver.INBOX.equals(aName)) {
				theFolder = getFolder(aName, Folder.READ_WRITE);
                if (theFolder != null) {
					_folders.put(aName, theFolder); // only cache inbox
                    theFolder.addConnectionListener(this);
                }
                else {
                    Logger.error("Unable to get INBOX Folder", this);
                }
            }
            else {
				Folder theDefault = getDefaultFolder();
                if (theDefault != null) {
                    theFolder = theDefault.getFolder(aName);
    
                    if (theFolder != null && theFolder.exists() && !theFolder.isOpen()) {
                        theFolder.open(Folder.READ_WRITE);
                        Logger.debug("Opening folder "+aName, this);
                    }
                    else {
                        Logger.error("Unable to get mail folder " + aName, this);
                    }
                }
                else {
                    Logger.error("Unable to get default folder. Folder is null." , this);
                }
            }
            // don't cache all folders
			// _folders.put(aName, theFolder);
        }

        return (theFolder);
    }

    /**
     * Reconnects this proxy to server with the params used during initial login.
     * 
     * @return true if success, false otherwise
     * @throws IllegalStateException if no login was called before or when this proxy already isConnected() 
     * 
     */
    protected boolean reconnect() throws IllegalStateException{
		boolean hasStore = _store != null;

        if (!hasStore) {
			getStore();
        }
        try {
            if (hasStore) {
                try {
					_store.close();
                }
                catch (Exception ex) {
                    // Ignore this, will reconnect anyway.
                }
            }

			_store = null;
			_store = connect();

			return _store != null;
        }
        catch(Exception ex) {
            Logger.error("Unable to reconnect store to mailserver", ex, this);
            return false;
        }
    }

    /**
	 * Check, if this instance is connected to mail server.
	 * 
	 * @return <code>true</code>, if instance is connected to server.
	 * @see #login()
	 */
	public boolean isConnected() {
		return _store != null && _store.isConnected();
    }

    /**
     * Return the properties needed to connect to a mail server.
     * 
     * @return    The requested properties, never <code>null</code>.
     */
	protected Properties createProperties() {
		Properties properties = new Properties();

		properties.put("mail.imap.host", _config.getServer());
		properties.put("mail.imap.port", Integer.toString(_config.getPort()));
		properties.put("mail.imap.user", _config.getUser());
		
		String fromAddress = MailSenderService.getFromAddress();
		if (!StringServices.isEmpty(fromAddress)) {
			properties.put("mail.from", fromAddress);
        }

		for (Entry<String, String> option : _config.getOptions().entrySet()) {
			String value = option.getValue();
			if (!StringServices.isEmpty(value)) {
				properties.put(option.getKey(), value);
            }
        }
        
		return properties;
    }

    /** 
     * Values to be displayed in {@link #toString()} method.
     * 
     * @return   The values to be displayed in debugging.
     */
    protected String toStringValues() {
		return ("store: " + _store);
    }

	/**
	 * Logs in to the mail server.
	 *
	 * @return <code>true</code>, if login succeeds.
	 * @throws IllegalArgumentException
	 *         If one of the given parameters is invalid.
	 * @throws TopLogicException
	 *         If login fails for a communication reason.
	 */
	public boolean login() {
        try {
			Store store = initStore();
			_folders.clear();
			return (store != null);
		} catch (Exception ex) {
			Logger.warn("Login to mailserver failed.", MailReceiver.class);
			throw new TopLogicException(
				I18NConstants.LOGIN.fill(_config.getServer(), _config.getUser()), ex);
        }
    }

    /** 
     * Perform a logout for the demon.
     * 
     * @param     autoExpunge    <code>true</code> to expunge all folders known to the proxy. 
     * @throws    TopLogicException    If logout fails for a reason.
     */
    protected void logout(boolean autoExpunge) throws TopLogicException {
		for (Iterator<String> theIt = _folders.keySet().iterator(); theIt.hasNext();) {
			String theKey = theIt.next();
			Folder theFolder = _folders.get(theKey);

            try {
                if (theFolder.isOpen()) {
                    if (autoExpunge) {
                        theFolder.expunge();
                    }
    
                    theFolder.close(false);
                }
            }
            catch (Exception ex) {
                Logger.warn("Unable to close mail folder '" + theKey + "'!", ex, this);
            }
        }
        
		_folders.clear();
        
		if (_store != null) {
            try {
				_store.removeConnectionListener(this);
				_store.close();
            }
            catch (MessagingException ex) {
                Logger.warn("Unable to close mail store!", ex, this);
            }
        }
    }

    /**
	 * Initializes the mail store.
	 * 
	 * @return The requested connection to the mail server, never <code>null</code>.
	 * @exception NoSuchProviderException
	 *            If a provider for the given protocol is not found.
	 * @throws MessagingException
	 *         When closing store fails.
	 */
	private Store initStore() throws NoSuchProviderException, MessagingException {
		resetSession();
		_store = getSession().getStore();
		if (!_store.isConnected()) {
			String server = _config.getServer();
			int port = _config.getPort();
			String user = _config.getUser();
			String passwd = _config.getPassword();
			try {
				_store.connect(server, port, user, passwd);
			} catch (MessagingException ex) {
				String serverURL = (port == -1) ? server : server + ':' + Integer.toString(port);

				Logger.warn("Connect failed, try to reconnect to mail server " + serverURL + '!', ex,
					MailReceiver.class);

				resetSession();
				_store = getSession().getStore();

				if (_store != null) {
					try {
						_store.connect(server, port, user, passwd);
					} catch (MessagingException ex2) {
						resetSession();
						Logger.error("Connect to mail server " + serverURL + " failed!", ex2, MailReceiver.class);
						_store = null;
					}
                }
                else {
                    Logger.error("Mail session didn't return a store!", this);
                }
            }

			if (_store != null) {
				_store.addConnectionListener(this);
            }
        }

		return _store;
    }

	private void resetSession() throws MessagingException {
		resetStore();

		if (_session != null) {
			_session = null;
		}
	}

	private void resetStore() throws MessagingException {
		if (_store != null) {
			_store.removeConnectionListener(this);
			_store.close();
			_store = null;
		}
	}

	/**
	 * Return the full name of a child folder.
	 * 
	 * @param aParent
	 *        Folder the child is located in.
	 * @param aSubFolderName
	 *        Name of the requested child folder.
	 * @return Requested child folder name.
	 */
    public static String getFullName(Folder aParent, String aSubFolderName) {
        String thePostfix = "";
        
        MailReceiver theProxy = MailReceiverService.getMailReceiverInstance();
        String theStandardInboxName = INBOX;
        if (theProxy != null) {
			Folder theInbox = theProxy.getInbox();
            theStandardInboxName = theInbox.getFullName();
            if (!StringServices.isEmpty(aSubFolderName)) {
                try {
                    thePostfix = theInbox.getSeparator() + aSubFolderName;
                } catch (MessagingException e) {
                    Logger.error("Failed to get separator from inbox folder, using '/' as default", e, MailReceiver.class);
                }
            } 
            else {
                aSubFolderName = "";
            }
        }

        // no inbox
        if (StringServices.isEmpty(theStandardInboxName)) {
            throw new IllegalStateException("No standard inbox defined!");
        } 
        // theSubFolderName is in correct format
        else if (aSubFolderName.startsWith(theStandardInboxName)) { 
            return aSubFolderName;
        }

        if (aParent == null) {
            return theStandardInboxName + thePostfix;
        } 
        else {
            return aParent.getFullName() + thePostfix;
        }
    }

}
