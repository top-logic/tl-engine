/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.element.model.ddlimporter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;

import com.top_logic.basic.Logger;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.db.model.DBColumn;
import com.top_logic.basic.db.model.DBColumnRef;
import com.top_logic.basic.db.model.DBForeignKey;
import com.top_logic.basic.db.model.DBPrimary;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.util.DBSchemaUtils;
import com.top_logic.basic.generate.CodeUtil;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.element.config.ModelConfig;
import com.top_logic.element.meta.schema.ElementSchemaConstants;
import com.top_logic.element.model.ddlimporter.api.annotate.TLColumnBinding;
import com.top_logic.element.model.ddlimporter.api.annotate.TLForeignKeyBinding;
import com.top_logic.element.model.ddlimporter.api.annotate.TLTableBinding;
import com.top_logic.element.model.export.ModelConfigExtractor;
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
 * {@link AbstractCommandHandler} importing a model from an existing database.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DDLImporter extends AbstractCommandHandler {

	/**
	 * Configuration options for {@link DDLImporter}.
	 */
	public interface Config extends AbstractCommandHandler.Config {

		/**
		 * Name of the connection pool to import data from.
		 */
		@Mandatory
		@Name("poolName")
		String getPoolName();

	}

	/**
	 * Creates a {@link DDLImporter}.
	 */
	public DDLImporter(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {

		deliverModel(aContext);

		return HandlerResult.DEFAULT_RESULT;
	}

	private void deliverModel(DisplayContext aContext) {
		TLModel model = synthesizeModel();

		BinaryDataSource xml = new BinaryDataSource() {
			@Override
			public String getName() {
				return "schema.xml";
			}

			@Override
			public long getSize() {
				return -1;
			}

			@Override
			public String getContentType() {
				return "application/xml";
			}

			@Override
			public void deliverTo(OutputStream out) throws IOException {
				try (OutputStreamWriter w = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
					ModelPartConfig config = model.visit(new ModelConfigExtractor(), null);
					TypedConfiguration.minimize(config);

					ConfigurationWriter writer = new ConfigurationWriter(w);
					writer.write(ElementSchemaConstants.ROOT_ELEMENT, ModelConfig.class, config);
				} catch (XMLStreamException ex) {
					Logger.error("Cannot write schema.", ex, DDLImporter.class);
				}
			}
		};

		aContext.getWindowScope().deliverContent(xml);
	}

	private TLModel synthesizeModel() {
		String poolName = ((Config) getConfig()).getPoolName();
		ConnectionPool pool = ConnectionPoolRegistry.getConnectionPool(poolName);
		PooledConnection connection = pool.borrowReadConnection();
		try {
			DBSchema schema = DBSchemaUtils.extractSchema(pool);

			TLModel model = getModel();

			TLModule module = model.addModule(model, schema.getName());

			for (DBTable table : schema.getTables()) {
				createClass(model, module, table);
			}

			for (DBTable table : schema.getTables()) {
				String tableName = table.getName();
				TLClass type = (TLClass) module.getType(tableName);

				DBPrimary primaryKey = table.getPrimaryKey();
				if (primaryKey != null) {
					// Hide (probably technical) primary key columns.
					for (DBColumnRef pkColumn : primaryKey.getColumnRefs()) {
						String pkColumnName = pkColumn.getName();
						TLStructuredTypePart keyProperty = type.getPart(pkColumnName);
						keyProperty.setAnnotation(DisplayAnnotations.newVisibility(Visibility.HIDDEN));
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

			for (TLType type : module.getTypes()) {
				if (type.getModelKind() == ModelKind.CLASS) {
					type.setName(CodeUtil.toCamelCaseFromAllUpperCase(type.getName()));

					for (TLStructuredTypePart part : ((TLStructuredType) type).getLocalParts()) {
						part.setName(CodeUtil.toLowerCaseStart(CodeUtil.toCamelCaseFromAllUpperCase(part.getName())));
					}
				}
			}

			return model;
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		} finally {
			pool.releaseReadConnection(connection);
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

		Set<String> fkColumns = new HashSet<>();
		for (DBForeignKey fk : table.getForeignKeys()) {
			for (DBColumnRef col : fk.getSourceColumnRefs()) {
				fkColumns.add(col.getName());
			}
		}
		// Primary key columns are always required.
		DBPrimary key = table.getPrimaryKey();
		if (key != null) {
			List<String> primaryKeyColumns = new ArrayList<>();
			for (DBColumnRef col : key.getColumnRefs()) {
				String keyColumnName = col.getName();
				fkColumns.remove(keyColumnName);
				primaryKeyColumns.add(keyColumnName);
			}

			tableBinding.setPrimaryKey(primaryKeyColumns);
		}
		type.setAnnotation(tableBinding);

		for (DBColumn column : table.getColumns()) {
			String columnName = column.getName();
			if (fkColumns.contains(columnName)) {
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

	private TLType columnType(TLModel model, DBColumn column) {
		switch (column.getType()) {
			case BOOLEAN:
				return TLCore.getPrimitiveType(model, Kind.BOOLEAN);

			case BLOB:
			case BYTE:
				return TLCore.getPrimitiveType(model, Kind.BINARY);

			case CLOB:
				// TODO: "Text"
			case CHAR:
			case STRING:
				return TLCore.getPrimitiveType(model, Kind.STRING);

			case DATE:
				// TODO: "Day"
				return TLCore.getPrimitiveType(model, Kind.DATE);
			case TIME:
				// TODO: "Time"
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
