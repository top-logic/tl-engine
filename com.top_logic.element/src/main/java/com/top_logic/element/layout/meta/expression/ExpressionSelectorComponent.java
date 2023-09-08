/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.expression;

import static com.top_logic.basic.util.Utils.*;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.top_logic.base.security.SecurityContext;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.element.layout.meta.search.QueryUtils;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.knowledge.wrap.mapBasedPersistancy.MapBasedPersistancySupport;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.TLContext;

/**
 * A {@link LayoutComponent} for selecting a search or report expression.
 * <p>
 * The type parameter "W" represents the Wrapper that is selected.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class ExpressionSelectorComponent<W extends Wrapper> extends FormComponent
		implements Selectable {

	/**
	 * {@link ConfigurationItem} for the {@link ExpressionSelectorComponent}.
	 */
	public interface Config extends FormComponent.Config, Selectable.SelectableConfig {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/** The component displaying the search/report that is selected with this component. */
		ComponentName getConfigurator();

		@Override
		default void addAdditionalCommandGroups(Set<? super BoundCommandGroup> additionalGroups) {
			FormComponent.Config.super.addAdditionalCommandGroups(additionalGroups);
			additionalGroups.add(CommandGroupRegistry.resolve(QueryUtils.PUBLISH_NAME));
			additionalGroups.add(CommandGroupRegistry.resolve(QueryUtils.OWNER_WRITE_NAME));
		}

	}

	private final ComponentName _target;

	private boolean _active = true;

	/**
	 * Called by the {@link TypedConfiguration} for creating a
	 * {@link ExpressionSelectorComponent}.
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
	public ExpressionSelectorComponent(InstantiationContext context, Config config)
			throws ConfigurationException {
		super(context, config);
		// The name is resolved on-demand, as the component might not be instantiated.
		_target = config.getConfigurator();
	}

	/**
	 * @see Config#getConfigurator()
	 * 
	 * @return Never null.
	 */
	public LayoutComponent getTarget() {
		return requireNonNull(getMainLayout().getComponentByName(_target));
	}

	@Override
	public FormContext createFormContext() {
		FormContext result = new FormContext(this);
		SelectField expressionField = FormFactory.newSelectField(getExpressionSelectorName(), getStoredExpressions());
		expressionField.addValueListener(new ExpressionSelectedListener<>(this));
		result.addMember(expressionField);
		FormGroup publishingGroup = getPublishingFormGroup();
		result.addMember(publishingGroup);
		return result;
	}

	/** The options of the selection represented by this component. */
	protected abstract List<? extends W> getStoredExpressions();

	void updatePublishFields(W expression) {
		if (!hasFormContext()) {
			return;
		}

		FormContext context = getFormContext();
		FormGroup publishGroup = (FormGroup) context.getMember(QueryUtils.FORM_GROUP);
		SelectField selectionField = (SelectField) publishGroup.getMember(QueryUtils.VISIBLE_GROUPS_FIELD);

		if (expression == null) {
			publishGroup.setDisabled(true);
			publishGroup.setVisible(false);
			selectionField.setVisible(false);
		} else {
			// The publishing Group is only visible if the selected query is
			// already published and if the current user is equals the
			// owner of the selected query.
			if (QueryUtils.allowWriteAndPublish(this)) {
				{
					List<?> groupAssociations =
						MapBasedPersistancySupport.getGroupAssociation((AbstractWrapper) expression);
					Person creator = expression.getCreator();
					Person currentUser = TLContext.getContext().getCurrentPersonWrapper();

					// root or owner can modify published queries
					if (WrapperHistoryUtils.getUnversionedIdentity(currentUser).equals(
						WrapperHistoryUtils.getUnversionedIdentity(creator))
						|| SecurityContext.isAdmin()) {
						boolean isPublished;
						if (groupAssociations.isEmpty()) {
							isPublished = false;
						} else {
							isPublished = true;
						}
						publishGroup.setVisible(true);
						publishGroup.setDisabled(false);
						((BooleanField) publishGroup.getMember(QueryUtils.PUBLISH_QUERY_FIELD))
							.setAsBoolean(isPublished);
						selectionField.setAsSelection(groupAssociations);
						selectionField.setDisabled(!isPublished);
						selectionField.setVisible(true);
					} else {
						publishGroup.setDisabled(true);
						publishGroup.setVisible(false);
						selectionField.setVisible(false);
					}
				}
			} else {
				publishGroup.setVisible(false);
				publishGroup.setDisabled(true);
			}
		}
	}

	private FormGroup getPublishingFormGroup() {
		return getPublishingFormGroup(false, getResPrefix());
	}

	private FormGroup getPublishingFormGroup(boolean visible, ResPrefix resPrefix) {
		FormGroup publishGroup = new FormGroup(QueryUtils.FORM_GROUP, resPrefix);
		BooleanField publishQueryField = FormFactory.newBooleanField(QueryUtils.PUBLISH_QUERY_FIELD);

		publishQueryField.setAsBoolean(false);
		publishGroup.addMember(publishQueryField);

		List<?> visibleGroups = Group.getAll();
		final SelectField visibleGroupsField =
			FormFactory.newSelectField(QueryUtils.VISIBLE_GROUPS_FIELD, visibleGroups, true, false);

		visibleGroupsField.setDisabled(true);
		publishGroup.addMember(visibleGroupsField);

		publishGroup.setVisible(visible);

		publishQueryField.addValueListener(new ValueListener() {

			@Override
			public void valueChanged(FormField aField, Object aOldValue, Object aNewValue) {
				if (((BooleanField) aField).getAsBoolean()) {
					visibleGroupsField.setDisabled(false);
					visibleGroupsField.setMandatory(true);
				} else {
					visibleGroupsField.setDisabled(true);
					visibleGroupsField.setMandatory(false);
				}
			}
		});

		return publishGroup;
	}

	@Override
	public W getSelected() {
		if (!hasFormContext()) {
			return null;
		}

		SelectField field = getSelectorField();
		return getWrapperClass().cast(field.getSingleSelection());
	}

	@Override
	public boolean setSelected(Object selection) {
		if (!hasFormContext()) {
			return false;
		}
		if ((selection != null) && !getWrapperClass().isInstance(selection)) {
			return false;
		}

		SelectField field = getSelectorField();
		field.setAsSingleSelection(selection);
		return true;
	}

	/**
	 * The {@link SelectField} with which the user chooses the expression.
	 * 
	 * @return null, if there is no {@link FormContext}.
	 */
	public SelectField getSelectorField() {
		if (!hasFormContext()) {
			return null;
		}
		return (SelectField) getFormContext().getMember(getExpressionSelectorName());
	}

	@Override
	protected boolean observeAllTypes() {
		return true;
	}

	@Override
	protected boolean receiveModelCreatedEvent(Object model, Object changedBy) {
		if (!hasFormContext()) {
			return false;
		}
		if (isSupportedExpression(model)) {
			SelectField field = getSelectorField();
			setActive(false);
			/* Setting the options removes the value. Therefore, the value has to be saved before
			 * and set after setting the options. */
			Object value = field.getValue();
			field.setOptions(getStoredExpressions());
			field.setValue(value);
			setActive(true);
		}
		return super.receiveModelCreatedEvent(model, changedBy);
	}

	/**
	 * Whether the object is an expression that should be one of the options to select.
	 * 
	 * @param model
	 *        Is allowed to be null.
	 * @return true if the object is not null, has the correct type and a supported version.
	 * 
	 * @see #isSupportedVersion(Wrapper)
	 */
	protected boolean isSupportedExpression(Object model) {
		return getWrapperClass().isInstance(model) && isSupportedVersion(getWrapperClass().cast(model));
	}

	@Override
	protected boolean receiveModelDeletedEvent(Set<TLObject> models, Object changedBy) {
		if (!hasFormContext()) {
			return false;
		}
		if (models.stream().anyMatch(getWrapperClass()::isInstance)) {
			SelectField field = getSelectorField();
			handleDeletion(models, field);
		}
		return super.receiveModelDeletedEvent(models, changedBy);
	}

	/**
	 * The given models have been deleted and could be one of the options of the given
	 * {@link SelectField}.
	 * 
	 * @param models
	 *        Never null.
	 * @param field
	 *        The field that could contain the model.
	 */
	protected void handleDeletion(Set<TLObject> models, SelectField field) {
		Object currentSelection = field.getSingleSelection();
		List<?> options = field.getOptions();
		if (!Collections.disjoint(models, options)) {
			setActive(false);
			List<Object> newOptions = new ArrayList<>(options);
			newOptions.removeAll(models);
			field.setOptions(newOptions);
			if (models.contains(currentSelection)) {
				field.setAsSingleSelection(currentSelection);
			}
			setActive(true);
		}
	}

	/**
	 * Whether the expression should be loaded, when the selection of this component changes.
	 */
	protected boolean isActive() {
		return _active;
	}

	/** @see #isActive() */
	protected void setActive(boolean active) {
		_active = active;
	}

	/**
	 * The given expression has been selected. Inform the {@link #getTarget()} component.
	 * 
	 * @param expression
	 *        Never null.
	 * @throws ConfigurationException
	 *         When loading expression failed.
	 */
	protected abstract void loadExpression(W expression) throws ConfigurationException;

	/**
	 * The name of the {@link FormField} with which the user chooses the expression.
	 * 
	 * @return Is not allowed to be null.
	 */
	public abstract String getExpressionSelectorName();

	/**
	 * Whether the given {@link Wrapper} represents an expression of a supported version.
	 */
	protected abstract boolean isSupportedVersion(W model);

	/**
	 * The type of the {@link Wrapper} representing the expressions that are selected with this
	 * component.
	 * 
	 * @return Is not allowed to be null.
	 */
	protected abstract Class<W> getWrapperClass();

}
