/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.model.impl.generated.TLPropertyBase;

/**
 * A primitive attribute of a {@link TLClass}.
 * 
 * @see TLReference for a reference attribute.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Label("Object property")
public interface TLProperty extends DerivedTLTypePart, TLPropertyBase {

	@Override
	default ModelKind getModelKind() {
		return ModelKind.PROPERTY;
	}

}
