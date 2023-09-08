/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.modeler.upload;

import java.util.Map;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ExecutabilityRule} that returns a
 * {@link ExecutableState#createDisabledState(com.top_logic.basic.util.ResKey) disabled} state, when
 * the {@link UploadForm} of a {@link FormHandler} has no data.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DataAvailableRule implements ExecutabilityRule {

	/** Singleton {@link DataAvailableRule} instance. */
	public static final DataAvailableRule INSTANCE = new DataAvailableRule();

	/**
	 * Creates a new {@link DataAvailableRule}.
	 */
	protected DataAvailableRule() {
		// singleton instance
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		if (aComponent instanceof FormHandler) {
			FormContext fc = ((FormHandler) aComponent).getFormContext();
			UploadForm form = (UploadForm) EditorFactory.getModel(fc);
			BinaryData data = form.getData();
			if (data == null) {
				return ExecutableState.createDisabledState(I18NConstants.ERROR_NO_DATA);
			}

		}
		return ExecutableState.EXECUTABLE;
	}

}
