/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import static com.top_logic.basic.ArrayUtil.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.top_logic.base.services.simpleajax.AJAXConstants;
import com.top_logic.base.services.simpleajax.ElementReplacement;
import com.top_logic.base.services.simpleajax.FragmentInsertion;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.basic.DateUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.AbstractDisplayValue;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.AbstractConstantControl;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.FormFieldInternals;
import com.top_logic.layout.structure.PopupDialogModel;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.css.CssUtil;

/**
 * This class creates an displayable, interactive calendar.
 * 
 * @author     <a href="mailto:STS@top-logic.com">STS</a>
 */
public class CalendarControl extends AbstractConstantControl {
    
	private static final String ROOT_CSS_CLASS = "cal_root";

	private static final String NOW_CSS_CLASS = "cal_now";

	enum Mode {
		CENTURY {
			@Override
			int calendarField() {
				return Calendar.YEAR;
			}

			@Override
			int steps() {
				return 100;
			}

			@Override
			long rangeId(Calendar calendar) {
				return calendar.get(calendarField()) / 10;
			}

			@Override
			CalendarView createView(CalendarControl calendarControl, Date activeDate, Date origDate) {
				return new YearRangeView(calendarControl, activeDate, origDate, this);
			}

			@Override
			ResKey prevKey() {
				return I18NConstants.CALENDAR_PREV_CENTURY;
			}

			@Override
			ResKey nextKey() {
				return I18NConstants.CALENDAR_NEXT_CENTURY;
			}

			@Override
			ResKey upKey() {
				return null;
			}
		},

		DECADE {
			@Override
			int calendarField() {
				return Calendar.YEAR;
			}

			@Override
			int steps() {
				return 10;
			}

			@Override
			long rangeId(Calendar calendar) {
				return calendar.get(calendarField());
			}

			@Override
			CalendarView createView(CalendarControl calendarControl, Date activeDate, Date origDate) {
				return new YearRangeView(calendarControl, activeDate, origDate, this);
			}

			@Override
			ResKey prevKey() {
				return I18NConstants.CALENDAR_PREV_DECADE;
			}

			@Override
			ResKey nextKey() {
				return I18NConstants.CALENDAR_NEXT_DECADE;
			}

			@Override
			ResKey upKey() {
				return I18NConstants.CALENDAR_CENTURY_OVERVIEW;
			}
		},

		YEAR {
			@Override
			int calendarField() {
				return Calendar.YEAR;
			}

			@Override
			long rangeId(Calendar calendar) {
				return calendar.get(Calendar.YEAR) * 1000 + calendar.get(Calendar.MONTH);
			}

			@Override
			int steps() {
				return 1;
			}

			@Override
			CalendarView createView(CalendarControl calendarControl, Date activeDate, Date origDate) {
				return new YearView(calendarControl, activeDate, origDate);
			}

			@Override
			ResKey prevKey() {
				return I18NConstants.CALENDAR_PREV_YEAR;
			}

			@Override
			ResKey nextKey() {
				return I18NConstants.CALENDAR_NEXT_YEAR;
			}

			@Override
			ResKey upKey() {
				return I18NConstants.CALENDAR_DECADE_OVERVIEW;
			}
		},

		MONTH {
			@Override
			int calendarField() {
				return Calendar.MONTH;
			}

			@Override
			int steps() {
				return 1;
			}

			@Override
			long rangeId(Calendar calendar) {
				return calendar.get(Calendar.YEAR) * 1000 + calendar.get(Calendar.DAY_OF_YEAR);
			}

			@Override
			CalendarView createView(CalendarControl calendarControl, Date activeDate, Date origDate) {
				return new MonthView(calendarControl, activeDate, origDate);
			}

			@Override
			ResKey prevKey() {
				return I18NConstants.CALENDAR_PREV_MONTH;
			}

			@Override
			ResKey nextKey() {
				return I18NConstants.CALENDAR_NEXT_MONTH;
			}

			@Override
			ResKey upKey() {
				return I18NConstants.CALENDAR_YEAR_OVERVIEW;
			}
		};

		abstract CalendarView createView(CalendarControl calendarControl, Date activeDate, Date origDate);

		Mode zoomOut() {
			return mode(ordinal() - 1);
		}

		Mode zoomIn() {
			return mode(ordinal() + 1);
		}

		protected Mode mode(int index) {
			int clipped;
			if (index < 0) {
				clipped = 0;
			} else {
				int cnt = values().length;
				if (index >= cnt) {
					clipped = cnt - 1;
				} else {
					clipped = index;
				}
			}
			return Mode.values()[clipped];
		}

		abstract int calendarField();

		abstract int steps();

		abstract long rangeId(Calendar calendar);

		abstract ResKey prevKey();

		abstract ResKey nextKey();

		abstract ResKey upKey();

	}

	enum Navigation {
		NONE {
			@Override
			public int direction() {
				return 0;
			}
		},

		PREVIOUS {
			@Override
			public int direction() {
				return -1;
			}
		},

		NEXT {
			@Override
			public int direction() {
				return 1;
			}
		},

		ZOOM_OUT {
			@Override
			public int direction() {
				return 0;
			}
		},

		ZOOM_IN {
			@Override
			public int direction() {
				return 0;
			}
		},

		SHOW_TODAY {
			@Override
			public int direction() {
				return 0;
			}
		};

		/**
		 * Perform the navigation on the given {@link Calendar} value.
		 */
		public abstract int direction();
	}

	/** The special CSS class to mark a day as non-selectable. */
	public static final String DISABLED_MARKER = "disabled";

