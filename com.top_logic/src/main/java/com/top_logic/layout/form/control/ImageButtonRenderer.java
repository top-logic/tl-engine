/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.PropertyUpdate;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ButtonUIModel;
import com.top_logic.layout.basic.ConstantDisplayValue;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.form.FormConstants;

/**
 * Renderer that visually displays a {@link ButtonControl} as clickable image.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ImageButtonRenderer extends AbstractButtonRenderer<ImageButtonRenderer.Config> {

	public static final AbstractButtonRenderer<?> INSTANCE = newImageButtonRenderer(true);

	public static final AbstractButtonRenderer<?> NO_REASON_INSTANCE = newImageButtonRenderer(false);

	private static ImageButtonRenderer newImageButtonRenderer(boolean execStateInTooltip) {
		Config config = TypedConfiguration.newConfigItem(Config.class);
		config.setExecStateInTooltip(execStateInTooltip);
		return TypedConfigUtil.createInstance(config);
	}

	/**
	 * {@link ImageButtonRenderer} that uses a custom CSS class to not inherit default button
	 * styles.
	 * 
	 * @param cssClass
	 *        The CSS class to use, see {@link #getTypeCssClass(AbstractButtonControl)}.
	 */
	public static ImageButtonRenderer newSystemButtonRenderer(String cssClass) {
		Config config = TypedConfiguration.newConfigItem(Config.class);
		config.setTypeCSSClass(cssClass);
		return TypedConfigUtil.createInstance(config);
	}

	/**
	 * Typed configuration interface definition for {@link ImageButtonRenderer}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends AbstractButtonRenderer.Config<ImageButtonRenderer> {

		/**
		 * CSS class to set on buttons rendered by {@link IButtonRenderer}.
		 * 
		 * @see ImageButtonRenderer#getTypeCssClass(AbstractButtonControl)
		 */
		@StringDefault("cImageButton")
		@Nullable
		String getTypeCSSClass();

		/**
		 * Setter for {@link #getTypeCSSClass()}.
		 */
		void setTypeCSSClass(String cssClass);
	}

	/**
	 * Create a {@link ImageButtonRenderer}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public ImageButtonRenderer(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected String getTypeCssClass(AbstractButtonControl<?> button) {
		return getConfig().getTypeCSSClass();
	}

    @Override
	protected void writeButton(DisplayContext context, TagWriter out, AbstractButtonControl<?> button) throws IOException {
		out.beginBeginTag(SPAN);
		writeControlTagAttributes(context, out, button);

		this.writeTooltip(context, button, out);
		
		out.endBeginTag();
		{
			boolean isDisabled = button.isDisabled();
			XMLTag tag = lookUpImage(button, isDisabled).toButton();
			tag.beginBeginTag(context, out);

			ButtonUIModel model = button.getModel();
			writeAccessKeyAttribute(out, model);

			out.beginCssClasses();
			out.append(FormConstants.INPUT_IMAGE_CSS_CLASS);
			if (isDisabled) {
				out.append(FormConstants.DISABLED_CSS_CLASS);
			}
			out.endCssClasses();
			out.writeAttribute(ID_ATTR, getInputId(button));
			out.writeAttribute(NAME_ATTR, button.getID());

			if (isDisabled) {
				out.writeAttribute(DISABLED_ATTR, DISABLED_DISABLED_VALUE);
			} else {
				writeOnClickAttribute(context, out, button);

				if (button.hasTabindex()) {
					out.writeAttribute(TABINDEX_ATTR, button.getTabindex());
				}
			}
			out.writeAttribute(ALT_ATTR, context.getResources().getString(button.getAltText()));
			/*
			 * Older versions of the Internet Explorer display the text in the "alt" attribute if the "title" attribute is not set.
			 * As there is already a JavaScript tooltip in many cases, these versions of the IE display two tooltips.
			 * To prevent this, we write an empty "title" attribute. This tells the IE to display no tooltip.
			 */
			out.writeAttribute(TITLE_ATTR, "");

			tag.endEmptyTag(context, out);
		}
		out.endTag(SPAN);
	}


    private String getInputId(AbstractButtonControl<?> button) {
		return button.getID() + "-input";
	}

    @Override
	public void handleLabelPropertyChange(AbstractButtonControl<?> button, ResKey aNewLabel) {
    	// nothing to do since no label is written
    }

    @Override
	public void handleClassPropertyChange(AbstractButtonControl<?> button, String oldValue, String newValue) {
    	if (!button.isRepaintRequested()) {
    		if (StringServices.isEmpty(newValue)) {
    			newValue = StringServices.EMPTY_STRING;
    		}
    		addUpdate(button, new PropertyUpdate(getInputId(button), CLASS_PROPERTY, new ConstantDisplayValue(newValue)));
    	}
    }

    @Override
	protected ResKey getTooltip(AbstractButtonControl<?> aButtonControl) {
		return ResKey.text(ButtonRenderer.lookupTooltipLabelFallback(aButtonControl, false, execStateInTooltip));
    }

}
