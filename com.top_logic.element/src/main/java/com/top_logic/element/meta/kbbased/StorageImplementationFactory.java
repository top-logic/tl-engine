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
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference.DeletionPolicy;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.element.config.annotation.TLStorage;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.StorageImplementation;
import com.top_logic.element.meta.kbbased.storage.BinaryCollectionStorage;
import com.top_logic.element.meta.kbbased.storage.DocumentStorage;
import com.top_logic.element.meta.kbbased.storage.ImageGalleryStorage;
import com.top_logic.element.meta.kbbased.storage.ListStorage;
import com.top_logic.element.meta.kbbased.storage.PrimitiveStorage;
import com.top_logic.element.meta.kbbased.storage.ReverseStorage;
import com.top_logic.element.meta.kbbased.storage.SetStorage;
import com.top_logic.element.meta.kbbased.storage.SingletonLinkStorage;
import com.top_logic.element.model.imagegallery.GalleryImage;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLProperty;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePartVisitor;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.model.composite.CompositeStorage;
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
				PolymorphicConfiguration<? extends StorageImplementation> config;
				if (TLModelUtil.getEndIndex(model.getEnd()) == 0) {
					// Back reference.
					config = ReverseStorage.defaultConfig();
				} else {
					config = defaultReferenceStorage(model, unversionedStorage(model));
				}
				return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
			}

			private boolean isBinary(TLType type) {
				return type instanceof TLPrimitive primitive && primitive.getKind() == TLPrimitive.Kind.BINARY;
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
					if (model.isMultiple() && isBinary(model.getType())) {
						config = BinaryCollectionStorage.defaultConfig(unversionedStorage(model));
					} else {
						config = PRIMITIVE_STORAGE_CONFIG;
					}
				} else {
					config = annotation.getImplementation();
				}
				return TypedConfigUtil.createInstance(config);
			}
		};

	/**
	 * The configuration of the storage that the given reference uses, if it has no
	 * {@link TLStorage} annotation and is not an override copying the storage of the part it
	 * overrides.
	 *
	 * @param reference
	 *        The reference to create the storage configuration for.
	 * @param unversioned
	 *        Whether the reference values are stored unversioned, see
	 *        {@link #unversionedStorage(TLStructuredTypePart, MORepository)}.
	 * @return The default storage configuration, computed from the current properties of the
	 *         given reference (e.g. {@link TLStructuredTypePart#isBag()},
	 *         {@link TLReference#getHistoryType()}).
	 */
	public static PolymorphicConfiguration<? extends StorageImplementation> defaultReferenceStorage(
			TLReference reference, boolean unversioned) {
		TLAssociationEnd end = reference.getEnd();
		if (TLModelUtil.getEndIndex(end) == 0) {
			// Back reference.
			return ReverseStorage.defaultConfig();
		}

		HistoryType historyType = end.getHistoryType();
		DeletionPolicy deletionPolicy = end.getDeletionPolicy();
		if (deletionPolicy == null) {
			deletionPolicy = DeletionPolicy.CLEAR_REFERENCE;
		}
		boolean composite = end.isComposite();
		boolean bag = end.isBag();
		TLType endType = end.getType();
		if (end.isMultiple()) {
			if (end.isOrdered()) {
				if (endType.getName().equals(GalleryImage.GALLERY_IMAGE_TYPE)) {
					return ImageGalleryStorage.imageGalleryConfig(bag);
				}
				return ListStorage.listConfig(composite, historyType, deletionPolicy, unversioned, bag);
			}
			return SetStorage.setConfig(composite, historyType, deletionPolicy, unversioned, bag);
		}
		if (Document.DOCUMENT_TYPE.equals(TLModelUtil.qualifiedName(endType))) {
			return TypedConfiguration.newConfigItem(DocumentStorage.Config.class);
		}
		return SingletonLinkStorage.singletonLinkConfig(composite, historyType, deletionPolicy, unversioned);
	}

	/**
	 * Whether the values of the given part are stored unversioned.
	 *
	 * <p>
	 * The values of a part are stored unversioned, if the table of its owner is not versioned.
	 * </p>
	 *
	 * @param part
	 *        The part whose storage is determined.
	 * @param repository
	 *        The repository defining the table of the owner of the given part.
	 */
	public static boolean unversionedStorage(TLStructuredTypePart part, MORepository repository) {
		String tableName = TLAnnotations.getTable(part.getOwner());
		MetaObject table = repository.getTypeOrNull(tableName);

		if (table == null) {
			Logger.warn(
				"Unknown table " + tableName + " for owner  " + part.getOwner() + " of attribute " + part + ".",
				StorageImplementationFactory.class);
			return false;
		}
		if (table instanceof MOClass clazz) {
			return !clazz.isVersioned();
		}
		Logger.warn(
			"Table " + table + " for owner  " + part.getOwner() + " of attribute " + part + " is not a class.",
			StorageImplementationFactory.class);
		return false;
	}

	private static boolean unversionedStorage(TLStructuredTypePart part) {
		if (part.tTransient()) {
			// May happen during wrapper generation.
			return true;
		}
		return unversionedStorage(part, part.tKnowledgeBase().getMORepository());
	}

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
			return AbstractAttributeStorage.INSTANCE;
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
				} else if (isComposition(part)) {
					if (!CompositeStorage.class.isInstance(result)) {
						Logger.warn(
							"Ignoring invalid " + TLStorage.class.getName() + " annotation on compsite part " + part
									+ ". Storage implementation for composites must implement '"
									+ CompositeStorage.class.getName() + "': " + result,
							StorageImplementationFactory.class);
						return createDefaultStorageImplementation(part);
					}
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

	/**
	 * The part whose storage implementation the given part copies, or <code>null</code> if the
	 * storage of the given part is not copied from another part.
	 *
	 * <p>
	 * An override without {@link TLStorage} annotation uses the storage of the part it overrides,
	 * see {@link #createStorageImplementation(TLStructuredTypePart)}.
	 * </p>
	 */
	public static TLStructuredTypePart storageTemplate(TLStructuredTypePart part) {
		if (part.isAbstract() || part.getAnnotation(TLStorage.class) != null) {
			return null;
		}
		TLStructuredTypePart definition = part.getDefinition();
		if (definition == part) {
			return null;
		}
		return findStorageTemplate(part, definition);
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

