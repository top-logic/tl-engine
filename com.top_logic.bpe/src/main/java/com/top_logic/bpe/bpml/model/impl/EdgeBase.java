/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.bpml.model.impl;

/**
 * Basic interface for {@link #EDGE_TYPE} business objects.
 * 
 * @author Automatically generated by {@link com.top_logic.element.model.generate.InterfaceGenerator}
 */
public interface EdgeBase extends com.top_logic.bpe.bpml.model.Named, com.top_logic.bpe.bpml.model.Annotated {

	/**
	 * Name of type <code>Edge</code>
	 */
	String EDGE_TYPE = "Edge";

	/**
	 * Part <code>process</code> of <code>Edge</code>
	 * 
	 * <p>
	 * Declared as <code>tl.bpe.bpml:Process</code> in configuration.
	 * </p>
	 */
	String PROCESS_ATTR = "process";

	/**
	 * Part <code>source</code> of <code>Edge</code>
	 * 
	 * <p>
	 * Declared as <code>tl.bpe.bpml:Node</code> in configuration.
	 * </p>
	 */
	String SOURCE_ATTR = "source";

	/**
	 * Part <code>target</code> of <code>Edge</code>
	 * 
	 * <p>
	 * Declared as <code>tl.bpe.bpml:Node</code> in configuration.
	 * </p>
	 */
	String TARGET_ATTR = "target";

	/**
	 * Getter for part {@link #PROCESS_ATTR}.
	 */
	default com.top_logic.bpe.bpml.model.Process getProcess() {
		return (com.top_logic.bpe.bpml.model.Process) tValueByName(PROCESS_ATTR);
	}

	/**
	 * Getter for part {@link #SOURCE_ATTR}.
	 */
	default com.top_logic.bpe.bpml.model.Node getSource() {
		return (com.top_logic.bpe.bpml.model.Node) tValueByName(SOURCE_ATTR);
	}

	/**
	 * Setter for part {@link #SOURCE_ATTR}.
	 */
	default void setSource(com.top_logic.bpe.bpml.model.Node newValue) {
		tUpdateByName(SOURCE_ATTR, newValue);
	}

	/**
	 * Getter for part {@link #TARGET_ATTR}.
	 */
	default com.top_logic.bpe.bpml.model.Node getTarget() {
		return (com.top_logic.bpe.bpml.model.Node) tValueByName(TARGET_ATTR);
	}

	/**
	 * Setter for part {@link #TARGET_ATTR}.
	 */
	default void setTarget(com.top_logic.bpe.bpml.model.Node newValue) {
		tUpdateByName(TARGET_ATTR, newValue);
	}

}
