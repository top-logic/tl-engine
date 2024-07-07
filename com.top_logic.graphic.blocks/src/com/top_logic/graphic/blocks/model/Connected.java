/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.graphic.blocks.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.top_logic.graphic.blocks.model.block.Connector;

/**
 * {@link BlockShape} defining {@link Connector}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Connected extends BlockShape {

	/**
	 * Offers all defined {@link Connector}s to the given consumer.
	 */
	void forEachConnector(Consumer<Connector> consumer);

	/**
	 * All defined consumers stored in a {@link List}.
	 */
	default List<Connector> connectors() {
		ArrayList<Connector> result = new ArrayList<>();
		forEachConnector(result::add);
		return result;
	}

}
