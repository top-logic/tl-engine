/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.PropertyUpdate;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ButtonUIModel;
import com.top_logic.layout.basic.ConstantDisplayValue;
import com.top_logic.layout.basic.link.Link;

/**
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ButtonRenderer extends AbstractButtonRenderer<ButtonRenderer.Config> {

	private static final String CSS_CLASS_CMD_LINK  = "cmdLink";
	private static final String CSS_CLASS_CMD_LINK_DISABLED = "cmdLinkDisabled";
	private static final String CSS_CLASS_CMD_LINK_ACTIVE = "cmdLinkActive";
   
	public static final ButtonRenderer INSTANCE = newButtonRenderer(Config.EXEC_STATE_IN_TTOLTIP_DEFAULT);

	/**
	 * Typed configuration interface definition for {@link ButtonRenderer}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends AbstractButtonRenderer.Config<ButtonRenderer> {
		// configuration interface definition
	}

	public static ButtonRenderer newButtonRenderer(boolean someExecStateInTooltip) {
		Config config = TypedConfiguration.newConfigItem(ButtonRenderer.Config.class);
		config.setExecStateInTooltip(someExecStateInTooltip);
		return TypedConfigUtil.createInstance(config);
	}

	/**
	 * Create a {@link ButtonRenderer}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public ButtonRenderer(InstantiationContext context, Config config) {
		super(context, config);
	}

    /**
     * @see com.top_logic.layout.form.control.AbstractButtonRenderer#handleLabelPropertyChange(AbstractButtonControl, java.lang.String)
     */
    @Override
	public void handleLabelPropertyChange(AbstractButtonControl<?> aButton, String aNewLabel) {
        if (!aButton.isRepaintRequested()) {
			if (hasImage(aButton)) {
                addUpdate(aButton, new PropertyUpdate(aButton.getID(), TITLE_ATTR, new ConstantDisplayValue(getLabel(aButton))));
            }
        }
    }

	@Override
	protected String getTypeCssClass(AbstractButtonControl<?> button) {
		if (button.isActive()) {
			return CSS_CLASS_CMD_LINK_ACTIVE;
		} else if (button.isDisabled()) {
			return CSS_CLASS_CMD_LINK_DISABLED;
		} else {
			return CSS_CLASS_CMD_LINK;
		}
	}

    /**
     * @see com.top_logic.layout.form.control.AbstractButtonRenderer#writeButton(com.top_logic.layout.DisplayContext, com.top_logic.basic.xml.TagWriter, AbstractButtonControl)
     */
    @Override
	protected void writeButton(DisplayContext context, TagWriter out, AbstractButtonControl<?> button)
			throws IOException {
		boolean isDisabled = button.isDisabled();
		if (hasImage(button)) {
			AbstractButtonRenderer<?> imageRenderer;
			if (execStateInTooltip) {
				imageRenderer = ImageButtonRenderer.INSTANCE;
			} else {
				imageRenderer = ImageButtonRenderer.NO_REASON_INSTANCE;
			}
			imageRenderer.writeButton(context, out, button);
		} else {
			String theLabel = this.getLabel(button);
			if (isDisabled) {
				out.beginBeginTag(SPAN);
				writeTooltip(context, button, out);

				writeControlTagAttributes(context, out, button);
				out.endBeginTag();
				{
					out.writeText(theLabel);
				}
				out.endTag(SPAN);
			} else {
				out.beginBeginTag(ANCHOR);
				out.writeAttribute(HREF_ATTR, HREF_EMPTY_LINK);

				ButtonUIModel model = button.getModel();
				writeAccessKeyAttribute(out, model);

				writeControlTagAttributes(context, out, button);
				writeOnClickAttribute(context, out, button);

				writeTooltip(context, button, out);

				out.endBeginTag();
				{
					out.writeText(theLabel);
				}
				out.endTag(ANCHOR);
			}
        }
    }

    protected String getLabel(AbstractButtonControl<?> aButtonControl) {
        if (aButtonControl.isDisabled() && ! execStateInTooltip) {
			return aButtonControl.getLabel() + " (" + aButtonControl.getDisabledReason() + ")";
        }
        return aButtonControl.getLabel();
    }
    
	/**
	 * @see AbstractButtonRenderer#getTooltip(AbstractButtonControl)
	 */
	@Override
	protected String getTooltip(AbstractButtonControl<?> aButtonControl) {
		return lookupTooltipLabelFallback(aButtonControl, aButtonControl.getImage() == null, execStateInTooltip);
	}

	/**
	 * @param button
	 *        The button to render.
	 * @param labelRendered
	 *        Whether a label is already rendered.
	 * @param execStateInTooltip
	 *        Whether the state is considered for the tooltip.
	 * @return The tooltip of the button (with a disabled reason).
	 * 
	 * @see Link#getTooltip()
	 * @see AbstractButtonControl#getDisabledReason()
	 */
	public static final String lookupTooltipLabelFallback(Link button, boolean labelRendered,
			boolean execStateInTooltip) {
		if (execStateInTooltip && button.isDisabled() && (button instanceof AbstractButtonControl)) {
			return lookupDisabledTooltipLabelFallback((AbstractButtonControl<?>) button, labelRendered);
		}

		String tooltip = button.getTooltip();
		if (tooltip == null && !labelRendered) {
			return TagUtil.encodeXML(button.getLabel());
		}
		return tooltip;
	}

	private static final String lookupDisabledTooltipLabelFallback(AbstractButtonControl<?> button,
			boolean labelRendered) {
		String disabledReason = button.getDisabledReason();

		String tooltip = button.getTooltip();
		if (StringServices.isEmpty(tooltip)) {
			if (labelRendered) {
				return TagUtil.encodeXML(disabledReason);
			} else {
				tooltip = TagUtil.encodeXML(button.getLabel());
			}
		}
		if (StringServices.isEmpty(disabledReason)) {
			return tooltip;
		}
		return tooltip + " (" + TagUtil.encodeXML(disabledReason) + ")";
	}
    
    @Override
	public void handleClassPropertyChange(AbstractButtonControl<?> button, String oldValue, String newValue) {
    	if (!button.isRepaintRequested()) {
    		if (StringServices.isEmpty(newValue)) {
    			newValue = StringServices.EMPTY_STRING;
    		}
    		addUpdate(button, new PropertyUpdate(button.getID(), CLASS_PROPERTY, new ConstantDisplayValue(newValue)));
    	}
    }
 
}

