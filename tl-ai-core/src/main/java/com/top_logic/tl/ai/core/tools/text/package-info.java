/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */

/**
 * Reading and changing one text attribute of one object, a passage at a time.
 *
 * <p>
 * The tool layer over {@link com.top_logic.tl.ai.core.text.TextEdits}: it resolves the object, checks
 * what the run's principal may do with it, and enrols the change in the turn's transaction. The
 * editing itself happens in the applier, which knows nothing about any of that.
 * </p>
 *
 * @see com.top_logic.tl.ai.core.tools.text.TextEditTool
 * @see com.top_logic.tl.ai.core.text.TextEdits
 */
package com.top_logic.tl.ai.core.tools.text;
