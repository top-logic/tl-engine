/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import static com.top_logic.basic.col.factory.CollectionFactory.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * Algorithm for building names from context local object and matching them by their names.
 * 
 * <p>
 * A {@link ValueNamingScheme} is used to identify values in a
 * {@link com.top_logic.layout.scripting.recorder.specialcases.CollectionValueScope}.
 * </p>
 * 
 * @see NameBase
 * 
 * @param <M>
 *        The most general type of models that this {@link ValueNamingScheme} can identify.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ValueNamingScheme<M> {

	@Abstract
	public interface NameBase extends ConfigurationItem {
		/**
		 * {@link ValueNamingScheme} name that handles the {@link #getName()}.
		 * 
		 * @see ValueNamingSchemeRegistry#getNameProvider(String)
		 */
		String getProviderName();

		/**
		 * @see #getProviderName()
		 */
		void setProviderName(String value);

		/**
		 * The structured name of this reference.
		 */
		@EntryTag("part")
		@Key(NamePart.KEY_PROPERTY_NAME)
		Map<String, NamePart> getName();

		/**
		 * @see #getName()
		 */
		void setName(Map<String, NamePart> name);

		/**
		 * Part of a
		 * {@link com.top_logic.layout.scripting.recorder.ref.ValueNamingScheme.NameBase#getName()}.
		 */
		interface NamePart extends ConfigurationItem {

			String KEY_PROPERTY_NAME = "key";

			/**
			 * The key for this {@link NamePart}.
			 */
			@Name(KEY_PROPERTY_NAME)
			String getKey();

			/**
			 * @see #getKey()
			 */
			void setKey(String value);

			/**
			 * Description of the application value this {@link NamePart} consists of.
			 */
			ModelName getValue();

			/**
			 * @see #getValue()
			 */
			void setValue(ModelName value);
		}
	}

	/**
	 * Hook for subclasses that should not be recorded.
	 * <p>
	 * This is needed for implementations that the user has to use manually.
	 * </p>
	 */
	public boolean isRecorded() {
		return true;
	}

	/**
	 * Name of this provider for referencing it from a {@link NameBase}.
	 * 
	 * @see NameBase#getProviderName()
	 */
	public final String providerName() {
		return getClass().getName();
	}

	/**
	 * The dynamic representative of the most general type, this {@link ValueNamingScheme} can
	 * handle.
	 */
	public abstract Class<M> getModelClass();

	/**
	 * Checks whether this {@link ValueNamingScheme} can build {@link NameBase#getName()} for the
	 * given model. The standard implementation just checks if the model is of a
	 * {@link #getModelClass() supported type}.
	 * 
	 * Subclasses should override this method if there are further restrictions on the supported
	 * model.
	 */
	public boolean supportsModel(Object model) {
		return getModelClass().isInstance(model);
	}

	/**
	 * Constructs a structured name for the given target object.
	 * 
	 * @param model
	 *        The object to build a name for.
	 * @return Key value pairs that uniquely identify the given target object within all of its
	 *         potential contexts. The values in the returned map must have global names (The
	 *         {@link ModelResolver} must identify them without value context, see
	 *         {@link ModelResolver#buildModelName(Object)}).
	 */
	public abstract Map<String, Object> getName(M model);

	/**
	 * Tests, whether the given target object has the given name.
	 * 
	 * <p>
	 * Note: An (inefficient) default implementation is available with
	 * {@link #matchesDefault(Map, Object)}.
	 * </p>
	 * 
	 * @param name
	 *        Result of {@link #getName(Object)}.
	 * @return Whether the given model has the given name.
	 */
	public abstract boolean matches(Map<String, Object> name, M model);

	/**
	 * Utility for the most simple (but inefficient) implementation of {@link #matchesDefault(Map, Object)}.
	 */
	protected final boolean matchesDefault(Map<String, Object> name, M model) {
		return name.equals(getName(model));
	}

	/**
	 * Resolves the given {@link NameBase} in the given context.
	 * 
	 * @param actionContext
	 *        The current {@link ActionContext} during replay.
	 * @param options
	 *        The options to choose from.
	 * @param valueName
	 *        The {@link NameBase} that identifies an option.
	 * @return The referenced option.
	 */
	public static Object resolveNamedValue(ActionContext actionContext, Collection<?> options, NameBase valueName) {
		ValueNamingScheme<?> namingScheme = ValueNamingSchemeRegistry.getNameProvider(valueName.getProviderName());
		return namingScheme.resolveNamedValueInternal(actionContext, options, valueName);
	}

	private Object resolveNamedValueInternal(ActionContext actionContext, Collection<?> options, NameBase valueName) {
		// We can safely skip the unsupported options, as they are not the searched value.
		// If we would be searching for one of them value,
		// we would not have used this naming scheme.
		Collection<M> filteredOptions = CollectionUtil.copyOnly(getModelClass(), options);
		Map<String, Object> nameParts = resolveNameParts(actionContext, filteredOptions, valueName);
		return findNamedValue(filteredOptions, nameParts);
	}

	private Map<String, Object> resolveNameParts(ActionContext actionContext, Collection<M> options, NameBase valueName) {
		List<Map<String, Object>> optionsAsNameParts = getOptionsNameParts(options);
		Map<String, Object> nameParts = new HashMap<>();
		if (valueName.getName() != null) {
			for (NameBase.NamePart part : valueName.getName().values()) {
				String partKey = part.getKey();
				List<Object> propertyOptions = MapUtil.getFromAll(optionsAsNameParts, partKey);
				Object partValue = actionContext.resolve(part.getValue(), propertyOptions);
				nameParts.put(partKey, partValue);
			}
		}
		return nameParts;
	}

	private Object findNamedValue(Collection<M> options, Map<String, Object> nameParts) {
		Maybe<Object> candidate = Maybe.none();
		for (M row : options) {
			// The check here is necessary as subclasses can put further restrictions on the model
			// beyond the type check.
			// We can safely skip the unsupported options, as they are not the searched value.
			if (supportsModel(row) && matches(nameParts, row)) {
				if (candidate.hasValue()) {
					String message = "Resolving ValueNamingScheme '" + getClass().getName() + "' failed!"
						+ " Ambiguous match for: " + nameParts + ". First match: " + candidate.get()
						+ ". Second match: " + row + ". Options: " + makeDebugFriendly(options);
					throw new RuntimeException(message);
				}
				// Row cannot be null as "supportsModel" above contains an instance-of check.
				// Therefore, Maybe.some() and not Maybe.toMaybeButTreatNullAsValidValue().
				candidate = Maybe.<Object> some(row);
			}
		}

		if (!candidate.hasValue()) {
			String message = "Resolving ValueNamingScheme '" + getClass().getName() + "' failed!"
				+ " Local object not found: " + nameParts + "; Options: " + makeDebugFriendly(options);
			throw new RuntimeException(message);
		}
		return candidate.get();
	}

	private String makeDebugFriendly(Collection<M> options) {
		String optionsAsString = "'";
		for (Object option : options) {
			optionsAsString += (StringServices.getObjectDescription(option) + "'; '");
		}
		optionsAsString += "'";
		return optionsAsString;
	}

	/**
	 * Returns a list with the name parts of the given options. The options that cannot be
	 * referenced with this naming scheme will be skipped.
	 */
	private List<Map<String, Object>> getOptionsNameParts(Collection<M> options) {
		List<Map<String, Object>> optionsNameParts = new ArrayList<>();
		for (M option : options) {
			Map<String, Object> optionNameParts = getName(option);
			if (optionNameParts == null) {
				// We can safely skip the unsupported options, as they are not the searched value.
				continue;
			}
			optionsNameParts.add(optionNameParts);
		}
		return optionsNameParts;
	}

	/**
	 * Creates a {@link NameBase} for the value based on the registered {@link ValueNamingScheme}.
	 * Returns {@link Maybe#none()} if that fails.
	 */
	public static <N extends NameBase> Maybe<N> namedValue(Class<N> nameType, Object value) {
		List<ValueNamingScheme<Object>> namings = ValueNamingSchemeRegistry.getNameProviders(value.getClass());
		for (ValueNamingScheme<Object> naming : namings) {
			Maybe<N> namedValue = naming.namedValueDirect(nameType, value);
			if (namedValue.hasValue()) {
				return namedValue;
			}
		}
		return Maybe.none();
	}

	/**
	 * Creates a {@link NameBase} for the value directly with this naming scheme. Returns
	 * {@link Maybe#none()} if that fails.
	 */
	public <N extends NameBase> Maybe<N> namedValueDirect(Class<N> nameType, M value) {
		try {
			return namedValueUnsafe(nameType, value);
		} catch (RuntimeException ex) {
			/* Unable to use this ValueNamingScheme. This can happen, as not every NamingScheme can
			 * be used in every situation. The caller will try the next one. */
			return Maybe.none();
		}
	}

	private <N extends NameBase> Maybe<N> namedValueUnsafe(Class<N> nameType, M value) {
		if (!isRecorded()) {
			return Maybe.none();
		}
		if (!supportsModel(value)) {
			return Maybe.none();
		}
		Map<String, Object> nameParts = getName(value);
		if (nameParts == null) {
			return Maybe.none();
		}
		Map<String, NameBase.NamePart> nameDescription = referenceNameParts(nameParts);
		return Maybe.some(namedValue(nameType, providerName(), nameDescription));
	}

	private Map<String, NameBase.NamePart> referenceNameParts(Map<String, Object> nameParts) {
		Map<String, NameBase.NamePart> nameDescription = map();
		for (Entry<String, Object> entry : nameParts.entrySet()) {
			String property = entry.getKey();
			Object propertyValue = entry.getValue();
			NameBase.NamePart namePart = TypedConfiguration.newConfigItem(NameBase.NamePart.class);
			namePart.setKey(property);
			// We have to give a value context here so nested ValueNamingSchemes will be used.
			// And we fake it for simplicity & performance:
			// To build the real value scope, we would have to use this naming scheme
			// on all other values in the current value scope, and than most probably not even
			// use them as we need them only when we resolve the value.
			Set<Object> fakedValueContext = Collections.singleton(propertyValue);
			ModelName valueRef = ModelResolver.buildModelName(fakedValueContext, propertyValue);
			namePart.setValue(valueRef);

			nameDescription.put(property, namePart);
		}
		return nameDescription;
	}

	/**
	 * Creates a {@link NameBase} name.
	 */
	public static <N extends NameBase> N namedValue(Class<N> nameType, String providerName,
			Map<String, NameBase.NamePart> name) {
		N namedObject = TypedConfiguration.newConfigItem(nameType);
		namedObject.setProviderName(providerName);
		namedObject.setName(name);
		return namedObject;
	}

}
