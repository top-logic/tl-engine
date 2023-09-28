/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.importer;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.exception.ErrorSeverity;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.basic.xml.XsltUtil;
import com.top_logic.element.config.ModelConfig;
import com.top_logic.element.config.ModuleConfig;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.element.model.diff.apply.ApplyModelPatch;
import com.top_logic.element.model.diff.compare.CreateModelPatch;
import com.top_logic.element.model.diff.config.DiffElement;
import com.top_logic.element.model.export.ModelConfigExtractor;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.form.values.edit.ConfigLabelProvider;
import com.top_logic.layout.form.values.edit.annotation.AcceptedTypes;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;
import com.top_logic.layout.messagebox.CreateConfigurationDialog;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.structure.DefaultDialogModel;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLModel;
import com.top_logic.model.config.TypeConfig;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * {@link CommandHandler} requesting a model definition upload and applying the changes to the
 * current application model.
 * 
 * <p>
 * This handler is able to process <i>TopLogic</i> model definition XML files and UML/XMI model
 * definitions.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class XMIModelImportHandler extends AbstractCommandHandler {

	private static final String TL_MODEL_NS = "http://www.top-logic.com/ns/dynamic-types/6.0";
	private static final String XMI_NS = "http://www.omg.org/spec/XMI/20131001";

	/**
	 * Creates a {@link XMIModelImportHandler}.
	 */
	public XMIModelImportHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * GUI-Definition for the model upload dialog.
	 */
	public interface ModelUpload extends ConfigurationItem {
		/**
		 * The model file to process.
		 */
		@InstanceFormat
		@ItemDisplay(ItemDisplayType.VALUE)
		@AcceptedTypes({ "text/xml", "application/xml", "application/vnd.xmi+xml" })
		@Mandatory
		BinaryData getModelDefinition();

		/**
		 * Whether this model import should replace or extend existing modules.
		 * 
		 * <ul>
		 * <li><code>true</code> if types of imported modules are added to the types of existing
		 * modules. A type that is imported and has the same name as an existing type replaces
		 * it.</li>
		 * <li><code>false</code> if imported modules replace existing modules.</li>
		 * </ul>
		 */
		@BooleanDefault(true)
		boolean isPartialImport();
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {
		return new CreateConfigurationDialog<>(ModelUpload.class, DefaultDialogModel.dialogModel(I18NConstants.UPLOAD_MODEL_Definition,
			DisplayDimension.px(400), DisplayDimension.px(200)),
			this::processModelData).open(aContext);
	}

	private HandlerResult processModelData(ModelUpload upload) {
		try {
			String ns;
			BinaryData modelDefinition = upload.getModelDefinition();
			boolean isPartialImport = upload.isPartialImport();
			try (InputStream in = modelDefinition.getStream()) {
				XMLStreamReader reader = XMLStreamUtil.getDefaultInputFactory().createXMLStreamReader(in);
				XMLStreamUtil.nextStartTag(reader);
				ns = reader.getNamespaceURI();
			}

			if (XMI_NS.equals(ns)) {
				try {
					return importXmiModel(modelDefinition, isPartialImport);
				} catch (TransformerException ex) {
					throw new TopLogicException(I18NConstants.ERROR_INVALID_MODEL_DEFINITION, ex)
						.initSeverity(ErrorSeverity.WARNING)
						.initDetails(ResKey.text(ex.getMessage()));
				}
			} else if (TL_MODEL_NS.equals(ns)) {
				return importTlModel(modelDefinition, isPartialImport);
			} else {
				throw new TopLogicException(I18NConstants.ERROR_INVALID_MODEL_DEFINITION)
					.initSeverity(ErrorSeverity.WARNING)
					.initDetails(
						I18NConstants.ERROR_INVALID_MODEL_NAMESPACE__FOUND_XMI_TL.fill(ns, XMI_NS,
							TL_MODEL_NS));
			}
		} catch (ConfigurationException ex) {
			throw new TopLogicException(I18NConstants.ERROR_CANNOT_PARSE_MODEL, ex)
				.initSeverity(ErrorSeverity.WARNING);
		} catch (IOException | XMLStreamException ex) {
			throw new TopLogicException(I18NConstants.ERROR_CANNOT_PARSE_MODEL, ex)
				.initSeverity(ErrorSeverity.WARNING)
				.initDetails(ResKey.text(ex.getMessage()));
		}
	}

	private HandlerResult importXmiModel(BinaryData modelDefinition, boolean isPartialImport)
			throws TransformerException, IOException, ConfigurationException {
		try (InputStream in = modelDefinition.getStream()) {
			Source modelSource = new StreamSource(in);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(out);
			newTransformer().transform(modelSource, result);

			return importTlModel(BinaryDataFactory.createBinaryData(out.toByteArray()), isPartialImport);
		}
	}

	private Transformer newTransformer() {
		TransformerFactory factory = XsltUtil.safeTransformerFactory();

		StreamSource transformationSource =
			new StreamSource(XMIModelImportHandler.class.getResourceAsStream("xmi-to-tl-model.xslt"));

		try {
			return factory.newTransformer(transformationSource);
		} catch (TransformerConfigurationException ex) {
			throw (AssertionError) new AssertionError("Transformer cannot be instantiated.").initCause(ex);
		}
	}

	private HandlerResult importTlModel(BinaryData modelDefinition, boolean isPartialImport)
			throws ConfigurationException {
		ModelConfigExtractor extractor = new ModelConfigExtractor();
		DynamicModelService service = (DynamicModelService) ModelService.getInstance();

		TLModel targetModel = service.getModel();
		ModelConfig oldConfig = extractor.visitModel(targetModel, null);

		Protocol log = new BufferingProtocol() {
			@Override
			protected RuntimeException createAbort() {
				return new TopLogicException(I18NConstants.ERROR_INVALID_MODEL_DEFINITION)
					.initDetails(ResKey.text(getError()));
			}
		};
		TLModel oldModel = service.loadTransientModel(log, oldConfig);
		log.checkErrors();

		ModelConfig newConfig = TypedConfiguration.copy(oldConfig);
		
		ModelConfig importedConfig = TypedConfiguration.parse("model", ModelConfig.class, modelDefinition);
		for (ModuleConfig importedModule : importedConfig.getModules()) {
			ModuleConfig existingConfig = newConfig.getModule(importedModule.getName());

			if (existingConfig != null) {
				if (isPartialImport) {
					for (TypeConfig importedType : importedModule.getTypes()) {
						Collection<TypeConfig> existingTypes = existingConfig.getTypes();

						existingTypes.remove(existingConfig.getType(importedType.getName()));
						existingTypes.add(importedType);
					}
				} else {
					newConfig.getModules().remove(existingConfig);
					DynamicModelService.addTLObjectExtension(importedModule);
					newConfig.getModules().add(importedModule);
				}
			} else {
				DynamicModelService.addTLObjectExtension(importedModule);
				newConfig.getModules().add(importedModule);
			}
		}

		TLModel newModel = service.loadTransientModel(log, newConfig);
		log.checkErrors();

		CreateModelPatch patchCreator = new CreateModelPatch();
		patchCreator.addPatch(oldModel, newModel);

		List<DiffElement> patch = patchCreator.getPatch();
		if (patch.isEmpty()) {
			InfoService.showInfo(I18NConstants.NO_MODIFICATIONS);
		} else {
			HTMLFragment title = Fragments.message(I18NConstants.APPLY_CHANGES);
			HTMLFragment message = new HTMLFragment() {
				@Override
				public void write(DisplayContext context, TagWriter out) throws IOException {
					ConfigLabelProvider labelProvider = new ConfigLabelProvider();
					out.beginTag(H2);
					out.append(context.getResources().getString(I18NConstants.THE_FOLLOWING_CHANGES_ARE_APPLIED));
					out.endTag(H2);
					out.beginTag(UL);
					for (DiffElement diff : patch) {
						out.beginTag(LI);
						out.append(labelProvider.getLabel(diff));
						out.endTag(LI);
					}
					out.endTag(UL);
				}
			};
			CommandModel ok = MessageBox.button(ButtonType.OK, new Command() {
				@Override
				public HandlerResult executeCommand(DisplayContext context) {
					Logger.info("Applying model patch: " + patch, XMIModelImportHandler.class);

					try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
						ApplyModelPatch apply = new ApplyModelPatch(log, targetModel, service.getFactory());
						apply.applyPatch(patch);
						apply.complete();

						tx.commit();
					}
					return HandlerResult.DEFAULT_RESULT;
				}
			});
			return MessageBox
				.confirm(DefaultDisplayContext.getDisplayContext(),
					new DefaultLayoutData(DisplayDimension.dim(600, DisplayUnit.PIXEL), 100,
						DisplayDimension.dim(800, DisplayUnit.PIXEL), 100, Scrolling.AUTO),
					true, title, message, ok, MessageBox.button(ButtonType.CANCEL));
		}
		return HandlerResult.DEFAULT_RESULT;
	}

}
