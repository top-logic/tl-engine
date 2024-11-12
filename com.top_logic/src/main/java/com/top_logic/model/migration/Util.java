/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.migration;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.io.StringWriter;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Log;
import com.top_logic.basic.Logger;
import com.top_logic.basic.LongID;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.config.ConfigBuilder;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.copy.ConfigCopier;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLFactory;
import com.top_logic.basic.db.sql.SQLLiteral;
import com.top_logic.basic.db.sql.SQLOrder;
import com.top_logic.basic.db.sql.SQLParameter;
import com.top_logic.basic.db.sql.SQLQuery.Parameter;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOReference.DeletionPolicy;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.schema.config.DBColumnType;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.DestinationReference;
import com.top_logic.knowledge.service.db2.PersistentIdFactory;
import com.top_logic.knowledge.service.db2.RowLevelLockingSequenceManager;
import com.top_logic.knowledge.service.db2.SourceReference;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.knowledge.util.OrderedLinkUtil;
import com.top_logic.knowledge.util.OrderedLinkUtil.IndexRangeTooShort;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLNamed;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.TLProperty;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.annotate.TLClassifierAnnotation;
import com.top_logic.model.config.TLModuleAnnotation;
import com.top_logic.model.config.TLTypeAnnotation;
import com.top_logic.model.impl.generated.TlModelFactory;
import com.top_logic.model.impl.util.TLPrimitiveColumns;
import com.top_logic.model.impl.util.TLStructuredTypeColumns;
import com.top_logic.model.internal.PersistentModelPart;
import com.top_logic.model.internal.PersistentModelPart.AnnotationConfigs;
import com.top_logic.model.internal.PersistentType;
import com.top_logic.model.internal.PersistentTypePart;
import com.top_logic.model.migration.data.BranchIdType;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.Module;
import com.top_logic.model.migration.data.QualifiedPartName;
import com.top_logic.model.migration.data.QualifiedTypeName;
import com.top_logic.model.migration.data.Reference;
import com.top_logic.model.migration.data.ToString;
import com.top_logic.model.migration.data.Type;
import com.top_logic.model.migration.data.TypeGeneralization;
import com.top_logic.model.migration.data.TypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.TLContext;

