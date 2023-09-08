/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.StringReader;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.top_logic.base.office.excel.ExcelValue;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.SetBuilder;
import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.tool.export.ExcelCellRenderer;
import com.top_logic.tool.export.ExcelCellRendererProxy;
import com.top_logic.tool.export.RenderContextProxy;
import com.top_logic.util.Resources;

/**
 * {@link ExcelCellRendererProxy} that wraps the actual {@link ExcelCellRenderer} of a column and
 * enhances the cell with compare informations.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CompareExcelRenderer extends ExcelCellRendererProxy {

	private ExcelCellRenderer _impl;

	private boolean _treeTable;

	/**
	 * Creates a new {@link CompareExcelRenderer}.
	 */
	public CompareExcelRenderer(ExcelCellRenderer impl, boolean treeTable) {
		_impl = impl;
		_treeTable = treeTable;
	}

	@Override
	public ExcelValue renderCell(RenderContext context) {
		ExcelValue excelValue = super.renderCell(new CompareRenderContext(context));
		CompareInfo compareInfo = (CompareInfo) context.getCellValue();
		ChangeInfo changeInfo = compareInfo.getChangeInfo();
		if (changeInfo == ChangeInfo.NO_CHANGE) {
			return excelValue;
		}
		CompareRowObject rowObject = getCompareRow(context);
		if (rowObject.changeValue() == null || rowObject.baseValue() == null) {
			// format already applied to "change column"
			return excelValue;
		}
		setExcelComment(excelValue, compareInfo);
		excelValue.setTextColor(compareInfo.getExcelColor());
		return excelValue;
	}

	private CompareRowObject getCompareRow(RenderContext context) {
		Object tableRowObject = context.model().getRowObject(context.modelRow());
		if (_treeTable) {
			tableRowObject = ((TLTreeNode<?>) tableRowObject).getBusinessObject();
		}
		return (CompareRowObject) tableRowObject;
	}

	private void setExcelComment(ExcelValue excelValue, CompareInfo compareInfo) {
		String tooltip = Resources.getInstance().getString(compareInfo.getTooltip());
		String comment;
		try {
			comment = CompareExcelRenderer.xhtmlToRawString(ensureRootTag(tooltip));
		} catch (XMLStreamException ex) {
			// Can not parse tooltip as XHTML.
			comment = oldValueAsString(compareInfo);
		}
		excelValue.setComment(comment);
	}

	private String oldValueAsString(CompareInfo compareInfo) throws UnreachableAssertion {
		Object oldValue;
		switch (compareInfo.getChangeInfo()) {
			case NO_CHANGE:
				throw new UnreachableAssertion(ChangeInfo.NO_CHANGE + " was handled before.");
			case CHANGED:
				oldValue = compareInfo.getOtherObject();
				break;
			case CREATED:
				// No comment for new values.
				return null;
			case DEEP_CHANGED:
				oldValue = compareInfo.getOtherObject();
				break;
			case REMOVED:
				// No comment for removed values.
				return null;
			default:
				throw ChangeInfo.noSuchChangeInfo(compareInfo.getChangeInfo());
		}
		StringBuilder commentBuilder = new StringBuilder();
		commentBuilder.append(Resources.getInstance().getString(I18NConstants.EXCEL_COMMENT_PREFIX));
		commentBuilder.append(' ');
		commentBuilder.append(MetaLabelProvider.INSTANCE.getLabel(oldValue));
		return commentBuilder.toString();
	}

	private String ensureRootTag(String comment) {
		// if comment is not XML, e.g. a list of tags or plain text, parsing fails.
		return "<root>" + comment + "</root>";
	}

	@Override
	protected ExcelCellRenderer impl() {
		return _impl;
	}

	private static final Set<String> NEW_LINE_TAGS =
		new SetBuilder<String>().add(DIV).add(BR).add(PARAGRAPH).add(HR).toSet();

	private static String xhtmlToRawString(String xhtml) throws XMLStreamException {
		XMLStreamReader reader = XMLStreamUtil.getDefaultInputFactory().createXMLStreamReader(new StringReader(xhtml));
		try {
			return xhtmlToRawString(reader);
		} catch (XMLStreamException ex) {
			throw ex;
		} finally {
			reader.close();
		}
	}

	private static String xhtmlToRawString(XMLStreamReader reader) throws XMLStreamException {
		StringBuilder builder = new StringBuilder();
		while (reader.hasNext()) {
			int event = reader.next();
			switch (event) {
				case XMLStreamReader.CHARACTERS:
				case XMLStreamReader.SPACE:
					String text = reader.getText();
					if (text != null) {
						builder.append(text);
					}
					break;
				case XMLStreamReader.START_ELEMENT:
					String localPart = reader.getName().getLocalPart();
					if (PARAGRAPH.equals(localPart)) {
						ensureNewLine(builder);
					}
					if (HR.equals(localPart)) {
						ensureNewLine(builder);
						builder.append("-----");
					}
					if (NEW_LINE_TAGS.contains(localPart)) {
						addNewLine(builder);
					}
					break;
			}
		}
		return builder.toString();
	}

	private static StringBuilder addNewLine(StringBuilder builder) {
		return builder.append('\n');
	}

	private static void ensureNewLine(StringBuilder builder) {
		int builderLength = builder.length();
		switch (builderLength) {
			case 0:
				addNewLine(builder);
				break;
			default:
				if (builder.charAt(builderLength - 1) != '\n') {
					addNewLine(builder);
				}
				break;
		}
	}

	private static class CompareRenderContext extends RenderContextProxy {

		private RenderContext _context;

		public CompareRenderContext(RenderContext context) {
			_context = context;
		}

		@Override
		protected RenderContext impl() {
			return _context;
		}

		@Override
		public Object getCellValue() {
			CompareInfo compareInfo = (CompareInfo) super.getCellValue();
			return compareInfo.getDisplayedObject();
		}

	}

}
