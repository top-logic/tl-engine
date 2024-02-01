/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModelVisitor;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.model.visit.DefaultTLModelVisitor;

/**
 * Checks whether the given {@link TLModelPart}s can be deleted without destroying the model
 * consistency.
 * 
 * Must be complied with:
 * <ul>
 * <li>Deletion of a whole {@link TLModule}: Contained types must not have subtypes in other
 * modules.</li>
 * <li>{@link TLClass} must not have instances.</li>
 * <li>{@link TLClass} using {@link TLTypePart}s have to be deleted as well.</li>
 * </ul>
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class TLModelPartDeletionChecker extends DefaultTLModelVisitor<Void, Void> {

	private Collection<TLType> _toBeDeletedTypes = new HashSet<>();

	private Set<Pair<TLModelPart, ResKey>> _deleteConflictingModelParts = new HashSet<>();

	/**
	 * Creates a {@link TLModelVisitor} checking if the given {@link TLModelPart}s can be deleted.
	 * 
	 * The given model parts serves also as a context object to get information about all objects
	 * that should be removed.
	 */
	public TLModelPartDeletionChecker(Collection<TLModelPart> rootModelParts) {
		init(rootModelParts);
	}

	/**
	 * All {@link TLModelPart}s with reason {@link ResKey}s that prevent a deletion of the
	 * {@link TLModelPart}.
	 */
	public Set<Pair<TLModelPart, ResKey>> getDeleteConflictingModelParts() {
		return _deleteConflictingModelParts;
	}

	@Override
	protected Void visitStructuredType(TLStructuredType model, Void arg) {
		for (TLModelPart part : model.getLocalParts()) {
			part.visit(this, arg);
		}

		return super.visitStructuredType(model, arg);
	}

	@Override
	public Void visitClass(TLClass model, Void arg) {
		checkTypePartsUsingType(model);
		checkTypeInstances(model);
		checkTypeSpecializations(model, arg);

		return super.visitClass(model, arg);
	}

	private void checkTypeSpecializations(TLClass model, Void arg) {
		for (TLClass specialization : model.getSpecializations()) {
			if (_toBeDeletedTypes.contains(specialization)) {
				specialization.visit(this, arg);
			} else {
				ResKey2 errorKey = I18NConstants.ERROR_DELETE_TYPE_WITH_SPECIALIZATIONS__TYPE_SPECIALIZATION;
				addConflictingPart(specialization, errorKey.fill(model.getName(), specialization.getName()));
			}
		}
	}

	private void checkTypeInstances(TLClass model) {
		try (CloseableIterator<TLObject> instanceIt = MetaElementUtil.iterateDirectInstances(model, TLObject.class)) {
			if (instanceIt.hasNext()) {
				addConflictingPart(model, I18NConstants.ERROR_DELETE_TYPE_WITH_INSTANCES__TYPE.fill(model.getName()));
			}
		}
	}

	private void checkTypePartsUsingType(TLClass clazz) {
		for (TLStructuredTypePart typePart : TLModelUtil.getUsage(clazz.getModel(), clazz)) {
			if (typePart instanceof TLClassPart) {
				TLStructuredType owner = typePart.getOwner();

				if (!_toBeDeletedTypes.contains(owner)) {
					ResKey2 errorKey = I18NConstants.ERROR_DELETE_TYPE_USED_BY_TYPEPART__TYPE_TYPEPART;
					addConflictingPart(clazz, errorKey.fill(clazz.getName(), TLModelUtil.qualifiedName(typePart)));
				}
			}
		}
	}

	private boolean addConflictingPart(TLModelPart modelPart, ResKey errorKey) {
		return _deleteConflictingModelParts.add(new Pair<>(modelPart, errorKey));
	}

	@Override
	public Void visitEnumeration(TLEnumeration model, Void arg) {
		for (TLModelPart part : model.getClassifiers()) {
			part.visit(this, arg);
		}

		return super.visitEnumeration(model, arg);
	}

	@Override
	public Void visitModel(TLModel model, Void arg) {
		for (TLModelPart part : model.getModules()) {
			part.visit(this, arg);
		}

		return super.visitModel(model, arg);
	}

	@Override
	public Void visitModule(TLModule model, Void arg) {
		for (TLModelPart part : model.getDatatypes()) {
			part.visit(this, arg);
		}

		for (TLModelPart part : model.getClasses()) {
			part.visit(this, arg);
		}

		for (TLModelPart part : model.getAssociations()) {
			part.visit(this, arg);
		}

		for (TLModelPart part : model.getEnumerations()) {
			part.visit(this, arg);
		}

		return super.visitModule(model, arg);
	}

	private void init(Collection<TLModelPart> rootModelParts) {
		for(TLModelPart rootModelPart : rootModelParts) {
			if (rootModelPart instanceof TLModule) {
				_toBeDeletedTypes.addAll(((TLModule) rootModelPart).getTypes());
			} else if (rootModelPart instanceof TLType) {
				_toBeDeletedTypes.add((TLType) rootModelPart);
			}
		}
	}
}
