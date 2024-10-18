/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.layout.execution;

import static com.top_logic.bpe.layout.execution.ManualTaskTemplateBuilder.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.top_logic.bpe.bpml.model.Lane;
import com.top_logic.bpe.bpml.model.ManualTask;
import com.top_logic.bpe.bpml.model.Node;
import com.top_logic.bpe.bpml.model.Participant;
import com.top_logic.bpe.bpml.model.Process;
import com.top_logic.element.layout.formeditor.FormDefinitionTemplate;
import com.top_logic.element.meta.form.fieldprovider.form.TemplateResolver;
import com.top_logic.model.TLObject;
import com.top_logic.model.form.definition.FormDefinition;

/**
 * {@link Function} computing the {@link FormDefinitionTemplate} for a given {@link Participant}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ParticipantTemplateBuilder implements TemplateResolver {

	/** Singleton {@link ParticipantTemplateBuilder} instance. */
	public static final ParticipantTemplateBuilder INSTANCE = new ParticipantTemplateBuilder();

	/**
	 * Creates a new {@link ParticipantTemplateBuilder}.
	 */
	protected ParticipantTemplateBuilder() {
		// singleton instance
	}

	@Override
	public Supplier<List<FormDefinitionTemplate>> getTemplates(TLObject object) {
		Participant participant = (Participant) object;
		return () -> {
			List<FormDefinitionTemplate> templates = new ArrayList<>();
			Process process = participant.getProcess();

			for (Lane lane : process.getLanes()) {
				Set<? extends Node> nodes = lane.getNodes();

				for (Node node : nodes) {
					if (node instanceof ManualTask manualTask && process.equals(getProcess(node))) {
						FormDefinition displayDescription = manualTask.getDisplayDescription();
						if (displayDescription != null) {
							FormDefinitionTemplate template = copyDisplayDescription(node, displayDescription);
							templates.add(template);
						}
					}
				}
			}
		
			return templates;
		};
	}

}

