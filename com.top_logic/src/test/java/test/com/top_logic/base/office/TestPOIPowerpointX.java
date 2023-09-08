/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.office;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.base.office.ppt.HyperlinkDefinition;
import com.top_logic.base.office.ppt.POIPowerpointX;
import com.top_logic.base.office.ppt.POIPowerpointXUtil;
import com.top_logic.base.office.ppt.Powerpoint;
import com.top_logic.base.office.ppt.SlideReplacement;
import com.top_logic.base.office.ppt.SlideReplacerX;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Settings;
import com.top_logic.basic.col.ListBuilder;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.tool.export.AbstractOfficeExportHandler.OfficeExportValueHolder;

/**
 * Test class for {@link POIPowerpointX}
 * 
 * @author <a href=mailto:kbu@top-logic.com>kbu</a>
 */
public class TestPOIPowerpointX extends BasicTestCase {

    /**
     * Constructor for TestPowerpoint.
     */
    public TestPOIPowerpointX(String aName) {
        super(aName);
    }


	/**
	 * Test setting values in pptx template
	 * 
	 * @throws Exception
	 *         if tested code does
	 */
	public void testSetValues() throws Exception {
		File tmpFile = File.createTempFile("PowerpointExportHandler", ".pptx", Settings.getInstance().getTempDir());
		try (InputStream pptFile = getPPTFile()) {
			Map<String, Object> theResult = new HashMap<>();
			theResult.put(POIPowerpointXUtil.PREFIX_VALUE + "TITLE", "PowerPoint mit POI");
			theResult.put(POIPowerpointXUtil.PREFIX_VALUE + "TOPIC", "Arbeiten mit POI");
			theResult.put(POIPowerpointXUtil.PREFIX_VALUE + "HYPERLINK",
				new HyperlinkDefinition("file:c:\\Users\\test\\Desktop", "Hyperlink"));
			theResult.put(POIPowerpointXUtil.PREFIX_VALUE + "HYPERLINK_2",
				new HyperlinkDefinition("file:c:/Test mit Leerzeichen.txt", "Hyperlink"));
			theResult.put(POIPowerpointXUtil.PREFIX_VALUE + "FIXED_TABLE_TEST", "Tabellen mit fester Spaltenanzahl");
			List<Object> theTable = new ListBuilder<>().add("aaaaa1")
				.add("aaaaa2")
				.add("aaaaa3")
				.add("aaaaa4")
				.add("bbbbb1")
				.add("bbbbb2")
				.add("bbbbb3")
				.add("bbbbb4").toList();
			theResult.put(POIPowerpointXUtil.PREFIX_FIXEDTABLE + "4x2", theTable);
			theResult.put(POIPowerpointXUtil.PREFIX_VALUE + "CELL_1", "Zelle 1");
			theResult.put(POIPowerpointXUtil.PREFIX_VALUE + "CELL_2", "Zelle 2");
			theResult.put(POIPowerpointXUtil.PREFIX_VALUE + "CELL_3", "Zelle 3");
			theResult.put(POIPowerpointXUtil.PREFIX_VALUE + "CELL_4", "Zelle 4");
			theResult.put(POIPowerpointXUtil.PREFIX_VALUE + "CELL_5", "Zelle 5");
			theResult.put(POIPowerpointXUtil.PREFIX_VALUE + "CELL_6", "Zelle 6");
			theResult.put(POIPowerpointXUtil.PREFIX_PICTURE + "CELL_7", getTestImage());
			theResult.put(POIPowerpointXUtil.PREFIX_PICTURE + "CELL_8", getTestImageFile());
			
			theResult.put(POIPowerpointXUtil.PREFIX_PICTURE + "TEST", getTestImageFile());
			theResult.put(POIPowerpointXUtil.PREFIX_PICTURE + "LEFT", getTestImageFile());
			theResult.put(POIPowerpointXUtil.PREFIX_PICTURE + "RIGHT", getTestImageFile());
			theResult.put(POIPowerpointXUtil.PREFIX_PICTURE + "CENTER_HORIZONTAL", getTestImageFile());
			theResult.put(POIPowerpointXUtil.PREFIX_PICTURE + "TOP", getTestImage2File());
			theResult.put(POIPowerpointXUtil.PREFIX_PICTURE + "BOTTOM", getTestImage2File());
			theResult.put(POIPowerpointXUtil.PREFIX_PICTURE + "CENTER_VERTICAL", getTestImage2File());
			Object[][] table = new Object[][] {
				{ "42", new HyperlinkDefinition("http://www.top-logic.com", "Link to TL"), "34%", "250 TEUR", "5",
					"Risiko 1", getTestImageFile(), "Maßnahme 1",
				"30.04.2012" },
				{ "48", new HyperlinkDefinition(1, "Link to slide 1"), "25%", "150 TEUR", "3", "Risiko 2", "2",
					"Maßnahme 2", "28.04.2012" },
					{ "", new HyperlinkDefinition("http://www.top-logic.com"), "", "", "", "", "3", getTestImageFile(),
					"29.04.2012" },
					{ "49", new HyperlinkDefinition(1), "10%", "10 TEUR", "Chance", "Risiko 3", "", "", "Aus is" },
					{ "42", new HyperlinkDefinition("http://www.top-logic.com", "Link to TL"), "34%", "250 TEUR", "5",
						"Risiko 1", getTestImageFile(), "Maßnahme 1",
						"30.04.2012" },
					{ "48", new HyperlinkDefinition(1, "Link to slide 1"), "25%", "150 TEUR", "3", "Risiko 2", "2",
						"Maßnahme 2", "28.04.2012" },
					{ "", new HyperlinkDefinition("http://www.top-logic.com"), "", "", "", "", "3", getTestImageFile(),
						"29.04.2012" },
					{ "49", new HyperlinkDefinition(1), "10%", "10 TEUR", "Chance", "Risiko 3", "", "", "Aus is" },
			};
			theResult.put(POIPowerpointXUtil.PREFIX_FIXEDTABLE + "RISKS", table);

			table = new Object[][] { new Object[] { Color.green } };
			theResult.put(POIPowerpointXUtil.PREFIX_FIXEDTABLE + "REPLACED_BY_COLOR", table);

			// collect data for all issues
			ArrayList<OfficeExportValueHolder> slideData = new ArrayList<>();
			for (int outerCounter = 1; outerCounter < 4; outerCounter++) {
				Map<String, Object> values = new HashMap<>();

				values.put(POIPowerpointXUtil.PREFIX_PICTURE + "SLIDE_PIC", getTestImageFile());
				values.put(POIPowerpointXUtil.PREFIX_VALUE + "SLIDE_TITLE", "Ein Titel für Kopie " + outerCounter);
				slideData.add(new OfficeExportValueHolder("TestPoiPowerPointSlide.pptx", "dummyname.pptx", values,
					true));

				// collect data for all issues
				ArrayList<OfficeExportValueHolder> innerSlideData = new ArrayList<>();
				for (int innerCounter = 1; innerCounter < 4; innerCounter++) {
					Map<String, Object> innerValues = new HashMap<>();

					innerValues.put(POIPowerpointXUtil.PREFIX_PICTURE + "SLIDE_PIC", getTestImageFile());
					innerValues.put(POIPowerpointXUtil.PREFIX_VALUE + "SLIDE_TITLE", "Ein Titel für innere Kopie "
						+ innerCounter);
					innerValues.put(POIPowerpointXUtil.PREFIX_VALUE + "SLIDE_HYPERLINK",
						new HyperlinkDefinition("http://www.top-logic.com", "Hyperlink"));
					innerSlideData.add(new OfficeExportValueHolder("TestPoiPowerPointInnerSlide.pptx",
						"dummyname.pptx", innerValues,
						true));
				}

				// add the master slide to the export data
				values.put(POIPowerpointXUtil.PREFIX_ADDSLIDES + "TEST", new SlideReplacement(
					"TestPoiPowerPointInnerSlide.pptx", innerSlideData));

			}

			// add the master slide to the export data
			theResult.put(POIPowerpointXUtil.PREFIX_ADDSLIDES + "TEST", new SlideReplacement(
				"TestPoiPowerPointSlide.pptx", slideData));

			theResult.put(POIPowerpointXUtil.PREFIX_SLIDECONTENT + "REPLACER",
				getData("SlideContentReplacement.pptx"));

			new POIPowerpointX4Test().setValuesDirect(pptFile, tmpFile, theResult);
		}
    }

