/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration.processors;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.blob.BlobBinaryData;
import com.top_logic.basic.io.blob.BlobUpload;
import com.top_logic.basic.mime.MimeTypesModule;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.AbstractBinaryAttribute;
import com.top_logic.dob.attr.storage.BinaryColumnsStorage;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.objects.DCMetaData;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.knowledge.wrap.Document;

/**
 * {@link MigrationProcessor} storing the content of documents, which is held in a document
 * repository, in the binary content attribute of the documents.
 *
 * <p>
 * A document row references its content in the repository by its physical resource (the data
 * source name of the document in the repository) and its version number. For every row of the
 * document table, including all historic revisions, the content of that version is read from the
 * repository and stored in the content attribute of the same row. The content is placed inline or
 * in a blob store according to the declaration of the content attribute. The stored name is the
 * name of the row, the content type is the Dublin Core format of the row, or the type derived from
 * the name if the row has no format.
 * </p>
 *
 * <p>
 * Rows that already have content, rows without content version (if the repository is versioned)
 * and rows whose physical resource is no document of the repository are left unchanged. Content
 * missing in the repository is reported as warning and leaves the row without content.
 * </p>
 *
 * <p>
 * The repository is accessed through the configured reader, or through the reader of the
 * application configuration {@link DocumentRepositoryConfig}.
 * </p>
 *
 * <p>
 * The content attribute must be declared in the stored schema and its columns must exist when the
 * processor runs.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MigrateDocumentContentProcessor
		extends AbstractConfiguredInstance<MigrateDocumentContentProcessor.Config<?>>
		implements MigrationProcessor {

	/**
	 * Separator between the protocol and the path in a data source name.
	 */
	public static final String PROTOCOL_SEPARATOR = "://";

	/**
	 * Name of the attribute of the stored schema holding the data source name of a document.
	 */
	public static final String PHYSICAL_RESOURCE = "physicalResource";

	/**
	 * Number of processed rows after which progress is logged.
	 */
	private static final int LOG_INTERVAL = 10000;

	/**
	 * Configuration options for {@link MigrateDocumentContentProcessor}.
	 */
	@TagName(Config.TAG_NAME)
	@DisplayOrder({ Config.TABLE, Config.ATTRIBUTE, Config.PROTOCOL, Config.READER })
	public interface Config<I extends MigrateDocumentContentProcessor> extends PolymorphicConfiguration<I> {

		/** Tag name of a {@link MigrateDocumentContentProcessor} in a migration script. */
		String TAG_NAME = "migrate-document-content";

		/** Configuration name of {@link #getReader()}. */
		String READER = "reader";

		/** Configuration name of {@link #getTable()}. */
		String TABLE = "table";

		/** Configuration name of {@link #getAttribute()}. */
		String ATTRIBUTE = "attribute";

		/** Configuration name of {@link #getProtocol()}. */
		String PROTOCOL = "protocol";

		/** Default value of {@link #getProtocol()}. */
		String DEFAULT_PROTOCOL = "repository";

		/**
		 * Access to the repository holding the document content.
		 *
		 * <p>
		 * If not given, the reader of the application configuration {@link DocumentRepositoryConfig}
		 * is used.
		 * </p>
		 */
		@Name(READER)
		PolymorphicConfiguration<? extends RepositoryContentReader> getReader();

		/**
		 * The name of the table of the documents.
		 */
		@Name(TABLE)
		@StringDefault(Document.OBJECT_NAME)
		String getTable();

		/**
		 * The name of the binary attribute to store the content in.
		 */
		@Name(ATTRIBUTE)
		@StringDefault(Document.CONTENT)
		String getAttribute();

		/**
		 * The protocol of the data source names of documents in the repository.
		 *
		 * <p>
		 * Rows whose physical resource has another protocol are not migrated.
		 * </p>
		 */
		@Name(PROTOCOL)
		@StringDefault(DEFAULT_PROTOCOL)
		String getProtocol();

	}

	private final RepositoryContentReader _reader;

	/**
	 * Creates a {@link MigrateDocumentContentProcessor} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MigrateDocumentContentProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
		PolymorphicConfiguration<? extends RepositoryContentReader> readerConfig = config.getReader();
		if (readerConfig == null) {
			readerConfig = ApplicationConfig.getInstance().getConfig(DocumentRepositoryConfig.class).getReader();
		}
		_reader = context.getInstance(readerConfig);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		Config<?> config = getConfig();
		String tableName = config.getTable();

		if (_reader == null) {
			log.error("No reader for the document repository configured, see '"
				+ DocumentRepositoryConfig.class.getName() + "'.");
			return;
		}

		MORepository repository = context.getPersistentRepository();
		MetaObject type = repository.getTypeOrNull(tableName);
		if (!(type instanceof MOStructure)) {
			log.error("No table '" + tableName + "' for migrating document content.");
			return;
		}
		MOStructure table = (MOStructure) type;
		MOAttribute content = table.getAttributeOrNull(config.getAttribute());
		if (!(content instanceof AbstractBinaryAttribute)) {
			log.error("Table '" + tableName + "' has no binary attribute '" + config.getAttribute() + "'.");
			return;
		}

		try {
			migrate(log, connection, table, (AbstractBinaryAttribute) content);
		} catch (SQLException | IOException | RuntimeException ex) {
			log.error("Failed to migrate the document content of table '" + tableName + "': " + ex.getMessage(), ex);
		}
	}

	private void migrate(Log log, PooledConnection connection, MOStructure table, AbstractBinaryAttribute content)
			throws SQLException, IOException {
		DBHelper sqlDialect = connection.getSQLDialect();
		String tableName = table.getDBMapping().getDBName();
		List<DBAttribute> keyColumns = BinaryContentMigration.keyColumns(table);
		BinaryPlacement placement = BinaryPlacement.of(content);
		String protocolPrefix = getConfig().getProtocol() + PROTOCOL_SEPARATOR;

		List<SQLColumnDefinition> columns = new ArrayList<>();
		for (DBAttribute keyColumn : keyColumns) {
			columns.add(columnDef(keyColumn.getDBName()));
		}
		int resourceIndex = addColumn(columns, table, PHYSICAL_RESOURCE);
		int versionIndex = addColumn(columns, table, Document.VERSION_NUMBER);
		int nameIndex = addColumn(columns, table, Document.NAME_ATTRIBUTE);
		int formatIndex = addColumn(columns, table, DCMetaData.FORMAT);
		CompiledStatement select = query(
			select(columns, table(tableName),
				isNull(column(content.getSizeColumn().getDBName())))).toSql(sqlDialect);

		DBAttribute[] targetColumns = content.getDbMapping();
		StringBuilder update = new StringBuilder();
		update.append("UPDATE ").append(sqlDialect.tableRef(tableName)).append(" SET ");
		for (int n = 0; n < targetColumns.length; n++) {
			if (n > 0) {
				update.append(", ");
			}
			update.append(sqlDialect.columnRef(targetColumns[n].getDBName())).append(" = ?");
		}
		update.append(" WHERE ").append(BinaryContentMigration.keyCondition(sqlDialect, keyColumns));

		log.info("Migrating document content of table '" + table.getName() + "' to attribute '" + content.getName()
			+ "' stored as " + placement + ".");

		int maxBatchSize = BinaryContentMigration.maxBatchSize(sqlDialect, targetColumns.length + keyColumns.size());
		int processed = 0;
		int migrated = 0;
		int external = 0;
		int noVersion = 0;
		int otherSource = 0;
		int missing = 0;
		try (PreparedStatement statement = connection.prepareStatement(update.toString())) {
			int batchSize = 0;
			try (ResultSet rows = select.executeQuery(connection)) {
				while (rows.next()) {
					processed++;
					if (processed % LOG_INTERVAL == 0) {
						log.info("Processed " + processed + " rows of table '" + table.getName() + "', migrated "
							+ migrated + ".");
					}

					Object[] key = BinaryContentMigration.readKey(sqlDialect, rows, keyColumns);
					String resource = rows.getString(resourceIndex);
					int version = rows.getInt(versionIndex);
					if (_reader.isVersioned() && (rows.wasNull() || version <= 0)) {
						noVersion++;
						continue;
					}
					if (resource == null || !resource.startsWith(protocolPrefix)) {
						otherSource++;
						continue;
					}
					String path = resource.substring(protocolPrefix.length());

					BinaryData stored = _reader.read(path, version);
					if (stored == null) {
						missing++;
						log.info("No content of version " + version + " of document '" + resource
							+ "' in its data source (" + table.getName() + " row " + keyString(key)
							+ "), the row is left without content.", Log.WARN);
						continue;
					}

					String name = rows.getString(nameIndex);
					String contentType = contentType(rows.getString(formatIndex), name);
					BinaryData placed = place(stored, contentType, name, placement);
					if (placed instanceof BlobBinaryData) {
						external++;
					}

					Object[] values = BinaryColumnsStorage.columnValues(content, placed);
					for (int n = 0; n < targetColumns.length; n++) {
						sqlDialect.setFromJava(statement, values[n], n + 1, targetColumns[n].getSQLType());
					}
					BinaryContentMigration.bindKey(sqlDialect, statement, targetColumns.length + 1, keyColumns, key);
					statement.addBatch();
					migrated++;
					if (++batchSize >= maxBatchSize) {
						statement.executeBatch();
						batchSize = 0;
					}
				}
			}
			if (batchSize > 0) {
				statement.executeBatch();
			}
		}

		log.info("Migrated the content of " + migrated + " of " + processed + " rows of table '" + table.getName()
			+ "' (" + external + " stored in a blob store, " + (migrated - external) + " inline). Rows without content: "
			+ noVersion + ", rows of other data sources: " + otherSource + ", rows with content missing in the data source: "
			+ missing + ".", missing > 0 ? Log.WARN : Log.INFO);
	}

	/**
	 * Stores the given content according to the placement.
	 *
	 * <p>
	 * Content to store inline is read into memory, so that no stream stays open until the batch
	 * writing it is executed.
	 * </p>
	 */
	private static BinaryData place(BinaryData stored, String contentType, String name, BinaryPlacement placement)
			throws IOException {
		long size = stored.getSize();
		if (placement.isExternal(size)) {
			try (InputStream in = stored.getStream()) {
				return BlobUpload.upload(placement.getStoreName(), in, size, contentType,
					name);
			}
		}
		try (InputStream in = stored.getStream()) {
			return BinaryDataFactory.createMemoryBinaryData(in, size, contentType, name);
		}
	}

	private static String contentType(String format, String name) {
		if (!StringServices.isEmpty(format)) {
			return format;
		}
		if (name != null && MimeTypesModule.Module.INSTANCE.isActive()) {
			return MimeTypesModule.getInstance().getMimeType(name);
		}
		return BinaryData.CONTENT_TYPE_OCTET_STREAM;
	}

	/**
	 * Adds the column of the given attribute to the selected columns.
	 *
	 * @return The index of the column in the result set.
	 */
	private static int addColumn(List<SQLColumnDefinition> columns, MOStructure table, String attributeName)
			throws SQLException {
		MOAttribute attribute = table.getAttributeOrNull(attributeName);
		if (attribute == null || attribute.getDbMapping().length != 1) {
			throw new SQLException("Table '" + table.getName() + "' has no column for attribute '" + attributeName
				+ "'.");
		}
		columns.add(columnDef(attribute.getDbMapping()[0].getDBName()));
		return columns.size();
	}

	private static String keyString(Object[] key) {
		StringBuilder result = new StringBuilder();
		for (Object part : key) {
			if (result.length() > 0) {
				result.append('/');
			}
			result.append(part);
		}
		return result.toString();
	}

}
