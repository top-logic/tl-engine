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
import com.top_logic.layout.view.channel.Inputs;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewAction} that hands the chain's current value - a file - to the user as a download.
 *
 * <p>
 * The value must be {@link BinaryData}, as a TL-Script function producing a file returns it - an
 * Excel workbook from {@code excelFile(...)}, a document from a template, the content of a file
 * attribute. The browser saves the file under its name. The chain continues with the file
 * unchanged, so further actions may store it as well.
 * </p>
 *
 * <p>
 * Example: a toolbar button exporting the selected tickets:
 * </p>
 *
 * <pre>
 * &lt;button&gt;
 *   &lt;action class="com.top_logic.layout.view.command.GenericViewCommand" input="selection"&gt;
 *     &lt;execute-script function="tickets -&gt; excelFile(name: 'tickets', content: [excelSheet(content: $tickets.map(t -&gt; [$t.get(`demo:Ticket#name`)]))])"/&gt;
 *     &lt;download/&gt;
 *   &lt;/action&gt;
 * &lt;/button&gt;
 * </pre>
 *
 * <p>
 * A chain that produces no file - its value is empty - ends with a message telling the user so,
 * instead of silently doing nothing.
 * </p>
 */
@InApp
public class DownloadAction implements ViewAction {

	/**
	 * Configuration for {@link DownloadAction}.
	 */
	@TagName("download")
	public interface Config extends PolymorphicConfiguration<DownloadAction>, Inputs {

		/** @see #getFileName() */
		String FILE_NAME = "file-name";

		@Override
		@ClassDefault(DownloadAction.class)
		Class<? extends DownloadAction> getImplementationClass();

		/**
		 * TL-Script function computing the name the file is saved under, instead of its own name.
		 *
		 * <p>
		 * Called with the {@link #getInputs() input} channel values as leading positional arguments
		 * (in declaration order), followed by the file as the last argument. The result is a text
		 * or an internationalized text, and includes the file extension. A result of {@code null}
		 * keeps the file's own name.
		 * </p>
		 */
		@Name(FILE_NAME)
		Expr getFileName();
	}

	private final ActionScript _fileName;

	/**
	 * Creates a new {@link DownloadAction} from configuration.
	 */
	@CalledByReflection
	public DownloadAction(InstantiationContext context, Config config) {
		this(config.getFileName() == null ? null : ActionScript.compile(config.getFileName(), config.getInputs()));
	}

	/**
	 * Creates a {@link DownloadAction}.
	 *
	 * @param fileName
	 *        Computes the name the file is saved under from the file, {@code null} to keep the
	 *        file's own name.
	 */
	public DownloadAction(ActionScript fileName) {
		_fileName = fileName;
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		if (input == null) {
			throw new TopLogicException(I18NConstants.ERROR_NOTHING_TO_DOWNLOAD);
		}
		if (!(input instanceof BinaryData file)) {
			throw new TopLogicException(
				I18NConstants.ERROR_NOT_A_FILE__VALUE.fill(MetaLabelProvider.INSTANCE.getLabel(input)));
		}

		BinaryData download = rename(context, file);
		SSEUpdateQueue queue = context.getSSEQueue();
		if (queue == null) {
			// A chain running headless has no browser to save the file in.
			Logger.info("No window to deliver the download '" + download.getName() + "' to.", DownloadAction.class);
		} else {
			queue.deliverDownload(download);
		}
		return input;
	}

	private BinaryData rename(ReactContext context, BinaryData file) {
		if (_fileName == null) {
			return file;
		}
		ResKey name = _fileName.message(context, file);
		if (name == null) {
			return file;
		}
		return new DefaultDataItem(Resources.getInstance().getString(name), file, file.getContentType());
	}

}
