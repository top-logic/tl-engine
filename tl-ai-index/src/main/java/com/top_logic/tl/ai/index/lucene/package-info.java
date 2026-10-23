/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
/**
 * Everything that speaks Lucene: the document of a chunk, the index directory of one generation,
 * the hybrid query and the fusion of its two rankings.
 *
 * <p>
 * All Lucene code of the module is in this package, so that a move of the engine to another Lucene
 * version changes this package and nothing else.
 * </p>
 */
package com.top_logic.tl.ai.index.lucene;
