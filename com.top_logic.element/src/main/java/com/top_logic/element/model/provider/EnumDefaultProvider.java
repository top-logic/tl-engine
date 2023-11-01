/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.provider;

import static com.top_logic.layout.form.values.Fields.*;
import static com.top_logic.layout.form.values.Values.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationListener;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.element.layout.meta.TLStructuredTypePartFormBuilder.PartModel;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.knowledge.wrap.list.FastListElementComparator;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.template.SelectionControlProvider;
import com.top_logic.layout.form.values.ConfigurationValue;
import com.top_logic.layout.form.values.ListenerBinding;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.layout.form.values.edit.annotation.PropertyEditor;
import com.top_logic.layout.form.values.edit.editor.Editor;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.provider.DefaultProvider;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.TLCollator;

/**
 * {@link DefaultProvider} for {@link TLStructuredTypePart} of type {@link TLTypeKind#ENUMERATION
 * enumeration}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp(priority = 100)
@TargetType(value = TLTypeKind.ENUMERATION)
public class EnumDefaultProvider implements DefaultProvider, ConfiguredInstance<EnumDefaultProvider.Config> {

	/**
	 * Configuration of an {@link EnumDefaultProvider}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName("classifier")
	public interface Config extends PolymorphicConfiguration<EnumDefaultProvider>, ConfigPart {

		/** Configuration name of the value of {@link #getAnnotation}. */
		String ANNOTATION_NAME = "annotation";

		/**
		 * The {@link TLModelUtil#qualifiedName(com.top_logic.model.TLModelPart) qualified name} of
		 * the default {@link TLClassifier}.
		 */
		@Mandatory
		@PropertyEditor(ClassifierSelectionEditor.class)
		@Format(CommaSeparatedStrings.class)
		List<String> getValue();

		/**
		 * {@link TLAnnotation} container of this {@link Config}.
		 */
		@Container
		@Hidden
		@Name(ANNOTATION_NAME)
		TLAnnotation getAnnotation();

		/**
		 * {@link Editor} for the {@link Config#getValue()} field that reacts on the multiplicity of
		 * the context part configuration.
		 */
		class ClassifierSelectionEditor implements Editor {

			@Override
			public FormMember createUI(EditorFactory editorFactory, FormContainer container, ValueModel model) {
				Config providerConfig = (Config) model.getModel();
				TLAnnotation annotation = providerConfig.getAnnotation();
				PartModel partModel = getPartModel(annotation);

				SelectField input = createInput(container, model, partModel);

				ConfigurationListener update = change -> {
					container.removeMember(container.getMember(inputName(model)));
					createInput(container, model, partModel);
				};

				if (partModel != null) {
					partModel.addConfigurationListener(
						partModel.descriptor().getProperty(PartModel.MULTIPLE_PROPERTY), update);
					partModel.addConfigurationListener(
						partModel.descriptor().getProperty(PartModel.ORDERED_PROPERTY), update);
				}
				return input;
			}

			private SelectField createInput(FormContainer container, ValueModel model, PartModel partModel) {
				TLEnumeration type = partModel == null ? null : (TLEnumeration) partModel.getResolvedType();
				boolean multiple = partModel == null ? false : partModel.isMultiple();
				boolean ordered = partModel != null && partModel.isOrdered();
				SelectField input = selectField(container, inputName(model), multiple, false);

				if (multiple) {
					input.setControlProvider(SelectionControlProvider.SELECTION_INSTANCE);

					if (ordered) {
						input.setCustomOrder(true);
					}
				}

				options(input, optionList(partModel), new FastListElementComparator(new TLCollator()));
				ConfigurationValue<Object> storage = configurationValue(model);
				ListenerBinding binding1 = bindValue(input, storage, storageValue -> {
					if (storageValue == null || type == null) {
						return Collections.emptyList();
					}

					@SuppressWarnings("unchecked")
					Collection<String> names = (Collection<String>) storageValue;

					try {
						return resolveClassifiers(type, multiple, names);
					} catch (ConfigurationException ex) {
						String attributeName = partModel == null ? "<unknown>" : partModel.getName();
						errorInvalidDefault(
							TLModelUtil.qualifiedName(type) + TLModelUtil.QUALIFIED_NAME_PART_SEPARATOR
								+ attributeName,
							ex);
						return Collections.emptyList();
					}
				});
				ListenerBinding binding2 = linkStorage(fieldValue(input), storage, inputValue -> {
					if (inputValue == null) {
						return Collections.emptyList();
					}

					if (multiple) {
						@SuppressWarnings("unchecked")
						Collection<TLClassifier> classifiers = (Collection<TLClassifier>) inputValue;

						List<String> result = new ArrayList<>();
						for (TLClassifier c : classifiers) {
							result.add(toName(c));
						}
						return result;
					} else {
						TLClassifier classifier = (TLClassifier) inputValue;
						return Collections.singletonList(toName(classifier));
					}
				});

				input.addListener(FormMember.REMOVED_FROM_PARENT, (member, parent) -> {
					binding1.close();
					binding2.close();
					return Bubble.BUBBLE;
				});

				return input;
			}

			private String inputName(ValueModel model) {
				return normalizeFieldName(model.getProperty().getPropertyName());
			}

			private static String toName(TLClassifier c) {
				return c.getName();
			}

			static List<?> optionList(PartModel partModel) {
				TLEnumeration enumeration = getEnumerationType(partModel);
				if (enumeration == null) {
					return Collections.emptyList();
				}
				return enumeration.getClassifiers();
			}

			static TLEnumeration getEnumerationType(PartModel partModel) {
				if (partModel == null) {
					return null;
				}
				TLType type = partModel.getResolvedType();
				TLEnumeration enumeration = (TLEnumeration) type;
				return enumeration;
			}

			static PartModel getPartModel(TLAnnotation annotation) {
				if (annotation == null) {
					// May happen when reselecting EnumDefaultProider.
					return null;
				}
				AnnotatedConfig<?> annotated = annotation.getAnnotated();
				if (!(annotated instanceof PartModel)) {
					// Should actually not happen.
					return null;
				}
				return (PartModel) annotated;
			}
		}
	}

	private final Config _config;

	/**
	 * Creates a new {@link EnumDefaultProvider} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link EnumDefaultProvider}.
	 */
	public EnumDefaultProvider(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public Object createDefault(Object context, TLStructuredTypePart attribute, boolean createForUI) {
		List<TLClassifier> resolvedParts;
		try {
			TLEnumeration type = (TLEnumeration) attribute.getType();
			boolean multiple = attribute.isMultiple();
			resolvedParts = resolveClassifiers(type, multiple, getConfig().getValue());
			if (AttributeOperations.isCollectionValued(attribute)) {
				if (!attribute.isOrdered()) {
					return new HashSet<>((Collection<?>) resolvedParts);
				}
			} else {
				if (resolvedParts.size() != 1) {
					Logger.error("Expected exactly one classifier as default for attribute '" + attribute + "'.",
						EnumDefaultProvider.class);
					return null;
				}
				return resolvedParts.get(0);
			}
		} catch (ConfigurationException ex) {
			errorInvalidDefault(attribute, ex);
			resolvedParts = null;
		} catch (RuntimeException ex) {
			// Part does not longer exists.
			resolvedParts = null;
		}
		return resolvedParts;
	}

	static void errorInvalidDefault(Object attribute, ConfigurationException ex) {
		Logger.error("Invalid default configuration in '" + attribute + "'.", ex, EnumDefaultProvider.class);
	}

	static List<TLClassifier> resolveClassifiers(TLEnumeration type, boolean multiple,
			Collection<String> localOrQualifiedNames) throws ConfigurationException {
		List<TLClassifier> result = new ArrayList<>();
		for (String name : localOrQualifiedNames) {
			result.add(resolveClassifier(type, name));

			if (!multiple) {
				// For safety reasons in case of inconsistent data.
				break;
			}
		}
		return result;
	}

	static TLClassifier resolveClassifier(TLEnumeration type, String localOrQualifiedName)
			throws ConfigurationException {
		// Note: The classifier can be encoded as qualified name for legacy compatibility.
		int sep = localOrQualifiedName.indexOf(TLModelUtil.QUALIFIED_NAME_PART_SEPARATOR);
		String localName = sep >= 0 ? localOrQualifiedName.substring(sep + 1) : localOrQualifiedName;

		return TLModelUtil.resolvePart(type, localName);
	}

	@Override
	public Config getConfig() {
		return _config;
	}

}

