/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.ElementReplacement;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.base.services.simpleajax.PropertyUpdate;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.exception.I18NException;
import com.top_logic.basic.html.SafeHTML;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayValue;
import com.top_logic.layout.basic.ButtonUIModel;
import com.top_logic.layout.basic.ConstantDisplayValue;
import com.top_logic.layout.basic.ThemeImage;

/**
 * Renderer that visually displays a {@link ButtonControl} as XHTML button
 * element with a plain text label.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultButtonRenderer extends AbstractButtonRenderer<DefaultButtonRenderer.Config> {

	public static final AbstractButtonRenderer<?> INSTANCE = newDefaultButtonRenderer(true);

	public static final AbstractButtonRenderer<?> NO_REASON_INSTANCE = newDefaultButtonRenderer(false);

	private static AbstractButtonRenderer<?> newDefaultButtonRenderer(boolean execStateInTooltip) {
		Config config = TypedConfiguration.newConfigItem(Config.class);
		config.setExecStateInTooltip(execStateInTooltip);
		return TypedConfigUtil.createInstance(config);
	}


	/**
	 * Typed configuration interface definition for {@link DefaultButtonRenderer}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends AbstractButtonRenderer.Config<DefaultButtonRenderer> {
		// configuration interface definition
	}

	/**
	 * Create a {@link DefaultButtonRenderer}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public DefaultButtonRenderer(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected String getTypeCssClass(AbstractButtonControl<?> button) {
		return "cDefaultButton";
	}

	@Override
	protected void writeButton(DisplayContext context, TagWriter out, AbstractButtonControl<?> button) throws IOException {
		// The additional SPAN tag is necessary to show the tooltip also in disabled case (tooltips
		// are implemented using the 'mouseover' event, but such an event is not triggered on an
		// element in disabled state).
		out.beginBeginTag(SPAN);
		writeControlTagAttributes(context, out, button);
		this.writeTooltip(context, button, out);
		out.endBeginTag();
		{
			out.beginBeginTag(BUTTON);
			out.writeAttribute(ID_ATTR, getButtonId(button));

			ButtonUIModel model = button.getModel();
			writeAccessKeyAttribute(out, model);

			out.writeAttribute(TYPE_ATTR, BUTTON_TYPE_VALUE);
			out.writeAttribute(CLASS_ATTR, button.getModel().getCssClasses());
			out.writeAttribute(NAME_ATTR, button.getID());
			
			if (button.isDisabled()) {
				// The underlying command cannot be executed in the current
				// context. Since it is not hidden, it is rendered as disabled
				// button.
				out.writeAttribute(DISABLED_ATTR, DISABLED_DISABLED_VALUE);
			} else {
				writeOnClickAttribute(context, out, button);
			}
			
			if (button.hasTabindex()) {
				out.writeAttribute(TABINDEX_ATTR, button.getTabindex());
			}
			
			out.endBeginTag();
			{
				writeLabel(out, button);
			}
			out.endTag(BUTTON);
		}
		out.endTag(SPAN);
	}

	protected void writeLabel(TagWriter out, AbstractButtonControl<?> button) throws IOException {
        out.beginBeginTag(SPAN);
        out.writeAttribute(ID_ATTR, getLabelId(button));
        out.endBeginTag();
        
        out.writeText(button.getLabel());

        out.endTag(SPAN);
    }
	
    @Override
	public void handleLabelPropertyChange(final AbstractButtonControl<?> button, String newLabel) {
		if (!button.isRepaintRequested()) {
			addUpdate(button, new ElementReplacement(getLabelId(button), new HTMLFragment() {
				@Override
				public void write(DisplayContext aContext, TagWriter aOut) throws IOException {
					writeLabel(aOut, button);
				}
			}));
		}
    }
	
	private String getLabelId(AbstractButtonControl<?> button) {
		return button.getID() + "_label";
	}

	private String getButtonId(AbstractButtonControl<?> button) {
		return button.getID() + "_btn";
	}

	@Override
	public void handleDisabledReasonChanged(AbstractButtonControl<?> button, ResKey oldReasonKey, ResKey newReasonKey) {
		if (!button.isRepaintRequested()) {
			updateTooltip(button);
		}
	}

	/**
	 * Adds an update which updates the displayed tooltip
	 */
	private void updateTooltip(AbstractButtonControl<?> button) {
		String tooltip = getTooltip(button);
		try {
			SafeHTML.getInstance().check(tooltip);
		} catch (I18NException ex) {
			tooltip = TagUtil.encodeXMLAttribute(tooltip);
		}
		final DisplayValue newTooltip = ConstantDisplayValue.valueOf(tooltip);
		final String targetID = button.getID();
		@SuppressWarnings("deprecation")
		final PropertyUpdate tooltipUpdate = new PropertyUpdate(targetID, TL_TOOLTIP_ATTR, newTooltip);
		addUpdate(button, tooltipUpdate);
	}

	@Override
	public void handleImagePropertyChange(AbstractButtonControl<?> control, int disabled_image_property, ThemeImage oldValue, ThemeImage newValue) {
		//  Ignored.
	}

    @Override
	public void handleClassPropertyChange(AbstractButtonControl<?> button, String oldValue, String newValue) {
    	if (!button.isRepaintRequested()) {
    		if (StringServices.isEmpty(newValue)) {
    			newValue = StringServices.EMPTY_STRING;
    		}
    		addUpdate(button, new PropertyUpdate(getButtonId(button), CLASS_PROPERTY, new ConstantDisplayValue(newValue)));
    	}
    }

}
