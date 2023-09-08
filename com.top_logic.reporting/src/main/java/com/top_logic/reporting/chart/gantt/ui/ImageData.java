/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.gantt.ui;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.StandardEntityCollection;

import com.top_logic.base.chart.util.ChartUtil;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.reporting.chart.gantt.model.GanttObject.Goto;

/**
 * Holds a generated image page of the graphic.
 * 
 * @author <a href="mailto:mga@top-logic.com">mga</a>
 */
public class ImageData {

	static final Pattern PLACEHOLDER_PATTERN = Pattern.compile(getPlaceholder("(\\d*)"));

	/** The {@link BufferedImage} to write. */
	private BufferedImage _image = null;

	/** The dimension of the image. */
	private Dimension _dimension = null;

	private BinaryData _binaryData = null;

	private File _file = null;

	/** The image map data. */
	private EntityCollection _areaData = new StandardEntityCollection();

	private int _treeWidth;

	private int _headerHeight;

	final Map<Integer, Goto> _replacementMap = new HashMap<>();

	/**
	 * Returns a file containing the image formerly set via {@link #setImage(BufferedImage)}.
	 */
	public File toImageFile() throws IOException {
		if (_file == null) {
			_file = ChartUtil.writeImageAsPng(_image);
		}
		return _file;
	}

	/**
	 * Returns a {@link BinaryData} for the image formerly set via {@link #setImage(BufferedImage)}.
	 */
	public BinaryData toBinaryData() throws IOException {
		if (_binaryData == null) {
			_binaryData = ChartUtil.getImageAsPng(_image);
		}
		return _binaryData;
	}

	/**
	 * Setter for the image this data represent.
	 */
	public void setImage(BufferedImage image) {
		_image = image;
		reset();
	}

	private void reset() {
		_binaryData = null;
		_file = null;
	}

	/**
	 * Getter for {@link #_dimension}.
	 */
	public Dimension getDimension() {
		return _dimension;
	}

	/**
	 * Setter for {@link #_dimension}.
	 */
	public void setDimension(Dimension dimension) {
		_dimension = dimension;
		reset();
	}

	/**
	 * Getter for {@link #_areaData}.
	 */
	public EntityCollection getAreaData() {
		return _areaData;
	}

	/**
	 * Setter for {@link #_areaData}.
	 */
	public void setAreaData(EntityCollection areaData) {
		_areaData = areaData;
	}

	/**
	 * Height of the column header area.
	 */
	public int getHeaderHeight() {
		return _headerHeight;
	}

	/**
	 * @see #getHeaderHeight()
	 */
	public void setHeaderHeight(int headerHeight) {
		_headerHeight = headerHeight;
	}

	/**
	 * Width of the left description area of the graphic.
	 */
	public int getTreeWidth() {
		return _treeWidth;
	}

	/**
	 * @see #getTreeWidth()
	 */
	public void setTreeWidth(int treeWidth) {
		_treeWidth = treeWidth;
	}

	/**
	 * Returns a {@link String} that must be inserted into the image map for this {@link ImageData}
	 * that serves as placeholder for the link represented by the given {@link Goto}.
	 * 
	 * <p>
	 * The actual link is later replaced in {@link #replacePlaceholders(String)}.
	 * </p>
	 * 
	 * @param gotoArg
	 *        The {@link Goto} to create a placeholder string for.
	 * @return A placeholder for the given link.
	 */
	public String getLinkPlaceholder(Goto gotoArg) {
		int newKey = _replacementMap.size();
		_replacementMap.put(newKey, gotoArg);
		return getPlaceholder(newKey);
	}

