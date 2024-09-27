/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.AbstractControlRenderer;
import com.top_logic.layout.basic.ButtonUIModel;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;

/**
 * Base class for all view/renderer classes for {@link AbstractButtonControl}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractButtonRenderer<C extends AbstractButtonRenderer.Config<?>>
		extends AbstractControlRenderer<AbstractButtonControl<?>>
		implements ConfiguredInstance<C>, IButtonRenderer {

    private static final String XML_ATTRIBUTE_EXEC_STATE_IN_TOOLTIP = "execStateInTooltip";

	public interface Config<I extends AbstractButtonRenderer<?>> extends PolymorphicConfiguration<I> {

		/** Default value of {@link #getExecStateInTooltip()} when nothing is configured. */
		boolean EXEC_STATE_IN_TTOLTIP_DEFAULT = true;

		@Name(XML_ATTRIBUTE_EXEC_STATE_IN_TOOLTIP)
		@BooleanDefault(EXEC_STATE_IN_TTOLTIP_DEFAULT)
		boolean getExecStateInTooltip();

		/**
		 * Setter for {@link #getExecStateInTooltip()}.
		 */
		void setExecStateInTooltip(boolean value);

	}

	/**
     * Indicates whether the title / tooltip should be decorated with executability state
     * reason if not executable.
     */
    protected final boolean execStateInTooltip;

	private final C _config;

	public AbstractButtonRenderer(InstantiationContext context, C config) {
		_config = config;
		this.execStateInTooltip = config.getExecStateInTooltip();
    }

	@Override
	public C getConfig() {
		return _config;
	}
    
    protected void writeOnClickAttribute(DisplayContext context, TagWriter out, AbstractButtonControl<?> control) throws IOException {
		out.beginAttribute(ONCLICK_ATTR);
		control.writeOnClickContent(context, out);
		out.endAttribute();
    }

    /**
     * Overridden to provide a type-safe write method for sub-classes.
     *
     * <p>
     * Forwards to {@link #writeButton(DisplayContext, TagWriter, AbstractButtonControl)}
     * </p>
     *
     * @see Renderer#write(DisplayContext, TagWriter, Object)
     */
    @Override
	public final void write(DisplayContext context, TagWriter out, AbstractButtonControl<?> value) throws IOException {
		writeButton(context, out, value);
    }

	@Override
	public void appendControlCSSClasses(Appendable out, AbstractButtonControl<?> self) throws IOException {
		super.appendControlCSSClasses(out, self);
		out.append(getTypeCssClass(self));
	}

	/**
	 * The single CSS class that defines the client-side structure of this button.
	 * 
	 * <p>
	 * When overriding this method, super <b>must not</b> be called. Adding an additional class must
	 * be written by overriding {@link #appendControlCSSClasses(Appendable, AbstractButtonControl)}
	 * </p>
	 * 
	 * @param button
	 *        The button currently rendered.
	 * 
	 * @return A single CSS class or <code>null</code>. <code>null</code> means no CSS class at all.
	 */
	protected abstract String getTypeCssClass(AbstractButtonControl<?> button);

    /**
	 * Writes the enabled state of the given {@link AbstractButtonControl} to the given writer.
	 * 
	 * <p>
	 * Note: The public API {@link #write(DisplayContext, TagWriter, AbstractButtonControl)}
	 * dispatches to different renderer methods for enabled and disabled state. Therefore, this
	 * method (even if fully typed) must not be called directly from the outside.
	 * </p>
	 */
    protected abstract void writeButton(DisplayContext context, TagWriter out, AbstractButtonControl<?> button) throws IOException;
    
	/**
	 * Return the tooltip that should be displayed for that {@link AbstractButtonControl}
	 */
	protected ResKey getTooltip(AbstractButtonControl<?> aButtonControl) {
        // overwrite tooltip with reason for non-executability
        if (aButtonControl.isDisabled() && execStateInTooltip) {
            return aButtonControl.getDisabledReason();
        }
        return aButtonControl.getTooltip();
    }
    
    /**
     * Write the JavaScript used to display the tooltip
     */
	protected final void writeTooltip(DisplayContext context, AbstractButtonControl<?> button, TagWriter out)
			throws IOException {
		ResKey tooltip = this.getTooltip(button);
		if (!StringServices.isEmpty(tooltip)) {
			OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, tooltip,
				button.getTooltipCaption());
        }
    }

	protected final void writeAccessKeyAttribute(TagWriter out, ButtonUIModel model) throws IOException {
		char accessKey = model.getAccessKey();
		if (accessKey != 0) {
			out.writeAttribute(ACCESSKEY_ATTR, accessKey);
		}
	}

	/**
	 * Looks up the current image that is displayed for the given {@link ButtonControl}.
	 * 
	 * @param isDisabled
	 *        Same as {@link ButtonControl#isDisabled()}. Passed as parameter for efficiency
	 *        reasons, because it is computed also in the calling context. The computation may be
	 *        non-trivial, because it causes security checks.
	 */
	protected ThemeImage lookUpImage(AbstractButtonControl<?> button, boolean isDisabled) {
		if (!hasImage(button)) {
			return getMissingIcon(isDisabled);
		}
		if (button.isActive() && button.hasActiveImage()) {
			return button.getActiveImage();
		} else if (isDisabled && button.hasDisabledImage()) {
			return button.getDisabledImage();
		} else {
			return button.getImage();
		}
	}

	/**
	 * Whether the given button has a custom icon.
	 */
	protected boolean hasImage(AbstractButtonControl<?> button) {
		return button.getImage() != null;
	}

	protected ThemeImage getMissingIcon(boolean isDisabled) {
		return isDisabled ? Icons.BUTTON_MISSING_DISABLED : Icons.BUTTON_MISSING;
	}
}
