/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.top_logic.base.context.SubSessionListener;
import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.AbstractSubSessionContext;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.SessionContext;
import com.top_logic.basic.col.CopyOnChangeListProvider;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.thread.InContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.db2.UpdateChainLink;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.knowledge.wrap.person.PersonalConfigurationWrapper;
import com.top_logic.knowledge.wrap.person.TransientPersonalConfiguration;
import com.top_logic.layout.LayoutContext;

/**
 * This class is attached to the JSPSession and every Thread.
 *
 * {@link com.top_logic.util.TopLogicJspBase} looks up this class in the context
 *  and attaches it to the ThreadInfo. The class itself just is a value holder 
 *  and provides some static utility functions.
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TLContext extends ThreadContext implements TLSubSessionContext {

	/**
	 * Configuration for {@link TLContext}.
	 */
	public interface Config extends ConfigurationItem {
		/** See {@link Config#getPersonalConfiguration}. */
		String PERSONAL_CONFIGURATION = "personalConfiguration";

		/** Getter for {@link Config#PERSONAL_CONFIGURATION}. */
		@Name(PERSONAL_CONFIGURATION)
		@ClassDefault(TransientPersonalConfiguration.class)
		Class<? extends TransientPersonalConfiguration> getPersonalConfiguration();
	}

	/**
	 * Identifier of the initial system {@link Branch}.
	 * 
	 * <p>
	 * Access through {@link HistoryManager#getTrunk()}.
	 * </p>
	 */
	public static final long TRUNK_ID = 1L;
	
	private Branch _sessionBranch;

	private LayoutContext _layoutContext;

	private volatile UpdateChainLink _revision;

	private volatile Person _person;

	private volatile TransientPersonalConfiguration _personalConfig;

	private final CopyOnChangeListProvider<SubSessionListener> _listeners = new CopyOnChangeListProvider<>();

	/**
	 * Need a default C'tor to be create via Class.newInstance().
	 */
	@CalledByReflection
    public TLContext () {
        super ();
    }

	@Override
	public Person getPerson() {
		return _person;
	}

	@Override
	public void setPerson(Person person) {
		Person oldPerson = _person;
		if (person == oldPerson) {
			return;
		}
		_person = person;
		String newContextId;
		Locale newLocale;
		TimeZone newTimeZone;
		if (person == null) {
			newContextId = null;
			newLocale = null; // No user, no Locale
			newTimeZone = null;
		} else {
			newContextId = TLSessionContext.contextId(person);
			newLocale = findBestLocale(person);
			newTimeZone = person.getTimeZone();
		}
		resetPersonalConfiguration();
		setCurrentLocale(newLocale);
		setCurrentTimeZone(newTimeZone);
		internalSetContextId(newContextId);
	}

	@Override
	public void setContextId(String contextId) {
		if (getPerson() != null) {
			throw new IllegalStateException("Thers is a person, no external context id needed");
		}
		internalSetContextId(contextId);
	}

	private void internalSetContextId(String contextID) {
		super.setContextId(contextID);
	}

	private Locale findBestLocale(Person person) {
		return Resources.findBestLocale(person.tHandle());
	}

	@Override
	public void resetPersonalConfiguration() {
		_personalConfig = null;
	}

	@Override
	public void storePersonalConfiguration() {
		TransientPersonalConfiguration config = _personalConfig;

		if (config == null) {
			return;
		}

		_personalConfig = null;
		config.storeConfiguration(this);
	}

	@Override
	public PersonalConfiguration getPersonalConfiguration() {
		TransientPersonalConfiguration theConfig = _personalConfig;

		if (theConfig == null) {
			synchronized (this) {
				theConfig = _personalConfig;
				if (theConfig == null) {
					PersonalConfigurationWrapper theWrapper = null;

					Person currentPerson = getPerson();
					if (currentPerson != null) {
						theWrapper = PersonalConfigurationWrapper.getPersonalConfiguration(currentPerson);
					}
					theConfig = createPersonalConfiguration(currentPerson, theWrapper);
					_personalConfig = theConfig;
				}
			}
		}

		return theConfig;
	}

	private static TransientPersonalConfiguration createPersonalConfiguration(Person currentPerson,
			PersonalConfigurationWrapper persistentConfig) {
		Class<? extends TransientPersonalConfiguration> personalConfigClass = getConfiguredPersonalConfiguration();
		try {
			TransientPersonalConfiguration personalConfig = personalConfigClass.newInstance();
			personalConfig.init(currentPerson, persistentConfig);
			return personalConfig;
		} catch (Exception ex) {
			Logger.error("Failed to create transient personal configuration ", ex, TLContext.class);
		}
		return null;
	}

	/**
	 * Same as {@link TLSessionContext#getOriginalUser()} of {@link #getSessionContext() session
	 * context}.
	 * 
	 * get the the person for the current user.
	 * 
	 * @return null only just before successful login.
	 */
	public final Person getCurrentPersonWrapper() {
		return getPerson();
   }

   /** 
    * Set the the Person currently logged in.
    *
    * Take care that this does not conflict with the current User.
    *
    * This should not be done by normal code, but Person related
    * Objects.
    * 
    * @param aPerson the person to be set. May be <code>null</code>.
    */
	public final void setCurrentPerson(Person aPerson) {
		setPerson(aPerson);
   }
   
	@Override
	public final String getCurrentUserName() {
		return TLContextManager.getCurrentUserName(this);
	}

	/**
	 * Set the the name of the user currently logged in.
	 * 
	 * @deprecated User name is not stored explicit. Use setting of person in
	 *             {@link TLSubSessionContext}.
	 */
	@Deprecated
    @Override
	public final void setCurrentUserName(String aName) {
		throw new UnsupportedOperationException("Username must not be set. Set Person to SessionContext.");
    }

	@Override
	protected boolean isAdminContext() {
		return Person.isAdmin(getPerson());
	}

	@Override
	public void setSessionBranch(Branch sessionBranch) {
		this._sessionBranch = sessionBranch;
	}

	@Override
	public Branch getSessionBranch(HistoryManager historyManager) {
		assert _sessionBranch == null || _sessionBranch.getHistoryManager() == historyManager;
		return _sessionBranch;
	}

	/** Extract the TLContext from the perThread context, may return null. */
    public static TLContext getContext() {
        return (TLContext) ThreadContext.getThreadContext();
    }

	@Override
	public TLSessionContext getSessionContext() {
		return (TLSessionContext) super.getSessionContext();
	}

	@Override
	public void setSessionContext(SessionContext session) {
		if (session != null && !(session instanceof TLSessionContext)) {
			throw new IllegalArgumentException("Session '" + session + "' must be a "
				+ TLSessionContext.class.getName());
		}
		super.setSessionContext(session);
	}

	@Override
	public LayoutContext getLayoutContext() {
		return _layoutContext;
	}

	/**
	 * Initializes the {@link #getLayoutContext()}.
	 */
	public void initLayoutContext(LayoutContext layoutContext) {
		_layoutContext = layoutContext;
	}

	@Override
	public UpdateChainLink getSessionRevision(HistoryManager historyManager) {
		return _revision;
	}

	@Override
	public UpdateChainLink updateSessionRevision(HistoryManager historyManager, UpdateChainLink newSessionRevision) {
		UpdateChainLink oldRevision = _revision;
		_revision = newSessionRevision;
		return oldRevision;
	}

	@Override
	public void addUnboundListener(SubSessionListener listener) {
		_listeners.add(listener);
	}

	@Override
	public void removeUnboundListener(SubSessionListener listener) {
		_listeners.remove(listener);
	}

	/**
	 * Informs all {@link SubSessionListener} of this {@link TLSubSessionContext}.
	 */
	public void informUnboundListeners() {
		List<SubSessionListener> listeners = _listeners.get();
		_listeners.clear();
		if (!listeners.isEmpty()) {
			if (!this.equals(ThreadContextManager.getSubSession())) {
				ThreadContextManager.inContext(this, new InContext() {
					@Override
					public void inContext() {
						notifyListeners(listeners);
					}
				});
			} else {
				notifyListeners(listeners);
			}
		}
		storePersonalConfiguration();
	}

	void notifyListeners(List<SubSessionListener> listeners) {
		for (SubSessionListener listener : listeners) {
			notifyListener(listener);
		}
	}

	private void notifyListener(SubSessionListener listener) {
		try {
			listener.notifySubSessionUnbound(this);
		} catch (Exception ex) {
			Logger.error("Unable to trigger listener '" + listener + "' with subsession '" + this + "'.",
				ex, AbstractSubSessionContext.class);
		}
	}

	/**
	 * The user currently logged in.
	 */
	public static Person currentUser() {
		TLContext context = getContext();
	
		if (context != null) {
			return context.getPerson();
	    }
		return null;
	}

	/**
	 * Getter for the configuration.
	 */
	public static Config getConfig() {
		return ApplicationConfig.getInstance().getConfig(Config.class);
	}

	/**
	 * Getter for {@link Config#PERSONAL_CONFIGURATION}.
	 */
	public static Class<? extends TransientPersonalConfiguration> getConfiguredPersonalConfiguration() {
		return getConfig().getPersonalConfiguration();
	}

}