/**
 * Utility class for migration processors updating the {@link TLModel}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class Util {

	/**
	 * The order value of an element.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@FrameworkInternal
	public interface OrderValue extends NamedConfiguration {

		/**
		 * Optional local identifier for the element.
		 */
		@Override
		String getName();

		/**
		 * The order value of the element.
		 */
		@Mandatory
		int getOrder();

		/**
		 * Setter for {@link #getOrder()}.
		 */
		void setOrder(int value);

		/**
		 * Creates a new {@link OrderValue} with the given values.
		 */
		static OrderValue create(String name, int order) {
			OrderValue result = TypedConfiguration.newConfigItem(OrderValue.class);
			result.setName(name);
			result.setOrder(order);
			return result;
		}

	}

	private long _stopId = 0;

	private long _nextId = 0;

	private int _lastInc = 1;

	private long _lastIncTime;

	private long _revCreate = -1;

	private boolean _branchSupport;

	/**
	 * Whether the table {@link ApplicationObjectUtil#META_ATTRIBUTE_OBJECT_TYPE} has a column
	 * {@link TLAssociationEnd#HISTORY_TYPE_ATTR}.
	 * 
	 * <p>
	 * The column {@link TLAssociationEnd#HISTORY_TYPE_ATTR} was introduced in TL 7.5.0 with #27215.
	 * {@link MigrationProcessor} creating {@link TLStructuredTypePart} <b>before</b> #27215 can not
	 * set an history type.
	 * </p>
	 */
	private boolean _historyColumn;

	/**
	 * Whether the table {@link ApplicationObjectUtil#META_ATTRIBUTE_OBJECT_TYPE} has a column
	 * {@link TLStructuredTypePart#ABSTRACT_ATTR}.
	 * 
	 * <p>
	 * The column {@link TLAssociationEnd#ABSTRACT_ATTR} was introduced with #27999.
	 * {@link MigrationProcessor} creating {@link TLStructuredTypePart} <b>before</b> #27999 can not
	 * set "abstract".
	 * </p>
	 */
	private boolean _abstractColumn;

	private boolean _deletionColumn;

	/**
	 * Creates a {@link Util}.
	 */
	public Util(Log log, PooledConnection connection, boolean withBranchSupport) {
		_branchSupport = withBranchSupport;
		
		String metaAttributeTable = SQLH.mangleDBName(ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE);
		try {
			DatabaseMetaData metaData = connection.getMetaData();
			String catalog = connection.getCatalog();
			String schemaPattern = connection.getSQLDialect().getCurrentSchema(connection);
			try (ResultSet columns = metaData.getColumns(catalog, schemaPattern, metaAttributeTable, "%")) {
				while (columns.next()) {
					String columnName = columns.getString("COLUMN_NAME");
					if (SQLH.mangleDBName(TLAssociationEnd.HISTORY_TYPE_ATTR).equals(columnName)) {
						setHistoryColumn(true);
					} 
					else if (SQLH.mangleDBName(TLAssociationEnd.DELETION_POLICY_ATTR).equals(columnName)) {
						setDeletionColumn(true);
					}
					else if (SQLH.mangleDBName(TLStructuredTypePart.ABSTRACT_ATTR).equals(columnName)) {
						setAbstractColumn(true);
					}
				}
			}
			if (!hasHistoryColumn()) {
				log.info("No column '" + SQLH.mangleDBName(TLAssociationEnd.HISTORY_TYPE_ATTR) + "' found in table '"
						+ metaAttributeTable
						+ "'. This is ok if database belongs to a migration before TopLogic version 7.5.0.");
			}
			if (!hasDeletionColumn()) {
				log.info("No column '" + SQLH.mangleDBName(TLAssociationEnd.DELETION_POLICY_ATTR) + "' found in table '"
					+ metaAttributeTable
					+ "'. This is ok if database belongs to a migration before TopLogic version 7.9.0.");
			}
			if (!hasAbstractColumn()) {
				log.info("No column '" + SQLH.mangleDBName(TLStructuredTypePart.ABSTRACT_ATTR) + "' found in table '"
						+ metaAttributeTable
						+ "'. This is ok if database belongs to a migration before TopLogic version 7.9.0.");
			}
		} catch (SQLException ex) {
			throw new KnowledgeBaseException("Unable to analyse table " + metaAttributeTable + ".", ex);
		}
	}

	/**
	 * Fetches a new ID for elements to create.
	 */
	public TLID newID(PooledConnection con) throws SQLException {
		if (_nextId == _stopId) {
			long now = System.currentTimeMillis();

			// The number of ID chunks consumed in the next minute.
			int chunksAllocated =
				(int) Math.min(1024, Math.max(1, (60 * 1000 * _lastInc) / Math.max(1, now - _lastIncTime)));
			if (chunksAllocated > 1) {
				// Limit increase of allocation speed.
				if (chunksAllocated > _lastInc * 2) {
					chunksAllocated = _lastInc * 2;
				}

				Logger.info("Allocating " + chunksAllocated + " identifier chunks at once due to high demand.",
					Util.class);
			}
			_lastInc = chunksAllocated;
			_lastIncTime = now;

			DBHelper sqlDialect = con.getSQLDialect();
			long nextChunk = new RowLevelLockingSequenceManager().nextSequenceNumber(sqlDialect, con,
				sqlDialect.retryCount(), DBKnowledgeBase.ID_SEQ, chunksAllocated);
			_stopId = 1 + nextChunk * PersistentIdFactory.CHUNK_SIZE;
			_nextId = _stopId - PersistentIdFactory.CHUNK_SIZE * chunksAllocated;
		}
		return LongID.valueOf(_nextId++);
	}

	/**
	 * Gets the revision in which the {@link TLModel} was created.
	 */
	public long getRevCreate(PooledConnection con) throws SQLException {
		if (_revCreate == -1) {
			CompiledStatement select = query(
			select(
				columns(
					columnDef(min(column(BasicTypes.REV_CREATE_DB_NAME)))),
				table(SQLH.mangleDBName(TlModelFactory.KO_NAME_TL_MODULE)))).toSql(con.getSQLDialect());
			try (ResultSet dbResult = select.executeQuery(con)) {
				boolean next = dbResult.next();
				if (!next) {
					throw new IllegalStateException(
						"No entries in table " + SQLH.mangleDBName(TlModelFactory.KO_NAME_TL_MODULE) + ".");
				}
				_revCreate = dbResult.getLong(1);
			}
		}
		return _revCreate;
	}

	/**
	 * Resets the cache value returned by {@link #getRevCreate(PooledConnection)}.
	 */
	public void resetCachedRevCreate() {
		_revCreate = -1;
	}

	/**
	 * Serializes the given {@link AnnotatedConfig annotation} as child of an
	 * {@link AnnotationConfigs}.
	 * 
	 * @return A serialized {@link AnnotationConfigs} or <code>null</code> if there are actually no
	 *         annotations.
	 */
	@FrameworkInternal
	public static String toString(AnnotatedConfig<? extends TLAnnotation> annotations) {
		String annotationsAsStrings;
		if (annotations == null || annotations.getAnnotations().isEmpty()) {
			annotationsAsStrings = null;
		} else {
			ConfigBuilder sink = TypedConfiguration.createConfigBuilder(AnnotationConfigs.class);
			ConfigCopier.fillDeepCopy(annotations, sink, SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY);
			AnnotationConfigs annotationsCopy =
				(AnnotationConfigs) sink.createConfig(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY);
			annotationsAsStrings = writeToString("config", ConfigurationItem.class, annotationsCopy);
		}
		return annotationsAsStrings;
	}

	private static String writeToString(String rootTag, Class<? extends ConfigurationItem> staticType,
			ConfigurationItem config) {
		StringWriter storageMappingBuffer = new StringWriter();
		try {
			try (ConfigurationWriter w = new ConfigurationWriter(storageMappingBuffer)) {
				w.write(rootTag, staticType, config);
			}
		} catch (XMLStreamException ex) {
			throw new RuntimeException(ex);
		}
		return storageMappingBuffer.toString();
	}

	/**
	 * Creates a new {@link TLClass}.
	 * 
	 * @param className
	 *        Name of the {@link TLClass} to create.
	 */
	public Type createTLClass(PooledConnection con, QualifiedTypeName className,
			boolean isAbstract, boolean isFinal,
			AnnotatedConfig<TLTypeAnnotation> annotations)
			throws SQLException, MigrationException {
		return createTLStructuredType(con, TLContext.TRUNK_ID,
			className.getModuleName(), className.getTypeName(),
			isAbstract, isFinal,
			toString(annotations), false);
	}

	/**
	 * Creates a new {@link TLStructuredType}.
	 * 
	 * @param moduleName
	 *        Name of the module of the new class to create.
	 * @param className
	 *        Name of the new class to create.
	 * @param associationType
	 *        Whether the new {@link TLStructuredType} must be a {@link TLAssociation} or a
	 *        {@link TLClass}.
	 */
	public Type createTLStructuredType(PooledConnection con, long branch,
			String moduleName, String className,
			Boolean isAbstract, Boolean isFinal,
			String annotations, boolean associationType)
			throws SQLException, MigrationException {
		DBHelper sqlDialect = con.getSQLDialect();

		Module module = getTLModuleOrFail(con, branch, moduleName);
		TLID newIdentifier = newID(con);
		Long revCreate = getRevCreate(con);

		CompiledStatement createType = query(
		parameters(
			branchParamDef(),
			parameterDef(DBType.ID, "identifier"),
			parameterDef(DBType.LONG, "revCreate"),
			parameterDef(DBType.STRING, "annotations"),
			parameterDef(DBType.STRING, "name"),
			parameterDef(DBType.ID, "moduleID"),
			parameterDef(DBType.STRING, "impl"),
			parameterDef(DBType.BOOLEAN, "abstract"),
			parameterDef(DBType.BOOLEAN, "final")),
		insert(
			table(SQLH.mangleDBName(TlModelFactory.KO_NAME_TL_CLASS)),
				listWithoutNull(
					branchColumnOrNull(),
				BasicTypes.IDENTIFIER_DB_NAME,
				BasicTypes.REV_MAX_DB_NAME,
				BasicTypes.REV_MIN_DB_NAME,
				BasicTypes.REV_CREATE_DB_NAME,
				SQLH.mangleDBName(PersistentModelPart.ANNOTATIONS_MO_ATTRIBUTE),
				SQLH.mangleDBName(TLClass.NAME_ATTR),
				refID(PersistentType.MODULE_REF),
				refType(PersistentType.SCOPE_REF),
				refID(PersistentType.SCOPE_REF),
				SQLH.mangleDBName(TLStructuredTypeColumns.META_ELEMENT_IMPL),
					SQLH.mangleDBName(TLClass.ABSTRACT_ATTR),
					SQLH.mangleDBName(TLClass.FINAL_ATTR)),
				listWithoutNull(
					branchParamOrNull(),
				parameter(DBType.ID, "identifier"),
				literalLong(Revision.CURRENT_REV),
				parameter(DBType.LONG, "revCreate"),
				parameter(DBType.LONG, "revCreate"),
				parameter(DBType.STRING, "annotations"),
				parameter(DBType.STRING, "name"),
				parameter(DBType.ID, "moduleID"),
				literalString(TlModelFactory.KO_NAME_TL_MODULE),
				parameter(DBType.ID, "moduleID"),
				parameter(DBType.STRING, "impl"),
				parameter(DBType.BOOLEAN, "abstract"),
				parameter(DBType.BOOLEAN, "final")))).toSql(sqlDialect);

		String impl =
			associationType ? TLStructuredTypeColumns.ASSOCIATION_TYPE : TLStructuredTypeColumns.CLASS_TYPE;
		createType.executeUpdate(con, module.getBranch(), newIdentifier, revCreate, annotations, className,
			module.getID(), impl, isAbstract, isFinal);

		Type type =
			BranchIdType.newInstance(Type.class, module.getBranch(), newIdentifier, TlModelFactory.KO_NAME_TL_CLASS);
		type.setTypeName(className);
		type.setModule(module);
		type.setKind(associationType ? Type.Kind.ASSOCIATION : Type.Kind.CLASS);
		return type;
	}

	/**
	 * Creates a new {@link TLAssociation}.
	 * 
	 * @param associationName
	 *        Name of the association to create.
	 */
	public Type createTLAssociation(PooledConnection con, QualifiedTypeName associationName,
			AnnotatedConfig<TLTypeAnnotation> annotations)
			throws SQLException, MigrationException {
		return createTLStructuredType(con, TLContext.TRUNK_ID,
			associationName.getModuleName(), associationName.getTypeName(),
			null, null,
			toString(annotations), true);
	}

	/**
	 * Creates a new {@link TLProperty}.
	 * 
	 * @param name
	 *        Qualified name of the {@link TLProperty} to create.
	 * @param target
	 *        Qualified name of the type of the {@link TLProperty}.
	 */
	public TypePart createTLProperty(Log log, PooledConnection con, QualifiedPartName name,
			QualifiedTypeName target, boolean isMandatory, boolean isAbstract, boolean isMultiple, boolean bag,
			boolean ordered, AnnotatedConfig<TLAttributeAnnotation> annotations)
			throws SQLException, MigrationException {
		return createTLProperty(log, con,
			TLContext.TRUNK_ID, name.getModuleName(), name.getTypeName(),
			name.getPartName(), target.getModuleName(),
			target.getTypeName(), isMandatory, isAbstract, isMultiple, bag,
			ordered, toString(annotations));
	}

	/**
	 * Creates a new {@link TLProperty}.
	 * 
	 * @param module
	 *        Name of the module of the new {@link TLProperty} to create.
	 * @param className
	 *        Name of the owner of the new {@link TLProperty}.
	 * @param partName
	 *        Name of the new {@link TLProperty}.
	 * @param targetModule
	 *        Name of the module of the type of the {@link TLProperty} to create.
	 * @param targetType
	 *        Name of the type of the {@link TLProperty} to create.
	 */
	public TypePart createTLProperty(Log log, PooledConnection con,
			long branch, String module, String className,
			String partName, String targetModule,
			String targetType, boolean mandatory, boolean isAbstract, boolean multiple, boolean bag,
			boolean ordered, String annotations)
			throws SQLException, MigrationException {
		Boolean bagValue;
		Boolean orderedValue;
		if (multiple) {
			bagValue = Boolean.valueOf(bag);
			orderedValue = Boolean.valueOf(ordered);
		} else {
			// Only set if multiple
			bagValue = null;
			orderedValue = null;
		}
		TLID partID = newID(con);
		TLID definitionID = partID;

		TLID endID = IdentifierUtil.nullIdForMandatoryDatabaseColumns();
		Boolean composite = null;
		Boolean aggregate = null;
		Boolean navigate = null;
		HistoryType historyType = null;

		// Only defined for association ends.
		DeletionPolicy deletionPolicy = null;

		return createTLStructuredTypePart(log, con, branch, module, className, partName, partID,
			targetModule, targetType, TLStructuredTypeColumns.CLASS_PROPERTY_IMPL, endID, definitionID,
			mandatory, isAbstract, composite, aggregate, multiple, bagValue,
			orderedValue, navigate, historyType, deletionPolicy, annotations);
	}

	/**
	 * Creates a new {@link TLAssociationEnd}.
	 * @param assEnd
	 *        Name of the {@link TLAssociationEnd} to create.
	 * @param target
	 *        Name of {@link TLAssociationEnd#getType()} of the new {@link TLAssociationEnd}.
	 */
	public TypePart createTLAssociationEnd(Log log,
			PooledConnection con, QualifiedPartName assEnd,
			QualifiedTypeName target, boolean mandatory, boolean isAbstract, boolean composite, boolean aggregate,
			boolean multiple, boolean bag, boolean ordered, boolean navigate,
			HistoryType historyType, DeletionPolicy deletionPolicy, AnnotatedConfig<TLAttributeAnnotation> annotations)
			throws SQLException, MigrationException {
		return createTLAssociationEnd(log, con,
			TLContext.TRUNK_ID, assEnd.getModuleName(), assEnd.getTypeName(),
			assEnd.getPartName(), target.getModuleName(),
			target.getTypeName(), mandatory, isAbstract, composite, aggregate, multiple, bag, ordered, navigate,
			historyType, deletionPolicy, toString(annotations));
	}

	/**
	 * Creates a new {@link TLAssociationEnd}.
	 * @param moduleName
	 *        Name of the {@link TLModule} of the {@link TLAssociationEnd} to create.
	 * @param ownerName
	 *        Name of {@link TLAssociationEnd#getOwner()} of the new {@link TLAssociationEnd}.
	 * @param partName
	 *        Name of the new {@link TLAssociationEnd}.
	 */
	public TypePart createTLAssociationEnd(Log log, PooledConnection con,
			long branch, String moduleName, String ownerName,
			String partName, String targetModule,
			String targetTypeName, boolean mandatory, boolean isAbstract, boolean composite, boolean aggregate,
			boolean multiple, boolean bag, boolean ordered, boolean navigate,
			HistoryType historyType, DeletionPolicy deletionPolicy, String annotations)
			throws SQLException, MigrationException {
		Objects.requireNonNull(historyType);
		TLID partID = newID(con);
		TLID endID = IdentifierUtil.nullIdForMandatoryDatabaseColumns();
		TLID definitionID = IdentifierUtil.nullIdForMandatoryDatabaseColumns();

		return createTLStructuredTypePart(log, con, branch, moduleName, ownerName, partName, partID,
			targetModule, targetTypeName, TLStructuredTypeColumns.ASSOCIATION_END_IMPL, endID, definitionID,
			mandatory, isAbstract, composite, aggregate, multiple, bag,
			ordered, navigate, historyType, deletionPolicy, annotations);
	}

	private Reference internalCreateTLReference(Log log, PooledConnection con,
			long branch, String moduleName, String ownerName,
			String partName,
			TLID endID, String annotations)
			throws SQLException, MigrationException {
		TLID partID = newID(con);
		TLID definitionID = partID;

		String targetTable = null;
		TLID targetID = IdentifierUtil.nullIdForMandatoryDatabaseColumns();
		Boolean isMandatory = null;
		Boolean isAbstract = null;
		Boolean composite = null;
		Boolean aggregate = null;
		Boolean isMultiple = null;
		Boolean bag = null;
		Boolean ordered = null;
		Boolean navigate = null;

		// Defined only for association ends.
		HistoryType historyType = null;

		// Defined only for association ends.
		DeletionPolicy deletionPolicy = null;
		return (Reference) createTLStructuredTypePart(log, con, branch, moduleName, ownerName, partName, partID,
			targetTable, targetID, TLStructuredTypeColumns.REFERENCE_IMPL, endID, definitionID,
			isMandatory, isAbstract, composite, aggregate, isMultiple, bag,
			ordered, navigate, historyType, deletionPolicy, annotations);
	}

	private TypePart createTLStructuredTypePart(Log log, PooledConnection con,
			long branch, String moduleName, String ownerName, String partName,
			TLID partID, String targetModule,
			String targetTypeName, String impl, TLID endID,
			TLID definitionID, Boolean mandatory, Boolean isAbstract, Boolean composite, Boolean aggregate,
			Boolean multiple, Boolean bag, Boolean ordered, Boolean navigate,
			HistoryType historyType, DeletionPolicy deletionPolicy, String annotations)
			throws SQLException, MigrationException {
		Type targetType = getTLTypeOrFail(con, branch, targetModule, targetTypeName);
		if (targetType == null) {
			throw new MigrationException("No type " + TLModelUtil.qualifiedName(targetModule, targetTypeName)
				+ " in branch " + branch + " found.");
		}

		return createTLStructuredTypePart(log, con, branch, moduleName, ownerName, partName, partID,
			targetType.getTable(), targetType.getID(), impl, endID, definitionID, mandatory, isAbstract, composite,
			aggregate, multiple, bag, ordered, navigate, historyType, deletionPolicy, annotations);
	}

	private TypePart createTLStructuredTypePart(Log log, PooledConnection con,
			long branch, String moduleName, String ownerName, String partName,
			TLID partID, String targetTable,
			TLID targetID, String impl, TLID endID,
			TLID definitionID, Boolean mandatory, Boolean isAbstract, Boolean composite, Boolean aggregate,
			Boolean multiple, Boolean bag, Boolean ordered, Boolean navigate,
			HistoryType historyType, DeletionPolicy deletionPolicy, String annotations)
			throws SQLException, MigrationException {
		DBHelper sqlDialect = con.getSQLDialect();

		Type ownerClass = getTLTypeOrFail(con, branch, moduleName, ownerName);
		if (ownerClass == null) {
			throw new MigrationException("No type " + TLModelUtil.qualifiedName(moduleName, ownerName)
				+ " in branch " + branch + " found.");
		}
		TLID ownerID = ownerClass.getID();
		int ownerOrder = newAttributeOrder(con, branch, ownerID);
		Long revCreate = getRevCreate(con);

		internalCreateProperty(log, con, branch, partName, partID, targetTable, targetID, impl, endID,
			definitionID, mandatory, isAbstract, composite, aggregate, multiple, bag, ordered, navigate, historyType,
			deletionPolicy, annotations, sqlDialect, ownerClass, ownerOrder, revCreate);

		TypePart typePart;
		if (TLStructuredTypeColumns.REFERENCE_IMPL.equals(impl)) {
			typePart = TypePart.newInstance(Reference.class,
				branch, partID, ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE,
				TypePart.Kind.REFERENCE, ownerClass, partName, definitionID, ownerOrder);
			((Reference) typePart).setEndID(endID);
		} else {
			typePart = TypePart.newInstance(
				branch, partID, ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE,
				typePartKindFromImplAttribute(impl), ownerClass, partName, definitionID, ownerOrder);
		}
		return typePart;
	}

	private static TypePart.Kind typePartKindFromImplAttribute(String impl) {
		switch (impl) {
			case TLStructuredTypeColumns.REFERENCE_IMPL:
				return TypePart.Kind.REFERENCE;
			case TLStructuredTypeColumns.ASSOCIATION_END_IMPL:
				return TypePart.Kind.ASSOCIATION_END;
			case TLStructuredTypeColumns.CLASS_PROPERTY_IMPL:
				return TypePart.Kind.CLASS_PROPERTY;
			case TLStructuredTypeColumns.ASSOCIATION_PROPERTY_IMPL:
				return TypePart.Kind.ASSOCIATION_PROPERTY;
			default:
				throw new IllegalArgumentException("Unexpected impl type '" + impl + "' for type part.");
		}

	}

	private void internalCreateProperty(Log log, PooledConnection con, long branch, String partName,
			TLID partID, String targetTable, TLID targetID, String impl, TLID endID, TLID definitionID,
			Boolean mandatory, Boolean isAbstract, Boolean composite, Boolean aggregate, Boolean multiple, Boolean bag,
			Boolean ordered, Boolean navigate, HistoryType historyType, DeletionPolicy deletionPolicy, String annotations,
			DBHelper sqlDialect, Type ownerClass, int ownerOrder, Long revCreate) throws SQLException {
		if (!hasHistoryColumn()) {
			/* There is no history column, therefore a potentially given history type can not be
			 * set. */
			if (historyType != null) {
				StringBuilder noHistoryTypeColumn = new StringBuilder();
				noHistoryTypeColumn.append("Unable to create structured type part '");
				noHistoryTypeColumn.append(partName);
				noHistoryTypeColumn.append("' in type '");
				noHistoryTypeColumn.append(toString(ownerClass));
				noHistoryTypeColumn.append("' with history type '");
				noHistoryTypeColumn.append(historyType.getExternalName());
				noHistoryTypeColumn.append(
					"' as there is no column '");
				noHistoryTypeColumn.append(SQLH.mangleDBName(TLAssociationEnd.HISTORY_TYPE_ATTR));
				noHistoryTypeColumn.append(
					"' holding the history type. If the migration is a migration before TopLogic version 7.5.0 this is correct as the column was introduced in TopLogic 7.5.0. Create part without value for the history type column. Check correct value of history column for the part after migration has been finished.");
				log.info(noHistoryTypeColumn.toString(), Protocol.WARN);

				historyType = null;
			}
		}
		if (!hasDeletionColumn() && deletionPolicy != null) {
			log.info(
				"Cannot set deletion policy of '" + toString(ownerClass) + "#" + partName + "' to '" + deletionPolicy
				+ "', since references do not yet have the corresponding property.", Protocol.WARN);
			deletionPolicy = null;
		}
		CompiledStatement createProperty = query(
			parameters(
				branchParamDef(),
				parameterDef(DBType.ID, "identifier"),
				parameterDef(DBType.LONG, "revCreate"),
				parameterDef(DBType.STRING, "annotations"),
				parameterDef(DBType.STRING, "name"),
				parameterDef(DBType.STRING, "impl"),
				parameterDef(DBType.ID, "ownerID"),
				parameterDef(DBType.INT, "ownerOrder"),
				parameterDef(DBType.STRING, "targetType"),
				parameterDef(DBType.ID, "targetID"),
				parameterDef(DBType.ID, "endID"),
				parameterDef(DBType.ID, "definitionID"),
				parameterDef(DBType.BOOLEAN, "mandatory"),
				parameterDef(DBType.BOOLEAN, "abstract"),
				parameterDef(DBType.BOOLEAN, "multiple"),
				parameterDef(DBType.BOOLEAN, "composite"),
				parameterDef(DBType.BOOLEAN, "aggregate"),
				parameterDef(DBType.BOOLEAN, "ordered"),
				parameterDef(DBType.BOOLEAN, "bag"),
				parameterDef(DBType.BOOLEAN, "navigate"),
				parameterDef(DBType.STRING, "historyType"),
				parameterDef(DBType.STRING, "deletionPolicy")),
			insert(
				table(SQLH.mangleDBName(ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE)),
				listWithoutNull(
					branchColumnOrNull(),
					BasicTypes.IDENTIFIER_DB_NAME,
					BasicTypes.REV_MAX_DB_NAME,
					BasicTypes.REV_MIN_DB_NAME,
					BasicTypes.REV_CREATE_DB_NAME,
					SQLH.mangleDBName(PersistentModelPart.ANNOTATIONS_MO_ATTRIBUTE),
					SQLH.mangleDBName(TLClass.NAME_ATTR),
					SQLH.mangleDBName(ApplicationObjectUtil.IMPLEMENTATION_NAME),
					refID(TLStructuredTypePart.OWNER_ATTR),
					SQLH.mangleDBName(ApplicationObjectUtil.OWNER_REF_ORDER_ATTR),
					refType(TLStructuredTypePart.TYPE_ATTR),
					refID(TLStructuredTypePart.TYPE_ATTR),
					refID(TLReference.END_ATTR),
					refID(TLStructuredTypePart.DEFINITION_ATTR),
					SQLH.mangleDBName(TLStructuredTypePart.MANDATORY_ATTR),
					hasAbstractColumn() ? SQLH.mangleDBName(TLStructuredTypePart.ABSTRACT_ATTR) : null,
					SQLH.mangleDBName(TLStructuredTypePart.MULTIPLE_ATTR),
					SQLH.mangleDBName(TLAssociationEnd.COMPOSITE_ATTR),
					SQLH.mangleDBName(TLAssociationEnd.AGGREGATE_ATTR),
					SQLH.mangleDBName(TLAssociationEnd.ORDERED_ATTR),
					SQLH.mangleDBName(TLAssociationEnd.BAG_ATTR),
					SQLH.mangleDBName(TLAssociationEnd.NAVIGATE_ATTR),
					historyType == null ? null : SQLH.mangleDBName(TLAssociationEnd.HISTORY_TYPE_ATTR),
					deletionPolicy == null ? null : SQLH.mangleDBName(TLAssociationEnd.DELETION_POLICY_ATTR)
				),
				listWithoutNull(
					branchParamOrNull(),
					parameter(DBType.ID, "identifier"),
					literalLong(Revision.CURRENT_REV),
					parameter(DBType.LONG, "revCreate"),
					parameter(DBType.LONG, "revCreate"),
					parameter(DBType.STRING, "annotations"),
					parameter(DBType.STRING, "name"),
					parameter(DBType.STRING, "impl"),
					parameter(DBType.ID, "ownerID"),
					parameter(DBType.INT, "ownerOrder"),
					parameter(DBType.STRING, "targetType"),
					parameter(DBType.ID, "targetID"),
					parameter(DBType.ID, "endID"),
					parameter(DBType.ID, "definitionID"),
					parameter(DBType.BOOLEAN, "mandatory"),
					hasAbstractColumn() ? parameter(DBType.BOOLEAN, "abstract") : null,
					parameter(DBType.BOOLEAN, "multiple"),
					parameter(DBType.BOOLEAN, "composite"),
					parameter(DBType.BOOLEAN, "aggregate"),
					parameter(DBType.BOOLEAN, "ordered"),
					parameter(DBType.BOOLEAN, "bag"),
					parameter(DBType.BOOLEAN, "navigate"),
					historyType == null ? null : parameter(DBType.STRING, "historyType"),
					deletionPolicy == null ? null : parameter(DBType.STRING, "deletionPolicy")
				))).toSql(sqlDialect);

		createProperty.executeUpdate(con, branch, partID, revCreate, annotations, partName, impl,
			ownerClass.getID(), ownerOrder, targetTable, targetID, endID, definitionID, mandatory,
			isAbstract, multiple, composite, aggregate, ordered, bag, navigate,
			historyType == null ? null : historyType.getExternalName(),
			deletionPolicy == null ? null : deletionPolicy.getExternalName()
		);
	}

	private List<OrderValue> getOrders(PooledConnection con, long branch, TLID ownerId,
			String orderAttribute, String nameAttribute,
			String table, String ownerRef) throws SQLException {
		SQLExpression nameValue;
		if (nameAttribute != null) {
			nameValue = column(SQLH.mangleDBName(nameAttribute));
		} else {
			nameValue = literalNull(DBType.STRING);
		}
		CompiledStatement selectMaxOrder = query(
			parameters(
				branchParamDef(),
				parameterDef(DBType.ID, "ownerID")),
			selectDistinct(
				columns(
					columnDef(nameValue),
					columnDef(column(SQLH.mangleDBName(orderAttribute)))),
				table(SQLH.mangleDBName(table)),
				and(
					eqBranch(),
					eqSQL(
						column(
							refID(ownerRef)),
						parameter(DBType.ID, "ownerID"))),
				orders(order(false, column(SQLH.mangleDBName(orderAttribute)))))).toSql(con.getSQLDialect());
		List<OrderValue> attributeOrders = new ArrayList<>();
		try (ResultSet dbResult = selectMaxOrder.executeQuery(con, branch, ownerId)) {
			while (dbResult.next()) {
				String name = dbResult.getString(1);
				int order = dbResult.getInt(2);
				attributeOrders.add(OrderValue.create(name, order));
			}
		}
		return attributeOrders;
	}

	/**
	 * Creates a new {@link TLModule}.
	 */
	public Module createTLModule(PooledConnection con, String moduleName,
			AnnotatedConfig<TLModuleAnnotation> annotations)
			throws SQLException, MigrationException {
		return createTLModule(con, TLContext.TRUNK_ID, moduleName, toString(annotations));
	}

	/**
	 * Creates a new {@link TLModule}.
	 */
	public Module createTLModule(PooledConnection con, long branch, String moduleName, String annotations)
			throws SQLException, MigrationException {
		DBHelper sqlDialect = con.getSQLDialect();

		TLID modelId = getTLModuleOrFail(con, branch, TlModelFactory.TL_MODEL_STRUCTURE).getModel();
		TLID newIdentifier = newID(con);
		Long revCreate = getRevCreate(con);

		CompiledStatement createModule = query(
		parameters(
			branchParamDef(),
			parameterDef(DBType.ID, "identifier"),
			parameterDef(DBType.LONG, "revCreate"),
			parameterDef(DBType.STRING, "annotations"),
			parameterDef(DBType.STRING, "name")),
		insert(
			table(SQLH.mangleDBName(TlModelFactory.KO_NAME_TL_MODULE)),
				listWithoutNull(
					branchColumnOrNull(),
				BasicTypes.IDENTIFIER_DB_NAME,
				BasicTypes.REV_MAX_DB_NAME,
				BasicTypes.REV_MIN_DB_NAME,
				BasicTypes.REV_CREATE_DB_NAME,
				SQLH.mangleDBName(TLModelPart.ANNOTATIONS_ATTR),
				SQLH.mangleDBName(TLModule.NAME_ATTR),
				refID(TLModule.MODEL_ATTR)),
				listWithoutNull(
					branchParamOrNull(),
				parameter(DBType.ID, "identifier"),
				literalLong(Revision.CURRENT_REV),
				parameter(DBType.LONG, "revCreate"),
				parameter(DBType.LONG, "revCreate"),
				parameter(DBType.STRING, "annotations"),
				parameter(DBType.STRING, "name"),
				literalID(modelId)))).toSql(sqlDialect);

		createModule.executeUpdate(con, branch, newIdentifier, revCreate, annotations, moduleName);

		Module module = BranchIdType.newInstance(Module.class, branch,
			newIdentifier, TlModelFactory.KO_NAME_TL_MODULE);
		module.setModuleName(moduleName);
		module.setModel(modelId);
		return module;
	}

	/**
	 * Fetches an existing {@link TLModule} from the database.
	 * 
	 * @throws MigrationException
	 *         When no such module exists.
	 */
	public Module getTLModuleOrFail(PooledConnection con, String moduleName)
			throws SQLException, MigrationException {
		return getTLModuleOrFail(con, TLContext.TRUNK_ID, moduleName);
	}

	/**
	 * Fetches an existing {@link TLModule} from the database.
	 * 
	 * @throws MigrationException
	 *         When no such module exists.
	 */
	public Module getTLModuleOrFail(PooledConnection con, long branch, String moduleName)
			throws SQLException, MigrationException {
		Module module = getTLModule(con, branch, moduleName);
		if (module == null) {
			throw new MigrationException("No module with name " + moduleName + " found on branch " + branch + ".");
		}
		return module;
	}

	/**
	 * Fetches an existing {@link TLModule} from the database.
	 */
	public Module getTLModule(PooledConnection connection, long branch, String moduleName)
			throws SQLException, MigrationException {
		DBHelper sqlDialect = connection.getSQLDialect();

		String identifierAlias = "id";
		String modelAlias = "model";
		CompiledStatement selectModule = query(
		parameters(
			branchParamDef(),
			parameterDef(DBType.STRING, "moduleName")),
		selectDistinct(
			columns(
				columnDef(BasicTypes.IDENTIFIER_DB_NAME, NO_TABLE_ALIAS, identifierAlias),
				columnDef(
					refID(TLModule.MODEL_ATTR),
					NO_TABLE_ALIAS, modelAlias)),
			table(SQLH.mangleDBName(TlModelFactory.KO_NAME_TL_MODULE)),
			and(
				eqBranch(),
				eqSQL(
					column(SQLH.mangleDBName(ApplicationObjectUtil.MODULE_NAME_ATTR)),
					parameter(DBType.STRING, "moduleName"))))).toSql(sqlDialect);

		try (ResultSet dbResult = selectModule.executeQuery(connection, branch, moduleName)) {
			if (dbResult.next()) {
				Module module = BranchIdType.newInstance(Module.class, branch, tlId(dbResult, identifierAlias),
					TlModelFactory.KO_NAME_TL_MODULE);
				module.setModuleName(moduleName);
				module.setModel(tlId(dbResult, modelAlias));
				if (dbResult.next()) {
					throw new MigrationException(
						"Multiple modules with name " + moduleName + " found on branch " + branch + ".");
				}
				return module;
			} else {
				return null;
			}
		}
	}

	/**
	 * Fetches an existing {@link TLType} from the database.
	 * 
	 * @throws MigrationException
	 *         When no such type exists.
	 */
	public Type getTLTypeOrFail(PooledConnection con, QualifiedTypeName typeName)
			throws SQLException, MigrationException {
		return getTLTypeOrFail(con, TLContext.TRUNK_ID, typeName.getModuleName(), typeName.getTypeName());
	}

	/**
	 * Fetches an existing {@link TLType} from the database.
	 */
	public Type getTLTypeOrNull(PooledConnection con, QualifiedTypeName typeName)
			throws SQLException, MigrationException {
		return getTLTypeOrNull(con, TLContext.TRUNK_ID, typeName.getModuleName(), typeName.getTypeName());
	}

	/**
	 * Fetches an existing {@link TLType} from the database.
	 * 
	 * @throws MigrationException
	 *         When no such type exists.
	 */
	public Type getTLTypeOrFail(PooledConnection connection, long branch, String moduleName,
			String typeName)
			throws SQLException, MigrationException {
		Module module = getTLModuleOrFail(connection, branch, moduleName);
		return getTLTypeOrFail(connection, module, typeName);
	}

	/**
	 * Fetches an existing {@link TLType} from the database.
	 */
	public Type getTLTypeOrNull(PooledConnection connection, long branch, String moduleName,
			String typeName) throws SQLException, MigrationException {
		Module module = getTLModule(connection, branch, moduleName);
		if (module == null) {
			return null;
		}
		return getTLTypeOrNull(connection, module, typeName);
	}

	/**
	 * Fetches an existing {@link TLType} from the database.
	 * 
	 * @throws MigrationException
	 *         When no such type exists.
	 */
	public Type getTLTypeOrFail(PooledConnection connection, Module module, String typeName)
			throws SQLException, MigrationException {
		return notNull(getTLTypeOrNull(connection, module, typeName), module, typeName);
	}

	private Type notNull(Type result, Module module, String typeName) throws MigrationException {
		if (result == null) {
			throw new MigrationException(
				"No such type: " + TLModelUtil.qualifiedName(module.getModuleName(), typeName));
		}
		return result;
	}

	/**
	 * Fetches an existing {@link TLType} from the database.
	 */
	public Type getTLTypeOrNull(PooledConnection connection, Module module, String typeName) throws SQLException {
		Type tlClass = getTLClass(connection, module, typeName);
		if (tlClass != null) {
			return tlClass;
		}
		Type tlDataType = getTLDataType(connection, module, typeName);
		if (tlDataType != null) {
			return tlDataType;
		}
		Type enumType = getTLEnumeration(connection, module, typeName);
		if (enumType != null) {
			return enumType;
		}
		return null;
	}

	private Type getTLDataType(PooledConnection connection, Module module, String dataTypeName)
			throws SQLException {
		return getTLType(connection, module, TlModelFactory.KO_NAME_TL_PRIMITIVE, Type.Kind.DATATYPE, dataTypeName);
	}

	private Type getTLEnumeration(PooledConnection connection, Module module, String enumName)
			throws SQLException {
		return getTLType(connection, module, TlModelFactory.KO_NAME_TL_ENUMERATION, Type.Kind.ENUM, enumName);
	}

	private Type getTLClass(PooledConnection connection, Module module, String typeName)
			throws SQLException {
		String tlTypeTable = ApplicationObjectUtil.META_ELEMENT_OBJECT_TYPE;
		DBHelper sqlDialect = connection.getSQLDialect();

		String identifierAlias = "id";
		String implAlias = "impl";
		CompiledStatement selectClass = query(
			parameters(
				branchParamDef(),
				parameterDef(DBType.ID, "moduleId"),
				parameterDef(DBType.STRING, "typeName")),
			selectDistinct(
				columns(
					columnDef(BasicTypes.IDENTIFIER_DB_NAME, NO_TABLE_ALIAS, identifierAlias),
					columnDef(SQLH.mangleDBName(TLStructuredTypeColumns.META_ELEMENT_IMPL), NO_TABLE_ALIAS, implAlias)),
				table(SQLH.mangleDBName(tlTypeTable)),
				and(
					eqBranch(),
					eqSQL(
						column(refID(PersistentType.MODULE_REF)),
						parameter(DBType.ID, "moduleId")),
					eqSQL(
						column(SQLH.mangleDBName(TLNamed.NAME_ATTRIBUTE)),
						parameter(DBType.STRING, "typeName"))))).toSql(sqlDialect);

		try (ResultSet dbResult = selectClass.executeQuery(connection, module.getBranch(), module.getID(), typeName)) {
			if (dbResult.next()) {
				Type type = BranchIdType.newInstance(Type.class, module.getBranch(), tlId(dbResult, identifierAlias),
					tlTypeTable);
				type.setModule(module);
				type.setTypeName(typeName);
				switch (dbResult.getString(implAlias)) {
					case TLStructuredTypeColumns.ASSOCIATION_TYPE:
						type.setKind(Type.Kind.ASSOCIATION);
						break;
					case TLStructuredTypeColumns.CLASS_TYPE:
						type.setKind(Type.Kind.CLASS);
						break;
					default:
						throw new IllegalArgumentException(
							"Unexpected impl type: " + dbResult.getString(implAlias) + " for " + type + ".");
				}
				return type;
			}
		}
		return null;
	}

	private Type getTLType(PooledConnection connection, Module module, String tlTypeTable, Type.Kind kind,
			String typeName)
			throws SQLException {
		DBHelper sqlDialect = connection.getSQLDialect();

		String identifierAlias = "id";
		CompiledStatement selectClass = query(
			parameters(
				branchParamDef(),
				parameterDef(DBType.ID, "moduleId"),
				parameterDef(DBType.STRING, "typeName")),
			selectDistinct(
				columns(
					columnDef(BasicTypes.IDENTIFIER_DB_NAME, NO_TABLE_ALIAS, identifierAlias)),
				table(SQLH.mangleDBName(tlTypeTable)),
				and(
					eqBranch(),
					eqSQL(
						column(refID(PersistentType.MODULE_REF)),
						parameter(DBType.ID, "moduleId")),
					eqSQL(
						column(SQLH.mangleDBName(TLNamed.NAME_ATTRIBUTE)),
						parameter(DBType.STRING, "typeName"))))).toSql(sqlDialect);

		try (ResultSet dbResult = selectClass.executeQuery(connection, module.getBranch(), module.getID(), typeName)) {
			if (dbResult.next()) {
				Type type = BranchIdType.newInstance(Type.class, module.getBranch(), tlId(dbResult, identifierAlias),
					tlTypeTable);
				type.setModule(module);
				type.setTypeName(typeName);
				type.setKind(kind);
				return type;
			}
		}
		return null;
	}

	private TLID tlId(ResultSet dbResult, String col) throws SQLException {
		return LongID.valueOf(dbResult.getLong(col));
	}

	/**
	 * Fetches all {@link TLType} belonging to the given {@link Module}.
	 */
	public List<Type> getTLTypeIdentifiers(PooledConnection connection, Module module)
			throws SQLException {
		List<Type> searchResult = new ArrayList<>();
		addTLStructuredTypeIdentifiers(connection, module, searchResult);
		addTLEnumerationIdentifiers(connection, module, searchResult);
		addTLDataTypeIdentifiers(connection, module, searchResult);
		return searchResult;
	}

	/**
	 * Adds all {@link TLPrimitive}s belonging to the given {@link Module}.
	 */
	public void addTLDataTypeIdentifiers(PooledConnection connection, Module module,
			List<? super Type> searchResult) throws SQLException {
		addTLTypeIdentifiers(connection, module, searchResult, TlModelFactory.KO_NAME_TL_PRIMITIVE, Type.Kind.DATATYPE);
	}

	/**
	 * Adds all {@link TLEnumeration}s belonging to the given {@link Module}.
	 */
	public void addTLEnumerationIdentifiers(PooledConnection connection, Module module,
			List<? super Type> searchResult) throws SQLException {
		addTLTypeIdentifiers(connection, module, searchResult, TlModelFactory.KO_NAME_TL_ENUMERATION, Type.Kind.ENUM);
	}

	/**
	 * Adds all {@link TLStructuredType}s belonging to the given {@link Module}.
	 */
	public void addTLStructuredTypeIdentifiers(PooledConnection connection, Module module,
			List<? super Type> searchResult) throws SQLException {
		String mo = ApplicationObjectUtil.META_ELEMENT_OBJECT_TYPE;
		DBHelper sqlDialect = connection.getSQLDialect();

		String identifierAlias = "id";
		String branchAlias = "branch";
		String nameAlias = "name";
		String implAlias = "impl";
		CompiledStatement selectTLCLass = query(
			parameters(
				branchParamDef(),
				parameterDef(DBType.ID, "module")),
			selectDistinct(
				columns(
					_branchSupport ? columnDef(branchColumnOrNull(), NO_TABLE_ALIAS, branchAlias)
						: columnDef(trunkBranch(), branchAlias),
					columnDef(BasicTypes.IDENTIFIER_DB_NAME, NO_TABLE_ALIAS, identifierAlias),
					columnDef(SQLH.mangleDBName(TLNamed.NAME), NO_TABLE_ALIAS, nameAlias),
					columnDef(SQLH.mangleDBName(TLStructuredTypeColumns.META_ELEMENT_IMPL), NO_TABLE_ALIAS, implAlias)),
				table(SQLH.mangleDBName(mo)),
				and(
					eqSQL(
						column(refID(PersistentType.MODULE_REF)),
						parameter(DBType.ID, "module")),
					eqBranch()))).toSql(sqlDialect);

		try (ResultSet dbResult =
			selectTLCLass.executeQuery(connection, module.getBranch(), module.getID())) {
			while (dbResult.next()) {
				Type type = BranchIdType.newInstance(Type.class,
					dbResult.getLong(branchAlias),
					LongID.valueOf(dbResult.getLong(identifierAlias)),
					mo);
				type.setTypeName(dbResult.getString(nameAlias));
				type.setModule(module);
				switch (dbResult.getString(implAlias)) {
					case TLStructuredTypeColumns.ASSOCIATION_TYPE:
						type.setKind(Type.Kind.ASSOCIATION);
						break;
					case TLStructuredTypeColumns.CLASS_TYPE:
						type.setKind(Type.Kind.CLASS);
						break;
					default:
						throw new IllegalArgumentException(
							"Unexpected impl type: " + dbResult.getString(implAlias) + " for " + type + ".");
				}
				searchResult.add(type);
			}
		}
	}

	/**
	 * Adds all {@link TLType}s belonging to the given {@link Module}, stored in given
	 * {@link MetaObject}.
	 */
	private void addTLTypeIdentifiers(PooledConnection connection, Module module,
			List<? super Type> searchResult, String mo, Type.Kind kind) throws SQLException {
		DBHelper sqlDialect = connection.getSQLDialect();

		String identifierAlias = "id";
		String branchAlias = "branch";
		String nameAlias = "name";
		CompiledStatement selectTLCLass = query(
		parameters(
			branchParamDef(),
			parameterDef(DBType.ID, "module")),
		selectDistinct(
			columns(
					_branchSupport ? columnDef(branchColumnOrNull(), NO_TABLE_ALIAS, branchAlias)
						: columnDef(trunkBranch(), branchAlias),
				columnDef(BasicTypes.IDENTIFIER_DB_NAME, NO_TABLE_ALIAS, identifierAlias),
				columnDef(SQLH.mangleDBName(TLNamed.NAME), NO_TABLE_ALIAS, nameAlias)),
			table(SQLH.mangleDBName(mo)),
			and(
				eqSQL(
					column(refID(PersistentType.MODULE_REF)),
					parameter(DBType.ID, "module")),
				eqBranch()))).toSql(sqlDialect);

		try (ResultSet dbResult =
			selectTLCLass.executeQuery(connection, module.getBranch(), module.getID())) {
			while (dbResult.next()) {
				Type type = BranchIdType.newInstance(Type.class,
					dbResult.getLong(branchAlias),
					LongID.valueOf(dbResult.getLong(identifierAlias)),
					mo);
				type.setTypeName(dbResult.getString(nameAlias));
				type.setModule(module);
				type.setKind(kind);
				searchResult.add(type);
			}
		}
	}

	/**
	 * Creates a new {@link TLReference}.
	 * @param reference
	 *        Qualified name of the reference to create.
	 * @param target
	 *        Qualified name of {@link TLReference#getType()}.
	 */
	public Reference createTLReference(Log log,
			PooledConnection con, QualifiedPartName reference,
			QualifiedTypeName target, boolean mandatory, boolean isAbstract, boolean composite, boolean aggregate,
			boolean multiple, boolean bag, boolean ordered, boolean navigate,
			HistoryType historyType, DeletionPolicy deletionPolicy, AnnotatedConfig<TLAttributeAnnotation> annotations)
			throws SQLException, MigrationException {
		return createTLReference(log, con,
			TLContext.TRUNK_ID, reference.getModuleName(), reference.getTypeName(),
			reference.getPartName(), target.getModuleName(),
			target.getTypeName(), mandatory, isAbstract, composite, aggregate, multiple, bag, ordered, navigate,
			historyType, deletionPolicy, toString(annotations));
	}

	/**
	 * Creates a new {@link TLReference}.
	 * @param moduleName
	 *        Name of the module of the reference to create.
	 * @param ownerName
	 *        Name of {@link TLReference#getOwner()} of the reference to create.
	 * @param partName
	 *        Name of the reference to create.
	 * @param targetModule
	 *        Name of the module of {@link TLReference#getType()}.
	 * @param targetTypeName
	 *        Name of {@link TLReference#getType()}.
	 */
	public Reference createTLReference(Log log, PooledConnection con,
			long branch, String moduleName, String ownerName,
			String partName, String targetModule,
			String targetTypeName, boolean mandatory, boolean isAbstract, boolean composite, boolean aggregate,
			boolean multiple, boolean bag, boolean ordered, boolean navigate,
			HistoryType historyType, DeletionPolicy deletionPolicy, String annotations)
			throws SQLException, MigrationException {
		Objects.requireNonNull(historyType);

		Type associationType = createTLStructuredType(con, branch, moduleName,
			TLStructuredTypeColumns.syntheticAssociationName(ownerName, partName), null, null, null, true);

		createTLAssociationEnd(log, con, branch,
			associationType.getModule().getModuleName(), associationType.getTypeName(),
			TLStructuredTypeColumns.SELF_ASSOCIATION_END_NAME,
			moduleName, ownerName, false, false, false,
			false, true, false, false, false, HistoryType.CURRENT, DeletionPolicy.CLEAR_REFERENCE, null);

		TypePart targetEnd = createTLAssociationEnd(log, con, branch,
			associationType.getModule().getModuleName(), associationType.getTypeName(),
			partName,
			targetModule, targetTypeName, mandatory, isAbstract, composite,
			aggregate, multiple, bag, ordered, navigate, historyType, deletionPolicy, null);

		Reference reference =
			internalCreateTLReference(log, con, branch, moduleName, ownerName, partName, targetEnd.getID(),
				annotations);

		return reference;

	}

	/**
	 * Creates an inverse {@link TLReference}.
	 * 
	 * @param reference
	 *        Qualified name of the reference to create.
	 * @param inverseReference
	 *        Qualified name of the inverse reference.
	 */
	public Reference createInverseTLReference(Log log, PooledConnection con,
			QualifiedPartName reference,
			QualifiedPartName inverseReference, boolean mandatory, boolean composite, boolean aggregate,
			boolean multiple, boolean bag, boolean ordered,
			boolean navigate, AnnotatedConfig<TLAttributeAnnotation> annotations)
			throws SQLException, MigrationException {
		return createInverseTLReference(log, con,
			TLContext.TRUNK_ID, reference.getModuleName(), reference.getTypeName(),
			reference.getPartName(), inverseReference.getModuleName(), inverseReference.getTypeName(),
			inverseReference.getPartName(), mandatory, composite, aggregate, multiple, bag, ordered,
			navigate, toString(annotations));

	}

	/**
	 * Creates an inverse {@link TLReference}.
	 * 
	 * @param module
	 *        Name of the module of the reference to create.
	 * @param type
	 *        Name of the type of the reference to create.
	 * @param refName
	 *        Name of the reference to create.
	 * @param inverseRefModule
	 *        Name of the module of the inverse reference.
	 * @param inverseRefType
	 *        Name of the type of the inverse reference.
	 * @param inverseRefName
	 *        Name of the inverse reference.
	 */
	public Reference createInverseTLReference(Log log, PooledConnection con,
			long branch, String module, String type,
			String refName, String inverseRefModule, String inverseRefType,
			String inverseRefName, boolean mandatory, boolean composite, boolean aggregate,
			boolean multiple, boolean bag, boolean ordered,
			boolean navigate, String annotations) throws SQLException, MigrationException {

		String associationType = TLStructuredTypeColumns.syntheticAssociationName(inverseRefType, inverseRefName);
		Type tlType = getTLTypeOrFail(con, branch, inverseRefModule, associationType);
		if (tlType == null) {
			throw new MigrationException(
				"Association for inverse reference " + inverseRefName + " in "
					+ TLModelUtil.qualifiedName(inverseRefModule, inverseRefType) + " not found.");
		}
		TypePart selfEnd = getTLTypePart(con, tlType, TLStructuredTypeColumns.SELF_ASSOCIATION_END_NAME);
		Reference reference =
			internalCreateTLReference(log, con, branch, module, type, refName, selfEnd.getID(), annotations);

		CompiledStatement sql = query(
			parameters(
				branchParamDef(),
				parameterDef(DBType.ID, "endID"),
				parameterDef(DBType.BOOLEAN, "mandatory"),
				parameterDef(DBType.BOOLEAN, "composite"),
				parameterDef(DBType.BOOLEAN, "aggregate"),
				parameterDef(DBType.BOOLEAN, "multiple"),
				parameterDef(DBType.BOOLEAN, "bag"),
				parameterDef(DBType.BOOLEAN, "ordered"),
				parameterDef(DBType.BOOLEAN, "navigate")),
			update(
				table(SQLH.mangleDBName(ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE)),
				and(
					eqBranch(),
					eqSQL(
						column(BasicTypes.IDENTIFIER_DB_NAME),
						parameter(DBType.ID, "endID"))),
				listWithoutNull(
					SQLH.mangleDBName(TLAssociationEnd.MANDATORY_ATTR),
					SQLH.mangleDBName(TLAssociationEnd.COMPOSITE_ATTR),
					SQLH.mangleDBName(TLAssociationEnd.AGGREGATE_ATTR),
					SQLH.mangleDBName(TLAssociationEnd.MULTIPLE_ATTR),
					SQLH.mangleDBName(TLAssociationEnd.BAG_ATTR),
					SQLH.mangleDBName(TLAssociationEnd.ORDERED_ATTR),
					SQLH.mangleDBName(TLAssociationEnd.NAVIGATE_ATTR)),
				listWithoutNull(
					parameter(DBType.BOOLEAN, "mandatory"),
					parameter(DBType.BOOLEAN, "composite"),
					parameter(DBType.BOOLEAN, "aggregate"),
					parameter(DBType.BOOLEAN, "multiple"),
					parameter(DBType.BOOLEAN, "bag"),
					parameter(DBType.BOOLEAN, "ordered"),
					parameter(DBType.BOOLEAN, "navigate")))).toSql(con.getSQLDialect());

		sql.executeUpdate(con, branch, selfEnd.getID(), mandatory, composite, aggregate, multiple, bag, ordered,
			navigate);

		return reference;

	}

	/**
	 * Creates an {@link TLReference} for a given {@link TLAssociationEnd}.
	 * 
	 * @param reference
	 *        Name of the reference to create.
	 * @param assEnd
	 *        Name of association end.
	 */
	public Reference createTLEndReference(Log log, PooledConnection con,
			QualifiedPartName reference, QualifiedPartName assEnd, AnnotatedConfig<TLAttributeAnnotation> annotations)
			throws SQLException, MigrationException {
		return createTLEndReference(log, con,
			TLContext.TRUNK_ID, reference.getModuleName(), reference.getTypeName(),
			reference.getPartName(), assEnd.getModuleName(), assEnd.getTypeName(),
			assEnd.getPartName(), toString(annotations));
	}

	/**
	 * Creates an {@link TLReference} for a given {@link TLAssociationEnd}.
	 * 
	 * @param module
	 *        Name of the module of the reference to create.
	 * @param type
	 *        Name of the type of the reference to create.
	 * @param refName
	 *        Name of the reference to create.
	 * @param endModule
	 *        Name of the module of the association end.
	 * @param endType
	 *        Name of the type of the association end.
	 * @param endName
	 *        Name of the association end.
	 */
	public Reference createTLEndReference(Log log, PooledConnection con,
			long branch, String module, String type,
			String refName, String endModule, String endType,
			String endName, String annotations)
			throws SQLException, MigrationException {
		TypePart targetEnd = getTLTypePart(con, getTLTypeOrFail(con, branch, endModule, endType), endName);

		return internalCreateTLReference(log, con, branch, module, type, refName, targetEnd.getID(), annotations);

	}

	/**
	 * Removes a generalisation for the given specialisation.
	 */
	public void removeGeneralisation(Log log, PooledConnection con, QualifiedTypeName specialisation,
			QualifiedTypeName generalisation)
			throws SQLException, MigrationException {
		removeGeneralisation(log, con, TLContext.TRUNK_ID,
			specialisation.getModuleName(), specialisation.getTypeName(),
			generalisation.getModuleName(), generalisation.getTypeName());
	}

	/**
	 * Removes a generalisation for the given specialisation.
	 */
	public void removeGeneralisation(Log log, PooledConnection con, long branch, String specialisationModule,
			String specialisationName, String generalisationModuleName, String generalisationName)
			throws SQLException, MigrationException {
		Module generalisationModule = getTLModule(con, TLContext.TRUNK_ID, generalisationModuleName);
		if (generalisationModule == null) {
			log.info("No such generalization "
				+ TLModelUtil.qualifiedName(generalisationModuleName, generalisationName) + " in "
				+ TLModelUtil.qualifiedName(specialisationModule, specialisationName) + ", skipping removal.");
			return;
		}
		Type generalization = getTLClass(con, generalisationModule, generalisationName);
		if (generalization == null) {
			log.info("No such generalization "
				+ TLModelUtil.qualifiedName(generalisationModuleName, generalisationName) + " in "
				+ TLModelUtil.qualifiedName(specialisationModule, specialisationName) + ", skipping removal.");
			return;
		}
		Type specialization = getTLClass(con, getTLModuleOrFail(con, specialisationModule), specialisationName);
		if (specialization == null) {
			throw new MigrationException("No specialization "
				+ TLModelUtil.qualifiedName(specialisationModule, specialisationName) + " found for generalization "
				+ TLModelUtil.qualifiedName(generalisationModuleName, generalisationName) + ".");
		}

		CompiledStatement delete = query(
		parameters(
			branchParamDef(),
			parameterDef(DBType.ID, "specialization"),
			parameterDef(DBType.ID, "generalization")),
		delete(
			table(SQLH.mangleDBName(ApplicationObjectUtil.META_ELEMENT_GENERALIZATIONS)),
			and(
				eqBranch(),
				eqSQL(
					column(refID(SourceReference.REFERENCE_SOURCE_NAME)),
					parameter(DBType.ID, "specialization")),
				eqSQL(
					column(refID(DestinationReference.REFERENCE_DEST_NAME)),
					parameter(DBType.ID, "generalization"))))).toSql(con.getSQLDialect());

		delete.executeUpdate(con, branch, specialization.getID(), generalization.getID());
	}

	/**
	 * The column name of the {@link ReferencePart#name} aspect of the given reference attribute.
	 */
	public static String refID(String reference) {
		return ReferencePart.name.getReferenceAspectColumnName(SQLH.mangleDBName(reference));
	}

	/**
	 * The column name of the {@link ReferencePart#type} aspect of the given reference attribute.
	 */
	public static String refType(String reference) {
		return ReferencePart.type.getReferenceAspectColumnName(SQLH.mangleDBName(reference));
	}

	/**
	 * Adds a generalisation for the given specialisation.
	 */
	public void addGeneralisation(PooledConnection con, QualifiedTypeName specialisation,
			QualifiedTypeName generalisation)
			throws SQLException, MigrationException {
		addGeneralisation(con, TLContext.TRUNK_ID,
			specialisation.getModuleName(), specialisation.getTypeName(),
			generalisation.getModuleName(), generalisation.getTypeName());
	}

	/**
	 * Adds a generalisation for the given specialisation.
	 */
	public void addGeneralisation(PooledConnection con, long branch, String specialisationModule,
			String specialisationName, String generalisationModule, String generalisationName)
			throws SQLException, MigrationException {
		Type generalization = getTLClass(con, getTLModuleOrFail(con, generalisationModule), generalisationName);
		if (generalization == null) {
			throw new MigrationException("No generalization "
				+ TLModelUtil.qualifiedName(generalisationModule, generalisationName) + " found for specialization "
				+ TLModelUtil.qualifiedName(specialisationModule, specialisationName) + ".");
		}
		Type specialization = getTLClass(con, getTLModuleOrFail(con, specialisationModule), specialisationName);
		if (specialization == null) {
			throw new MigrationException("No specialization "
				+ TLModelUtil.qualifiedName(specialisationModule, specialisationName) + " found for generalization "
				+ TLModelUtil.qualifiedName(generalisationModule, generalisationName) + ".");
		}
		int newOrderValue = newGeneralizationOrder(con, branch, specialization.getID());
		addGeneralization(con, branch, specialization.getID(), generalization.getID(), newOrderValue);
	}

	/**
	 * Adds a new row in the table {@link ApplicationObjectUtil#META_ELEMENT_GENERALIZATIONS}.
	 * 
	 * @see #addGeneralisation(PooledConnection, QualifiedTypeName, QualifiedTypeName)
	 */
	public BranchIdType addGeneralization(PooledConnection con, long branch, TLID specializationID,
			TLID generalizationID, int newOrderValue) throws SQLException {
		TLID newIdentifier = newID(con);
		Long revCreate = getRevCreate(con);

		CompiledStatement createGeneralisation = query(
		parameters(
			branchParamDef(),
			parameterDef(DBType.ID, "identifier"),
			parameterDef(DBType.LONG, "revCreate"),
			parameterDef(DBType.ID, "specialisation"),
			parameterDef(DBType.ID, "generalisation"),
			parameterDef(DBType.INT, "order")),
		insert(
			table(SQLH.mangleDBName(ApplicationObjectUtil.META_ELEMENT_GENERALIZATIONS)),
				listWithoutNull(
					branchColumnOrNull(),
				BasicTypes.IDENTIFIER_DB_NAME,
				BasicTypes.REV_MAX_DB_NAME,
				BasicTypes.REV_MIN_DB_NAME,
				BasicTypes.REV_CREATE_DB_NAME,
				refID(SourceReference.REFERENCE_SOURCE_NAME),
				refID(DestinationReference.REFERENCE_DEST_NAME),
				SQLH.mangleDBName(TLStructuredTypeColumns.META_ELEMENT_GENERALIZATIONS__ORDER)),
				listWithoutNull(
					branchParamOrNull(),
				parameter(DBType.ID, "identifier"),
				literalLong(Revision.CURRENT_REV),
				parameter(DBType.LONG, "revCreate"),
				parameter(DBType.LONG, "revCreate"),
				parameter(DBType.ID, "specialisation"),
				parameter(DBType.ID, "generalisation"),
				parameter(DBType.INT, "order")))).toSql(con.getSQLDialect());

		createGeneralisation.executeUpdate(con, branch, newIdentifier, revCreate, specializationID,
			generalizationID, newOrderValue);

		return BranchIdType.newInstance(BranchIdType.class, branch, newIdentifier,
			ApplicationObjectUtil.META_ELEMENT_GENERALIZATIONS);
	}

	private int newAttributeOrder(PooledConnection con, long branch, TLID ownerId) throws SQLException {
		String orderAttribute = ApplicationObjectUtil.OWNER_REF_ORDER_ATTR;
		String nameAttribute = TLNamed.NAME_ATTRIBUTE;
		String table = ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE;
		String ownerRef = ApplicationObjectUtil.META_ELEMENT_ATTR;
		return newOrderValue(con, branch, ownerId, orderAttribute, nameAttribute, table, ownerRef);
	}

	private int newGeneralizationOrder(PooledConnection con, long branch, TLID ownerId) throws SQLException {
		String orderAttribute = TLStructuredTypeColumns.META_ELEMENT_GENERALIZATIONS__ORDER;
		String nameAttribute = null;
		String table = ApplicationObjectUtil.META_ELEMENT_GENERALIZATIONS;
		String ownerRef = SourceReference.REFERENCE_SOURCE_NAME;
		return newOrderValue(con, branch, ownerId, orderAttribute, nameAttribute, table, ownerRef);
	}

	private int newOrderValue(PooledConnection con, long branch, TLID ownerId, String orderAttribute,
			String nameAttribute, String table, String ownerRef) throws SQLException {
		List<OrderValue> orders = getOrders(con, branch, ownerId, orderAttribute, nameAttribute, table, ownerRef);
		for (int i = orders.size(); i >= 0; i--) {
			int insertOrder;
			try {
				insertOrder = OrderedLinkUtil.getInsertOrder(orders, i, OrderValue::getOrder);
			} catch (IndexRangeTooShort ex) {
				continue;
			}
			return insertOrder;
		}
		throw new UnreachableAssertion("At least one sort order must be available.");
	}

	/**
	 * Fetches an existing {@link TLTypePart} from the database.
	 * 
	 * @throws MigrationException
	 *         If no such part exists.
	 */
	public TypePart getTLTypePartOrFail(PooledConnection connection, QualifiedPartName part)
			throws SQLException, MigrationException {
		return getTLTypePartOrFail(connection, TLContext.TRUNK_ID,
			part.getModuleName(), part.getTypeName(), part.getPartName());
	}

	/**
	 * Fetches an existing {@link TLTypePart} from the database.
	 * 
	 * @throws MigrationException
	 *         If no such part exists.
	 */
	public TypePart getTLTypePartOrFail(PooledConnection connection, long branch, String module, String type,
			String partName) throws SQLException, MigrationException {
		Type ownerType = getTLTypeOrFail(connection, branch, module, type);
		TypePart part = getTLTypePart(connection, ownerType, partName);
		if (part == null) {
			throw new MigrationException("No part '" + partName + "' found in '" + toString(ownerType) + "'.");
		}
		return part;
	}

	/**
	 * Fetches an existing {@link TLTypePart} from the database.
	 * 
	 * @throws MigrationException
	 *         If no such part exists.
	 */
	public TypePart getTLTypePart(PooledConnection connection, Type owner, String partName)
			throws SQLException, MigrationException {
		if (owner.getTable().equals(TlModelFactory.KO_NAME_TL_ENUMERATION)) {
			return getTLClassifier(connection, owner, partName);
		}
		if (owner.getTable().equals(ApplicationObjectUtil.META_ELEMENT_OBJECT_TYPE)) {
			return getTLStructuredTypePart(connection, owner, partName);
		}
		if (owner.getTable().equals(TlModelFactory.KO_NAME_TL_PRIMITIVE)) {
			// no parts for primitives
			return null;
		}
		throw new IllegalArgumentException("No TLType: " + owner.getTable());
	}

	/**
	 * Fetches the {@link TLTypePart} for the given owner.
	 */
	public List<TypePart> getTLTypeParts(PooledConnection connection, Type owner)
			throws SQLException {
		if (owner.getTable().equals(TlModelFactory.KO_NAME_TL_ENUMERATION)) {
			return getTLClassifiers(connection, owner);
		}
		if (owner.getTable().equals(ApplicationObjectUtil.META_ELEMENT_OBJECT_TYPE)) {
			return getTLStructuredTypeParts(connection, owner);
		}
		if (owner.getTable().equals(TlModelFactory.KO_NAME_TL_PRIMITIVE)) {
			// no parts for primitives
			return Collections.emptyList();
		}
		throw new IllegalArgumentException("No TLType: " + owner.getTable());
	}

	private List<TypePart> getTLStructuredTypeParts(PooledConnection connection, Type type) throws SQLException {
		DBHelper sqlDialect = connection.getSQLDialect();

		String identifierAlias = "id";
		String nameAlias = "name";
		String implAlias = "impl";
		String endIDAlias = "endId";
		String definitionAlias = "definition";
		String orderAlias = "order";
		CompiledStatement selectParts = query(
			parameters(
				branchParamDef(),
				parameterDef(DBType.ID, "owner")),
			selectDistinct(
				columns(
					columnDef(BasicTypes.IDENTIFIER_DB_NAME, NO_TABLE_ALIAS, identifierAlias),
					columnDef(SQLH.mangleDBName(TLNamed.NAME), NO_TABLE_ALIAS, nameAlias),
					columnDef(SQLH.mangleDBName(ApplicationObjectUtil.IMPLEMENTATION_NAME), NO_TABLE_ALIAS,
						implAlias),
					columnDef(refID(TLReference.END_ATTR), NO_TABLE_ALIAS, endIDAlias),
					columnDef(refID(TLStructuredTypePart.DEFINITION_ATTR), NO_TABLE_ALIAS, definitionAlias),
					columnDef(SQLH.mangleDBName(ApplicationObjectUtil.OWNER_REF_ORDER_ATTR), NO_TABLE_ALIAS,
						orderAlias)
					),
				table(SQLH.mangleDBName(ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE)),
				and(
					eqSQL(
						column(refID(ApplicationObjectUtil.META_ELEMENT_ATTR)),
						parameter(DBType.ID, "owner")),
					eqBranch()),
				orders(
					order(false, column(SQLH.mangleDBName(ApplicationObjectUtil.OWNER_REF_ORDER_ATTR))))))
						.toSql(sqlDialect);

		List<TypePart> searchResult = new ArrayList<>();
		try (ResultSet dbResult =
			selectParts.executeQuery(connection, type.getBranch(), type.getID())) {
			while (dbResult.next()) {
				TLID id = LongID.valueOf(dbResult.getLong(identifierAlias));
				TLID definition = readTLIDColumn(dbResult, definitionAlias, id);
				String name = dbResult.getString(nameAlias);
				String impl = dbResult.getString(implAlias);
				int order = dbResult.getInt(orderAlias);
				TypePart part;
				if (TLStructuredTypeColumns.REFERENCE_IMPL.equals(impl)) {
					part = TypePart.newInstance(Reference.class,
						type.getBranch(), id, ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE,
						TypePart.Kind.REFERENCE, type, name, definition, order);
					((Reference) part).setEndID(LongID.valueOf(dbResult.getLong(endIDAlias)));
				} else {
					part = TypePart.newInstance(TypePart.class,
						type.getBranch(), id, ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE,
						typePartKindFromImplAttribute(impl), type, name, definition, order);
				}
				searchResult.add(part);
			}
		}
		return searchResult;
	}

	private List<TypePart> getTLClassifiers(PooledConnection connection, Type enumType) throws SQLException {
		DBHelper sqlDialect = connection.getSQLDialect();

		String identifierAlias = "id";
		String nameAlias = "name";
		String orderAlias = "order";
		CompiledStatement selectParts = query(
			parameters(
				branchParamDef(),
				parameterDef(DBType.ID, "owner")),
			selectDistinct(
				columns(
					columnDef(BasicTypes.IDENTIFIER_DB_NAME, NO_TABLE_ALIAS, identifierAlias),
					columnDef(SQLH.mangleDBName(TLNamed.NAME_ATTRIBUTE), NO_TABLE_ALIAS, nameAlias),
					columnDef(FastListElement.ORDER_DB_NAME, NO_TABLE_ALIAS, orderAlias)),
				table(SQLH.mangleDBName(TlModelFactory.KO_NAME_TL_CLASSIFIER)),
				and(
					eqSQL(
						column(refID(FastListElement.OWNER_ATTRIBUTE)),
						parameter(DBType.ID, "owner")),
					eqBranch()),
				orders(
					order(false, column(FastListElement.ORDER_DB_NAME))))).toSql(sqlDialect);

		List<TypePart> searchResult = new ArrayList<>();
		try (ResultSet dbResult =
			selectParts.executeQuery(connection, enumType.getBranch(), enumType.getID())) {
			while (dbResult.next()) {
				TLID id = LongID.valueOf(dbResult.getLong(identifierAlias));
				String name = dbResult.getString(nameAlias);
				int order = dbResult.getInt(orderAlias);
				TypePart part = TypePart.newInstance(TypePart.class,
					enumType.getBranch(), id, TlModelFactory.KO_NAME_TL_CLASSIFIER,
					TypePart.Kind.CLASSIFIER, enumType, name, IdentifierUtil.nullIdForMandatoryDatabaseColumns(),
					order);
				searchResult.add(part);
			}
		}
		return searchResult;
	}

	private TypePart getTLStructuredTypePart(PooledConnection connection, Type structuredType, String partName)
			throws SQLException, MigrationException {
		DBHelper sqlDialect = connection.getSQLDialect();

		String identifierAlias = "id";
		String implAlias = "impl";
		String endIDAlias = "endId";
		String definitionAlias = "definition";
		String orderAlias = "order";
		CompiledStatement selectPart = query(
		parameters(
			branchParamDef(),
			parameterDef(DBType.ID, "owner"),
			parameterDef(DBType.STRING, "part")),
		selectDistinct(
			columns(
				columnDef(BasicTypes.IDENTIFIER_DB_NAME, NO_TABLE_ALIAS, identifierAlias),
				columnDef(SQLH.mangleDBName(ApplicationObjectUtil.IMPLEMENTATION_NAME), NO_TABLE_ALIAS,
					implAlias),
				columnDef(refID(TLReference.END_ATTR), NO_TABLE_ALIAS, endIDAlias),
					columnDef(refID(TLStructuredTypePart.DEFINITION_ATTR), NO_TABLE_ALIAS, definitionAlias),
					columnDef(SQLH.mangleDBName(ApplicationObjectUtil.OWNER_REF_ORDER_ATTR), NO_TABLE_ALIAS,
						orderAlias)),
			table(SQLH.mangleDBName(ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE)),
			and(
				eqBranch(),
				eqSQL(
					column(BasicTypes.REV_MAX_DB_NAME),
					literal(DBType.LONG, Revision.CURRENT_REV)),
				eqSQL(
					column(refID(ApplicationObjectUtil.META_ELEMENT_ATTR)),
					parameter(DBType.ID, "owner")),
				eqSQL(
					column(SQLH.mangleDBName(TLNamed.NAME)),
					parameter(DBType.STRING, "part"))))).toSql(sqlDialect);

		List<TypePart> searchResult = new ArrayList<>();
		try (ResultSet dbResult =
			selectPart.executeQuery(connection, structuredType.getBranch(), structuredType.getID(), partName)) {
			while (dbResult.next()) {
				TLID id = LongID.valueOf(dbResult.getLong(identifierAlias));
				TLID definition = readTLIDColumn(dbResult, definitionAlias, id);
				String impl = dbResult.getString(implAlias);
				int order = dbResult.getInt(orderAlias);
				TypePart part;
				if (TLStructuredTypeColumns.REFERENCE_IMPL.equals(impl)) {
					part = TypePart.newInstance(Reference.class,
						structuredType.getBranch(), id, ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE,
						TypePart.Kind.REFERENCE, structuredType, partName, definition, order);
					((Reference) part).setEndID(LongID.valueOf(dbResult.getLong(endIDAlias)));
				} else {
					part = TypePart.newInstance(TypePart.class,
						structuredType.getBranch(), id, ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE,
						typePartKindFromImplAttribute(impl), structuredType, partName, definition, order);
				}
				searchResult.add(part);
			}
		}
		switch (searchResult.size()) {
			case 0:
				return null;
			case 1:
				return searchResult.get(0);
			default:
				throw new MigrationException("Multiple identifiers found for part '" + partName + "' in '"
					+ toString(structuredType) + "': " + toString(searchResult));
		}
	}

	private TLID readTLIDColumn(ResultSet dbResult, String columnAlias, TLID defaultValue) throws SQLException {
		long colValue = dbResult.getLong(columnAlias);
		if (dbResult.wasNull()) {
			return defaultValue;
		}
		TLID id;
		if (colValue == ((LongID) IdentifierUtil.nullIdForMandatoryDatabaseColumns()).longValue()) {
			id = IdentifierUtil.nullIdForMandatoryDatabaseColumns();
		} else {
			id = LongID.valueOf(colValue);
		}
		return id;
	}

	private TypePart getTLClassifier(PooledConnection connection, Type structuredType, String partName)
			throws SQLException, MigrationException {
		DBHelper sqlDialect = connection.getSQLDialect();

		String identifierAlias = "id";
		String orderAlias = "order";
		CompiledStatement selectPart = query(
		parameters(
			branchParamDef(),
			parameterDef(DBType.ID, "owner"),
			parameterDef(DBType.STRING, "part")),
		selectDistinct(
			columns(
				columnDef(BasicTypes.IDENTIFIER_DB_NAME, NO_TABLE_ALIAS, identifierAlias),
					columnDef(FastListElement.ORDER_DB_NAME, NO_TABLE_ALIAS, orderAlias)),
			table(SQLH.mangleDBName(TlModelFactory.KO_NAME_TL_CLASSIFIER)),
			and(
				eqBranch(),
				eqSQL(
					column(refID(FastListElement.OWNER_ATTRIBUTE)),
					parameter(DBType.ID, "owner")),
				eqSQL(
					column(SQLH.mangleDBName(TLNamed.NAME)),
					parameter(DBType.STRING, "part"))))).toSql(sqlDialect);

		List<TypePart> searchResult = new ArrayList<>();
		try (ResultSet dbResult =
			selectPart.executeQuery(connection, structuredType.getBranch(), structuredType.getID(), partName)) {
			while (dbResult.next()) {
				TLID id = LongID.valueOf(dbResult.getLong(identifierAlias));
				int order = dbResult.getInt(orderAlias);
				TypePart part = TypePart.newInstance(
					structuredType.getBranch(), id, TlModelFactory.KO_NAME_TL_CLASSIFIER,
					TypePart.Kind.CLASSIFIER, structuredType, partName,
					IdentifierUtil.nullIdForMandatoryDatabaseColumns(), order);
				searchResult.add(part);
			}
		}
		switch (searchResult.size()) {
			case 0:
				return null;
			case 1:
				return searchResult.get(0);
			default:
				throw new MigrationException("Multiple identifiers found for part '" + partName + "' in '"
					+ toString(structuredType) + "': " + toString(searchResult));
		}
	}

	/**
	 * Retrieves the {@link TypeGeneralization} links where the given {@link TLClass} is in the
	 * source end.
	 */
	public List<TypeGeneralization> getTLClassGeneralizationLinks(PooledConnection connection, Type specialization)
			throws SQLException {
		return getGeneralizationLinks(connection, specialization, false);
	}

	/**
	 * Retrieves the {@link TypeGeneralization} links where the given {@link TLClass} is in the
	 * destination end.
	 */
	public List<TypeGeneralization> getTLClassSpecializationLinks(PooledConnection connection, Type generalization)
			throws SQLException {
		return getGeneralizationLinks(connection, generalization, true);
	}

	private List<TypeGeneralization> getGeneralizationLinks(PooledConnection connection, Type type, boolean backwards)
			throws SQLException {
		DBHelper sqlDialect = connection.getSQLDialect();

		String givenColumn;
		String resultColumn;
		if (backwards) {
			givenColumn = refID(DestinationReference.REFERENCE_DEST_NAME);
			resultColumn = refID(SourceReference.REFERENCE_SOURCE_NAME);
		} else {
			givenColumn = refID(SourceReference.REFERENCE_SOURCE_NAME);
			resultColumn = refID(DestinationReference.REFERENCE_DEST_NAME);
		}

		String identifierAlias = "id";
		String otherAlias = "other";
		String orderAlias = "order";
		CompiledStatement selectTLStructuredTypePart = query(
			parameters(
				branchParamDef(),
				parameterDef(DBType.ID, "tlClass")),
			selectDistinct(
				columns(
					columnDef(BasicTypes.IDENTIFIER_DB_NAME, NO_TABLE_ALIAS, identifierAlias),
					columnDef(resultColumn, NO_TABLE_ALIAS, otherAlias),
					columnDef(SQLH.mangleDBName(TLStructuredTypeColumns.META_ELEMENT_GENERALIZATIONS__ORDER),
						NO_TABLE_ALIAS, orderAlias)),
				table(SQLH.mangleDBName(ApplicationObjectUtil.META_ELEMENT_GENERALIZATIONS)),
				and(
					eqSQL(
						column(givenColumn),
						parameter(DBType.ID, "tlClass")),
					eqBranch()))).toSql(sqlDialect);

		List<TypeGeneralization> searchResult = new ArrayList<>();
		try (ResultSet dbResult =
			selectTLStructuredTypePart.executeQuery(connection, type.getBranch(), type.getID())) {
			while (dbResult.next()) {
				TypeGeneralization generalization = BranchIdType.newInstance(TypeGeneralization.class,
					type.getBranch(),
					LongID.valueOf(dbResult.getLong(identifierAlias)),
					ApplicationObjectUtil.META_ELEMENT_GENERALIZATIONS);
				if (backwards) {
					generalization.setSource(LongID.valueOf(dbResult.getLong(otherAlias)));
					generalization.setDestination(type.getID());
				} else {
					generalization.setSource(type.getID());
					generalization.setDestination(LongID.valueOf(dbResult.getLong(otherAlias)));
				}
				generalization.setOrder(dbResult.getInt(orderAlias));
				searchResult.add(generalization);
			}
		}
		return searchResult;
	}

	private Set<TypeGeneralization> getAllTLClassGeneralizations(PooledConnection connection) throws SQLException {
		DBHelper sqlDialect = connection.getSQLDialect();

		String sourceColumn = refID(SourceReference.REFERENCE_SOURCE_NAME);
		String destColumn = refID(DestinationReference.REFERENCE_DEST_NAME);

		String identifierAlias = "id";
		String sourceAlias = "source";
		String destAlias = "dest";
		String orderAlias = "order";
		CompiledStatement selectTLStructuredTypePart = query(
			selectDistinct(
				columns(
					branchColumnDef(),
					columnDef(BasicTypes.IDENTIFIER_DB_NAME, NO_TABLE_ALIAS, identifierAlias),
					columnDef(sourceColumn, NO_TABLE_ALIAS, sourceAlias),
					columnDef(destColumn, NO_TABLE_ALIAS, destAlias),
					columnDef(SQLH.mangleDBName(TLStructuredTypeColumns.META_ELEMENT_GENERALIZATIONS__ORDER),
						NO_TABLE_ALIAS, orderAlias)),
				table(SQLH.mangleDBName(ApplicationObjectUtil.META_ELEMENT_GENERALIZATIONS)),
				SQLFactory.literalTrueLogical())).toSql(sqlDialect);

		Set<TypeGeneralization> searchResult = new HashSet<>();
		try (ResultSet dbResult = selectTLStructuredTypePart.executeQuery(connection)) {
			while (dbResult.next()) {
				TypeGeneralization generalization = BranchIdType.newInstance(TypeGeneralization.class,
					dbResult.getLong(BasicTypes.BRANCH_DB_NAME),
					LongID.valueOf(dbResult.getLong(identifierAlias)),
					ApplicationObjectUtil.META_ELEMENT_GENERALIZATIONS);
				generalization.setSource(LongID.valueOf(dbResult.getLong(sourceAlias)));
				generalization.setDestination(LongID.valueOf(dbResult.getLong(destAlias)));
				generalization.setOrder(dbResult.getInt(orderAlias));
				searchResult.add(generalization);
			}
		}
		return searchResult;
	}

	/**
	 * Retrieves the generalization links for the given {@link TLType}.
	 */
	public List<TypeGeneralization> getGeneralizations(PooledConnection connection, Type type) throws SQLException {
		if (type.getTable().equals(TlModelFactory.KO_NAME_TL_ENUMERATION)) {
			// no generalizations for enumerations
			return Collections.emptyList();
		}
		if (type.getTable().equals(ApplicationObjectUtil.META_ELEMENT_OBJECT_TYPE)) {
			return getTLClassGeneralizationLinks(connection, type);
		}
		if (type.getTable().equals(TlModelFactory.KO_NAME_TL_PRIMITIVE)) {
			// no generalizations for primitives
			return Collections.emptyList();
		}
		throw new IllegalArgumentException("No TLType: " + type.getTable());
	}

	/**
	 * Retrieves the {@link TLID local id} for the given {@link TLType} and all inherited
	 * specialisations.
	 */
	public Set<TLID> getTransitiveSpecializations(PooledConnection connection, Type type) throws SQLException {
		if (type.getTable().equals(TlModelFactory.KO_NAME_TL_ENUMERATION)) {
			// no specializations for enumerations
			return Collections.singleton(type.getID());
		}
		if (type.getTable().equals(ApplicationObjectUtil.META_ELEMENT_OBJECT_TYPE)) {
			Set<TypeGeneralization> allGeneralisations = getAllTLClassGeneralizations(connection);
			Set<TLID> allSpecialisations = new HashSet<>();
			allSpecialisations.add(type.getID());
			while (true) {
				boolean foundNew = false;
				for (TypeGeneralization gen : allGeneralisations) {
					if (gen.getBranch() != type.getBranch()) {
						// foreign branch.
						continue;
					}
					if (allSpecialisations.contains(gen.getDestination())) {
						boolean isNew = allSpecialisations.add(gen.getSource());
						foundNew |= isNew;
					}
				}
				if (!foundNew) {
					break;
				}
			}
			return allSpecialisations;
		}
		if (type.getTable().equals(TlModelFactory.KO_NAME_TL_PRIMITIVE)) {
			// no specializations for primitives
			return Collections.singleton(type.getID());
		}
		throw new IllegalArgumentException("No TLType: " + type.getTable());
	}

	/**
	 * Retrieves the specialization links for the given {@link TLType}.
	 */
	public List<TypeGeneralization> getSpecializations(PooledConnection connection, Type type) throws SQLException {
		if (type.getTable().equals(TlModelFactory.KO_NAME_TL_ENUMERATION)) {
			// no specializations for enumerations
			return Collections.emptyList();
		}
		if (type.getTable().equals(ApplicationObjectUtil.META_ELEMENT_OBJECT_TYPE)) {
			return getTLClassSpecializationLinks(connection, type);
		}
		if (type.getTable().equals(TlModelFactory.KO_NAME_TL_PRIMITIVE)) {
			// no specializations for primitives
			return Collections.emptyList();
		}
		throw new IllegalArgumentException("No TLType: " + type.getTable());
	}

	/**
	 * Creates a new {@link TLPrimitive}.
	 */
	public Type createTLDatatype(PooledConnection connection, QualifiedTypeName type, Kind kind,
			DBColumnType dbType, PolymorphicConfiguration<StorageMapping<?>> storageMapping,
			AnnotatedConfig<TLTypeAnnotation> annotations) throws SQLException, MigrationException {
		return createTLDatatype(connection, TLContext.TRUNK_ID, type.getModuleName(), type.getTypeName(), kind,
			dbType, storageMapping, toString(annotations));
	}

	/**
	 * Creates a new {@link TLPrimitive}.
	 */
	public Type createTLDatatype(PooledConnection con, long branch, String moduleName, String typeName,
			Kind kind, DBColumnType dbType, PolymorphicConfiguration<StorageMapping<?>> storageMapping,
			String annotations) throws SQLException, MigrationException {
		Module module = getTLModuleOrFail(con, branch, moduleName);
		TLID newIdentifier = newID(con);
		Long revCreate = getRevCreate(con);

		CompiledStatement createType = query(
		parameters(
			branchParamDef(),
			parameterDef(DBType.ID, "identifier"),
			parameterDef(DBType.LONG, "revCreate"),
			parameterDef(DBType.STRING, "annotations"),
			parameterDef(DBType.STRING, "name"),
			parameterDef(DBType.ID, "moduleID"),
			parameterDef(DBType.STRING, "kind"),
			parameterDef(DBType.STRING, "dbType"),
			parameterDef(DBType.INT, "size"),
			parameterDef(DBType.INT, "precision"),
			parameterDef(DBType.BOOLEAN, "binary"),
			parameterDef(DBType.STRING, "storage")),
		insert(
			table(SQLH.mangleDBName(TlModelFactory.KO_NAME_TL_PRIMITIVE)),
				listWithoutNull(
					branchColumnOrNull(),
				BasicTypes.IDENTIFIER_DB_NAME,
				BasicTypes.REV_MAX_DB_NAME,
				BasicTypes.REV_MIN_DB_NAME,
				BasicTypes.REV_CREATE_DB_NAME,
				SQLH.mangleDBName(PersistentModelPart.ANNOTATIONS_MO_ATTRIBUTE),
				SQLH.mangleDBName(TLClass.NAME_ATTR),
				refID(PersistentType.MODULE_REF),
				refType(PersistentType.SCOPE_REF),
				refID(PersistentType.SCOPE_REF),
					SQLH.mangleDBName(TLPrimitiveColumns.KIND_ATTR),
					SQLH.mangleDBName(TLPrimitiveColumns.DB_TYPE_ATTR),
					SQLH.mangleDBName(TLPrimitiveColumns.DB_SIZE_ATTR),
					SQLH.mangleDBName(TLPrimitiveColumns.DB_PRECISION_ATTR),
					SQLH.mangleDBName(TLPrimitiveColumns.BINARY_ATTR),
					SQLH.mangleDBName(TLPrimitiveColumns.STORAGE_MAPPING)),
				listWithoutNull(
					branchParamOrNull(),
				parameter(DBType.ID, "identifier"),
				literalLong(Revision.CURRENT_REV),
				parameter(DBType.LONG, "revCreate"),
				parameter(DBType.LONG, "revCreate"),
				parameter(DBType.STRING, "annotations"),
				parameter(DBType.STRING, "name"),
				parameter(DBType.ID, "moduleID"),
				literalString(TlModelFactory.KO_NAME_TL_MODULE),
				parameter(DBType.ID, "moduleID"),
				parameter(DBType.STRING, "kind"),
				parameter(DBType.STRING, "dbType"),
				parameter(DBType.INT, "size"),
				parameter(DBType.INT, "precision"),
				parameter(DBType.BOOLEAN, "binary"),
				parameter(DBType.STRING, "storage")))).toSql(con.getSQLDialect());

		createType.executeUpdate(con, module.getBranch(), newIdentifier, revCreate, annotations, typeName,
			module.getID(), kind.getExternalName(), dbType.getDBType().getExternalName(), dbType.getDBSize(),
			dbType.getDBPrecision(), dbType.isBinary(),
			toString(storageMapping));

		Type type = BranchIdType.newInstance(Type.class, module.getBranch(), newIdentifier,
			TlModelFactory.KO_NAME_TL_PRIMITIVE);
		type.setTypeName(typeName);
		type.setModule(module);
		type.setKind(Type.Kind.DATATYPE);
		return type;

	}

	private String toString(PolymorphicConfiguration<StorageMapping<?>> storageMapping) {
		if (storageMapping == null) {
			return null;
		}
		return writeToString("config", PolymorphicConfiguration.class, storageMapping);
	}

	/**
	 * Removes the given annotations from {@link TLModule#getAnnotations()}.
	 */
	public void removeModuleAnnotations(Log log, PooledConnection con, String moduleName,
			Collection<Class<? extends TLAnnotation>> toRemove)
			throws SQLException, MigrationException {
		String moduleAnnotations = getModuleAnnotations(log, con, TLContext.TRUNK_ID, moduleName);
		if (moduleAnnotations == null) {
			log.info("No module with name '" + moduleName + "' found.", Protocol.WARN);
			return;
		}
		AnnotatedConfig<? extends TLAnnotation> newAnnotations;
		try {
			newAnnotations = removeAnnotations(moduleAnnotations, toRemove);
		} catch (ConfigurationException ex) {
			throw new MigrationException("Unable to parse annotations for module '" + moduleName + "'.", ex);
		}
		updateModuleAnnotations(con, moduleName, newAnnotations);
	}

	/**
	 * Adds the given annotations increment to {@link TLModule#getAnnotations()}.
	 */
	public void addModuleAnnotations(Log log, PooledConnection con, String moduleName,
			AnnotatedConfig<? extends TLAnnotation> increment)
			throws SQLException, MigrationException {
		String moduleAnnotations = getModuleAnnotations(log, con, TLContext.TRUNK_ID, moduleName);
		if (moduleAnnotations == null) {
			log.info("No module with name '" + moduleName + "' found.", Protocol.WARN);
			return;
		}
		AnnotatedConfig<? extends TLAnnotation> newAnnotations;
		try {
			newAnnotations = addAnnotations(moduleAnnotations, increment);
		} catch (ConfigurationException ex) {
			throw new MigrationException("Unable to parse annotations for module '" + moduleName + "'.", ex);
		}
		updateModuleAnnotations(con, moduleName, newAnnotations);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private AnnotatedConfig<? extends TLAnnotation> removeAnnotations(String persistentAnnotations,
			Collection<Class<? extends TLAnnotation>> toRemove) throws ConfigurationException {
		AnnotatedConfig newAnnotations;
		if (persistentAnnotations.isEmpty()) {
			newAnnotations = TypedConfiguration.newConfigItem(AnnotationConfigs.class);
		} else {
			newAnnotations = (AnnotatedConfig) TypedConfiguration.fromString(persistentAnnotations);
			for (Class<? extends TLAnnotation> annotationType : toRemove) {
				TLAnnotation formerAnnotation = newAnnotations.getAnnotation(annotationType);
				if (formerAnnotation != null) {
					newAnnotations.getAnnotations().remove(formerAnnotation);
				}
			}
		}
		return newAnnotations;
	}

	/**
	 * Modifies the given {@link AnnotationConfigs} (given as serialized value with root tag
	 * "config") by adding the given increment.
	 * 
	 * @param persistentAnnotations
	 *        Serialized {@link AnnotationConfigs} or empty.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@FrameworkInternal
	public static AnnotatedConfig<? extends TLAnnotation> addAnnotations(String persistentAnnotations,
			AnnotatedConfig<? extends TLAnnotation> increment) throws ConfigurationException {
		AnnotatedConfig newAnnotations;
		if (persistentAnnotations.isEmpty()) {
			newAnnotations = increment;
		} else {
			newAnnotations = parsePersistentAnnotations(persistentAnnotations);
			for (TLAnnotation annotation : increment.getAnnotations()) {
				TLAnnotation formerAnnotation =
					newAnnotations.getAnnotation(annotation.getConfigurationInterface());
				if (formerAnnotation != null) {
					newAnnotations.getAnnotations().remove(formerAnnotation);
				}
				newAnnotations.getAnnotations().add(TypedConfiguration.copy(annotation));
			}
		}
		return newAnnotations;
	}

	/**
	 * Parses the given {@link AnnotationConfigs}, given as serialized value with root tag "config".
	 */
	public static AnnotationConfigs parsePersistentAnnotations(String persistentAnnotations)
			throws ConfigurationException {
		return TypedConfiguration.parse("config", AnnotationConfigs.class,
			CharacterContents.newContent(persistentAnnotations));
	}

	/**
	 * Sets the {@link TLModule#getAnnotations()} of a {@link TLModule}.
	 */
	public void updateModuleAnnotations(PooledConnection con, String moduleName,
			AnnotatedConfig<? extends TLAnnotation> annotations)
			throws SQLException {
		updateModuleAnnotations(con, TLContext.TRUNK_ID, moduleName, toString(annotations));
	}

	/**
	 * Sets the {@link TLModule#getAnnotations()} of a {@link TLModule}.
	 */
	public void updateModuleAnnotations(PooledConnection con, long branch, String moduleName,
			String annotations) throws SQLException {
		CompiledStatement sql = query(
			parameters(
				branchParamDef(),
				parameterDef(DBType.STRING, "name"),
				parameterDef(DBType.STRING, "annotations")),
			update(
				table(SQLH.mangleDBName(TlModelFactory.KO_NAME_TL_MODULE)),
				and(
					eqBranch(),
					eqSQL(
						column(SQLH.mangleDBName(TLModule.NAME_ATTR)),
						parameter(DBType.STRING, "name"))),
				listWithoutNull(
					SQLH.mangleDBName(PersistentModelPart.ANNOTATIONS_MO_ATTRIBUTE)),
				listWithoutNull(
					parameter(DBType.STRING, "annotations")))).toSql(con.getSQLDialect());

		sql.executeUpdate(con, branch, moduleName, annotations);
	}

	/**
	 * Reads the {@link TLModule#getAnnotations()} of a {@link TLModule}.
	 * 
	 * @return The stored {@link AnnotationConfigs} object (potentially empty) or <code>null</code>
	 *         if the module can not be found.
	 */
	public String getModuleAnnotations(Log log, PooledConnection con, long branch, String moduleName) throws SQLException {
		CompiledStatement sql = query(
			parameters(
				branchParamDef(),
				parameterDef(DBType.STRING, "name")),
			select(
				columns(
					columnDef(BasicTypes.REV_MAX_DB_NAME),
					columnDef(SQLH.mangleDBName(PersistentModelPart.ANNOTATIONS_MO_ATTRIBUTE))),
				table(SQLH.mangleDBName(TlModelFactory.KO_NAME_TL_MODULE)),
				and(
					eqBranch(),
					eqSQL(
						column(SQLH.mangleDBName(TLModule.NAME_ATTR)),
						parameter(DBType.STRING, "name"))),
				orders(order(true, column(BasicTypes.REV_MAX_DB_NAME)))))
					.toSql(con.getSQLDialect());

		try (ResultSet result = sql.executeQuery(con, branch, moduleName)) {
			if (result.next()) {
				long revMax = result.getLong(1);
				String annotations = StringServices.nonNull(result.getString(2));
				if (result.next()) {
					if (revMax == Revision.CURRENT_REV) {
						log.info(
							"Multiple entries for module '" + moduleName
									+ "' found. Use annotations of 'current' row.");
					} else {
						log.info(
							"Multiple entries for module '" + moduleName
									+ "' found. Use annotations of row with revMax '" + revMax + "'.");
					}
				}
				return annotations;
			}
		}
		return null;
	}

	/**
	 * Removes the given annotations from {@link TLType#getAnnotations()}.
	 */
	public void removeTypeAnnotations(Log log, PooledConnection con, Module module, String typeName,
			Collection<Class<? extends TLAnnotation>> toRemove)
			throws SQLException, MigrationException {
		boolean success = false;
		if (!success) {
			success = removeTypeAnnotations(log, con, module, ApplicationObjectUtil.META_ELEMENT_OBJECT_TYPE, typeName,
				toRemove);
		}
		if (!success) {
			success =
				removeTypeAnnotations(log, con, module, TlModelFactory.KO_NAME_TL_ENUMERATION, typeName, toRemove);
		}
		if (!success) {
			success =
				removeTypeAnnotations(log, con, module, TlModelFactory.KO_NAME_TL_PRIMITIVE, typeName, toRemove);
		}
		if (!success) {
			String qTypeName = TLModelUtil.qualifiedName(module.getModuleName(), typeName);
			log.info("No type '" + qTypeName + "' found.", Protocol.WARN);
		}
	}

	/**
	 * Adds the given annotations increment to {@link TLType#getAnnotations()}.
	 */
	public void addTypeAnnotations(Log log, PooledConnection con, Module module, String typeName,
			AnnotatedConfig<? extends TLAnnotation> increment)
			throws SQLException, MigrationException {
		boolean success = false;
		if (!success) {
			success = addTypeAnnotations(log, con, module, ApplicationObjectUtil.META_ELEMENT_OBJECT_TYPE, typeName,
				increment);
		}
		if (!success) {
			success =
				addTypeAnnotations(log, con, module, TlModelFactory.KO_NAME_TL_ENUMERATION, typeName, increment);
		}
		if (!success) {
			success =
				addTypeAnnotations(log, con, module, TlModelFactory.KO_NAME_TL_PRIMITIVE, typeName, increment);
		}
		if (!success) {
			String qTypeName = TLModelUtil.qualifiedName(module.getModuleName(), typeName);
			log.info("No type '" + qTypeName + "' found.", Protocol.WARN);
		}
	}

	private boolean removeTypeAnnotations(Log log, PooledConnection con, Module module, String table, String typeName,
			Collection<Class<? extends TLAnnotation>> toRemove)
			throws SQLException, MigrationException {
		String currentAnnotations = getTypeAnnotations(log, con, module, table, typeName);
		if (currentAnnotations == null) {
			return false;
		}
		AnnotatedConfig<? extends TLAnnotation> newAnnotations;
		try {
			newAnnotations = removeAnnotations(currentAnnotations, toRemove);
		} catch (ConfigurationException ex) {
			String qTypeName = TLModelUtil.qualifiedName(module.getModuleName(), typeName);
			throw new MigrationException("Unable to parse annotations for type '" + qTypeName + "'.", ex);
		}
		updateTypeAnnotations(con, module, table, typeName, toString(newAnnotations));
		return true;
	}

	private boolean addTypeAnnotations(Log log, PooledConnection con, Module module, String table, String typeName,
			AnnotatedConfig<? extends TLAnnotation> annotations)
			throws SQLException, MigrationException {
		String currentAnnotations = getTypeAnnotations(log, con, module, table, typeName);
		if (currentAnnotations == null) {
			return false;
		}
		AnnotatedConfig<? extends TLAnnotation> newAnnotations;
		try {
			newAnnotations = addAnnotations(currentAnnotations, annotations);
		} catch (ConfigurationException ex) {
			String qTypeName = TLModelUtil.qualifiedName(module.getModuleName(), typeName);
			throw new MigrationException("Unable to parse annotations for type '" + qTypeName + "'.", ex);
		}
		updateTypeAnnotations(con, module, table, typeName, toString(newAnnotations));
		return true;
	}

	/**
	 * Sets the {@link TLModule#getAnnotations()} of a {@link TLType}.
	 */
	public void updateTypeAnnotations(PooledConnection con, Module module, String typeName,
			AnnotatedConfig<? extends TLAnnotation> annotations)
			throws SQLException {
		updateTypeAnnotations(con, module, typeName, toString(annotations));
	}

	/**
	 * Sets the {@link TLModule#getAnnotations()} of a {@link TLType}.
	 */
	public void updateTypeAnnotations(PooledConnection con, Module module, String typeName,
			String annotations) throws SQLException {
		updateTypeAnnotations(con, module, ApplicationObjectUtil.META_ELEMENT_OBJECT_TYPE, typeName, annotations);
		updateTypeAnnotations(con, module, TlModelFactory.KO_NAME_TL_ENUMERATION, typeName, annotations);
		updateTypeAnnotations(con, module, TlModelFactory.KO_NAME_TL_PRIMITIVE, typeName, annotations);
	}

	private void updateTypeAnnotations(PooledConnection con, Module module, String typeTable, String typeName,
			String annotations) throws SQLException {
		CompiledStatement sql = query(
			parameters(
				branchParamDef(),
				parameterDef(DBType.ID, "module"),
				parameterDef(DBType.STRING, "name"),
				parameterDef(DBType.STRING, "annotations")),
			update(
				table(SQLH.mangleDBName(typeTable)),
				and(
					eqBranch(),
					eqSQL(
						column(refID(PersistentType.MODULE_REF)),
						parameter(DBType.ID, "module")),
					eqSQL(
						column(SQLH.mangleDBName(PersistentType.NAME_ATTR)),
						parameter(DBType.STRING, "name"))),
				listWithoutNull(
					SQLH.mangleDBName(PersistentModelPart.ANNOTATIONS_MO_ATTRIBUTE)),
				listWithoutNull(
					parameter(DBType.STRING, "annotations")))).toSql(con.getSQLDialect());

		sql.executeUpdate(con, module.getBranch(), module.getID(), typeName, annotations);
	}

	private String getTypeAnnotations(Log log, PooledConnection con, Module module, String typeTable, String typeName)
			throws SQLException {
		CompiledStatement sql = query(
			parameters(
				branchParamDef(),
				parameterDef(DBType.ID, "module"),
				parameterDef(DBType.STRING, "name")),
			select(
				columns(
					columnDef(BasicTypes.REV_MAX_DB_NAME),
					columnDef(SQLH.mangleDBName(PersistentModelPart.ANNOTATIONS_MO_ATTRIBUTE))),
				table(SQLH.mangleDBName(typeTable)),
				and(
					eqBranch(),
					eqSQL(
						column(refID(PersistentType.MODULE_REF)),
						parameter(DBType.ID, "module")),
					eqSQL(
						column(SQLH.mangleDBName(PersistentType.NAME_ATTR)),
						parameter(DBType.STRING, "name"))),
				orders(order(true, column(BasicTypes.REV_MAX_DB_NAME))))).toSql(con.getSQLDialect());

		try (ResultSet result = sql.executeQuery(con, module.getBranch(), module.getID(), typeName)) {
			if (result.next()) {
				long revMax = result.getLong(1);
				String annotations = StringServices.nonNull(result.getString(2));
				if (result.next()) {
					if (revMax == Revision.CURRENT_REV) {
						log.info(
							"Multiple entries for type '" + TLModelUtil.qualifiedName(module.getModuleName(), typeName)
									+ "' found. Use annotations of 'current' row.");
					} else {
						log.info(
							"Multiple entries for type '" + TLModelUtil.qualifiedName(module.getModuleName(), typeName)
									+ "' found. Use annotations of row with revMax '" + revMax + "'.");
					}
				}
				return annotations;
			}
		}
		return null;
	}

	/**
	 * Removes the given annotations from{@link TLTypePart#getAnnotations()}.
	 */
	public void removeTypePartAnnotations(Log log, PooledConnection con, Type owner, String partName,
			Collection<Class<? extends TLAnnotation>> toRemove)
			throws SQLException, MigrationException {
		boolean success = false;
		if (!success) {
			success = removeTypePartAnnotations(log, con, owner, ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE,
				partName, toRemove);
		}
		if (!success) {
			success =
				removeTypePartAnnotations(log, con, owner, TlModelFactory.KO_NAME_TL_CLASSIFIER, partName, toRemove);
		}
		if (!success) {
			String qPartName =
				TLModelUtil.qualifiedName(owner.getModule().getModuleName(), owner.getTypeName(), partName);
			log.info("No type part '" + qPartName + "' found.", Protocol.WARN);
		}
	}

	/**
	 * Adds the given annotations increment to {@link TLTypePart#getAnnotations()}.
	 */
	public void addTypePartAnnotations(Log log, PooledConnection con, Type owner, String partName,
			AnnotatedConfig<? extends TLAnnotation> increment)
			throws SQLException, MigrationException {
		boolean success = false;
		if (!success) {
			success = addTypePartAnnotations(log, con, owner, ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE,
				partName, increment);
		}
		if (!success) {
			success =
				addTypePartAnnotations(log, con, owner, TlModelFactory.KO_NAME_TL_CLASSIFIER, partName, increment);
		}
		if (!success) {
			String qPartName = TLModelUtil.qualifiedName(owner.getModule().getModuleName(), owner.getTypeName(), partName);
			log.info("No type part '" + qPartName + "' found.", Protocol.WARN);
		}
	}

	private boolean removeTypePartAnnotations(Log log, PooledConnection con, Type owner, String table, String partName,
			Collection<Class<? extends TLAnnotation>> toRemove)
			throws SQLException, MigrationException {
		String currentAnnotations = getTypePartAnnotations(log, con, owner, table, partName);
		if (currentAnnotations == null) {
			return false;
		}
		AnnotatedConfig<? extends TLAnnotation> newAnnotations;
		try {
			newAnnotations = removeAnnotations(currentAnnotations, toRemove);
		} catch (ConfigurationException ex) {
			String qTypePartName =
				TLModelUtil.qualifiedName(owner.getModule().getModuleName(), owner.getTypeName(), partName);
			throw new MigrationException("Unable to parse annotations for type part '" + qTypePartName + "'.", ex);
		}
		updateTypePartAnnotations(con, owner, table, partName, toString(newAnnotations));
		return true;
	}

	private boolean addTypePartAnnotations(Log log, PooledConnection con, Type owner, String table, String partName,
			AnnotatedConfig<? extends TLAnnotation> annotations)
			throws SQLException, MigrationException {
		String currentAnnotations = getTypePartAnnotations(log, con, owner, table, partName);
		if (currentAnnotations == null) {
			return false;
		}
		AnnotatedConfig<? extends TLAnnotation> newAnnotations;
		try {
			newAnnotations = addAnnotations(currentAnnotations, annotations);
		} catch (ConfigurationException ex) {
			String qTypePartName =
				TLModelUtil.qualifiedName(owner.getModule().getModuleName(), owner.getTypeName(), partName);
			throw new MigrationException("Unable to parse annotations for type part '" + qTypePartName + "'.", ex);
		}
		updateTypePartAnnotations(con, owner, table, partName, toString(newAnnotations));
		return true;
	}

	/**
	 * Sets the {@link TLTypePart#getAnnotations()} of a {@link TLTypePart}.
	 */
	public void updateTypePartAnnotations(PooledConnection con, Type owner, String partName,
			AnnotatedConfig<? extends TLAnnotation> annotations)
			throws SQLException {
		updateTypePartAnnotations(con, owner, partName, toString(annotations));
	}

	/**
	 * Sets the {@link TLTypePart#getAnnotations()} of a {@link TLTypePart}.
	 */
	public void updateTypePartAnnotations(PooledConnection con, Type owner, String partName,
			String annotations) throws SQLException {
		updateTypePartAnnotations(con, owner, ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE, partName, annotations);
		updateTypePartAnnotations(con, owner, TlModelFactory.KO_NAME_TL_CLASSIFIER, partName, annotations);
	}

	private void updateTypePartAnnotations(PooledConnection con, Type owner, String partTable, String partName,
			String annotations) throws SQLException {
		CompiledStatement sql = query(
			parameters(
				branchParamDef(),
				parameterDef(DBType.ID, "owner"),
				parameterDef(DBType.STRING, "name"),
				parameterDef(DBType.STRING, "annotations")),
			update(
				table(SQLH.mangleDBName(partTable)),
				and(
					eqBranch(),
					eqSQL(
						column(refID(PersistentTypePart.OWNER_ATTR)),
						parameter(DBType.ID, "owner")),
					eqSQL(
						column(SQLH.mangleDBName(PersistentType.NAME_ATTR)),
						parameter(DBType.STRING, "name"))),
				listWithoutNull(
					SQLH.mangleDBName(PersistentModelPart.ANNOTATIONS_MO_ATTRIBUTE)),
				listWithoutNull(
					parameter(DBType.STRING, "annotations")))).toSql(con.getSQLDialect());

		sql.executeUpdate(con, owner.getBranch(), owner.getID(), partName, annotations);
	}

	private String getTypePartAnnotations(Log log, PooledConnection con, Type owner, String partTable, String partName)
			throws SQLException {
		CompiledStatement sql = query(
			parameters(
				branchParamDef(),
				parameterDef(DBType.ID, "owner"),
				parameterDef(DBType.STRING, "name")),
			select(
				columns(
					columnDef(BasicTypes.REV_MAX_DB_NAME),
					columnDef(SQLH.mangleDBName(PersistentModelPart.ANNOTATIONS_MO_ATTRIBUTE))),
				table(SQLH.mangleDBName(partTable)),
				and(
					eqBranch(),
					eqSQL(
						column(refID(PersistentTypePart.OWNER_ATTR)),
						parameter(DBType.ID, "owner")),
					eqSQL(
						column(SQLH.mangleDBName(PersistentType.NAME_ATTR)),
						parameter(DBType.STRING, "name"))),
				orders(order(true, column(BasicTypes.REV_MAX_DB_NAME))))).toSql(con.getSQLDialect());

		try (ResultSet result = sql.executeQuery(con, owner.getBranch(), owner.getID(), partName)) {
			if (result.next()) {
				long revMax = result.getLong(1);
				String annotations = StringServices.nonNull(result.getString(2));
				if (result.next()) {
					if (revMax == Revision.CURRENT_REV) {
						log.info(
							"Multiple entries for part '"
									+ TLModelUtil.qualifiedName(owner.getModule().getModuleName(), owner.getTypeName(),
										partName)
									+ "' found. Use annotations of 'current' row.");
					} else {
						log.info(
							"Multiple entries for part '"
									+ TLModelUtil.qualifiedName(owner.getModule().getModuleName(), owner.getTypeName(),
										partName)
									+ "' found. Use annotations of row with revMax '" + revMax + "'.");
					}
				}
				return annotations;
			}
		}
		return null;

	}

	/**
	 * Creates a new {@link TLEnumeration}.
	 * 
	 * @param enumName
	 *        Name of the {@link TLClass} to create.
	 */
	public Type createTLEnumeration(PooledConnection con, QualifiedTypeName enumName,
			AnnotatedConfig<TLTypeAnnotation> annotations)
			throws SQLException, MigrationException {
		return createTLEnumeration(con, TLContext.TRUNK_ID,
			enumName.getModuleName(), enumName.getTypeName(),
			toString(annotations));
	}

	/**
	 * Creates a new {@link TLEnumeration}.
	 * 
	 * @param moduleName
	 *        Name of the module of the new enumeration to create.
	 * @param enumName
	 *        Name of the new enumeration to create.
	 */
	public Type createTLEnumeration(PooledConnection con, long branch, String moduleName, String enumName,
			String annotations) throws SQLException, MigrationException {
		DBHelper sqlDialect = con.getSQLDialect();

		Module module = getTLModuleOrFail(con, branch, moduleName);
		TLID newIdentifier = newID(con);
		Long revCreate = getRevCreate(con);

		CompiledStatement createEnum = query(
		parameters(
			branchParamDef(),
			parameterDef(DBType.ID, "identifier"),
			parameterDef(DBType.LONG, "revCreate"),
			parameterDef(DBType.STRING, "annotations"),
			parameterDef(DBType.STRING, "name"),
			parameterDef(DBType.ID, "moduleID")),
		insert(
			table(SQLH.mangleDBName(TlModelFactory.KO_NAME_TL_ENUMERATION)),
				listWithoutNull(
					branchColumnOrNull(),
				BasicTypes.IDENTIFIER_DB_NAME,
				BasicTypes.REV_MAX_DB_NAME,
				BasicTypes.REV_MIN_DB_NAME,
				BasicTypes.REV_CREATE_DB_NAME,
				SQLH.mangleDBName(PersistentModelPart.ANNOTATIONS_MO_ATTRIBUTE),
				SQLH.mangleDBName(TLEnumeration.NAME_ATTR),
				refID(PersistentType.MODULE_REF),
				refType(PersistentType.SCOPE_REF),
				refID(PersistentType.SCOPE_REF),
				refID(FastList.DEFAULT_ATTRIBUTE)),
				listWithoutNull(
					branchParamOrNull(),
				parameter(DBType.ID, "identifier"),
				literalLong(Revision.CURRENT_REV),
				parameter(DBType.LONG, "revCreate"),
				parameter(DBType.LONG, "revCreate"),
				parameter(DBType.STRING, "annotations"),
				parameter(DBType.STRING, "name"),
				parameter(DBType.ID, "moduleID"),
				literalString(TlModelFactory.KO_NAME_TL_MODULE),
				parameter(DBType.ID, "moduleID"),
				literalID(IdentifierUtil.nullIdForMandatoryDatabaseColumns())))).toSql(sqlDialect);
		createEnum.executeUpdate(con, branch, newIdentifier, revCreate, annotations, enumName, module.getID());
		Type enumType =
			BranchIdType.newInstance(Type.class, branch, newIdentifier, TlModelFactory.KO_NAME_TL_ENUMERATION);
		enumType.setTypeName(enumName);
		enumType.setModule(module);
		enumType.setKind(Type.Kind.ENUM);
		return enumType;
	}

	/**
	 * Reorders the {@link TLStructuredTypePart}s such that the part is in the order directly before
	 * the part <code>before</code>. If <code>before</code> is <code>null</code>, the part is moved
	 * to the end of the list.
	 */
	public void reorderTLStructuredTypePart(PooledConnection con, Type structuredType, String part, String before)
			throws SQLException, MigrationException {
		if (part.equals(before)) {
			throw new MigrationException("Can not move part before itself: " + part);
		}
		List<TypePart> parts = getTLStructuredTypeParts(con, structuredType);
		int partIndex = findPartIndex(parts, part, structuredType);
		int beforeIndex;
		if (before == null) {
			beforeIndex = parts.size();
		} else {
			beforeIndex = findPartIndex(parts, before, structuredType);
		}
		if (partIndex == beforeIndex - 1) {
			// Already at correct position.
			return;
		}
		TypePart movedPart = parts.remove(partIndex);
		if (partIndex < beforeIndex) {
			parts.add(beforeIndex-1, movedPart);
		} else {
			parts.add(beforeIndex, movedPart);
		}
		Set<TypePart> changed = new HashSet<>();
		Function<TypePart, Number> getter = TypePart::getOrder;
		BiConsumer<TypePart, Integer> setter = (p, o) -> {
			int newOrder = o.intValue();
			if (newOrder != p.getOrder()) {
				p.setOrder(newOrder);
				changed.add(p);
			}
		};
		OrderedLinkUtil.updateIndices(parts, getter, setter);
		for (TypePart change : changed) {
			updateTLStructuredTypePartSortOrder(con, change, change.getOrder());
		}

	}

	private int findPartIndex(List<? extends TypePart> parts, String part, Type owner) throws MigrationException {
		for (int i = 0; i < parts.size(); i++) {
			if (part.equals(parts.get(i).getPartName())) {
				return i;
			}
		}
		throw new MigrationException("No part with name '" + part + "' found in '" + toString(owner) + "'.");
	}

	/**
	 * Reorders the generalization links of a {@link Type} such that the generalization type is in
	 * the order directly before the type <code>before</code>. If <code>before</code> is
	 * <code>null</code>, the generalization is moved to the end of the list.
	 */
	public void reorderTLTypeGeneralization(PooledConnection con, Type specialization, Type generalization,
			Type before) throws SQLException, MigrationException {
		if (generalization.equals(before)) {
			throw new MigrationException("Can not move generalization before itself: " + generalization);
		}
		List<TypeGeneralization> generalizations = getGeneralizations(con, specialization);
		int generalizationIdx = findGeneralization(specialization, generalizations, generalization);
		int beforeIndex;
		if (before == null) {
			beforeIndex = generalizations.size();
		} else {
			beforeIndex = findGeneralization(specialization, generalizations, before);
		}
		if (generalizationIdx == beforeIndex - 1) {
			// Already at correct position.
			return;
		}
		TypeGeneralization movedGeneralization = generalizations.remove(generalizationIdx);
		if (generalizationIdx < beforeIndex) {
			generalizations.add(beforeIndex - 1, movedGeneralization);
		} else {
			generalizations.add(beforeIndex, movedGeneralization);
		}
		Set<TypeGeneralization> changed = new HashSet<>();
		Function<TypeGeneralization, Number> getter = TypeGeneralization::getOrder;
		BiConsumer<TypeGeneralization, Integer> setter = (p, o) -> {
			int newOrder = o.intValue();
			if (newOrder != p.getOrder()) {
				p.setOrder(newOrder);
				changed.add(p);
			}
		};
		OrderedLinkUtil.updateIndices(generalizations, getter, setter);
		for (TypeGeneralization change : changed) {
			updateTypeGeneralizationSortOrder(con, change, change.getOrder());
		}

	}

	private int findGeneralization(Type specialization, List<? extends TypeGeneralization> generalizations,
			Type generalization) throws MigrationException {
		TLID id = generalization.getID();
		for (int i = 0; i < generalizations.size(); i++) {
			if (id.equals(generalizations.get(i).getDestination())) {
				return i;
			}
		}
		throw new MigrationException(
			"Type '" + toString(generalization) + "' is not a generalization of '" + toString(specialization) + "'.");
	}

	/**
	 * Reorders the {@link TLClassifier}s such that the classifier is in the order directly before
	 * the classifier <code>before</code>. If <code>before</code> is <code>null</code>, the
	 * classifier is moved to the end of the list.
	 */
	public void reorderTLClassifier(PooledConnection con, Type enumType, String classifier, String before)
			throws SQLException, MigrationException {
		if (classifier.equals(before)) {
			throw new MigrationException("Can not move classifier before itself: " + classifier);
		}
		List<TypePart> classifiers = getTLClassifiers(con, enumType);
		int currentIndex = findPartIndex(classifiers, classifier, enumType);
		int beforeIndex;
		if (before == null) {
			beforeIndex = classifiers.size();
		} else {
			beforeIndex = findPartIndex(classifiers, before, enumType);
		}
		if (currentIndex == beforeIndex - 1) {
			// Already at correct position.
			return;
		}
		TypePart movedClassifier = classifiers.get(currentIndex);
		// First move classifier away to avoid duplicate-key constraint.
		updateTLClassifierSortOrder(con, movedClassifier, Integer.MAX_VALUE);
		int targetOrder;
		if (currentIndex < beforeIndex) {
			targetOrder =
				beforeIndex == classifiers.size() ? beforeIndex : classifiers.get(beforeIndex).getOrder() - 1;
			for (int i = currentIndex + 1; i < beforeIndex; i++) {
				TypePart tlClassifier = classifiers.get(i);
				updateTLClassifierSortOrder(con, tlClassifier, tlClassifier.getOrder() - 1);
			}
		} else {
			assert beforeIndex < currentIndex;
			targetOrder = classifiers.get(beforeIndex).getOrder();
			for (int i = currentIndex - 1; i >= beforeIndex; i--) {
				TypePart tlClassifier = classifiers.get(i);
				updateTLClassifierSortOrder(con, tlClassifier, tlClassifier.getOrder() + 1);
			}
		}
		updateTLClassifierSortOrder(con, movedClassifier, targetOrder);
	}

	private void updateTypeGeneralizationSortOrder(PooledConnection con, TypeGeneralization part, int newSortOrder)
			throws SQLException {
		updateSortOrder(con, part, SQLH.mangleDBName(TLStructuredTypeColumns.META_ELEMENT_GENERALIZATIONS__ORDER),
			newSortOrder);
	}

	private void updateTLClassifierSortOrder(PooledConnection con, TypePart part, int newSortOrder)
			throws SQLException {
		updateSortOrder(con, part, FastListElement.ORDER_DB_NAME, newSortOrder);
	}

	private void updateTLStructuredTypePartSortOrder(PooledConnection con, BranchIdType part, int sortOrder)
			throws SQLException {
		updateSortOrder(con, part, SQLH.mangleDBName(ApplicationObjectUtil.OWNER_REF_ORDER_ATTR), sortOrder);
	}

	private void updateSortOrder(PooledConnection con, BranchIdType part, String orderColumn, int newSortOrder)
			throws SQLException {
		DBHelper sqlDialect = con.getSQLDialect();
		CompiledStatement updateStmt = query(
			parameters(
				branchParamDef(),
				parameterDef(DBType.ID, "identifier"),
				parameterDef(DBType.INT, "order")),
			update(
				table(SQLH.mangleDBName(part.getTable())),
				and(
					eqBranch(),
					eqSQL(
						column(BasicTypes.IDENTIFIER_DB_NAME),
						parameter(DBType.ID, "identifier"))),
				listWithoutNull(
					orderColumn),
				listWithoutNull(
					parameter(DBType.INT, "order")))).toSql(sqlDialect);
		updateStmt.executeUpdate(con, part.getBranch(), part.getID(), newSortOrder);
	}

	/**
	 * Creates a new {@link TLClassifier}.
	 * 
	 * @param classifierName
	 *        Name of the {@link TLClassifier} to create.
	 */
	public TypePart createTLClassifier(PooledConnection con, QualifiedPartName classifierName,
			AnnotatedConfig<TLClassifierAnnotation> annotations) throws SQLException, MigrationException {
		return createTLClassifier(con, TLContext.TRUNK_ID,
			classifierName.getModuleName(), classifierName.getTypeName(), classifierName.getPartName(),
			toString(annotations));
	}

	/**
	 * Creates a new {@link TLClassifier}.
	 * 
	 * @param moduleName
	 *        Name of the module of the new classifier to create.
	 * @param enumName
	 *        Name of the enumeration of the new classifier.
	 * @param classifierName
	 *        Name of the new classifier.
	 */
	public TypePart createTLClassifier(PooledConnection con, long branch, String moduleName, String enumName,
			String classifierName, String annotations) throws SQLException, MigrationException {
		DBHelper sqlDialect = con.getSQLDialect();

		Module module = getTLModuleOrFail(con, branch, moduleName);
		Type enumeration = getTLEnumeration(con, module, enumName);
		if (enumeration == null) {
			throw new MigrationException(
				"No enumeration with name '" + enumName + "' found in module " + toString(module));
		}
		
		TypePart existing = getTLClassifier(con, enumeration, classifierName);
		if (existing != null) {
			throw new MigrationException(
				"Classifier '" + toString(existing) + "' already exists.");
		}
		
		List<OrderValue> orders = getOrders(con, branch, enumeration.getID(), FastListElement.ORDER_DB_NAME,
			PersistentTypePart.NAME_ATTR, TlModelFactory.KO_NAME_TL_CLASSIFIER, FastListElement.OWNER_ATTRIBUTE);
		int sortOrder;
		if (orders.isEmpty()) {
			sortOrder = 0;
		} else {
			sortOrder = orders.get(orders.size() - 1).getOrder() + 1;
		}
		TLID newIdentifier = newID(con);
		Long revCreate = getRevCreate(con);

		CompiledStatement createClassifier = query(
		parameters(
			branchParamDef(),
			parameterDef(DBType.ID, "identifier"),
			parameterDef(DBType.LONG, "revCreate"),
			parameterDef(DBType.STRING, "annotations"),
			parameterDef(DBType.ID, "enum"),
			parameterDef(DBType.STRING, "name"),
			parameterDef(DBType.INT, "sortOrder")),
		insert(
			table(SQLH.mangleDBName(TlModelFactory.KO_NAME_TL_CLASSIFIER)),
				listWithoutNull(
					branchColumnOrNull(),
				BasicTypes.IDENTIFIER_DB_NAME,
				BasicTypes.REV_MAX_DB_NAME,
				BasicTypes.REV_MIN_DB_NAME,
				BasicTypes.REV_CREATE_DB_NAME,
				SQLH.mangleDBName(PersistentModelPart.ANNOTATIONS_MO_ATTRIBUTE),
				refID(FastListElement.OWNER_ATTRIBUTE),
				SQLH.mangleDBName(TLClassifier.NAME_ATTR),
				SQLH.mangleDBName(FastListElement.ORDER_DB_NAME)),
				listWithoutNull(
					branchParamOrNull(),
				parameter(DBType.ID, "identifier"),
				literalLong(Revision.CURRENT_REV),
				parameter(DBType.LONG, "revCreate"),
				parameter(DBType.LONG, "revCreate"),
				parameter(DBType.STRING, "annotations"),
				parameter(DBType.ID, "enum"),
				parameter(DBType.STRING, "name"),
				parameter(DBType.INT, "sortOrder")))).toSql(sqlDialect);
		createClassifier.executeUpdate(con, branch, newIdentifier, revCreate, annotations, enumeration.getID(),
			classifierName, sortOrder);
		return TypePart.newInstance(
			branch, newIdentifier, TlModelFactory.KO_NAME_TL_CLASSIFIER,
			TypePart.Kind.CLASSIFIER, enumeration, classifierName, IdentifierUtil.nullIdForMandatoryDatabaseColumns(),
			sortOrder);
	}

	/**
	 * Creates a new {@link TLClassifier}.
	 * 
	 * @param enumName
	 *        Name of the {@link TLEnumeration} to adapt.
	 * @param newDefaultClassifier
	 *        Name of the {@link TLClassifier} to set as default. If <code>null</code> no classifier
	 *        will be default.
	 */
	public void setDefaultTLClassifier(PooledConnection con, QualifiedTypeName enumName,
			String newDefaultClassifier)
			throws SQLException, MigrationException {
		setDefaultTLClassifier(con, TLContext.TRUNK_ID, enumName.getModuleName(), enumName.getTypeName(),
			newDefaultClassifier);
	}

	/**
	 * Creates a new {@link TLEnumeration}.
	 * 
	 * @param moduleName
	 *        Name of the module of the new enumeration to adapt.
	 * @param enumName
	 *        Name of the enumeration to adapt.
	 * @param classifierName
	 *        Name of the classifier to set as default.
	 */
	public void setDefaultTLClassifier(PooledConnection con, long branch, String moduleName, String enumName,
			String classifierName) throws SQLException, MigrationException {
		DBHelper sqlDialect = con.getSQLDialect();

		Module module = getTLModuleOrFail(con, branch, moduleName);
		Type enumeration = getTLEnumeration(con, module, enumName);
		if (enumeration == null) {
			throw new MigrationException(
				"No enumeration with name '" + enumName + "' found in module " + toString(module));
		}
		TLID defaultID;
		if (classifierName == null) {
			defaultID = IdentifierUtil.nullIdForMandatoryDatabaseColumns();
		} else {
			TypePart classifier = getTLTypePart(con, enumeration, classifierName);
			if (classifier != null) {
				defaultID = classifier.getID();
			} else {
				throw new MigrationException(
					"No classifier with name '" + classifierName + "' found in enumeration " + toString(enumeration));
			}
		}

		CompiledStatement updateStmt = query(
		parameters(
			branchParamDef(),
			parameterDef(DBType.ID, "identifier"),
			parameterDef(DBType.ID, "defaultClassifier")),
		update(
			table(SQLH.mangleDBName(TlModelFactory.KO_NAME_TL_ENUMERATION)),
			and(
				eqBranch(),
				eqSQL(
					column(BasicTypes.IDENTIFIER_DB_NAME),
					parameter(DBType.ID, "identifier"))),
				listWithoutNull(
				refID(FastList.DEFAULT_ATTRIBUTE)),
				listWithoutNull(
				parameter(DBType.ID, "defaultClassifier")))).toSql(sqlDialect);
		updateStmt.executeUpdate(con, branch, enumeration.getID(), defaultID);
	}

	/**
	 * Serializes a sequence of {@link BranchIdType}.
	 */
	public StringBuilder toString(Iterable<? extends BranchIdType> elements) {
		return toString(new StringBuilder(), elements);
	}

	/**
	 * Serializes a sequence of {@link BranchIdType}.
	 */
	public StringBuilder toString(StringBuilder out, Iterable<? extends BranchIdType> elements) {
		out.append("[");
		for (BranchIdType elem : elements) {
			if (out.length() > 1) {
				out.append(", ");
			}
			toString(out, elem);
		}
		out.append("]");
		return out;
	}

	/**
	 * Serializes a {@link BranchIdType}.
	 */
	public StringBuilder toString(BranchIdType elem) {
		return toString(new StringBuilder(), elem);
	}

	/**
	 * Serializes a {@link BranchIdType}.
	 */
	public StringBuilder toString(StringBuilder out, BranchIdType elem) {
		elem.visit(ToString.INSTANCE, out);
		return out;
	}

	/**
	 * Serializes a {@link QualifiedTypeName}.
	 */
	public String qualifiedName(QualifiedTypeName elem) {
		return elem.getName();
	}

	/**
	 * Updates a {@link TLProperty}.
	 */
	public void updateTLProperty(PooledConnection con, TypePart part, Type newType, Type newOwner,
			String newName, Boolean mandatory, Boolean isAbstract, Boolean multiple, Boolean bag,
			Boolean ordered, AnnotatedConfig<TLAttributeAnnotation> annotations)
			throws SQLException {
		updateTLStructuredTypePart(con, part, newType, newOwner, newName, mandatory, isAbstract, null, null, multiple,
			bag, ordered, null, null, toString(annotations), null);
	}

	/**
	 * Updates an inverse {@link TLReference}.
	 * 
	 * <p>
	 * In contrast to the forwards reference, a new type or a new owner can not be set, because they
	 * are determined from the the corresponding forwards reference.
	 * </p>
	 * 
	 * @see #updateTLReference(PooledConnection, Reference, Type, Type, String, Boolean, Boolean,
	 *      Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, HistoryType, AnnotatedConfig,
	 *      TypePart)
	 */
	public void updateInverseReference(PooledConnection con, Reference inverseReference,
			String newName, Boolean mandatory, Boolean isAbstract, Boolean composite, Boolean aggregate,
			Boolean multiple, Boolean bag,
			Boolean ordered, Boolean navigate, HistoryType historyType,
			AnnotatedConfig<TLAttributeAnnotation> annotations, TypePart newEnd)
			throws SQLException {
		TLID endID = null;
		if (newEnd != null) {
			endID = newEnd.getID();
		} else {
			endID = inverseReference.getEndID();
		}
		if (endID == null) {
			throw new IllegalStateException("Reference " + toString(inverseReference) + " has no end id.");
		}
		BranchIdType associationEnd =
			BranchIdType.newInstance(BranchIdType.class, inverseReference.getBranch(), endID,
				ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE);

		updateTLStructuredTypePart(con, associationEnd, null, null, null,
			mandatory, isAbstract, composite, aggregate, multiple, bag, ordered, navigate, historyType,
			null, null);
		updateTLStructuredTypePart(con, inverseReference, null, null, newName,
			null, null, null, null, null, null, null, null, null,
			toString(annotations), newEnd);
	}

	/**
	 * Updates a {@link TLReference}.
	 * 
	 * @see #updateInverseReference(PooledConnection, Reference, String, Boolean, Boolean, Boolean,
	 *      Boolean, Boolean, Boolean, Boolean, Boolean, HistoryType, AnnotatedConfig, TypePart)
	 */
	public void updateTLReference(PooledConnection con, Reference reference, Type newType, Type newOwner,
			String newName, Boolean mandatory, Boolean isAbstract, Boolean composite, Boolean aggregate,
			Boolean multiple, Boolean bag,
			Boolean ordered, Boolean navigate, HistoryType historyType,
			AnnotatedConfig<TLAttributeAnnotation> annotations, TypePart newEnd)
			throws SQLException, MigrationException {

		TLID endID = null;
		if (newEnd != null) {
			endID = newEnd.getID();
		} else {
			endID = reference.getEndID();
		}
		if (endID == null) {
			throw new IllegalStateException("Reference " + toString(reference) + " has no end id.");
		}
		BranchIdType associationEnd =
			BranchIdType.newInstance(BranchIdType.class, reference.getBranch(), endID,
				ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE);

		// Name of the association end is the name as the reference.
		updateTLStructuredTypePart(con, associationEnd, newType, null, newName,
			mandatory, isAbstract, composite, aggregate, multiple, bag, ordered, navigate, historyType,
			null, null);
		updateTLStructuredTypePart(con, reference, null, newOwner, newName,
			null, null, null, null, null, null, null, null, null,
			toString(annotations), newEnd);

		if (newOwner != null || newType != null || newName != null) {
			Type associationType = getTLTypeOrFail(con, reference.getOwner().getModule(),
				TLStructuredTypeColumns.syntheticAssociationName(reference.getOwner().getTypeName(), reference.getPartName()));
			List<TypePart> parts = getTLStructuredTypeParts(con, associationType);
			if (parts.size() != 2) {
				throw new MigrationException("Association '" + toString(associationType)
					+ "' is expected to needs exactly two ends, but has " + toString(parts));
			}
			TypePart otherPart;
			if (endID.equals(parts.get(0).getID())) {
				otherPart = parts.get(1);
			} else if (endID.equals(parts.get(1).getID())) {
				otherPart = parts.get(0);
			} else {
				throw new MigrationException("Association '" + toString(associationType)
					+ "' is expected to have end with ID '" + endID + "', but has " + toString(parts));
			}
			if (newOwner != null || newName != null) {
				String newAssociationNameOwnerPart =
					newOwner != null ? newOwner.getTypeName() : reference.getOwner().getTypeName();
				String newAssociationNameAttributePart =
					newName != null ? newName : reference.getPartName();
				Module newModule = newOwner != null ? newOwner.getModule() : null;
				updateTLStructuredType(con, associationType, newModule,
					TLStructuredTypeColumns.syntheticAssociationName(newAssociationNameOwnerPart,
						newAssociationNameAttributePart),
					null, null, (String) null);
			}
			if (newOwner != null) {
				// owner of the association is part of the name of the association and the target
				// type
				// of the other end.
				updateTLStructuredTypePart(con, otherPart, newOwner, null, null, null, null, null, null, null, null,
					null, null, null, null, null);
			}
			if (newType != null) {
				// new type is the new owner of the inverse reference, if exists.

				CompiledStatement sql = query(
				parameters(
					branchParamDef(),
					parameterDef(DBType.ID, "endID"),
					parameterDef(DBType.ID, "ownerID")),
				update(
					table(SQLH.mangleDBName(ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE)),
					and(
						eqBranch(),
						eqSQL(
							column(refID(TLReference.END_ATTR)),
							parameter(DBType.ID, "endID"))),
						listWithoutNull(
						refID(TLReference.OWNER_ATTR)),
						listWithoutNull(
						parameter(DBType.ID, "ownerID")))).toSql(con.getSQLDialect());

				sql.executeUpdate(con, reference.getBranch(), otherPart.getID(), newType.getID());
			}
		}
	}

	/**
	 * Updates the given {@link TLStructuredTypePart}.
	 */
	public void updateTLStructuredTypePart(PooledConnection con, BranchIdType part, Type newType, Type newOwner,
			String name, Boolean mandatory, Boolean isAbstract, Boolean composite, Boolean aggregate, Boolean multiple,
			Boolean bag, Boolean ordered, Boolean navigate, HistoryType historyType, String annotations,
			TypePart newEnd)
			throws SQLException {
		List<Parameter> parameterDefs = new ArrayList<>();
		List<String> columns = new ArrayList<>();
		List<SQLExpression> parameters = new ArrayList<>();
		List<Object> arguments = new ArrayList<>();

		parameterDefs.add(branchParamDef());
		parameterDefs.add(parameterDef(DBType.ID, "identifier"));
		arguments.add(part.getBranch());
		arguments.add(part.getID());
		if (newType != null) {
			parameterDefs.add(parameterDef(DBType.STRING, "typeType"));
			columns.add(refType(TLStructuredTypePart.TYPE_ATTR));
			parameters.add(parameter(DBType.STRING, "typeType"));
			arguments.add(newType.getTable());
			parameterDefs.add(parameterDef(DBType.ID, "typeID"));
			columns.add(refID(TLStructuredTypePart.TYPE_ATTR));
			parameters.add(parameter(DBType.ID, "typeID"));
			arguments.add(newType.getID());
		}
		if (newOwner != null) {
			parameterDefs.add(parameterDef(DBType.ID, "ownerID"));
			columns.add(refID(TLClassPart.OWNER_ATTR));
			parameters.add(parameter(DBType.ID, "ownerID"));
			arguments.add(newOwner.getID());
		}
		if (name != null) {
			parameterDefs.add(parameterDef(DBType.STRING, "name"));
			columns.add(SQLH.mangleDBName(TLStructuredTypePart.NAME_ATTR));
			parameters.add(parameter(DBType.STRING, "name"));
			arguments.add(name);
		}
		if (mandatory != null) {
			parameterDefs.add(parameterDef(DBType.BOOLEAN, "mandatory"));
			columns.add(SQLH.mangleDBName(TLStructuredTypePart.MANDATORY_ATTR));
			parameters.add(parameter(DBType.BOOLEAN, "mandatory"));
			arguments.add(mandatory);
		}
		if (isAbstract != null) {
			parameterDefs.add(parameterDef(DBType.BOOLEAN, "abstract"));
			columns.add(SQLH.mangleDBName(TLStructuredTypePart.ABSTRACT_ATTR));
			parameters.add(parameter(DBType.BOOLEAN, "abstract"));
			arguments.add(isAbstract);
		}
		if (composite != null) {
			parameterDefs.add(parameterDef(DBType.BOOLEAN, "composite"));
			columns.add(SQLH.mangleDBName(TLAssociationEnd.COMPOSITE_ATTR));
			parameters.add(parameter(DBType.BOOLEAN, "composite"));
			arguments.add(composite);
		}
		if (aggregate != null) {
			parameterDefs.add(parameterDef(DBType.BOOLEAN, "aggregate"));
			columns.add(SQLH.mangleDBName(TLAssociationEnd.AGGREGATE_ATTR));
			parameters.add(parameter(DBType.BOOLEAN, "aggregate"));
			arguments.add(aggregate);
		}
		if (multiple != null) {
			parameterDefs.add(parameterDef(DBType.BOOLEAN, "multiple"));
			columns.add(SQLH.mangleDBName(TLReference.MULTIPLE_ATTR));
			parameters.add(parameter(DBType.BOOLEAN, "multiple"));
			arguments.add(multiple);
		}
		if (bag != null) {
			parameterDefs.add(parameterDef(DBType.BOOLEAN, "bag"));
			columns.add(SQLH.mangleDBName(TLReference.BAG_ATTR));
			parameters.add(parameter(DBType.BOOLEAN, "bag"));
			arguments.add(bag);
		}
		if (ordered != null) {
			parameterDefs.add(parameterDef(DBType.BOOLEAN, "ordered"));
			columns.add(SQLH.mangleDBName(TLAssociationEnd.ORDERED_ATTR));
			parameters.add(parameter(DBType.BOOLEAN, "ordered"));
			arguments.add(ordered);
		}
		if (navigate != null) {
			parameterDefs.add(parameterDef(DBType.BOOLEAN, "navigate"));
			columns.add(SQLH.mangleDBName(TLAssociationEnd.NAVIGATE_ATTR));
			parameters.add(parameter(DBType.BOOLEAN, "navigate"));
			arguments.add(navigate);
		}
		if (historyType != null) {
			parameterDefs.add(parameterDef(DBType.STRING, "historyType"));
			columns.add(SQLH.mangleDBName(TLAssociationEnd.HISTORY_TYPE_ATTR));
			parameters.add(parameter(DBType.STRING, "historyType"));
			arguments.add(historyType.getExternalName());
		}
		if (annotations != null) {
			parameterDefs.add(parameterDef(DBType.STRING, "annotations"));
			columns.add(SQLH.mangleDBName(PersistentModelPart.ANNOTATIONS_MO_ATTRIBUTE));
			parameters.add(parameter(DBType.STRING, "annotations"));
			arguments.add(annotations);
		}
		if (newEnd != null) {
			parameterDefs.add(parameterDef(DBType.ID, "endID"));
			columns.add(refID(TLReference.END_ATTR));
			parameters.add(parameter(DBType.ID, "endID"));
			arguments.add(newEnd.getID());
		}
		if (columns.isEmpty()) {
			return;
		}
		CompiledStatement sql = query(parameterDefs,
		update(
			table(SQLH.mangleDBName(ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE)),
			and(
				eqBranch(),
				eqSQL(
					column(BasicTypes.IDENTIFIER_DB_NAME),
					parameter(DBType.ID, "identifier"))),
			columns,
			parameters)).toSql(con.getSQLDialect());

		sql.executeUpdate(con, arguments.toArray());
	}

	/**
	 * Updates the given {@link TLStructuredType}.
	 */
	public void updateTLStructuredType(PooledConnection con, Type type, Module newModule, String newName,
			Boolean isAbstract, Boolean isFinal, AnnotatedConfig<TLTypeAnnotation> annotations)
			throws SQLException {
		updateTLStructuredType(con, type, newModule, newName, isAbstract, isFinal, toString(annotations));
	}

	/**
	 * Updates the given {@link TLStructuredType}.
	 */
	public void updateTLStructuredType(PooledConnection con, Type type, Module newModule, String newName,
			Boolean isAbstract, Boolean isFinal, String annotations) throws SQLException {
		List<Parameter> parameterDefs = new ArrayList<>();
		List<String> columns = new ArrayList<>();
		List<SQLExpression> parameters = new ArrayList<>();
		List<Object> arguments = new ArrayList<>();

		parameterDefs.add(branchParamDef());
		parameterDefs.add(parameterDef(DBType.ID, "identifier"));
		arguments.add(type.getBranch());
		arguments.add(type.getID());
		if (newModule != null) {
			columns.add(refType(ApplicationObjectUtil.META_ELEMENT_SCOPE_REF));
			parameters.add(literalString(newModule.getTable()));

			parameterDefs.add(parameterDef(DBType.ID, "moduleID"));
			columns.add(refID(ApplicationObjectUtil.META_ELEMENT_SCOPE_REF));
			parameters.add(parameter(DBType.ID, "moduleID"));
			columns.add(refID(TLClass.MODULE_ATTR));
			parameters.add(parameter(DBType.ID, "moduleID"));
			arguments.add(newModule.getID());
		}
		if (newName != null) {
			parameterDefs.add(parameterDef(DBType.STRING, "name"));
			columns.add(SQLH.mangleDBName(PersistentType.NAME_ATTR));
			parameters.add(parameter(DBType.STRING, "name"));
			arguments.add(newName);
		}
		if (isAbstract != null) {
			parameterDefs.add(parameterDef(DBType.BOOLEAN, "isAbstract"));
			columns.add(SQLH.mangleDBName(TLClass.ABSTRACT_ATTR));
			parameters.add(parameter(DBType.BOOLEAN, "isAbstract"));
			arguments.add(isAbstract);
		}
		if (isFinal != null) {
			parameterDefs.add(parameterDef(DBType.BOOLEAN, "isFinal"));
			columns.add(SQLH.mangleDBName(TLClass.FINAL_ATTR));
			parameters.add(parameter(DBType.BOOLEAN, "isFinal"));
			arguments.add(isFinal);
		}
		if (annotations != null) {
			parameterDefs.add(parameterDef(DBType.STRING, "annotations"));
			columns.add(SQLH.mangleDBName(PersistentModelPart.ANNOTATIONS_MO_ATTRIBUTE));
			parameters.add(parameter(DBType.STRING, "annotations"));
			arguments.add(annotations);
		}
		if (columns.isEmpty()) {
			return;
		}
		CompiledStatement sql = query(parameterDefs,
		update(
			table(SQLH.mangleDBName(ApplicationObjectUtil.META_ELEMENT_OBJECT_TYPE)),
			and(
				eqBranch(),
				eqSQL(
					column(BasicTypes.IDENTIFIER_DB_NAME),
					parameter(DBType.ID, "identifier"))),
			columns,
			parameters)).toSql(con.getSQLDialect());

		sql.executeUpdate(con, arguments.toArray());
	}

	/**
	 * Updates the given {@link TLPrimitive}.
	 */
	public void updateTLDataType(PooledConnection con, Type type, Module newModule, String newName, Kind kind,
			DBColumnType columnType, PolymorphicConfiguration<StorageMapping<?>> storageMapping,
			AnnotatedConfig<TLTypeAnnotation> annotations) throws SQLException {
		String dbType;
		Integer dbSize;
		Integer dbPrecision;
		Boolean isBinary;
		if (columnType != null) {
			dbType = columnType.getDBType().getExternalName();
			dbSize = columnType.getDBSize();
			dbPrecision = columnType.getDBPrecision();
			isBinary = columnType.isBinary();
		} else {
			dbType = null;
			dbSize = null;
			dbPrecision = null;
			isBinary = null;
		}
		updateTLDataType(con, type, newModule, newName, kind, dbType, dbSize, dbPrecision, isBinary,
			toString(storageMapping), toString(annotations));
	}

	/**
	 * Updates the given {@link TLPrimitive}.
	 */
	public void updateTLDataType(PooledConnection con, Type type, Module newModule, String newName,
			Kind kind, String dbType, Integer dbSize, Integer dbPrecision, Boolean isBinary, String storageMapping,
			String annotations) throws SQLException {
		List<Parameter> parameterDefs = new ArrayList<>();
		List<String> columns = new ArrayList<>();
		List<SQLExpression> parameters = new ArrayList<>();
		List<Object> arguments = new ArrayList<>();

		parameterDefs.add(branchParamDef());
		parameterDefs.add(parameterDef(DBType.ID, "identifier"));
		arguments.add(type.getBranch());
		arguments.add(type.getID());
		if (newModule != null) {
			columns.add(refType(ApplicationObjectUtil.META_ELEMENT_SCOPE_REF));
			parameters.add(literalString(newModule.getTable()));

			parameterDefs.add(parameterDef(DBType.ID, "moduleID"));
			columns.add(refID(ApplicationObjectUtil.META_ELEMENT_SCOPE_REF));
			parameters.add(parameter(DBType.ID, "moduleID"));
			columns.add(refID(TLClass.MODULE_ATTR));
			parameters.add(parameter(DBType.ID, "moduleID"));
			arguments.add(newModule.getID());
		}
		if (newName != null) {
			parameterDefs.add(parameterDef(DBType.STRING, "name"));
			columns.add(SQLH.mangleDBName(PersistentType.NAME_ATTR));
			parameters.add(parameter(DBType.STRING, "name"));
			arguments.add(newName);
		}
		if (kind != null) {
			parameterDefs.add(parameterDef(DBType.STRING, "kind"));
			columns.add(SQLH.mangleDBName(TLPrimitiveColumns.KIND_ATTR));
			parameters.add(parameter(DBType.STRING, "kind"));
			arguments.add(kind.getExternalName());
		}
		if (dbType != null) {
			parameterDefs.add(parameterDef(DBType.STRING, "dbType"));
			columns.add(SQLH.mangleDBName(TLPrimitiveColumns.DB_TYPE_ATTR));
			parameters.add(parameter(DBType.STRING, "dbType"));
			arguments.add(dbType);
		}
		if (dbSize != null) {
			parameterDefs.add(parameterDef(DBType.INT, "dbSize"));
			columns.add(SQLH.mangleDBName(TLPrimitiveColumns.DB_SIZE_ATTR));
			parameters.add(parameter(DBType.INT, "dbSize"));
			arguments.add(dbSize);
		}
		if (dbPrecision != null) {
			parameterDefs.add(parameterDef(DBType.INT, "dbPrecision"));
			columns.add(SQLH.mangleDBName(TLPrimitiveColumns.DB_PRECISION_ATTR));
			parameters.add(parameter(DBType.INT, "dbPrecision"));
			arguments.add(dbPrecision);
		}
		if (isBinary != null) {
			parameterDefs.add(parameterDef(DBType.BOOLEAN, "isBinary"));
			columns.add(SQLH.mangleDBName(TLPrimitiveColumns.BINARY_ATTR));
			parameters.add(parameter(DBType.BOOLEAN, "isBinary"));
			arguments.add(isBinary);
		}
		if (storageMapping != null) {
			parameterDefs.add(parameterDef(DBType.STRING, "storageMapping"));
			columns.add(SQLH.mangleDBName(TLPrimitiveColumns.STORAGE_MAPPING));
			parameters.add(parameter(DBType.STRING, "storageMapping"));
			arguments.add(storageMapping);
		}
		if (annotations != null) {
			parameterDefs.add(parameterDef(DBType.STRING, "annotations"));
			columns.add(SQLH.mangleDBName(PersistentModelPart.ANNOTATIONS_MO_ATTRIBUTE));
			parameters.add(parameter(DBType.STRING, "annotations"));
			arguments.add(annotations);
		}
		if (columns.isEmpty()) {
			return;
		}
		CompiledStatement sql = query(parameterDefs,
			update(
				table(SQLH.mangleDBName(TLPrimitiveColumns.OBJECT_TYPE)),
				and(
					eqBranch(),
					eqSQL(
						column(BasicTypes.IDENTIFIER_DB_NAME),
						parameter(DBType.ID, "identifier"))),
				columns,
				parameters)).toSql(con.getSQLDialect());

		sql.executeUpdate(con, arguments.toArray());
	}

	/**
	 * Deletes the rows for given model part.
	 */
	public void deleteModelPart(PooledConnection connection, BranchIdType modelPart)
			throws SQLException {
		CompiledStatement delete = query(
		parameters(
			branchParamDef(),
			parameterDef(DBType.ID, "id")),
		delete(
			table(SQLH.mangleDBName(modelPart.getTable())),
			and(
				eqBranch(),
				eqSQL(
					column(BasicTypes.IDENTIFIER_DB_NAME),
					parameter(DBType.ID, "id"))))).toSql(connection.getSQLDialect());
	
		delete.executeUpdate(connection, modelPart.getBranch(), modelPart.getID());
	}

	/**
	 * Deletes the rows for given model parts.
	 */
	public void deleteModelParts(PooledConnection connection, Collection<? extends BranchIdType> modelParts)
			throws SQLException {
		Map<String, Map<Long, List<BranchIdType>>> byTypeAndBranch = new HashMap<>();
		for (BranchIdType entry : modelParts) {
			Map<Long, List<BranchIdType>> byBranch =
				byTypeAndBranch.computeIfAbsent(entry.getTable(), key -> new HashMap<>());
			List<BranchIdType> elements = byBranch.computeIfAbsent(entry.getBranch(), key -> new ArrayList<>());
			elements.add(entry);
		}

		for (Map<Long, List<BranchIdType>> tmp1 : byTypeAndBranch.values()) {
			for (List<BranchIdType> tmp2 : tmp1.values()) {
				deleteModelPartsHomogeneous(connection, tmp2);
			}
		}
	}

	/**
	 * Deletes the rows for given model parts.
	 * 
	 * <p>
	 * <b>Note:</b> All elements must be stored in the same table. For inhomogeneous list of model
	 * parts use {@link #deleteModelParts(PooledConnection, Collection)}
	 * </p>
	 */
	public void deleteModelPartsHomogeneous(PooledConnection connection,
			Collection<? extends BranchIdType> modelParts)
			throws SQLException {
		switch (modelParts.size()) {
			case 0:
				return;
			case 1:
				deleteModelPart(connection, modelParts.iterator().next());
				break;
			default:
				BranchIdType representative = modelParts.iterator().next();
				CompiledStatement delete = query(
				parameters(
					branchParamDef(),
					setParameterDef("id", DBType.ID)),
				delete(
					table(SQLH.mangleDBName(representative.getTable())),
					and(
						eqBranch(),
						inSet(
							column(BasicTypes.IDENTIFIER_DB_NAME),
							setParameter("id", DBType.ID))))).toSql(connection.getSQLDialect());

				Set<TLID> ids = modelParts.stream().map(BranchIdType::getID).collect(Collectors.toSet());
				delete.executeUpdate(connection, representative.getBranch(), ids);
		}
	}

	/**
	 * Deletes the given type and all its attributes.
	 * 
	 * @param type
	 *        The {@link Type} to delete.
	 * @param failOnExistingAttributes
	 *        Whether operation must fail, when given type has attributes.
	 */
	public void deleteTLType(PooledConnection connection, Type type, boolean failOnExistingAttributes)
			throws SQLException, MigrationException {
		List<BranchIdType> toDelete = new ArrayList<>();
	
		toDelete.add(type);
	
		List<TypePart> tlTypeParts = getTLTypeParts(connection, type);
		if (!tlTypeParts.isEmpty() && failOnExistingAttributes) {
			throw new MigrationException("Type '" + toString(type) + "' has parts: " + toString(tlTypeParts));
		}
		toDelete.addAll(tlTypeParts);

		toDelete.addAll(getGeneralizations(connection, type));
		toDelete.addAll(getSpecializations(connection, type));

		deleteModelParts(connection, toDelete);

		// Delete associations of forwards references.
		for (TypePart part : tlTypeParts) {
			if (part instanceof Reference ref) {
				Type association = getTLTypeOrNull(connection, ref.getOwner().getModule(),
					TLStructuredTypeColumns.syntheticAssociationName(ref.getOwner().getTypeName(), ref.getPartName()));
				if (association != null) {
					deleteTLType(connection, association, false);
				}
			}
		}

		// Not necessary: Delete references of ends. The migration must have produced an explicit
		// delete of potentially existing reverse references pointing to deleted classes.
	}

	/**
	 * Whether branch support is enabled.
	 */
	public boolean hasBranches() {
		return _branchSupport;
	}

	/**
	 * Name of the branch column, or <code>null</code> if the application has no branch support.
	 */
	public String branchColumnOrNull() {
		if (_branchSupport) {
			return BasicTypes.BRANCH_DB_NAME;
		} else {
			return null;
		}
	}

	/**
	 * {@link SQLParameter} for the branch column, or <code>null</code>, if the application has no
	 * branch support.
	 */
	public SQLExpression branchParamOrNull() {
		if (_branchSupport) {
			return parameter(DBType.LONG, "branch");
		} else {
			return null;
		}
	}

	/**
	 * {@link SQLExpression} for the trunk branch ID.
	 */
	public SQLLiteral trunkBranch() {
		return literalLong(1L);
	}

	/**
	 * {@link Parameter} for a branch ID.
	 */
	public Parameter branchParamDef() {
		return parameterDef(DBType.LONG, "branch");
	}

	/**
	 * Check for the branch column matching a branch parameter.
	 */
	public SQLExpression eqBranch() {
		if (_branchSupport) {
			return eqSQL(
				column(branchColumnOrNull()),
				branchParamOrNull());
		} else {
			return SQLFactory.literalTrueLogical();
		}
	}

	/**
	 * The given values as list excluding <code>null</code> values.
	 */
	@SafeVarargs
	public static <T> List<T> listWithoutNull(T... values) {
		return listWithoutNull(Arrays.asList(values));
	}

	/**
	 * Filters the given list such that no <code>null</code> values are contained.
	 */
	public static <T> List<T> listWithoutNull(List<T> result) {
		if (result.contains(null)) {
			return result.stream().filter(Objects::nonNull).collect(Collectors.toList());
		}
		return result;
	}

	/**
	 * A select column returning the object's branch.
	 */
	public SQLColumnDefinition branchColumnDef() {
		return branchColumnDef(NO_TABLE_ALIAS);
	}

	/**
	 * A select column returning the object's branch.
	 */
	public SQLColumnDefinition branchColumnDef(String tableAlias) {
		if (_branchSupport) {
			return columnDef(column(tableAlias, BasicTypes.BRANCH_DB_NAME));
		} else {
			return columnDef(trunkBranch(), BasicTypes.BRANCH_DB_NAME);
		}
	}

	/**
	 * A select column returning the object's branch.
	 */
	public SQLColumnDefinition branchColumnDefOrNull() {
		return branchColumnDefOrNull(NO_TABLE_ALIAS);
	}

	/**
	 * A select column returning the object's branch.
	 */
	public SQLColumnDefinition branchColumnDefOrNull(String tableAlias) {
		if (_branchSupport) {
			return columnDef(column(tableAlias, BasicTypes.BRANCH_DB_NAME));
		} else {
			return null;
		}
	}

	/**
	 * The branch of the object.
	 */
	public SQLExpression branchColumnRef() {
		return branchColumnRef(NO_TABLE_ALIAS);
	}

	/**
	 * The branch of the object.
	 */
	public SQLExpression branchColumnRef(String tableAlias) {
		if (_branchSupport) {
			return column(tableAlias, SQLH.mangleDBName(BasicTypes.BRANCH_DB_NAME));
		} else {
			return trunkBranch();
		}
	}

	/**
	 * An order expression for the branch column, or <code>null</code>, if no branches are
	 * supported.
	 */
	public SQLOrder branchOrderOrNull() {
		return branchOrderOrNull(NO_TABLE_ALIAS);
	}

	/**
	 * An order expression for the branch column, or <code>null</code>, if no branches are
	 * supported.
	 */
	public SQLOrder branchOrderOrNull(String tableAlias) {
		return _branchSupport ? order(false, branchColumnRef(tableAlias)) : null;
	}

	/**
	 * Increment to the column index depending on the existence of the branch column.
	 */
	public int getBranchIndexInc() {
		return _branchSupport ? 1 : 0;
	}

	/**
	 * Whether the table {@link ApplicationObjectUtil#META_ATTRIBUTE_OBJECT_TYPE} has a column
	 * {@link TLAssociationEnd#HISTORY_TYPE_ATTR}.
	 */
	public boolean hasHistoryColumn() {
		return _historyColumn;
	}

	/**
	 * Sets value of {@link #hasHistoryColumn()}.
	 */
	public void setHistoryColumn(boolean historyColumn) {
		_historyColumn = historyColumn;
	}

	/**
	 * Whether the model already has the column that stores the deletion policy for association
	 * ends.
	 * 
	 * @see TLAssociationEnd#getDeletionPolicy()
	 */
	public boolean hasDeletionColumn() {
		return _deletionColumn;
	}

	/**
	 * @see #hasDeletionColumn()
	 */
	public void setDeletionColumn(boolean value) {
		_deletionColumn = value;
	}

	/**
	 * Whether the table {@link ApplicationObjectUtil#META_ATTRIBUTE_OBJECT_TYPE} has a column
	 * {@link TLStructuredTypePart#ABSTRACT_ATTR}.
	 */
	public boolean hasAbstractColumn() {
		return _abstractColumn;
	}

	/**
	 * Sets value of {@link #hasAbstractColumn()}.
	 */
	public void setAbstractColumn(boolean abstractColumn) {
		_abstractColumn = abstractColumn;
	}

	/**
	 * Looks up all identifiers of potential subclasses of the given type.
	 */
	public Collection<TLID> getImplementationIds(PooledConnection connection, Type type) throws SQLException {
		Set<TLID> result = new HashSet<>();
		result.add(type.getID());
		List<Type> worklist = new ArrayList<>();
		worklist.add(type);

		for (int n = 0; n < worklist.size(); n++) {
			List<TypeGeneralization> specializationLinks = getTLClassSpecializationLinks(connection, worklist.get(n));
			for (TypeGeneralization link : specializationLinks) {
				TLID specializationId = link.getSource();
				if (result.add(specializationId)) {
					worklist
						.add(BranchIdType.newInstance(Type.class, type.getBranch(), specializationId, type.getTable()));
				}
			}
		}
		return result;
	}

	/**
	 * Resolves a model part based on its qualified name.
	 * 
	 * @see TLModelUtil#resolveModelPart(String)
	 */
	public BranchIdType getModelPartOrFail(PooledConnection connection, long branch, String qualifiedName)
			throws SQLException, MigrationException {
		int partSeparatorIndex = qualifiedName.lastIndexOf(TLModelUtil.QUALIFIED_NAME_PART_SEPARATOR);
		if (partSeparatorIndex < 0) {
			return resolveModuleOrType(connection, branch, qualifiedName);
		}
		String scopeName = qualifiedName.substring(0, partSeparatorIndex);
		String partName = qualifiedName.substring(partSeparatorIndex + 1);

		int moduleSep = scopeName.indexOf(TLModelUtil.QUALIFIED_NAME_SEPARATOR);
		if (moduleSep >= 0) {
			String moduleName = scopeName.substring(0, moduleSep);
			String typeName = scopeName.substring(moduleSep + 1);
			return getTLTypePartOrFail(connection, branch, moduleName, typeName, partName);
		} else {
			throw new UnsupportedOperationException("Resolving singletons during migration not implemented.");
		}
	}

	private BranchIdType resolveModuleOrType(PooledConnection connection, long branch, String qualifiedName)
			throws SQLException, MigrationException {
		int moduleSep = qualifiedName.indexOf(TLModelUtil.QUALIFIED_NAME_SEPARATOR);
		if (moduleSep >= 0) {
			String moduleName = qualifiedName.substring(0, moduleSep);
			String typeName = qualifiedName.substring(moduleSep + 1);
			return getTLTypeOrFail(connection, branch, moduleName, typeName);
		} else {
			return getTLModuleOrFail(connection, qualifiedName);
		}
	}

}
