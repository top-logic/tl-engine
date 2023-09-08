/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.io.IOError;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.LongID;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.ConfigBuilder;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.copy.ConfigCopier;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLQuery.Parameter;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.schema.config.DBColumnType;
import com.top_logic.element.meta.PersistentClass;
import com.top_logic.element.meta.kbbased.KBBasedMetaAttribute;
import com.top_logic.element.meta.kbbased.KBBasedMetaAttributeFactory;
import com.top_logic.element.meta.kbbased.KBBasedMetaElement;
import com.top_logic.element.meta.kbbased.KBBasedMetaElementFactory;
import com.top_logic.element.meta.kbbased.PersistentEnd;
import com.top_logic.element.meta.kbbased.PersistentReference;
import com.top_logic.element.meta.kbbased.PersistentStructuredTypePart;
import com.top_logic.element.model.ModelResolver;
import com.top_logic.element.model.PersistentDatatype;
import com.top_logic.element.model.PersistentModule;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.DestinationReference;
import com.top_logic.knowledge.service.db2.PersistentIdFactory;
import com.top_logic.knowledge.service.db2.RowLevelLockingSequenceManager;
import com.top_logic.knowledge.service.db2.SourceReference;
import com.top_logic.knowledge.util.OrderedLinkUtil;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
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
import com.top_logic.model.internal.PersistentModelPart;
import com.top_logic.model.internal.PersistentModelPart.AnnotationConfigs;
import com.top_logic.model.internal.PersistentType;
import com.top_logic.model.internal.PersistentTypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.TLContext;

