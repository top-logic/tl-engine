/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit;

import static com.top_logic.basic.util.Utils.*;
import static com.top_logic.layout.form.template.model.Templates.*;
import static com.top_logic.layout.form.values.Fields.*;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Logger;
import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.ConcatenatedIterable;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamePath;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.ReadOnly;
import com.top_logic.basic.config.annotation.ReadOnly.ReadOnlyMode;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.constraint.ConstraintAnnotation;
import com.top_logic.basic.config.constraint.ConstraintFactory;
import com.top_logic.basic.config.constraint.ConstraintSpec;
import com.top_logic.basic.config.constraint.algorithm.ConstraintAlgorithm;
import com.top_logic.basic.config.constraint.annotation.OverrideConstraints;
import com.top_logic.basic.config.customization.AnnotationCustomizations;
import com.top_logic.basic.config.customization.ConfiguredAnnotationCustomizations;
import com.top_logic.basic.config.customization.CustomizationContainer;
import com.top_logic.basic.config.customization.NoCustomizations;
import com.top_logic.basic.config.misc.PropertyValue;
import com.top_logic.basic.config.misc.PropertyValueImpl;
import com.top_logic.basic.config.order.DefaultOrderStrategy;
import com.top_logic.basic.config.order.OrderStrategy;
import com.top_logic.basic.func.GenericFunction;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.AbstractFormContainer;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FieldMode;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.template.model.AbstractMember;
import com.top_logic.layout.form.template.model.Templates;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.form.values.Fields;
import com.top_logic.layout.form.values.Value;
import com.top_logic.layout.form.values.Values;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.CssClass;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;
import com.top_logic.layout.form.values.edit.annotation.PropertyEditor;
import com.top_logic.layout.form.values.edit.annotation.RenderWholeLine;
import com.top_logic.layout.form.values.edit.annotation.UseBuilder;
import com.top_logic.layout.form.values.edit.annotation.UseTemplate;
import com.top_logic.layout.form.values.edit.editor.ComplexEditor;
import com.top_logic.layout.form.values.edit.editor.Editor;
import com.top_logic.layout.form.values.edit.editor.ItemEditor;
import com.top_logic.layout.form.values.edit.editor.ListEditor;
import com.top_logic.layout.form.values.edit.editor.ValueDisplay;
import com.top_logic.layout.form.values.edit.editor.ValueEditor;
import com.top_logic.layout.form.values.edit.initializer.DescendingInitializer;
import com.top_logic.layout.form.values.edit.initializer.DispatchingInitializer;
import com.top_logic.layout.form.values.edit.initializer.Initializer;
import com.top_logic.layout.form.values.edit.initializer.InitializerProvider;
import com.top_logic.layout.form.values.edit.initializer.InitializerUtil;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.resources.AbstractResourceView;
import com.top_logic.util.error.TopLogicException;

