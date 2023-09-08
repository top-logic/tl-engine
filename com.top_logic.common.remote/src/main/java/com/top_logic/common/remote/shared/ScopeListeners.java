/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.remote.shared;

import com.top_logic.common.remote.event.ListenerContainer;

/**
 * {@link ListenerContainer} for {@link ScopeListener}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ScopeListeners extends ListenerContainer<ScopeListener, ScopeEvent>
		implements ScopeListener, ListenerContainer.NotificationCallback<ScopeListener, ScopeEvent> {

	@Override
	public void notifyListener(ScopeListener listener, ScopeEvent event) {
		listener.handleObjectScopeEvent(event);
	}

	@Override
	public void handleObjectScopeEvent(ScopeEvent event) {
		sendScopeEvent(event);
	}

	/**
	 * Notifies about the preparation.
	 * 
	 * @param scope
	 *        The modified {@link ObjectScope}.
	 */
	public void notifyPrepare(ObjectScope scope) {
		if (idle()) {
			return;
		}
		sendScopeEvent(new ScopeEvent.Prepare(scope));
	}

	/**
	 * Notifies about the post processing.
	 * 
	 * @param scope
	 *        The modified {@link ObjectScope}.
	 */
	public void notifyPostProcess(ObjectScope scope) {
		if (idle()) {
			return;
		}
		sendScopeEvent(new ScopeEvent.PostProcess(scope));
	}

	/**
	 * Notifies about an object creation.
	 * 
	 * @param scope
	 *        The modified {@link ObjectScope}.
	 * @param obj
	 *        The created {@link Object}.
	 */
	public void notifyCreate(ObjectScope scope, Object obj) {
		if (idle()) {
			return;
		}
		sendScopeEvent(new ScopeEvent.Create(scope, obj));
	}

	/**
	 * Notifies about an object deletion.
	 * 
	 * @param scope
	 *        The modified {@link ObjectScope}.
	 * @param obj
	 *        The deleted {@link Object}.
	 */
	public void notifyDelete(ObjectScope scope, Object obj) {
		if (idle()) {
			return;
		}
		sendScopeEvent(new ScopeEvent.Delete(scope, obj));
	}

	/**
	 * Notifies about an object modification.
	 * 
	 * @param scope
	 *        The modified {@link ObjectScope}.
	 * @param obj
	 *        The modified {@link Object}.
	 * @param property
	 *        The modified property of the given object.
	 */
	public void notifyUpdate(ObjectScope scope, Object obj, String property) {
		if (idle()) {
			return;
		}
		sendScopeEvent(new ScopeEvent.Update(scope, obj, property));
	}

	private void sendScopeEvent(ScopeEvent event) {
		notifyListeners(this, event);
	}

}
