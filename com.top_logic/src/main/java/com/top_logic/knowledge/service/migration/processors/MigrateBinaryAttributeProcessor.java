/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration.processors;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.constraint.annotation.Bound;
import com.top_logic.basic.config.constraint.annotation.Comparision;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.impl.Positive;
import com.top_logic.basic.config.format.MemorySizeFormat;
import com.top_logic.basic.db.model.DBColumn;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLModifyColumn;
import com.top_logic.basic.io.blob.BlobStoreNames;
import com.top_logic.basic.io.blob.BlobUpload;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.AbstractBinaryAttribute;
import com.top_logic.dob.attr.BinaryAttributeKind;
import com.top_logic.dob.attr.HybridBinaryAttribute;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.schema.config.AttributeConfig;
import com.top_logic.dob.schema.config.MetaObjectConfig;
import com.top_logic.dob.schema.config.MetaObjectName;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.xml.DOXMLConstants;
import com.top_logic.knowledge.service.BinaryStorageFieldModes;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.db2.PersistentObject;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.QualifiedTypeName;
import com.top_logic.model.migration.data.Type;

/**
 * {@link MigrationProcessor} changing the kind, the blob store or the threshold of a binary
 * attribute and moving its stored content accordingly.
 *
 * <p>
 * The processor has two modes, decided by the table:
 * </p>
 *
 * <dl>
 * <dt>Declared attribute</dt>
 * <dd>If the table (or one of its super tables) declares a column with the attribute name, the
 * attribute declaration in the stored schema is changed to the configured kind, store and
 * threshold. The current declaration is either one of the kinds of {@link BinaryAttributeKind} or
 * a plain <code>Blob</code> column storing only the content. The processor adds the columns of the
 * target kind, moves the content of all rows (including historic revisions) between the database
 * and the blob stores, fills size and content type of values from a plain <code>Blob</code> column
 * (size of the content, content type <code>application/octet-stream</code>, no name), and drops
 * the columns no longer used. If the declaration already has the configured kind, store and
 * threshold, nothing is done.</dd>
 *
 * <dt>Dynamic attribute</dt>
 * <dd>If the table has no column with the attribute name, the attribute is a dynamic attribute of
 * the objects of the table (and its sub tables), whose values are stored in the table of dynamic
 * binary values. The values of the attribute are re-stored with the configured kind, store and
 * threshold: {@link BinaryAttributeKind#INLINE} moves all content into the database,
 * {@link BinaryAttributeKind#REF} all content into the store, {@link BinaryAttributeKind#HYBRID}
 * decides by the threshold. The values to move can be restricted to the objects of a model type.
 * This mode does not change any schema; the storage settings used for new values are given by the
 * annotation of the model attribute.</dd>
 * </dl>
 *
 * <p>
 * Content in a blob store that is no longer referenced after the migration (because it was moved
 * to the database or to another store) is removed by the blob garbage collection.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MigrateBinaryAttributeProcessor
		extends AbstractConfiguredInstance<MigrateBinaryAttributeProcessor.Config<?>>
		implements MigrationProcessor {

	/**
	 * Properties of an attribute declaration that are not taken over to the migrated declaration.
	 */
	private static final Set<String> NON_COPIED_PROPERTIES = Set.of(
		PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME,
		DOXMLConstants.ATT_TYPE_ATTRIBUTE,
		DOXMLConstants.DB_TYPE_ATTRIBUTE,
		DOXMLConstants.DB_SIZE_ATTRIBUTE,
		DOXMLConstants.DB_PREC_ATTRIBUTE,
		DOXMLConstants.BINARY_ATTRIBUTE,
		AbstractBinaryAttribute.ExternalConfig.STORE,
		HybridBinaryAttribute.Config.THRESHOLD,
		AttributeConfig.STORAGE_PROPERTY);

	/**
	 * Configuration options for {@link MigrateBinaryAttributeProcessor}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config<I extends MigrateBinaryAttributeProcessor> extends PolymorphicConfiguration<I> {

		/** Tag name of a {@link MigrateBinaryAttributeProcessor} in a migration script. */
		String TAG_NAME = "migrate-binary-attribute";

		/** Configuration name of {@link #getTable()}. */
		String TABLE = "table";

		/** Configuration name of {@link #getAttribute()}. */
		String ATTRIBUTE = "attribute";

		/** Configuration name of {@link #getKind()}. */
		String KIND = "kind";

		/** Configuration name of {@link #getStore()}. */
		String STORE = "store";

		/** Configuration name of {@link #getThreshold()}. */
		String THRESHOLD = "threshold";

		/** Configuration name of {@link #getTLType()}. */
		String TL_TYPE = "tl-type";

		/**
		 * The name of the table whose attribute is migrated.
		 *
		 * <p>
		 * The logical name of the table as defined in the schema of the knowledge base. If the
		 * attribute is declared in a super table, the declaration in the super table is migrated
		 * together with the values of all its sub tables.
		 * </p>
		 */
		@Name(TABLE)
		@Mandatory
		String getTable();

		/**
		 * The name of the binary attribute to migrate.
		 *
		 * <p>
		 * If the table has no column of this name, the name is the name of a dynamic attribute of
		 * the objects of the table.
		 * </p>
		 */
		@Name(ATTRIBUTE)
		@Mandatory
		String getAttribute();

		/**
		 * The kind of binary attribute to migrate to.
		 */
		@Name(KIND)
		@Mandatory
		BinaryAttributeKind getKind();

		/**
		 * The name of the blob store for content stored externally.
		 *
		 * <p>
		 * If no store is given, the default store of the blob store service is used. Only relevant
		 * for the kinds storing content in a blob store.
		 * </p>
		 */
		@Name(STORE)
		@Nullable
		@Options(fun = BlobStoreNames.class)
		@DynamicMode(fun = BinaryStorageFieldModes.StoreMode.class, args = @Ref(KIND))
		String getStore();

		/**
		 * The size from which on content is stored in the blob store.
		 *
		 * <p>
		 * Only relevant for the hybrid kind. The size is given in bytes, optionally with a unit,
		 * e.g. <code>64KB</code> or <code>1MB</code>. The size must be positive, since a hybrid
		 * attribute storing all content in the blob store is a reference attribute, and must not
		 * exceed 2147483639 bytes (2 GB minus 8 bytes), since content of unknown size is buffered in
		 * memory up to this size to decide.
		 * </p>
		 *
		 * @implNote The upper bound is {@link BlobUpload#MAX_BUFFERED_THRESHOLD}.
		 */
		@Name(THRESHOLD)
		@Format(MemorySizeFormat.class)
		@LongDefault(HybridBinaryAttribute.DEFAULT_THRESHOLD)
		@Constraint(Positive.class)
		@Bound(comparison = Comparision.SMALLER_OR_EQUAL, value = BlobUpload.MAX_BUFFERED_THRESHOLD)
		@DynamicMode(fun = BinaryStorageFieldModes.ThresholdMode.class, args = @Ref(KIND))
		long getThreshold();

		/**
		 * The model type whose objects' values are migrated.
		 *
		 * <p>
		 * Only relevant for a dynamic attribute: if given, only the values of objects of this type
		 * and its specializations are migrated. Otherwise all values of the attribute of the objects
		 * of the table are migrated.
		 * </p>
		 */
		@Name(TL_TYPE)
		@Nullable
		QualifiedTypeName getTLType();

	}

	/**
	 * Creates a {@link MigrateBinaryAttributeProcessor} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MigrateBinaryAttributeProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		Config<?> config = getConfig();
		String tableName = config.getTable();
		String attributeName = config.getAttribute();

		MORepository repository = context.getPersistentRepository();
		MetaObject type = repository.getTypeOrNull(tableName);
		if (!(type instanceof MOStructure)) {
			log.error("No such table '" + tableName + "' for migrating binary attribute '" + attributeName + "'.");
			return;
		}
		MOStructure table = (MOStructure) type;

		try {
			MOAttribute attribute = table.getAttributeOrNull(attributeName);
			if (attribute == null) {
				migrateDynamic(context, log, connection, repository, table);
			} else {
				migrateDeclared(context, log, connection, repository, attribute);
			}
		} catch (SQLException | IOException | MigrationException | RuntimeException ex) {
			log.error("Failed to migrate binary attribute '" + attributeName + "' of table '" + tableName + "': "
				+ ex.getMessage(), ex);
		}
	}

	private void migrateDeclared(MigrationContext context, Log log, PooledConnection connection,
			MORepository repository, MOAttribute attribute) throws SQLException, IOException {
		Config<?> config = getConfig();
		String attributeName = attribute.getName();
		MOStructure declaringTable = attribute.getOwner();

		boolean plain;
		if (attribute instanceof AbstractBinaryAttribute) {
			plain = false;
		} else if (attribute.getMetaObject() == MOPrimitive.BLOB && attribute.getDbMapping().length == 1) {
			plain = true;
		} else {
			log.error("Attribute '" + attributeName + "' of table '" + declaringTable.getName()
				+ "' is not a binary attribute.");
			return;
		}

		SchemaConfiguration schema = context.getPersistentSchema();
		MetaObjectConfig tableConfig = tableConfig(schema, declaringTable.getName());
		if (tableConfig == null) {
			log.error("Table '" + declaringTable.getName() + "' is not defined in the stored schema.");
			return;
		}
		List<AttributeConfig> attributeConfigs = tableConfig.getAttributes();
		int index = indexOf(attributeConfigs, attributeName);
		if (index < 0) {
			log.error("Attribute '" + attributeName + "' is not declared in table '" + declaringTable.getName()
				+ "' of the stored schema.");
			return;
		}
		AttributeConfig sourceConfig = attributeConfigs.get(index);

		AbstractBinaryAttribute.Config targetConfig = createConfig(sourceConfig, config.getKind());
		if (targetConfig.isOverride()) {
			log.error("Attribute '" + attributeName + "' of table '" + declaringTable.getName()
				+ "' overrides an attribute of a super table, migrate the attribute of the super table.");
			return;
		}
		if (config.getKind().isExternal()) {
			((AbstractBinaryAttribute.ExternalConfig) targetConfig).setStore(config.getStore());
		}
		if (config.getKind() == BinaryAttributeKind.HYBRID) {
			((HybridBinaryAttribute.Config) targetConfig).setThreshold(config.getThreshold());
		}
		AbstractBinaryAttribute target = instantiate(targetConfig);
		BinaryPlacement placement = BinaryPlacement.of(target);

		if (!plain && isSame(BinaryPlacement.of((AbstractBinaryAttribute) attribute), placement)) {
			log.info("Binary attribute '" + attributeName + "' of table '" + declaringTable.getName()
				+ "' is already stored as " + placement + ".");
			return;
		}

		AbstractBinaryAttribute plainSource = plain ? instantiate(createConfig(sourceConfig, BinaryAttributeKind.INLINE)) : null;

		log.info("Migrating binary attribute '" + attributeName + "' of table '" + declaringTable.getName() + "' to "
			+ placement + ".");
		for (MOStructure concreteTable : concreteTables(repository, declaringTable)) {
			MOAttribute sourceAttribute = concreteTable.getAttributeOrNull(attributeName);
			if (sourceAttribute == null) {
				continue;
			}
			migrateTable(log, connection, concreteTable, sourceAttribute, plainSource, target, placement);
		}

		attributeConfigs.remove(index);
		attributeConfigs.add(index, targetConfig);
		AddMOAttributeProcessor.updateStoredSchema(log, connection, schema);
	}

	private void migrateTable(Log log, PooledConnection connection, MOStructure table, MOAttribute sourceAttribute,
			AbstractBinaryAttribute plainSource, AbstractBinaryAttribute target, BinaryPlacement placement)
			throws SQLException, IOException {
		SQLProcessor sql = new SQLProcessor(connection);
		String tableName = table.getDBMapping().getDBName();

		// Database column name to whether the column is declared not null.
		Map<String, Boolean> columns = new HashMap<>();

		AbstractBinaryAttribute source;
		if (plainSource != null) {
			source = plainSource;
			DBAttribute plainColumn = sourceAttribute.getDbMapping()[0];
			String dataColumn = source.getDataColumn().getDBName();
			if (!plainColumn.getDBName().equals(dataColumn)) {
				log.info("Renaming column '" + plainColumn.getDBName() + "' of table '" + table.getName() + "' to '"
					+ dataColumn + "'.");
				sql.execute(modifyColumnName(table(tableName), plainColumn.getDBName(), DBType.BLOB, dataColumn));
			}
			columns.put(dataColumn, Boolean.valueOf(plainColumn.isSQLNotNull()));
		} else {
			source = (AbstractBinaryAttribute) sourceAttribute;
			for (DBAttribute column : source.getDbMapping()) {
				columns.put(column.getDBName(), Boolean.valueOf(column.isSQLNotNull()));
			}
		}

		List<DBAttribute> required = new ArrayList<>();
		addAll(required, source.getDbMapping());
		addAll(required, target.getDbMapping());
		for (DBAttribute column : required) {
			if (columns.containsKey(column.getDBName())) {
				continue;
			}
			DBColumn definition = SchemaSetup.createColumn(column);
			// Filled below, constraints are added after the content has been moved.
			definition.setMandatory(false);
			sql.execute(addColumn(table(tableName), definition, null));
			log.info("Created column '" + column.getDBName() + "' in table '" + table.getName() + "'.");
			columns.put(column.getDBName(), Boolean.FALSE);
		}

		for (DBAttribute column : target.getDbMapping()) {
			if (!column.isSQLNotNull() && columns.get(column.getDBName()).booleanValue()) {
				setNotNull(sql, tableName, column, false);
				columns.put(column.getDBName(), Boolean.FALSE);
			}
		}

		if (plainSource != null) {
			BinaryContentMigration.fillMissingMetadata(log, connection, table, source, null);
		}

		BinaryContentMigration.moveContent(log, connection, table, source, target, placement, null);

		for (DBAttribute column : source.getDbMapping()) {
			if (!contains(target.getDbMapping(), column.getDBName())) {
				sql.execute(dropColumn(table(tableName), column.getDBName()));
				log.info("Dropped column '" + column.getDBName() + "' of table '" + table.getName() + "'.");
			}
		}

		for (DBAttribute column : target.getDbMapping()) {
			if (column.isSQLNotNull() && !columns.get(column.getDBName()).booleanValue()) {
				setNotNull(sql, tableName, column, true);
			}
		}
	}

	private void migrateDynamic(MigrationContext context, Log log, PooledConnection connection,
			MORepository repository, MOStructure table) throws SQLException, IOException, MigrationException {
		Config<?> config = getConfig();
		String attributeName = config.getAttribute();

		MOStructure binaryTable = (MOStructure) repository.getTypeOrNull(AbstractFlexDataManager.FLEX_BINARY_DATA);
		if (binaryTable == null || !MoveFlexBinaryDataProcessor.exists(connection, binaryTable)) {
			log.info("No table of dynamic binary values, nothing to migrate for attribute '" + attributeName
				+ "' of table '" + table.getName() + "'.");
			return;
		}
		AbstractBinaryAttribute content =
			(AbstractBinaryAttribute) binaryTable.getAttribute(AbstractFlexDataManager.CONTENT);

		BinaryPlacement placement = new BinaryPlacement(config.getKind(), config.getStore(), config.getThreshold());

		Collection<TLID> typeIds = null;
		QualifiedTypeName tlType = config.getTLType();
		if (tlType != null) {
			Util util = context.getSQLUtils();
			Type modelType = util.getTLTypeOrFail(connection, tlType);
			typeIds = util.getTransitiveSpecializations(connection, modelType);
		}

		log.info("Migrating dynamic binary attribute '" + attributeName + "' of table '" + table.getName() + "'"
			+ (tlType != null ? " for objects of type '" + tlType.getName() + "'" : "") + " to " + placement + ".");
		for (MOStructure concreteTable : concreteTables(repository, table)) {
			SQLExpression filter = and(
				eqSQL(column(AbstractFlexDataManager.TYPE_DBNAME), literal(DBType.STRING, concreteTable.getName())),
				eqSQL(column(AbstractFlexDataManager.ATTRIBUTE_DBNAME), literal(DBType.STRING, attributeName)));
			if (typeIds != null) {
				MOAttribute typeRef = concreteTable.getAttributeOrNull(PersistentObject.TYPE_REF);
				if (!(typeRef instanceof MOReference)) {
					log.info("Table '" + concreteTable.getName() + "' has no type reference, skipping.", Log.WARN);
					continue;
				}
				String typeColumn = ((MOReference) typeRef).getColumn(ReferencePart.name).getDBName();
				filter = and(filter,
					inSetSelect(column(AbstractFlexDataManager.IDENTIFIER_DBNAME),
						selectDistinct(
							columns(columnDef(BasicTypes.IDENTIFIER_DB_NAME)),
							table(concreteTable.getDBMapping().getDBName()),
							inSet(column(typeColumn), typeIds, DBType.ID))));
			}
			BinaryContentMigration.moveContent(log, connection, binaryTable, content, content, placement, filter);
		}
	}

	/**
	 * Creates the declaration of a binary attribute of the given kind, taking over the name,
	 * database name and constraints of the given declaration.
	 */
	static AbstractBinaryAttribute.Config createConfig(AttributeConfig source, BinaryAttributeKind kind) {
		AbstractBinaryAttribute.Config result = TypedConfiguration.newConfigItem(kind.getConfigType());
		ConfigurationDescriptor sourceDescriptor = source.descriptor();
		for (PropertyDescriptor property : result.descriptor().getProperties()) {
			String propertyName = property.getPropertyName();
			if (property.isDerived() || NON_COPIED_PROPERTIES.contains(propertyName)) {
				continue;
			}
			PropertyDescriptor sourceProperty = sourceDescriptor.getProperty(propertyName);
			if (sourceProperty == null || !source.valueSet(sourceProperty)) {
				continue;
			}
			result.update(property, source.value(sourceProperty));
		}
		return result;
	}

	private static AbstractBinaryAttribute instantiate(AbstractBinaryAttribute.Config config) {
		return (AbstractBinaryAttribute) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	private static boolean isSame(BinaryPlacement current, BinaryPlacement target) {
		if (current.getKind() != target.getKind()) {
			return false;
		}
		if (target.getKind().isExternal() && !target.isTargetStore(current.getStoreName())) {
			return false;
		}
		return target.getKind() != BinaryAttributeKind.HYBRID || current.getThreshold() == target.getThreshold();
	}

	private static MetaObjectConfig tableConfig(SchemaConfiguration schema, String tableName) {
		MetaObjectName type = schema.getMetaObjects().getTypes().get(tableName);
		return type instanceof MetaObjectConfig ? (MetaObjectConfig) type : null;
	}

	private static int indexOf(List<AttributeConfig> attributes, String name) {
		for (int n = 0; n < attributes.size(); n++) {
			if (Objects.equals(attributes.get(n).getAttributeName(), name)) {
				return n;
			}
		}
		return -1;
	}

	/**
	 * All tables with rows that are sub tables of the given table, including the table itself.
	 */
	static List<MOStructure> concreteTables(MORepository repository, MOStructure table) {
		List<MOStructure> result = new ArrayList<>();
		for (MetaObject type : repository.getMetaObjects()) {
			if (!(type instanceof MOStructure)) {
				continue;
			}
			if (type instanceof MOClass && ((MOClass) type).isAbstract()) {
				continue;
			}
			if (type.isSubtypeOf(table)) {
				result.add((MOStructure) type);
			}
		}
		return result;
	}

	private static void setNotNull(SQLProcessor sql, String tableName, DBAttribute column, boolean notNull)
			throws SQLException {
		SQLModifyColumn modification =
			modifyColumnMandatory(table(tableName), column.getDBName(), column.getSQLType(), notNull);
		modification.setBinary(column.isBinary());
		modification.setSize(column.getSQLSize());
		modification.setPrecision(column.getSQLPrecision());
		sql.execute(modification);
	}

	private static void addAll(List<DBAttribute> result, DBAttribute[] columns) {
		for (DBAttribute column : columns) {
			result.add(column);
		}
	}

	private static boolean contains(DBAttribute[] columns, String name) {
		for (DBAttribute column : columns) {
			if (column.getDBName().equals(name)) {
				return true;
			}
		}
		return false;
	}

}
