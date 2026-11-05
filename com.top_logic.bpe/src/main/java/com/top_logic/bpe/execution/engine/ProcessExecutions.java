/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.execution.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.basic.util.ResKey;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.bpe.bpml.model.Edge;
import com.top_logic.bpe.bpml.model.StartEvent;
import com.top_logic.bpe.bpml.model.TlBpeBpmlFactory;
import com.top_logic.bpe.execution.model.ProcessExecution;
import com.top_logic.bpe.execution.model.Token;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.model.copy.CopyOperation;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * Operations of a user working on processes: the open tasks of a person, starting a process, the
 * choices for finishing a task and finishing it.
 *
 * <p>
 * A task is an active {@link Token} on a node the user works on (see
 * {@link Token#getUserRelevant()}). None of the operations opens a transaction. An operation
 * modifying the knowledge base must be called within a transaction of the caller.
 * </p>
 *
 * @see GuiEngine
 * @see com.top_logic.bpe.execution.script.BPEFunctions
 */
public final class ProcessExecutions {

	private ProcessExecutions() {
		// Static API.
	}

	/**
	 * The open tasks the given person is an actor for.
	 *
	 * @see GuiEngine#isActor(Person, Token)
	 */
	public static List<Token> tasks(Person person) {
		GuiEngine engine = GuiEngine.getInstance();
		List<Token> result = new ArrayList<>();
		for (ProcessExecution execution : ExecutionEngine.getInstance().getAllExecutions()) {
			for (Token token : execution.getUserRelevantTokens()) {
				if (engine.isActor(person, token)) {
					result.add(token);
				}
			}
		}
		return result;
	}

	/**
	 * The {@link StartEvent}s of all released workflows the given person may start a process
	 * with.
	 *
	 * @see GuiEngine#getStartEventsFor(Collaboration, Person)
	 */
	public static List<StartEvent> startEvents(Person person) {
		GuiEngine engine = GuiEngine.getInstance();
		List<StartEvent> result = new ArrayList<>();
		try (CloseableIterator<Collaboration> collaborations = MetaElementUtil
			.iterateDirectInstances(TlBpeBpmlFactory.getCollaborationType(), Collaboration.class)) {
			collaborations.forEachRemaining(c -> result.addAll(engine.getStartEventsFor(c, person)));
		}
		return result;
	}

	/**
	 * The {@link StartEvent} of a released workflow, identified by the names of the workflow and
	 * the event, the given person may start a process with.
	 *
	 * <p>
	 * A view offering to start a process of one workflow - a button next to the data the process
	 * is about - finds the start event this way and is disabled while the result is
	 * <code>null</code>.
	 * </p>
	 *
	 * @param workflowName
	 *        The {@link Collaboration#getName() name} of the workflow.
	 * @param eventName
	 *        The name of the start event, <code>null</code> for the only manual start event of the
	 *        workflow.
	 * @return The start event, <code>null</code> if there is no released workflow of that name
	 *         with such a start event, or the person may not start a process with it.
	 * @throws TopLogicException
	 *         If several start events match: the names do not identify the start event.
	 *
	 * @see #startEvents(Person)
	 */
	public static StartEvent startEvent(String workflowName, String eventName, Person person) {
		List<StartEvent> matches = startEvents(person).stream()
			.filter(event -> workflowName.equals(event.getProcess().getCollaboration().getName()))
			.filter(event -> eventName == null || eventName.equals(event.getName()))
			.toList();
		switch (matches.size()) {
			case 0:
				return null;
			case 1:
				return matches.get(0);
			default:
				throw new TopLogicException(
					I18NConstants.ERROR_AMBIGUOUS_START_EVENT__WORKFLOW_EVENT.fill(workflowName, eventName));
		}
	}

	/**
	 * The label of a {@link StartEvent} among the start events of all workflows: the name of its
	 * workflow, followed by the name of the event if the workflow can be started at several
	 * events.
	 */
	public static String startEventLabel(StartEvent startEvent) {
		Collaboration collaboration = startEvent.getProcess().getCollaboration();
		String workflowName = collaboration.getName();
		if (ExecutionEngine.getInstance().getManualStartEvents(collaboration).size() > 1) {
			return workflowName + " – " + name(startEvent);
		}
		return workflowName;
	}

	/**
	 * Whether the given person may start a process at the given {@link StartEvent}.
	 */
	public static boolean mayStart(Person person, StartEvent startEvent) {
		Collaboration collaboration = startEvent.getProcess().getCollaboration();
		return GuiEngine.getInstance().getStartEventsFor(collaboration, person).contains(startEvent);
	}

	/**
	 * Starts a process at the given {@link StartEvent} for the given person.
	 *
	 * <p>
	 * The new {@link ProcessExecution} runs to its first task (see {@link #startTask(ProcessExecution)}).
	 * </p>
	 *
	 * @throws TopLogicException
	 *         If the person {@link #mayStart(Person, StartEvent) may not start} the process.
	 */
	public static ProcessExecution start(StartEvent startEvent, Person person) {
		return start(startEvent, null, person);
	}

	/**
	 * Starts a process at the given {@link StartEvent} for the given person with a process object
	 * the caller prepared.
	 *
	 * <p>
	 * The prepared object becomes the {@link ProcessExecution}: a transient object - the draft a
	 * creation form has filled - is made persistent with its values, a persistent one is taken as it
	 * is. The process runs to its first task (see {@link #startTask(ProcessExecution)}).
	 * </p>
	 *
	 * @param process
	 *        The process object, an instance of the {@link ExecutionEngine#getModelType(StartEvent)
	 *        model type} of the process not started yet, transient or persistent;
	 *        <code>null</code> to allocate a new one.
	 * @return The started {@link ProcessExecution}, the persistent version of the given object.
	 *
	 * @throws TopLogicException
	 *         If the person {@link #mayStart(Person, StartEvent) may not start} the process, the
	 *         object is not of the model type of the process, or it was already started.
	 */
	public static ProcessExecution start(StartEvent startEvent, TLObject process, Person person) {
		if (!mayStart(person, startEvent)) {
			throw new TopLogicException(I18NConstants.ERROR_START_NOT_ALLOWED__EVENT.fill(name(startEvent)));
		}
		ProcessExecution execution =
			process == null ? ExecutionEngine.createProcessModel(startEvent) : prepared(startEvent, process);
		initialize(execution, startEvent);
		return execution;
	}

	private static ProcessExecution prepared(StartEvent startEvent, TLObject process) {
		TLClass modelType = ExecutionEngine.getModelType(startEvent);
		if (!TLModelUtil.isCompatibleInstance(modelType, process)) {
			throw new TopLogicException(
				I18NConstants.ERROR_NOT_OF_MODEL_TYPE__EVENT_TYPE.fill(name(startEvent), modelType));
		}
		ProcessExecution execution = (ProcessExecution) (process.tTransient() ? persistent(process) : process);
		if (execution.getProcess() != null) {
			throw new TopLogicException(I18NConstants.ERROR_ALREADY_STARTED__EVENT.fill(name(startEvent)));
		}
		return execution;
	}

	/**
	 * The persistent version of a transient object with its values and the parts of its
	 * compositions.
	 *
	 * <p>
	 * Whether the process may be started is decided by {@link #mayStart(Person, StartEvent)}, so
	 * the object is created without a check of the right to create it, as
	 * {@link ExecutionEngine#createProcessModel(StartEvent)} creates it. An attribute whose default
	 * is computed in the creating transaction (the execution number) gets the default of the
	 * persistent object.
	 * </p>
	 */
	private static TLObject persistent(TLObject draft) {
		CopyOperation operation = CopyOperation.initial();
		operation.setTransient(Boolean.FALSE);
		operation.withSecurity(Boolean.FALSE);
		operation.skipTransactionDefaults(true);
		TLObject result = (TLObject) operation.copyReference(draft);
		operation.finish();
		return result;
	}

	/**
	 * Binds a newly allocated {@link ProcessExecution} to the process of the given
	 * {@link StartEvent} and runs it to its first task.
	 *
	 * @see ExecutionEngine#createProcessModel(StartEvent)
	 * @see ExecutionEngine#init(ProcessExecution, StartEvent)
	 */
	public static void initialize(ProcessExecution execution, StartEvent startEvent) {
		execution.setProcess(startEvent.getProcess());
		execution.setCollaboration(startEvent.getProcess().getCollaboration());
		ExecutionEngine.getInstance().init(execution, startEvent);
	}

	/**
	 * The task of a just started process, the one a start dialog covers.
	 *
	 * <p>
	 * When the {@link StartEvent} does not {@link StartEvent#getRequireReview() require a review},
	 * the start dialog shows the form of this task, and the process continues with finishing it
	 * right after the start (see {@link #finish(Token, Edge, Object, Person)}).
	 * </p>
	 *
	 * @return The single active {@link Token} of the given {@link ProcessExecution},
	 *         <code>null</code> if there is none or more than one.
	 */
	public static Token startTask(ProcessExecution execution) {
		Set<Token> active = execution.getActiveTokens().stream()
			.filter(Token::getActive)
			.collect(Collectors.toSet());
		return active.size() == 1 ? active.iterator().next() : null;
	}

	/**
	 * Whether the given person may finish the given {@link Token}.
	 *
	 * <p>
	 * An actor of the task may finish it. Additionally, the person who started the process may
	 * finish its first task, which is shown in the start dialog.
	 * </p>
	 *
	 * @see GuiEngine#isActor(Person, Token)
	 */
	public static boolean mayFinish(Person person, Token token) {
		return GuiEngine.getInstance().isActor(person, token) || isStartedBy(token, person);
	}

	private static boolean isStartedBy(Token token, Person person) {
		Set<? extends Token> previous = token.getPrevious();
		return person != null && !previous.isEmpty() && previous.stream()
			.allMatch(p -> p.getNode() instanceof StartEvent && p.getFinishBy() == person);
	}

	/**
	 * The choices for finishing the given {@link Token}.
	 *
	 * @see GuiEngine#getDecisions(Token)
	 */
	public static List<Decision> decisions(Token token) {
		return GuiEngine.getInstance().getDecisions(token);
	}

	/**
	 * The choice for finishing the given {@link Token} identified by its
	 * {@link Decision#getLastEdge() last edge}.
	 *
	 * @param option
	 *        The last edge of the choice, <code>null</code> for the only choice.
	 * @return The choice, <code>null</code> if no such choice is offered, or if no option is given
	 *         and there is not exactly one choice.
	 */
	public static Decision decision(Token token, Edge option) {
		List<Decision> decisions = decisions(token);
		if (option == null) {
			return decisions.size() == 1 ? decisions.get(0) : null;
		}
		for (Decision decision : decisions) {
			if (decision.getLastEdge() == option) {
				return decision;
			}
		}
		return null;
	}

	/**
	 * The choice for finishing the given {@link Token} identified by its
	 * {@link Decision#getLastEdge() last edge}.
	 *
	 * @param option
	 *        The last edge of the choice, <code>null</code> for the only choice.
	 * @throws TopLogicException
	 *         If the choice is not offered, or no option is given and there is not exactly one
	 *         choice.
	 *
	 * @see #decision(Token, Edge)
	 */
	public static Decision decisionOrFail(Token token, Edge option) {
		Decision decision = decision(token, option);
		if (decision == null) {
			if (option == null) {
				throw new TopLogicException(I18NConstants.ERROR_DECISION_REQUIRED__TASK.fill(name(token)));
			}
			throw new TopLogicException(
				I18NConstants.ERROR_NO_SUCH_OPTION__TASK_OPTION.fill(name(token), option.getName()));
		}
		return decision;
	}

	/**
	 * Finishes the given {@link Token} for the given person with the given choice.
	 *
	 * @param option
	 *        The {@link Decision#getLastEdge() last edge} of the choice, <code>null</code> to take
	 *        the only choice of the task.
	 * @param additional
	 *        Additional data entered with the choice, an object of its
	 *        {@link Decision#getFormType() form type} (usually a transient one a form has filled),
	 *        or <code>null</code>. It is handed to the operations walking the path, see
	 *        {@link Decision#execute(Object)}.
	 * @param person
	 *        The person finishing the task.
	 * @return The task of the same process the person continues with: the single open task in it
	 *         the person is an {@link GuiEngine#isActor(Person, Token) actor} for, <code>null</code>
	 *         if there is none or more than one.
	 * @throws TopLogicException
	 *         If the task is not open, the person may not finish it, the choice is not offered or
	 *         its path is blocked by errors.
	 */
	public static Token finish(Token token, Edge option, Object additional, Person person) {
		if (!token.getActive()) {
			throw new TopLogicException(I18NConstants.ERROR_TASK_NOT_ACTIVE__TASK.fill(name(token)));
		}
		if (!mayFinish(person, token)) {
			throw new TopLogicException(I18NConstants.ERROR_NOT_ACTOR__TASK.fill(name(token)));
		}
		Decision decision = decisionOrFail(token, option);
		if (decision.isBlocked()) {
			throw new TopLogicException(
				I18NConstants.ERROR_BLOCKED__TASK_ERRORS.fill(name(token), String.join("\n", localize(decision.getErrors()))));
		}

		ProcessExecution execution = token.getProcessExecution();
		decision.execute(additional);

		Token next = null;
		for (Token candidate : execution.getUserRelevantTokens()) {
			if (GuiEngine.getInstance().isActor(person, candidate)) {
				if (next != null) {
					return null;
				}
				next = candidate;
			}
		}
		return next;
	}

	/**
	 * The given messages in the current user's language.
	 */
	public static List<String> localize(List<ResKey> messages) {
		Resources resources = Resources.getInstance();
		return messages.stream().map(resources::getString).toList();
	}

	private static String name(Token token) {
		return token.getNode().getName();
	}

	private static String name(StartEvent startEvent) {
		String name = startEvent.getName();
		return name != null ? name : startEvent.getProcess().getCollaboration().getName();
	}

}
