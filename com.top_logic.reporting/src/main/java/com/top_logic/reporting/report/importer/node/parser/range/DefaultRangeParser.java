/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.importer.node.parser.range;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Node;

import com.top_logic.base.time.TimeRangeIterator;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.EqualsFilter;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.reporting.report.exception.ReportingException;
import com.top_logic.reporting.report.importer.AbstractXMLImporter;
import com.top_logic.reporting.report.importer.node.NodeParser;
import com.top_logic.reporting.report.model.Report;
import com.top_logic.reporting.report.model.ReportConfiguration;
import com.top_logic.reporting.report.model.filter.ContainsFilter;
import com.top_logic.reporting.report.model.filter.FilterEntry;
import com.top_logic.reporting.report.model.filter.NumberInterval;
import com.top_logic.reporting.report.model.filter.NumberIntervalProvider;
import com.top_logic.reporting.report.model.filter.ObjectFilter;
import com.top_logic.reporting.report.model.partition.TimeRangeFactory;
import com.top_logic.reporting.report.model.partition.function.ClassificationPartitionFunction;
import com.top_logic.reporting.report.model.partition.function.DatePartitionFunction;
import com.top_logic.reporting.report.model.partition.function.NumberPartitionFunction;
import com.top_logic.reporting.report.model.partition.function.StringPartitionFunction;
import com.top_logic.reporting.report.xmlutilities.ReportReader;
import com.top_logic.util.TLContext;

/**
 * The DefaultRangeParser is used to parse &lt;range> tags for {@link StringPartitionFunction}
 * {@link NumberPartitionFunction} {@link DatePartitionFunction}
 * {@link ClassificationPartitionFunction}
 * 
 * The range tags are used to categorize the business objects based on manually defined
 * categories.
 * 
 * It is invoked by a CategoryFunctionParser.
 * 
 * Example XML:
 * <xmp>
 *  <range> <i18n> <de>Kategorie Eins</de> <en>Category one</en> </i18n>
 * 
 * <range> <i18n> <de>Kategorie Zwei</de> <en>Category two</en> </i18n>
 * <equals>Genau_dieser_Wert</equals> </range>
 * 
 * <range> <i18n> <de>Kategorie Drei</de> <en>Category truee</en> </i18n>
 * <equals-list>Mindestens,einer,dieser,Werte</equals-list> </range>
 * </xmp>
 * 
 * @author <a href="mailto:fsc@top-logic.com">Friedemann Schneider</a>
 * @deprecated use {@link ReportReader} and {@link ReportConfiguration}
 */
@Deprecated
public class DefaultRangeParser extends AbstractRangeParser implements NodeParser {

	/**
	 * begin and end must be set together. With this option, the user can set one interval
	 * with fixed borders for a category. This category will be named following the given
	 * i18n tag. The includeBorder-attribute determins, if the borders are included in the
	 * interval :) The values will be extracted by some kind of magic extraction method
	 * {@link AbstractXMLImporter#extractObject(String)} e.g &lt;begin includeBorder="true">1&lt;/begin>
	 * &lt;end includeBorder="false">10&lt;/end> will result in the interval [1,10)
	 * 
	 * &lt;begin includeBorder="true">currentYear&lt;/begin> &lt;end
	 * includeBorder="false">currentYear +1&lt;/end> will result in a range from 01.01. to
	 * 31.12. of the current year
	 * 
	 * &lt;begin includeBorder="true">01.01.2007&lt;/begin> &lt;end includeBorder="false">01.01.2008&lt;/end>
	 * will result in a range from 01.01.2007 to 31.12.2007 (borders are fixed)
	 */
	public static final String BEGIN_TAG = "begin";
	public static final String END_TAG = "end";
	public static final String INCLUDE_ATTR = "includeBorder";
	/** MANDATORY in context */

	/**
	 * Optionally for a begin-end interval the granularity option can be used to subdivide
	 * the interval in intervals with the given span These categories(intervals will be
	 * named following the given i18n tag as prefix. e.g &lt;begin includeBorder="true">1&lt;/begin>
	 * &lt;end includeBorder="false">10&lt;/end> &lt;granularity>2&lt;/granularity> will result in the
	 * intervals [1,3), [3,5), [5,7), [7,9), [9,10)
	 * 
	 * &lt;begin includeBorder="true">currentYear&lt;/begin> &lt;end
	 * includeBorder="false">currentYear +1&lt;/end> &lt;granularity>month&lt;/granularity> will
	 * result in 12 intervals, each containing a month of the current year
	 */
	public static final String GRANULARITY_TAG = "granularity";

	/**
	 * Equals provides one category for objects, whose attributes are equal the given value.
	 * This category will be named following the given i18n tag.
	 */
	public static final String EQUALS_TAG = "equals";

	/**
	 * Equals-List provides one category for objects, whose attribute values are equal to at
	 * least one of the values in this list. This category will be named following the given
	 * i18n tag.
	 */
	public static final String EQUALS_LIST_TAG = "equals-list";

	/** The single instance of this class. */
	private static DefaultRangeParser instance;

	protected DefaultRangeParser() {

	}

	public static DefaultRangeParser getInstance() {
		if (instance == null) {
			instance = new DefaultRangeParser();
		}
		return instance;
	}