	/**
	 * Replaces the place holders formerly added into the given image map string.
	 */
	public HTMLFragment replacePlaceholders(final String imageMap) {
		return new HTMLFragment() {
			
			@Override
			public void write(DisplayContext context, TagWriter out) throws IOException {
				int depthBefore = out.getDepth();
				try {
					writeMap(context, out);
				} catch (XMLStreamException ex) {
					out.endAll(depthBefore);
					throw new RuntimeException(ex);
				}
			}

			private void writeMap(DisplayContext context, TagWriter out) throws XMLStreamException, IOException {
				XMLStreamReader in = createStreamReader();
				while (in.hasNext()) {
					int event = in.next();
					switch (event) {
						case XMLStreamConstants.CDATA:
							out.beginCData();
							out.writeCDATAContent(in.getText());
							out.endCData();
							break;
						case XMLStreamConstants.SPACE:
						case XMLStreamConstants.CHARACTERS:
							out.write(in.getText());
							break;
						case XMLStreamConstants.COMMENT:
							out.beginComment();
							out.writeCommentContent(in.getText());
							out.endComment();
							break;
						case XMLStreamConstants.START_ELEMENT: {
							String tagName = in.getLocalName();
							out.beginBeginTag(tagName);
							writeAttributes(context, out, in);
							if (HTMLUtil.isVoidElement(tagName)) {
								out.endEmptyTag();
							} else {
								out.endBeginTag();
							}
							break;
						}
						case XMLStreamConstants.END_ELEMENT: {
							String tagName = in.getLocalName();
							if (HTMLUtil.isVoidElement(tagName)) {
								// empty tag was written
							} else {
								out.endTag(tagName);
							}
							break;
						}
						case XMLStreamConstants.END_DOCUMENT:
						case XMLStreamConstants.START_DOCUMENT:
						case XMLStreamConstants.ENTITY_REFERENCE:
						case XMLStreamConstants.DTD:
						case XMLStreamConstants.ENTITY_DECLARATION:
						case XMLStreamConstants.NOTATION_DECLARATION:
						case XMLStreamConstants.PROCESSING_INSTRUCTION:
							break;
						case XMLStreamConstants.ATTRIBUTE:
						case XMLStreamConstants.NAMESPACE:
							throw new UnsupportedOperationException("Encountered unsupported event type: " + event);

					}
				}
			}

			private String replacePlaceHolders(DisplayContext context, String origOnClick) {
				Matcher matcher = PLACEHOLDER_PATTERN.matcher(origOnClick);
				if (!matcher.find()) {
					// no placeholder written
					return origOnClick;
				}
				StringBuffer sb = new StringBuffer();
				do {
					String id = matcher.group(1);
					Goto gotoArg = _replacementMap.get(Integer.parseInt(id));
					if (gotoArg == null) {
						// Invalid imageMap
						matcher.appendReplacement(sb, matcher.group(0));
					} else {
						String link = gotoArg.createGotoLink(context);
						if (link == null) {
							// No goto link. Ignore on click.
							return null;
						}
						matcher.appendReplacement(sb, link);
					}
				} while (matcher.find());
				matcher.appendTail(sb);
				return sb.toString();
			}

			private void writeAttributes(DisplayContext context, TagWriter out, XMLStreamReader in) {
				String onClickValue = in.getAttributeValue(null, HTMLConstants.ONCLICK_ATTR);
				if (onClickValue == null) {
					for (int n = 0, cnt = in.getAttributeCount(); n < cnt; n++) {
						String attributeName = in.getAttributeLocalName(n);
						String attributeValue = in.getAttributeValue(n);
						out.writeAttribute(attributeName, attributeValue);
					}
					return;
				}
				String onClick = replacePlaceHolders(context, onClickValue);
				if (onClick != null) {
					// Valid on click
					for (int n = 0, cnt = in.getAttributeCount(); n < cnt; n++) {
						String attributeName = in.getAttributeLocalName(n);
						String attributeValue = in.getAttributeValue(n);
						if (HTMLConstants.ONCLICK_ATTR.equals(attributeName)) {
							// Use replaced on click
							attributeValue = onClick;
						}
						out.writeAttribute(attributeName, attributeValue);
					}
					return;
				}
				// No on click by resource provider
				for (int n = 0, cnt = in.getAttributeCount(); n < cnt; n++) {
					String attributeName = in.getAttributeLocalName(n);
					if (HTMLConstants.ONCLICK_ATTR.equals(attributeName)
						|| HTMLConstants.HREF_ATTR.equals(attributeName)) {

						// Skip attributes written by OnClickURLTagFragmentGenerator
						continue;
					}
					String attributeValue = in.getAttributeValue(n);
					out.writeAttribute(attributeName, attributeValue);
				}
			}

			private XMLStreamReader createStreamReader() throws XMLStreamException {
				return XMLStreamUtil.getDefaultInputFactory().createXMLStreamReader(new StringReader(imageMap));
			}

		};
	}

	private static String getPlaceholder(Object key) {
		return "#!-- Replacement for id " + key + " --!#";
	}

}
