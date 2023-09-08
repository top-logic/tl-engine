/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import static java.util.Objects.*;

import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * Algorithm to compute {@link ModelName}s for models and to resolve {@link ModelName}s to their
 * models.
 * 
 * @param <C>
 *        The class of the supported/required context.
 * @param <M>
 *        The type of the model that can be handled.
 * @param <N>
 *        The type of the names produced/interpreted.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ModelNamingScheme<C, M, N extends ModelName> {

	/**
	 * {@link PolymorphicConfiguration} for {@ModelNamingScheme}.
	 */
	public interface Config extends PolymorphicConfiguration<ModelNamingScheme<?, ?, ?>> {

		/** Name of the property {@link #getPriority()}. */
		String PRIORITY = "priority";

		/**
		 * @see ModelResolver.Config#getPriorities()
		 */
		@Name(PRIORITY)
		@StringDefault(ModelResolver.DEFAULT_PRIORITY_LEVEL_NAME)
		String getPriority();

	}

	private final Class<M> _modelClass;

	private final Class<? extends N> _nameClass;

	private final Class<C> _contextClass;

	/**
	 * Creates a {@link ModelNamingScheme}s.
	 * 
	 * @param modelClass
	 *        Is allowed to be null, when {@link #getModelClass()} does not return null.
	 * @param nameClass
	 *        Is allowed to be null, when {@link #getNameClass()} does not return null.
	 * @param contextClass
	 *        Is allowed to be null, when {@link #getContextClass()} does not return null.
	 */
	protected ModelNamingScheme(Class<M> modelClass, Class<? extends N> nameClass, Class<C> contextClass) {
		_modelClass = modelClass;
		_nameClass = nameClass;
		_contextClass = contextClass;
		requireNonNull(getModelClass(),
			"The 'model class' has to be either passed in the constructor or provided by overriding getModelClass().");
		requireNonNull(getNameClass(),
			"The 'name class' has to be either passed in the constructor or provided by overriding getNameClass().");
		requireNonNull(getContextClass(),
			"The 'context class' has to be either passed in the constructor or provided by overriding getContextClass().");
	}

	/**
	 * Creates a {@link ModelNamingScheme}.
	 * <p>
	 * Variant of {@link #ModelNamingScheme(Class, Class, Class)} where the parameters are all null.
	 * This is used for the subclasses that define the model-, name- and context-class by overriding
	 * the corresponding method.
	 * </p>
	 * 
	 * @deprecated Use {@link #ModelNamingScheme(Class, Class, Class)}
	 */
	@Deprecated
	public ModelNamingScheme() {
		this(null, null, null);
	}

	/**
	 * The configuration interface that represents the structured name of this
	 * {@link ModelNamingScheme}.
	 */
	public Class<? extends N> getNameClass() {
		return _nameClass;
	}

	/**
	 * The type of the model that is processed by this {@link ModelNamingScheme}.
	 */
	public Class<M> getModelClass() {
		return _modelClass;
	}

	/**
	 * The type of the required value context.
	 */
	public Class<C> getContextClass() {
		return _contextClass;
	}

	/**
	 * Locates the model that is identified by the given {@link ModelName}.
	 * 
	 * @param context
	 *        The context in which the given name should be resolved.
	 *        @param valueContext The context object relative to which the given name should be resolved.
	 * @param name
	 *        The name of the model.
	 * 
	 * @return the located named model.
	 */
	public abstract M locateModel(ActionContext context, C valueContext, N name);

	/**
	 * Creates a {@link ModelName} for the given model.
	 * 
	 * <p>
	 * This method can serve as implementation of {@link NamedModel#getModelName()} in
	 * {@link NamedModel} instances.
	 * </p>
	 * 
	 * @param valueContext
	 *        The context object relative to which the created name must be resolved later on, see
	 *        {@link #locateModel(ActionContext, Object, ModelName)}.
	 * @param model
	 *        The model to be named.
	 * @return The {@link ModelName} of the given model.
	 */
	protected abstract Maybe<N> buildName(C valueContext, M model);

	/**
	 * Creates a {@link ModelName} compatible with this {@link ModelNamingScheme}.
	 */
	protected final N createName() {
		return TypedConfiguration.newConfigItem(getNameClass());
	}

}