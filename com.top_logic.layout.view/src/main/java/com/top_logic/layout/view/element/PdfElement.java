/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.SimpleBinaryDataValue;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.pdf.ReactPdfViewerControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;

/**
 * {@link UIElement} that displays a {@link BinaryData} PDF document via the
 * {@link ReactPdfViewerControl} ({@code TLPdfViewer}).
 *
 * <p>
 * The PDF is read from an input channel and kept in sync with that channel: when the channel value
 * changes, the displayed document is updated. This element is the declarative rendering target for
 * PDF content; a higher-level Office-document viewer can convert {@code .docx}/{@code .xlsx}/
 * {@code .pptx} to PDF and bind the result to the same channel.
 * </p>
 */
public class PdfElement implements UIElement {

	/**
	 * Configuration for {@link PdfElement}.
	 */
	@TagName("pdf")
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		@Override
		@ClassDefault(PdfElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Channel whose {@link BinaryData} value (a PDF document) is displayed.
		 */
		@Name(INPUT)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();
	}

	private final ChannelRef _inputRef;

	/**
	 * Creates a new {@link PdfElement} from configuration.
	 */
	@CalledByReflection
	public PdfElement(InstantiationContext context, Config config) {
		_inputRef = config.getInput();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		SimpleBinaryDataValue model = new SimpleBinaryDataValue(pdf(null));
		ReactPdfViewerControl control = new ReactPdfViewerControl(context, model);
		if (_inputRef != null) {
			ViewChannel channel = context.resolveChannel(_inputRef);
			model.setData(pdf(channel.get()));
			ChannelListener listener = (sender, oldValue, newValue) -> model.setData(pdf(newValue));
			channel.addListener(listener);
			control.addCleanupAction(() -> channel.removeListener(listener));
		}
		return control;
	}

	private static BinaryData pdf(Object value) {
		return value instanceof BinaryData ? (BinaryData) value : null;
	}

}
