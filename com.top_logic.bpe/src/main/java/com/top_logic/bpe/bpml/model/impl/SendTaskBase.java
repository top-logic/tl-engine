/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.bpml.model.impl;

/**
 * Basic interface for {@link #SEND_TASK_TYPE} business objects.
 * 
 * @author Automatically generated by {@link com.top_logic.element.model.generate.InterfaceGenerator}
 */
public interface SendTaskBase extends com.top_logic.bpe.bpml.model.Task {

	/**
	 * Name of type <code>SendTask</code>
	 */
	String SEND_TASK_TYPE = "SendTask";

	/**
	 * Part <code>contentTemplate</code> of <code>SendTask</code>
	 * 
	 * <p>
	 * Declared as <code>tl.model.search:Template</code> in configuration.
	 * </p>
	 */
	String CONTENT_TEMPLATE_ATTR = "contentTemplate";

	/**
	 * Part <code>receiverGroups</code> of <code>SendTask</code>
	 * 
	 * <p>
	 * Declared as <code>tl.accounts:Group</code> in configuration.
	 * </p>
	 */
	String RECEIVER_GROUPS_ATTR = "receiverGroups";

	/**
	 * Part <code>receiverRule</code> of <code>SendTask</code>
	 * 
	 * <p>
	 * Declared as <code>tl.model.search:Expr</code> in configuration.
	 * </p>
	 */
	String RECEIVER_RULE_ATTR = "receiverRule";

	/**
	 * Part <code>subjectTemplate</code> of <code>SendTask</code>
	 * 
	 * <p>
	 * Declared as <code>tl.model.search:Template</code> in configuration.
	 * </p>
	 */
	String SUBJECT_TEMPLATE_ATTR = "subjectTemplate";

	/**
	 * Getter for part {@link #CONTENT_TEMPLATE_ATTR}.
	 */
	default com.top_logic.model.search.expr.SearchExpression getContentTemplate() {
		return (com.top_logic.model.search.expr.SearchExpression) tValueByName(CONTENT_TEMPLATE_ATTR);
	}

	/**
	 * Setter for part {@link #CONTENT_TEMPLATE_ATTR}.
	 */
	default void setContentTemplate(com.top_logic.model.search.expr.SearchExpression newValue) {
		tUpdateByName(CONTENT_TEMPLATE_ATTR, newValue);
	}

	/**
	 * Getter for part {@link #RECEIVER_GROUPS_ATTR}.
	 */
	@SuppressWarnings("unchecked")
	default java.util.Set<? extends com.top_logic.tool.boundsec.wrap.Group> getReceiverGroups() {
		return (java.util.Set<? extends com.top_logic.tool.boundsec.wrap.Group>) tValueByName(RECEIVER_GROUPS_ATTR);
	}

	/**
	 * Live view of the {@link #RECEIVER_GROUPS_ATTR} part.
	 * <p>
	 * Changes to this {@link java.util.Collection} change directly the attribute value.
	 * The caller has to take care of the transaction handling.
	 * </p>
	 */
	default java.util.Set<com.top_logic.tool.boundsec.wrap.Group> getReceiverGroupsModifiable() {
		com.top_logic.model.TLStructuredTypePart attribute = tType().getPart(RECEIVER_GROUPS_ATTR);
		@SuppressWarnings("unchecked")
		java.util.Set<com.top_logic.tool.boundsec.wrap.Group> result = (java.util.Set<com.top_logic.tool.boundsec.wrap.Group>) com.top_logic.element.meta.kbbased.WrapperMetaAttributeUtil.getLiveCollection(this, attribute);
		return result;
	}

	/**
	 * Setter for part {@link #RECEIVER_GROUPS_ATTR}.
	 */
	default void setReceiverGroups(java.util.Set<com.top_logic.tool.boundsec.wrap.Group> newValue) {
		tUpdateByName(RECEIVER_GROUPS_ATTR, newValue);
	}

	/**
	 * Adds a value to the {@link #RECEIVER_GROUPS_ATTR} reference.
	 */
	default void addReceiverGroup(com.top_logic.tool.boundsec.wrap.Group newValue) {
		tAddByName(RECEIVER_GROUPS_ATTR, newValue);
	}

	/**
	 * Removes the given value from the {@link #RECEIVER_GROUPS_ATTR} reference.
	 */
	default void removeReceiverGroup(com.top_logic.tool.boundsec.wrap.Group oldValue) {
		tRemoveByName(RECEIVER_GROUPS_ATTR, oldValue);
	}

	/**
	 * Getter for part {@link #RECEIVER_RULE_ATTR}.
	 */
	default com.top_logic.model.search.expr.SearchExpression getReceiverRule() {
		return (com.top_logic.model.search.expr.SearchExpression) tValueByName(RECEIVER_RULE_ATTR);
	}

	/**
	 * Setter for part {@link #RECEIVER_RULE_ATTR}.
	 */
	default void setReceiverRule(com.top_logic.model.search.expr.SearchExpression newValue) {
		tUpdateByName(RECEIVER_RULE_ATTR, newValue);
	}

	/**
	 * Getter for part {@link #SUBJECT_TEMPLATE_ATTR}.
	 */
	default com.top_logic.model.search.expr.SearchExpression getSubjectTemplate() {
		return (com.top_logic.model.search.expr.SearchExpression) tValueByName(SUBJECT_TEMPLATE_ATTR);
	}

	/**
	 * Setter for part {@link #SUBJECT_TEMPLATE_ATTR}.
	 */
	default void setSubjectTemplate(com.top_logic.model.search.expr.SearchExpression newValue) {
		tUpdateByName(SUBJECT_TEMPLATE_ATTR, newValue);
	}

}
