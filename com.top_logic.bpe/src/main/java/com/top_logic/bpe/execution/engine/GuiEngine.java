/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.execution.engine;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.bpe.bpml.model.Edge;
import com.top_logic.bpe.bpml.model.EndEvent;
import com.top_logic.bpe.bpml.model.ExclusiveGateway;
import com.top_logic.bpe.bpml.model.Gateway;
import com.top_logic.bpe.bpml.model.Lane;
import com.top_logic.bpe.bpml.model.LaneSet;
import com.top_logic.bpe.bpml.model.Node;
import com.top_logic.bpe.bpml.model.ParallelGateway;
import com.top_logic.bpe.bpml.model.Process;
import com.top_logic.bpe.bpml.model.SequenceFlow;
import com.top_logic.bpe.bpml.model.StartEvent;
import com.top_logic.bpe.bpml.model.Task;
import com.top_logic.bpe.bpml.model.impl.LaneSetBase;
import com.top_logic.bpe.execution.model.ProcessExecution;
import com.top_logic.bpe.execution.model.Token;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.tool.boundsec.wrap.Group;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class GuiEngine {


	private static GuiEngine INSTANCE = new GuiEngine();

	public static GuiEngine getInstance() {
		return INSTANCE;
	}

	private GuiEngine() {

	}

	/**
	 * all active {@link Token} of all {@link ProcessExecution}
	 */
	public Set<Token> getAllActiveTokens() {
		Collection<ProcessExecution> allExecutions = ExecutionEngine.getInstance().getAllExecutions();
		Set<Token> res = new HashSet<>();
		for (ProcessExecution execution : allExecutions) {
			res.addAll(execution.getActiveTokens());
		}
		return res;
	}

	/**
	 * all active {@link Token} for which the given {@link PersonContact} is actor
	 */
	public Set<Token> getTokensFor(Person person) {
		Set<Token> res = new HashSet<>();
		for (Token token : getAllActiveTokens()) {
			if (isActor(person, token)) {
				res.add(token);
			}
		}
		return res;
	}

	/**
	 * Assumption: the token points to a {@link Task}
	 * 
	 * Check, if the (only) {@link Gateway}, to which the task is connected, is a manual gateway
	 */
	public boolean isManualStep(Token token) {
		Node node = token.getNode();
		Set<? extends Edge> outgoing = node.getOutgoing();
		Edge next = outgoing.iterator().next();
		Node target = next.getTarget();
		if (target instanceof Gateway) {
			return isManual((Gateway) target);
		}
		return true;
	}

	public Node getNextNode(Token token) {
		return getNextNode(token.getNode());
	}

	public Node getNextNode(Node node) {
		return CollectionUtil.getSingleValueFromCollection(node.getOutgoing()).getTarget();
	}

	public Edge getSingleOutgoingEdge(Token token) {
		Node node = token.getNode();
		Set<? extends Edge> outgoing = node.getOutgoing();
		if (outgoing.size() != 1) {
			throw new RuntimeException(
				"Node " + node + " has not exaclty one outgoing, but " + outgoing.size() + " outgoings.");
		}
		return outgoing.iterator().next();
	}

	/**
	 * Checks whether navigating the given {@link Edge} is possible.
	 * 
	 * @return Either <code>null</code> meaning, navigation is possible, or a {@link ResKey}
	 *         representing the error why the edge can not be navigated.
	 */
	public ResKey checkError(Edge edge, Token token) {
		if (edge instanceof SequenceFlow) {
			SequenceFlow flow = (SequenceFlow) edge;
			SearchExpression rule = flow.getRule();
			if (rule != null) {
				Object error = ExecutionEngine.getInstance().calculate(rule, token.getProcessExecution());
				if (error instanceof Boolean) {
					if (((Boolean) error).booleanValue()) {
						return null;
					} else {
						return I18NConstants.SELECTION_NOT_ALLOWED;
					}
				}
				if (error instanceof ResKey) {
					return (ResKey) error;
				}
				if (error instanceof String && !((String) error).isEmpty()) {
					return ResKey.text((String) error);
				}
			}
		}
		return null;
	}

	public boolean isManual(Node node) {
		if (node instanceof ParallelGateway) {
			return false;
		}
		if (node instanceof ExclusiveGateway) {
			return !((ExclusiveGateway) node).getAutomatic();
		}
		if (node instanceof EndEvent) {
			return false;
		}
		return true;
	}


	/**
	 * Set of all outgoing {@link Edge}s whose target is a {@link Task}.
	 */
	public Set<? extends Edge> getTaskEdges(Gateway gateway) {
		Set<Edge> res = new HashSet<>();
		Set<? extends Edge> outgoing = gateway.getOutgoing();
		for (Edge edge : outgoing) {
			Node target = edge.getTarget();
			if (target instanceof Task) {
				res.add(edge);
			}
		}
		return res;
	}

	/**
	 * A set with all {@link Token}s of the given {@link ProcessExecution} pointing to the given
	 * {@link Node}
	 */
	public Set<Token> getAllActiveTokensShowingTo(ProcessExecution processExecution, Node node) {
		Set<? extends Token> allTokens = processExecution.getActiveTokens();
		Set<Token> res = new HashSet<>();
		for (Token token : allTokens) {
			if (token.getNode() == node) {
				res.add(token);
			}
		}
		return res;
	}

	/**
	 * a set with the {@link StartEvent}s which may be executed by the given person,
	 *         depending on the state of the given collaboration
	 */
	public Set<StartEvent> getStartEventsFor(Collaboration collaboration, Person person) {
		Set<StartEvent> res = new HashSet<>();
		TLClassifier state = collaboration.getState();
		if (isActive(state)) {
			for (Process process : collaboration.getProcesses()) {
				addProcessStartEvents(person, res, process);
				addLaneStartEvents(person, res, process);
			}
		}
		return res;

	}

	private boolean isActive(TLClassifier state) {
		return "Released".equals(state.getName());
	}

	private void addProcessStartEvents(Person person, Collection<StartEvent> res, Process process) {
		if (isActor(person, process)) {
			addStartEvents(res, process.getNodes());
		}
	}

	private void addLaneStartEvents(Person person, Collection<StartEvent> res, LaneSet lanes) {
		for (Lane lane : lanes.getLanes()) {
			addDirectLaneStartEvents(person, res, lane);
			addLaneStartEvents(person, res, lane);
		}
	}

	private void addDirectLaneStartEvents(Person person, Collection<StartEvent> res, Lane lane) {
		if (isActor(person, lane)) {
			addStartEvents(res, lane.getNodes());
		}
	}

	private boolean isActor(Person person, LaneSet lane) {
		return isActor(person, lane, null);
	}

	private void addStartEvents(Collection<StartEvent> res, Set<? extends Node> nodes) {
		res.addAll(ExecutionEngine.getInstance().getManualStartEvents(nodes));
	}

	/**
	 * Whether the given {@link Person} an actor for the node the given {@link Token} belongs to.
	 */
	public boolean isActor(Person person, Token token) {
		if (person == null) {
			return false;
		}

		Node node = token.getNode();
		Lane lane = node.getLane();
		if (lane == null) {
			return false;
		}
		return isActor(person, lane, token.getProcessExecution());
	}

	/**
	 * Whether the given {@link Person} is actor in the given {@link LaneSetBase} for the given
	 * {@link ProcessExecution}.
	 * 
	 * @param processExecution
	 *        May be <code>null</code>, e.g. when the actor is checked for the {@link StartEvent}.
	 *        Then no {@link ProcessExecution} is available.
	 */
	public boolean isActor(Person person, LaneSet lane, ProcessExecution processExecution) {
		if (isStaticActor(person, lane)) {
			return true;
		}

		SearchExpression personRule = lane.getActorRule();
		if (personRule == null) {
			return false;
		}

		Object calculate = ExecutionEngine.getInstance().calculate(personRule, processExecution);
		if (calculate instanceof Collection) {
			for (Object entry : (Collection<?>) calculate) {
				if (isMember(entry, person)) {
					return true;
				}
			}
		}
		return isMember(calculate, person);
	}

	private boolean isMember(Object result, Person person) {
		Person resultPerson = null;
		if (result instanceof PersonContact) {
			resultPerson = ((PersonContact) result).getPerson();
		}
		if (result instanceof Person) {
			resultPerson = (Person) result;
		}
		if (resultPerson != null) {
			Person currentResultPerson = WrapperHistoryUtils.getCurrent(resultPerson);
			return currentResultPerson == person;
		}
		if (result instanceof Group) {
			return ((Group) result).containsPerson(person);
		}
		return false;
	}

	/**
	 * Whether the given {@link Person} is member of any {@link Lane#getActorGroups()}.
	 */
	private boolean isStaticActor(Person person, LaneSet lane) {
		if (lane == null) {
			return false;
		}

		Set<? extends Group> actorGroups = lane.getActorGroups();
		if (actorGroups == null) {
			return false;
		}

		for (Group group : actorGroups) {
			if (group.containsPerson(person)) {
				return true;
			}
		}
		return false;
	}

}
