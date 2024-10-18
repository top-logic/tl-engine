/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.diff.apply;

import static com.top_logic.basic.config.TypedConfiguration.*;
import static com.top_logic.basic.config.misc.TypedConfigUtil.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.top_logic.basic.Log;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.dob.schema.config.DBColumnType;
import com.top_logic.element.config.AssociationConfig;
import com.top_logic.element.config.AssociationConfig.EndConfig;
import com.top_logic.element.config.AttributeConfig;
import com.top_logic.element.config.ClassConfig;
import com.top_logic.element.config.EndAspect;
import com.top_logic.element.config.ExtendsConfig;
import com.top_logic.element.config.InterfaceConfig;
import com.top_logic.element.config.ModuleConfig;
import com.top_logic.element.config.ObjectTypeConfig;
import com.top_logic.element.config.PartConfig;
import com.top_logic.element.config.ReferenceConfig;
import com.top_logic.element.config.ReferenceConfig.ReferenceKind;
import com.top_logic.element.config.SingletonConfig;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.model.ModelResolver;
import com.top_logic.element.model.diff.config.AddAnnotations;
import com.top_logic.element.model.diff.config.AddGeneralization;
import com.top_logic.element.model.diff.config.CreateClassifier;
import com.top_logic.element.model.diff.config.CreateModule;
import com.top_logic.element.model.diff.config.CreateRole;
import com.top_logic.element.model.diff.config.CreateSingleton;
import com.top_logic.element.model.diff.config.CreateStructuredTypePart;
import com.top_logic.element.model.diff.config.CreateType;
import com.top_logic.element.model.diff.config.Delete;
import com.top_logic.element.model.diff.config.DeleteRole;
import com.top_logic.element.model.diff.config.DiffElement;
import com.top_logic.element.model.diff.config.MakeAbstract;
import com.top_logic.element.model.diff.config.MakeConcrete;
import com.top_logic.element.model.diff.config.MoveClassifier;
import com.top_logic.element.model.diff.config.MoveGeneralization;
import com.top_logic.element.model.diff.config.MoveStructuredTypePart;
import com.top_logic.element.model.diff.config.RemoveAnnotation;
import com.top_logic.element.model.diff.config.RemoveGeneralization;
import com.top_logic.element.model.diff.config.RenamePart;
import com.top_logic.element.model.diff.config.SetAnnotations;
import com.top_logic.element.model.diff.config.UpdateMandatory;
import com.top_logic.element.model.diff.config.UpdatePartType;
import com.top_logic.element.model.diff.config.UpdateStorageMapping;
import com.top_logic.element.model.diff.config.visit.DiffVisitor;
import com.top_logic.element.model.migration.ChangeAttributeTargetType;
import com.top_logic.element.model.migration.ChangeAttributeTargetType.ChangeRefConfig;
import com.top_logic.element.model.migration.model.AbstractCreateTypePartProcessor;
import com.top_logic.element.model.migration.model.AbstractEndAspectProcessor;
import com.top_logic.element.model.migration.model.AddTLAnnotations;
import com.top_logic.element.model.migration.model.AddTLClassGeneralization;
import com.top_logic.element.model.migration.model.CreateInverseTLReferenceProcessor;
import com.top_logic.element.model.migration.model.CreateTLAssociationEndProcessor;
import com.top_logic.element.model.migration.model.CreateTLAssociationProcessor;
import com.top_logic.element.model.migration.model.CreateTLClassProcessor;
import com.top_logic.element.model.migration.model.CreateTLClassifierProcessor;
import com.top_logic.element.model.migration.model.CreateTLDatatypeProcessor;
import com.top_logic.element.model.migration.model.CreateTLEnumerationProcessor;
import com.top_logic.element.model.migration.model.CreateTLModuleProcessor;
import com.top_logic.element.model.migration.model.CreateTLObjectProcessor;
import com.top_logic.element.model.migration.model.CreateTLPropertyProcessor;
import com.top_logic.element.model.migration.model.CreateTLReferenceProcessor;
import com.top_logic.element.model.migration.model.CreateTLSingletonProcessor;
import com.top_logic.element.model.migration.model.DeleteTLClassProcessor;
import com.top_logic.element.model.migration.model.DeleteTLDatatypeProcessor;
import com.top_logic.element.model.migration.model.DeleteTLEnumerationProcessor;
import com.top_logic.element.model.migration.model.DeleteTLModuleProcessor;
import com.top_logic.element.model.migration.model.DeleteTLPropertyProcessor;
import com.top_logic.element.model.migration.model.DeleteTLReferenceProcessor;
import com.top_logic.element.model.migration.model.MarkTLTypePartOverride;
import com.top_logic.element.model.migration.model.RemoveTLAnnotations;
import com.top_logic.element.model.migration.model.RemoveTLClassGeneralization;
import com.top_logic.element.model.migration.model.ReorderTLClassGeneralization;
import com.top_logic.element.model.migration.model.ReorderTLTypePart;
import com.top_logic.element.model.migration.model.SetDefaultTLClassifierProcessor;
import com.top_logic.element.model.migration.model.UpdateTLAnnotations;
import com.top_logic.element.model.migration.model.UpdateTLAssociationEndProcessor;
import com.top_logic.element.model.migration.model.UpdateTLClassProcessor;
import com.top_logic.element.model.migration.model.UpdateTLDataTypeProcessor;
import com.top_logic.element.model.migration.model.UpdateTLPropertyProcessor;
import com.top_logic.element.model.migration.model.UpdateTLReferenceProcessor;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
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
import com.top_logic.model.TLProperty;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLScope;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.security.RoleConfig;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.model.config.DatatypeConfig;
import com.top_logic.model.config.EnumConfig;
import com.top_logic.model.config.EnumConfig.ClassifierConfig;
import com.top_logic.model.config.TypeConfig;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.model.impl.generated.TlModelFactory;
import com.top_logic.model.migration.data.QualifiedPartName;
import com.top_logic.model.migration.data.QualifiedTypeName;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.error.TopLogicException;

