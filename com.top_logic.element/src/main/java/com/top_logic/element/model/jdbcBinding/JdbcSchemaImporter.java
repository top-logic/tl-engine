/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.jdbcBinding;

import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;

import com.top_logic.basic.Logger;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.format.RegExpValueProvider;
import com.top_logic.basic.db.model.DBColumn;
import com.top_logic.basic.db.model.DBColumnRef;
import com.top_logic.basic.db.model.DBForeignKey;
import com.top_logic.basic.db.model.DBPrimary;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.util.DBSchemaUtils;
import com.top_logic.basic.generate.CodeUtil;
import com.top_logic.basic.generate.TokenSplitter;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.mime.MimeTypesConstants;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.element.config.ModelConfig;
import com.top_logic.element.meta.schema.ElementSchemaConstants;
import com.top_logic.element.model.export.ModelConfigExtractor;
import com.top_logic.element.model.jdbcBinding.api.annotate.TLColumnBinding;
import com.top_logic.element.model.jdbcBinding.api.annotate.TLForeignKeyBinding;
import com.top_logic.element.model.jdbcBinding.api.annotate.TLTableBinding;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassProperty;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.annotate.Visibility;
import com.top_logic.model.builtin.TLCore;
import com.top_logic.model.config.ModelPartConfig;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link AbstractCommandHandler} importing a model from an existing database schema.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class JdbcSchemaImporter extends AbstractCommandHandler {

	/**
	 * Configuration options for {@link JdbcSchemaImporter}.
	 */
	public interface Config extends AbstractCommandHandler.Config {

		/**
		 * Name of the connection pool to import data from.
		 */
		@Mandatory
		@Name("poolName")
		String getPoolName();

		/**
		 * Regular expression matching table names that should not be imported.
		 */
		@Name("excludeTablePattern")
		@Format(RegExpValueProvider.class)
		Pattern getExcludeTablePattern();

		/**
		 * Regular expression matching column names that should not be imported.
		 */
		@Name("excludeColumnPattern")
		@Format(RegExpValueProvider.class)
		Pattern getExcludeColumnPattern();

		/**
		 * Whether primary key columns should receive an annotation that hides them from the UI by
		 * default.
		 */
		@BooleanDefault(true)
		boolean getHidePrimaryKeyColumns();

		/**
		 * A map of tokens and their replacement during import.
		 * <p>
		 * The token and its replacement are separated by a tab character. Each pair of token and
		 * replacement are on their own line. Lines can be separated by either <code>\n</code> or
		 * <code>\r\n</code>.
		 * </p>
		 */
		String getGlossary();
	}

	/**
	 * Creates a {@link JdbcSchemaImporter}.
	 */
	public JdbcSchemaImporter(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> arguments) {

		deliverModel(context);

		return HandlerResult.DEFAULT_RESULT;
	}

	private void deliverModel(DisplayContext context) {
		TLModel model = synthesizeModel();
		/* The TL_CORE module was added to the model to be able to use it. But it should not be
		 * exported, as it is not a part of the imported database. */
		model.getModules().remove(model.getModule(TLCore.TL_CORE));

		BinaryDataSource xml = new BinaryDataSource() {
			@Override
			public String getName() {
				return model.getModules().iterator().next().getName() + ".model.xml";
			}

			@Override
			public long getSize() {
				return -1;
			}

			@Override
			public String getContentType() {
				return MimeTypesConstants.APPLICATION_XML;
			}

			@Override
			public void deliverTo(OutputStream out) throws IOException {
				try (OutputStreamWriter streamWriter = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
					ModelPartConfig config = model.visit(new ModelConfigExtractor(), null);
					TypedConfiguration.minimize(config);

					ConfigurationWriter configWriter = new ConfigurationWriter(streamWriter);
					configWriter.setNamespace("", ElementSchemaConstants.MODEL_6_NS);
					configWriter.write(ElementSchemaConstants.ROOT_ELEMENT, ModelConfig.class, config);
				} catch (XMLStreamException ex) {
					Logger.error("Cannot write schema.", ex, JdbcSchemaImporter.class);
				}
			}
		};

		context.getWindowScope().deliverContent(xml);
	}

	private TLModel synthesizeModel() {
		String poolName = config().getPoolName();
		ConnectionPool pool = ConnectionPoolRegistry.getConnectionPool(poolName);
		PooledConnection connection = pool.borrowReadConnection();
		try {
			DBSchema schema = DBSchemaUtils.extractSchema(pool);

			TLModel model = getModel();

			TLModule module = model.addModule(model, schema.getName());

			Collection<DBTable> tables = getClassTables(schema);

			for (DBTable table : tables) {
				createClass(model, module, table);
			}

			boolean hidePrimaryKeyColumns = config().getHidePrimaryKeyColumns();
			for (DBTable table : tables) {
				String tableName = table.getName();
				TLClass type = (TLClass) module.getType(tableName);

				DBPrimary primaryKey = table.getPrimaryKey();
				if (hidePrimaryKeyColumns) {
					if (primaryKey != null) {
						// Hide (probably technical) primary key columns.
						for (DBColumnRef pkColumn : primaryKey.getColumnRefs()) {
							String pkColumnName = pkColumn.getName();
							TLStructuredTypePart keyProperty = type.getPart(pkColumnName);
							keyProperty.setAnnotation(DisplayAnnotations.newVisibility(Visibility.HIDDEN));
						}
					}
				}

				for (DBForeignKey foreinKey : table.getForeignKeys()) {
					String targetTable = foreinKey.getTargetTableRef().getName();
					TLClass targetType = (TLClass) module.getType(targetTable);

					DBColumn sourceColumn = foreinKey.getSourceColumns().get(0);
					String refName = sourceColumn.getName();

					if (type.getPart(refName) != null) {
						// The property already exists. This can happen, if a primary key is also a
						// foreign key.
						refName = refName + "Ref";
					}

					TLAssociation association = model.addAssociation(module, module,
						CodeUtil.toCamelCase(type.getName()) + "$" + CodeUtil.toCamelCase(refName));
					@SuppressWarnings("unused")
					TLAssociationEnd selfEnd = model.addAssociationEnd(association, "_self", type);
					TLAssociationEnd targetEnd = model.addAssociationEnd(association, refName, targetType);
					TLReference reference = model.addReference(type, refName, targetEnd);

					if (sourceColumn.isMandatory()) {
						reference.setMandatory(true);
					}

					TLForeignKeyBinding foreignKeyBinding = TypedConfiguration.newConfigItem(TLForeignKeyBinding.class);
					foreignKeyBinding.setColumns(names(foreinKey.getSourceColumnRefs()));
					reference.setAnnotation(foreignKeyBinding);
				}
			}

			fixModelNames(module);

			return model;
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		} finally {
			pool.releaseReadConnection(connection);
		}
	}

	private void fixModelNames(TLModule module) {
		Map<String, List<String>> glossary = readGlossary();

		TokenSplitter tokenSplitter = new TokenSplitter(glossary);

		Map<String, Integer> typeClashes = new HashMap<>();
		for (TLType type : module.getTypes()) {
			if (type.getModelKind() == ModelKind.CLASS) {
				type.setName(makeUnique(typeClashes, modelName(glossary, tokenSplitter, type.getName())));

				Map<String, Integer> propertyClashes = new HashMap<>();
				for (TLStructuredTypePart part : ((TLStructuredType) type).getLocalParts()) {
					part.setName(
						CodeUtil.toLowerCaseStart(
							makeUnique(propertyClashes, modelName(glossary, tokenSplitter, part.getName()))));
				}
			}
		}
	}

	private String makeUnique(Map<String, Integer> clashes, String newName) {
		Integer clash = clashes.put(newName, Integer.valueOf(1));
		if (clash != null) {
			int nextId = clash.intValue() + 1;
			newName = newName + nextId;
			clashes.put(newName, Integer.valueOf(nextId));
		}
		return newName;
	}

	private Map<String, List<String>> readGlossary() {
		String contents = config().getGlossary();

		Map<String, List<String>> glossary = new HashMap<>();
		for (String line : contents.split("\\r?\\n")) {
			String trimmedLine = line.trim();
			String[] tokens = trimmedLine.split("\\t");
			if (tokens.length == 0) {
				continue;
			}
			String token = tokens[0];
			if (token.isEmpty()) {
				continue;
			}
			String replacement;
			if (tokens.length < 2) {
				replacement = token;
			} else {
				replacement = tokens[1];
			}
			if ("-".equals(replacement)) {
				glossary.put(token, emptyList());
			} else {
				glossary.put(token, List.of(CodeUtil.simpleNameParts(replacement)));
			}
		}
		return glossary;
	}

	private String modelName(Map<String, List<String>> glossary, TokenSplitter tokenSplitter, String dbName) {
		// Test whole match first.
		List<String> replacement = glossary.get(dbName);
		if (replacement != null) {
			if (!replacement.isEmpty()) {
				StringBuffer result = new StringBuffer();
				appendParts(result, replacement);
				return result.toString();
			}
		} else if (glossary.containsKey(dbName)) {
			return dbName;
		}

		// Match parts.
		StringBuffer result = new StringBuffer();
		for (String part : CodeUtil.simpleNameParts(dbName)) {
			appendParts(result, tokenSplitter.split(part));
		}
		return result.toString();
	}

	private void appendParts(StringBuffer result, List<String> parts) {
		for (String subpart : parts) {
			result.append(CodeUtil.toUpperCaseStart(subpart.toLowerCase()));
		}
	}

	private List<String> names(List<DBColumnRef> refs) {
		return refs.stream().map(ref -> ref.getName()).collect(Collectors.toList());
	}

	/**
	 * The model to import the DDL schema to.
	 */
	protected TLModelImpl getModel() {
		TLModelImpl result = new TLModelImpl();
		result.addCoreModule();
		return result;
	}

	private void createClass(TLModel model, TLModule module, DBTable table) {
		String tableName = table.getName();

		TLClass type = model.addClass(module, module, tableName);

		TLTableBinding tableBinding = TypedConfiguration.newConfigItem(TLTableBinding.class);
		tableBinding.setName(tableName);

		Set<String> foreignKeyColumns = new HashSet<>();
		for (DBForeignKey foreignKey : table.getForeignKeys()) {
			for (DBColumnRef column : foreignKey.getSourceColumnRefs()) {
				foreignKeyColumns.add(column.getName());
			}
		}
		// Primary key columns are always required.
		DBPrimary key = table.getPrimaryKey();
		if (key != null) {
			List<String> primaryKeyColumns = new ArrayList<>();
			for (DBColumnRef column : key.getColumnRefs()) {
				String keyColumnName = column.getName();
				foreignKeyColumns.remove(keyColumnName);
				primaryKeyColumns.add(keyColumnName);
			}

			tableBinding.setPrimaryKey(primaryKeyColumns);
		}
		type.setAnnotation(tableBinding);

		List<DBColumn> columns = getPropertyColumns(table);

		for (DBColumn column : columns) {
			String columnName = column.getName();
			if (foreignKeyColumns.contains(columnName)) {
				// Will result in a reference later on.
				continue;
			}

			TLClassProperty property = model.addClassProperty(type, columnName, columnType(model, column));
			if (column.isMandatory()) {
				property.setMandatory(true);
			}

			TLColumnBinding columnBinding = TypedConfiguration.newConfigItem(TLColumnBinding.class);
			columnBinding.setName(columnName);
			property.setAnnotation(columnBinding);
		}
	}

	private Collection<DBTable> getClassTables(DBSchema schema) {
		Pattern excludeTablePattern = config().getExcludeTablePattern();
		Collection<DBTable> allTables = schema.getTables();
		return excludeMatches(allTables, excludeTablePattern, DBTable::getName);
	}

	private List<DBColumn> getPropertyColumns(DBTable table) {
		Pattern excludeColumnPattern = config().getExcludeColumnPattern();
		List<DBColumn> allColumns = table.getColumns();
		return excludeMatches(allColumns, excludeColumnPattern, DBColumn::getName);
	}

	private <T> List<T> excludeMatches(Collection<T> entries, Pattern excludePattern, Function<T, String> nameFunction) {
		if (excludePattern == null) {
			return new ArrayList<>(entries);
		}
		return entries.stream()
			.filter(column -> !excludePattern.matcher(nameFunction.apply(column)).matches())
			.collect(toList());
	}

	private Config config() {
		return (Config) getConfig();
	}

	private TLType columnType(TLModel model, DBColumn column) {
		switch (column.getType()) {
			case BOOLEAN:
				return TLCore.getPrimitiveType(model, Kind.BOOLEAN);
			case BLOB:
			case BYTE:
				return TLCore.getPrimitiveType(model, Kind.BINARY);
			case CLOB:
			case CHAR:
			case STRING:
				return TLCore.getPrimitiveType(model, Kind.STRING);
			case DATE:
				return TLCore.getPrimitiveType(model, Kind.DATE);
			case TIME:
				return TLCore.getPrimitiveType(model, Kind.DATE);
			case DATETIME:
				return TLCore.getPrimitiveType(model, Kind.DATE);
			case FLOAT:
			case DOUBLE:
				return TLCore.getPrimitiveType(model, Kind.FLOAT);
			case DECIMAL:
				if (column.getPrecision() > 0) {
					return TLCore.getPrimitiveType(model, Kind.FLOAT);
				} else {
					return TLCore.getPrimitiveType(model, Kind.INT);
				}
			case INT:
			case SHORT:
			case LONG:
				return TLCore.getPrimitiveType(model, Kind.INT);
			case ID:
				return TLCore.getPrimitiveType(model, Kind.INT);
		}
		throw new UnreachableAssertion("No such DB type: " + column.getType());
	}

}
