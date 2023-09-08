/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout;

import java.util.HashSet;

import com.top_logic.layout.NotifyListener;

/**
 * The class {@link DummyNotifyListener} is a dummy implementation of
 * {@link NotifyListener} for test uses.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DummyNotifyListener implements NotifyListener {

	protected final HashSet	listenedObjects	= new HashSet();

	@Override
	public void notifyAttachedTo(Object model) {
		listenedObjects.add(model);
	}

	@Override
	public void notifyDetachedFrom(Object model) {
		listenedObjects.remove(model);
	}

	/**
	 * This method returns the number of models for which
	 * {@link #notifyAttachedTo(Object)} was called and not
	 * {@link #notifyDetachedFrom(Object)}.
	 */
	public int numberOfListenedModels() {
		return listenedObjects.size();
	}

}
