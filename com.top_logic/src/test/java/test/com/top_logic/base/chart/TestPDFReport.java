/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.chart;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Random;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.FontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

import com.top_logic.basic.Logger;

/** 
 * This class is no real test case. 
 * This class shows how you can write a pdf document 
 * with internal and external links and images. 
 *
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public class TestPDFReport extends BasicTestCase {

    /** Counter for the chart in the document. */
    private int chartCount = 1;
    
    public void testGeneratePDFReport() throws Exception{
        Document      theDocument = new Document();
        String        theTmpPath  = "./tmp/pdf/";
        File theDir = new File(theTmpPath);
        if(!theDir.exists()){
            theDir.mkdirs();
        }
        OutputStream  theOut        = new FileOutputStream(theTmpPath + "testPDFReport.pdf");
        PdfWriter     thePdfWriter = PdfWriter.getInstance(theDocument, theOut);
        
        /* Set meta data to the pdf document. */
        theDocument.addTitle   ("Title");
        theDocument.addSubject ("This example explains how to add metadata.");
        theDocument.addKeywords("BOS,Business Operation Systems, Report");
        theDocument.addCreator ("Thomas Dickhut");
        theDocument.addAuthor  ("Thomas Dickhut");
        
        /* Open the document to work with it. */
        theDocument.open();
        try{
            setReportTitle(theDocument, "Report Title");
            setParagraphWithExternalLink(theDocument, "Before the external link. ", "<i>TopLogic</i>", "http://www.top-logic.com", " After the external link.");
            setSomeParagraphs(theDocument, "This is a text. This is a text. This is a text. This is a text. This is a text. This is a text. This is a text. ", 5);
            setChart(theDocument, thePdfWriter);
            setSomeParagraphs(theDocument, "This is a text. This is a text. This is a text. This is a text. This is a text. This is a text. This is a text. ", 5);
            theDocument.newPage();
            setTable(theDocument);
            setSomeParagraphs(theDocument, "This is a text. This is a text. This is a text. This is a text. This is a text. This is a text. This is a text. ", 5);
            setParagraphWithLocalLink(theDocument, "I am a text before the link. ", "Click here. ", "anchor1", "I am a text after the link.");
            setSomeParagraphs(theDocument, "This is a text. This is a text. This is a text. This is a text. This is a text. This is a text. This is a text. ", 5);
            setChart(theDocument, thePdfWriter);
            setSomeParagraphs(theDocument, "This is a text. This is a text. This is a text. This is a text. This is a text. This is a text. This is a text. ", 5);
            setParagraphWithLocalAnchor(theDocument, "This paragraph contains a ", "anchor. ", "anchor1", " Here comes nothing more. ");
            setSomeParagraphs(theDocument, "This is a text. This is a text. This is a text. This is a text. This is a text. This is a text. This is a text. ", 5);
            /*
             * Images are inserted exclusively into existing pages. That is,
             * for a image no new page is put on. Caution, if a image stands
             * at the end of a document and it does not fit no more on the last
             * page, then this image is swallowed. 
             */
            theDocument.newPage();
            theDocument.newPage();
            theDocument.newPage();
        }
        finally{
            theDocument.close();
            theOut     .close();
        }
    }

    /** 
     * This method sets a count of paragraphs to the given document.
     */
    private void setSomeParagraphs(Document aDocument, String aText, int aParagraphCount) throws DocumentException{
        for(int i=0; i<aParagraphCount; i++){
            aDocument.add(new Paragraph(aText));
        }
    }
    
    /** 
     * This method sets a chart with align center and spaces before and after.
     */
    private void setChart(Document aDocument, PdfWriter thePdfWriter) throws BadElementException, DocumentException {
        int theWidth  = 500;
        int theHeight = 400;
        PdfContentByte theContent  = thePdfWriter.getDirectContent();
        FontMapper     theMapper   = new DefaultFontMapper();
        PdfTemplate    theTemplate = theContent.createTemplate(theWidth, theHeight);
        Graphics2D     theG2       = theTemplate.createGraphics(theWidth, theHeight, theMapper);
        Rectangle2D    theR2D      = new Rectangle2D.Double(0, 0, theWidth, theHeight);
        getChart().draw(theG2, theR2D, null);
        theG2.dispose();
        
        Image theImage = Image.getInstance(theTemplate);
        theImage.setSpacingBefore(20f);
        theImage.setAlignment(Image.ALIGN_CENTER);
        theImage.setSpacingAfter(20f);
        aDocument.add(theImage);
    }

    /** 
     * This method sets a table with a title and 25 rows and 3 columns.
     */
    private void setTable(Document theDocument) throws DocumentException {
        PdfPTable table = new PdfPTable(3);
        table.setSpacingBefore(20f);
        PdfPCell cell = new PdfPCell(new Paragraph("Table Title"));
        cell.setColspan(3);
        cell.setBorderColor    (new Color(255, 0, 0));
        cell.setBackgroundColor(Color.lightGray);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        table.addCell("Row: 01 Column: 1");
        table.addCell("Row: 01 Column: 2");
        table.addCell("Row: 01 Column: 3");
        for(int j=2; j<25; j++){
            String theFillChar = "";
            if(j<10){
                theFillChar    = "0";
            }
            cell = new PdfPCell(new Paragraph("Row: " + theFillChar + j + " Column: 1"));
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph("Row: " + theFillChar + j + " Column: 2"));
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph("Row: " + theFillChar + j + " Column: 3"));
            table.addCell(cell);
        }
        table.setSpacingAfter(20f);
        theDocument.add(table);
    }

    private void setParagraphWithLocalAnchor(Document aDocument, String aTextBefore, String anAnchorLabel, String anAnchor, String aTextAfter) throws DocumentException{
        Paragraph theParagraph = new Paragraph(aTextBefore);
        theParagraph.add(new Chunk(anAnchorLabel, FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL, new Color(0, 255, 0))).setLocalDestination(anAnchor));
        theParagraph.add(aTextAfter);
        aDocument.add(theParagraph);
    }
    
    private void setParagraphWithLocalLink(Document aDocument, String aTextBefore, String aLinkLabel, String anAnchor, String aTextAfter) throws DocumentException{
        Paragraph theParagraph = new Paragraph(aTextBefore);
        theParagraph.add(new Chunk(aLinkLabel, FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL, new Color(0, 0, 255))).setLocalGoto(anAnchor));
        theParagraph.add(aTextAfter);
        aDocument.add(theParagraph);
    }
    
    /** 
     * This method sets a paragraph with an external link.
     */
    private void setParagraphWithExternalLink(Document aDocument, String aTextBefore, String aLinkLabel, String anAnchor, String aTextAfter) throws Exception{
        Paragraph theParagraph = new Paragraph(aTextBefore);
        theParagraph.add(new Chunk(aLinkLabel, FontFactory.getFont(FontFactory.HELVETICA, 12, Font.UNDERLINE, new Color(0, 0, 255))).setAnchor(new URL(anAnchor)));
        theParagraph.add(aTextAfter);
        aDocument.add(theParagraph);
    }
    
    /** 
     * This method sets the report title.
     */
    private void setReportTitle(Document theDocument, String aTitle) throws DocumentException {
        Paragraph theTitle = new Paragraph(aTitle, FontFactory.getFont(FontFactory.HELVETICA, 20));
        theTitle.setAlignment(Paragraph.ALIGN_CENTER);
        theTitle.setSpacingAfter(30f);
        theDocument.add(theTitle);
    }

    /** 
     * This method returns a random pie chart.
     * 
     * @return Returns a random pie chart.
     */
    private JFreeChart getChart(){
        JFreeChart theChart = ChartFactory.createPieChart("Pie Chart for the category " + chartCount++, 
                                           getDataSet(), 
                                           true  /* Legend */, 
                                           false /* Tooltip */, 
                                           false /* URLs */);
        
       theChart.setBackgroundPaint(Color.WHITE); 
       theChart.setBorderVisible(false);
       return theChart;
    }
    
    /**
     * This method returns a random data set for a pie chart.
     * 
     * @return Returns a random data set for a pie chart.
     */
    private PieDataset getDataSet(){
        DefaultPieDataset theDataset = new DefaultPieDataset();
        Random            theRandom  = new Random();
        String[] series        = new String[]{"Series 1", 
                                                      "Series 2", 
                                                      "Series 3", 
                                                      "Series 4", 
                                                      "Series 5"};
        
        for(int j=0; j<series.length; j++){
			theDataset.setValue(series[j], Integer.valueOf(theRandom.nextInt(100)));
        }
        return theDataset;
    }
    
    /**
     * This method summarizes all tests in a suite and returns it.
     * 
     * @return The Suite of all Tests.
     */
    public static Test suite() {
        return TLTestSetup.createTLTestSetup(new TestSuite(TestPDFReport.class)); 
    }

    /**
     * This main method is for direct testing.
     */
    public static void main(String[] args) {
        SHOW_TIME               = true;
        Logger.configureStdout  ("ERROR"); // "INFO"

        TestRunner.run(suite());
    }
    
}
