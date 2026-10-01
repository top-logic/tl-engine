/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.wysiwyg.migration;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.db.sql.Batch;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLQuery.Parameter;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.element.model.migration.model.refactor.InternationalizeAttributeProcessor;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.QualifiedPartName;
import com.top_logic.model.migration.data.TypePart;
import com.top_logic.model.wysiwyg.i18n.I18NStructuredTextAttributeStorage;
import com.top_logic.model.wysiwyg.storage.StructuredTextAttributeStorage;

/**
 * {@link MigrationProcessor} that makes HTML (structured text) attributes to I18N HTML attributes.
 * 
 * @see InternationalizeAttributeProcessor
 */
public class InternationalizeHtmlAttributeProcessor extends InternationalizeAttributeProcessor {

	/**
	 * Configuration options for {@link InternationalizeHtmlAttributeProcessor}.
	 */
	@TagName("internationalize-html-attribute")
	public interface Config<I extends InternationalizeHtmlAttributeProcessor>
			extends InternationalizeAttributeProcessor.Config<I> {

		/**
		 * The supported languages mapped to source attributes from which the value for the
		 * corresponding language is taken from.
		 * 
		 * <p>
		 * By default, the default system language is taken from the given {@link #getAttribute()
		 * target attribute}.
		 * </p>
		 * 
		 * <p>
		 * The default system language can be taken from an attribute explicitly using the name
		 * <code>default</code> for the language.
		 * </p>
		 * 
		 * <p>
		 * Only if the source attributes do not store their values in a column with the same name a
		 * the attribute, {@link #getColumns()} has to be configured separately. The source column
		 * is not derived from potential attribute annotations.
		 * </p>
		 */
		@Name("source-attributes")
		@MapBinding(key = "lang", attribute = "name", tag = "attribute")
		Map<String, QualifiedPartName> getSourceAttributes();

	}

	/**
	 * Creates a {@link InternationalizeHtmlAttributeProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public InternationalizeHtmlAttributeProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	/**
	 * Index of the first column of the image content in the result of the image query.
	 */
	private static final int DATA_COLUMN_OFFSET = 12;

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		super.doMigration(context, log, connection);

