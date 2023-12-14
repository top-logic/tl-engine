/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.layoutRenderer;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.Control;
import com.top_logic.layout.DefaultControlRenderer;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.structure.DialogWindowControl;
import com.top_logic.layout.structure.PopupDialogControl;
import com.top_logic.layout.structure.PopupDialogModel;
import com.top_logic.layout.toolbar.ToolbarControl;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.util.Resources;
import com.top_logic.util.css.CssUtil;

/**
 * This class is used to render the content of a {@link PopupDialogControl}.
 * 
 * @author     <a href="mailto:STS@top-logic.com">STS</a>
 */
public class PopupDialogRenderer extends DefaultControlRenderer<PopupDialogControl> {

	/**
	 * Singleton {@link PopupDialogRenderer} instance.
	 */
	public static final PopupDialogRenderer DEFAULT_INSTANCE = new PopupDialogRenderer();

	/**
	 * CSS class for a button group in the toolbar area.
	 */
	public static final String TOOLBAR_GROUP_CSS_CLASS = "dlgToolbarGroup";

	/**
	 * Creates a {@link PopupDialogRenderer}.
	 */
	protected PopupDialogRenderer() {
		// Could be sub-classed.
	}
	
	@Override
	public void appendControlCSSClasses(Appendable out, PopupDialogControl control) throws IOException {
		super.appendControlCSSClasses(out, control);
		HTMLUtil.appendCSSClass(out, "pdlgWindow");
		HTMLUtil.appendCSSClass(out, control.getModel().getCssClass());
	}

	@Override
	protected void writeControlTagAttributes(DisplayContext context, TagWriter out, PopupDialogControl control)
			throws IOException {
		super.writeControlTagAttributes(context, out, control);

		PopupDialogControl popupDialog = control;
		PopupDialogModel dialogModel = popupDialog.getPopupDialogModel();
				
		writeControlStyle(out, dialogModel);

		out.writeAttribute(TL_KEY_SELECTORS, popupDialog.getKeySelectors());
	}

	private void writeControlStyle(TagWriter out, PopupDialogModel dialogModel) throws IOException {
		out.beginAttribute(STYLE_ATTR);
		if(!dialogModel.hasBorder()) {
			writeDialogWidthStyle(out, dialogModel);
			writeDialogHeightStyle(out, dialogModel);
		}
		else {
			writeDialogWidthStyle(out, dialogModel);
			writeDialogHeightStyle(out, dialogModel);
			out.append("border-style: solid; ");
			out.append("border-width: ");
			out.writeInt(dialogModel.getBorderWidth());
			out.append("px; ");
			if (!dialogModel.getBorderColor().equals("")) {
				out.append("border-color: ");
				out.append(dialogModel.getBorderColor());
				out.append(";");
			}
		}
		out.endAttribute();
	}
	
	private void writeDialogWidthStyle(TagWriter out, PopupDialogModel dialogModel) throws IOException {
		if (dialogModel.hasFixedWidth()) {
			float width = dialogModel.getLayoutData().getWidth();
			DisplayUnit widthUnit = dialogModel.getLayoutData().getWidthUnit();

			out.append("width: ");
			out.writeFloat(width);
			out.append(widthUnit.toString());
			out.append(";");
		}
	}

	private void writeDialogHeightStyle(TagWriter out, PopupDialogModel dialogModel) throws IOException {
		if (dialogModel.hasFixedHeight()) {
			float height = dialogModel.getLayoutData().getHeight();
			DisplayUnit heightUnit = dialogModel.getLayoutData().getHeightUnit();
			if (dialogModel.hasTitleBar()) {
				int titleHeight = (int) ThemeFactory.getTheme().getValue(Icons.POPUP_TITLE_HEIGHT).getValue();
				height += titleHeight;
			}
			out.append("height: ");
			out.writeFloat(height);
			out.append(heightUnit.toString());
			out.append(";");
		}
	}

	private void writeContentHeightStyle(TagWriter out, PopupDialogModel dialogModel) throws IOException {
		if (dialogModel.hasFixedHeight()) {
			float height = dialogModel.getLayoutData().getHeight();
			DisplayUnit heightUnit = dialogModel.getLayoutData().getHeightUnit();

			out.append("height: ");
			out.writeFloat(height);
			out.append(heightUnit.toString());
			out.append(";");
		}
	}

