/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.graphic.blocks.model.connector;

import com.top_logic.graphic.blocks.model.BlockSchema;

/**
 * Well-known names of {@link ConnectorType}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ConnectorTypes {

	/**
	 * ID for {@link BooleanConnector}s.
	 * 
	 * @see BlockSchema#getConnectorType(String)
	 */
	String BOOLEAN_KIND = "boolean";

	/**
	 * ID for {@link NumberConnector}s.
	 * 
	 * @see BlockSchema#getConnectorType(String)
	 */
	String NUMBER_KIND = "number";

	/**
	 * ID for {@link SequenceConnector}s.
	 * 
	 * @see BlockSchema#getConnectorType(String)
	 */
	String SEQUENCE_KIND = "sequence";

	/**
	 * ID for {@link ValueConnector}s.
	 * 
	 * @see BlockSchema#getConnectorType(String)
	 */
	String VALUE_KIND = "value";

	/**
	 * ID for {@link VoidConnector}s.
	 * 
	 * @see BlockSchema#getConnectorType(String)
	 */
	String VOID_KIND = "void";

}
