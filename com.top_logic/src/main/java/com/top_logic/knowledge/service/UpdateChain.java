/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

/**
 * Pointer to the chain of {@link UpdateEvent}s in a {@link KnowledgeBase}.
 * 
 * <p>
 * The {@link UpdateChain} is an asynchronous pull-style listener to changes
 * that occur in a {@link KnowledgeBase}. Registering for {@link UpdateEvent}s
 * that can be processed asynchronously (e.g. from a UI update thread) requires
 * the following steps:
 * </p>
 * 
 * <ol>
 * <li>Requesting a pointer to the update chain by calling
 * {@link KnowledgeBase#getUpdateChain()}.</li>
 * 
 * <li>Requesting new updates that occurred after the call to
 * {@link KnowledgeBase#getUpdateChain()} by calling {@link #next()} on this
 * update chain pointer.</li>
 * 
 * <li>If {@link #next()} returns <code>true</code>, the methods
 * {@link #getRevision()} and {@link #getUpdateEvent()} can be called and the
 * resulting events can be processed.</li>
 * 
 * <li>The process is repeated, until {@link #next()} returns <code>false</code>. 
 * This signals that there are no more updates. This pointer to the update
 * chain is kept until the next opportunity for requesting updates. During the
 * next update cycle, the call to {@link #next()} is repeated and eventually
 * reports further updates.</li>
 * </ol>
 * 
 * <p>
 * Note: Before the first call to {@link #getUpdateEvent() changes} or
 * {@link #getRevision() revision}, {@link #next()} must be called. Calling
 * {@link #getUpdateEvent()} or {@link #getRevision()} is only legal, if
 * {@link #next()} returned <code>true</code>. 
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface UpdateChain {

	/**
	 * Advances the pointer to the update chain to the next entry.
	 * 
	 * <p>
	 * If this method returns <code>true</code>, {@link #getRevision()} or
	 * {@link #getUpdateEvent()} might be called to get access to the current
	 * updates.
	 * </p>
	 * 
	 * @return Whether there are more updates available that can be fetched by
	 *         calling {@link #getUpdateEvent()} and {@link #getRevision()}.
	 */
	boolean next();

	/**
	 * The revision, the current {@link #getUpdateEvent() updates} belong to.
	 * 
	 * <p>
	 * Note: Must only be called after {@link #next()} returns <code>true</code>.
	 * </p>
	 * 
	 * @return The revision number of the current updates.
	 */
	long getRevision();
	
	/**
	 * The current updates.
	 * 
	 * <p>
	 * Note: Must only be called after {@link #next()} returns <code>true</code>.
	 * </p>
	 */
	UpdateEvent getUpdateEvent();

}
