/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.thread.BindingSnapshot;
import com.top_logic.basic.thread.ThreadLocalBinding;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.util.error.TopLogicException;

/**
 * The person for whom the current call acts in another system.
 *
 * <p>
 * A {@link CredentialProvider#isPersonal() personal credential provider} asks {@link #require()}
 * for the person whose credentials it sends. The engine names an acting person in two cases only:
 * </p>
 * <ul>
 * <li>The person is present: a request from the browser window of a signed-in person, that is a
 * command, the load of a view or the change of a field, in the React and in the classic user
 * interface. A script that runs within such a request belongs to it.</li>
 * <li>A present person started the run: a chat run with a message of the person, an
 * <code>aiAgentChatAsync</code> run and an <code>aiResearch</code> run started in a request of the
 * person, a background job that a request of the person starts in its window (a job of a view, the
 * task of the progress dialog of the classic user interface), a detached subagent of such a run,
 * and the turn that a finished subagent starts for its lead if each subagent whose news it reads was
 * started for the person.</li>
 * </ul>
 *
 * <p>
 * A run takes over the acting person of the place that starts it. The second case ends with the end
 * of the run, and earlier when the account of the person is locked or deleted. Sign-out, a closed
 * window or the timeout of a session do not end it.
 * </p>
 *
 * <p>
 * No acting person exists in a system context, in a scheduled task, in a request to an endpoint of
 * the application for MCP, A2A or the <code>OpenAPI</code> server, and in each run that one of these
 * starts. The anonymous account is never an acting person, because all visitors share it.
 * </p>
 *
 * <p>
 * The acting person is a {@link ThreadLocalBinding}, so a {@link BindingSnapshot} carries it to a
 * worker thread together with the other bindings.
 * </p>
 */
public final class ActingPerson {

	private static final ThreadLocalBinding<Person> PERSON = new ThreadLocalBinding<>();

	private ActingPerson() {
		// Static access only.
	}

	/**
	 * The person for whom the current call acts.
	 *
	 * <p>
	 * The person must still be active: the newest committed version of the account is read at each
	 * call, so that a lock or delete takes effect also in a run that reads the knowledge base at an
	 * older revision. On another node of a cluster, a lock or delete takes effect after the next
	 * refetch of that node.
	 * </p>
	 *
	 * @return The acting person, never <code>null</code>.
	 * @throws TopLogicException
	 *         If no account acts in the current call, if no acting person exists, or if another
	 *         account acts than the acting person. The message names the reason.
	 */
	public static Person require() {
		Person bound = PERSON.get();
		ResKey refusal = refusal(bound);
		if (refusal != null) {
			throw new TopLogicException(refusal);
		}
		return bound;
	}

	/**
	 * The acting person that a run started here takes over.
	 *
	 * @return The person {@link #require()} returns now, <code>null</code> where {@link #require()}
	 *         refuses.
	 */
	@FrameworkInternal
	public static Person handOver() {
		Person bound = PERSON.get();
		return refusal(bound) == null ? bound : null;
	}

	/**
	 * Binds the acting person to the current thread.
	 *
	 * <p>
	 * Only the entry points of the engine call this: a request from a browser window, with the person
	 * of the window, and a place that starts a run on another thread, with the person of
	 * {@link #handOver()}.
	 * </p>
	 *
	 * @param person
	 *        The acting person, <code>null</code> for none. The anonymous account is never an acting
	 *        person, also when it is bound.
	 * @return The handle whose {@link ThreadLocalBinding.Handle#close() close()} restores the acting
	 *         person bound before (or its absence).
	 */
	@FrameworkInternal
	public static ThreadLocalBinding<Person>.Handle bind(Person person) {
		return PERSON.bind(person);
	}

	private static ResKey refusal(Person bound) {
		Person current = TLContext.currentUser();
		if (bound == null) {
			return current == null ? I18NConstants.ERROR_NO_ACCOUNT : I18NConstants.ERROR_NO_ACTING_PERSON;
		}
		if (!bound.equals(current)) {
			return I18NConstants.ERROR_OTHER_ACCOUNT_ACTS;
		}
		if (!isActive(bound)) {
			return I18NConstants.ERROR_NO_ACTING_PERSON;
		}
		return null;
	}

	/**
	 * Whether the newest committed version of the given account exists, is not locked and is not the
	 * anonymous account.
	 *
	 * <p>
	 * A long run reads the knowledge base at the revision it last committed or updated to, so the
	 * account would not show a later lock there. A historic version newer than this revision cannot
	 * be resolved either. The account is therefore read in the newest revision of this node, as a
	 * cache does that follows the commits. All checks of the account are in this read: after the run
	 * caught up with a delete, any other read of the account fails.
	 * </p>
	 */
	private static boolean isActive(Person person) {
		KnowledgeBase kb = person.getKnowledgeBase();
		long newest = kb.getHistoryManager().getLastRevision();
		return kb.inRevision(newest, () -> person.tValid() && !person.isLocked()
			&& !PersonManager.getManager().isAnonymous(person));
	}

}
