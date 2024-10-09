/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.layout.execution;

import static com.top_logic.layout.form.template.model.Templates.*;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.bpe.bpml.model.Participant;
import com.top_logic.bpe.execution.model.ProcessExecution;
import com.top_logic.element.layout.formeditor.FormEditorUtil;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.component.DefaultEditAttributedComponent;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.model.form.implementation.FormMode;

/**
 * Component editing the {@link FormDefinition} of the {@link ProcessExecution} model.
 * 
 * @author <a href="mailto:fma@top-logic.com">fma</a>
 */
public class ProcessExecutionComponent extends DefaultEditAttributedComponent implements DisplayDescriptionAware {

	/**
	 * Creates a new {@link ProcessExecutionComponent}.
	 */
	public ProcessExecutionComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public FormDefinition getDisplayDescription() {
		return getParticipant().getDisplayDescription();
	}

	private Participant getParticipant() {
		return getProcessExecution().getProcess().getParticipant();
	}

	/**
	 * Type-safe access to {@link #getModel()}.
	 */
	public final ProcessExecution getProcessExecution() {
		ProcessExecution pe = (ProcessExecution) getModel();
		return pe;
	}

	@Override
	public TLObject getContextModel() {
		return getProcessExecution();
	}

	@Override
	protected void addMoreAttributes(Object someObject, AttributeFormContext someContext, boolean create) {
		super.addMoreAttributes(someObject, someContext, create);

		TLObject pe = getContextModel();
		TLClass tType = (TLClass) pe.tType();
		FormDefinition fd = getDisplayDescription();
		if (fd == null) {
			template(someContext, div(htmlTemplate(
				Fragments.h2(Fragments.message(I18NConstants.NO_DISPLAY_DESCRIPTION__WF.fill(getParticipant()))))));
		} else {
			FormMode formMode = create ? FormMode.CREATE : FormMode.EDIT;
			FormEditorUtil.createEditorGroup(someContext, tType, fd, pe, formMode);
		}
	}

}
