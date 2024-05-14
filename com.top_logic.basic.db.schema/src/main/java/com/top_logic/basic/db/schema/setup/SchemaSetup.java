/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.schema.setup;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedInstanceConfig;
import com.top_logic.basic.config.NamedResource;
import com.top_logic.basic.config.ResourceDeclaration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.db.model.DBColumn;
import com.top_logic.basic.db.model.DBColumnRef;
import com.top_logic.basic.db.model.DBPrimary;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.DBSchemaFactory;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.util.DBSchemaUtils;
import com.top_logic.basic.db.schema.io.MORepositoryBuilder;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.db.schema.setup.config.TypeProvider;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MOFactory;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.MetaObject.Kind;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.ex.DuplicateAttributeException;
import com.top_logic.dob.ex.IncompatibleTypeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.DefaultMORepository;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOClassImpl;
import com.top_logic.dob.meta.MOIndex;
import com.top_logic.dob.meta.MOIndexImpl;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.schema.config.MetaObjectName;
import com.top_logic.dob.schema.config.MetaObjectsConfig;
import com.top_logic.dob.schema.config.annotation.DirectIndexColumnsStrategy;
import com.top_logic.dob.schema.config.annotation.IndexColumnsStrategy;
import com.top_logic.dob.schema.config.annotation.IndexColumnsStrategyAnnotation;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.DBIndex;
import com.top_logic.dob.sql.DBTableMetaObject;
import com.top_logic.dsa.util.ConfigResourceLoader;

