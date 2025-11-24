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

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.bpe.bpml.display.FormProvider;
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
import com.top_logic.element.layout.formeditor.definition.TLFormDefinition;
import com.top_logic.element.meta.form.fieldprovider.form.TLFormTemplates;
import com.top_logic.element.meta.form.fieldprovider.form.TemplateResolver;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.Resources;

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
			TLClass modelType = process.getParticipant().getModelType();

			Set<? extends TLObject> referers = getAllParticipants(modelType);
		
			for (TLObject ref : referers) {
				if (ref instanceof Participant participant) {
					List<? extends Lane> lanes = participant.getProcess().getLanes();
		
					for (Lane lane : lanes) {
						Set<? extends Node> nodes = lane.getNodes();
		
						for (Node node : nodes) {
							if (node == baseNode) {
								continue;
							}
							if (node instanceof ManualTask manualTask && process.equals(getProcess(node))) {
								ProcessFormDefinition displayDescription = manualTask.getFormDefinition();
								if (displayDescription == null) {
									continue;
								}
								PolymorphicConfiguration<? extends FormProvider> formProvider =
									displayDescription.getFormProvider();
								if (formProvider instanceof SpecializedForm.Config<?> form) {
									FormDefinitionTemplate template = copyDisplayDescription(node, form.getForm());
									templates.add(template);
								}
							}
						}
					}
				}
			}

			addModelTypeForms(templates, modelType);
		
			return templates;
		};
	}

	private void addModelTypeForms(List<FormDefinitionTemplate> templates, TLClass modelType) {
		Resources resources = Resources.getInstance();
		for (TLClass type: TLModelUtil.getReflexiveTransitiveGeneralizations(modelType)) {
			TLFormDefinition formDefinition = type.getAnnotation(TLFormDefinition.class);
			if (formDefinition == null) {
				continue;
			}
			FormDefinition form = formDefinition.getForm();
			if (form == null) {
				// must actually not occur.
				continue;
			}
			String name = resources.getString(I18NConstants.FORM_DEF_TEMPLATE__TYPE.fill(type));
			templates.add(new FormDefinitionTemplate(name, form));
		}
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

	private Set<? extends TLObject> getAllParticipants(TLStructuredType modelType) {
		TLReference reference = TlBpeBpmlFactory.getModelTypeParticipantAttr();
		Set<? extends TLObject> referers = modelType.tReferers(reference);

		return referers;
	}

	static FormDefinitionTemplate copyDisplayDescription(Node node, FormDefinition displayDescription) {
		Resources resources = Resources.getInstance();
		String name = resources.getString(I18NConstants.FORM_DEF_TEMPLATE__TASK.fill(node.getName()));
		return new FormDefinitionTemplate(name, displayDescription);
	}

}