/**
 * Utility class for migration processors updating the {@link TLModel}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class Util {

	/** @see #createTLClassifier(PooledConnection, QualifiedPartName, int, AnnotatedConfig) */
	public static final int NO_SORT_ORDER = -1;

	private static long _stopId = 0;

	private static long _nextId = 0;

	private static long _revCreate = -1;

	/**
	 * Fetches a new ID for elements to create.
	 */
	public static TLID newID(PooledConnection con) throws SQLException {
		if (_nextId == _stopId) {
			DBHelper sqlDialect = con.getSQLDialect();
			long nextChunk = new RowLevelLockingSequenceManager().nextSequenceNumber(sqlDialect, con,
				sqlDialect.retryCount(), DBKnowledgeBase.ID_SEQ);
			_stopId = 1 + nextChunk * PersistentIdFactory.CHUNK_SIZE;
			_nextId = _stopId - PersistentIdFactory.CHUNK_SIZE;
		}
		return LongID.valueOf(_nextId++);
	}

	/**
	 * Gets the revision in which the {@link TLModel} was created.
	 */
	public static long getRevCreate(PooledConnection con) throws SQLException {
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
	public static void resetCachedRevCreate() {
		_revCreate = -1;
	}

	private static String toString(AnnotatedConfig<? extends TLAnnotation> annotations) throws XMLStreamException {
		String annotationsAsStrings;
		if (annotations == null || annotations.getAnnotations().isEmpty()) {
			annotationsAsStrings = null;
		} else {
			ConfigBuilder sink = TypedConfiguration.createConfigBuilder(AnnotationConfigs.class);
			ConfigCopier.fillDeepCopy(annotations, sink, SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY);
			try (StringWriter out = new StringWriter()) {
				new ConfigurationWriter(out).write("config", ConfigurationItem.class, sink);
				annotationsAsStrings = out.toString();
			} catch (IOException ex) {
				// StringWriter does not throw IOException!
				throw new IOError(ex);
			}
		}
		return annotationsAsStrings;
	}

	/**
	 * Creates a new {@link TLClass}.
	 * 
	 * @param className
	 *        Name of the {@link TLClass} to create.
	 */
	public static Type createTLClass(PooledConnection con, QualifiedTypeName className,
			boolean isAbstract, boolean isFinal,
			AnnotatedConfig<TLTypeAnnotation> annotations)
			throws SQLException, MigrationException, XMLStreamException {
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
	public static Type createTLStructuredType(PooledConnection con, long branch,
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
			parameterDef(DBType.LONG, "branch"),
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
			Arrays.asList(
				BasicTypes.BRANCH_DB_NAME,
				BasicTypes.IDENTIFIER_DB_NAME,
				BasicTypes.REV_MAX_DB_NAME,
				BasicTypes.REV_MIN_DB_NAME,
				BasicTypes.REV_CREATE_DB_NAME,
				SQLH.mangleDBName(PersistentModelPart.ANNOTATIONS_MO_ATTRIBUTE),
				SQLH.mangleDBName(TLClass.NAME_ATTR),
				refID(PersistentType.MODULE_REF),
				refType(PersistentType.SCOPE_REF),
				refID(PersistentType.SCOPE_REF),
				SQLH.mangleDBName(KBBasedMetaElement.META_ELEMENT_IMPL),
				SQLH.mangleDBName(KBBasedMetaElement.ABSTRACT_ATTR),
				SQLH.mangleDBName(KBBasedMetaElement.FINAL_ATTR)),
			Arrays.asList(
				parameter(DBType.LONG, "branch"),
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
			associationType ? KBBasedMetaElementFactory.ASSOCIATION_TYPE : KBBasedMetaElementFactory.CLASS_TYPE;
		createType.executeUpdate(con, module.getBranch(), newIdentifier, revCreate, annotations, className,
			module.getID(), impl, isAbstract, isFinal);

		Type type =
			BranchIdType.newInstance(Type.class, module.getBranch(), newIdentifier, TlModelFactory.KO_NAME_TL_CLASS);
		type.setTypeName(className);
		type.setModule(module);
		return type;
	}

	/**
	 * Creates a new {@link TLAssociation}.
	 * 
	 * @param associationName
	 *        Name of the association to create.
	 */
	public static Type createTLAssociation(PooledConnection con, QualifiedTypeName associationName,
			AnnotatedConfig<TLTypeAnnotation> annotations)
			throws SQLException, MigrationException, XMLStreamException {
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
	public static TypePart createTLProperty(PooledConnection con, QualifiedPartName name, QualifiedTypeName target,
			boolean isMandatory, boolean isMultiple, Boolean bag, Boolean ordered,
			AnnotatedConfig<TLAttributeAnnotation> annotations)
			throws SQLException, MigrationException, XMLStreamException {
		return createTLProperty(con, TLContext.TRUNK_ID,
			name.getModuleName(), name.getTypeName(), name.getPartName(),
			target.getModuleName(), target.getTypeName(),
			isMandatory, isMultiple, bag, ordered,
			toString(annotations));
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
	public static TypePart createTLProperty(PooledConnection con, long branch,
			String module, String className, String partName,
			String targetModule, String targetType,
			boolean mandatory, boolean multiple, Boolean bag, Boolean ordered,
			String annotations)
			throws SQLException, MigrationException {
		if (multiple) {
			if (bag == null) {
				bag = Boolean.FALSE;
			}
			if (ordered == null) {
				ordered = Boolean.FALSE;
			}
		} else {
			// Only set if multiple
			bag = null;
			ordered = null;
		}
		TLID partID = newID(con);
		TLID definitionID = partID;

		TLID endID = IdentifierUtil.nullIdForMandatoryDatabaseColumns();
		Boolean composite = null;
		Boolean aggregate = null;
		Boolean navigate = null;

		return createTLStructuredTypePart(con, branch, module, className, partName, partID, targetModule,
			targetType, KBBasedMetaAttributeFactory.CLASS_PROPERTY_IMPL, endID, definitionID, mandatory,
			composite, aggregate, multiple, bag, ordered,
			navigate, annotations);
	}

	/**
	 * Creates a new {@link TLAssociationEnd}.
	 * 
	 * @param assEnd
	 *        Name of the {@link TLAssociationEnd} to create.
	 * @param target
	 *        Name of {@link TLAssociationEnd#getType()} of the new {@link TLAssociationEnd}.
	 */
	public static TypePart createTLAssociationEnd(PooledConnection con,
			QualifiedPartName assEnd, QualifiedTypeName target,
			boolean mandatory, boolean composite, boolean aggregate, boolean multiple,
			boolean bag, boolean ordered, boolean navigate,
			AnnotatedConfig<TLAttributeAnnotation> annotations)
			throws SQLException, MigrationException, XMLStreamException {
		return createTLAssociationEnd(con, TLContext.TRUNK_ID,
			assEnd.getModuleName(), assEnd.getTypeName(), assEnd.getPartName(),
			target.getModuleName(), target.getTypeName(),
			mandatory, composite, aggregate, multiple, bag, ordered, navigate,
			toString(annotations));
	}

	/**
	 * Creates a new {@link TLAssociationEnd}.
	 * 
	 * @param moduleName
	 *        Name of the {@link TLModule} of the {@link TLAssociationEnd} to create.
	 * @param ownerName
	 *        Name of {@link TLAssociationEnd#getOwner()} of the new {@link TLAssociationEnd}.
	 * @param partName
	 *        Name of the new {@link TLAssociationEnd}.
	 */
	public static TypePart createTLAssociationEnd(PooledConnection con, long branch,
			String moduleName, String ownerName, String partName,
			String targetModule, String targetTypeName,
			boolean mandatory, boolean composite, boolean aggregate, boolean multiple,
			boolean bag, boolean ordered, boolean navigate,
			String annotations)
			throws SQLException, MigrationException {
		TLID partID = newID(con);
		TLID endID = IdentifierUtil.nullIdForMandatoryDatabaseColumns();
		TLID definitionID = IdentifierUtil.nullIdForMandatoryDatabaseColumns();

		return createTLStructuredTypePart(con, branch, moduleName, ownerName, partName, partID, targetModule,
			targetTypeName, KBBasedMetaAttributeFactory.ASSOCIATION_END_IMPL, endID, definitionID, mandatory,
			composite, aggregate, multiple, bag, ordered,
			navigate, annotations);
	}

	private static Reference internalCreateTLReference(PooledConnection con, long branch,
			String moduleName, String ownerName, String partName,
			TLID endID,
			String annotations)
			throws SQLException, MigrationException {
		TLID partID = newID(con);
		TLID definitionID = partID;

		String targetTable = null;
		TLID targetID = IdentifierUtil.nullIdForMandatoryDatabaseColumns();
		Boolean isMandatory = null;
		Boolean isMultiple = null;
		Boolean composite = null;
		Boolean aggregate = null;
		Boolean ordered = null;
		Boolean bag = null;
		Boolean navigate = null;
		return (Reference) createTLStructuredTypePart(con, branch, moduleName, ownerName, partName, partID, targetTable,
			targetID, KBBasedMetaAttributeFactory.REFERENCE_IMPL, endID, definitionID, isMandatory,
			composite, aggregate, isMultiple, bag, ordered,
			navigate, annotations);
	}

	private static TypePart createTLStructuredTypePart(PooledConnection con, long branch,
			String moduleName, String ownerName, String partName, TLID partID,
			String targetModule, String targetTypeName,
			String impl, TLID endID, TLID definitionID,
			Boolean mandatory, Boolean composite, Boolean aggregate, Boolean multiple,
			Boolean bag, Boolean ordered, Boolean navigate,
			String annotations)
			throws SQLException, MigrationException {
		Type targetType = getTLTypeOrFail(con, branch, targetModule, targetTypeName);
		if (targetType == null) {
			throw new MigrationException("No type " + TLModelUtil.qualifiedName(targetModule, targetTypeName)
				+ " in branch " + branch + " found.");
		}

		return createTLStructuredTypePart(con, branch, moduleName, ownerName, partName, partID, targetType.getTable(),
			targetType.getID(), impl, endID, definitionID, mandatory, composite, aggregate, multiple, bag,
			ordered, navigate, annotations);
	}

	private static TypePart createTLStructuredTypePart(PooledConnection con, long branch,
			String moduleName, String ownerName, String partName, TLID partID,
			String targetTable, TLID targetID,
			String impl, TLID endID, TLID definitionID,
			Boolean mandatory, Boolean composite, Boolean aggregate, Boolean multiple,
			Boolean bag, Boolean ordered, Boolean navigate,
			String annotations)
			throws SQLException, MigrationException {
		DBHelper sqlDialect = con.getSQLDialect();

		Type ownerClass = getTLTypeOrFail(con, branch, moduleName, ownerName);
		if (ownerClass == null) {
			throw new MigrationException("No type " + TLModelUtil.qualifiedName(moduleName, ownerName)
				+ " in branch " + branch + " found.");
		}
		int ownerOrder = newAttributeOrder(con, branch, ownerClass.getID());
		Long revCreate = getRevCreate(con);

		CompiledStatement createProperty = query(
		parameters(
			parameterDef(DBType.LONG, "branch"),
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
			parameterDef(DBType.BOOLEAN, "multiple"),
			parameterDef(DBType.BOOLEAN, "composite"),
			parameterDef(DBType.BOOLEAN, "aggregate"),
			parameterDef(DBType.BOOLEAN, "ordered"),
			parameterDef(DBType.BOOLEAN, "bag"),
			parameterDef(DBType.BOOLEAN, "navigate")),
		insert(
			table(SQLH.mangleDBName(ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE)),
			Arrays.asList(
				BasicTypes.BRANCH_DB_NAME,
				BasicTypes.IDENTIFIER_DB_NAME,
				BasicTypes.REV_MAX_DB_NAME,
				BasicTypes.REV_MIN_DB_NAME,
				BasicTypes.REV_CREATE_DB_NAME,
				SQLH.mangleDBName(PersistentModelPart.ANNOTATIONS_MO_ATTRIBUTE),
				SQLH.mangleDBName(TLClass.NAME_ATTR),
				SQLH.mangleDBName(KBBasedMetaAttribute.IMPLEMENTATION_NAME),
				refID(KBBasedMetaAttribute.OWNER_REF),
				SQLH.mangleDBName(KBBasedMetaAttribute.OWNER_REF_ORDER_ATTR),
				refType(KBBasedMetaAttribute.TYPE_REF),
				refID(KBBasedMetaAttribute.TYPE_REF),
				refID(PersistentReference.END_ATTR),
				refID(KBBasedMetaAttribute.DEFINITION_REF),
				SQLH.mangleDBName(KBBasedMetaAttribute.MANDATORY),
				SQLH.mangleDBName(KBBasedMetaAttribute.MULTIPLE_ATTR),
				SQLH.mangleDBName(PersistentEnd.COMPOSITE_ATTR),
				SQLH.mangleDBName(PersistentEnd.AGGREGATE_ATTR),
				SQLH.mangleDBName(PersistentEnd.ORDERED_ATTR),
				SQLH.mangleDBName(PersistentEnd.BAG_ATTR),
				SQLH.mangleDBName(PersistentEnd.NAVIGATE_ATTR)),
			Arrays.asList(
				parameter(DBType.LONG, "branch"),
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
				parameter(DBType.BOOLEAN, "multiple"),
				parameter(DBType.BOOLEAN, "composite"),
				parameter(DBType.BOOLEAN, "aggregate"),
				parameter(DBType.BOOLEAN, "ordered"),
				parameter(DBType.BOOLEAN, "bag"),
				parameter(DBType.BOOLEAN, "navigate")))).toSql(sqlDialect);

		createProperty.executeUpdate(con, branch, partID, revCreate, annotations, partName, impl,
			ownerClass.getID(),
			ownerOrder, targetTable, targetID, endID, definitionID, mandatory, multiple,
			composite, aggregate, ordered, bag, navigate);

		TypePart typePart;
		if (KBBasedMetaAttributeFactory.REFERENCE_IMPL.equals(impl)) {
			typePart = BranchIdType.newInstance(Reference.class,
				branch,
				partID,
				ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE);
			typePart.setOwner(ownerClass);
			typePart.setPartName(partName);
			typePart.setDefinition(definitionID);
			((Reference) typePart).setEndID(endID);
		} else {
			typePart = BranchIdType.newInstance(TypePart.class,
				branch,
				partID,
				ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE);
			typePart.setOwner(ownerClass);
			typePart.setPartName(partName);
			typePart.setDefinition(definitionID);
		}
		return typePart;
	}

	private static List<Integer> getOrders(PooledConnection con, long branch, TLID ownerId, String orderAttribute,
			String table, String ownerRef) throws SQLException {
		CompiledStatement selectMaxOrder = query(
		parameters(
			parameterDef(DBType.LONG, "branch"),
			parameterDef(DBType.ID, "ownerID")),
		selectDistinct(
			columns(
				columnDef(column(SQLH.mangleDBName(orderAttribute)))),
			table(SQLH.mangleDBName(table)),
			and(
				eqSQL(
					column(BasicTypes.BRANCH_DB_NAME),
					parameter(DBType.LONG, "branch")),
				eqSQL(
					column(
						refID(ownerRef)),
					parameter(DBType.ID, "ownerID"))),
			orders(order(false, column(SQLH.mangleDBName(orderAttribute)))))).toSql(con.getSQLDialect());
		List<Integer> attributeOrders = new ArrayList<>();
		try (ResultSet dbResult = selectMaxOrder.executeQuery(con, branch, ownerId)) {
			while (dbResult.next()) {
				attributeOrders.add(dbResult.getInt(1));
			}
		}
		return attributeOrders;
	}

	/**
	 * Creates a new {@link TLModule}.
	 */
	public static Module createTLModule(PooledConnection con, String moduleName,
			AnnotatedConfig<TLModuleAnnotation> annotations)
			throws SQLException, MigrationException, XMLStreamException {
		return createTLModule(con, TLContext.TRUNK_ID, moduleName, toString(annotations));
	}

	/**
	 * Creates a new {@link TLModule}.
	 */
	public static Module createTLModule(PooledConnection con, long branch, String moduleName, String annotations)
			throws SQLException, MigrationException {
		DBHelper sqlDialect = con.getSQLDialect();

		TLID modelId = getTLModuleOrFail(con, branch, TlModelFactory.TL_MODEL_STRUCTURE).getModel();
		TLID newIdentifier = newID(con);
		Long revCreate = getRevCreate(con);

		CompiledStatement createModule = query(
		parameters(
			parameterDef(DBType.LONG, "branch"),
			parameterDef(DBType.ID, "identifier"),
			parameterDef(DBType.LONG, "revCreate"),
			parameterDef(DBType.STRING, "annotations"),
			parameterDef(DBType.STRING, "name")),
		insert(
			table(SQLH.mangleDBName(TlModelFactory.KO_NAME_TL_MODULE)),
			Arrays.asList(
				BasicTypes.BRANCH_DB_NAME,
				BasicTypes.IDENTIFIER_DB_NAME,
				BasicTypes.REV_MAX_DB_NAME,
				BasicTypes.REV_MIN_DB_NAME,
				BasicTypes.REV_CREATE_DB_NAME,
				SQLH.mangleDBName(PersistentModelPart.ANNOTATIONS_MO_ATTRIBUTE),
				SQLH.mangleDBName(TLModule.NAME_ATTR),
				refID(TLModule.MODEL_ATTR)),
			Arrays.asList(
				parameter(DBType.LONG, "branch"),
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
	public static Module getTLModuleOrFail(PooledConnection con, String moduleName)
			throws SQLException, MigrationException {
		return getTLModuleOrFail(con, TLContext.TRUNK_ID, moduleName);
	}

	/**
	 * Fetches an existing {@link TLModule} from the database.
	 * 
	 * @throws MigrationException
	 *         When no such module exists.
	 */
	public static Module getTLModuleOrFail(PooledConnection con, long branch, String moduleName)
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
	public static Module getTLModule(PooledConnection connection, long branch, String moduleName)
			throws SQLException, MigrationException {
		DBHelper sqlDialect = connection.getSQLDialect();

		String identifierAlias = "id";
		String modelAlias = "model";
		CompiledStatement selectModule = query(
		parameters(
			parameterDef(DBType.LONG, "branch"),
			parameterDef(DBType.STRING, "moduleName")),
		selectDistinct(
			columns(
				columnDef(BasicTypes.IDENTIFIER_DB_NAME, NO_TABLE_ALIAS, identifierAlias),
				columnDef(
					refID(TLModule.MODEL_ATTR),
					NO_TABLE_ALIAS, modelAlias)),
			table(SQLH.mangleDBName(TlModelFactory.KO_NAME_TL_MODULE)),
			and(
				eqSQL(
					column(BasicTypes.BRANCH_DB_NAME),
					parameter(DBType.LONG, "branch")),
				eqSQL(
					column(SQLH.mangleDBName(PersistentModule.NAME_ATTR)),
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
	public static Type getTLTypeOrFail(PooledConnection con, QualifiedTypeName typeName)
			throws SQLException, MigrationException {
		return getTLTypeOrFail(con, TLContext.TRUNK_ID, typeName.getModuleName(), typeName.getTypeName());
	}

	/**
	 * Fetches an existing {@link TLType} from the database.
	 * 
	 * @throws MigrationException
	 *         When no such type exists.
	 */
	public static Type getTLTypeOrFail(PooledConnection connection, long branch, String moduleName,
			String typeName)
			throws SQLException, MigrationException {
		Module module = getTLModuleOrFail(connection, branch, moduleName);
		return getTLTypeOrFail(connection, module, typeName);
	}

	/**
	 * Fetches an existing {@link TLType} from the database.
	 * 
	 * @throws MigrationException
	 *         When no such type exists.
	 */
	public static Type getTLTypeOrFail(PooledConnection connection, Module module, String typeName)
			throws SQLException, MigrationException {
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
		throw new MigrationException("No such type: " + TLModelUtil.qualifiedName(module.getModuleName(), typeName));
	}

	private static Type getTLDataType(PooledConnection connection, Module module, String dataTypeName)
			throws SQLException {
		return getTLType(connection, module, TlModelFactory.KO_NAME_TL_PRIMITIVE, dataTypeName);
	}

	private static Type getTLClass(PooledConnection connection, Module module, String className)
			throws SQLException {
		return getTLType(connection, module, KBBasedMetaElement.META_ELEMENT_KO, className);
	}

	private static Type getTLEnumeration(PooledConnection connection, Module module, String enumName)
			throws SQLException {
		return getTLType(connection, module, TlModelFactory.KO_NAME_TL_ENUMERATION, enumName);
	}

	private static Type getTLType(PooledConnection connection, Module module, String tlTypeTable, String typeName)
			throws SQLException {
		DBHelper sqlDialect = connection.getSQLDialect();

		String identifierAlias = "id";
		CompiledStatement selectClass = query(
		parameters(
			parameterDef(DBType.LONG, "branch"),
			parameterDef(DBType.ID, "moduleId"),
			parameterDef(DBType.STRING, "className")),
		selectDistinct(
			columns(
				columnDef(BasicTypes.IDENTIFIER_DB_NAME, NO_TABLE_ALIAS, identifierAlias)),
			table(SQLH.mangleDBName(tlTypeTable)),
			and(
				eqSQL(
					column(BasicTypes.BRANCH_DB_NAME),
					parameter(DBType.LONG, "branch")),
				eqSQL(
					column(refID(PersistentType.MODULE_REF)),
					parameter(DBType.ID, "moduleId")),
				eqSQL(
					column(SQLH.mangleDBName(PersistentType.NAME_ATTRIBUTE)),
					parameter(DBType.STRING, "className"))))).toSql(sqlDialect);

		try (ResultSet dbResult = selectClass.executeQuery(connection, module.getBranch(), module.getID(), typeName)) {
			if (dbResult.next()) {
				Type type = BranchIdType.newInstance(Type.class, module.getBranch(), tlId(dbResult, identifierAlias),
					tlTypeTable);
				type.setModule(module);
				type.setTypeName(typeName);
				return type;
			}
		}
		return null;
	}

	private static TLID tlId(ResultSet dbResult, String col) throws SQLException {
		return LongID.valueOf(dbResult.getLong(col));
	}

	/**
	 * Fetches all {@link TLType} belonging to the given {@link Module}.
	 */
	public static List<Type> getTLTypeIdentifiers(PooledConnection connection, Module module)
			throws SQLException {
		List<Type> searchResult = new ArrayList<>();
		addTLStructuredTypeIdentifiers(connection, module, searchResult);
		addTLEnumerationIdentifiers(connection, module, searchResult);
		addTLDataTypeIdentifiers(connection, module, searchResult);
		return searchResult;
	}

	/**
	 * Adds all {@link TLStructuredType}s belonging to the given {@link Module}.
	 */
	protected static void addTLStructuredTypeIdentifiers(PooledConnection connection, Module module,
			List<? super Type> searchResult) throws SQLException {
		addTLTypeIdentifiers(connection, module, searchResult, ApplicationObjectUtil.META_ELEMENT_OBJECT_TYPE);
	}

	/**
	 * Adds all {@link TLPrimitive}s belonging to the given {@link Module}.
	 */
	protected static void addTLDataTypeIdentifiers(PooledConnection connection, Module module,
			List<? super Type> searchResult) throws SQLException {
		addTLTypeIdentifiers(connection, module, searchResult, TlModelFactory.KO_NAME_TL_PRIMITIVE);
	}

	/**
	 * Adds all {@link TLEnumeration}s belonging to the given {@link Module}.
	 */
	protected static void addTLEnumerationIdentifiers(PooledConnection connection, Module module,
			List<? super Type> searchResult) throws SQLException {
		addTLTypeIdentifiers(connection, module, searchResult, TlModelFactory.KO_NAME_TL_ENUMERATION);
	}

	/**
	 * Adds all {@link TLType}s belonging to the given {@link Module}, stored in given
	 * {@link MetaObject}.
	 */
	protected static void addTLTypeIdentifiers(PooledConnection connection, Module module,
			List<? super Type> searchResult, String mo) throws SQLException {
		DBHelper sqlDialect = connection.getSQLDialect();

		String identifierAlias = "id";
		String branchAlias = "branch";
		String nameAlias = "name";
		CompiledStatement selectTLCLass = query(
		parameters(
			parameterDef(DBType.LONG, "branch"),
			parameterDef(DBType.ID, "module")),
		selectDistinct(
			columns(
				columnDef(BasicTypes.BRANCH_DB_NAME, NO_TABLE_ALIAS, branchAlias),
				columnDef(BasicTypes.IDENTIFIER_DB_NAME, NO_TABLE_ALIAS, identifierAlias),
				columnDef(SQLH.mangleDBName(TLNamed.NAME), NO_TABLE_ALIAS, nameAlias)),
			table(SQLH.mangleDBName(mo)),
			and(
				eqSQL(
					column(refID(PersistentType.MODULE_REF)),
					parameter(DBType.ID, "module")),
				eqSQL(
					column(BasicTypes.BRANCH_DB_NAME),
					parameter(DBType.LONG, "branch"))))).toSql(sqlDialect);

		try (ResultSet dbResult =
			selectTLCLass.executeQuery(connection, module.getBranch(), module.getID())) {
			while (dbResult.next()) {
				Type type = BranchIdType.newInstance(Type.class,
					dbResult.getLong(branchAlias),
					LongID.valueOf(dbResult.getLong(identifierAlias)),
					mo);
				type.setTypeName(dbResult.getString(nameAlias));
				type.setModule(module);
				searchResult.add(type);
			}
		}
	}

	/**
	 * Creates a new {@link TLReference}.
	 * 
	 * @param reference
	 *        Qualified name of the reference to create.
	 * @param target
	 *        Qualified name of {@link TLReference#getType()}.
	 */
	public static Reference createTLReference(PooledConnection con,
			QualifiedPartName reference, QualifiedTypeName target,
			boolean mandatory, boolean composite, boolean aggregate, boolean multiple,
			boolean bag, boolean ordered, boolean navigate,
			AnnotatedConfig<TLAttributeAnnotation> annotations)
			throws XMLStreamException, SQLException, MigrationException {
		return createTLReference(con, TLContext.TRUNK_ID,
			reference.getModuleName(), reference.getTypeName(), reference.getPartName(),
			target.getModuleName(), target.getTypeName(),
			mandatory, composite, aggregate, multiple, bag, ordered, navigate,
			toString(annotations));
	}

	/**
	 * Creates a new {@link TLReference}.
	 * 
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
	public static Reference createTLReference(PooledConnection con, long branch,
			String moduleName, String ownerName, String partName,
			String targetModule, String targetTypeName,
			boolean mandatory, boolean composite, boolean aggregate, boolean multiple,
			boolean bag, boolean ordered, boolean navigate,
			String annotations)
			throws SQLException, MigrationException {

		Type associationType = createTLStructuredType(con, branch, moduleName,
			ModelResolver.syntheticAssociationName(ownerName, partName), null, null, null, true);

		createTLAssociationEnd(con, branch, associationType.getModule().getModuleName(), associationType.getTypeName(),
			ModelResolver.SELF_ASSOCIATION_END_NAME, moduleName, ownerName, false, false, false, true, false, false,
			false, null);

		TypePart targetEnd = createTLAssociationEnd(con, branch, associationType.getModule().getModuleName(),
			associationType.getTypeName(),
			partName, targetModule, targetTypeName, mandatory, composite, aggregate, multiple, bag, ordered, navigate,
			null);

		Reference reference =
			internalCreateTLReference(con, branch, moduleName, ownerName, partName, targetEnd.getID(), annotations);

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
	public static Reference createInverseTLReference(PooledConnection con, QualifiedPartName reference,
			QualifiedPartName inverseReference,
			boolean mandatory, boolean composite, boolean aggregate, boolean multiple,
			boolean bag, boolean ordered, boolean navigate,
			AnnotatedConfig<TLAttributeAnnotation> annotations)
			throws SQLException, MigrationException, XMLStreamException {
		return createInverseTLReference(con, TLContext.TRUNK_ID,
			reference.getModuleName(), reference.getTypeName(), reference.getPartName(),
			inverseReference.getModuleName(), inverseReference.getTypeName(), inverseReference.getPartName(),
			mandatory, composite, aggregate, multiple, bag, ordered, navigate,
			toString(annotations));

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
	public static Reference createInverseTLReference(PooledConnection con, long branch,
			String module, String type, String refName,
			String inverseRefModule, String inverseRefType, String inverseRefName,
			boolean mandatory, boolean composite, boolean aggregate, boolean multiple,
			boolean bag, boolean ordered, boolean navigate,
			String annotations) throws SQLException, MigrationException {

		String associationType = ModelResolver.syntheticAssociationName(inverseRefType, inverseRefName);
		Type tlType = getTLTypeOrFail(con, branch, inverseRefModule, associationType);
		if (tlType == null) {
			throw new MigrationException(
				"Association for inverse reference " + inverseRefName + " in "
					+ TLModelUtil.qualifiedName(inverseRefModule, inverseRefType) + " not found.");
		}
		TypePart selfEnd = getTLTypePart(con, tlType, ModelResolver.SELF_ASSOCIATION_END_NAME);
		Reference reference =
			internalCreateTLReference(con, branch, module, type, refName, selfEnd.getID(), annotations);

		CompiledStatement sql = query(
		parameters(
			parameterDef(DBType.LONG, "branch"),
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
				eqSQL(
					column(BasicTypes.BRANCH_DB_NAME),
					parameter(DBType.LONG, "branch")),
				eqSQL(
					column(refID(PersistentReference.END_ATTR)),
					parameter(DBType.ID, "endID"))),
			Arrays.asList(
				SQLH.mangleDBName(PersistentEnd.MANDATORY),
				SQLH.mangleDBName(PersistentEnd.COMPOSITE_ATTR),
				SQLH.mangleDBName(PersistentEnd.AGGREGATE_ATTR),
				SQLH.mangleDBName(PersistentEnd.MULTIPLE_ATTR),
				SQLH.mangleDBName(PersistentEnd.BAG_ATTR),
				SQLH.mangleDBName(PersistentEnd.ORDERED_ATTR),
				SQLH.mangleDBName(PersistentEnd.NAVIGATE_ATTR)),
			Arrays.asList(
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
	public static Reference createTLEndReference(PooledConnection con, QualifiedPartName reference,
			QualifiedPartName assEnd, AnnotatedConfig<TLAttributeAnnotation> annotations)
			throws XMLStreamException, SQLException, MigrationException {
		return createTLEndReference(con, TLContext.TRUNK_ID,
			reference.getModuleName(), reference.getTypeName(), reference.getPartName(),
			assEnd.getModuleName(), assEnd.getTypeName(), assEnd.getPartName(),
			toString(annotations));
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
	public static Reference createTLEndReference(PooledConnection con, long branch,
			String module, String type, String refName,
			String endModule, String endType, String endName,
			String annotations)
			throws SQLException, MigrationException {
		TypePart targetEnd = getTLTypePart(con, getTLTypeOrFail(con, branch, endModule, endType), endName);

		return internalCreateTLReference(con, branch, module, type, refName, targetEnd.getID(), annotations);

	}

	/**
	 * Removes a generalisation for the given specialisation.
	 */
	public static void removeGeneralisation(PooledConnection con, QualifiedTypeName specialisation,
			QualifiedTypeName generalisation)
			throws SQLException, MigrationException {
		removeGeneralisation(con, TLContext.TRUNK_ID,
			specialisation.getModuleName(), specialisation.getTypeName(),
			generalisation.getModuleName(), generalisation.getTypeName());
	}

	/**
	 * Removes a generalisation for the given specialisation.
	 */
	public static void removeGeneralisation(PooledConnection con, long branch, String specialisationModule,
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

		CompiledStatement delete = query(
		parameters(
			parameterDef(DBType.LONG, "branch"),
			parameterDef(DBType.ID, "specialization"),
			parameterDef(DBType.ID, "generalization")),
		delete(
			table(SQLH.mangleDBName(ApplicationObjectUtil.META_ELEMENT_GENERALIZATIONS)),
			and(
				eqSQL(
					column(BasicTypes.BRANCH_DB_NAME),
					parameter(DBType.LONG, "branch")),
				eqSQL(
					column(refID(SourceReference.REFERENCE_SOURCE_NAME)),
					parameter(DBType.ID, "specialization")),
				eqSQL(
					column(refID(DestinationReference.REFERENCE_DEST_NAME)),
					parameter(DBType.ID, "generalization"))))).toSql(con.getSQLDialect());

		delete.executeUpdate(con, branch, specialization.getID(), generalization.getID());

	}

	static String refID(String reference) {
		return ReferencePart.name.getReferenceAspectColumnName(SQLH.mangleDBName(reference));
	}

	static String refType(String reference) {
		return ReferencePart.type.getReferenceAspectColumnName(SQLH.mangleDBName(reference));
	}

	/**
	 * Adds a generalisation for the given specialisation.
	 */
	public static void addGeneralisation(PooledConnection con, QualifiedTypeName specialisation,
			QualifiedTypeName generalisation)
			throws SQLException, MigrationException {
		addGeneralisation(con, TLContext.TRUNK_ID,
			specialisation.getModuleName(), specialisation.getTypeName(),
			generalisation.getModuleName(), generalisation.getTypeName());
	}

	/**
	 * Adds a generalisation for the given specialisation.
	 */
	public static void addGeneralisation(PooledConnection con, long branch, String specialisationModule,
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
	public static BranchIdType addGeneralization(PooledConnection con, long branch, TLID specializationID,
			TLID generalizationID, int newOrderValue) throws SQLException {
		TLID newIdentifier = newID(con);
		Long revCreate = getRevCreate(con);

		CompiledStatement createGeneralisation = query(
		parameters(
			parameterDef(DBType.LONG, "branch"),
			parameterDef(DBType.ID, "identifier"),
			parameterDef(DBType.LONG, "revCreate"),
			parameterDef(DBType.ID, "specialisation"),
			parameterDef(DBType.ID, "generalisation"),
			parameterDef(DBType.INT, "order")),
		insert(
			table(SQLH.mangleDBName(ApplicationObjectUtil.META_ELEMENT_GENERALIZATIONS)),
			Arrays.asList(
				BasicTypes.BRANCH_DB_NAME,
				BasicTypes.IDENTIFIER_DB_NAME,
				BasicTypes.REV_MAX_DB_NAME,
				BasicTypes.REV_MIN_DB_NAME,
				BasicTypes.REV_CREATE_DB_NAME,
				refID(SourceReference.REFERENCE_SOURCE_NAME),
				refID(DestinationReference.REFERENCE_DEST_NAME),
				SQLH.mangleDBName(KBBasedMetaElement.META_ELEMENT_GENERALIZATIONS__ORDER)),
			Arrays.asList(
				parameter(DBType.LONG, "branch"),
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

	private static int newAttributeOrder(PooledConnection con, long branch, TLID ownerId) throws SQLException {
		String orderAttribute = KBBasedMetaAttribute.OWNER_REF_ORDER_ATTR;
		String table = ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE;
		String ownerRef = KBBasedMetaAttribute.OWNER_REF;
		return newOrderValue(con, branch, ownerId, orderAttribute, table, ownerRef);
	}

	private static int newGeneralizationOrder(PooledConnection con, long branch, TLID ownerId) throws SQLException {
		String orderAttribute = KBBasedMetaElement.META_ELEMENT_GENERALIZATIONS__ORDER;
		String table = ApplicationObjectUtil.META_ELEMENT_GENERALIZATIONS;
		String ownerRef = SourceReference.REFERENCE_SOURCE_NAME;
		return newOrderValue(con, branch, ownerId, orderAttribute, table, ownerRef);
	}

	private static int newOrderValue(PooledConnection con, long branch, TLID ownerId, String orderAttribute,
			String table, String ownerRef) throws SQLException {
		List<Integer> orders = getOrders(con, branch, ownerId, orderAttribute, table, ownerRef);
		int ownerOrder;
		if (orders.isEmpty()) {
			ownerOrder = OrderedLinkUtil.MAX_ORDER / 2;
		} else {
			Integer maxOrder = orders.get(orders.size() - 1);
			if (OrderedLinkUtil.MAX_ORDER == maxOrder.intValue()) {
				ownerOrder = orders.get(0) - 1;
			} else {
				ownerOrder = maxOrder.intValue() + 1;
			}
		}
		return ownerOrder;
	}

	/**
	 * Fetches an existing {@link TLTypePart} from the database.
	 * 
	 * @throws MigrationException
	 *         If no such part exists.
	 */
	public static TypePart getTLTypePartOrFail(PooledConnection connection, QualifiedPartName part)
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
	public static TypePart getTLTypePartOrFail(PooledConnection connection, long branch, String module, String type,
			String partName) throws SQLException, MigrationException {
		Type ownerType = getTLTypeOrFail(connection, branch, module, type);
		TypePart part = getTLTypePart(connection, ownerType, partName);
		if (part == null) {
			throw new MigrationException("No part " + partName + " found in " + toString(ownerType) + ".");
		}
		return part;
	}

	/**
	 * Fetches an existing {@link TLTypePart} from the database.
	 * 
	 * @throws MigrationException
	 *         If no such part exists.
	 */
	public static TypePart getTLTypePart(PooledConnection connection, Type owner, String partName)
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
	public static List<TypePart> getTLTypeParts(PooledConnection connection, Type owner)
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

	private static List<TypePart> getTLStructuredTypeParts(PooledConnection connection, Type type) throws SQLException {
		DBHelper sqlDialect = connection.getSQLDialect();

		String identifierAlias = "id";
		String nameAlias = "name";
		String implAlias = "impl";
		String endIDAlias = "endId";
		String definitionAlias = "definition";
		CompiledStatement selectParts = query(
		parameters(
			parameterDef(DBType.LONG, "branch"),
			parameterDef(DBType.ID, "owner")),
		selectDistinct(
			columns(
				columnDef(BasicTypes.IDENTIFIER_DB_NAME, NO_TABLE_ALIAS, identifierAlias),
				columnDef(SQLH.mangleDBName(TLNamed.NAME), NO_TABLE_ALIAS, nameAlias),
				columnDef(SQLH.mangleDBName(KBBasedMetaAttribute.IMPLEMENTATION_NAME), NO_TABLE_ALIAS,
					implAlias),
				columnDef(refID(PersistentReference.END_ATTR), NO_TABLE_ALIAS, endIDAlias),
				columnDef(refID(PersistentStructuredTypePart.DEFINITION_REF), NO_TABLE_ALIAS, definitionAlias)),
			table(SQLH.mangleDBName(ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE)),
			and(
				eqSQL(
					column(refID(ApplicationObjectUtil.META_ELEMENT_ATTR)),
					parameter(DBType.ID, "owner")),
				eqSQL(
					column(BasicTypes.BRANCH_DB_NAME),
					parameter(DBType.LONG, "branch"))))).toSql(sqlDialect);

		List<TypePart> searchResult = new ArrayList<>();
		try (ResultSet dbResult =
			selectParts.executeQuery(connection, type.getBranch(), type.getID())) {
			while (dbResult.next()) {
				String impl = dbResult.getString(implAlias);
				TypePart part;
				if (KBBasedMetaAttributeFactory.REFERENCE_IMPL.equals(impl)) {
					part = BranchIdType.newInstance(Reference.class,
						type.getBranch(),
						LongID.valueOf(dbResult.getLong(identifierAlias)),
						ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE);
					part.setOwner(type);
					part.setPartName(dbResult.getString(nameAlias));
					part.setDefinition(LongID.valueOf(dbResult.getLong(definitionAlias)));
					((Reference) part).setEndID(LongID.valueOf(dbResult.getLong(endIDAlias)));
				} else {
					part = BranchIdType.newInstance(TypePart.class,
						type.getBranch(),
						LongID.valueOf(dbResult.getLong(identifierAlias)),
						ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE);
					part.setOwner(type);
					part.setPartName(dbResult.getString(nameAlias));
					part.setDefinition(LongID.valueOf(dbResult.getLong(definitionAlias)));
				}
				searchResult.add(part);
			}
		}
		return searchResult;
	}

	private static List<TypePart> getTLClassifiers(PooledConnection connection, Type enumType) throws SQLException {
		DBHelper sqlDialect = connection.getSQLDialect();

		String identifierAlias = "id";
		String nameAlias = "name";
		CompiledStatement selectParts = query(
		parameters(
			parameterDef(DBType.LONG, "branch"),
			parameterDef(DBType.ID, "owner")),
		selectDistinct(
			columns(
				columnDef(BasicTypes.IDENTIFIER_DB_NAME, NO_TABLE_ALIAS, identifierAlias),
				columnDef(SQLH.mangleDBName(TLNamed.NAME), NO_TABLE_ALIAS, nameAlias)),
			table(SQLH.mangleDBName(TlModelFactory.KO_NAME_TL_CLASSIFIER)),
			and(
				eqSQL(
					column(refID(FastListElement.OWNER_ATTRIBUTE)),
					parameter(DBType.ID, "owner")),
				eqSQL(
					column(BasicTypes.BRANCH_DB_NAME),
					parameter(DBType.LONG, "branch"))))).toSql(sqlDialect);

		List<TypePart> searchResult = new ArrayList<>();
		try (ResultSet dbResult =
			selectParts.executeQuery(connection, enumType.getBranch(), enumType.getID())) {
			while (dbResult.next()) {
				TypePart part = BranchIdType.newInstance(TypePart.class,
					enumType.getBranch(),
					LongID.valueOf(dbResult.getLong(identifierAlias)),
					TlModelFactory.KO_NAME_TL_CLASSIFIER);
				part.setOwner(enumType);
				part.setPartName(dbResult.getString(nameAlias));
				part.setDefinition(IdentifierUtil.nullIdForMandatoryDatabaseColumns());
				searchResult.add(part);
			}
		}
		return searchResult;
	}

	private static TypePart getTLStructuredTypePart(PooledConnection connection, Type structuredType, String partName)
			throws SQLException, MigrationException {
		DBHelper sqlDialect = connection.getSQLDialect();

		String identifierAlias = "id";
		String nameAlias = "name";
		String implAlias = "impl";
		String endIDAlias = "endId";
		String definitionAlias = "definition";
		CompiledStatement selectPart = query(
		parameters(
			parameterDef(DBType.LONG, "branch"),
			parameterDef(DBType.ID, "owner"),
			parameterDef(DBType.STRING, "part")),
		selectDistinct(
			columns(
				columnDef(BasicTypes.IDENTIFIER_DB_NAME, NO_TABLE_ALIAS, identifierAlias),
				columnDef(SQLH.mangleDBName(TLNamed.NAME), NO_TABLE_ALIAS, nameAlias),
				columnDef(SQLH.mangleDBName(KBBasedMetaAttribute.IMPLEMENTATION_NAME), NO_TABLE_ALIAS,
					implAlias),
				columnDef(refID(PersistentReference.END_ATTR), NO_TABLE_ALIAS, endIDAlias),
				columnDef(refID(PersistentStructuredTypePart.DEFINITION_REF), NO_TABLE_ALIAS, definitionAlias)),
			table(SQLH.mangleDBName(ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE)),
			and(
				eqSQL(
					column(BasicTypes.BRANCH_DB_NAME),
					parameter(DBType.LONG, "branch")),
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
				String impl = dbResult.getString(implAlias);
				TypePart part;
				if (KBBasedMetaAttributeFactory.REFERENCE_IMPL.equals(impl)) {
					part = BranchIdType.newInstance(Reference.class,
						structuredType.getBranch(),
						LongID.valueOf(dbResult.getLong(identifierAlias)),
						ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE);
					part.setOwner(structuredType);
					part.setPartName(dbResult.getString(nameAlias));
					part.setDefinition(LongID.valueOf(dbResult.getLong(definitionAlias)));
					((Reference) part).setEndID(LongID.valueOf(dbResult.getLong(endIDAlias)));
				} else {
					part = BranchIdType.newInstance(TypePart.class,
						structuredType.getBranch(),
						LongID.valueOf(dbResult.getLong(identifierAlias)),
						ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE);
					part.setOwner(structuredType);
					part.setPartName(dbResult.getString(nameAlias));
					part.setDefinition(LongID.valueOf(dbResult.getLong(definitionAlias)));
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

	private static TypePart getTLClassifier(PooledConnection connection, Type structuredType, String partName)
			throws SQLException, MigrationException {
		DBHelper sqlDialect = connection.getSQLDialect();

		String identifierAlias = "id";
		String nameAlias = "name";
		CompiledStatement selectPart = query(
		parameters(
			parameterDef(DBType.LONG, "branch"),
			parameterDef(DBType.ID, "owner"),
			parameterDef(DBType.STRING, "part")),
		selectDistinct(
			columns(
				columnDef(BasicTypes.IDENTIFIER_DB_NAME, NO_TABLE_ALIAS, identifierAlias),
				columnDef(SQLH.mangleDBName(TLNamed.NAME), NO_TABLE_ALIAS, nameAlias)),
			table(SQLH.mangleDBName(TlModelFactory.KO_NAME_TL_CLASSIFIER)),
			and(
				eqSQL(
					column(BasicTypes.BRANCH_DB_NAME),
					parameter(DBType.LONG, "branch")),
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
				TypePart part = BranchIdType.newInstance(TypePart.class,
					structuredType.getBranch(),
					LongID.valueOf(dbResult.getLong(identifierAlias)),
					TlModelFactory.KO_NAME_TL_CLASSIFIER);
				part.setOwner(structuredType);
				part.setPartName(dbResult.getString(nameAlias));
				part.setDefinition(IdentifierUtil.nullIdForMandatoryDatabaseColumns());
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
	 * Retrieves the generalization links for the given {@link TLClass}.
	 * 
	 * <p>
	 * Note: The result does not contain the actual generalizations, but only the links.
	 * </p>
	 */
	public static List<BranchIdType> getTLClassGeneralizationLinks(PooledConnection connection, Type specialization)
			throws SQLException {
		return getTLClassGeneralizationsOrSpecializations(connection, specialization,
			SourceReference.REFERENCE_SOURCE_NAME);
	}

	/**
	 * Retrieves the specialization links for the given {@link TLClass}.
	 * <p>
	 * Note: The result does not contain the actual specializations, but only the links.
	 * </p>
	 */
	public static List<BranchIdType> getTLClassSpecializationLinks(PooledConnection connection, Type generalization)
			throws SQLException {
		return getTLClassGeneralizationsOrSpecializations(connection, generalization,
			DestinationReference.REFERENCE_DEST_NAME);
	}

	private static List<BranchIdType> getTLClassGeneralizationsOrSpecializations(PooledConnection connection,
			Type source, String reference) throws SQLException {
		DBHelper sqlDialect = connection.getSQLDialect();

		String identifierAlias = "id";
		CompiledStatement selectTLStructuredTypePart = query(
		parameters(
			parameterDef(DBType.LONG, "branch"),
			parameterDef(DBType.ID, "tlClass")),
		selectDistinct(
			columns(
				columnDef(BasicTypes.IDENTIFIER_DB_NAME, NO_TABLE_ALIAS, identifierAlias)),
			table(SQLH.mangleDBName(ApplicationObjectUtil.META_ELEMENT_GENERALIZATIONS)),
			and(
				eqSQL(
					column(refID(reference)),
					parameter(DBType.ID, "tlClass")),
				eqSQL(
					column(BasicTypes.BRANCH_DB_NAME),
					parameter(DBType.LONG, "branch"))))).toSql(sqlDialect);

		List<BranchIdType> searchResult = new ArrayList<>();
		try (ResultSet dbResult =
			selectTLStructuredTypePart.executeQuery(connection, source.getBranch(), source.getID())) {
			while (dbResult.next()) {
				searchResult.add(BranchIdType.newInstance(BranchIdType.class,
					source.getBranch(),
					LongID.valueOf(dbResult.getLong(identifierAlias)),
					ApplicationObjectUtil.META_ELEMENT_GENERALIZATIONS));
			}
		}
		return searchResult;
	}

	/**
	 * Retrieves the generalization links for the given {@link TLType}.
	 * <p>
	 * Note: The result does not contain the actual generalizations, but only the links.
	 * </p>
	 */
	public static List<BranchIdType> getGeneralizations(PooledConnection connection, Type type) throws SQLException {
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
	 * Retrieves the specialization links for the given {@link TLType}.
	 * <p>
	 * Note: The result does not contain the actual specializations, but only the links.
	 * </p>
	 */
	public static List<BranchIdType> getSpecializations(PooledConnection connection, Type type) throws SQLException {
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
	public static Type createTLDatatype(PooledConnection connection, QualifiedTypeName type, Kind kind,
			DBColumnType dbType, PolymorphicConfiguration<StorageMapping<?>> storageMapping,
			AnnotatedConfig<TLTypeAnnotation> annotations) throws SQLException, MigrationException, XMLStreamException {
		return createTLDatatype(connection, TLContext.TRUNK_ID, type.getModuleName(), type.getTypeName(), kind,
			dbType, storageMapping, toString(annotations));
	}

	/**
	 * Creates a new {@link TLPrimitive}.
	 */
	public static Type createTLDatatype(PooledConnection con, long branch, String moduleName, String typeName,
			Kind kind, DBColumnType dbType, PolymorphicConfiguration<StorageMapping<?>> storageMapping,
			String annotations) throws SQLException, MigrationException, XMLStreamException {
		Module module = getTLModuleOrFail(con, branch, moduleName);
		TLID newIdentifier = newID(con);
		Long revCreate = getRevCreate(con);

		CompiledStatement createType = query(
		parameters(
			parameterDef(DBType.LONG, "branch"),
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
			Arrays.asList(
				BasicTypes.BRANCH_DB_NAME,
				BasicTypes.IDENTIFIER_DB_NAME,
				BasicTypes.REV_MAX_DB_NAME,
				BasicTypes.REV_MIN_DB_NAME,
				BasicTypes.REV_CREATE_DB_NAME,
				SQLH.mangleDBName(PersistentModelPart.ANNOTATIONS_MO_ATTRIBUTE),
				SQLH.mangleDBName(TLClass.NAME_ATTR),
				refID(PersistentType.MODULE_REF),
				refType(PersistentType.SCOPE_REF),
				refID(PersistentType.SCOPE_REF),
				SQLH.mangleDBName(PersistentDatatype.KIND_ATTR),
				SQLH.mangleDBName(PersistentDatatype.DB_TYPE_ATTR),
				SQLH.mangleDBName(PersistentDatatype.DB_SIZE_ATTR),
				SQLH.mangleDBName(PersistentDatatype.DB_PRECISION_ATTR),
				SQLH.mangleDBName(PersistentDatatype.BINARY_ATTR),
				SQLH.mangleDBName(PersistentDatatype.STORAGE_MAPPING)),
			Arrays.asList(
				parameter(DBType.LONG, "branch"),
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
		return type;

	}

	private static String toString(PolymorphicConfiguration<StorageMapping<?>> storageMapping)
			throws XMLStreamException {
		if (storageMapping == null) {
			return null;
		}
		StringWriter storageMappingBuffer = new StringWriter();
		new ConfigurationWriter(storageMappingBuffer).write("config", PolymorphicConfiguration.class, storageMapping);
		return storageMappingBuffer.toString();
	}

	/**
	 * Sets the {@link TLModule#getAnnotations()} of a {@link TLModule}.
	 */
	public static void updateModuleAnnotations(PooledConnection con, String moduleName,
			AnnotatedConfig<? extends TLAnnotation> annotations)
			throws SQLException, XMLStreamException {
		updateModuleAnnotations(con, TLContext.TRUNK_ID, moduleName, toString(annotations));
	}

	/**
	 * Sets the {@link TLModule#getAnnotations()} of a {@link TLModule}.
	 */
	public static void updateModuleAnnotations(PooledConnection con, long branch, String moduleName,
			String annotations) throws SQLException {
		CompiledStatement sql = query(
		parameters(
			parameterDef(DBType.LONG, "branch"),
			parameterDef(DBType.STRING, "name"),
			parameterDef(DBType.STRING, "annotations")),
		update(
			table(SQLH.mangleDBName(TlModelFactory.KO_NAME_TL_MODULE)),
			and(
				eqSQL(
					column(BasicTypes.BRANCH_DB_NAME),
					parameter(DBType.LONG, "branch")),
				eqSQL(
					column(SQLH.mangleDBName(PersistentModule.NAME_ATTR)),
					parameter(DBType.STRING, "name"))),
			Arrays.asList(
				SQLH.mangleDBName(PersistentModelPart.ANNOTATIONS_MO_ATTRIBUTE)),
			Arrays.asList(
				parameter(DBType.STRING, "annotations")))).toSql(con.getSQLDialect());

		sql.executeUpdate(con, branch, moduleName, annotations);

	}

	/**
	 * Sets the {@link TLModule#getAnnotations()} of a {@link TLType}.
	 */
	public static void updateTypeAnnotations(PooledConnection con, Module module, String typeName,
			AnnotatedConfig<? extends TLAnnotation> annotations)
			throws SQLException, XMLStreamException {
		updateTypeAnnotations(con, module, typeName, toString(annotations));
	}

	/**
	 * Sets the {@link TLModule#getAnnotations()} of a {@link TLType}.
	 */
	public static void updateTypeAnnotations(PooledConnection con, Module module, String typeName,
			String annotations) throws SQLException {
		updateTypeAnnotations(con, module, KBBasedMetaElement.META_ELEMENT_KO, typeName, annotations);
		updateTypeAnnotations(con, module, TlModelFactory.KO_NAME_TL_ENUMERATION, typeName, annotations);
		updateTypeAnnotations(con, module, TlModelFactory.KO_NAME_TL_PRIMITIVE, typeName, annotations);
	}

	private static void updateTypeAnnotations(PooledConnection con, Module module, String typeTable, String typeName,
			String annotations) throws SQLException {
		CompiledStatement sql = query(
		parameters(
			parameterDef(DBType.LONG, "branch"),
			parameterDef(DBType.ID, "module"),
			parameterDef(DBType.STRING, "name"),
			parameterDef(DBType.STRING, "annotations")),
		update(
			table(SQLH.mangleDBName(typeTable)),
			and(
				eqSQL(
					column(BasicTypes.BRANCH_DB_NAME),
					parameter(DBType.LONG, "branch")),
				eqSQL(
					column(refID(PersistentType.MODULE_REF)),
					parameter(DBType.ID, "module")),
				eqSQL(
					column(SQLH.mangleDBName(PersistentType.NAME_ATTR)),
					parameter(DBType.STRING, "name"))),
			Arrays.asList(
				SQLH.mangleDBName(PersistentModelPart.ANNOTATIONS_MO_ATTRIBUTE)),
			Arrays.asList(
				parameter(DBType.STRING, "annotations")))).toSql(con.getSQLDialect());

		sql.executeUpdate(con, module.getBranch(), module.getID(), typeName, annotations);
	}

	/**
	 * Sets the {@link TLModule#getAnnotations()} of a {@link TLTypePart}.
	 */
	public static void updateTypePartAnnotations(PooledConnection con, Type owner, String partName,
			AnnotatedConfig<? extends TLAnnotation> annotations)
			throws SQLException, XMLStreamException {
		updateTypePartAnnotations(con, owner, partName, toString(annotations));
	}

	/**
	 * Sets the {@link TLModule#getAnnotations()} of a {@link TLTypePart}.
	 */
	public static void updateTypePartAnnotations(PooledConnection con, Type owner, String partName,
			String annotations) throws SQLException {
		updateTypeAnnotations(con, owner, KBBasedMetaAttribute.OBJECT_NAME, partName, annotations);
		updateTypeAnnotations(con, owner, TlModelFactory.KO_NAME_TL_CLASSIFIER, partName, annotations);
	}

	private static void updateTypeAnnotations(PooledConnection con, Type owner, String partTable, String partName,
			String annotations) throws SQLException {
		CompiledStatement sql = query(
		parameters(
			parameterDef(DBType.LONG, "branch"),
			parameterDef(DBType.ID, "owner"),
			parameterDef(DBType.STRING, "name"),
			parameterDef(DBType.STRING, "annotations")),
		update(
			table(SQLH.mangleDBName(partTable)),
			and(
				eqSQL(
					column(BasicTypes.BRANCH_DB_NAME),
					parameter(DBType.LONG, "branch")),
				eqSQL(
					column(refID(PersistentTypePart.OWNER_ATTR)),
					parameter(DBType.ID, "owner")),
				eqSQL(
					column(SQLH.mangleDBName(PersistentType.NAME_ATTR)),
					parameter(DBType.STRING, "name"))),
			Arrays.asList(
				SQLH.mangleDBName(PersistentModelPart.ANNOTATIONS_MO_ATTRIBUTE)),
			Arrays.asList(
				parameter(DBType.STRING, "annotations")))).toSql(con.getSQLDialect());

		sql.executeUpdate(con, owner.getBranch(), owner.getID(), partName, annotations);
	}

	/**
	 * Creates a new {@link TLEnumeration}.
	 * 
	 * @param enumName
	 *        Name of the {@link TLClass} to create.
	 */
	public static Type createTLEnumeration(PooledConnection con, QualifiedTypeName enumName,
			AnnotatedConfig<TLTypeAnnotation> annotations)
			throws SQLException, MigrationException, XMLStreamException {
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
	public static Type createTLEnumeration(PooledConnection con, long branch, String moduleName, String enumName,
			String annotations) throws SQLException, MigrationException {
		DBHelper sqlDialect = con.getSQLDialect();

		Module module = getTLModuleOrFail(con, branch, moduleName);
		TLID newIdentifier = newID(con);
		Long revCreate = getRevCreate(con);

		CompiledStatement createEnum = query(
		parameters(
			parameterDef(DBType.LONG, "branch"),
			parameterDef(DBType.ID, "identifier"),
			parameterDef(DBType.LONG, "revCreate"),
			parameterDef(DBType.STRING, "annotations"),
			parameterDef(DBType.STRING, "name"),
			parameterDef(DBType.ID, "moduleID")),
		insert(
			table(SQLH.mangleDBName(TlModelFactory.KO_NAME_TL_ENUMERATION)),
			Arrays.asList(
				BasicTypes.BRANCH_DB_NAME,
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
			Arrays.asList(
				parameter(DBType.LONG, "branch"),
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
		return enumType;
	}

	/**
	 * Creates a new {@link TLClassifier}.
	 * 
	 * @param classifierName
	 *        Name of the {@link TLClassifier} to create.
	 * @param sortOrder
	 *        Sort order of the new classifier. If {@link #NO_SORT_ORDER}, the new TLCLasifier will
	 *        be the last one.
	 */
	public static TypePart createTLClassifier(PooledConnection con, QualifiedPartName classifierName, int sortOrder,
			AnnotatedConfig<TLClassifierAnnotation> annotations)
			throws SQLException, MigrationException, XMLStreamException {
		return createTLClassifier(con, TLContext.TRUNK_ID,
			classifierName.getModuleName(), classifierName.getTypeName(), classifierName.getPartName(), sortOrder,
			toString(annotations));
	}

	/**
	 * Creates a new {@link TLEnumeration}.
	 * 
	 * @param moduleName
	 *        Name of the module of the new classifier to create.
	 * @param enumName
	 *        Name of the enumeration of the new classifier.
	 * @param classifierName
	 *        Name of the new classifier.
	 */
	public static TypePart createTLClassifier(PooledConnection con, long branch, String moduleName, String enumName,
			String classifierName, int sortOrder,
			String annotations) throws SQLException, MigrationException {
		DBHelper sqlDialect = con.getSQLDialect();

		Module module = getTLModuleOrFail(con, branch, moduleName);
		Type enumeration = getTLEnumeration(con, module, enumName);
		if (enumeration == null) {
			throw new MigrationException(
				"No enumeration with name '" + enumName + "' found in module " + toString(module));
		}
		if (sortOrder == NO_SORT_ORDER) {
			List<Integer> orders = getOrders(con, branch, enumeration.getID(), FastListElement.ORDER_DB_NAME,
				TlModelFactory.KO_NAME_TL_CLASSIFIER, FastListElement.OWNER_ATTRIBUTE);
			if (orders.isEmpty()) {
				sortOrder = 0;
			} else {
				sortOrder = orders.get(orders.size() - 1) + 1;
			}
		}
		TLID newIdentifier = newID(con);
		Long revCreate = getRevCreate(con);

		CompiledStatement createClassifier = query(
		parameters(
			parameterDef(DBType.LONG, "branch"),
			parameterDef(DBType.ID, "identifier"),
			parameterDef(DBType.LONG, "revCreate"),
			parameterDef(DBType.STRING, "annotations"),
			parameterDef(DBType.ID, "enum"),
			parameterDef(DBType.STRING, "name"),
			parameterDef(DBType.INT, "sortOrder")),
		insert(
			table(SQLH.mangleDBName(TlModelFactory.KO_NAME_TL_CLASSIFIER)),
			Arrays.asList(
				BasicTypes.BRANCH_DB_NAME,
				BasicTypes.IDENTIFIER_DB_NAME,
				BasicTypes.REV_MAX_DB_NAME,
				BasicTypes.REV_MIN_DB_NAME,
				BasicTypes.REV_CREATE_DB_NAME,
				SQLH.mangleDBName(PersistentModelPart.ANNOTATIONS_MO_ATTRIBUTE),
				refID(FastListElement.OWNER_ATTRIBUTE),
				SQLH.mangleDBName(TLClassifier.NAME_ATTR),
				SQLH.mangleDBName(FastListElement.ORDER_DB_NAME)),
			Arrays.asList(
				parameter(DBType.LONG, "branch"),
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
		TypePart classifierType =
			BranchIdType.newInstance(TypePart.class, branch, newIdentifier, TlModelFactory.KO_NAME_TL_CLASSIFIER);
		classifierType.setOwner(enumeration);
		classifierType.setPartName(classifierName);
		classifierType.setDefinition(IdentifierUtil.nullIdForMandatoryDatabaseColumns());
		return classifierType;
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
	public static void setDefaultTLClassifier(PooledConnection con, QualifiedTypeName enumName,
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
	public static void setDefaultTLClassifier(PooledConnection con, long branch, String moduleName, String enumName,
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
			parameterDef(DBType.LONG, "branch"),
			parameterDef(DBType.ID, "identifier"),
			parameterDef(DBType.ID, "defaultClassifier")),
		update(
			table(SQLH.mangleDBName(TlModelFactory.KO_NAME_TL_ENUMERATION)),
			and(
				eqSQL(
					column(BasicTypes.BRANCH_DB_NAME),
					parameter(DBType.LONG, "branch")),
				eqSQL(
					column(BasicTypes.IDENTIFIER_DB_NAME),
					parameter(DBType.ID, "identifier"))),
			Arrays.asList(
				refID(FastList.DEFAULT_ATTRIBUTE)),
			Arrays.asList(
				parameter(DBType.ID, "defaultClassifier")))).toSql(sqlDialect);
		updateStmt.executeUpdate(con, branch, enumeration.getID(), defaultID);
	}

	/**
	 * Serializes a sequence of {@link BranchIdType}.
	 */
	public static StringBuilder toString(Iterable<? extends BranchIdType> elements) {
		return toString(new StringBuilder(), elements);
	}

	/**
	 * Serializes a sequence of {@link BranchIdType}.
	 */
	public static StringBuilder toString(StringBuilder out, Iterable<? extends BranchIdType> elements) {
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
	public static StringBuilder toString(BranchIdType elem) {
		return toString(new StringBuilder(), elem);
	}

	/**
	 * Serializes a {@link BranchIdType}.
	 */
	public static StringBuilder toString(StringBuilder out, BranchIdType elem) {
		elem.visit(ToString.INSTANCE, out);
		return out;
	}

	/**
	 * Serializes a {@link QualifiedTypeName}.
	 */
	public static String qualifiedName(QualifiedTypeName elem) {
		return elem.getName();
	}

	/**
	 * Updates a {@link TLProperty}.
	 */
	public static void updateTLProperty(PooledConnection con, TypePart part, Type newType, Type newOwner,
			String newName, Boolean mandatory, Boolean multiple, Boolean bag,
			Boolean ordered, AnnotatedConfig<TLAttributeAnnotation> annotations)
			throws SQLException, XMLStreamException {
		updateTLStructuredTypePart(con, part, newType, newOwner, newName, mandatory, null, null, multiple, bag,
			ordered, null, toString(annotations));
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
	 *      Boolean, Boolean, Boolean, Boolean, Boolean, AnnotatedConfig)
	 */
	public static void updateInverseReference(PooledConnection con, Reference inverseReference,
			String newName, Boolean mandatory, Boolean composite, Boolean aggregate, Boolean multiple, Boolean bag,
			Boolean ordered, Boolean navigate, AnnotatedConfig<TLAttributeAnnotation> annotations)
			throws SQLException, XMLStreamException {
		TLID endID = inverseReference.getEndID();
		if (endID == null) {
			throw new IllegalStateException("Reference " + toString(inverseReference) + " has no end id.");
		}
		BranchIdType associationEnd =
			BranchIdType.newInstance(BranchIdType.class, inverseReference.getBranch(), endID,
				ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE);

		updateTLStructuredTypePart(con, associationEnd, null, null, null,
			mandatory, composite, aggregate, multiple, bag, ordered, navigate,
			null);
		updateTLStructuredTypePart(con, inverseReference, null, null, newName,
			null, null, null, null, null, null, null,
			toString(annotations));
	}

	/**
	 * Updates a {@link TLReference}.
	 * 
	 * @see #updateInverseReference(PooledConnection, Reference, String, Boolean, Boolean, Boolean,
	 *      Boolean, Boolean, Boolean, Boolean, AnnotatedConfig)
	 */
	public static void updateTLReference(PooledConnection con, Reference reference, Type newType, Type newOwner,
			String newName, Boolean mandatory, Boolean composite, Boolean aggregate, Boolean multiple, Boolean bag,
			Boolean ordered, Boolean navigate, AnnotatedConfig<TLAttributeAnnotation> annotations)
			throws SQLException, XMLStreamException, MigrationException {

		TLID endID = reference.getEndID();
		if (endID == null) {
			throw new IllegalStateException("Reference " + toString(reference) + " has no end id.");
		}
		BranchIdType associationEnd =
			BranchIdType.newInstance(BranchIdType.class, reference.getBranch(), endID,
				ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE);

		// Name of the association end is the name as the reference.
		updateTLStructuredTypePart(con, associationEnd, newType, null, newName,
			mandatory, composite, aggregate, multiple, bag, ordered, navigate,
			null);
		updateTLStructuredTypePart(con, reference, null, newOwner, newName,
			null, null, null, null, null, null, null,
			toString(annotations));

		if (newOwner != null || newType != null || newName != null) {
			Type associationType = getTLTypeOrFail(con, reference.getOwner().getModule(),
				ModelResolver.syntheticAssociationName(reference.getOwner().getTypeName(), reference.getPartName()));
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
					ModelResolver.syntheticAssociationName(newAssociationNameOwnerPart,
						newAssociationNameAttributePart),
					null, null, (String) null);
			}
			if (newOwner != null) {
				// owner of the association is part of the name of the association and the target
				// type
				// of the other end.
				updateTLStructuredTypePart(con, otherPart, newOwner, null, null, null, null, null, null, null, null,
					null, null);
			}
			if (newType != null) {
				// new type is the new owner of the inverse reference, if exists.

				CompiledStatement sql = query(
				parameters(
					parameterDef(DBType.LONG, "branch"),
					parameterDef(DBType.ID, "endID"),
					parameterDef(DBType.ID, "ownerID")),
				update(
					table(SQLH.mangleDBName(ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE)),
					and(
						eqSQL(
							column(BasicTypes.BRANCH_DB_NAME),
							parameter(DBType.LONG, "branch")),
						eqSQL(
							column(refID(PersistentReference.END_ATTR)),
							parameter(DBType.ID, "endID"))),
					Arrays.asList(
						refID(PersistentReference.OWNER_REF)),
					Arrays.asList(
						parameter(DBType.ID, "ownerID")))).toSql(con.getSQLDialect());

				sql.executeUpdate(con, reference.getBranch(), otherPart.getID(), newType.getID());
			}
		}
	}

	/**
	 * Updates the given {@link TLStructuredTypePart}.
	 */
	public static void updateTLStructuredTypePart(PooledConnection con, BranchIdType part, Type newType, Type newOwner,
			String name, Boolean mandatory, Boolean composite, Boolean aggregate, Boolean multiple, Boolean bag,
			Boolean ordered, Boolean navigate, String annotations) throws SQLException {
		List<Parameter> parameterDefs = new ArrayList<>();
		List<String> columns = new ArrayList<>();
		List<SQLExpression> parameters = new ArrayList<>();
		List<Object> arguments = new ArrayList<>();

		parameterDefs.add(parameterDef(DBType.LONG, "branch"));
		parameterDefs.add(parameterDef(DBType.ID, "identifier"));
		arguments.add(part.getBranch());
		arguments.add(part.getID());
		if (newType != null) {
			parameterDefs.add(parameterDef(DBType.STRING, "typeType"));
			columns.add(refType(PersistentStructuredTypePart.TYPE_REF));
			parameters.add(parameter(DBType.STRING, "typeType"));
			arguments.add(newType.getTable());
			parameterDefs.add(parameterDef(DBType.ID, "typeID"));
			columns.add(refID(PersistentStructuredTypePart.TYPE_REF));
			parameters.add(parameter(DBType.ID, "typeID"));
			arguments.add(newType.getID());
		}
		if (newOwner != null) {
			parameterDefs.add(parameterDef(DBType.ID, "ownerID"));
			columns.add(refID(KBBasedMetaAttribute.OWNER_REF));
			parameters.add(parameter(DBType.ID, "ownerID"));
			arguments.add(newOwner.getID());
		}
		if (name != null) {
			parameterDefs.add(parameterDef(DBType.STRING, "name"));
			columns.add(SQLH.mangleDBName(PersistentStructuredTypePart.NAME_ATTR));
			parameters.add(parameter(DBType.STRING, "name"));
			arguments.add(name);
		}
		if (mandatory != null) {
			parameterDefs.add(parameterDef(DBType.BOOLEAN, "mandatory"));
			columns.add(SQLH.mangleDBName(PersistentStructuredTypePart.MANDATORY));
			parameters.add(parameter(DBType.BOOLEAN, "mandatory"));
			arguments.add(mandatory);
		}
		if (composite != null) {
			parameterDefs.add(parameterDef(DBType.BOOLEAN, "composite"));
			columns.add(SQLH.mangleDBName(PersistentReference.COMPOSITE_ATTR));
			parameters.add(parameter(DBType.BOOLEAN, "composite"));
			arguments.add(composite);
		}
		if (aggregate != null) {
			parameterDefs.add(parameterDef(DBType.BOOLEAN, "aggregate"));
			columns.add(SQLH.mangleDBName(PersistentReference.AGGREGATE_ATTR));
			parameters.add(parameter(DBType.BOOLEAN, "aggregate"));
			arguments.add(aggregate);
		}
		if (multiple != null) {
			parameterDefs.add(parameterDef(DBType.BOOLEAN, "multiple"));
			columns.add(SQLH.mangleDBName(PersistentReference.MULTIPLE_ATTR));
			parameters.add(parameter(DBType.BOOLEAN, "multiple"));
			arguments.add(multiple);
		}
		if (bag != null) {
			parameterDefs.add(parameterDef(DBType.BOOLEAN, "bag"));
			columns.add(SQLH.mangleDBName(PersistentReference.BAG_ATTR));
			parameters.add(parameter(DBType.BOOLEAN, "bag"));
			arguments.add(bag);
		}
		if (ordered != null) {
			parameterDefs.add(parameterDef(DBType.BOOLEAN, "ordered"));
			columns.add(SQLH.mangleDBName(PersistentReference.ORDERED_ATTR));
			parameters.add(parameter(DBType.BOOLEAN, "ordered"));
			arguments.add(ordered);
		}
		if (navigate != null) {
			parameterDefs.add(parameterDef(DBType.BOOLEAN, "navigate"));
			columns.add(SQLH.mangleDBName(PersistentReference.NAVIGATE_ATTR));
			parameters.add(parameter(DBType.BOOLEAN, "navigate"));
			arguments.add(navigate);
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
			table(SQLH.mangleDBName(ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE)),
			and(
				eqSQL(
					column(BasicTypes.BRANCH_DB_NAME),
					parameter(DBType.LONG, "branch")),
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
	public static void updateTLStructuredType(PooledConnection con, Type type, Module newModule, String newName,
			Boolean isAbstract, Boolean isFinal, AnnotatedConfig<TLTypeAnnotation> annotations)
			throws SQLException, XMLStreamException {
		updateTLStructuredType(con, type, newModule, newName, isAbstract, isFinal, toString(annotations));
	}

	/**
	 * Updates the given {@link TLStructuredType}.
	 */
	public static void updateTLStructuredType(PooledConnection con, Type type, Module newModule, String newName,
			Boolean isAbstract, Boolean isFinal, String annotations) throws SQLException {
		List<Parameter> parameterDefs = new ArrayList<>();
		List<String> columns = new ArrayList<>();
		List<SQLExpression> parameters = new ArrayList<>();
		List<Object> arguments = new ArrayList<>();

		parameterDefs.add(parameterDef(DBType.LONG, "branch"));
		parameterDefs.add(parameterDef(DBType.ID, "identifier"));
		arguments.add(type.getBranch());
		arguments.add(type.getID());
		if (newModule != null) {
			columns.add(refType(KBBasedMetaElement.SCOPE_REF));
			parameters.add(literalString(newModule.getTable()));

			parameterDefs.add(parameterDef(DBType.ID, "moduleID"));
			columns.add(refID(KBBasedMetaElement.SCOPE_REF));
			parameters.add(parameter(DBType.ID, "moduleID"));
			columns.add(refID(KBBasedMetaElement.MODULE_REF));
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
			columns.add(SQLH.mangleDBName(PersistentClass.ABSTRACT_ATTR));
			parameters.add(parameter(DBType.BOOLEAN, "isAbstract"));
			arguments.add(isAbstract);
		}
		if (isFinal != null) {
			parameterDefs.add(parameterDef(DBType.BOOLEAN, "isFinal"));
			columns.add(SQLH.mangleDBName(PersistentClass.FINAL_ATTR));
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
				eqSQL(
					column(BasicTypes.BRANCH_DB_NAME),
					parameter(DBType.LONG, "branch")),
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
	public static void updateTLDataType(PooledConnection con, Type type, Module newModule, String newName, Kind kind,
			DBColumnType columnType, PolymorphicConfiguration<StorageMapping<?>> storageMapping,
			AnnotatedConfig<TLTypeAnnotation> annotations) throws SQLException, XMLStreamException {
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
	public static void updateTLDataType(PooledConnection con, Type type, Module newModule, String newName,
			Kind kind, String dbType, Integer dbSize, Integer dbPrecision, Boolean isBinary, String storageMapping,
			String annotations) throws SQLException {
		List<Parameter> parameterDefs = new ArrayList<>();
		List<String> columns = new ArrayList<>();
		List<SQLExpression> parameters = new ArrayList<>();
		List<Object> arguments = new ArrayList<>();

		parameterDefs.add(parameterDef(DBType.LONG, "branch"));
		parameterDefs.add(parameterDef(DBType.ID, "identifier"));
		arguments.add(type.getBranch());
		arguments.add(type.getID());
		if (newModule != null) {
			columns.add(refType(KBBasedMetaElement.SCOPE_REF));
			parameters.add(literalString(newModule.getTable()));

			parameterDefs.add(parameterDef(DBType.ID, "moduleID"));
			columns.add(refID(KBBasedMetaElement.SCOPE_REF));
			parameters.add(parameter(DBType.ID, "moduleID"));
			columns.add(refID(KBBasedMetaElement.MODULE_REF));
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
			columns.add(SQLH.mangleDBName(PersistentDatatype.KIND_ATTR));
			parameters.add(parameter(DBType.STRING, "kind"));
			arguments.add(kind.getExternalName());
		}
		if (dbType != null) {
			parameterDefs.add(parameterDef(DBType.STRING, "dbType"));
			columns.add(SQLH.mangleDBName(PersistentDatatype.DB_TYPE_ATTR));
			parameters.add(parameter(DBType.STRING, "dbType"));
			arguments.add(dbType);
		}
		if (dbSize != null) {
			parameterDefs.add(parameterDef(DBType.INT, "dbSize"));
			columns.add(SQLH.mangleDBName(PersistentDatatype.DB_SIZE_ATTR));
			parameters.add(parameter(DBType.INT, "dbSize"));
			arguments.add(dbSize);
		}
		if (dbPrecision != null) {
			parameterDefs.add(parameterDef(DBType.INT, "dbPrecision"));
			columns.add(SQLH.mangleDBName(PersistentDatatype.DB_PRECISION_ATTR));
			parameters.add(parameter(DBType.INT, "dbPrecision"));
			arguments.add(dbPrecision);
		}
		if (isBinary != null) {
			parameterDefs.add(parameterDef(DBType.BOOLEAN, "isBinary"));
			columns.add(SQLH.mangleDBName(PersistentDatatype.BINARY_ATTR));
			parameters.add(parameter(DBType.BOOLEAN, "isBinary"));
			arguments.add(isBinary);
		}
		if (storageMapping != null) {
			parameterDefs.add(parameterDef(DBType.STRING, "storageMapping"));
			columns.add(SQLH.mangleDBName(PersistentDatatype.STORAGE_MAPPING));
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
				table(SQLH.mangleDBName(PersistentDatatype.OBJECT_TYPE)),
				and(
					eqSQL(
						column(BasicTypes.BRANCH_DB_NAME),
						parameter(DBType.LONG, "branch")),
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
	public static void deleteModelPart(PooledConnection connection, BranchIdType modelPart)
			throws SQLException {
		CompiledStatement delete = query(
		parameters(
			parameterDef(DBType.LONG, "branch"),
			parameterDef(DBType.ID, "id")),
		delete(
			table(SQLH.mangleDBName(modelPart.getTable())),
			and(
				eqSQL(
					column(BasicTypes.BRANCH_DB_NAME),
					parameter(DBType.LONG, "branch")),
				eqSQL(
					column(BasicTypes.IDENTIFIER_DB_NAME),
					parameter(DBType.ID, "id"))))).toSql(connection.getSQLDialect());
	
		delete.executeUpdate(connection, modelPart.getBranch(), modelPart.getID());
	}

	/**
	 * Deletes the rows for given model parts.
	 */
	public static void deleteModelParts(PooledConnection connection, Collection<? extends BranchIdType> modelParts)
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
	public static void deleteModelPartsHomogeneous(PooledConnection connection,
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
					parameterDef(DBType.LONG, "branch"),
					setParameterDef("id", DBType.ID)),
				delete(
					table(SQLH.mangleDBName(representative.getTable())),
					and(
						eqSQL(
							column(BasicTypes.BRANCH_DB_NAME),
							parameter(DBType.LONG, "branch")),
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
	public static void deleteTLType(PooledConnection connection, Type type, boolean failOnExistingAttributes)
			throws SQLException, MigrationException {
		List<BranchIdType> toDelete = new ArrayList<>();
	
		toDelete.add(type);
	
		List<TypePart> tlTypeParts = getTLTypeParts(connection, type);
		if (!tlTypeParts.isEmpty() && failOnExistingAttributes) {
			throw new MigrationException(
				"Class '" + toString(type) + "' has parts " + toString(tlTypeParts));
		}
		toDelete.addAll(tlTypeParts);
		toDelete.addAll(getGeneralizations(connection, type));
		toDelete.addAll(getSpecializations(connection, type));
	
		deleteModelParts(connection, toDelete);
	}


}
