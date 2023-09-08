/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.office.excel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.textui.TestRunner;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.PictureData;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.PaneInformation;

import test.com.top_logic.base.office.AbstractPOIExcelTest;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.office.excel.ExcelContext;
import com.top_logic.base.office.excel.POICellStyleProvider;
import com.top_logic.base.office.excel.POICellValueProvider;
import com.top_logic.base.office.excel.POIDrawingManager;
import com.top_logic.base.office.excel.POIExcelContext;
import com.top_logic.base.office.excel.POIExcelTemplate;
import com.top_logic.base.office.excel.POIExcelTemplate.POITemplateEntry;
import com.top_logic.base.office.excel.POIExcelUtil;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.dsa.DataAccessService;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;
import com.top_logic.layout.tree.model.DefaultMutableTreeNodeBuilder;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.layout.tree.model.TLTreeModelUtil;
import com.top_logic.layout.tree.renderer.DefaultColumnDeclaration;
import com.top_logic.layout.tree.renderer.DefaultTableDeclaration;
import com.top_logic.layout.tree.renderer.TableDeclaration;

/**
 * JUnit tests for {@link POIExcelUtil}.
 * 
 * @author <a href=mailto:wta@top-logic.com>wta</a>
 */
public class TestExcelUtil extends AbstractPOIExcelTest {

	/**
	 * The name of the XLSX-Document containing test data.
	 */
	public static final String FILE_NAME = "TestExcelUtil.xlsx";

	private static final String SHEET_EMPTY = "emptyContents";
	private static final String SHEET_INFORMATION = "sheetInformation";
	private static final String SHEET_TEMPLATES = "sheetTemplates";
	private static final String SHEET_CONTENTS = "sheetContents";
	private static final String SHEET_SIZES = "testSizes";

	/**
	 * @see POIExcelUtil#areRegionsEqual(CellRangeAddress, CellRangeAddress)
	 */
	public void testAreRegionsEqual() {
		final CellRangeAddress region1 = new CellRangeAddress(0, 1, 0, 1);
		final CellRangeAddress region2 = new CellRangeAddress(0, 2, 0, 2);
		final CellRangeAddress region3 = new CellRangeAddress(0, 1, 0, 1);

		assertFalse(POIExcelUtil.areRegionsEqual(region1, null));
		assertFalse(POIExcelUtil.areRegionsEqual(null, region2));
		assertFalse(POIExcelUtil.areRegionsEqual(region1, region2));
		assertTrue(POIExcelUtil.areRegionsEqual(region1, region1));
		assertTrue(POIExcelUtil.areRegionsEqual(region1, region3));
		assertTrue(POIExcelUtil.areRegionsEqual(null, null));
	}
	
	/**
	 * @throws Exception
	 *         if an error occurred while executing the test case
	 * 
	 * @see POIExcelUtil#computeHeight(Cell)
	 */
	public void testComputeHeight() throws Exception {
		final Workbook book = open();
		final Sheet sheet = book.getSheet(SHEET_SIZES);
		final float height = sheet.getDefaultRowHeight();

		assertEquals(height, (float) POIExcelUtil.computeHeight(POIExcelUtil.getCell(sheet, 0, 0)));
		assertEquals(2 * height, (float) POIExcelUtil.computeHeight(POIExcelUtil.getCell(sheet, 0, 2)));
	}

	/**
	 * @throws Exception
	 *         if an error occurred while executing the test case.
	 * 
	 * @see POIExcelUtil#computePhysicalColumnNumber(POITemplateEntry)
	 */
	public void testComputePhysicalColumnNumber() throws Exception {
		final Workbook book = open();
		final Sheet sheet = book.getSheet(SHEET_TEMPLATES);
		final POIExcelTemplate template = new POIExcelTemplate(sheet);
		final POITemplateEntry single = template.getEntry("single");
		final POITemplateEntry multiple = template.getEntry("multiple");
		final POITemplateEntry merged = template.getEntry("merged");

		assertEquals(1, POIExcelUtil.computePhysicalColumnNumber(single));
		assertEquals(2, POIExcelUtil.computePhysicalColumnNumber(multiple));
		assertEquals(3, POIExcelUtil.computePhysicalColumnNumber(merged));
	}

	/**
	 * @throws Exception
	 *         if an error occurred while executing the test case.
	 * 
	 * @see POIExcelUtil#computeWidth(Cell)
	 */
	public void testComputeWidth() throws Exception {
		final Workbook book = open();
		final Sheet sheet = book.getSheet(SHEET_SIZES);
		final int width = sheet.getDefaultColumnWidth() * 256;

		assertEquals(width, POIExcelUtil.computeWidth(POIExcelUtil.getCell(sheet, 0, 0)));
		assertEquals(2 * width, POIExcelUtil.computeWidth(POIExcelUtil.getCell(sheet, 0, 2)));
	}

	/**
	 * @throws Exception
	 *         if an error occurred while executing the test case.
	 * 
	 * @see POIExcelUtil#copyCell(Cell, Cell, boolean)
	 */
	public void testCopyCell() throws Exception {
		final Workbook book = open();
		final Sheet sheet = book.getSheet(SHEET_CONTENTS);

		final Cell source = POIExcelUtil.getCell(sheet, 0, 0);
		final Cell target1 = sheet.createRow(1).createCell(0);
		final Cell target2 = sheet.createRow(2).createCell(0);

		POIExcelUtil.copyCell(source, target1, false);
		assertEqualsCell(source, target1);
		assertFalse(source.getCellStyle().equals(target1.getCellStyle()));

		POIExcelUtil.copyCell(source, target2, true);
		assertEqualsCell(source, target2);
		assertEquals(source.getCellStyle(), target2.getCellStyle());
	}

