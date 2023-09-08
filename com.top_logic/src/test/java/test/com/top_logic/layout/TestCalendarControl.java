/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout;

import static com.top_logic.basic.ArrayUtil.*;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import junit.framework.Test;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.layout.form.control.CalendarControl;
import com.top_logic.layout.form.control.DefaultCalendarMarker;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DefaultPopupDialogModel;
import com.top_logic.layout.structure.PopupDialogModel;

/**
 * Tests the class {@link CalendarControl}
 * 
 * @author <a href="mailto:Diana.Pankratz@top-logic.com">Diana Pankratz</a>
 */
@SuppressWarnings("javadoc")
public class TestCalendarControl extends TestControl {

	private static final String MARKED_GREEN = "markedGreen";

	private static final String MARKED_RED = "markedRed";

	private CalendarControl _calendar;

	private Calendar _lowerBound;

	private Calendar _upperBound;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		PopupDialogModel dialog = new DefaultPopupDialogModel(DefaultLayoutData.DEFAULT_CONSTRAINT);

		ComplexField dateField = FormFactory.newDateField("dateInputControl", new Date(), false);

		TimeZone timezone = getTimeZoneBerlin();

		Locale locale = Locale.GERMANY;

		Calendar cal = CalendarUtil.createCalendar(timezone);

		cal.set(Calendar.DAY_OF_MONTH, 5);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		_lowerBound = (Calendar) cal.clone();
		cal.add(Calendar.DAY_OF_MONTH, 1);
		_upperBound = (Calendar) cal.clone();

		dateField.set(CalendarControl.MARKER_CSS,
			new DefaultCalendarMarker(_lowerBound, _upperBound, false, MARKED_GREEN, MARKED_RED, false));

		_calendar = new CalendarControl(dialog, dateField, timezone, locale);
	}

	public void testSetMark() {
		String content = writeControl(_calendar);
		Element controlElement = DOMUtil.parse(content).getDocumentElement();
		NodeList columns = getCalendarColumns(controlElement);

		for (int i = 0; i < columns.getLength(); i++) {
			// List of days in a column
			NodeList days = columns.item(i).getChildNodes();
			for (int j = 1; j < days.getLength(); j++) {
				testDayMark(days.item(j));
			}
		}
	}

	private NodeList getCalendarColumns(Element controlElement) {
		return DOMUtil.getFirstElementChild(controlElement).getChildNodes().item(1)
			.getFirstChild().getLastChild().getChildNodes();
	}

	private void testDayMark(Node dayItem) {
		Calendar day = parseDateFromOnClick(dayItem);
		String[] cssClasses = getCssClasses(dayItem);

		if (day.after(_lowerBound) && day.before(_upperBound)) {
			assertTrue(contains(cssClasses, MARKED_GREEN));
		} else {
			assertTrue(contains(cssClasses, MARKED_RED));
		}
	}

	private Calendar parseDateFromOnClick(Node dayItem) {
		String[] onclickString = getOnClick(dayItem);
		// The last element of onclickString contains the Millis with some special
		// characters.
		// Removes the special characters.
		long date = Long.parseLong(onclickString[onclickString.length - 1].replaceAll("[^\\d]", ""));
		return CalendarUtil.createCalendar(date);
	}

	private String[] getOnClick(Node dayItem) {
		// onclickString = String of the attribute "onclick" of a <td> element.
		// It contains the date of the day in Millis
		return dayItem.getAttributes().item(1).getNodeValue().split(" ");
	}

	private String[] getCssClasses(Node dayItem) {
		// Splits the attribute "class" of a <td> element to get the CSS-Classes of a day in the
		// calendar
		return dayItem.getAttributes().item(0).getNodeValue().split(" ");
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		_calendar.detach();
		_calendar = null;
		_upperBound = null;
		_lowerBound = null;
	}

	public static Test suite() {
		return TestControl.suite(TestCalendarControl.class);
	}

}