	/**
	 * @see com.top_logic.layout.DefaultControlRenderer#writeControlContents(com.top_logic.layout.DisplayContext, com.top_logic.basic.xml.TagWriter, com.top_logic.layout.Control)
	 */
	@Override
	protected void writeControlContents(DisplayContext context, TagWriter out, PopupDialogControl control)
			throws IOException {
		PopupDialogControl popupDialog = control;
		PopupDialogModel dialogModel = popupDialog.getPopupDialogModel();
		
		// Lay out title bar, if present
		if(dialogModel.hasTitleBar()) {
			
			// Layout
			out.beginBeginTag(DIV);
			out.writeAttribute(ID_ATTR, popupDialog.getID() + PopupDialogControl.POPUP_DIALOG_TITLE_BAR_SUFFIX);
			out.writeAttribute(CLASS_ATTR, "pdlgTitleBar");
			out.endBeginTag();
			
			// Create close button
			out.beginBeginTag(SPAN);
			out.writeAttribute(CLASS_ATTR, "pdlgToolbar");
			out.endBeginTag();
			{
				DialogWindowControl.OPEN_GUI_INSPECTOR_FRAGMENT.write(context, out);

				ToolbarControl.writeToolbarContents(context, out,
					SPAN, TOOLBAR_GROUP_CSS_CLASS, dialogModel.getToolBar());

				out.beginBeginTag(SPAN);
				out.writeAttribute(CLASS_ATTR, TOOLBAR_GROUP_CSS_CLASS);
				out.endBeginTag();

				XMLTag tag = com.top_logic.layout.structure.Icons.CLOSE_DIALOG.toButton();
				tag.beginBeginTag(context, out);
				CssUtil.writeCombinedCssClasses(out, FormConstants.INPUT_IMAGE_CSS_CLASS, "dlgClose");
				out.writeAttribute(ALT_ATTR, Resources.getInstance().getString(I18NConstants.CLOSE_DIALOG));
				out.writeAttribute(TITLE_ATTR, "");
				OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out,
					Resources.getInstance().getString(I18NConstants.CLOSE_DIALOG));
				writeOnClick(out, popupDialog);
				tag.endEmptyTag(context, out);

				out.endTag(SPAN);
			}
			out.endTag(SPAN);
			
			// Write title
			{
				out.beginBeginTag(DIV);
				out.writeAttribute(ID_ATTR, popupDialog.getID() + PopupDialogControl.POPUP_DIALOG_TITLE_SUFFIX);
				out.writeAttribute(CLASS_ATTR, "pdlgTitle");
				out.endBeginTag();
				dialogModel.getDialogTitle().write(context, out);
				out.endTag(DIV);
			}

			out.endTag(DIV);
		}
		
		// Lay out child content
		out.beginBeginTag(DIV);
		out.writeAttribute(ID_ATTR, getContentId(popupDialog));
		out.writeAttribute(CLASS_ATTR, "pdlgContent");
		writeContentStyle(out, dialogModel);
		out.writeAttribute(TL_TAB_ROOT, true);
		out.endBeginTag();
		{
			popupDialog.getPopupContent().write(context, out);
		}
		writeSizeScript(out, popupDialog);
		out.endTag(DIV);
	}

	private void writeSizeScript(TagWriter out, PopupDialogControl popupDialog) throws IOException {
		HTMLUtil.beginScriptAfterRendering(out);
		out.append("window.PlaceDialog.setSize('");
		out.append(getContentId(popupDialog));
		out.append("', ");
		out.append(String.valueOf(popupDialog.getPopupDialogModel().hasTitleBar()));
		out.append(", '");
		out.append(String.valueOf(ThemeFactory.getTheme().getValue(Icons.POPUP_TITLE_HEIGHT).getValue()));
		out.append("');");
		HTMLUtil.endScriptAfterRendering(out);
	}

	private String getContentId(PopupDialogControl popupDialog) {
		return popupDialog.getID() + "-content";
	}

	private void writeContentStyle(TagWriter out, PopupDialogModel dialogModel) throws IOException {
		out.beginAttribute(STYLE_ATTR);
		writeDialogWidthStyle(out, dialogModel);
		writeContentHeightStyle(out, dialogModel);

		out.append("overflow: ");
		out.append(dialogModel.getLayoutData().getScrollable().toOverflowAttribute());
		out.append(";");

		out.endAttribute();
	}

	private void writeOnClick(TagWriter out, PopupDialogControl popupDialog) throws IOException {
		out.beginAttribute(ONCLICK_ATTR);
		popupDialog.writeClientSideCloseAction(out);
		out.endAttribute();
	}	

	/**	
	 * @see com.top_logic.layout.DefaultControlRenderer#getControlTag(Control)
	 */
	@Override
	protected String getControlTag(PopupDialogControl control) {
		return DIV;
	}

}
