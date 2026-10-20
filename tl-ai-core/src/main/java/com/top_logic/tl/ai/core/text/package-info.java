/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
/**
 * Text manipulation an agent's tools need, written so that nothing else does.
 *
 * <p>
 * Everything here is {@link java.lang.String} in, value out: no knowledge base, no model, no
 * services. An application that addresses its own text its own way - by heading, by section, by line
 * range - reuses these without taking on the generic tools built on them.
 * </p>
 *
 * @see com.top_logic.tl.ai.core.text.TextEdits
 */
package com.top_logic.tl.ai.core.text;