	/**
	 * Property of {@link CalendarMarker} to get CSS-classes for marking dates in the date picker.
	 */
	public static final Property<CalendarMarker> MARKER_CSS = TypedAnnotatable.property(CalendarMarker.class, "markerCSS");

    private static final Map<String, ControlCommand> COMMANDS 
        = createCommandMap(new ControlCommand[] {
            new SetCalendarDateCommand(),
            new SwitchCalendarViewCommand()
    });     
        
    private PopupDialogModel _dialog;

    private FormField _dateField;

	/**
	 * @see #getActiveDate()
	 */
    private Date _activeDate;

	/**
	 * @see #getOrigDate()
	 */
    private Date _origDate;
    
	private Mode _mode = Mode.MONTH;

	private CalendarView _view;

	private Navigation _update;

	private Date _oldDate;

	final TimeZone _timeZone;

	final Locale _locale;

	/**
	 * Creates a {@link CalendarControl}.
	 * 
	 * @param dialog
	 *        The dialog in which the control is displayed. The dialog is closed when a new date is
	 *        chosen.
	 * @param dateField
	 *        The {@link FormField} model that is updated, when a new date is chosen.
	 * @param timeZone
	 *        The {@link TimeZone} in which dates should be displayed.
	 * @param locale
	 *        {@link Locale} to use to display dates, e.g. for the calendar week.
	 */
    public CalendarControl(PopupDialogModel dialog, FormField dateField, TimeZone timeZone, Locale locale) {
        super(COMMANDS);
		_dialog = dialog;
		_dateField = dateField;
		_timeZone = timeZone;
		_locale = locale;

		initOrigDate(dateField);
		initActiveDate();
    }

	/**
	 * Retrieve the currently active value from the given field model.
	 */
	private void initOrigDate(FormField dateField) {
		_origDate = getFieldValueOrNull(dateField);
	}

	/**
	 * Retrieve the currently active value from {@link #_dateField}.
	 */
	private void initActiveDate() {
		Date initialValue;
		if (_origDate != null) {
			initialValue = _origDate;
		} else {
			Calendar userNow = CalendarUtil.createCalendarInUserTimeZone();
			Calendar systemNow = CalendarUtil.convertToSystemZone(userNow);

			initialValue = DateUtil.adjustToDayBegin(systemNow);
		}
		setActiveDate(initialValue);
	}
    
	private Date getFieldValueOrNull(FormField dateField) {
		return dateField.hasValue() ? (Date) dateField.getValue() : null;
	}

	/**
	 * The current display {@link Mode}.
	 */
	public Mode getMode() {
		return _mode;
	}

	/**
	 * @see #getMode()
	 */
	public void setMode(Mode mode) {
		_mode = mode;
	}

	/**
	 * The value of the underlying {@link #_dateField}, when this view was opened.
	 */
	public Date getOrigDate() {
		return _origDate;
	}

	FormField getDateField() {
		return _dateField;
	}

	/**
	 * The date that is marked as active in the calendar view.
	 */
	public Date getActiveDate() {
		return _activeDate;
	}

	/**
	 * @see #getActiveDate()
	 */
	public void setActiveDate(Date activeDate) {
		_activeDate = activeDate;
	}

	@Override
	protected String getTypeCssClass() {
		return "cCalendar";
	}

    @Override
    protected void internalWrite(DisplayContext context, TagWriter out)
            throws IOException {
		_view = createView();

		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		out.endBeginTag();
		_view.write(context, out);
		out.endTag(DIV);
	}

	private CalendarView createView() {
		return _mode.createView(this, getActiveDate(), _origDate);
	}

	abstract static class CalendarView implements HTMLFragment {

		private static final String CAL_VIEW = "cal_view";

		private static final String CONTENT_CSS_CLASS = "cal_content";

		private static final String TITLEBAR_CSS_CLASS = "cal_titlebar";

		private static final String TABLE_CSS_CLASS = "cal_table";

		private static final String HEADER_CSS_CLASS = "cal_header";

		private static final String LEFT_NAVIGATION_CSS_CLASS = "cal_leftNav";

		private static final String RIGHT_NAVIGATION_CSS_CLASS = "cal_rightNav";

		private static final String NEXT_BUTTON_CSS_CLASS = "cal_next";

		private static final String PREV_BUTTON_CSS_CLASS = "cal_prev";

		protected static final String CELL_CSS_CLASS = "cal_cell";

		private static final String SELECTED_CSS_CLASS = "cal_sel";

		protected static final String OTHER_RANGE_CSS_CLASS = "cal_om";

		/**
		 * {@link #rangeId(Calendar)} compatible value for "no day".
		 */
		private static final long NO_DAY_ID = Long.MIN_VALUE;

		protected final CalendarControl _control;

		protected final Mode _mode;

		protected Calendar _calendar;

		protected long _todayDayId;

		protected long _origDayId;

		protected final Date _current;

		protected final Date _orig;

		private String _id;

		public CalendarView(CalendarControl control, Mode mode, Date month, Date origDay) {
			_control = control;
			_mode = mode;

			_current = month;
			_orig = origDay;
		}

		/**
		 * The identifier of the top-level DOM element.
		 */
		public String getId() {
			return _id;
		}

		public abstract int count();

		protected abstract void initCalendar(Calendar calendar);
		
	    /**
		 * Whether the given {@link Calendar} points to the day identified by the given day ID.
		 * 
		 * @param dayId
		 *        The ID of the day to compare with, see {@link #rangeId(Calendar)}.
		 * @param current
		 *        The {@link Calendar} to test.
		 * @return Whether the given calendar points to the given day.
		 */
		boolean isSameDay(long dayId, Calendar current) {
			return rangeId(current) == dayId;
		}
	    
