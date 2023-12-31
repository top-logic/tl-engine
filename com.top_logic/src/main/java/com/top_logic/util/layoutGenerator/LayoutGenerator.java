/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.layoutGenerator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.basic.tools.layout.InfoLine;
import com.top_logic.basic.tools.layout.LayoutConstants;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.layout.processor.LayoutModelConstants;
import com.top_logic.layout.tabbar.TabInfo;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutReference;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.mig.html.layout.SubComponentConfig;

/**
 * Generator that translates a CSV layout definition into XML component definitions.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LayoutGenerator extends AbstractLayoutGenerator {

	private static final String ENCODING = StringServices.UTF8;

	private final Log _log;

	private String _moduleName;

	/**
	 * Creates a new {@link LayoutGenerator} with a new {@link LogProtocol}.
	 */
	public LayoutGenerator(List<InfoLine> someInfoLines) {
		this(new LogProtocol(LayoutGenerator.class), someInfoLines);
	}

	/**
	 * Creates a new {@link LayoutGenerator} with the given {@link Log} and {@link InfoLine}s.
	 */
	public LayoutGenerator(Log log, List<InfoLine> someInfoLines) {
		super(someInfoLines);
		_log = log;
	}

	@Override
	public void generateLayout(boolean interactive) throws IOException {
		_moduleName = new File(".").getCanonicalFile().getName();

		Path relativePath = Paths.get(getMasterFrameFileName());

		InfoLine rootLine = getRootLine();
		print(rootLine, relativePath, tagWriter -> {
			writeLayoutReference(tagWriter, rootLine, 0, rootLine.getChildren(), interactive);
		}, interactive);
	}

	@FunctionalInterface
	private static interface Sink {

		void doIt(TagWriter out) throws IOException;

	}

	private void print(InfoLine line, Path relativePath, Sink action, boolean interactive) throws IOException {
		Path destination = destination(line, interactive);
		if (destination == null) {
			return;
		}
		Path outPath = destination.resolve(relativePath);
		if (hasType(line, LayoutConstants.TYPE_NO_GENERATION)) {
			if (interactive) {
				_log.info(
					outPath + " not generated, because type '" + LayoutConstants.TYPE_NO_GENERATION + "' was set.");
			}
			return;
		}
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		try (
				OutputStreamWriter out = new OutputStreamWriter(buffer, ENCODING);
				TagWriter tagOut = new TagWriter(out)) {
			tagOut.setIndent(false);
			tagOut.writeXMLHeader(ENCODING);
			{
				tagOut.beginComment();
				tagOut.append("Automatically generated by {@link ");
				tagOut.append(LayoutGenerator.this.getClass().getName());
				tagOut.append("}.");
				tagOut.endComment();
			}
			tagOut.flush();
			int writtenBytes = buffer.size();
			action.doIt(tagOut);
			tagOut.flush();
			if (writtenBytes == buffer.size()) {
				_log.error("No content written to " + outPath);
				return;
			}
		}

		prettyPrint(outPath, buffer.toByteArray());
		if (interactive) {
			_log.info(outPath + " generated.");
		}
	}

	private Path destination(InfoLine line, boolean interactive) {
		String targetProject = targetProject(line);
		if (targetProject == null) {
			targetProject = _moduleName;
		}
		Path module = Paths.get("..", targetProject);
		Path webapp = module.resolve(ModuleLayoutConstants.WEBAPP_DIR);
		if (!Files.exists(webapp)) {
			StringBuilder msg = new StringBuilder();
			msg.append("Root directory '");
			msg.append(webapp);
			msg.append("' for line '");
			msg.append(name(line));
			msg.append("' does not exist.");
			_log.error(msg.toString());
			return null;
		}

		Path destinationDir = webapp.resolve(getDestinationDir());
		if (!Files.exists(destinationDir)) {
			try {
				destinationDir = Files.createDirectories(destinationDir);
			} catch (IOException ex) {
				StringBuilder msg = new StringBuilder();
				msg.append("Destination directory '");
				msg.append(destinationDir);
				msg.append("' for generated file for line '");
				msg.append(name(line));
				msg.append("' does not exist and can not be created");
				_log.error(msg.toString(), ex);
				return null;
			}
			if (interactive)
				_log.info("Created dir " + destinationDir);
		}
		return destinationDir;
	}

	private void prettyPrint(Path path, byte[] content) throws IOException {
		Document dom = parse(content);
		Path dir = path.getParent();
		if (dir != null) {
			// Create parent directories
			Files.createDirectories(dir);
		}
		try (
				OutputStream out = Files.newOutputStream(path,
					StandardOpenOption.TRUNCATE_EXISTING,
					StandardOpenOption.CREATE,
					StandardOpenOption.WRITE);
				XMLPrettyPrinter pretty = new XMLPrettyPrinter(out, ENCODING, true)) {
			pretty.getOut().setNewLine(System.getProperty("line.separator"));
			pretty.write(dom);
		}
	}

	private Document parse(byte[] content) throws IOException {
		InputSource source = new InputSource(new ByteArrayInputStream(content));
		DocumentBuilder documentBuilder = DOMUtil.getDocumentBuilder();
		try {
			return documentBuilder.parse(source);
		} catch (SAXException ex) {
			String contentString = new String(content, ENCODING);
			_log.error("Unable to pretty print " + contentString, ex);
			Document newDocument = documentBuilder.newDocument();
			Element elem = newDocument.createElement("errorContent");
			elem.appendChild(newDocument.createCDATASection(contentString));
			newDocument.appendChild(elem);
			return newDocument;
		}
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

	private void writeLayoutReference(TagWriter out, InfoLine line, int level, List<InfoLine> children,
			boolean interactive) throws IOException {
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
					Path pathForLine = relativePathFor(childLine);
					writeReference(out, pathForLine);

					print(childLine, pathForLine, tagOut -> {
						writeLayoutReference(tagOut, childLine, level + 1, childLine.getChildren(), interactive);
					}, interactive);
				}
				out.endTag(SubComponentConfig.COMPONENTS);
			}
		}

		out.endTag(LayoutModelConstants.INCLUDE_ELEMENT);
	}

	private void writeLayoutAttributes(TagWriter out, InfoLine line) {
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

	private void writeReference(TagWriter out, Path relPath) throws IOException {
		out.beginBeginTag(LayoutReference.TAG_NAME);
		out.beginAttribute(LayoutReference.RESOURCE_ATTRIBUTE);
		int numberNames = relPath.getNameCount();
		for (int index = 0; index < numberNames; index++) {
			if (index == 0) {
				// Hide leading '/'.
			} else {
				out.append('/');
			}
			out.append(relPath.getName(index).toString());
		}
		out.endAttribute();
		out.endEmptyTag();
	}

	private Path relativePathFor(InfoLine line) {
		String first;
		String[] split;
		String targetPath = targetPath(line);
		if (targetPath == null) {
			split = nameByKey(line);
			first = _moduleName;
		} else {
			split = targetPath.split("/");
			first = split[0];
			split = Arrays.copyOfRange(split, 1, split.length);
		}
		split[split.length - 1] = split[split.length - 1] + LayoutModelConstants.LAYOUT_XML_FILE_SUFFIX;
		return Paths.get(first, split);
		

	}

	private String[] nameByKey(InfoLine line) {
		String name = name(line);
		String key = key(line);
		String[] split = key.split(Pattern.quote("."));
		split = Arrays.copyOf(split, split.length + 1);
		split[split.length - 1] = name;
		return split;
	}

	private String targetProject(InfoLine line) {
		return InfoLine.value(line, InfoLine.TARGET_PROJECT);
	}

	private String targetPath(InfoLine line) {
		return InfoLine.value(line, InfoLine.TARGET_PATH);
	}

	private String key(InfoLine line) {
		return InfoLine.value(line, InfoLine.KEY);
	}

	private String name(InfoLine line) {
		return InfoLine.value(line, InfoLine.NAME);
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
