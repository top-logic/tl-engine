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
import com.top_logic.element.meta.kbbased.storage.ForeignKeyStorage;
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
import com.top_logic.model.annotate.persistency.CompositionStorage;
import com.top_logic.model.annotate.persistency.CompositionStorage.InSourceTable;
import com.top_logic.model.annotate.persistency.CompositionStorage.InTargetTable;
import com.top_logic.model.annotate.persistency.CompositionStorage.LinkTable;
import com.top_logic.model.annotate.persistency.CompositionStorage.Storage;
import com.top_logic.model.annotate.util.TLAnnotations;
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
					boolean isComposite = end.isComposite();
					if (isComposite) {
						CompositionStorage.Storage compositionStorage = getCompositionStorage(model);
						if (multiple) {
							if (compositionStorage instanceof CompositionStorage.InSourceTable) {
								Logger.warn(
									"Ignoring invalid " + CompositionStorage.class.getName() + " annotation on " + model
											+ " since a multiple composite reference can not be stored in the source table.",
									StorageImplementationFactory.class);
								compositionStorage = CompositionStorage.defaultCompositionLinkStorage();
							}
							if (ordered) {
								if (compositionStorage instanceof CompositionStorage.InTargetTable) {
									CompositionStorage.InTargetTable inTargetTable = (InTargetTable) compositionStorage;
									if (inTargetTable.getContainerOrder() == null) {
										Logger.warn("Ignoring invalid " + CompositionStorage.class.getName()
												+ " annotation on " + model
												+ " since no sort order column is set for sorted composite reference.",
											StorageImplementationFactory.class);
										config = ListStorage
											.listConfig(ApplicationObjectUtil.STRUCTURE_CHILD_ASSOCIATION, historyType);
									} else {
										config = InlineListStorage.listConfig(TLAnnotations.getTable(model.getType()),
											inTargetTable.getContainer(), inTargetTable.getContainerReference(),
											inTargetTable.getContainerOrder());
									}
								} else {
									CompositionStorage.LinkTable linkTable = (LinkTable) compositionStorage;
									config = ListStorage.listConfig(linkTable.getTable(), historyType);
								}
							} else {
								if (compositionStorage instanceof CompositionStorage.InTargetTable) {
									CompositionStorage.InTargetTable inTargetTable = (InTargetTable) compositionStorage;
									config = InlineSetStorage.setConfig(TLAnnotations.getTable(model.getType()),
										inTargetTable.getContainer(), inTargetTable.getContainerReference());
								} else {
									CompositionStorage.LinkTable linkTable = (LinkTable) compositionStorage;
									config = SetStorage.setConfig(linkTable.getTable(), historyType);
								}
							}
						} else {
							if (compositionStorage instanceof CompositionStorage.InTargetTable) {
								CompositionStorage.InTargetTable inTargetTable = (InTargetTable) compositionStorage;
								config = ReverseForeignKeyStorage.defaultConfig(TLAnnotations.getTable(model.getType()),
									inTargetTable.getContainer(), inTargetTable.getContainerReference());
							} else if (compositionStorage instanceof CompositionStorage.InSourceTable) {
								CompositionStorage.InSourceTable inSourceTable = (InSourceTable) compositionStorage;
								ForeignKeyStorage.Config<?> foreignKeyConfig =
									TypedConfiguration.newConfigItem(ForeignKeyStorage.Config.class);
								foreignKeyConfig.setStorageType(TLAnnotations.getTable(model.getOwner()));
								foreignKeyConfig.setStorageAttribute(inSourceTable.getPart());

								config = foreignKeyConfig;
							} else {
								CompositionStorage.LinkTable linkTable = (LinkTable) compositionStorage;
								config = SingletonLinkStorage.singletonLinkConfig(linkTable.getTable(), historyType);
							}
						}
					} else {
						if (multiple) {
							if (ordered) {
								TLType endType = end.getType();
								if (endType.getName().equals(GalleryImage.GALLERY_IMAGE_TYPE)) {
									config = ImageGalleryStorage.imageGalleryConfig();
								} else {
									config = ListStorage.listConfig(null, historyType);
								}
							} else {
								config = SetStorage.setConfig(null, historyType);
							}
						} else {
							TLType endType = end.getType();
							if (Document.DOCUMENT_TYPE.equals(TLModelUtil.qualifiedName(endType))) {
								config = TypedConfiguration.newConfigItem(DocumentStorage.Config.class);
							} else {
								config = SingletonLinkStorage.singletonLinkConfig(null, historyType);
							}
						}
					}
				}

				return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
			}


			private Storage getCompositionStorage(TLReference model) {
				CompositionStorage compositionStorage = TLAnnotations.getCompositionStorage(model);
				if (compositionStorage == null) {
					return CompositionStorage.defaultCompositionLinkStorage();
				}
				return compositionStorage.getStorage();
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
					"Ignoring invalid " + TLStorage.class.getName() + " annotation on composition " + part
							+ ". Use " + CompositionStorage.class.getName()
							+ " annotation to customize storage strategy.",
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

