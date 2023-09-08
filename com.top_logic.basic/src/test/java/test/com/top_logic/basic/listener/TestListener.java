/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.listener;

import java.util.ArrayList;
import java.util.List;

import test.com.top_logic.basic.DeactivatedTest;

import com.top_logic.basic.listener.Listener;
import com.top_logic.basic.listener.ListenerRegistry;

/**
 * A {@link Listener} for testing the {@link ListenerRegistry}.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
@DeactivatedTest("Not a test, but a tool for tests.")
public class TestListener implements Listener<Object> {

	private List<Object> _notifications = new ArrayList<>();

	@Override
	public void notify(Object notification) {
		_notifications.add(notification);
	}

	/** Returns the internal {@link List} of notifications. */
	public List<Object> getNotifications() {
		return _notifications;
	}

}
