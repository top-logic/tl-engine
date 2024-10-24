/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.execution.engine;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import com.top_logic.basic.util.ResKey;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.bpe.bpml.model.Edge;
import com.top_logic.bpe.bpml.model.ExclusiveGateway;
import com.top_logic.bpe.bpml.model.Lane;
import com.top_logic.bpe.bpml.model.LaneSet;
import com.top_logic.bpe.bpml.model.Node;
import com.top_logic.bpe.bpml.model.ParallelGateway;
import com.top_logic.bpe.bpml.model.SequenceFlow;
import com.top_logic.bpe.bpml.model.StartEvent;
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

	public static Node getNextNode(Token token) {
		return getNextNode(token.getNode());
	}

	public static Node getNextNode(Node node) {
		return ExecutionEngine.getSingleOutgoingEdge(node).getTarget();
	}

	public static Edge getSingleOutgoingEdge(Token token) {
		return ExecutionEngine.getSingleOutgoingEdge(token.getNode());
	}

	/**
	 * Checks whether navigating the given {@link Edge} is possible.
	 * 
	 * @return Either <code>null</code> meaning, navigation is possible, or a {@link ResKey}
	 *         representing the error why the edge can not be navigated.
	 */
	public ResKey checkError(Token token, Edge edge) {
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
