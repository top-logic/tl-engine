/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe;

import com.top_logic.bpe.bpml.model.AnnotationAssociation;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.bpe.bpml.model.Edge;
import com.top_logic.bpe.bpml.model.Externalized;
import com.top_logic.bpe.bpml.model.Lane;
import com.top_logic.bpe.bpml.model.MessageFlow;
import com.top_logic.bpe.bpml.model.Node;
import com.top_logic.bpe.bpml.model.Participant;
import com.top_logic.bpe.bpml.model.Process;
import com.top_logic.bpe.bpml.model.TextAnnotation;
import com.top_logic.bpe.bpml.model.impl.LaneSetBase;

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

}

