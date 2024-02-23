/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import static com.top_logic.layout.form.FormConstants.*;
import static com.top_logic.layout.form.template.model.Templates.*;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.top_logic.basic.DateUtil;
import com.top_logic.basic.col.InlineList;
import com.top_logic.basic.config.XmlDateTimeFormat;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.KeyEvent;
import com.top_logic.layout.KeyEventListener;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.form.BlockedStateChangedListener;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.DisabledPropertyListener;
import com.top_logic.layout.form.ErrorChangedListener;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.FormMemberVisitor;
import com.top_logic.layout.form.ImmutablePropertyListener;
import com.top_logic.layout.form.MandatoryChangedListener;
import com.top_logic.layout.form.SimpleProxyConstraint;
import com.top_logic.layout.form.UINonBlocking;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.WarningsChangedListener;
import com.top_logic.layout.form.constraints.GenericMandatoryConstraint;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;
import com.top_logic.util.ToBeValidated;

/**
 * {@link FormField} that holds the day and time aspect of a {@link Date} object.
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class DateTimeField extends CompositeField {

	private static final String DATE_TIME_CLASS = "cDatetime";

	private static final String TIME_CSS_CLASS = "timePart";

	static final String DAY_CLASS = "cDatetimeDay";

	static final String TIME_CLASS = "cDatetimeTime";

	private static final int BASE_YEAR = 1970;

	private static final int BASE_DAY_OF_YEAR = 1;

	private static final String DATE_FIELD = "date";

	private static final String DAY_FIELD = "day";

	private static final String TIME_FIELD = "time";

	/**
	 * Raw value of the {@link DateTimeFieldProxy}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	final class DateTime {

		/**
		 * Either the {@link Date} value or the {@link String} raw value if there is no value in the
		 * {@link DateTimeField#getDayField() day field}.
		 */
		private final Object _day;

		/**
		 * Either the {@link Date} value or the {@link String} raw value if there is no value in the
		 * {@link DateTimeField#getTimeField() time field}.
		 */
		private final Object _time;

		private String _dayError;

		private String _timeError;

		public DateTime(Object day, String dayError, Object time, String timeError) {
			_day = day;
			_time = time;
			_dayError = dayError;
			_timeError = timeError;
		}

		public Date toDate() throws CheckException {
			checkErrors();
			return mergeDate();
		}

		private Date mergeDate() {
			if (_day == null || _time == null) {
				return null;
			}
			_cal.setTime((Date) _day);
			Calendar timeCal = CalendarUtil.createCalendar((Date) _time, timezone());
			_cal.set(Calendar.MILLISECOND, timeCal.get(Calendar.MILLISECOND));
			_cal.set(Calendar.SECOND, timeCal.get(Calendar.SECOND));
			_cal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
			_cal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
			_cal.add(Calendar.DAY_OF_YEAR, timeCal.get(Calendar.DAY_OF_YEAR) - BASE_DAY_OF_YEAR);
			_cal.add(Calendar.YEAR, timeCal.get(Calendar.YEAR) - BASE_YEAR);
			Date date = _cal.getTime();
			return date;
		}

		private void checkErrors() throws CheckException {
			String dayError = dayError();
			String timeError = timeError();
			if (dayError != null) {
				if (timeError != null) {
					throw new CheckException(dayError + " " + timeError);
				} else {
					throw new CheckException(dayError);
				}
			} else if (timeError != null) {
				throw new CheckException(timeError);
			}
		}

		private String dayError() {
			String error;
			if (_dayError != null) {
				error = _dayError;
			} else if (_day instanceof String) {
				error =
					Resources.getInstance().getString(I18NConstants.DATE_TIME_FIELD_NO_DATE_VALUE__VALUE.fill(_day));
			} else {
				error = null;
			}
			return error;
		}

		private String timeError() {
			String error;
			if (_timeError != null) {
				error = _timeError;
			} else if (_time instanceof String) {
				error =
					Resources.getInstance().getString(I18NConstants.DATE_TIME_FIELD_NO_TIME_VALUE__VALUE.fill(_time));
			} else {
				error = null;
			}
			return error;
		}

		@Override
		public String toString() {
			Date mergeDate = mergeDate();
			if (mergeDate == null) {
				return "No date.";
			}
			return XmlDateTimeFormat.formatTimeStamp(mergeDate.getTime());
		}

	}

	/**
	 * {@link FormField} that holds the date including time aspect.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	final class DateTimeFieldProxy extends AbstractFormField {

		DateTimeFieldProxy(String name, boolean immutable) {
			super(name, !MANDATORY, immutable, NORMALIZE, NO_CONSTRAINT);
		}

		@Override
		protected FormField selfAsField() {
			return DateTimeField.this;
		}

		@Override
		public <R, A> R visit(FormMemberVisitor<R, A> v, A arg) {
			return v.visitFormField(this, arg);
		}

		@Override
		protected Object unparseValue(Object value) {
			if (value == null) {
				return null;
			}
			Date date = (Date) value;
			return new DateTime(toDay(date), null, toTime(date), null);
		}

		@Override
		protected Object parseRawValue(Object rawValue) throws CheckException {
			if (rawValue == null) {
				return null;
			}
			return ((DateTime) rawValue).toDate();
		}

		@Override
		protected Object narrowValue(Object value) throws IllegalArgumentException, ClassCastException {
			Date dateTime = (Date) value;
			return dateTime;
		}

		public void handleErrorChange(FormField dayField, FormField timeField) {
			Object day = valueOrRawValue(dayField);
			String dayError = errorOrNull(dayField);
			Object time = valueOrRawValue(timeField);
			String timeError = errorOrNull(timeField);
			DateTime newClientValue;
			if (day == null && time == null && dayError == null && timeError == null) {
				newClientValue = null;
			} else {
				newClientValue = new DateTime(day, dayError, time, timeError);
			}
			updateField(newClientValue);
		}

		void updateField(DateTime newClientValue) {
			try {
				FormFieldInternals.updateFieldNoClientUpdate(this, newClientValue);
			} catch (VetoException ex) {
				// ignore
			}
		}
	}

	/**
	 * Listener to transport {@link FormField#getError()} and {@link FormField#getWarnings()} from
	 * the day and time field to the proxy.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	class ErrorWarningsUpdater implements ErrorChangedListener, WarningsChangedListener {

		private final DateTimeFieldProxy _target;

		private final FormField _day;

		private final FormField _time;

		private ErrorWarningsUpdater(DateTimeFieldProxy target, FormField day, FormField time) {
			_target = target;
			_day = day;
			_time = time;
		}

		@Override
		public Bubble warningsChanged(FormField field, List<String> oldWarnings, List<String> newWarnings) {
			transportWarnings();
			return Bubble.CANCEL_BUBBLE;
		}

		@Override
		public Bubble hasWarningsChanged(FormField field, Boolean oldWarnings, Boolean newWarnings) {
			transportWarnings();
			return Bubble.CANCEL_BUBBLE;
		}

		private void transportWarnings() {
			if (_listenerDeactivated) {
				return;
			}
			Object warnings = InlineList.newInlineList();
			if (_day.hasWarnings()) {
				warnings = InlineList.addAll(String.class, warnings, _day.getWarnings());
			}
			if (_time.hasWarnings()) {
				warnings = InlineList.addAll(String.class, warnings, _time.getWarnings());
			}
			_target.setWarnings(InlineList.toList(String.class, warnings));
		}

		@Override
		public Bubble handleErrorChanged(FormField sender, String oldError, String newError) {
			if (!_listenerDeactivated) {
				_target.handleErrorChange(_day, _time);
			}
			return Bubble.CANCEL_BUBBLE;
		}

	}

	void addErrorWarningsUpdater(DateTimeFieldProxy target, FormField day, FormField time) {
		ErrorWarningsUpdater updater = new ErrorWarningsUpdater(target, day, time);
		day.addListener(FormField.ERROR_PROPERTY, updater);
		time.addListener(FormField.ERROR_PROPERTY, updater);
		day.addListener(FormField.WARNINGS_PROPERTY, updater);
		time.addListener(FormField.WARNINGS_PROPERTY, updater);
	}

	abstract class DeactivatingListener implements ValueListener {

		@Override
		public final void valueChanged(FormField field, Object oldValue, Object newValue) {
			if (_listenerDeactivated) {
				return;
			}
			_listenerDeactivated = true;
			try {
				internalValueChanged(field, oldValue, newValue);
			} finally {
				_listenerDeactivated = false;
			}

		}

		protected abstract void internalValueChanged(FormField field, Object oldValue, Object newValue);

	}

	final class DateChangedListener extends DeactivatingListener {

		@Override
		protected void internalValueChanged(FormField field, Object oldValue, Object newValue) {
			handleDateChanged((Date) newValue);
		}
	}

	@UINonBlocking
	final class DayChangedListener extends DeactivatingListener {

		@Override
		protected void internalValueChanged(FormField field, Object oldValue, Object newValue) {
			handleDayChanged(field, (Date) oldValue, (Date) newValue);
		}
	}

	@UINonBlocking
	final class TimeChangedListener extends DeactivatingListener {

		@Override
		protected void internalValueChanged(FormField field, Object oldValue, Object newValue) {
			handleTimeChanged(field, (Date) oldValue, (Date) newValue);
		}
	}

	private abstract static class ConstraintProxy extends SimpleProxyConstraint {

		protected final DateTimeField _dateTimeField;

		public ConstraintProxy(Constraint impl, DateTimeField dateTimeField) {
			super(impl);
			_dateTimeField = dateTimeField;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getImpl().hashCode();
			result = prime * result + _dateTimeField.hashCode();
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ConstraintProxy other = (ConstraintProxy) obj;
			if (!getImpl().equals(other.getImpl()))
				return false;
			if (!_dateTimeField.equals(other._dateTimeField))
				return false;
			return true;
		}

		@Override
		public boolean check(Object value) throws CheckException {
			try {
				boolean result = super.check(value);
				markFields(false);
				return result;
			} catch (CheckException ex) {
				markFields(true);
				throw ex;
			}
		}

		protected abstract void markFields(boolean mark);
	}

	private class ErrorConstraintProxy extends ConstraintProxy {

		public ErrorConstraintProxy(Constraint impl, DateTimeField dateTimeField) {
			super(impl, dateTimeField);
		}

		@Override
		protected void markFields(boolean mark) {
			if (mark) {
				_dateTimeField.addCssClass(ERROR_ANNOTATION_CSS_CLASS);
			} else {
				_dateTimeField.removeCssClass(ERROR_ANNOTATION_CSS_CLASS);
			}
		}

	}

	private class WarningConstraintProxy extends ConstraintProxy {

		public WarningConstraintProxy(Constraint impl, DateTimeField dateTimeField) {
			super(impl, dateTimeField);
		}

		@Override
		protected void markFields(boolean mark) {
			if (mark) {
				_dateTimeField.addCssClass(WARNING_ANNOTATION_CSS_CLASS);
			} else {
				_dateTimeField.removeCssClass(WARNING_ANNOTATION_CSS_CLASS);
			}
		}

	}

	Calendar _cal = CalendarUtil.createCalendar(timezone());

	private Map<KeyEventListener, KeyEventListener> _listenerProxies = new HashMap<>();

	boolean _listenerDeactivated = false;

	/**
	 * Creates a new {@link DateTimeField}.
	 * 
	 * @param name
	 *        The name of the field.
	 * @param date
	 *        The initial value of this field.
	 * @param immutable
	 *        Whether the field should be immutable.
	 */
	public DateTimeField(String name, Date date, boolean immutable) {
		super(name, I18NConstants.DATE_TIME_FIELD);
		DateTimeFieldProxy dateField = createDateField(date, immutable);
		FormField dayField = createDayField(date, immutable);
		ComplexField timeField = createTimeField(date, immutable);

		// Note: The day field must be added first to ensure that the focus() call is dispatched to
		// this member (the focus() call is dispatched to the first member of a composite field).
		addMember(dayField);

		addMember(timeField);
		addMember(dateField);
		
		template(this,
			span(css(DATE_TIME_CLASS),
				span(css(FLEXIBLE_CSS_CLASS), member(DAY_FIELD)),
				span(css(FIXED_RIGHT_CSS_CLASS + " " + TIME_CSS_CLASS), member(TIME_FIELD))));

		addErrorWarningsUpdater(dateField, dayField, timeField);

		addListeners(dayField, timeField);
	}

	private void addListeners(final FormField dayField, final FormField timeField) {
		addListener(DISABLED_PROPERTY, new DisabledPropertyListener() {

			@Override
			public Bubble handleDisabledChanged(FormMember sender, Boolean oldValue, Boolean newValue) {
				timeField.setDisabled(newValue);
				dayField.setDisabled(newValue);
				return Bubble.BUBBLE;
			}
		});
		addListener(IMMUTABLE_PROPERTY, new ImmutablePropertyListener() {

			@Override
			public Bubble handleImmutableChanged(FormMember sender, Boolean oldValue, Boolean newValue) {
				timeField.setImmutable(newValue);
				dayField.setImmutable(newValue);
				return Bubble.BUBBLE;
			}
		});
		addListener(MANDATORY_PROPERTY, new MandatoryChangedListener() {

			@Override
			public Bubble handleMandatoryChanged(FormField sender, Boolean oldValue, Boolean newValue) {
				if (newValue) {
					timeField.setMandatory(true);
					dayField.setMandatory(true);
				} else {
					if (!timeField.hasValue()) {
						timeField.setMandatory(false);
					} else {
						Object time = timeField.getValue();
						if (time != null && dayField.hasValue() && dayField.getValue() == null) {
							// dayField remains mandatory, because a time is set but no day.
						} else {
							dayField.setMandatory(false);
						}
					}
					if (!dayField.hasValue()) {
						dayField.setMandatory(false);
					} else {
						Object day = dayField.getValue();
						if (day != null && timeField.hasValue() && timeField.getValue() == null) {
							// timeField remains mandatory, because a day is set but no time.
						} else {
							timeField.setMandatory(false);
						}
					}
				}
				return Bubble.BUBBLE;
			}
		});
		addListener(BLOCKED_PROPERTY, new BlockedStateChangedListener() {

			@Override
			public Bubble handleIsBlockedChanged(FormField sender, Boolean oldValue, Boolean newValue) {
				timeField.setBlocked(newValue);
				dayField.setBlocked(newValue);
				return Bubble.BUBBLE;
			}
		});
	}

	private DateTimeFieldProxy createDateField(Date date, boolean immutable) {
		DateTimeFieldProxy dateField = new DateTimeFieldProxy(DATE_FIELD, immutable);
		dateField.initializeField(date);
		dateField.addValueListener(new DateChangedListener());
		return dateField;
	}

	private ComplexField createTimeField(Date date, boolean immutable) {
		DateFormat timeFormat = HTMLFormatter.getInstance().getShortTimeFormat();
		DateFormat clonedTimeFormat = (DateFormat) timeFormat.clone();
		clonedTimeFormat.setTimeZone(timezone());

		Date time = date == null ? null : toTime(date);
		ComplexField timeField = FormFactory.newTimeField(TIME_FIELD, time, immutable);
		timeField.setFormatAndParser(clonedTimeFormat);
		timeField.addCssClass(TIME_CLASS);
		timeField.addValueListener(new TimeChangedListener());

		return timeField;
	}

	private ComplexField createDayField(Date date, boolean immutable) {
		Date day = date == null ? null : toDay(date);
		ComplexField dayField = FormFactory.newDateField(DAY_FIELD, day, immutable);
		DateFormat clonedDateFormat = (DateFormat) dayField.getFormat().clone();
		clonedDateFormat.setTimeZone(timezone());
		dayField.setFormatAndParser(clonedDateFormat);

		// NOTE: When setting the parser, the application value changes to keep the current user
		// display.
		dayField.initializeField(day);

		dayField.addCssClass(DAY_CLASS);
		dayField.addValueListener(new DayChangedListener());
		return dayField;
	}

	Date toTime(Date date) {
		_cal.setTime(date);
		_cal.set(BASE_YEAR, Calendar.JANUARY, BASE_DAY_OF_YEAR);
		Date time = _cal.getTime();
		return time;
	}

	Date toDay(Date date) {
		_cal.setTime(date);
		DateUtil.adjustToDayBegin(_cal);
		Date day = _cal.getTime();
		return day;
	}

	void handleDayChanged(FormField dayField, Date oldDay, Date day) {
		boolean dateMandatory = isMandatory();
		FormField timeField = getTimeField();
		
		Object timeVal = valueOrRawValue(timeField);
		if (timeVal instanceof String) {
			String timeError = errorOrNull(timeField);
			String dayError = errorOrNull(dayField);
			getProxy().updateField(new DateTime(day, dayError, timeVal, timeError));
			return;
		}
		if (day == null) {
			if (timeVal != null) {
				dayField.setMandatory(true);
			} else if (!dateMandatory) {
				timeField.setMandatory(false);
			}
		} else {
			if (!dateMandatory) {
				dayField.setMandatory(false);
			}
			if (timeVal != null) {
				if (!dateMandatory) {
					timeField.setMandatory(false);
				}
			} else {
				timeField.setMandatory(true);
			}
		}
		/* Changing mandatory may change error state, so errors must be fetched after changing
		 * mandatory. */
		String timeError = errorOrNull(timeField);
		String dayError = errorOrNull(dayField);
		deliverDate(new DateTime(day, dayError, timeVal, timeError), dayField, oldDay);
	}

	void handleTimeChanged(FormField timeField, Date oldTime, Date time) {
		boolean dateMandatory = isMandatory();
		FormField dayField = getDayField();
		
		Object currentDay = valueOrRawValue(dayField);
		if (currentDay instanceof String) {
			String dayError = errorOrNull(dayField);
			String timeError = errorOrNull(timeField);
			getProxy().updateField(new DateTime(currentDay, dayError, time, timeError));
			return;
		}
		if (time == null) {
			if (currentDay != null) {
				timeField.setMandatory(true);
			} else if (!dateMandatory) {
				dayField.setMandatory(false);
			}
		} else {
			if (!dateMandatory) {
				timeField.setMandatory(false);
			}
			if (currentDay != null) {
				if (!dateMandatory) {
					dayField.setMandatory(false);
				}
			} else {
				dayField.setMandatory(true);
			}
		}
		/* Changing mandatory may change error state, so errors must be fetched after changing
		 * mandatory. */
		String dayError = errorOrNull(dayField);
		String timeError = errorOrNull(timeField);
		deliverDate(new DateTime(currentDay, dayError, time, timeError), timeField, oldTime);
	}

	private void deliverDate(final DateTime dateTime, final FormField changedField, final Date formerValue) {
		try {
			FormFieldInternals.updateFieldNoClientUpdate(getProxy(), dateTime);
		} catch (final VetoException ex) {
			/* TODO Ticket #15445: Value can not be reset by the ValueListener that triggers the
			 * change. Therefore the whole exception handling is done in a validation step. */
			DefaultDisplayContext.getDisplayContext().getLayoutContext().notifyInvalid(new ToBeValidated() {

				@Override
				public void validate(DisplayContext validationContext) {
					handleVetoInDateTime(dateTime, changedField, formerValue, ex);
				}
			});
		}
	}

	/**
	 * Handles a {@link VetoException} when setting {@link Date} to {@link DateTimeField} does not
	 * work.
	 * 
	 * <p>
	 * Reverts the value in the changed field and sets a continuation to the {@link Exception} that
	 * sets the {@link DateTime}.
	 * </p>
	 */
	void handleVetoInDateTime(final DateTime dateTime, FormField changedField, Date formerValue, VetoException ex) {
		changedField.setValue(formerValue);
		ex.setContinuationCommand(new Command() {

			@Override
			public HandlerResult executeCommand(DisplayContext continuationContext) {
				DateTimeField.this.getProxy().updateField(dateTime);
				return HandlerResult.DEFAULT_RESULT;
			}
		});
		ex.process(DefaultDisplayContext.getDisplayContext().getWindowScope());
	}

	void handleDateChanged(Date date) {
		if (date == null) {
			getDayField().setValue(null);
			getTimeField().setValue(null);
		} else {
			getDayField().setValue(toDay(date));
			getTimeField().setValue(toTime(date));

		}
	}

	/**
	 * Returns the field holding the day.
	 */
	public ComplexField getDayField() {
		return (ComplexField) getField(DAY_FIELD);
	}

	/**
	 * Returns the field holding the time.
	 */
	public ComplexField getTimeField() {
		return (ComplexField) getField(TIME_FIELD);
	}

	@Override
	public void setCssClasses(String newCssClasses) {
		super.setCssClasses(newCssClasses);
		getDayField().setCssClasses(newCssClasses);
		getTimeField().setCssClasses(newCssClasses);
	}

	@Override
	public void checkDependency() {
		getDayField().checkDependency();
		getTimeField().checkDependency();
	}

	@Override
	public boolean addDependant(FormField dependant) {
		return getDayField().addDependant(dependant) | getTimeField().addDependant(dependant);
	}

	@Override
	public boolean removeDependant(FormField dependant) {
		return getDayField().removeDependant(dependant) | getTimeField().removeDependant(dependant);
	}

	@Override
	public DateTimeFieldProxy getProxy() {
		return (DateTimeFieldProxy) getField(DATE_FIELD);
	}

	@Override
	public void check() {
		getDayField().check();
		getTimeField().check();
		super.check();
	}

	@Override
	public boolean checkConstraints() {
		if (!getDayField().checkConstraints()) {
			return false;
		}
		if (!getTimeField().checkConstraints()) {
			return false;
		}
		return super.checkConstraints();
	}

	@Override
	public boolean checkConstraints(Object value) {
		if (!getDayField().checkConstraints(value)) {
			return false;
		}
		if (!getTimeField().checkConstraints(value)) {
			return false;
		}
		return super.checkConstraints(value);
	}

	@Override
	public void initializeField(Object defaultValue) {
		// Deactivate listener, because initializing value also calls setValue()
		_listenerDeactivated = true;
		super.initializeField(defaultValue);
		if (defaultValue == null) {
			getDayField().initializeField(null);
			getTimeField().initializeField(null);
		} else {
			Date defaultDate = (Date) defaultValue;
			getDayField().initializeField(toDay(defaultDate));
			getTimeField().initializeField(toTime(defaultDate));
		}
		_listenerDeactivated = false;
	}

	@Override
	public FormField addWarningConstraint(Constraint constraint) {
		return super.addWarningConstraint(wrapWarningConstraint(constraint));
	}

	@Override
	public boolean removeWarningConstraint(Constraint constraint) {
		return super.removeWarningConstraint(wrapWarningConstraint(constraint));
	}

	private Constraint wrapWarningConstraint(Constraint constraint) {
		return new WarningConstraintProxy(constraint, this);
	}

	@Override
	public FormField addConstraint(Constraint constraint) {
		return super.addConstraint(wrapErrorConstraint(constraint));
	}

	@Override
	public boolean removeConstraint(Constraint constraint) {
		return super.removeConstraint(wrapErrorConstraint(constraint));
	}

	@Override
	public boolean addKeyListener(KeyEventListener listener) {
		if (_listenerProxies.containsKey(listener)) {
			return false;
		}
		KeyEventListener proxyListener = new KeyEventListener(listener.subscribedKeys()) {
			@Override
			public HandlerResult handleKeyEvent(DisplayContext commandContext, KeyEvent event) {
				return listener.handleKeyEvent(commandContext,
					new KeyEvent(self(), event.getKeyCode(), event.getModifiers()));
			}
		};
		_listenerProxies.put(listener, proxyListener);

		getDayField().addKeyListener(proxyListener);
		getTimeField().addKeyListener(proxyListener);
		return true;
	}

	@Override
	public boolean removeKeyListener(KeyEventListener listener) {
		KeyEventListener proxyListener = _listenerProxies.remove(listener);
		if (proxyListener == null) {
			return false;
		}

		getDayField().removeKeyListener(proxyListener);
		getTimeField().removeKeyListener(proxyListener);
		return true;
	}

	private Constraint wrapErrorConstraint(Constraint constraint) {
		Constraint wrappedConstraint;
		if (constraint == GenericMandatoryConstraint.SINGLETON) {
			wrappedConstraint = constraint;
		} else {
			wrappedConstraint = new ErrorConstraintProxy(constraint, this);
		}
		return wrappedConstraint;
	}

	TimeZone timezone() {
		return ThreadContext.getTimeZone();
	}

	Object valueOrRawValue(FormField dayOrTimeField) {
		Object day;
		if (dayOrTimeField.hasValue()) {
			day = dayOrTimeField.getValue();
		} else {
			day = dayOrTimeField.getRawValue();
		}
		return day;
	}

	String errorOrNull(FormField dayOrTimeField) {
		String timeError;
		if (dayOrTimeField.hasError()) {
			timeError = dayOrTimeField.getError();
		} else {
			timeError = null;
		}
		return timeError;
	}

}