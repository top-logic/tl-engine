/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.util.Collections;

import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.basic.AbstractConstantControl;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ControlRenderer;

/**
 * Control that just renders a command to download a given {@link BinaryDataSource}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DownloadControl extends AbstractConstantControl implements IDownloadControl {
	
	private final BinaryDataSource _item;

	private boolean _downloadInline;
	
	private ControlRenderer<? super DownloadControl> _renderer = DownloadRenderer.INSTANCE;

	/**
	 * Creates a new {@link DownloadControl} for the given item.
	 * 
	 * @see WindowScope#deliverContent(BinaryDataSource, boolean)
	 * 
	 * @param item
	 *        the item for download.
	 */
	public DownloadControl(BinaryDataSource item) {
		super(Collections.<String, ControlCommand> singletonMap(DownloadCommand.INSTANCE.getID(),
			DownloadCommand.INSTANCE));
		_item = item;
	}

	@Override
	protected String getTypeCssClass() {
		return "cDownload";
	}

	@Override
	public boolean downloadInline() {
		return _downloadInline;
	}

	@Override
	public BinaryDataSource dataItem() {
		return _item;
	}

	@Override
	public ResKey noValueKey() {
		return I18NConstants.NO_DOWNLOAD_AVAILABLE;
	}

	/**
	 * Whether the download should be offered inline.
	 * 
	 * @see WindowScope#deliverContent(BinaryDataSource, boolean)
	 * 
	 */
	public void setDownloadInline(boolean inline) {
		this._downloadInline = inline;
	}

	/**
	 * Sets the renderer for this control.
	 * 
	 * @param renderer
	 *        must not be <code>null</code>
	 */
	public void setRenderer(ControlRenderer<? super DownloadControl> renderer) {
		this._renderer = renderer;
		requestRepaint();
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		_renderer.write(context, out, this);
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		_renderer.appendControlCSSClasses(out, this);
	}

}
