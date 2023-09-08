/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.listener;

import java.util.EventListener;

/**
 * Property listeners are informed about changes to in {@link PropertyObservable}s.
 * 
 * <p>
 * The concrete type {@link PropertyListener} is defined by an {@link EventType}.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface PropertyListener extends EventListener {

	// pure marker interface

}

