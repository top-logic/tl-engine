/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import static com.top_logic.basic.config.TypedConfiguration.*;
import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.list;
import static java.util.Collections.*;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * A {@link FormContextModificator} that combines multiple others.
 * 
 * <p>
 * The {@link Config#getChildren()} are called in the configured order, except for
 * {@link #clear(LayoutComponent, TLClass, TLObject, AttributeUpdateContainer, FormContainer) the
 * clear operation}, for which they are called in the reverse order.
 * </p>
 * 
 * @see #clear(LayoutComponent, TLClass, TLObject, AttributeUpdateContainer, FormContainer)
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class FormContextModificatorComposite extends
		AbstractConfiguredInstance<FormContextModificatorComposite.Config> implements FormContextModificator {

	/** {@link ConfigurationItem} for the {@link FormContextModificatorComposite}. */
	public interface Config extends PolymorphicConfiguration<FormContextModificatorComposite> {

		/** The inner {@link FormContextModificator}s. */
		@DefaultContainer
		List<PolymorphicConfiguration<? extends FormContextModificator>> getChildren();

	}

	private final List<? extends FormContextModificator> _children;

	private final List<? extends FormContextModificator> _childrenReversed;

	/**
	 * Called by the {@link TypedConfiguration} for creating a
	 * {@link FormContextModificatorComposite}.
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
	public FormContextModificatorComposite(InstantiationContext context, Config config) {
		super(context, config);
		_children = unmodifiableList(getInstanceList(context, config.getChildren()));
		_childrenReversed = reverseChildren(_children);
	}

	private List<? extends FormContextModificator> reverseChildren(List<? extends FormContextModificator> children) {
		List<? extends FormContextModificator> reversed = list(children);
		reverse(reversed);
		return unmodifiableList(reversed);
	}

	/**
	 * Returns "true" unless at least one of the children returns "false".
	 */
	@Override
	public boolean preModify(LayoutComponent component, TLClass type, TLObject wrapper) {
		boolean result = true;
		for (FormContextModificator modificator : _children) {
			result &= modificator.preModify(component, type, wrapper);
		}
		return result;
	}

	@Override
	public void modify(LayoutComponent component, String name, FormMember member, TLStructuredTypePart typePart,
			TLClass type, TLObject wrapper, AttributeUpdate update, AttributeFormContext context, FormContainer group) {
		_children.forEach(modificator -> modificator.modify(component, name, member, typePart, type, wrapper,
			update, context, group));
	}

	@Override
	public void postModify(LayoutComponent component, TLClass type, TLObject wrapper, AttributeFormContext context,
			FormContainer group) {
		_children.forEach(modificator -> modificator.postModify(component, type, wrapper, context, group));
	}

	@Override
	public void clear(LayoutComponent component, TLClass type, TLObject wrapper, AttributeUpdateContainer container,
			FormContainer group) {
		_childrenReversed
			.forEach(modificator -> modificator.clear(component, type, wrapper, container, group));
	}

}