/**
 * Factory for providing generic editors for {@link ConfigurationItem}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class EditorFactory implements AnnotationCustomizations {

	private static final Property<Object> MODEL = TypedAnnotatable.property(Object.class, "model");

	private static final Property<ValueModel> VALUE_MODEL = TypedAnnotatable.property(ValueModel.class, "valueModel");

	/**
	 * CSS class to mark content written by the declarative form definitions.
	 * <p>
	 * Is written on the outermost HTML tag.
	 * </p>
	 */
	public static final String CSS_CLASS_DECLARATIVE_FORM_DEFINITION = "dfContent";

	/**
	 * CSS class for the {@link #isCompact()} mode.
	 * <p>
	 * Is written on the outermost HTML tag.
	 * </p>
	 * 
	 * @see #CSS_CLASS_MODE_FULL
	 */
	public static final String CSS_CLASS_MODE_COMPACT = "dfModeCompact";

	/**
	 * CSS class for the non-{@link #isCompact()} mode.
	 * <p>
	 * Is written on the outermost HTML tag.
	 * </p>
	 * 
	 * @see #CSS_CLASS_MODE_COMPACT
	 */
	public static final String CSS_CLASS_MODE_FULL = "dfModeFull";

	/**
	 * The {@link TypedConfiguration} of the {@link EditorFactory}.
	 */
	public interface Config extends CustomizationContainer {

		/** Property name of {@link #getDisplayFieldsAsText()}. */
		String DISPLAY_FIELDS_AS_TEXT = "display-fields-as-text";

		/**
		 * Whether {@link FormField}s should be displayed as text and get activated by clicking on
		 * them.
		 * <p>
		 * If not, they are displayed how fields are normally displayed in the application.
		 * </p>
		 */
		@Name(DISPLAY_FIELDS_AS_TEXT)
		boolean getDisplayFieldsAsText();

	}

	private final Config _config;

	final DescendingInitializer _initializer;

	private final LabelProvider _groupLabels = new ConfigLabelProvider("@option");

	private boolean _compact;

	private final Binding _binding = new Binding();

	private int _depth;

	/**
	 * The identifier of the special property
	 * {@link PolymorphicConfiguration#IMPLEMENTATION_CLASS_NAME}.
	 */
	private static final NamedConstant IMPLEMENTATION_CLASS_ID =
		TypedConfiguration.getConfigurationDescriptor(PolymorphicConfiguration.class).getProperty(
			PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME).identifier();

	private AnnotationCustomizations _customizations = NoCustomizations.INSTANCE;

	private final AnnotationCustomizations _defaultCustomizations;

	private final OrderStrategy _orderStrategy;

	/**
	 * Creates a {@link EditorFactory}.
	 * 
	 * @param initializerProvider
	 *        See {@link #getInitializerProvider()}.
	 */
	public EditorFactory(InitializerProvider initializerProvider) {
		_config = ApplicationConfig.getInstance().getConfig(Config.class);
		_defaultCustomizations = new ConfiguredAnnotationCustomizations(_config);
		_initializer = wrapInitializers(initializerProvider);
		_orderStrategy = createOrderStrategy();
	}

	protected OrderStrategy createOrderStrategy() {
		return new DefaultOrderStrategy(this);
	}

	/**
	 * Sets additional {@link AnnotationCustomizations customizations} for resolving annotations.
	 */
	public void setCustomizations(AnnotationCustomizations customizations) {
		_customizations = Objects.requireNonNull(customizations);
	}

	/**
	 * Whether to build forms without surrounding collections with fieldsets.
	 */
	public boolean isCompact() {
		return _compact;
	}

	/**
	 * @see #isCompact()
	 */
	public void setCompact(boolean compact) {
		_compact = compact;
	}

	/**
	 * {@link LabelProvider} for {@link ConfigurationItem}s providing labels for groups that display
	 * otherwise anonymous configuration item values.
	 */
	public LabelProvider getGroupLabels() {
		return _groupLabels;
	}

	/**
	 * {@link LabelProvider} for the options of the given property.
	 */
	public LabelProvider getOptionLabels(PropertyDescriptor property) {
		LabelProvider optionLabels = Fields.optionLabelsOrNull(formOptions(property));
		if (optionLabels != null) {
			return optionLabels;
		}
		if (property.kind() == PropertyKind.PLAIN) {
			return MetaLabelProvider.INSTANCE;
		}
		if (property.kind() == PropertyKind.COMPLEX) {
			return MetaLabelProvider.INSTANCE;
		}
		return getGroupLabels();
	}

	/**
	 * Creates {@link DeclarativeFormOptions} for the given {@link PropertyDescriptor}.
	 */
	public DeclarativeFormOptions formOptions(PropertyDescriptor contextProperty) {
		return new DeclarativeFormOptions() {
			@Override
			public PropertyDescriptor getProperty() {
				return contextProperty;
			}

			@Override
			public <T> T get(Property<T> property) {
				return _initializer.get(property);
			}

			@Override
			public boolean isSet(Property<?> property) {
				return _initializer.isSet(property);
			}

			@Override
			public <T> T set(Property<T> property, T value) {
				throw errorUnmodifiable();
			}

			@Override
			public <T> T reset(Property<T> property) {
				throw errorUnmodifiable();
			}

			private RuntimeException errorUnmodifiable() {
				throw new UnsupportedOperationException("Global settings must not be modified.");
			}

			@Override
			public AnnotationCustomizations getCustomizations() {
				return EditorFactory.this;
			}
		};
	}

	/**
	 * Builds an editor for the given {@link ConfigurationItem} model within the given
	 * {@link FormContainer} in normal (non-compact) mode.
	 * 
	 * 
	 * @see #initEditorGroup(FormContainer, ConfigurationItem, InitializerProvider, boolean)
	 */
	public static void initEditorGroup(FormContainer container, ConfigurationItem model,
			InitializerProvider initializerProvider) {
		initEditorGroup(container, model, initializerProvider, false);
	}

	/**
	 * Build an editor for the given {@link ConfigurationItem} model within the given
	 * {@link FormContainer}.
	 * 
	 * @param container
	 *        The {@link FormContainer} to fill with editor inputs.
	 * @param model
	 *        The {@link ConfigurationItem} model to bind to the editor inputs.
	 * @param initializerProvider
	 *        Custom {@link InitializerProvider}s for certain types and properties in the edit
	 *        model. Is not allowed to be null.
	 * @param compact
	 *        See: {@link EditorFactory#isCompact()}
	 */
	public static void initEditorGroup(FormContainer container, ConfigurationItem model,
			InitializerProvider initializerProvider, boolean compact) {

		EditorFactory editorFactory = new EditorFactory(initializerProvider);
		editorFactory.setCompact(compact);
		InitializerUtil.initAll(editorFactory.getInitializer(), model);
		editorFactory.initEditorGroup(container, model);
	}

	private DescendingInitializer wrapInitializers(InitializerProvider initializerProvider) {
		/* This is necessary to resolve the correct initializer for default values of new values. */
		DispatchingInitializer dispatchingInitializer = new DispatchingInitializer(initializerProvider) {
			@Override
			public void init(ConfigurationItem model, PropertyDescriptor property, Object value) {
				if (value instanceof ConfigurationItem) {
					InitializerUtil.initProperties(EditorFactory.this, model);
				}

				super.init(model, property, value);
			}
		};
		/* This is necessary to visit default values of new values, too. */
		DescendingInitializer result = new DescendingInitializer(dispatchingInitializer, initializerProvider);
		return result;
	}

	/**
	 * The {@link Initializer} for the {@link ConfigurationItem}s.
	 */
	public Initializer getInitializer() {
		return _initializer;
	}

	/**
	 * Custom {@link Initializer}s for certain types and properties in the edit model.
	 */
	public InitializerProvider getInitializerProvider() {
		return _initializer.getInitializer().getInitializerProvider();
	}

	/**
	 * Custom properties for creating the form.
	 */
	public TypedAnnotatable getSettings() {
		return _initializer;
	}

	/**
	 * Builds an editor for the given {@link ConfigurationItem} model.
	 * 
	 * @param container
	 *        The {@link FormContainer} to create the input elements in.
	 * @param model
	 *        The model object to bind the input fields to.
	 * 
	 * @see #initEditorGroup(FormContainer, ConfigurationItem, boolean, PropertyDescriptor, Set,
	 *      Set)
	 */
	public void initEditorGroup(FormContainer container, ConfigurationItem model) {
		initEditorGroup(container, model, false, null, Collections.emptySet(), Collections.emptySet());
	}

	/**
	 * Builds an editor for the given {@link ConfigurationItem} model.
	 * 
	 * @param container
	 *        The {@link FormContainer} to create the input elements in.
	 * @param model
	 *        The model object to bind the input fields to.
	 * @param monomorphicContext
	 *        Whether the special property
	 *        {@link PolymorphicConfiguration#IMPLEMENTATION_CLASS_NAME} should be skipped.
	 * @param keyProperty
	 *        The (optional) property that is used for indexing in the context. A key property is
	 *        excluded from editing, because editing that property would corrupt the index structure
	 *        in the context.
	 * @param hiddenProperties
	 *        Properties for which fields maybe created (depending on the model), but that must not
	 *        be displayed, e.g. because they are displayed explicit by caller.
	 * @param enforceFields
	 *        Properties for which fields are created (depending on the model), also when they are
	 *        not configured to be displayed, e.g. because they are displayed explicit by caller.
	 */
	public void initEditorGroup(FormContainer container, ConfigurationItem model, boolean monomorphicContext,
			PropertyDescriptor keyProperty, Set<PropertyDescriptor> hiddenProperties,
			Set<PropertyDescriptor> enforceFields) {
		_depth++;

		setModel(container, model);

		ConfigurationDescriptor descriptor = model.descriptor();
		((AbstractFormContainer) container).setResources(new PropertyBasedResources(descriptor));
		
		NamedConstant keyIdentifier = keyProperty == null ? null : keyProperty.identifier();
		Set<NamedConstant> hiddenPropertiesIds =
			hiddenProperties.stream().map(PropertyDescriptor::identifier).collect(Collectors.toSet());

		List<FormMember> members = new ArrayList<>();
		for (PropertyDescriptor property : getDisplayProperties(descriptor, enforceFields)) {
			FormMember field = createMember(container, model, property);

			appendDeclarativeDefinitionCssClasses(field);

			NamedConstant identifier = property.identifier();
			if (monomorphicContext && identifier == IMPLEMENTATION_CLASS_ID) {
				field.setVisible(false);
			} else if (identifier == keyIdentifier) {
				field.setImmutable(true);
			} else {
				Value<FieldMode> visible = fieldMode(property, model);
				bindMode(field, visible, property, model);
			}

			CssClass cssClass = getAnnotation(property, CssClass.class);
			if (cssClass != null) {
				field.setCssClasses(cssClass.value());
			}

			inspectConstraintAnnotations(model, property);

			_binding.enterField(model, property, field);

			if (hiddenPropertiesIds.contains(property.identifier())) {
				// Do not display member for property.
				continue;
			}
			members.add(field);
		}
	
		initAnnotatedTemplate(container, model, members);

		_depth--;
		if (_depth == 0) {
			processConstraintAnnotations();
		}
	}

	private FormMember createMember(FormContainer container, ConfigurationItem model, PropertyDescriptor property) {
		ValueModel setting = valueModel(model, property);

		Class<? extends Editor> annotatedEditor = annotatedEditor(property);
		FormMember member;
		if (annotatedEditor != null) {
			member = createMember(annotatedEditor, container, setting);
			applyRenderWholeLine(member, property, false);
		} else {
			annotatedEditor = annotatedEditor(property.getType());
			if (annotatedEditor != null) {
				member = createMember(annotatedEditor, container, setting);
			} else {
				switch (property.kind()) {
					case COMPLEX:
						if (hasBinding(property)) {
							member = complexEditor().createUI(this, container, setting);
						} else {
							member = valueEditor().createUI(this, container, setting);
						}
						break;
					case PLAIN:
					case REF:
						member = valueEditor().createUI(this, container, setting);
						break;
					case ITEM:
						if (Fields.itemDisplay(this, property) == ItemDisplay.ItemDisplayType.VALUE) {
							member = valueEditor().createUI(this, container, setting);
						} else {
							member = itemEditor().createUI(this, container, setting);
						}
						break;
					case ARRAY:
					case LIST:
					case MAP:
						if (Fields.itemDisplay(this, property) == ItemDisplay.ItemDisplayType.VALUE) {
							member = valueEditor().createUI(this, container, setting);
						} else {
							member = listEditor().createUI(this, container, setting);
						}
						break;
					case DERIVED: {
						member = valueDisplay().createUI(this, container, setting);
						break;
					}
					default:
						throw new UnreachableAssertion("No such property kind: " + property.kind());
				}
			}
			applyRenderWholeLine(member, property, true);
		}

		member.set(VALUE_MODEL, setting);
		return member;
	}

	private Class<? extends Editor> annotatedEditor(Class<?> type) {
		PropertyEditor annotation = getAnnotation(type, PropertyEditor.class);
		if (annotation != null) {
			return annotation.value();
		}
		return null;
	}

	private Class<? extends Editor> annotatedEditor(PropertyDescriptor property) {
		PropertyEditor annotation = getAnnotation(property, PropertyEditor.class);
		if (annotation != null) {
			return annotation.value();
		}
		return null;
	}

	private void applyRenderWholeLine(FormMember field, PropertyDescriptor property, boolean considerValueType) {
		RenderWholeLine propertyAnnotation = getAnnotation(property, RenderWholeLine.class);
		if (propertyAnnotation != null) {
			field.set(AbstractMember.RENDER_WHOLE_LINE, propertyAnnotation.value());
			return;
		}
		if (!considerValueType) {
			return;
		}
		ConfigurationDescriptor valueDescriptor = property.getValueDescriptor();
		if (valueDescriptor == null) {
			return;
		}
		RenderWholeLine descriptorAnnotation = getAnnotation(valueDescriptor, RenderWholeLine.class);
		if (descriptorAnnotation != null) {
			field.set(AbstractMember.RENDER_WHOLE_LINE, descriptorAnnotation.value());
		}
	}

	private Editor complexEditor() {
		return ComplexEditor.INSTANCE;
	}

	/**
	 * Installs the control provider annotated by the {@link ControlProvider} annotation.
	 * 
	 * @param property
	 *        The {@link PropertyDescriptor} for which the given field was created.
	 * @param field
	 *        The field to set the control provider,
	 *        {@link FormMember#setControlProvider(com.top_logic.layout.form.template.ControlProvider)}
	 * @return Whether a {@link ControlProvider} annotation was found.
	 */
	public boolean processControlProviderAnnotation(PropertyDescriptor property, FormMember field) {
		ControlProvider controlProvider = getAnnotation(property, ControlProvider.class);
		if (controlProvider != null) {
			installAnnotatedControlProvider(field, property, controlProvider);
			return true;
		}
		return false;
	}

	private FormMember createMember(Class<? extends Editor> editorClass, FormContainer container, ValueModel setting) {
		try {
			return ConfigUtil.getInstance(editorClass).createUI(this, container, setting);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
	}

	/**
	 * Looks up the (top-level) {@link FormMember} representing the given property of the given
	 * item.
	 */
	public FormMember getMember(ConfigurationItem model, PropertyDescriptor property) {
		return _binding.getMember(model, property);
	}

	/**
	 * Wraps the given property of the given {@link ConfigurationItem} to a {@link ValueModel}
	 * encapsulating access to this property of the given item.
	 */
	public ValueModel valueModel(ConfigurationItem model, PropertyDescriptor property) {
		PropertyValue propertyValue = new PropertyValueImpl(model, property);
		return new Setting(this, propertyValue, _initializer);
	}

	/**
	 * Installs the control provider annotated by the {@link ControlProvider} annotation.
	 * 
	 * @param field
	 *        The field to set the control provider,
	 *        {@link FormMember#setControlProvider(com.top_logic.layout.form.template.ControlProvider)}
	 * @param property
	 *        The {@link PropertyDescriptor} for which the given field was created.
	 * @param controlProvider
	 *        The annotation to instantiate.
	 */
	public static void installAnnotatedControlProvider(FormMember field, PropertyDescriptor property,
			ControlProvider controlProvider) {
		try {
			field.setControlProvider(ConfigUtil.getInstance(controlProvider.value()));
		} catch (ConfigurationException ex) {
			throw new ConfigurationError("Control provider annotation for '" + property + "' is invalid.", ex);
		}
	}

	private void inspectConstraintAnnotations(ConfigurationItem model, PropertyDescriptor property) {
		boolean descend = true;
		for (Annotation annotation : property.getLocalAnnotations()) {
			Class<? extends Annotation> annotationType = annotation.annotationType();
			if (annotationType == OverrideConstraints.class) {
				descend = false;
				continue;
			}
			processConstraintAnnotation(model, property, annotation);
		}

		if (descend) {
			for (ConfigurationDescriptor superDescriptor : property.getDescriptor().getSuperDescriptors()) {
				PropertyDescriptor superProperty = superDescriptor.getProperty(property.getPropertyName());
				if (superProperty != null) {
					inspectConstraintAnnotations(model, superProperty);
				}
			}
		}
	}

	private void processConstraintAnnotation(ConfigurationItem model, PropertyDescriptor property, Annotation annotation) {
		ConstraintAnnotation metaAnnotation = annotation.annotationType().getAnnotation(ConstraintAnnotation.class);
		if (metaAnnotation == null) {
			return;
		}

		List<ConstraintSpec> specs = factory(property, annotation, metaAnnotation);
		scheduleConstraintCreation(model, property, specs);
	}

	private List<ConstraintSpec> factory(PropertyDescriptor property, Annotation annotation,
			ConstraintAnnotation metaAnnotation) {
		try {
			@SuppressWarnings("unchecked")
			ConstraintFactory<Annotation> factory = ConfigUtil.getInstance(metaAnnotation.value());
			return factory.createConstraint(annotation);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError("Cannot instantiate constraint factory for '" + property + "'.", ex);
		}
	}

	private void scheduleConstraintCreation(ConfigurationItem model, PropertyDescriptor property,
			List<ConstraintSpec> specs) {
		_binding.scheduleConstraintCreation(model, property, specs);
	}

	private void processConstraintAnnotations() {
		_binding.processConstraintAnnotations();
	}
		
	private Iterable<PropertyDescriptor> getDisplayProperties(ConfigurationDescriptor descriptor,
			Set<PropertyDescriptor> additionalProperties) {
		List<PropertyDescriptor> displayProperties = orderStrategy().getDisplayProperties(descriptor);
		if (additionalProperties.isEmpty()) {
			return displayProperties;
		}
		HashSet<PropertyDescriptor> descriptorLocalAdditionalProperties = additionalProperties.stream()
				.map(prop -> descriptor.getProperty(prop.getPropertyName()))
				.filter(Objects::nonNull)
				.collect(Collectors.toCollection(HashSet::new));
		// remove all properties which are displayed by default.
		descriptorLocalAdditionalProperties.removeAll(displayProperties);
		if (descriptorLocalAdditionalProperties.isEmpty()) {
			return displayProperties;
		}
		return ConcatenatedIterable.concat(displayProperties, descriptorLocalAdditionalProperties);
	}

	/**
	 * The {@link Editor} to create the UI for a {@link PropertyKind#ITEM} property.
	 */
	public Editor itemEditor() {
		return ItemEditor.INSTANCE;
	}

	/**
	 * The {@link Editor} to create the UI for a {@link PropertyKind#LIST} or
	 * {@link PropertyKind#ARRAY} or {@link PropertyKind#MAP} property.
	 */
	public Editor listEditor() {
		return ListEditor.INSTANCE;
	}

	/**
	 * The {@link Editor} to create the UI for a {@link PropertyKind#PLAIN} property.
	 */
	public Editor valueEditor() {
		return ValueEditor.INSTANCE;
	}
	
	/**
	 * The {@link Editor} to create the UI for a read-only property.
	 */
	public Editor valueDisplay() {
		return ValueDisplay.INSTANCE;
	}

	/**
	 * The {@link OrderStrategy} for ordering defined properties for generic display (if no
	 * template with explicit display order has been annotated).
	 */
	public OrderStrategy orderStrategy() {
		return _orderStrategy;
	}

	/**
	 * The {@link InstantiationContext} to be used for instantiating values.
	 */
	public InstantiationContext getInstantiationContext() {
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
	}

	static class Setting implements ValueModel {

		private final PropertyValue _propertyValue;

		private final Initializer _initializer;

		private final EditorFactory _factory;
	
		Setting(EditorFactory factory, PropertyValue propertyValue, Initializer initializer) {
			_factory = factory;
			_propertyValue = propertyValue;
			_initializer = initializer;
		}

		@Override
		public PropertyValue getPropertyValue() {
			return _propertyValue;
		}

		@Override
		public ConfigurationItem getModel() {
			return _propertyValue.getItem();
		}
	
		@Override
		public PropertyDescriptor getProperty() {
			return _propertyValue.getProperty();
		}
	
		@Override
		public Object getValue() {
			return NamePath.value(_propertyValue.getItem(), _propertyValue.getProperty());
		}
	
		@Override
		public void setValue(Object value) {
			_propertyValue.set(value);

			ConfigurationItem model = _propertyValue.getItem();
			PropertyDescriptor property = _propertyValue.getProperty();
			Collection<ConfigurationItem> childConfigs = ConfigUtil.getChildConfigs(model, property);
			for (ConfigurationItem childConfig : childConfigs) {
				_initializer.init(model, property, childConfig);
			}
		}
	
		@Override
		public boolean addValue(Object value) {
			PropertyDescriptor property = _propertyValue.getProperty();
			Mapping<Object, Object> keyMapping = property.getKeyMapping();
			if (property.kind() == PropertyKind.MAP) {
				Object key = keyMapping.map(value);
				Map<Object, Object> mapValue = mapValue();
				if (mapValue == null) {
					_propertyValue.set(Collections.singletonMap(key, value));
				} else {
					if (mapValue.containsKey(key)) {
						errorKeyViolation(property.getKeyProperty(), key);
						return false;
					}
					mapValue.put(key, value);
				}
			} else {
				Collection<Object> collectionValue = collectionValue();
				if (collectionValue == null) {
					_propertyValue.set(Collections.singletonList(value));
				} else {
					if (keyMapping != null) {
						Object key = keyMapping.map(value);
						boolean containsKey = collectionValue.stream()
							.map(keyMapping)
							.anyMatch(key::equals);
						if (containsKey) {
							errorKeyViolation(property.getKeyProperty(), key);
							return false;
						}
					}
					collectionValue.add(value);
				}
			}

			_initializer.init(_propertyValue.getItem(), property, value);
			return true;
		}

		private void errorKeyViolation(PropertyDescriptor property, Object key) {
			String option = _factory.getOptionLabels(property).getLabel(key);
			throw new TopLogicException(
				I18NConstants.SELECTED_OPTION_ALREADY_ADDED__PROPERTY_OPTION.fill(Labels.propertyLabelKey(property),
					option));
		}

		@Override
		public void removeValue(Object value) {
			PropertyDescriptor property = _propertyValue.getProperty();
			if (property.kind() == PropertyKind.MAP) {
				Object key = property.getKeyMapping().map(value);
				mapValue().remove(key);
			} else {
				collectionValue().remove(value);
			}
		}

		private Collection<Object> collectionValue() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Collection<Object> collectionValue = (Collection) getValue();
			return collectionValue;
		}

		private Map<Object, Object> mapValue() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Map<Object, Object> mapValue = (Map) getValue();
			return mapValue;
		}

		@Override
		public void clearValue() {
			Object value = getValue();
			if (value instanceof Map<?, ?>) {
				((Map<?, ?>) value).clear();
			} else {
				((Collection<?>) value).clear();
			}
		}

	}

	/**
	 * Applies the annotated {@link HTMLTemplateFragment} to the given {@link FormMember}.
	 * 
	 * @param members
	 *        The {@link FormMember}s to be displayed in display order.
	 */
	private void initAnnotatedTemplate(FormContainer container, ConfigurationItem model,
			List<FormMember> members) {
		ConfigurationDescriptor descriptor = model.descriptor();
		
		UseBuilder builderAnnotation = getAnnotation(descriptor, UseBuilder.class);
		if (builderAnnotation != null) {
			for (Class<? extends FormBuilder<?>> builderClass : builderAnnotation.value()) {
				try {
					// No type safety possible.
					@SuppressWarnings({ "unchecked", "rawtypes" })
					FormBuilder<ConfigurationItem> builder = (FormBuilder) ConfigUtil.getInstance(builderClass);
					builder.initForm(container, model);
				} catch (ConfigurationException ex) {
					throw new ConfigurationError("Template provider resolution failed.", ex);
				}
			}
		}
		
		UseTemplate templateAnnotation = getAnnotation(descriptor, UseTemplate.class);
		if (templateAnnotation != null) {
			TemplateProvider provider;
			try {
				provider = ConfigUtil.getInstance(templateAnnotation.value());
			} catch (ConfigurationException ex) {
				throw new ConfigurationError("Template provider resolution failed.", ex);
			}
			Templates.template(container, provider.get(model));
		} else {
			Templates.template(container, defaultItemTemplate(model, members));
		}
	}

	/**
	 * @param model
	 *        The item to be displayed.
	 * @param members
	 *        The created {@link FormMember} in their creation order.
	 */
	private HTMLTemplateFragment defaultItemTemplate(ConfigurationItem model,
			List<FormMember> members) {
		return div(css("rf_columnsLayout cols3"), verticalBox(templates(members)));
	}

	private HTMLTemplateFragment[] templates(List<FormMember> members) {
		HTMLTemplateFragment[] lines = new HTMLTemplateFragment[members.size()];
		for (int n = 0, cnt = members.size(); n < cnt; n++) {
			FormMember member = members.get(n);
			HTMLTemplateFragment template = template(member);
			lines[n] = template;
		}
		return lines;
	}

	private HTMLTemplateFragment template(FormMember member) {
		HTMLTemplateFragment template;
		HTMLTemplateFragment annotatedTemplate = member.get(Editor.MEMBER_TEMPLATE);
		if (annotatedTemplate != null) {
			template = annotatedTemplate;
		} else {
			PropertyDescriptor property = member.get(VALUE_MODEL).getProperty();
			if (displayAsBox(property)) {
				template = contentBox(div(member(member.getName())));
			} else {
				Class<?> type = property.getType();
				if (type == Boolean.class || type == boolean.class || member instanceof BooleanField) {
					template = fieldBoxInputFirst(member.getName());
				} else {
					template = fieldBox(member.getName());
				}
			}
		}
		return template;
	}

	private boolean displayAsBox(PropertyDescriptor property) {
		ItemDisplayType explicitItemDisplay = Fields.itemDisplayOrNull(this, property);
		if (explicitItemDisplay != null) {
			return explicitItemDisplay != ItemDisplayType.VALUE;
		}

		if (hasBinding(property)) {
			return true;
		}

		PropertyKind kind = property.kind();
		return kind == PropertyKind.LIST || kind == PropertyKind.MAP || kind == PropertyKind.ITEM;
	}

	private boolean hasBinding(PropertyDescriptor property) {
		return this.getAnnotation(property, MapBinding.class) != null
			|| this.getAnnotation(property, ListBinding.class) != null;
	}

	private static final class PropertyBasedResources extends AbstractResourceView {

		private final ConfigurationDescriptor _descriptor;
	
		public PropertyBasedResources(ConfigurationDescriptor descriptor) {
			_descriptor = descriptor;
		}
	
		@Override
		protected String getResource(String resourceKey, boolean optional) {
			int sepIndex = resourceKey.indexOf('.');
			String baseKey;
			if (sepIndex >= 0) {
				baseKey = resourceKey.substring(0, sepIndex);
			} else {
				baseKey = resourceKey;
			}
	
			PropertyDescriptor property = _descriptor.getProperty(baseKey);
			if (property == null) {
				property = searchPropertyByNormalizedName(baseKey);
				if (property == null) {
					if (!optional) {
						Logger.error("No such property '" + baseKey + "' in '"
							+ _descriptor.getConfigurationInterface().getName() + "'.", PropertyBasedResources.class);
					}
					return optional ? null : "[" + resourceKey + "]";
				}
			}
	
			String propertyLabel;
			if (sepIndex >= 0) {
				String keySuffix = resourceKey.substring(sepIndex);
				propertyLabel = Labels.propertyLabel(property, keySuffix, true);
			} else {
				propertyLabel = Labels.propertyLabel(property, true);
			}

			if (!optional && propertyLabel == null) {
				return enhancePropertyNameFormat(property.getPropertyName());
			}
			return propertyLabel;
		}

		private PropertyDescriptor searchPropertyByNormalizedName(String searchedName) {
			for (PropertyDescriptor property : _descriptor.getProperties()) {
				if (normalizeFieldName(property.getPropertyName()).equals(searchedName)) {
					return property;
				}
			}
			return null;
		}

		private String enhancePropertyNameFormat(String name) {
			String nameWithoutDash = name.replace('-', ' ');

			return nameWithoutDash.substring(0, 1).toUpperCase() + nameWithoutDash.substring(1);
		}
	}

	/**
	 * The model object, the given {@link FormContainer} was initialized with, see
	 * {@link EditorFactory#initEditorGroup(FormContainer, ConfigurationItem)}.
	 */
	public static Object getModel(FormMember container) {
		return container.get(MODEL);
	}

	/**
	 * The model object, the {@link FormContext} of the given {@link FormHandler} was created for,
	 * see {@link EditorFactory#initEditorGroup(FormContainer, ConfigurationItem)}.
	 */
	public static Object getModel(FormHandler form) {
		return getModel(form.getFormContext());
	}

	/** Setter for {@link #getModel(FormMember)}. */
	public static void setModel(FormMember container, Object model) {
		container.set(MODEL, model);
	}

	/**
	 * The {@link ValueModel} that represents access to the property represented by the given form
	 * member.
	 */
	public static ValueModel getValueModel(FormMember member) {
		return member.get(VALUE_MODEL);
	}

	/**
	 * Book-keeping of mapping between properties and fields and constraints that must be added to
	 * fields.
	 */
	public static class Binding {

		private final Map<ConfigurationItem, Map<NamedConstant, FormMember>> _members =
			new HashMap<>();

		private final List<ConstraintAnnotation> _constraintAnnotations = new ArrayList<>();

		public Binding() {
			super();
		}

		public void enterField(ConfigurationItem model, PropertyDescriptor property, FormMember member) {
			Map<NamedConstant, FormMember> properties = _members.get(model);
			if (properties == null) {
				properties = new HashMap<>();
				_members.put(model, properties);
			}
			properties.put(property.identifier(), member);
		}

		public void scheduleConstraintCreation(ConfigurationItem model, PropertyDescriptor property,
				List<ConstraintSpec> specs) {
			_constraintAnnotations.add(new ConstraintAnnotation(model, property, specs));
		}

		public void processConstraintAnnotations() {
			for (ConstraintAnnotation annotation : _constraintAnnotations) {
				annotation.process();
			}
			_constraintAnnotations.clear();
		}

		public FormMember getMember(ConfigurationItem model, PropertyDescriptor property) {
			Map<NamedConstant, FormMember> properties = _members.get(model);
			if (properties == null) {
				return null;
			}
			return properties.get(property.identifier());
		}

		/**
		 * Information required to create a constraint-based annotation after the complete model has
		 * been translated into fields.
		 */
		public class ConstraintAnnotation {

			public final ConfigurationItem _model;

			public final PropertyDescriptor _property;

			private final List<ConstraintSpec> _specs;

			public ConstraintAnnotation(ConfigurationItem model, PropertyDescriptor property,
					List<ConstraintSpec> specs) {
				_model = model;
				_property = property;
				_specs = specs;
			}

			public void process() {
				for (ConstraintSpec spec : _specs) {
					processSpec(spec);
				}
			}

			private void processSpec(ConstraintSpec spec) {
				final Ref[] args = spec.getRelated();
				final PropertyEditModel[] models = new PropertyEditModel[args.length + 1];
				{
					int index = 0;
					FormField ownerField = constraintField(_model, _property);
					models[index++] = new PropertyEditModel(_model, _property, ownerField);
					for (Ref arg : args) {
						PropertyValue target = resolve(arg);
						if (target == null) {
							return;
						}
						ConfigurationItem base = target.getItem();
						PropertyDescriptor targetProperty = target.getProperty();
						FormField targetField = constraintField(base, targetProperty);
						models[index++] = new PropertyEditModel(base, targetProperty, targetField);
					}
				}

				boolean asWarning = spec.asWarning();
				ConstraintAlgorithm constraint = spec.getAlgorithm();
				for (int n = 0, cnt = models.length; n < cnt; n++) {
					if (!constraint.isChecked(n)) {
						continue;
					}
					FormField field = models[n].getConstraintField();
					if (field != null) {
						if (asWarning) {
							field.addWarningConstraint(new DeclarativeConstraint(models, n, constraint));
						} else {
							field.addConstraint(new DeclarativeConstraint(models, n, constraint));
						}
					}
				}
			}

			private FormField constraintField(ConfigurationItem model, PropertyDescriptor property) {
				FormMember member = getMember(model, property);
				if (member == null) {
					return null;
				} else if (member.isSet(Editor.CONSTRAINT_FIELD)) {
					return member.get(Editor.CONSTRAINT_FIELD);
				} else if (member instanceof FormField) {
					return (FormField) member;
				} else {
					return null;
				}

			}

			private PropertyValue resolve(Ref ref) {
				return NamePath.resolve(ref, _model);
			}

		}

	}

	/**
	 * The {@link TypedConfiguration} of this {@link EditorFactory}.
	 */
	public Config getConfig() {
		return _config;
	}

	/**
	 * @see Config#getDisplayFieldsAsText()
	 */
	public boolean getDisplayFieldsAsText() {
		return getConfig().getDisplayFieldsAsText();
	}

	/**
	 * Append the CSS classes for the declarative form definition.
	 * 
	 * @return The given {@link FormMember}, for convenience.
	 */
	public <T extends FormMember> T appendDeclarativeDefinitionCssClasses(T formMember) {
		requireNonNull(formMember);
		appendCssClass(formMember, CSS_CLASS_DECLARATIVE_FORM_DEFINITION);
		appendCssClass(formMember, getCompactModeCssClass());
		return formMember;
	}

	/**
	 * The CSS class for the {@link #isCompact()} mode.
	 * 
	 * @return Never null.
	 */
	public String getCompactModeCssClass() {
		if (isCompact()) {
			return CSS_CLASS_MODE_COMPACT;
		}
		return CSS_CLASS_MODE_FULL;
	}

	/**
	 * Dynamic value of {@link FormMember#getMode()} of the UI created for the given property.
	 */
	public Value<FieldMode> fieldMode(PropertyDescriptor property, ConfigurationItem model) {
		ReadOnly annotation = getAnnotation(property, ReadOnly.class);
		if (annotation != null) {
			ReadOnlyMode staticMode = annotation.value();
			switch (staticMode) {
				case ON:
					return Values.literal(FieldMode.IMMUTABLE);
				case LOCAL:
					return Values.literal(FieldMode.LOCALLY_IMMUTABLE);
				default:
					// Use dynamic mode.
			}
		}

		DynamicMode modeAnnotation = getAnnotation(property, DynamicMode.class);
		if (modeAnnotation == null) {
			if (property.canHaveSetter()) {
				return Values.literal(FieldMode.ACTIVE);
			} else {
				return Values.literal(FieldMode.IMMUTABLE);
			}
		}
		ConfigurationDescriptor descriptor = property.getDescriptor();
		Class<? extends GenericFunction<FieldMode>> functionClass = modeAnnotation.fun();
		Ref[] argumentsSpec = modeAnnotation.args();
		String location = "visible annotation of '" + property.getPropertyName() + "'";

		return Fields
			.getDerivedValue(formOptions(property), descriptor, FieldMode.class, functionClass, argumentsSpec, location)
			.getValue(model);
	}

	/**
	 * Creates a new {@link EditorFactory} with the same settings as this one.
	 */
	public EditorFactory copy() {
		EditorFactory result = new EditorFactory(getInitializerProvider());
		result.setCustomizations(_customizations);
		result.setCompact(isCompact());
		return result;
	}

	@Override
	public <T extends Annotation> T getAnnotation(ConfigurationDescriptor type, Class<T> annotationType) {
		T result = _customizations.getAnnotation(type, annotationType);
		if (result == null) {
			result = _defaultCustomizations.getAnnotation(type, annotationType);
		}
		return result;
	}

	@Override
	public <T extends Annotation> T getAnnotation(PropertyDescriptor property, Class<T> annotationType) {
		T result = _customizations.getAnnotation(property, annotationType);
		if (result == null) {
			result = _defaultCustomizations.getAnnotation(property, annotationType);
		}
		return result;
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<?> type, Class<T> annotationType) {
		T result = _customizations.getAnnotation(type, annotationType);
		if (result == null) {
			result = _defaultCustomizations.getAnnotation(type, annotationType);
		}
		return result;
	}

}
