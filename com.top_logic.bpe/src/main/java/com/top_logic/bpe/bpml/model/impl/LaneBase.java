/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.bpml.model.impl;

/**
 * Basic interface for {@link #LANE_TYPE} business objects.
 * 
 * @author Automatically generated by {@link com.top_logic.element.model.generate.InterfaceGenerator}
 */
public interface LaneBase extends com.top_logic.bpe.bpml.model.Named, com.top_logic.bpe.bpml.model.LaneSet, com.top_logic.bpe.bpml.model.Annotated {

	/**
	 * Name of type <code>Lane</code>
	 */
	String LANE_TYPE = "Lane";

	/**
	 * Part <code>nodes</code> of <code>Lane</code>
	 * 
	 * <p>
	 * Declared as <code>tl.bpe.bpml:Node</code> in configuration.
	 * </p>
	 */
	String NODES_ATTR = "nodes";

	/**
	 * Part <code>owner</code> of <code>Lane</code>
	 * 
	 * <p>
	 * Declared as <code>tl.bpe.bpml:LaneSet</code> in configuration.
	 * </p>
	 */
	String OWNER_ATTR = "owner";

	/**
	 * Getter for part {@link #NODES_ATTR}.
	 */
	@SuppressWarnings("unchecked")
	default java.util.Set<? extends com.top_logic.bpe.bpml.model.Node> getNodes() {
		return (java.util.Set<? extends com.top_logic.bpe.bpml.model.Node>) tValueByName(NODES_ATTR);
	}

	/**
	 * Getter for part {@link #OWNER_ATTR}.
	 */
	default com.top_logic.bpe.bpml.model.LaneSet getOwner() {
		return (com.top_logic.bpe.bpml.model.LaneSet) tValueByName(OWNER_ATTR);
	}

}
