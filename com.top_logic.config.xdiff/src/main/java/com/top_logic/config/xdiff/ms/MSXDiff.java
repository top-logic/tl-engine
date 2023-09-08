/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.ms;

import com.top_logic.basic.StringServices;
import com.top_logic.config.xdiff.XApplyException;

/**
 * Base class for operations defined by the {@link MSXDiffSchema} language.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class MSXDiff {

	private final String _component;

	/**
	 * Creates a {@link MSXDiff}.
	 * 
	 * @param component
	 *        See {@link #getComponent()}.
	 */
	public MSXDiff(String component) {
		if (StringServices.isEmpty(component)) {
			throw new XApplyException("Component must not be empty.");
		}
		_component = component;
	}

	/**
	 * Description of the target document to which this {@link MSXDiff} applies.
	 */
	public String getComponent() {
		return _component;
	}

	/**
	 * The type of change directive this {@link MSXDiff} represents.
	 */
	public abstract ArtifactType getType();

	/**
	 * Visits this {@link MSXDiff} with the given visitor.
	 */
	public abstract <R, A> R visit(MSXDiffVisitor<R, A> v, A arg);

}
