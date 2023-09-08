/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ButtonUIModel;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.form.FormConstants;

/**
 * Renderer that visually displays a {@link ButtonControl} as as clickable image followed by
 * a linked plain text.
 *
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class ImageLinkButtonRenderer extends AbstractButtonRenderer<ImageLinkButtonRenderer.Config> {

	public static final AbstractButtonRenderer<?> INSTANCE = newImageLinkButtonRenderer(true);

	public static final AbstractButtonRenderer<?> NO_REASON_INSTANCE = newImageLinkButtonRenderer(false);

	private static ImageLinkButtonRenderer newImageLinkButtonRenderer(boolean someExecStateInTooltip) {
		Config config = TypedConfiguration.newConfigItem(ImageLinkButtonRenderer.Config.class);
		config.setExecStateInTooltip(someExecStateInTooltip);
		return TypedConfigUtil.createInstance(config);
	}

	/**
	 * Typed configuration interface definition for {@link ImageLinkButtonRenderer}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends AbstractButtonRenderer.Config<ImageLinkButtonRenderer> {
		// configuration interface definition
	}

	/**
	 * Create a {@link ImageLinkButtonRenderer}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public ImageLinkButtonRenderer(InstantiationContext context, Config config) {
		super(context, config);
	}

    @Override
	protected String getTypeCssClass(AbstractButtonControl<?> button) {
		return "cImageLinkButton";
	}

	@Override
	protected void writeButton(DisplayContext context, TagWriter out, AbstractButtonControl<?> button) throws IOException {
		String controlTag = controlTag();
		out.beginBeginTag(controlTag);
		writeControlTagAttributes(context, out, button);

		this.writeTooltip(context, button, out);
		boolean disabled = button.isDisabled();
		if (!disabled) {
			writeOnClickAttribute(context, out, button);
			ButtonUIModel model = button.getModel();
			writeAccessKeyAttribute(out, model);
		}

		out.endBeginTag();
		{
			writeImage(context, out, button);
			writeLink(context, out, button);
		}
		out.endTag(controlTag);
    }

	protected String controlTag() {
		return SPAN;
	}

	protected String enabledClass() {
		return "cmdLink";
	}

	protected String disabledClass() {
		return "cmdLinkDisabled";
	}

	protected void writeImage(DisplayContext context, TagWriter out, AbstractButtonControl<?> button) throws IOException {
		boolean isDisabled = button.isDisabled();
		if (!hasImage(button)) {
			return;
		}

		XMLTag tag = lookUpImage(button, isDisabled).toButton();
		tag.beginBeginTag(context, out);
		out.beginCssClasses();
		out.append(FormConstants.INPUT_IMAGE_CSS_CLASS);
		button.writeCssClassesContent(out);
		if (isDisabled) {
			out.append(FormConstants.DISABLED_CSS_CLASS);
		}
		out.endCssClasses();
		out.writeAttribute(ID_ATTR, getButtonId(button));
		out.writeAttribute(NAME_ATTR, button.getID());

		if (button.hasTabindex()) {
			out.writeAttribute(TABINDEX_ATTR, button.getTabindex());
		}

		out.writeAttribute(ALT_ATTR, button.getAltText());
		/*
		 * Older versions of the Internet Explorer display the text in the "alt" attribute if the
		 * "title" attribute is not set. As there is already a JavaScript tooltip in many cases,
		 * these versions of the IE display two tooltips. To prevent this, we write an empty "title"
		 * attribute. This tells the IE to display no tooltip.
		 */
		out.writeAttribute(TITLE_ATTR, "");

		tag.endEmptyTag(context, out);
	}

	protected void writeLink(DisplayContext context, TagWriter out, AbstractButtonControl<?> button) throws IOException {
		String label = button.getLabel();
		if (button.isDisabled()) {
			out.beginBeginTag(controlTag());
			out.writeAttribute(ID_ATTR, getLinkId(button));
			writeCssClassesForLabel(out, button);
			out.endBeginTag();
			{
				out.writeText(label);
			}
			out.endTag(controlTag());
		} else {
			out.beginBeginTag(ANCHOR);
			out.writeAttribute(HREF_ATTR, HREF_EMPTY_LINK);
			out.writeAttribute(ID_ATTR, getLinkId(button));
			writeCssClassesForLabel(out, button);
			out.endBeginTag();
			{
				out.writeText(label);
			}
			out.endTag(ANCHOR);
		}
	}

	private void writeCssClassesForLabel(TagWriter out, AbstractButtonControl<?> button) throws IOException {
		out.beginCssClasses();
		writeCssClassesForLableContent(out, button);
		out.endCssClasses();
	}

	private void writeCssClassesForLableContent(Appendable out, AbstractButtonControl<?> button)
			throws IOException {
		button.writeCssClassesContent(out);
		out.append(button.isDisabled() ? disabledClass() : enabledClass());
	}

	private String getButtonId(AbstractButtonControl<?> button) {
		return button.getID() + "-input";
	}

	private String getLinkId(AbstractButtonControl<?> button) {
		return button.getID() + "-label";
	}

    @Override
	public void handleLabelPropertyChange(AbstractButtonControl<?> button, String aNewLabel) {
		//
	}

}