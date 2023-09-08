/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.ms;

import java.util.List;

import com.top_logic.config.xdiff.model.Node;

/**
 * Provider of XML versions of {@link MSXDiff} operations by {@link ArtifactType}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface MSXDiffProvider {

	/**
	 * Serialized {@link MSXDiff} operations by {@link ArtifactType}.
	 */
	List<Node> createOperations(ArtifactType type);

}
