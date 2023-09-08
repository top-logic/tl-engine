/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.config.diff.filediff;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.config.diff.filediff.AbstractLine;
import com.top_logic.config.diff.filediff.FileDiff;
import com.top_logic.config.diff.filediff.FileDiffGenerator;
import com.top_logic.config.diff.filediff.LinePart;
import com.top_logic.config.diff.filediff.Region;
import com.top_logic.config.diff.filediff.TempLine;
import com.top_logic.config.diff.filediff.TempLine.TempDestLine;
import com.top_logic.config.diff.filediff.TempLine.TempSourceLine;
import com.top_logic.config.diff.filediff.TextPart;
import com.top_logic.config.diff.google.diff_match_patch.Operation;

/**
 * This is the testclass, associated with the {@link FileDiffGenerator}, and implicitly 
 * the {@link FileDiff} class.
 * Contains the static classes {@link ExpectedDiff} and {@link ExpectedRegion}, to better
 * generate expected results to compare to.
 *  
 * @author     <a href="mailto:aru@top-logic.com">aru</a>
 */
public class TestFileDiffGenerator extends TestCase {

	FileDiffGenerator fileDiffGenerator = FileDiffGenerator.INSTANCE;

	public TestFileDiffGenerator(String string) {
		super(string);
	}

	/**
	 * Test if the FileDiffGenerators constructor works
	 */
	public void testGenerate() {
		// generation
		assertNotNull(getFileDiffGenerator());
	}

	/**
	 * Test if a generation run with null doesn't fail
	 */
	public void testGenerateNullTest() {
		// null test
		assertNotNull(getFileDiffGenerator().generate(null, null));
	}

	/**
	 * Test if the normalization works
	 */
	public void testNormalization(){
		String source = "Good Dog.";
		String dest = "Bad Dog!";
		List<LinePart> normalization = getFileDiffGenerator().getNormalization(source, dest);
		FileDiff fileDiff = getFileDiffGenerator().generateFileDiff(normalization);
		assertNotNull(fileDiff);
		// todo add better assertions
	}

	/**
	 * Tests the generation with two small Strings.
	 */
	public void testGenerationWithSmallStrings() {
		// small Strings
		String testText1 = "Good Dog.";
		String testText2 = "Bad Dog!";

		FileDiff fileDiff = getFileDiffGenerator().generate(testText1, testText2); 
		assertNotNull(fileDiff);
	}
	/**
	 *	Tests if an insertion works
	 */
	public void testInsertLine() {
		String source = "unchanged line\n" + 
		"abc\n" + 
		"unchanged line\n";

		String dest = "unchanged line\n" + 
		"def\n" + 
		"unchanged line\n";

		ExpectedRegion er1 = new ExpectedRegion();
		er1.setChanged(false);
		er1.addLine("unchanged line\n");

		ExpectedRegion er2 = new ExpectedRegion();
		er2.setChanged(true);
		er2.addSourceLine("abc\n");
		er2.addDestLine("def\n");

		ExpectedRegion er3 = new ExpectedRegion();
		er3.setChanged(false);
		er3.addLine("unchanged line\n");

		ExpectedDiff expDiff = new ExpectedDiff();
		expDiff.addRegion(er1);
		expDiff.addRegion(er2);
		expDiff.addRegion(er3);

		FileDiff resultDiff = getFileDiffGenerator().generate(source, dest);

		check(expDiff, resultDiff);		
	}

	/** 
	 * A Test to check if the generation of a last new Line at the end works (flushing)
	 */
	public void testGenerationWithNewLineAtEnd() {
		String source = "abc";	
		String dest = "abc\n";

		ExpectedRegion er1 = new ExpectedRegion();
		er1.setChanged(true);
		er1.addSourceLine("abc");
		er1.addDestLine("abc\n");

		ExpectedDiff expDiff = new ExpectedDiff();
		expDiff.addRegion(er1);

		FileDiff resultDiff = getFileDiffGenerator().generate(source, dest);

		check(expDiff, resultDiff);		
	}