	@Override
	public Object parse( Node aNode, Report aReport ) {
		HashMap theResources = getI18N(aNode, aReport);
		if (hasSingleNode(aNode, EQUALS_TAG)) {
			return createEqualsFilter(aNode, aReport, theResources);
		}
		else if (hasSingleNode(aNode, EQUALS_LIST_TAG)) {
			return createEqualsListFilter(aNode, aReport, theResources);
		}
		else if (hasSingleNode(aNode, BEGIN_TAG)) {
			if (!hasSingleNode(aNode, END_TAG)) {
				throw new ReportingException(this.getClass(),
						"Inside the range tag <begin> without <end> was found!");
			}
			return createBeginAndFilter(aNode, aReport, theResources);
		}
		else {
			return createFalseFilter(aNode, aReport, theResources);
		}
	}

	/**
	 * @see #EQUALS_TAG
	 */
	private List createEqualsFilter( Node aNode, Report aReport, HashMap someResources ) {
		String theString = getNodeDataAsString(aNode, EQUALS_TAG);
		Object theValue = extractObject(theString);
		Filter theFilter = new EqualsFilter(theValue);
		List theReturn = new ArrayList(1);

		theReturn.add(new FilterEntry(theFilter, aReport.getLanguage(), someResources));

		return theReturn;
	}

	/**
	 * @see #EQUALS_LIST_TAG
	 */
	private List createEqualsListFilter( Node aNode, Report aReport, HashMap someResources ) {
		String theString = getNodeDataAsString(aNode, EQUALS_LIST_TAG);
		Filter theFilter = new ContainsFilter(theString);
		List theReturn = new ArrayList(1);

		theReturn.add(new FilterEntry(theFilter, aReport.getLanguage(), someResources));

		return theReturn;
	}

	private List createFalseFilter( Node aNode, Report aReport, HashMap someResources ) {
		Filter theFilter = FilterFactory.falseFilter();
		List theReturn = new ArrayList(1);

		theReturn.add(new FilterEntry(theFilter, aReport.getLanguage(), someResources));

		return theReturn;
	}

	/**
	 * @see #BEGIN_TAG
	 */
	private List createBeginAndFilter( Node aNode, Report aReport, HashMap someResources ) {
		Object theBeginObject = null;
		boolean includeBegin = true;
		Long theGranularity = null;
		List theReturn = new ArrayList(1);
		Node theBeginNode = getSingleNode(aNode, BEGIN_TAG);

		if (theBeginNode != null) {
			theBeginObject = extractObject(theBeginNode);
			includeBegin = getAttributeAsBoolean(theBeginNode, INCLUDE_ATTR);
		}

		Object theEndObject = null;
		boolean includeEnd = false;

		Node theEndNode = getSingleNode(aNode, END_TAG);

		if (theEndNode != null) {
			theEndObject = extractObject(theEndNode);
			includeEnd = getAttributeAsBoolean(theEndNode, INCLUDE_ATTR);
		}

		Node theGranNode = getSingleNode(aNode, GRANULARITY_TAG);
		if (theGranNode != null) {
			theGranularity = Long.valueOf(getDataAsInt(theGranNode));
			if (!StringServices.isEmpty(theGranularity)) {
				if (theBeginObject instanceof Date && theEndObject instanceof Date) {
					TimeRangeIterator theTRI = TimeRangeFactory.getInstance().getTimeRange(
							theGranularity, TLContext.getLocale(), (Date) theBeginObject,
							(Date) theEndObject);

					while (theTRI.next() != null) {
						HashMap theResources = new HashMap(1);
						TimeRangeIterator theInnerTRI = TimeRangeFactory.getInstance()
								.getTimeRange(theGranularity, TLContext.getLocale(),
										theTRI.current(), theTRI.current());
						Filter theFilter = new ObjectFilter(theInnerTRI.getStart(), includeBegin,
								theInnerTRI.getEnd(), includeEnd);
						theInnerTRI.next();
						theResources.put(aReport.getLanguage(), theInnerTRI.getSimpleFormat()
								.format(theInnerTRI.current()));
						theReturn.add(new FilterEntry(theFilter, aReport.getLanguage(),
								theResources));
					}

				}
				else if (theBeginObject instanceof Number && theEndObject instanceof Number) {
					try {
						Double theGran = Double.valueOf(theGranularity.intValue());
						NumberIntervalProvider theProvider = new NumberIntervalProvider(theGran);
						List theEntries = theProvider.getIntervals(theBeginObject, theEndObject);
						for (Iterator theIter = theEntries.iterator(); theIter.hasNext();) {
							NumberInterval theEntry = (NumberInterval) theIter.next();
							HashMap theResources = new HashMap(1);
							theResources.put(aReport.getLanguage(), theEntry.toString());
							Filter theFilter = new ObjectFilter(theEntry.getBegin(), true, theEntry
									.getEnd(), false);
							theReturn.add(new FilterEntry(theFilter, aReport.getLanguage(),
									theResources));
						}
					}
					catch (NumberFormatException nex) {
						throw new ReportingException(this.getClass(),
								"Unable to parse number from string", nex);
					}
				}
			}
		}
		else {
			if (theBeginObject != null && theEndObject != null) {
				Filter theFilter = new ObjectFilter(theBeginObject, includeBegin, theEndObject,
						includeEnd);
				theReturn.add(new FilterEntry(theFilter, aReport.getLanguage(), someResources));
			}
		}

		return theReturn;
	}
}
