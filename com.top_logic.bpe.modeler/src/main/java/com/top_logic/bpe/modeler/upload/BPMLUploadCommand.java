/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.modeler.upload;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.exception.ErrorSeverity;
import com.top_logic.basic.i18n.log.BufferingI18NLog;
import com.top_logic.basic.i18n.log.BufferingI18NLog.Entry;
import com.top_logic.basic.i18n.log.I18NLog;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.logging.Level;
import com.top_logic.bpe.bpml.importer.BPMLImporter;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.component.AbstractCreateCommandHandler;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;
import com.top_logic.xio.importer.binding.ApplicationModelBinding;
import com.top_logic.xio.importer.binding.ModelBinding;

/**
 * {@link CommandHandler} importing a BPML file.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BPMLUploadCommand extends AbstractCreateCommandHandler {
	
	/**
	 * Creates a {@link BPMLUploadCommand} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public BPMLUploadCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Object createObject(LayoutComponent component, Object createContext, FormContainer formContainer,
			Map<String, Object> arguments) {
		UploadForm form = (UploadForm) EditorFactory.getModel(formContainer);
		BinaryData data = form.getData();
		
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		
		Collaboration collaboration;
		try {
			collaboration = importCollaboration(DefaultDisplayContext.getDisplayContext(), kb, data);
		} catch (XMLStreamException | IOException ex) {
			throw reportProblem(ex);
		}
		if (StringServices.isEmpty(collaboration.getName())) {
			collaboration.setName(strip(data.getName()));
		}

		return collaboration;
	}

	/**
	 * Loads the BPML from the given data and creates a {@link Collaboration}.
	 *
	 * @param context
	 *        The command context.
	 * @param kb
	 *        The target {@link KnowledgeBase}.
	 * @param data
	 *        The BPML source code.
	 * @return The newly created {@link Collaboration}.
	 */
	public static Collaboration importCollaboration(DisplayContext context, KnowledgeBase kb, BinaryContent data)
			throws XMLStreamException, IOException {
		try (InputStream in = data.getStream()) {
			ModelBinding binding = new ApplicationModelBinding(kb, ModelService.getApplicationModel());
			return BPMLUploadCommand.importBPML(new StreamSource(in), binding);
		}
	}

	static Collaboration importBPML(Source source, ModelBinding binding)
			throws XMLStreamException {
		BufferingI18NLog errors = new BufferingI18NLog();
		I18NLog log = errors.filter(Level.ERROR).tee(
			new LogProtocol(BPMLUpdateCommand.class)
				.asI18NLog(Resources.getSystemInstance())
				.filter(Level.FATAL));
		BPMLImporter importer = new BPMLImporter(log);
		Collaboration newCollaboration = importer.importBPML(binding, source);
		if (errors.hasEntries()) {
			// Hack to get a structured error display: Join multiple errors in a chain of
			// TopLogicException instances.
			Throwable details = null;
			List<Entry> entries = errors.getEntries();
			for (int n = entries.size() - 1; n >= 0; n--) {
				Entry e = entries.get(n);
				details = new TopLogicException(e.getMessage(), details);
			}
			throw new TopLogicException(I18NConstants.ERROR_IMPORT_FAILED, details).initSeverity(ErrorSeverity.WARNING);
		}
		return newCollaboration;
	}

	private static String strip(String name) {
		int slashIndex = name.lastIndexOf('/');
		if (slashIndex >= 0) {
			name = name.substring(slashIndex + 1);
		}
		int dotIndex = name.indexOf('.');
		if (dotIndex >= 0) {
			name = name.substring(0, dotIndex);
		}
		return name;
	}
	
	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return CombinedExecutabilityRule.combine(super.intrinsicExecutability(), DataAvailableRule.INSTANCE);
	}

}
