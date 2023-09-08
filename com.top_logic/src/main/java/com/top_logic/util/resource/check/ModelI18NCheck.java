/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.resource.check;

import com.top_logic.basic.Logger;
import com.top_logic.basic.i18n.I18NChecker;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.util.model.ModelService;

/**
 * {@link I18NChecker} checking the I18N of the model elements.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ModelI18NCheck implements I18NChecker {

	@Override
	public void checkI18N() {
		Logger.info("Checking model resources.", ModelI18NCheck.class);
		checkModelI18N();
		Logger.info("Checking model resources done.", ModelI18NCheck.class);
	}

	private void checkModelI18N() {
		MetaResourceProvider resourceProvider = MetaResourceProvider.INSTANCE;
		TLModel model = ModelService.getInstance().getModel();
		for (TLModule module : model.getModules()) {
			for (TLType type : module.getTypes()) {
				resourceProvider.getLabel(type);
				if (type instanceof TLStructuredType && type.getModelKind() != ModelKind.ASSOCIATION) {
					for (TLStructuredTypePart part : ((TLStructuredType) type).getAllParts()) {
						resourceProvider.getLabel(part);
					}
				} else if (type instanceof TLEnumeration) {
					for (TLClassifier part : ((TLEnumeration) type).getClassifiers()) {
						resourceProvider.getLabel(part);
					}
				}
			}
		}
	}

}

