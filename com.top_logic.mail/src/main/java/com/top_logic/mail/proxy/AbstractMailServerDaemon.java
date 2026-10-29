/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.proxy;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import jakarta.mail.Address;
import jakarta.mail.Flags.Flag;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessageRemovedException;
import jakarta.mail.MessagingException;
import jakarta.mail.Store;
import jakarta.mail.UIDFolder;
import jakarta.mail.event.ConnectionEvent;
import jakarta.mail.event.ConnectionListener;
import jakarta.mail.event.MessageCountAdapter;
import jakarta.mail.event.MessageCountEvent;
import jakarta.mail.event.MessageCountListener;
import jakarta.mail.internet.InternetAddress;

import com.top_logic.base.context.TLInteractionContext;
import com.top_logic.base.mail.MailSenderService;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.util.TokenBasedTask;
import com.top_logic.mail.base.MailFolder;
import com.top_logic.mail.base.MailFolderAware;
import com.top_logic.mail.base.MailPreprocessor;
import com.top_logic.mail.base.MailServer;
import com.top_logic.mail.proxy.exchange.ExchangeMail;
import com.top_logic.mail.proxy.exchange.ExchangeMeeting;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.Utils;

/**
 * At scheduled times, the AbstractMailServerDaemon takes a look at the INBOX
 * of a mail server. The Daemon itself only invokes the MailDaemonCountListener by
 * sending a MessageCountEvent. The MailDaemonCountListener then converts the {@link Message}s to
 * {@link MailServerMessage}s and calls back AbstractMailServerDaemon#processMail(MailMessage)
 * resp.AbstractMailServerDaemon#processMeeting(ExchangeMeeting) to handle these mails.
 *
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class AbstractMailServerDaemon<C extends AbstractMailServerDaemon.Config<?>> extends TokenBasedTask<C> {
	
	/**
	 * Configuration for the mail server daemon.
	 * 
	 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public interface Config<I extends AbstractMailServerDaemon<?>> extends TokenBasedTask.Config<I> {

		/**
		 * Configuration name of {@link #getInitialMails()}.
		 */
		String INITIAL_MAILS = "initial-mails";

		/**
		 * <code>true</code> for activating mail server daemon.
		 */
		Boolean isActivated();

		/**
		 * Strategy to handle mails that cannot be assigned to an object.
		 * 
		 * <p>
		 * By default, such a mail is left untouched in the INBOX ({@link UnknownMailStrategy#KEEP}).
		 * Deleting it ({@link UnknownMailStrategy#DELETE}) must be configured explicitly.
		 * </p>
		 */
		@FormattedDefault("KEEP")
		UnknownMailStrategy getStrategyForUnknownMails();
		
		/**
		 * Name of folder to move unknown mails to.
		 * 
		 * @see UnknownMailStrategy#MOVE
		 */
		@StringDefault("unbekannt")
		String getFolderNameForUnknownMails();

		/**
		 * Preprocessors for handling incoming mails.
		 */
		@Format(CommaSeparatedStrings.class)
		List<String> getPreprocessor();

		/**
		 * How to handle the mails already present in the INBOX when the daemon reads it for the
		 * first time.
		 * 
		 * <p>
		 * The daemon remembers the UID of the last mail it has handled and reads only mails with
		 * a greater UID in each run. This option decides what happens with the mails present when
		 * no such mark exists yet, or when the server has renumbered the mails of the INBOX. By
		 * default, these mails are left untouched ({@link InitialMails#SKIP}).
		 * </p>
		 */
		@Name(INITIAL_MAILS)
		@FormattedDefault("SKIP")
		InitialMails getInitialMails();

		/**
		 * Default value for begin/commit is <code>false</code> in mail daemon.
		 */
		@Override
		@BooleanDefault(false)
		boolean isUseCommit();
	}

	private static final Property<Integer> PROCESSED_MAILS = TypedAnnotatable.property(Integer.class, "processedMails", Integer.valueOf(0));

	private static final int ONE_HOUR = (1000 * 60 * 60);

    private static final int RETRY   = 3;

    /**
     * Strategy for mails that cannot by identified or mails that triggered an
     * exception during the standard processing.
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public enum UnknownMailStrategy implements ExternallyNamed {
		/** Delete the mail. */
		DELETE,
		/** Move the mail to a special folder. */
		MOVE,
		/** Leave the mail unchanged in the INBOX. */
		KEEP;

		@Override
		public String getExternalName() {
			return name();
		}
	}

    /** The mail folder on the mail server observed by this daemon. */
    Folder                  folder;
    /** When false Task will not anything */
    private boolean                 activated;
    private MailReceiver         server;
    private MailDaemonCountListener folderListener;
    private long                    errorTime;

    /**
     * @see UnknownMailStrategy
     */
    private UnknownMailStrategy unknownMailStrategy;
    
    /**
	 * Name of the folder where unidentified mails are moved to.
	 * 
	 * @see UnknownMailStrategy#MOVE
	 */
    private String folderNameForUnknownMails;

	private List<MailPreprocessor> preprocessors;

	/** Configuration of this daemon. */
	protected final C _config;

	/**
	 * Storage of the {@link UIDMark} of the INBOX, created on first use.
	 */
	private UIDMarkStore _uidMarkStore;

	/**
	 * The {@link UIDMark} the current run has fetched the messages from.
	 * 
	 * @see #getMessages()
	 */
	private UIDMark _fetchMark;

	/**
	 * Creates a {@link AbstractMailServerDaemon}.
	 */
	public AbstractMailServerDaemon(InstantiationContext context, C config) {
    	super(context, config);
    	_config = config;
    	activated = Utils.isTrue(config.isActivated());
    	internalReload();
    }

	/**
	 * Perform the initialize sequence again.
	 */
    public void reload() {
		this.internalReload();
    }

	/**
	 * Initializing sequence to configure this instance.
	 */
    protected void internalReload() {
        this.unknownMailStrategy = _config.getStrategyForUnknownMails();
        
        if (UnknownMailStrategy.MOVE == this.unknownMailStrategy) {
            this.folderNameForUnknownMails = _config.getFolderNameForUnknownMails();
        }

		// init preprocessors
		this.preprocessors = new ArrayList<>();

		for (String theClassName : _config.getPreprocessor()) {
			try {
				@SuppressWarnings("unchecked")
				Class<MailPreprocessor>       theClass = (Class<MailPreprocessor>) Class.forName(theClassName);
				Constructor<MailPreprocessor> theCtor  = theClass.getConstructor(new Class[] {});

				if (this.addPreprocessor(theCtor.newInstance(new Object[] {}))) {
					Logger.info("Added " + theClassName + " as Preprocessor", AbstractMailServerDaemon.class);
                }
			} catch (Exception e) {
				Logger.error("Cannot instanciate the preprocessor " + theClassName, e, AbstractMailServerDaemon.class);
            }
		}

		if (this.preprocessors.isEmpty()) {
			Logger.info("No preprocessors configured", AbstractMailServerDaemon.class);
        }

        if (!this.activated) {
			Logger.info(
				"Mail daemon is deactivated. To receive mail, change configuration and restart the scheduler or set the daemon active.",
				AbstractMailServerDaemon.class);
        }
        else {
			this.server = MailReceiverService.getMailReceiverInstance();
			Logger.info("Started mail processing.", AbstractMailServerDaemon.class);
        }
	}

    /**
	 * Process a mail within the application.
	 *
	 * If the mail was send from ourself, {@link #processMailFromSelf(MailMessage)} is called.
	 * Otherwise the mail will be passed to all registered {@link MailPreprocessor}s and if the
	 * pre-processing is successful, the mail will be process internally.
	 *
	 * @param aMail
	 *        The mail to be processed.
	 * @return <code>true</code>, if processing succeeds and mail can accepted, <code>false</code>,
	 *         otherwise.
	 */
    protected boolean processMail(MailMessage aMail) {
        if (aMail.isRemoved()) {
			Logger.info("Skipped mail number " + aMail.getMessage().getMessageNumber()
				+ ", it was already removed from the folder by another client.", AbstractMailServerDaemon.class);
            return false;
        }

        try {
        	if (this.isReportMail(aMail) && this.processReportMail(aMail)) {
        		return true;
        	}
            if (this.isMailFromSelf(aMail) && this.processMailFromSelf(aMail)) {
                return true;
            }
			if (this.preprocessMail(aMail)) {
                return processMailInternal(aMail);
            }
        } catch (Exception ex) {
            if (ex instanceof MessageRemovedException || aMail.isRemoved()) {
				Logger.info("Stopped processing mail number " + aMail.getMessage().getMessageNumber()
					+ ", it was removed from the folder by another client.", AbstractMailServerDaemon.class);
            }
            else if (ex instanceof MessagingException) {
                Logger.error("Unable to process mail due to messaging problems!" + aMail.getName(), ex, AbstractMailServerDaemon.class);
            }
            else {
                Logger.error("Unable to process mail " + aMail.getName(), ex, AbstractMailServerDaemon.class);
                return this.processUnknownMail(aMail);
            }
        }
        return false;
    }

	/** 
	 * Check, if given mail is a server report mail.
	 * 
	 * @param    aMail    Mail to be inspected.
	 * @return   <code>true</code> when mail is a {@link MailMessage#isReportMail()}.
	 */
	protected boolean isReportMail(MailMessage aMail) {
		return aMail.isReportMail();
	}

	/**
	 * Process a mail according to the configured {@link UnknownMailStrategy}.
	 */
    protected boolean processUnknownMail(MailMessage aMail) {
        switch (this.unknownMailStrategy) {
			case KEEP:
				return true;
			case DELETE:
				return deleteMail(aMail);
			case MOVE:
				return moveMailToFolder(aMail, this.folderNameForUnknownMails);
			default:
				throw new IllegalStateException("Strategy for unknown mails is not configured but it should!");
        }
    }
    
    /**
	 * Check if a 'from'-header of a mail contains the sender address of the mail system.
	 *
	 * @param    aMail    Mail received.
	 * @return   <code>true</code>, if a 'from' address equals the configured one.
	 * @throws   MessagingException    When parsing the 'from' address fails.
	 * @see      MailSenderService#getFromAddress()
	 */
    protected final boolean isMailFromSelf(MailMessage aMail) throws MessagingException {
		String theServerAddress = MailSenderService.getFromAddress();

		if (!StringServices.isEmpty(theServerAddress)) {
			theServerAddress = theServerAddress.toLowerCase();

			for (Address theFrom : aMail.getMessage().getFrom()) {
				if (theFrom instanceof InternetAddress) {
					String theAddress = ((InternetAddress) theFrom).getAddress().toLowerCase();

					if (theAddress.equals(theServerAddress)) {
                        return true;
                    }
                }
            }
        }

		return false;
    }

    /**
     * Check if the Mail is from our own server (e.g. send via BCC) and handle this mail.
     */
    protected abstract boolean processMailFromSelf(MailMessage aMail);
    
    /**
     * Check if the mail has been send by the mail server itself (e.g. cannot deliver mail) and handle this mail.
     */
    protected abstract boolean processReportMail(MailMessage aMail);

    /**
     * Handle a mail that comes from an external sender.
     */
    protected abstract boolean processMailInternal(MailMessage aMail);

    /**
     * Process a meeting within the application.
     *
     * @param    aMeeting    The meeting to be processed.
     * @return   <code>true</code>, if processing succeeds and meeting can accepted,
     *           <code>false</code>, when meeting has to be declined.
     * @see      ExchangeMeeting#reply(boolean)
     */
    protected abstract boolean processMeeting(ExchangeMeeting aMeeting);

    @Override
	protected String getLockOperation() {
		return "checkMail";
    }

    /**
     * Change the activation flag of this daemon.
     *
     * @param    aFlag    The new activation state of this daemon.
     */
    public void setActivated(boolean aFlag) {
        if (this.activated != aFlag) {
            this.activated = aFlag;
            this.reload();
        }
    }

    /**
	 * Handle new mails via the {@link #sendMessageCountEvent()}.
	 */
    @Override
	protected boolean perform() {
		if (!this.activated) {
			return false;
		}

		resetFolder();

		// The message count event is sent when the new folder is allocated.
		return this.getFolder() != null;
    }

	private void resetFolder() {
		if (folder != null) {
			try {
				// This call may trigger a connection closed event.
				folder.close(false);
			} catch (Exception ex) {
				// Ignore.
			}
			MailReceiverService.getMailReceiverInstance().closed(new ConnectionEvent(folder, ConnectionEvent.CLOSED));
			disconnectFromFolder();
		}
	}

    /**
     * Connect to the INBOX and register {@link MessageCountListener} and {@link ConnectionListener}
     * to it.
     */
    protected void connectToFolder() {

        try {
            MailDaemonCountListener theListener = this.getFolderListener();

            this.folder = this.server.getInbox();
            this.folder.addConnectionListener(theListener);
			// TODO Ticket #20853: fall back to normal method, register a listener on the folder.
			// this.folder.addMessageCountListener(theListener);

            this.sendMessageCountEvent();

            this.errorTime = 0L;

            Logger.info("Daemon on server '" + this.server + "' activated!", AbstractMailServerDaemon.class);
        }
        catch (Exception ex) {
        	// Do not log problem for one hour...
            if (this.errorTime == 0L) {
                Logger.warn("Unable to connect message count listener to mail server, daemon will continue trying to connect!", ex, AbstractMailServerDaemon.class);

                this.errorTime = System.currentTimeMillis();
            }
            else if ((System.currentTimeMillis() - this.errorTime) > AbstractMailServerDaemon.ONE_HOUR) {
                this.errorTime = 0L;
            }

            this.folder = null;
        }
    }

    /**
     * Disconnect from the INBOX and unregister Listeners
     *
     */
    protected void disconnectFromFolder() {
        if (this.folder != null) {
            try {
                MailDaemonCountListener theListener = this.getFolderListener();

                this.folder.removeConnectionListener(theListener);
//                this.folder.removeMessageCountListener(theListener);
            }
            catch (Exception ex1) {
                Logger.warn("Also unable to remove listeners from folder " + this.folder, ex1, AbstractMailServerDaemon.class);
            }

            Logger.warn("Disconnecting from " + this.folder + "!", AbstractMailServerDaemon.class);
            this.folder = null;
        }
    }

	/**
	 * Send a message count event to the {@link #getFolderListener()}.
	 * 
	 * TODO Ticket #20853: fall back to normal method, register a listener on the folder.
	 * 
	 * @return <code>true</code> always.
	 * @throws MessagingException
	 *         When getting messages from {@link #getFolder()} fails.
	 */
    protected boolean sendMessageCountEvent() throws MessagingException {
        List<Message> theList = this.getMessages();

        if (!theList.isEmpty()) {
            Message[]         theMessages = theList.toArray(new Message[theList.size()]);
            MessageCountEvent theEvent    = new MessageCountEvent(this.getFolder(), theMessages.length, true, theMessages);

            this.getFolderListener().messagesAdded(theEvent);
        }

        TLInteractionContext interaction = TLContextManager.getInteraction();
		interaction.set(PROCESSED_MAILS, Integer.valueOf(interaction.get(PROCESSED_MAILS).intValue() + theList.size()));
        return true;
    }

	@Override
	protected ResKey getResultMessageKey(String duration) {
		TLInteractionContext interaction    = TLContextManager.getInteraction();
		Integer              processedMails = interaction.get(PROCESSED_MAILS);

		return I18NConstants.FETCH_MAILS_COMPLETED_SUCCESSFULLY__COUNT_DURATION.fill(processedMails, duration);
	}

	/**
	 * This method returns all new messages from the INBOX.
	 * 
	 * <p>
	 * New messages are the ones with an UID greater than the stored {@link UIDMark} of this
	 * daemon, in ascending UID order. If no mark applies to the INBOX, it is initialized according
	 * to {@link Config#getInitialMails()}.
	 * </p>
	 *
	 * @return The requested list of messages.
	 * @throws MessagingException
	 *         When getting messages from {@link #getFolder()} fails.
	 */
	protected List<Message> getMessages() throws MessagingException {
		_fetchMark = null;

		UIDFolder uidFolder = getUIDFolder(this.getFolder());
		long uidValidity = uidFolder.getUIDValidity();
		UIDMarkStore store = getUIDMarkStore();
		UIDMark stored = store.load();
		UIDMark mark = UIDMark.start(stored, uidValidity, _config.getInitialMails(), () -> highestUID(uidFolder));
		if (mark != stored) {
			Logger.info("Initialized UID mark '" + store.getKey() + "' of INBOX to " + mark + " (stored mark: "
				+ stored + ", initial mails: " + _config.getInitialMails() + ").", AbstractMailServerDaemon.class);
			store.store(mark);
		}

		List<Message> theList = new ArrayList<>();
		long lastUID = mark.getLastUID();
		for (Message theMessage : uidFolder.getMessagesByUID(lastUID + 1, UIDFolder.LASTUID)) {
			try {
				// The server may report the last message, even if its UID is not in the range.
				if (uidFolder.getUID(theMessage) > lastUID) {
					theList.add(theMessage);
				}
			} catch (MessageRemovedException mrex) {
				Logger.warn("Found a removed message in INBOX. Ignoring this message!", AbstractMailServerDaemon.class);
			}
		}
		_fetchMark = mark;

		Logger.info("Found " + theList.size() + " new mails on server after UID mark " + mark + ".",
			AbstractMailServerDaemon.class);

		return theList;
	}

	private static UIDFolder getUIDFolder(Folder folder) throws MessagingException {
		if (folder instanceof UIDFolder) {
			return (UIDFolder) folder;
		}
		throw new MessagingException("Folder '" + folder + "' does not support message UIDs.");
	}

	private static Long highestUID(UIDFolder uidFolder) throws MessagingException {
		long lastMessageUID = 0;
		long uidNext = uidFolder.getUIDNext();
		if (uidNext <= 0) {
			Folder folder = (Folder) uidFolder;
			int count = folder.getMessageCount();
			if (count > 0) {
				lastMessageUID = uidFolder.getUID(folder.getMessage(count));
			}
		}
		return Long.valueOf(UIDMark.highestUID(uidNext, lastMessageUID));
	}

	/**
	 * The storage of the {@link UIDMark} of the INBOX read by this daemon.
	 */
	protected UIDMarkStore getUIDMarkStore() {
		if (_uidMarkStore == null) {
			KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
			_uidMarkStore = UIDMarkStore.forName(new DBProperties(KBUtils.getConnectionPool(kb)), getName());
		}
		return _uidMarkStore;
	}

	/**
	 * Stores the {@link UIDMark} after the messages fetched by {@link #getMessages()} have been
	 * handled.
	 *
	 * @param results
	 *        Whether handling succeeded for the message with the given UID.
	 */
	protected void advanceUIDMark(SortedMap<Long, Boolean> results) {
		UIDMark fetchMark = _fetchMark;
		if (fetchMark == null) {
			return;
		}
		UIDMark newMark = fetchMark.advance(results);
		if (newMark != fetchMark) {
			getUIDMarkStore().store(newMark);
			_fetchMark = newMark;
		}
	}

	/**
	 * Return the listener for the mail folder.
	 * 
	 * @return The requested listener (lazy initialization).
	 */
    protected MailDaemonCountListener getFolderListener() {
        if (this.folderListener == null) {
            this.folderListener = new MailDaemonCountListener();
		}

        return this.folderListener;
    }

    /**
	 * Get the INBOX with some retry logic.
	 *
	 * @return The folder the Daemons is working on may be <code>null</code> in case of error.
	 */
    protected Folder getFolder() {
        int theCount = 0;
        while (theCount < RETRY) {
            theCount++;
            if (this.folder == null) {
                this.connectToFolder();
            }

			if (folder != null && isFolderInvalid()) {
                String theNumber = Integer.toString(theCount) + '/' + RETRY;
				Logger.warn("Folder is closed, trying to reconnect (" + theNumber + ")!", AbstractMailServerDaemon.class);
                this.disconnectFromFolder();
                this.connectToFolder();
            }

            if (this.folder != null) {
                return this.folder;
            }
        }

        Logger.error("Unable to get INBOX after multiple retry, this may cause inconsistent server/KO data!", AbstractMailServerDaemon.class);

        return null;
    }


	private boolean isFolderInvalid() {
		if (!folder.isOpen()) {
			return true;
		}
		/* Check that store is still connected. IMAPStore uses this method to verify that the server
		 * does not have terminated the connection. After call of this method and the result is
		 * false, the folder is not longer usable. */
		Store store = folder.getStore();
		if (!store.isConnected()) {
			return true;
		}
		return false;
	}

    /**
     * Return the text to be send in a mail, which cannot be assigned to a project.
     *
     * @return    The requested mail (I18N).
     */
    protected String getFailMessage4Meeting() {
		return Resources.getInstance().getString(I18NConstants.ERROR_MEETING_ASSIGNMENT_FAILED);
    }

    /**
     * Hook for post processing the mails.
     *
     * @param    aMail    The first mail processed, may be <code>null</code>.
     * @return   <code>true</code>, if post processing succeeds.
     */
    protected boolean postProcessMail(MailMessage aMail) {
        // At least one mail has been processed.
        if (aMail instanceof ExchangeMail) {
            try {
                ((ExchangeMail) aMail).delete(true);

				return true;
            }
            catch (MessagingException ex) {
				Logger.error("Unable to delete mail from mail server", ex, AbstractMailServerDaemon.class);
            }
        }

		return false;
    }

    /**
     * Hook for post processing the messages.
     *
     * @param    aMessage    The first message processed, may be <code>null</code>.
     * @return   <code>true</code>, if post processing succeeds.
     */
    protected boolean postProcessMessage(MailMeeting aMessage) {
        // At least one meeting has been processed.
        if (aMessage instanceof ExchangeMeeting) {
            try {
                ((ExchangeMeeting) aMessage).delete(false);

				return true;
            }
            catch (MessagingException ex) {
				Logger.error("Unable to delete meeting from mail server", ex, AbstractMailServerDaemon.class);
            }
        }

		return false;
    }

    /**
     * Return a {@link MailFolderAware} for this message.
     *
     * @param    aMessage    The message to find the holder of the folder for.
     * @return   The requested holder of the mail folder the given mail lives in.
     */
    protected abstract MailFolderAware getMailFolderAware(MailServerMessage aMessage);

    /**
     * Pass the mail trough all registered {@link MailPreprocessor}s.
     *
     * @param    aMail    Mail to be processed.
     * @return   <code>true</code>, if all {@link MailPreprocessor}s have returned <code>true</code>.
     */
    protected final boolean preprocessMail(MailMessage aMail) {
        List<MailPreprocessor> thePreprocessors = this.getPreprocessors();

        if (!thePreprocessors.isEmpty()) {
        	MailFolderAware theAware  = this.getMailFolderAware(aMail);
        	boolean         theResult = true;

        	for (MailPreprocessor theProcessor : thePreprocessors) {
        		theResult = theProcessor.preprocessMail(aMail, theAware) && theResult;
	        }

        	return theResult;
        }
        else {
        	return true;
        }
    }

    /**
	 * Get all registered {@link MailPreprocessor}s.
	 */
    protected List<MailPreprocessor> getPreprocessors() {
        return Collections.unmodifiableList(this.preprocessors);
    }

    /**
     * Register a {@link MailPreprocessor}.
     */
    public synchronized boolean addPreprocessor(MailPreprocessor aPreprocessor) {
        if (!this.preprocessors.contains(aPreprocessor)) {
			return this.preprocessors.add(aPreprocessor);
        }
        else {
            Logger.info(aPreprocessor.toString() + " is already registered as preprocessor!", AbstractMailServerDaemon.class);

            return false;
        }
    }

    /**
     * Unregister a {@link MailPreprocessor}
     */
    public synchronized boolean removePreprocessor(MailPreprocessor aPreprocessor) {
        return this.preprocessors.remove(aPreprocessor);
    }

	/**
	 * Move a {@link MailMessage} to the {@link MailFolder} of a {@link MailFolderAware}.
	 * 
	 * If the Folder does not exists, it will be created.
	 */
	protected boolean moveMailToFolder(MailMessage aMail, MailFolderAware anAware) {
		return this.moveMailToFolder(aMail, this.getOrCreateMailFolder(anAware));
	}

	/**
	 * Move a {@link MailMessage} to the {@link MailFolder} named aFoldername If the Folder does not
	 * exists, it will be created. After the operation, the folder will be closed and must be
	 * reconnected.
	 */
	protected boolean moveMailToFolder(MailMessage aMail, String aFoldername) {
		return this.moveMailToFolder(aMail, this.getOrCreateMailFolder(aFoldername));
	}

	/**
	 * Create a {@link MailFolder} named aFoldername
	 */
	protected MailFolder getOrCreateMailFolder(String aFoldername) {
		MailServer theServer = MailServer.getInstance();
		MailFolder theFolder = theServer.getFolder(aFoldername);

		if (theFolder == null) {
			theFolder = theServer.createMailFolder(aFoldername);
		}

		return theFolder;
	}

	/**
	 * Create a {@link MailFolder} for an {@link MailFolderAware} and bind them
	 */
	protected MailFolder getOrCreateMailFolder(MailFolderAware anAware) {
		MailServer theServer = MailServer.getInstance();
		MailFolder theFolder = theServer.getFolder(anAware);

		if (theFolder == null) {
			theFolder = theServer.createMailFolder(anAware);
			theServer.bindMailFolderAware(anAware);
		}

		return theFolder;
	}

	/**
	 * Move a {@link MailMessage} to a {@link MailFolder} and close the folder.
	 */
	protected boolean moveMailToFolder(MailMessage aMail, MailFolder aFolder) {
		boolean theResult = false;
		if (aFolder != null) {
			theResult = aFolder.move(aMail);
			aFolder.disconnect();
		}
		return theResult;
	}

	/**
	 * Set the {@link Flag#DELETED} flag on the given message.
	 * 
	 * <p>
	 * This Message will be delete when its folder is expunged.
	 * </p>
	 *
	 * @param aMailMessage
	 *        Message to be deleted.
	 */
	protected boolean deleteMail(MailServerMessage aMailMessage) {
		return aMailMessage.setFlag(Flag.DELETED, true);
	}

	/**
	 * Reset the {@link Flag#DELETED} and {@link Flag#SEEN} flag on the given message.
	 *
	 * @param aMailMessage
	 *        Message to be marked as unread and not deleted.
	 */
	protected boolean undeleteMail(MailServerMessage aMailMessage) {
		return aMailMessage.setFlag(Flag.SEEN, false) && aMailMessage.setFlag(Flag.DELETED, false);
	}

    /**
     * Performing class for new mails arrived on the mail server.
     *
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public class MailDaemonCountListener extends MessageCountAdapter implements ConnectionListener {

    	@Override
		public void messagesAdded(MessageCountEvent anEvent) {
			Folder theFolder = null;
			int theMailCount = 0;
			int theMeetCount = 0;
			int theMailFail = 0;
			int theMeetFail = 0;

			// Handling result by UID. A message without a successful result has failed.
			Map<Message, Long> theUIDs = new HashMap<>();
			boolean theUIDsComplete = true;
			SortedMap<Long, Boolean> theResults = new TreeMap<>();
			for (Message theMessage : anEvent.getMessages()) {
				try {
					Long theUID = Long.valueOf(getUIDFolder(theMessage.getFolder()).getUID(theMessage));
					theUIDs.put(theMessage, theUID);
					theResults.put(theUID, Boolean.FALSE);
				} catch (MessagingException ex) {
					// Without the UID, the message cannot be accounted for: The mark must not advance.
					theUIDsComplete = false;
					Logger.warn("Unable to get UID of message " + theMessage.getMessageNumber(), ex,
						MailDaemonCountListener.class);
				}
			}

            KnowledgeBase theKB = PersistencyLayer.getKnowledgeBase();
			try (Transaction theTX = theKB.beginTransaction(com.top_logic.mail.base.I18NConstants.RECEIVED_MESSAGES)) {
				for (MailServerMessage theMessage : MailReceiverService.getMailReceiverInstance()
					.convertMessages(anEvent.getMessages())) {
					// All processed messages are from the same folder. get it once, to expunge it
					// later
					if (theFolder == null) {
						theFolder = theMessage.getMessage().getFolder();
					}

					if (theMessage instanceof ExchangeMeeting) {
						ExchangeMeeting theExchange = (ExchangeMeeting) theMessage;
						try {
							boolean success = AbstractMailServerDaemon.this.processMeeting(theExchange);
							String theText = null;

							if (!success) {
								theMeetFail++;
								theText = AbstractMailServerDaemon.this.getFailMessage4Meeting();
							} else {
								theMeetCount++;
							}

							theExchange.reply(success, theText);
							theExchange.delete(false);
							setResult(theResults, theUIDs, theMessage, success);
						} catch (Exception ex) {
							theMeetFail++;
							Logger.error("Unable to reply on meeting " + theExchange.getName(), ex,
								MailDaemonCountListener.class);
						}
					} else if (theMessage instanceof ExchangeMail) {
						ExchangeMail theExchange = (ExchangeMail) theMessage;

						try {
							if (AbstractMailServerDaemon.this.processMail(theExchange)) {
								theMailCount++;
								setResult(theResults, theUIDs, theMessage, true);
							} else {
								theMailFail++;
							}
						} catch (Exception ex) {
							theMailFail++;
							Logger.error("Unable to process mail " + theMessage.getName(), ex,
								MailDaemonCountListener.class);
						}
					}
				}

				String theResultMail = "OK: " + theMailCount + ", Fail: " + theMailFail;
				String theResultMeet = "OK: " + theMeetCount + ", Fail: " + theMeetFail;

				Logger.info("Finished processing mails (" + theResultMail + "), meetings (" + theResultMeet + ")!",
					MailDaemonCountListener.class);

				if (theFolder != null) {
					this.expungeFolder(theFolder);
				}

				try {
					theTX.commit();
				} catch (KnowledgeBaseException ex) {
					Logger.warn("Unable to commit KB!", ex, MailDaemonCountListener.class);
					return;
				}
			}

			if (theUIDsComplete) {
				AbstractMailServerDaemon.this.advanceUIDMark(theResults);
			}
        }

		private void setResult(SortedMap<Long, Boolean> results, Map<Message, Long> uids, MailServerMessage message,
				boolean success) {
			Long uid = uids.get(message.getMessage());
			if (uid != null) {
				results.put(uid, Boolean.valueOf(success));
			}
		}

        /**
         * Expunge a folder to delete flagged messages.
         *
         * @param    aFolder   Folder to be expunged.
         */
        protected void expungeFolder(Folder aFolder) {
            if (aFolder != null) {
                try {
                    aFolder.expunge();
                }
                catch (MessagingException mex) {
                    Logger.error("Unable to expunge folder '" + aFolder.getName() + "'!", mex, MailDaemonCountListener.class);
                }
            }
        }

        // what to to in this case? reconnect the daemon instantly
        // or just set folder=null and reconnect on the next run?
        @Override
		public void closed(ConnectionEvent anEvent) {
			// MailServerProxy.getInstance().closed(anEvent);
        	this.close(anEvent, "closed");
        }

        @Override
        public void disconnected(ConnectionEvent anEvent) {
        	this.close(anEvent, "disconnected");
        }

        @Override
        public void opened(ConnectionEvent anEvent) {
        	Logger.debug("Folder '" + anEvent.getSource() + "' opened!", this);
        }

		// Private methods

		private void close(ConnectionEvent anEvent, String anAction) {
			final Folder outerFolder = AbstractMailServerDaemon.this.folder;
			if (outerFolder == null) {
     			return;
     		}

     		Object theSource  = anEvent.getSource();
     		String theSrcName = null;

     		if (theSource instanceof Store) {
     			theSrcName = ((Store) theSource).getURLName().getFile();
     			AbstractMailServerDaemon.this.folder = null;
				outerFolder.removeConnectionListener(this);
     		} 
     		else if (theSource instanceof Folder) {
     			theSrcName = ((Folder) theSource).getName();

				if (theSrcName.equals(outerFolder.getName())) {
     				AbstractMailServerDaemon.this.folder = null;
					outerFolder.removeConnectionListener(this);
     			}
     		}

     		if (theSrcName != null) {
     			Logger.debug("Folder '" + theSrcName + "' " + anAction + " (thread: "  + Thread.currentThread().toString() + ")!", MailDaemonCountListener.class);
     		}
     	}
    }
}