/**
 * Algorithm applying {@link DiffElement}s to existing {@link TLModel} instances.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ApplyModelPatch extends ModelResolver implements DiffVisitor<Void, Void, RuntimeException> {

	private List<? super PolymorphicConfiguration<? extends MigrationProcessor>> _processors;

	/**
	 * {@link DiffVisitor} computing a priority to sort {@link DiffElement}s before applying a
	 * patch.
	 * 
	 * <p>
	 * The priority of a patch element is higher, if the returned number is smaller.
	 * </p>
	 */
	static final class DiffPriority implements DiffVisitor<Priority, Void, RuntimeException>, Comparator<DiffElement> {

		/**
		 * Singleton {@link ApplyModelPatch.DiffPriority} instance.
		 */
		public static final DiffPriority INSTANCE = new ApplyModelPatch.DiffPriority();

		private DiffPriority() {
			// Singleton constructor.
		}

		@Override
		public Priority visit(CreateModule diff, Void arg) throws RuntimeException {
			return Priority.CREATE_MODULE;
		}

		@Override
		public Priority visit(CreateSingleton diff, Void arg) throws RuntimeException {
			return Priority.CREATE_SINGLETONS;
		}

		@Override
		public Priority visit(CreateRole diff, Void arg) throws RuntimeException {
			return Priority.CREATE_ROLE;
		}

		@Override
		public Priority visit(CreateType diff, Void arg) throws RuntimeException {
			return Priority.CREATE_TYPE;
		}

		@Override
		public Priority visit(CreateStructuredTypePart diff, Void arg) throws RuntimeException {
			if (diff.getPart().isOverride()) {
				return Priority.CREATE_TYPE_PART_OVERRIDE;
			}
			return Priority.CREATE_TYPE_PART;
		}

		@Override
		public Priority visit(CreateClassifier diff, Void arg) throws RuntimeException {
			return Priority.CREATE_TYPE_PART;
		}

		@Override
		public Priority visit(UpdateStorageMapping diff, Void arg) throws RuntimeException {
			return Priority.UPDATE_STORAGE_MAPPING;
		}

		@Override
		public Priority visit(UpdatePartType diff, Void arg) throws RuntimeException {
			return Priority.UPDATE_TYPE_PART;
		}

		@Override
		public Priority visit(Delete diff, Void arg) throws RuntimeException {
			if (diff.getName().indexOf('#') > 0) {
				// A part (type part or singleton).
				return Priority.DELETE_TYPE_PART;
			}
			if (diff.getName().indexOf(':') > 0) {
				// A type.
				return Priority.DELETE_TYPE;
			}
			// A module.
			return Priority.DELETE_MODULE;
		}

		@Override
		public Priority visit(DeleteRole diff, Void arg) throws RuntimeException {
			return Priority.DELETE_ROLE;
		}

		@Override
		public Priority visit(AddAnnotations diff, Void arg) throws RuntimeException {
			return Priority.CHANGE_ANNOTATIONS;
		}

		@Override
		public Priority visit(SetAnnotations diff, Void arg) throws RuntimeException {
			return Priority.CHANGE_ANNOTATIONS;
		}

		@Override
		public Priority visit(RemoveAnnotation diff, Void arg) throws RuntimeException {
			return Priority.REMOVE_ANNOTATION;
		}

		@Override
		public Priority visit(AddGeneralization diff, Void arg) throws RuntimeException {
			return Priority.CREATE_OR_MOVE_GENERALISATION;
		}

		@Override
		public Priority visit(RemoveGeneralization diff, Void arg) throws RuntimeException {
			return Priority.REMOVE_GENERALISATION;
		}

		@Override
		public Priority visit(MoveGeneralization diff, Void arg) throws RuntimeException {
			return Priority.CREATE_OR_MOVE_GENERALISATION;
		}

		@Override
		public Priority visit(MakeAbstract diff, Void arg) throws RuntimeException {
			return Priority.CHANGE_TYPE_ABSTRACT;
		}

		@Override
		public Priority visit(MakeConcrete diff, Void arg) throws RuntimeException {
			return Priority.CHANGE_TYPE_ABSTRACT;
		}

		@Override
		public Priority visit(UpdateMandatory diff, Void arg) throws RuntimeException {
			return Priority.CHANGE_TYPE_PART_MANDATORY;
		}

		@Override
		public Priority visit(MoveClassifier diff, Void arg) throws RuntimeException {
			return Priority.MOVE_TYPE_PART;
		}

		@Override
		public Priority visit(MoveStructuredTypePart diff, Void arg) throws RuntimeException {
			return Priority.MOVE_TYPE_PART;
		}

		@Override
		public Priority visit(RenamePart diff, Void arg) throws RuntimeException {
			return Priority.MOVE_TYPE_PART;
		}

		/**
		 * Compares {@link DiffElement} by their {@link Priority}.
		 */
		@Override
		public int compare(DiffElement o1, DiffElement o2) {
			return o1.visit(this, null).compareTo(o2.visit(this, null));
		}
	}

	/**
	 * Creates a {@link ApplyModelPatch}.
	 * 
	 * @param processors
	 *        A sequence of {@link MigrationProcessor} configuration to add processors which can be
	 *        used to apply the patch in SQL. May be <code>null</code>.
	 */
	public ApplyModelPatch(Protocol log, TLModel model, TLFactory factory,
			List<? super PolymorphicConfiguration<? extends MigrationProcessor>> processors) {
		super(log, model, factory);
		_processors = processors;
	}

	/**
	 * Applies all given diffs to the given model.
	 * 
	 * @param log
	 *        Protocol to write messages to.
	 * @param model
	 *        The {@link TLModel} to adapt.
	 * @param factory
	 *        {@link TLFactory} to create new elements.
	 * @param patch
	 *        The {@link DiffElement}s to apply.
	 */
	public static void applyPatch(Protocol log, TLModel model, TLFactory factory, List<? extends DiffElement> patch) {
		applyPatch(log, model, factory, patch, null);
	}

	/**
	 * Applies all given diffs to the given model.
	 * 
	 * @param log
	 *        Protocol to write messages to.
	 * @param model
	 *        The {@link TLModel} to adapt.
	 * @param factory
	 *        {@link TLFactory} to create new elements.
	 * @param patch
	 *        The {@link DiffElement}s to apply.
	 * @param processors
	 *        Output list to fill with configurations of {@link MigrationProcessor}s that can be
	 *        used to apply the patch in SQL.
	 */
	public static void applyPatch(Protocol log, TLModel model, TLFactory factory, List<? extends DiffElement> patch,
			List<? super PolymorphicConfiguration<? extends MigrationProcessor>> processors) {
		ApplyModelPatch apply = new ApplyModelPatch(log, model, factory, processors);
		apply.applyPatch(patch);
		apply.complete();
	}

	/**
	 * Applies all given diffs to the {@link #getModel() encapsulated model}.
	 */
	public void applyPatch(List<? extends DiffElement> patch) {
		List<? extends DiffElement> elements = sortByPriority(patch);
		for (DiffElement diff : elements) {
			diff.visit(this, null);
		}
	}

	/**
	 * Creates a new list with the given {@link DiffElement}s and sorts it by priority.
	 */
	public static <E extends DiffElement> List<E> sortByPriority(Collection<E> patch) {
		List<E> elements = new ArrayList<>(patch);
		Collections.sort(elements, DiffPriority.INSTANCE);
		return elements;
	}

	@Override
	public Void visit(AddAnnotations diff, Void arg) throws RuntimeException {
		String partName = diff.getPart();
		TLModelPart part;
		try {
			part = resolvePart(partName);
		} catch (TopLogicException ex) {
			log().info("Merge conflict: Adding annotations to '" + partName + "', but part does not exist.", Log.WARN);
			return null;
		}

		for (TLAnnotation annotation : diff.getAnnotations()) {
			log().info("Adding annotation '" + annotation.getConfigurationInterface().getName() + "' to '" + partName
				+ "'.");
			part.setAnnotation(TypedConfiguration.copy(annotation));
		}
		if (createProcessors()) {
			AddTLAnnotations.Config config = newConfigItem(AddTLAnnotations.Config.class);
			config.setName(diff.getPart());
			copyAnnotations(diff, config);
			addProcessor(config);
		}
		return null;
	}

	private boolean createProcessors() {
		return _processors != null;
	}

	@Override
	public Void visit(SetAnnotations diff, Void arg) throws RuntimeException {
		String partName = diff.getPart();
		TLModelPart part;
		try {
			part = resolvePart(partName);
		} catch (TopLogicException ex) {
			log().info("Merge conflict: Setting annotations to '" + partName + "', but part does not exist.", Log.WARN);
			return null;
		}

		// Remove existing annotations.
		part.getAnnotations()
			.stream()
			.map(TLAnnotation::getConfigurationInterface)
			.collect(Collectors.toList())
			.forEach(part::removeAnnotation);

		for (TLAnnotation annotation : diff.getAnnotations()) {
			log().info("Set annotation '" + annotation.getConfigurationInterface().getName() + "' to '" + partName
					+ "'.");
			part.setAnnotation(TypedConfiguration.copy(annotation));
		}

		if (createProcessors()) {
			UpdateTLAnnotations.Config config = newConfigItem(UpdateTLAnnotations.Config.class);
			config.setName(diff.getPart());
			copyAnnotations(diff, config);
			addProcessor(config);
		}
		return null;
	}

	@Override
	public Void visit(AddGeneralization diff, Void arg) throws RuntimeException {
		TLClass target;
		try {
			target = asClass(TLModelUtil.findType(getModel(), diff.getType()));
		} catch (TopLogicException ex) {
			log().info("Merge conflict: Type '" + diff.getType() + "' for adding generalization '"
					+ diff.getGeneralization() + "' does not exist: " + ex.getMessage(),
				Log.WARN);
			return null;
		}

		TLClass before;
		try {
			before = diff.getBefore() == null ? null : asClass(TLModelUtil.findType(getModel(), diff.getBefore()));
		} catch (TopLogicException ex) {
			log().info(
				"Merge conflict: Generalization reference type '" + diff.getBefore() + "' for insert does not exist: "
						+ ex.getMessage(),
				Log.INFO);
			before = null;
		}

		TLClass generalization;
		try {
			generalization = asClass(TLModelUtil.findType(getModel(), diff.getGeneralization()));
		} catch (TopLogicException ex) {
			log().info("Merge conflict: New generalization '" + diff.getGeneralization() + "' of type '"
					+ diff.getType() + "' does not exist: " + ex.getMessage(),
				Log.WARN);
			return null;
		}

		List<TLClass> generalizations = target.getGeneralizations();
		if (generalizations.contains(generalization)) {
			log().info("Ignoring new generalization '" + diff.getGeneralization() + "' of '" + diff.getType()
				+ "': Already present.");
			return null;
		}

		log().info("Adding generalization '" + diff.getGeneralization() + "' to '" + diff.getType() + "'.");
		int index = before == null ? generalizations.size() : generalizations.indexOf(before);
		if (index < 0) {
			log().info("Referenced generalization '" + before + "' not found in '" + diff.getType() + "', appending.");
			index = 0;
		}
		generalizations.add(index, generalization);

		if (createProcessors()) {
			AddTLClassGeneralization.Config config = newConfigItem(AddTLClassGeneralization.Config.class);
			QualifiedTypeName type = qTypeName(diff.getType());
			config.setName(type);

			QualifiedTypeName generalizationName = qTypeName(diff.getGeneralization());
			config.getGeneralizations().add(newGeneralization(generalizationName));
			addProcessor(config);

			if (before != null) {
				addReorderGeneralization(type, generalizationName, diff.getBefore());
			}
		}
		return null;
	}

	@Override
	public Void visit(MoveGeneralization diff, Void arg) throws RuntimeException {
		TLClass target;
		try {
			target = asClass(TLModelUtil.findType(getModel(), diff.getType()));
		} catch (TopLogicException ex) {
			log().info("Merge conflict: Target type '" + diff.getType() + "' of moved generalization '"
				+ diff.getGeneralization() + "' does not exist.", Log.WARN);
			return null;
		}

		TLClass before;
		try {
			before = diff.getBefore() == null ? null : asClass(TLModelUtil.findType(getModel(), diff.getBefore()));
		} catch (TopLogicException ex) {
			log().info("Merge conflict: Generalization reference '" + diff.getBefore() + "' does not exist.", Log.INFO);
			before = null;
		}

		TLClass generalization;
		try {
			generalization = asClass(TLModelUtil.findType(getModel(), diff.getGeneralization()));
		} catch (TopLogicException ex) {
			log().info("Merge conflict: Moved generalization '" + diff.getGeneralization() + "' of type '"
				+ diff.getType() + "' does not exist.", Log.WARN);
			return null;
		}

		List<TLClass> generalizations = target.getGeneralizations();
		if (!generalizations.remove(generalization)) {
			log().info(
				"Merge conflict: Cannot move generalization '" + diff.getGeneralization() + "' of '" + diff.getType()
				+ "': Not present.");
			return null;
		}

		String pos;
		if (before == null) {
			pos = "to the end";
		} else {
			pos = "before generalization '" + diff.getBefore() + "'";
		}
		log().info("Moving generalization '" + diff.getGeneralization() + "' of '" + diff.getType() + "' " + pos + ".");
		int index = before == null ? generalizations.size() : generalizations.indexOf(before);

		generalizations.add(index, generalization);
		if (createProcessors()) {
			addReorderGeneralization(qTypeName(diff.getType()), qTypeName(diff.getGeneralization()), diff.getBefore());
		}
		return null;
	}

	private void addReorderGeneralization(QualifiedTypeName type, QualifiedTypeName generalization, String before) {
		assert createProcessors();
		ReorderTLClassGeneralization.Config config = newConfigItem(ReorderTLClassGeneralization.Config.class);
		config.setType(type);
		config.setGeneralization(generalization);
		if (before != null) {
			config.setBefore(qTypeName(before));
		}
		addProcessor(config);
	}

	@Override
	public Void visit(CreateModule diff, Void arg) {
		TLModule existing = getModel().getModule(diff.getModule().getName());
		if (existing != null) {
			log().info(
				"Merge conflict: Adding module, but module with same name '" + diff.getModule().getName()
					+ "' already exists.",
				Log.INFO);
		} else {
			log().info("Adding module '" + diff.getModule().getName() + "'.");
		}
		createModule(diff.getModule());
		return null;
	}

	@Override
	public Void visit(CreateSingleton diff, Void arg) throws RuntimeException {
		TLModule module;
		SingletonConfig config = diff.getSingleton();
		try {
			module = TLModelUtil.findModule(getModel(), diff.getModule());
		} catch (TopLogicException ex) {
			log().info(
				"Merge conflict: Adding singleton '" + config.getName() + "' to module '"
					+ diff.getModule()
						+ "': " + ex.getMessage(),
				Log.WARN);
			return null;
		}

		createSingleton(module, config);
		return null;
	}

	@Override
	public void createSingleton(TLModule module, SingletonConfig singleton) {
		super.createSingleton(module, singleton);
		if (getFactory() != null && createProcessors()) {
			addSingleton(module.getName(), singleton);
		}
	}

	private void addSingleton(String module, SingletonConfig singleton) {
		CreateTLSingletonProcessor.Config config = newConfigItem(CreateTLSingletonProcessor.Config.class);
		config.setModule(module);
		config.setName(singleton.getName());
		CreateTLObjectProcessor.Config singletonConf = newConfigItem(CreateTLObjectProcessor.Config.class);
		String typeSpec = singleton.getTypeSpec();
		QualifiedTypeName qTypeSpec;
		if (typeSpec.indexOf(TLModelUtil.QUALIFIED_NAME_SEPARATOR) < 0) {
			// Local name
			qTypeSpec = qTypeName(module, typeSpec);
		} else {
			qTypeSpec = qTypeName(typeSpec);
		}
		singletonConf.setType(qTypeSpec);
		singletonConf.setTable(TLAnnotations.getTable((TLType) resolvePart(qTypeSpec.getName())));
		config.setSingleton(singletonConf);
		addProcessor(config);
	}

	@Override
	public Void visit(CreateRole diff, Void arg) throws RuntimeException {
		TLModule module;
		RoleConfig config = diff.getRole();
		try {
			module = TLModelUtil.findModule(getModel(), diff.getModule());
		} catch (TopLogicException ex) {
			log().info(
				"Merge conflict: Adding role '" + config.getName() + "' to module '"
						+ diff.getModule() + "': " + ex.getMessage(),
				Log.WARN);
			return null;
		}

		log().info("Creating role '" + diff.getRole().getName() + " in module '" + diff.getModule() + "'.");
		createRole(module, config);
		return null;
	}

	@Override
	public Void visit(CreateType diff, Void arg) throws RuntimeException {
		TLModule module;
		try {
			module = TLModelUtil.findModule(getModel(), diff.getModule());
		} catch (TopLogicException ex) {
			log().info(
				"Merge conflict: Adding type '" + diff.getType().getName() + "' to module '" + diff.getModule()
						+ "': " + ex.getMessage(),
				Log.WARN);
			return null;
		}
		TLType existing = module.getType(diff.getType().getName());
		if (existing != null) {
			log().info(
				"Merge conflict: Adding type to module '" + diff.getModule() + "', but type with same name '"
					+ diff.getType().getName() + "' already exists.",
				Log.INFO);
		} else {
			log().info("Adding type '" + diff.getType().getName() + " to module '" + diff.getModule() + "'.");
		}
		addType(module, module, diff.getType());
		return null;
	}

	@Override
	protected void addObjectType(TLModule module, TLScope scope, ObjectTypeConfig config) {
		super.addObjectType(module, scope, config);

		if (createProcessors()) {
			String moduleName = module.getName();
			if (config instanceof InterfaceConfig) {
				addObjectType(moduleName, config, Boolean.TRUE, null, Collections.emptyList());
			} else if (config instanceof ClassConfig) {
				ClassConfig type = (ClassConfig) config;
				addObjectType(moduleName, config, type.isAbstract(), type.isFinal(), type.getTypes());
			} else {
				throw new UnreachableAssertion("Unexpected type config: " + config);
			}
		}
	}

	private void addObjectType(String moduleName, ObjectTypeConfig type, Boolean isAbstract, Boolean isFinal,
			Collection<TypeConfig> innerTypes) {
		QualifiedTypeName newClassName = qTypeName(moduleName, type.getName());
		CreateTLClassProcessor.Config newClass = newConfigItem(CreateTLClassProcessor.Config.class);
		newClass.setName(newClassName);
		if (isAbstract != null) {
			newClass.setAbstract(isAbstract.booleanValue());
		}
		if (isFinal != null) {
			newClass.setFinal(isFinal.booleanValue());
		}
		copyAnnotations(type, newClass);

		List<ExtendsConfig> generalizations = type.getGeneralizations();
		if (generalizations.isEmpty()) {
			newClass.setPrimaryGeneralization(qTypeName(TlModelFactory.TL_MODEL_STRUCTURE, TLObject.TL_OBJECT_TYPE));
		} else {
			/* Generalizations are created later. */
			newClass.setWithoutPrimaryGeneralization(true);

			AddTLClassGeneralization.Config generalizationsConfig =
				newConfigItem(AddTLClassGeneralization.Config.class);
			generalizationsConfig.setName(newClassName);
			for (int i = 0; i < generalizations.size(); i++) {
				QualifiedTypeName generalizationName =
					getQualifiedTypeName(moduleName, generalizations.get(i).getQualifiedTypeName());
				generalizationsConfig.getGeneralizations().add(newGeneralization(generalizationName));
			}
			schedule().createTypeHierarchy(() -> addProcessor(generalizationsConfig));
		}

		addProcessor(newClass);

		if (!innerTypes.isEmpty()) {
			log().info("Unable to create migration processors for inner types of type '" + newClassName + "'.",
				Protocol.WARN);
		}

	}

	private QualifiedTypeName getQualifiedTypeName(String moduleName, String typeName) {
		if (typeName.indexOf(TLModelUtil.QUALIFIED_NAME_SEPARATOR) >= 0) {
			return qTypeName(typeName);
		}
		return qTypeName(moduleName, typeName);
	}

	@Override
	protected void addDataType(TLModule module, TLScope scope, DatatypeConfig config) {
		super.addDataType(module, scope, config);
		if (createProcessors()) {
			addDatatypeProcessor(module.getName(), config);
		}
	}

	private void addDatatypeProcessor(String moduleName, DatatypeConfig type) {
		CreateTLDatatypeProcessor.Config config = newConfigItem(CreateTLDatatypeProcessor.Config.class);
		config.setName(qTypeName(moduleName, type.getName()));
		config.setKind(type.getKind());
		config.setStorageMapping(TypedConfiguration.copy(type.getStorageMapping()));
		copyAnnotations(type, config);
		copyDBColumn(type, config);

		addProcessor(config);
	}

	@Override
	public Void visit(UpdateStorageMapping diff, Void arg) throws RuntimeException {
		TLPrimitive target = (TLPrimitive) TLModelUtil.findType(getModel(), diff.getType());
		StorageMapping<?> storageMapping = createInstance(diff.getStorageMapping());
		target.setStorageMapping(storageMapping);
		if (createProcessors()) {
			UpdateTLDataTypeProcessor.Config config = newConfigItem(UpdateTLDataTypeProcessor.Config.class);
			config.setName(qTypeName(target.getModule().getName(), target.getName()));
			config.setStorageMapping(TypedConfiguration.copy(diff.getStorageMapping()));
			addProcessor(config);
		}
		return null;
	}

	@Override
	protected void addEnumType(TLModule module, TLScope scope, EnumConfig config) {
		// Note: The processor for the enum creation must be generated before the processors of the
		// classifiers (which happens from the super call below).
		if (createProcessors()) {
			addEnumProcessor(module.getName(), config);
		}

		super.addEnumType(module, scope, config);
	}

	private void addEnumProcessor(String moduleName, EnumConfig type) {
		CreateTLEnumerationProcessor.Config config = newConfigItem(CreateTLEnumerationProcessor.Config.class);
		QualifiedTypeName enumName = qTypeName(moduleName, type.getName());
		config.setName(enumName);
		copyAnnotations(type, config);
		addProcessor(config);
	}

	@Override
	protected void addAssociationType(TLModule module, TLScope scope, AssociationConfig config) {
		super.addAssociationType(module, scope, config);
		if (createProcessors()) {
			addAssociationProcessor(module.getName(), config);
		}
	}

	private void addAssociationProcessor(String moduleName, AssociationConfig type) {
		CreateTLAssociationProcessor.Config config = newConfigItem(CreateTLAssociationProcessor.Config.class);
		QualifiedTypeName newAssociationName = qTypeName(moduleName, type.getName());
		config.setName(newAssociationName);
		copyAnnotations(type, config);
		addProcessor(config);
	}

	@Override
	public Void visit(CreateStructuredTypePart diff, Void arg) throws RuntimeException {
		TLStructuredType type;
		String partName = diff.getPart().getName();
		String typeName = diff.getType();
		try {
			type = (TLStructuredType) TLModelUtil.findType(getModel(), typeName);
		} catch (TopLogicException ex) {
			log().info(
				"Merge conflict: Adding part '" + partName + "' to type '" + typeName
					+ "', but type does not exist.",
				Log.WARN);
			return null;
		}

		TLStructuredTypePart existing = type.getPart(partName);
		if (existing != null) {
			if (existing.getOwner() == type) {
				log().info(
					"Merge conflict: Adding part '" + partName + "' to type '" + typeName
						+ "', but part already exists.",
					Log.WARN);
				return null;
			} else if (!diff.getPart().isOverride()) {
				log().error(
					"Merge conflict: Adding part '" + partName
						+ "' which is not declared as override to type '" + typeName
						+ "', but overrides corresponding  attribute from '"
						+ TLModelUtil.qualifiedName(existing.getOwner()) + "'.");
				return null;
			}
		}
		
		log().info("Adding part '" + partName + " to type '" + type + "'.");
		addPart(type, diff.getPart());
		return null;
	}

	/**
	 * Moves a part in the list of local parts of a {@link TLStructuredType}.
	 *
	 * @param partName
	 *        Local name of the part to move.
	 * @param beforeName
	 *        Local name of the part to move the given part before. May be <code>null</code>
	 */
	protected void movePart(TLStructuredType type, String partName, String beforeName) {
		TLStructuredTypePart before;
		if (beforeName != null) {
			before = type.getPart(beforeName);
			if (before == null) {
				log().info(
					"Merge conflict: Reference part '" + beforeName + "' for adding part '"
						+ partName + "' to type '" + type
						+ "' does not exist.",
					Log.INFO);
				return;
			} else {
				if (before.getOwner() != type) {
					log().info(
						"Merge conflict: Reference part '" + beforeName + "' for adding part '"
							+ partName + "' to type '" + type
							+ "' is declared in super type.",
						Log.INFO);
					return;
				}
			}
		} else {
			before = null;
		}
		TLClassPart part = (TLClassPart) type.getPart(partName);
		movePart(part, before);
	}

	@Override
	public Void visit(UpdatePartType diff, Void arg) throws RuntimeException {
		TLModelPart part;
		String partName = diff.getPart();
		try {
			part = resolvePart(partName);
		} catch (TopLogicException ex) {
			log().info(
				"Merge conflict: Changing type of part '" + partName + "', but part does not exist.",
				Log.WARN);
			return null;
		}
		if (!(part instanceof TLTypePart)) {
			log().info("Referenced part '" + part + "' is not a type part.", Log.WARN);
			return null;
		}
		schedule().createReference(() -> {
			TLModelPart type;
			try {
				type = resolvePart(diff.getTypeSpec());
			} catch (TopLogicException ex) {
				log().error(diff.getTypeSpec() + " in " + diff.location() + " could not be resolved to a valid type.",
					ex);
				return;
			}
			if (!(type instanceof TLType)) {
				log().error("Configured type " + type + " is not a " + TLType.class.getName());
				return;
			}
			((TLTypePart) part).setType((TLType) type);
		});

		if (createProcessors()) {
			ChangeAttributeTargetType.Config config;
			if (part instanceof TLReference) {
				ChangeRefConfig changeRefConfig = newConfigItem(ChangeAttributeTargetType.ChangeRefConfig.class);
				changeRefConfig.setReference(qTypePartName(diff.getPart()));
				config = changeRefConfig;
			} else {
				config = newConfigItem(ChangeAttributeTargetType.Config.class);
				config.setPart(qTypePartName(diff.getPart()));
			}
			config.setTarget(qTypeName(diff.getTypeSpec()));
			addProcessor(config);
		}
		return null;
	}

	@Override
	public TLProperty createProperty(TLStructuredType owner, AttributeConfig attributeConfig) {
		TLProperty property = super.createProperty(owner, attributeConfig);

		if (createProcessors()) {
			CreateTLPropertyProcessor.Config config = newConfigItem(CreateTLPropertyProcessor.Config.class);
			config.setType(getQualifiedTypeName(owner.getModule().getName(), attributeConfig.getTypeSpec()));
			String qOwnerName = TLModelUtil.qualifiedName(owner);
			fillPartProcessor(config, qOwnerName, attributeConfig);

			addProcessor(config);
			addOverride(qOwnerName, attributeConfig);
		}
		return property;
	}

	@Override
	protected TLReference addReference(TLClass owner, ReferenceConfig referenceConfig,
			TLAssociationEnd associationEnd) {
		TLReference reference = super.addReference(owner, referenceConfig, associationEnd);

		if (createProcessors()) {
			AbstractEndAspectProcessor.Config<?> config;
			QualifiedTypeName targetType =
				getQualifiedTypeName(owner.getModule().getName(), referenceConfig.getTypeSpec());
			if (referenceConfig.getKind() == ReferenceKind.BACKWARDS) {
				CreateInverseTLReferenceProcessor.Config inverseConf =
					newConfigItem(CreateInverseTLReferenceProcessor.Config.class);
				String inverseReference = referenceConfig.getInverseReference();
				if (StringServices.isEmpty(inverseReference)) {
					if (referenceConfig.isOverride()) {
						TLReference inverseRef = TLModelUtil.getOtherEnd(associationEnd).getReference();
						if (inverseRef == null) {
							log().info(
								"Override of a backwards reference without inverse-reference: " + referenceConfig,
								Protocol.WARN);
						} else {
							inverseReference = inverseRef.getName();
						}
					} else {
						log().info("Non override backwards reference without inverse-reference annotation: "
								+ referenceConfig,
							Protocol.WARN);
					}
				}
				inverseConf.setInverseReference(qTypePartName(targetType, inverseReference));
				config = inverseConf;
			} else {
				CreateTLReferenceProcessor.Config refConf = newConfigItem(CreateTLReferenceProcessor.Config.class);
				refConf.setType(targetType);
				config = refConf;
			}
			String qOwnerName = TLModelUtil.qualifiedName(owner);
			fillEndAspectProcessor(config, qOwnerName, referenceConfig);

			addProcessor(config);
			addOverride(qOwnerName, referenceConfig);
		}
		return reference;
	}

	@Override
	protected TLAssociationEnd addAssociationEnd(TLAssociation owner, EndConfig endConfig, TLType targetType) {
		TLAssociationEnd end = super.addAssociationEnd(owner, endConfig, targetType);

		if (createProcessors()) {
			CreateTLAssociationEndProcessor.Config config = newConfigItem(CreateTLAssociationEndProcessor.Config.class);
			config.setType(getQualifiedTypeName(owner.getModule().getName(), endConfig.getTypeSpec()));
			String qOwnerName = TLModelUtil.qualifiedName(owner);
			fillEndAspectProcessor(config, qOwnerName, endConfig);

			addProcessor(config);
			addOverride(qOwnerName, endConfig);
		}
		return end;
	}

	private void fillEndAspectProcessor(AbstractEndAspectProcessor.Config<?> config,
			String qOwnerName, EndAspect part) {
		copyIfSet(part, EndAspect.COMPOSITE_PROPERTY, config::setComposite);
		copyIfSet(part, EndAspect.AGGREGATE_PROPERTY, config::setAggregate);
		copyIfSet(part, EndAspect.NAVIGATE_PROPERTY, config::setNavigate);
		copyIfSet(part, EndAspect.HISTORY_TYPE_PROPERTY, config::setHistoryType);
		fillPartProcessor(config, qOwnerName, part);
	}

	private void fillPartProcessor(AbstractCreateTypePartProcessor.Config<?> config,
			String qOwnerName,
			PartConfig part) {
		QualifiedPartName qPartName = qTypePartName(qOwnerName, part.getName());
		config.setName(qPartName);
		copyIfSet(part, PartConfig.MULTIPLE_PROPERTY, config::setMultiple);
		copyIfSet(part, PartConfig.ORDERED_PROPERTY, config::setOrdered);
		copyIfSet(part, PartConfig.ABSTRACT_PROPERTY, config::setAbstract);
		copyIfSet(part, PartConfig.MANDATORY, config::setMandatory);
		copyIfSet(part, PartConfig.BAG_PROPERTY, config::setBag);
		copyAnnotations(part, config);
	}

	private void addOverride(String qOwnerName, PartConfig part) {
		if (part.isOverride()) {
			TLStructuredType owner = (TLStructuredType) resolvePart(qOwnerName);
			TLStructuredTypePart createdPart = owner.getPartOrFail(part.getName());
			TLStructuredTypePart definition = createdPart.getDefinition();

			MarkTLTypePartOverride.Config overrideConf = newConfigItem(MarkTLTypePartOverride.Config.class);
			overrideConf.setName(qTypePartName(qOwnerName, part.getName()));
			overrideConf.setDefinition(qTypePartName(definition));
			addProcessor(overrideConf);
		}
	}

	@Override
	public TLModule createModule(ModuleConfig moduleConf) {
		if (createProcessors()) {
			CreateTLModuleProcessor.Config newModule = newConfigItem(CreateTLModuleProcessor.Config.class);
			String moduleName = moduleConf.getName();
			newModule.setName(moduleName);
			copyAnnotations(moduleConf, newModule);
			addProcessor(newModule);
		}

		/* Creating module must occur before the types are created. */
		return super.createModule(moduleConf);
	}

	@Override
	public Void visit(CreateClassifier diff, Void arg) throws RuntimeException {
		String classifierName = diff.getClassifierConfig().getName();
		TLEnumeration type;
		try {
			type = (TLEnumeration) resolveQName(diff.getType());
		} catch (TopLogicException ex) {
			log().info("Merge conflict: Adding classifier '" + classifierName + " to enumeration '"
				+ diff.getType() + "', but enumeration does not exist.", Log.WARN);
			return null;
		}

		TLClassifier existing = type.getClassifier(classifierName);
		if (existing != null) {
			log().info(
				"Merge conflict: Adding classifier '" + classifierName + "' to enumeration '"
					+ diff.getType() + "', but classifier already exists.",
				Log.WARN);
			return null;
		}

		TLClassifier before;
		String beforeName = diff.getBefore();
		if (beforeName == null) {
			before = null;
		} else {
			before = type.getClassifier(beforeName);
		}

		log().info("Adding classifier '" + classifierName + " to enumeration '" + diff.getType() + "'.");
		addClassifier(type, diff.getClassifierConfig(), before);
		return null;
	}

	@Override
	public void addClassifier(TLEnumeration classification, ClassifierConfig classifierConfig, TLClassifier before) {
		super.addClassifier(classification, classifierConfig, before);

		if (createProcessors()) {
			QualifiedPartName newClassifierName = classifierMigration(qTypeName(classification), classifierConfig);
			if (before != null) {
				addMoveTLTypePart(newClassifierName, before.getName());
			}
		}
	}

	private QualifiedPartName classifierMigration(QualifiedTypeName enumName, ClassifierConfig part) {
		QualifiedPartName classifierName = qTypePartName(enumName, part.getName());
		CreateTLClassifierProcessor.Config config = newConfigItem(CreateTLClassifierProcessor.Config.class);
		config.setName(classifierName);
		copyAnnotations(part, config);
		addProcessor(config);

		if (part.isDefault()) {
			SetDefaultTLClassifierProcessor.Config setDefaultConfig =
				newConfigItem(SetDefaultTLClassifierProcessor.Config.class);
			setDefaultConfig.setEnumeration(enumName);
			setDefaultConfig.setDefaultClassifier(part.getName());
			addProcessor(setDefaultConfig);
		}
		return classifierName;
	}

	@Override
	public Void visit(Delete diff, Void arg) throws RuntimeException {
		TLObject part;
		try {
			part = resolveQName(diff.getName());
		} catch (TopLogicException ex) {
			log().info(
				"Merge conflict: Deleting '" + diff.getName() + "' but it does not exist.", Log.INFO);
			return null;
		}
		
		if (part instanceof TLClass) {
			TLClass type = (TLClass) part;
			if (!type.isAbstract() && !type.tTransient()) {
				try (CloseableIterator<TLObject> it = directInstances(type)) {
					if (it.hasNext()) {
						log().info("Merge conflict deleting '" + type + "': Instances exist.");
						return null;
					}
				}
			}
		}
		
		log().info("Deleting '" + diff.getName() + "'.");
		if (part instanceof TLModelPart) {
			TLModelUtil.deleteRecursive((TLModelPart) part);
		} else {
			String name = diff.getName();
			int partSeparatorIndex = name.lastIndexOf(TLModelUtil.QUALIFIED_NAME_PART_SEPARATOR);

			TLModule module = (TLModule) resolveQName(name.substring(0, partSeparatorIndex));
			String singletonName = name.substring(partSeparatorIndex + 1);

			// Clear veto reference.
			module.removeSingleton(singletonName);

			part.tDelete();
		}

		if (createProcessors()) {
			if (part instanceof TLModule) {
				DeleteTLModuleProcessor.Config config = newConfigItem(DeleteTLModuleProcessor.Config.class);
				config.setName(diff.getName());
				addProcessor(config);
			} else if (part instanceof TLClass) {
				DeleteTLClassProcessor.Config config = newConfigItem(DeleteTLClassProcessor.Config.class);
				config.setName(qTypeName(diff.getName()));
				addProcessor(config);
			} else if (part instanceof TLEnumeration) {
				DeleteTLEnumerationProcessor.Config config = newConfigItem(DeleteTLEnumerationProcessor.Config.class);
				config.setName(qTypeName(diff.getName()));
				addProcessor(config);
			} else if (part instanceof TLPrimitive) {
				DeleteTLDatatypeProcessor.Config config = newConfigItem(DeleteTLDatatypeProcessor.Config.class);
				config.setName(qTypeName(diff.getName()));
				addProcessor(config);
			} else if (part instanceof TLProperty) {
				DeleteTLPropertyProcessor.Config config = newConfigItem(DeleteTLPropertyProcessor.Config.class);
				config.setName(qTypePartName(diff.getName()));
				addProcessor(config);
			} else if (part instanceof TLReference) {
				DeleteTLReferenceProcessor.Config config = newConfigItem(DeleteTLReferenceProcessor.Config.class);
				config.setName(qTypePartName(diff.getName()));
				addProcessor(config);
			} else {
				log().info(
					"No deletion supported for '" + diff.getName() + "' of type '" + part.getClass().getName() + "'.");
			}
		}
		return null;
	}

	@Override
	public Void visit(DeleteRole diff, Void arg) throws RuntimeException {
		TLModule module;
		try {
			module = TLModelUtil.findModule(diff.getModule());
		} catch (TopLogicException ex) {
			log().info(
				"Merge conflict: Deleting role '" + diff.getRole() + "' in module '" + diff.getModule()
						+ "': " + ex.getMessage(),
				Log.INFO);
			return null;
		}

		BoundedRole role = BoundedRole.getDefinedRole(module, diff.getRole());
		if (role == null) {
			log().info(
				"Merge conflict: Deleting role '" + diff.getRole() + "' in module '" + diff.getModule()
					+ "', but role does not exist.",
				Log.INFO);
			return null;
		}

		log().info("Deleting role '" + diff.getRole() + "' in module '" + diff.getModule() + "'.");
		role.tDelete();
		return null;
	}

	@Override
	public Void visit(UpdateMandatory diff, Void arg) throws RuntimeException {
		TLStructuredTypePart part;
		try {
			part = (TLStructuredTypePart) resolveQName(diff.getPart());
		} catch (TopLogicException ex) {
			log().info(
				"Merge conflict: Updating mandatory state of '" + diff.getPart() + "' to '" + diff.isMandatory()
					+ "', but part does not exist.",
				Log.WARN);
			return null;
		}
		log().info("Updating mandatory state of '" + diff.getPart() + "' to '" + diff.isMandatory() + "'.");
		part.setMandatory(diff.isMandatory());

		if (createProcessors()) {
			if (part instanceof TLProperty) {
				UpdateTLPropertyProcessor.Config config = newConfigItem(UpdateTLPropertyProcessor.Config.class);
				config.setName(qTypePartName(diff.getPart()));
				config.setMandatory(diff.isMandatory());
				addProcessor(config);
			} else if (part instanceof TLReference) {
				UpdateTLReferenceProcessor.Config config = newConfigItem(UpdateTLReferenceProcessor.Config.class);
				config.setName(qTypePartName(diff.getPart()));
				config.setMandatory(diff.isMandatory());
				addProcessor(config);
			} else if (part instanceof TLAssociationEnd) {
				UpdateTLAssociationEndProcessor.Config config =
					newConfigItem(UpdateTLAssociationEndProcessor.Config.class);
				config.setName(qTypePartName(diff.getPart()));
				config.setMandatory(diff.isMandatory());
				addProcessor(config);
			} else {
				throw new UnsupportedOperationException("No update for '" + diff.getPart() + "' of type '"
						+ part.getClass().getName() + "' possible.");
			}
		}
		return null;
	}

	@Override
	public Void visit(MoveClassifier diff, Void arg) throws RuntimeException {
		TLClassifier moved;
		try {
			moved = TLModelUtil.findPart(getModel(), diff.getClassifier());
		} catch (TopLogicException ex) {
			log().info(
				"Merge conflict: Moving classifier '" + diff.getClassifier() + "', but classifier does not exist.",
				Log.INFO);
			return null;
		}

		TLEnumeration owner = moved.getOwner();
		List<TLClassifier> classifiers = owner.getClassifiers();

		TLClassifier beforeClassifier;
		String before = diff.getBefore();
		String pos;
		if (before != null) {
			beforeClassifier = owner.getClassifier(before);
			if (beforeClassifier == null) {
				log().info(
					"Merge conflict: Moving classifier '" + diff.getClassifier() + "', but reference classifier '"
						+ before + "' does not exist.",
					Log.INFO);
				return null;
			}
			pos = "before '" + before + "'";
		} else {
			beforeClassifier = null;
			pos = "to the end";
		}

		log().info(
			"Moving classifier '" + diff.getClassifier() + "' " + pos + ".",
			Log.INFO);
		classifiers.remove(moved);
		classifiers.add(beforeClassifier == null ? classifiers.size() : classifiers.indexOf(beforeClassifier), moved);

		if (createProcessors()) {
			addMoveTLTypePart(qTypePartName(diff.getClassifier()), diff.getBefore());
		}
		return null;
	}

	@Override
	public Void visit(MoveStructuredTypePart diff, Void arg) throws RuntimeException {
		schedule().reorderProperties(() -> movePart(diff.getPart(), diff.getBefore()));
		return null;
	}

	/**
	 * Moves a part in the list of local parts of a {@link TLClass}.
	 *
	 * @param qPartName
	 *        Qualified name of the part to move.
	 * @param beforeName
	 *        Local name of the part to move the given part before. May be <code>null</code>.
	 */
	protected void movePart(String qPartName, String beforeName) {
		TLClassPart part;
		try {
			part = (TLClassPart) resolveQName(qPartName);
		} catch (TopLogicException ex) {
			log().info(
				"Merge conflict: Moved part '" + qPartName + "' does not exist.",
				Log.INFO);
			return;
		}

		TLStructuredTypePart before;
		if (beforeName != null) {
			before = part.getOwner().getPart(beforeName);
			if (before == null) {
				log().info(
					"Merge conflict: Reference part '" + beforeName + "' for moving part '" + qPartName
						+ "' does not exist.",
					Log.INFO);
				return;
			}
			if (before.getOwner() != part.getOwner()) {
				log().info(
					"Merge conflict: Reference part '" + beforeName + "' for moving part '" + qPartName
						+ "' is declared in super type.",
					Log.INFO);
				return;
			}
		} else {
			before = null;
		}

		String pos = before == null ? "to the end" : "before part '" + beforeName + "'";
		log().info("Moving part '" + qPartName + "' " + pos + ".", Log.INFO);
		movePart(part, before);
	}

	private void movePart(TLClassPart part, TLStructuredTypePart before) {
		List<TLClassPart> localParts = part.getOwner().getLocalClassParts();
		localParts.remove(part);
		int index;
		if (before == null) {
			index = localParts.size();
		} else {
			index = localParts.indexOf(before);
			if (index < 0) {
				log().info("Part '" + before + "' not found for adjusting order of '" + part + "'.", Log.WARN);
				index = localParts.size();
			}
		}
		localParts.add(index, part);

		if (createProcessors()) {
			addMoveTLTypePart(qTypePartName(TLModelUtil.qualifiedName(part)), before == null ? null : before.getName());
		}
	}

	private void addMoveTLTypePart(QualifiedPartName partName, String before) {
		ReorderTLTypePart.Config config = newConfigItem(ReorderTLTypePart.Config.class);
		config.setName(partName);
		if (before != null) {
			config.setBefore(before);
		}
		addProcessor(config);
	}

	@Override
	public Void visit(RemoveAnnotation diff, Void arg) throws RuntimeException {
		TLModelPart part;
		try {
			part = resolvePart(diff.getPart());
		} catch (TopLogicException ex) {
			log().info(
				"Merge conflict: Removing annotation '" + diff.getAnnotation().getName() + "' from '" + diff.getPart()
					+ "', but part does not exist.",
				Log.INFO);
			return null;
		}
		log().info("Removing annotation '" + diff.getAnnotation().getName() + "' from '" + diff.getPart() + "'.");
		part.removeAnnotation(diff.getAnnotation());

		if (createProcessors()) {
			RemoveTLAnnotations.Config config = newConfigItem(RemoveTLAnnotations.Config.class);
			config.setName(diff.getPart());
			RemoveTLAnnotations.AnnotationConfig toRemove = newConfigItem(RemoveTLAnnotations.AnnotationConfig.class);
			toRemove.setAnnotationClass(diff.getAnnotation());
			config.getAnnotations().add(toRemove);
			addProcessor(config);
		}
		return null;
	}

	@Override
	public Void visit(RemoveGeneralization diff, Void arg) throws RuntimeException {
		TLClass type;
		try {
			type = asClass(TLModelUtil.findType(getModel(), diff.getType()));
		} catch (TopLogicException ex) {
			log().info(
				"Merge conflict: Removing generalization '" + diff.getGeneralization() + "' from '" + diff.getType()
					+ "', but type does not exist.",
				Log.INFO);
			return null;
		}
		TLType generalization;
		try {
			generalization = TLModelUtil.findType(getModel(), diff.getGeneralization());
		} catch (TopLogicException ex) {
			log().info(
				"Merge conflict: Removing generalization '" + diff.getGeneralization() + "' from '" + diff.getType()
					+ "', but generalization does not exist.",
				Log.INFO);
			return null;
		}

		log().info("Removing generalization '" + diff.getGeneralization() + "' from '" + diff.getType() + "'.");
		type.getGeneralizations().remove(generalization);

		if (createProcessors()) {
			RemoveTLClassGeneralization.Config config = newConfigItem(RemoveTLClassGeneralization.Config.class);
			config.setName(qTypeName(diff.getType()));

			config.getGeneralizations().add(newGeneralization(qTypeName(diff.getGeneralization())));
			addProcessor(config);
		}
		return null;
	}

	private static AddTLClassGeneralization.Generalization newGeneralization(QualifiedTypeName type) {
		AddTLClassGeneralization.Generalization generalizationConf =
			newConfigItem(AddTLClassGeneralization.Generalization.class);
		generalizationConf.setType(type);
		return generalizationConf;
	}

	@Override
	public Void visit(MakeAbstract diff, Void arg) throws RuntimeException {
		TLClass type;
		try {
			type = asClass(TLModelUtil.findType(getModel(), diff.getType()));
		} catch (TopLogicException ex) {
			log().info(
				"Merge conflict: Making type '" + diff.getType() + "' abstract, but type does not exist.",
				Log.INFO);
			return null;
		}

		try (CloseableIterator<TLObject> it = directInstances(type)) {
			if (it.hasNext()) {
				log().info(
					"Merge conflict: Cannot make '" + diff.getType() + "' abstract: Instances exist.",
					Log.INFO);
				return null;
			}

		}

		log().info("Making type abstract: " + diff.getType());
		type.setAbstract(true);

		if (createProcessors()) {
			UpdateTLClassProcessor.Config config = newConfigItem(UpdateTLClassProcessor.Config.class);
			config.setName(qTypeName(diff.getType()));
			config.setAbstract(true);
			addProcessor(config);
		}
		return null;
	}

	/**
	 * Finds all direct instances of the given type.
	 */
	protected CloseableIterator<TLObject> directInstances(TLClass type) {
		return MetaElementUtil.iterateDirectInstances(type, TLObject.class);
	}

	@Override
	public Void visit(MakeConcrete diff, Void arg) throws RuntimeException {
		TLClass type;
		try {
			type = asClass(TLModelUtil.findType(getModel(), diff.getType()));
		} catch (TopLogicException ex) {
			log().info(
				"Merge conflict: Making type '" + diff.getType() + "' concrete, but type does not exist.",
				Log.INFO);
			return null;
		}

		log().info("Making type concrete: " + diff.getType());
		type.setAbstract(false);

		if (createProcessors()) {
			UpdateTLClassProcessor.Config config = newConfigItem(UpdateTLClassProcessor.Config.class);
			config.setName(qTypeName(diff.getType()));
			config.setAbstract(false);
			addProcessor(config);
		}
		return null;
	}

	@Override
	public Void visit(RenamePart diff, Void arg) throws RuntimeException {
		TLNamedPart part;
		try {
			log().info("Renaming '" + diff.getPart() + "' to '" + diff.getNewName() + "'.");
			part = (TLNamedPart) resolveQName(diff.getPart());
		} catch (TopLogicException ex) {
			log().info(
				"Merge conflict: Renaming '" + diff.getPart() + "' to '" + diff.getNewName()
					+ "', but part does not exist.",
				Log.WARN);
			return null;
		}
		part.setName(diff.getNewName());

		if (createProcessors()) {
			QualifiedPartName oldName = qTypePartName(diff.getPart());
			QualifiedPartName newName =
				qTypePartName(qTypeName(oldName.getModuleName(), oldName.getTypeName()), diff.getNewName());
			if (part instanceof TLProperty) {
				UpdateTLPropertyProcessor.Config config = newConfigItem(UpdateTLPropertyProcessor.Config.class);
				config.setName(oldName);
				config.setNewName(newName);
				addProcessor(config);
			} else if (part instanceof TLReference) {
				UpdateTLReferenceProcessor.Config config = newConfigItem(UpdateTLReferenceProcessor.Config.class);
				config.setName(oldName);
				config.setNewName(newName);
				addProcessor(config);
			} else if (part instanceof TLAssociationEnd) {
				UpdateTLAssociationEndProcessor.Config config =
					newConfigItem(UpdateTLAssociationEndProcessor.Config.class);
				config.setName(oldName);
				config.setNewName(newName);
				addProcessor(config);
			} else {
				throw new UnsupportedOperationException("No rename for '" + diff.getPart() + "' of type '"
						+ part.getClass().getName() + "' possible.");
			}
		}

		return null;
	}

	@Override
	public void complete() {
		super.complete();
	}

	private boolean addProcessor(PolymorphicConfiguration<? extends MigrationProcessor> p) {
		return _processors.add(p);
	}

	/**
	 * Service method to resolve a {@link TLModelPart} by its qualified name.
	 */
	protected TLModelPart resolvePart(String qname) throws TopLogicException {
		return (TLModelPart) resolveQName(qname);
	}

	private TLObject resolveQName(String qname) throws TopLogicException {
		return TLModelUtil.resolveQualifiedName(getModel(), qname);
	}

	private TLClass asClass(TLType type) {
		return (TLClass) type;
	}

	private static QualifiedPartName qTypePartName(TLTypePart part) {
		return qTypePartName(TLModelUtil.qualifiedName(part));
	}

	private static QualifiedPartName qTypePartName(QualifiedTypeName typeName, String partName) {
		return qTypePartName(typeName.getName(), partName);
	}

	private static QualifiedPartName qTypePartName(String qTypeName, String partName) {
		return qTypePartName(TLModelUtil.qualifiedTypePartName(qTypeName, partName));
	}

	private static QualifiedPartName qTypePartName(String name) {
		QualifiedPartName qName = newConfigItem(QualifiedPartName.class);
		qName.setName(name);
		return qName;
	}

	private static QualifiedTypeName qTypeName(TLType type) {
		return qTypeName(TLModelUtil.qualifiedName(type));
	}

	private static QualifiedTypeName qTypeName(String module, String typeName) {
		return qTypeName(TLModelUtil.qualifiedName(module, typeName));
	}

	private static QualifiedTypeName qTypeName(String name) {
		QualifiedTypeName qName = newConfigItem(QualifiedTypeName.class);
		qName.setName(name);
		return qName;
	}

	private static <A extends TLAnnotation> void copyAnnotations(AnnotatedConfig<A> source,
			AnnotatedConfig<A> dest) {
		for (A annotation : source.getAnnotations()) {
			dest.getAnnotations().add(TypedConfiguration.copy(annotation));
		}
	}

	private static void copyDBColumn(DBColumnType source, DBColumnType dest) {
		dest.setDBType(source.getDBType());
		Integer dbPrecision = source.getDBPrecision();
		if (dbPrecision != null) {
			dest.setDBPrecision(dbPrecision);
		}
		Integer dbSize = source.getDBSize();
		if (dbSize != null) {
			dest.setDBSize(dbSize);
		}
		Boolean binary = source.isBinary();
		if (binary != null) {
			dest.setBinary(binary);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> void copyIfSet(ConfigurationItem source, String propertyName, Consumer<T> target) {
		PropertyDescriptor property = source.descriptor().getProperty(propertyName);
		if (source.valueSet(property)) {
			target.accept((T) source.value(property));
		}
	}

}
