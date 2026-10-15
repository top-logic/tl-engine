/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.gui.layout.upload.DefaultDataItem;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.view.channel.NamedInputs;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.ui.ScriptContextVariables;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewAction} that hands a file to the user as a download: the innermost result of the
 * actions it is nested in, or the argument selected with {@code value}.
 *
 * <p>
 * The value must be {@link BinaryData}, as a TL-Script function producing a file returns it - an
 * Excel workbook from {@code excelFile(...)}, a document from a template, the content of a file
 * attribute. The browser saves the file under its name.
 * </p>
 *
 * <p>
 * Example: a toolbar button exporting the selected tickets:
 * </p>
 *
 * <pre>
 * &lt;button&gt;
 *   &lt;action class="com.top_logic.layout.view.command.GenericViewCommand" inputs="selection as tickets"&gt;
 *     &lt;execute-script function="excelFile(name: 'tickets', content: [excelSheet(content: $tickets.map(t -&gt; [$t.get(`demo:Ticket#name`)]))])"&gt;
 *       &lt;download/&gt;
 *     &lt;/execute-script&gt;
 *   &lt;/action&gt;
 * &lt;/button&gt;
 * </pre>
 *
 * <p>
 * A chain that produces no file - the value is empty - ends with a message telling the user so,
 * instead of silently doing nothing.
 * </p>
 */
@InApp
public class DownloadAction extends ArgumentsViewAction {

	/**
	 * Configuration for {@link DownloadAction}.
	 */
	@TagName("download")
	public interface Config extends PolymorphicConfiguration<DownloadAction>, NamedInputs, WithValueArgument {

		/** @see #getFileName() */
		String FILE_NAME = "file-name";

		@Override
		@ClassDefault(DownloadAction.class)
		Class<? extends DownloadAction> getImplementationClass();

		/**
		 * TL-Script computing the name the file is saved under, instead of its own name.
		 *
		 * <p>
		 * Uses the arguments of the chain and the values of the {@link #getInputs() inputs} as
		 * variables named after them, without declaring them; the file is the argument this action
		 * processes. The result is a text
		 * or an internationalized text, and includes the file extension. A result of {@code null}
		 * keeps the file's own name.
		 * </p>
		 */
		@Name(FILE_NAME)
		@ScriptContextVariables(ActionScriptVariables.class)
		Expr getFileName();
	}

	private final ActionScript _fileName;

	/**
	 * Creates a new {@link DownloadAction} from configuration.
	 */
	@CalledByReflection
	public DownloadAction(InstantiationContext context, Config config) {
		this(config.getFileName() == null ? null
			: ActionScript.compile(config.getFileName(), ActionScope.of(context), config.getInputs()));
	}

	/**
	 * Creates a {@link DownloadAction}.
	 *
	 * @param fileName
	 *        Computes the name the file is saved under, {@code null} to keep the file's own name.
	 */
	public DownloadAction(ActionScript fileName) {
		_fileName = fileName;
	}

	@Override
	public void execute(ReactContext context, ActionArguments arguments, Continuation continuation) {
		Object input = arguments.value();
		if (input == null) {
			throw new TopLogicException(I18NConstants.ERROR_NOTHING_TO_DOWNLOAD);
		}
		if (!(input instanceof BinaryData file)) {
			throw new TopLogicException(
				I18NConstants.ERROR_NOT_A_FILE__VALUE.fill(MetaLabelProvider.INSTANCE.getLabel(input)));
		}

		BinaryData download = rename(context, arguments, file);
		SSEUpdateQueue queue = context.getSSEQueue();
		if (queue == null) {
			// A chain running headless has no browser to save the file in.
			Logger.info("No window to deliver the download '" + download.getName() + "' to.", DownloadAction.class);
		} else {
			queue.deliverDownload(download);
		}
		continuation.resume(input);
	}

	private BinaryData rename(ReactContext context, ActionArguments arguments, BinaryData file) {
		if (_fileName == null) {
			return file;
		}
		ResKey name = _fileName.message(context, arguments);
		if (name == null) {
			return file;
		}
		return new DefaultDataItem(Resources.getInstance().getString(name), file, file.getContentType());
	}

}
