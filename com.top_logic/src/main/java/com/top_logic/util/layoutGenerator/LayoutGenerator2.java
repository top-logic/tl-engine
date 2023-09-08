/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.layoutGenerator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.tools.layout.InfoLine;
import com.top_logic.basic.tools.layout.LayoutConstants;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.layout.processor.LayoutModelConstants;
import com.top_logic.layout.tabbar.TabInfo;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.mig.html.layout.SubComponentConfig;

/**
 * Generator that translates a CSV layout definition into XML component definitions.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LayoutGenerator2 extends AbstractLayoutGenerator {

	private static final String ENCODING = "utf-8";

	public LayoutGenerator2(List<InfoLine> someInfoLines) {
		super(someInfoLines);
	}

	@Override
	public void generateLayout(boolean interactive) throws IOException {
		File theDestination = null;
		FileManager fmgr = FileManager.getInstance();

		try {
			theDestination = fmgr.getIDEFile(this.getDestinationDir());
		} catch (Exception ex) {
			Logger.error("No directory found in " + this.getDestinationDir(), ex, this);
			return;
		}

		if (!theDestination.exists()) {
			if (!theDestination.mkdirs()) {
				throw new RuntimeException(
					"Destination directory for generated files does not exist and can not be created");
			}
			if (interactive)
				System.out.println("Created dir " + theDestination);
		}
		File masterFrame = new File(theDestination, getMasterFrameFileName());

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		OutputStreamWriter out = new OutputStreamWriter(buffer, ENCODING);
		TagWriter tagOut = new TagWriter(out);
		try {
			tagOut.setIndent(false);
			tagOut.writeXMLHeader(ENCODING);
			expand(tagOut);
		} finally {
			tagOut.close();
		}

		InputSource source = new InputSource(new ByteArrayInputStream(buffer.toByteArray()));
		Document dom;
		try {
			dom = DOMUtil.getDocumentBuilder().parse(source);
		} catch (SAXException ex) {
			throw new UnreachableAssertion(ex);
		}

		String lineEnding = System.getProperty("line.separator");
		XMLPrettyPrinter pretty = new XMLPrettyPrinter(new FileOutputStream(masterFrame), ENCODING, true);
		pretty.getOut().setNewLine(lineEnding);
		pretty.write(dom);
		pretty.close();
		System.out.println("Generated layout to " + masterFrame.getCanonicalPath());
	}

	private void expand(TagWriter out) throws IOException {
		InfoLine rootLine = getRootLine();
		writeLayoutReference(out, rootLine, 0, rootLine.getChildren());
	}

	/**
	 * Create a synthetic Root InfoLine, as the parses do not provide this, yet.
	 * 
	 * This is a fix for #5491
	 */
	private InfoLine getRootLine() {
		InfoLine rootLine = new InfoLine("0", "main", "", "", "", "", "", "", "", "", "", "", "");

		List<InfoLine> infoLines = getInfoLines();
		for (InfoLine childLine = infoLines.get(0); childLine != null; childLine = childLine.getNextSibbling()) {
			rootLine.addChild(childLine);
		}
		return rootLine;
	}

	private void writeLayoutReference(TagWriter out, InfoLine line, int level, List<InfoLine> children)
			throws IOException {
		out.beginBeginTag(LayoutModelConstants.INCLUDE_ELEMENT);
		out.writeAttribute(LayoutModelConstants.INCLUDE_NAME, getTemplateLayout(getTemplateForLevel(line, level)));
		writeLayoutAttributes(out, line);
		out.endBeginTag();
		if (level > 0) {
			writeTabInfo(out, line);
		}

		{
			if (!children.isEmpty()) {
				out.beginTag(SubComponentConfig.COMPONENTS);
				for (InfoLine childLine : children) {
					writeLayoutReference(out, childLine, level + 1, childLine.getChildren());
				}
				out.endTag(SubComponentConfig.COMPONENTS);
			}
		}

		out.endTag(LayoutModelConstants.INCLUDE_ELEMENT);
	}

	protected void writeLayoutAttributes(TagWriter out, InfoLine line) throws IOException {
		for (String propertyName : getAllPropertyNames()) {
			String value = getReplacement(propertyName, line);
			out.writeAttribute(propertyName, value);
		}
	}

	private void writeTabInfo(TagWriter out, InfoLine line) {
		out.beginTag(LayoutModelConstants.INJECT_ELEMENT);
		{
			boolean isIconTab = hasType(line, LayoutConstants.TYPE_TAB_ICON);
			out.beginBeginTag(LayoutComponent.Config.TAB_INFO_NAME);
			if (isIconTab) {
				out.writeAttribute(TabInfo.TabConfig.ID, getName(line));
			}
			out.writeAttribute(TabInfo.TabConfig.LABEL, InfoLine.value(line, InfoLine.KEY) + LayoutUtils.TABBER);
			out.writeAttribute(TabInfo.TabConfig.IMAGE, getImage(line));
			if (isIconTab) {
				out.writeAttribute(TabInfo.TabConfig.RENDERED, String.valueOf(false));
			}
			out.endEmptyTag();
		}
		out.endTag(LayoutModelConstants.INJECT_ELEMENT);
	}

	static String themeVar(String name) {
		return "%" + name + "%";
	}

	static String themeVarDef(String name) {
		return "%" + name + "%";
	}

	private static String getTemplateLayout(String templateName) {
		if (templateName.endsWith(".template")) {
			return "templates/"
				+ templateName.substring(0, templateName.length() - ".template".length())
				+ LayoutModelConstants.XML_SUFFIX;
		} else {
			return templateName;
		}
	}

}
