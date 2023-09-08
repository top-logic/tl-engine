/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.layout.flexreporting.producer.tooltips;

import com.top_logic.basic.StringServices;
import com.top_logic.reporting.layout.flexreporting.producer.FlexibleReportingProducer;
import com.top_logic.reporting.report.util.ReportConstants;
import com.top_logic.reporting.report.util.ReportUtilities;
import com.top_logic.util.Resources;

/**
 * Base class for the tooltip generator used in the {@link FlexibleReportingProducer}.
 * 
 * @author    <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public abstract class ReportingTooltipGenerator implements ReportConstants {
	protected final String translatedCount;
	protected final String amount;
	protected final String attribute;
	protected final String function;
	protected final String range;

	public ReportingTooltipGenerator() {
		Resources theRes     = Resources.getInstance();
		this.translatedCount = theRes.getString(I18NConstants.COUNT_FUNCTION);
		this.amount = theRes.getString(I18NConstants.AMOUNT_TOOLTIP);
		this.attribute = theRes.getString(I18NConstants.ATTRIBUTE_TOOLTIP);
		this.function = theRes.getString(I18NConstants.FUNCTION_TOOLTIP);
		this.range = theRes.getString(I18NConstants.RANGE_TOOLTIP);
    }
	
	/** 
	 * Builds the actual tooltip for each item displayed in a chart.
	 * 
	 * @param functionKey the name of the function used
	 * @param rangeKey    the category or rage (X-axis)
	 * @param theVal      the value (Y-axis)
	 * @return the tooltip for the item
	 */
	protected String buildTooltip(String functionKey, String rangeKey, Number theVal) {
		int    len        = functionKey.length();
		int    pos        = functionKey.indexOf("(");
		StringBuilder theBuffer = new StringBuilder();
		// add the line for the amount
		theBuffer.append(this.amount);
		theBuffer.append(": <strong>");
		theBuffer.append(ReportUtilities.getNumberFormat().format(theVal == null ? Double.valueOf(0.0) : theVal));
		theBuffer.append("</strong><br/>");
		
		if (pos < 0) {
			// add the line for the function
			theBuffer.append(this.function);
			theBuffer.append(": <strong>");
			theBuffer.append(functionKey);
			theBuffer.append("</strong><br/>");
		}
		else {
			String theAttribute = functionKey.substring(pos+1, len-1);
			String theFunction = functionKey.substring(0, pos);

			if(!theFunction.equals(translatedCount)) {
				// add the line for the attribute
				theBuffer.append(this.attribute);
				theBuffer.append(": <strong>");
				theBuffer.append(theAttribute);
				theBuffer.append("</strong><br/>");
				
				// add the line for the function
				theBuffer.append(this.function);
				theBuffer.append(": <strong>");
				theBuffer.append(theFunction);
				theBuffer.append("</strong><br/>");
			}
			else {
				theBuffer.append(this.function);
				theBuffer.append(": <strong>");
				theBuffer.append(theFunction);
				theBuffer.append("(");
				theBuffer.append(theAttribute);
				theBuffer.append(")");
				theBuffer.append("</strong><br/>");
			}
		}
		if (!StringServices.isEmpty(rangeKey)) {
			// add the line for the range
			theBuffer.append(this.range);
			theBuffer.append(": <strong>");
			theBuffer.append(rangeKey);
			theBuffer.append("</strong><br/>");
		}
		return theBuffer.toString();
	}
}
