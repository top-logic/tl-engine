/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

/**
 * The "Agent-Sicherheit" administration view: what the tool-access policy means, in one place.
 *
 * <p>
 * Three views over {@link com.top_logic.tl.ai.core.security.AiToolAccessService}, contributed as a tab
 * of <em>Administration &#8594; Berechtigungen</em> by
 * {@code WEB-INF/layouts/admin/security/index.layout.overlay.xml}:
 * </p>
 * <ul>
 * <li><b>Tool-Katalog</b> - every registered tool with its capability group, where that group
 * comes from, and which roles reach it. Answers "what am I granting with {@code data.read}", which the
 * raw policy editor cannot: it shows names, never the catalog behind them.</li>
 * <li><b>Richtlinie</b> - the editor: per role and capability a tri-state, three named states for the
 * baseline and for the background limit, and one either-or per entry that names single tools. The one
 * view here that writes, and the one that refuses to save a policy the next start would reject.</li>
 * <li><b>Effektive Rechte</b> - one row per capability group for a chosen account or role set, in
 * either run mode, each row naming the rule that decided and the rule it overrode. Answers "what can
 * this account's agent actually call, and why".</li>
 * </ul>
 *
 * <p>
 * Nothing here decides who may do what. The decisions come from
 * {@link com.top_logic.tl.ai.core.security.EffectiveToolAccess}, which is the read-only twin of the
 * enforcement path; what the editor writes is the same configuration file the engine's service editor
 * writes, which remains the raw view of the same policy. The catalog and the editor report the
 * <em>saved</em> policy, the effective-permissions view the <em>running</em> service, and the editor
 * says so while the two differ. See {@code doc/security/tool-access-configuration.md}.
 * </p>
 */
package com.top_logic.tl.ai.core.security.layout;
