/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.AbstractCssClassUpdate;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ButtonUIModel;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.mig.html.HTMLUtil;

/**
 * Renderer that visually displays a {@link ButtonControl} as linked plain text.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LinkButtonRenderer extends AbstractButtonRenderer<LinkButtonRenderer.Config> {

	private static final String CSS_CLASS_CMD_LINK = "cmdLink";
	private static final String CSS_CLASS_CMD_LINK_DISABLED = "cmdLinkDisabled";

	public static final AbstractButtonRenderer<?> INSTANCE = newLinkButtonRenderer(true);

	public static final AbstractButtonRenderer<?> NO_REASON_INSTANCE = newLinkButtonRenderer(false);

	private static LinkButtonRenderer newLinkButtonRenderer(boolean someExecStateInTooltip) {
		Config config = TypedConfiguration.newConfigItem(LinkButtonRenderer.Config.class);
		config.setExecStateInTooltip(someExecStateInTooltip);
		return TypedConfigUtil.createInstance(config);
	}

	/**
	 * Typed configuration interface definition for {@link ButtonRenderer}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends AbstractButtonRenderer.Config<LinkButtonRenderer> {
		// configuration interface definition
	}

	/**
	 * Create a {@link LinkButtonRenderer}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public LinkButtonRenderer(InstantiationContext context, Config config) {
		super(context, config);
	}
	
	@Override
	protected void writeButton(DisplayContext context, TagWriter out, AbstractButtonControl<?> button) throws IOException {
		if (button.isDisabled()) {
			writeDisabled(context, out, button);
		} else {
			writeEnabled(context, out, button);
		}
	}

	@Override
	protected String getTypeCssClass(AbstractButtonControl<?> button) {
		return "cLinkButton";
	}

	/**
	 * Writes the enabled view of the button.
	 */
	protected void writeEnabled(DisplayContext context, TagWriter out, AbstractButtonControl<?> button) throws IOException {
		out.beginBeginTag(ANCHOR);
		out.writeAttribute(HREF_ATTR, HREF_EMPTY_LINK);

		ButtonUIModel model = button.getModel();
		writeAccessKeyAttribute(out, model);

		String label = context.getResources().getString(button.getLabel());
		writeTooltip(context, button, out);

		writeControlTagAttributes(context, out, button);
		writeOnClickAttribute(context, out, button);
		out.endBeginTag();
		writeLabel(out, label);
		out.endTag(ANCHOR);
	}

	/**
	 * Writes the disabled view of the button.
	 */
	protected void writeDisabled(DisplayContext context, TagWriter out, AbstractButtonControl<?> button) throws IOException {
		out.beginBeginTag(SPAN);

		this.writeTooltip(context, button, out);

		writeControlTagAttributes(context, out, button);
		out.endBeginTag();
		writeLabel(out, context.getResources().getString(button.getLabel()));
		out.endTag(SPAN);
	}

	/**
	 * Writes the given laben to the output-writer.
	 */
	protected void writeLabel(TagWriter out, String label) throws IOException {
		out.writeText(label);
	}

	@Override
	public void appendControlCSSClasses(Appendable out, AbstractButtonControl<?> self) throws IOException {
		super.appendControlCSSClasses(out, self);
		HTMLUtil.appendCSSClass(out, self.isDisabled() ? CSS_CLASS_CMD_LINK_DISABLED : CSS_CLASS_CMD_LINK);
	}
	
	/**
	 * Does nothing as no reason is written to the client.
	 * 
	 * @see AbstractButtonRenderer#handleDisabledReasonChanged(AbstractButtonControl,
	 *      ResKey, ResKey)
	 */
	@Override
	public void handleDisabledReasonChanged(AbstractButtonControl<?> button, ResKey oldReasonKey, ResKey newReasonKey) {
		// Ignored as we write no reason.
	}

	@Override
	public void handleImagePropertyChange(AbstractButtonControl<?> control, int property, ThemeImage oldValue, ThemeImage newValue) {
		// Ignored as we write no image.
	}
	
	@Override
	public void handleClassPropertyChange(final AbstractButtonControl<?> button, String oldValue, String newValue) {
		if (!button.isRepaintRequested()) {
			addUpdate(button, new AbstractCssClassUpdate(button.getID()) {
				@Override
				protected void writeCssClassContent(DisplayContext context, Appendable out) throws IOException {
					appendControlCSSClasses(out, button);
				}
			});
		}
	}

}
