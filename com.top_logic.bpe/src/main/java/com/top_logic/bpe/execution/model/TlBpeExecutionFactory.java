/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.execution.model;

/**
 * Factory for <code>tl.bpe.execution</code> objects.
 * 
 * <p>
 * Note: this is generated code. Do not modify. Instead, create a subclass and register this in the module system.
 * </p>
 * 
 * @author Automatically generated by {@link com.top_logic.element.model.generate.FactoryGenerator}
 */
public class TlBpeExecutionFactory extends com.top_logic.element.meta.kbbased.AbstractElementFactory {

	/**
	 * Name of the structure <code>tl.bpe.execution</code> defined by {@link TlBpeExecutionFactory}.
	 */
	public static final String TL_BPE_EXECUTION_STRUCTURE = "tl.bpe.execution";

	/**
	 * Name of the enumeration <code>ExecutionState</code> in module {@value #TL_BPE_EXECUTION_STRUCTURE}.
	 */
	public static final String EXECUTION_STATE_ENUM = "ExecutionState";

	/**
	 * Name of the classifier <code>ABORTED</code> in enumeration {@value #EXECUTION_STATE_ENUM}.
	 */
	public static final String ABORTED_EXECUTION_STATE_CLASSIFIER = "ABORTED";

	/**
	 * Name of the classifier <code>FINISHED</code> in enumeration {@value #EXECUTION_STATE_ENUM}.
	 */
	public static final String FINISHED_EXECUTION_STATE_CLASSIFIER = "FINISHED";

	/**
	 * Name of the classifier <code>RUNNING</code> in enumeration {@value #EXECUTION_STATE_ENUM}.
	 */
	public static final String RUNNING_EXECUTION_STATE_CLASSIFIER = "RUNNING";

	/**
	 * Lookup {@link ProcessExecution} type.
	 */
	public static com.top_logic.model.TLClass getProcessExecutionType() {
		return (com.top_logic.model.TLClass) com.top_logic.util.model.ModelService.getApplicationModel().getModule(TL_BPE_EXECUTION_STRUCTURE).getType(ProcessExecution.PROCESS_EXECUTION_TYPE);
	}

	/**
	 * Lookup {@link ProcessExecution#ACTIVE_TOKENS_ATTR} of {@link ProcessExecution}.
	 */
	public static com.top_logic.model.TLReference getActiveTokensProcessExecutionAttr() {
		return (com.top_logic.model.TLReference) getProcessExecutionType().getPart(ProcessExecution.ACTIVE_TOKENS_ATTR);
	}

	/**
	 * Lookup {@link ProcessExecution#ALL_TOKENS_ATTR} of {@link ProcessExecution}.
	 */
	public static com.top_logic.model.TLReference getAllTokensProcessExecutionAttr() {
		return (com.top_logic.model.TLReference) getProcessExecutionType().getPart(ProcessExecution.ALL_TOKENS_ATTR);
	}

	/**
	 * Lookup {@link ProcessExecution#COLLABORATION_ATTR} of {@link ProcessExecution}.
	 */
	public static com.top_logic.model.TLReference getCollaborationProcessExecutionAttr() {
		return (com.top_logic.model.TLReference) getProcessExecutionType().getPart(ProcessExecution.COLLABORATION_ATTR);
	}

	/**
	 * Lookup {@link ProcessExecution#CREATED_BY_ATTR} of {@link ProcessExecution}.
	 */
	public static com.top_logic.model.TLReference getCreatedByProcessExecutionAttr() {
		return (com.top_logic.model.TLReference) getProcessExecutionType().getPart(ProcessExecution.CREATED_BY_ATTR);
	}

	/**
	 * Lookup {@link ProcessExecution#CREATED_BY_CONTACT_ATTR} of {@link ProcessExecution}.
	 */
	public static com.top_logic.model.TLReference getCreatedByContactProcessExecutionAttr() {
		return (com.top_logic.model.TLReference) getProcessExecutionType().getPart(ProcessExecution.CREATED_BY_CONTACT_ATTR);
	}

	/**
	 * Lookup {@link ProcessExecution#DURATION_IN_MINUTES_ATTR} of {@link ProcessExecution}.
	 */
	public static com.top_logic.model.TLProperty getDurationInMinutesProcessExecutionAttr() {
		return (com.top_logic.model.TLProperty) getProcessExecutionType().getPart(ProcessExecution.DURATION_IN_MINUTES_ATTR);
	}

	/**
	 * Lookup {@link ProcessExecution#EXECUTION_NUMBER_ATTR} of {@link ProcessExecution}.
	 */
	public static com.top_logic.model.TLProperty getExecutionNumberProcessExecutionAttr() {
		return (com.top_logic.model.TLProperty) getProcessExecutionType().getPart(ProcessExecution.EXECUTION_NUMBER_ATTR);
	}

