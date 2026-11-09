/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.List;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.CommandErrors;
import com.top_logic.layout.react.control.button.UploadCommandModel;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewCommandModel} for an {@link UploadCommand}: runs the command's action chain once per
 * uploaded file, sequentially.
 *
 * <p>
 * Files are processed one at a time - the next file starts only after the current file's chain has
 * fully settled (resumed past its last action <em>or</em> aborted). This serializes any per-file
 * {@code <confirm>} dialog, and makes each file independent: aborting one file's confirmation skips
 * only that file.
 * </p>
 *
 * <p>
 * A file whose chain fails is reported to the user through the window's snackbar - naming the file
 * and the reason the chain gave - and the remaining files are processed regardless, see
 * {@link CommandErrors}.
 * </p>
 */
public class ViewUploadCommandModel extends ViewCommandModel implements UploadCommandModel {

	private final UploadCommand _uploadCommand;

	/**
	 * Creates a new {@link ViewUploadCommandModel}.
	 */
	public ViewUploadCommandModel(UploadCommand command, UploadCommand.Config config, ViewChannel inputChannel,
			ViewExecutabilityRule rule) {
		this(command, config, inputChannel == null ? List.of() : List.of(inputChannel), rule);
	}

	/**
	 * Creates a new {@link ViewUploadCommandModel} for a command reading the given input channels.
	 *
	 * <p>
	 * The uploaded file is the value the trigger supplies: the actions run with each file in turn.
	 * The rules decide before any file exists, over the arguments with the file being {@code null}.
	 * </p>
	 *
	 * @param inputChannels
	 *        The channels of the {@link UploadCommand.Config#getInputs() inputs}, in their order.
	 * @param rule
	 *        The rule of the command, built by
	 *        {@link ViewExecutabilityRules#build(ViewCommand.Config, String, ViewContext)}, so that
	 *        its configured rules decide over the {@link UploadCommand.Config#ruleScope(ActionScope)
	 *        inputs}.
	 */
	public ViewUploadCommandModel(UploadCommand command, UploadCommand.Config config,
			List<ViewChannel> inputChannels, ViewExecutabilityRule rule) {
		super(command, config, inputChannels, command.getArguments().getTrigger(), rule, null);
		_uploadCommand = command;
	}

	@Override
	public String getAccept() {
		return _uploadCommand.getAccept();
	}

	@Override
	public boolean isMultiple() {
		return _uploadCommand.isMultiple();
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Before the first file is processed, the rules decide once more over the same arguments as for
	 * the {@link #getExecutableState() state of the button}: the current values of the inputs, no
	 * file yet.
	 * </p>
	 *
	 * <p>
	 * The files are processed one after the other, and a file whose processing fails is reported
	 * on its own.
	 * </p>
	 *
	 * @throws TopLogicException
	 *         The {@link CommandErrors#notExecutable(ExecutableState) refusal}, if the rules reject
	 *         the current input.
	 */
	@Override
	public void uploadFiles(ReactContext context, List<BinaryData> files) {
		ExecutableState state = executability(resolveInput());
		if (!state.isExecutable()) {
			throw CommandErrors.notExecutable(state);
		}
		processFile(context, files, 0);
	}

	private void processFile(ReactContext context, List<BinaryData> files, int index) {
		if (index >= files.size()) {
			return;
		}
		BinaryData file = files.get(index);
		try {
			// onComplete advances to the next file once this file's chain settles (success or
			// abort), so a suspended <confirm> dialog resolves before the next file is processed.
			ViewActionChain.run(context, _uploadCommand.getActions(),
				_uploadCommand.getArguments().arguments(file, inputValues()), _uploadCommand.getToneAfterActions(),
				result -> processFile(context, files, index + 1), null);
		} catch (RuntimeException ex) {
			CommandErrors.log(ex, "Upload of '" + file.getName() + "'", ViewUploadCommandModel.class);

			// The summary names the file that could not be uploaded, the reason the chain gave is
			// listed below it as detail.
			CommandErrors.show(context.getErrorSink(),
				CommandErrors.error(I18NConstants.ERROR_UPLOAD_FAILED__FILE.fill(file.getName()), List.of(CommandErrors.message(ex))));

			processFile(context, files, index + 1);
		}
	}
}
