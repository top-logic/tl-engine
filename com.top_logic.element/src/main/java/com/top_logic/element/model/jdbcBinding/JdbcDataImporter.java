/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.jdbcBinding;

import static com.top_logic.basic.db.sql.SQLFactory.*;
import static com.top_logic.basic.util.Utils.*;
import static com.top_logic.model.util.TLModelUtil.*;
import static java.util.Collections.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.top_logic.basic.AbortExecutionException;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.format.RegExpValueProvider;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.util.DBSchemaUtils;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.db.sql.SQLFactory;
import com.top_logic.basic.db.sql.SQLSelect;
import com.top_logic.basic.i18n.log.I18NLog;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.element.model.jdbcBinding.api.ColumnParser;
import com.top_logic.element.model.jdbcBinding.api.ImportRow;
import com.top_logic.element.model.jdbcBinding.api.KeyColumnNormalizer;
import com.top_logic.element.model.jdbcBinding.api.RowReader;
import com.top_logic.element.model.jdbcBinding.api.TypeSelector;
import com.top_logic.element.model.jdbcBinding.api.annotate.TLColumnBinding;
import com.top_logic.element.model.jdbcBinding.api.annotate.TLForeignKeyBinding;
import com.top_logic.element.model.jdbcBinding.api.annotate.TLForeignKeyBinding.KeyColumnConfig;
import com.top_logic.element.model.jdbcBinding.api.annotate.TLLinkTableBinding;
import com.top_logic.element.model.jdbcBinding.api.annotate.TLReverseForeignKeyBinding;
import com.top_logic.element.model.jdbcBinding.api.annotate.TLTableBinding;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.messagebox.ProgressDialog;
import com.top_logic.layout.messagebox.ProgressDialog.State;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelPartRef.ModuleRefValueProvider;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * {@link AbstractCommandHandler} importing a model from an existing database.
 * <p>
 * Uses the following {@link TLAnnotation annotations}:
 * <ul>
 * <li>TLTableBinding</li>
 * <li>TLColumnBinding</li>
 * <li>TLLinkTableBinding</li>
 * <li>TLForeignKeyBinding</li>
 * <li>TLReverseForeignKeyBinding</li>
 * </ul>
 * </p>
 * <p>
 * Limitations: The messages displayed in the log are not internationalized, yet.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class JdbcDataImporter extends AbstractCommandHandler {

	/** Used to report the progress and log messages. */
	public static class JdbcDataImporterProgressHandle {

		private final ProgressDialog _dialog;

		private final I18NLog _log;

		/** Creates a {@link JdbcDataImporterProgressHandle}. */
		public JdbcDataImporterProgressHandle(ProgressDialog dialog, I18NLog log) {
			_dialog = requireNonNull(dialog);
			_log = requireNonNull(log);
		}

		/** Increment the display progress by one step. */
		public void incrementProgress() {
			_dialog.incProgress(1);
		}

		/** Whether the user canceled the operation. */
		public boolean isCanceled() {
			return _dialog.getState() == State.CANCELED;
		}

		/** The operation log displayed to the user. */
		public I18NLog getLog() {
			return _log;
		}
	}

	private final class JdbcDataImporterProgressDialog extends ProgressDialog {

		private final String _poolName;

		private final TLModule _module;

		private JdbcDataImporterProgressDialog(ResKey title, DisplayDimension width, DisplayDimension height,
				String poolName, TLModule module) {
			super(title, width, height);
			_poolName = poolName;
			_module = module;
		}

		@Override
		protected int getStepCnt() {
			// There are two additional steps: Starting the import and requesting the database scheme.
			return 2 + countAttributes();
		}

		private int countAttributes() {
			return _module.getClasses().size();
		}

		@Override
		protected void run(I18NLog log) throws AbortExecutionException {
			log = logToGuiAndLogger(log);
			JdbcDataImporterProgressHandle progressHandle = new JdbcDataImporterProgressHandle(this, log);
			logInfo(progressHandle, "Import started.");
			progressHandle.incrementProgress();
			KBUtils.inTransaction(() -> {
				loadDataInTransaction(progressHandle, _poolName, _module);
				logInfo(progressHandle, "Comitting data.");
			});
			logInfo(progressHandle, "Import finished.");
		}

		private I18NLog logToGuiAndLogger(I18NLog log) {
			return (level, messageKey, exception) -> {
				log.log(level, messageKey, exception);
				wrapLog(level, messageKey, exception);
			};
		}

		private void wrapLog(Level level, ResKey messageKey, Throwable exception) {
			String message = Resources.getInstance(ResourcesModule.getLogLocale()).getString(messageKey);
			switch (level) {
				case FATAL: {
					Logger.fatal(message, exception, JdbcDataImporter.class);
					return;
				}
				case ERROR: {
					Logger.error(message, exception, JdbcDataImporter.class);
					return;
				}
				case WARN: {
					Logger.warn(message, exception, JdbcDataImporter.class);
					return;
				}
				case INFO: {
					Logger.info(message, exception, JdbcDataImporter.class);
					return;
				}
				case DEBUG: {
					Logger.debug(message, exception, JdbcDataImporter.class);
					return;
				}
			}
		}

	}

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

		/** A warning or error log message matching any of these Regex patterns is ignored, i.e. not logged. */
		@Name("knownProblems")
		@ListBinding(format = RegExpValueProvider.class, attribute = "regex", tag = "problem")
		List<Pattern> getKnownProblems();

	}

	private ImportContext _context;

	/**
	 * Creates a {@link JdbcDataImporter}.
	 */
	public JdbcDataImporter(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> arguments) {
		Config config = (Config) getConfig();
		String poolName = config.getPoolName();
		TLModule module = config.getModule().resolveModule();

		ResKey title = I18NConstants.PROGRESS_DIALOG_TITLE;
		DisplayDimension width = DisplayDimension.FIFTY_PERCENT;
		DisplayDimension height = DisplayDimension.FIFTY_PERCENT;
		return new JdbcDataImporterProgressDialog(title, width, height, poolName, module).open(context);
	}

	void loadDataInTransaction(JdbcDataImporterProgressHandle progressHandle, String poolName, TLModule module) {
		ConnectionPool pool = ConnectionPoolRegistry.getConnectionPool(poolName);
		PooledConnection connection = pool.borrowReadConnection();
		try {
			logInfo(progressHandle, "Requesting schema.");
			DBSchema schema = DBSchemaUtils.extractSchema(pool);
			logInfo(progressHandle, "Analyzing schema.");
			progressHandle.incrementProgress();
			if (progressHandle.isCanceled()) {
				throw new RuntimeException("Import aborted.");
			}
			List<Pattern> knownProblems = getConfigTyped().getKnownProblems();
			_context = new ImportContext(knownProblems, progressHandle, connection, schema, module);
			logInfo("Registering non-primary indices.");
			registerNonPrimaryIndices();
			logInfo("Importing data.");
			loadData();
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		} finally {
			_context = null;
			pool.releaseReadConnection(connection);
		}
	}

	private void logInfo(JdbcDataImporterProgressHandle progressHandle, String message) {
		progressHandle.getLog().info(ImportContext.toLogMessage(message));
	}

	private void loadData() throws SQLException {
		for (TLClass type : _context.getModule().getClasses()) {
			if (_context.getProgressHandle().isCanceled()) {
				return;
			}
			logInfo("Importing class: " + type.getName());
			TypeLoader loader = typeLoader(type);
			if (loader != null) {
				loader.load(_context.getConnection());
			}
			_context.getProgressHandle().incrementProgress();
		}
		logInfo("Finalizing import.");
		_context.resolve();
	}

	private void registerNonPrimaryIndices() {
		for (TLClass type : _context.getModule().getClasses()) {
			for (TLClassPart attribute : type.getAllClassParts()) {
				TLForeignKeyBinding foreignKeyBinding = attribute.getAnnotation(TLForeignKeyBinding.class);
				if (foreignKeyBinding == null) {
					continue;
				}
				if (!usesNonPrimaryIndex(foreignKeyBinding.getColumnConfigs())) {
					continue;
				}
				List<String> indexColumns = getIndexColumns(foreignKeyBinding.getColumns(), foreignKeyBinding.getColumnConfigs());
				registerNonPrimaryIndex(attribute, indexColumns);
			}
		}
	}

	private void registerNonPrimaryIndex(TLClassPart attribute, List<String> indexColumns) {
		TLTableBinding tableBinding = attribute.getType().getAnnotation(TLTableBinding.class);
		if (tableBinding == null) {
			errorMissingTableBindingForNonPrimaryIndex(attribute, indexColumns);
			return;
		}
		String targetTable = tableBinding.getName();
		_context.addNonPrimaryIndex(targetTable, indexColumns);
	}

	private void errorMissingTableBindingForNonPrimaryIndex(TLStructuredTypePart attribute, List<String> indexColumns) {
		_context.logError("Missing " + TLTableBinding.class.getSimpleName() + " for type: "
			+ qualifiedName(attribute.getType()) + ". There is a non-primary foreign key index in "
			+ qualifiedName(attribute) + " to it. Index: " + indexColumns);
	}

	interface TypeLoader {
		void load(PooledConnection connection) throws SQLException;
	}

	private TypeLoader typeLoader(TLClass type) {
		TLTableBinding tableBinding = type.getAnnotation(TLTableBinding.class);
		if (tableBinding == null) {
			/* This is severity "info", not "warning", as that is normal: Not every type is
			 * imported. */
			logInfo("No table binding for type " + qualifiedName(type) + ". Type will not be imported.");
			return null;
		}

		String tableName = tableBinding.getName();
		DBTable table = _context.getSchema().getTable(tableName);
		if (table == null) {
			logErrorTableMissing(type, tableName);
			return null;
		}

		TypeSelector typeSelector = typeSelector(type, tableBinding);

		List<String> primaryKey = tableBinding.getPrimaryKey();

		Set<String> columns = new LinkedHashSet<>();
		Map<Object, TLObject> typeIndex = _context.typeIndex(tableName);
		List<RowReader> readers = new ArrayList<>();
		List<TypeLoader> subLoaders = new ArrayList<>();
		for (TLStructuredTypePart property : type.getAllParts()) {
			if (_context.getProgressHandle().isCanceled()) {
				throw new RuntimeException("Import aborted.");
			}
			TLColumnBinding columnBinding = property.getAnnotation(TLColumnBinding.class);
			if (columnBinding != null) {
				columns.add(columnBinding.getName());
				readers.add(propertyLoader(property, columnBinding));
				continue;
			}

			TLForeignKeyBinding foreignKeyBinding = property.getAnnotation(TLForeignKeyBinding.class);
			if (foreignKeyBinding != null) {
		
				TLType targetType = property.getType();
				TLTableBinding targetBinding = targetType.getAnnotation(TLTableBinding.class);
				if (targetBinding == null) {
					logErrorMissingTableBindingOnTarget(property, targetType);
				} else {
					String targetTableName = targetBinding.getName();
					List<String> keyColumns = foreignKeyBinding.getColumns();
					Map<String, KeyColumnConfig> columnConfigs = foreignKeyBinding.getColumnConfigs();
					readers.add(foreinKeyLoader(property, keyColumns, columnConfigs, targetTableName));

					for (String keyColumnName : keyColumns) {
						columns.add(keyColumnName);
					}
				}
				continue;
			}
			
			TLLinkTableBinding linkBinding = property.getAnnotation(TLLinkTableBinding.class);
			if (linkBinding != null) {
				TLReference reference = (TLReference) property;

				List<String> sourceKey = linkBinding.getSourceColumns();
				List<String> destinationKey = linkBinding.getDestinationColumns();
				
				List<SQLColumnDefinition> linkColumns = new ArrayList<>();
				linkColumns.addAll(columnDefs(sourceKey));
				linkColumns.addAll(columnDefs(destinationKey));
				String orderColumn = linkBinding.getOrderColumn();
				boolean isOrdered = orderColumn != null && reference.isOrdered();
				if (isOrdered) {
					linkColumns.add(columnDef(orderColumn));
				}
				SQLSelect select = select(linkColumns, table(linkBinding.getLinkTable()));
				CompiledStatement linkRows = query(select).toSql(_context.getSqlDialect());
				
				TLType targetType = reference.getType();
				TLTableBinding targetBinding = targetType.getAnnotation(TLTableBinding.class);
				if (targetBinding == null) {
					logErrorMissingTableBindingOnTarget(reference, targetType);
				} else {
					String destTableName = targetBinding.getName();
					Map<Object, TLObject> destIndex = _context.typeIndex(destTableName);
					subLoaders.add((PooledConnection connection) -> {
						try (ResultSet links = linkRows.executeQuery(connection)) {
							ImportRow linkCursor = cursor(links);
							while (links.next()) {
								Object sourceId = readId(linkCursor, sourceKey);
								Object destId = readId(linkCursor, destinationKey);

								if (isOrdered) {
									Object orderValue = linkCursor.getValue(orderColumn);
									_context.reference(tableName, sourceId, reference)
										.addOrderedDeferred(destIndex, destId, orderValue);
								} else {
									_context.addResolver(() -> {
										TLObject source = typeIndex.get(sourceId);
										if (source == null) {
											logErrorLinkTableUnknownSourceId(reference, sourceId, destId);
										} else {
											TLObject dest = destIndex.get(destId);
											if (dest == null) {
												logErrorLinkTableUnknownTargetId(reference, sourceId, destId);
											} else if (((Collection<?>) source.tValue(reference)).contains(dest)) {
												logWarnDuplicateCollectionReferenceEntry(source, reference, dest);
											} else {
												source.tAdd(reference, dest);
											}
										}
									});
								}
							}
						}
					});
				}
				continue;
			}
			if (property.isDerived()) {
				/* Check this after building the import bindings, to make sure an import binding on
				 * a derived attribute causes an error which notifies the developer of the
				 * problem. */
				continue;
			}
			logInfo("No import binding for attribute " + qualifiedName(property) + ". Attribute will not be imported.");
		}

		// All references with the currently imported type as target type.
		for (TLObject referenceAttribute : _context.referrers(type)) {
			if (referenceAttribute instanceof TLAssociationEnd) {
				continue;
			}
			if (referenceAttribute instanceof TLReference) {
				TLReference reference = (TLReference) referenceAttribute;
				TLReverseForeignKeyBinding reverseForeignKeyBinding =
					reference.getAnnotation(TLReverseForeignKeyBinding.class);
				if (reverseForeignKeyBinding != null) {
					TLClass sourceType = reference.getOwner();

					TLTableBinding sourceBinding = sourceType.getAnnotation(TLTableBinding.class);
					if (sourceBinding == null) {
						logErrorMissingTableBindingOnTarget(reference, sourceType);
					} else {
						String sourceTableName = sourceBinding.getName();

						List<String> targetColumns = reverseForeignKeyBinding.getTargetColumns();
						if (targetColumns.isEmpty()) {
							logErrorNoTargetColumnsInReverseKey(reference, sourceType);
							continue;
						}
						String orderColumn = reverseForeignKeyBinding.getOrderColumn();

						boolean multiple = reference.isMultiple();
						if ((orderColumn != null) && !multiple) {
							logErrorOrderForSingleReference(reference, sourceType);
						}
						if ((orderColumn != null) && !reference.isOrdered()) {
							logErrorOrderForUnorderedReference(reference, sourceType);
						}

						columns.addAll(targetColumns);
						if (orderColumn != null) {
							columns.add(orderColumn);

							readers.add((TLObject target, ImportRow cursor) -> {
								Object idRef = readId(cursor, targetColumns);
								if (idRef != null) {
									Object orderValue = cursor.getValue(orderColumn);
									_context.reference(sourceTableName, idRef, reference).addOrdered(target, orderValue);
								}
							});
						} else {
							Map<Object, TLObject> sourceIndex = _context.typeIndex(sourceTableName);

							readers.add((TLObject target, ImportRow cursor) -> {
								Object idRef = readId(cursor, targetColumns);
								if (idRef != null) {
									_context.addResolver(() -> {
										TLObject source = sourceIndex.get(idRef);
										if (source == null) {
											logErrorReverseLinkUnknownSourceId(reference, idRef, target);
										} else {
											if (multiple) {
												source.tAdd(reference, target);
											} else {
												Object oldTarget = source.tValue(reference);
												if (oldTarget != null) {
													logErrorReverseLinkValueClash(reference, source, oldTarget, target);
												} else {
													source.tUpdate(reference, target);
												}
											}
										}
									});
								}
							});
						}
					}
				}
			} else {
				logErrorReferenceNoTLReference(type, referenceAttribute);
			}
		}
		RowReader rowReader = TypedConfigUtil.createInstance(tableBinding.getRowReader());
		if (rowReader != null) {
			/* This is an optional post-processing of the imported rows. No else-block is needed. */
			readers.add(rowReader);
		}

		SQLSelect select = select(columnDefs(columns), table(tableName));
		CompiledStatement allRows = query(select).toSql(_context.getSqlDialect());
		TLFactory factory = _context.getFactory();

		return (PooledConnection connection) -> {
			try (ResultSet resultSet = allRows.executeQuery(connection)) {
				ImportRow cursor = cursor(resultSet);
				while (resultSet.next()) {
					TLClass createType = calcType(typeSelector, cursor);
					if (createType == null) {
						continue;
					}
					TLObject object = factory.createObject(createType);

					for (RowReader loader : readers) {
						try {
							loader.read(object, cursor);
						} catch (RuntimeException exception) {
							logError("Failed to read row. Reader: " + loader + ". Row: " + resultSet, exception);
						}
					}

					if (!primaryKey.isEmpty()) {
						Object id = readId(cursor, primaryKey);
						if (id != null) {
							typeIndex.put(id, object);
						} else {
							logError("Primary key is null for object of type " + qualifiedName(createType) + ": " + object);
						}
					}
					for (List<String> index : _context.getNonPrimaryIndices(tableName)) {
						Object id = readId(cursor, index);
						if (id != null) {
							_context.typeBySecondaryIndex(tableName, index).put(id, object);
						} else {
							logWarn("Non-Primary key is null for object of type " + qualifiedName(createType) + ": " + object);
						}
					}
				}
			}

			for (TypeLoader subLoader : subLoaders) {
				subLoader.load(connection);
			}
		};
	}

	private List<SQLColumnDefinition> columnDefs(Collection<String> columns) {
		return columns.stream().map(SQLFactory::columnDef).collect(Collectors.toList());
	}

	private TypeSelector typeSelector(TLClass type, TLTableBinding tableBinding) {
		TypeSelector typeSelector = TypedConfigUtil.createInstance(tableBinding.getTypeSelector());
		if (typeSelector == null) {
			typeSelector = momomorphicTypeSelector(type);
		}
		return typeSelector;
	}

	private TLClass calcType(TypeSelector typeSelector, ImportRow cursor) {
		try {
			return typeSelector.getType(cursor);
		} catch (RuntimeException exception) {
			logError("Failed to determine TLClass for row. Type selector: " + typeSelector, exception);
			return null;
		}
	}

	private RowReader foreinKeyLoader(TLStructuredTypePart property, List<String> columns,
			Map<String, KeyColumnConfig> columnConfigs, String targetTableName) {
		Map<Object, TLObject> targetIndex = getTargetIndex(columns, columnConfigs, targetTableName);
		Map<String, KeyColumnNormalizer> normalizers = getNormalizers(columnConfigs);

		return (TLObject object, ImportRow cursor) -> {
			Object idRef = readId(cursor, columns, normalizers);
			if (idRef != null) {
				_context.addResolver(() -> {
					TLObject target = targetIndex.get(idRef);
					if (target == null) {
						logErrorLinkUnknownTargetId(property, object, idRef);
					} else {
						object.tUpdate(property, target);
					}
				});
			}
		};
	}

	private Map<String, KeyColumnNormalizer> getNormalizers(Map<String, KeyColumnConfig> columnConfigs) {
		Map<String, KeyColumnNormalizer> map = new HashMap<>();
		for (KeyColumnConfig config : columnConfigs.values()) {
			PolymorphicConfiguration<KeyColumnNormalizer> normalizer = config.getNormalizer();
			if (normalizer != null) {
				map.put(config.getSourceColumn(), TypedConfigUtil.createInstance(normalizer));
			}
		}
		return map;
	}

	private Map<Object, TLObject> getTargetIndex(List<String> columns,
			Map<String, KeyColumnConfig> columnConfigs, String targetTableName) {
		if (usesNonPrimaryIndex(columnConfigs)) {
			return _context.typeBySecondaryIndex(targetTableName, getIndexColumns(columns, columnConfigs));
		}
		return _context.typeIndex(targetTableName);
	}

	private boolean usesNonPrimaryIndex(Map<String, KeyColumnConfig> columnConfigs) {
		if (columnConfigs.isEmpty()) {
			return false;
		}
		for (KeyColumnConfig config : columnConfigs.values()) {
			if (config.getTargetColumn() != null) {
				return true;
			}
		}
		return false;
	}

	private List<String> getIndexColumns(List<String> columns, Map<String, KeyColumnConfig> columnConfigs) {
		List<String> index = new ArrayList<>();
		for (String column : columns) {
			KeyColumnConfig config = columnConfigs.get(column);
			if (config == null || config.getTargetColumn().isEmpty()) {
				index.add(column);
			} else {
				index.add(config.getTargetColumn());
			}
		}
		return index;
	}

	private RowReader propertyLoader(TLStructuredTypePart property, TLColumnBinding columnBinding) {
		String columnName = columnBinding.getName();
		ColumnParser columnParser = columnParser(columnBinding);

		return (TLObject object, ImportRow cursor) -> {
			Object columnValue = cursor.getValue(columnName);
			Object value = parseValue(property, columnParser, columnValue);

			object.tUpdate(property, value);
		};
	}

	private Object parseValue(TLStructuredTypePart property, ColumnParser parser, Object value) {
		try {
			return parser.getApplicationValue(value);
		} catch (RuntimeException exception) {
			logError("Failed to parse value: '" + value + "'. Parser: " + parser
				+ ". Attribute: " + qualifiedName(property), exception);
			return null;
		}
	}

	private ColumnParser columnParser(TLColumnBinding columnBinding) {
		ColumnParser customParser = TypedConfigUtil.createInstance(columnBinding.getParser());
		if (customParser != null) {
			return customParser;
		}
		return value -> value;
	}

	private Object readId(ImportRow cursor, List<String> columns) {
		return readId(cursor, columns, emptyMap());
	}

	private Object readId(ImportRow cursor, List<String> columns, Map<String, KeyColumnNormalizer> normalizers) {
		int keySize = columns.size();
		List<Object> result = new ArrayList<>(keySize);
		boolean onlyNulls = true;
		for (String columnName : columns) {
			Object rawValue = cursor.getValue(columnName);
			Object value = normalizeKeyColumn(normalizers.get(columnName), rawValue);
			/* The value might be null. That is okay. That means either that reference is null. Or,
			 * it can be that a part of the key is null. That does not imply that the key is
			 * invalid. In a key consisting of multiple columns, some columns can sometimes be null.
			 * Example: A person could be identified by their first, middle and last name. But some
			 * people have no middle name. That column would be null in that case. */
			onlyNulls &= (value == null);
			result.add(value);
		}
		return onlyNulls ? null : result;
	}

	private Object normalizeKeyColumn(KeyColumnNormalizer normalizer, Object value) {
		if (normalizer == null) {
			return value;
		}
		try {
			return normalizer.normalize(value);
		} catch (RuntimeException exception) {
			logError("Failed to normalize value: '" + value + "'. Normalizer: " + normalizer);
			return null;
		}
	}

	private ImportRow cursor(ResultSet resultSet) {
		return column -> {
			try {
				return resultSet.getObject(column);
			} catch (SQLException exception) {
				throw new RuntimeException("Failed to read column '" + column + "'.", exception);
			}
		};
	}

	private TypeSelector momomorphicTypeSelector(TLClass type) {
		return row -> type;
	}

	private void logErrorTableMissing(TLType type, String tableName) {
		logError("Type " + qualifiedName(type) + " can not be imported, as its source SQL table '" + tableName + "' is missing.");
	}

	private void logErrorReferenceNoTLReference(TLType target, TLObject referenceAttribute) {
		logError("Reference " + label(referenceAttribute) + " is not a " + TLReference.class.getSimpleName()
			+ ", but refers to type " + qualifiedName(target));
	}

	private void logErrorMissingTableBindingOnTarget(TLStructuredTypePart reference, TLType targetType) {
		logError("Reference " + qualifiedName(reference) + " can not be imported. The target type "
			+ qualifiedName(targetType) + " has no " + TLTableBinding.class.getSimpleName() + " annotation.");
	}

	private void logErrorNoTargetColumnsInReverseKey(TLReference reference, TLClass sourceType) {
		logError("Reference " + qualifiedName(reference) + ": The " + TLReverseForeignKeyBinding.class.getSimpleName()
			+ " annotation on its target type " + qualifiedName(sourceType) + " does not specify key columns.");
	}

	private void logErrorOrderForSingleReference(TLReference reference, TLClass sourceType) {
		logError("Reference " + qualifiedName(reference) + ": The " + TLReverseForeignKeyBinding.class.getSimpleName()
			+ " annotation on its target type " + qualifiedName(sourceType) + " specifies an order column,"
			+ " but it is a single value reference.");
	}

	private void logErrorOrderForUnorderedReference(TLReference reference, TLClass sourceType) {
		logError("Reference " + qualifiedName(reference) + ": The " + TLReverseForeignKeyBinding.class.getSimpleName()
			+ " annotation on its target type " + qualifiedName(sourceType) + " specifies an order column,"
			+ " but the reference is not ordered.");
	}

	private void logErrorLinkTableUnknownSourceId(TLReference reference, Object sourceId, Object targetId) {
		logError("Reference " + qualifiedName(reference) + ": There is no object for source id: " + sourceId
			+ ". Target id: " + targetId);
	}

	private void logErrorLinkTableUnknownTargetId(TLReference reference, Object sourceId, Object targetId) {
		logError("Reference " + qualifiedName(reference) + ": There is no object for target id: " + targetId
			+ ". Source id: " + sourceId);
	}

	private void logErrorReverseLinkUnknownSourceId(TLReference reference, Object sourceId, TLObject target) {
		logError("Reference " + qualifiedName(reference) + ": There is no object for source id: " + sourceId
			+ ". Target: " + label(target));
	}

	private void logErrorLinkUnknownTargetId(TLStructuredTypePart reference, TLObject source, Object targetId) {
		logError("Reference " + qualifiedName(reference) + ": There is no object for target id: " + targetId
			+ ". Source: " + label(source));
	}

	private void logErrorReverseLinkValueClash(TLReference reference, Object source, Object oldTarget, Object newTarget) {
		logError("Reference " + qualifiedName(reference) + ": This is a single value reference."
			+ " But object '" + label(source) + "' references two objects: '" + label(oldTarget) + "' and: '"
			+ label(newTarget));
	}

	private void logWarnDuplicateCollectionReferenceEntry(TLObject source, TLReference reference, TLObject target) {
		logWarn("Duplicate entry in collection-valued reference. Source: " + source
			+ ". Reference: " + reference.getName()
			+ ". Duplicate entry: " + target
			+ ". Existing entries: " + source.tValue(reference));
	}

	private String label(Object object) {
		return MetaLabelProvider.INSTANCE.getLabel(object);
	}

	private void logError(String message) {
		_context.logError(message);
	}

	private void logError(String message, Throwable exception) {
		_context.logError(message, exception);
	}

	private void logWarn(String message) {
		_context.logWarn(message);
	}

	private void logInfo(String message) {
		_context.logInfo(message);
	}

	private Config getConfigTyped() {
		return (Config) getConfig();
	}

}
