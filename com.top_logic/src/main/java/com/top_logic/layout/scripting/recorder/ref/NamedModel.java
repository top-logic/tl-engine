/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import com.top_logic.base.context.TLSessionContext;

/**
 * A UI model that is uniquely identified by a name within a {@link TLSessionContext session}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface NamedModel {

	/**
	 * The unique name of this {@link NamedModel} within its {@link TLSessionContext session}.
	 * 
	 * <p>
	 * This interface must be implemented by delegating to a concrete {@link ModelNamingScheme} for
	 * this concrete model type.
	 * </p>
	 */
	ModelName getModelName();

}
