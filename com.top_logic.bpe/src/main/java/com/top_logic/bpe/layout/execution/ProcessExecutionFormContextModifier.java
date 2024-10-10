/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.app.layout;

import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.bpe.bpml.model.Process;
import com.top_logic.bpe.bpml.model.StartEvent;
import com.top_logic.bpe.execution.model.ProcessExecution;
import com.top_logic.element.layout.meta.DefaultFormContextModificator;
import com.top_logic.element.layout.meta.FormContextModificator;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link FormContextModificator} the initializes the {@link Collaboration}-Field for a
 * {@link ProcessExecution}.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class ProcessExecutionFormContextModifier extends DefaultFormContextModificator {

	/**
	 * Singleton {@link ProcessExecutionFormContextModifier} instance.
	 */
	public static final ProcessExecutionFormContextModifier INSTANCE = new ProcessExecutionFormContextModifier();

	private ProcessExecutionFormContextModifier() {
		// Singleton constructor.
	}

	@Override
	public void modify(LayoutComponent component, String aName, FormMember aMember, TLStructuredTypePart aMA,
			TLClass type, TLObject anAttributed, AttributeUpdate anUpdate, AttributeFormContext aContext,
			FormContainer currentGroup) {
		super.modify(component, aName, aMember, aMA, type, anAttributed, anUpdate, aContext, currentGroup);
		if (ProcessExecution.COLLABORATION_ATTR.equals(aName)) {
			StartEvent se = (StartEvent) component.getModel();
			Collaboration collaboration = se.getProcess().getParticipant().getCollaboration();
			((SelectField) aMember).setImmutable(true);
			if (anAttributed == null) {
				((SelectField) aMember).setAsSingleSelection(collaboration);
				anUpdate.updateValue(collaboration);
			}
		}
		if (ProcessExecution.PROCESS_ATTR.equals(aName)) {
			StartEvent se = (StartEvent) component.getModel();
			Process process = se.getProcess();
			((SelectField) aMember).setImmutable(true);
			if (anAttributed == null) {
				((SelectField) aMember).setAsSingleSelection(process);
				anUpdate.updateValue(process);
			}
		}

	}

}

