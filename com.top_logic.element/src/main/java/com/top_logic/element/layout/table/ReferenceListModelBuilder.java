/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.Utils;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.ReferenceStorage;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link ListModelBuilder} retrieving the list values by accessing a {@link Config#getReference()
 * configured reference} on the passed model.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ReferenceListModelBuilder<C extends ReferenceListModelBuilder.Config<?>>
		extends AbstractConfiguredInstance<C> implements ListModelBuilder {

	/**
	 * Configuration options for {@link ReferenceListModelBuilder}.
	 */
	public interface Config<I extends ReferenceListModelBuilder<?>> extends PolymorphicConfiguration<I> {

		/** @see #getReference() */
		String REFERENCE = "reference";

		/** Name of the reference to retrieve values for display. */
		@Name(REFERENCE)
		@Mandatory
		String getReference();

	}

	private final TLStructuredTypePart _reference;

	/**
	 * Creates a {@link ReferenceListModelBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ReferenceListModelBuilder(InstantiationContext context, C config) throws ConfigurationException {
		super(context, config);
		_reference = (TLStructuredTypePart) TLModelUtil.findPart(config.getReference());
		
		if (!(AttributeOperations.getStorageImplementation(getReference()) instanceof ReferenceStorage)) {
			context.error("Configured attribute " + config.getReference() + " is not a reference attribute.");
		}
	}

	/**
	 * See {@link Config#getReference()}.
	 */
	public final TLStructuredTypePart getReference() {
		return _reference;
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		if (businessModel == null) {
			return Collections.emptyList();
		}
		List<TLObject> result = new ArrayList<>();
		addValues(result, (TLObject) businessModel);
		return result;
	}

	/**
	 * Adds all values of {@link #getReference()} of the given model element.
	 * 
	 * @param model
	 *        Business model of the component.
	 */
	protected void addValues(List<TLObject> result, TLObject model) {
		addDirectValues(result, model);
	}

	/**
	 * Adds all values found in {@link #getReference()} of the given parent object to the given
	 * result collection.
	 */
	protected void addDirectValues(Collection<TLObject> result, TLObject parent) {
		Object value = parent.tValue(getReference());

		if (value instanceof Collection) {
			for (Object theObject : (Collection<?>) value) {
				result.add((TLObject) theObject);
			}
		} else if (value != null) {
			result.add((TLObject) value);
		}
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return (aModel instanceof TLObject) && hasSupportedType((TLObject) aModel);
	}

	/**
	 * Whether the given object can be used as component model.
	 * 
	 * <p>
	 * Implementation of {@link #supportsModel(Object, LayoutComponent)} with model cast to
	 * {@link TLObject}.
	 * </p>
	 */
	protected boolean hasSupportedType(TLObject obj) {
		return definesReference(obj);
	}

	/**
	 * Whether {@link #getReference()} is defined for the given object.
	 */
	protected final boolean definesReference(TLObject obj) {
		return TLModelUtil.isCompatibleInstance(getReference().getOwner(), obj);
	}

	@Override
	public boolean supportsListElement(LayoutComponent aComponent, Object anElement) {
		if (!(anElement instanceof TLObject)) {
			return false;
		}
		Object currentBusinessModel = aComponent.getModel();
		TLObject potentialListElem = (TLObject) anElement;
		Collection<? extends TLObject> owners = getOwners(potentialListElem);
		for (TLObject owner : owners) {
			if (Utils.equals(currentBusinessModel, owner)) {
				return true;
			}

			boolean result = supportsOwner(aComponent, owner);
			if (result) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Whether the given owner (object that has some list element in its {@link #getReference()
	 * reference}) can be (directly or indirectly) be displayed by the component using this builder.
	 */
	protected boolean supportsOwner(LayoutComponent component, TLObject owner) {
		return supportsModel(owner, component);
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent aComponent, Object anElement) {
		Object currentModel = aComponent.getModel();
		Collection<? extends TLObject> owners = getOwners((TLObject) anElement);
		if (owners.isEmpty()) {
			return null;
		}

		if (shouldKeepModel(aComponent, owners)) {
			return currentModel;
		}

		return owners.iterator().next();
	}

	/**
	 * If an element should be displayed that is owned by some of the given owners, whether the
	 * component should keep its current business model (or select another one).
	 */
	private boolean shouldKeepModel(LayoutComponent aComponent, Collection<? extends TLObject> owners) {
		for (TLObject owner : owners) {
			if (shouldKeepModel(aComponent, owner)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * If an element should be displayed that is owned by the given owner, whether the component
	 * should keep its current business model (or select another one).
	 */
	protected boolean shouldKeepModel(LayoutComponent aComponent, TLObject owner) {
		Object currentModel = aComponent.getModel();
		return Utils.equals(currentModel, owner);
	}

	private Collection<? extends TLObject> getOwners(TLObject element) {
		return AttributeOperations.getReferers(element, getReference());
	}

}
