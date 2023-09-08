/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.diff.filediff;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.Configuration;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.config.diff.CompareStatistics;
import com.top_logic.config.diff.RootCompareStatistic;
import com.top_logic.config.diff.filediff.TempLine.TempDestLine;
import com.top_logic.config.diff.filediff.TempLine.TempSourceLine;
import com.top_logic.config.diff.google.diff_match_patch;
import com.top_logic.config.diff.google.diff_match_patch.Diff;
import com.top_logic.config.diff.google.diff_match_patch.Operation;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Computes the differences between two to compared texts, computing a {@link FileDiff}-object.
 * 
 * <p>
 * The <code>generateHTML()</code> method allows to output the result as a html-file. To achieve
 * this, the two texts are processed by Googles diff_match_patch-library, generating a
 * {@link LinkedList} of {@link Diff}s. In a following normalization step, the list of diffs ist
 * processed to a list {@link LinePart}s. From this {@link LinePart}s, lines are computed.
 * {@link Line}s are organized in {@link Region}s. Those {@link Region}s can contain {@link Line}s
 * with changes - inserted and deleted Text - or those without. If a {@link Region} contains changed
 * lines, a boolean flag, <code>isChanged</code>, is set.
 * </p>
 * 
 * <p>
 * The information about changes in parts of lines and regions is used for documentation and to
 * support a better visualization of the differences at the UI.
 * </p>
 * 
 * @author <a href="mailto:aru@top-logic.com">aru</a>
 */
public class FileDiffGenerator {

	private static final String PREFIX = "ConfigDiff";
	private static final String SEEN = PREFIX + "Seen";
	private static final String UNSEEN = PREFIX + "Unseen";
	private static final String INSERT = PREFIX + "Insert";
	private static final String DELETE = PREFIX + "Delete";
	private static final String UNCHANGED_LINE_D = PREFIX + "UnchangedLineD";
	private static final String CHANGED_LINE_D = PREFIX + "ChangedLineD";
	private static final String UNCHANGED_LINE_NUMBER_D = PREFIX + "UnchangedLineNumberD";
	private static final String CHANGED_LINE_NUMBER_D = PREFIX + "ChangedLineNumberD";
	private static final String UNCHANGED_LINE_NUMBER_S = PREFIX + "UnchangedLineNumberS";
	private static final String CHANGED_LINE_NUMBER_S = PREFIX + "ChangedLineNumberS";
	private static final String UNCHANGED_LINE_S = PREFIX + "UnchangedLineS";
	private static final String CHANGED_LINE_S = PREFIX + "ChangedLineS";
	private static final String HIGHLIGHTED  = "ConfigDiffHighlighted";
	private static final String HIGHLIGHTED_LR  = "ConfigDiffHighlightedLR";
	public static final String ANCHOR_PREFIX = "CHANGED_REGION_";
	public static final String END_PREFIX = "END_CHANGED_REGION_";
	public static final String CHANGED_TABLEROW_PREFIX = "TABLEROW_CHANGED_REGION_";
	public static final String TABLE_ROW = PREFIX;
	public static final String TABLE_HEAD = PREFIX;
	public static final String TABLE_HEAD_LR = PREFIX + "THLR";
	public static final String DIFF_TABLE = PREFIX + "Table";

	private boolean markViewedChange;

	public static FileDiffGenerator INSTANCE = new FileDiffGenerator();
	

	public FileDiffGenerator() {
		this(true);
	}
	
	public FileDiffGenerator(boolean markViewedChange) {
		this.markViewedChange = markViewedChange;
	}

	/**
	 * Googles diff-match-patch is invoked to generate the differences,
	 * represented by a linked list of {@link Diff}-objects, the
	 * {@link FileDiff} generation is based on. The Diffs getting normalized and
	 * afterwards the FileDiff is generated.
	 * 
	 * @return generateFileDiff(normalizedDiffs)
	 */
	public FileDiff generate(String sourceText, String destText) {
		List<LinePart> normalizedDiffs = getNormalization(sourceText, destText);
		return generateFileDiff(normalizedDiffs);
	}

	/**
	 * This method triggers the computation of the {@link LinkedList} of
	 * {@link Diff}s and normalizes the result.
	 * 
	 * @return normalize(diffMatchPatchDiffs)
	 */
	public List<LinePart> getNormalization(String sourceText, String destText) {
		if (sourceText == null) {
			sourceText = "";
		}
		if (destText == null) {
			destText = "";
		}
		List<Diff> diffMatchPatchDiffs = computeDiffMatchPatchDiffs(sourceText,
				destText);
		return normalize(diffMatchPatchDiffs);
	}

