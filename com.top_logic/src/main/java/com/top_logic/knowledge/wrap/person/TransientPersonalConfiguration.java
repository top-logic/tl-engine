/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.person;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.Logger;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.layout.LayoutContext;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.commandhandlers.SetHomepageHandler;
import com.top_logic.util.TLContext;

/**
 * This class is stored at the {@link TLContext} and made persistent when the user logs out.
 * 
 * @author <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TransientPersonalConfiguration implements PersonalConfiguration {

	private Person _person;

	private final ConcurrentHashMap<String, Object> _storage;

	private volatile boolean _modified;

	/**
	 * Create a new {@link TransientPersonalConfiguration} with default size.
	 */
    public TransientPersonalConfiguration() {
		this(Collections.emptyMap());
    }

    /**
	 * Create a new {@link TransientPersonalConfiguration} as copy from a Map.
	 */
	public TransientPersonalConfiguration(Map<String, Object> copyFrom) {
		_storage = new ConcurrentHashMap<>(copyFrom);
		if (getStartPageAutomatism()) {
			markModified();
		}
    }

	/**
	 * Whether there are pending transient changes not yet stored.
	 */
	public boolean isModified() {
		return _modified;
	}

    @Override
	public void setValue(String aKey, Object aValue) {
		if (aValue == null) {
			Object formerEntry = _storage.remove(aKey);
			if (formerEntry != null) {
				/* Object was removed; null was not the value because null is not allowed as entry. */
				markModified();
			}
        } else {
			Object former = _storage.put(aKey, aValue);
			if (!aValue.equals(former)) {
				// Entry was changed
				markModified();
			}
        }
    }

    @Override
	public Object getValue(String aKey) {
		return _storage.get(aKey);
    }
    
	/**
	 * Stores the values of this {@link TransientPersonalConfiguration} back to the persistence.
	 */
	public void storeConfiguration(TLSubSessionContext subSession) {
		if (!_modified) {
			return;
		}

		// Note: At least in tests it may happen that an account is deleted before its session
		// has been logged out.
		if (_person == null || !_person.isAlive()) {
			return;
		}
		prepareStorageInternal(subSession);

		PersonalConfigurationWrapper theWrapper =
			PersonalConfigurationWrapper.getPersonalConfiguration(_person);

		if (theWrapper == null && _storage.isEmpty()) { // nothing to save, done we are
			return;
		}

		try (Transaction tx = PersonalConfigurationWrapper.getDefaultKnowledgeBase().beginTransaction()) {
			if (theWrapper != null) {
				theWrapper.tDelete(); // Delete Old values
			}

			// Recreate it to avoid checking for previously set Attributes ...
			theWrapper = PersonalConfigurationWrapper.createPersonalConfiguration(_person);

			storeValues(theWrapper, subSession);

			tx.commit();
		} catch (KnowledgeBaseException kx) {
			Logger.error("Failed to commit PersonalConfiguration", kx, TransientPersonalConfiguration.class);
		}
	}

	private void prepareStorageInternal(TLSubSessionContext context) {
		storeStartPage(context);
		prepareStorage(context);
	}

	private void storeStartPage(TLSubSessionContext context) {
		try {
			storeStartPageUnsafe(context);
		} catch (RuntimeException exception) {
			throw new RuntimeException("Failed to store the start page on logout: "
				+ exception.getMessage(), exception);
		}
	}

	private void storeStartPageUnsafe(TLSubSessionContext context) {
		if (!getStartPageAutomatism()) {
			return;
		}
		MainLayout mainLayout = getMainLayout(context);
		if (mainLayout == null) {
			return;
		}
		Homepage startPage = getStartPage(mainLayout);
		if (startPage == null) {
			return;
		}
		setHomepage(mainLayout, startPage);
	}

	private MainLayout getMainLayout(TLSubSessionContext subSession) {
		LayoutContext layoutContext = subSession.getLayoutContext();
		if (layoutContext == null) {
			return null;
		}
		return layoutContext.getMainLayout();
	}

	private Homepage getStartPage(MainLayout mainLayout) {
		SetHomepageHandler startPageHandler = getStartPageHandler();
		if (startPageHandler == null) {
			return null;
		}
		return startPageHandler.getHomepage(mainLayout);
	}

	/**
	 * Return the handler for finding the start page.
	 * 
	 * @return The requested handler for setting the start page. Null, if there is no command for
	 *         setting the start page.
	 */
	private SetHomepageHandler getStartPageHandler() {
		return (SetHomepageHandler) getCommand(SetHomepageHandler.COMMAND_ID);
	}

	private CommandHandler getCommand(String commandId) {
		return CommandHandlerFactory.getInstance().getHandler(commandId);
	}

	/**
	 * Hook for extending the information to be stored in the personal configuration.
	 * <p>
	 * This will be called before checking {@link Map#isEmpty()} on the {@link #_storage}.
	 * </p>
	 * 
	 * @param context
	 *        The context we currently live in, must not be <code>null</code>.
	 */
	protected void prepareStorage(TLSubSessionContext context) {
		// hook for subclasses
	}

	/**
	 * Allow subclasses to add extra values to store here.
	 * 
	 * @param context
	 *        The current {@link TLSubSessionContext} is passed explicitly per parameter, as it
	 *        cannot be retrieved via {@link TLContext#getContext()}, as the session has already
	 *        been invalidated.
	 */
	protected void storeValues(PersonalConfigurationWrapper aWrapper, TLSubSessionContext context) {
		for (Map.Entry<String, Object> theEntry : _storage.entrySet()) {
			String theKey = theEntry.getKey();
            if (theKey.length() <= AbstractFlexDataManager.MAX_ATTRIBUTE_NAME_LENGTH) {
            	aWrapper.setValue(theKey, theEntry.getValue());
            }
            else {
            	Logger.warn("PersonalConfiguration for " + theKey + " could not be stored, key to long", this);
            }
        }
    }

	/**
	 * Initializes this personal configuration with the person it is created for.
	 * 
	 * @param person
	 *        {@link Person} this {@link TransientPersonalConfiguration} is responsible for.
	 * @param persistentConfig
	 *        Current {@link PersonalConfigurationWrapper} for the given {@link Person}.
	 */
	public void init(Person person, PersonalConfigurationWrapper persistentConfig) {
		if (_person != null && !_person.equals(person)) {
			throw new IllegalStateException("Must not initialize person twice.");
		}
		_person = person;
		if (persistentConfig == null) {
			return;
		}
		_storage.clear();
		try {
			for (String attributeName : persistentConfig.getAllAttributeNames()) {
				if (KBUtils.KA_SYSTEM_ATTRIBUTES.contains(attributeName)) {
					// ignore system attributes.
					continue;
				}
				Object value = persistentConfig.getValue(attributeName);
				if (value == null) {
					// Null values are not supported by ConcurrentHashMap. Moreover null means "no
					// personal configuration entry".
					continue;
				}
				_storage.put(attributeName, value);
			}
		} catch (Exception ex) {
			Logger.error("Failed to clone values from persistent personal configuration", ex,
				TransientPersonalConfiguration.class);
		}
		if (getStartPageAutomatism()) {
			markModified();
		}
	}

	private void markModified() {
		_modified = true;
	}

}
