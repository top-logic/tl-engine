/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.model.diff.apply;

import static com.top_logic.basic.config.TypedConfiguration.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.basic.col.EmptyClosableIterator;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.constraint.check.ConstraintChecker;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.SQLH;
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
import com.top_logic.element.model.I18NConstants;
import com.top_logic.element.model.diff.config.AddAnnotations;
import com.top_logic.element.model.diff.config.AddGeneralization;
import com.top_logic.element.model.diff.config.CreateClassifier;
import com.top_logic.element.model.diff.config.CreateRole;
import com.top_logic.element.model.diff.config.CreateSingleton;
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
import com.top_logic.element.model.diff.config.UpdateMandatory;
import com.top_logic.element.model.migration.model.AbstractCreateTypePartProcessor;
import com.top_logic.element.model.migration.model.AbstractEndAspectProcessor;
import com.top_logic.element.model.migration.model.AddTLAnnotations;
import com.top_logic.element.model.migration.model.AddTLClassGeneralization;
import com.top_logic.element.model.migration.model.CreateInverseTLReferenceProcessor;
import com.top_logic.element.model.migration.model.CreateTLAssociationEndProcessor;
import com.top_logic.element.model.migration.model.CreateTLAssociationProcessor;
import com.top_logic.element.model.migration.model.CreateTLClassProcessor;
import com.top_logic.element.model.migration.model.CreateTLClassifierProcessor;
import com.top_logic.element.model.migration.model.CreateTLEnumerationProcessor;
import com.top_logic.element.model.migration.model.CreateTLModuleProcessor;
import com.top_logic.element.model.migration.model.CreateTLObjectProcessor;
import com.top_logic.element.model.migration.model.CreateTLPropertyProcessor;
import com.top_logic.element.model.migration.model.CreateTLReferenceProcessor;
import com.top_logic.element.model.migration.model.CreateTLSingletonProcessor;
import com.top_logic.element.model.migration.model.DeleteTLClassProcessor;
import com.top_logic.element.model.migration.model.DeleteTLModuleProcessor;
import com.top_logic.element.model.migration.model.DeleteTLPropertyProcessor;
import com.top_logic.element.model.migration.model.DeleteTLReferenceProcessor;
import com.top_logic.element.model.migration.model.MarkTLTypePartOverride;
import com.top_logic.element.model.migration.model.RemoveTLAnnotations;
import com.top_logic.element.model.migration.model.RemoveTLClassGeneralization;
import com.top_logic.element.model.migration.model.ReorderTLClassGeneralization;
import com.top_logic.element.model.migration.model.ReorderTLTypePart;
import com.top_logic.element.model.migration.model.SetDefaultTLClassifierProcessor;
import com.top_logic.element.model.migration.model.UpdateTLAssociationEndProcessor;
import com.top_logic.element.model.migration.model.UpdateTLClassProcessor;
import com.top_logic.element.model.migration.model.UpdateTLPropertyProcessor;
import com.top_logic.element.model.migration.model.UpdateTLReferenceProcessor;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLProperty;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLScope;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.security.RoleConfig;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.model.config.DatatypeConfig;
import com.top_logic.model.config.EnumConfig;
import com.top_logic.model.config.EnumConfig.ClassifierConfig;
import com.top_logic.model.config.TypeConfig;
import com.top_logic.model.impl.generated.TlModelFactory;
import com.top_logic.model.migration.CreateTLDatatypeProcessor;
import com.top_logic.model.migration.data.QualifiedPartName;
import com.top_logic.model.migration.data.QualifiedTypeName;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.Resources;

