/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.TooltipChangedListener;
import com.top_logic.layout.form.model.AbstractFormMember;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;

/**
 * Writes the tooltip of a {@link FormMember} around the given inner view.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class TooltipControl extends AbstractFormMemberControl implements TooltipChangedListener {

	private HTMLFragment _content;

    /**
     * Creates a new instance of this class.
     */
	public TooltipControl(FormMember aModel, HTMLFragment aWrappedViev) {
		super(aModel, COMMANDS);
		_content = aWrappedViev;
    }

    /**
     * Creates a new instance of this class.
     */
    public TooltipControl(AbstractFormMemberControl aControl) {
        this(aControl.getModel(), aControl);
    }


    /**
	 * Gets the inner view of this tooltip control.
	 */
	public HTMLFragment getWrappedView() {
		return _content;
    }

	/**
	 * Setter for {@link #getWrappedView()}.
	 */
	public void setWrappedView(HTMLFragment view) {
		_content = view;
		requestRepaint();
	}

    @Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
        out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
        FormMember field = getModel();
        if (field instanceof AbstractFormMember && field.isVisible()) {
            String tooltip = ((AbstractFormMember)field).getTooltip();
            String tooltipCaption = ((AbstractFormMember)field).getTooltipCaption();
            if (!StringServices.isEmpty(tooltip)) {
				OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, tooltip, tooltipCaption);
            }
        }
        out.endBeginTag();
		getWrappedView().write(context, out);
        out.endTag(SPAN);
    }

	@Override
	protected void registerListener(FormMember member) {
		super.registerListener(member);
		member.addListener(FormMember.TOOLTIP_PROPERTY, this);
	}

	@Override
	protected void deregisterListener(FormMember member) {
		member.removeListener(FormMember.TOOLTIP_PROPERTY, this);
		super.deregisterListener(member);
	}

	@Override
	public Bubble handleCSSClassChange(Object sender, String oldValue, String newValue) {
		// Don't care about css. This control doesn't us it.
		return Bubble.BUBBLE;
	}

	@Override
	public Bubble handleTooltipChanged(Object sender, String oldValue, String newValue) {
		return repaintOnEvent(sender);
	}

}
