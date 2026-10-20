/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
/**
 * Hybrid search over the application's objects: words and meaning in one query, scoped and checked
 * against the reader's rights.
 *
 * <p>
 * The index is derived data in a Lucene directory of its own, one document per chunk of an
 * object's text. See {@code tl-ai-index/README.md}.
 * </p>
 */
package com.top_logic.tl.ai.index;