		Config<?> config = (Config<?>) getConfig();
		try {
			DBHelper sqlDialect = connection.getSQLDialect();

			Util util = context.getSQLUtils();

			// Move images.

			// The tables as currently stored in the database: The image content may still be stored
			// in a form the application schema no longer declares.
			MORepository repository = context.getPersistentRepository();
			MOClass imagesTable = (MOClass) repository.getType(StructuredTextAttributeStorage.HTML_ATTRIBUTE_STORAGE);

			MOReference objRef =
				(MOReference) imagesTable.getAttribute(I18NStructuredTextAttributeStorage.OBJECT_ATTRIBUTE_NAME);
			MOReference attrRef =
				(MOReference) imagesTable
					.getAttribute(I18NStructuredTextAttributeStorage.META_ATTRIBUTE_ATTRIBUTE_NAME);
			String fileNameColumn =
				columnName(imagesTable.getAttribute(I18NStructuredTextAttributeStorage.FILENAME_ATTRIBUTE_NAME));
			String contentTypeColumn =
				columnName(imagesTable.getAttribute(I18NStructuredTextAttributeStorage.CONTENT_TYPE_ATTRIBUTE_NAME));
			String hashColumn =
				columnName(imagesTable.getAttribute(I18NStructuredTextAttributeStorage.HASH_ATTRIBUTE_NAME));
			DBAttribute[] dataColumns =
				imagesTable.getAttribute(I18NStructuredTextAttributeStorage.DATA_ATTRIBUTE_NAME).getDbMapping();

			TypePart attr = util.getTLTypePartOrFail(connection, config.getAttribute());
			TLID attrId = attr.getDefinition();

			for (Entry<String, QualifiedPartName> source : config.getSourceAttributes().entrySet()) {
				TypePart sourceAttr = util.getTLTypePartOrFail(connection, source.getValue());
				TLID sourceAttrId = sourceAttr.getDefinition();
				String lang = source.getKey();
				if (lang.equals(DEFAULT_LANG)) {
					lang = ResourcesModule.getInstance().getDefaultLocale().getLanguage();
				}

				List<SQLColumnDefinition> selectColumns = new ArrayList<>(columns(
					util.branchColumnDef(),
					columnDef(BasicTypes.IDENTIFIER_DB_NAME),
					columnDef(BasicTypes.REV_MIN_DB_NAME),
					columnDef(BasicTypes.REV_MAX_DB_NAME),
					columnDef(BasicTypes.REV_CREATE_DB_NAME),

					columnDef(objRef.getColumn(ReferencePart.type).getDBName()),
					columnDef(objRef.getColumn(ReferencePart.name).getDBName()),
					columnDef(attrRef.getColumn(ReferencePart.name).getDBName()),

					columnDef(fileNameColumn),
					columnDef(contentTypeColumn),
					columnDef(hashColumn)));
				// Some drivers require the stream of a BLOB to be read after all other columns.
				for (DBAttribute dataColumn : dataColumns) {
					selectColumns.add(columnDef(dataColumn.getDBName()));
				}

				CompiledStatement select = query(
					select(
						selectColumns,
						table(imagesTable.getDBMapping().getDBName()),
						and(
							eqSQL(column(attrRef.getColumn(ReferencePart.name).getDBName()),
								literal(DBType.ID, sourceAttrId)),
							eqSQL(column(objRef.getColumn(ReferencePart.type).getDBName()),
								literal(DBType.STRING, config.getTable()))),
						orders(
							order(util.branchColumnRef()),
							order(column(BasicTypes.IDENTIFIER_DB_NAME)),
							order(column(BasicTypes.REV_MIN_DB_NAME))))).toSql(sqlDialect);

				select.setResultSetConfiguration(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

				MOClass i18nImagesTable =
					(MOClass) repository.getType(I18NStructuredTextAttributeStorage.IMAGES_TABLE_NAME);

				long start = System.nanoTime();
				int perSecond = 0;
				int batchSize = 0;
				int total = 0;
				int maxBatchSize = sqlDialect.getMaxBatchSize(10);

				CompiledStatement insert = createI18NImagesInsert(context, connection, i18nImagesTable);
				try (Batch batch = insert.createBatch(connection)) {
					try (ResultSet result = select.executeQuery(connection)) {
						while (result.next()) {
							long imgBranch = result.getLong(1);
							// long imgId = result.getLong(2);
							long revMin = result.getLong(3);
							long revMax = result.getLong(4);
							long revCreate = result.getLong(5);

							String objType = result.getString(6);
							long objId = result.getLong(7);
							// long oldAttrId = result.getLong(8);

							String fileName = result.getString(9);
							String contentType = result.getString(10);
							String hash = result.getString(11);

							List<Object> values = new ArrayList<>(Arrays.asList(
								imgBranch, util.newID(connection), revMin, revMax, revCreate, objType, objId, attrId,
								fileName, contentType, hash, lang));
							for (int n = 0; n < dataColumns.length; n++) {
								int index = DATA_COLUMN_OFFSET + n;
								values.add(dataColumns[n].getSQLType() == DBType.BLOB ? result.getBlob(index)
									: result.getObject(index));
							}

							batch.addBatch(values.toArray());

							if (++batchSize >= maxBatchSize) {
								batch.executeBatch();

								total += batchSize;
								perSecond += batchSize;
								batchSize = 0;

								// Log only once each second.
								long now = System.nanoTime();
								long elapsed = now - start;
								if (elapsed > 1000000000L) {
									start = now;
									log.info(
										"Moved '" + perSecond + "' values of '" + config.getAttribute().getName()
											+ "' from '" + objType + "' to internationalization table (" + lang
											+ ").");
									perSecond = 0;
								}
							}

							result.deleteRow();
						}
					}
					if (batchSize > 0) {
						batch.executeBatch();

						total += batchSize;
						perSecond += batchSize;
						batchSize = 0;
					}

					if (perSecond > 0) {
						log.info(
							"Moved '" + perSecond + "' images of '" + config.getAttribute().getName()
								+ "' to internationalization table (" + lang + ").");
						perSecond = 0;
					}

					if (total > 0) {
						context.invalidateXRef(i18nImagesTable);
					}
				}
			}
		} catch (SQLException | MigrationException ex) {
			log.error("Failed to move images of '" + config.getAttribute().getName()
				+ "' from  to internationalization table.", ex);
		}
	}

	@Override
	protected Map<String, String> getSourceColumns() {
		Config<?> config = (Config<?>) getConfig();
		Map<String, String> sourceColumns = new HashMap<>();
		String defaultLang = ResourcesModule.getInstance().getDefaultLocale().getLanguage();
		sourceColumns.put(defaultLang, config.getAttribute().getPartName());

		for (Entry<String, QualifiedPartName> source : config.getSourceAttributes().entrySet()) {
			String lang = source.getKey();
			if (lang.equals(DEFAULT_LANG)) {
				lang = ResourcesModule.getInstance().getDefaultLocale().getLanguage();
			}

			sourceColumns.put(lang, source.getValue().getPartName());
		}

		Map<String, String> columns = config.getColumns();
		for (Entry<String, String> column : columns.entrySet()) {
			String lang = column.getKey();
			if (lang.equals(DEFAULT_LANG)) {
				lang = ResourcesModule.getInstance().getDefaultLocale().getLanguage();
			}
			sourceColumns.put(lang, column.getValue());
		}
		return sourceColumns;
	}

