/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.preview;

/**
 * The answer of {@link PreviewWorkspaces} to a request for the preview of a workspace: the
 * {@link PreviewTarget application} to forward to, or why there is none.
 *
 * @param target
 *        The application to forward to, {@code null} if the request is refused.
 * @param refusal
 *        Why the request is refused, {@code null} if it is forwarded.
 */
public record PreviewLookup(PreviewTarget target, PreviewRefusal refusal) {

	/**
	 * A lookup that forwards to the given application.
	 */
	public static PreviewLookup found(PreviewTarget target) {
		return new PreviewLookup(target, null);
	}

	/**
	 * A lookup that refuses the request.
	 */
	public static PreviewLookup refused(PreviewRefusal refusal) {
		return new PreviewLookup(null, refusal);
	}

}