	/**
	 * @throws Exception
	 *         if an error occurred while executing the test case.
	 * 
	 * @see POIExcelUtil#copyFooter(Sheet, Sheet)
	 */
	public void testCopyFooter() throws Exception {
		final Workbook book = open();
		final Sheet source = book.getSheet(SHEET_INFORMATION);
		final Sheet target = book.createSheet();

		POIExcelUtil.copyFooter(source, target);
		assertEquals(source.getFooter().getLeft(), target.getFooter().getLeft());
		assertEquals(source.getFooter().getCenter(), target.getFooter().getCenter());
		assertEquals(source.getFooter().getRight(), target.getFooter().getRight());
	}

	/**
	 * @throws Exception
	 *         if an error occurred while executing the test case.
	 * 
	 * @see POIExcelUtil#copyHeader(Sheet, Sheet)
	 */
	public void testCopyHeader() throws Exception {
		final Workbook book = open();
		final Sheet source = book.getSheet(SHEET_INFORMATION);
		final Sheet target = book.createSheet();

		POIExcelUtil.copyHeader(source, target);
		assertEquals(source.getHeader().getLeft(), target.getHeader().getLeft());
		assertEquals(source.getHeader().getCenter(), target.getHeader().getCenter());
		assertEquals(source.getHeader().getRight(), target.getHeader().getRight());
	}

	/**
	 * @throws Exception
	 *         if an error occurred while executing the test case.
	 * 
	 * @see POIExcelUtil#copyPaneInformation(Sheet, Sheet)
	 */
	public void testCopyPaneInformation() throws Exception {
		final Workbook book = open();
		final Sheet source = book.getSheet(SHEET_INFORMATION);
		final Sheet target = book.createSheet();

		POIExcelUtil.copyPaneInformation(source, target);

		PaneInformation srcPane = source.getPaneInformation();
		PaneInformation tgtPane = target.getPaneInformation();

		assertEquals(srcPane.isFreezePane(), tgtPane.isFreezePane());
		assertEquals(srcPane.getActivePane(), tgtPane.getActivePane());
		assertEquals(srcPane.getHorizontalSplitPosition(), tgtPane.getHorizontalSplitPosition());
		assertEquals(srcPane.getHorizontalSplitTopRow(), tgtPane.getHorizontalSplitTopRow());
		assertEquals(srcPane.getVerticalSplitLeftColumn(), tgtPane.getVerticalSplitLeftColumn());
		assertEquals(srcPane.getVerticalSplitPosition(), tgtPane.getVerticalSplitPosition());
	}

	/**
	 * @throws Exception
	 *         if an error occurred while executing the test case.
	 * 
	 * @see POIExcelUtil#copyPrintSetup(Sheet, Sheet)
	 */
	public void testCopyPrintSetup() throws Exception {
		final Workbook book = open();
		final Sheet source = book.getSheet(SHEET_INFORMATION);
		final Sheet target = book.createSheet();

		POIExcelUtil.copyPrintSetup(source, target);

		final PrintSetup srcSetup = source.getPrintSetup();
		final PrintSetup tgtSetup = target.getPrintSetup();

		assertEquals(srcSetup.getCopies(), tgtSetup.getCopies());
		assertEquals(srcSetup.getDraft(), tgtSetup.getDraft());
		assertEquals(srcSetup.getFitHeight(), tgtSetup.getFitHeight());
		assertEquals(srcSetup.getFitWidth(), tgtSetup.getFitWidth());
		assertEquals(srcSetup.getFooterMargin(), tgtSetup.getFooterMargin());
		assertEquals(srcSetup.getHeaderMargin(), tgtSetup.getHeaderMargin());
		assertEquals(srcSetup.getHResolution(), tgtSetup.getHResolution());
		assertEquals(srcSetup.getLandscape(), tgtSetup.getLandscape());
		assertEquals(srcSetup.getLeftToRight(), tgtSetup.getLeftToRight());
		assertEquals(srcSetup.getNoColor(), tgtSetup.getNoColor());
		assertEquals(srcSetup.getNoOrientation(), tgtSetup.getNoOrientation());
		assertEquals(srcSetup.getNotes(), tgtSetup.getNotes());
		assertEquals(srcSetup.getPageStart(), tgtSetup.getPageStart());
		assertEquals(srcSetup.getPaperSize(), tgtSetup.getPaperSize());
		assertEquals(srcSetup.getScale(), tgtSetup.getScale());
		assertEquals(srcSetup.getUsePage(), tgtSetup.getUsePage());
		assertEquals(srcSetup.getValidSettings(), tgtSetup.getValidSettings());
		assertEquals(srcSetup.getVResolution(), tgtSetup.getVResolution());
	}

	/**
	 * @throws Exception
	 *         if an error occurred while executing the test case.
	 * 
	 * @see POIExcelUtil#copyRow(Row, Row)
	 */
	public void testCopyRow() throws Exception {
		final Workbook book = open();
		final Sheet sheet = book.getSheet(SHEET_CONTENTS);

		final Row source = sheet.getRow(0);
		final Row target = sheet.createRow(1);

		POIExcelUtil.copyRow(source, target);

		assertEqualsRow(source, target);
	}

	/**
	 * @throws Exception
	 *         if an error occurred while executing the test case.
	 * 
	 * @see POIExcelUtil#copySheets(Sheet, Sheet, int)
	 */
	public void testCopySheets() throws Exception {
		final Workbook book = open();

		final Sheet source = book.getSheet(SHEET_CONTENTS);
		final Sheet target = book.createSheet();

		POIExcelUtil.copySheets(source, target, source.getLastRowNum() + 1);

		assertEqualsSheet(source, target);
	}

