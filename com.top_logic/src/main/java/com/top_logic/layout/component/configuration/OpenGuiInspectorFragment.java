/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.configuration;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.View;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.link.Link;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.scripting.recorder.gui.inspector.GuiInspectorCommand;

/**
 * {@link HTMLFragment}, which renders the trigger, that opens the {@link GuiInspectorCommand}
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class OpenGuiInspectorFragment implements View, Link {
	private final Renderer<? super OpenGuiInspectorFragment> _renderer;

	private ThemeImage image;

	private ResKey labelKey;

	/**
	 * @param image
	 *        - path to theme image, which shall be rendered as trigger of
	 *        {@link GuiInspectorCommand}, may be null. In case the path is <code>null</code> the
	 *        label, referenced in {@link #labelKey} will be used as textual representation.
	 * @param labelKey
	 *        - key to label, which either defines tooltip label or in case of empty image path the
	 *        textual representation of {@link GuiInspectorCommand} trigger.
	 */
	public OpenGuiInspectorFragment(Renderer<? super OpenGuiInspectorFragment> renderer, ThemeImage image,
			ResKey labelKey) {
		_renderer = renderer;
		this.image = image;
		this.labelKey = labelKey;
	}

	@Override
	public void write(DisplayContext context, TagWriter out) throws IOException {
		_renderer.write(context, out, this);
	}
	
	@Override
	public void writeCssClassesContent(Appendable out) {
		// Ignore.
	}

	@Override
	public String getID() {
		return null;
	}

	@Override
	public String getOnclick() {
		return "INSPECT.openGuiInspectorAfterTargetClicked();";
	}

	@Override
	public ResKey getTooltip() {
		return getLabel();
	}

	@Override
	public ResKey getTooltipCaption() {
		return null;
	}

	@Override
	public boolean isDisabled() {
		return false;
	}

	@Override
	public ResKey getLabel() {
		return labelKey;
	}

	@Override
	public ResKey getAltText() {
		return getLabel();
	}

	@Override
	public ThemeImage getImage() {
		return image;
	}

	@Override
	public boolean isVisible() {
		return ScriptingRecorder.isEnabled();
	}
}