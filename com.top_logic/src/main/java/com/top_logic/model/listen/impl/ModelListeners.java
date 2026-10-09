/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.listen.impl;

import com.top_logic.basic.util.AbstractListeners;
import com.top_logic.model.listen.ModelChangeEvent;
import com.top_logic.model.listen.ModelListener;

/**
 * Registrations of {@link ModelListener}s for a single observed object, a single observed type, or
 * for all objects in a {@link DefaultModelScope}.
 * 
 * @see EventBuilder
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ModelListeners extends AbstractListeners<ModelListener, ModelChangeEvent> {

	@Override
	protected void sendEvent(ModelListener listener, ModelChangeEvent event) {
		listener.notifyChange(event);
	}

}
