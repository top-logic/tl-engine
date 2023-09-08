/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value.object;

import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.NamedModel;
import com.top_logic.layout.scripting.recorder.ref.value.ValueRef;


/**
 * Represents a reference to a {@link NamedModel}. Uses the {@link ModelName} of the
 * {@link NamedModel} to identify it.
 * 
 * @deprecated See {@link ValueRef}
 * 
 * @author <a href="mailto:Jan.Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
@Deprecated
public interface NamedModelRef extends ObjectRef {

	ModelName getModelName();
	void setModelName(ModelName modelName);

}
