/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task.schedule;

import java.util.Calendar;
import java.util.Date;

import org.w3c.dom.Document;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.template.FormPatternConstants;
import com.top_logic.layout.form.template.FormTemplate;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.layout.form.template.FormTemplateControlProvider;
import com.top_logic.mig.html.HTMLConstants;
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
	 * The CSS class on the outermost HTML tag written by the {@link #getFormTemplateDocument()
	 * FormTemplate}.
	 */
	public static final String CSS_CLASS = "schedulingAlgorithm";

	private static final Document TEMPLATE = DOMUtil.parseThreadSafe(""
		+ "	<table " + templateRootAttributes() + " >"
		+ templateStandardFields()
		+ "	</table>"
		);

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
	 * The {@link FormTemplate} for the GUI representation of this {@link SchedulingAlgorithm}.
	 */
	protected Document getFormTemplateDocument() {
		return TEMPLATE;
	}

	@Override
	public void fillFormGroup(FormGroup group) {
		group.addMember(FormFactory.newStringField(NAME_FIELD_CLASS, getClass().getName(), FormFactory.IMMUTABLE));
		String translatedName = Resources.getInstance().getString(ResKey.forClass(getClass()));
		group.addMember(FormFactory.newStringField(NAME_FIELD_STRATEGY, translatedName, FormFactory.IMMUTABLE));
		group.setControlProvider(createControlProvider(getFormTemplateDocument()));
	}

	/**
	 * Is called in {@link #fillFormGroup(FormGroup)} and set as the {@link ControlProvider} of its
	 * {@link FormGroup}.
	 */
	public static ControlProvider createControlProvider(Document template) {
		return new FormTemplateControlProvider(
			new FormTemplate(getI18nPrefix(), DefaultFormFieldControlProvider.INSTANCE, false,
				template));
	}

	private static ResPrefix getI18nPrefix() {
		return ResPrefix.legacyClass(SchedulingAlgorithm.class);
	}

	// Convenience shortcuts for writing the FormTemplate String

	/**
	 * Writes the {@link FormTemplate} XML snippet for the {@link FormField}s every
	 * {@link SchedulingAlgorithm} has: Class, strategy (i18n of class) and scheduling window
	 * length.
	 */
	public static String templateStandardFields() {
		return ""
			+ "<tr>"
			+ templateSmallField(NAME_FIELD_STRATEGY)
			+ "</tr>"
			+ "<tr>"
			+ templateLargeField(NAME_FIELD_CLASS)
			+ "</tr>";
	}

	/** Writes all the attributes needed on the {@link FormTemplate} XML root tag. */
	public static String templateRootAttributes() {
		return ""
			+ " class='" + CSS_CLASS + "'"
			+ " xmlns='" + HTMLConstants.XHTML_NS + "'"
			+ " xmlns:t='" + FormTemplateConstants.TEMPLATE_NS + "'"
			+ " xmlns:p='" + FormPatternConstants.PATTERN_NS + "'";
	}

	/**
	 * Writes a {@link FormTemplate} XML snippet for a {@link FormField} that uses only half of its
	 * row.
	 * <p>
	 * Extracted for subclasses to prevent mistakes when writing the template: Missing class on
	 * 'td', missing colon after label, ...
	 * </p>
	 */
	protected static String templateSmallField(String name) {
		return templateField(name, false);
	}

	/**
	 * Writes a {@link FormTemplate} XML snippet for a {@link FormField} that uses all of its row.
	 * <p>
	 * Extracted for subclasses to prevent mistakes when writing the template: Missing class on
	 * 'td', missing colon after label, ...
	 * </p>
	 */
	protected static String templateLargeField(String name) {
		return templateField(name, true);
	}

	/**
	 * Extracted for subclasses to prevent mistakes when writing the template: Missing class on
	 * 'td', missing colon after label, ...
	 */
	private static String templateField(String name, boolean large) {
		return ""
			+ "<td class='label'>"
			+ "<p:field name='" + name + "' style='label' />:"
			+ "</td>"
			+ "<td class='content' " + (large ? "colspan='3'" : "") + ">"
			+ "<p:field name='" + name + "' style='input' />"
			+ "</td>";
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