	/**
	 * This method processes a {@link List} of {@link Diff}s and returns a List
	 * of {@link LinePart}s. A LinePart is basically a Diff, with the exception,
	 * that a Diff contains a text, possibly with newLines in it, and an
	 * operation. The {@link Operation} describes what was done with this text.
	 * Was it inserted or deleted, or was it simply equal in both compared
	 * texts. <br/>
	 * The text contained in LineParts are without newLines. To achieve this,
	 * the Diffs are splitted at a newLine into two LineParts. Nevertheless the
	 * information if a line is ending is crucial to compute {@link Line}s and
	 * {@link Region}s, so LineParts have a boolean flag, <tt>hasEndOfLine</tt>,
	 * to indicate this.
	 * 
	 * @return lineParts
	 */
	private List<LinePart> normalize(List<Diff> diffs) {
		List<LinePart> lineParts = new ArrayList<>();

		int diffIndex = 0;
		for (Diff diff : diffs) {
			String text = diff.text;
			Matcher nlMatcher = Pattern.compile("\r?\n|\r").matcher(text);
			int textStart = 0;
			while (textStart < text.length()) {
				boolean nlFound = nlMatcher.find();
				
				int textEnd;
				if (nlFound) {
					textEnd = nlMatcher.start();
				} else {
					textEnd = text.length();
				}

				boolean textFound = textEnd > textStart;
				if (textFound) {
					lineParts.add(new TextPart(text.substring(textStart, textEnd), diff.operation, diffIndex));
				}
				
				if (nlFound) {
					int nlEnd = nlMatcher.end();
					
					lineParts.add(new NLPart(text.substring(textEnd, nlEnd), diff.operation, diffIndex));
					
					textStart = nlEnd;
				} else {
					textStart = textEnd;
				}
			}
			
			diffIndex++;
		}
		return lineParts;
	}

	/**
	 * This method is the core of the {@link FileDiffGenerator} Class. Here,
	 * based on the computed {@link LinePart}s, the {@link FileDiff} is build. <br/>
	 * A FileDiff contains {@link Region}s with or without changes. Those
	 * regions containing {@link Line}s, representing the lines in the compared
	 * texts. Lines again are lists of {@link LinePart}s. a LinePart describes a
	 * part of a lines text and if it was inserted, deleted, or simply not
	 * changed.
	 * 
	 * @return fileDiff
	 */
	public FileDiff generateFileDiff(List<LinePart> lineParts) {
		TempLine tempSourceLine = new TempSourceLine();
		TempLine tempDestLine = new TempDestLine();
		/*
		 * Process the lineParts, one by one
		 */
		for (LinePart part : lineParts) {
			/*
			 * A tempSourcLine, btw. a tempDestLine knows what kind of linePart
			 * it accepts
			 */
			tempSourceLine.offerLinePart(part);
			tempDestLine.offerLinePart(part);
		}
		tempSourceLine.flush();
		tempDestLine.flush();

		return new DiffBuilder().createDiff(tempSourceLine.getProducedLines(), tempDestLine.getProducedLines());
	}

	/**
	 * This Method computes from the texts to compare, the list of {@link Diff}s
	 * that represents the changes made on those texts. This is done with
	 * Googles diff_match_patch library.
	 */
	private List<Diff> computeDiffMatchPatchDiffs(String sourceText,
			String destText) {
		diff_match_patch theDiffMatchPatch = new diff_match_patch();
		theDiffMatchPatch.Diff_Timeout = getTimeOut();
		LinkedList<Diff> diffs = theDiffMatchPatch.diff_main(sourceText, destText, false);

		/*
		 * It is possible to use a so called semantic cleanup. This means that
		 * not all parts of lines - so possibly parts of words - are marked whit
		 * its changes (or not), but that changes are marked on basis of a word
		 * (whitespace seperated text). if there is, for example, in a name one
		 * character changed, with the semantic cleanup the hole name will be
		 * marked as changed, not just this one character.
		 */
		// theDiffMatchPatch.diff_cleanupSemantic(diffs);
		return diffs;
	}

