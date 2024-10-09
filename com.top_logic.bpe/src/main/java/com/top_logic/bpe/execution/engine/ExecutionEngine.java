/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.execution.engine;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.bpe.bpml.model.BoundaryEvent;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.bpe.bpml.model.Edge;
import com.top_logic.bpe.bpml.model.EndEvent;
import com.top_logic.bpe.bpml.model.Event;
import com.top_logic.bpe.bpml.model.EventDefinition;
import com.top_logic.bpe.bpml.model.ExclusiveGateway;
import com.top_logic.bpe.bpml.model.FlowSource;
import com.top_logic.bpe.bpml.model.FlowTarget;
import com.top_logic.bpe.bpml.model.IntermediateThrowEvent;
import com.top_logic.bpe.bpml.model.MessageEventDefinition;
import com.top_logic.bpe.bpml.model.MessageFlow;
import com.top_logic.bpe.bpml.model.Node;
import com.top_logic.bpe.bpml.model.ParallelGateway;
import com.top_logic.bpe.bpml.model.Process;
import com.top_logic.bpe.bpml.model.SendTask;
import com.top_logic.bpe.bpml.model.SequenceFlow;
import com.top_logic.bpe.bpml.model.ServiceTask;
import com.top_logic.bpe.bpml.model.StartEvent;
import com.top_logic.bpe.bpml.model.Task;
import com.top_logic.bpe.bpml.model.TimerEventDefinition;
import com.top_logic.bpe.execution.model.ProcessExecution;
import com.top_logic.bpe.execution.model.TlBpeExecutionFactory;
import com.top_logic.bpe.execution.model.Token;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.util.TLContext;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class ExecutionEngine {

	private static final Filter MANUAL_START_EVENT_FILTER = new Filter() {

		@Override
		public boolean accept(Object anObject) {
			if (anObject instanceof StartEvent) {
				EventDefinition definition = ((StartEvent) anObject).getDefinition();
				return definition == null;
			}
			return false;
		}

	};

	private static final Filter TIMER_START_EVENT_FILTER = new Filter() {

		@Override
		public boolean accept(Object anObject) {
			if (anObject instanceof StartEvent) {
				EventDefinition definition = ((StartEvent) anObject).getDefinition();
				return definition instanceof TimerEventDefinition;
			}
			return false;
		}

	};

	private static ExecutionEngine INSTANCE = new ExecutionEngine();

	public static ExecutionEngine getInstance() {
		return INSTANCE;
	}

	private ExecutionEngine() {

	}

	/**
	 * updates all existing {@link ProcessExecution} do this until no new executions appear during
	 * the update
	 *
	 */
	public void updateAll() {

		int emergencyStop = 0;

		Set<ProcessExecution> newExecutions = new HashSet<>();
		do {
			Collection<ProcessExecution> allExecutions = getAllExecutions();
			for (ProcessExecution execution : allExecutions) {
				update(execution);
			}
			newExecutions = new HashSet<>(getAllExecutions());
			newExecutions.removeAll(allExecutions);

			emergencyStop++;
			if (emergencyStop > 1000) {
				throw new RuntimeException("More than 1000 cycles. Can not complete update for executions");
			}

		} while (!newExecutions.isEmpty());

	}

	/**
	 * a set with all instances of {@link ProcessExecution} and all instances of all
	 *         subtypes
	 */
	public Collection<ProcessExecution> getAllExecutions() {
		TLClass processExecutionType = TlBpeExecutionFactory.getProcessExecutionType();
		return MetaElementUtil.getAllInstancesOf(processExecutionType, ProcessExecution.class);
	}

	/**
	 * updates the given {@link ProcessExecution}, i.e. calculates new tokens if needed until
	 * nothing more has to be done
	 */
	public void update(ProcessExecution execution) {

		int emergencyStop = 0;
		boolean allTokensFinished = false;
		while (!allTokensFinished) {
			emergencyStop++;
			if (emergencyStop > 1000) {
				throw new RuntimeException("More than 1000 cycles. Can not complete update for " + execution);
			}
			allTokensFinished = true;

			// make copy of the set of active tokens because it may change during execution
			Set<? extends Token> activeTokens = new HashSet(execution.getActiveTokens());
			activeTokens.removeAll(execution.getUserRelevantTokens());

			if (activeTokens.size() > 0) {
				for (Token token : activeTokens) {
					boolean anyChange = doExecute(token, execution);
					if (anyChange) {
						// break after first change because during execution some tokens may have
						// become not active

						Logger.info("Change for token " + token + " and node " + token.getNode(),
							ExecutionEngine.class);

						allTokensFinished = false;
						break;
					}
				}
			}

		}
	}

	/**
	 * executes toe given token, i.e. checks if anything can be performed for the token and performs
	 * it
	 */
	private boolean doExecute(Token token, ProcessExecution processExecution) {
		Node node = token.getNode();
		if (node instanceof ParallelGateway) {
			ParallelGateway parallelGateway = (ParallelGateway) node;
			if (checkForCompletion(processExecution, parallelGateway)) {
				handleCompletion(processExecution, parallelGateway);
				return true;
			}
		}
		else if (node instanceof ExclusiveGateway) {
			ExclusiveGateway exclusiveGateway = (ExclusiveGateway) node;

			Node nextNode = calculateNextNode(exclusiveGateway, processExecution);
			if (nextNode != null) {
				moveActiveTokenTo(token, nextNode);
				return true;
			}
		}
		else if (node instanceof StartEvent) {
			StartEvent startEvent = (StartEvent) node;
			EventDefinition definition = startEvent.getDefinition();
			if (definition instanceof TimerEventDefinition) {
				return handleTimerEvent(token, processExecution, startEvent, (TimerEventDefinition) definition);
			} else {
				handleStartEvent(token, processExecution, (StartEvent) node);
				return true;
			}
		}
		else if (node instanceof EndEvent) {
			handleEndEvent(processExecution);
			return true;
		}
		else if (node instanceof BoundaryEvent) {
			BoundaryEvent event = (BoundaryEvent) node;

			// has the holder of the BoundaryEvent an active token?
			Task task = event.getAttachedTo();
			if (hasActiveToken(processExecution, task)) {
				EventDefinition definition = event.getDefinition();
				if (definition instanceof TimerEventDefinition) {
					return handleBoundaryTimerEvent(token, processExecution, event, (TimerEventDefinition) definition);
				}
			}
			else {
				// the task of the BoundaryEvent is not active any more
				// cancel the Boundary event
				changeActiveTokens(processExecution, token, null);
			}

		}
		else if (node instanceof SendTask) {
			executeSendTask(token, processExecution, (SendTask) node);
			return true;
		}
		else if (node instanceof ServiceTask) {
			executeServiceTask(token, processExecution, (ServiceTask) node);
			return true;
		}
		else if (node instanceof IntermediateThrowEvent) {
			IntermediateThrowEvent event = (IntermediateThrowEvent) node;
			EventDefinition definition = event.getDefinition();
			if (definition instanceof MessageEventDefinition) {
				handleMessageEvent(token, processExecution, (IntermediateThrowEvent) node,
					(MessageEventDefinition) definition);
			}

			return true;
		}

		else if (node != null) {
			int i = 0;
			i++;
		}

		return false;
	}

	private void handleMessageEvent(Token token, ProcessExecution processExecution, IntermediateThrowEvent event,
			MessageEventDefinition definition) {

		// find the Flow
		MessageFlow flow = getFlowForSource(processExecution, event);
		FlowTarget flowTarget = flow.getTarget();

		// create new ProcessExecution
		if (flowTarget instanceof StartEvent) {
			StartEvent startEvent = (StartEvent) flowTarget;
			ProcessExecution execution = ExecutionProcessCreateHandler.newProcessExecution(startEvent);
			execution.setCollaboration(processExecution.getCollaboration());
			execution.setProcess(startEvent.getProcess());
			TLStructuredType tType = execution.tType();
			// connect the origin ProcessExecution with the new one

			SearchExpression connectExpession = flow.getConnectExpession();
			if (connectExpession != null) {
				calculate(connectExpession, processExecution, execution);
			}

			// init the new one
			init(execution, startEvent);
		}

		// move the original token
		createTokensForOutgoings(token, processExecution, event);
	}



	private MessageFlow getFlowForSource(ProcessExecution processExecution, FlowSource source) {
		Set<? extends MessageFlow> messageFlows = processExecution.getCollaboration().getMessageFlows();
		for (MessageFlow flow : messageFlows) {
			if (source == flow.getSource()) {
				return flow;
			}
		}
		return null;
	}

	private boolean hasActiveToken(ProcessExecution processExecution, Task task) {
		Set<? extends Token> activeTokens = processExecution.getActiveTokens();
		for (Token token : activeTokens) {
			if (token.getNode() == task) {
				return true;
			}
		}
		return false;
	}

	private Token getActiveTokenForTask(ProcessExecution processExecution, Task task) {
		Set<? extends Token> allTokens = processExecution.getAllTokens();
		for (Token token : allTokens) {
			if (token.getNode() == task) {
				if (token.getActive()) {
					return token;
				}
			}
		}
		return null;
	}

	private boolean handleBoundaryTimerEvent(Token token, ProcessExecution processExecution, BoundaryEvent event,
			TimerEventDefinition definition) {
		boolean timerReached =
			handleTimerEvent(token, processExecution, event, definition);
		if (timerReached) {
			if (event.getCancelActivity()) {
				Task task = event.getAttachedTo();
				Token taskToken = getActiveTokenForTask(processExecution, task);
				changeActiveTokens(processExecution, taskToken, null);
			}
		}
		return timerReached;
	}

	private void executeSendTask(Token token, ProcessExecution processExecution, SendTask sendTask) {

		BPMailer mailer = new BPMailer(token, processExecution, sendTask);
		mailer.sendMail();

		createTokensForOutgoings(token, processExecution, sendTask);
	}

	private void executeServiceTask(Token token, ProcessExecution processExecution, ServiceTask serviceTask) {
		SearchExpression action = serviceTask.getAction();

		if (action != null) {
			calculate(action, processExecution);
		}

		createTokensForOutgoings(token, processExecution, serviceTask);
	}

	private boolean handleTimerEvent(Token token, ProcessExecution processExecution,
			Event node, TimerEventDefinition definition) {
		// check if the timeout is reached
		Date created = token.tCreationDate();
		if (created != null) {
			// token must be existing, i.e. do not do this during creation of token

			long delayInMillis = definition.getDelayInMillis();

			Date now = new Date();
			if (now.getTime() - created.getTime() > delayInMillis) {
				// timeout reached
				createTokensForOutgoings(token, processExecution, node);
				return true;
			}
		}
		return false;
	}

	private void handleStartEvent(Token token, ProcessExecution processExecution, StartEvent startEvent) {
		createTokensForOutgoings(token, processExecution, startEvent);
	}

	private void createTokensForOutgoings(Token token, ProcessExecution processExecution, Node node) {
		for (Edge edge : node.getOutgoing()) {
			Node target = edge.getTarget();
			Token newToken = createTokenFor(processExecution, target, token);
			changeActiveTokens(processExecution, token, newToken);

			// may be there are boundary events
			activateBoundaryEvents(token, processExecution, target);

		}

		// beware: if there are no outgoings, the token has to be removed from the active tokens
		changeActiveTokens(processExecution, token, null);
	}

	/**
	 * activates all {@link BoundaryEvent} attached to target token is the parent token of the
	 * tokens which are placed on the {@link BoundaryEvent}s
	 */
	private void activateBoundaryEvents(Token token, ProcessExecution processExecution, Node target) {
		Set<? extends Node> additionalNodes = getAdditionalNodes(target);
		for (Node additional : additionalNodes) {
			Token newToken = createTokenFor(processExecution, additional, token);
			changeActiveTokens(processExecution, token, newToken);
		}
	}

	/**
	 * a set with all nodes for which also a token must be created if a token on the given
	 *         node is set
	 */
	private Set<? extends Node> getAdditionalNodes(Node node) {
		if (node instanceof Task) {
			return ((Task) node).getBoundaryEvents();
		}
		return Collections.emptySet();
	}

	/**
	 * returns the target of the first edge for which the configured rule is true
	 */
	private Node calculateNextNode(ExclusiveGateway exclusiveGateway, ProcessExecution processExecution) {
		Set<? extends Edge> outgoing = exclusiveGateway.getOutgoing();
		SequenceFlow defaultFlow = exclusiveGateway.getDefaultFlow();
		for (Edge edge : outgoing) {
			if (edge == defaultFlow) {
				continue;
			}
			if (edge instanceof SequenceFlow) {
				SequenceFlow sf = (SequenceFlow) edge;
				SearchExpression rule = sf.getRule();
				if (rule != null) {
					boolean result = calculateBoolean(rule, processExecution);
					if (result) {
						return edge.getTarget();
					}
				}
			}
		}
		if (defaultFlow != null) {
			return defaultFlow.getTarget();
		}
		return null;
	}

	/**
	 * evaluates the given rule in the context of the given {@link ProcessExecution} When the result
	 * of the evaluation is a {@link Boolean}, the boolean value of it is returned If the value is
	 * not a {@link Boolean}, false is returned
	 * 
	 * @return the booleaValue of the evaluation of the rule
	 */
	public boolean calculateBoolean(SearchExpression rule, ProcessExecution processExecution) {
		Object result = calculate(rule, processExecution);
		if (result instanceof Boolean) {
			return (Boolean) result;
		}
		return false;
	}


	/**
	 * evaluates the given rule in the context of the given context objects
	 * 
	 * @return result of evaluation
	 */
	public Object calculate(SearchExpression rule, Object... contextObjects) {
		QueryExecutor function = QueryExecutor.compile(rule);
		return function.execute((Object[]) contextObjects);
	}

	/**
	 * called if the given parallel gateway is completed
	 */
	private void handleCompletion(ProcessExecution processExecution, ParallelGateway parallelGateway) {
		// get all tokens pointing to the gateway
		Set<Token> tokens =
			GuiEngine.getInstance().getActiveTokensOf(processExecution, parallelGateway);

		Set<? extends Edge> outgoing = parallelGateway.getOutgoing();
		for (Edge edge : outgoing) {
			Node target = edge.getTarget();
			Token newToken = createTokenFor(processExecution, target, null);
			newToken.setPrevious(tokens);
			changeActiveTokens(processExecution, tokens, newToken);
		}

	}

	/**
	 * executes the token for the given {@link Edge}
	 */
	public void execute(Token token, Edge edge) {
//		if (edge instanceof SequenceFlow) {
//			SequenceFlow flow = (SequenceFlow) edge;
//			SearchExpression rule = flow.getRule();
//			if (rule != null) {
//				Object calculate = calculate(rule, token.getProcessExecution());
//				int i = 0;
//				i++;
//			}
//		}

		Node target = edge.getTarget();
		if (target instanceof Task) {
			moveActiveTokenTo(token, target);
		} else if (target instanceof ParallelGateway) {
			moveActiveTokenTo(token, target);
		} else if (target instanceof ExclusiveGateway) {
			moveActiveTokenTo(token, target);
		} else if (target instanceof EndEvent) {
			moveActiveTokenTo(token, target);
		} else if (target instanceof IntermediateThrowEvent) {
			moveActiveTokenTo(token, target);
		} else {
			String msg = "can not execute token for target " + target;
			Logger.error(msg, ExecutionEngine.class);
			throw new RuntimeException(msg);
		}
		update(token.getProcessExecution());
	}



	/**
	 * creates a new active token for target, sets the given token as previous for the new token and
	 * deactivates the given token
	 */
	private void moveActiveTokenTo(Token token, Node target) {
		ProcessExecution execution = token.getProcessExecution();
		createActiveTokenFor(token, target, execution);
	}

	private void createActiveTokenFor(Token token, Node target, ProcessExecution execution) {
		Token newToken = createTokenFor(execution, target, token);
		changeActiveTokens(execution, token, newToken);

		if (target instanceof Task) {
			// start all timer BoundaryEvents
			Task task = (Task) target;
			Set<? extends BoundaryEvent> boundaryEvents = task.getBoundaryEvents();
			for (BoundaryEvent event : boundaryEvents) {
				EventDefinition definition = event.getDefinition();
				if (definition instanceof TimerEventDefinition) {
					Token eventToken = createTokenFor(execution, event, token);
					changeActiveTokens(execution, token, eventToken);
				}
			}
		}

	}

	private void handleEndEvent(ProcessExecution processExecution) {
		// create token on end event to store the date the process execution stopped
		removeAllActiveTokens(processExecution);
		processExecution.setExecutionState(FastListElement.getElementByName("bpe.execution.state.finished"));
	}

	private void removeAllActiveTokens(ProcessExecution processExecution) {
		processExecution.setActiveTokens(new HashSet<>());
	}



	/**
	 * a {@link ParallelGateway} is completed if it has as many active tokens showing to it as it
	 * has incoming edges
	 */
	private boolean checkForCompletion(ProcessExecution processExecution, ParallelGateway parallelGateway) {

		// get all tokens showing to the gateway
		Set<? extends Token> tokensShowingTo =
			GuiEngine.getInstance().getActiveTokensOf(processExecution, parallelGateway);

		int numberOfTokensNeededToComplete = parallelGateway.getIncomming().size();
		return tokensShowingTo.size() == numberOfTokensNeededToComplete;
	}


	/**
	 * removes / adds the given token from the set of active tokens of the {@link ProcessExecution}
	 */
	private void changeActiveTokens(ProcessExecution processExecution, Token toRemove, Token toAdd) {
		HashSet<Token> activeTokens = new HashSet<>(processExecution.getActiveTokens());
		activeTokens.remove(toRemove);
		if (toRemove != null) {
			toRemove.setFinishDate(new Date());
			PersonManager r = PersonManager.getManager();
			toRemove.setFinishBy(TLContext.currentUser());
		}
		if (toAdd != null) {
			activeTokens.add(toAdd);
		}
		processExecution.setActiveTokens(activeTokens);
	}

	/**
	 * removes / adds the given token from the set of active tokens of the {@link ProcessExecution}
	 */
	private void changeActiveTokens(ProcessExecution processExecution, Set<Token> toRemove, Token toAdd) {
		HashSet<Token> activeTokens = new HashSet<>(processExecution.getActiveTokens());
		activeTokens.removeAll(toRemove);
		Date now = new Date();
		for (Token token : toRemove) {
			token.setFinishDate(now);
		}
		if (toAdd != null) {
			activeTokens.add(toAdd);
		}
		processExecution.setActiveTokens(activeTokens);
	}

	/**
	 * does the gateway have more than one outgoing edges
	 */
	private boolean isOutgoing(ParallelGateway parallelGateway) {
		return parallelGateway.getOutgoing().size() > 1;
	}

	private void handleOutgoing(Token token, ParallelGateway gateway) {
		Collection<? extends Edge> taskEdges = GuiEngine.getInstance().getTaskEdges(gateway);

		ProcessExecution execution = token.getProcessExecution();

		// create new token
		for (Edge edge : taskEdges) {
			Task task = (Task) edge.getTarget();
			Token newToken = createTokenFor(execution, task, token);
			changeActiveTokens(execution, token, newToken);
		}

	}


	/**
	 * inits the new created {@link ProcessExecution}
	 * 
	 * Creates a token for the first {@link Task}, i.e. the task after the start event. If more than
	 * one start events exist, a context for the initialization of the ProcessExecution must be
	 * provided. This is not covered here.
	 */
	public void init(ProcessExecution execution, StartEvent startEvent) {

		// set token on start event
		createActiveTokenFor(null, startEvent, execution);

		// start also all timer events of the process of the start event
		Process process = startEvent.getProcess();
		Set<StartEvent> timerStartEvents = getTimerStartEvents(process);

		for (StartEvent timerStartEvent : timerStartEvents) {
			createActiveTokenFor(null, timerStartEvent, execution);
		}

		update(execution);
	}

	/**
	 * creates a new token for the given ProcessExecution which is connected to the given node the
	 * given token is set as a previous token for the new one
	 * @return the new token
	 */
	private Token createTokenFor(ProcessExecution execution, Node node, Token previousToken) {
		Token token = TlBpeExecutionFactory.getInstance().createToken();
		token.setProcessExecution(execution);
		token.setNode(node);

		// handle previous token
		Set<Token> previousTokes = new HashSet();
		if (previousToken != null) {
			previousTokes.add(previousToken);
		}
		token.setPrevious(previousTokes);

		return token;
	}

	private Task getTask(StartEvent startEvent) {
		Set<? extends Edge> outgoing = startEvent.getOutgoing();
		for (Edge edge : outgoing) {
			Node target = edge.getTarget();
			if (target instanceof Task) {
				return (Task) target;
			}
		}
		throw new RuntimeException("No Task found for start event");
	}

	private Set<StartEvent> getStartEvents(Collaboration collaboration) {
		Set<StartEvent> events = new HashSet<>();
		Set<? extends Process> processes = collaboration.getProcesses();
		for (Process process : processes) {
			Set<StartEvent> startEvents = CollectionUtil.copyOnly(StartEvent.class, process.getNodes());
			events.addAll(startEvents);
		}
		if (events.isEmpty()) {
			throw new RuntimeException("No StartEvent found in collaboration " + collaboration.getName());
		}
		return events;
	}

	private Set<StartEvent> getStartEvents(Process process) {
		Set<StartEvent> startEvents = CollectionUtil.copyOnly(StartEvent.class, process.getNodes());
		return startEvents;
	}

	private Set<StartEvent> getTimerStartEvents(Process process) {
		Set<StartEvent> startEvents = getStartEvents(process);
		return getTimerStartEvents(startEvents);
	}


	public Set<StartEvent> getManualStartEvents(Collaboration collaboration) {
		Set<StartEvent> startEvents = getStartEvents(collaboration);
		return getManualStartEvents(startEvents);
	}

	/**
	 * @param nodes
	 *        set of {@link Node}
	 * @return all nodes from the given ones which are manual start events
	 */
	@SuppressWarnings("unchecked")
	public Set<StartEvent> getManualStartEvents(Set<? extends Node> nodes) {
		return FilterUtil.filterSet(MANUAL_START_EVENT_FILTER, nodes);
	}

	@SuppressWarnings("unchecked")
	private Set<StartEvent> getTimerStartEvents(Set<? extends Node> nodes) {
		return FilterUtil.filterSet(TIMER_START_EVENT_FILTER, nodes);
	}

}
