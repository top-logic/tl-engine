/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

/**
 * Singleton assignment link.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TLModuleSingleton extends TLNamed, com.top_logic.model.impl.generated.TLModuleSingletonsBase {

	/**
	 * The name under which the singleton is known in {@link TLModuleSingletons}.
	 * 
	 * @see com.top_logic.model.TLNamed#getName()
	 */
	@Override
	String getName();

	/**
	 * The {@link TLModule} for which {@link #getSingleton()} is a singleton.
	 */
	TLModule getModule();

	/**
	 * The actual singleton object.
	 */
	TLObject getSingleton();
}
