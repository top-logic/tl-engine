/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.job;

import com.top_logic.basic.util.ResKey;

/**
 * One of the steps a job announces it goes through.
 *
 * <p>
 * The phases are known before the job starts, so the display can show the whole way and mark how
 * far it has come. The body of the job names the phase it enters by {@link #name()}, an identifier
 * of the configuration; the reader sees the {@link #label()}.
 * </p>
 *
 * @param name
 *        The identifier the body of the job names this phase by; unique among the phases of a job.
 * @param label
 *        What the reader sees for this phase; the {@link #name()} where none is given.
 */
public record JobPhase(String name, ResKey label) {

	/**
	 * Creates a {@link JobPhase} whose label is its name, for a job whose phases carry no
	 * internationalized text of their own.
	 *
	 * @param name
	 *        The identifier the body of the job names the phase by.
	 */
	public static JobPhase named(String name) {
		return new JobPhase(name, ResKey.text(name));
	}

}
