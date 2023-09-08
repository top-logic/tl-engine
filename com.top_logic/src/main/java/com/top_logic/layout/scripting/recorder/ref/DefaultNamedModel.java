/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

/**
 * {@link NamedModel} that dispatched {@link #getModelName()} to
 * {@link ModelResolver#buildModelName(Object)}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultNamedModel implements NamedModel {

	@Override
	public ModelName getModelName() {
		return ModelResolver.buildModelName(this);
	}

}

