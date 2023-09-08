/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value.object;

import java.util.List;

import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.value.ValueRef;

/**
 * A path of {@link ValueRef}s that are resolved in a context-dependent manner.
 * 
 * Root is not contained in the path and represented by an empty path.
 * 
 * @deprecated See {@link ValueRef}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Deprecated
public interface ValuePath extends ContextRef {

	/**
	 * Path of {@link ValueRef}s.
	 */
	List<ModelName> getNodes();

	/** @see #getNodes() */
	void setNodes(List<ModelName> value);

}
