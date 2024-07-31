/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;
import com.top_logic.model.StorageDetail;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TransientObject;
import com.top_logic.model.impl.TransientModelFactory;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLObjectProxy;

/**
 * {@link ModelNamingScheme} of transient {@link TLObject} values.
 * 
 * @implSpec The class is not parameterized with {@link TransientObject} to support
 *           {@link TLObjectProxy} which wrap a transient item.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TransientTLObjectNaming extends AbstractModelNamingScheme<TLObject, TransientTLObjectNaming.Name> {

	/** {@link Property} to hold already created names to detect cycles. */
	private static final Property<Map<Object, Name>> BUILT_NAMES =
		TypedAnnotatable.propertyMap("built names");

	/** {@link Property} to hold already resolved items to reuse in cycles. */
	private static final Property<Map<Integer, TLObject>> RESOLVED_ITEMS =
		TypedAnnotatable.propertyMap("resolved items");

	private static final Property<Integer> NEXT_ID =
		TypedAnnotatable.property(Integer.class, "nextId", Integer.valueOf(1));

	/**
	 * {@link ModelName} of {@link TransientTLObjectNaming}.
	 */
	public interface Name extends ModelName {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/**
		 * Type of the transient {@link TLObject}.
		 */
		@Mandatory
		TLModelPartRef getType();

		/**
		 * Setter for {@link #getType()}.
		 */
		void setType(TLModelPartRef value);

		/**
		 * Local identifier to identify the represented item in cycles, or <code>null</code> when
		 * the item is not represented multiple times.
		 */
		Integer getId();

		/**
		 * Setter for {@link #getId()}.
		 */
		void setId(Integer id);

		/**
		 * Reference to the name of a former occurrence of the represented item.
		 */
		Integer getRef();
		
		/**
		 * Setter for {@link #getRef()}.
		 */
		void setRef(Integer id);
		
		/**
		 * The values of the represented {@link TLObject}.
		 */
		@Key(Attribute.NAME_ATTRIBUTE)
		Map<String, Attribute> getAttributes();

		/**
		 * Service method to add a value to this {@link Name}.
		 * 
		 * @see #getAttributes()
		 */
		default Attribute putValue(String attribute, ModelName value) {
			Attribute result = TypedConfiguration.newConfigItem(Attribute.class);
			result.setName(attribute);
			result.setValue(value);
			return getAttributes().put(attribute, result);
		}
	}

	/**
	 * Holder for an attribute value.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Attribute extends NamedConfigMandatory {

		/** Configuration name for {@link #getValue()}. */
		String VALUE = "value";

		/**
		 * {@link ModelName} for the actual attribute value.
		 */
		@Mandatory
		@com.top_logic.basic.config.annotation.Name(VALUE)
		ModelName getValue();

		/**
		 * Setter for {@link #getValue()}.
		 */
		void setValue(ModelName value);

	}

	/**
	 * Creates a new {@link TransientTLObjectNaming}.
	 */
	public TransientTLObjectNaming() {
		super(TLObject.class, Name.class);
	}

	@Override
	protected void initName(Name name, TLObject model) {
		TLStructuredType type = model.tType();
		name.setType(TLModelPartRef.ref(type));
		
		boolean alreadyProcessed = checkAlreadyProcessed(name, model);
		if (alreadyProcessed) {
			return;
		}

		for (TLStructuredTypePart part : type.getAllParts()) {
			StorageDetail storage = part.getStorageImplementation();
			if (storage.isReadOnly()) {
				continue;
			}
			Object currentValue = model.tValue(part);
			Maybe<? extends ModelName> encodedValue = ModelResolver.buildModelNameIfAvailable(currentValue);
			if (!encodedValue.hasValue()) {
				continue;
			}
			name.putValue(part.getName(), encodedValue.get());
		}

	}

	private boolean checkAlreadyProcessed(Name name, TLObject model) {
		DisplayContext context = context();
		Map<Object, Name> existingNames = context.get(BUILT_NAMES);
		if (existingNames.isEmpty()) {
			// Default value: Build a new map
			Map<Object, Name> newSeenValues = new HashMap<>();
			newSeenValues.put(model, name);
			context.set(BUILT_NAMES, newSeenValues);
			return false;
		}
		Name existingName = existingNames.get(model);
		if (existingName == null) {
			existingNames.put(model, name);
			return false;
		}
		Integer id = existingName.getId();
		if (id != null) {
			// Already referenced.
		} else {
			id = nextId(context);
			existingName.setId(id);
		}
		name.setRef(id);
		return true;
	}

	private static DisplayContext context() {
		return DefaultDisplayContext.getDisplayContext();
	}

	private int nextId(DisplayContext context) {
		Integer id = context.get(NEXT_ID);
		context.set(NEXT_ID, Integer.valueOf(id.intValue() + 1));
		return id;
	}

	@Override
	public TLObject locateModel(ActionContext context, Name name) {
		Map<Integer, TLObject> allResolved = context().get(RESOLVED_ITEMS);
		TLClass itemType = resolveType(name);
		Integer reference = name.getRef();
		if (reference != null) {
			TLObject resolved = allResolved.get(reference);
			ApplicationAssertions.assertEquals(name, "Referenced object has different type.", itemType,
				resolved.tType());
			return resolved;
		}
		TLObject resolved = TransientModelFactory.createTransientObject(itemType);
		Integer id = name.getId();
		if (id != null) {
			if (allResolved.isEmpty()) {
				allResolved = new HashMap<>();
				context().set(RESOLVED_ITEMS, allResolved);
			}
			allResolved.put(id, resolved);
		}
		for (Attribute value : name.getAttributes().values()) {
			Object val = context.resolve(value.getValue());
			resolved.tUpdateByName(value.getName(), val);
		}
		return resolved;
	}

	private TLClass resolveType(Name name) {
		try {
			return name.getType().resolveClass();
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
	}

	@Override
	protected boolean isCompatibleModel(TLObject model) {
		if (!model.tTransient()) {
			return false;
		}
		return super.isCompatibleModel(model);
	}

}