		/**
		 * Computes an identifier for the range in which the given date is located.
		 * 
		 * @param calendar
		 *        The source date.
		 * @return The day ID for the given calendar's day.
		 */
		final long rangeId(Calendar calendar) {
			return _mode.rangeId(calendar);
		}

		@Override
		public void write(DisplayContext context, TagWriter out) throws IOException {
			init();

			out.beginBeginTag(DIV);
			out.writeAttribute(ID_ATTR, _id = createId(context));
			out.writeAttribute(CLASS_ATTR, CAL_VIEW);
			out.endBeginTag();

			writeContent(context, out);

			out.endTag(DIV);
		}

		private String createId(DisplayContext context) {
			return context.getExecutionScope().getFrameScope().createNewID();
		}

		private void init() {
			Calendar calendar = CalendarUtil.createCalendarInUserTimeZone(_control._locale);
			calendar = CalendarUtil.convertToTimeZone(calendar, _control._timeZone);

			_calendar = calendar;
			_todayDayId = rangeId(calendar);

			long origDayId;
			if (_orig != null) {
				// Clear out irrelevant detail.
				_calendar.setTime(_orig);
				origDayId = rangeId(_calendar);
			} else {
				origDayId = NO_DAY_ID;
			}
			_origDayId = origDayId;

			initCalendar(_calendar);
		}

		protected void writeContent(DisplayContext context, TagWriter out) throws IOException {
			out.beginBeginTag(DIV);
			out.writeAttribute(CLASS_ATTR, TITLEBAR_CSS_CLASS);
			out.endBeginTag();
			{
				writeTitleContent(context, out);
			}
			out.endTag(DIV);

			out.beginBeginTag(DIV);
			out.writeAttribute(CLASS_ATTR, CONTENT_CSS_CLASS);
			out.endBeginTag();
			{
				writeTable(out);
			}
			out.endTag(DIV);
		}

