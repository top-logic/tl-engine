/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.diff.compare;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.diff.CollectionDiff;
import com.top_logic.basic.col.diff.SetDiff;
import com.top_logic.basic.col.diff.op.Create;
import com.top_logic.basic.col.diff.op.DiffOp;
import com.top_logic.basic.col.diff.op.DiffOp.Visitor;
import com.top_logic.basic.col.diff.op.Move;
import com.top_logic.basic.col.diff.op.Update;
import com.top_logic.basic.config.InstanceAccess;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.basic.util.Utils;
import com.top_logic.element.config.ModuleConfig;
import com.top_logic.element.config.PartConfig;
import com.top_logic.element.config.SingletonConfig;
import com.top_logic.element.config.annotation.TLSingletons;
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
import com.top_logic.element.model.diff.config.UpdateMandatory;
import com.top_logic.element.model.export.ModelConfigExtractor;
import com.top_logic.model.ModelKind;
import com.top_logic.model.StorageDetail;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModelVisitor;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLNamedPart;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLProperty;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypeVisitor;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.security.RoleConfig;
import com.top_logic.model.annotate.security.TLRoleDefinitions;
import com.top_logic.model.config.TypeConfig;
import com.top_logic.model.util.TLModelUtil;

/**
 * Algorithm comparing two {@link TLModel}s and constructing a List of {@link DiffElement}s that
 * would transform the left {@link TLModel} into the right {@link TLModel}.
 * 
 * @see #addPatch(TLModel, TLModel)
 * @see #getPatch()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CreateModelPatch {

	private List<DiffElement> _diffs = new ArrayList<>();

	final ModelConfigExtractor _configExtractor = new ModelConfigExtractor();

	private final TLModelVisitor<Void, TLModelPart> _creator = new TLModelVisitor<>() {

		@Override
		public Void visitModule(TLModule model, TLModelPart before) {
			createModule(model);
			return null;
		}

		@Override
		public Void visitClass(TLClass model, TLModelPart before) {
			createType(model);
			return null;
		}

		@Override
		public Void visitEnumeration(TLEnumeration model, TLModelPart before) {
			createType(model);
			return null;
		}

		@Override
		public Void visitPrimitive(TLPrimitive model, TLModelPart before) {
			createType(model);
			return null;
		}

		@Override
		public Void visitAssociation(TLAssociation model, TLModelPart before) {
			return null;
		}

		@Override
		public Void visitClassifier(TLClassifier model, TLModelPart before) {
			createClassifier(model, (TLClassifier) before);
			return null;
		}

		@Override
		public Void visitProperty(TLProperty model, TLModelPart before) {
			createStructuredTypePart(model, (TLStructuredTypePart) before);
			return null;
		}

		@Override
		public Void visitReference(TLReference model, TLModelPart before) {
			createStructuredTypePart(model, (TLStructuredTypePart) before);
			return null;
		}

		@Override
		public Void visitAssociationEnd(TLAssociationEnd model, TLModelPart before) {
			createStructuredTypePart(model, (TLStructuredTypePart) before);
			return null;
		}

		@Override
		public Void visitModel(TLModel model, TLModelPart before) {
			return null;
		}

	};

	private final TLTypeVisitor<Void, TLType> _typeDiff = new TLTypeVisitor<>() {
		@Override
		public Void visitPrimitive(TLPrimitive model, TLType arg) {
			addPatchPrimitive(model, (TLPrimitive) arg);
			return null;
		}

		@Override
		public Void visitEnumeration(TLEnumeration model, TLType arg) {
			addPatchEnumeration(model, (TLEnumeration) arg);
			return null;
		}

		@Override
		public Void visitClass(TLClass model, TLType arg) {
			addPatchClass(model, (TLClass) arg);
			return null;
		}

		@Override
		public Void visitAssociation(TLAssociation model, TLType arg) {
			// No changes supported.
			return null;
		}
	};

	abstract class PartOpPatcher<T extends TLNamedPart> implements Visitor<T, Void, Void> {
		@Override
		public Void visit(Update<? extends T> op, Void arg) {
			processAnnotationChanges(op);
			return null;
		}

		@Override
		public Void visit(com.top_logic.basic.col.diff.op.Delete<? extends T> op, Void arg) {
			delete(op.getDelted());
			return null;
		}

	}

	private final Visitor<TLClassifier, Void, Void> _classifiersDiff = new PartOpPatcher<>() {
		@Override
		public Void visit(Create<? extends TLClassifier> op, Void arg) {
			TLClassifier created = op.getItem();
			TLClassifier before = op.getBefore();

			createClassifier(created, before);
			return null;
		}

		@Override
		public Void visit(Move<? extends TLClassifier> op, Void arg) {
			TLClassifier movedLeft = op.getMovedLeft();
			processAnnotationChanges(movedLeft, op.getMovedRight());

			MoveClassifier result = TypedConfiguration.newConfigItem(MoveClassifier.class);
			result.setClassifier(TLModelUtil.qualifiedName(movedLeft));
			TLClassifier before = op.getBefore();
			result.setBefore(before == null ? null : before.getName());
			addDiff(result);
			return null;
		}
	};

	private Visitor<TLClass, Void, TLClass> _applyGeneralization = new Visitor<>() {

		@Override
		public Void visit(Create<? extends TLClass> op, TLClass arg) {
			addGeneralization(arg, op.getItem(), op.getBefore());
			return null;
		}

		private void addGeneralization(TLClass type, TLClass added, TLClass before) {
			AddGeneralization add = TypedConfiguration.newConfigItem(AddGeneralization.class);
			add.setType(TLModelUtil.qualifiedName(type));
			add.setGeneralization(TLModelUtil.qualifiedName(added));
			add.setBefore(before == null ? null : TLModelUtil.qualifiedName(before));
			addDiff(add);
		}

		@Override
		public Void visit(Update<? extends TLClass> op, TLClass arg) {
			return null;
		}

		@Override
		public Void visit(com.top_logic.basic.col.diff.op.Delete<? extends TLClass> op, TLClass arg) {
			TLClass delted = op.getDelted();
			removeGeneralization(arg, delted);
			return null;
		}

		private void removeGeneralization(TLClass clazz, TLClass delted) {
			RemoveGeneralization remove = TypedConfiguration.newConfigItem(RemoveGeneralization.class);
			remove.setType(TLModelUtil.qualifiedName(clazz));
			remove.setGeneralization(TLModelUtil.qualifiedName(delted));
			addDiff(remove);
		}

		@Override
		public Void visit(Move<? extends TLClass> op, TLClass arg) {
			MoveGeneralization move = TypedConfiguration.newConfigItem(MoveGeneralization.class);
			move.setType(TLModelUtil.qualifiedName(arg));
			move.setGeneralization(TLModelUtil.qualifiedName(op.getMovedLeft()));
			move.setBefore(op.getBefore() == null ? null : TLModelUtil.qualifiedName(op.getBefore()));
			addDiff(move);
			return null;
		}
	};

	private Visitor<TLClassPart, Void, Void> _partUpdater = new PartOpPatcher<>() {

		@Override
		public Void visit(Create<? extends TLClassPart> op, Void arg) {
			createStructuredTypePart(op.getItem(), op.getBefore());
			return null;
		}

		@Override
		public Void visit(Update<? extends TLClassPart> op, Void arg) {
			addPartUpdate(op.getLeft(), op.getRight(), op.getRightSuccessor());
			return null;
		}

		@Override
		public Void visit(Move<? extends TLClassPart> op, Void arg) {
			addPartMove(op.getMovedLeft(), op.getMovedRight(), op.getBefore());
			return null;
		}
	};

	private TLTypeVisitor<Boolean, TLType> _isCompatible = new TLTypeVisitor<>() {

		@Override
		public Boolean visitPrimitive(TLPrimitive model, TLType arg) {
			return isCompatiblePrimitive(model, (TLPrimitive) arg);
		}

		@Override
		public Boolean visitEnumeration(TLEnumeration model, TLType arg) {
			return true;
		}

		@Override
		public Boolean visitClass(TLClass model, TLType arg) {
			return true;
		}

		@Override
		public Boolean visitAssociation(TLAssociation model, TLType arg) {
			throw new UnreachableAssertion("Associations are not used as value types.");
		}

	};

	/**
	 * Analyzed differences in the given models and adds {@link DiffElement} to the current patch
	 * transforming the left model to the right model.
	 * 
	 * @param left
	 *        The original.
	 * @param right
	 *        The target.
	 */
	public void addPatch(TLModel left, TLModel right) {
		SetDiff<TLModule> diff = CreateModelPatch.diffSet(left.getModules(), right.getModules());

		processCreateDelete(diff);
		for (Update<? extends TLNamedPart> updated : diff.getUpdated()) {
			processAnnotationChanges(updated);
		}
		for (Update<TLModule> update : diff.getUpdated()) {
			addPatch(update.getLeft(), update.getRight());
		}
	}

	/**
	 * Analyzed differences in the given modules and adds {@link DiffElement} to the current patch
	 * transforming the left module to the right module.
	 * 
	 * @param left
	 *        The original.
	 * @param right
	 *        The target.
	 */
	public void addPatch(TLModule left, TLModule right) {
		// Associations are handled implicitly.
		processTypeDiff(left.getClasses(), right.getClasses());
		processTypeDiff(left.getEnumerations(), right.getEnumerations());
		processTypeDiff(left.getDatatypes(), right.getDatatypes());

		addSingletonsPatch(right, left.getAnnotation(TLSingletons.class), right.getAnnotation(TLSingletons.class));
		addRolesPatch(right, left.getAnnotation(TLRoleDefinitions.class), right.getAnnotation(TLRoleDefinitions.class));
	}

	private void processTypeDiff(Collection<? extends TLType> leftTypes, Collection<? extends TLType> rightTypes) {
		SetDiff<TLType> diff = CreateModelPatch.diffSet(leftTypes, rightTypes);
		processCreateDelete(diff);
		for (Update<TLType> update : diff.getUpdated()) {
			addPatch(update.getLeft(), update.getRight());
		}
	}

	/**
	 * Creates a patch for a singleton change in a {@link TLModule}.
	 */
	public void addSingletonsPatch(TLModule module, TLSingletons left, TLSingletons right) {
		Collection<SingletonConfig> leftSingletons = getSingletons(left);
		Collection<SingletonConfig> rightSingletons = getSingletons(right);

		SetDiff<SingletonConfig> diff =
			CollectionDiff.diffSet(SingletonConfig::getName, leftSingletons, rightSingletons);
		for (SingletonConfig singletonConfig : diff.getDeleted()) {
			addDelete(module, singletonConfig);
		}
		for (SingletonConfig singletonConfig : diff.getCreated()) {
			addCreate(module, singletonConfig);
		}
		for (Update<SingletonConfig> update : diff.getUpdated()) {
			SingletonConfig leftSingleton = update.getLeft();
			SingletonConfig rightSingleton = update.getRight();

			if (!leftSingleton.getTypeSpec().equals(rightSingleton.getTypeSpec())) {
				addDelete(module, leftSingleton);
				addCreate(module, rightSingleton);
			}
		}
	}

	private void addCreate(TLModule module, SingletonConfig singletonConfig) {
		CreateSingleton create = TypedConfiguration.newConfigItem(CreateSingleton.class);
		create.setModule(module.getName());
		create.setSingleton(TypedConfiguration.copy(singletonConfig));
		addDiff(create);
	}

	private void addDelete(TLModule module, SingletonConfig singletonConfig) {
		Delete delete = TypedConfiguration.newConfigItem(Delete.class);
		delete.setName(module.getName() + TLModelUtil.QUALIFIED_NAME_PART_SEPARATOR + singletonConfig.getName());
		addDiff(delete);
	}

	private Collection<SingletonConfig> getSingletons(TLSingletons annotation) {
		return annotation == null ? Collections.emptyList() : annotation.getSingletons();
	}

	/**
	 * Creates a patch for a singleton change in a {@link TLModule}.
	 */
	public void addRolesPatch(TLModule module, TLRoleDefinitions left, TLRoleDefinitions right) {
		Collection<RoleConfig> leftValues = getRoles(left);
		Collection<RoleConfig> rightValues = getRoles(right);

		SetDiff<RoleConfig> diff =
			CollectionDiff.diffSet(RoleConfig::getName, leftValues, rightValues);
		for (RoleConfig config : diff.getDeleted()) {
			addDelete(module, config);
		}
		for (RoleConfig config : diff.getCreated()) {
			addCreate(module, config);
		}
	}

	private void addCreate(TLModule module, RoleConfig config) {
		CreateRole create = TypedConfiguration.newConfigItem(CreateRole.class);
		create.setModule(module.getName());
		create.setRole(TypedConfiguration.copy(config));
		addDiff(create);
	}

	private void addDelete(TLModule module, RoleConfig config) {
		DeleteRole delete = TypedConfiguration.newConfigItem(DeleteRole.class);
		delete.setModule(module.getName());
		delete.setRole(config.getName());
		addDiff(delete);
	}

	private Collection<RoleConfig> getRoles(TLRoleDefinitions annotation) {
		return annotation == null ? Collections.emptyList() : annotation.getRoles();
	}

	/**
	 * Analyzed differences in the given types and adds {@link DiffElement} to the current patch
	 * transforming the left type to the right type.
	 * 
	 * @param left
	 *        The original.
	 * @param right
	 *        The target.
	 */
	public void addPatch(TLType left, TLType right) {
		if (left.getModelKind() == right.getModelKind()) {
			left.visitType(_typeDiff, right);
		} else {
			delete(left);
			createType(right);
		}
	}

	/**
	 * The patch created by {@link #addPatch(TLModel, TLModel)}.
	 */
	public List<DiffElement> getPatch() {
		return _diffs;
	}

	final void addPatchPrimitive(TLPrimitive model, TLPrimitive right) {
		if (isCompatiblePrimitive(model, right)) {
			processAnnotationChanges(model, right);
		} else {
			delete(model);
			createType(right);
		}
	}

	boolean isCompatiblePrimitive(TLPrimitive left, TLPrimitive right) {
		return Utils.equals(left.getDBPrecision(), right.getDBPrecision()) &&
			Utils.equals(left.getDBSize(), right.getDBSize()) &&
			Utils.equals(left.getDBType(), right.getDBType()) &&
			Utils.equals(left.getKind(), right.getKind()) &&
			Utils.equals(left.getStorageMapping(), right.getStorageMapping());
	}

	final void addPatchEnumeration(TLEnumeration model, TLEnumeration other) {
		processAnnotationChanges(model, other);

		List<DiffOp<TLClassifier>> diff =
			CreateModelPatch.diffList(model.getClassifiers(), other.getClassifiers());

		for (DiffOp<TLClassifier> op : diff) {
			op.visit(_classifiersDiff, null);
		}
	}

	final void addPatchClass(TLClass left, TLClass right) {
		processAnnotationChanges(left, right);

		boolean wasAbstract = left.isAbstract();
		boolean isAbstract = right.isAbstract();
		if (wasAbstract != isAbstract) {
			if (wasAbstract) {
				MakeConcrete result = TypedConfiguration.newConfigItem(MakeConcrete.class);
				result.setType(TLModelUtil.qualifiedName(right));
				addDiff(result);
			} else {
				MakeAbstract result = TypedConfiguration.newConfigItem(MakeAbstract.class);
				result.setType(TLModelUtil.qualifiedName(right));
				addDiff(result);
			}
		}

		{
			List<DiffOp<TLClassPart>> diff =
				CreateModelPatch.diffList(left.getLocalClassParts(), right.getLocalClassParts());
	
			for (DiffOp<TLClassPart> op : diff) {
				op.visit(_partUpdater, null);
			}
		}
	
		{
			List<DiffOp<TLClass>> diff =
				CreateModelPatch.diffList(left.getGeneralizations(), right.getGeneralizations());
			for (DiffOp<TLClass> op : diff) {
				op.visit(_applyGeneralization, left);
			}
		}
	}

	void addPartUpdate(TLStructuredTypePart left, TLStructuredTypePart right, TLClassPart rightSuccessor) {
		if (!isCompatiblePart(left, right)) {
			delete(left);

			createStructuredTypePart(right, rightSuccessor);
			reCreateInverseReference(left, right);
			return;
		}

		addPartChanges(left, right);
	}

	void addPartMove(TLStructuredTypePart left, TLStructuredTypePart right, TLStructuredTypePart before) {
		if (!isCompatiblePart(left, right)) {
			delete(left);

			createStructuredTypePart(right, before);
			reCreateInverseReference(left, right);
			return;
		}

		addPartChanges(left, right);

		MoveStructuredTypePart move = TypedConfiguration.newConfigItem(MoveStructuredTypePart.class);
		move.setPart(TLModelUtil.qualifiedName(left));
		if (before != null) {
			move.setBefore(before.getName());
		}
		addDiff(move);
	}

	/**
	 * When a {@link TLReference} is deleted, and there is a back reference for it, the back
	 * reference is automatically deleted. Therefore it must be ensured that it is also recreated.
	 */
	private void reCreateInverseReference(TLStructuredTypePart left, TLStructuredTypePart right) {
		if (!isReference(left)) {
			// No reference => no back reference.
			return;
		}
		TLReference referenceToDelete = (TLReference) left;
		boolean isForwardsReference = TLModelUtil.isForwardReference(referenceToDelete);
		if (!isForwardsReference) {
			// Back reference => ignore.
			return;
		}
		TLReference inverse = TLModelUtil.getForeignName(referenceToDelete);
		if (inverse == null) {
			// No inverse reference defined.
			return;
		}
		TLType newInverseOwner = right.getType();
		if (newInverseOwner.getModelKind() != ModelKind.CLASS) {
			// New target type is not a structured type. No inverse reference can be created.
			return;
		}
		TLStructuredTypePart before;
		if (TLModelUtil.qualifiedName(newInverseOwner).equals(TLModelUtil.qualifiedName(left.getType()))) {
			// Try to find the correct place the list of children
			List<? extends TLStructuredTypePart> localParts = inverse.getOwner().getLocalParts();
			int inverseIndex = localParts.indexOf(inverse);
			if (inverseIndex < 0) {
				Logger.warn(
					"Inverse reference '" + inverse + "' is not part of its owner '" + inverse.getOwner() + "'.",
					CreateModelPatch.class);
				return;
			}
			if (inverseIndex < localParts.size() - 1) {
				before = localParts.get(inverseIndex + 1);
			} else {
				// last child
				before = null;
			}
		} else {
			// inverse reference must be moved to new type. No reference can be determined.
			before = null;
		}

		createStructuredTypePart(inverse, (TLStructuredType) newInverseOwner, before);
	}
	private void addPartChanges(TLStructuredTypePart left, TLStructuredTypePart right) {
		processAnnotationChanges(left, right);

		boolean oldMandatory = left.isMandatory();
		boolean newMandatory = right.isMandatory();
		if (oldMandatory != newMandatory) {
			UpdateMandatory update = TypedConfiguration.newConfigItem(UpdateMandatory.class);
			update.setPart(TLModelUtil.qualifiedName(left));
			update.setMandatory(newMandatory);
			addDiff(update);
		}
	}

	private boolean isCompatiblePart(TLStructuredTypePart left, TLStructuredTypePart right) {
		return ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(storageConfig(left), storageConfig(right)) &&
			isCompatibleValueType(left.getType(), right.getType()) &&
				(!isProperty(left)
				|| isCompatibleProperty((TLProperty) left, (TLProperty) right))
			&&
				(!isReference(left)
				|| isCompatibleReference((TLReference) left, (TLReference) right));
	}

	private boolean isProperty(TLStructuredTypePart part) {
		return part.getModelKind() == ModelKind.PROPERTY;
	}

	private boolean isReference(TLStructuredTypePart part) {
		return part.getModelKind() == ModelKind.REFERENCE;
	}

	private boolean isCompatibleValueType(TLType left, TLType right) {
		return left.getModelKind() == right.getModelKind() &&
			isCorrespondingModule(left.getModule(), right.getModule()) &&
			left.getName().equals(right.getName()) &&
			left.visitType(_isCompatible, right);
	}

	private boolean isCorrespondingModule(TLModule left, TLModule right) {
		return left.getName().equals(right.getName());
	}

	private boolean isCompatibleProperty(TLProperty left, TLProperty right) {
		return left.isDerived() == right.isDerived() &&
			left.isMultiple() == right.isMultiple() &&
			left.isOrdered() == right.isOrdered();
	}

	private boolean isCompatibleReference(TLReference left, TLReference right) {
		return left.isDerived() == right.isDerived() &&
			left.isMultiple() == right.isMultiple() &&
			left.isOrdered() == right.isOrdered() &&
			left.isBag() == right.isBag();
	}

	private PolymorphicConfiguration<?> storageConfig(TLStructuredTypePart left) {
		StorageDetail impl = left.getStorageImplementation();
		if (impl == null) {
			return null;
		}
		return (PolymorphicConfiguration<?>) InstanceAccess.INSTANCE.getConfig(impl);
	}

	void processCreateDelete(SetDiff<? extends TLNamedPart> diff) {
		for (TLNamedPart deleted : diff.getDeleted()) {
			delete(deleted);
		}
		for (TLNamedPart created : diff.getCreated()) {
			created.visit(_creator, null);
		}
	}

	final void processAnnotationChanges(Update<? extends TLNamedPart> updated) {
		processAnnotationChanges(updated.getLeft(), updated.getRight());
	}

	void processAnnotationChanges(TLModelPart left, TLModelPart right) {
		SetDiff<TLAnnotation> diff =
			CollectionDiff.diffSet(TLAnnotation::getConfigurationInterface, left.getAnnotations(), right.getAnnotations());

		for (TLAnnotation removed : diff.getDeleted()) {
			removeAnnotation(left, removed);
		}

		AddAnnotations add = null;
		for (TLAnnotation added : diff.getCreated()) {
			add = addAnnotation(add, left, added);
		}

		for (Update<TLAnnotation> updated : diff.getUpdated()) {
			TLAnnotation leftAnnotation = updated.getLeft();
			TLAnnotation rightAnnotation = updated.getRight();

			if (!ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(leftAnnotation, rightAnnotation)) {
				removeAnnotation(left, leftAnnotation);
				add = addAnnotation(add, right, rightAnnotation);
			}
		}

		if (add != null) {
			addDiff(add);
		}
	}

	private AddAnnotations addAnnotation(AddAnnotations add, TLModelPart left, TLAnnotation added) {
		add = mkAddAnnotatons(add, left);
		add.getAnnotations().add(TypedConfiguration.copy(added));
		return add;
	}

	private AddAnnotations mkAddAnnotatons(AddAnnotations add, TLModelPart left) {
		if (add != null) {
			return add;
		}
		return createAddAnnotation(left);
	}

	private AddAnnotations createAddAnnotation(TLModelPart left) {
		AddAnnotations add = TypedConfiguration.newConfigItem(AddAnnotations.class);
		add.setPart(TLModelUtil.qualifiedName(left));
		return add;
	}

	private void removeAnnotation(TLModelPart left, TLAnnotation removed) {
		RemoveAnnotation remove = TypedConfiguration.newConfigItem(RemoveAnnotation.class);
		remove.setPart(TLModelUtil.qualifiedName(left));
		remove.setAnnotation(removed.getConfigurationInterface());
		addDiff(remove);
	}

	final void createModule(TLModule model) {
		CreateModule result = TypedConfiguration.newConfigItem(CreateModule.class);
		ModuleConfig config = _configExtractor.visitModule(model, null);
		result.setModule(config);
		addDiff(result);
	}

	final void createType(TLType model) {
		CreateType result = TypedConfiguration.newConfigItem(CreateType.class);
		TypeConfig config = (TypeConfig) model.visit(_configExtractor, null);
		result.setModule(TLModelUtil.qualifiedName(model.getModule()));
		result.setType(config);
		addDiff(result);
	}

	final void createStructuredTypePart(TLStructuredTypePart model, TLStructuredTypePart before) {
		createStructuredTypePart(model, model.getOwner(), before);
	}

	private void createStructuredTypePart(TLStructuredTypePart model, TLStructuredType newOwner,
			TLStructuredTypePart before) {
		CreateStructuredTypePart result = TypedConfiguration.newConfigItem(CreateStructuredTypePart.class);
		result.setType(TLModelUtil.qualifiedName(newOwner));
		result.setPart((PartConfig) model.visit(_configExtractor, null));
		if (before != null) {
			result.setBefore(before.getName());
		}
		addDiff(result);
	}

	final void createClassifier(TLClassifier created, TLClassifier before) {
		CreateClassifier result = TypedConfiguration.newConfigItem(CreateClassifier.class);
		result.setType(TLModelUtil.qualifiedName(created.getOwner()));
		result.setClassifierConfig(_configExtractor.visitClassifier(created, null));
		result.setBefore(before == null ? null : before.getName());
		addDiff(result);
	}

	final void delete(TLNamedPart deleted) {
		Delete result = TypedConfiguration.newConfigItem(Delete.class);
		result.setName(TLModelUtil.qualifiedName(deleted));
		addDiff(result);
	}

	void addDiff(DiffElement diff) {
		_diffs.add(diff);
	}

	/**
	 * Compares the two sets of {@link TLNamedPart}s.
	 * 
	 * @param left
	 *        Original items.
	 * @param right
	 *        Target set of items.
	 * @return {@link SetDiff} analyzing the differences.
	 */
	public static <T extends TLNamedPart> SetDiff<T> diffSet(Collection<? extends T> left, Collection<? extends T> right) {
		return CollectionDiff.diffSet(TLNamedPart::getName, left, right);
	}

	/**
	 * Compares the two lists of {@link TLNamedPart}s.
	 * 
	 * @param left
	 *        Original items.
	 * @param right
	 *        Target set of items.
	 * @return Description of differences.
	 */
	public static <T extends TLNamedPart> List<DiffOp<T>> diffList(List<? extends T> left, List<? extends T> right) {
		return CollectionDiff.diffList(TLNamedPart::getName, left, right);
	}

}
