/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task.schedule;

import java.util.List;

import org.w3c.dom.Document;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.ResourceView;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.util.Resources;

/**
 * Combines multiple {@link SchedulingAlgorithm}s.
 * 
 * <p>
 * The earliest result from all {@link SchedulingAlgorithm}s is used for the next schedule.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@InApp
@Label("Combined schedule")
public class SchedulingAlgorithmCombinator implements SchedulingAlgorithm {

	/** The name of the {@link FormGroup} containing all the children's {@link FormGroup}s. */
	public static final String NAME_FORM_GROUP_CHILDREN = "children";

	/**
	 * The name stem for the {@link FormGroup}s of the children.
	 * <p>
	 * The index of the child will be appended to it. (Therefore "stem")
	 * </p>
	 */
	public static final String NAME_STEM_FORM_GROUP_CHILD = "child_";

	private static final Document TEMPLATE = DOMUtil.parseThreadSafe(""
		+ "	<table " + AbstractSchedulingAlgorithm.templateRootAttributes() + " >"
		+ "		<p:field name='" + NAME_FORM_GROUP_CHILDREN + "'>"
		+ "			<t:list>"
		+ "				<tbody>"
		+ "					<t:items>"
		+ "						<tr>"
		+ "							<td colspan='4'>"
		+ "								<fieldset>"
		+ "									<legend>"
		+ "										<p:self style='label' />"
		+ "									</legend>"
		+ "									<p:self />"
		+ "								</fieldset>"
		+ "							</td>"
		+ "						</tr>"
		+ "					</t:items>"
		+ "				</tbody>"
		+ "			</t:list>"
		+ "		</p:field>"
		+ "	</table>"
	);

	/**
	 * Instantiates and combines the given {@link SchedulingAlgorithm} configurations.
	 */
	public static SchedulingAlgorithm combine(InstantiationContext context,
			List<? extends PolymorphicConfiguration<? extends SchedulingAlgorithm>> scheduleConfigs) {
		List<SchedulingAlgorithm> schedules = TypedConfiguration.getInstanceList(context, scheduleConfigs);
		return combine(schedules);
	}

	/**
	 * Combines the given {@link SchedulingAlgorithm}s.
	 */
	public static SchedulingAlgorithm combine(List<? extends SchedulingAlgorithm> schedules) {
		if (schedules.isEmpty()) {
			return NeverSchedule.INSTANCE;
		}
		if (schedules.size() == 1) {
			return schedules.get(0);
		}
		return new SchedulingAlgorithmCombinator(schedules);
	}

	private List<? extends SchedulingAlgorithm> _schedules;

	private SchedulingAlgorithmCombinator(List<? extends SchedulingAlgorithm> schedules) {
		_schedules = schedules;
	}

	@Override
	public long nextSchedule(long notBefore, long lastSchedule) {
		long result = NO_SCHEDULE;
		for (SchedulingAlgorithm child : getChildren()) {
			long next = child.nextSchedule(notBefore, lastSchedule);
			result = first(result, next);
		}
		return result;
	}

	/**
	 * Returns the first of the two times.
	 * <p>
	 * {@link Maybe#none()} is treated as last possible time.
	 * </p>
	 * 
	 * @param current
	 *        Is not allowed to be or contain <code>null</code>.
	 * @param next
	 *        Is not allowed to be or contain <code>null</code>.
	 * @return The parameter which is the first.
	 */
	protected final long first(long current, long next) {
		if (current == NO_SCHEDULE) {
			return next;
		}
		if (next == NO_SCHEDULE) {
			return current;
		}
		return Math.min(current, next);
	}

	/**
	 * Hook for subclasses that want to change the list of children programmatically.
	 * <p>
	 * The order of the children is not relevant.
	 * </p>
	 */
	protected List<? extends SchedulingAlgorithm> getChildren() {
		return _schedules;
	}

	@Override
	public void fillFormGroup(FormGroup group) {
		group.addMember(createChildrenFormGroup());
		group.setControlProvider(AbstractSchedulingAlgorithm.createControlProvider(TEMPLATE));
	}

	/**
	 * Create the {@link FormGroup} containing all the children's {@link FormGroup}s.
	 * 
	 * @return Never null.
	 */
	protected FormGroup createChildrenFormGroup() {
		FormGroup childrenGroup = new FormGroup(NAME_FORM_GROUP_CHILDREN, getI18nPrefix());
		int i = 0;
		for (SchedulingAlgorithm child : getChildren()) {
			FormGroup childGroup = createChildFormGroup(child, i);
			childrenGroup.addMember(childGroup);
			i += 1;
		}
		return childrenGroup;
	}

	private ResPrefix getI18nPrefix() {
		return ResPrefix.legacyClass(SchedulingAlgorithmCombinator.class);
	}

	/**
	 * Create the {@link FormGroup} for the given child {@link SchedulingAlgorithm}.
	 * 
	 * @param child
	 *        Is never null.
	 * @param index
	 *        The index of the child.
	 * @return The {@link FormGroup} created by the child, enhanced with the top level
	 *         {@link ResourceView} and a label containing its index. Never null.
	 */
	protected FormGroup createChildFormGroup(SchedulingAlgorithm child, int index) {
		FormGroup childGroup = AbstractSchedulingAlgorithm.createUI(child, NAME_STEM_FORM_GROUP_CHILD + index);
		ResKey1 labelKey = I18NConstants.COMBINATOR_CHILD_TITLE__INDEX;
		childGroup.setLabel(Resources.getInstance().getString(labelKey.fill(index + 1)));
		return childGroup;
	}

}
