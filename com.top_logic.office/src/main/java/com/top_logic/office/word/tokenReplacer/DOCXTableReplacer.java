/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.office.word.tokenReplacer;

import java.util.List;
import java.util.Map;
import java.util.Stack;

import jakarta.xml.bind.JAXBElement;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;

import com.top_logic.base.office.AbstractOffice.TokenReplacer;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.office.word.visitor.DOCXCreateOrReplaceParagraphText;
import com.top_logic.office.word.visitor.DOCXFindTableCells;
import com.top_logic.office.word.visitor.DOCXFindTableRows;
import com.top_logic.office.word.visitor.DOCXMapTables;
import com.top_logic.office.word.visitor.DOCXVisitor;

/**
 * This {@link TokenReplacer} searches the given "aDocument" for a {@link Tbl} containing the given
 * "token" and fills this {@link Tbl} with the values from "aReplacement" (which has to be an
 * Object[][]).
 * 
 * @author <a href="mailto:jes@top-logic.com">jes</a>
 */
public class DOCXTableReplacer implements TokenReplacer {
	@Override
	public boolean replaceToken(String token, Object aReplacement, Object anApplication, Object aDocument,
			Stack aReferencesStack) throws Exception {

		assert (aReplacement == null || aReplacement instanceof TableReplacerConfig);

		WordprocessingMLPackage document = (WordprocessingMLPackage) aDocument;
		MainDocumentPart mainDocumentPart = document.getMainDocumentPart();

		return replaceTable(token, (TableReplacerConfig) aReplacement, mainDocumentPart);
	}

	/**
	 * Searches the {@link MainDocumentPart} for a {@link Tbl} containing the given token and fills
	 * it with the values from the "replacement".
	 * 
	 * @param token
	 *        The token to search for in the {@link Tbl}s of the {@link MainDocumentPart}
	 * @param replacement
	 *        The values to fill the {@link Tbl} with
	 * @param mainDocumentPart
	 *        The {@link MainDocumentPart} to search for tables
	 */
	private boolean replaceTable(String token, TableReplacerConfig replacement, MainDocumentPart mainDocumentPart) {
		Tbl table = findTable(token, mainDocumentPart);

		if (table == null) {
			return false;
		}

		if (replacement == null /* ||replacement._tableValues == null */) {
			// delete table
			deleteTable(table);
			return true;
		}

		fillTable(token, table, replacement);
		return true;
	}

	/**
	 * Returns the {@link Tbl} found within the given {@link ContentAccessor} that contains the
	 * given token name.
	 * 
	 * @param token
	 *        The token-string searched for in all {@link Tbl}s of the given {@link ContentAccessor}
	 * @param part
	 *        The {@link ContentAccessor} searched for {@link Tbl}s
	 * @return The {@link Tbl} from the given {@link ContentAccessor} containing the given token
	 *         name
	 */
	private Tbl findTable(String token, ContentAccessor part) {
		DOCXMapTables visitor = new DOCXMapTables();
		DOCXVisitor.visit(part, visitor);
		Map<String, Tbl> tables = visitor.getTables();
		return tables.get(token);
	}

	/**
	 * Removes the given {@link Tbl}.
	 * 
	 * @param table
	 *        The {@link Tbl} to remove.
	 */
	private void deleteTable(Tbl table) {
		JAXBElement<?> parent = (JAXBElement<?>) table.getParent();
		parent.setValue(null);
		parent.setNil(true);
		// TODO: optional remove the empty JAXBElement
	}

