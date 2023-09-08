/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.layout.scripting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.bpe.bpml.model.Edge;
import com.top_logic.bpe.bpml.model.Lane;
import com.top_logic.bpe.bpml.model.LaneSet;
import com.top_logic.bpe.bpml.model.MessageFlow;
import com.top_logic.bpe.bpml.model.Named;
import com.top_logic.bpe.bpml.model.Node;
import com.top_logic.bpe.bpml.model.Participant;
import com.top_logic.bpe.bpml.model.SequenceFlow;
import com.top_logic.bpe.bpml.model.TlBpeBpmlFactory;
import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ui.BreadcrumbStrings;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class BPMLObjectNaming extends AbstractModelNamingScheme<Named, BPMLObjectNaming.BPMLObjectName> {

	/**
	 * {@link ModelName} of the {@link BPMLObjectNaming}.
	 */
	public interface BPMLObjectName extends ModelName {

		/**
		 * The path to a tab as "Breadcrumb String".
		 * 
		 * @see BreadcrumbStrings
		 */
		@Format(BreadcrumbStrings.class)
		List<String> getNodePath();

		/** @see #getNodePath() */
		void setNodePath(List<String> value);

	}

	@Override
	protected void initName(BPMLObjectName name, Named model) {
		List<String> path = new ArrayList<>();
		buildPath(path, model);
		name.setNodePath(path);
	}

	public static void buildPath(List<String> path, Named model) {
		String name = model.getName();
		if (StringServices.isEmpty(name)) {
			String msg = "Node " + model + " has no name";
			throw new RuntimeException(msg);
		}
		path.add(0, normalize(model.getName()));
		Named parent = null;
		if (model instanceof Collaboration) {
			// done
		} else if (model instanceof Participant) {
			Participant participant = (Participant) model;
			parent = participant.getCollaboration();
		} else if (model instanceof Node) {
			Node task = (Node) model;
			Lane lane = task.getLane();
			if (lane != null && !StringServices.isEmpty(lane.getName())) {
				// Record lane, if it has name.
				parent = lane;
			} else {
				com.top_logic.bpe.bpml.model.Process process = task.getProcess();
				parent = process.getParticipant();
			}
		} else if (model instanceof Lane) {
			Lane lane = (Lane) model;
			LaneSet owner = lane.getOwner();
			if (owner instanceof com.top_logic.bpe.bpml.model.Process) {
				com.top_logic.bpe.bpml.model.Process process = (com.top_logic.bpe.bpml.model.Process) owner;
				parent = process.getParticipant();
			} else if (owner instanceof Lane) {
				parent = (Lane) owner;
			} else {
				String msg = "Owner " + owner + " for lane " + lane + " can not be processed";
				throw new RuntimeException(msg);
			}
		} else if (model instanceof SequenceFlow) {
			SequenceFlow sequenceFlow = (SequenceFlow) model;
			com.top_logic.bpe.bpml.model.Process process = sequenceFlow.getProcess();
			parent = process.getParticipant();
		} else if (model instanceof MessageFlow) {
			MessageFlow messageFlow = (MessageFlow) model;
			parent = messageFlow.getCollaboration();
		} else {
			String msg = "Model " + model + " can not be processed";
			throw new RuntimeException(msg);
		}
		if (parent != null) {
			buildPath(path, parent);
		}

	}

	@Override
	public Named locateModel(ActionContext context, BPMLObjectName bpmlObjectName) {
		List<String> names = bpmlObjectName.getNodePath();
		return resolvePath(bpmlObjectName, names, null);
	}

	/**
	 * Resolves the given path in the context of the given context object.
	 */
	public static Named resolvePath(ConfigurationItem objectName, List<String> path, Named context) {
		for (int pos = 0; pos < path.size(); pos++) {
			String name = path.get(pos).trim();

			Named newContext;
			if (context == null) {
				newContext = getCollaboration(name);
			} else if (context instanceof Collaboration) {
				Collaboration collaboration = (Collaboration) context;
				Set<? extends Participant> participants = collaboration.getParticipants();
				newContext = find(name, participants);
				if (newContext == null) {
					Set<? extends MessageFlow> messageFlows = collaboration.getMessageFlows();
					newContext = find(name, messageFlows);
				}
			} else if (context instanceof Participant) {
				Participant participant = (Participant) context;
				List<? extends Lane> lanes = participant.getProcess().getLanes();
				newContext = find(name, lanes);
				if (newContext == null) {
					Set<? extends Node> nodes = participant.getProcess().getNodes();
					newContext = find(name, nodes);
					if (newContext == null) {
						Set<? extends Edge> edges = participant.getProcess().getEdges();
						newContext = find(name, edges);
					}
				}
			} else if (context instanceof Lane) {
				Lane lane = (Lane) context;
				Set<? extends Node> nodes = lane.getNodes();
				newContext = find(name, nodes);
			} else {
				throw ApplicationAssertions.fail(objectName,
					"Can not resolve '" + name + "' in context '" + context + "'.");
			}

			if (newContext == null) {
				throw ApplicationAssertions.fail(objectName,
					"No object found for '" + name + "' in context '" + context + "'.");
			}

			context = newContext;
		}

		return context;
	}

	private static Named find(String name, Collection<? extends Named> coll) {
		for(Named named : coll) {
			String name2 = normalize(named.getName());
			if (name.equals(name2)) {
				return named;
			}
		}
		return null;
	}

	private static String normalize(String name) {
		if (name != null) {
			return name.replaceAll("\n", "").trim();
		}
		return name;
	}

	private static Collaboration getCollaboration(String name) {
		List allWrappersFor =
			TlBpeBpmlFactory.getInstance().getAllWrappersFor(Collaboration.COLLABORATION_TYPE);
		List<Collaboration> collaborations = CollectionUtil.dynamicCastView(Collaboration.class,
			allWrappersFor);

		for (Collaboration coll : collaborations) {
			if (name.equals(coll.getName())) {
				return coll;
			}
		}
		return null;
	}

	@Override
	public Class<? extends BPMLObjectName> getNameClass() {
		return BPMLObjectName.class;
	}

	@Override
	public Class<Named> getModelClass() {
		return Named.class;
	}

	@Override
	protected boolean isCompatibleModel(Named model) {
		List<String> path = new ArrayList<>();
		try {
			buildPath(path, model);
		} catch (RuntimeException e) {
			return false;
		}
		return super.isCompatibleModel(model);
	}
}
