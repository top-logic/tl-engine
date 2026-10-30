/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.system;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.element.boundsec.manager.StorageAccessManager;
import com.top_logic.knowledge.security.SecurityStorage;
import com.top_logic.model.search.expr.config.operations.AdminOnly;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.error.TopLogicException;

/**
 * TL-Script functions maintaining the {@link SecurityStorage} of a {@link StorageAccessManager}:
 * the commands of the detail page of the access manager in the administration.
 *
 * <p>
 * Rebuilding the storage computes the roles of all users on all objects and may take a long time,
 * so a view runs it as the body of a background job:
 * </p>
 *
 * <pre>
 * &lt;start-job job="job" function="{ $job.jobIndeterminate(); securityStorageRebuild(); }"/&gt;
 * </pre>
 *
 * @implNote Every public static method here is a TL-Script function named by the
 *           {@link ScriptPrefix} of this class followed by the capitalized method name, so
 *           {@link #rebuild()} is called <code>securityStorageRebuild</code>. The functions change
 *           the access rights of the application, which only an administrator may do from a script
 *           entered interactively.
 */
@ScriptPrefix(SecurityStorageFunctions.PREFIX)
@AdminOnly
public class SecurityStorageFunctions extends TLScriptFunctions {

	/** Prefix all function names of this class start with. */
	public static final String PREFIX = "securityStorage";

	/**
	 * Computes the security storage of the running access manager again from the role rules.
	 *
	 * <p>
	 * The storage is computed without access checks, since it reads the objects of all users. The
	 * function returns once the storage is complete.
	 * </p>
	 *
	 * @throws TopLogicException
	 *         If the access manager has no security storage, or the storage was not rebuilt,
	 *         because a rebuild was already running on this or another cluster node, or failed.
	 *
	 * @see StorageAccessManager#reload()
	 */
	@Label("Rebuild the security storage")
	public static void rebuild() {
		AccessManager.Module module = AccessManager.Module.INSTANCE;
		if (!module.isActive() || !(module.getImplementationInstance() instanceof StorageAccessManager manager)) {
			throw new TopLogicException(I18NConstants.ERROR_NO_SECURITY_STORAGE);
		}
		boolean rebuilt = ThreadContext.withoutSecurityChecks(() -> manager.reload());
		if (!rebuilt) {
			throw new TopLogicException(I18NConstants.ERROR_SECURITY_STORAGE_NOT_REBUILT);
		}
	}

}
