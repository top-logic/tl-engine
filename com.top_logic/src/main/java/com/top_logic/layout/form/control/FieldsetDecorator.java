/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;

import com.top_logic.basic.Configuration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.ContentDecorator;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.tag.FormGroupTag;
import com.top_logic.layout.form.tag.Icons;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.util.css.CssUtil;

/**
 * {@link ContentDecorator} that writes a {@link HTMLConstants#FIELDSET} for a
 * {@link FormGroupControl}.
 * 
 * TODO #18927: Remove FieldsetDecorator.
 * 
 * @deprecated Use {@link FormGroupTag}
 * 
 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
 */
@Deprecated
public class FieldsetDecorator implements ContentDecorator {

	/**
	 * CSS class on the surrounding table element.
	 */
	public static final String LAYOUT_CSS_CLASS = "fsdLayout";

	private static boolean imageFirst = "true".equals(Configuration.getConfiguration(FieldsetDecorator.class).getValue("imageFirst", "false"));

	public static final FieldsetDecorator INSTANCE = new FieldsetDecorator(null, DefaultFormMemberLabelRenderer.INSTANCE);
	
	private final String style;

	private final Renderer<? super FormGroup> labelRenderer;
	
	/**
	 * Creates a {@link FieldsetDecorator}.
	 * 
	 * @param style
	 *        the style attribute of the rendered fieldset.
	 * @param labelRenderer
	 *        the renderer to write the label of the represented
	 *        {@link FormGroup}.
	 */
	public FieldsetDecorator(String style, Renderer<? super FormGroup> labelRenderer) {
		this.style = style;
		this.labelRenderer = labelRenderer;
	}

	/**
	 * Custom style attribute of the generated {@link HTMLConstants#FIELDSET}
	 * element.
	 */
	public String getStyle() {
		return style;
	}
	
	/**
	 * The {@link Renderer} that creates the <code>fieldset</code> legend.
	 */
	public Renderer<? super FormGroup> getLabelRenderer() {
		return labelRenderer;
	}

	@Override
	public void startDecoration(DisplayContext context, TagWriter writer, Object value) throws IOException {
		FormGroupControl control = (FormGroupControl) value;
		FormGroup member = control.getFormGroup();

		beginBeginFieldSet(writer);
		boolean collapsed = member.isCollapsed();
		writer.writeAttribute(CLASS_ATTR, getFieldsetClass(collapsed));
		writer.writeAttribute(STYLE_ATTR, style);

		writer.endBeginTag();
		{
			writer.beginBeginTag(LEGEND);
			ResKey tooltip = member.getTooltip();
			ResKey tooltipCaption = member.getTooltipCaption();

			if (tooltip != null) {
				OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, writer, tooltip,
					tooltipCaption);
			}
			
			writer.endBeginTag();
			{
				if(!imageFirst){
					labelRenderer.write(context, writer, member);
				}
				
				if (control.isCollapsible()) {
					XMLTag tag = getCollapseImage(collapsed).toButton();

					tag.beginBeginTag(context, writer);
					CssUtil.writeCombinedCssClasses(writer,
						FormConstants.INPUT_IMAGE_CSS_CLASS,
						FormConstants.TOGGLE_BUTTON_CSS_CLASS);
					HTMLUtil.writeImageTooltip(context, writer, getAltText(collapsed, member.getLabel()));
					control.writeOnClickToggle(writer);
					tag.endEmptyTag(context, writer);
				}
				
				if( imageFirst){
					labelRenderer.write(context, writer, member);
				}
			}
			writer.endTag(LEGEND);
		}
	}

	private ThemeImage getCollapseImage(boolean collapsed) {
		return collapsed ? Icons.GROUP_COLLAPSED : Icons.GROUP_EXPANDED;
	}

	private ResKey getAltText(boolean collapsed, ResKey label) {
		return collapsed ? I18NConstants.FORM_GROUP_COLLAPSED__LABEL.fill(label)
			: I18NConstants.FORM_GROUP_EXPANDED__LABEL.fill(label);
	}

	private String getFieldsetClass(boolean collapsed) {
		return collapsed ? FormConstants.COLLAPSED_CLASS : FormConstants.EXPANDED_CLASS;
	}

	@Override
	public void endDecoration(DisplayContext context, TagWriter writer, Object value) throws IOException {
		endFieldSet(writer);
	}

	public static void beginBeginFieldSet(TagWriter out) throws IOException {
		// Note: A fieldset must be surrounded with a table to ensure that table
		// contents does not overlap with the fieldset border.
		out.beginBeginTag(TABLE);
		out.writeAttribute(CLASS_ATTR, LAYOUT_CSS_CLASS);
		out.endBeginTag();
		out.beginTag(TR);
		out.beginTag(TD);
		out.beginBeginTag(FIELDSET);
	}

	public static void endFieldSet(TagWriter out) throws IOException {
		out.endTag(FIELDSET);
		out.endTag(TD);
		out.endTag(TR);
		out.endTag(TABLE);
	}

}