	/**
	 * Lookup {@link ProcessExecution#EXECUTION_STATE_ATTR} of {@link ProcessExecution}.
	 */
	public static com.top_logic.model.TLReference getExecutionStateProcessExecutionAttr() {
		return (com.top_logic.model.TLReference) getProcessExecutionType().getPart(ProcessExecution.EXECUTION_STATE_ATTR);
	}

	/**
	 * Lookup {@link ProcessExecution#PROCESS_ATTR} of {@link ProcessExecution}.
	 */
	public static com.top_logic.model.TLReference getProcessProcessExecutionAttr() {
		return (com.top_logic.model.TLReference) getProcessExecutionType().getPart(ProcessExecution.PROCESS_ATTR);
	}

	/**
	 * Lookup {@link ProcessExecution#USER_RELEVANT_TOKENS_ATTR} of {@link ProcessExecution}.
	 */
	public static com.top_logic.model.TLReference getUserRelevantTokensProcessExecutionAttr() {
		return (com.top_logic.model.TLReference) getProcessExecutionType().getPart(ProcessExecution.USER_RELEVANT_TOKENS_ATTR);
	}

	/**
	 * Lookup {@link Token} type.
	 */
	public static com.top_logic.model.TLClass getTokenType() {
		return (com.top_logic.model.TLClass) com.top_logic.util.model.ModelService.getApplicationModel().getModule(TL_BPE_EXECUTION_STRUCTURE).getType(Token.TOKEN_TYPE);
	}

	/**
	 * Lookup {@link Token#ACTIVE_ATTR} of {@link Token}.
	 */
	public static com.top_logic.model.TLProperty getActiveTokenAttr() {
		return (com.top_logic.model.TLProperty) getTokenType().getPart(Token.ACTIVE_ATTR);
	}

	/**
	 * Lookup {@link Token#ACTOR_ATTR} of {@link Token}.
	 */
	public static com.top_logic.model.TLProperty getActorTokenAttr() {
		return (com.top_logic.model.TLProperty) getTokenType().getPart(Token.ACTOR_ATTR);
	}

	/**
	 * Lookup {@link Token#DESCRIPTION_I18N_ATTR} of {@link Token}.
	 */
	public static com.top_logic.model.TLProperty getDescriptionI18NTokenAttr() {
		return (com.top_logic.model.TLProperty) getTokenType().getPart(Token.DESCRIPTION_I18N_ATTR);
	}

	/**
	 * Lookup {@link Token#DURATION_IN_MINUTES_ATTR} of {@link Token}.
	 */
	public static com.top_logic.model.TLProperty getDurationInMinutesTokenAttr() {
		return (com.top_logic.model.TLProperty) getTokenType().getPart(Token.DURATION_IN_MINUTES_ATTR);
	}

	/**
	 * Lookup {@link Token#EXECUTION_NUMBER_ATTR} of {@link Token}.
	 */
	public static com.top_logic.model.TLProperty getExecutionNumberTokenAttr() {
		return (com.top_logic.model.TLProperty) getTokenType().getPart(Token.EXECUTION_NUMBER_ATTR);
	}

	/**
	 * Lookup {@link Token#FINISH_BY_ATTR} of {@link Token}.
	 */
	public static com.top_logic.model.TLReference getFinishByTokenAttr() {
		return (com.top_logic.model.TLReference) getTokenType().getPart(Token.FINISH_BY_ATTR);
	}

	/**
	 * Lookup {@link Token#FINISH_DATE_ATTR} of {@link Token}.
	 */
	public static com.top_logic.model.TLProperty getFinishDateTokenAttr() {
		return (com.top_logic.model.TLProperty) getTokenType().getPart(Token.FINISH_DATE_ATTR);
	}

	/**
	 * Lookup {@link Token#ICON_ATTR} of {@link Token}.
	 */
	public static com.top_logic.model.TLProperty getIconTokenAttr() {
		return (com.top_logic.model.TLProperty) getTokenType().getPart(Token.ICON_ATTR);
	}

	/**
	 * Lookup {@link Token#NAME_ATTR} of {@link Token}.
	 */
	public static com.top_logic.model.TLProperty getNameTokenAttr() {
		return (com.top_logic.model.TLProperty) getTokenType().getPart(Token.NAME_ATTR);
	}

	/**
	 * Lookup {@link Token#NEXT_ATTR} of {@link Token}.
	 */
	public static com.top_logic.model.TLReference getNextTokenAttr() {
		return (com.top_logic.model.TLReference) getTokenType().getPart(Token.NEXT_ATTR);
	}

	/**
	 * Lookup {@link Token#NODE_ATTR} of {@link Token}.
	 */
	public static com.top_logic.model.TLReference getNodeTokenAttr() {
		return (com.top_logic.model.TLReference) getTokenType().getPart(Token.NODE_ATTR);
	}

