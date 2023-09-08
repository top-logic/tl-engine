/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.model;


/**
 * {@link Box} that surrounds its {@link #getCenter() center box} with border and corner boxes in
 * each direction.
 * 
 * <p>
 * The {@link #getCenter()} box gets all extra space if available. If one of the nine boxes are
 * missing, the box to its right consumes its space.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BorderBox extends AbstractCollectionBox {

	private Box _topLeft;

	private Box _topBorder;

	private Box _topRight;

	private Box _leftBorder;

	private Box _center;

	private Box _rightBorder;

	private Box _bottomLeft;

	private Box _bottomBorder;

	private Box _bottomRigth;

	/**
	 * Creates a {@link BorderBox}.
	 */
	public BorderBox() {
		super();
	}

	/**
	 * The box rendering the top-left corner.
	 */
	public Box getTopLeft() {
		return _topLeft;
	}

	/**
	 * @see #getTopLeft()
	 */
	public void setTopLeft(Box topLeft) {
		_topLeft = replace(_topLeft, topLeft);
		notifyLayoutChange();
	}

	/**
	 * The box rendering the top border.
	 */
	public Box getTopBorder() {
		return _topBorder;
	}

	/**
	 * @see #getTopBorder()
	 */
	public void setTopBorder(Box topBorder) {
		_topBorder = replace(_topBorder, topBorder);
		notifyLayoutChange();
	}

	/**
	 * The box rendering the top-right corner.
	 */
	public Box getTopRight() {
		return _topRight;
	}

	/**
	 * @see #getTopRight()
	 */
	public void setTopRight(Box topRight) {
		_topRight = replace(_topRight, topRight);
		notifyLayoutChange();
	}

	/**
	 * The box rendering the left border.
	 */
	public Box getLeftBorder() {
		return _leftBorder;
	}

	/**
	 * @see #getLeftBorder()
	 */
	public void setLeftBorder(Box leftBorder) {
		_leftBorder = replace(_leftBorder, leftBorder);
		notifyLayoutChange();
	}

	/**
	 * The box rendering the central content area.
	 */
	public Box getCenter() {
		return _center;
	}

	/**
	 * @see #getCenter()
	 */
	public void setCenter(Box center) {
		_center = replace(_center, center);
		notifyLayoutChange();
	}

	/**
	 * The box rendering the right border
	 */
	public Box getRightBorder() {
		return _rightBorder;
	}

	/**
	 * @see #getRightBorder()
	 */
	public void setRightBorder(Box rightBorder) {
		_rightBorder = replace(_rightBorder, rightBorder);
		notifyLayoutChange();
	}

	/**
	 * The box rendering the bottom-left corner.
	 */
	public Box getBottomLeft() {
		return _bottomLeft;
	}

	/**
	 * @see #getBottomLeft()
	 */
	public void setBottomLeft(Box bottomLeft) {
		_bottomLeft = replace(_bottomLeft, bottomLeft);
		notifyLayoutChange();
	}

	/**
	 * The box rendering the bottom border.
	 */
	public Box getBottomBorder() {
		return _bottomBorder;
	}

	/**
	 * @see #getBottomBorder()
	 */
	public void setBottomBorder(Box bottomBorder) {
		_bottomBorder = replace(_bottomBorder, bottomBorder);
		notifyLayoutChange();
	}

	/**
	 * The box rendering the bottom-right corner.
	 */
	public Box getBottomRigth() {
		return _bottomRigth;
	}

	/**
	 * @see #getBottomRigth()
	 */
	public void setBottomRigth(Box bottomRigth) {
		_bottomRigth = replace(_bottomRigth, bottomRigth);
		notifyLayoutChange();
	}

	@Override
	protected void localLayout() {
		layout(_topLeft);
		layout(_topBorder);
		layout(_topRight);

		layout(_leftBorder);
		layout(_center);
		layout(_rightBorder);

		layout(_bottomLeft);
		layout(_bottomBorder);
		layout(_bottomRigth);

		setDimension(
			leftColumns() + centerColumns() + rightColumns(),
			topRows() + centerRows() + bottomRows());
	}

	/**
	 * {@link Box#layout() Layouts} the given box, if the box is non-<code>null</code>.
	 */
	protected static void layout(Box box) {
		if (box != null) {
			box.layout();
		}
	}

	/**
	 * Number for columns reserved for the left border.
	 */
	protected int leftColumns() {
		return max(columns(_topLeft), columns(_leftBorder), columns(_bottomLeft));
	}

	/**
	 * Number for columns reserved for the certer area.
	 */
	protected int centerColumns() {
		return max(columns(_topBorder), columns(_center), columns(_bottomBorder));
	}

	/**
	 * Number for columns reserved for the right border.
	 */
	protected int rightColumns() {
		return max(columns(_topRight), columns(_rightBorder), columns(_bottomRigth));
	}

	/**
	 * Number for rows reserved for the top border.
	 */
	protected int topRows() {
		return max(rows(_topLeft), rows(_topBorder), rows(_topRight));
	}

	/**
	 * Number for rows reserved for the center area.
	 */
	protected int centerRows() {
		return max(rows(_leftBorder), rows(_center), rows(_rightBorder));
	}

	/**
	 * Number for rows reserved for the bottom border.
	 */
	protected int bottomRows() {
		return max(rows(_bottomLeft), rows(_bottomBorder), rows(_bottomRigth));
	}

	/**
	 * Number of the columns of the given box, or <code>0</code>, if the box is <code>null</code>.
	 */
	protected static int columns(Box box) {
		return box == null ? 0 : box.getColumns();
	}

	/**
	 * Number of the rows of the given box, or <code>0</code>, if the box is <code>null</code>.
	 */
	private static int rows(Box box) {
		return box == null ? 0 : box.getRows();
	}

	private static int max(int x, int y, int z) {
		return Math.max(x, Math.max(y, z));
	}

	@Override
	protected void enterContent(int x, int y, int availableColumns, int availableRows, Table<ContentBox> table) {
		int leftColumns = leftColumns();
		int rightColumns = rightColumns();
		int centerColumns = availableColumns - leftColumns - rightColumns;

		int topRows = topRows();
		int bottomRows = bottomRows();
		int centerRows = availableRows - topRows - bottomRows;

		RowLayouter layouter = new RowLayouter(table);

		layouter.startRow(x, y, topRows);
		layouter.add(leftColumns, _topLeft);
		layouter.add(centerColumns, _topBorder);
		layouter.add(rightColumns, _topRight);
		layouter.endRow();

		layouter.startRow(x, y + topRows, centerRows);
		layouter.add(leftColumns, _leftBorder);
		layouter.add(centerColumns, _center);
		layouter.add(rightColumns, _rightBorder);
		layouter.endRow();

		layouter.startRow(x, y + topRows + centerRows, bottomRows);
		layouter.add(leftColumns, _bottomLeft);
		layouter.add(centerColumns, _bottomBorder);
		layouter.add(rightColumns, _bottomRigth);
		layouter.endRow();
	}

	/**
	 * Algorithm for positioning boxes in a virtual row.
	 */
	protected static final class RowLayouter {

		private final Table<ContentBox> _table;

		private int _xPos;

		private int _yPos;

		private int _availableColumns;

		private int _availableRows;

		private Box _lastBox;

		/**
		 * Creates a {@link RowLayouter}.
		 * 
		 * @param table
		 *        The result able that is created.
		 */
		public RowLayouter(Table<ContentBox> table) {
			super();
			_table = table;
		}

		/**
		 * Starts a new row.
		 * 
		 * @param x
		 *        The first column in the table of the virtual row.
		 * @param y
		 *        The first row in the table of the virtual row.
		 * @param availableRows
		 *        The height in table rows of the layouted virtual row.
		 */
		public void startRow(int x, int y, int availableRows) {
			_xPos = x;
			_yPos = y;
			_availableColumns = 0;
			_availableRows = availableRows;
			_lastBox = null;
		}

		/**
		 * Adds a {@link Box} to the virtual row.
		 * 
		 * @param reservedColumns
		 *        The number of columns that are reserved for the box.
		 * @param box
		 *        The box to position, <code>null</code> uses the given columns for tbe next box in
		 *        this row.
		 */
		public void add(int reservedColumns, Box box) {
			// More columns available, additional ones may be left over from previous calls.
			_availableColumns += reservedColumns;

			if (box != null && _availableColumns > 0 && _availableRows > 0) {
				box.enter(_xPos, _yPos, _availableColumns, _availableRows, _table);
				_xPos += box.getColumns();

				// All columns consumed.
				_availableColumns = 0;

				_lastBox = box;
			}
		}

		/**
		 * Ends the layout of the virtual row.
		 */
		public void endRow() {
			if (_availableColumns > 0 && _lastBox != null) {
				// Extends last box to fill all space.
				int lastColumns = _lastBox.getColumns();

				_lastBox.enter(_xPos - lastColumns, _yPos, lastColumns + _availableColumns, _availableRows, _table);
			}
		}

	}

}