	private CompiledStatement createI18NImagesInsert(MigrationContext context, PooledConnection connection,
			MOClass i18nTable)
			throws SQLException {
		Util util = context.getSQLUtils();

		MOReference objRef =
			(MOReference) i18nTable.getAttribute(I18NStructuredTextAttributeStorage.OBJECT_ATTRIBUTE_NAME);
		MOReference attrRef =
			(MOReference) i18nTable.getAttribute(I18NStructuredTextAttributeStorage.META_ATTRIBUTE_ATTRIBUTE_NAME);
		String fileNameColumn =
			columnName(i18nTable.getAttribute(I18NStructuredTextAttributeStorage.FILENAME_ATTRIBUTE_NAME));
		String contentTypeColumn =
			columnName(i18nTable.getAttribute(I18NStructuredTextAttributeStorage.CONTENT_TYPE_ATTRIBUTE_NAME));
		String hashColumn = columnName(i18nTable.getAttribute(I18NStructuredTextAttributeStorage.HASH_ATTRIBUTE_NAME));
		String langColumn =
			columnName(i18nTable.getAttribute(I18NStructuredTextAttributeStorage.LANGUAGE_ATTRIBUTE_NAME));
		DBAttribute[] dataColumns =
			i18nTable.getAttribute(I18NStructuredTextAttributeStorage.DATA_ATTRIBUTE_NAME).getDbMapping();

		List<Parameter> parameters = new ArrayList<>(parameters(
			parameterDef(DBType.LONG, "branch"),
			parameterDef(DBType.ID, "id"),
			parameterDef(DBType.LONG, "revMin"),
			parameterDef(DBType.LONG, "revMax"),
			parameterDef(DBType.LONG, "revCreate"),

			parameterDef(DBType.LONG, "objType"),
			parameterDef(DBType.LONG, "objId"),
			parameterDef(DBType.ID, "attrId"),

			parameterDef(DBType.STRING, "fileName"),
			parameterDef(DBType.STRING, "contentType"),
			parameterDef(DBType.STRING, "hash"),
			parameterDef(DBType.LONG, "lang")));
		List<String> columns = new ArrayList<>(Util.listWithoutNull(columnNames(
			util.branchColumnOrNull(),
			BasicTypes.IDENTIFIER_DB_NAME,
			BasicTypes.REV_MIN_DB_NAME,
			BasicTypes.REV_MAX_DB_NAME,
			BasicTypes.REV_CREATE_DB_NAME,
			objRef.getColumn(ReferencePart.type).getDBName(),
			objRef.getColumn(ReferencePart.name).getDBName(),
			attrRef.getColumn(ReferencePart.name).getDBName(),
			fileNameColumn,
			contentTypeColumn,
			hashColumn,
			langColumn)));
		List<SQLExpression> values = new ArrayList<>(Util.listWithoutNull(expressions(
			util.branchParamOrNull(),
			parameter(DBType.ID, "id"),
			parameter(DBType.LONG, "revMin"),
			parameter(DBType.LONG, "revMax"),
			parameter(DBType.LONG, "revCreate"),
			parameter(DBType.LONG, "objType"),
			parameter(DBType.LONG, "objId"),
			parameter(DBType.ID, "attrId"),
			parameter(DBType.ID, "fileName"),
			parameter(DBType.ID, "contentType"),
			parameter(DBType.ID, "hash"),
			parameter(DBType.LONG, "lang"))));
		// All columns of the image content in the order of the selected values.
		for (int n = 0; n < dataColumns.length; n++) {
			DBAttribute dataColumn = dataColumns[n];
			String parameterName = "data" + n;
			parameters.add(parameterDef(dataColumn.getSQLType(), parameterName));
			columns.add(dataColumn.getDBName());
			values.add(parameter(dataColumn.getSQLType(), parameterName));
		}

		CompiledStatement insert = query(parameters,
			insert(table(i18nTable.getDBMapping().getDBName()), columns, values)).toSql(connection.getSQLDialect());
		return insert;
	}

	private static String columnName(MOAttribute attribute) {
		return attribute.getDbMapping()[0].getDBName();
	}

}