/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.diff.apply;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.Log;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.basic.config.TypedConfiguration;
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
import com.top_logic.element.model.diff.config.UpdateMandatory;
import com.top_logic.element.model.diff.config.visit.DiffVisitor;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLNamedPart;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.security.RoleConfig;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.error.TopLogicException;

/**
 * Algorithm applying {@link DiffElement}s to existing {@link TLModel} instances.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ApplyModelPatch extends ModelResolver implements DiffVisitor<Void, Void, RuntimeException> {

	/**
	 * {@link DiffVisitor} computing a priority to sort {@link DiffElement}s before applying a
	 * patch.
	 * 
	 * <p>
	 * The priority of a patch element is higher, if the returned number is smaller.
	 * </p>
	 */
	static final class DiffPriority implements DiffVisitor<Integer, Void, RuntimeException> {

		/**
		 * Singleton {@link ApplyModelPatch.DiffPriority} instance.
		 */
		public static final DiffPriority INSTANCE = new ApplyModelPatch.DiffPriority();

		private DiffPriority() {
			// Singleton constructor.
		}

		@Override
		public Integer visit(CreateModule diff, Void arg) throws RuntimeException {
			return 10;
		}

		@Override
		public Integer visit(CreateSingleton diff, Void arg) throws RuntimeException {
			return 100;
		}

		@Override
		public Integer visit(CreateRole diff, Void arg) throws RuntimeException {
			return 110;
		}

		@Override
		public Integer visit(CreateType diff, Void arg) throws RuntimeException {
			return 20;
		}

		@Override
		public Integer visit(CreateStructuredTypePart diff, Void arg) throws RuntimeException {
			return 30;
		}

		@Override
		public Integer visit(CreateClassifier diff, Void arg) throws RuntimeException {
			return 30;
		}

		@Override
		public Integer visit(Delete diff, Void arg) throws RuntimeException {
			if (diff.getName().indexOf('#') > 0) {
				// A part (type part or singleton).
				return 2;
			}
			if (diff.getName().indexOf(':') > 0) {
				// A type.
				return 4;
			}
			// A module.
			return 6;
		}

		@Override
		public Integer visit(DeleteRole diff, Void arg) throws RuntimeException {
			return 5;
		}

		@Override
		public Integer visit(AddAnnotations diff, Void arg) throws RuntimeException {
			return 40;
		}

		@Override
		public Integer visit(RemoveAnnotation diff, Void arg) throws RuntimeException {
			return 1;
		}

		@Override
		public Integer visit(AddGeneralization diff, Void arg) throws RuntimeException {
			return 25;
		}

		@Override
		public Integer visit(RemoveGeneralization diff, Void arg) throws RuntimeException {
			// Must occur before removing types, because removing types removes specializations.
			return 3;
		}

		@Override
		public Integer visit(MoveGeneralization diff, Void arg) throws RuntimeException {
			return 25;
		}

		@Override
		public Integer visit(MakeAbstract diff, Void arg) throws RuntimeException {
			return 27;
		}

		@Override
		public Integer visit(MakeConcrete diff, Void arg) throws RuntimeException {
			return 27;
		}

		@Override
		public Integer visit(UpdateMandatory diff, Void arg) throws RuntimeException {
			return 40;
		}

		@Override
		public Integer visit(MoveClassifier diff, Void arg) throws RuntimeException {
			return 30;
		}

		@Override
		public Integer visit(MoveStructuredTypePart diff, Void arg) throws RuntimeException {
			return 30;
		}

		@Override
		public Integer visit(RenamePart diff, Void arg) throws RuntimeException {
			return 30;
		}
	}

	/**
	 * Creates a {@link ApplyModelPatch}.
	 */
	public ApplyModelPatch(Protocol log, TLModel model, TLFactory factory) {
		super(log, model, factory);
	}

	/**
	 * Applies all given diffs to the {@link #getModel() encapsulated model}.
	 */
	public void applyPatch(List<DiffElement> patch) {
		List<DiffElement> elements = new ArrayList<>(patch);
		Collections.sort(elements,
			(d1, d2) -> Integer.compare(d1.visit(DiffPriority.INSTANCE, null), d2.visit(DiffPriority.INSTANCE, null)));
		for (DiffElement diff : elements) {
			diff.visit(this, null);
		}
	}

	@Override
	public Void visit(AddAnnotations diff, Void arg) throws RuntimeException {
		String partName = diff.getPart();
		TLModelPart part;
		try {
			part = resovePart(partName);
		} catch (TopLogicException ex) {
			log().info("Merge conflict: Adding annotations to '" + partName + "', but part does not exist.", Log.WARN);
			return null;
		}

		for (TLAnnotation annotation : diff.getAnnotations()) {
			log().info("Adding annotation '" + annotation.getConfigurationInterface().getName() + "' to '" + partName
				+ "'.");
			part.setAnnotation(TypedConfiguration.copy(annotation));
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
		generalizations.add(index, generalization);
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
		return null;
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

		// Apply order, since create API has no order attribute.
		String beforeName = diff.getBefore();
		schedule().reorderProperties(() -> movePart(type, partName, beforeName));
		
		return null;
	}

	private void movePart(TLStructuredType type, String partName, String beforeName) {
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

			TLClassPart part = (TLClassPart) type.getPart(partName);
			movePart(part, before);
		}
	}

	@Override
	public Void visit(CreateClassifier diff, Void arg) throws RuntimeException {
		TLEnumeration type;
		try {
			type = (TLEnumeration) resoveQName(diff.getType());
		} catch (TopLogicException ex) {
			log().info("Merge conflict: Adding classifier '" + diff.getClassifier().getName() + " to enumeration '"
				+ diff.getType() + "', but enumeration does not exist.", Log.WARN);
			return null;
		}

		TLClassifier existing = type.getClassifier(diff.getClassifier().getName());
		if (existing != null) {
			log().info(
				"Merge conflict: Adding classifier '" + diff.getClassifier().getName() + "' to enumeration '"
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

		log().info("Adding classifier '" + diff.getClassifier().getName() + " to enumeration '" + diff.getType() + "'.");
		addClassifier(type, diff.getClassifier(), before);
		return null;
	}

	@Override
	public Void visit(Delete diff, Void arg) throws RuntimeException {
		TLObject part;
		try {
			part = resoveQName(diff.getName());
		} catch (TopLogicException ex) {
			log().info(
				"Merge conflict: Deleting '" + diff.getName() + "' but it does not exist.", Log.INFO);
			return null;
		}
		
		if (part instanceof TLClass) {
			TLClass type = (TLClass) part;
			if (!type.isAbstract()) {
				try (CloseableIterator<TLObject> it = MetaElementUtil.iterateDirectInstances(type, TLObject.class)) {
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
			part.tDelete();
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
			part = (TLStructuredTypePart) resoveQName(diff.getPart());
		} catch (TopLogicException ex) {
			log().info(
				"Merge conflict: Updating mandatory state of '" + diff.getPart() + "' to '" + diff.isMandatory()
					+ "', but part does not exist.",
				Log.WARN);
			return null;
		}
		log().info("Updating mandatory state of '" + diff.getPart() + "' to '" + diff.isMandatory() + "'.");
		part.setMandatory(diff.isMandatory());
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
	 * @param partName
	 *        Qualified name of the part to move.
	 * @param beforeName
	 *        Local name of the part to move the given part before.
	 */
	private void movePart(String partName, String beforeName) {
		TLClassPart part;
		try {
			part = (TLClassPart) resoveQName(partName);
		} catch (TopLogicException ex) {
			log().info(
				"Merge conflict: Moved part '" + partName + "' does not exist.",
				Log.INFO);
			return;
		}

		TLStructuredTypePart before;
		if (beforeName != null) {
			before = part.getOwner().getPart(beforeName);
			if (before == null) {
				log().info(
					"Merge conflict: Reference part '" + beforeName + "' for moving part '" + partName
						+ "' does not exist.",
					Log.INFO);
				return;
			}
			if (before.getOwner() != part.getOwner()) {
				log().info(
					"Merge conflict: Reference part '" + beforeName + "' for moving part '" + partName
						+ "' is declared in super type.",
					Log.INFO);
				return;
			}
		} else {
			before = null;
		}

		String pos = before == null ? "to the end" : "before part '" + beforeName + "'";
		log().info("Moving part '" + partName + "' " + pos + ".", Log.INFO);
		movePart(part, before);
	}

	private void movePart(TLClassPart part, TLStructuredTypePart before) {
		List<TLClassPart> localParts = part.getOwner().getLocalClassParts();
		localParts.remove(part);
		int index = before == null ? localParts.size() : localParts.indexOf(before);
		localParts.add(index, part);
	}

	@Override
	public Void visit(RemoveAnnotation diff, Void arg) throws RuntimeException {
		TLModelPart part;
		try {
			part = resovePart(diff.getPart());
		} catch (TopLogicException ex) {
			log().info(
				"Merge conflict: Removing annotation '" + diff.getAnnotation().getName() + "' from '" + diff.getPart()
					+ "', but part does not exist.",
				Log.INFO);
			return null;
		}
		log().info("Removing annotation '" + diff.getAnnotation().getName() + "' from '" + diff.getPart() + "'.");
		part.removeAnnotation(diff.getAnnotation());
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
		return null;
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

		List<TLObject> directInstances = MetaElementUtil.getAllDirectInstancesOf(type, TLObject.class);
		if (!directInstances.isEmpty()) {
			log().info(
				"Merge conflict: Cannot make '" + diff.getType() + "' abstract, " + directInstances.size()
						+ " instances exist.",
				Log.INFO);
			return null;
		}

		log().info("Making type abstract: " + diff.getType());
		type.setAbstract(true);

		return null;
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

		return null;
	}

	@Override
	public Void visit(RenamePart diff, Void arg) throws RuntimeException {
		TLNamedPart part;
		try {
			log().info("Renaming '" + diff.getPart() + "' to '" + diff.getNewName() + "'.");
			part = (TLNamedPart) resoveQName(diff.getPart());
		} catch (TopLogicException ex) {
			log().info(
				"Merge conflict: Renaming '" + diff.getPart() + "' to '" + diff.getNewName()
					+ "', but part does not exist.",
				Log.WARN);
			return null;
		}
		part.setName(diff.getNewName());
		return null;
	}

	private TLModelPart resovePart(String qname) throws TopLogicException {
		return (TLModelPart) resoveQName(qname);
	}

	private TLObject resoveQName(String qname) throws TopLogicException {
		return TLModelUtil.resolveQualifiedName(getModel(), qname);
	}

	private TLClass asClass(TLType type) {
		return (TLClass) type;
	}

}
