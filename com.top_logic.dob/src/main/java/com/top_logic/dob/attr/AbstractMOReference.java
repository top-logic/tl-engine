/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.attr;


import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.IdentifierTypes;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.DeferredMetaObject;
import com.top_logic.dob.meta.MOIndex;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.TypeContext;
import com.top_logic.dob.schema.config.ReferenceAttributeConfig;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.DBMetaObject;

/**
 * Abstract implementation of {@link MOReference}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractMOReference extends AbstractMOAttribute implements MOReference {

	/** Suffix for the name of the branch column of this reference (if exists). */
	public static final String KO_ATT_SUFFIX_BRANCH = "_BRC";

	/** Suffix for the name of the revision column of this reference (if exists). */
	public static final String KO_ATT_SUFFIX_REVISION = "_REV";

	/** Suffix for the name of the type column of this reference (if exists). */
	public static final String KO_ATT_SUFFIX_TYPE = "_TYPE";

	/** Suffix for the name of the ID column of this reference. */
	public static final String KO_ATT_SUFFIX_ID = "_ID";

	/**
	 * The column in the database responsible for the branch. Is <code>null</code> iff the attribute
	 * is not branch global.
	 * 
	 * @see #isBranchGlobal()
	 */
	private DBAttribute branchColumn;

	/** @see #isBranchGlobal() */
	private boolean branchGlobal;

	/**
	 * The column in the database responsible for the concrete type of the target object. Is
	 * <code>null</code> iff the attribute is monomorphic, as in this case the concrete type of the
	 * target is the {@link MetaObject} of this {@link MOReference}.
	 * 
	 * @see #isMonomorphic()
	 * @see #getTargetType()
	 */
	private DBAttribute typeColumn;

	/**
	 * The column in the database responsible for the history context of the target object. Is
	 * <code>null</code> iff the attribute is not historic.
	 * 
	 * @see #getHistoryType()
	 */
	private DBAttribute revColumn;

	/** @see #getHistoryType() */
	private HistoryType historyType = MOReference.DEFAULT_HISTORY_TYPE;

	/**
	 * The column in the database responsible for the id of the target object. Never
	 * <code>null</code>.
	 */
	private final DBAttribute idColumn = newDBAttribute(ReferencePart.name);

	/** @see #getDbMapping() */
	private DBAttribute[] dbMapping;

	/** @see MOReference#isContainer() */
	private boolean container;

	/** @see MOReference#getDeletionPolicy() */
	private DeletionPolicy deletionPolicy = MOReference.DEFAULT_DELETION_POLICY;

	/** Whether this {@link MOReference} shall report the default index. */
	private boolean _defaultIndex = true;

	private boolean _targetTypeHasBranchColumn = false;

	/**
	 * Creates a new {@link AbstractMOReference}.
	 * 
	 * @param name
	 *        name of the reference. (see {@link #getName()})
	 * @param targetType
	 *        The target type of this reference. Must not be <code>null</code>
	 */
	public AbstractMOReference(String name, MetaObject targetType) {
		super(name, targetType);
		if (targetType == null) {
			throw new IllegalArgumentException("targetType must not be null.");
		}
		handleDBMappingChanged();
	}

	/**
	 * Creates a new {@link AbstractMOReference} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link AbstractMOReference}.
	 */
	public AbstractMOReference(InstantiationContext context, ReferenceAttributeConfig config) {
		super(context, config);
		String targetType = config.getValueType();
		if (StringServices.isEmpty(targetType)) {
			throw new IllegalArgumentException(
				"The target type of reference '" + config.getAttributeName() + "' must be given.");
		}
		setMetaObject(new DeferredMetaObject(targetType));

		setBranchGlobal(config.isBranchGlobal());
		setHistoryType(config.getHistoryType());
		setMonomorphic(config.isMonomorphic());
		setContainer(config.isContainer());
		setDeletionPolicy(config.getDeletionPolicy());
		useDefaultIndex(config.isUseDefaultIndex());

		handleDBMappingChanged();
	}

	/**
	 * The (potential) polymorphic type of the referenced object.
	 */
	public final MetaObject getTargetType() {
		return getMetaObject();
	}

	/**
	 * Creates a new {@link DBAttribute column} for the given part. Must work stateless, because it
	 * is called during initialization.
	 * 
	 * @param part
	 *        The part to create a {@link DBAttribute column} for.
	 * @return Is later returned by
	 *         {@link #getColumn(com.top_logic.dob.meta.MOReference.ReferencePart)} and part of
	 *         {@link #getDbMapping()}
	 * 
	 * @see #getDbMapping()
	 */
	protected abstract DBAttribute newDBAttribute(ReferencePart part);

	@Override
	public DBMetaObject getType(ReferencePart part) {
		switch (part) {
			case name: {
				return IdentifierTypes.REFERENCE_MO_TYPE;
			}
			case revision: {
				return MOPrimitive.LONG;
			}
			case branch: {
				return MOPrimitive.LONG;
			}
			case type: {
				return IdentifierTypes.TYPE_REFERENCE_MO_TYPE;
			}
		}
		throw ReferencePart.noSuchPart(part);
	}

	@Override
	public boolean isMonomorphic() {
		return this.typeColumn() == null;
	}

	/**
	 * Sets the value of {@link #isMonomorphic()}
	 * 
	 * @param monomorphic
	 *        the new value
	 */
	@Override
	public void setMonomorphic(boolean monomorphic) {
		checkUpdate();
		boolean wasMonomorphic = isMonomorphic();
		if (wasMonomorphic != monomorphic) {
			if (monomorphic) {
				this.typeColumn = null;
			} else {
				this.typeColumn = newDBAttribute(ReferencePart.type);
			}
			handleDBMappingChanged();
		}
	}

	@Override
	public HistoryType getHistoryType() {
		return historyType;
	}

	/**
	 * Sets the value of {@link #getHistoryType()}
	 * 
	 * @param type
	 *        the new value
	 */
	@Override
	public void setHistoryType(HistoryType type) {
		checkUpdate();
		HistoryType formerHistory = getHistoryType();
		if (formerHistory != type) {
			checkConfigurationChanged(getDeletionPolicy(), type, isContainer());
			if (type == HistoryType.HISTORIC || type == HistoryType.MIXED) {
				this.revColumn = newDBAttribute(ReferencePart.revision);
			} else {
				this.revColumn = null;
			}
			historyType = type;
			handleDBMappingChanged();
		}
	}

	@Override
	public boolean isBranchGlobal() {
		return this.branchGlobal;
	}

	/**
	 * Sets the value of {@link #isBranchGlobal()}
	 * 
	 * @param branchGlobal
	 *        the new value
	 */
	@Override
	public void setBranchGlobal(boolean branchGlobal) {
		checkUpdate();
		boolean wasBranchGlobal = isBranchGlobal();
		this.branchGlobal = branchGlobal;
		if (wasBranchGlobal != branchGlobal) {
			handleDBMappingChanged();
		}
	}

	/**
	 * Sets the {@link #getDbMapping()} of this {@link MOAttribute}.
	 * 
	 * <p>
	 * <b>ATTENTION: CHANGING THE ORDER OF COLUMNS IN THE ARRAY MAY FORCE A DATA MIGRATION AS THIS
	 * IS THE ORDER IN THE DATABASE </b>
	 * </p>
	 */
	protected void handleDBMappingChanged() {
		int numberDBColumns = 1; // id column;
		if (!isMonomorphic()) {
			numberDBColumns++;
		}
		if (hasRevisionColumn()) {
			numberDBColumns++;
		}
		if (hasBranchColumn()) {
			numberDBColumns++;
		}
		DBAttribute[] dbColumns = new DBAttribute[numberDBColumns];
		int index = 0;
		if (!isMonomorphic()) {
			dbColumns[index++] = typeColumn();
		}
		dbColumns[index++] = idColumn();
		if (hasRevisionColumn()) {
			dbColumns[index++] = revColumn();
		}
		if (hasBranchColumn()) {
			this.branchColumn = newDBAttribute(ReferencePart.branch);
			dbColumns[index++] = branchColumn();
		} else {
			this.branchColumn = null;
		}
		this.dbMapping = dbColumns;
	}

	/**
	 * Whether a revision column exists.
	 */
	public boolean hasRevisionColumn() {
		return revColumn() != null;
	}

	@Override
	public DBAttribute[] getDbMapping() {
		return dbMapping;
	}

	/**
	 * Whether there is a column in the database in which a branch value is stored.
	 * 
	 * The branch column is not just existing if the reference is branch global, but also if it is a
	 * historic reference. The reason is a technical one: If the branch of a branch local, historic
	 * reference is not stored, it is problematic to write a SQL query to identify the branch of the
	 * referenced object. To find the branch the query must consider that the branch may not be the
	 * same as the source object but it can also be a previous one (as the revision may be before
	 * the branch revision).
	 */
	public boolean hasBranchColumn() {
		return targetTypeHasBranchColumn() && (isBranchGlobal() || hasRevisionColumn());
	}

	/**
	 * Whether the target type support values on multiple branches.
	 */
	public boolean targetTypeHasBranchColumn() {
		return _targetTypeHasBranchColumn;
	}

	@Override
	public DBAttribute getColumn(ReferencePart part) {
		switch (part) {
			case name: {
				return idColumn();
			}
			case revision: {
				return revColumn();
			}
			case branch: {
				return branchColumn();
			}
			case type: {
				return typeColumn();
			}
		}
		throw ReferencePart.noSuchPart(part);
	}

	@Override
	public ReferencePart getPart(DBAttribute attribute) {
		if (attribute == null) {
			throw new NullPointerException("'attribute' must not be 'null'.");
		}
		if (attribute == idColumn()) {
			return ReferencePart.name;
		}
		if (attribute == revColumn()) {
			return ReferencePart.revision;
		}
		if (attribute == branchColumn()) {
			return ReferencePart.branch;
		}
		if (attribute == typeColumn()) {
			return ReferencePart.type;
		}
		throw new IllegalArgumentException(attribute + " does not belong to " + this);
	}

	/**
	 * Returns the type of the object this {@link MOReference} references.
	 * 
	 * @see com.top_logic.dob.attr.AbstractMOAttribute#getMetaObject()
	 * @see #getTargetType()
	 */
	@Override
	public MetaObject getMetaObject() {
		return super.getMetaObject();
	}

	@Override
	public boolean isContainer() {
		return container;
	}

	@Override
	public void setContainer(boolean isContainer) {
		checkConfigurationChanged(getDeletionPolicy(), getHistoryType(), isContainer);
		this.container = isContainer;
	}

	@Override
	public void setDeletionPolicy(DeletionPolicy policy) {
		checkConfigurationChanged(policy, getHistoryType(), isContainer());
		this.deletionPolicy = policy;
	}

	private void checkConfigurationChanged(DeletionPolicy newPolicy, HistoryType newHistoryType, boolean newContainer) {
		if (newPolicy == DeletionPolicy.STABILISE_REFERENCE && newHistoryType != HistoryType.MIXED) {
			StringBuilder error = new StringBuilder();
			error.append("Illegal combination of deletion policy (\"");
			error.append(newPolicy.getExternalName());
			error.append("\") and history type (\"");
			error.append(newHistoryType.getExternalName());
			error.append("\") in ");
			error.append(this);
			error.append(". ");
			error.append(DeletionPolicy.STABILISE_REFERENCE.getExternalName());
			error.append(" is just allowed for references of type ");
			error.append(HistoryType.MIXED.getExternalName());
			throw new ConfigurationError(error.toString());
		}
		if (newContainer && newHistoryType != HistoryType.CURRENT) {
			StringBuilder error = new StringBuilder();
			error.append("Reference '");
			error.append(this);
			error.append("' cannot be a container with history type '");
			error.append(newHistoryType.getExternalName());
			error.append("'. Only references with type '");
			error.append(HistoryType.CURRENT.getExternalName());
			error.append("' can be container");
			throw new ConfigurationError(error.toString());
		}
	}

	@Override
	public DeletionPolicy getDeletionPolicy() {
		return deletionPolicy;
	}

	/**
	 * The branch column or null if not exists.
	 * 
	 * @see #hasBranchColumn()
	 */
	public DBAttribute branchColumn() {
		return branchColumn;
	}

	/**
	 * The type column or null if not exists.
	 * 
	 * @see #isMonomorphic()
	 */
	public DBAttribute typeColumn() {
		return typeColumn;
	}

	/**
	 * The revision column or null if not exists.
	 * 
	 * @see #hasRevisionColumn()
	 */
	public DBAttribute revColumn() {
		return revColumn;
	}

	/**
	 * The identifier column.
	 */
	public DBAttribute idColumn() {
		return idColumn;
	}

	@Override
	public void resolve(TypeContext context) throws DataObjectException {
		super.resolve(context);
		boolean newTargetTypeHasBranchColumn = context.multipleBranches();
		if (_targetTypeHasBranchColumn != newTargetTypeHasBranchColumn) {
			_targetTypeHasBranchColumn = newTargetTypeHasBranchColumn;
			handleDBMappingChanged();
		}
	}

	/**
	 * Copies {@link AbstractMOReference} from the given instance.
	 * 
	 * @see #initFrom(AbstractMOAttribute)
	 */
	protected void initFrom(AbstractMOReference orig) {
		super.initFrom(orig);

		this.setBranchGlobal(orig.isBranchGlobal());
		this.setHistoryType(orig.getHistoryType());
		this.setMonomorphic(orig.isMonomorphic());
		this.setContainer(orig.isContainer());
		this.setDeletionPolicy(orig.getDeletionPolicy());
		this.useDefaultIndex(orig.useDefaultIndex());
	}

	private String getDBNamePrefix() {
		return SQLH.mangleDBName(getName());
	}

	/**
	 * Returns the potential column name of the column for the given
	 * {@link com.top_logic.dob.meta.MOReference.ReferencePart}. This also works when no column for
	 * that part exists.
	 */
	protected String getColumnName(ReferencePart part) {
		return part.getReferenceAspectColumnName(getDBNamePrefix());
	}

	@Override
	public final boolean useDefaultIndex() {
		return _defaultIndex;
	}

	@Override
	public void useDefaultIndex(boolean b) {
		_defaultIndex = b;
	}

	@Override
	public MOIndex getIndex() {
		if (!_defaultIndex) {
			return null;
		}
		if (getHistoryType() == HistoryType.HISTORIC) {
			/* No need to create reference to stable object, because a backwards navigation is quite
			 * senseless. */
			return null;
		}

		return createIndex();
	}

	/**
	 * Creates the index for a backward navigation of this reference.
	 */
	protected abstract MOIndex createIndex();

}

