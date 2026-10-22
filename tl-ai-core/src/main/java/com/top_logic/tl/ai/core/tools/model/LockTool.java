/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.model;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tl.ai.core.tools.model.messages.LockStatusResponse;
import com.top_logic.tl.ai.core.tools.util.DataClassSerializer;
import com.top_logic.tl.ai.tool.api.AnswerMarking;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * Tool for checking whether a model element is currently locked by another editor.
 */
public class LockTool {

	/**
	 * Configuration for {@link LockTool}.
	 */
	public interface Config extends PolymorphicConfiguration<LockTool> {
		// no additional properties
	}

	/**
	 * Creates a new {@link LockTool}.
	 */
	public LockTool() {
		// stateless tool
	}

	/**
	 * Creates a new {@link LockTool} from configuration.
	 */
	@SuppressWarnings("unused")
	@CalledByReflection
	public LockTool(InstantiationContext context, Config config) {
		// no-op for stateless tools
	}

	/**
	 * Check if a model element is currently locked by another editor.
	 */
	@ToolMethod(
		description = "Check if a model element is currently locked by another editor. "
			+ "Use this before modifying an element if you suspect it may be locked, "
			+ "or after receiving a lock conflict error. "
			+ "Qualified name format: 'moduleName' for modules, 'moduleName:TypeName' for classes/enums, "
			+ "'moduleName:TypeName#partName' for properties/references/classifiers.",
		mode = ToolMode.READ, group = ToolGroups.DATA_READ,
		marking = AnswerMarking.NONE)
	public String checkLock(
			@P("Qualified name of the model element to check") String qualifiedName) {

		try {
			return doCheckLock(qualifiedName);
		} catch (Exception ex) {
			Logger.error("checkLock() error: " + ex.getMessage(), ex, LockTool.class);
			return DataClassSerializer.errorToJson("Failed to check lock: " + ex.getMessage(),
				LockTool.class, "checkLock()");
		}
	}

	private String doCheckLock(String qualifiedName) {
		if (qualifiedName == null || qualifiedName.trim().isEmpty()) {
			return DataClassSerializer.errorToJson("Qualified name is required.",
				LockTool.class, "checkLock()");
		}

		TLModelPart resolved;
		try {
			resolved = TLModelUtil.resolveModelPart(qualifiedName);
		} catch (Exception ex) {
			return DataClassSerializer.errorToJson("Element '" + qualifiedName + "' not found.",
				LockTool.class, "checkLock()");
		}

		String holder = LockHolderLookup.findLockHolder(resolved);
		LockStatusResponse response = LockStatusResponse.create();
		response.setResource(qualifiedName);

		if (holder != null) {
			response.setLocked(true);
			response.setHolder(holder);
		} else {
			response.setLocked(false);
			response.setStatusText("The element is available for editing.");
		}

		return DataClassSerializer.toJson(response, LockTool.class, "checkLock()");
	}
}