	/**
	 * @throws Exception
	 *         if an error occurred while executing the test case.
	 * 
	 * @see POIExcelUtil#copySheetSettings(Sheet, Sheet)
	 */
	public void testCopySheetSettings() throws Exception {
		final Workbook book = open();
		final Sheet source = book.getSheet(SHEET_INFORMATION);
		final Sheet target = book.createSheet();

		POIExcelUtil.copySheetSettings(source, target);

		assertEquals(source.getAutobreaks(), target.getAutobreaks());
		assertEquals(source.getDefaultColumnWidth(), target.getDefaultColumnWidth());
		assertEquals(source.getDefaultRowHeight(), target.getDefaultRowHeight());
		assertEquals(source.isDisplayFormulas(), target.isDisplayFormulas());
		assertEquals(source.isDisplayGridlines(), target.isDisplayGridlines());
		assertEquals(source.getDisplayGuts(), target.getDisplayGuts());
		assertEquals(source.isDisplayRowColHeadings(), target.isDisplayRowColHeadings());
		assertEquals(source.isDisplayZeros(), target.isDisplayZeros());
		assertEquals(source.getFitToPage(), target.getFitToPage());
		assertEquals(source.getHorizontallyCenter(), target.getHorizontallyCenter());
		assertEquals(source.isPrintGridlines(), target.isPrintGridlines());
		assertEquals(source.getRowSumsBelow(), target.getRowSumsBelow());
		assertEquals(source.getRowSumsRight(), target.getRowSumsRight());
		assertEquals(source.isSelected(), target.isSelected());
		assertEquals(source.getVerticallyCenter(), target.getVerticallyCenter());
	}

	/**
	 * @throws Exception
	 *         if an error occurred while executing the test case.
	 * 
	 * @see POIExcelUtil#copyTemplate(POITemplateEntry, POITemplateEntry)
	 */
	public void testCopyTemplate() throws Exception {
		final Workbook book = open();
		final Sheet sheet = book.getSheet(SHEET_TEMPLATES);
		final POIExcelTemplate template = new POIExcelTemplate(sheet);
		final POITemplateEntry source = template.getEntry("merged");
		final POITemplateEntry target = new POITemplateEntry(sheet.createRow(sheet.getLastRowNum() + 1).createCell(0));

		POIExcelUtil.copyTemplate(source, target);

		assertEqualsRow(source.getCell().getRow(), target.getCell().getRow());
	}

	/**
	 * @throws Exception
	 *         if an error occurred while executing the test case.
	 * 
	 * @see POIExcelUtil#createCell(Row, int, CellStyle, boolean)
	 * @see POIExcelUtil#createCell(Sheet, int, int, CellStyle, boolean)
	 */
	public void testCreateCell() throws Exception {
		final Workbook book = open();
		final Sheet sheet = book.getSheet(SHEET_EMPTY);
		final Row row = POIExcelUtil.createRow(sheet, 0);

		final Cell newCell = POIExcelUtil.createCell(row, 0, null, false);
		assertNotNull(newCell);
		assertEquals(newCell, POIExcelUtil.createCell(row, 0, null, false));

		sheet.setColumnWidth(newCell.getColumnIndex(), 256);
		final Cell cellWithWidth = POIExcelUtil.createCell(sheet, 0, 1, null, true);
		assertEquals(sheet.getColumnWidth(newCell.getColumnIndex()), sheet.getColumnWidth(cellWithWidth.getColumnIndex()));
	}

	/**
	 * @throws Exception
	 *         if an error occurred while executing the test case.
	 * 
	 * @see POIExcelUtil#createCellComment(Cell, String, POIDrawingManager)
	 * @see POIExcelUtil#createCellComment(Cell, String, POIDrawingManager, int, int)
	 */
	public void testCreateCellComment() throws Exception {
		final Workbook book = open();
		final Sheet sheet = book.getSheet(SHEET_EMPTY);
		final Cell cell = POIExcelUtil.createCell(sheet, 0, 0, null, false);
		final String string = "comment";
		final Comment comment = POIExcelUtil.createCellComment(cell, string, new POIDrawingManager());

		assertEquals(string, comment.getString().getString());
		assertEquals(cell.getRowIndex(), comment.getRow());
		assertEquals(cell.getColumnIndex(), comment.getColumn());
	}

	/**
	 * @throws Exception
	 *         if an error occurred while executing the test case.
	 * 
	 * @see POIExcelUtil#createRow(Sheet, int)
	 */
	public void testCreateRow() throws Exception {
		final Workbook book = open();
		final Sheet sheet = book.getSheet(SHEET_EMPTY);

		final Row row = POIExcelUtil.createRow(sheet, 0);
		assertNotNull(row);
		assertEquals(row, POIExcelUtil.createRow(sheet, 0));
	}

	/**
	 * @throws Exception
	 *         if an error occurred while executing the test case.
	 * 
	 * @see POIExcelUtil#createSheetName(Workbook, String)
	 */
	public void testCreateSheetName() throws Exception {
		final Workbook book = open();
		final String validName = "sheetName";
		final String validLongName = "sheetNameWithMoreThanThrityoneCharacters";
		final String invalidName = "\\/?*[]";

		assertEquals(validName, book.createSheet(POIExcelUtil.createSheetName(book, validName)).getSheetName());
		assertEquals(validName + "(1)", book.createSheet(POIExcelUtil.createSheetName(book, validName)).getSheetName());
		assertEquals("sheetNameWithMoreThanThrityo...", book.createSheet(POIExcelUtil.createSheetName(book, validLongName)).getSheetName());
		assertEquals("sheetNameWithMoreThanThri...(1)", book.createSheet(POIExcelUtil.createSheetName(book, validLongName)).getSheetName());
		assertEquals("sheetNameWithMoreThanThri...(2)", book.createSheet(POIExcelUtil.createSheetName(book, validLongName)).getSheetName());
		assertEquals("sheetNameWithMoreThanThri...(3)", book.createSheet(POIExcelUtil.createSheetName(book, validLongName)).getSheetName());
		assertEquals("sheetNameWithMoreThanThri...(4)", book.createSheet(POIExcelUtil.createSheetName(book, validLongName)).getSheetName());
		assertEquals("sheetNameWithMoreThanThri...(5)", book.createSheet(POIExcelUtil.createSheetName(book, validLongName)).getSheetName());
		assertEquals("sheetNameWithMoreThanThri...(6)", book.createSheet(POIExcelUtil.createSheetName(book, validLongName)).getSheetName());
		assertEquals("sheetNameWithMoreThanThri...(7)", book.createSheet(POIExcelUtil.createSheetName(book, validLongName)).getSheetName());
		assertEquals("sheetNameWithMoreThanThri...(8)", book.createSheet(POIExcelUtil.createSheetName(book, validLongName)).getSheetName());
		assertEquals("sheetNameWithMoreThanThri...(9)", book.createSheet(POIExcelUtil.createSheetName(book, validLongName)).getSheetName());
		assertEquals("sheetNameWithMoreThanThr...(10)", book.createSheet(POIExcelUtil.createSheetName(book, validLongName)).getSheetName());
		assertEquals("------", book.createSheet(POIExcelUtil.createSheetName(book, invalidName)).getSheetName());
	}

