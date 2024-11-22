/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.execution.engine;

import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.top_logic.basic.Logger;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.bpe.bpml.display.RuleCondition;
import com.top_logic.bpe.bpml.display.RuleType;
import com.top_logic.bpe.bpml.display.SequenceFlowRule;
import com.top_logic.bpe.bpml.display.StandardRule;
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
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.parser.ParseException;
import com.top_logic.model.search.expr.parser.SearchExpressionParser;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.TLContext;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class ExecutionEngine {

	private static ExecutionEngine INSTANCE = new ExecutionEngine();
	private QueryExecutor _activeExecutions;

	/**
	 * The singleton {@link ExecutionEngine} instance.
	 */
	public static ExecutionEngine getInstance() {
		return INSTANCE;
	}

	private ExecutionEngine() {
		try {
			Expr expr = new SearchExpressionParser(
				new StringReader(
					"""
					all(`tl.bpe.execution:ProcessExecution`)
						.filter(e -> $e.get(`tl.bpe.execution:ProcessExecution#executionState`) == `tl.bpe.execution:ExecutionState#RUNNING`)
					""")).expr();
			_activeExecutions = QueryExecutor.compile(expr);
		} catch (ParseException ex) {
			throw new UnreachableAssertion(ex);
		}
	}

	/**
	 * Updates all existing {@link ProcessExecution} do this until no new executions appear during
	 * the update
	 */
	public void updateAll() {
		for (ProcessExecution execution : getAllExecutions()) {
			update(execution);
		}
	}

	/**
	 * All active {@link ProcessExecution}s.
	 */
	public Collection<ProcessExecution> getAllExecutions() {
		@SuppressWarnings("unchecked")
		Collection<ProcessExecution> allExecutions = (Collection<ProcessExecution>) _activeExecutions.execute();
		return allExecutions;
	}

	/**
	 * Performs all automatic actions that are due to execution.
	 */
	public void update(ProcessExecution execution) {
		int step = 0;

		boolean changed;
		do {
			step++;
			if (step > 1000) {
				throw new RuntimeException(
					"More than 1000 steps performed. Cannot complete update for " + execution + ".");
			}

			Set<? extends Token> userRelevant = execution.getUserRelevantTokens();
			List<? extends Token> automaticTokens =
				execution.getActiveTokens().stream().filter(t -> !userRelevant.contains(t)).toList();

			changed = false;
			for (Token token : automaticTokens) {
				boolean anyChange = doExecute(execution, token);
				if (anyChange) {
					Logger.info("Completed " + token.getNode() + " for " + execution + ".", ExecutionEngine.class);

					// Break after first change because some tokens may become inactive during
					// execution.
					changed = true;
					break;
				}
			}
		} while (changed);
	}

	/**
	 * Executes the given {@link Token} of the given {@link ProcessExecution}.
	 * 
	 * <p>
	 * Checks whether anything can be performed for the token and performs it.
	 * </p>
	 * 
	 * @return Whether the given {@link Token} was consumed.
	 */
	private boolean doExecute(ProcessExecution processExecution, Token token) {
		Node node = token.getNode();
		if (node instanceof ParallelGateway) {
			ParallelGateway parallelGateway = (ParallelGateway) node;
			if (checkForCompletion(processExecution, parallelGateway)) {
				handleCompletion(processExecution, parallelGateway);
				return true;
			}
		}
		else if (node instanceof ExclusiveGateway exclusiveGateway) {

			Node nextNode = calculateNextNode(exclusiveGateway, processExecution);
			if (nextNode != null) {
				createActiveToken(token.getProcessExecution(), nextNode, token);
				return true;
			}
		}
		else if (node instanceof StartEvent) {
			StartEvent startEvent = (StartEvent) node;
			EventDefinition definition = startEvent.getDefinition();
			if (definition instanceof TimerEventDefinition) {
				return handleTimerEvent(processExecution, startEvent, (TimerEventDefinition) definition, token);
			} else {
				handleStartEvent(processExecution, (StartEvent) node, token);
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
			if (isActive(processExecution, task)) {
				EventDefinition definition = event.getDefinition();
				if (definition instanceof TimerEventDefinition) {
					return handleBoundaryTimerEvent(processExecution, event, (TimerEventDefinition) definition, token);
				}
			}
			else {
				// the task of the BoundaryEvent is not active any more
				// cancel the Boundary event
				advance(processExecution, token, null);
			}

		}
		else if (node instanceof SendTask) {
			executeSendTask(processExecution, (SendTask) node, token);
			return true;
		}
		else if (node instanceof ServiceTask) {
			executeServiceTask(processExecution, (ServiceTask) node, token);
			return true;
		}
		else if (node instanceof IntermediateThrowEvent) {
			IntermediateThrowEvent event = (IntermediateThrowEvent) node;
			EventDefinition definition = event.getDefinition();
			if (definition instanceof MessageEventDefinition) {
				handleMessageEvent(processExecution, (IntermediateThrowEvent) node, (MessageEventDefinition) definition,
					token);
			}

			return true;
		}

		else if (node != null) {
			int i = 0;
			i++;
		}

		return false;
	}

	private void handleMessageEvent(ProcessExecution processExecution, IntermediateThrowEvent event, MessageEventDefinition definition,
			Token previousToken) {

		// find the Flow
		MessageFlow flow = getFlowForSource(processExecution, event);
		FlowTarget flowTarget = flow.getTarget();

		// create new ProcessExecution
		if (flowTarget instanceof StartEvent) {
			StartEvent startEvent = (StartEvent) flowTarget;
			ProcessExecution execution = ExecutionProcessCreateHandler.createProcessModel(startEvent);
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
		createNextTokens(processExecution, event, previousToken);
	}

	private MessageFlow getFlowForSource(ProcessExecution processExecution, FlowSource source) {
		for (MessageFlow flow : processExecution.getCollaboration().getMessageFlows()) {
			if (flow.getSource() == source) {
				return flow;
			}
		}
		return null;
	}

	/**
	 * Whether there is an active {@link Token} pointing to the given {@link Task} in the given
	 * {@link ProcessExecution}.
	 */
	private boolean isActive(ProcessExecution processExecution, Task task) {
		for (Token token : processExecution.getActiveTokens()) {
			if (token.getNode() == task) {
				return true;
			}
		}
		return false;
	}

	private Token getActiveTokenForTask(ProcessExecution processExecution, Task task) {
		for (Token token : processExecution.getAllTokens()) {
			if (token.getNode() == task) {
				if (token.getActive()) {
					return token;
				}
			}
		}
		return null;
	}

	private boolean handleBoundaryTimerEvent(ProcessExecution processExecution, BoundaryEvent event,
			TimerEventDefinition definition, Token previousToken) {
		boolean timerReached =
			handleTimerEvent(processExecution, event, definition, previousToken);
		if (timerReached) {
			if (event.getCancelActivity()) {
				Task task = event.getAttachedTo();
				Token taskToken = getActiveTokenForTask(processExecution, task);
				advance(processExecution, taskToken, null);
			}
		}
		return timerReached;
	}

	private void executeSendTask(ProcessExecution processExecution, SendTask sendTask, Token previousToken) {
		BPMailer mailer = new BPMailer(previousToken, processExecution, sendTask);
		mailer.sendMail();

		createNextTokens(processExecution, sendTask, previousToken);
	}

	private void executeServiceTask(ProcessExecution processExecution, ServiceTask serviceTask, Token previousToken) {
		SearchExpression action = serviceTask.getAction();

		if (action != null) {
			calculate(action, processExecution);
		}

		createNextTokens(processExecution, serviceTask, previousToken);
	}

	private boolean handleTimerEvent(ProcessExecution processExecution, Event event,
			TimerEventDefinition definition, Token previousToken) {
		// check if the timeout is reached
		Date created = previousToken.tCreationDate();
		if (created != null) {
			// token must be existing, i.e. do not do this during creation of token

			long delayInMillis = definition.getDelayInMillis();

			Date now = new Date();
			if (now.getTime() - created.getTime() > delayInMillis) {
				// timeout reached
				createNextTokens(processExecution, event, previousToken);
				return true;
			}
		}
		return false;
	}

	private void handleStartEvent(ProcessExecution processExecution, StartEvent startEvent, Token previousToken) {
		createNextTokens(processExecution, startEvent, previousToken);
	}

	private void createNextTokens(ProcessExecution processExecution, Node current, Token previousToken) {
		for (Edge edge : current.getOutgoing()) {
			Node next = edge.getTarget();
			Token newToken = createFollowupToken(processExecution, next, previousToken);

			activate(processExecution, newToken);
			activateBoundaryEvents(processExecution, next, previousToken);
		}

		if (previousToken != null) {
			complete(processExecution, previousToken);
		}
	}

	/**
	 * activates all {@link BoundaryEvent} attached to target token is the parent token of the
	 * tokens which are placed on the {@link BoundaryEvent}s
	 */
	private void activateBoundaryEvents(ProcessExecution processExecution, Node target, Token previousToken) {
		for (Node boundaryEvent : boundaryEvents(target)) {
			Token newToken = createFollowupToken(processExecution, boundaryEvent, previousToken);

			activate(processExecution, newToken);
		}
	}

	/**
	 * a set with all nodes for which also a token must be created if a token on the given
	 *         node is set
	 */
	private Set<? extends BoundaryEvent> boundaryEvents(Node node) {
		if (node instanceof Task task) {
			return task.getBoundaryEvents();
		} else {
			return Collections.emptySet();
		}
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
				SequenceFlowRule rule = sf.getRule();
				Boolean allConditionsTrue = rule.getRuleConditions().stream()
					.map(config -> (RuleCondition) new StandardRule(null, (StandardRule.Config<?>) config)) // Cast
																											// to
																											// RuleCondition
					.filter(condition -> condition.getRuleType() != RuleType.WARNING) // Ignore
																						// warnings
					.allMatch(condition -> condition.getCondition(processExecution)); // Check all
																						// non-warning
																						// conditions
				// If all conditions are true, return the target
				if (allConditionsTrue) {
					return edge.getTarget();
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
		return QueryExecutor.compile(rule).execute(contextObjects);
	}

	/**
	 * called if the given parallel gateway is completed
	 */
	private void handleCompletion(ProcessExecution processExecution, ParallelGateway parallelGateway) {
		Set<Token> current = activeTokensIn(processExecution, parallelGateway);

		Set<? extends Edge> outgoing = parallelGateway.getOutgoing();
		for (Edge edge : outgoing) {
			Node target = edge.getTarget();
			Token next = createToken(processExecution, target);
			next.setPrevious(current);

			advanceAll(processExecution, current, next);
		}
	}

	/**
	 * Walks the given path to the new task pointed to.
	 */
	public void execute(Token previousToken, List<Edge> path, Object additional) {
		ProcessExecution processExecution = previousToken.getProcessExecution();

		Edge lastEdge = path.get(path.size() - 1);
		Node target = lastEdge.getTarget();

		if (target instanceof Task
			|| target instanceof ParallelGateway
			|| target instanceof ExclusiveGateway
			|| target instanceof EndEvent
			|| target instanceof IntermediateThrowEvent
		) {
			Token nextToken = createActiveToken(processExecution, target, previousToken);

			for (Edge edge : path) {
				if (edge instanceof SequenceFlow flow) {
					SearchExpression operation = flow.getOperation();
					if (operation != null) {
						QueryExecutor.compile(operation).execute(processExecution, previousToken, nextToken,
							additional);
					}
				}
			}

			update(processExecution);
		} else {
			throw new RuntimeException(
				"Cannot execute edge with target " + target + " on " + processExecution);
		}
	}



	/**
	 * Creates a new active token for the given node as follow-up for the given previous token and
	 * deactivates the previous token.
	 * 
	 * <p>
	 * Additionally, tokesn for potential boundary events are also created.
	 * </p>
	 * 
	 * @return The newly created token for the given node.
	 */
	private Token createActiveToken(ProcessExecution execution, Node node, Token previousToken) {
		Token nextToken = createFollowupToken(execution, node, previousToken);
		advance(execution, previousToken, nextToken);

		// Start all boundary timer events.
		for (BoundaryEvent event : boundaryEvents(node)) {
			EventDefinition definition = event.getDefinition();
			if (definition instanceof TimerEventDefinition) {
				Token eventToken = createFollowupToken(execution, event, previousToken);
				activate(execution, eventToken);
			}
		}

		return nextToken;
	}

	private void handleEndEvent(ProcessExecution processExecution) {
		// create token on end event to store the date the process execution stopped
		removeAllActiveTokens(processExecution);
		processExecution.setExecutionState(
			(TLClassifier) TLModelUtil.resolveQualifiedName("tl.bpe.execution:ExecutionState#FINISHED"));
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
		Set<? extends Token> tokensPointingTo = activeTokensIn(processExecution, parallelGateway);

		int numberOfTokensNeededToComplete = parallelGateway.getIncomming().size();
		return tokensPointingTo.size() == numberOfTokensNeededToComplete;
	}

	/**
	 * removes / adds the given token from the set of active tokens of the {@link ProcessExecution}
	 */
	private void advanceAll(ProcessExecution processExecution, Set<Token> from, Token to) {
		for (Token token : from) {
			complete(processExecution, token);
		}
		if (to != null) {
			activate(processExecution, to);
		}
	}

	/**
	 * removes / adds the given token from the set of active tokens of the {@link ProcessExecution}
	 */
	private void advance(ProcessExecution processExecution, Token from, Token to) {
		if (from != null) {
			complete(processExecution, from);
		}
		if (to != null) {
			activate(processExecution, to);
		}
	}

	private void complete(ProcessExecution processExecution, Token token) {
		token.setFinishDate(new Date());
		token.setFinishBy(TLContext.currentUser());

		processExecution.removeActiveToken(token);
	}

	private void activate(ProcessExecution processExecution, Token token) {
		processExecution.addActiveToken(token);
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
		createActiveToken(execution, startEvent, null);

		// start also all timer events of the process of the start event
		Process process = startEvent.getProcess();
		for (StartEvent timerStartEvent : getTimerStartEvents(process)) {
			createActiveToken(execution, timerStartEvent, null);
		}

		update(execution);
	}

	/**
	 * creates a new token for the given ProcessExecution which is connected to the given node the
	 * given token is set as a previous token for the new one
	 * @return the new token
	 */
	private Token createFollowupToken(ProcessExecution execution, Node node, Token previousToken) {
		Token token = createToken(execution, node);
		token.setPrevious(asSet(previousToken));
		return token;
	}

	private Token createToken(ProcessExecution execution, Node node) {
		Token token = TlBpeExecutionFactory.getInstance().createToken();
		token.setNode(node);
		execution.addAllToken(token);

		return token;
	}

	private static <T> Set<T> asSet(T singleton) {
		return singleton == null ? Collections.emptySet() : Collections.singleton(singleton);
	}

	private List<StartEvent> getTimerStartEvents(Process process) {
		return getStartEvents(process)
			.filter(start -> start.getDefinition() instanceof TimerEventDefinition)
			.toList();
	}

	/**
	 * All start events that can be triggered manually in the given {@link Collaboration}.
	 */
	public List<StartEvent> getManualStartEvents(Collaboration collaboration) {
		return filterManual(
			collaboration.getProcesses().stream()
			.flatMap(ExecutionEngine::getStartEvents)
		).toList();
	}

	/**
	 * All active {@link Token}s of the given {@link ProcessExecution} in the given state..
	 */
	private static Set<Token> activeTokensIn(ProcessExecution processExecution, Node state) {
		return processExecution.getActiveTokens().stream().filter(t -> t.getNode() == state)
			.collect(Collectors.toSet());
	}

	/**
	 * The one and only outgoing {@link Edge} from the given {@link Node}, or <code>null</code> if
	 * there is none.
	 *
	 * @throws IllegalStateException
	 *         If there are multiple outgoing edges.
	 */
	public static Edge getSingleOutgoingEdge(Node node) {
		Set<? extends Edge> edges = node.getOutgoing();
		switch (edges.size()) {
			case 0:
				return null;
			case 1:
				return edges.iterator().next();
			default:
				throw new IllegalStateException(
					"Node " + node + " must only have a single outgoing edge, found: " + edges);
		}
	}

	/**
	 * The given stream filtered for {@link StartEvent}s that can be triggered manually.
	 */
	public static Stream<StartEvent> filterManual(Stream<StartEvent> startEvents) {
		return startEvents.filter(start -> start.getDefinition() == null);
	}

	private static Stream<StartEvent> getStartEvents(Process process) {
		return filterStartEvents(process.getNodes().stream());
	}

	/**
	 * The given stream filtered for {@link StartEvent}s.
	 */
	public static Stream<StartEvent> filterStartEvents(Stream<? extends Node> nodes) {
		return nodes
			.filter(n -> n instanceof StartEvent)
			.map(n -> ((StartEvent) n));
	}

}
