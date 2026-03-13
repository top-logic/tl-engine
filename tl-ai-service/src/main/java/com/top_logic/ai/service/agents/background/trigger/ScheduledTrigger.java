/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background.trigger;

import java.util.List;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.util.sched.task.schedule.SchedulingAlgorithm;

/**
 * Trigger that fires on a time-based schedule.
 *
 * <p>
 * The background agent runs periodically according to one or more {@link SchedulingAlgorithm}
 * configurations. This is useful for periodic reviews, report generation, or maintenance tasks.
 * </p>
 */
public interface ScheduledTrigger extends TriggerConfig {

	/** @see #getSchedules() */
	String SCHEDULES = "schedules";

	/**
	 * The scheduling algorithms that determine when this trigger fires.
	 *
	 * <p>
	 * Multiple schedules can be configured; the trigger fires whenever any of them is due.
	 * </p>
	 *
	 * @return The list of scheduling algorithm configurations, may be empty.
	 */
	@Name(SCHEDULES)
	@Options(fun = AllInAppImplementations.class)
	List<PolymorphicConfiguration<? extends SchedulingAlgorithm>> getSchedules();
}
