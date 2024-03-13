/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task.schedule;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.form.model.FormGroup;

/**
 * {@link SchedulingAlgorithm} that schedules a task immediately after first system startup.
 */
@InApp
public class OnStartup implements SchedulingAlgorithm {

	/**
	 * Configuration options for {@link OnStartup}.
	 */
	@TagName("on-startup")
	public interface Config<I extends OnStartup> extends PolymorphicConfiguration<I> {
		// Marker only.
	}

	/**
	 * Creates a {@link OnStartup} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public OnStartup(InstantiationContext context, Config<?> config) {
		super();
	}

	@Override
	public long nextSchedule(long notBefore, long lastSchedule) {
		return lastSchedule == NO_SCHEDULE ? notBefore : NO_SCHEDULE;
	}

	@Override
	public void fillFormGroup(FormGroup group) {
		// No contents.
	}

}