	/**
	 * @throws Exception
	 *         if an error occurred while executing the test case.
	 * 
	 * @see POIExcelUtil#exportImage(DataAccessProxy, POITemplateEntry, POIDrawingManager)
	 */
	public void testExportImage() throws Exception {
		final Workbook book = open();
		final Sheet sheet = book.getSheet(SHEET_TEMPLATES);
		final POIExcelTemplate templates = new POIExcelTemplate(sheet);
		final POITemplateEntry template = templates.getEntry("single");
		final DataAccessProxy image = new DataAccessProxy("webinf://images/excel.png");
		POIExcelUtil.exportImage(image, template, new POIDrawingManager());

		final List<? extends PictureData> pictures = book.getAllPictures();
		assertEquals(1, pictures.size());

		final InputStream expected = image.getEntry();
		final InputStream actual = new ByteArrayInputStream(pictures.get(0).getData());

		try {
			assertTrue(StreamUtilities.equalsStreamContents(expected, actual));
		} finally {
			expected.close();
			actual.close();
		}
	}

	/**
	 * @throws Exception
	 *         if an error occurred while executing the test case.
	 * 
	 * @see POIExcelUtil#exportTable(TableModel, Set, POITemplateEntry, POICellStyleProvider,
	 *      POICellValueProvider, int)
	 * @see POIExcelUtil#exportTable(TableModel, Set, POITemplateEntry, POICellStyleProvider,
	 *      POICellValueProvider, POICellStyleProvider, POICellValueProvider, int)
	 */
	public void testExportTable() throws Exception {
		final Workbook book = open();
		final Sheet sheet = book.getSheet(SHEET_TEMPLATES);
		final POIExcelTemplate templates = new POIExcelTemplate(sheet);
		
		final List<String> columns = Arrays.asList("column-1", "column-2", "column-3", "column-4", "column-5");
		final TableConfiguration config = TableConfiguration.table();
		config.getDefaultColumn().setAccessor(new TestPOIValueAccessor());
		final List<String> rows = Arrays.asList("row-1", "row-2", "row-3", "row-4", "row-5");

		final TableModel table = new ObjectTableModel(columns, config, rows);
		final Set<String> ignore = new HashSet<>(Arrays.asList("_select", "column-2"));
		final Set<String> all = Collections.singleton("_select");
		final POICellStyleProvider styles = new TestPOICellStyleProvider();
		final POICellValueProvider values = new TestPOICellValueProvider();
		
		// export all columns
		final POITemplateEntry template1 = templates.getEntry("table1");
		POIExcelUtil.exportTable(table, all, template1, styles, values, styles, values, -1);
		assertEqualsTable(table, all, template1, -1);
		
		// export non-ignored columns
		final POITemplateEntry template2 = templates.getEntry("table2");
		POIExcelUtil.exportTable(table, ignore, template2, styles, values, styles, values, -1);
		assertEqualsTable(table, ignore, template2, -1);

		// export all columns with maximum column count
		final POITemplateEntry template3 = templates.getEntry("table3");
		POIExcelUtil.exportTable(table, all, template3, styles, values, styles, values, 3);
		assertEqualsTable(table, all, template3, 3);

		// export non-ignored columns with maximum column count
		final POITemplateEntry template4 = templates.getEntry("table4");
		POIExcelUtil.exportTable(table, ignore, template4, styles, values, styles, values, 3);
		assertEqualsTable(table, ignore, template4, 3);

		// export all columns without header
		final POITemplateEntry template5 = templates.getEntry("table5");
		POIExcelUtil.exportTable(table, ignore, template5, styles, values, styles, values, -1);
		assertEqualsTable(table, ignore, template5, -1);

		// export ignored columns without header with maximum column count
		final POITemplateEntry template6 = templates.getEntry("table6");
		POIExcelUtil.exportTable(table, ignore, template6, styles, values, styles, values, 3);
		assertEqualsTable(table, ignore, template6, 3);

		// export all columns with merged header and body cells
		final POITemplateEntry template7 = templates.getEntry("table7");
		POIExcelUtil.exportTable(table, all, template7, styles, values, styles, values, 3);
		assertEqualsTable(table, all, template7, 3);
	}

