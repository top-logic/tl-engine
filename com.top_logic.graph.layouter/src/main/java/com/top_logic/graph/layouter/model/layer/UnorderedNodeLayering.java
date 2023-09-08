/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.model.layer;

import java.util.List;

/**
 * Unordered {@link NodeLayering}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class UnorderedNodeLayering extends NodeLayering<UnorderedNodeLayer> {

	/**
	 * Creates an unordered {@link NodeLayering}.
	 */
	public UnorderedNodeLayering(List<UnorderedNodeLayer> layering) {
		super(layering);
	}

}
