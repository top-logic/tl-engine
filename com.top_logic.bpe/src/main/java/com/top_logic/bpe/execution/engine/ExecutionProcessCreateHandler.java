/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.execution.engine;

import java.util.Iterator;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.bpe.bpml.model.Participant;
import com.top_logic.bpe.bpml.model.Process;
import com.top_logic.bpe.bpml.model.StartEvent;
import com.top_logic.bpe.execution.model.ProcessExecution;
import com.top_logic.bpe.execution.model.TlBpeExecutionFactory;
import com.top_logic.element.meta.gui.DefaultCreateAttributedCommandHandler;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.util.model.ModelService;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class ExecutionProcessCreateHandler extends DefaultCreateAttributedCommandHandler {

	public ExecutionProcessCreateHandler(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public Object createObject(LayoutComponent component, Object createContext, FormContainer formContainer,
			@SuppressWarnings("rawtypes") Map someValues) {

		Object newObject = super.createObject(component, createContext, formContainer, someValues);
		StartEvent startEvent = (StartEvent) createContext;
		ExecutionEngine.getInstance().init((ProcessExecution) newObject, startEvent);

		return newObject;
	}

	@Override
	public Wrapper createNewObject(Map<String, Object> someValues, Wrapper aModel) {
		StartEvent startEvent = (StartEvent) aModel;
		return (Wrapper) newProcessExecution(startEvent);
	}

	public static ProcessExecution newProcessExecution(StartEvent startEvent) {
		TLClass modelType = getTypeForStartEvent(startEvent);
		return (ProcessExecution) ModelService.getInstance().getFactory().createObject(modelType, null, null);
	}

	public static TLClass getTypeForStartEvent(StartEvent startEvent) {
		Process process = startEvent.getProcess();
		Participant participant = process.getParticipant();
		TLClass modelType = (TLClass) participant.getModelType();
		if (modelType == null) {
			modelType = TlBpeExecutionFactory.getProcessExecutionType();
		}
		return modelType;
	}

	@Override
	protected Map<String, Object> extractValues(FormContainer aContainer, Wrapper anAttributed) {
		Map<String, Object> res = super.extractValues(aContainer, anAttributed);
		Iterator<? extends FormMember> members = aContainer.getMembers();
		while (members.hasNext()) {
			FormMember member = members.next();
			if (member instanceof FormContainer) {
				res.putAll(extractValues((FormContainer) member, anAttributed));
			}
		}
		return res;
	}

}
