/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.model.util;

/**
 * Configuration constants for the LayoutGraph.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface LayoutGraphUtilConstants {
	/**
	 * String UML representation for the zero cardinality.
	 */
	public final static String ZERO_CARDINALITY = "0";

	/**
	 * String UML representation for the one cardinality.
	 */
	public final static String ONE_CARDINALITY = "1";

	/**
	 * String UML representation for the arbitrary cardinality.
	 */
	public final static String ARBITRARY_CARDINALITY = "*";

	/**
	 * String UML representation to connect two cardinalities.
	 */
	public final static String CARDINALITY_POINTS = "..";
}
