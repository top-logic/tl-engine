/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.model;

import java.io.IOException;
import java.util.Collection;

import com.top_logic.base.locking.token.LockInfo;
import com.top_logic.base.locking.token.Token;
import com.top_logic.base.locking.token.TokenService;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;

import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonWriter;

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
	 */
	@ToolMethod(
		description = "Check if a model element is currently locked by another editor. "
			+ "Use this before modifying an element if you suspect it may be locked, "
			+ "or after receiving a lock conflict error. "
			+ "Qualified name format: 'moduleName' for modules, 'moduleName:TypeName' for classes/enums, "
			+ "'moduleName:TypeName#partName' for properties/references/classifiers.",
		mode = ToolMode.READ)
	public String checkLock(
			@P("Qualified name of the model element to check") String qualifiedName) {

		try {
			return ThreadContextManager.inSystemInteraction(
				LockTool.class,
				() -> doCheckLock(qualifiedName));
		} catch (Exception ex) {
			Logger.error("checkLock() error: " + ex.getMessage(), ex, LockTool.class);
			return buildErrorJson("Failed to check lock: " + ex.getMessage());
		}
	}

	private String doCheckLock(String qualifiedName) throws IOException {
		if (qualifiedName == null || qualifiedName.trim().isEmpty()) {
			return buildErrorJson("Qualified name is required.");
		}

		TLModelPart resolved;
		try {
			resolved = TLModelUtil.resolveModelPart(qualifiedName);
		} catch (Exception ex) {
			return buildErrorJson("Element '" + qualifiedName + "' not found.");
		}

		Collection<LockInfo> allLocks = TokenService.getInstance().getAllLocks();
		for (LockInfo info : allLocks) {
			for (Token token : info.getTokens()) {
				TLObject lockedObject = token.getObject();
				if (lockedObject != null && lockedObject.equals(resolved)) {
					String holderName = resolveOwnerName(info.getOwner());
					return buildLockedJson(qualifiedName, holderName);
				}
			}
		}

		return buildUnlockedJson(qualifiedName);
	}

	private static String resolveOwnerName(TLObject owner) {
		if (owner == null) {
			return "unknown";
		}
		String label = MetaLabelProvider.INSTANCE.getLabel(owner);
		return label != null ? label : owner.toString();
	}

	private static String buildLockedJson(String qualifiedName, String holder) throws IOException {
		StringW buffer = new StringW();
		try (JsonWriter json = new JsonWriter(buffer)) {
			json.beginObject();
			json.name("locked");
			json.value(true);
			json.name("resource");
			json.value(qualifiedName);
			json.name("holder");
			json.value(holder);
			json.endObject();
		}
		return buffer.toString();
	}

	private static String buildUnlockedJson(String qualifiedName) throws IOException {
		StringW buffer = new StringW();
		try (JsonWriter json = new JsonWriter(buffer)) {
			json.beginObject();
			json.name("locked");
			json.value(false);
			json.name("resource");
			json.value(qualifiedName);
			json.name("message");
			json.value("The element is available for editing.");
			json.endObject();
		}
		return buffer.toString();
	}

	private static String buildErrorJson(String message) {
		try {
			StringW buffer = new StringW();
			try (JsonWriter json = new JsonWriter(buffer)) {
				json.beginObject();
				json.name("error");
				json.value(message);
				json.endObject();
			}
			return buffer.toString();
		} catch (IOException ex) {
			return "{\"error\":\"" + message.replace("\"", "\\\"") + "\"}";
		}
	}
}
