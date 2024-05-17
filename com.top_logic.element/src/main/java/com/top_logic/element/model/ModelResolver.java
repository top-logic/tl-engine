/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model;

import static com.top_logic.basic.config.misc.TypedConfigUtil.*;
import static com.top_logic.basic.shared.collection.CollectionUtilShared.*;
import static com.top_logic.basic.util.Utils.*;
import static com.top_logic.model.util.TLModelUtil.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Log;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.col.factory.CollectionFactory;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.element.config.AssociationConfig;
import com.top_logic.element.config.AssociationConfig.EndConfig;
import com.top_logic.element.config.AttributeConfig;
import com.top_logic.element.config.AttributedTypeConfig;
import com.top_logic.element.config.ClassConfig;
import com.top_logic.element.config.EndAspect;
import com.top_logic.element.config.ExtendsConfig;
import com.top_logic.element.config.ModelConfig;
import com.top_logic.element.config.ModuleConfig;
import com.top_logic.element.config.ObjectTypeConfig;
import com.top_logic.element.config.PartConfig;
import com.top_logic.element.config.ReferenceConfig;
import com.top_logic.element.config.ReferenceConfig.ReferenceKind;
import com.top_logic.element.config.RoleAssignment;
import com.top_logic.element.config.SingletonConfig;
import com.top_logic.element.config.annotation.TLSingletons;
import com.top_logic.element.config.annotation.TLStorage;
import com.top_logic.element.meta.StorageImplementation;
import com.top_logic.element.meta.kbbased.KBBasedMetaAttribute;
import com.top_logic.element.meta.kbbased.PersistentAssociation;
import com.top_logic.element.meta.schema.HolderType;
import com.top_logic.knowledge.util.OrderedLinkUtil;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLNamedPart;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.TLProperty;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLScope;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.TransientObject;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.annotate.AnnotationInheritance;
import com.top_logic.model.annotate.AnnotationInheritance.Policy;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.annotate.security.RoleConfig;
import com.top_logic.model.annotate.security.TLRoleDefinitions;
import com.top_logic.model.config.DatatypeConfig;
import com.top_logic.model.config.EnumConfig;
import com.top_logic.model.config.EnumConfig.ClassifierConfig;
import com.top_logic.model.config.ScopeConfig;
import com.top_logic.model.config.TLTypeAnnotation;
import com.top_logic.model.config.TypeConfig;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.model.impl.util.TLStructuredTypeColumns;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * Algorithm to build a {@link TLModel} from a {@link ModelConfig}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ModelResolver {

	private static final Set<String> PROPERTIES_FOR_OVERRIDES = CollectionFactory.set(
		AttributeConfig.NAME,
		AttributeConfig.OVERRIDE,
		AttributeConfig.TYPE_SPEC,
		AttributeConfig.ABSTRACT_PROPERTY,
		AttributeConfig.MANDATORY,
		ReferenceConfig.END,
		ReferenceConfig.KIND,
		AttributeConfig.ANNOTATIONS);

	private final Protocol _log;

	private final TLModel _model;

	private final TLFactory _factory;

	private final ModelCreation _schedule;

	/**
	 * Creates a {@link ModelResolver}.
	 */
	public ModelResolver(Protocol log, TLModel model, TLFactory factory) {
		_log = log;
		_model = model;
		_factory = factory;
		_schedule = new ModelCreation();
	}

	/**
	 * The {@link Log} to write errors to.
	 */
	protected final Log log() {
		return _log;
	}

	/**
	 * The model creation schedule.
	 */
	protected final ModelCreation schedule() {
		return _schedule;
	}

	/**
	 * Completes all deferred creations.
	 */
	public void complete() {
		_schedule.complete(_log);
		_log.checkErrors();
	}

	/**
	 * The {@link TLModel} to operate on.
	 */
	public TLModel getModel() {
		return _model;
	}

	/**
	 * The {@link TLFactory} to create instances of the {@link #getModel()}s types.
	 */
	public TLFactory getFactory() {
		return _factory;
	}

	private TLType lookupType(Object scopeBase, String scopeRef, String moduleName, String interfaceName) {
		return DynamicModelService.lookupType(_model, scopeBase, scopeRef, moduleName, interfaceName);
	}

	/**
	 * Create all types specified in the given {@link ScopeConfig} locally within the scope
	 * instance.
	 * @param module
	 *        The defining module.
	 * @param scopeInstance
	 *        The object defining local types.
	 * @param scopeConfig
	 *        The definition of local types to create.
	 */
	public void setupScope(TLModule module, TLScope scopeInstance, ScopeConfig scopeConfig) {
		assert scopeInstance != null;
		if (scopeInstance == module) {
			// setup of module itself
			addAnnotations(module, (ModuleConfig) scopeConfig);
		}

		// Create new types and update existing ones.
		for (TypeConfig config : scopeConfig.getTypes()) {
			addType(module, scopeInstance, config);
		}
	}

	/**
	 * Creates a new {@link TLType} in the given {@link TLScope} assigned to the given
	 * {@link TLModule}.
	 */
	public void addType(TLModule module, TLScope scope, TypeConfig config) {
		if (config instanceof ObjectTypeConfig) {
			addObjectType(module, scope, (ObjectTypeConfig) config);
		} else if (config instanceof DatatypeConfig) {
			addDataType(module, scope, (DatatypeConfig) config);
		} else if (config instanceof EnumConfig) {
			addEnumType(module, scope, (EnumConfig) config);
		} else if (config instanceof AssociationConfig) {
			addAssociationType(module, scope, (AssociationConfig) config);
		} else {
			log().error("Unsupported type configuration '" + config.getConfigurationInterface() + "'.");
		}
	}

	/**
	 * Creates a new {@link TLAssociation} in the given {@link TLScope} assigned to the given
	 * {@link TLModule}.
	 */
	protected void addAssociationType(TLModule module, TLScope scope, AssociationConfig config) {
		String interfaceName = config.getName();

		TLAssociation existingType =
			(TLAssociation) lookupType(scope, HolderType.THIS, module.getName(), interfaceName);
		if (existingType == null) {
			TLAssociation newType =
				TLModelUtil.addAssociation(module, scope, interfaceName);

			for (TLTypeAnnotation annotation : config.getAnnotations()) {
				newType.setAnnotation(annotation);
			}

			_schedule.createTypeHierarchy(new NewAssociation(module, scope, newType, config));
		} else {
			_schedule.createTypeHierarchy(new CreateParts(module, scope, existingType, config, false));
		}
	}

	/**
	 * Creates a new {@link TLEnumeration} in the given {@link TLScope} assigned to the given
	 * {@link TLModule}.
	 */
	protected void addEnumType(TLModule module, TLScope scope, EnumConfig config) {
		TLType existing = module.getType(config.getName());
		if (existing != null) {
			return;
		}

		TLEnumeration classification = TLModelUtil.addEnumeration(module, scope, config.getName());
		addAnnotations(classification, config);

		List<ClassifierConfig> classifierConfigs = config.getClassifiers();
		for (ClassifierConfig classifierConfig : classifierConfigs) {
			addClassifier(classification, classifierConfig);
		}
	}

	/**
	 * Creates a new {@link TLPrimitive} in the given {@link TLScope} assigned to the given
	 * {@link TLModule}.
	 */
	protected void addDataType(TLModule module, TLScope scope, DatatypeConfig config) {
		String datatypeName = config.getName();
		TLPrimitive existing = (TLPrimitive) module.getType(datatypeName);
		if (existing == null) {
			Kind kind = config.getKind();
			StorageMapping<?> storageMapping = createInstance(config.getStorageMapping());
			TLPrimitive newPrimitive =
				TLModelUtil.addDatatype(module, scope, datatypeName, kind, storageMapping);
			newPrimitive.setDBType(config.getDBType());
			newPrimitive.setDBSize(config.getDBSize());
			newPrimitive.setDBPrecision(config.getDBPrecision());
			newPrimitive.setBinary(config.isBinary());

			addAnnotations(newPrimitive, config);
		}
	}

	/**
	 * Creates a new {@link TLClass} in the given {@link TLScope} assigned to the given
	 * {@link TLModule}.
	 */
	protected void addObjectType(TLModule module, TLScope scope, ObjectTypeConfig config) {
		TLClass existingType = (TLClass)
		this.lookupType(scope, HolderType.THIS, module.getName(), config.getName());
		if (existingType == null) {
			TLClass newType = TLModelUtil.addClass(module, scope, config.getName());
			boolean isAbstract;
			boolean isFinal;
			if (config instanceof ClassConfig) {
				ClassConfig classConfig = (ClassConfig) config;

				isAbstract = classConfig.isAbstract();
				isFinal = classConfig.isFinal();
			} else {
				isAbstract = true;
				isFinal = false;

			}
			newType.setAbstract(isAbstract);
			newType.setFinal(isFinal);

			addAnnotations(newType, config);

			_schedule.createTypeHierarchy(new NewType(module, scope, newType, config));
			_schedule.createParts(newType, new CreateParts(module, scope, newType, config, true));
		} else {
			_schedule.createParts(existingType, new CreateParts(module, scope, existingType, config, false));
		}
	}

	/**
	 * Appends new {@link TLClassifier} to the given {@link TLEnumeration}.
	 * 
	 * @see #addClassifier(TLEnumeration, ClassifierConfig, TLClassifier)
	 */
	public final void addClassifier(TLEnumeration classification, ClassifierConfig classifierConfig) {
		addClassifier(classification, classifierConfig, null);
	}

	/**
	 * Creates a new {@link TLClassifier} before the given one in the given {@link TLEnumeration}.
	 * 
	 * @param before
	 *        May be <code>null</code> in which case the new classifier is appended to the list of
	 *        classifiers.
	 */
	public void addClassifier(TLEnumeration classification, ClassifierConfig classifierConfig, TLClassifier before) {
		TLClassifier classifier = _model.createClassifier(classifierConfig.getName());
		if (before == null) {
			classification.getClassifiers().add(classifier);
		} else {
			classification.getClassifiers().add(classification.getClassifiers().indexOf(before), classifier);
		}
		addTypePartAnnotations(classifier, false, classifierConfig);

		// Note: The persistent implementation only supports changing the default, if
		// the classifier is currently linked to its container.
		classifier.setDefault(classifierConfig.isDefault());
	}

	/**
	 * Add the configured attributes to the {@link TLClass}
	 * 
	 * @param type
	 *        The {@link TLClass} to add the attributes to.
	 * @param typeConfig
	 *        The type configuration.
	 * @param isNew
	 *        If <code>true</code> do not check for existing attributes.
	 */
	void addParts(final TLStructuredType type, AttributedTypeConfig typeConfig, final boolean isNew) {
		boolean needsResort = false;
		for (PartConfig partConfig : typeConfig.getAttributes()) {
			try {
				if (isNew || type.getPart(partConfig.getName()) == null) {
					needsResort = addPart(isNew, type, partConfig, needsResort);
				}
			} catch (Exception e) {
				String qPartName = TLModelUtil.qualifiedTypePartName(type, partConfig.getName());
				log().error("Failed to create attribute '" + qPartName + "' in '" + typeConfig.location() + "'.", e);
			}
		}
		if (needsResort) {
			_schedule.reorderProperties(() -> resort(type, typeConfig));
		}
	}

	/**
	 * Adds a new {@link TLStructuredTypePart} to an existing type.
	 * @param type
	 *        The type to update.
	 * @param partConfig
	 *        The configuration of the new part.
	 */
	public void addPart(TLStructuredType type, PartConfig partConfig) {
		addPart(false, type, partConfig, false);
	}

	private boolean addPart(final boolean isNew, final TLStructuredType type, PartConfig partConfig,
			boolean needsResort) {
		if (partConfig instanceof ReferenceConfig) {
			boolean stepAdded = handleReferenceConfig((TLClass) type, isNew, (ReferenceConfig) partConfig);
			needsResort |= stepAdded;
		} else if (partConfig instanceof AssociationConfig.EndConfig) {
			boolean stepAdded = handleEndConfig(type, isNew, (EndConfig) partConfig);
			needsResort |= stepAdded;
		} else {
			createProperty(type, (AttributeConfig) partConfig);
		}
		if (!isNew) {
			// A new property is found. Need resort to have correct order.
			needsResort = true;
		}
		return needsResort;
	}

	private boolean handleEndConfig(final TLStructuredType type, final boolean isNew,
			final AssociationConfig.EndConfig endConfig) {
		if (!(type instanceof TLAssociation)) {
			log().error("End configuration for a non TLAssociation " + type);
			return false;
		}
		// In next phase all TLTypes exists.
		_schedule.createAssociationEnd(new Runnable() {

			@Override
			public void run() {
				if (!isNew) {
					if (type.getPart(endConfig.getName()) != null) {
						log().info("Type '" + type + "' already has an end '" + endConfig.getName() + "'.", Log.WARN);
						return;
					}
				}
				// Create destination end
				TLType targetType;
				try {
					targetType = lookupAttributeType(type, endConfig);
				} catch (ConfigurationException ex) {
					log().error("Unable to determine target type for association end: " + ex.getMessage(), ex);
					return;
				}
				addAssociationEnd((TLAssociation) type, endConfig, targetType);
			}

		});
		return true;
	}

	void resort(TLStructuredType type, AttributedTypeConfig typeConfig) {
		Collection<PartConfig> attributes = typeConfig.getAttributes();
		final Map<String, Integer> attributeIndex = MapUtil.newMap(attributes.size());
		int index = 0;
		for (PartConfig attribute : attributes) {
			attributeIndex.put(attribute.getName(), index++);
		}
		List<TLStructuredTypePart> localParts = new ArrayList<>(type.getLocalParts());
		Collections.sort(localParts, new Comparator<TLStructuredTypePart>() {
			@Override
			public int compare(TLStructuredTypePart o1, TLStructuredTypePart o2) {
				String name1 = o1.getName();
				String name2 = o2.getName();
				Integer index1 = attributeIndex.get(name1);
				Integer index2 = attributeIndex.get(name2);
				if (index1 == null || index2 == null) {
					return name1.compareTo(name2);
				} else {
					return index1.compareTo(index2);
				}
			}
		});
		if (!localParts.isEmpty() && !(localParts.get(0) instanceof TransientObject)) {
			OrderedLinkUtil.updateIndices(localParts, KBBasedMetaAttribute.OWNER_REF_ORDER_ATTR);
		} else {
			type.getLocalParts().clear();
			((Collection) type.getLocalParts()).addAll(localParts);
		}
	}

	private boolean handleReferenceConfig(final TLClass type, final boolean isNew,
			final ReferenceConfig referenceConfig) {
		final String otherEndName = referenceConfig.getInverseReference();
		ReferenceKind kind = referenceConfig.getKind();

		if (!referenceConfig.getEndName().isEmpty()) {
			// in next phase all TLTypes exists.
			/* All TLTypes exists, but ends are created in next phase. Therefore resolving the
			 * configured end can only happen in next phase. */
			_schedule.createReference(new Runnable() {
				@Override
				public void run() {
					if (!isNew) {
						if (type.getPart(referenceConfig.getName()) != null) {
							log().info(
								"Type '" + type + "' already has an reference '" + referenceConfig.getName() + "'.",
								Log.WARN);
							return;
						}
					}

					TLStructuredTypePart end;
					try {
						TLModule module = type.getModule();
						end = TLModelUtil.findPart(module, referenceConfig.getEndName());
					} catch (TopLogicException ex) {
						log().error(referenceConfig.getEndName() + " in " + referenceConfig.location()
							+ " could not be resolved to a valid type part.", ex);
						return;
					}
					if (!(end instanceof TLAssociationEnd)) {
						log().error("Configured end " + end + " is not a " + TLAssociationEnd.class.getName());
						return;
					}

					addReference(type, referenceConfig, (TLAssociationEnd) end);
				}
			});
			return true;
		} else if (kind == ReferenceKind.FORWARDS || (kind == ReferenceKind.NONE && otherEndName.isEmpty())) {
			if (referenceConfig.isOverride()) {
				_schedule.createReferenceOverride(type, () -> createForwardsRefOverride(type, referenceConfig, isNew));
			} else {
				_schedule.createReference(() -> createForwardsRef(type, referenceConfig, isNew));
			}
			return true;
		} else {
			if (referenceConfig.isOverride()) {
				_schedule.createBackReferenceOverride(type, () -> createBackwardsRefOverride(type, referenceConfig, isNew));
			} else {
				_schedule.createBackReference(() -> createBackwardsRef(type, referenceConfig, otherEndName, isNew));
			}
			return true;
		}
	}

	private void createBackwardsRefOverride(TLClass type, ReferenceConfig referenceConfig, boolean isNew) {
		if (!isNew) {
			String referenceName = referenceConfig.getName();
			if (type.getLocalClassParts()
				.stream()
				.map(TLNamedPart::getName)
				.anyMatch(name -> name.equals(referenceName))) {
				log().info("Type '" + type + "' already has a reference '" + referenceName + "'.", Log.WARN);
				return;
			}
		}

		TLReference reference = (TLReference) type.getPart(referenceConfig.getName());
		TLAssociationEnd otherEnd = TLModelUtil.getOtherEnd(reference.getEnd());
		String otherEndName = otherEnd.getName();

		TLType sourceType;
		try {
			sourceType = lookupAttributeType(type, referenceConfig);
		} catch (ConfigurationException ex) {
			log().error("Unable to determine target type for back reference: " + ex.getMessage(), ex);
			return;
		}

		String associationName = TLStructuredTypeColumns.syntheticAssociationName(sourceType.getName(), otherEndName);
		TLModule module = type.getModule();

		TLType associationType = module.getType(associationName);
		TLAssociationEnd sourceEnd;
		if (associationType == null) {
			log().error("No association " + TLModelUtil.qualifiedName(module.getName(), associationName)
					+ " found for overridden backward reference found. The forwards reference must also be overridden in order to be symetrical.");
			TLAssociation association = TLModelUtil.addAssociation(module, type.getScope(), associationName);

			// Add source end
			sourceEnd =
					TLModelUtil.addEnd(association, TLStructuredTypeColumns.SELF_ASSOCIATION_END_NAME, sourceType);
			sourceEnd.setMultiple(true);

			// Create destination end *after* self reference
			TLModelUtil.addEnd(association, otherEndName, type);
		} else {
			List<TLAssociationEnd> ends = TLModelUtil.getEnds((TLAssociation) associationType);
			if (ends.isEmpty()) {
				log().error("No ends found for " + associationType);
			}

			TLAssociationEnd end = ends.get(0);
			if (TLStructuredTypeColumns.SELF_ASSOCIATION_END_NAME.equals(end.getName())) {
				sourceEnd = end;
			} else {
				sourceEnd = TLModelUtil.getOtherEnd(end);
			}
		}

		try {
			addReference(type, referenceConfig, sourceEnd);
		} catch (IllegalArgumentException ex) {
			log().error(
				"In back reference '" + referenceConfig.getName()
						+ "', association end could not be implemented by reference in type '"
						+ TLModelUtil.qualifiedName(type) + "' in '" + referenceConfig.location() + "'.",
				ex);
			return;
		}
	}

	private void createBackwardsRef(TLClass type, ReferenceConfig referenceConfig, String otherEndName, boolean isNew) {
		if (!isNew) {
			if (type.getPart(referenceConfig.getName()) != null) {
				log().info("Type '" + type + "' already has a reference '" + referenceConfig.getName() + "'.",
					Log.WARN);
				return;
			}
		}

		TLType sourceType;
		try {
			sourceType = lookupAttributeType(type, referenceConfig);
		} catch (ConfigurationException ex) {
			log().error("Unable to determine target type for back reference: " + ex.getMessage(), ex);
			return;
		}
		if (!(sourceType instanceof TLClass)) {
			if (sourceType == null) {
				log().error("No type found for back reference '" + referenceConfig.getName() + "' at '"
						+ referenceConfig.location() + "'.");
			} else {
				log().error("In back reference '" + referenceConfig.getName() + "', type '"
						+ sourceType.getName() + "' is not a TLClass at '" + referenceConfig.location() + "'.");
			}
			return;
		}

		TLReference destinationRef = (TLReference) ((TLClass) sourceType).getPart(otherEndName);
		if (destinationRef == null) {
			// Reference not found.
			log().error("In back reference '" + referenceConfig.getName() + "', destination reference '"
					+ otherEndName + "' in type '" + qualifiedName(sourceType) + "' not found in '"
					+ referenceConfig.location() + "'.");
			return;
		}

		TLAssociationEnd destinationEnd = destinationRef.getEnd();

		TLType destinationType = destinationEnd.getType();
		if (!(destinationType instanceof TLClass)) {
			log().error("In back reference " + referenceConfig.getName() + "', destination type '"
					+ destinationType + "' of corresponding forward reference '" + otherEndName
					+ "' is not a class in '" + referenceConfig.location() + "'.");
			return;
		}

		TLAssociationEnd sourceEnd = TLModelUtil.getOtherEnd(destinationEnd);
		try {
			addReference(type, referenceConfig, sourceEnd);
		} catch (IllegalArgumentException ex) {
			log().error(
				"In back reference '" + referenceConfig.getName()
						+ "', associtiation end could not be implemented by reference in type '"
						+ TLModelUtil.qualifiedName(type) + "' in '" + referenceConfig.location() + "'.",
				ex);
			return;
		}
	}

	private void createForwardsRefOverride(TLClass type, ReferenceConfig referenceConfig, boolean isNew) {
		if (!isNew) {
			String referenceName = referenceConfig.getName();
			if (type.getLocalClassParts()
				.stream()
				.map(TLNamedPart::getName)
				.anyMatch(name -> name.equals(referenceName))) {
				log().info("Type '" + type + "' already has a reference '" + referenceName + "'.", Log.WARN);
				return;
			}
		}
		createForwardsRef(type, referenceConfig);
	}

	private void createForwardsRef(TLClass type, ReferenceConfig referenceConfig, boolean isNew) {
		if (!isNew) {
			if (type.getPart(referenceConfig.getName()) != null) {
				log().info("Type '" + type + "' already has a reference '" + referenceConfig.getName() + "'.",
					Log.WARN);
				return;
			}
		}
		createForwardsRef(type, referenceConfig);
	}

	private void createForwardsRef(TLClass type, ReferenceConfig referenceConfig) {
		String referenceName = referenceConfig.getName();
		String associationName = TLStructuredTypeColumns.syntheticAssociationName(type.getName(), referenceName);
		TLModule module = type.getModule();

		TLType associationType = module.getType(associationName);
		if (associationType == null) {
			TLReference orig;
			if (referenceConfig.isOverride()) {
				TLStructuredTypePart overriding = type.getPart(referenceName);
				TLStructuredTypePart definition = overriding.getDefinition();
				orig = (TLReference) definition;
			} else {
				orig = null;
			}

			TLAssociation association = TLModelUtil.addAssociation(module, type.getScope(), associationName);

			HistoryType historyType;
			boolean composite;
			boolean canNavigate;
			if (orig == null) {
				historyType = referenceConfig.getHistoryType();
				composite = referenceConfig.isComposite();
				canNavigate = referenceConfig.canNavigate();
			} else {
				historyType = orig.getHistoryType();
				composite = orig.isComposite();
				canNavigate = orig.getEnd().canNavigate();
			}

			boolean isCurrent = historyType == HistoryType.CURRENT;

			// Add source end
			TLAssociationEnd sourceEnd = TLModelUtil.addEnd(association, TLStructuredTypeColumns.SELF_ASSOCIATION_END_NAME, type);
			sourceEnd.setMultiple(!composite);
			sourceEnd.setAggregate(composite);
			sourceEnd.setAbstract(referenceConfig.isAbstract());
			
			// Create destination end
			TLType targetType;
			try {
				targetType = lookupAttributeType(type, referenceConfig);
			} catch (ConfigurationException | TopLogicException ex) {
				log().error("Unable to determine target type " + referenceConfig.getTypeSpec() + " for reference "
					+ TLModelUtil.qualifiedTypePartName(type, referenceName),
					ex);
				return;
			}
			TLAssociationEnd destEnd = TLModelUtil.addEnd(association, referenceName, targetType);

			if (isCurrent) {
				// Only current references may be composites
				destEnd.setComposite(composite);
			} else {
				destEnd.setComposite(false);
			}
			destEnd.setAggregate(false);
			destEnd.setNavigate(canNavigate);
			destEnd.setHistoryType(historyType);
			applyMultiplicity(referenceConfig, destEnd);

			if (orig != null) {
				sourceEnd.setDefinition(orig.getEnd());
				destEnd.setDefinition(orig.getOppositeEnd());
			}

			// Add destination reference
			addReference(type, referenceConfig, destEnd);
		} else {
			log().info("Module '" + module + "' already contains a type with name '" + associationName
				+ "', skipping creation of association.");
		}
	}

	/**
	 * Adds a new {@link TLAssociationEnd} to the given {@link TLAssociation}.
	 * 
	 * @param owner
	 *        Owner of the new {@link TLAssociationEnd}.
	 * @param endConfig
	 *        Definition of the new {@link TLAssociationEnd}.
	 * @param targetType
	 *        {@link TLAssociationEnd#getType() Target type} of the end.
	 */
	protected TLAssociationEnd addAssociationEnd(TLAssociation owner, AssociationConfig.EndConfig endConfig, TLType targetType) {
		String attributeName = endConfig.getName();
		TLAssociationEnd newEnd = TLModelUtil.addEnd(owner, attributeName, targetType);
		installConfiguration(newEnd, endConfig);
		return newEnd;
	}

	/**
	 * Adds a new {@link TLReference} to the given {@link TLClass}.
	 * 
	 * @param owner
	 *        Owner of the new {@link TLReference}.
	 * @param referenceConfig
	 *        Definition of the new {@link TLReference}.
	 * @param associationEnd
	 *        {@link TLAssociationEnd} to implement by the new {@link TLReference}.
	 */
	protected TLReference addReference(TLClass owner, ReferenceConfig referenceConfig, TLAssociationEnd associationEnd) {
		String partName = referenceConfig.getName();
		TLReference newReference = TLModelUtil.addReference(owner, partName, associationEnd);
		configureClassPart(newReference, referenceConfig);
		return newReference;
	}

	private abstract static class AbstractCompletion implements Runnable {

		private final TLModule _module;

		private final TLScope _scopeInstance;

		public AbstractCompletion(TLModule module, TLScope scopeInstance) {
			_module = module;
			_scopeInstance = scopeInstance;
		}

		/**
		 * The {@link TLModel} to assign created types to.
		 */
		public TLModule getModule() {
			return _module;
		}

		/**
		 * The {@link TLScope} to create something in.
		 */
		public TLScope getScope() {
			return _scopeInstance;
		}

	}

	private class NewAssociation extends AbstractCompletion {

		private final TLAssociation _type;

		private final AssociationConfig _config;

		public NewAssociation(TLModule module, TLScope scopeInstance, TLAssociation type,
				AssociationConfig typeConfig) {
			super(module, scopeInstance);
			_type = type;
			_config = typeConfig;
		}

		AssociationConfig getConfig() {
			return _config;
		}

		TLAssociation getType() {
			return _type;
		}

		@Override
		public void run() {
			TLStructuredType type = getType();
			AssociationConfig typeConfig = getConfig();

			List<ExtendsConfig> subsets = typeConfig.getSubsets();
			if (!subsets.isEmpty()) {
				for (ExtendsConfig subsetConfig : subsets) {
					String superTypeName = subsetConfig.getTypeName();
					String superScopeRef = subsetConfig.getScopeRef();

					String moduleName = subsetConfig.getModuleName();
					if (moduleName == null) {
						moduleName = getModule().getName();
					}
					TLType subset =
						lookupType(getScope(), superScopeRef, moduleName, superTypeName);
					if (subset == null) {
						log().error("Cannot find configured subset '" + superTypeName + "' of '"
							+ typeConfig.getName() + "' in scope '" + superScopeRef + "' defined in '"
							+ typeConfig.location() + "'.");
						continue;
					}

					if (!(subset instanceof TLAssociation)) {
						log().error("Configured subset '" + superTypeName + "' of '"
							+ typeConfig.getName() + "' in scope '" + superScopeRef + "' defined in '"
							+ typeConfig.location() + "' is not a TLAssociation.");
						continue;

					}
					addUnion((TLAssociation) subset, (TLAssociation) type);
				}

			}

			addParts(type, typeConfig, true);
		}

		private void addUnion(TLAssociation type, TLAssociation unionType) {
			if (unionType instanceof PersistentAssociation && type instanceof PersistentAssociation) {
				((PersistentAssociation) unionType).addUnionPart((PersistentAssociation) type);
			} else {
				type.getUnions().add(unionType);
			}
		}

	}

	private class NewType extends AbstractCompletion {

		private final TLClass _type;

		private final ObjectTypeConfig _config;

		public NewType(TLModule module, TLScope scopeInstance, TLClass type, ObjectTypeConfig typeConfig) {
			super(module, scopeInstance);
			_type = type;
			_config = typeConfig;
		}

		ObjectTypeConfig getConfig() {
			return _config;
		}

		TLClass getType() {
			return _type;
		}

		@Override
		public void run() {
			TLClass type = getType();
			ObjectTypeConfig typeConfig = getConfig();

			List<ExtendsConfig> generalizations = typeConfig.getGeneralizations();
			{
				List<TLClass> superTypes = new ArrayList<>();
				for (ExtendsConfig generalizationConfig : generalizations) {
					String superTypeName = generalizationConfig.getTypeName();
					String superScopeRef = generalizationConfig.getScopeRef();

					String moduleName = generalizationConfig.getModuleName();
					if (moduleName == null) {
						moduleName = getModule().getName();
					}
					TLClass superType = (TLClass)
					lookupType(getScope(), superScopeRef, moduleName, superTypeName);
					if (superType != null && !superTypes.contains(superType)) {
						superTypes.add(superType);
					}
				}

				TLModelUtil.setGeneralizations(type, superTypes);
			}
		}

	}

	private class CreateParts extends AbstractCompletion {

		private final TLStructuredType _type;

		private final AttributedTypeConfig _typeConfig;

		private final boolean _isNew;

		public CreateParts(TLModule module, TLScope scopeInstance, TLStructuredType type,
				AttributedTypeConfig typeConfig, boolean isNew) {
			super(module, scopeInstance);

			_type = type;
			_typeConfig = typeConfig;
			_isNew = isNew;
		}

		@Override
		public void run() {
			addParts(_type, _typeConfig, _isNew);
		}

	}

	/**
	 * Applies the given configuration to the given type part.
	 * @param part
	 *        The type part to modify.
	 * @param config
	 *        The configuration to apply.
	 */
	public void installConfiguration(TLStructuredTypePart part, PartConfig config) {
		if (part.getModelKind() == ModelKind.REFERENCE) {
			TLReference reference = (TLReference) part;
			TLAssociationEnd end = reference.getEnd();
			EndAspect endConfig = (EndAspect) config;

			if (TLModelUtil.isForwardReference(reference)) {
				// Defaults already applied.
			} else {
				TLAssociationEnd forwardsEnd = TLModelUtil.getOtherEnd(end);
				boolean backOfComposition = forwardsEnd.isComposite();
				if (backOfComposition) {
					// Makes no sense to change multiplicity.
				} else {
					applyMultiplicity(config, end);
				}
				end.setNavigate(endConfig.canNavigate());
			}
		} else {
			applyMultiplicity(config, part);
		}
		addTypePartAnnotations(part, part.isOverride(), config);
	}

	private static void applyMultiplicity(PartConfig config, TLStructuredTypePart part) {
		part.setMandatory(config.getMandatory());
		part.setAbstract(config.isAbstract());

		boolean multiple = config.isMultiple();
		part.setMultiple(multiple);
		if (multiple) {
			part.setOrdered(config.isOrdered());
			part.setBag(config.isBag());
		}
	}

	/**
	 * Retrieves the target type specified by an {@link AttributeConfig}.
	 */
	public static TLType lookupAttributeType(TLStructuredType owner, PartConfig config) throws ConfigurationException {
		String typeSpec = config.getTypeSpec();
		if (typeSpec.isEmpty()) {
			return null;
		}
		int moduleSep = typeSpec.indexOf(TLModelUtil.QUALIFIED_NAME_SEPARATOR);

		if (moduleSep >= 0) {
			return TLModelUtil.findType(owner.getModel(), typeSpec);
		}

		TLType type = owner.getModule().getType(typeSpec);
		if (type == null) {
			throw new ConfigurationException(
				"Undefined type '" + typeSpec + "' in module '" + owner.getModule().getName()
					+ "' used in attribute '" + owner + TLModelUtil.QUALIFIED_NAME_PART_SEPARATOR + config.getName()
					+ "' at '" + config.location() + "'.");
		}
		return type;
	}

	/**
	 * Instantiates the given {@link ModelConfig} in the current {@link #getModel()}.
	 */
	public void createModel(ModelConfig modelConfig) {
		for (ModuleConfig moduleConf : modelConfig.getModules()) {
			createModule(moduleConf);
		}
	}

	/**
	 * Instantiates the given {@link ModuleConfig} in the current {@link #getModel()}.
	 */
	public TLModule createModule(ModuleConfig moduleConf) {
		String moduleName = moduleConf.getName();
		TLModule module = TLModelUtil.makeModule(_model, moduleName);

		setupScope(module, module, moduleConf);

		scheduleRoleCreation(module);

		TLSingletons singletons = module.getAnnotation(TLSingletons.class);
		if (singletons != null) {
			for (SingletonConfig singleton : singletons.getSingletons()) {
				scheduleSingletonCreation(module, singleton);
			}
		}
		DynamicModelService.addTLObjectExtension(moduleConf);
		
		return module;
	}

	private void scheduleRoleCreation(TLModule module) {
		if (getFactory() == null) {
			return;
		}

		_schedule.createRole(() -> {
			TLRoleDefinitions roleDefinitions = module.getAnnotation(TLRoleDefinitions.class);
			if (roleDefinitions == null) {
				return;
			}

			Collection<RoleConfig> roleConfigs = roleDefinitions.getRoles();
			if (roleConfigs.isEmpty()) {
				return;
			}

			for (RoleConfig roleConfig : roleConfigs) {
				createRole(module, roleConfig);
			}
		});
	}

	private void scheduleSingletonCreation(TLModule module, SingletonConfig singleton) {
		_schedule.createSingleton(() -> createSingleton(module, singleton));
	}

	/**
	 * Creates a singleton with the given configuration in the given module.
	 */
	public void createSingleton(TLModule module, SingletonConfig singleton) {
		if (getFactory() == null) {
			return;
		}

		String name = singleton.getName();
		TLObject root = module.getSingleton(name);
		if (root != null) {
			return;
		}

		String typeName = singleton.getTypeSpec();

		TLClass type;
		try {
			if (typeName.indexOf(TLModelUtil.QUALIFIED_NAME_SEPARATOR) >= 0) {
				type = (TLClass) TLModelUtil.findType(module.getModel(), typeName);
			} else {
				type = (TLClass) TLModelUtil.findType(module, typeName);
			}
		} catch (TopLogicException ex) {
			log().error("Cannot resolve type '" + typeName + "' of singleton '" + name + "' in module '"
				+ module.getName() + "'.", ex);
			return;
		}

		root = getFactory().createObject(type);
		module.addSingleton(name, root);

		setupRoles(module, singleton);
	}
	
	private void setupRoles(TLModule module, SingletonConfig singleton) {
		for (RoleAssignment assignment : singleton.getRoleAssignments()) {
			for (String roleName : assignment.getRoles()) {
				BoundedRole role = BoundedRole.getDefinedRole(module, roleName);
				if (role == null) {
					log().error("Role '" + roleName + "' used in assignment at '" + assignment.location()
							+ "' is not defined.");
				}

				Group group = Group.getGroupByName(assignment.getGroup());
				if (group == null) {
					log().error("Reference to undefined group '" + assignment.getGroup() + "' in assignment at '"
						+ assignment.location() + "'.");
				}

				BoundedRole.assignRole(module.getSingleton(singleton.getName()), group, role);
			}
		}
	}

	/**
	 * Creates the {@link BoundedRole} from the given configuration in the given {@link TLModule}.
	 */
	public BoundedRole createRole(TLModule scope, RoleConfig role) {
		if (scope.tTransient()) {
			return null;
		}
		String name = role.getName();
		BoundedRole existingRole = BoundedRole.getDefinedRole(scope, name);
		if (existingRole != null) {
			log().info("Role '" + name + "' already exists in module '" + scope + "'.");
			return existingRole;
		}

		BoundedRole newRole = BoundedRole.createBoundedRole(name, scope.tHandle().getKnowledgeBase());
		newRole.setIsSystem(true);

		newRole.setValue(BoundedRole.ATTRIBUTE_DESCRIPTION,
			Resources.getInstance().getString(I18NConstants.ROLE_DESCRIPTION.key(name), ""));

		newRole.bind(scope);
		return newRole;
	}

	/**
	 * Create a new property on the given {@link TLStructuredType}.
	 * 
	 * @param type
	 *        The type to modify.
	 * @param config
	 *        additional value which depends on the attribute type
	 * @return The created property.
	 * @throws IllegalArgumentException
	 *         if one of the params does not match the constraints
	 */
	public TLProperty createProperty(TLStructuredType type, AttributeConfig config) {
		if (type == null) {
			throw new IllegalArgumentException("Type must not be null!");
		}

		TLType contentType;
		try {
			contentType = lookupAttributeType(type, config);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}

		String partName = config.getName();
		TLProperty newProperty = TLModelUtil.addProperty(type, partName, contentType);
		if (type instanceof TLAssociation) {
			installConfiguration(newProperty, config);
			return newProperty;
		}
		configureClassPart((TLClassPart) newProperty, config);
		return newProperty;
	}

	private void configureClassPart(TLClassPart classPart, PartConfig partConfig) {
		boolean isDeclaredOverride = partConfig.isOverride();
		boolean isActualOverride = classPart.isOverride();
		if (isDeclaredOverride) {
			if (isActualOverride) {
				checkStorageImplementation(partConfig, classPart);
			} else {
				errorNoOverride(classPart);
			}
			checkOverride(partConfig, classPart);
			addTypePartAnnotations(classPart, true, partConfig);
		} else {
			if (isActualOverride) {
				errorUndeclaredOverride(classPart);
			}
			installConfiguration(classPart, partConfig);
		}
	}

	private void errorUndeclaredOverride(TLClassPart classPart) {
		TLClassPart conflict = getFirst(getOverriddenParts(classPart));
		String message = "Override failed. Undeclared override of part '" + qualifiedName(conflict)
			+ "' through " + qualifiedName(classPart) + ".";
		log().error(message, new ConfigurationError(message));
	}

	private void errorNoOverride(TLClassPart classPart) {
		String message = "Override failed. " + qualifiedName(classPart)
			+ "' is declared to be an override, but is not overriding anything.";
		log().error(message, new ConfigurationError(message));
	}

	private void checkOverride(PartConfig partConfig, TLClassPart classPart) {
		for (PropertyDescriptor property : partConfig.descriptor().getProperties()) {
			String propertyName = property.getPropertyName();
			if (partConfig.valueSet(property) && !PROPERTIES_FOR_OVERRIDES.contains(propertyName)) {
				errorPropertyNotAllowedInOverride(classPart, propertyName, partConfig.value(property));
			}
		}
	}

	private void checkStorageImplementation(PartConfig partConfig, TLClassPart classPart) {
		TLStorage storageAnnotation = partConfig.getAnnotation(TLStorage.class);
		if (partConfig.isAbstract() && storageAnnotation != null) {
			String message = "Storage implementation (" + TLStorage.TAG_NAME + ") is not allowed for abstract '"
					+ classPart + "': " + partConfig;
			log().error(message);
		}

		if (getOverriddenParts(classPart).stream().allMatch(TLStructuredTypePart::isAbstract)) {
			// its ok when no storage implementation is given because there is a default.
			return;
		}
		if (storageAnnotation != null) {
			StorageImplementation storage = TypedConfigUtil.createInstance(storageAnnotation.getImplementation());
			assert storage != null : "Implementation is mandatory in configuration '" + partConfig + "'.";
			if (!storage.isReadOnly()) {
				String message = "Storage implementation (" + TLStorage.TAG_NAME
						+ ") is only allowed for derived storages in overridden property '" + classPart + "': "
						+ partConfig;
				log().error(message);
			}
		}
		if (partConfig instanceof ReferenceConfig) {
			if (((ReferenceConfig) partConfig).getKind() == ReferenceKind.BACKWARDS) {
				String message = "Re-declaration of non-abstract backwards reference '" + classPart + "' not allowed.";
				log().error(message);
			}
		}
		if (partConfig.isAbstract()) {
			String message =
				"Part '" + classPart + "' can not be declared abstract, because it overrides a non-abstract part.";
			log().error(message);
		}
	}


	private void errorPropertyNotAllowedInOverride(TLClassPart classPart, String propertyName, Object value) {
		String message = "Override failed. Property '" + propertyName + "' is not allowed in overrides."
			+ " TLClassPart: " + classPart + ". Property value: " + debug(value);
		log().error(message, new ConfigurationError(message));
	}

	private void addTypePartAnnotations(TLTypePart typePart, boolean isOverride, AnnotatedConfig<?> config) {

		TLType targetType = typePart.getType();
		TLTypeKind targetKind = TLTypeKind.getTLTypeKind(typePart);
		for (TLAnnotation annotation : config.getAnnotations()) {
			Class<? extends TLAnnotation> annotationClass = annotation.getConfigurationInterface();
			TargetType targetTypeAnnotation = annotationClass.getAnnotation(TargetType.class);
			if (targetTypeAnnotation != null) {
				TLTypeKind[] allowedTypes = targetTypeAnnotation.value();
				if (ArrayUtil.indexOf(targetKind, allowedTypes) < 0) {
					log().error("Annotation " + annotation + " at part " + typePart + " is not allowed for kind '"
						+ targetKind + "' of target type " + targetType + ". Allowed kinds: "
						+ Arrays.toString(allowedTypes));
					continue;
				}
				String[] expectedSuperTypeNames = targetTypeAnnotation.name();
				if (expectedSuperTypeNames.length > 0 && !isAnyCompatible(expectedSuperTypeNames, targetType)) {
					log().error("Annotation " + annotation + " at part " + typePart + " is not allowed for target type "
						+ targetType + ". Only subtypes of " + Arrays.toString(expectedSuperTypeNames) + " allowed.");
					continue;
				}
			}
			if (isOverride && AnnotationInheritance.Policy.getInheritancePolicy(annotationClass) == Policy.FINAL) {
				log().error("Annotation " + annotation + " at part " + typePart
					+ " is not allowed, because part overrides another part.");
				continue;
			}
			setAnnotation(typePart, annotation);
		}
	}

	private static boolean isAnyCompatible(String[] expectedTypeNames, TLType actualType) {
		for (String expectedTypeName : expectedTypeNames) {
			if (isCompatible(expectedTypeName, actualType)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isCompatible(String expectedTypeName, TLType actualType) {
		try {
			TLType expectedType = TLModelUtil.findType(actualType.getModel(), expectedTypeName);
			return TLModelUtil.isCompatibleType(expectedType, actualType);
		} catch (TopLogicException ex) {
			return false;
		}
	}

	private static void addAnnotations(TLModelPart modelPart, AnnotatedConfig<?> config) {
		for (TLAnnotation annotation : (Collection<? extends TLAnnotation>) config.getAnnotations()) {
			setAnnotation(modelPart, annotation);
		}
	}

	private static void setAnnotation(TLModelPart modelPart, TLAnnotation annotation) {
		modelPart.setAnnotation(TypedConfiguration.copy(annotation));
	}
	
}
