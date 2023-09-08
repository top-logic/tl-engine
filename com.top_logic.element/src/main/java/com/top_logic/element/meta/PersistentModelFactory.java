/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

import com.top_logic.element.meta.kbbased.AbstractElementFactory;
import com.top_logic.model.TLModel;
import com.top_logic.model.impl.generated.TlModelFactory;

/**
 * {@link AbstractElementFactory} for module {@link TlModelFactory#TL_MODEL_STRUCTURE}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class PersistentModelFactory extends AbstractElementFactory implements TlModelFactory {

	/**
	 * Factory method to instantiate node {@link com.top_logic.model.TLModel#TL_MODEL_TYPE}.
	 */
	public TLModel newTLModel() {
		return (TLModel) createNewWrapper(com.top_logic.model.TLModel.TL_MODEL_TYPE);
	}

}

