/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task.schedule;

import java.util.Date;

import org.w3c.dom.Document;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.values.edit.annotation.PropertyEditor;
import com.top_logic.layout.form.values.edit.editor.DateTimeEditor;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.util.sched.task.Task;

/**
 * {@link SchedulingAlgorithm} for running a {@link Task} once.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
public class OnceSchedule<C extends OnceSchedule.Config<?>> extends AbstractSchedulingAlgorithm<OnceSchedule.Config<?>> {

	/** {@link TypedConfiguration} of {@link OnceSchedule}. */
	@TagName("once")
	public interface Config<S extends OnceSchedule<?>> extends AbstractSchedulingAlgorithm.Config<S> {

		/**
		 * @see #getTime()
		 */
		String TIME = "time";

		/** The time when to schedule the task. */
		@Mandatory
		@Name(TIME)
		@PropertyEditor(DateTimeEditor.class)
		Date getTime();

	}

	private static final String TIME_FIELD = "time";

	private static final Document TEMPLATE = DOMUtil.parseThreadSafe(""
		+ "	<table " + templateRootAttributes() + " >"
		+ templateStandardFields()
		+ "		<tr>"
		+ templateSmallField(TIME_FIELD)
		+ "		</tr>"
		+ "	</table>"
		);

		private long _time;

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link OnceSchedule}.
	 * 
	 * @param context
	 *        For instantiation of dependent configured objects.
	 * @param config
	 *        The configuration of this {@link OnceSchedule}.
	 */
	@CalledByReflection
	public OnceSchedule(InstantiationContext context, C config) {
		super(context, config);

		_time = config.getTime().getTime();
	}

	@Override
	public long nextSchedule(long notBefore, long lastSchedule) {
		if (lastSchedule >= _time) {
			// Has already been executed.
			return NO_SCHEDULE;
		}
		if (notBefore > _time) {
			// Too late.
			return NO_SCHEDULE;
		}
		return _time;
	}

	@Override
	public Document getFormTemplateDocument() {
		return TEMPLATE;
	}

	@Override
	public void fillFormGroup(FormGroup group) {
		super.fillFormGroup(group);

		group.addMember(FormFactory.newComplexField(
			TIME_FIELD, HTMLFormatter.getInstance().getDateTimeFormat(), _time, FormFactory.IMMUTABLE));
	}

}
