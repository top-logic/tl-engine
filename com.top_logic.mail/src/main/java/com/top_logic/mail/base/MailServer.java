/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.base;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;

import com.top_logic.basic.Log;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KBBasedManagedClass;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.mail.proxy.MailMessage;
import com.top_logic.mail.proxy.MailReceiver;
import com.top_logic.mail.proxy.MailReceiverService;
import com.top_logic.mail.proxy.exchange.ExchangeMail;

/**
 * {@link ManagedClass} for creating model instances for received mails.
 * 
 * <p>
 * All mail folders are located on the mail server in the {@link MailServer#getRootFolderName() root
 * folder}.
 * </p>
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
@Label("Mail processor")
@ServiceDependencies({
	MailReceiverService.Module.class
})
public class MailServer extends KBBasedManagedClass<MailServer.Config> implements MessageCountListener {

	/**
	 * Configuration of a mail folder.
	 * 
	 * @author <a href="mailto:christian.canterino@top-logic.com">Christian Canterino</a>
	 */
	public interface NamedFolder extends NamedConfigMandatory {

		/**
		 * Indicates whether this is a folder directly located in the INBOX (<code>false</code>) or
		 * it is a sub-folder of the project-specific standard-folder (<code>true</code>,
		 * default-value).
		 * 
		 * @see Config#getRootFolderName()
		 */
		@BooleanDefault(true)
		boolean isInStandardFolder();
	}

	/**
	 * Configuration of the {@link MailServer}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends KBBasedManagedClass.Config<MailServer> {

		/** Name of property {@link #isActivated()}. */
		String IS_ACTIVATED_ATTRIBUTE = "activated";

		/** Name of property {@link #getRootFolderName()}. */
		String ROOT_FOLDER_NAME_ATTRIBUTE = "root-folder";

		/** Default for {@link #getRootFolderName()}. */
		String ROOT_FOLDER_NAME_DEFAULT = "Mails";

		/** Property name of {@link #getMissingFolderDelay()}. */
		String MISSING_FOLDER_DELAY = "missing-folder-delay";

		/** Property name of {@link #getExcludeFromListenerFolders()}. */
		String EXCLUDE_FROM_LISTENER = "exclude-from-listener";

		/**
		 * Name of the root folder.
		 */
		@Name(ROOT_FOLDER_NAME_ATTRIBUTE)
		@StringDefault(ROOT_FOLDER_NAME_DEFAULT)
		String getRootFolderName();

		/**
		 * Whether the {@link MailServer} is activated.
		 */
		@Name(IS_ACTIVATED_ATTRIBUTE)
		boolean isActivated();

		/**
		 * The delay <em>in milliseconds</em> after a folder was NOT found on the server, before the
		 * next time the server is contacted to check if it exists.
		 * <p>
		 * This is necessary to reduce the amount of network accesses for "com.top_logic.mail",
		 * which slows down top-logic a lot.
		 * </p>
		 */
		@LongDefault(10 * 1000)
		@Name(MISSING_FOLDER_DELAY)
		long getMissingFolderDelay();

		/**
		 * A List of folders where no listener should be attached to prevent the mails in
		 *         this folder from further processing, e.g. creating wrapper-representations for
		 *         all its content.
		 */
		@EntryTag("folder")
		@Name(EXCLUDE_FROM_LISTENER)
		List<NamedFolder> getExcludeFromListenerFolders();

	}

    /** Reference to folders already found. */
	private final Map<String, MailFolder> _folderCache;

	private final Map<String, Long> _folderCacheMisses = new HashMap<>();

	private final String _rootFolder;

	private final boolean _actived;

	private final long _missingFolderDelay;

	private final Set<String> _excludeFolderFromListener;

	/**
	 * Creates a new {@link MailServer} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link MailServer}.
	 */
	public MailServer(InstantiationContext context, Config config) {
		super(context, config);
		_rootFolder = config.getRootFolderName();
		_actived    = config.isActivated();
		_folderCache   = new HashMap<>();
		_missingFolderDelay = config.getMissingFolderDelay();

		initRootFolder(context);
		_excludeFolderFromListener = initExcludeFolder(config);
	}

	private Set<String> initExcludeFolder(Config config) {
		if (!_actived) {
			return Collections.emptySet();
		}
		Set<String> res = new HashSet<>();
		try {
			Folder inbox = MailReceiverService.getMailReceiverInstance().getInbox();
			for (NamedFolder folder : config.getExcludeFromListenerFolders()) {
				Folder target;
				if (folder.isInStandardFolder()) {
					target = inbox.getFolder(getRootFolderName());
				} else {
					target = inbox;
				}
				res.add(MailReceiver.getFullName(target, folder.getName()));
			}
		} catch (Exception ex) {
			Logger.warn("Unable to get names for exclude-folder.", ex, MailServer.class);
		}
		return res;
	}

	private void initRootFolder(Log log) {
		if (!_actived) {
			return;
		}
		Transaction tx = kb().beginTransaction(Messages.INITIALISING_ROOT_FOLDER);
		try {
			getRootFolder();
		} catch (Exception ex) {
			// This occurs if the MailServer is temporarily not accessible. An error can not be
			// logged to the protocol, because this prevents start up.
			Logger.error("Unable to connect to root folder.", ex, MailServer.class);
		}
		try {
			tx.commit();
		} catch (KnowledgeBaseException ex) {
			log.error("Unable to commit root folder.", ex);
		} finally {
			tx.rollback();
		}
	}

    @Override
	public void messagesAdded(MessageCountEvent event) {
		Message[] messages = event.getMessages();

		int messageCnt = messages.length;
		if (messageCnt > 0) {
			Transaction tx = kb().beginTransaction();
			try {
				for (int position = 0; position < messageCnt; position++) {
					Message message = messages[position];
					try {
						createMail(new ExchangeMail(message));
					} catch (Exception ex) {
						Logger.warn("Unable to create mail wrapper for " + message, ex, this);
					}
				}
				tx.commit();
			} catch (KnowledgeBaseException ex) {
				Logger.warn("Unable to commit new mails.", ex, this);
			} finally {
				tx.rollback();
			}
        }
    }

    @Override
	public void messagesRemoved(MessageCountEvent arg0) {
    	// No messages here.
    }

    /**
	 * Create a hasMailBox association between an existing {@link MailFolder} an a
	 * {@link MailFolderAware}
	 * 
	 * @param folderAware
	 *        The mail folder aware object to bind the mail folder to, must not be
	 *        <code>null</code>.
	 * @return <code>true</code>, if binding succeeds.
	 */
	public boolean bindMailFolderAware(MailFolderAware folderAware) {
		return bindMailFolderAware(folderAware, this);
    }

    /**
	 * Create a hasMailBox association between an existing {@link MailFolder} an a
	 * {@link MailFolderAware}
	 * 
	 * @param folderAware
	 *        The mail folder aware object to bind the mail folder to, must not be
	 *        <code>null</code>.
	 * @param listener
	 *        Listener to be notified (ignored in here).
	 * @return <code>true</code>, if binding succeeds.
	 */
	protected boolean bindMailFolderAware(MailFolderAware folderAware, MessageCountListener listener) {
        try {
			MailFolder folder = getFolder(folderAware, listener);

			if (folder != null) {
				KnowledgeObject ko = folder.tHandle();
				KnowledgeObject folderAwareKO = folderAware.tHandle();
				Iterator<KnowledgeAssociation> iterator =
					folderAwareKO.getOutgoingAssociations(MailFolder.PARENT_LINK, ko);

				if (iterator == null || !iterator.hasNext()) {
					KnowledgeAssociation ka =
						ko.getKnowledgeBase().createAssociation(folderAwareKO, ko, MailFolder.PARENT_LINK);

					return (ka != null);
                }
                else {
                    return true;
                }
            }
            else {
				Logger.warn("Unable to find folder for mail folder aware " + folderAware, MailServer.class);
            }
        }
        catch (DataObjectException ex) {
			Logger.error("Unable to append mail folder to mail folder aware " + folderAware, ex, MailServer.class);
        }

        return false;
    }

    /**
	 * Return the project mail folder for the given project.
	 * 
	 * If the given element is a project, this method will return the mail folder, otherwise
	 * <code>null</code>.
	 * 
	 * @param folderAware
	 *        The project to get the mail folder for.
	 * @return The requested folder or <code>null</code>.
	 * @see #getFolder(MailFolderAware, MessageCountListener)
	 */
	public MailFolder getFolder(MailFolderAware folderAware) {
		return getFolder(folderAware, this);
    }

    /**
	 * Return the project mail folder for the given project.
	 * 
	 * If the given element is a project, this method will return the mail folder, otherwise
	 * <code>null</code>.
	 * 
	 * @param folderAware
	 *        The project to get the mail folder for.
	 * @param listener
	 *        Listener to be notified (ignored in here).
	 * @return The requested folder or <code>null</code>.
	 * @see #addMessageCountListener(MailFolder, MessageCountListener)
	 */
	protected MailFolder getFolder(MailFolderAware folderAware, MessageCountListener listener) {
		if (folderAware == null) {
			return null;
		}
		MailFolder folder = getMailFolder(folderAware);
		addMessageCountListener(folder, listener);
		return folder;
    }

    /**
     * Get a {@link MailFolder} with the given name. Returns <code>null</code>, if no
     * such folder exists.
     */
	public MailFolder getFolder(String name) {
		return getFolder(name, this);
    }

    /**
     * Get a {@link MailFolder} with the given name. Returns <code>null</code>, if no
     * such folder exists.
     * 
     * If the Folder is accessible, a {@link MessageCountListener} will be added to that
     * folder
     */
	public MailFolder getFolder(String name, MessageCountListener listener) {
		MailFolder folder = getMailFolder(name, true);
		addMessageCountListener(folder, listener);

		return folder;
    }

    /**
     * Add a {@link MessageCountListener} to a {@link MailFolder}
     */
	private void addMessageCountListener(MailFolder folder, MessageCountListener listener) {
		if (listener == null) {
			return;
		}
		if (!excludeFolderFromListener(folder)) {
			folder.addMessageCountListener(listener);
		}
    }

	private boolean excludeFolderFromListener(MailFolder folder) {
		if (folder == null) {
			return true;
		}
		return _excludeFolderFromListener.contains(folder.getName());
	}

	/**
	 * This method creates a folder at the given aware.
	 * 
	 * <p>
	 * It does not register any listeners to the folder!
	 * </p>
	 * 
	 * @param folderAware
	 *        Object the folder belongs to.
	 * @return The requested folder.
	 */
	public MailFolder createMailFolder(MailFolderAware folderAware) {
		return getMailFolder(folderAware, true);
    }

    /**
	 * This method creates a folder by its name.
	 * 
	 * <p>
	 * It does not register any listeners to the folder!
	 * </p>
	 * 
	 * @param name
	 *        Name of the requested folder.
	 * @return The requested folder.
	 */
	public MailFolder createMailFolder(String name) {
		return getMailFolder(name, true);
    }

    /**
	 * This method returns the folder at the given aware.
	 * 
	 * @param folderAware
	 *        Object the folder belongs to.
	 * @return A folder or <code>null</code>
	 */
	protected MailFolder getMailFolder(MailFolderAware folderAware) {
		return getMailFolder(folderAware, false);
    }

	private synchronized MailFolder getMailFolder(MailFolderAware folderAware, boolean createIfMissing) {
		String quotedName = getQuotedFolderName(folderAware.getMailFolderName());
		MailFolder folder = getMailFolderFromCache(quotedName);
		if (folder == null) {
			folder = getMailFolderFromAssociation(folderAware);
			if (folder == null) {
				folder = getFolderUncached(quotedName, createIfMissing);
			}
			if (folder != null) {
				addMailFolderToCache(quotedName, folder);
			}
		}
		return folder;
	}

	private synchronized MailFolder getMailFolder(String folderName, boolean createIfMissing) {
		String quotedName = getQuotedFolderName(folderName);
		MailFolder folder = getMailFolderFromCache(quotedName);
		if (folder == null) {
			folder = getFolderUncached(quotedName, createIfMissing);

			if (folder != null) {
				addMailFolderToCache(quotedName, folder);
			}
		}
		return folder;
    }

	/**
	 * @param folderName
	 *        The <em>quoted</em> folder name.
	 */
	private MailFolder getFolderUncached(String folderName, boolean createIfMissing) {
		return getRootFolder().getFolder(folderName, createIfMissing);
	}

	/**
	 * Returns an accessible {@link MailFolder} from the local cache.
	 * 
	 * @param name
	 *        The <em>quoted</em> folder name.
	 * @return null, if the folder is not cached or not accessible.
	 */
	private MailFolder getMailFolderFromCache(String name) {
		Long cacheMiss = _folderCacheMisses.get(name);
		if (cacheMiss != null && shouldSuppressRetry(cacheMiss)) {
			return null;
		}
		MailFolder folder = _folderCache.get(name);
		if (folder == null) {
			_folderCacheMisses.put(name, System.currentTimeMillis());
			return null;
		}
		if (!folder.tValid()) {
			_folderCache.remove(name);
			return null;
		}
		return folder;
    }

	private boolean shouldSuppressRetry(long cacheMiss) {
		return (cacheMiss + _missingFolderDelay) > System.currentTimeMillis();
	}

    /**
	 * Add a {@link MailFolder} to the local cache.
	 * 
	 * @param folderName
	 *        The <em>quoted</em> folder name.
	 */
	private void addMailFolderToCache(String folderName, MailFolder folder) {
		_folderCache.put(folderName, folder);
		_folderCacheMisses.remove(folderName);
    }

    /**
	 * Try to get the mail folder from the given project.
	 * 
	 * @param folderAware
	 *        The project to get the mail folder for, must not be <code>null</code>.
	 * @return The requested mail folder or <code>null</code>, if not found or errors occurred.
	 */
	protected MailFolder getMailFolderFromAssociation(MailFolderAware folderAware) {
        try {
			Iterator<KnowledgeAssociation> iterator =
				folderAware.tHandle().getOutgoingAssociations(MailFolder.PARENT_LINK);

			if (iterator.hasNext()) {
				KnowledgeAssociation ka = iterator.next();

				return MailFactory.getMailFolder(ka.getDestinationObject());
            }
        }
        catch (Exception ex) {
			Logger.warn("Unable to get mail folder by association from " + folderAware, ex, MailServer.class);
        }

        return null;
    }

    /**
     * Return the parent mail folder for all project folders.
     * 
     * @return The requested parent folder.
     */
	public synchronized MailFolder getRootFolder() {
		String rootFolderName = getRootFolderName();
		String quotedName = getQuotedFolderName(rootFolderName);
		MailFolder rootFolder = getMailFolderFromCache(quotedName);
		if (rootFolder == null) {
			/* TODO Ticket #20852: root folder is Projects but stored as INBOX/Projects in the KB so
			 * it will never be found inside the MailFactory -> getObjectByAttribute()-method */
			rootFolder = MailFactory.getFolder(quotedName, true);
			addMailFolderToCache(quotedName, rootFolder);
		}
		return rootFolder;
    }

    /** 
     * Name of root folder all mails and folders are located in.
     * 
     * @return   Name of the requested folder.
     */
    protected String getRootFolderName() {
		return _rootFolder;
    }

    /**
	 * Return the quoted name of the mail folder for the one.
	 * 
	 * @param name
	 *        The folder name.
	 * @return The name of the mail folder or <code>null</code>, if given project is
	 *         <code>null</code>.
	 */
	public String getQuotedFolderName(String name) {
		if (name == null) {
    		return null;
    	}
    	else {
	        return StringServices.replace(StringServices.replace(StringServices.replace(
				name, '.', "_"), '/', "_"), ' ', "_");
    	}
    }

    /**
	 * Create a {@link Mail mail} for the given {@link MailMessage mail message} from mail server).
	 * 
	 * @param mail
	 *        The mail message to create a new mail for, must not be <code>null</code>.
	 * @return The new created mail, may be <code>null</code>.
	 */
	public Mail createMail(MailMessage mail) {
		Folder folder = mail.getMessage().getFolder();

		if (folder != null) {
			MailFolder mailFolder = MailFactory.getMailFolder(folder);

			if (mailFolder != null) {
				return mailFolder.createMail(mail);
            }
        }

        return null;
    }

    /**
     * Check, if mail server handling is activated in this installation.
     * 
     * @return <code>true</code>, if there is a section in the properties.
     */
    public static boolean isActivated() {
		if (!Module.INSTANCE.isActive()) {
			return false;
		}
		return getInstance()._actived;
    }

    /**
     * Return the only instance of this class.
     * 
     * The instance can be configured in the DispatchService section of the configuration.
     * 
     * @return The only instance of this class.
     */
    public static MailServer getInstance() {
		return Module.INSTANCE.getImplementationInstance();
    }

	/**
	 * Module for {@link MailServer}.
	 * 
	 * @since 5.7.5
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class Module extends TypedRuntimeModule<MailServer> {

		/** Singleton {@link MailServer.Module} instance. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// singleton instance
		}

		@Override
		public Class<MailServer> getImplementation() {
			return MailServer.class;
		}
	}
}