		protected void writeTitleContent(DisplayContext context, TagWriter out) throws IOException {
			out.beginBeginTag(DIV);
			out.writeAttribute(CLASS_ATTR, LEFT_NAVIGATION_CSS_CLASS);
			out.endBeginTag();
			writeLeftNavigation(context, out);
			out.endTag(DIV);

			out.beginBeginTag(DIV);
			out.writeAttribute(CLASS_ATTR, RIGHT_NAVIGATION_CSS_CLASS);
			out.endBeginTag();
			writeRightNavigation(context, out);
			out.endTag(DIV);

			out.beginBeginTag(DIV);
			out.writeAttribute(CLASS_ATTR, HEADER_CSS_CLASS);
			out.endBeginTag();
			{
				String tooltipToday = context.getResources().getString(I18NConstants.CALENDAR_SHOW_TODAY);

				out.beginBeginTag(ANCHOR);
				_control.writeOnClickDirection(out, new Date(), null, Mode.MONTH);
				OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, tooltipToday);
				out.writeAttribute(HREF_ATTR, "#");
				out.endBeginTag();
				{
					Icons.TODAY.writeWithCss(context, out, FormConstants.INPUT_IMAGE_CSS_CLASS);
				}
				out.endTag(ANCHOR);

				out.writeText(NBSP);

				ResKey upKey = _mode.upKey();
				if (upKey != null) {
					String tooltip = context.getResources().getString(upKey);

					out.beginBeginTag(ANCHOR);
					_control.writeOnClickDirection(out, Navigation.ZOOM_OUT, null);
					OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, tooltip);
					out.writeAttribute(HREF_ATTR, "#");
					out.endBeginTag();
				}
				{
					writeHeader(context, out);

					if (_mode != Mode.CENTURY) {
						out.writeText(NBSP);
						Icons.ARROW_UP.writeWithCss(context, out, FormConstants.INPUT_IMAGE_CSS_CLASS);
					}
				}
				if (upKey != null) {
					out.endTag(ANCHOR);
				}
			}
			out.endTag(DIV);
		}

		protected abstract void writeLeftNavigation(DisplayContext context, TagWriter out) throws IOException;

		protected abstract void writeRightNavigation(DisplayContext context, TagWriter out) throws IOException;

		protected abstract void writeHeader(DisplayContext context, TagWriter out) throws IOException;

		protected void writeTable(TagWriter out) throws IOException {
			out.beginBeginTag(TABLE);
			CssUtil.writeCombinedCssClasses(out, TABLE_CSS_CLASS, getTableCssClass());
			out.endBeginTag();

			writeHeader(out);
			writeBody(out);

			out.endTag(TABLE);
		}

		private void writeHeader(TagWriter out) throws IOException {
			out.beginTag(THEAD);
			writeHeaderRow(out);
			out.endTag(THEAD);
		}

		private void writeBody(TagWriter out) throws IOException {
			out.beginTag(TBODY);
			writeTableContents(out);
			out.endTag(TBODY);
		}

		protected String getTableCssClass() {
			return null;
		}

		protected void writeHeaderRow(TagWriter out) throws IOException {
			beginHeaderRow(out);
			writeHeaderRowContents(out);
			endHeaderRow(out);
		}

		/**
		 * Write the beginning of the calendar header row.
		 * 
		 * @throws IOException
		 *         If writing fails.
		 */
		protected void beginHeaderRow(TagWriter out) throws IOException {
			out.beginTag(TR);
		}

		/**
		 * Write the end tag of the calendar header row.
		 * 
		 * @throws IOException
		 *         If writing fails.
		 */
		protected void endHeaderRow(TagWriter out) throws IOException {
			out.endTag(TR);
		}

		protected abstract void writeHeaderRowContents(TagWriter out) throws IOException;

		protected abstract void writeTableContents(TagWriter out) throws IOException;

		protected void writeYear(TagWriter out) throws IOException {
			int activeYear = _calendar.get(Calendar.YEAR);
			out.writeInt(activeYear);
		}

		protected final void writeNavigationPrev(DisplayContext context, TagWriter out, Navigation navigation, Mode mode)
				throws IOException {
			writeNavigationPrev(context, out, navigation, mode, Icons.ARROW_LEFT);
		}

		protected final void writeNavigationPrev(DisplayContext context, TagWriter out, Navigation navigation,
				Mode mode, ThemeImage icon) throws IOException {
			writeNavigationButton(context, out, navigation, mode, mode.prevKey(), icon, PREV_BUTTON_CSS_CLASS);
		}

		protected final void writeNavigationNext(DisplayContext context, TagWriter out, Navigation navigation, Mode mode)
				throws IOException {
			writeNavigationNext(context, out, navigation, mode, Icons.ARROW_RIGHT);
		}

		protected final void writeNavigationNext(DisplayContext context, TagWriter out, Navigation navigation,
				Mode mode, ThemeImage icon) throws IOException {
			writeNavigationButton(context, out, navigation, mode, mode.nextKey(), icon, NEXT_BUTTON_CSS_CLASS);
		}

		protected void writeNavigationButton(DisplayContext context, TagWriter out, Navigation navigation, Mode mode,
				ResKey resourceKey, ThemeImage icon, String cssClass) throws IOException {
			out.beginBeginTag(ANCHOR);
			out.writeAttribute(HREF_ATTR, "#");
			_control.writeOnClickDirection(out, navigation, mode);
			out.endBeginTag();
			{
				icon.writeWithCssTooltip(context, out,
					CssUtil.joinCssClasses(FormConstants.INPUT_IMAGE_CSS_CLASS, cssClass),
					context.getResources().getString(resourceKey));
			}
			out.endTag(ANCHOR);
		}

		protected void writeCellClass(TagWriter out, String additionalClass) throws IOException {
			String selectedClass;
			if (isSameDay(_origDayId, _calendar)) {
				selectedClass = SELECTED_CSS_CLASS;
			} else {
				selectedClass = null;
			}

			String todayClass;
			if (isSameDay(_todayDayId, _calendar)) {
				todayClass = NOW_CSS_CLASS;
			} else {
				todayClass = null;
			}

			CssUtil.writeCombinedCssClasses(out, CELL_CSS_CLASS,
				CssUtil.joinCssClasses(selectedClass, todayClass), additionalClass);
		}

		/**
		 * The number of valied entries in the given {@link Calendar} field.
		 */
		protected static int fieldRange(Calendar calendar, int field) {
			return calendar.getMaximum(field) - calendar.getMinimum(field) + 1;
		}

	}

	static class YearView extends RangeView {

		public YearView(CalendarControl control, Date month, Date origDay) {
			super(control, Mode.YEAR, month, origDay);
		}

		@Override
		public int count() {
			return fieldRange(_calendar, Calendar.MONTH);
		}

		@Override
		protected void writeLeftNavigation(DisplayContext context, TagWriter out) throws IOException {
			writeNavigationPrev(context, out, Navigation.PREVIOUS, Mode.YEAR);
		}

		@Override
		protected void writeRightNavigation(DisplayContext context, TagWriter out) throws IOException {
			writeNavigationNext(context, out, Navigation.NEXT, Mode.YEAR);
		}

		@Override
		protected void writeHeader(DisplayContext context, TagWriter out) throws IOException {
			writeYear(out);
		}

		@Override
		protected void writeTableContents(TagWriter out) throws IOException {
			SimpleDateFormat monthFormat = CalendarUtil.newSimpleDateFormat("MMM", _control._locale);
			monthFormat.setTimeZone(_control._timeZone);

			writeTable(out, Calendar.MONTH, 1, monthFormat);
		}

		@Override
		protected void initCalendar(Calendar calendar) {
			calendar.setTime(_current);
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			calendar.set(Calendar.MONTH, calendar.getMinimum(Calendar.MONTH));
		}

	}

	abstract static class RangeView extends CalendarView {

		private static final String FIXED_CSS_CLASS = "cal_fixed";

		public RangeView(CalendarControl control, Mode mode, Date month, Date origDay) {
			super(control, mode, month, origDay);
		}

		@Override
		protected String getTableCssClass() {
			return FIXED_CSS_CLASS;
		}

		@Override
		protected void writeHeaderRow(TagWriter out) throws IOException {
			// No header.
		}

		@Override
		protected void writeHeaderRowContents(TagWriter out) throws IOException {
			// No header.
		}

		protected void writeTable(TagWriter out, int unit, int step, SimpleDateFormat format)
				throws IOException {
			initCalendar(_calendar);
			final int colums = 3;

			CalendarMarker calendarMarker = _control.getDateField().get(MARKER_CSS);
			Calendar rangeEnd = (Calendar) _calendar.clone();
			rangeEnd.add(unit, step);
			String cssString = "";


			out.beginTag(TR);
			int col = 0;
			for (int n = 0, cnt = count(); n < cnt; n++) {
				if (col == colums) {
					out.endTag(TR);
					out.beginTag(TR);
					col = 0;
				}

				out.beginBeginTag(TD);
				if (calendarMarker != null) {
					cssString = calendarMarker.getCss(_calendar, rangeEnd);
				}
				writeCellClass(out, getRangeClass(n, cnt) + " " + cssString);
				String[] cssClasses = cssString.split(" ");
				if (!Arrays.asList(cssClasses).contains("disabled")) {
					_control.writeOnClickDirection(out, _calendar.getTime(), Navigation.ZOOM_IN, null);
				}
				out.endBeginTag();
				{
					out.beginBeginTag(ANCHOR);
					out.writeAttribute(HREF_ATTR, "#");
					out.endBeginTag();
					{
						writeCellContent(out, format);
					}
					out.endTag(ANCHOR);
				}
				out.endTag(TD);

				_calendar.add(unit, step);
				rangeEnd.add(unit, step);
				col++;
			}
			out.endTag(TR);
		}

		/**
		 * CSS class for cell.
		 * 
		 * @param index
		 *        The index of the rendered range.
		 * @param cnt
		 *        The total number of ranges rendered.
		 * @return CSS class to use, or <code>null</code> for no class.
		 */
		protected String getRangeClass(int index, int cnt) {
			return null;
		}

		/**
		 * Write contents of a single day cell of the {@link MonthView}.
		 * 
		 * @throws IOException
		 *         If writing fails.
		 */
		protected void writeCellContent(TagWriter out, SimpleDateFormat format) throws IOException {
			out.writeText(format.format(_calendar.getTime()));
		}

	}

	static class YearRangeView extends RangeView {

		private final SimpleDateFormat _yearFormat;

		private final int _step;

		public YearRangeView(CalendarControl control, Date month, Date origDay, Mode mode) {
			super(control, mode, month, origDay);

			_step = _mode.steps() / 10;

			_yearFormat = CalendarUtil.newSimpleDateFormat("yyyy", control._locale);
		}

		@Override
		public int count() {
			// Two ranges overlap.
			return 10 + 2;
		}

		@Override
		protected void writeLeftNavigation(DisplayContext context, TagWriter out) throws IOException {
			writeNavigationPrev(context, out, Navigation.PREVIOUS, _mode);
		}

		@Override
		protected void writeRightNavigation(DisplayContext context, TagWriter out) throws IOException {
			writeNavigationNext(context, out, Navigation.NEXT, _mode);
		}

		@Override
		protected void writeHeader(DisplayContext context, TagWriter out) throws IOException {
			initCalendar(_calendar);
			
			// One overlap at the beginning of the range.
			_calendar.add(Calendar.YEAR, _step);

			out.writeText(_yearFormat.format(_calendar.getTime()));
			out.write(" - ");

			// One overlap at the end of the range.
			_calendar.add(Calendar.YEAR, (count() - 2) * _step - 1);

			out.writeText(_yearFormat.format(_calendar.getTime()));
		}

		@Override
		protected void writeTableContents(TagWriter out) throws IOException {
			writeTable(out, Calendar.YEAR, _step, _yearFormat);
		}

		@Override
		protected String getRangeClass(int index, int cnt) {
			// First and last entry are overlap entries.
			return index == 0 || index == cnt - 1 ? OTHER_RANGE_CSS_CLASS : null;
		}

		@Override
		protected void writeCellContent(TagWriter out, SimpleDateFormat format) throws IOException {

			if (_step > 1) {
				out.beginBeginTag(SPAN);
				out.writeAttribute(CLASS_ATTR, "cal_range");
				out.endBeginTag();
				{
					out.beginBeginTag(SPAN);
					out.writeAttribute(CLASS_ATTR, "cal_rangePart");
					out.endBeginTag();
					{
						super.writeCellContent(out, format);
						out.writeText(" - ");
					}
					out.endTag(SPAN);

					out.beginBeginTag(SPAN);
					out.writeAttribute(CLASS_ATTR, "cal_rangePart");
					out.endBeginTag();
					{
						long before = _calendar.getTimeInMillis();
						_calendar.add(Calendar.YEAR, _step - 1);
						super.writeCellContent(out, format);
						_calendar.setTimeInMillis(before);
					}
					out.endTag(SPAN);
				}
				out.endTag(SPAN);
			} else {
				super.writeCellContent(out, format);
			}
		}

		@Override
		protected void initCalendar(Calendar calendar) {
			calendar.setTime(_current);
			int startYear = _calendar.get(Calendar.YEAR);
			startYear -= startYear % _mode.steps();
			// Start with one range overlap
			startYear -= _step;
			calendar.set(Calendar.YEAR, startYear);
			// Set start date of year
			calendar.set(Calendar.MONTH, Calendar.JANUARY);
			calendar.set(Calendar.DAY_OF_MONTH, 1);
		}

	}

	/**
	 * {@link CalendarView} writing a calendar view of a single month.
	 */
	static class MonthView extends CalendarView {

		private static final String COLUMN_HEADER_CSS_CLASS = "cal_th";

		private static final String WEEKNO_CSS_CLASS = "cal_wn";

		private static final String WEEKDAY_CSS_CLASS = "cal_wd";

		private static final String WEEKEND_CSS_CLASS = "cal_we";

		private final SimpleDateFormat _monthFormat;

		/**
		 * Creates a {@link MonthView}.
		 */
		public MonthView(CalendarControl control, Date month, Date origDay) {
			super(control, Mode.MONTH, month, origDay);

			_monthFormat = CalendarUtil.newSimpleDateFormat("MMMM", control._locale);
		}

		@Override
		public int count() {
			return _calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
				- _calendar.getActualMinimum(Calendar.DAY_OF_MONTH) + 1;
		}

		@Override
		protected void initCalendar(Calendar calendar) {
			calendar.setTime(_current);
			calendar.set(Calendar.DAY_OF_MONTH, 1);
		}

		@Override
		protected void writeLeftNavigation(DisplayContext context, TagWriter out) throws IOException {
			writeNavigationPrev(context, out, Navigation.PREVIOUS, Mode.YEAR, Icons.FAST_BACKWARD);
			writeNavigationPrev(context, out, Navigation.PREVIOUS, Mode.MONTH);
		}

		@Override
		protected void writeHeader(DisplayContext context, TagWriter out) throws IOException {
			writeMonth(out);
			out.writeText(' ');
			writeYear(out);
		}

		@Override
		protected void writeRightNavigation(DisplayContext context, TagWriter out) throws IOException {
			writeNavigationNext(context, out, Navigation.NEXT, Mode.MONTH);
			writeNavigationNext(context, out, Navigation.NEXT, Mode.YEAR, Icons.FAST_FORWARD);
		}

		/**
		 * Writes the name of the current month.
		 * 
		 * @throws IOException
		 *         If writing fails.
		 */
		private void writeMonth(TagWriter out) throws IOException {
			String activeMonth = _monthFormat.format(_calendar.getTime());

			out.writeText(activeMonth);
		}

		@Override
		protected void writeTableContents(TagWriter out) throws IOException {
			out.beginTag(TR);

			writeWeekCell(out);

			int firstDayOfWeek = _calendar.getFirstDayOfWeek();
			int dayOfFirstDayInMonth = _calendar.get(Calendar.DAY_OF_WEEK);

			int daysInWeek = daysInWeek();
			int daysInLastMonth = (dayOfFirstDayInMonth - firstDayOfWeek + daysInWeek) % daysInWeek;
			int daysInMonth = count();
			int sixWeeks = 6 * 7;

			_calendar.add(Calendar.DAY_OF_MONTH, -daysInLastMonth);
			Calendar rangeEnd = (Calendar) _calendar.clone();
			rangeEnd.add(Calendar.DAY_OF_MONTH, 1);

			// now for the real days of this month:
			for (int n = -daysInLastMonth, cnt = sixWeeks - daysInLastMonth; n < cnt; n++) {
				writeDayCell(out, n < 0 || n >= daysInMonth, rangeEnd);

				// increase the Calendar
				_calendar.add(Calendar.DAY_OF_MONTH, 1);
				rangeEnd.add(Calendar.DAY_OF_MONTH, 1);

				// On monday begin a new line and enter the calendar week entry.
				int dayOfWeek = _calendar.get(Calendar.DAY_OF_WEEK);
				if (dayOfWeek == firstDayOfWeek && (n + 1 < cnt)) {
					out.endTag(TR);
					out.beginTag(TR);

					writeWeekCell(out);
				}
			}
			out.endTag(TR);
		}

		@Override
		protected void writeHeaderRowContents(TagWriter out) throws IOException {
			String[] dayNames = _monthFormat.getDateFormatSymbols().getShortWeekdays();
			{
				out.beginBeginTag(TH);
				CssUtil.writeCombinedCssClasses(out, COLUMN_HEADER_CSS_CLASS, WEEKNO_CSS_CLASS);
				out.endBeginTag();
				out.endTag(TH);

				int offset = _calendar.getMinimum(Calendar.DAY_OF_WEEK);
				int daysInWeek = daysInWeek();
				int firstDayOfWeek = _calendar.getFirstDayOfWeek();
				for (int n = 0, cnt = daysInWeek; n < cnt; n++) {
					out.beginBeginTag(TH);
					out.writeAttribute(CLASS_ATTR, COLUMN_HEADER_CSS_CLASS);
					out.endBeginTag();
					out.writeText(dayNames[offset + (firstDayOfWeek + n - offset) % daysInWeek]);
					out.endTag(TH);
				}
			}
		}

		private int daysInWeek() {
			return fieldRange(_calendar, Calendar.DAY_OF_WEEK);
		}

		private void writeWeekCell(TagWriter out) throws IOException {
			out.beginBeginTag(TD);
			CssUtil.writeCombinedCssClasses(out, CELL_CSS_CLASS, WEEKNO_CSS_CLASS);
			out.endBeginTag();
			out.writeText(Integer.toString(_calendar.get(Calendar.WEEK_OF_YEAR)));
			out.endTag(TD);
		}


		/**
		 * Writes the days in a date picker.
		 * <p>
		 * Creates day cells in a date picker. The used CSS-classes depend on the day of the week
		 * and user settings. The day cells are only selectable if the 'disabled' CSS-class is not
		 * set.
		 * </p>
		 * 
		 * @param out
		 *        The stream the output is written to.
		 * @param otherMonth
		 *        Defines if the day is in the current month.
		 * @param rangeEnd
		 *        Defines the end time of the day.
		 * @see CalendarMarker
		 * @see #MARKER_CSS
		 */
		private void writeDayCell(TagWriter out, boolean otherMonth, Calendar rangeEnd) throws IOException {
			int dayOfWeek = _calendar.get(Calendar.DAY_OF_WEEK);

			String weekEndClass;
			if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
				weekEndClass = WEEKEND_CSS_CLASS;
			} else {
				weekEndClass = WEEKDAY_CSS_CLASS;
			}

			out.beginBeginTag(TD);
			String markerCssClasses = getMarkerCssClasses(rangeEnd);
			writeCellClass(out, joinRelevantClasses(weekEndClass, otherMonth, markerCssClasses));

			if (!containsDisabledMarker(markerCssClasses)) {
				_control.writeOnClickDay(out, _calendar.getTime());
			}

			out.endBeginTag();
			{
				out.beginBeginTag(SPAN);
				out.endBeginTag();
				{
					out.writeText(Integer.toString(_calendar.get(Calendar.DAY_OF_MONTH)));
				}
				out.endTag(SPAN);
			}
			out.endTag(TD);
		}

		private String joinRelevantClasses(String weekEndClass, boolean otherMonth, String markerCss) {
			String otherMonthClass = otherMonth ? OTHER_RANGE_CSS_CLASS : null;
			return CssUtil.joinCssClasses(weekEndClass, otherMonthClass, markerCss);
		}

		private String getMarkerCssClasses(Calendar rangeEnd) {
			CalendarMarker calendarMarker = _control.getDateField().get(MARKER_CSS);
			if (calendarMarker == null) {
				return "";
			}
			return calendarMarker.getCss(_calendar, rangeEnd);
		}

		/** Whether the given CSS classes contain the {@link CalendarControl#DISABLED_MARKER} marker. */
		private boolean containsDisabledMarker(String cssString) {
			String[] cssClasses = cssString.split(" ");
			return contains(cssClasses, DISABLED_MARKER);
		}

	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		HTMLUtil.appendCSSClass(out, ROOT_CSS_CLASS);
	}

	void writeOnClickDirection(TagWriter out, Navigation navigation, Mode mode) throws IOException {
		writeOnClickDirection(out, null, navigation, mode);
	}

	void writeOnClickDirection(TagWriter out, Date date, Navigation navigation, Mode mode) throws IOException {
		out.beginAttribute(ONCLICK_ATTR);
		out.append("return ");
		out.append(FormConstants.CALENDAR_DIALOG_HANDLER_CLASS);
		out.append(".");
		out.append(SwitchCalendarViewCommand.COMMAND_NAME);
		out.append("(");
		writeIdJsString(out);
		out.append(", ");
		out.writeJsString(date == null ? null : String.valueOf(date.getTime()));
		out.append(", ");
		out.writeJsString(navigation == null ? null : navigation.name());
		out.append(", ");
		out.writeJsString(mode == null ? null : mode.name());
		out.append(")");
		out.endAttribute();
	}

	void writeOnClickDay(TagWriter out, Date day) throws IOException {
		out.beginAttribute(ONCLICK_ATTR);
		out.append("return ");
		out.append(FormConstants.CALENDAR_DIALOG_HANDLER_CLASS);
		out.append(".");
		out.append(SetCalendarDateCommand.COMMAND_NAME);
		out.append("('");
		out.append(getID());
		out.append("', '");
		out.append(String.valueOf(day.getTime()));
		out.append("')");
		out.endAttribute();
	}

	void updateDate(final Object rawDate) {
		updateField((AbstractFormField) getDateField(), rawDate);
	}

	void updateActiveDate(final Object rawDate) {
		setActiveDate(getDate(rawDate));
	}

	private Date getDate(final Object rawDate) {
		return new Date(Long.valueOf((String) rawDate));
	}

	private void updateField(final AbstractFormField target, final Object rawDate)
			throws UnreachableAssertion {
		try {
			FormFieldInternals.setValue(target, getDate(rawDate));
		} catch (VetoException ex) {
			ex.setContinuationCommand(createUpdateContinuation(target, getDate(rawDate)));
			ex.process(getWindowScope());
		}
	}

	private static Command createUpdateContinuation(final AbstractFormField target, final Date date) {
		return new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				target.setValue(date);
				return HandlerResult.DEFAULT_RESULT;
			}
		};
	}

	void closeCalendar() {
		if (_dialog != null) {
			_dialog.setClosed();
		}
	}

    /**
	 * {@link Command} updating the date value to the the selected date.
	 * 
	 * @author <a href=mailto:STS@top-logic.com>STS</a>
	 */
    public static final class SetCalendarDateCommand extends ControlCommand {
        
        private static final String COMMAND_NAME = "setCalendarDate";
                        
		/**
		 * Creates a {@link SetCalendarDateCommand}.
		 */
        public SetCalendarDateCommand() {
            super(COMMAND_NAME);
        }
                        
        @Override
        protected HandlerResult execute(DisplayContext commandContext,
                                        Control control, Map<String, Object> arguments) {
            
			final CalendarControl calendarControl = (CalendarControl) control;
			calendarControl.closeCalendar();
            
            // Set selected date in input field
			final Object selectedDate = arguments.get("selectedDate");
			calendarControl.updateDate(selectedDate);
            
            return HandlerResult.DEFAULT_RESULT;
        }

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.SET_CALENDAR_DATE;
		}
    }
            
    /**
	 * {@link ControlCommand} switching the year or month view of the current calendar.
	 * 
	 * @author <a href=mailto:STS@top-logic.com>STS</a>
	 */
    public static final class SwitchCalendarViewCommand extends ControlCommand {
        
		private static final String COMMAND_NAME = "switchCalendarView";

		private static final Object DATE_PARAM = "date";

		private static final String NAVIGATION_PARAM = "navigation";
		
		private static final String STEP_PARAM = "step";

		/**
		 * Creates a {@link SwitchCalendarViewCommand}.
		 */
        public SwitchCalendarViewCommand() {
            super(COMMAND_NAME);
        }
                                
        @Override
        protected HandlerResult execute(DisplayContext commandContext,
                                        Control control, Map<String, Object> arguments) {
                                                    
            CalendarControl calendarControl = (CalendarControl)control;
            
			if (arguments == null) {
				return HandlerResult.DEFAULT_RESULT;
			}
			
			final Object newDate = arguments.get(DATE_PARAM);
			String navigationArg = (String) arguments.get(NAVIGATION_PARAM);
			if (newDate != null) {
				String stepArg = (String) arguments.get(STEP_PARAM);
				Mode newMode;
				if (stepArg != null) {
					newMode = Mode.valueOf(stepArg);
				} else {
					newMode = calendarControl.getMode();
				}

				if (navigationArg == null) {
					Date oldDate = calendarControl.getActiveDate();

					Calendar calendar =
						CalendarUtil.createCalendar(commandContext.getSubSessionContext().getCurrentLocale());
					Navigation navigation;
					if (newMode == calendarControl.getMode()) {
						Mode parentMode = newMode.zoomOut();
						long nowId = parentMode.rangeId(calendar);

						calendar.setTime(oldDate);
						long currentId = parentMode.rangeId(calendar);
						navigation =
							(currentId < nowId ? Navigation.NEXT :
								(currentId > nowId ? Navigation.PREVIOUS : Navigation.NONE));
					} else {
						navigation = Navigation.ZOOM_IN;
					}

					calendarControl.setMode(newMode);
					calendarControl.requestAnimate(navigation, oldDate);
				}

				calendarControl.updateActiveDate(newDate);
			}

			if (navigationArg != null) {
				Navigation navigation = Navigation.valueOf(navigationArg);
				
				Date oldDate = calendarControl.getActiveDate();

                // Setup current date
				Calendar switchCalendar = CalendarUtil.createCalendar(oldDate);

				Mode mode = calendarControl.getMode();
				
				Mode stepMode;
				String stepArg = (String) arguments.get(STEP_PARAM);
				if (stepArg != null) {
					stepMode = Mode.valueOf(stepArg);
				} else {
					stepMode = mode;
				}
				switchCalendar.add(stepMode.calendarField(), stepMode.steps() * navigation.direction());
				
				if (navigation != Navigation.ZOOM_OUT || mode != Mode.CENTURY) {
					if (navigation == Navigation.ZOOM_OUT) {
						calendarControl.setMode(mode.zoomOut());
					} else if (navigation == Navigation.ZOOM_IN) {
						calendarControl.setMode(mode.zoomIn());
					}
	                    
					calendarControl.requestAnimate(navigation, oldDate);
				}
				calendarControl.setActiveDate(switchCalendar.getTime());
            }
            
            return HandlerResult.DEFAULT_RESULT;
        }

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.SWITCH_CALENDAR_VIEW;
		}
    }

	@Override
	protected boolean hasUpdates() {
		return _update != null && _update != Navigation.NONE;
	}

	@Override
	protected void internalRevalidate(DisplayContext context, UpdateQueue actions) {
		super.internalRevalidate(context, actions);
		if (_update != null) {
			final Navigation pendingUpdate = _update;
			_update = null;

			final CalendarView oldView = _view;
			final CalendarView newView = createView();

			_view = newView;
			if (pendingUpdate == Navigation.NONE) {
				actions.add(new ElementReplacement(oldView.getId(), newView));
			} else {
				HTMLFragment animationFrame = Fragments.div("cal_animation", newView);
				actions.add(
					new FragmentInsertion(getID(), AJAXConstants.AJAX_POSITION_END_VALUE, animationFrame));

				final Mode mode = _mode;
				final Date oldDate = _oldDate;
				final Date activeDate = _activeDate;
				actions.add(new JSSnipplet(
					new AbstractDisplayValue() {
						@Override
						public void append(DisplayContext actionContext, Appendable out) throws IOException {
							int newPosition = pendingUpdate == Navigation.PREVIOUS ? -1 : (
									pendingUpdate == Navigation.NEXT ? 1 : 0);
							
							boolean animateOld = pendingUpdate == Navigation.ZOOM_OUT;

							int x = 0;
							int y = 0;

							if (pendingUpdate == Navigation.ZOOM_OUT) {
								Mode oldMode = mode.zoomIn();
								Calendar calendar = CalendarUtil.createCalendar(_timeZone, _locale);

								calendar.setTime(oldDate);
								int oldIndex = calendar.get(oldMode.calendarField());
								newView.initCalendar(calendar);
								int newIndex = calendar.get(oldMode.calendarField());

								int offset = (oldIndex - newIndex) / oldMode.steps();
								y = offset / 3;
								x = offset % 3;
							}
							else if (pendingUpdate == Navigation.ZOOM_IN) {
								Calendar calendar = CalendarUtil.createCalendar(_timeZone, _locale);

								calendar.setTime(activeDate);
								int newIndex = calendar.get(mode.calendarField());
								oldView.initCalendar(calendar);
								int offsetIndex = calendar.get(mode.calendarField());

								int offset = (newIndex - offsetIndex) / mode.steps();
								y = offset / 3;
								x = offset % 3;
							}

							out.append(FormConstants.CALENDAR_DIALOG_HANDLER_CLASS);
							out.append(".animate(");
							TagUtil.writeJsString(out, newView.getId());
							out.append(", ");
							TagUtil.writeJsString(out, oldView.getId());
							out.append(", ");
							StringServices.append(out, newPosition);
							out.append(", ");
							out.append(Boolean.toString(animateOld));
							out.append(", ");
							TagUtil.writeJsString(out, "cal_" + pendingUpdate.name().toLowerCase() + "Animation");
							out.append(", ");
							StringServices.append(out, x);
							out.append(", ");
							StringServices.append(out, y);
							out.append(");");
						}
					}));
			}
		}
	}

	void requestAnimate(Navigation navigation, Date oldDate) {
		_update = navigation;
		_oldDate = oldDate;
	}

}
