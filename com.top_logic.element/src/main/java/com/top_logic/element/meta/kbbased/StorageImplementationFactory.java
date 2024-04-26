/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstanceAccess;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.element.config.annotation.TLStorage;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.StorageImplementation;
import com.top_logic.element.meta.kbbased.storage.DocumentStorage;
import com.top_logic.element.meta.kbbased.storage.ImageGalleryStorage;
import com.top_logic.element.meta.kbbased.storage.InlineListStorage;
import com.top_logic.element.meta.kbbased.storage.InlineSetStorage;
import com.top_logic.element.meta.kbbased.storage.ListStorage;
import com.top_logic.element.meta.kbbased.storage.PrimitiveStorage;
import com.top_logic.element.meta.kbbased.storage.ReverseForeignKeyStorage;
import com.top_logic.element.meta.kbbased.storage.ReverseStorage;
import com.top_logic.element.meta.kbbased.storage.SetStorage;
import com.top_logic.element.meta.kbbased.storage.SingletonLinkStorage;
import com.top_logic.element.model.imagegallery.GalleryImage;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLProperty;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePartVisitor;
import com.top_logic.model.annotate.persistency.CompositionLinkTable;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.model.config.annotation.TableName;
import com.top_logic.model.util.TLModelUtil;

/**
 * Factory class to create {@link StorageImplementation} for a {@link TLStructuredTypePart}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class StorageImplementationFactory extends AnnotationsBasedCacheValueFactory {

	/**
	 * Singleton {@link StorageImplementationFactory} instance.
	 */
	public static final StorageImplementationFactory INSTANCE = new StorageImplementationFactory();

	private StorageImplementationFactory() {
		// Singleton constructor.
	}

	@Override
	public Object getCacheValue(MOAttribute attribute, DataObject item, Object[] storage) {
		TLStructuredTypePart obj = ((KnowledgeItem) item).getWrapper();
		StorageImplementation result = createStorageImplementation(obj);
		if (result != null) {
			result.init(obj);
		}
		return result;
	}

	static final PrimitiveStorage.Config<?> PRIMITIVE_STORAGE_CONFIG =
		TypedConfiguration.newConfigItem(PrimitiveStorage.Config.class);

	private static final TLTypePartVisitor<StorageImplementation, Void> DEFAULT_STORAGE_IMPLEMENTATION =
		new TLTypePartVisitor<>() {

			@Override
			public StorageImplementation visitReference(TLReference model, Void arg) {
				TLAssociationEnd end = model.getEnd();

				PolymorphicConfiguration<? extends StorageImplementation> config;
				if (TLModelUtil.getEndIndex(end) == 0) {
					// Back reference.
					config = ReverseStorage.defaultConfig();
				} else {
					HistoryType historyType = end.getHistoryType();
					boolean ordered = end.isOrdered();
					boolean multiple = end.isMultiple();
					if (multiple) {
						if (ordered) {
							TLType endType = end.getType();
							if (endType.getName().equals(GalleryImage.GALLERY_IMAGE_TYPE)) {
								config = ImageGalleryStorage.imageGalleryConfig();
							} else {
								TableName tableDefinition = getInlineCompositeDefinition(model);
								if (tableDefinition != null) {
									if (tableDefinition.getContainerOrder() == null) {
										Logger.warn(
											"Ignoring invalid " + TableName.class.getName() + " annotation on "
													+ end.getType() + " since the composite reference " + model
													+ " is ordered but no order column is set.",
											StorageImplementationFactory.class);
										config = ListStorage.listConfig(linkTable(model), historyType);
									} else {
										config = InlineListStorage.listConfig(tableDefinition.getName(),
											tableDefinition.getContainer(), tableDefinition.getContainerReference(),
											tableDefinition.getContainerOrder());
									}
								} else {
									config = ListStorage.listConfig(linkTable(model), historyType);
								}
							}
						} else {
							TableName tableDefinition = getInlineCompositeDefinition(model);
							if (tableDefinition != null) {
								config = InlineSetStorage.setConfig(tableDefinition.getName(),
									tableDefinition.getContainer(), tableDefinition.getContainerReference());
							} else {
								config = SetStorage.setConfig(linkTable(model), historyType);
							}
						}
					} else {
						TableName tableDefinition = getInlineCompositeDefinition(model);
						if (tableDefinition != null) {
							config = ReverseForeignKeyStorage.defaultConfig(tableDefinition.getName(),
								tableDefinition.getContainer(), tableDefinition.getContainerReference());
						} else {
							TLType endType = end.getType();
							if (Document.DOCUMENT_TYPE.equals(TLModelUtil.qualifiedName(endType))) {
								config = TypedConfiguration.newConfigItem(DocumentStorage.Config.class);
							} else {
								config = SingletonLinkStorage.singletonLinkConfig(linkTable(model), historyType);
							}
						}
					}
				}

				return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
			}

			private String linkTable(TLReference reference) {
				if (!reference.getEnd().isComposite()) {
					return null;
				}
				CompositionLinkTable storageTableAnnotation = TLAnnotations.getCompositionLinkTable(reference);
				if (storageTableAnnotation == null) {
					return ApplicationObjectUtil.STRUCTURE_CHILD_ASSOCIATION;
				} else {
					return storageTableAnnotation.getTable();
				}

			}

			private TableName getInlineCompositeDefinition(TLReference reference) {
				if (!reference.getEnd().isComposite()) {
					return null;
				}
				TLType targetType = reference.getType();
				return getCompositeInlineDefinition(targetType);

			}

			private TableName getCompositeInlineDefinition(TLType targetType) {
				TableName table = TLAnnotations.getTableName(targetType);
				if (table == null) {
					return null;
				}
				if (table.getContainer() == null) {
					return null;
				}
				if (table.getContainerReference() == null) {
					Logger.warn("Ignoring invalid " + TableName.class.getName() + " annotation on " + targetType
							+ " since the container column is set, but no column that contains the container reference.",
						StorageImplementationFactory.class);
					return null;
				}
				return table;
			}

			@Override
			public StorageImplementation visitAssociationEnd(TLAssociationEnd model, Void arg) {
				return NoStorage.INSTANCE;
			}

			@Override
			public StorageImplementation visitClassifier(TLClassifier model, Void arg) {
				throw new UnsupportedOperationException("No storage implementation for classifier.");
			}

			@Override
			public StorageImplementation visitProperty(TLProperty model, Void arg) {
				TLStorage annotation = (model.getType()).getAnnotation(TLStorage.class);
				PolymorphicConfiguration<? extends StorageImplementation> config;
				if (annotation == null) {
					config = PRIMITIVE_STORAGE_CONFIG;
				} else {
					config = annotation.getImplementation();
				}
				return TypedConfigUtil.createInstance(config);
			}
		};

	/**
	 * Creates a {@link StorageImplementation} for the given {@link TLStructuredTypePart}.
	 * 
	 * @param part
	 *        The part to create {@link StorageImplementation} for.
	 */
	public static StorageImplementation createStorageImplementation(TLStructuredTypePart part) {
		TLStorage storageAnnotation = part.getAnnotation(TLStorage.class);

		StorageImplementation result;
		PolymorphicConfiguration<? extends StorageImplementation> config;
		if (part.isAbstract()) {
			if (storageAnnotation != null) {
				Logger.warn(
					"Ignoring invalid " + TLStorage.class.getName() + " annotation on abstract part " + part
							+ ". Storage implementations are set on the concrete implementation.",
					StorageImplementationFactory.class);
			}
			return NoStorage.INSTANCE;
		} else if (isComposition(part)) {
			if (storageAnnotation != null) {
				Logger.warn(
					"Ignoring invalid " + TLStorage.class.getName() + " annotation on " + part
						+ " since the storage of a composition cannot be customized. ",
					StorageImplementationFactory.class);
			}
			return createDefaultStorageImplementation(part);
		} else if (storageAnnotation == null) {
			TLStructuredTypePart definition = part.getDefinition();
			if (definition != part) {
				TLStructuredTypePart storageTemplate = findStorageTemplate(part, definition);
				if (storageTemplate == null) {
					// No part to copy storage implementation from.
					return createDefaultStorageImplementation(part);
				}
				StorageImplementation original = AttributeOperations.getStorageImplementation(storageTemplate);

				// Note: The Storage implementation is bound to its attribute. Therefore it must be
				// newly instantiated.
				@SuppressWarnings("unchecked")
				PolymorphicConfiguration<? extends StorageImplementation> instanceConfig =
					(PolymorphicConfiguration<? extends StorageImplementation>) InstanceAccess.INSTANCE
						.getConfig(original);

				config = instanceConfig;
			} else {
				return createDefaultStorageImplementation(part);
			}
		} else {
			config = storageAnnotation.getImplementation();
		}

		{
			try {
				result = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
				if (result == null) {
					// Configuration error that did not result in an exception (e.g. empty implementation config).
					result = NoStorage.INSTANCE;
				}
			} catch (RuntimeException ex) {
				Logger.error(
					"Cannot create storage implementation for attribute '" + part + "'.", ex,
					StorageImplementationFactory.class);

				result = NoStorage.INSTANCE;
			}
		}
		return result;

	}

	private static TLStructuredTypePart findStorageTemplate(TLStructuredTypePart part,
			TLStructuredTypePart definition) {
		if (!definition.isAbstract()) {
			return definition;
		}
		if (!(part instanceof TLClassPart)) {
			return null;
		}
		return firstOverriddenNonAbstract((TLClassPart) part);
	}

	private static TLClassPart firstOverriddenNonAbstract(TLClassPart part) {
		for (TLClassPart overriddenPart : TLModelUtil.getOverriddenParts(part)) {
			if (overriddenPart.isAbstract()) {
				TLClassPart firstOverriddenNonAbstract = firstOverriddenNonAbstract(overriddenPart);
				if (firstOverriddenNonAbstract != null) {
					return firstOverriddenNonAbstract;
				} else {
					continue;
				}
			} else {
				return overriddenPart;
			}
		}
		return null;
	}

	private static boolean isComposition(TLStructuredTypePart part) {
		return part.getModelKind() == ModelKind.REFERENCE && ((TLReference) part).getEnd().isComposite();
	}

	private static StorageImplementation createDefaultStorageImplementation(TLStructuredTypePart part) {
		return part.visitTypePart(DEFAULT_STORAGE_IMPLEMENTATION, null);
	}

}

