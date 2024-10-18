/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.layout.execution;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.top_logic.bpe.bpml.display.ProcessFormDefinition;
import com.top_logic.bpe.bpml.display.SpecializedForm;
import com.top_logic.bpe.bpml.model.Lane;
import com.top_logic.bpe.bpml.model.LaneSet;
import com.top_logic.bpe.bpml.model.ManualTask;
import com.top_logic.bpe.bpml.model.Node;
import com.top_logic.bpe.bpml.model.Participant;
import com.top_logic.bpe.bpml.model.Process;
import com.top_logic.bpe.bpml.model.TlBpeBpmlFactory;
import com.top_logic.element.layout.formeditor.FormDefinitionTemplate;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.form.fieldprovider.form.TLFormTemplates;
import com.top_logic.element.meta.form.fieldprovider.form.TemplateResolver;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.form.definition.FormDefinition;

/**
 * {@link Function} computing the {@link FormDefinitionTemplate} for a given {@link Node}.
 * 
 * @see TLFormTemplates
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ManualTaskTemplateBuilder implements TemplateResolver {

	/** Singleton {@link ManualTaskTemplateBuilder} instance. */
	public static final ManualTaskTemplateBuilder INSTANCE = new ManualTaskTemplateBuilder();

	/**
	 * Creates a new {@link ManualTaskTemplateBuilder}.
	 */
	protected ManualTaskTemplateBuilder() {
		// singleton instance
	}

	@Override
	public Supplier<List<FormDefinitionTemplate>> getTemplates(TLObject object) {
		Node baseNode = (Node) object;
		return () -> {
			List<FormDefinitionTemplate> templates = new ArrayList<>();
			Process process = getProcess(baseNode);
			Set<? extends TLObject> referers = getParticipant(baseNode);
		
			for (TLObject ref : referers) {
				if (ref instanceof Participant) {
					List<? extends Lane> lanes = ((Participant) ref).getProcess().getLanes();
		
					for (Lane lane : lanes) {
						Set<? extends Node> nodes = lane.getNodes();
		
						for (Node node : nodes) {
							if (node instanceof ManualTask && process.equals(getProcess(node))) {
								if (node != baseNode) {
									ProcessFormDefinition displayDescription = ((ManualTask) node).getFormDefinition();
									if (displayDescription != null
										&& displayDescription instanceof SpecializedForm.Config<?> form) {
										FormDefinitionTemplate template =
											copyDisplayDescription(node, form.getForm());
										templates.add(template);
									}
								}
							}
						}
					}
				}
			}
		
			return templates;
		};
	}

	private TLStructuredType getType(Node businessModel) {
		Process process = getProcess(businessModel);

		return (TLClass) process.getParticipant().getModelType();
	}

	static Process getProcess(Node businessModel) {
		Lane lane = businessModel.getLane();

		if (lane != null) {
			return getProcess(lane);
		} else {
			return businessModel.getProcess();
		}
	}

	static Process getProcess(Lane lane) {
		LaneSet laneSet = lane.getOwner();

		if (laneSet instanceof Lane) {
			return getProcess((Lane) laneSet);
		} else {
			return ((Process) laneSet);
		}
	}

	private Set<? extends TLObject> getParticipant(Object businessModel) {
		TLStructuredType type = getType((ManualTask) businessModel);
		TLStructuredTypePart reference = TlBpeBpmlFactory.getModelTypeParticipantAttr();
		Set<? extends TLObject> referers = AttributeOperations.getReferers(type, reference);

		return referers;
	}

	static FormDefinitionTemplate copyDisplayDescription(Node node, FormDefinition displayDescription) {
		return new FormDefinitionTemplate(node.getName(), displayDescription);
	}

}