	/**
	 * Fills the given {@link Tbl} with the given values.
	 * 
	 * @param table
	 *        The {@link Tbl} to fill
	 * @param tableReplacerConfig
	 *        The values to fill the {@link Tbl} with.
	 */
	private void fillTable(String token, Tbl table, TableReplacerConfig tableReplacerConfig) {

		boolean addCols = tableReplacerConfig.isAddColumns();
		boolean addRows = tableReplacerConfig.isAddRows();

		boolean removeCols = tableReplacerConfig.isRemoveColumns();
		boolean removeRows = tableReplacerConfig.isRemoveRows();

		Object[][] tableValues = tableReplacerConfig.getTableValues();

		// rows
		List<Tr> tableRows = getRows(table);

		// Step 1: Tabellenspalten und Zeilen gemäß Konfig anpassen
		// Step 1a: Annahme: Erste Zeile definiert die Spaltenanzahl => Prüfung ob Anzahl Spalten
		// aus Template und Anzahl Werte übereinstimmt, Anpassung gemäß Konfig, auch für alle
		// folgenden Zeilen (hier dann ohne Prüfung).
		// Step 2b: Hinzufügen oder Entfernen von Zeilen gemäß Konfig.
		// => Ergebnis: Tabelle die der Konfig und der Anzahl der Spalten/Zeilen mit den Werten
		// übereinstimmt oder ConfigurationError!

		// Step 3: Wertersetzung

		Tr tableRow = null;
		int valueRowCount = tableValues.length;
		int tableRowCount = tableRows.size();
		for (int iRow = 0; iRow < valueRowCount; iRow++) {
			if (iRow < tableRowCount) {
				tableRow = tableRows.get(iRow);
			}
			else {
				if (addRows) {
					// create new row, append to table and go on
					tableRow = copyTableRow(tableRow);
					table.getContent().add(tableRow);
				} else {
					throw new ConfigurationError(
						"Table values contain more rows than table '" + token
							+ "' in template, but the configuration does not allow to add new rows!");
				}
			}

			// cells
			Object[] tableRowValues = tableValues[iRow];
			List<Tc> cells = getCells(tableRow);

			Tc cell = null;
			int valueColCount = tableRowValues.length;
			int tableColCount = cells.size();
			for (int iCol = 0; iCol < valueColCount; iCol++) {
				if (iCol < tableColCount) {
					cell = cells.get(iCol);
				}
				else {
					if (addCols) {
						// create new Cell and append to Row
						cell = copyTableCell(cell);
						tableRow.getContent().add(cell);
					} else {
						// stop adding of new columns
						throw new ConfigurationError(
							"Table values contain more columns than table '" + token
								+ "' in template, but the configuration does not allow to add new columns!");
					}
				}

				Object cellValue = tableRowValues[iCol];
				createOrReplaceFirstParagraphsText((String) cellValue, cell);
			}

			if (removeCols) {
				removeEmptyCols(tableRow, cells, valueColCount, tableColCount);
				// TODO use empty space, e.g. enlarge table or columns.
			}

		}

		if (removeRows) {
			removeEmptyRows(table, tableRows, valueRowCount, tableRowCount);
		}
	}

	private void removeEmptyRows(Tbl table, List<Tr> tableRows, int valueRowCount, int tableRowCount) {
		Tr tableRow;
		int emptyRowCount = tableRowCount - valueRowCount;
		for (int i = 0; i < emptyRowCount; i++) {
			tableRow = tableRows.get(valueRowCount + i);
			table.getContent().remove(tableRow);
		}
	}

	private void removeEmptyCols(Tr tableRow, List<Tc> cells, int valueColCount, int tableColCount) {
		Tc cell;
		int emptyColCount = tableColCount - valueColCount;
		for (int i = 0; i < emptyColCount; i++) {
			cell = cells.get(valueColCount + i);
			tableRow.getContent().remove(cell.getParent());
		}
	}

	/**
	 * Returns a {@link List} of {@link Tr} elements found in the given {@link Tbl}.
	 * 
	 * @param table
	 *        The {@link Tbl} to search for {@link Tr} elements in.
	 * @return The {@link List} of {@link Tr} elements found in the given {@link Tbl}.
	 */
	private List<Tr> getRows(Tbl table) {
		DOCXFindTableRows visitor = new DOCXFindTableRows();
		DOCXVisitor.visit(table, visitor);
		return visitor.getRows();
	}

	/**
	 * Creates a deep copy of the given {@link Tr}.
	 * 
	 * @param tableRow
	 *        The {@link Tr} to copy.
	 * @return The copy of the given {@link Tr}.
	 */
	private Tr copyTableRow(Tr tableRow) {
		Tr rowCopy = XmlUtils.deepCopy(tableRow);
		return rowCopy;
	}

	/**
	 * Creates a deep copy of the given {@link Tc}.
	 * 
	 * @param tableCell
	 *        The {@link Tc} to copy.
	 * @return The copy of the given {@link Tc}.
	 */
	private Tc copyTableCell(Tc tableCell) {
		Tc cellCopy = XmlUtils.deepCopy(tableCell);
		return cellCopy;
	}

	/**
	 * Returns a {@link List} of {@link Tc} elements found in the given {@link Tr}.
	 * 
	 * @param tableRow
	 *        The {@link Tr} to search for {@link Tc} elements in.
	 * @return The {@link List} of {@link Tc} elements found in the given {@link Tr}.
	 */
	private List<Tc> getCells(Tr tableRow) {
		DOCXFindTableCells visitor = new DOCXFindTableCells();
		DOCXVisitor.visit(tableRow, visitor);
		return visitor.getCells();
	}

	/**
	 * Uses the {@link DOCXCreateOrReplaceParagraphText}-visitor to replace the first {@link Text}
	 * in the given {@link ContentAccessor}s first {@link P}-element with the given text or create a
	 * new {@link Text} containing the given text if no {@link Text} exists in the first {@link P}
	 * -element.
	 * 
	 * @param text
	 *        The text to use as replacement.
	 * @param part
	 *        The {@link ContentAccessor} in which the text has to be replaced or created.
	 */
	private void createOrReplaceFirstParagraphsText(String text, ContentAccessor part) {
		DOCXCreateOrReplaceParagraphText visitor = new DOCXCreateOrReplaceParagraphText(text);
		DOCXVisitor.visit(part, visitor);
	}

}