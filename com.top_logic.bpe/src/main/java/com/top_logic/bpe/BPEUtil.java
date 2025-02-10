/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.bpe.bpml.display.ConfigurableCondition;
import com.top_logic.bpe.bpml.display.RuleCondition;
import com.top_logic.bpe.bpml.display.RuleType;
import com.top_logic.bpe.bpml.display.SequenceFlowRule;
import com.top_logic.bpe.bpml.model.AnnotationAssociation;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.bpe.bpml.model.Edge;
import com.top_logic.bpe.bpml.model.Externalized;
import com.top_logic.bpe.bpml.model.Gateway;
import com.top_logic.bpe.bpml.model.Lane;
import com.top_logic.bpe.bpml.model.MessageFlow;
import com.top_logic.bpe.bpml.model.Node;
import com.top_logic.bpe.bpml.model.Participant;
import com.top_logic.bpe.bpml.model.Process;
import com.top_logic.bpe.bpml.model.SequenceFlow;
import com.top_logic.bpe.bpml.model.TextAnnotation;
import com.top_logic.bpe.bpml.model.impl.LaneSetBase;
import com.top_logic.bpe.execution.engine.GuiEngine;
import com.top_logic.bpe.execution.model.Token;

/**
 * Utility class for the business process engine.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class BPEUtil {

	/**
	 * Searches for the part with the given {@link Externalized#getExtId() external id} in the given
	 * collaboration.
	 * 
	 * @param extId
	 *        The external id to look for.
	 * @param collaboration
	 *        The {@link Collaboration} to get part from.
	 * @return The {@link Externalized} part in the given Collaboration with the given external id,
	 *         or <code>null</code> if there is no such part.
	 */
	public static Object findPart(String extId, Collaboration collaboration) {
		if (matches(extId, collaboration)) {
			return collaboration;
		}
		for (Participant participant : collaboration.getParticipants()) {
			if (matches(extId, participant)) {
				return participant;
			}
		}
		for (MessageFlow flowEdge : collaboration.getMessageFlows()) {
			if (matches(extId, flowEdge)) {
				return flowEdge;
			}
		}
		for (Process process : collaboration.getProcesses()) {
			if (matches(extId, process)) {
				return process;
			}
	
			Lane lane = findLane(extId, process);
			if (lane != null) {
				return lane;
			}
	
			for (Node node : process.getNodes()) {
				if (matches(extId, node)) {
					return node;
				}
			}
			for (Edge edge : process.getEdges()) {
				if (matches(extId, edge)) {
					return edge;
				}
			}
			for (TextAnnotation annotation : process.getAnnotationDefinitions()) {
				if (matches(extId, annotation)) {
					return annotation;
				}
				for (AnnotationAssociation link : annotation.getAnnotationAssociations()) {
					if (matches(extId, link)) {
						return annotation;
					}
				}
			}
		}
		for (TextAnnotation annotation : collaboration.getAnnotationDefinitions()) {
			if (matches(extId, annotation)) {
				return annotation;
			}
			for (AnnotationAssociation link : annotation.getAnnotationAssociations()) {
				if (matches(extId, link)) {
					return annotation;
				}
			}
		}
	
		return null;
	}

	private static Lane findLane(String extId, LaneSetBase laneSet) {
		for (Lane lane : laneSet.getLanes()) {
			if (matches(extId, lane)) {
				return lane;
			}
	
			Lane subLane = findLane(extId, lane);
			if (subLane != null) {
				return subLane;
			}
		}
		return null;
	}

	private static boolean matches(String selectionId, Externalized model) {
		return selectionId.equals(model.getExtId());
	}

	/**
	 * Gets all possible target nodes and their paths from a given token's current position.
	 * 
	 * @param token
	 *        The token representing current position in the workflow
	 * @return Map of possible target nodes to their respective paths
	 */
	public static Map<Node, List<Edge>> getPossibleTransitions(Token token) {
		Map<Node, List<Edge>> transitions = new HashMap<>();
		Node currentNode = token.getNode();

		// Check direct outgoing edges
		for (Edge edge : currentNode.getOutgoing()) {
			Node nextNode = edge.getTarget();

			if (!(nextNode instanceof Gateway)) {
				// Direct non-gateway target
				transitions.put(nextNode, Collections.singletonList(edge));
			} else {
				Gateway gateway = (Gateway) nextNode;
				if (GuiEngine.getInstance().needsDecision(gateway)) {
					// Check all gateway paths
					for (Edge gatewayOutgoing : gateway.getOutgoing()) {
						if (gatewayOutgoing instanceof SequenceFlow flow) {
							SequenceFlowRule rule = flow.getRule();
							if (rule != null) {
								// only valid if no error or hidden conditions are false
								boolean isValid = rule.getRuleConditions().stream()
									.map(config -> (RuleCondition) new ConfigurableCondition(null,
										(ConfigurableCondition.Config<?>) config))
									.filter(condition -> condition.getRuleType() == RuleType.HIDDEN ||
										condition.getRuleType() == RuleType.DEFAULT)
									.allMatch(condition -> condition.getTestCondition(token.getProcessExecution()));

								if (isValid) {
									transitions.put(flow.getTarget(), Arrays.asList(edge, gatewayOutgoing));
								}
							} else {
								// No rules - path is valid
								transitions.put(flow.getTarget(), Arrays.asList(edge, gatewayOutgoing));
							}
						}
					}
				} else {
					// Automatic gateway - add all targets
					for (Edge gatewayOutgoing : gateway.getOutgoing()) {
						transitions.put(gatewayOutgoing.getTarget(), Arrays.asList(edge, gatewayOutgoing));
					}
				}
			}
		}
		return transitions;
	}

}

