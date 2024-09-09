/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.jdbcBinding;

import static com.top_logic.basic.config.TypedConfiguration.*;
import static java.nio.file.StandardOpenOption.*;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.ListDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.db.schema.io.MORepositoryBuilder;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.basic.tooling.Workspace;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.dob.schema.config.AssociationConfig;
import com.top_logic.dob.schema.config.MetaObjectConfig;
import com.top_logic.dob.schema.config.MetaObjectsConfig;
import com.top_logic.dob.schema.config.ReferenceAttributeConfig;
import com.top_logic.dob.xml.DOXMLConstants;
import com.top_logic.element.config.annotation.TLStorage;
import com.top_logic.element.meta.AbstractStorageBase;
import com.top_logic.element.meta.kbbased.storage.ForeignKeyStorage;
import com.top_logic.element.meta.kbbased.storage.LinkStorage;
import com.top_logic.element.meta.kbbased.storage.ListStorage;
import com.top_logic.element.meta.kbbased.storage.SetStorage;
import com.top_logic.element.model.export.ui.IDEModelExtractCommand;
import com.top_logic.element.model.export.ui.ModelExportCommand;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLReference;
import com.top_logic.model.config.annotation.TableName;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.NullModelDisabled;

/**
 * Generates a database schema for the selected {@link TLModule}.
 * <p>
 * Limitations:
 * <ul>
 * <li>There are no tests.</li>
 * <li>After generating it might be necessary to stop the application, delete the database and add
 * the new <code>meta.xml</code> file to the application configuration.</li>
 * <li>The generated <code>meta.xml</code> file has to be added manually to the application
 * configuration.</li>
 * <li>It has not been tested whether generating the schema leaves the application in an
 * inconsistent state. For example the stored database schema in the {@link DBProperties} might not
 * have been updated.</li>
 * <li>It only generates for one {@link TLModule}. Workaround: To generate for example for three
 * modules (a, b, c), generate in the order: a, b, c, a, b. The first three iterations create the
 * necessary tables. The next two generate the correct associations from these modules to the later
 * modules. Between each generation, the steps given above might be necessary.</li>
 * <li>Associations with monomorphic ends are created as non-monomorphic, as they are generated as
 * subtypes of {@value ApplicationObjectUtil#WRAPPER_ATTRIBUTE_ASSOCIATION_BASE}</li>
 * </ul>
 * </p>
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
@Label("Generate database schema")
public class DBSchemaGeneratorCommand extends AbstractCommandHandler {

	/** {@link ConfigurationItem} for the {@link DBSchemaGeneratorCommand}. */
	public interface Config extends AbstractCommandHandler.Config {

		@Override
		@StringDefault(CommandHandlerFactory.EXPORT_BUTTONS_GROUP)
		String getClique();

		@Override
		@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
		CommandGroupReference getGroup();

		@Override
		@FormattedDefault(CommandHandler.TargetConfig.TARGET_SELECTION_SELF)
		ModelSpec getTarget();

		@Override
		@ListDefault(NullModelDisabled.class)
		List<PolymorphicConfiguration<? extends ExecutabilityRule>> getExecutability();

		/**
		 * Whether an existing database schema should be ignored.
		 * <p>
		 * This is applied to types from the configured {@link TLModule}. For types outside of it,
		 * existing tables are always used and never overridden.
		 * </p>
		 */
		boolean getOverrideExistingSchema();

	}

	private static final String MODEL_FILE_ENDING = ".model.xml";

	private static final String KBASE = "kbase";

	private static final Property<Boolean> NOTICE_GIVEN =
		TypedAnnotatable.property(Boolean.class, "noticeGiven", Boolean.FALSE);

	private static final String RESUMED = "resumed";

	/** {@link TypedConfiguration} constructor for {@link DBSchemaGeneratorCommand}. */
	public DBSchemaGeneratorCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> arguments) {

		String resumed = RESUMED;
		if (arguments.get(resumed) == null && !context.getSubSessionContext().get(NOTICE_GIVEN)) {
			HandlerResult result = HandlerResult.suspended();
			Command resume = result.resumeContinuation(Collections.singletonMap(RESUMED, Boolean.TRUE));
			MessageBox.confirm(context.getWindowScope(), new DefaultLayoutData(
				DisplayDimension.dim(400, DisplayUnit.PIXEL), 100,
				DisplayDimension.dim(250, DisplayUnit.PIXEL), 100, Scrolling.AUTO), true,
				Fragments.message(I18NConstants.GENERATE_SCHEMA_TITLE),
				Fragments.message(I18NConstants.GENERATE_SCHEMA_NOTICE),
				MessageBox.button(ButtonType.YES, resume), MessageBox.button(ButtonType.NO));
			return result;
		}

		context.getSubSessionContext().set(NOTICE_GIVEN, Boolean.TRUE);

		return changeSchema(model);
	}

	private HandlerResult changeSchema(Object model) {
		if (!(model instanceof TLModule)) {
			return HandlerResult.error(I18NConstants.NO_TL_MODULE_SELECTED);
		}
		generate((TLModule) model);
		return HandlerResult.DEFAULT_RESULT;
	}

	private HandlerResult generate(TLModule module) {
		DBSchemaGenerator generator = new DBSchemaGenerator(module, getConfigTyped().getOverrideExistingSchema());
		generator.generate();
		writeMetaObjects(module.getName(), generator.getMetaObjectConfig());
		KBUtils.inTransaction(() -> {
			addTableAnnotations(generator);
			addInlineReferenceAnnotations(generator);
			addAssociationReferenceAnnotations(generator);
		});
		replaceModelXml(module);
		return HandlerResult.DEFAULT_RESULT;
	}

	private void replaceModelXml(TLModule module) {
		try {
			byte[] xml = ModelExportCommand.toXMLBytes(module);
			String fileName =
				IDEModelExtractCommand.MODEL_RESOURCE_PREFIX + File.separator + module.getName() + MODEL_FILE_ENDING;
			Path modelFile = FileManager.getInstance().getIDEFile(fileName).toPath();
			Path backupFile = modelFile.resolveSibling(module.getName() + ".backup" + MODEL_FILE_ENDING);
			Files.copy(modelFile, backupFile);
			Files.write(modelFile, xml, WRITE, TRUNCATE_EXISTING);
		} catch (IOException exception) {
			throw new UncheckedIOException(exception);
		} catch (XMLStreamException exception) {
			throw new RuntimeException(exception);
		}
	}

	private void writeMetaObjects(String fileNamePrefix, MetaObjectsConfig metaObjectContainer) {
		String xml = TypedConfiguration.toString(MORepositoryBuilder.ROOT_TAG, metaObjectContainer);
		writeMetaXml(fileNamePrefix, xml.getBytes(TypedConfiguration.DEFAULT_CHARSET));
	}

	private void writeMetaXml(String fileNamePrefix, byte[] content) {
		String fileName = fileNamePrefix + DOXMLConstants.FILE_ENDING;
		Path filePath = getKBasePath().resolve(fileName);
		try {
			Path backupFile = filePath.resolveSibling(fileNamePrefix + ".backup" + DOXMLConstants.FILE_ENDING);
			if (Files.exists(filePath)) {
				Files.copy(filePath, backupFile);
			}
			Files.write(filePath, content, CREATE, WRITE, TRUNCATE_EXISTING);
		} catch (IOException exception) {
			String message = "Failed to write meta xml file: " + FileUtilities.getSafeDetailedPath(filePath);
			throw new UncheckedIOException(message, exception);
		}
	}

	private Path getKBasePath() {
		return Workspace.topLevelWebapp().toPath()
			.resolve(ModuleLayoutConstants.WEB_INF_DIR)
			.resolve(KBASE);
	}
	private void addTableAnnotations(DBSchemaGenerator generator) {
		generator.getClassMapping()
			.entrySet()
			.stream()
			.filter(entry -> entry.getKey().getAnnotation(TableName.class) == null)
			.forEach(entry -> entry.getKey().setAnnotation(createTableAnnotation(entry.getValue())));
	}

	private TableName createTableAnnotation(MetaObjectConfig moConfig) {
		TableName annotation = newConfigItem(TableName.class);
		annotation.setName(moConfig.getObjectName());
		return annotation;
	}

	private void addInlineReferenceAnnotations(DBSchemaGenerator generator) {
		generator.getClassMapping()
			.entrySet()
			.stream()
			.forEach(entry -> addInlineReferenceAnnotations(entry.getKey(), entry.getValue()));
	}

	private void addInlineReferenceAnnotations(TLClass tlClass, MetaObjectConfig moClass) {
		moClass.getAttributes()
			.stream()
			.filter(ReferenceAttributeConfig.class::isInstance)
			.map(ReferenceAttributeConfig.class::cast)
			.forEach(moReference -> addInlineReferenceAnnotation(tlClass, moClass, moReference));
	}

	private void addInlineReferenceAnnotation(
			TLClass tlClass, MetaObjectConfig moClass, ReferenceAttributeConfig moReference) {
		String attributeName = moReference.getAttributeName();
		TLClassPart tlClassPart = (TLClassPart) tlClass.getPart(attributeName);
		tlClassPart.setAnnotation(createInlineReferenceAnnotation(moClass.getObjectName(), attributeName));
	}

	private TLStorage createInlineReferenceAnnotation(String tableName, String attributeName) {
		ForeignKeyStorage.Config<?> inlineReference = newConfigItem(ForeignKeyStorage.Config.class);
		inlineReference.setStorageType(tableName);
		inlineReference.setStorageAttribute(attributeName);
		return createStorageAnnotation(inlineReference);
	}

	private void addAssociationReferenceAnnotations(DBSchemaGenerator generator) {
		generator.getReferenceToAssociationMapping()
			.entrySet()
			.stream()
			.forEach(entry -> addAssociationReferenceAnnotation(entry.getKey(), entry.getValue()));
	}

	private void addAssociationReferenceAnnotation(TLReference tlReference, AssociationConfig moAssociation) {
		String tableName = moAssociation.getObjectName();
		LinkStorage.Config<?> storageConfig = createAssociationReferenceStorageConfig(tableName, tlReference);
		tlReference.setAnnotation(createStorageAnnotation(storageConfig));
	}

	private LinkStorage.Config<?> createAssociationReferenceStorageConfig(String tableName, TLReference tlReference) {
		boolean isComposite = tlReference.isComposite();
		HistoryType historyType = tlReference.getHistoryType();
		LinkStorage.Config<?> storageConfig;
		if (tlReference.isOrdered()) {
			storageConfig = ListStorage.listConfig(isComposite, historyType);
		} else {
			storageConfig = SetStorage.setConfig(isComposite, historyType);
		}
		storageConfig.setTable(tableName);
		return storageConfig;
	}

	private TLStorage createStorageAnnotation(AbstractStorageBase.Config<?> storage) {
		TLStorage storageAnnotation = newConfigItem(TLStorage.class);
		storageAnnotation.setImplementation(storage);
		return storageAnnotation;
	}

	/**
	 * The {@link ConfigurationItem} for this class.
	 * <p>
	 * This method is necessary, as {@link #getConfig()} does not declare the actual type and is
	 * intentionally final.
	 * </p>
	 */
	public Config getConfigTyped() {
		return (Config) getConfig();
	}

}