	/**
	 * Sub class to use local test file path
	 * 
	 * @author <a href=mailto:kbu@top-logic.com>kbu</a>
	 */
	protected class POIPowerpointX4Test extends POIPowerpointX {
		/**
		 * Create a new POIPowerpointX4Test ...
		 */
		public POIPowerpointX4Test() {
			super();
		}

		@Override
		protected SlideReplacerX createReplacer() {
			return new SlideReplacerX4Test(this);
		}
	}

	/**
	 * The powerpoint file to be used for testing.
	 */
	protected InputStream getPPTFile() throws Exception {
		return getDataStream("TestPoiPowerPoint.pptx");
	}

	private InputStream getDataStream(String fileName) throws IOException {
		return FileManager.getInstance().getStream(resourceName(fileName));
	}

	private BinaryData getData(String fileName) {
		return FileManager.getInstance().getData(resourceName(fileName));
	}

	private static String resourceName(String fileName) {
		return ModuleLayoutConstants.PATH_TO_MODULE_ROOT + "/" + ModuleLayoutConstants.SRC_TEST_DIR
			+ "/test/com/top_logic/base/office/data/" + fileName;
	}

	/**
	 * Sub class to use local test files
	 * 
	 * @author <a href=mailto:kbu@top-logic.com>kbu</a>
	 */
	protected class SlideReplacerX4Test extends SlideReplacerX {
		/**
		 * Create a new SlideReplacerX4Test ...
		 * 
		 * @param aPpt
		 *        the POIPowerpointX
		 */
		public SlideReplacerX4Test(POIPowerpointX aPpt) {
			super(aPpt);
		}