	/**
	 * Test if the generation of two last new Lines, source- and destLines works (flushing)
	 */
	public void testIfGenerateNewLinesAtEnd() {
		String source = "a\n"+
		"b";	
		String dest = "a\n"+
		"b\n"+
		"x\n"+
		"y";

		ExpectedRegion er1 = new ExpectedRegion();
		er1.setChanged(false);
		er1.addLine("a\n");

		ExpectedRegion er2 = new ExpectedRegion();
		er2.setChanged(true);
		er2.addSourceLine("b");
		er2.addDestLine("b\n");
		er2.addDestLine("x\n");
		er2.addDestLine("y");

		ExpectedDiff expDiff = new ExpectedDiff();
		expDiff.addRegion(er1);
		expDiff.addRegion(er2);

		FileDiff resultDiff = getFileDiffGenerator().generate(source, dest);

		check(expDiff, resultDiff);		
	}

	/**
	 * Test if the algorithm can deal with wrapping lines
	 */
	public void testGenerationWithLineWrap() {
		String source = "abcdef\n" + 
		"ghijkl\n" + 
		"mnopqr\n";

		String dest = "123abc\n" + 
		"defghi\n" + 
		"jklmno\n" +
		"pqr\n";

		// Building expDiff		
		ExpectedRegion er1 = new ExpectedRegion();
		er1.setChanged(true);

		er1.addSourceLine("abcdef\n");
		er1.addSourceLine("ghijkl\n");
		er1.addSourceLine("mnopqr\n");		

		er1.addDestLine("123abc\n");
		er1.addDestLine("defghi\n");
		er1.addDestLine("jklmno\n");
		er1.addDestLine("pqr\n");

		ExpectedDiff expDiff = new ExpectedDiff();
		expDiff.addRegion(er1);
		// end building expdiff

		FileDiff resultDiff = getFileDiffGenerator().generate(source, dest);
		check(expDiff, resultDiff);
		getFileDiffGenerator().generateHtmlToFile(resultDiff,
			ModuleLayoutConstants.SRC_TEST_DIR + "/test/data/diffHtmlCompare/bernhardTestDiff.html", "s", "d");
	}

	/**
	 * Test if the html generation succeeds
	 */
	public void testGenerationOfHTML() throws Exception {
	
		String source = "unchanged line\n" + 
		"abc\n" + 
		"unchanged line\n";

		String dest = "unchanged line\n" + 
		"def\n" + 
		"unchanged line\n";

		ExpectedRegion er1 = new ExpectedRegion();
		er1.setChanged(false);
		er1.addLine("unchanged line");

		ExpectedRegion er2 = new ExpectedRegion();
		er2.setChanged(true);
		er2.addSourceLine("abc");
		er2.addDestLine("def");

		ExpectedRegion er3 = new ExpectedRegion();
		er3.setChanged(false);
		er3.addLine("unchanged line");

		ExpectedDiff expDiff = new ExpectedDiff();
		expDiff.addRegion(er1);
		expDiff.addRegion(er2);
		expDiff.addRegion(er3);

		FileDiff resultDiff = getFileDiffGenerator().generate(source, dest);
		getFileDiffGenerator().generateHtmlToFile(
				resultDiff, 
			ModuleLayoutConstants.SRC_TEST_DIR + "/test/data/diffHtmlCompare/diffHtmlTestResult.html",
				"SourceTitle", 
				"DestTitle");
		
		File diffExpectedHtml =
			new File(ModuleLayoutConstants.SRC_TEST_DIR + "/test/data/diffHtmlCompare/diffHtmlTestExpected.html");
		String expectedHTML = FileUtilities.readFileToString(diffExpectedHtml);

		File diffResultHtml =
			new File(ModuleLayoutConstants.SRC_TEST_DIR + "/test/data/diffHtmlCompare/diffHtmlTestResult.html");
		String resultHTML =FileUtilities.readFileToString(diffResultHtml);
		
		assertEquals(expectedHTML, resultHTML);
	}

