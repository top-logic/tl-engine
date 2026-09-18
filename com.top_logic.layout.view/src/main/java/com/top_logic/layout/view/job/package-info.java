/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

/**
 * Work of a view that takes longer than a request may take.
 *
 * <p>
 * A {@link com.top_logic.layout.view.job.StartJobAction} hands the work of a command to a worker
 * thread and suspends the command until it has ended. The work reports what it is doing through a
 * {@link com.top_logic.layout.view.job.JobMonitor}, and every report becomes an immutable
 * {@link com.top_logic.layout.view.job.JobState} on a channel - so a display of a running job is an
 * ordinary display of a channel value.
 * </p>
 *
 * @see com.top_logic.layout.view.job.JobRunner
 */
package com.top_logic.layout.view.job;
