/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */

/**
 * Security architecture for agent runs - the foundation, enforcement off by default.
 *
 * <p>
 * This package holds the layered agent security design documented in
 * {@code doc/security/agent-security-architecture.md}. The decision types are implemented and
 * unit-tested, and {@link com.top_logic.tl.ai.core.security.ModelSecurity} is reachable from every
 * tool through {@code ToolContext.modelSecurity()}. What is <b>not</b> here is the enforcement
 * rollout: apart from {@code ViewDocumentTool}, no data tool filters its results through
 * {@link com.top_logic.tl.ai.core.security.ModelSecurity} yet, and the pre-commit guard the
 * architecture document describes in section 3.5 is design only, with no code in this package.
 * </p>
 *
 * <p>
 * <b>Nothing is enforced unless an application opts in.</b> The default mode is
 * {@link com.top_logic.tl.ai.core.security.AiSecurityMode#LEGACY}, which checks nothing - see
 * {@link com.top_logic.tl.ai.core.security.AiSecurityMode#resolve} for why that asymmetry is
 * deliberate.
 * </p>
 *
 * <p>
 * The layers, in the order a tool call passes through them:
 * </p>
 * <ol>
 * <li>{@link com.top_logic.tl.ai.core.security.AiSecurityMode} - the run's enforcement level,
 * resolved once from the deployment-wide ceiling.</li>
 * <li>{@link com.top_logic.tl.ai.core.security.RoleToolAccess} - role-based tool authorization
 * over the {@link com.top_logic.tl.ai.core.security.ToolAccessPolicy}.</li>
 * <li>{@link com.top_logic.tl.ai.core.security.ModelSecurity} - per-object model authorization,
 * backed by the engine's {@code ModelAccessRights} through
 * {@link com.top_logic.tl.ai.core.security.ModelAccessRightsSecurity}.</li>
 * </ol>
 *
 * @see com.top_logic.tl.ai.core.agent.SecurityContext
 */
package com.top_logic.tl.ai.core.security;
