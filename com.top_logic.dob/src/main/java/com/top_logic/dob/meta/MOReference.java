/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.meta;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.dob.AttributeStorage;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ReferenceStorage;
import com.top_logic.dob.attr.AbstractMOReference;
import com.top_logic.dob.schema.config.ReferenceAttributeConfig;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.DBMetaObject;

/**
 * A {@link MOReference} describes a reference to some object.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface MOReference extends MOAttribute {
	
	/**
	 * Specification of how to react on the deletion of an object stored in a reference.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public enum DeletionPolicy implements ExternallyNamed {
		
		/**
		 * The deleted object is removed from the reference.
		 * 
		 * <p>
		 * A reference that can store only a single object is reset to <code>null</code>, if the
		 * referenced object is deleted.
		 * </p>
		 */
		CLEAR_REFERENCE("clear-reference"),
		
		/**
		 * The object that refers to the deleted object is also deleted.
		 * 
		 * <p>
		 * Note: When using this setting on a reference that can contain multiple values, deleting
		 * one of the referenced objects deletes the referring object, but not the other objects
		 * also referenced. To also delete those sibling objects, also mark the reference as
		 * composition.
		 * </p>
		 */
		DELETE_REFERER("delete-referer"),
		
		/**
		 * The reference to a deleted object is stabilized. This means that the reference is
		 * modified to point to the the historic version of the referenced object directly before
		 * the deletion.
		 * 
		 * <p>
		 * This setting is only possible for a reference of "mixed" history type (meaning that the
		 * reference may contain current and historic objects).
		 * </p>
		 */
		STABILISE_REFERENCE("stabilise-reference"),
		
		/**
		 * The deletion of a referenced object fails, if the reference is not explicitly cleared in
		 * the same transaction or the referring object is deleted altogether with the referenced
		 * object.
		 */
		VETO("veto"),
		;

		private final String _externalName;
		
		private DeletionPolicy(String externalName) {
			this._externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}

		/**
		 * Service method to break control flow when an {@link DeletionPolicy} does not exists,
		 * resp. a case for a {@link DeletionPolicy} is not covered.
		 */
		public static RuntimeException noSuchPolicy(DeletionPolicy policy) {
			throw new UnreachableAssertion("No such " + DeletionPolicy.class + ": " + policy);
		}
	}
	
	/**
	 * Classification of allowed values of a reference regarding to their history context.
	 * 
	 * @see MOReference#getHistoryType()
	 */
	public enum HistoryType implements ExternallyNamed {
		
		/**
		 * The reference is a current value. It can not be filled with historic objects.
		 * 
		 * <p>
		 * This is the most common type of reference. It is most efficient one in terms of space and
		 * time.
		 * </p>
		 */
		CURRENT("current"),

		/**
		 * The reference is a historic value. If it is filled with a current object, the object is
		 * stabilized to the revision currently created.
		 */
		HISTORIC("historic"),

		/**
		 * The reference is an attribute which contains any a current value. It
		 * can not be filled with historic objects and current objects. Current
		 * objects remain current.
		 */
		MIXED("mixed"), 
		;

		private final String _externalName;

		private HistoryType(String externalName) {
			this._externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}

		/**
		 * Service method to break control flow when an {@link HistoryType} does not exists, resp. a
		 * case for a {@link HistoryType} is not covered.
		 */
		public static RuntimeException noSuchType(HistoryType historyType) {
			throw new UnreachableAssertion("No such " + HistoryType.class + ": " + historyType);
		}
	}
	
	/**
	 * Constants to access certain aspects of the referenced object.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	public enum ReferencePart implements ExternallyNamed {

		/**
		 * constant to access the name of the reference
		 */
		name("name"),

		/**
		 * constant to access the history context of the reference
		 */
		revision("revision"),

		/**
		 * constant to access the branch context of the reference
		 */
		branch("branch"),

		/**
		 * constant to access the type of the reference
		 */
		type("type"),
		
		;
		
		private String _externalName;
		
		private ReferencePart(String externalName) {
			this._externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}

		/**
		 * Service method to break control flow when an {@link ReferencePart} does not exists, resp.
		 * a case for a {@link ReferencePart} is not covered.
		 */
		public static RuntimeException noSuchPart(ReferencePart part) {
			throw new UnreachableAssertion("No such " + ReferencePart.class + ": " + part);
		}

		/**
		 * The column name of this {@link ReferencePart}.
		 * 
		 * @param dbNamePrefix
		 *        The mangled DB name of the reference attribute.
		 */
		public String getReferenceAspectColumnName(String dbNamePrefix) {
			switch (this) {
				case name: {
					return dbNamePrefix + AbstractMOReference.KO_ATT_SUFFIX_ID;
				}
				case revision: {
					return dbNamePrefix + AbstractMOReference.KO_ATT_SUFFIX_REVISION;
				}
				case branch: {
					return dbNamePrefix + AbstractMOReference.KO_ATT_SUFFIX_BRANCH;
				}
				case type: {
					return dbNamePrefix + AbstractMOReference.KO_ATT_SUFFIX_TYPE;
				}
			}
			throw noSuchPart(this);
		}
	}

	/** @see #getDeletionPolicy() */
	public static final DeletionPolicy DEFAULT_DELETION_POLICY = DeletionPolicy.CLEAR_REFERENCE;
	
	/** @see #getHistoryType() */
	public static final HistoryType DEFAULT_HISTORY_TYPE = HistoryType.CURRENT;

	/**
	 * Whether this reference is monomorphic.
	 * 
	 * <p>
	 * A monomorphic reference can only point to objects of the exact type given in
	 * {@link #getMetaObject()}, not to subtypes thereof. Monomporphic references are more efficient
	 * in terms of time and space.
	 * </p>
	 * 
	 * @see #getMetaObject()
	 */
	public boolean isMonomorphic();

	/**
	 * The type of the target of the reference. It can also be of a subtype of
	 * the returned type if the reference is not {@link #isMonomorphic()
	 * monomorphic}.
	 * 
	 * @see #isMonomorphic()
	 */
	@Override
	public MetaObject getMetaObject();
	
	/**
	 * Sets the target type of the reference.
	 * 
	 * @see com.top_logic.dob.MOAttribute#setMetaObject(com.top_logic.dob.MetaObject)
	 */
	@Override
	public void setMetaObject(MetaObject type);

	/**
	 * Whether this reference can point to historic objects.
	 * 
	 * If nothing special was set, the history type is {@link MOReference#DEFAULT_HISTORY_TYPE}.
	 */
	public HistoryType getHistoryType();

	/**
	 * Whether this reference can point to objects in other branches.
	 * 
	 * <p>
	 * A branch local reference can only point to objects of the branch the referring object lives
	 * in. Branch local references are more efficient in terms of time and space.
	 * </p>
	 */
	public boolean isBranchGlobal();

	/**
	 * Set the value of {@link #isBranchGlobal()}
	 * 
	 * Must not be called on a {@link #isFrozen()} attribute.
	 * 
	 * @param branchGlobal
	 *        the new value.
	 */
	public void setBranchGlobal(boolean branchGlobal);

	/**
	 * Set the value of {@link #getHistoryType()}
	 * 
	 * Must not be called on a {@link #isFrozen()} attribute.
	 * 
	 * @param type
	 *        the new value.
	 */
	public void setHistoryType(HistoryType type);

	/**
	 * Set the value of {@link #isMonomorphic()}
	 * 
	 * Must not be called on a {@link #isFrozen()} attribute.
	 * 
	 * @param monomorphic
	 *        the new value.
	 */
	public void setMonomorphic(boolean monomorphic);
	
	/**
	 * Returns the database column used for the given type. If there is no
	 * column to access the given aspect, e.g. the attribute is
	 * {@link #isMonomorphic() monomorph} and the column of type
	 * {@link ReferencePart#type} is requested, then <code>null</code> will be
	 * returned.
	 * 
	 * @param part
	 *        the type of the reference for which a database column is
	 *        requested.
	 * 
	 * @return the used {@link DBAttribute} for the requested type or
	 *         <code>null</code> if there is no persistent representation of
	 *         that type.
	 * 
	 * @see MOReference#getPart(DBAttribute)
	 */
	public DBAttribute getColumn(ReferencePart part);
	
	/**
	 * Returns the {@link ReferencePart} the given {@link DBAttribute}
	 * describes.
	 * 
	 * @param attribute
	 *        must not be <code>null</code>
	 * @return never <code>null</code>
	 * 
	 * @throws IllegalArgumentException
	 *         if the given attribute is not a {@link DBAttribute} of this
	 *         reference.
	 * @throws NullPointerException
	 *         if the given attribute is <code>null</code>
	 * 
	 * @see #getColumn(ReferencePart)
	 */
	public ReferencePart getPart(DBAttribute attribute);
	
	/**
	 * Returns the database type of the given reference aspect.
	 */
	public DBMetaObject getType(ReferencePart part);
	
	/**
	 * Whether the referencing object is the container of the referenced content object.
	 * 
	 * <p>
	 * The life span of a content object is bound to the live span of the container object. This
	 * means that the referenced object is automatically deleted, if the referencing object is
	 * deleted.
	 * </p>
	 * 
	 * @see #getDeletionPolicy()
	 */
	boolean isContainer();
	
	/**
	 * @see #isContainer()
	 */
	void setContainer(boolean isContainer);
	
	/**
	 * Policy how to react upon deletion of the referenced object.
	 * 
	 * <p>
	 * If nothing special was set, the deletion policy is
	 * {@link MOReference#DEFAULT_DELETION_POLICY}.
	 * </p>
	 * 
	 * @see #isContainer()
	 */
	DeletionPolicy getDeletionPolicy();
	
	/**
	 * @see #getDeletionPolicy() 
	 */
	void setDeletionPolicy(DeletionPolicy policy);
	
	/**
	 * Whether to create a default index for the reference.
	 * 
	 * <p>
	 * This should be set to <code>false</code>, to avoid duplicate indices in case a custom index
	 * is created that is "as good as" the default reference index that is created for each
	 * reference by default.
	 * </p>
	 * 
	 * @see ReferenceAttributeConfig#isUseDefaultIndex()
	 * @see #useDefaultIndex(boolean)
	 */
	boolean useDefaultIndex();

	/**
	 * Whether this {@link MOReference} shall use a default index.
	 * 
	 * @param b
	 *        if <code>false</code> then {@link #getIndex()} returns no index.
	 */
	void useDefaultIndex(boolean b);

	/**
	 * Creates an index to fasten search for the value of this reference.
	 * 
	 * @return May be <code>null</code>, in case no index is needed.
	 */
	MOIndex getIndex();

	/**
	 * Special {@link AttributeStorage} for {@link MOReference}.
	 * 
	 * @see com.top_logic.dob.MOAttribute#getStorage()
	 */
	@Override
	ReferenceStorage getStorage();

}