    /**
     * Returns the timeout in seconds to cancel the diff algorithm after.
     * 
     * <strong>Note: </strong> zero means that no timeout is desired
     * 
     * @return the diff algorithm timeout in seconds
     */
    private int getTimeOut() {
        final String theValue = Configuration.getConfiguration(FileDiffGenerator.class).getValue("diffTimeOut", "10");
        return Integer.valueOf(theValue);
    }

	public String generateHtmlToString(FileDiff aFileDiff, 
			String aSource,
			String aDest, 
			CompareStatistics aStatistic) {

		StringWriter theWriter = new StringWriter();
		doWriteHTML(aFileDiff, theWriter, aSource, aDest,aStatistic);
		return theWriter.getBuffer().toString();
	}

	/**
	 * This Method generates a HTML-file in {@link StreamUtilities#ENCODING}, representing the
	 * changes between the compared texts, documented by the FileDiff.
	 * 
	 * @see #generateHtmlToFile(FileDiff, String, String, String, String)
	 */
	public void generateHtmlToFile(FileDiff aFileDiff, String fileNameWithPath, String aSource, String aDest) {
		generateHtmlToFile(aFileDiff, fileNameWithPath, StreamUtilities.ENCODING, aSource, aDest);
	}

	/**
	 * This Method generates a HTML-file, representing the changes between the compared texts,
	 * documented by the FileDiff.
	 * <p>
	 * For the highlighting of changes regions and changed parts of lines, an accompanying CSS-file
	 * is needed!
	 * </p>
	 */
	public void generateHtmlToFile(FileDiff aFileDiff, String fileNameWithPath, String encoding, String aSource,
			String aDest) {
		try {
			FileOutputStream stream = new FileOutputStream(fileNameWithPath);
			try {
				Writer writer = new OutputStreamWriter(stream, encoding);
				try {
					doWriteHTML(aFileDiff, writer, aSource, aDest, new RootCompareStatistic());
				} finally {
					writer.close();
				}
			} finally {
				stream.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * writes the actual HTML-code of the comparison
	 */
	private void doWriteHTML(FileDiff aFileDiff, Writer writer, String aSource,
			String aDest, CompareStatistics aStatistic) {
		try {
			
			
			if(aStatistic.getCurrentChangedRegion()==-1){
				if(aStatistic.getNumOfChangedRegions()>0){
					aStatistic.setCurrentChangedRegion(1);
					aStatistic.markChangedRegionAsEvaluated(1);
				}
			}else{
//				aStatistic.markChangedRegionAsEvaluated(aStatistic.getCurrentChangedRegion());
			}
			
			
			TagWriter tw = new TagWriter(writer);
			tw.setIndent(false);

//			////////// Header /////////////////////////////////////////////////////////
//			tw.beginTag(HTML);
//			tw.beginTag(HEAD);
//			tw.endTag(HEAD);
//
//			////////// Body ///////////////////////////////////////////////////////////
//			tw.beginTag(BODY);

			/////////////// Table ////////////////////////////////////////////////////
			
//			tw.beginBeginTag(DIV);
//			tw.writeAttribute(STYLE_ATTR, "margin: 10px");
//			tw.endBeginTag();
			
			tw.beginBeginTag(TABLE);
			tw.writeAttribute(CLASS_ATTR, DIFF_TABLE);
			tw.writeAttribute("cellspacing", "0");
			tw.writeAttribute(WIDTH_ATTR, "100%");
			tw.endBeginTag();

			tw.beginTag(TR);

			//////////////////// Header of Table ///////////////////////////////////
			tw.beginBeginTag(TH);
			tw.writeAttribute(CLASS_ATTR, TABLE_HEAD);
			tw.writeAttribute(WIDTH_ATTR, "36");
			tw.endBeginTag();
			{
				tw.writeText("#");
			}
			tw.endTag(TH);

			tw.beginBeginTag(TH);
			tw.writeAttribute(CLASS_ATTR, TABLE_HEAD_LR);
			tw.writeAttribute(WIDTH_ATTR, "44%");
			tw.endBeginTag();
			{
				tw.writeText(aSource);
			}
			tw.endTag(TH);

			tw.beginBeginTag(TH);
			tw.writeAttribute(CLASS_ATTR, TABLE_HEAD);
			tw.writeAttribute(WIDTH_ATTR, "10");
			tw.endBeginTag();
			{
				tw.writeText("C");
			}
			tw.endTag(TH);

			tw.beginBeginTag(TH);
			tw.writeAttribute(CLASS_ATTR, TABLE_HEAD_LR);
			tw.writeAttribute(WIDTH_ATTR, "36");
			tw.endBeginTag();
			{
				tw.writeText("#");
			}
			tw.endTag(TH);

			tw.beginBeginTag(TH);
			tw.writeAttribute(CLASS_ATTR, TABLE_HEAD);
			tw.writeAttribute(WIDTH_ATTR, "44%");
			tw.endBeginTag();
			{
				tw.writeText(aDest);
			}
			tw.endTag(TH);

			tw.endTag(TR);

			/////////////////// table content ////////////////////////////////////////
			int sourceLineCounter = 1;
			int destLineCounter = 1;
			int anchorCounter = 0;
			int changedRegionCounter = 0;
			List<Region> regions = aFileDiff.getRegions();

			for (Region region : regions) {

				if (region.isChanged()) {
					changedRegionCounter++;
				}
				boolean anchorSet = false;

				int linesInRegion;
				if (region.getSourceLines().size() >= region.getDestLines().size()) {
					linesInRegion = region.getSourceLines().size();
				} else {
					linesInRegion = region.getDestLines().size();
				}

				int lineCounter = 0;

				if (region.isChanged() && !anchorSet) {
					anchorCounter++;
				}

				List<AbstractLine> sourceLines = region.getSourceLines();
				Iterator<AbstractLine> sourceLinesIterator = sourceLines.iterator();

				List<AbstractLine> destLines = region.getDestLines();
				Iterator<AbstractLine> destLinesIterator = destLines.iterator();

				boolean printSourceLine = true;
				boolean printDestLine = true;
				
				boolean hasSourceLine = false;
				boolean hasDestLine = false;
				
				AbstractLine sourceLine = null;
				AbstractLine destLine = null;
				while (true) {
					if (printSourceLine) {
						hasSourceLine = sourceLinesIterator.hasNext();
						
						if (hasSourceLine) {
							sourceLine = sourceLinesIterator.next();
						} else {
							sourceLine = null;
						}
					}
					
					if (printDestLine) {
						hasDestLine = destLinesIterator.hasNext();
						
						if (hasDestLine) {
							destLine = destLinesIterator.next();
						} else {
							destLine = null;
						}
					}
					
					if (!(hasSourceLine || hasDestLine)) {
						break;
					}
					

					lineCounter++;

					if (hasSourceLine) {
						printSourceLine = sourceLine.getLineParts().get(0).getDstLine() <= destLineCounter || destLine==null;
					} else {
						printSourceLine = false;
					}
						
					if (hasDestLine) {
						printDestLine = destLine.getLineParts().get(0).getSrcLine() <= sourceLineCounter || sourceLine == null;
					} else {
						printDestLine = false;
					}
					
					
					if ((!printSourceLine) && (! printDestLine)) {
						throw new AssertionError("Invalid regions.");
					}
					
					tw.beginTag(TR);
					
					tw.beginBeginTag(TD);
					{
						tw.writeAttribute("valign", "top");
					}
					if (region.isChanged()) {
						tw.writeAttribute(CLASS_ATTR, CHANGED_LINE_NUMBER_S);
					} else {
						tw.writeAttribute(CLASS_ATTR, UNCHANGED_LINE_NUMBER_S);
					}

					tw.endBeginTag();
					{
						// startOfRegionMarker
						if (region.isChanged() && !anchorSet) {
							String regionID = ANCHOR_PREFIX + anchorCounter;
							tw.beginBeginTag(ANCHOR);
							tw.writeAttribute(ID_ATTR, regionID);
							tw.endEmptyTag();
							anchorSet = true;
						}
						// endOfRegionMarker
						if (region.isChanged() && (lineCounter == linesInRegion)) {
							String endRegionID = END_PREFIX + anchorCounter;
							tw.beginBeginTag(ANCHOR);
							tw.writeAttribute(ID_ATTR, endRegionID);
							tw.endEmptyTag();
						}
						if (printSourceLine) {
							tw.writeText("" + sourceLineCounter);
						}
					}
					tw.endTag(TD);

					tw.beginBeginTag(TD);
					tw.writeAttribute("valign", "top");
					if (region.isChanged()) {
						tw.writeAttribute(CLASS_ATTR, CHANGED_LINE_S);
					} else {
						tw.writeAttribute(CLASS_ATTR, UNCHANGED_LINE_S);
					}
					tw.endBeginTag();
					{
						if (printSourceLine) {
							markChangesInLine(tw, sourceLine);
						}
					}
					tw.endTag(TD);

					tw.beginBeginTag(TD);
					tw.writeAttribute("valign", "top");
					if (region.isChanged() 
						&& (!(aStatistic.getSeenChangedRegions().contains(Integer.valueOf(changedRegionCounter))))) {
						tw.writeAttribute(CLASS_ATTR, UNSEEN); 
					} else {
						tw.writeAttribute(CLASS_ATTR, SEEN);
					}
					tw.endBeginTag();
					{
						tw.writeText(NBSP);
					}
					tw.endTag(TD);

					tw.beginBeginTag(TD);
					tw.writeAttribute("valign", "top");
					if (region.isChanged()) {
						tw.writeAttribute(CLASS_ATTR, CHANGED_LINE_NUMBER_D);
					} else {
						tw.writeAttribute(CLASS_ATTR, UNCHANGED_LINE_NUMBER_D);
					}
					tw.endBeginTag();
					{
						if (printDestLine) {
							tw.writeText("" + destLineCounter);
						}
					}
					tw.endTag(TD);

					tw.beginBeginTag(TD);
					tw.writeAttribute("valign", "top");
					if (region.isChanged()) {
						tw.writeAttribute(CLASS_ATTR, CHANGED_LINE_D);
					} else {
						tw.writeAttribute(CLASS_ATTR, UNCHANGED_LINE_D);
					}
					tw.endBeginTag();
					{
						if (printDestLine) {
							markChangesInLine(tw, destLine);
						}
					}

					tw.endTag(TD);

					tw.endTag(TR);

					if (printSourceLine) {
						sourceLineCounter++;
					}
					if (printDestLine) {
						destLineCounter++;
					}
				}
			}
			tw.endTag(TABLE);

//			tw.endTag(DIV);
			
			if(markViewedChange) {
				tw.beginScript();
				
				tw.append("jumpToRegion('" + FileDiffGenerator.ANCHOR_PREFIX
						+ aStatistic.getCurrentChangedRegion() + "', " 
						+ aStatistic.getCurrentChangedRegion() + ", " 
						+ aStatistic.getNumOfChangedRegions() + ");");
				tw.endScript();
			}

//			// the getOffSet for the next current changed region is missing
			
//			tw.endTag(BODY);
//			tw.endTag(HTML);
			////////// End of HTML ////////////////////////////////////////////////////

			tw.flush();
			tw.close();

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Sets a highlighting color for the backround of the text of a changed
	 * linePart, discriminating between an insert and an delete operation.
	 * <p>
	 * An accompanying CSS-file is needed for this method to take effect!
	 */
	private void markChangesInLine(TagWriter tw, AbstractLine line)
	throws IOException {
		if (line.isUnchanged() == false) {
			for (LinePart linePart : line.getLineParts()) {
				String text = linePart.getText();
				if (linePart.getOperation().equals(Operation.DELETE)) {
					tw.beginBeginTag(SPAN);
					tw.writeAttribute(CLASS_ATTR, DELETE);
					tw.endBeginTag();
					writeContentText(tw, showNewlines(text));
					tw.endTag(SPAN);
				} else if (linePart.getOperation().equals(Operation.INSERT)) {
					tw.beginBeginTag(SPAN);
					tw.writeAttribute(CLASS_ATTR, INSERT);
					tw.endBeginTag();
					writeContentText(tw, showNewlines(text));
					tw.endTag(SPAN);
				} else { // EQUAL
					writeContentText(tw, hideNewLine(text));
				}
			}
		} else {
			writeContentText(tw, hideNewLine(line.getText()));
			/*
			 * More efficient this way if the text is really big. Avoids the
			 * unnessesary distinction of cases in the for-loop above.
			 */
		}
	}

	private String hideNewLine(String text) {
		return text.replaceAll("\r|\n", "");
	}

	private String showNewlines(String text) {
//		return text;
		return text.replaceAll("\r", "\u240D").replaceAll("\n", "\u240A");
	}

	private void writeContentText(TagWriter tw, String text) throws IOException {
		tw.writeText(text.replaceAll(" ", HTMLConstants.NBSP).replaceAll("\t", HTMLConstants.NBSP + HTMLConstants.NBSP + HTMLConstants.NBSP + HTMLConstants.NBSP));
	}

}
