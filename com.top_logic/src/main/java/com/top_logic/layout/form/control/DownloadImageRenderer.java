/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DefaultControlRenderer;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.model.DataField;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.mig.html.DefaultResourceProvider;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.util.Resources;
import com.top_logic.util.TLMimeTypes;

/**
 * Renderer for an {@link IDownloadControl} which renders the name of the {@link BinaryDataSource}
 * for download and an image to trigger the download.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DownloadImageRenderer extends DefaultControlRenderer<IDownloadControl> {

	/** A {@link DownloadImageRenderer} instance with allowed downloads. */
	public static final DownloadImageRenderer INSTANCE = new DownloadImageRenderer(true);

	/**
	 * css class of the additional span rendered in download mode
	 */
	public static final String DOWNLOAD_CSS = "downloadControl";

	/** @see #isDownloadAllowed() */
	private boolean _downloadAllowed;

	/**
	 * Creates a {@link DownloadImageRenderer} with the possibility to not allow downloads at all.
	 */
	public DownloadImageRenderer(boolean downloadAllowed) {
		_downloadAllowed = downloadAllowed;
	}

	@Override
	protected String getControlTag(IDownloadControl control) {
		return SPAN;
	}

	@Override
	protected final void writeControlContents(DisplayContext context, TagWriter out, IDownloadControl control)
			throws IOException {
		writeDownloadContents(context, out, control);
	}

	/**
	 * Renders the given {@link IDownloadControl}
	 * 
	 * @see #writeControlContents(DisplayContext, TagWriter, IDownloadControl)
	 */
	protected void writeDownloadContents(DisplayContext context, TagWriter out, IDownloadControl control)
			throws IOException {
		BinaryDataSource dataItem = control.dataItem();

		if (dataItem != null) {
			out.beginBeginTag(SPAN);
			out.writeAttribute(CLASS_ATTR, FormConstants.FLEXIBLE_CSS_CLASS);
			out.endBeginTag();

			out.beginBeginTag(SPAN);
			out.writeAttribute(CLASS_ATTR, "content");
			OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out,
				ResKey.text(dataItem.getName()));
			out.endBeginTag();
			DownloadImageRenderer.writeImage(context, out, dataItem);
			out.writeText(dataItem.getName());
			out.endTag(SPAN);

			out.endTag(SPAN);

			// Download is possible as well in active as in disabled state
			out.beginBeginTag(SPAN);
			out.writeAttribute(CLASS_ATTR, FormConstants.FIXED_RIGHT_CSS_CLASS);
			out.endBeginTag();
			renderDownloadImage(context, out, control, false);
			out.endTag(SPAN);
		} else {
			out.writeText(Resources.getInstance().getString(control.noValueKey()));
			// No download possible as no value is present
			renderDownloadImage(context, out, control, true);
		}
	}

	/**
	 * Writes the type image for the given {@link BinaryDataSource}.
	 */
	public static void writeImage(DisplayContext context, TagWriter out, BinaryDataSource dataItem) throws IOException {
		ThemeImage typeImage = DefaultResourceProvider.getTypeImage(dataItem.getContentType(), Flavor.DEFAULT,
			TLMimeTypes.getInstance().getUnknownImage());

		XMLTag tag = typeImage.toIcon();
		tag.beginBeginTag(context, out);
		out.writeAttribute(CLASS_ATTR, FormConstants.TYPE_IMAGE_CSS_CLASS);
		out.writeAttribute(ALT_ATTR, StringServices.EMPTY_STRING);
		out.writeAttribute(TITLE_ATTR, StringServices.EMPTY_STRING); // Avoid popup for IE
		out.writeAttribute(BORDER_ATTR, 0);
		tag.endEmptyTag(context, out);
	}

	@Override
	public void appendControlCSSClasses(Appendable out, IDownloadControl control) throws IOException {
		super.appendControlCSSClasses(out, control);
		HTMLUtil.appendCSSClass(out, DownloadImageRenderer.DOWNLOAD_CSS);
	}

	/**
	 * Renders an active download image for the given {@link IDownloadControl}.
	 * 
	 * @see #renderDownloadImage(DisplayContext, TagWriter, IDownloadControl, boolean)
	 */
	public final void renderDownloadImage(DisplayContext context, TagWriter out, IDownloadControl control)
			throws IOException {
		renderDownloadImage(context, out, control, false);
	}

	/**
	 * Renders a download image for the given {@link IDownloadControl} if
	 * {@link #isDownloadAllowed()} is <code>true</code>.
	 * 
	 * @param disabled
	 *        Whether the button should be active.
	 */
	public void renderDownloadImage(DisplayContext context, TagWriter out, IDownloadControl control, boolean disabled)
			throws IOException {
		if (!isDownloadAllowed()) {
			return;
		}
		ButtonWriter buttonWriter = new ButtonWriter(control, com.top_logic.tool.export.Icons.DOWNLOAD,
			com.top_logic.tool.export.Icons.DOWNLOAD_DISABLED, DownloadCommand.INSTANCE);
		buttonWriter.setCss(FormConstants.DOWNLOAD_BUTTON_CSS_CLASS);
		if (disabled) {
			buttonWriter.writeDisabledButton(context, out);
		} else {
			buttonWriter.writeButton(context, out);
		}
	}

	/**
	 * If the download of the {@link DataField} is allowed.
	 * 
	 * <p>
	 * If <code>false</code> the download image will never be rendered, even though the field has a
	 * {@link BinaryDataSource} item as value. If <code>true</code> the download is basically
	 * possible and only depends on the value of the {@link BinaryDataSource} item.
	 * </p>
	 * 
	 * @return If downloads are basically allowed.
	 */
	public boolean isDownloadAllowed() {
		return (_downloadAllowed);
	}
}