/**
 * Factory to create {@link MigrationProcessor} based on a model patch.
 * 
 * @see DiffElement
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CreateMigrationProcessors extends ApplyModelPatch {

	/**
	 * Sequence of {@link MigrationProcessor} that can be execute to apply a model patch within a
	 * migration.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface MigrationProcessors extends ConfigurationItem {

		/**
		 * {@link MigrationProcessor}s to execute.
		 */
		@DefaultContainer
		List<PolymorphicConfiguration<? extends MigrationProcessor>> getProcessors();

	}

	private MigrationProcessors _processors = newConfigItem(MigrationProcessors.class);

	/**
	 * Creates a new {@link CreateMigrationProcessors}.
	 * 
	 * @param model
	 *        Base {@link TLModel} to create {@link MigrationProcessor} for.
	 */
	public CreateMigrationProcessors(Protocol log, TLModel model) {
		super(log, model, null);
	}

	/**
	 * Creates a sequence of that can be executed within a migration file to apply the given patch.
	 */
	public static MigrationProcessors createProcessors(Protocol log, TLModel tlModel,
			List<? extends DiffElement> patch) {
		CreateMigrationProcessors v = new CreateMigrationProcessors(log, tlModel);
		v.applyPatch(patch);
		v.complete();
		return v._processors;
	}

	@Override
	public Void visit(AddAnnotations diff, Void arg) throws RuntimeException {
		super.visit(diff, arg);

		AddTLAnnotations.Config config = newConfigItem(AddTLAnnotations.Config.class);
		config.setName(diff.getPart());
		copyAnnotations(diff, config);
		addProcessor(config);
		return null;
	}

	@Override
	public Void visit(RemoveAnnotation diff, Void arg) throws RuntimeException {
		super.visit(diff, arg);

		RemoveTLAnnotations.Config config = newConfigItem(RemoveTLAnnotations.Config.class);
		config.setName(diff.getPart());
		RemoveTLAnnotations.AnnotationConfig toRemove = newConfigItem(RemoveTLAnnotations.AnnotationConfig.class);
		toRemove.setAnnotationClass(diff.getAnnotation());
		config.getAnnotations().add(toRemove);
		addProcessor(config);
		return null;
	}

	@Override
	public Void visit(AddGeneralization diff, Void arg) throws RuntimeException {
		super.visit(diff, arg);

		AddTLClassGeneralization.Config config = newConfigItem(AddTLClassGeneralization.Config.class);
		QualifiedTypeName type = qTypeName(diff.getType());
		config.setName(type);

		AddTLClassGeneralization.Generalization generalization =
			newConfigItem(AddTLClassGeneralization.Generalization.class);
		QualifiedTypeName generalizationName = qTypeName(diff.getGeneralization());
		generalization.setType(generalizationName);
		config.getGeneralizations().add(generalization);
		addProcessor(config);

		String before = diff.getBefore();
		if (before != null) {
			addReorderGeneralization(type, generalizationName, before);
		}
		return null;
	}

	@Override
	public Void visit(MoveGeneralization diff, Void arg) throws RuntimeException {
		super.visit(diff, arg);

		addReorderGeneralization(qTypeName(diff.getType()), qTypeName(diff.getGeneralization()), diff.getBefore());
		return null;
	}

	private void addReorderGeneralization(QualifiedTypeName type, QualifiedTypeName generalization, String before) {
		ReorderTLClassGeneralization.Config config = newConfigItem(ReorderTLClassGeneralization.Config.class);
		config.setType(type);
		config.setGeneralization(generalization);
		config.setBefore(before == null ? null : qTypeName(before));
		addProcessor(config);
	}

	@Override
	public Void visit(RemoveGeneralization diff, Void arg) throws RuntimeException {
		super.visit(diff, arg);

		RemoveTLClassGeneralization.Config config = newConfigItem(RemoveTLClassGeneralization.Config.class);
		config.setName(qTypeName(diff.getType()));

		AddTLClassGeneralization.Generalization generalization =
			newConfigItem(AddTLClassGeneralization.Generalization.class);
		generalization.setType(qTypeName(diff.getGeneralization()));

		config.getGeneralizations().add(generalization);
		addProcessor(config);
		return null;
	}

	@Override
	public Void visit(MakeAbstract diff, Void arg) throws RuntimeException {
		super.visit(diff, arg);

		UpdateTLClassProcessor.Config config = newConfigItem(UpdateTLClassProcessor.Config.class);
		config.setName(qTypeName(diff.getType()));
		config.setAbstract(true);
		addProcessor(config);
		return null;
	}

	/**
	 * {@link #getModel()} is a transient model. There are no instances.
	 */
	@Override
	protected CloseableIterator<TLObject> directInstances(TLClass type) {
		assert type.tTransient();
		return EmptyClosableIterator.getInstance();
	}

	@Override
	public Void visit(MakeConcrete diff, Void arg) throws RuntimeException {
		super.visit(diff, arg);

		UpdateTLClassProcessor.Config config = newConfigItem(UpdateTLClassProcessor.Config.class);
		config.setName(qTypeName(diff.getType()));
		config.setAbstract(false);
		addProcessor(config);
		return null;
	}

	@Override
	public Void visit(UpdateMandatory diff, Void arg) throws RuntimeException {
		super.visit(diff, arg);

		TLObject part = resolvePart(diff.getPart());
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
			UpdateTLAssociationEndProcessor.Config config = newConfigItem(UpdateTLAssociationEndProcessor.Config.class);
			config.setName(qTypePartName(diff.getPart()));
			config.setMandatory(diff.isMandatory());
			addProcessor(config);
		} else {
			throw new UnsupportedOperationException("No update for '" + diff.getPart() + "' of type '"
					+ part.getClass().getName() + "' possible.");
		}
		return null;
	}

	@Override
	public Void visit(Delete diff, Void arg) throws RuntimeException {
		TLObject part = resolvePart(diff.getName());
		if (part instanceof TLModule) {
			DeleteTLModuleProcessor.Config config = newConfigItem(DeleteTLModuleProcessor.Config.class);
			config.setName(diff.getName());
			addProcessor(config);
		} else if (part instanceof TLClass) {
			DeleteTLClassProcessor.Config config = newConfigItem(DeleteTLClassProcessor.Config.class);
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
			throw new UnsupportedOperationException("No deletion for '" + diff.getName() + "' of type '"
					+ part.getClass().getName() + "' possible.");
		}

		/* Deletes part, so it must be executed *after* resolving part. */
		return super.visit(diff, arg);
	}

	@Override
	public Void visit(DeleteRole diff, Void arg) throws RuntimeException {
		deleteRole(diff.getModule(), diff.getRole());

		if (!getModel().tTransient()) {
			// Can delete role only in non transient model.
			super.visit(diff, arg);
		}
		return null;
	}

	private void deleteRole(String module, String role) {
		if (true)
			failUnimplemented();
	}

	/**
	 * {@link MigrationProcessor} to create role is created in
	 * {@link #createRole(TLModule, RoleConfig)}.
	 * 
	 * @see #createRole(TLModule, RoleConfig)
	 */
	@Override
	public Void visit(CreateRole diff, Void arg) throws RuntimeException {
		return super.visit(diff, arg);
	}

	@Override
	public BoundedRole createRole(TLModule scope, RoleConfig role) {
		addRole(scope.getName(), role);

		if (!scope.tTransient()) {
			/* can not only create role object in non transient module. */
			super.createRole(scope, role);
		}
		return null;
	}

	private void addRole(String module, RoleConfig roleConfig) {
		CreateTLObjectProcessor.Config config = newConfigItem(CreateTLObjectProcessor.Config.class);
		config.setTable(BoundedRole.OBJECT_NAME);
		config.setType(qTypeName(BoundedRole.ROLE_TYPE));
		config.setNoTypeColumn(true);

		CreateTLObjectProcessor.Value value;
		value = newConfigItem(CreateTLObjectProcessor.Value.class);
		value.setColumnType(DBType.STRING);
		value.setColumn(SQLH.mangleDBName(BoundedRole.NAME_ATTRIBUTE));
		value.setValue(roleConfig.getName());
		config.getValues().add(value);

		value = newConfigItem(CreateTLObjectProcessor.Value.class);
		value.setColumnType(DBType.BOOLEAN);
		value.setColumn(SQLH.mangleDBName(BoundedRole.ROLE_SYSTEM));
		value.setValue(Boolean.toString(true));
		config.getValues().add(value);

		value = newConfigItem(CreateTLObjectProcessor.Value.class);
		value.setColumnType(DBType.STRING);
		value.setColumn(SQLH.mangleDBName(BoundedRole.ATTRIBUTE_DESCRIPTION));
		value.setValue(Resources.getInstance().getString(I18NConstants.ROLE_DESCRIPTION.key(roleConfig.getName()), ""));
		config.getValues().add(value);

		addProcessor(config);

		// Connection to module is missing.
		failUnimplemented();
	}

	/**
	 * {@link MigrationProcessor} to create classifier is created in
	 * {@link #addClassifier(TLEnumeration, ClassifierConfig, TLClassifier)}.
	 * 
	 * @see #addClassifier(TLEnumeration, ClassifierConfig, TLClassifier)
	 */
	@Override
	public Void visit(CreateClassifier diff, Void arg) throws RuntimeException {
		return super.visit(diff, arg);
	}

	@Override
	public void addClassifier(TLEnumeration classification, ClassifierConfig classifierConfig, TLClassifier before) {
		super.addClassifier(classification, classifierConfig, before);
		
		QualifiedPartName newClassifierName = classifierMigration(qTypeName(classification), null, classifierConfig);
		if (before != null) {
			addMoveTLTypePart(newClassifierName, before.getName());
		}
		
	}

	private QualifiedPartName classifierMigration(QualifiedTypeName enumName, Integer sortOrder,
			ClassifierConfig part) {
		QualifiedPartName classifierName = qTypePartName(enumName, part.getName());
		CreateTLClassifierProcessor.Config config = newConfigItem(CreateTLClassifierProcessor.Config.class);
		config.setName(classifierName);
		if (sortOrder != null) {
			config.setSortOrder(sortOrder.intValue());
		}
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

	/**
	 * {@link MigrationProcessor} to create singleton is created in
	 * {@link #addSingleton(String, SingletonConfig)}.
	 * 
	 * @see #addSingleton(String, SingletonConfig)
	 */
	@Override
	public Void visit(CreateSingleton diff, Void arg) throws RuntimeException {
		addSingleton(diff.getModule(), diff.getSingleton());
		return super.visit(diff, arg);
	}

	@Override
	public void createSingleton(TLModule module, SingletonConfig singleton) {
		super.createSingleton(module, singleton);

		addSingleton(module.getName(), singleton);
	}

	private void addSingleton(String module, SingletonConfig singleton) {
		CreateTLSingletonProcessor.Config config = newConfigItem(CreateTLSingletonProcessor.Config.class);
		config.setModule(module);
		config.setName(singleton.getName());
		CreateTLObjectProcessor.Config singletonConf = newConfigItem(CreateTLObjectProcessor.Config.class);
		String typeSpec = singleton.getTypeSpec();
		singletonConf.setType(qTypeName(typeSpec));
		singletonConf.setTable(TLAnnotations.getTable((TLType) resolvePart(typeSpec)));
		config.setSingleton(singletonConf);
		addProcessor(config);
	}

	/**
	 * {@link MigrationProcessor} to create type is created in
	 * {@link #addType(TLModule, TLScope, TypeConfig)}.
	 * 
	 * @see #addType(TLModule, TLScope, TypeConfig)
	 */
	@Override
	public Void visit(CreateType diff, Void arg) throws RuntimeException {
		return super.visit(diff, arg);
	}

	@Override
	protected void addObjectType(TLModule module, TLScope scope, ObjectTypeConfig config) {
		super.addObjectType(module, scope, config);

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
				AddTLClassGeneralization.Generalization generalization =
					newConfigItem(AddTLClassGeneralization.Generalization.class);
				generalization.setType(getQualifiedTypeName(moduleName, generalizations.get(i).getQualifiedTypeName()));
				generalizationsConfig.getGeneralizations().add(generalization);
			}
			schedule().createTypeHierarchy(() -> addProcessor(generalizationsConfig));
		}

		addProcessor(newClass);

		if (!innerTypes.isEmpty()) {
			failUnimplemented();
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
		addDatatypeProcessor(module.getName(), config);
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
	protected void addEnumType(TLModule module, TLScope scope, EnumConfig config) {
		super.addEnumType(module, scope, config);
		addEnumProcessor(module.getName(), config);
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
		addAssociationProcessor(module.getName(), config);
	}

	private void addAssociationProcessor(String moduleName, AssociationConfig type) {
		CreateTLAssociationProcessor.Config config = newConfigItem(CreateTLAssociationProcessor.Config.class);
		QualifiedTypeName newAssociationName = qTypeName(moduleName, type.getName());
		config.setName(newAssociationName);
		copyAnnotations(type, config);
		addProcessor(config);
	}

	@Override
	public TLProperty createProperty(TLStructuredType owner, AttributeConfig attributeConfig) {
		TLProperty property = super.createProperty(owner, attributeConfig);

		CreateTLPropertyProcessor.Config config = newConfigItem(CreateTLPropertyProcessor.Config.class);
		config.setType(qTypeName(attributeConfig.getTypeSpec()));
		fillPartProcessor(config, TLModelUtil.qualifiedName(owner), attributeConfig);

		addProcessor(config);
		return property;
	}

	@Override
	protected TLReference addReference(TLClass owner, ReferenceConfig referenceConfig,
			TLAssociationEnd associationEnd) {
		TLReference reference = super.addReference(owner, referenceConfig, associationEnd);

		AbstractEndAspectProcessor.Config<?> config;
		QualifiedTypeName targetType = getQualifiedTypeName(owner.getModule().getName(), referenceConfig.getTypeSpec());
		if (referenceConfig.getKind() == ReferenceKind.BACKWARDS) {
			CreateInverseTLReferenceProcessor.Config inverseConf =
				newConfigItem(CreateInverseTLReferenceProcessor.Config.class);
			inverseConf.setInverseReference(qTypePartName(targetType, referenceConfig.getInverseReference()));
			config = inverseConf;
		} else {
			CreateTLReferenceProcessor.Config refConf = newConfigItem(CreateTLReferenceProcessor.Config.class);
			refConf.setType(targetType);
			config = refConf;
		}
		fillEndAspectProcessor(config, TLModelUtil.qualifiedName(owner), referenceConfig);

		addProcessor(config);
		return reference;
	}

	@Override
	protected TLAssociationEnd addAssociationEnd(TLAssociation owner, EndConfig endConfig, TLType targetType) {
		TLAssociationEnd end = super.addAssociationEnd(owner, endConfig, targetType);

		CreateTLAssociationEndProcessor.Config config = newConfigItem(CreateTLAssociationEndProcessor.Config.class);
		config.setType(qTypeName(endConfig.getTypeSpec()));
		fillEndAspectProcessor(config, TLModelUtil.qualifiedName(owner), endConfig);

		addProcessor(config);
		return end;
	}

	private void fillEndAspectProcessor(AbstractEndAspectProcessor.Config<?> config,
			String qOwnerName, EndAspect part) {
		config.setComposite(part.isComposite());
		config.setAggregate(part.isAggregate());
		config.setNavigate(part.canNavigate());
		config.setHistoryType(part.getHistoryType());
		fillPartProcessor(config, qOwnerName, part);
	}

	private void fillPartProcessor(AbstractCreateTypePartProcessor.Config<?> config,
			String qOwnerName,
			PartConfig part) {
		QualifiedPartName qPartName = qTypePartName(qOwnerName, part.getName());
		config.setName(qPartName);
		config.setMultiple(part.isMultiple());
		config.setOrdered(part.isOrdered());
		config.setMandatory(part.getMandatory());
		config.setBag(part.isBag());
		copyAnnotations(part, config);
		if (part.isOverride()) {
			TLStructuredType owner = (TLStructuredType) resolvePart(qOwnerName);
			TLStructuredTypePart createdPart = owner.getPartOrFail(part.getName());
			TLStructuredTypePart definition = createdPart.getDefinition();

			MarkTLTypePartOverride.Config overrideConf = newConfigItem(MarkTLTypePartOverride.Config.class);
			overrideConf.setName(qPartName);
			overrideConf.setDefinition(qTypePartName(definition));
			addProcessor(overrideConf);
		}
	}

	@Override
	public TLModule createModule(ModuleConfig moduleConf) {
		CreateTLModuleProcessor.Config newModule = newConfigItem(CreateTLModuleProcessor.Config.class);
		String moduleName = moduleConf.getName();
		newModule.setName(moduleName);
		copyAnnotations(moduleConf, newModule);
		addProcessor(newModule);

		/* Creating module must occur before the types are created. */
		return super.createModule(moduleConf);
	}

	@Override
	public Void visit(RenamePart diff, Void arg) throws RuntimeException {
		TLObject part = resolvePart(diff.getPart());
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
			UpdateTLAssociationEndProcessor.Config config = newConfigItem(UpdateTLAssociationEndProcessor.Config.class);
			config.setName(oldName);
			config.setNewName(newName);
			addProcessor(config);
		} else {
			throw new UnsupportedOperationException("No rename for '" + diff.getPart() + "' of type '"
					+ part.getClass().getName() + "' possible.");
		}

		/* Renames part, so it must be executed *after* resolving part. */
		return super.visit(diff, arg);
	}

	@Override
	public Void visit(MoveClassifier diff, Void arg) throws RuntimeException {
		super.visit(diff, arg);

		addMoveTLTypePart(qTypePartName(diff.getClassifier()), diff.getBefore());
		return null;
	}

	/**
	 * {@link MigrationProcessor} to move part is created in {@link #movePart(String, String)}.
	 * 
	 * @see #movePart(String, String)
	 */
	@Override
	public Void visit(MoveStructuredTypePart diff, Void arg) throws RuntimeException {
		return super.visit(diff, arg);
	}

	@Override
	protected void movePart(String qPartName, String beforeName) {
		super.movePart(qPartName, beforeName);

		addMoveTLTypePart(qTypePartName(qPartName), beforeName);
	}

	@Override
	protected void movePart(TLStructuredType type, String partName, String beforeName) {
		super.movePart(type, partName, beforeName);

		addMoveTLTypePart(qTypePartName(TLModelUtil.qualifiedName(type), partName), beforeName);
	}

	private void addMoveTLTypePart(QualifiedPartName partName, String before) {
		ReorderTLTypePart.Config config = newConfigItem(ReorderTLTypePart.Config.class);
		config.setName(partName);
		config.setBefore(before);
		addProcessor(config);
	}

	@Override
	public void complete() {
		super.complete();
		new ConstraintChecker().check(log(), _processors);
	}

	private boolean addProcessor(PolymorphicConfiguration<? extends MigrationProcessor> p) {
		return _processors.getProcessors().add(p);
	}

	private static void failUnimplemented() {
		throw new UnsupportedOperationException("Generated stub");
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
		dest.setDBPrecision(source.getDBPrecision());
		dest.setDBSize(source.getDBSize());
		dest.setBinary(source.isBinary());
	}

}
