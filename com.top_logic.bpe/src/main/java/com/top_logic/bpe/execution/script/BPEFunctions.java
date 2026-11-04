/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.execution.script;

import java.util.List;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.util.ResKey;
import com.top_logic.bpe.bpml.model.Edge;
import com.top_logic.bpe.bpml.model.StartEvent;
import com.top_logic.bpe.execution.engine.Decision;
import com.top_logic.bpe.execution.engine.ExecutionEngine;
import com.top_logic.bpe.execution.engine.ProcessExecutions;
import com.top_logic.bpe.execution.model.ProcessExecution;
import com.top_logic.bpe.execution.model.Token;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.util.TLContext;

/**
 * TL-Script functions for working on processes: the open tasks of a user, starting a process,
 * the options for completing a task and completing it.
 *
 * <p>
 * An <i>option</i> for completing a task is an edge of the workflow (<code>tl.bpe.bpml:Edge</code>):
 * the outgoing edge of the task, or, when that edge leads to a gateway at which the user decides,
 * the chosen outgoing edge of the gateway. {@link #finish(Token, Edge, Object)} walks the path
 * through the gateway that the option stands for.
 * </p>
 *
 * <p>
 * The functions modifying processes open no transaction of their own. In a view, they are called
 * within a <code>&lt;with-transaction&gt;</code>.
 * </p>
 *
 * @implNote Every public static method here is a TL-Script function named by the
 *           {@link ScriptPrefix} of this class followed by the capitalized method name, so
 *           {@link #tasks(Person)} is called <code>bpeTasks</code>.
 *
 * @see ProcessExecutions
 */
@ScriptPrefix(BPEFunctions.PREFIX)
public class BPEFunctions extends TLScriptFunctions {

	/** Prefix all function names of this class start with. */
	public static final String PREFIX = "bpe";

	/**
	 * The open tasks of a user.
	 *
	 * @param person
	 *        The user whose tasks are listed, the current user by default.
	 * @return The active tokens the user is an actor for.
	 */
	@SideEffectFree
	public static List<Token> tasks(Person person) {
		return ProcessExecutions.tasks(user(person));
	}

	/**
	 * The start events of the released workflows a user may start a process with.
	 *
	 * @param person
	 *        The user starting processes, the current user by default.
	 * @return The start events.
	 */
	@SideEffectFree
	public static List<StartEvent> startEvents(Person person) {
		return ProcessExecutions.startEvents(user(person));
	}

	/**
	 * Starts a process at a start event.
	 *
	 * <p>
	 * The process runs to its first task, see {@link #startTask(ProcessExecution)}.
	 * </p>
	 *
	 * @param startEvent
	 *        The start event the current user starts the process with.
	 * @param process
	 *        The object becoming the process execution, an instance of the
	 *        {@link #modelType(StartEvent) model type} of the process: a transient object a
	 *        creation form has filled, which is made persistent with its values, or a persistent
	 *        object not started yet. A new object is created if omitted.
	 * @return The new process execution.
	 */
	public static ProcessExecution start(@Mandatory StartEvent startEvent, TLObject process) {
		return ProcessExecutions.start(startEvent, process, TLContext.currentUser());
	}

	/**
	 * The type of the process execution a start event starts.
	 *
	 * <p>
	 * A view creates a transient object of this type for the creation form of a process and starts
	 * the process with it, see {@link #start(StartEvent, TLObject)}.
	 * </p>
	 *
	 * @param startEvent
	 *        The start event.
	 * @return The model type of the participant of the start event's process, the type
	 *         <code>tl.bpe.execution:ProcessExecution</code> if the participant has none.
	 */
	@SideEffectFree
	public static TLClass modelType(@Mandatory StartEvent startEvent) {
		return ExecutionEngine.getModelType(startEvent);
	}

	/**
	 * The first task of a just started process.
	 *
	 * <p>
	 * When the start event does not require a review, the start dialog shows the form of this task
	 * and completes it right after the start with {@link #finish(Token, Edge, Object)}.
	 * </p>
	 *
	 * @param execution
	 *        The process execution.
	 * @return The single active token of the process, <code>null</code> if there is none or more
	 *         than one.
	 */
	@SideEffectFree
	public static Token startTask(@Mandatory ProcessExecution execution) {
		return ProcessExecutions.startTask(execution);
	}

