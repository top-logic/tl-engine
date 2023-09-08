/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.context;

import com.top_logic.basic.SessionContext;
import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.db2.UpdateChainLink;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.LayoutContext;

/**
 * {@link SubSessionContext} with special methods not available in Basic.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TLSubSessionContext extends SubSessionContext {

	@Override
	public TLSessionContext getSessionContext();

	/**
	 * The user on behalf of which, this sub-session operates.
	 * 
	 * <p>
	 * Note: The user is per sub-session instead of per {@link #getSessionContext() session} to
	 * allow logging in as a different user in a separate tab.
	 * </p>
	 * 
	 * @return The current user, <code>null</code> only before the login has finished .
	 */
	Person getPerson();

	/**
	 * @see #getPerson()
	 */
	void setPerson(Person p);

	/**
	 * A transient version of the {@link PersonalConfiguration} for the current person that can be
	 * modified without a commit context.
	 * 
	 * <p>
	 * The {@link PersonalConfiguration} is automatically made persistent when this context goes out
	 * of scope.
	 * </p>
	 * 
	 * <p>
	 * Consider using {@link PersonalConfiguration#getPersonalConfiguration()} for convenient
	 * access.
	 * </p>
	 * 
	 * @see PersonalConfiguration#getPersonalConfiguration()
	 */
	PersonalConfiguration getPersonalConfiguration();

	/**
	 * Removes the transient version of the personal configuration.
	 * 
	 * <p>
	 * All changes not yet stored are lost.
	 * </p>
	 */
	void resetPersonalConfiguration();

	/**
	 * Stores changes to the personal configuration.
	 * 
	 * @see #getPersonalConfiguration()
	 */
	void storePersonalConfiguration();

	/**
	 * The default branch of the current session.
	 * 
	 * @param historyManager
	 *        The {@link HistoryManager} from which a potential default is taken.
	 * 
	 * @return The session branch installed with {@link #setSessionBranch(Branch)} or
	 *         <code>null</code> if none was installed before.
	 */
	Branch getSessionBranch(HistoryManager historyManager);

	/**
	 * Installs the branch of the current session.
	 * 
	 * <p>
	 * The current branch must only be accessed through the methods
	 * {@link HistoryManager#getContextBranch()} to guarantee stability of the result between
	 * multiple lookups (without interleaving {@link HistoryUtils#setContextBranch(Branch)} calls).
	 * If the context branch is not stable, internal APIs break that perform multiple lookups of the
	 * context branch.
	 * </p>
	 * 
	 * @param sessionBranch
	 *        The branch on which all operation in the current session should happen. must not be
	 *        <code>null</code>
	 */
	void setSessionBranch(Branch sessionBranch);

	/**
	 * Returns the revision of the current session.
	 * 
	 * <p>
	 * Must not be called by the application. Use {@link HistoryManager#getSessionRevision()}
	 * instead.
	 * </p>
	 * 
	 * @param historyManager
	 *        The {@link HistoryManager} which is used to access with the returned revision.
	 * 
	 * @see HistoryManager#getSessionRevision()
	 */
	@FrameworkInternal
	UpdateChainLink getSessionRevision(HistoryManager historyManager);

	/**
	 * Updates the revision of the current session for the given {@link HistoryManager}.
	 * 
	 * <p>
	 * Must not be called by the application.
	 * </p>
	 * 
	 * @param historyManager
	 *        The {@link HistoryManager} to update revision for.
	 * @param newSessionRevision
	 *        The new session revision.
	 * @return The former session revision.
	 */
	@FrameworkInternal
	UpdateChainLink updateSessionRevision(HistoryManager historyManager, UpdateChainLink newSessionRevision);

	/**
	 * The {@link LayoutContext} the current request is targeted to.
	 * 
	 * @return The current {@link LayoutContext}, or <code>null</code>, if the current
	 *         {@link TLSubSessionContext} is does not target a layout tree.
	 */
	LayoutContext getLayoutContext();

	/**
	 * Adds the given {@link SubSessionListener} to be informed, when the {@link SubSessionContext}
	 * is removed from its {@link SessionContext}.
	 * 
	 * @param listener
	 *        The listener to inform.
	 * 
	 * @see #removeUnboundListener(SubSessionListener)
	 */
	void addUnboundListener(SubSessionListener listener);

	/**
	 * Removes the given {@link SubSessionListener} from the list of listeners to be informed.
	 * 
	 * @param listener
	 *        The listener to remove. If the listener was not added before, nothing happens.
	 * 
	 * @see #addUnboundListener(SubSessionListener)
	 */
	void removeUnboundListener(SubSessionListener listener);

}

