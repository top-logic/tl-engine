/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.List;
import java.util.Objects;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link ViewCommand} rendered as a toolbar button that, on click, opens a native file picker and
 * uploads the selected file(s) directly - without an intermediate dialog.
 *
 * <p>
 * The configured action chain (e.g. {@code <confirm>} + {@code <with-transaction><execute-script>})
 * runs <em>once per uploaded file</em>, sequentially, with the uploaded file (a
 * {@link com.top_logic.basic.io.binary.BinaryData}) as the chain's input value. Container/context
 * values are threaded into the actions via their {@code <inputs>} channel references, exactly as for
 * a {@code <generic-command>}.
 * </p>
 *
 * <p>
 * The per-file orchestration lives in {@link ViewUploadCommandModel}; the click itself does not
 * dispatch a server command, so {@link #execute(ReactContext, Object)} is a no-op.
 * </p>
 */
public class UploadCommand implements ViewCommand {

	/**
	 * Configuration for {@link UploadCommand}.
	 */
	@TagName("upload-command")
	public interface Config extends ViewCommand.Config {

		@Override
		@ClassDefault(UploadCommand.class)
		Class<? extends ViewCommand> getImplementationClass();

		/**
		 * The chain of actions to execute once per uploaded file (the file is the chain input).
		 */
		@DefaultContainer
		List<PolymorphicConfiguration<ViewAction>> getActions();

		/**
		 * The {@code accept} filter for the native file input (e.g. {@code "image/*,.pdf"}).
		 *
		 * <p>
		 * Defaults to {@code "*"} (no restriction). Client-side hint only.
		 * </p>
		 */
		@Name("accept")
		@StringDefault("*")
		String getAccept();

		/**
		 * Whether multiple files may be selected at once.
		 */
		@Name("multiple")
		@BooleanDefault(true)
		boolean isMultiple();
	}

	private final List<ViewAction> _actions;

	private final String _accept;

	private final boolean _multiple;

	/**
	 * Creates a new {@link UploadCommand} from configuration.
	 */
	@CalledByReflection
	public UploadCommand(InstantiationContext context, Config config) {
		_actions = config.getActions().stream()
			.map(context::getInstance)
			.filter(Objects::nonNull)
			.toList();
		_accept = config.getAccept();
		_multiple = config.isMultiple();
	}

	/**
	 * The action chain run once per uploaded file.
	 */
	public List<ViewAction> getActions() {
		return _actions;
	}

	/**
	 * The {@code accept} filter for the native file input.
	 */
	public String getAccept() {
		return _accept;
	}

	/**
	 * Whether multiple files may be selected at once.
	 */
	public boolean isMultiple() {
		return _multiple;
	}

	@Override
	public HandlerResult execute(ReactContext context, Object input) {
		// The work happens per uploaded file via ViewUploadCommandModel.uploadFiles(...); a plain
		// click does not dispatch a server command for an upload button.
		return HandlerResult.DEFAULT_RESULT;
	}
}
