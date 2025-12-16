/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task.schedule;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.Calendar;
import java.util.Date;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.model.form.ReactiveFormCSS;
import com.top_logic.model.form.definition.Columns;
import com.top_logic.util.Resources;
import com.top_logic.util.sched.task.Task;

/**
 * Base class for configurable {@link SchedulingAlgorithm}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class AbstractSchedulingAlgorithm<C extends PolymorphicConfiguration<? extends SchedulingAlgorithm>>
		extends AbstractConfiguredInstance<C> implements SchedulingAlgorithm {

	/** The {@link TypedConfiguration} interface for the {@link AbstractSchedulingAlgorithm}. */
	public interface Config<S extends SchedulingAlgorithm> extends PolymorphicConfiguration<S> {
		// Pure marker.
	}

	/**
	 * The name of the {@link FormField} presenting the {@link AbstractSchedulingAlgorithm#getClass()}.
	 */
	public static final String NAME_FIELD_CLASS = "class";

	/**
	 * The name of the {@link FormField} presenting the "strategy".
	 * <p>
	 * "Strategy" means: <code>i18n(schedulingAlgorithm.getClass())</code> (pseudo code)<br/>
	 * In comparison to the direct class name, it should be understandable to the average user.
	 * </p>
	 */
	public static final String NAME_FIELD_STRATEGY = "strategy";

	/**
	 * The CSS class on the outermost HTML tag written by the {@link #createTemplate()
	 * FormTemplate}.
	 */
	public static final String CSS_CLASS = "schedulingAlgorithm";

	/**
	 * Creates a {@link AbstractSchedulingAlgorithm} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AbstractSchedulingAlgorithm(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public long nextSchedule(long notBefore, long lastSchedule) {
		return Compatibility
			.unwrap(nextScheduleImpl(Compatibility.wrap(notBefore), Compatibility.wrapOptional(lastSchedule)));
	}

	/**
	 * Calculates the next schedule.
	 * <p>
	 * <em>Always use the given 'now' as the only time source!</em> Some callers will pass a time
	 * which is not the real "now" to simulate large time spans passing by. This is for example done
	 * by tests. Using another time source would break such callers.
	 * </p>
	 * 
	 * @param now
	 *        The current time, but at least <code>lastSchedule + 1ms</code>. Never
	 *        <code>null</code>. A copy of the passed {@link Calendar} object. Is allowed to be
	 *        changed and returned.
	 * @param lastSchedule
	 *        The last time the {@link Task} was run. Never <code>null</code>. Never contains
	 *        <code>null</code>. A copy of the passed {@link Calendar} object. Is allowed to be
	 *        changed and returned.
	 * @return <em>If the result is not {@link Maybe#none()}, it always has to be a new
	 *         {@link Calendar} object. It is not allowed to store that returned object anywhere.
	 *         </em><br/>
	 *         Is not allowed to be <code>null</code>. Is not allowed to contain <code>null</code>.
	 * 
	 * @deprecated Implement {@link #nextSchedule(long, long)} directly.
	 */
	@Deprecated
	protected Maybe<Calendar> nextScheduleImpl(Calendar now, Maybe<Calendar> lastSchedule) {
		return Maybe.none();
	}

	/**
	 * Util: Apply the time of day (hour and minute) from the {@link Date} to the {@link Calendar}.
	 */
	protected static void applyTimeOfDay(Date timeOfDay, Calendar calendar) {
		calendar.set(Calendar.HOUR_OF_DAY, timeOfDay.getHours());
		calendar.set(Calendar.MINUTE, timeOfDay.getMinutes());
		/* Clear these fields, as the time is expected to be exactly the given one. */
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
	}

	/**
	 * Creates the {@link HTMLTemplateFragment} to display the group filled in
	 * {@link #fillFormGroup(FormGroup)}.
	 */
	protected abstract HTMLTemplateFragment createTemplate();

	@Override
	public void fillFormGroup(FormGroup group) {
		group.addMember(FormFactory.newStringField(NAME_FIELD_CLASS, getClass().getName(), FormFactory.IMMUTABLE));
		String translatedName = Resources.getInstance().getString(ResKey.forClass(getClass()));
		group.addMember(FormFactory.newStringField(NAME_FIELD_STRATEGY, translatedName, FormFactory.IMMUTABLE));
		
		HTMLTemplateFragment template = createTemplate();
		template(group,
			contentBox(div(
				css(Columns.TWO.appendColsCSSto(ReactiveFormCSS.RF_COLUMNS_LAYOUT + " " + CSS_CLASS)),
				template)));
	}


	private static ResPrefix getI18nPrefix() {
		return ResPrefix.legacyClass(SchedulingAlgorithm.class);
	}

	/**
	 * Creates a UI for displaying the properties of the given {@link SchedulingAlgorithm}.
	 */
	public static FormGroup createUI(SchedulingAlgorithm schedulingAlgorithm, String groupName) {
		FormGroup group = new FormGroup(groupName, getI18nPrefix());
		schedulingAlgorithm.fillFormGroup(group);
		return group;
	}

}
