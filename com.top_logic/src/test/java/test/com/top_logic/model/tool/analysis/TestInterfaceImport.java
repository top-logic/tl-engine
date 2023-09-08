/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.tool.analysis;

import java.io.File;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;

import junit.framework.Test;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.binding.xml.ModelWriter;
import com.top_logic.model.export.XMIExporter;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.tool.analysis.InterfaceImport;

/**
 * Test case for {@link InterfaceImport}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestInterfaceImport extends BasicTestCase {
	
	public void testAnalysis() throws XMLStreamException, IOException, TransformerException {
		TLModel model = new TLModelImpl();
		try {
			new InterfaceImport(model).importInterface(TLModelPart.class);
		} catch (IllegalArgumentException ex) {
			/* Exptected due to the known bug in ticket #10494: TLModelPart is not a correct
			 * configuration hierarchy. */
			return;
		}
		fail("Ticket #10494: Remove failure wrapping above, since TLModelPart seems now to be correct.");
		
		File xmlFile = BasicTestCase.createNamedTestFile("tl-model.xml");
		File xmiFile = BasicTestCase.createNamedTestFile("tl-model.xmi");
		ModelWriter.writeModel(xmlFile, model, true);
		new XMIExporter().toXMI(model, xmiFile);
	}
	
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(TestInterfaceImport.class);
	}
	
}
