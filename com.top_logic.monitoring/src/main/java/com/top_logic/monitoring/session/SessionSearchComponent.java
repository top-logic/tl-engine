/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.session;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.base.time.TimeRangeIterator;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.DateUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.constraints.ComparisonDependency;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.mig.html.I18NResourceProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.TLContext;

/**
 * Search for user sessions. More precisely passes the search params to the
 * UserMonitorComponent.
 *
 * @author     <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class SessionSearchComponent extends FormComponent implements Selectable {

	/** The name of the start date. */
	public static final String	START_DATE_FIELD	    = "startDate";
	/** The name of the end date. */
	public static final String	END_DATE_FIELD	        = "endDate";
	/** The Name of the time range selection field */
	public static final String	TIME_RANGE_FIELD	    = "timeRange";
	/** The name of the checkbox for fixed or relative timeranges */
	public static final String	RELATIVE_RANGES_FIELD	= "relativeRanges";

	/** Strings for time range selections */
	public static final String	DAILY	                = "daily";
	public static final String	WEEKLY	                = "weekly";
	public static final String	MONTHLY	                = "monthly";
	public static final String	QUARTERLY	            = "quarterly";
	public static final String	ANNUALLY	            = "annually";
	public static final String	MANUALLY	            = "manually";
	public static final String	CURRENT	                = "currentDay";
	public static final String	ABSOLUTE	            = "absolute";

	/**
	 * Configuration options for {@link SessionSearchComponent}.
	 */
	public interface Config extends FormComponent.Config {
		// Sum interface.
	}

	/**
	 * Ctor for layout framework
	 *
	 * @param atts
	 *        the xml attributes
	 */
	public SessionSearchComponent(InstantiationContext context, Config atts) throws ConfigurationException {
		super(context, atts);
	}

	@Override
	protected void becomingVisible() {
	    super.becomingVisible();
	    update();
	}

	@Override
	public FormContext createFormContext() {
		FormContext theContext = new FormContext("search", this.getResPrefix());

		this.addTimeRangeSelectionFields(theContext, getResPrefix());

		return theContext;
	}

	public void addTimeRangeSelectionFields(FormContext aContext, ResPrefix resPrefix) {
		// select field for time range
		List theTimeRangeOptions = Arrays.asList((new String[] { DAILY, WEEKLY, MONTHLY, QUARTERLY, ANNUALLY, MANUALLY }));
		SelectField theTimeRange = FormFactory.newSelectField(TIME_RANGE_FIELD, theTimeRangeOptions);
		theTimeRange.setOptionLabelProvider(new I18NResourceProvider(resPrefix));
		theTimeRange.setMandatory(true);

		// the field for choosing between fixed and relative timeranges
		BooleanField theTimeRangeChoice = FormFactory.newBooleanField(RELATIVE_RANGES_FIELD);
		theTimeRangeChoice.setAsBoolean(true);

		// The fields for selecting a date range
		ComplexField theStartField = FormFactory.newDateField(START_DATE_FIELD, null, false);
		theStartField.setMandatory(false);
		theStartField.setDisabled(true);
		ComplexField theEndField = FormFactory.newDateField(END_DATE_FIELD, null, false);
		theEndField.setMandatory(false);
		theEndField.setDisabled(true);

		/* Init the calendar. */
		Calendar theCalendar = CalendarUtil.createCalendar();
		Date theToday = DateUtil.adjustToDayEnd(theCalendar);

		// End date must not lay in some unknown future
		ComplexField theTodayField = FormFactory.newDateField(START_DATE_FIELD, null, false);
		theTodayField.initializeField(theToday);

		ComparisonDependency.buildStartEndWithEqualEqualDependency(theStartField, theEndField);
		ComparisonDependency.buildStartEndWithEqualEqualDependency(theEndField, theTodayField);

		aContext.addMember(theTimeRange);
		aContext.addMember(theTimeRangeChoice);
		aContext.addMember(theStartField);
		aContext.addMember(theEndField);

		theStartField.initializeField(DateUtil.addDays(theToday, -7));
		theEndField.initializeField(theToday);
		theTimeRange.setAsSingleSelection(WEEKLY);

		theEndField.addValueListener(new FieldChangeListener(theStartField));
		theTimeRange.addValueListener(new TimeRangeListener(theStartField));
		theTimeRangeChoice.addValueListener(new BooleanFieldListener(theEndField));
	}

	
	public static class Update extends AbstractCommandHandler {
		
		/**
		 * Creates a {@link SessionSearchComponent.Update} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public Update(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent,
				Object model, Map<String, Object> someArguments) {
			
			((SessionSearchComponent) aComponent).update();
			return HandlerResult.DEFAULT_RESULT;
		}
	}
	
	public void update() {
	    FormContext theContext = getFormContext();
	    ComplexField theStart = (ComplexField)theContext.getMember(START_DATE_FIELD);
	    ComplexField theEnd = (ComplexField)theContext.getMember(END_DATE_FIELD);
	    SelectField theTimeRange = (SelectField) theContext.getMember(TIME_RANGE_FIELD);
	    BooleanField checkField = (BooleanField)theContext.getMember(RELATIVE_RANGES_FIELD);

	    Date start = (Date)theStart.getValue();
	    Date end = (Date)theEnd.getValue();
	    TimeRangeIterator theIter = TimeRangeIterator.createTimeRange("day", TLContext.getLocale(), start, end);
	    start = theIter.alignToStart(start);
	    end = theIter.alignToEnd(end);

	    Map theOptions = new HashMap(4);
	    theOptions.put(START_DATE_FIELD, start);
	    theOptions.put(END_DATE_FIELD, end);
	    theOptions.put(TIME_RANGE_FIELD, theTimeRange.getSingleSelection());
	    theOptions.put(RELATIVE_RANGES_FIELD, Boolean.valueOf(checkField.getAsBoolean()));

		// Make sure, something actually changes.
		theOptions.put("touch", new Object());

//	    DayInterval theInt = new DayInterval(start, end);

		setSelected(theOptions);
	}

	/**
	 * Relationship between startDate and endDate depending on the selected
	 * timerange.
	 */
	private class FieldChangeListener implements ValueListener {
		private FormField	referenceField;

		// Constructors
		public FieldChangeListener(FormField aField) {
			this.referenceField = aField;
		}

		/**
		 * @see com.top_logic.layout.form.ValueListener#valueChanged(com.top_logic.layout.form.FormField,
		 *      java.lang.Object, java.lang.Object)
		 */
		@Override
		public void valueChanged(FormField aField, Object aOldValue, Object aNewValue) {

			FormContext theFormContext = aField.getFormContext();
			Date theReferenceDate = (Date) aNewValue;
			String theTimeRange = (String) ((SelectField) theFormContext.getField(SessionSearchComponent.TIME_RANGE_FIELD))
			        .getSingleSelection();

			if (DAILY.equals(theTimeRange)) {
				this.referenceField.setValue(DateUtil.addDays(theReferenceDate, -1));
			}
			else if (WEEKLY.equals(theTimeRange)) {
				this.referenceField.setValue(DateUtil.addDays(theReferenceDate, -7));
			}
			else if (MONTHLY.equals(theTimeRange)) {
				this.referenceField.setValue(DateUtil.addMonths(theReferenceDate, -1));
			}
			else if (QUARTERLY.equals(theTimeRange)) {
				this.referenceField.setValue(DateUtil.addQuarters(theReferenceDate, -1));
			}
			else if (ANNUALLY.equals(theTimeRange)) {
				this.referenceField.setValue(DateUtil.addYears(theReferenceDate, -1));
			}
//			update();
		}
	}

	/**
	 * Absolute or relative dates.
	 */
	private class BooleanFieldListener implements ValueListener {
		private FormField referenceField;

		// Constructors
		/**
		 * This constructor creates a new
		 * FlexibleReportingComponent.BooleanFieldListener.
		 */
		public BooleanFieldListener(FormField aFormField) {
			this.referenceField = aFormField;
		}

		/**
		 * @see com.top_logic.layout.form.ValueListener#valueChanged(com.top_logic.layout.form.FormField,
		 *      java.lang.Object, java.lang.Object)
		 */
		@Override
		public void valueChanged(FormField aField, Object aOldValue, Object aNewValue) {
			boolean relative = ((Boolean) aNewValue).booleanValue();
			if (relative) {
				this.referenceField.setMandatory(false);
				this.referenceField.setDisabled(true);
				this.referenceField.setValue(new Date());
			}
			else {
				this.referenceField.setMandatory(true);
				this.referenceField.setDisabled(false);
			}
//			update();
		}
	}

	/**
	 * Enabling/disabling of startDate and endDate depending on the selected
	 * timeRange
	 */
	private class TimeRangeListener implements ValueListener {
		private FormField	referenceField;

		// Constructors
		/**
		 * This constructor creates a new
		 * FlexibleReportingComponent.DisableListener.
		 */
		public TimeRangeListener(FormField aField) {
			this.referenceField = aField;
		}

		/**
		 * @see com.top_logic.layout.form.ValueListener#valueChanged(com.top_logic.layout.form.FormField,
		 *      java.lang.Object, java.lang.Object)
		 */
		@Override
		public void valueChanged(FormField aField, Object aOldValue, Object aNewValue) {
			String theNewValue;
			if(aNewValue.equals(Collections.EMPTY_LIST)) {
				return;
			}
			else {
				theNewValue = (String) ((List) aNewValue).get(0);
			}
			FormField theEndField = this.referenceField.getFormContext().getField(SessionSearchComponent.END_DATE_FIELD);
			BooleanField theCheckField = (BooleanField) this.referenceField.getFormContext().getField(
					SessionSearchComponent.RELATIVE_RANGES_FIELD);
			if (SessionSearchComponent.MANUALLY.equals(theNewValue)) {
				this.referenceField.setMandatory(true);
				this.referenceField.setDisabled(false);
				theEndField.setMandatory(true);
				theEndField.setDisabled(false);
				theCheckField.setDisabled(true);
			}
			else {
				theCheckField.setDisabled(false);
				if (theCheckField.getAsBoolean()) {
					theEndField.setMandatory(false);
					theEndField.setDisabled(true);
				}
				else {
					theEndField.setMandatory(true);
					theEndField.setDisabled(false);
				}
				Date theReferenceDate = (Date) aField.getFormContext().getField(SessionSearchComponent.END_DATE_FIELD).getValue();
				this.referenceField.setMandatory(false);
				this.referenceField.setDisabled(true);
				if (SessionSearchComponent.DAILY.equals(theNewValue)) {
					this.referenceField.setValue(DateUtil.addDays(theReferenceDate, -1));
				}
				else if (SessionSearchComponent.WEEKLY.equals(theNewValue)) {
					this.referenceField.setValue(DateUtil.addDays(theReferenceDate, -7));
				}
				else if (SessionSearchComponent.MONTHLY.equals(theNewValue)) {
					this.referenceField.setValue(DateUtil.addMonths(theReferenceDate, -1));
				}
				else if (SessionSearchComponent.QUARTERLY.equals(theNewValue)) {
					this.referenceField.setValue(DateUtil.addQuarters(theReferenceDate, -1));
				}
				else {
					this.referenceField.setValue(DateUtil.addYears(theReferenceDate, -1));
				}
			}
//			update();
		}
	}

}
