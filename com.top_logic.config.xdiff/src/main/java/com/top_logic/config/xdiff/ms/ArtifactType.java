/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.ms;

/**
 * Kind of operations in the {@link MSXDiffSchema}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum ArtifactType {
	/** @see MSXDiffSchema#NODE_DELETE_ELEMENT */
	NODE_DELETE,

	/** @see MSXDiffSchema#NODE_ADD_ELEMENT */
	NODE_ADD,

	/** @see MSXDiffSchema#NODE_VALUE_ELEMENT */
	NODE_VALUE,

	/** @see MSXDiffSchema#ATTRIBUTE_SET_ELEMENT */
	ATTRIBUTE_SET;
}
