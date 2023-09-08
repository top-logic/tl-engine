/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.expression;

import java.util.Iterator;
import java.util.List;

import com.top_logic.base.services.simpleajax.AJAXCommandHandler;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.element.layout.meta.search.QueryUtils;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.mapBasedPersistancy.MapBasedPersistancySupport;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.wrap.Group;

/**
 * The common superclass for saving searches and reports.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class SaveExpressionCommand extends AJAXCommandHandler {

	/**
	 * Configuration for {@link SaveExpressionCommand}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AJAXCommandHandler.Config {

		@FormattedDefault(QueryUtils.OWNER_WRITE_NAME)
		@Override
		CommandGroupReference getGroup();

	}

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link SaveExpressionCommand}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public SaveExpressionCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	protected void publish(AbstractWrapper expression, FormContext parent) {
		FormGroup publishGroup = getPublishGroup(parent);
		if (!publishGroup.isVisible()) {
			return;
		}
		boolean publish = shouldPublish(publishGroup);
		List<Group> assignedGroups = getAssociatedGroups(expression);
		if (publish) {
			List<Group> selectedGroups = getSelectedGroups(publishGroup);
			updateGroups(expression, assignedGroups, selectedGroups);
		} else {
			deleteGroups(expression, assignedGroups);
		}
	}

	protected void updateGroups(AbstractWrapper expression, List<Group> currentGroups, List<Group> selectedGroups) {
		List<Group> selected = CollectionUtil.modifiableList(selectedGroups);
		List<Group> current = CollectionUtil.modifiableList(currentGroups);

		for (Iterator<Group> theIt = selected.iterator(); theIt.hasNext();) {
			Object group = theIt.next();

			if (current.contains(group)) {
				current.remove(group);
				theIt.remove();
			}
		}
		addGroups(expression, selected);
		deleteGroups(expression, current);
	}

	protected List<Group> getAssociatedGroups(AbstractWrapper expression) {
		return asGroups(MapBasedPersistancySupport.getGroupAssociation(expression));
	}

	public static void addGroups(Wrapper expression, List<Group> groups) {
		for (int i = 0; i < groups.size(); i++) {
			Group theGroup = groups.get(i);
			MapBasedPersistancySupport.assignContainer(expression, theGroup);
		}
	}

	public static void deleteGroups(AbstractWrapper expression, List<Group> groups) {
		for (int i = 0; i < groups.size(); i++) {
			Group theGroup = groups.get(i);
			MapBasedPersistancySupport.deleteGroupAssociation(expression, theGroup);
		}
	}

	public static FormGroup getPublishGroup(FormContainer parent) {
		return (FormGroup) parent.getMember(QueryUtils.FORM_GROUP);
	}

	public static boolean shouldPublish(FormGroup publishGroup) {
		BooleanField shouldPublishField = (BooleanField) publishGroup.getMember(QueryUtils.PUBLISH_QUERY_FIELD);
		return shouldPublishField.getAsBoolean();
	}

	public static List<Group> getSelectedGroups(FormGroup publishGroup) {
		SelectField selectedGroupsField = (SelectField) publishGroup.getMember(QueryUtils.VISIBLE_GROUPS_FIELD);
		return asGroups(selectedGroupsField.getSelection());
	}

	protected static List<Group> asGroups(List<?> untypedGroups) {
		return CollectionUtil.dynamicCastView(Group.class, untypedGroups);
	}

}