/**
 * Helper class to setup the tables of a {@link SchemaConfiguration}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SchemaSetup implements ConfiguredInstance<SchemaConfiguration> {

	private final SchemaConfiguration _schemaConfig;

	/**
	 * Creates a {@link SchemaSetup} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public SchemaSetup(InstantiationContext context, SchemaConfiguration config) {
		_schemaConfig = config;
	}

	@Override
	public SchemaConfiguration getConfig() {
		return _schemaConfig;
	}

	/**
	 * Resolves this {@link SchemaSetup} and returns the resolved one.
	 * 
	 * <p>
	 * This method fetches the {@link MetaObjectsConfig} stored in the resources given in
	 * {@link SchemaConfiguration#getDeclarations()}. A new {@link SchemaConfiguration} is created
	 * with the fetched {@link MetaObjectsConfig} as {@link SchemaConfiguration#getMetaObjects()}.
	 * </p>
	 * 
	 * <p>
	 * This method is idempotent.
	 * </p>
	 * 
	 * @param context
	 *        {@link InstantiationContext} to create {@link MetaObjectsConfig} and new
	 *        {@link SchemaSetup}.
	 * 
	 * @return The resolved {@link SchemaSetup}. A reference to this {@link SchemaSetup} when it is
	 *         already resolved.
	 */
	public final SchemaSetup resolve(InstantiationContext context) throws ConfigurationException {
		if (isResolved()) {
			return this;
		}
		return internalResolve(context);
	}

	/**
	 * Actual implementation of {@link #resolve(InstantiationContext)}.
	 * 
	 * <p>
	 * This {@link SchemaSetup} is not resolved.
	 * </p>
	 */
	protected SchemaSetup internalResolve(InstantiationContext context) throws ConfigurationException {
		SchemaConfiguration copy = TypedConfiguration.copy(getConfig());
		resolveDeclarations(context, copy);
		addAdditionalTables(copy);
		return context.getInstance(copy);
	}

	private void addAdditionalTables(SchemaConfiguration copy) {
		DBSchema schema = buildSchema();
		copy.setAdditionalTables(schema);
	}

	/**
	 * Resolves {@link SchemaConfiguration#getDeclarations()} and adds parsed contents of the
	 * referenced files to {@link SchemaConfiguration#getMetaObjects()} of the given
	 * {@link SchemaConfiguration}.
	 */
	public static void resolveDeclarations(InstantiationContext context, SchemaConfiguration schema)
			throws ConfigurationException {
		MetaObjectsConfig typeSystem = MORepositoryBuilder.readTypeSystem(context, schema.getDeclarations());

		if (schema.getMetaObjects() == null) {
			schema.setMetaObjects(typeSystem);
		} else {
			// Move types to given SchemaConfiguration.
			Collection<MetaObjectName> resolvedTypes = new ArrayList<>(typeSystem.getTypes().values());
			typeSystem.getTypes().clear();

			// Add to existing types.
			for (MetaObjectName type : resolvedTypes) {
				schema.getMetaObjects().getTypes().put(type.getObjectName(), type);
			}
		}

		// Mark as resolved.
		schema.getDeclarations().clear();
	}

	private boolean isResolved() {
		return getConfig().getDeclarations().isEmpty();
	}

	/**
	 * Creates the types in the {@link SchemaConfiguration} in the given {@link MORepository}.
	 */
	public final void createTypes(InstantiationContext context, MORepository repository, MOFactory typeFactory) {
		internalCreateTypes(context, repository, typeFactory);
	}

	/**
	 * Actual implementation of {@link #createTypes(InstantiationContext, MORepository, MOFactory)}.
	 */
	protected void internalCreateTypes(InstantiationContext context, MORepository repository, MOFactory typeFactory) {
		MetaObjectsConfig metaObjects = getConfig().getMetaObjects();
		if (metaObjects == null) {
			throw new IllegalStateException("SchemaSetup not resolved.");
		}
		new MORepositoryBuilder(context, typeFactory, repository).build(metaObjects);
	
		List<NamedInstanceConfig<? extends TypeProvider>> providerConfigs = getConfig().getProviders();
		List<TypeProvider> providers =
			TypedConfiguration.getInstanceListNamed(context, providerConfigs);
		for (TypeProvider provider : providers) {
			provider.createTypes(context, typeFactory, repository);
		}
	}

	/**
	 * Creates the tables in the given {@link ConnectionPool}.
	 * 
	 * @param checkExistence
	 *        Whether a check for existence of the tables must occur.
	 * 
	 * @see DBSchemaUtils#createTables(PooledConnection, DBSchema, boolean)
	 */
	public void createTables(ConnectionPool pool, MORepository typeRepository, boolean checkExistence)
			throws SQLException {
		DBSchema schema = buildSchema(typeRepository);
		PooledConnection connection = pool.borrowWriteConnection();
		try {
			DBSchemaUtils.createTables(connection, schema, checkExistence);
		} finally {
			pool.releaseWriteConnection(connection);
		}
	}

	/**
	 * Writes the create statements of the tables to the given stream.
	 */
	public void printCreateTables(Appendable out, MORepository typeRepository, DBHelper sqlDialect) {
		DBSchema schema = buildSchema(typeRepository);
		DBSchemaUtils.toSQL(out, sqlDialect, schema);
	}

	/**
	 * Resets the tables.
	 * 
	 * @see DBSchemaUtils#resetTables(ConnectionPool, DBSchema, boolean)
	 */
	public void resetTables(ConnectionPool pool, MORepository typeRepository, boolean truncate) {
		DBSchema schema = buildSchema(typeRepository);
		DBSchemaUtils.resetTables(pool, schema, truncate);
	}

	/**
	 * Writes the "Reset tables" statement to the given writer.
	 */
	public void printResetTables(PrintWriter out, MORepository typeRepository, DBHelper sqlDialect, boolean truncate) {
		DBSchema schema = buildSchema(typeRepository);
		for (DBTable table : schema.getTables()) {
			String tableName = table.getDBName();
			if (truncate) {
				out.println(sqlDialect.getTruncateTableStatement(tableName));
			} else {
				out.println("DROP TABLE " + sqlDialect.tableRef(tableName));
			}
			out.println(';');
			out.println();
		}
	}

	/**
	 * Creates a {@link DBSchema} for all configured schemas and all {@link MOClass} in the given
	 * {@link MORepository}.
	 * 
	 * <p>
	 * Shortcut for {@link #buildSchema()} and {@link #addTables(DBSchema, MORepository)}.
	 * </p>
	 */
	public final DBSchema buildSchema(MORepository typeRepository) {
		DBSchema schema = buildSchema();
		addTables(schema, typeRepository);
		return schema;
	}

	/**
	 * Creates a {@link DBSchema} for all configured schemas.
	 */
	public DBSchema buildSchema() {
		List<NamedResource> schemas = getConfig().getSchemas();
		DBSchema schema = newDBSchema(schemas);
		// Allocate tables locally in the current schema.
		schema.setName(null);
		return schema;
	}

	/**
	 * Create {@link DBTable} for all types in the given {@link MORepository} and adds them to the
	 * given {@link DBSchema}.
	 * 
	 * @see #addTables(DBSchema, MORepository, Collection)
	 */
	public static void addTables(DBSchema schema, MORepository typeRepository) {
		List<String> moNames = typeRepository.getMetaObjectNames();
		try {
			addTables(schema, typeRepository, moNames);
		} catch (UnknownTypeException e) {
			throw new UnreachableAssertion("Iterated over all types in the repository.");
		}
	}

	/**
	 * Create {@link DBTable} for the types in the given {@link MORepository} with the given names
	 * and adds them to the given {@link DBSchema}.
	 * 
	 * @param schema
	 *        The schema to add tables to
	 * @param typeRepository
	 *        The {@link MORepository} to fetch types from.
	 * @param moNames
	 *        The names of the types in the {@link MORepository} to create tables for.
	 * @throws UnknownTypeException
	 *         iff the names contain an unknown name.
	 */
	public static void addTables(DBSchema schema, MORepository typeRepository, Collection<String> moNames)
			throws UnknownTypeException {
		for (String name : moNames) {
			MetaObject metaObject = typeRepository.getMetaObject(name);
			if (metaObject.getKind() != Kind.item) {
				continue;
			}

			MOClass type = (MOClass) metaObject;
			if (type.isAbstract()) {
				continue; // No table for abstract types.
			}
			schema.getTables().add(createTable(type));
		}
	}

	/**
	 * Creates a {@link DBTable} for the given {@link MOClass}
	 * 
	 * @param type
	 *        The {@link MOClass} to create {@link DBTable} for.
	 */
	public static DBTable createTable(MOClass type) {
		DBTable result = createDBTable(type);

		IndexColumnsStrategy indexStrategy = getIndexStrategy(type);
		List<MOIndex> indexes = type.getIndexes();
		for (int i = 0, n = indexes.size(); i < n; i++) {
			addIndex(type, result, (DBIndex) indexes.get(i), indexStrategy);
		}

		return result;
	}

	/**
	 * Creates a {@link DBColumn} for the given {@link DBAttribute}
	 * 
	 * @param attribute
	 *        The {@link DBAttribute} to create {@link DBColumn} for.
	 */
	public static DBColumn createColumn(DBAttribute attribute) {
		DBType dbType = attribute.getSQLType();
		int dbSize = attribute.getSQLSize();
		int dbPrec = attribute.getSQLPrecision();
		boolean dbMandatory = attribute.isSQLNotNull();
		boolean binary = attribute.isBinary();

		DBColumn column = DBSchemaFactory.createColumn(attribute.getDBName());
		column.setType(dbType);
		column.setBinary(binary);
		column.setMandatory(dbMandatory);
		column.setSize(dbSize);
		column.setPrecision(dbPrec);
		return column;
	}

	/**
	 * Creates a generic table declaration for the given type.
	 * 
	 * @param type
	 *        The type to translate in a table declaration.
	 */
	private static DBTable createDBTable(MOClass type) {
		DBTable result = DBSchemaFactory.createTable(type.getName());
		DBTableMetaObject dbMapping = type.getDBMapping();
		result.setExplicitDBName(dbMapping.getDBName());

		List<DBAttribute> attrs = dbMapping.getDBAttributes();
		for (int n = 0, cnt = attrs.size(); n < cnt; n++) {
			DBColumn column = createColumn(attrs.get(n));
			result.getColumns().add(column);
		}

		// Define the primary key.
		MOIndex primaryKey = type.getPrimaryKey();
		if (primaryKey != null) {
			DBPrimary primary = DBSchemaFactory.createPrimary();
			for (DBAttribute keyColumn : primaryKey.getKeyAttributes()) {
				primary.getColumnRefs().add(DBSchemaFactory.ref(keyColumn.getDBName()));
			}
			result.setPrimaryKey(primary);
		}

		result.setPKeyStorage(dbMapping.isPKeyStorage());
		result.setCompress(dbMapping.getCompress());

		return result;
	}

	/**
	 * Adds an index declaration to the given table representing the given index declaration.
	 * 
	 * @param type
	 *        The context type.
	 * @param table
	 *        The base table for the context type.
	 * @param index
	 *        The index declaration to translate.
	 * @param indexStrategy
	 *        The {@link IndexColumnsStrategy} to use for translation.
	 */
	private static void addIndex(MOClass type, DBTable table, DBIndex index, IndexColumnsStrategy indexStrategy) {
		if (index.isInMemory()) {
			return;
		}

		com.top_logic.basic.db.model.DBIndex dbIndex = DBSchemaFactory.createIndex(index.getDBName());
		if (index.isUnique()) {
			dbIndex.setKind(com.top_logic.basic.db.model.DBIndex.Kind.UNIQUE);
		} else {
			dbIndex.setKind(com.top_logic.basic.db.model.DBIndex.Kind.DEFAULT);
		}
		dbIndex.setCompress(index.getCompress());

		List<DBAttribute> indexColumns = indexStrategy.createIndexColumns(type, index);
		for (int n = 0, size = indexColumns.size(); n < size; n++) {
			dbIndex.getColumnRefs().add(DBSchemaFactory.ref(indexColumns.get(n).getDBName()));
		}

		table.getIndices().add(dbIndex);
	}

	/**
	 * Creates a {@link MORepository} for types in the represented {@link SchemaConfiguration}.
	 */
	public MORepository createMORepository(MOFactory typeFactory) {
		InstantiationContext context = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
		MORepository repository = new DefaultMORepository(getConfig().hasMultipleBranches());
		createTypes(context, repository, typeFactory);
		repository.resolveReferences();
		return repository;
	}

	/**
	 * Creates a {@link MOStructure} from the given {@link DBTable table definition}.
	 */
	public static MOClass createTableType(DBTable table, MOFactory factory) {
		MOClassImpl tableType = (MOClassImpl) factory.createMOClass(table.getName());
		tableType.setDBName(table.getDBName());
		for (DBColumn column : table.getColumns()) {
			String columnName = column.getName();
			DBType dbType = column.getType();
			MetaObject targetType = toPrimitive(dbType);
			int dbSize = (int) column.getSize();
			int dbPrecision = column.getPrecision();
			boolean mandatory = column.isMandatory();
			MOAttribute attribute = factory.createMOAttribute(columnName, targetType);
			attribute.setMandatory(mandatory);
			attribute.setImmutable(!MOAttribute.IMMUTABLE);
			((MOAttributeImpl) attribute).setDBName(column.getDBName());
			((MOAttributeImpl) attribute).setSQLType(dbType);
			((MOAttributeImpl) attribute).setSQLSize(dbSize);
			((MOAttributeImpl) attribute).setSQLPrecision(dbPrecision);
			try {
				tableType.addAttribute(attribute);
			} catch (DuplicateAttributeException ex) {
				throw new UnreachableAssertion("Table with non-unique column names.", ex);
			}
		}
		for (com.top_logic.basic.db.model.DBIndex index : table.getIndices()) {
			MOIndex moIndex;
			switch (index.getKind()) {
				case DEFAULT:
					moIndex = newIndex(tableType, index, false);
					break;
				case UNIQUE:
					moIndex = newIndex(tableType, index, true);
					break;
				case PRIMARY:
					moIndex = MOIndexImpl.newPrimaryKey(getIndexAttribute(tableType, index.getColumnRefs()));
					break;
				default:
					throw new UnreachableAssertion("No such kind " + index.getKind());
			}
			tableType.addIndex(moIndex);
		}

		return tableType;
	}

	static MOIndexImpl newIndex(MOStructure tableType, com.top_logic.basic.db.model.DBIndex index,
			boolean uniqueFlag) {
		DBAttribute[] columns = getIndexAttribute(tableType, index.getColumnRefs());
		try {
			return new MOIndexImpl(index.getName(), index.getDBName(), columns, uniqueFlag, false, false,
				index.getCompress());
		} catch (IncompatibleTypeException ex) {
			throw new ConfigurationError("Invalid index definition: " + index, ex);
		}
	}

	private static DBAttribute[] getIndexAttribute(MOStructure tableType, List<DBColumnRef> columnRefs) {
		DBAttribute[] indexAttributes = new DBAttribute[columnRefs.size()];
		for (int i = 0; i < indexAttributes.length; i++) {
			indexAttributes[i] = (MOAttributeImpl) tableType.getAttribute(columnRefs.get(i).getName());
		}
		return indexAttributes;
	}

	static MetaObject toPrimitive(DBType type) {
		switch (type) {
			case BLOB:
				return MOPrimitive.BLOB;
			case BOOLEAN:
				return MOPrimitive.BOOLEAN;
			case BYTE:
				return MOPrimitive.BYTE;
			case CHAR:
				return MOPrimitive.CHARACTER;
			case CLOB:
				return MOPrimitive.CLOB;
			case DATE:
				return MOPrimitive.DATE;
			case DATETIME:
				return MOPrimitive.DATE;
			case DECIMAL:
				return MOPrimitive.DOUBLE;
			case DOUBLE:
				return MOPrimitive.DOUBLE;
			case FLOAT:
				return MOPrimitive.FLOAT;
			case ID:
				return MOPrimitive.TLID;
			case INT:
				return MOPrimitive.INTEGER;
			case LONG:
				return MOPrimitive.LONG;
			case SHORT:
				return MOPrimitive.SHORT;
			case STRING:
				return MOPrimitive.STRING;
			case TIME:
				return MOPrimitive.DATE;
		}
		throw new UnreachableAssertion("No such type: " + type);
	}

	/**
	 * Looks up the {@link IndexColumnsStrategy} for the given type.
	 */
	public static IndexColumnsStrategy getIndexStrategy(MOClass self) {
		IndexColumnsStrategyAnnotation annotation = self.getAnnotation(IndexColumnsStrategyAnnotation.class);
		if (annotation == null) {
			MOClass superclass = self.getSuperclass();
			if (superclass != null) {
				return getIndexStrategy(superclass);
			}
			return DirectIndexColumnsStrategy.INSTANCE;
		} else {
			return annotation.getStrategy();
		}
	}

	/**
	 * Creates a new {@link DBSchema} containing the given schemas
	 * 
	 * @param schemas
	 *        The names of the resources to contain in the result schema.
	 */
	public static DBSchema newDBSchema(List<? extends ResourceDeclaration> schemas) {
		if (schemas.isEmpty()) {
			return DBSchemaUtils.newDBSchema();
		}
		DBSchema schema;
		try {
			InstantiationContext ctx = new DefaultInstantiationContext(DBSchemaUtils.class);
			schema = ConfigResourceLoader.loadDeclarations(ctx, DBSchema.SCHEMA_ELEMENT, DBSchema.class, schemas);
			ctx.checkErrors();
		} catch (ConfigurationException ex) {
			throw new ConfigurationError("Loading schema declarations failed.", ex);
		}
		return schema;
	}

}
