/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.execution.engine;

import com.top_logic.base.config.i18n.InternationalizedUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.bpe.bpml.display.ProcessFormDefinition;
import com.top_logic.bpe.bpml.display.SpecializedForm;
import com.top_logic.bpe.bpml.display.SpecializedForm.Config;
import com.top_logic.bpe.bpml.model.ManualTask;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;
import com.top_logic.layout.scripting.runtime.action.ApplicationActionOp;
import com.top_logic.model.form.definition.FormDefinition;

/**
 * {@link ApplicationActionOp} to set a {@link FormDefinition} for a {@link ManualTask}.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class FormEditActionOp extends AbstractApplicationActionOp<FormEditAction> {

	/** {@link TypedConfiguration} constructor. */
	public FormEditActionOp(InstantiationContext context, FormEditAction action) {
		super(context, action);
	}

	@Override
	protected Object processInternal(ActionContext context, Object argument) {
		ManualTask manualTask = getManualTask(context);
		FormDefinition formDefinition = InternationalizedUtil.storeI18N(copyFormDefinition());

		try (Transaction transaction = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			ProcessFormDefinition form = TypedConfiguration.newConfigItem(ProcessFormDefinition.class);
			Config<?> specializedForm = TypedConfiguration.newConfigItem(SpecializedForm.Config.class);
			specializedForm.setForm(formDefinition);
			form.setFormProvider(specializedForm);
			manualTask.setFormDefinition(form);
			transaction.commit();
		}
		return argument;
	}

	private ManualTask getManualTask(ActionContext context) {
		ModelName manualTaskName = getConfig().getManualTask();
		ManualTask manualTask = (ManualTask) ModelResolver.locateModel(context, manualTaskName);

		return manualTask;
	}

	private FormDefinition copyFormDefinition() {
		return TypedConfiguration.copy(getConfig().getFormDefinition());
	}
}