	/**
	 * The options for completing a task.
	 *
	 * <p>
	 * Options blocked by errors are included, see {@link #errors(Token, Edge)}. Options hidden by
	 * their conditions are not.
	 * </p>
	 *
	 * @param token
	 *        The task.
	 * @return The edges standing for the options, ordered by their weight first, then by their
	 *         label.
	 */
	@SideEffectFree
	public static List<Edge> options(@Mandatory Token token) {
		return ProcessExecutions.decisions(token).stream().map(Decision::getLastEdge).toList();
	}

	/**
	 * The option preselected when completing a task: the only option, or the default flow of the
	 * gateway the user decides at.
	 *
	 * @param token
	 *        The task.
	 * @return The edge standing for the option, <code>null</code> if no option is preselected.
	 */
	@SideEffectFree
	public static Edge defaultOption(@Mandatory Token token) {
		List<Decision> decisions = ProcessExecutions.decisions(token);
		if (decisions.size() == 1) {
			return decisions.get(0).getLastEdge();
		}
		return decisions.stream().filter(Decision::isDefault).map(Decision::getLastEdge).findFirst().orElse(null);
	}

	/**
	 * The label of an option for completing a task.
	 *
	 * @param token
	 *        The task.
	 * @param option
	 *        The edge standing for the option.
	 * @return The title or name of the edge, or the name of its target.
	 */
	@SideEffectFree
	public static ResKey optionLabel(@Mandatory Token token, @Mandatory Edge option) {
		return decision(token, option).getLabel();
	}

	/**
	 * The errors that prevent completing a task with an option.
	 *
	 * @param token
	 *        The task.
	 * @param option
	 *        The edge standing for the option.
	 * @return The messages in the current user's language, empty if the option can be taken.
	 */
	@SideEffectFree
	public static List<String> errors(@Mandatory Token token, @Mandatory Edge option) {
		return ProcessExecutions.localize(decision(token, option).getErrors());
	}

	/**
	 * The warnings the user confirms before completing a task with an option.
	 *
	 * @param token
	 *        The task.
	 * @param option
	 *        The edge standing for the option.
	 * @return The messages in the current user's language, empty if there is nothing to confirm.
	 */
	@SideEffectFree
	public static List<String> warnings(@Mandatory Token token, @Mandatory Edge option) {
		return ProcessExecutions.localize(decision(token, option).getWarnings());
	}

	/**
	 * The type of the additional data entered with an option for completing a task.
	 *
	 * @param token
	 *        The task.
	 * @param option
	 *        The edge standing for the option.
	 * @return The type, <code>null</code> if no additional data is entered.
	 */
	@SideEffectFree
	public static TLClass formType(@Mandatory Token token, @Mandatory Edge option) {
		return decision(token, option).getFormType();
	}

	/**
	 * Completes a task for the current user.
	 *
	 * @param token
	 *        The task.
	 * @param option
	 *        The edge standing for the chosen option, see {@link #options(Token)}. May be omitted
	 *        for a task with a single option.
	 * @param additional
	 *        Additional data entered with the option, an object of its
	 *        {@link #formType(Token, Edge) form type}, usually a transient one a form has filled.
	 *        It is handed to the operations walking the option's path (the default operation of
	 *        the participant and the operations of the sequence flows) as their fourth argument,
	 *        which keep what the process needs of it.
	 * @return The task of the same process the current user continues with, <code>null</code> if
	 *         there is none or more than one.
	 */
	public static Token finish(@Mandatory Token token, Edge option, Object additional) {
		return ProcessExecutions.finish(token, option, additional, TLContext.currentUser());
	}

	private static Decision decision(Token token, Edge option) {
		return ProcessExecutions.decisionOrFail(token, option);
	}

	private static Person user(Person person) {
		return person != null ? person : TLContext.currentUser();
	}

}
