/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.util;

/**
 * Graph constants for layouting.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface GraphLayoutConstants {

	/**
	 * Final class modifier name.
	 */
	public static final String NODE_FINAL_MODIFIER = "final";

	/**
	 * Abstract class modifier name.
	 */
	public static final String NODE_ABSTRACT_MODIFIER = "abstract";

	/**
	 * Enumeration stereotype name for classes.
	 */
	public static final String ENUMERATION_STEREOTYPE = "enumeration";

	/**
	 * Class property label type name.
	 */
	public static final String LABEL_PROPERTY_TYPE = "property";

	/**
	 * Edge source name label type name.
	 */
	public static final String LABEL_EDGE_SOURCE_NAME_TYPE = "sourceName";

	/**
	 * Edge target name label type name.
	 */
	public static final String LABEL_EDGE_TARGET_NAME_TYPE = "targetName";

	/**
	 * Edge source cardinality label type name.
	 */
	public static final String LABEL_EDGE_SOURCE_CARDINALITY_TYPE = "sourceCard";

	/**
	 * Edge target cardinality label type name.
	 */
	public static final String LABEL_EDGE_TARGET_CARDINALITY_TYPE = "targetCard";

	/**
	 * Class classifier label type name.
	 */
	public static final String LABEL_CLASSIFIER_TYPE = "classifier";

	/**
	 * Composition edge type name.
	 */
	public static final String EDGE_COMPOSITION_TYPE = "composition";

	/**
	 * Aggregation edge type name.
	 */
	public static final String EDGE_AGGREGATION_TYPE = "aggregation";

	/**
	 * Association edge type name.
	 */
	public static final String EDGE_ASSOCIATION_TYPE = "association";

	/**
	 * Inheritance edge type name.
	 */
	public static final String EDGE_INHERITANCE_TYPE = "inheritance";
	
}
