/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.OptionProvider;
import com.top_logic.element.meta.SimpleEditContext;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.SelectFieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.form.AttributeSelectFieldModel.OptionSource;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;

/**
 * Service that resolves the appropriate {@link ReactFieldControlProvider} for a model attribute.
 *
 * <p>
 * Resolution chain:
 * </p>
 * <ol>
 * <li>{@link TLInputControl} annotation on the attribute or its type (via
 * {@link com.top_logic.model.annotate.DefaultStrategy.Strategy#VALUE_TYPE}).</li>
 * <li>Global type-to-provider map configured in this service.</li>
 * <li>Built-in fallback based on {@link com.top_logic.model.TLPrimitive.Kind}.</li>
 * </ol>
 */
public class FieldControlService extends ConfiguredManagedClass<FieldControlService.Config> {

	/**
	 * Configuration options for {@link FieldControlService}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<FieldControlService> {

		/**
		 * Global type-to-provider mappings keyed by model type reference.
		 */
		@Key(ProviderMapping.TYPE)
		Map<TLModelPartRef, ProviderMapping> getProviders();

	}

	/**
	 * A single type-to-provider mapping entry.
	 */
	public interface ProviderMapping extends ConfigurationItem {

		/** Property name of {@link #getType()}. */
		String TYPE = "type";

		/**
		 * The model type this mapping applies to.
		 */
		@Name(TYPE)
		TLModelPartRef getType();

		/**
		 * The control provider to use for attributes of this type.
		 */
		@Mandatory
		PolymorphicConfiguration<? extends ReactFieldControlProvider> getImpl();

	}

	private final InstantiationContext _context;

	private Map<String, ReactFieldControlProvider> _providerByQualifiedType;

	private final ReactFieldControlProvider _checkboxProvider = new CheckboxControlProvider();

	private final ReactFieldControlProvider _numberProvider = new NumberInputControlProvider();

	private final ReactFieldControlProvider _dateProvider = new DatePickerControlProvider();

	private final ReactFieldControlProvider _textProvider = new TextInputControlProvider();

	private final ReactFieldControlProvider _selectProvider = new SelectControlProvider();

	/**
	 * Creates a {@link FieldControlService} from configuration.
	 */
	@CalledByReflection
	public FieldControlService(InstantiationContext context, Config config) {
		super(context, config);
		_context = context;
	}

	@Override
	protected void startUp() {
		super.startUp();

		_providerByQualifiedType = new HashMap<>();
		for (ProviderMapping mapping : getConfig().getProviders().values()) {
			TLModelPartRef typeRef = mapping.getType();
			if (typeRef != null) {
				TLType type = typeRef.resolveType();
				if (type != null) {
					ReactFieldControlProvider provider = _context.getInstance(mapping.getImpl());
					_providerByQualifiedType.put(TLModelUtil.qualifiedName(type), provider);
				}
			}
		}
	}

	/**
	 * The configured {@link ReactFieldControlProvider} for the given type, or {@code null}.
	 */
	private ReactFieldControlProvider byType(TLType type) {
		return _providerByQualifiedType.get(TLModelUtil.qualifiedName(type));
	}

	/**
	 * Resolves and creates the appropriate input control for the given attribute.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param part
	 *        The model attribute.
	 * @param model
	 *        The field model providing value, editability, and change notifications.
	 * @return A React control for the field input widget.
	 */
	public ReactControl createFieldControl(ReactContext context, TLStructuredTypePart part, FieldModel model) {
		// 1. Annotation on attribute (includes type-level default via VALUE_TYPE strategy).
		TLInputControl annotation = part.getAnnotation(TLInputControl.class);
		if (annotation != null) {
			ReactFieldControlProvider provider = _context.getInstance(annotation.getImpl());
			return provider.createControl(context, part, model);
		}

		// 2. Option-based attributes use a select control.
		if (model instanceof SelectFieldModel) {
			return _selectProvider.createControl(context, part, model);
		}

		// 3. Configured control by type.
		ReactFieldControlProvider mapped = byType(part.getType());
		if (mapped != null) {
			return mapped.createControl(context, part, model);
		}

		// 4. Built-in primitive-kind fallback.
		return primitiveFallback(context, part, model);
	}

	/**
	 * Creates the {@link AttributeFieldModel} for the given attribute.
	 *
	 * <p>
	 * Returns an {@link AttributeSelectFieldModel} when the attribute is edited by selecting from a
	 * set of options (a configured option-bearing datatype, an enumeration, a reference, an enum
	 * datatype, or an attribute with a TL-Script options annotation), and a plain
	 * {@link AttributeFieldModel} otherwise.
	 * </p>
	 *
	 * @param object
	 *        The object to read/write the attribute value from.
	 * @param part
	 *        The attribute to bind to.
	 * @param form
	 *        The enclosing form.
	 */
	public AttributeFieldModel createModel(TLObject object, TLStructuredTypePart part, FormControl form) {
		OptionSource optionSource = selectOptionSource(part);
		if (optionSource != null) {
			return new AttributeSelectFieldModel(object, part, form, optionSource);
		}
		return new AttributeFieldModel(object, part);
	}

	/**
	 * The {@link OptionSource} for the given attribute if it is edited as a select, or {@code null}
	 * if it uses a plain (non-select) control.
	 */
	private OptionSource selectOptionSource(TLStructuredTypePart part) {
		// 1. Explicit control annotation.
		TLInputControl annotation = part.getAnnotation(TLInputControl.class);
		if (annotation != null) {
			ReactFieldControlProvider provider = _context.getInstance(annotation.getImpl());
			if (provider instanceof SelectControlProvider) {
				return optionSourceFor(part, (SelectControlProvider) provider);
			}
			return null;
		}

		// 2. Configured control by type.
		ReactFieldControlProvider mapped = byType(part.getType());
		if (mapped instanceof SelectControlProvider) {
			return optionSourceFor(part, (SelectControlProvider) mapped);
		}
		if (mapped != null) {
			// A configured non-select control (e.g. color, icon).
			return null;
		}

		// 3. Structural select (enumeration, reference, enum datatype, options generator).
		if (AttributeOptions.isStructuralSelect(part)) {
			return (self, overlays, dependencies) -> AttributeOptions.optionsFor(self, part, overlays, dependencies);
		}
		return null;
	}

	/**
	 * The {@link OptionSource} for an attribute edited by the given {@link SelectControlProvider}.
	 *
	 * <p>
	 * An attribute-level options generator (e.g. supported locales) takes precedence over the
	 * provider's configured option source; otherwise the configured option source is used, falling
	 * back to the attribute's structural options.
	 * </p>
	 */
	private OptionSource optionSourceFor(TLStructuredTypePart part, SelectControlProvider provider) {
		OptionProvider options = provider.getConfiguredOptions();
		if (options != null && AttributeOperations.getOptions(part) == null) {
			OptionProvider configured = options;
			return (self, overlays, dependencies) -> AttributeOptions
				.toList(configured.getOptions(SimpleEditContext.createContext(self, part)));
		}
		return (self, overlays, dependencies) -> AttributeOptions.optionsFor(self, part, overlays, dependencies);
	}

	private ReactControl primitiveFallback(ReactContext context, TLStructuredTypePart part, FieldModel model) {
		TLType type = part.getType();
		if (type instanceof TLPrimitive) {
			TLPrimitive primitive = (TLPrimitive) type;
			switch (primitive.getKind()) {
				case BOOLEAN:
					return _checkboxProvider.createControl(context, part, model);
				case INT:
				case FLOAT:
					return _numberProvider.createControl(context, part, model);
				case DATE:
					return _dateProvider.createControl(context, part, model);
				case STRING:
				case TRISTATE:
				case BINARY:
				case CUSTOM:
				default:
					return _textProvider.createControl(context, part, model);
			}
		}
		return _textProvider.createControl(context, part, model);
	}

	/**
	 * The {@link FieldControlService} singleton.
	 */
	public static FieldControlService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Singleton holder for the {@link FieldControlService}.
	 */
	public static final class Module extends TypedRuntimeModule<FieldControlService> {

		/**
		 * Singleton {@link FieldControlService.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<FieldControlService> getImplementation() {
			return FieldControlService.class;
		}

	}

}
