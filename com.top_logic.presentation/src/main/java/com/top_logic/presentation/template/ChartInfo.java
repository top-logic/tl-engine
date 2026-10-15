/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.template;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.poi.xslf.usermodel.XSLFChart;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

import com.top_logic.presentation.data.PresentationData;

/**
 * Description of a chart in a presentation template.
 *
 * @param type
 *        The kind of chart, named after the chart element of the drawing XML without the suffix
 *        <code>Chart</code>, e.g. <code>line</code>, <code>pie</code> or <code>area</code>. A bar
 *        chart is <code>bar</code> with horizontal bars, <code>column</code> with vertical
 *        columns. A chart combining several kinds names them separated by commas.
 * @param series
 *        The number of data series.
 * @param categories
 *        The number of categories of the first data series.
 */
public record ChartInfo(String type, int series, int categories) {

	/**
	 * The namespace of the chart XML.
	 */
	public static final String CHART_NS = "http://schemas.openxmlformats.org/drawingml/2006/chart";

	/**
	 * The suffix of the names of the chart elements in the plot area of a chart, e.g.
	 * <code>barChart</code>.
	 */
	private static final String CHART_SUFFIX = "Chart";

	/**
	 * The {@link #type()} of a bar chart with vertical columns.
	 */
	public static final String COLUMN_TYPE = "column";

	/**
	 * Describes the given chart by the chart elements of its plot area, the number of their series
	 * and the number of categories of the first series.
	 *
	 * @return The description, <code>null</code> if the chart is <code>null</code>.
	 */
	public static ChartInfo of(XSLFChart chart) {
		if (chart == null) {
			return null;
		}
		List<String> types = new ArrayList<>();
		int series = 0;
		int categories = -1;
		try (XmlCursor cursor = chart.getCTChart().getPlotArea().newCursor()) {
			for (boolean found = cursor.toFirstChild(); found; found = cursor.toNextSibling()) {
				String name = cursor.getName().getLocalPart();
				if (!name.endsWith(CHART_SUFFIX) || !CHART_NS.equals(cursor.getName().getNamespaceURI())) {
					continue;
				}
				XmlObject group = cursor.getObject();
				types.add(chartType(name, group));
				for (XmlObject ser : group.selectChildren(CHART_NS, "ser")) {
					series++;
					if (categories < 0) {
						categories = pointCount(ser);
					}
				}
			}
		}
		return new ChartInfo(String.join(",", types), series, Math.max(categories, 0));
	}

	private static String chartType(String elementName, XmlObject group) {
		String type = elementName.substring(0, elementName.length() - CHART_SUFFIX.length());
		XmlObject[] direction = group.selectChildren(CHART_NS, "barDir");
		if (direction.length > 0 && "col".equals(attribute(direction[0], "val"))) {
			return COLUMN_TYPE;
		}
		return type;
	}

	/**
	 * The number of categories of a chart series: the point count of its categories, or of its
	 * values if it has no categories.
	 */
	private static int pointCount(XmlObject ser) {
		for (String data : new String[] { "cat", "val" }) {
			for (XmlObject element : ser.selectChildren(CHART_NS, data)) {
				XmlObject[] counts = element.selectPath("declare namespace c='" + CHART_NS + "' .//c:ptCount");
				if (counts.length > 0) {
					String value = attribute(counts[0], "val");
					if (value != null) {
						return Integer.parseInt(value);
					}
				}
			}
		}
		return 0;
	}

	private static String attribute(XmlObject element, String name) {
		try (XmlCursor cursor = element.newCursor()) {
			return cursor.getAttributeText(new QName(name));
		}
	}

	/**
	 * The data representation with the keys {@link PresentationData#TYPE},
	 * {@link PresentationData#SERIES} and {@link PresentationData#CATEGORIES}.
	 */
	public Map<String, Object> toData() {
		Map<String, Object> result = PresentationData.map();
		result.put(PresentationData.TYPE, type);
		result.put(PresentationData.SERIES, Double.valueOf(series));
		result.put(PresentationData.CATEGORIES, Double.valueOf(categories));
		return result;
	}

}