		@Override
		protected InputStream getTemplateFileInputStream(String aFilename) throws IOException {
			File theFile =
				FileManager.getInstance().getIDEFileOrNull(ModuleLayoutConstants.PATH_TO_MODULE_ROOT + "/" + ModuleLayoutConstants.SRC_TEST_DIR
			+ "/test/com/top_logic/base/office/data/" + aFilename);

			if (theFile.exists()) {
				return (new FileInputStream(theFile));
			} else {
				return (null);
			}
		}
	}

    /**
	 * The {@link File} containing the image to be embed into the presentation.
	 */
	public static File getTestImageFile() throws Exception {
		return getImaageFile("TestImage.jpg");
	}

	/**
	 * The {@link File} containing the image to be embed into the presentation.
	 */
	public static File getTestImage2File() throws Exception {
		return getImaageFile("TestImage2.png");
	}

	private static File getImaageFile(String fileName) throws IOException {
		File theFile = FileManager.getInstance().getIDEFile(resourceName(fileName));
		assertTrue(theFile.exists());
		return theFile;
	}

	/**
	 * The {@link BinaryData} containing the image to be embed into the presentation.
	 */
	public static BinaryData getTestImage() throws Exception {
		return BinaryDataFactory.createBinaryData(getTestImageFile());
    }

	/**
	 * The {@link BinaryData} containing the image to be embed into the presentation.
	 */
	public static BinaryData getTestImage2() throws Exception {
		return BinaryDataFactory.createBinaryData(getTestImage2File());
	}

    /**
     * The filename for the resulting powerpoint presentation.
     */
    protected File getTargetFile() throws IOException {
		return BasicTestCase.createTestFile("TestPoiPowerpointX", Powerpoint.PPTX_EXT);
    }

	/**
	 * the suite
	 */
    public static Test suite () {
		TestSuite theSuite = new TestSuite(TestPOIPowerpointX.class);
		return TLTestSetup.createTLTestSetup(theSuite);
    }

	/**
	 * Run as test
	 * 
	 * @param args
	 *        the args (not used locally)
	 */
    public static void main(String[] args) {
        Logger.configureStdout();
        junit.textui.TestRunner.run(suite ());
    }
}

