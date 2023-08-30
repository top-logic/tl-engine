/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.element.model.jdbcBinding;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.util.DBSchemaUtils;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.db.sql.SQLFactory;
import com.top_logic.basic.db.sql.SQLSelect;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.element.model.jdbcBinding.api.ColumnParser;
import com.top_logic.element.model.jdbcBinding.api.ImportRow;
import com.top_logic.element.model.jdbcBinding.api.RowReader;
import com.top_logic.element.model.jdbcBinding.api.TypeSelector;
import com.top_logic.element.model.jdbcBinding.api.annotate.TLColumnBinding;
import com.top_logic.element.model.jdbcBinding.api.annotate.TLForeignKeyBinding;
import com.top_logic.element.model.jdbcBinding.api.annotate.TLReverseForeignKeyBinding;
import com.top_logic.element.model.jdbcBinding.api.annotate.TLTableBinding;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelPartRef.ModuleRefValueProvider;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link AbstractCommandHandler} importing a model from an existing database.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class JdbcDataImporter extends AbstractCommandHandler {

	/**
	 * Configuration options for {@link JdbcDataImporter}.
	 */
	public interface Config extends AbstractCommandHandler.Config {

		/**
		 * Name of the connection pool to import data from.
		 */
		@Mandatory
		@Name("poolName")
		String getPoolName();

		/**
		 * The {@link TLModule} that defines the types to import.
		 */
		@Mandatory
		@Format(ModuleRefValueProvider.class)
		TLModelPartRef getModule();

	}

	/**
	 * Creates a {@link JdbcDataImporter}.
	 */
	public JdbcDataImporter(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {

		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			loadData();

			tx.commit();
		}

		return HandlerResult.DEFAULT_RESULT;
	}

	private void loadData() {
		Config config = (Config) getConfig();
		String poolName = config.getPoolName();
		ConnectionPool pool = ConnectionPoolRegistry.getConnectionPool(poolName);
		PooledConnection connection = pool.borrowReadConnection();
		try {
			DBSchema schema = DBSchemaUtils.extractSchema(pool);
			TLModule module = config.getModule().resolveModule();

			loadData(new ImportContext(connection, schema, module));
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		} finally {
			pool.releaseReadConnection(connection);
		}
	}

	private void loadData(ImportContext context) throws SQLException {
		for (TLType type : context.getModule().getTypes()) {
			TypeLoader loader =
				typeLoader(context, type);

			if (loader != null) {
				loader.load(context.getConnection());
			}
		}

		context.resolve();
	}

	interface TypeLoader {
		void load(PooledConnection connection) throws SQLException;
	}

	private TypeLoader typeLoader(ImportContext context, TLType type) {
		TLTableBinding tableBinding = type.getAnnotation(TLTableBinding.class);
		if (tableBinding == null) {
			return null;
		}

		String tableName = tableBinding.getName();
		DBTable table = context.getSchema().getTable(tableName);
		if (table == null) {
			// TODO: Warning.
			return null;
		}

		TypeSelector typeSelector = typeSelector((TLClass) type, tableBinding);

		List<String> primaryKey = tableBinding.getPrimaryKey();

		Set<String> columns = new LinkedHashSet<>();
		Map<Object, TLObject> typeIndex = context.typeIndex(tableName);
		List<RowReader> readers = new ArrayList<>();
		for (TLStructuredTypePart property : ((TLClass) type).getAllParts()) {
			TLColumnBinding columnBinding = property.getAnnotation(TLColumnBinding.class);
			if (columnBinding != null) {
				columns.add(columnBinding.getName());
				readers.add(propertyLoader(property, columnBinding));
			}
		
			TLForeignKeyBinding foreignKeyBinding = property.getAnnotation(TLForeignKeyBinding.class);
			if (foreignKeyBinding != null) {
				List<String> keyColumns = foreignKeyBinding.getColumns();
		
				TLType targetType = property.getType();
				TLTableBinding targetBinding = targetType.getAnnotation(TLTableBinding.class);
				if (targetBinding == null) {
					// TODO: Warning.
				} else {
					String targetTableName = targetBinding.getName();
					readers.add(foreinKeyLoader(context, property, keyColumns, targetTableName));

					for (String keyColumnName : keyColumns) {
						columns.add(keyColumnName);
					}
				}
			}
		}

		// All references with the currently imported type as target type.
		for (TLObject referenceObject : context.referers(type)) {
			if (referenceObject instanceof TLReference) {
				TLReference reference = (TLReference) referenceObject;
				TLReverseForeignKeyBinding reverseForeignKeyBinding =
					reference.getAnnotation(TLReverseForeignKeyBinding.class);
				if (reverseForeignKeyBinding != null) {
					TLClass sourceType = reference.getOwner();

					TLTableBinding sourceBinding = sourceType.getAnnotation(TLTableBinding.class);
					if (sourceBinding == null) {
						// TODO: Warning.
					} else {
						String sourceTableName = sourceBinding.getName();

						List<String> targetColumns = reverseForeignKeyBinding.getTargetColumns();
						String orderColumn = reverseForeignKeyBinding.getOrderColumn();

						boolean multiple = reference.isMultiple();

						columns.addAll(targetColumns);
						if (multiple && reference.isOrdered() && orderColumn != null) {
							columns.add(orderColumn);

							readers.add((TLObject target, ImportRow cursor) -> {
								Object idRef = readId(cursor, targetColumns);
								if (idRef != null) {
									Object orderValue = cursor.getValue(orderColumn);
									context.reference(sourceTableName, idRef, reference).addOrdered(target, orderValue);
								}
							});
						} else {
							Map<Object, TLObject> sourceIndex = context.typeIndex(sourceTableName);

							readers.add((TLObject target, ImportRow cursor) -> {
								Object idRef = readId(cursor, targetColumns);
								if (idRef != null) {
									context.addResolver(() -> {
										TLObject source = sourceIndex.get(idRef);
										if (source == null) {
											// TODO: Warning.
										} else {
											if (multiple) {
												source.tAdd(reference, target);
											} else {
												source.tUpdate(reference, target);
											}
										}
									});
								}
							});
						}
					}

				}
			}
		}

		RowReader rowReader = TypedConfigUtil.createInstance(tableBinding.getRowReader());
		if (rowReader != null) {
			readers.add(rowReader);
		}

		List<SQLColumnDefinition> selectColumns =
			columns.stream().map(SQLFactory::columnDef).collect(Collectors.toList());
		SQLSelect select = select(selectColumns, table(tableName));
		CompiledStatement allRows = query(select).toSql(context.getSqlDialect());
		TLFactory factory = context.getFactory();

		return (PooledConnection connection) -> {
			try (ResultSet resultSet = allRows.executeQuery(connection)) {
				ImportRow cursor = cursor(resultSet);
				while (resultSet.next()) {
					TLClass createType = typeSelector.getType(cursor);
					TLObject object = factory.createObject(createType);

					for (RowReader loader : readers) {
						loader.read(object, cursor);
					}

					if (!primaryKey.isEmpty()) {
						Object id = readId(cursor, primaryKey);
						if (id == null) {
							// TODO: Warning.
						} else {
							typeIndex.put(id, object);
						}
					}
				}
			}
		};
	}

	private TypeSelector typeSelector(TLClass type, TLTableBinding tableBinding) {
		TypeSelector typeSelector = TypedConfigUtil.createInstance(tableBinding.getTypeSelector());
		if (typeSelector == null) {
			typeSelector = momomorphicTypeSelector(type);
		}
		return typeSelector;
	}

	private RowReader foreinKeyLoader(ImportContext context, TLStructuredTypePart property, List<String> keyColumns,
			String targetTableName) {
		Map<Object, TLObject> targetIndex = context.typeIndex(targetTableName);

		return (TLObject object, ImportRow cursor) -> {
			Object idRef = readId(cursor, keyColumns);
			if (idRef != null) {
				context.addResolver(() -> {
					TLObject target = targetIndex.get(idRef);
					if (target == null) {
						// TODO: Warning.
					} else {
						object.tUpdate(property, target);
					}
				});
			}
		};
	}

	private RowReader propertyLoader(TLStructuredTypePart property, TLColumnBinding columnBinding) {
		String columnName = columnBinding.getName();
		ColumnParser columnParser = columnParser(columnBinding);

		return (TLObject object, ImportRow cursor) -> {
			Object columnValue = cursor.getValue(columnName);
			Object value = columnParser.getApplicationValue(columnValue);

			object.tUpdate(property, value);
		};
	}

	private ColumnParser columnParser(TLColumnBinding columnBinding) {
		ColumnParser customParser = TypedConfigUtil.createInstance(columnBinding.getParser());
		if (customParser != null) {
			return customParser;
		}

		return value -> value;
	}

	private Object readId(ImportRow cursor, List<String> columns) {
		int keySize = columns.size();
		Object[] array = new Object[keySize];
		for (int n = 0; n < keySize; n++) {
			Object value = cursor.getValue(columns.get(n));
			if (value == null) {
				return null;
			}
			array[n] = value;
		}
		return Arrays.asList(array);
	}

	private ImportRow cursor(ResultSet resultSet) {
		return column -> {
			try {
				return resultSet.getObject(column);
			} catch (SQLException ex) {
				throw new RuntimeException(ex);
			}
		};
	}

	private TypeSelector momomorphicTypeSelector(TLClass type) {
		return (row) -> type;
	}

}
