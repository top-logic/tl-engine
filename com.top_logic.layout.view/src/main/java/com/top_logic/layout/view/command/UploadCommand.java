/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodHandles;
import java.util.List;

import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link ViewCommand} rendered as a toolbar button that, on click, opens a native file picker and
 * uploads the selected file(s) directly - without an intermediate dialog.
 *
 * <p>
 * The configured action chain (e.g. {@code <confirm>} + {@code <with-transaction><execute-script>})
 * runs <em>once per uploaded file</em>, sequentially, with the uploaded file (a
 * {@link com.top_logic.basic.io.binary.BinaryData}) as the chain's main argument, named by
 * {@link Config#getAs()}. Container/context values are threaded into the actions via their
 * {@code <inputs>} channel references, exactly as for
 * a {@code <generic-command>}.
 * </p>
 *
 * <p>
 * The per-file orchestration lives in {@link ViewUploadCommandModel}; the click itself does not
 * dispatch a server command.
 * </p>
 *
 * @implNote {@link #execute(ReactContext, Object)} is a no-op, since the click does not dispatch a
 *           server command.
 */
@InApp
public class UploadCommand implements ViewCommand {

	/**
	 * Configuration for {@link UploadCommand}.
	 */
	@TagName("upload-command")
	public interface Config extends ViewCommand.Config {

		/** Access to the default methods of this interface for the configuration proxy. */
		Lookup LOOKUP = MethodHandles.lookup();

		@Override
		@ClassDefault(UploadCommand.class)
		Class<? extends ViewCommand> getImplementationClass();

		/** Configuration name for {@link #getAs()}. */
		String AS = "as";

		/**
		 * The chain of actions to execute once per uploaded file.
		 *
		 * <p>
		 * The actions run with the uploaded file as main argument named by {@link #getAs() as},
		 * together with the values of the {@link #getInputs() inputs}.
		 * </p>
		 */
		@DefaultContainer
		@Options(fun = AllInAppImplementations.class)
		List<PolymorphicConfiguration<? extends ViewAction>> getActions();

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

		/**
		 * The name of the argument holding the uploaded file.
		 */
		@Name(AS)
		@StringDefault(FILE_ARGUMENT)
		String getAs();

		/**
		 * The uploaded file is the value the trigger of this command supplies.
		 */
		@Override
		default String triggerName(ActionScope trigger) {
			return getAs();
		}
	}

	/**
	 * Name of the argument holding the uploaded file when no other {@link Config#getAs() name} is
	 * given.
	 */
	public static final String FILE_ARGUMENT = "file";

	private final CommandArguments _arguments;

	private final List<ViewAction> _actions;

	private final String _accept;

	private final boolean _multiple;

	private final ResKey _label;

	/**
	 * Creates a new {@link UploadCommand} from configuration.
	 */
	@CalledByReflection
	public UploadCommand(InstantiationContext context, Config config) {
		_arguments = CommandArguments.of(config, ActionScope.of(context));
		_actions = ViewActions.instantiate(context, config.getActions(), _arguments.scope());
		_accept = config.getAccept();
		_multiple = config.isMultiple();
		_label = config.getLabel();
	}

	/**
	 * The {@link Config#getLabel() label} this command is configured with, {@code null} for a
	 * command without a label.
	 */
	@Override
	public ResKey getLabel() {
		return _label;
	}

	/**
	 * The arguments the actions run with, the uploaded file being the main argument.
	 */
	public CommandArguments getArguments() {
		return _arguments;
	}

	/**
	 * The action chain run once per uploaded file.
	 */
	public List<ViewAction> getActions() {
		return _actions;
	}

	/**
	 * The rules the actions run per uploaded file {@link ViewAction#getIntrinsicRule() bring of
	 * their own}, combined.
	 */
	@Override
	public ViewExecutabilityRule getIntrinsicRule() {
		return ViewActions.intrinsicRule(_actions);
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