	/**
	 * @throws Exception
	 *         if an error occurred while executing the test case.
	 *
	 * @see POIExcelUtil#exportTree(TLTreeModel, TableDeclaration, POITemplateEntry,
	 *      POICellStyleProvider, POICellValueProvider)
	 * @see POIExcelUtil#exportTree(TLTreeModel, List, Accessor, POITemplateEntry,
	 *      POICellStyleProvider, POICellValueProvider)
	 */
	public void testExportTree() throws Exception {
		final Workbook book = open();
		final Sheet sheet = book.getSheet(SHEET_TEMPLATES);
		final POIExcelTemplate templates = new POIExcelTemplate(sheet);
		final POICellStyleProvider styles = new TestPOICellStyleProvider();
		final POICellValueProvider values = new TestPOICellValueProvider();
		final Accessor<DefaultMutableTLTreeNode> accessor = new ReadOnlyAccessor<>() {
			@Override
			public Object getValue(final DefaultMutableTLTreeNode object, final String property) {
				final String string = (String) object.getBusinessObject();

				if ("name".equals(property)) {
					return string;
				} else {
					return string + ' ' + property;
				}
			}
		};
		final DefaultTableDeclaration declaration = new DefaultTableDeclaration();
		declaration.setAccessor(accessor);
		for (final String column : Arrays.asList("name", "column-1", "column-2")) {
			declaration.addColumnDeclaration(column,
				new DefaultColumnDeclaration(DefaultColumnDeclaration.RENDERED_COLUMN));
		}

		final TLTreeModel<DefaultMutableTLTreeNode> tree =
			new DefaultMutableTLTreeModel(new DefaultMutableTreeNodeBuilder() {
				@Override
				public List<DefaultMutableTLTreeNode> createChildList(final DefaultMutableTLTreeNode node) {
					final AbstractMutableTLTreeModel<DefaultMutableTLTreeNode> model = node.getModel();
					final String object = (String) node.getBusinessObject();
					final List<DefaultMutableTLTreeNode> children = new ArrayList<>();
					final int depth = TLTreeModelUtil.createPathToRoot(node).size();

					if (depth == 1) {
						children.add(createNode(model, node, "1"));
						children.add(createNode(model, node, "2"));
						children.add(createNode(model, node, "3"));
					} else if (depth < 4) {
						children.add(createNode(model, node, object + ".1"));
						children.add(createNode(model, node, object + ".2"));
						children.add(createNode(model, node, object + ".3"));
					} else {
						// stop creation
					}

					return children;
				}
			},
			"root");

		final POITemplateEntry tree1 = templates.getEntry("tree1");
		POIExcelUtil.exportTree(tree, declaration, tree1, styles, values);
		assertEqualsTree(tree, declaration, tree1);

		final POITemplateEntry tree2 = templates.getEntry("tree2");
		POIExcelUtil.exportTree(tree, declaration, tree2, styles, values);
		assertEqualsTree(tree, declaration, tree2);

		final POITemplateEntry tree3 = templates.getEntry("tree3");
		POIExcelUtil.exportTree(tree, declaration, tree3, styles, values);
		assertEqualsTree(tree, declaration, tree3);

		final POITemplateEntry tree4 = templates.getEntry("tree4");
		POIExcelUtil.exportTree(tree, declaration, tree4, styles, values);
		assertEqualsTree(tree, declaration, tree4);

		final POITemplateEntry tree5 = templates.getEntry("tree5");
		POIExcelUtil.exportTree(tree, declaration, tree5, styles, values);
		assertEqualsTree(tree, declaration, tree5);

		final POITemplateEntry tree6 = templates.getEntry("tree6");
		POIExcelUtil.exportTree(tree, declaration, tree6, styles, values);
		assertEqualsTree(tree, declaration, tree6);
	}

	/**
	 * @throws Exception
	 *         if an error occurred while executing the test case.
	 *
	 * @see POIExcelUtil#findCell(Sheet, String)
	 */
	public void testFindCell() throws Exception {
		final Workbook book = open();
		final Sheet sheet = book.getSheet(SHEET_CONTENTS);

		final Cell cell = POIExcelUtil.findCell(sheet, "string");
		assertNotNull(cell);
		assertEquals("string", cell.getStringCellValue());

		assertNull(POIExcelUtil.findCell(sheet, "contents"));
	}

	/**
	 * @throws Exception
	 *         if an error occurred while executing the test case.
	 *
	 * @see POIExcelUtil#getCell(Workbook, CellReference)
	 * @see POIExcelUtil#getCell(Sheet, int, int)
	 */
	public void testGetCell() throws Exception {
		final Workbook book = open();
		final Sheet sheet = book.getSheet(SHEET_CONTENTS);

		final Cell boolCell = POIExcelUtil.getCell(book, new CellReference(SHEET_CONTENTS, 0, 2, true, true));
		assertTrue(boolCell.getBooleanCellValue());

		final Cell numberCell = POIExcelUtil.getCell(sheet, 0, 1);
		assertEquals(42d, numberCell.getNumericCellValue());

		assertNull(POIExcelUtil.getCell(sheet, 128, 128));
	}

	/**
	 * @throws Exception
	 *         if an error occurred while executing the test case.
	 *
	 * @see POIExcelUtil#getCellValue(Cell)
	 */
	public void testGetCellValue() throws Exception {
		final Workbook book = open();
		final Sheet sheet = book.getSheet(SHEET_CONTENTS);

		final ExcelContext context = ExcelContext.getInstance(sheet).row(0);
		assertEquals("string", context.value());
		assertEquals(42d, context.right().value());
		assertEquals(true, (boolean) context.right().value());

		Date d = context.right().<Date> value();
		final Calendar cal = CalendarUtil.createCalendar(d);
		assertEquals(2016, cal.get(Calendar.YEAR));
		assertEquals(0, cal.get(Calendar.MONTH));
		assertEquals(1, cal.get(Calendar.DATE));
	}

	/**
	 * @throws Exception
	 *         if an error occurred while executing the test case.
	 *
	 * @see POIExcelUtil#getLogicalColumnCount(Sheet, CellRangeAddress)
	 */
	public void testGetLogicalColumnCount() throws Exception {
		final Workbook book = open();
		final Sheet sheet = book.getSheet(SHEET_TEMPLATES);
		final POIExcelTemplate templates = new POIExcelTemplate(sheet);
		final POITemplateEntry template = templates.getEntry("table8");
		final int columns = template.getInteger(POITemplateEntry.ATTRIBUTE_COLS, 1);
		final Cell anchor = template.getCell();

		final CellRangeAddress range =
			new CellRangeAddress(anchor.getRowIndex(), anchor.getRowIndex(), anchor.getColumnIndex(), 11);

		assertEquals(columns, POIExcelUtil.getLogicalColumnCount(sheet, range));
	}

	/**
	 * @throws Exception
	 *         if an error occurred while executing the test case.
	 *
	 * @see POIExcelUtil#getLogicalColumnIndex(Cell, POITemplateEntry)
	 */
	public void testGetLogicalColumnIndex() throws Exception {
		final Workbook book = open();
		final Sheet sheet = book.getSheet(SHEET_TEMPLATES);
		final POIExcelTemplate templates = new POIExcelTemplate(sheet);
		final POITemplateEntry template = templates.getEntry("table8");
		final Cell anchor = template.getCell();
		final POIExcelContext context = (POIExcelContext) ExcelContext.getInstance(sheet);

		// first column (merged colspan=3)
		context.row(anchor.getRowIndex()).column(anchor.getColumnIndex());
		assertEquals(0, POIExcelUtil.getLogicalColumnIndex(context.getCell(true), template));

		// second column
		context.right().right().right();
		assertEquals(1, POIExcelUtil.getLogicalColumnIndex(context.getCell(true), template));

		// out of bounds
		context.column(12);
		assertEquals(-1, POIExcelUtil.getLogicalColumnIndex(context.getCell(true), template));
	}

