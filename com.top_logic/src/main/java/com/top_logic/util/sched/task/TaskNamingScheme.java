/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task;

import java.util.Collections;
import java.util.Map;

import com.top_logic.layout.scripting.recorder.ref.ValueNamingScheme;

/**
 * {@link ValueNamingScheme} identifying a {@link Task}.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("rawtypes")
public class TaskNamingScheme extends ValueNamingScheme<Task> {

	private static final String NAME = "name";

	@Override
	public Class<Task> getModelClass() {
		return Task.class;
	}

	@Override
	public Map<String, Object> getName(Task model) {
		return Collections.<String, Object> singletonMap(NAME, model.getName());
	}

	@Override
	public boolean matches(Map<String, Object> name, Task model) {
		return matchesDefault(name, model);
	}


}

