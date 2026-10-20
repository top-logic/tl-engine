/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.perception;

/**
 * Whether the active model can perceive a given {@link Modality}.
 *
 * <p>
 * The answer is derived from the model catalogue, which is best-effort metadata: it may be missing
 * entirely for locally hosted or custom models. {@link #UNKNOWN} keeps that case distinguishable
 * from a model that is documented as text-only.
 * </p>
 */
public enum PerceptionSupport {

	/** The catalogue states the model accepts this modality. */
	SUPPORTED,

	/** The catalogue states the model does not accept this modality. */
	UNSUPPORTED,

	/** The catalogue has no entry for the model, so nothing can be concluded. */
	UNKNOWN;

	/**
	 * Whether content of this modality may be placed into the model's context.
	 *
	 * <p>
	 * {@link #UNKNOWN} counts as permitted: refusing on missing catalogue metadata would break
	 * every model the catalogue does not know, and a provider that truly cannot take the content
	 * reports a clear API error.
	 * </p>
	 */
	public boolean permitted() {
		return this != UNSUPPORTED;
	}
}