	/**
	 * Lookup {@link Token#PREVIOUS_ATTR} of {@link Token}.
	 */
	public static com.top_logic.model.TLReference getPreviousTokenAttr() {
		return (com.top_logic.model.TLReference) getTokenType().getPart(Token.PREVIOUS_ATTR);
	}

	/**
	 * Lookup {@link Token#PROCESS_EXECUTION_ATTR} of {@link Token}.
	 */
	public static com.top_logic.model.TLReference getProcessExecutionTokenAttr() {
		return (com.top_logic.model.TLReference) getTokenType().getPart(Token.PROCESS_EXECUTION_ATTR);
	}

	/**
	 * Lookup {@link Token#START_DATE_ATTR} of {@link Token}.
	 */
	public static com.top_logic.model.TLProperty getStartDateTokenAttr() {
		return (com.top_logic.model.TLProperty) getTokenType().getPart(Token.START_DATE_ATTR);
	}

	/**
	 * Lookup {@link Token#USER_RELEVANT_ATTR} of {@link Token}.
	 */
	public static com.top_logic.model.TLProperty getUserRelevantTokenAttr() {
		return (com.top_logic.model.TLProperty) getTokenType().getPart(Token.USER_RELEVANT_ATTR);
	}

	/**
	 * Lookup {@value #EXECUTION_STATE_ENUM} enumeration.
	 */
	public static com.top_logic.model.TLEnumeration getExecutionStateEnum() {
		return (com.top_logic.model.TLEnumeration) com.top_logic.util.model.ModelService.getApplicationModel().getModule(TL_BPE_EXECUTION_STRUCTURE).getType(EXECUTION_STATE_ENUM);
	}

	/**
	 * Lookup classifier {@value #ABORTED_EXECUTION_STATE_CLASSIFIER} of enumeration {@value #EXECUTION_STATE_ENUM}.
	 */
	public static com.top_logic.model.TLClassifier getABORTEDExecutionStateClassifier() {
		return getExecutionStateEnum().getClassifier(ABORTED_EXECUTION_STATE_CLASSIFIER);
	}

	/**
	 * Lookup classifier {@value #FINISHED_EXECUTION_STATE_CLASSIFIER} of enumeration {@value #EXECUTION_STATE_ENUM}.
	 */
	public static com.top_logic.model.TLClassifier getFINISHEDExecutionStateClassifier() {
		return getExecutionStateEnum().getClassifier(FINISHED_EXECUTION_STATE_CLASSIFIER);
	}

	/**
	 * Lookup classifier {@value #RUNNING_EXECUTION_STATE_CLASSIFIER} of enumeration {@value #EXECUTION_STATE_ENUM}.
	 */
	public static com.top_logic.model.TLClassifier getRUNNINGExecutionStateClassifier() {
		return getExecutionStateEnum().getClassifier(RUNNING_EXECUTION_STATE_CLASSIFIER);
	}

	/**
	 * Name of type <code>ProcessExecution</code> in structure {@link #TL_BPE_EXECUTION_STRUCTURE}.
	 * 
	 * @deprecated Use {@link ProcessExecution#PROCESS_EXECUTION_TYPE}.
	 */
	@Deprecated
	public static final String PROCESS_EXECUTION_NODE = ProcessExecution.PROCESS_EXECUTION_TYPE;

	/**
	 * Storage table name of {@link #PROCESS_EXECUTION_NODE} objects.
	 */
	public static final String KO_NAME_PROCESS_EXECUTION = "BPExecution";

	/**
	 * Name of type <code>Token</code> in structure {@link #TL_BPE_EXECUTION_STRUCTURE}.
	 * 
	 * @deprecated Use {@link Token#TOKEN_TYPE}.
	 */
	@Deprecated
	public static final String TOKEN_NODE = Token.TOKEN_TYPE;

	/**
	 * Storage table name of {@link #TOKEN_NODE} objects.
	 */
	public static final String KO_NAME_TOKEN = "BPExecution";


	/**
	 * Create an instance of {@link ProcessExecution} type.
	 */
	public final ProcessExecution createProcessExecution(com.top_logic.model.TLObject context) {
		return (ProcessExecution) createObject(getProcessExecutionType(), context);
	}

	/**
	 * Create an instance of {@link ProcessExecution} type.
	 */
	public final ProcessExecution createProcessExecution() {
		return createProcessExecution(null);
	}

	/**
	 * Create an instance of {@link Token} type.
	 */
	public final Token createToken(com.top_logic.model.TLObject context) {
		return (Token) createObject(getTokenType(), context);
	}

	/**
	 * Create an instance of {@link Token} type.
	 */
	public final Token createToken() {
		return createToken(null);
	}

	/**
	 * The singleton instance of {@link TlBpeExecutionFactory}.
	 */
	public static TlBpeExecutionFactory getInstance() {
		return (TlBpeExecutionFactory) com.top_logic.element.model.DynamicModelService.getFactoryFor(TL_BPE_EXECUTION_STRUCTURE);
	}
}
