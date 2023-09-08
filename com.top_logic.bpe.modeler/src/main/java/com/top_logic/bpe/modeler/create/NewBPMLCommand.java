/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.modeler.create;

import java.io.IOException;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.bpe.bpml.model.Participant;
import com.top_logic.bpe.modeler.upload.BPMLUploadCommand;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.component.AbstractCreateCommandHandler;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * {@link CommandHandler} creating a new empty BPMN diagram.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NewBPMLCommand extends AbstractCreateCommandHandler {

	/**
	 * Creates a {@link NewBPMLCommand} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public NewBPMLCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Object createObject(LayoutComponent component, Object createContext, FormContainer formContainer,
			Map<String, Object> arguments) {
		NewForm form = (NewForm) EditorFactory.getModel(formContainer);

		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		BinaryContent data = new ClassRelativeBinaryContent(NewBPMLCommand.class, "default-diagram.bpmn.xml");
		Collaboration collaboration;
		try {
			collaboration = BPMLUploadCommand.importCollaboration(DefaultDisplayContext.getDisplayContext(), kb, data);
		} catch (XMLStreamException | IOException ex) {
			throw reportProblem(ex);
		}
		collaboration.setName(form.getName());
		collaboration.setIcon(form.getIcon());
		for (Participant p : collaboration.getParticipants()) {
			p.setName(form.getName());
		}
		return collaboration;
	}

}
