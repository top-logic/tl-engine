/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.gantt.component;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.DateUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.constraints.StartEndDateOrderConstraint;
import com.top_logic.layout.form.listener.AbstractValueListenerDependency;
import com.top_logic.layout.form.listener.RadioButtonValueListenerDependency;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.structure.LayoutControlProvider;
import com.top_logic.reporting.chart.gantt.component.form.AbstractContextDatesDependency;
import com.top_logic.reporting.chart.gantt.component.form.ColumnSelectorProperty;
import com.top_logic.reporting.chart.gantt.component.form.GranularitySelectorProperty;
import com.top_logic.reporting.chart.gantt.component.form.StringListSelectorProperty;
import com.top_logic.reporting.chart.gantt.model.GanttChartConstants;
import com.top_logic.reporting.chart.gantt.model.TimeGranularity;
import com.top_logic.reporting.view.component.property.BooleanProperty;
import com.top_logic.reporting.view.component.property.DateProperty;
import com.top_logic.reporting.view.component.property.FilterProperty;
import com.top_logic.reporting.view.component.property.IntRangeProperty;
import com.top_logic.reporting.view.component.property.WrapperListProperty;
import com.top_logic.util.Utils;

/**
 * {@link AbstractGanttFilterComponent} that handles the filter input for the Gantt chart.
 * 
 * @author <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public abstract class GanttChartFilterComponent extends AbstractGanttFilterComponent implements
		GanttChartConstants {

	public interface Config extends AbstractGanttFilterComponent.Config {

		@Name(XML_PARAM_MONTH_DIFF_START)
		@IntDefault(1)
		int getMonthsBack();

		@Name(XML_PARAM_MONTH_DIFF_END)
		@IntDefault(60)
		int getMonthsForward();

		@Name(XML_PARAM_DEPTH)
		@IntDefault(3)
		int getDepth();

		@Name(XML_PARAM_MAX_DEPTH)
		@IntDefault(10)
		int getMaxDepth();

		@Name(XML_PARAM_MIN_DEPTH)
		@IntDefault(0)
		int getMinDepth();

		@Name(XML_PARAM_INITIAL_COLUMNS)
		@StringDefault(COLUMN_START_DATE + ',' + COLUMN_END_DATE)
		String getColumns();

		@Override
		@ItemDefault(com.top_logic.reporting.chart.gantt.component.GanttChartFilterComponentControlProvider.class)
		PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();
	}

	/** Configuration parameter for months to go back from now to set default filter start date */
    public static final String XML_PARAM_MONTH_DIFF_START = "monthsBack";

    /** Configuration parameter for months to go forward from now to set default filter end date */
    public static final String XML_PARAM_MONTH_DIFF_END	  = "monthsForward";

    /** Configuration parameter for default filter depth */
    public static final String XML_PARAM_DEPTH			  = "depth";

	/** Configuration parameter for max filter depth */
	public static final String XML_PARAM_MAX_DEPTH = "maxDepth";

	/** Configuration parameter for min filter depth */
	public static final String XML_PARAM_MIN_DEPTH = "minDepth";

    /** Configuration parameter for default filter depth */
    public static final String XML_PARAM_INITIAL_COLUMNS = "initialColumns";

	/** Suffix for personal configuration key. */
	public static final String PERS_CONF_PROPERTY_SETTINGS_SUFFIX = "_filterSettings";


    private Date _initStartDate;
    private Date _initEndDate;
    private int _defaultDepth;

	/** @see Config#getMaxDepth() */
	private int _maxDepth;

	/** @see Config#getMinDepth() */
	private int _minDepth;

    private List<String> _initialColumns;

	/**
	 * Creates a {@link GanttChartFilterComponent} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public GanttChartFilterComponent(InstantiationContext context, Config config)
			throws ConfigurationException {
		super(context, config);
		int monthsBack = config.getMonthsBack();
		int monthsFwd = config.getMonthsForward();
		initStartEndDate(monthsBack, monthsFwd);
		_defaultDepth = config.getDepth();
		_maxDepth = config.getMaxDepth();
		_minDepth = config.getMinDepth();
		String columnsString = config.getColumns();
		_initialColumns = StringServices.toList(columnsString, ',');
	}

	/**
	 * Initialize the default start/end date used for the filter
	 * 
	 * @param monthsBack
	 *        Number of months to go back back from now to set default filter start date
	 * @param monthsFwd
	 *        Number of months to go back forward from now to set default filter end date
	 */
	protected void initStartEndDate(int monthsBack, int monthsFwd) {
		/* Init the calendar and set the end date. */
		Calendar calendar = CalendarUtil.createCalendar();
		calendar.add(Calendar.MONTH, monthsFwd);
		_initEndDate = DateUtil.adjustToDayEnd(calendar.getTime());

		/* Init the calendar and set the start date. */
		calendar.setTime(new Date());
		calendar.add(Calendar.MONTH, -1 * monthsBack);
		_initStartDate = DateUtil.adjustToDayBegin(calendar.getTime());
	}

	@Override
	public FormContext createFormContext() {
		FormContext context = super.createFormContext();
		addStartEndDateConstraint(context);
		addScalingSelectorListener(context);
		addCommitteeSelectorListener(context);
		addAdditionalColumnsListener(context);
		addTargetMilestoneListener(context);
		addFinishedElementsDependency(context);
		addUseContextElementDatesListener(context);
		return context;
	}

	private void addUseContextElementDatesListener(FormContext context) {
		AbstractContextDatesDependency dependency = createContextDatesDependency(context);
		if (dependency != null) {
			dependency.attach();
			dependency.touchAll(); // initially fire dependency (e.g. to update dates when model changes)
		}
	}

	/**
	 * Create an {@link AbstractContextDatesDependency} to listen to date range changes, e.g. if
	 * PROPERTY_USE_CONTEXT_DATES is set.
	 */
	protected abstract AbstractContextDatesDependency createContextDatesDependency(FormContext context);

	private void addStartEndDateConstraint(FormContext context) {
		ComplexField startDateField = (ComplexField) context.getMember(PROPERTY_START_DATE);
		ComplexField endDateField = (ComplexField) context.getMember(PROPERTY_END_DATE);
		if (startDateField != null && endDateField != null) {
			Constraint startConstraint = createStartFieldConstraint(endDateField);
			if (startConstraint != null) {
				startDateField.addConstraint(startConstraint);
			}
			Constraint endConstraint = createEndFieldConstraint(startDateField);
			if (endConstraint != null) {
				endDateField.addConstraint(endConstraint);
			}
		}
	}

	/**
	 * Creates the {@link Constraint} for the range end field.
	 * 
	 * @see StartEndDateOrderConstraint
	 */
	protected Constraint createEndFieldConstraint(ComplexField startDateField) {
		return new StartEndDateOrderConstraint(startDateField, false);
	}

	/**
	 * Creates the {@link Constraint} for the range start field.
	 * 
	 * @see StartEndDateOrderConstraint
	 */
	protected Constraint createStartFieldConstraint(ComplexField endDateField) {
		return new StartEndDateOrderConstraint(endDateField, true);
	}

	private void addScalingSelectorListener(FormContext context) {
		SelectField chooseScalingField = (SelectField) context.getMember(PROPERTY_SCALING_OPTION);
		SelectField granularityOptionsField = (SelectField) context.getMember(PROPERTY_SCALING_GRANULARITY);
		ValueListener scalingListener = createScaingValueListener(granularityOptionsField);
		if (scalingListener != null) {
			chooseScalingField.addValueListener(scalingListener);
			Object value = chooseScalingField.getValue();
			scalingListener.valueChanged(chooseScalingField, value, value);
		}
	}

	/**
	 * Creates the value listener for the scaling field.
	 * 
	 * @see ScalingSelectorListener
	 */
	protected ValueListener createScaingValueListener(SelectField granularityOptionsField) {
		return new ScalingSelectorListener(granularityOptionsField);
	}
	
	private void addCommitteeSelectorListener(FormContext context) {
		if (context.hasMember(PROPERTY_SHOW_MEETINGS) && context.hasMember(PROPERTY_COMMITTEES)) {
			BooleanField showMeetingsField = (BooleanField) context.getMember(PROPERTY_SHOW_MEETINGS);
			SelectField committeeSelectorField = (SelectField) context.getMember(PROPERTY_COMMITTEES);
			ValueListener meetingListener = createShowMeetingListener(committeeSelectorField);
			if (meetingListener != null) {
				showMeetingsField.addValueListener(meetingListener);
				Object value = showMeetingsField.getValue();
				meetingListener.valueChanged(showMeetingsField, value, value);
			}
		}
	}

	/**
	 * Creates the value listener for the "show meetings" field.
	 * 
	 * @see ShowHideFieldListener
	 */
	protected ValueListener createShowMeetingListener(SelectField committeeSelectorField) {
		return new ShowHideFieldListener(committeeSelectorField, false);
	}

	private void addAdditionalColumnsListener(FormContext context) {
		BooleanField showAdditionsColumnsField = (BooleanField) context.getMember(PROPERTY_SHOW_ADDITIONAL_COLUMNS);
		SelectField columnsField = (SelectField) context.getMember(PROPERTY_ADDITIONAL_COLUMNS);
		ValueListener listener = createAdditionalColumnsListener(columnsField);
		if (listener != null) {
			showAdditionsColumnsField.addValueListener(listener);
			Object value = showAdditionsColumnsField.getValue();
			listener.valueChanged(showAdditionsColumnsField, value, value);
		}
	}

	/**
	 * Creates the value listener for the "show additional columns" field.
	 * 
	 * @see ShowHideFieldListener
	 */
	protected ValueListener createAdditionalColumnsListener(SelectField columnsField) {
		return new ShowHideFieldListener(columnsField, true);
	}

	private void addTargetMilestoneListener(FormContext context) {
		if (context.hasMember(PROPERTY_SHOW_DURATION_TO_MS) && context.hasMember(PROPERTY_TARGET_MILESTONE)) {
			BooleanField showDurationToMSField = (BooleanField) context.getMember(PROPERTY_SHOW_DURATION_TO_MS);
			SelectField targetMSField = (SelectField) context.getMember(PROPERTY_TARGET_MILESTONE);
			ValueListener listener = createShowDurationListener(targetMSField);
			if (listener != null) {
				showDurationToMSField.addValueListener(listener);
				Object value = showDurationToMSField.getValue();
				listener.valueChanged(showDurationToMSField, value, value);
			}
		}
	}

	/**
	 * Creates the value listener for the "show duration" field.
	 * 
	 * @see ShowHideFieldListener
	 */
	protected ValueListener createShowDurationListener(SelectField targetMSField) {
		return new ShowHideFieldListener(targetMSField, true);
	}

	/**
	 * Adds a radio {@link RadioButtonValueListenerDependency} between disable and hide finished
	 * elements option.
	 */
	private void addFinishedElementsDependency(FormContext context) {
		BooleanField disableFinishedField = (BooleanField) context.getMember(PROPERTY_DISABLE_FINISHED_ELEMENTS);
		BooleanField hideFinishedField = (BooleanField) context.getMember(PROPERTY_HIDE_FINISHED_ELEMENTS);
		AbstractValueListenerDependency dependency = createFinishedDependency(disableFinishedField, hideFinishedField);
		if (dependency != null) {
			dependency.attach();
		}
	}

	/**
	 * Creates the dependency for the show/hide finished fields.
	 */
	protected AbstractValueListenerDependency createFinishedDependency(BooleanField disableFinishedField,
			BooleanField hideFinishedField) {
		return new RadioButtonValueListenerDependency(new BooleanField[] {disableFinishedField, hideFinishedField}, true);
	}


	/**
	 * a new collection with the new FilterProperties used by this filter
	 */
	@Override
	protected List<FilterProperty> createNewProperties() {
		List<FilterProperty> properties = new ArrayList<>();
		properties.add(new DateProperty(PROPERTY_START_DATE, _initStartDate, this));
		properties.add(new DateProperty(PROPERTY_END_DATE, _initEndDate, this));
		properties.add(new BooleanProperty(PROPERTY_USE_CONTEXT_DATES, Boolean.FALSE, this));
		properties.add(new IntRangeProperty(PROPERTY_DEPTH, _defaultDepth, _minDepth, _maxDepth, this));
		properties.add(new BooleanProperty(PROPERTY_SHOW_PARENT_ELEMENTS, Boolean.FALSE, this));
		properties.add(new BooleanProperty(PROPERTY_SHOW_FORECAST, Boolean.FALSE, this));
		properties.add(new BooleanProperty(PROPERTY_SHOW_REPORT_LINES, Boolean.FALSE, this));
		properties.add(new BooleanProperty(PROPERTY_SHOW_DEPENDENCIES, Boolean.TRUE, this));
		properties.add(new BooleanProperty(PROPERTY_HIDE_NODE_DATE_RANGES, Boolean.FALSE, this));
		properties.add(new BooleanProperty(PROPERTY_HIDE_START_END_LABEL, Boolean.TRUE, this));
		properties.add(new BooleanProperty(PROPERTY_HIDE_FINISHED_ELEMENTS, Boolean.FALSE, this));
		properties.add(new BooleanProperty(PROPERTY_DISABLE_FINISHED_ELEMENTS, Boolean.FALSE, this));
		properties.add(new BooleanProperty(PROPERTY_ADD_COLLISION_AVOIDING_ROWS, Boolean.FALSE, this));
		properties.add(new BooleanProperty(PROPERTY_SHOW_ROOT, Boolean.TRUE, this));
		properties.add(new BooleanProperty(PROPERTY_SHOW_ADDITIONAL_COLUMNS, Boolean.FALSE, this));
		properties.add(new ColumnSelectorProperty(PROPERTY_ADDITIONAL_COLUMNS, getAdditionalColumns(), getInitialColumns(), this));
		properties.add(new BooleanProperty(PROPERTY_SHOW_DURATION_TO_MS, Boolean.FALSE, this));
		properties.add(new BooleanProperty(PROPERTY_SHOW_MEETINGS, Boolean.TRUE, this));
		properties.add(new WrapperListProperty(PROPERTY_COMMITTEES, getInitialCommittees(), true, this) {
			@Override
			protected List<? extends Wrapper> getAllElements() {
				return getCommitteeList();
			}
		});
		properties.add(new StringListSelectorProperty(PROPERTY_SCALING_OPTION, getInitialScalingOption(),
			getScalingOptions(), this));
		properties.add(new GranularitySelectorProperty(PROPERTY_SCALING_GRANULARITY, getInitialTimeInterval(),
			getTimeIntervals(), this));
		return properties;
	}

	/**
	 * Returns the initial selected committees.
	 */
	protected List<? extends Wrapper> getInitialCommittees() {
		return Collections.emptyList();
	}

	/**
	 * Returns the list of committees.
	 */
	protected abstract List<? extends Wrapper> getCommitteeList();
	
	/**
	 * Gets the names of the available additional columns.
	 */
	protected List<String>getAdditionalColumns() {
		return CollectionUtil.toList(COLUMN_MAP.keySet());
	}

	/**
	 * Gets the names of the additional columns which shall be initially selected.
	 */
	protected List<String>getInitialColumns() {
		return _initialColumns;
	}

	/**
	 * Gets the available TimeInterval options.
	 */
	protected List<TimeGranularity> getTimeIntervals() {
		TimeGranularity[] timeIntervals = TimeGranularity.values();
		List<TimeGranularity> relevantTimeIntervals = CollectionUtil.toList(timeIntervals);
		relevantTimeIntervals.remove(TimeGranularity.YEAR);
		return relevantTimeIntervals;
	}

	/**
	 * Gets the initial selected TimeInterval.
	 */
	protected TimeGranularity getInitialTimeInterval() {
		return TimeGranularity.QUARTER;
	}

	/**
	 * Gets the available scaling options.
	 */
	protected List<String> getScalingOptions() {
		return CollectionUtil.createList(/* SCALING_OPTION_WINDOW, */SCALING_OPTION_AUTO, SCALING_OPTION_MANUAL);
	}

	/**
	 * Gets the initial selected scaling option.
	 */
	protected String getInitialScalingOption() {
		return SCALING_OPTION_AUTO;
	}

	@Override
	protected
	abstract boolean supportsInternalModel(Object object);

	/**
	 * Shows or hides the granularity option, depending on the selected scaling option.
	 */
	public static class ScalingSelectorListener implements ValueListener {
		
		private final SelectField _granularityField;

		/**
		 * Creates a new {@link ScalingSelectorListener}.
		 */
		public ScalingSelectorListener(SelectField field) {
			_granularityField = field;
		}

		@Override
		public void valueChanged(FormField field, Object oldValue, Object newValue) {
			if (newValue instanceof Collection) {
				String first = (String) CollectionUtil.getFirst(newValue);
				if (SCALING_OPTION_MANUAL.equals(first)) {
					_granularityField.setVisible(true);
					_granularityField.setMandatory(true);
				}
				else {
					_granularityField.setVisible(false);
					_granularityField.setMandatory(false);
				}
			}
		}
	}


	/**
	 * Shows or hides the given target field, depending on the selected option of this listeners field.
	 */
	public static class ShowHideFieldListener implements ValueListener {

		private final FormField _targetField;

		private final boolean _mandatory, _reset;

		/**
		 * The value to be used as the target fields default value while invisible. It has been done this
		 * way, because a simple call to the fields reset() method will not always be enough. When the field
		 * gets loaded from personal configuration it will have its internal default to whatever it is set
		 * to. Thus calling reset() on the field when it becomes invisible will not change this value and it
		 * remains set even if invisble. Hence we provide this listener with an explicit default value which
		 * is to be applied to the target field once it becomes invisible.
		 */
		private final Object _invisibleDefaultValue;

		/**
		 * Creates a new {@link ShowHideFieldListener}.
		 */
		public ShowHideFieldListener(FormField targetField, boolean mandatory) {
			this(targetField, mandatory, false, null);
		}

		/**
		 * Creates a new {@link ShowHideFieldListener}. This listener is meant to be bound on a boolean
		 * field. Dependend to this boolean fields value, this listener will toggle the visibility of the
		 * given targetField, so it becomes visible when the boolean field is set to true and gets hidden,
		 * if the boolean field is set to false
		 * 
		 * @param targetField
		 *        the target field to set the visibility for
		 * @param mandatory
		 *        if true, the target field gets set to mandatory when visible and reset back to optional
		 *        when invisible
		 * @param reset
		 *        if true, the target field gets reset back to the given default value when becoming
		 *        invisible
		 * @param invisibleDefaultValue
		 *        this value is set as value of the target field when reset is true and the field is
		 *        becoming invisible. May be null.
		 */
		public ShowHideFieldListener(FormField targetField, boolean mandatory, boolean reset,
				Object invisibleDefaultValue) {
			_targetField = targetField;
			_mandatory = mandatory;
			_reset = reset;
			_invisibleDefaultValue = invisibleDefaultValue;

		}

		@Override
		public void valueChanged(FormField field, Object oldValue, Object newValue) {
			if (Utils.getbooleanValue(newValue)) {
				_targetField.setVisible(true);
				if (_mandatory) _targetField.setMandatory(true);
			}
			else {
				_targetField.setVisible(false);
				if (_mandatory) _targetField.setMandatory(false);
				if (_reset)	_targetField.setValue(_invisibleDefaultValue);
			}
		}
	}
	
}
