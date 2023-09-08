/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.renderer;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.wrap.exceptions.WrapperRuntimeException;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.table.AbstractCellRenderer;
import com.top_logic.layout.table.CellRenderer;
import com.top_logic.layout.table.TableRenderer.Cell;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.layout.tree.renderer.TreeCellRenderer;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.Media;
import com.top_logic.tool.boundsec.commandhandlers.GotoHandler;
import com.top_logic.util.css.CssUtil;

/**
 * {@link CellRenderer} renders the cell value as link with an image of the row objects type before.
 * 
 * <p>
 * This renderer should be used only for flat tables. For tree like tables such as tree grids or
 * tree tables the more general {@link IDColumnCellRenderer} should be used instead.
 * </p>
 * 
 * <p>
 * The {@link IDColumnCellRenderer} uses the {@link TreeCellRenderer} to display an expand and
 * collapse button to navigate through the tree and the {@link IDColumnTableCellRenderer} to render
 * the cell content.
 * </p>
 * 
 * @see IDColumnCellRenderer
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class IDColumnTableCellRenderer extends AbstractCellRenderer {

	private CellRenderer _cellRenderer;

	private ResourceProvider _rowObjectResourceProvider;

	/**
	 * Creates a {@link IDColumnTableCellRenderer} from configuration.
	 */
	public IDColumnTableCellRenderer(TableConfiguration tableConfig, ColumnConfiguration columnConfig) {
		_cellRenderer = columnConfig.finalCellRenderer();
		_rowObjectResourceProvider = getNonNullResourceProvider(tableConfig.getRowObjectResourceProvider());
	}

	private ResourceProvider getNonNullResourceProvider(ResourceProvider resourceProvider) {
		if (resourceProvider != null) {
			return resourceProvider;
		} else {
			return MetaResourceProvider.INSTANCE;
		}
	}

	@Override
	public void writeCell(DisplayContext context, TagWriter out, Cell cell) throws IOException {
		boolean useLink = context.getOutputMedia() != Media.PDF;
		boolean useToolTip = context.getOutputMedia() != Media.PDF;

		String link;
		String tooltip;
		boolean hasLink;
		boolean hasTooltip;
		String cssClass;
		boolean hasCssClass;
		boolean inEditMode;

		Object rowObject = cell.getRowObject();

		try {
			Object cellValue = cell.getValue();
			inEditMode = cellValue instanceof FormMember;
			link = useLink ? _rowObjectResourceProvider.getLink(context, rowObject) : null;
			tooltip = useToolTip ? _rowObjectResourceProvider.getTooltip(rowObject) : null;
			cssClass = _rowObjectResourceProvider.getCssClass(cellValue);
			hasCssClass = cssClass != null;
			hasLink = link != null && !inEditMode;
			hasTooltip = tooltip != null;
		} catch (WrapperRuntimeException ex) {
			// fix for error while trying to render a deleted object
			return;
		}

		out.beginTag(SPAN, CLASS_ATTR, "cDecoratedCell");
		out.beginTag(SPAN, CLASS_ATTR, FormConstants.FIXED_LEFT_CSS_CLASS);
		ThemeImage image = _rowObjectResourceProvider.getImage(rowObject, Flavor.DEFAULT);
		if (image != null) {
			writeImageTag(context, out, rowObject, image, tooltip);
		}
		out.endTag(SPAN);

		out.beginTag(SPAN, CLASS_ATTR, FormConstants.FLEXIBLE_CSS_CLASS);

		if (hasLink) {
			out.beginBeginTag(HTMLConstants.ANCHOR);
			out.writeAttribute(HTMLConstants.HREF_ATTR, HTMLConstants.HREF_EMPTY_LINK);
			CssUtil.writeCombinedCssClasses(out, GotoHandler.GOTO_CLASS, cssClass);
			out.writeAttribute(HTMLConstants.ONCLICK_ATTR, link);
			if (hasTooltip) {
				// OverLib attributes are OK for ANCHOR, too
				OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, tooltip);
			}
			out.endBeginTag();
		} else if (hasTooltip) {
			out.beginBeginTag(HTMLConstants.SPAN);
			out.writeAttribute(HTMLConstants.CLASS_ATTR, cssClass);
			OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, tooltip);
			out.endBeginTag();
		} else if (hasCssClass) {
			out.beginBeginTag(HTMLConstants.SPAN);
			out.writeAttribute(HTMLConstants.CLASS_ATTR, cssClass);
			out.endBeginTag();
		}

		_cellRenderer.writeCell(context, out, cell);

		if (hasLink) {
			out.endTag(HTMLConstants.ANCHOR);
		} else if (hasTooltip) {
			out.endTag(HTMLConstants.SPAN);
		} else if (hasCssClass) {
			out.endTag(HTMLConstants.SPAN);
		}

		out.endTag(SPAN);
		out.endTag(SPAN);

	}

	private void writeImageTag(DisplayContext context, TagWriter out, Object node, ThemeImage image, String tooltip)
			throws IOException {
		XMLTag tag = image.toIcon();
		tag.beginBeginTag(context, out);
		out.writeAttribute(CLASS_ATTR, FormConstants.TREE_TYPE_IMAGE_CSS_CLASS);
		out.writeAttribute(ALT_ATTR, StringServices.nonNull(_rowObjectResourceProvider.getLabel(node)));
		out.writeAttribute(TITLE_ATTR, StringServices.EMPTY_STRING); // Avoid popup for IE
		if (tooltip != null) {
			OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, tooltip);
		}
		tag.endEmptyTag(context, out);
	}

}