	/**
	 * Some basic tests for a {@link Region}
	 */
	public void testRegions() {

		Region region = new Region(false);

		assertNotNull(region.toString());
		assertFalse(region.isChanged());
		assertNotNull(region.getDestLines());
		assertNotNull(region.getSourceLines());

		TempLine tempSourceLine = new TempSourceLine();
		TempLine tempDestLine   = new TempDestLine();
		LinePart lp1 = new TextPart("blah", Operation.EQUAL, 0);
		LinePart lp2 = new TextPart("blubb", Operation.EQUAL, 0);

		tempSourceLine.addLinePart(lp1);
		tempDestLine.addLinePart(lp2);
		region.addDestLine(tempDestLine);
		region.addSourceLine(tempSourceLine);

		assertFalse(region.isChanged());
		assertNotNull(region.getDestLines());
		assertNotNull(region.getSourceLines());
	}

	/**
	 * Some basic tests for a {@link TempLine}
	 */
	public void testLines() {
		TempLine tempSourceLine = new TempSourceLine();
		TempLine tempDestLine   = new TempDestLine();

		assertNotNull(tempSourceLine);
		assertNotNull(tempDestLine);

		LinePart lp1 = new TextPart("blah", Operation.EQUAL, 0);
		LinePart lp2 = new TextPart("blubb", Operation.EQUAL, 0);
		tempSourceLine.addLinePart(lp1);
		tempDestLine.addLinePart(lp2);

		assertEquals(lp1, tempSourceLine.getLineParts().get(0));
		assertEquals(lp2, tempDestLine.getLineParts().get(0));
		assertEquals(lp1.hashCode(), lp1.hashCode());
		assertTrue(lp1.equals(lp1));
	}

	/**
	 * Test if the {@link FileDiff} generation with big {@link String}s works
	 * <p>
	 * ->This test will fail if not run with at least the following VM argument: 
	 * -<i>X</i>ms<b>128m</b> -<i>X</i>mx<b>2048m</b>
	 */
	@SuppressWarnings("deprecation")
	public void testGenerationWithBigStrings() {
		// big Strings
		//		String randomtext1 = StringServices.getRandomWords(new Random(123), 64*1000000);
		//		String randomtext2 = StringServices.getRandomWords(new Random(321), 64*1000000);

		String source = "meier\nmüller\n";
		String dest = "meier\nmüller\n";

		for(int j=0;j<10;j++){
			int n1 = Double.valueOf(Math.random() * 12).intValue();
			int n2 = Double.valueOf(Math.random() * 12).intValue();

			for(int i=0;i<n1;i++){
				//				source = source+StringServices.getRandomWords(new Random(123), Double.valueOf(Math.random()*100).intValue())+"\n";
			}		
			for(int i=0;i<n2;i++){
				dest = dest + StringServices.getRandomWords(new Random(321), Double.valueOf(Math.random() * 100).intValue())
					+ "\n";
			}		
			source = source+"meier\nmüller\n";
			dest = dest+"meier\nmüller\n";
		}
		//		assertNotNull(getFileDiffGenerator().generate(randomtext1, randomtext2));
		FileDiff resultDiff = getFileDiffGenerator().generate(source, dest);
		getFileDiffGenerator().generateHtmlToFile(resultDiff,
			ModuleLayoutConstants.SRC_TEST_DIR + "/test/data/diffHtmlCompare/diff-unchangedLines.html", "s", "d");

	}

