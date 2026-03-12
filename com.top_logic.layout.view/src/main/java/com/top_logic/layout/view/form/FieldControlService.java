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
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelPartRef;

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
 * <li>Built-in fallback based on {@link TLPrimitive.Kind}.</li>
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

	private Map<String, ReactFieldControlProvider> _providerByTypeName;

	private final ReactFieldControlProvider _checkboxProvider = new CheckboxControlProvider();

	private final ReactFieldControlProvider _numberProvider = new NumberInputControlProvider();

	private final ReactFieldControlProvider _dateProvider = new DatePickerControlProvider();

	private final ReactFieldControlProvider _textProvider = new TextInputControlProvider();

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

		_providerByTypeName = new HashMap<>();
		for (ProviderMapping mapping : getConfig().getProviders().values()) {
			TLModelPartRef typeRef = mapping.getType();
			if (typeRef != null) {
				TLType type = typeRef.resolveType();
				if (type != null) {
					ReactFieldControlProvider provider = _context.getInstance(mapping.getImpl());
					_providerByTypeName.put(type.getName(), provider);
				}
			}
		}
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

		// 2. Global service map by type name.
		TLType type = part.getType();
		ReactFieldControlProvider mapped = _providerByTypeName.get(type.getName());
		if (mapped != null) {
			return mapped.createControl(context, part, model);
		}

		// 3. Built-in primitive-kind fallback.
		return primitiveFallback(context, part, model);
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
