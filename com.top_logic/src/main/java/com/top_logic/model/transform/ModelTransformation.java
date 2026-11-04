/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.transform;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.Protocol;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModelVisitor;
import com.top_logic.model.TLProperty;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.model.visit.DefaultTLModelVisitor;

/**
 * Base class for {@link TLModel} transformations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ModelTransformation {

	/** The {@link Protocol} for error reporting. */
	protected final Protocol log;
	/** The model to transform. */
	protected final TLModel index;

	/**
	 * Creates a {@link ModelTransformation}.
	 * 
	 * @param log
	 *        The {@link Protocol} for error reporting.
	 * @param index
	 *        The indexed model.
	 */
	public ModelTransformation(Protocol log, TLModel index) {
		this.log = log;
		this.index = index;
	}
	
	/**
	 * Transforms the model passed to the constructor.
	 */
	public abstract void transform();

	
	/**
	 * Removes the given class from the model. Its generalizations are inlined into the extends
	 * declaration of its specializations.
	 */
	protected void removeClass(TLClass clazz) {
		inlineExtendsIntoSpecializations(clazz);
		internalDropClass(clazz);
	}

	/**
	 * Removes the given class from the model. It is just removed from the extends declaration of
	 * its specializations.
	 */
	protected void dropClass(TLClass clazz) {
		removeFromExtendsOfSpecializations(clazz);
		internalDropClass(clazz);
	}

	private void internalDropClass(TLClass clazz) {
		List<TLAssociation> privateAssociations = new ArrayList<>();
		for (TLReference reference : TLModelUtil.getLocalReferences(clazz)) {
			TLAssociation association = reference.getEnd().getOwner();
			if (TLModelUtil.isPrivateAssociation(association)) {
				privateAssociations.add(association);
			}
		}
		
		for (TLAssociation privateAssociation : privateAssociations) {
			removeAssociation(privateAssociation);
		}
		
		clazz.getScope().getClasses().remove(clazz);
	}
	
	/** Removes the given {@link TLEnumeration} from its scope. */
	protected void removeEnumeration(TLEnumeration enumeration) {
		enumeration.getScope().getTypes().remove(enumeration);
	}
	
	/**
	 * Replaces the given class in the extends declaration of all its specializations with the
	 * generalizations of the given class.
	 */
	protected void inlineExtendsIntoSpecializations(TLClass clazz) {
		// Inline all super classes into the extends declaration of all
		// specializations of the removed class.
		for (TLClass specialization : new ArrayList<>(clazz.getSpecializations())) {
			// Copy to preserve overall order of extends during transformation.
			List<TLClass> specializationGeneralizations = new ArrayList<>(specialization.getGeneralizations());
			
			int extendsIndex = specializationGeneralizations.indexOf(clazz);
			assert extendsIndex >= 0 : "Class '" + clazz + "' not found in specialization '" + specialization + "' extends declaration.";
			
			specializationGeneralizations.remove(extendsIndex);
			specializationGeneralizations.addAll(extendsIndex, clazz.getGeneralizations());
			
			specialization.getGeneralizations().clear();
			specialization.getGeneralizations().addAll(specializationGeneralizations);
			
			for (TLClass extendedClass : clazz.getGeneralizations()) {
			}
		}
	}
	
	/** Removes the given class from the extends declaration of all its specializations. */
	protected void removeFromExtendsOfSpecializations(TLClass clazz) {
		for (TLClass specialization : new ArrayList<>(clazz.getSpecializations())) {
			specialization.getGeneralizations().remove(clazz);
		}
	}
	
	/**
	 * Removes the given {@link TLAssociation} and all references implemented by its ends from the
	 * model.
	 */
	protected void removeAssociation(TLAssociation association) {
		// Remove references that are implemented by the removed association's
		// ends.
		for (TLAssociationEnd end : TLModelUtil.getCopiedEnds(association)) {
			TLReference reference = end.getReference();
			if (reference != null) {
				removeReferenceLocally(reference);
			}
		}
		
		// Unlink from unions.
		association.getUnions().clear();
		
		// Unlink from subsets.
		for (TLAssociation subset : new ArrayList<>(association.getSubsets())) {
			subset.getUnions().remove(association);
		}
		
		// Remove the association itself.
		association.getScope().getAssociations().remove(association);
	}

	/**
	 * Removes the given {@link TLReference} from its owner. If the reference is implemented by a
	 * private association, the association is removed as well.
	 */
	protected void removeReference(TLReference reference) {
		TLAssociation association = reference.getEnd().getOwner();
		
		if (TLModelUtil.isPrivateAssociation(association)) {
			removeAssociation(association);
		} else {
			removeReferenceLocally(reference);
			
			// Detach from association end.
			reference.setEnd(null);
		}
	}

	private void removeReferenceLocally(TLReference reference) {
		// First remove from index, because after detaching from end, there the
		// reference type is no longer known.
		reference.getOwner().getLocalParts().remove(reference);
	}
	
	/** Removes the given {@link TLProperty} from its owner. */
	protected void removeProperty(TLProperty property) {
		property.getOwner().getLocalParts().remove(property);
	}
	
	/** Removes the given {@link TLAssociationEnd} from its association. */
	protected void removeEnd(TLAssociationEnd end) {
		end.getOwner().getLocalParts().remove(end);
	}

	/** Removes the given {@link TLModelPart} from the model. */
	protected void removePart(TLModelPart model) {
		model.visit(REMOVER, index);
	}
	
	/** Sets the type of the given part to the given new type. */
	protected void changeType(TLTypePart part, TLClass newType) {
		TLType oldType = part.getType();
		part.setType(newType);
	}
	
	/** Adds the given new class to the generalizations of the given class. */
	protected void addExtends(TLClass clazz, TLClass newExtendedClass) {
		clazz.getGeneralizations().add(newExtendedClass);
	}

	/**
	 * Visitor removing the visited {@link TLModelPart} from the model.
	 *
	 * @see #removePart(TLModelPart)
	 */
	protected final TLModelVisitor<Void, TLModel> REMOVER = new DefaultTLModelVisitor<>() {
		@Override
		public Void visitAssociation(TLAssociation model, TLModel arg) {
			removeAssociation(model);
			return super.visitAssociation(model, arg);
		}
		
		@Override
		public Void visitClass(TLClass model, TLModel arg) {
			removeClass(model);
			return super.visitClass(model, arg);
		}
		
		@Override
		public Void visitEnumeration(TLEnumeration model, TLModel arg) {
			removeEnumeration(model);
			return super.visitEnumeration(model, arg);
		}
		
		@Override
		public Void visitReference(TLReference model, TLModel arg) {
			removeReference(model);
			return super.visitReference(model, arg);
		}
		
		@Override
		public Void visitProperty(TLProperty model, TLModel arg) {
			removeProperty(model);
			return super.visitProperty(model, arg);
		}
		
		@Override
		public Void visitAssociationEnd(TLAssociationEnd model, TLModel arg) {
			removeEnd(model);
			return super.visitAssociationEnd(model, arg);
		}
		
	};

}
