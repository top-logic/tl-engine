/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools.tempates;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.layout.tools.LayoutRewrite;

/**
 * Tool to process layout XML files and move information of fixed column count from renderer
 * configuration to table configuration.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class UpgradeTableConfigurationSyntax extends LayoutRewrite {

	private static final String TABLE_TAG = "table";
	private static final String TABLE_RENDERER_TAG = "tableRenderer";
	private static final String FIXED_COLUMNS_ATTRIBUTE = "fixedColumns";
	private static final String CLASS_ATTRIBUTE = "class";
	private static final String FROZEN_COLUMN_TABLE_RENDERER = "com.top_logic.layout.table.renderer.FrozenColumnTableRenderer";

	/**
	 * Main entry point.
	 * 
	 * @param args
	 *        {@link File#pathSeparator}-separated paths with files to process.
	 */
	public static void main(String[] args) throws Exception {
		UpgradeTableConfigurationSyntax tool = new UpgradeTableConfigurationSyntax();
		tool.runMainCommandLine(args);
	}

	@Override
	protected boolean process(Document layout) {
		return rewriteFixedColumnConfiguration(layout.getDocumentElement());
	}

	private boolean rewriteFixedColumnConfiguration(Element element) {
		boolean rewriteSuccessful = true;
		Node next;
		for (Node child = element.getFirstChild(); child != null; child = next) {
			next = child.getNextSibling();

			if (child instanceof Element) {
				rewriteSuccessful &= rewriteFixedColumnConfiguration((Element) child);
			}
		}

		if (DOMUtil.isElement(null, TABLE_RENDERER_TAG, element)) {
			String fixedColumnAttribute = element.getAttributeNS(null, FIXED_COLUMNS_ATTRIBUTE);
			if (!StringServices.isEmpty(fixedColumnAttribute)) {
				Element tableConfigElement = (Element) element.getParentNode();
				if (DOMUtil.isElement(null, TABLE_TAG, tableConfigElement)) {
					element.removeAttributeNS(null, FIXED_COLUMNS_ATTRIBUTE);
					tableConfigElement.setAttributeNS(null, FIXED_COLUMNS_ATTRIBUTE, fixedColumnAttribute);
					if (hasNoAttributes(element) || isSimpleFrozenTableRendererConfig(element)) {
						pullUpSubRendererConfigurations(tableConfigElement, element);
						tableConfigElement.removeChild(element);
					}
				} else {
					rewriteSuccessful = false;
				}
			}
		}
		return rewriteSuccessful;
	}

	private boolean hasNoAttributes(Element element) {
		return !element.hasAttributes();
	}

	private boolean isSimpleFrozenTableRendererConfig(Element element) {
		String classAttribute = element.getAttributeNS(null, CLASS_ATTRIBUTE);
		if (!StringServices.isEmpty(classAttribute)) {
			if (classAttribute.equals(FROZEN_COLUMN_TABLE_RENDERER)) {
				if (element.getAttributes().getLength() == 1) {
					return true;
				}
			}
		}
		return false;
	}

	private void pullUpSubRendererConfigurations(Element tableConfigElement, Element tableRendererElement) {
		Node next;
		for (Node child = tableRendererElement.getFirstChild(); child != null; child = next) {
			next = child.getNextSibling();

			if (child instanceof Element) {
				tableRendererElement.removeChild(child);
				tableConfigElement.insertBefore(child, tableRendererElement);
			}
		}
	}

}
