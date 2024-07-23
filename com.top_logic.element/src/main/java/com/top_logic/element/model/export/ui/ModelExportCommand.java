/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.export.ui;

import java.io.CharArrayWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.element.config.ModelConfig;
import com.top_logic.element.config.ModuleConfig;
import com.top_logic.element.meta.schema.ElementSchemaConstants;
import com.top_logic.element.model.export.ModelConfigExtractor;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.progress.DefaultProgressInfo;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLType;
import com.top_logic.model.config.ModelPartConfig;
import com.top_logic.model.config.TypeConfig;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.AbstractDownloadHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerUtil;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.I18NConstants;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link CommandHandler} performing a model export in the <i>TopLogic</i>-native configuration syntax.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ModelExportCommand extends AbstractDownloadHandler {

	/**
	 * Configuration options for {@link ModelExportCommand}.
	 */
	public interface Config extends AbstractDownloadHandler.Config {

		@Override
		@FormattedDefault(TARGET_SELECTION_SELF)
		ModelSpec getTarget();

	}

	/**
	 * Creates a {@link ModelExportCommand} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ModelExportCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {
		if (model == null) {
			throw new TopLogicException(I18NConstants.ERROR_NO_MODEL);
		}

		return super.handleCommand(aContext, aComponent, model, someArguments);
	}

	@Override
	protected Object prepareDownload(LayoutComponent aComponent, DefaultProgressInfo progressInfo, Map<String, Object> arguments)
			throws Exception {
		return CommandHandlerUtil.getTargetModel(this, aComponent, arguments);
	}

	@Override
	public String getDownloadName(LayoutComponent aComponent, Object download) {
		return TLModelUtil.qualifiedName(part(download)) + ".model.xml";
	}

	private TLModelPart part(Object download) {
		return (TLModelPart) download;
	}

	@Override
	public BinaryDataSource getDownloadData(Object download) throws Exception {
		return toXML(part(download));
	}

	/**
	 * Creates a pretty-printed XML representation of the given {@link TLModelPart} ({@link TLType}
	 * or {@link TLModule}).
	 */
	public static BinaryData toXML(TLModelPart part) throws XMLStreamException {
		CharArrayWriter buffer = new CharArrayWriter();
		serializeModuleOrType(buffer, part, ElementSchemaConstants.MODEL_6_NS);
		String xml = XMLPrettyPrinter.prettyPrint(buffer.toString());
		return BinaryDataFactory.createBinaryData(xml.getBytes(Charset.forName("utf-8")), "text/xml",
			TLModelUtil.qualifiedName(part) + ".model.xml");
	}

	/**
	 * Serialized the given {@link TLModelPart} ({@link TLType} or {@link TLModule}) to the given
	 * {@link Writer} (without pretty-printing).
	 * 
	 * @see #toXML(TLModelPart)
	 */
	public static void serializeModuleOrType(Writer out, TLModelPart part, String namespace) throws XMLStreamException {
		ModelPartConfig partConfig = part.visit(new ModelConfigExtractor(), null);
		ModelConfig config = TypedConfiguration.newConfigItem(ModelConfig.class);
		if (part.getModelKind() == ModelKind.MODULE) {
			config.getModules().add((ModuleConfig) partConfig);
		} else {
			TLModule module = ((TLType) part).getModule();
			ModuleConfig moduleConfig = TypedConfiguration.newConfigItem(ModuleConfig.class);
			moduleConfig.setName(TLModelUtil.qualifiedName(module));
			moduleConfig.getTypes().add((TypeConfig) partConfig);
			config.getModules().add(moduleConfig);
		}

		TypedConfiguration.minimize(config);

		try (ConfigurationWriter writer = new ConfigurationWriter(out)) {
			if (!StringServices.isEmpty(namespace)) {
				writer.setNamespace("", namespace);
			}
			writer.write(ElementSchemaConstants.ROOT_ELEMENT, ModelConfig.class, config);
		}
	}

	@Override
	public void cleanupDownload(Object model, Object download) {
		// Ignore.
	}

}
