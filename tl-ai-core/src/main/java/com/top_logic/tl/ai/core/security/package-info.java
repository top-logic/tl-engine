/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */

/**
 * Security architecture for agent runs - always on, decided by who the run acts as.
 *
 * <p>
 * This package holds the layered agent security design documented in
 * {@code doc/security/agent-security-architecture.md}. The decision types are implemented and
 * unit-tested, {@link com.top_logic.tl.ai.core.security.ModelSecurity} is reachable from every tool
 * through {@code ToolContext.modelSecurity()}, and the
 * {@link com.top_logic.tl.ai.core.security.PreCommitGuard} of section 3.5 runs once per writing turn.
 * What is <b>not</b> here is the rest of the enforcement rollout: apart from {@code ViewDocumentTool},
 * no data tool filters its results through {@link com.top_logic.tl.ai.core.security.ModelSecurity}
 * yet, which is tracked as issue #289.
 * </p>
 *
 * <p>
 * <b>There is no security mode.</b> Both layers apply to every run, unconditionally and
 * unconfigurably, and a run that cannot name an authenticated principal is refused where its context
 * is built rather than at its first tool call. What a run may do therefore follows from <em>who it
 * acts as</em>: an interactive run acts as the logged-in user, and an unattended one has to be given a
 * configured service account. Genuine framework-internal bypasses (security storage rebuild,
 * migrations) use {@code ThreadContext.inSystemContext} in Java, which is where a bypass belongs.
 * </p>
 *
 * <p>
 * Both layers deny by default, so an application that configures neither gets an agent that can do
 * almost nothing rather than one that is unchecked: an absent {@code <tool-access>} matrix grants only
 * {@link com.top_logic.tl.ai.core.security.RoleToolAccess#IMPLICIT_BASELINE}, and the engine's
 * {@code SecurityConfigurationService} is active in every TL 8 application and instantiates with empty
 * rights. See {@code doc/security/role-model-worked-example.md} for the configuration an application
 * has to write.
 * </p>
 *
 * <p>
 * The layers, in the order a tool call passes through them:
 * </p>
 * <ol>
 * <li>{@link com.top_logic.tl.ai.core.security.RoleToolAccess} - role-based tool authorization over
 * the {@link com.top_logic.tl.ai.core.security.ToolAccessPolicy}, reading the roles the principal
 * holds on the security root. A run that has nobody to answer an approval prompt is additionally
 * bounded by the policy's {@code <background-allowlist>}.</li>
 * <li>{@link com.top_logic.tl.ai.core.security.ModelSecurity} - per-object model authorization,
 * backed by the engine's {@code ModelAccessRights} through
 * {@link com.top_logic.tl.ai.core.security.ModelAccessRightsSecurity}.</li>
 * <li>{@link com.top_logic.tl.ai.core.security.PreCommitGuard} - the last gate before the turn's
 * changes are persisted, asking the same {@link com.top_logic.tl.ai.core.security.ModelSecurity} about
 * the {@link com.top_logic.tl.ai.core.security.ToolChangeSet} the engine's transaction accumulated. The
 * only layer that does not assume the tool cooperated.</li>
 * </ol>
 *
 * @see com.top_logic.tl.ai.core.agent.SecurityContext
 */
package com.top_logic.tl.ai.core.security;
