/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.bpml.model.impl;

/**
 * Basic interface for {@link #MESSAGE_FLOW_TYPE} business objects.
 * 
 * @author Automatically generated by {@link com.top_logic.element.model.generate.InterfaceGenerator}
 */
public interface MessageFlowBase extends com.top_logic.bpe.bpml.model.Named {

	/**
	 * Name of type <code>MessageFlow</code>
	 */
	String MESSAGE_FLOW_TYPE = "MessageFlow";

	/**
	 * Part <code>collaboration</code> of <code>MessageFlow</code>
	 * 
	 * <p>
	 * Declared as <code>tl.bpe.bpml:Collaboration</code> in configuration.
	 * </p>
	 */
	String COLLABORATION_ATTR = "collaboration";

	/**
	 * Part <code>connectExpession</code> of <code>MessageFlow</code>
	 * 
	 * <p>
	 * Declared as <code>tl.model.search:Expr</code> in configuration.
	 * </p>
	 */
	String CONNECT_EXPESSION_ATTR = "connectExpession";

	/**
	 * Part <code>source</code> of <code>MessageFlow</code>
	 * 
	 * <p>
	 * Declared as <code>tl.bpe.bpml:FlowSource</code> in configuration.
	 * </p>
	 */
	String SOURCE_ATTR = "source";

	/**
	 * Part <code>target</code> of <code>MessageFlow</code>
	 * 
	 * <p>
	 * Declared as <code>tl.bpe.bpml:FlowTarget</code> in configuration.
	 * </p>
	 */
	String TARGET_ATTR = "target";

	/**
	 * Getter for part {@link #COLLABORATION_ATTR}.
	 */
	default com.top_logic.bpe.bpml.model.Collaboration getCollaboration() {
		return (com.top_logic.bpe.bpml.model.Collaboration) tValueByName(COLLABORATION_ATTR);
	}

	/**
	 * Getter for part {@link #CONNECT_EXPESSION_ATTR}.
	 */
	default com.top_logic.model.search.expr.SearchExpression getConnectExpession() {
		return (com.top_logic.model.search.expr.SearchExpression) tValueByName(CONNECT_EXPESSION_ATTR);
	}

	/**
	 * Setter for part {@link #CONNECT_EXPESSION_ATTR}.
	 */
	default void setConnectExpession(com.top_logic.model.search.expr.SearchExpression newValue) {
		tUpdateByName(CONNECT_EXPESSION_ATTR, newValue);
	}

	/**
	 * Getter for part {@link #SOURCE_ATTR}.
	 */
	default com.top_logic.bpe.bpml.model.FlowSource getSource() {
		return (com.top_logic.bpe.bpml.model.FlowSource) tValueByName(SOURCE_ATTR);
	}

	/**
	 * Setter for part {@link #SOURCE_ATTR}.
	 */
	default void setSource(com.top_logic.bpe.bpml.model.FlowSource newValue) {
		tUpdateByName(SOURCE_ATTR, newValue);
	}

	/**
	 * Getter for part {@link #TARGET_ATTR}.
	 */
	default com.top_logic.bpe.bpml.model.FlowTarget getTarget() {
		return (com.top_logic.bpe.bpml.model.FlowTarget) tValueByName(TARGET_ATTR);
	}

	/**
	 * Setter for part {@link #TARGET_ATTR}.
	 */
	default void setTarget(com.top_logic.bpe.bpml.model.FlowTarget newValue) {
		tUpdateByName(TARGET_ATTR, newValue);
	}

}
