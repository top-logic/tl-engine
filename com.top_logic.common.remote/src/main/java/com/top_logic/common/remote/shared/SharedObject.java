/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.remote.shared;

import com.top_logic.common.remote.listener.AttributeObservable;

/**
 * Member of an {@link ObjectScope application-typed interconnected object net} that can be shared
 * between multiple machines and allows collective manipulation in each machine.
 * 
 * @see ObjectScope
 * @see DefaultSharedObject
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface SharedObject extends AttributeObservable {

	/**
	 * Access to the {@link ObjectData data container} with managed instance data for this instance.
	 */
	ObjectData data();

}