	/**
	 * @throws Exception
	 *         if an error occurred while executing the test case.
	 *
	 * @see POIExcelUtil#getLogicalRowIndex(Cell, POITemplateEntry)
	 */
	public void testGetLogicalRowIndex() throws Exception {
		final Workbook book = open();
		final Sheet sheet = book.getSheet(SHEET_TEMPLATES);
		final POIExcelTemplate templates = new POIExcelTemplate(sheet);
		final POITemplateEntry template = templates.getEntry("table8");
		final Cell anchor = template.getCell();
		final POIExcelContext context = (POIExcelContext) ExcelContext.getInstance(sheet);

		// header cell is the first logical row
		context.row(anchor.getRowIndex()).column(anchor.getColumnIndex());
		assertEquals(0, POIExcelUtil.getLogicalRowIndex(context.getCell(true), template));

		// 5th row (out of 10)
		context.row(anchor.getRowIndex() + 4);
		assertEquals(4, POIExcelUtil.getLogicalRowIndex(context.getCell(true), template));

		context.row(anchor.getRowIndex() + 10);
		assertEquals(-1, POIExcelUtil.getLogicalRowIndex(context.getCell(true), template));
	}

	/**
	 * @throws Exception
	 *         if an error occurred while executing the test case.
	 *
	 * @see POIExcelUtil#getRange(Sheet, int, int)
	 * @see POIExcelUtil#getRange(Cell)
	 */
	public void testGetRange() throws Exception {
		final Workbook book = open();
		final Sheet sheet = book.getSheet(SHEET_SIZES);
		
		// existing region:
		// rowStart = 0, rowEnd = 1
		// colStart = 2, colEnd = 3
		assertNull(POIExcelUtil.getRange(sheet, 0, 0));

		final CellRangeAddress range = POIExcelUtil.getRange(sheet, 0, 2);
		assertNotNull(range);
		assertEquals(0, range.getFirstRow());
		assertEquals(1, range.getLastRow());
		assertEquals(2, range.getFirstColumn());
		assertEquals(3, range.getLastColumn());
	}

	/**
	 * @throws Exception
	 *         if an error occurred while executing the test case.
	 *
	 * @see POIExcelUtil#insertRow(Sheet, int, POITemplateEntry)
	 */
	public void testInsertRow() throws Exception {
		final Workbook book = open();
		final Sheet sheet = book.getSheet(SHEET_CONTENTS);

		final Row newRow = POIExcelUtil.insertRow(sheet, 0, new POITemplateEntry(null));
		assertNotNull(newRow);

		final ExcelContext context = ExcelContext.getInstance(sheet).row(1);
		assertEquals("string", context.value());
		assertEquals(42d, context.right().value());
		assertEquals(true, (boolean) context.right().value());

		Date d = context.right().value();
		final Calendar cal = CalendarUtil.createCalendar(d);
		assertEquals(2016, cal.get(Calendar.YEAR));
		assertEquals(0, cal.get(Calendar.MONTH));
		assertEquals(1, cal.get(Calendar.DATE));
	}

	/**
	 * @throws Exception
	 *         if an error occurred while executing the test case.
	 *
	 * @see POIExcelUtil#isPartOfMergedRegion(Sheet, int, int, int, int)
	 */
	public void testIsPartOfMergedRegion() throws Exception {
		final Workbook book = open();
		final Sheet sheet = book.getSheet(SHEET_SIZES);

		// existing region:
		// rowStart = 0, rowEnd = 1
		// colStart = 2, colEnd = 3
		assertFalse(POIExcelUtil.isPartOfMergedRegion(sheet, 0, 0, 0, 1));
		assertTrue(POIExcelUtil.isPartOfMergedRegion(sheet, 0, 0, 2, 3));
		assertTrue(POIExcelUtil.isPartOfMergedRegion(sheet, 0, 5, 0, 5));
	}

	/**
	 * @throws Exception
	 *         if an error occurred while executing the test case.
	 *
	 * @see POIExcelUtil#mergeColumns(Row, int, int)
	 */
	public void testMergeColumns() throws Exception {
		final Workbook book = open();
		final Sheet sheet = book.getSheet(SHEET_SIZES);
		final Row row = sheet.getRow(0);

		// existing region:
		// rowStart = 0, rowEnd = 1
		// colStart = 2, colEnd = 3

		// enclose -> fail
		assertEquals(-1, POIExcelUtil.mergeColumns(row, 0, 5));

		// overlap -> fail
		assertEquals(-1, POIExcelUtil.mergeColumns(row, 0, 2));
		assertEquals(-1, POIExcelUtil.mergeColumns(row, 3, 5));

		// same -> fail
		assertEquals(-1, POIExcelUtil.mergeColumns(row, 2, 3));

		// no overlapping -> good
		assertNotSame(-1, POIExcelUtil.mergeColumns(row, 10, 15));
	}

	/**
	 * @see POIExcelUtil#normalizeSheetName(String)
	 */
	public void testNormalizeSheetName() {
		final String validName = "validSheetName123";
		final String invalidName = "\\/?*[]\n";

		assertEquals(validName, POIExcelUtil.normalizeSheetName(validName));
		assertEquals("-------", POIExcelUtil.normalizeSheetName(invalidName));
	}

	/**
	 * @throws Exception
	 *         if an error occurred while executing the test case.
	 *
	 * @see POIExcelUtil#setCellStyle(Cell, CellStyle, boolean)
	 */
	public void testSetCellStyle() throws Exception {
		final Workbook book = open();
		final Sheet sheet = book.getSheet(SHEET_SIZES);

		final CellStyle style = book.createCellStyle();
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);

