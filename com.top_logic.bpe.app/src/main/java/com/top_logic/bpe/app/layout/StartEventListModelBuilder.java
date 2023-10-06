/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.app.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.bpe.bpml.model.Lane;
import com.top_logic.bpe.bpml.model.Node;
import com.top_logic.bpe.bpml.model.Process;
import com.top_logic.bpe.bpml.model.StartEvent;
import com.top_logic.bpe.execution.engine.ExecutionEngine;
import com.top_logic.bpe.execution.engine.GuiEngine;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.TLContext;

/**
 * {@link ListModelBuilder} for {@link StartEvent}s. Returns a List of {@link StartEvent}s to a
 * given {@link Collaboration} where the current user has access.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class StartEventListModelBuilder<C extends StartEventListModelBuilder.Config>
		extends AbstractConfiguredInstance<C>
		implements ListModelBuilder {

	private static class ActorFilter implements Filter<Node> {

		private Person _currentPerson;

		private Map<Node, Lane> _laneByNode;

		public ActorFilter(Collaboration collaboration) {
			_currentPerson = TLContext.currentUser();
			_laneByNode = laneByNode(collaboration);

		}

		private Map<Node, Lane> laneByNode(Collaboration collaboration) {
			Map<Node, Lane> res = new HashMap<>();
			Set<? extends Process> processes = collaboration.getProcesses();
			for (Process process : processes) {
				List<? extends Lane> lanes = process.getLanes();
				for (Lane lane : lanes) {
					Set<? extends Node> nodes = lane.getNodes();
					for (Node node : nodes) {
						res.put(node, lane);
					}
				}
			}
			return res;
		}

		@Override
		public boolean accept(Node node) {
			Lane lane = _laneByNode.get(node);
			return GuiEngine.getInstance().isActor(_currentPerson, lane, null);
		}

	}

	public interface Config extends PolymorphicConfiguration<StartEventListModelBuilder<?>> {

	}

	public StartEventListModelBuilder(InstantiationContext context, C config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		if (businessModel == null) {
			return Collections.emptyList();
		}
		Collaboration collaboration = (Collaboration) businessModel;
		List<StartEvent> res = new ArrayList<>(ExecutionEngine.getInstance().getManualStartEvents(collaboration));

		if (!CollectionUtil.isEmptyOrNull(res)) {
			res = FilterUtil.filterList(filter(CollectionUtil.getFirst(res)), res);
		}

		return res;
	}

	private Filter filter(Node node) {
		Collaboration collaboration = node.getProcess().getParticipant().getCollaboration();
		return new ActorFilter(collaboration);
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return aModel instanceof Collaboration && hasStartEvent((Collaboration) aModel);
	}

	private boolean hasStartEvent(Collaboration collaboration) {
		return !GuiEngine.getInstance().getStartEventsFor(collaboration, TLContext.currentUser()).isEmpty();
	}

	@Override
	public boolean supportsListElement(LayoutComponent contextComponent, Object listElement) {
		return listElement instanceof StartEvent;
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent aComponent, Object anElement) {
		/* The list does not depend on the model. So any model is correct. For stability return
		 * current model. */
		return aComponent.getModel();
	}

}