	public void testfma() {

		for(int k=0;k<1;k++){
			String source = "meier\nmüller\n";
			String dest = "meier\nmüller\n";

			for(int j=0;j<10;j++){		
				for(int i=0;i<10;i++){
					dest = dest+"schulze";
					String hlp="";
					for(int l=0;l<k;l++){
						hlp=hlp+"xx";
					}
					dest=dest+hlp+"\n";
				}		
				source = source+"meier\nmüller\n";
				dest = dest+"meier\nmüller\n";
			}
			List<LinePart> normalization = getFileDiffGenerator().getNormalization(source, dest);

			System.out.println(""+k+" parts:"+normalization.size());
		FileDiff resultDiff = getFileDiffGenerator().generate(source, dest);
			getFileDiffGenerator().generateHtmlToFile(resultDiff,
				ModuleLayoutConstants.SRC_TEST_DIR + "/test/data/diffHtmlCompare/difffma.html", "s", "d");
			
		}
	}

	public FileDiffGenerator getFileDiffGenerator() {
		return (fileDiffGenerator);
	}

	/**
	 * Checks if two FileDiffs are equal
	 */
	private void check(ExpectedDiff expDiff, FileDiff resultDiff) {
		int numResultRegions= resultDiff.getRegions().size();
		int numExpected = expDiff.getRegions().size();
		assertEquals(numExpected, numResultRegions);

		for(int i=0;i<numResultRegions;i++){
			Region result = resultDiff.getRegions().get(i);
			ExpectedRegion expected = expDiff.getRegions().get(i);
			if(expected.isChanged()){
				assertTrue(result.isChanged());
				for(int j=0; j< expected.getSourceLines().size();j++){
					String expString = expected.getSourceLines().get(j);
					AbstractLine line = result.getSourceLines().get(j);
					assertEquals(expString,line.getText());
				}
				for(int j=0; j< expected.getDestLines().size();j++){
					String expString = expected.getDestLines().get(j);
					AbstractLine line = result.getDestLines().get(j);
					assertEquals(expString,line.getText());
				}
			}else{
				assertFalse(result.isChanged());
				for(int j=0; j< expected.getLines().size();j++){
					String expString = expected.getLines().get(j);
					AbstractLine line = result.getSourceLines().get(j);
					assertEquals(expString,line.getText());
				}
			}
		}
	}



	/**
	 * Class to assist in generate Data to compare resulting {@link FileDiff}s with.
	 * Allows to describe what a expected result is for a FileDiffs region. 
	 * 
	 * @author    <a href=mailto:aru@top-logic.com>aru</a>
	 */
	private static class ExpectedRegion {

		private boolean changed;
		private List<String> lines;
		private List<String> sourceLines;
		private List<String> destLines;

		ExpectedRegion(){
			lines = new ArrayList<>();
			sourceLines = new ArrayList<>();
			destLines = new ArrayList<>();
		}

		public List<String> getDestLines() {
			return destLines;
		}

		public List<String> getSourceLines() {
			return sourceLines;
		}

		public List<String> getLines() {
			return lines;
		}

		public boolean isChanged() {
			return changed;
		}

		public void addDestLine(String string) {
			destLines.add(string);
		}

		public void addSourceLine(String string) {
			sourceLines.add(string);
		}

		public void setChanged(boolean b) {
			this.changed = b;
		}

		public void addLine(String string) {
			lines.add(string);
		}

	}



	/**
	 * Class to assist in generate Data to compare resulting {@link FileDiff}s with.
	 * Allows to describe what a expected resulting FileDiff is. 
	 * 
	 * @author    <a href=mailto:aru@top-logic.com>aru</a>
	 */
	private static class ExpectedDiff {
		private List<ExpectedRegion> regions;

		ExpectedDiff(){
			regions = new ArrayList<>();
		}

		public List<ExpectedRegion> getRegions() {
			return regions;
		}


		public void addRegion(ExpectedRegion er) {
			regions.add(er);
		}

	}

	/** 
	 * Needed to tie this TestClass to the TestAll
	 */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(TestFileDiffGenerator.class);
	}

}
