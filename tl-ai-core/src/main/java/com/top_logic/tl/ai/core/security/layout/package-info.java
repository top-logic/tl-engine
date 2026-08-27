/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */

/**
 * The "Agent-Sicherheit" administration view: what the tool-access policy means, in one place.
 *
 * <p>
 * Two read-only views over {@link com.top_logic.tl.ai.core.security.AiToolAccessService}, contributed
 * as a tab of <em>Administration &rarr; Berechtigungen</em> by
 * {@code WEB-INF/layouts/admin/security/index.layout.overlay.xml}:
 * </p>
 * <ul>
 * <li><b>Werkzeug-Katalog</b> - every registered tool with its capability group, where that group
 * comes from, and which roles reach it. Answers "what am I granting with {@code data.read}", which the
 * raw policy editor cannot: it shows names, never the catalog behind them.</li>
 * <li><b>Effektive Rechte</b> - one row per capability group for a chosen account or role set, in
 * either run mode, each row naming the rule that decided and the rule it overrode. Answers "what can
 * this account's agent actually call, and why".</li>
 * </ul>
 *
 * <p>
 * Nothing here decides anything. The decisions come from
 * {@link com.top_logic.tl.ai.core.security.EffectiveToolAccess}, which is the read-only twin of the
 * enforcement path, and the policy itself is still edited in the service editor - the raw view of the
 * same configuration. See {@code doc/security/tool-access-configuration.md}.
 * </p>
 */
package com.top_logic.tl.ai.core.security.layout;
