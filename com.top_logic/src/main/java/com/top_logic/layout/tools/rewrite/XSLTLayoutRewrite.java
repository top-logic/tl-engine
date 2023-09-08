/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools.rewrite;

import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.XsltUtil;

/**
 * {@link LayoutRewrite} implementation based on a configured XSL transformation.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class XSLTLayoutRewrite extends AbstractConfiguredInstance<XSLTLayoutRewrite.Config<?>>
		implements LayoutRewrite {

	private Transformer _transformer;

	private Set<String> _templates;

	private Log _log;

	/**
	 * Configuration options for {@link XSLTLayoutRewrite}.
	 */
	public interface Config<I extends XSLTLayoutRewrite> extends PolymorphicConfiguration<I> {

		/**
		 * The layout template names for which argument configurations must be rewritten.
		 */
		@ListBinding()
		@Name("templates")
		List<String> getTemplates();

		/**
		 * The XSLT resource.
		 * 
		 * <p>
		 * The value is a resource name starting with a '/' character rooted at the web application
		 * root folder. It typically starts with the prefix <code>/WEB-INF/kbase/migration/</code>.
		 * </p>
		 * 
		 * @see FileManager#getStream(String)
		 */
		@Mandatory
		@Name("transform")
		String getTransform();

	}

	/**
	 * Creates a {@link XSLTLayoutRewrite} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public XSLTLayoutRewrite(InstantiationContext context, Config<?> config) {
		super(context, config);

		_templates = new HashSet<>(config.getTemplates());
	}

	@Override
	public void init(Log log) {
		_log = log;
		Config<?> config = getConfig();
		String transform = config.getTransform();
		log.info("Applying '" + transform + "' to in-app layout definitions.");

		_transformer = XsltUtil.createTransformer(transform, false);
	}

	@Override
	public boolean rewriteLayout(String layoutKey, String template, Document document) {
		if (!isRelevantTemplate(template)) {
			return false;
		}

		// API is not optimal for implementing by XSL transformation, must dump before, since
		// transformation must be inline.
		String inputXml = DOMUtil.toString(document);

		// Clear.
		while (document.getFirstChild() != null) {
			document.removeChild(document.getFirstChild());
		}

		try {
			_transformer.transform(new StreamSource(new StringReader(inputXml)), new DOMResult(document));
		} catch (TransformerException ex) {
			_log.error("Layout '" + layoutKey + "' transformation failed for template '" + template + "': " + inputXml,
				ex);
		}

		return true;
	}

	private boolean isRelevantTemplate(String template) {
		return _templates.contains(template);
	}

}
