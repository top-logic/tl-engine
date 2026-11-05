/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.execution.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.top_logic.basic.util.ResKey;
import com.top_logic.bpe.bpml.display.RuleType;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.bpe.bpml.model.Edge;
import com.top_logic.bpe.bpml.model.EndEvent;
import com.top_logic.bpe.bpml.model.ExclusiveGateway;
import com.top_logic.bpe.bpml.model.Gateway;
import com.top_logic.bpe.bpml.model.Lane;
import com.top_logic.bpe.bpml.model.LaneSet;
import com.top_logic.bpe.bpml.model.Node;
import com.top_logic.bpe.bpml.model.ParallelGateway;
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
import com.top_logic.util.TLContext;

/**
 * Service methods for displaying the state of process executions in the user interface.
 *
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class GuiEngine {

	private static GuiEngine INSTANCE = new GuiEngine();

	/**
	 * The singleton instance.
	 */
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
	 * The node following the node of the given {@link Token}.
	 *
	 * @see #getNextNode(Node)
	 */
	public static Node getNextNode(Token token) {
		return getNextNode(token.getNode());
	}

	/**
	 * The target of the single outgoing {@link Edge} of the given node.
	 */
	public static Node getNextNode(Node node) {
		return ExecutionEngine.getSingleOutgoingEdge(node).getTarget();
	}

	/**
	 * The single outgoing {@link Edge} of the node of the given {@link Token}.
	 */
	public static Edge getSingleOutgoingEdge(Token token) {
		return ExecutionEngine.getSingleOutgoingEdge(token.getNode());
	}

	/**
	 * Checks whether navigating the given {@link Edge} is possible.
	 * 
	 * @return The messages of the {@link RuleType#DEFAULT} conditions of the edge that fail, an
	 *         empty list meaning that navigation is possible.
	 */
	public List<ResKey> checkErrors(Token token, Edge edge) {
		return FlowConditions.failures(edge, token.getProcessExecution(), RuleType.DEFAULT);
	}

	/**
	 * Checks whether the given {@link Edge} has active warnings.
	 * 
	 * @return The messages of the {@link RuleType#WARNING} conditions of the edge that fail, an
	 *         empty list meaning that no warnings are present.
	 */
	public List<ResKey> checkWarnings(Token token, Edge edge) {
		return FlowConditions.failures(edge, token.getProcessExecution(), RuleType.WARNING);
	}

	/**
	 * Whether the given {@link Edge} is offered to the user finishing the given {@link Token}: all
	 * its {@link RuleType#HIDDEN} conditions hold.
	 */
	public boolean isVisible(Token token, Edge edge) {
		return FlowConditions.holds(edge, token.getProcessExecution(), RuleType.HIDDEN);
	}

	/**
	 * The choices a user has when finishing the given {@link Token}.
	 *
	 * <p>
	 * Each outgoing edge of the token's node leading to a {@link Task} or an {@link EndEvent} is a
	 * choice of its own. An edge leading to a {@link Gateway} that {@link #needsDecision(Node)
	 * needs a decision} offers a choice for each {@link #isVisible(Token, Edge) visible} outgoing
	 * edge of the gateway; an edge leading to an automatic gateway is a single choice. Choices
	 * whose path is blocked by an error are included, see {@link Decision#getErrors()}.
	 * </p>
	 *
	 * @return The choices, ordered by the {@link Edge#getWeight() weight} of their
	 *         {@link Decision#getLastEdge() last edge} first, then by their
	 *         {@link Decision#getDisplayLabel() label} in the current user's language.
	 */
	public List<Decision> getDecisions(Token token) {
		List<Decision> result = new ArrayList<>();
		for (Edge edge : token.getNode().getOutgoing()) {
			Node target = edge.getTarget();
			if (target instanceof Gateway gateway) {
				if (needsDecision(gateway)) {
					for (Edge gatewayOutgoing : gateway.getOutgoing()) {
						if (isVisible(token, gatewayOutgoing)) {
							result.add(new Decision(token, List.of(edge, gatewayOutgoing)));
						}
					}
				} else {
					result.add(new Decision(token, List.of(edge)));
				}
			} else if (target instanceof Task || target instanceof EndEvent) {
				result.add(new Decision(token, List.of(edge)));
			}
		}
		result.sort(Decision.order(TLContext.getLocale()));
		return result;
	}

	/**
	 * Whether the given target node of an {@link Edge} represents a decision that must be made by a
	 * user finishing a manual task.
	 */
	public boolean needsDecision(Node node) {
		if (node instanceof ParallelGateway) {
			return false;
		}
		if (node instanceof ExclusiveGateway exclusive) {
			return !exclusive.getAutomatic();
		}
		return false;
	}

	/**
	 * a set with the {@link StartEvent}s which may be executed by the given person,
	 *         depending on the state of the given collaboration
	 */
	public Collection<StartEvent> getStartEventsFor(Collaboration collaboration, Person person) {
		if (!isActive(collaboration.getState())) {
			return Collections.emptySet();
		}

		return collaboration.getProcesses().stream().flatMap(process -> 
			Stream.concat(
				isActor(person, process) ? startEvents(process.getNodes()) : Stream.empty(),
				laneSetStartEvents(process, person))).toList();
	}

	private boolean isActive(TLClassifier state) {
		return "Released".equals(state.getName());
	}

	private Stream<StartEvent> laneSetStartEvents(LaneSet laneSet, Person person) {
		return laneSet.getLanes()
			.stream()
			.flatMap(lane -> Stream.concat(
				isActor(person, lane) ? startEvents(lane.getNodes()) : Stream.empty(),
				laneSetStartEvents(lane, person)));
	}

	private boolean isActor(Person person, LaneSet lane) {
		return isActor(person, lane, null);
	}

	private Stream<StartEvent> startEvents(Set<? extends Node> nodes) {
		return ExecutionEngine.filterManual(ExecutionEngine.filterStartEvents(nodes.stream()));
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

	private boolean isMember(Object actor, Person accountToCheck) {
		Person resultPerson = null;
		if (actor instanceof PersonContact) {
			resultPerson = ((PersonContact) actor).getPerson();
		}
		else if (actor instanceof Person) {
			resultPerson = (Person) actor;
		}

		if (resultPerson != null) {
			Person currentResultPerson = WrapperHistoryUtils.getCurrent(resultPerson);
			return currentResultPerson == accountToCheck
				|| (currentResultPerson != null && currentResultPerson.getRepresentativeGroup().containsPerson(accountToCheck));
		}
		if (actor instanceof Group) {
			return ((Group) actor).containsPerson(accountToCheck);
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
