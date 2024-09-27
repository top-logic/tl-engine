/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.TooltipChangedListener;
import com.top_logic.layout.form.model.AbstractFormMember;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;

/**
 * {@link AbstractFormMemberControl} that writes the tooltip of the form member
 * on a separate element (image, or text).
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class InfoControl extends AbstractFormMemberControl implements TooltipChangedListener {
	
	private ThemeImage image;

	protected InfoControl(FormMember model) {
		super(model, COMMANDS);
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

	public void setImage(ThemeImage image) {
		if (image != this.image) {
			this.image = image;
			requestRepaint();
		}
	}
	
	@Override
	protected String getTypeCssClass() {
		return "cInfo";
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		FormMember field = getModel();

		if (! field.isVisible()) {
	        out.beginBeginTag(SPAN);
			writeControlAttributes(context, out);
	        out.writeAttribute(STYLE_ATTR, "display: none;");
	        out.endBeginTag();
	        out.endTag(SPAN);

	        return;
		}


		ResKey tooltip = null;
		ResKey tooltipCaption = null;
		// TODO: Remove spurious cast by pulling up "tooltip" to FormMember.
		if (field instanceof AbstractFormMember) {
			tooltip = ((AbstractFormMember)field).getTooltip();
			tooltipCaption = ((AbstractFormMember)field).getTooltipCaption();
		}

		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		writeStyle(out);
		out.endBeginTag();
		if(! StringServices.isEmpty(tooltip)) {
			if (image != null) {
				writeImage(out, context, tooltip, tooltipCaption);
			} else {
				writeText(out, context, tooltip, tooltipCaption);
			}
		}
		out.endTag(SPAN);
	}

	private void writeImage(TagWriter out, DisplayContext context, ResKey tooltip, ResKey tooltipCaption)
			throws IOException {
		image.write(context, out, null, tooltip, tooltipCaption);
	}

	private void writeText(TagWriter out, DisplayContext context, ResKey tooltip, ResKey tooltipCaption)
			throws IOException {
		out.beginBeginTag(SPAN);
		OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, tooltip, tooltipCaption);
		out.endBeginTag();
		{
			out.writeText(context.getResources().getString(I18NConstants.INFO_TEXT));
		}
		out.endTag(SPAN);
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		out.append(FormConstants.IS_INFO_CSS_CLASS);
	}
	
	/**
	 * Returns a new {@link InfoControl} for the given {@link FormMember} with the given image.
	 */
	public static InfoControl createInfoControl(FormMember model, ThemeImage image) {
		InfoControl info = new InfoControl(model);
		if (image != null) {
			info.setImage(image);
		}
		return info;
	}

	@Override
	public Bubble handleCSSClassChange(Object sender, String oldValue, String newValue) {
		return repaintOnEvent(sender);
	}

	@Override
	public Bubble handleTooltipChanged(Object sender, ResKey oldValue, ResKey newValue) {
		return repaintOnEvent(sender);
	}

}
