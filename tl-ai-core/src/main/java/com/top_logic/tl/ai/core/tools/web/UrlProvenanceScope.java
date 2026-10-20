/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

/**
 * Which agent runs may open only the web addresses they were given.
 *
 * @see UrlProvenance
 */
public enum UrlProvenanceScope {

	/** No run is restricted: an agent may open any address the browsing policy permits. */
	OFF,

	/**
	 * Runs that another run started - a delegated agent or a background subagent, such as a research
	 * worker - may open only the addresses they were given. The chat's own agent is not restricted.
	 */
	SUBAGENTS,

	/** Every agent run may open only the addresses it was given. */
	ALL;
}
