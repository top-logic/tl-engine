/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.export;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Context for a {@link PreloadOperation} that keeps pre-loaded objects from being garbage
 * collected.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PreloadContext implements AccessContext {

	private Set<Object> hardObjectReferences = new HashSet<>();

	private List<Object> hardValueReferences = new ArrayList<>();

	/**
	 * Establish a hard reference to the given object (with identity that may be encountered more
	 * than once during a single preload) to keep it from being garbage collected.
	 * 
	 * @see #keepValue(Object)
	 */
	public void keepObject(Object obj) {
		hardObjectReferences.add(obj);
	}

	/**
	 * Establish a hard reference to the given value (object without identity) to keep it from being
	 * garbage collected.
	 * 
	 * @see #keepObject(Object)
	 */
	public void keepValue(Object value) {
		hardValueReferences.add(value);
	}

	/**
	 * Removes all references established through {@link #keepObject(Object)} or
	 * {@link #keepValue(Object)}.
	 */
	@Override
	public void close() {
		hardObjectReferences.clear();
		hardValueReferences.clear();
	}

}
