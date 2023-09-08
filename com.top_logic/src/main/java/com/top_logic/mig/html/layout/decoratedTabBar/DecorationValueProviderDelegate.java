/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.decoratedTabBar;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

/**
 * Helper for managing a set of {@link DecorationValueProvider}s.
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class DecorationValueProviderDelegate implements Serializable {
    
	Collection<DecorationValueListener> listerners;

    public DecorationValueProviderDelegate() {
        super();
		this.listerners = new HashSet<>();
    }

    public void registerDecorationValueListener(DecorationValueListener aListener) {
        this.listerners.add(aListener);
    }
    
    public void fireDecorationValueChanged(DecorationValueProvider aProvider) {
		int cnt = listerners.size();
		if (cnt > 0) {
			DecorationValueListener[] currentListeners = this.listerners.toArray(new DecorationValueListener[cnt]);
			for (DecorationValueListener listener : currentListeners) {
				listener.receiveDecorationValueChanged(aProvider);
			}
        }
    }

	public boolean unregisterDecorationValueListener(DecorationValueListener aListener) {
		return this.listerners.remove(aListener);
	}

}
