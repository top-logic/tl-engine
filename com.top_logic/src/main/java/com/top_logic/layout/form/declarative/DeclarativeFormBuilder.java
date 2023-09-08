/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.declarative;

import static com.top_logic.basic.config.TypedConfiguration.*;
import static com.top_logic.layout.form.values.Fields.*;

import java.util.function.Function;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.customization.ConfiguredAnnotationCustomizations;
import com.top_logic.basic.config.customization.CustomizationContainer;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.declarative.mapping.TLPropertyAccess;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.initializer.Initializer;
import com.top_logic.layout.form.values.edit.initializer.InitializerIndex;
import com.top_logic.layout.form.values.edit.initializer.InitializerProvider;
import com.top_logic.layout.form.values.edit.initializer.InitializerUtil;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ModelBuilder} for {@link FormComponent}s based on a {@link ConfigurationItem} as edit
 * model.
 * 
 * @param <T>
 *        The supported {@link LayoutComponent}'s model type.
 * @param <C>
 *        The type of the {@link ConfigurationItem} form model to use.
 * 
 * @see DeclarativeApplyHandler
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class DeclarativeFormBuilder<T, C extends ConfigurationItem> implements ModelBuilder,
		ConfiguredInstance<DeclarativeFormBuilder.Config> {

	/**
	 * Property set on the {@link DeclarativeFormOptions} context that allows access to the edited
	 * form model as a whole.
	 * 
	 * @see #getFormType(Object)
	 */
	public static final Property<ConfigurationItem> FORM_MODEL =
		TypedAnnotatable.property(ConfigurationItem.class, "formModel");

	/**
	 * Property set on the {@link DeclarativeFormOptions} context that allows access to the context
	 * component.
	 */
	public static final Property<LayoutComponent> COMPONENT =
		TypedAnnotatable.property(LayoutComponent.class, "component");

	/**
	 * {@link Property} providing access to a custom context object that can be configured using the
	 * configuration option {@link Config#getContext()}.
	 * 
	 * <p>
	 * The resolved concrete value can be accessed from the {@link DeclarativeFormOptions} context
	 * of all providers.
	 * </p>
	 * 
	 * <p>
	 * The value is provided through the configured {@link Config#getContext()} reference.
	 * </p>
	 */
	public static final Property<Object> CONTEXT = TypedAnnotatable.property(Object.class, "context");

	private final Config _config;

	/**
	 * Configuration interface for {@link DeclarativeFormBuilder}.
	 */
	public interface Config
			extends PolymorphicConfiguration<DeclarativeFormBuilder<?, ?>>, CustomizationContainer, WithShowNoModel {

		/**
		 * @see #isCreate()
		 */
		String CREATE = "create";

		/**
		 * @see #getCreateContextType()
		 */
		String CREATE_CONTEXT_TYPE = "createContextType";

		/**
		 * @see #isContextOptional()
		 */
		String CONTEXT_OPTIONAL = "contextOptional";

		/** Property name of {@link #getModelToFormMapping()}. */
		String MODEL_TO_FORM_MAPPING = "modelToFormMapping";

		/**
		 * Whether the form is built in create mode.
		 * 
		 * <p>
		 * In create mode, the form is not filled with an existing model. Instead of forcing the
		 * component's model to be of {@link DeclarativeFormBuilder#getModelType() display model
		 * type}, the component's model must be of {@link #getCreateContextType()}.
		 * </p>
		 */
		@Name(CREATE)
		boolean isCreate();

		/**
		 * The required type of the component's model in {@link #isCreate() create mode}.
		 * 
		 * <p>
		 * Note, in edit mode, the required context type is always
		 * {@link DeclarativeFormBuilder#getModelType() display model type}, or <code>null</code>,
		 * if {@link #isContextOptional()}.
		 * </p>
		 * 
		 * <p>
		 * If not given, the context component's model has to be <code>null</code> during
		 * {@link #isCreate() create}.
		 * </p>
		 */
		@Name(CREATE_CONTEXT_TYPE)
		Class<?> getCreateContextType();

		/**
		 * Whether a form can be built with <code>null</code> as context model.
		 * 
		 * @see #getCreateContextType()
		 */
		@Name(CONTEXT_OPTIONAL)
		boolean isContextOptional();

		/** Property name of {@link #getCompactLayout()}. */
		String COMPACT_LAYOUT = "compactLayout";

		/**
		 * @see EditorFactory#isCompact()
		 */
		@Name(COMPACT_LAYOUT)
		boolean getCompactLayout();

		/**
		 * An optional {@link Function} used during
		 * {@link DeclarativeFormBuilder#createFormModel(Object) form model creation} to create the
		 * form model {@link ConfigurationItem} from the model of the component.
		 * 
		 * <p>
		 * If nothing is configured, the {@link DeclarativeFormBuilder#getFormType(Object) builder's
		 * form type} is instantiated directly.
		 * </p>
		 * 
		 * @see TLPropertyAccess
		 * @see DeclarativeFormBuilder#createFormModel(Object)
		 */
		@Name(MODEL_TO_FORM_MAPPING)
		PolymorphicConfiguration<Function<Object, ConfigurationItem>> getModelToFormMapping();

		/**
		 * Additional context object to be added to the {@link DeclarativeFormOptions} context
		 * passed to all dynamic elements of this form.
		 * 
		 * <p>
		 * The value is derived from the context component this builder is responsible for by
		 * resolving the configured {@link ModelSpec}.
		 * </p>
		 * 
		 * @implNote The resolved value can be accessed through the
		 *           {@link DeclarativeFormBuilder#CONTEXT} property from the
		 *           {@link DeclarativeFormOptions} passed to the constructors of active elements of
		 *           the form (e.g. option providers).
		 * 
		 * @implNote Predefined context information is available through the properties
		 *           {@link DeclarativeFormBuilder#FORM_MODEL} and
		 *           {@link DeclarativeFormBuilder#COMPONENT} without explicitly configuring a
		 *           context value.
		 */
		@Name("context")
		ModelSpec getContext();

	}

	private final Function<Object, ConfigurationItem> _modelToFormMapping;

	private final ChannelLinking _context;

	/**
	 * Creates a {@link DeclarativeFormBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DeclarativeFormBuilder(InstantiationContext context, Config config) {
		_config = config;
		_modelToFormMapping = context.getInstance(config.getModelToFormMapping());
		_context = context.getInstance(getConfig().getContext());
	}
	
	@Override
	public final Config getConfig() {
		return _config;
	}

	@Override
	public Object getModel(Object businessModel, LayoutComponent aComponent) {
		if (getConfig().getShowNoModel() && !modelSupported(businessModel, aComponent)) {
			return null;
		}
		C formModel = createFormModel(businessModel);
		initFormModel(formModel, businessModel);
		if (!getConfig().isCreate() && businessModel != null) {
			@SuppressWarnings("unchecked")
			T typedModel = (T) businessModel;
			fillFormModel(formModel, typedModel);
		}
		return createForm(aComponent, formModel);
	}

	/**
	 * Creates the {@link FormContext} for the given {@link LayoutComponent} based on the form model
	 * of {@link #getFormType(Object) form type}.
	 */
	protected FormContext createForm(LayoutComponent component, C formModel) {
		FormContext formContext = formContext(component);
		InitializerProvider initializerProvider = createInitializerProvider(component, formModel);
		boolean compactLayout = getConfig().getCompactLayout();
		EditorFactory editorFactory = new EditorFactory(initializerProvider);
		editorFactory.setCustomizations(new ConfiguredAnnotationCustomizations(getConfig()));
		editorFactory.setCompact(compactLayout);
		InitializerUtil.initAll(editorFactory.getInitializer(), formModel);
		editorFactory.initEditorGroup(formContext, formModel);
		return formContext;
	}

	/**
	 * Type of the business object being edited.
	 */
	protected abstract Class<? extends T> getModelType();

	/**
	 * {@link ConfigurationItem} type that serves as form model.
	 * 
	 * @param contextModel
	 *        The model of the context component.
	 */
	protected abstract Class<? extends C> getFormType(Object contextModel);

	/**
	 * Optional model-independent preparation of the given form model.
	 * 
	 * @param formModel
	 *        The {@link ConfigurationItem} edit model to set up with initial values.
	 * @param contextModel
	 *        The context model of the component.
	 */
	protected void initFormModel(C formModel, Object contextModel) {
		// Hook for sub-classes.
	}

	/**
	 * Fills the given form model with current values take form the given business object.
	 * 
	 * <p>
	 * This method is only called, if not in {@link Config#isCreate() create mode} and the component
	 * has a non-<code>null</code> model. See {@link #initFormModel(ConfigurationItem, Object)} for
	 * model-independent preparation.
	 * </p>
	 * 
	 * @param formModel
	 *        The {@link ConfigurationItem} edit model to fill with values.
	 * @param businessModel
	 *        The component's model to take values to edit from.
	 * 
	 * @see #initFormModel(ConfigurationItem, Object) Preparing the edit model independently of the
	 *      existence of a business model.
	 */
	protected abstract void fillFormModel(C formModel, T businessModel);

	/**
	 * Creates the {@link ConfigurationItem} that describes this form.
	 * 
	 * @param contextModel
	 *        The current model of the context component.
	 */
	protected C createFormModel(Object contextModel) {
		if (_modelToFormMapping == null) {
			return newConfigItem(getFormType(contextModel));
		}
		return mapModelToForm(contextModel);
	}

	private C mapModelToForm(Object contextModel) {
		ConfigurationItem formModel = _modelToFormMapping.apply(contextModel);
		return getFormType(contextModel).cast(formModel);
	}

	/**
	 * @deprecated Override {@link #createInitializerProvider(LayoutComponent, ConfigurationItem)}
	 */
	@Deprecated
	protected InitializerProvider createInitializerProvider() {
		return new InitializerIndex();
	}

	/**
	 * Create the collection of {@link Initializer}s that react on property updates.
	 * 
	 * @param component
	 *        The context component.
	 * @param formModel
	 *        The {@link ConfigurationItem} model being edited.
	 * 
	 * @return The initializers to apply.
	 * 
	 * @see #buildInitializerIndex(InitializerProvider, LayoutComponent, ConfigurationItem)
	 */
	protected InitializerProvider createInitializerProvider(LayoutComponent component, C formModel) {
		InitializerProvider result = createInitializerProvider();
		buildInitializerIndex(result, component, formModel);
		return result;
	}

	/**
	 * @param index 
	 *        The index of all initializers to build.
	 * @deprecated Override
	 *             {@link #buildInitializerIndex(InitializerProvider, LayoutComponent, ConfigurationItem)}
	 */
	@Deprecated
	protected void buildInitializerIndex(InitializerIndex index) {
		// Legacy hook for subclasses.
	}

	/**
	 * Allows to add own dynamic initializers.
	 * 
	 * @param index
	 *        The index of all initializers to build.
	 * @param component
	 *        The context component.
	 * @param formModel
	 *        The {@link ConfigurationItem} model being edited.
	 * 
	 * @see #createInitializerProvider(LayoutComponent, ConfigurationItem)
	 */
	protected void buildInitializerIndex(InitializerProvider index, LayoutComponent component, C formModel) {
		index.set(FORM_MODEL, formModel);
		index.set(COMPONENT, component);

		if (_context != null) {
			index.set(CONTEXT, (SchemaConfiguration) ChannelLinking.eval(component, _context));
		}

		if (index instanceof InitializerIndex) {
			buildInitializerIndex((InitializerIndex) index);
		}
	}

	@Override
	public final boolean supportsModel(Object model, LayoutComponent component) {
		return getConfig().getShowNoModel() || modelSupported(model, component);
	}

	/**
	 * Whether the given model object is supported by this builder for creating a form.
	 * 
	 * <p>
	 * In contrast to {@link #supportsModel(Object, LayoutComponent)}, the component may even be
	 * displayed, if <code>false</code> is returned, if the option {@link Config#getShowNoModel()}
	 * is enabled.
	 * </p>
	 * 
	 * @param model
	 *        See {@link #supportsModel(Object, LayoutComponent)}.
	 * @param component
	 *        See {@link #supportsModel(Object, LayoutComponent)}.
	 */
	protected boolean modelSupported(Object model, LayoutComponent component) {
		Config config = getConfig();
		if (config.isContextOptional()) {
			return true;
		}
		if (config.isCreate()) {
			Class<?> contextType = config.getCreateContextType();
			if (contextType == null) {
				return model == null;
			} else {
				return contextType.isInstance(model);
			}
		} else {
			return getModelType().isInstance(model);
		}
	}

}
