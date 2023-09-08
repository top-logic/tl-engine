/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.bpml.importer;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;

import com.top_logic.basic.i18n.log.I18NLog;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.xio.importer.XmlImporter;
import com.top_logic.xio.importer.binding.ModelBinding;

/**
 * {@link XmlImporter} loading BPMN.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BPMLImporter {

	private XmlImporter _importer;

	/**
	 * Creates a {@link BPMLImporter}.
	 *
	 * @param log
	 *        See
	 *        {@link XmlImporter#newInstance(I18NLog, com.top_logic.basic.io.Content)}
	 */
	public BPMLImporter(I18NLog log) {
		_importer = XmlImporter.newInstance(log, definition());
	}

	private ClassRelativeBinaryContent definition() {
		return new ClassRelativeBinaryContent(BPMLImporter.class, "importBpml.xml");
	}

	/**
	 * Import the BPML.
	 */
	public Collaboration importBPML(ModelBinding binding, Source source) throws XMLStreamException {
		return (Collaboration) _importer.importModel(binding, source);
	}

}