		final Cell single = sheet.getRow(0).getCell(0);
		POIExcelUtil.setCellStyle(single, style, true);
		assertEquals(single.getCellStyle(), style);

		final Cell merged = sheet.getRow(0).getCell(2);
		POIExcelUtil.setCellStyle(merged, style, true);
		final CellRangeAddress range = POIExcelUtil.getRange(merged);
		for (int row = range.getFirstRow(); row <= range.getLastRow(); row++) {
			for (int col = range.getFirstColumn(); col <= range.getLastColumn(); col++) {
				assertEquals(style, sheet.getRow(row).getCell(col).getCellStyle());
			}
		}
	}

	/**
	 * @throws Exception
	 *         if an error occurred while executing the test case.
	 *
	 * @see POIExcelUtil#setCellValue(Cell, Object)
	 */
	public void testSetCellValue() throws Exception {
		final Workbook book = open();
		final Sheet sheet = book.getSheet(SHEET_EMPTY);
		final ExcelContext context = ExcelContext.getInstance(sheet);

		final boolean bool = true;
		final double number = 42;
		final String string = "string";
		final Date date = new Date();

		context.value(bool);
		assertEquals(bool, (boolean) context.value());

		context.value(number);
		assertEquals(number, context.value());

		context.value(string);
		assertEquals(string, context.value());

		context.value(date);
		assertEquals(date, context.value());

		context.value(null);
		assertNull(context.value());
	}
	
	/**
	 * Load the excel file containing test cases.
	 * 
	 * @return the loaded {@link Workbook}
	 * @throws IOException
	 *         if an error occurred while reading the file
	 * @throws InvalidFormatException
	 *         if the file is malformed
	 */
	private Workbook open() throws IOException, InvalidFormatException {
		final InputStream stream = getClass().getResourceAsStream(FILE_NAME);
		try {
			return WorkbookFactory.create(stream);
		} finally {
			stream.close();
		}
	}

	/**
	 * Check if the given tree model is written to the given template.
	 * 
	 * @param tree
	 *        the {@link TLTreeModel} to check against
	 * @param declaration
	 *        the {@link TableDeclaration} for columns
	 * @param template
	 *        the {@link POITemplateEntry} to check
	 */
	private void assertEqualsTree(final TLTreeModel<DefaultMutableTLTreeNode> tree,
			final DefaultTableDeclaration declaration, final POITemplateEntry template) {

		// initialize the ExcelContext for content reading
		final Cell cell = template.getCell();
		final POIExcelContext context = (POIExcelContext) ExcelContext.getInstance(cell.getSheet());
		context.row(cell.getRowIndex()).column(cell.getColumnIndex());

		final boolean showHeader = template.getBoolean(POITemplateEntry.ATTRIBUTE_HEADER, true);
		final boolean includeRoot = template.getBoolean(POITemplateEntry.ATTRIBUTE_ROOT, true);

		// check for the header
		if (showHeader) {
			final List<String> columns = declaration.getColumnNames();
			for (final String column : columns) {
				final Object actual = context.value();

				assertEquals(column, actual);
				
				final CellRangeAddress range = POIExcelUtil.getRange(context.getCell(false));
				if (range != null) {
					context.column(range.getLastColumn());
				}
				context.right();
			}
		}

		// position the cursor at the first column of the first row
		context.down().column(cell.getColumnIndex());

		if (includeRoot) {
			assertEqualsNode(tree.getRoot(), declaration, template, context);
		} else {
			for (final DefaultMutableTLTreeNode node : tree.getRoot().getChildren()) {
				assertEqualsNode(node, declaration, template, context);
			}
		}
	}

	/**
	 * Check if the subtree starting at the given node is written to the given template correctly.
	 * 
	 * @param node
	 *        the {@link DefaultMutableTLTreeNode} defining the subtree to check
	 * @param declaration
	 *        the {@link TableDeclaration} defining the tree's column
	 * @param template
	 *        the {@link POITemplateEntry} the given node was written to
	 * @param context
	 *        the {@link POIExcelContext} to be used value access
	 */
	private void assertEqualsNode(final DefaultMutableTLTreeNode node, final TableDeclaration declaration,
			final POITemplateEntry template, final POIExcelContext context) {
		
		// assume that the context has already been positioned correctly at the
		// first column in the given node's row. We just need to check the cell contents.
		final List<String> columns = declaration.getColumnNames();
		final Accessor<Object> accessor = declaration.getAccessor();
		for (final String column : columns) {
			// check the node's value against excel contents
			final Object expected = accessor.getValue(node, column);
			final Object actual = context.value();
			assertEquals(expected, actual);

			// move the cursor to the next column
			final CellRangeAddress range = POIExcelUtil.getRange(context.getCell(false));
			if (range != null) {
				context.column(range.getLastColumn());
			}
			context.right();
		}

		// move the cursor to the next row and position it at the first column
		context.down().column(template.getCell().getColumnIndex());

		// descend deeper if maxDepth has not been reached
		final int maxDepth = template.getInteger(POITemplateEntry.ATTRIBUTE_MAX_DEPTH, POITemplateEntry.INFINITE_DEPTH);
		final int curDepth = node.getModel().createPathToRoot(node).size();
		if (maxDepth >= curDepth || maxDepth == POITemplateEntry.INFINITE_DEPTH) {
			for (final DefaultMutableTLTreeNode child : node.getChildren()) {
				assertEqualsNode(child, declaration, template, context);
			}
		}
	}

	/**
	 * Check if the given table model is written to the given template.
	 * 
	 * @param table
	 *        the {@link TableModel} to check against
	 * @param ignoredColumns
	 *        a (possibly empty) {@link Set} of ignored column names
	 * @param template
	 *        the {@link POITemplateEntry} to check
	 * @param maxColumns
	 *        the maximum number of columns in a row or -1 for no limit
	 */
	private void assertEqualsTable(final TableModel table, Set<String> ignoredColumns, final POITemplateEntry template,
			final int maxColumns) {

		// cache table rows and rows count
		final Collection<?> tableRows = table.getAllRows();
		final int numRows = tableRows.size();

		// build the list of columns without the ignored ones
		final List<String> tableColumns = new ArrayList<>(table.getColumnNames());
		tableColumns.removeAll(ignoredColumns);
		final int numColumns = tableColumns.size();

		final Cell anchor = template.getCell();
		final int anchorColumn = anchor.getColumnIndex();
		final boolean hasHeader = template.getBoolean(POITemplateEntry.ATTRIBUTE_HEADER, true);

		// create an excel context for reading
		final POIExcelContext context = (POIExcelContext) ExcelContext.getInstance(anchor.getSheet());
		context.row(anchor.getRowIndex()).column(anchor.getColumnIndex());

		// compute the number of blocks the given table will be split up in
		final int blockColumns = maxColumns > 0 ? maxColumns : numColumns;
		final int numBlocks = (int) Math.ceil((double) numColumns / blockColumns);

		// check all blocks
		for (int block = 0; block < numBlocks; block++) {
			final int firstColumn = block * blockColumns;
			final int lastColumn = Math.min(firstColumn + blockColumns, numColumns);

			// check all columns in the current block
			for (int tableColumn = firstColumn; tableColumn < lastColumn; tableColumn++) {
				final String columnName = tableColumns.get(tableColumn);

				// check the header value for the current column
				if (hasHeader) {
					final Object expected = columnName;
					final Object actual = context.value();

					assertEquals(expected, actual);
				}

				// check all row values for the current column
				for (int tableRow = 0; tableRow < numRows; tableRow++) {
					final Object expected = table.getValueAt(tableRow, columnName);
					final Object actual = context.down().value();

					assertEquals(expected, actual);
				}

				// move the ExcelContext if we have not reached to block end
				if (tableColumn < lastColumn - 1) {
					// move the ExcelContext to the first cell of the next column
					final Cell cell = context.getCell(false);
					final CellRangeAddress range = POIExcelUtil.getRange(cell);
					if (range != null) {
						context.column(range.getLastColumn());
					}
					context.right();

					// move the ExcelContext to the first row of the block
					context.row(context.row() - numRows);
				}
			}

			// move the ExcelContext to the next block row and first column
			if (hasHeader) {
				context.down();
			}
			context.column(anchorColumn);
		}
	}

	/**
	 * Check for sheet content equality.
	 * 
	 * @param source
	 *        the expected {@link Sheet}
	 * @param target
	 *        the actual {@link Sheet}
	 */
	private void assertEqualsSheet(final Sheet source, final Sheet target) {
		for (int i = 0; i <= source.getLastRowNum(); i++) {
			assertEqualsRow(source.getRow(i), target.getRow(i));
		}
	}

	/**
	 * Check for row content equality.
	 * 
	 * @param source
	 *        the expected {@link Row} or {@code null}
	 * @param target
	 *        the actual {@link Row} or {@code null}
	 */
	private void assertEqualsRow(final Row source, final Row target) {
		if (source != null) {
			if (target != null) {
				for (int i = 0; i < source.getLastCellNum(); i++) {
					assertEqualsCell(source.getCell(i), target.getCell(i));
				}
			} else {
				throw new AssertionFailedError("expected:<non-null> but was:<null>");
			}
		} else {
			if (target != null) {
				throw new AssertionFailedError("expected:<null> but was:<non-null>");
			} else {
				// both null -> equal
			}
		}
	}

	/**
	 * Check for cell content equality.
	 * 
	 * @param source
	 *        the expected {@link Cell} or {@code null}
	 * @param target
	 *        the actual {@link Cell} or {@code null}
	 */
	private void assertEqualsCell(final Cell source, final Cell target) {
		if (source != null) {
			if (target != null) {
				final ExcelContext src = ExcelContext.getInstance(source.getSheet()).row(source.getRowIndex()).column(source.getColumnIndex());
				final ExcelContext tgt = ExcelContext.getInstance(target.getSheet()).row(target.getRowIndex()).column(target.getColumnIndex());

				Object srcValue = src.value();
				Object tgtValue = tgt.value();
				assertEquals(srcValue, tgtValue);
			} else {
				throw new AssertionFailedError("expected:<non-null> but was:<null>");
			}
		} else {
			if (target != null) {
				throw new AssertionFailedError("expected:<null> but was:<non-null>");
			} else {
				// both null -> equal
			}
		}
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestExcelUtil}.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				TestExcelUtil.class,
				DataAccessService.Module.INSTANCE,
				TableConfigurationFactory.Module.INSTANCE));
	}

	/**
	 * Main function for direct testing.
	 */
	public static void main(String[] args) {
		TestRunner.run(suite());
	}

	/**
	 * An {@link Accessor} implementation which appends the property name to the row object.
	 * 
	 * @author <a href=mailto:wta@top-logic.com>wta</a>
	 */
	protected class TestPOIValueAccessor extends ReadOnlyAccessor<String> {

		@Override
		public Object getValue(final String object, final String property) {
			return object + ' ' + property;
		}
	}

	/**
	 * A {@link POICellValueProvider} implementation setting the cell value as is.
	 * 
	 * @author <a href=mailto:wta@top-logic.com>wta</a>
	 */
	protected class TestPOICellValueProvider implements POICellValueProvider {

		@Override
		public void setCellValue(final Cell cell, final Object value) {
			ExcelContext.getInstance(cell.getSheet()).row(cell.getRowIndex()).column(cell.getColumnIndex()).value(value);
		}
	}

	/**
	 * A {@link POICellValueProvider} implementation for test purposes which does not change the
	 * cell style.
	 * 
	 * @author <a href=mailto:wta@top-logic.com>wta</a>
	 */
	protected class TestPOICellStyleProvider implements POICellStyleProvider {

		@Override
		public CellStyle getCellStyle(final Cell cell, final Object rowObject, final String property, final Object value) {
			return cell.getCellStyle();
		}
	}
}
