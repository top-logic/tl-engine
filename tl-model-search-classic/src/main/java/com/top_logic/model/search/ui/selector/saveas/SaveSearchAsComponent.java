/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.selector.saveas;

import static com.top_logic.layout.form.values.Fields.*;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.element.layout.meta.search.PublishableFieldSupport.VisisbleGroupsFieldDisableListener;
import com.top_logic.element.layout.meta.search.QueryUtils;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.linking.Null;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.component.AbstractCreateComponent;
import com.top_logic.layout.form.constraints.StringLengthConstraint;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.model.search.persistency.expressions.SearchExpression;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.tool.boundsec.wrap.Group;

/**
 * {@link AbstractCreateComponent} for saving a search expression under a new name.
 */
public class SaveSearchAsComponent extends AbstractCreateComponent {

	/**
	 * {@link ConfigurationItem} for the {@link SaveSearchAsComponent}.
	 */
	public interface Config extends AbstractCreateComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Override
		@Deprecated
		@FormattedDefault(AbstractCreateComponent.VALUE_NEW_MODEL_NO_ACTION)
		NewModelAction getNewModelAction();

		@Override
		@Deprecated
		@BooleanDefault(false)
		boolean getSetParentToEdit();

		@ItemDefault(Null.class)
		@Override
		ModelSpec getModelSpec();

		@StringDefault(SaveSearchAsCommand.COMMAND_ID)
		@Override
		String getCreateHandler();

		@Override
		default void addAdditionalCommandGroups(Set<? super BoundCommandGroup> additionalGroups) {
			AbstractCreateComponent.Config.super.addAdditionalCommandGroups(additionalGroups);
			additionalGroups.add(CommandGroupRegistry.resolve(QueryUtils.PUBLISH_NAME));
		}

	}

	/**
	 * The name of the {@link FormField} representing the "name" under which the search is
	 * persisted.
	 */
	public static final String FIELD_NAME_NAME = "name";

	/** The maximum size of the name of a {@link SearchExpression}. */
	public static final int MAX_NAME_SIZE = 100;

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link SaveSearchAsComponent}.
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
	public SaveSearchAsComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public FormContext createFormContext() {
		FormContext formContext = formContext(this);
		StringField nameField = line(formContext, FIELD_NAME_NAME);
		nameField.setMandatory(MANDATORY);
		nameField.addConstraint(new StringLengthConstraint(1, MAX_NAME_SIZE));
		FormGroup publishGroup = group(formContext, QueryUtils.FORM_GROUP);
		BooleanField publishField = checkbox(publishGroup, QueryUtils.PUBLISH_QUERY_FIELD);
		publishField.setAsBoolean(false); // The initial value is "null", causing an NPE when calling getAsBoolean().
		publishField.addValueListener(new VisisbleGroupsFieldDisableListener());
		SelectField groupField =
			selectField(publishGroup, QueryUtils.VISIBLE_GROUPS_FIELD, SelectField.MULTIPLE, !SelectField.IMMUTABLE);
		groupField.setOptions(getAvailableGroups());
		return formContext;
	}

	private List<?> getAvailableGroups() {
		return Group.getAll();
	}

	@Override
	protected boolean supportsInternalModel(Object object) {
		return object == null;
	}

}
