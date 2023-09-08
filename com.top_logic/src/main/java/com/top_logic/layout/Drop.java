/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;


/**
 * This is &quot;Drag &amp; Drop&quot; target which can handle business objects
 * of type T.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface Drop<T> {

	/**
	 * The class {@link DropException} is thrown by {@link Drop#notifyDrop(Object, Object)} to
	 * propagate that the drop operation failed.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	public static class DropException extends Exception {

		/**
		 * Creates a {@link DropException}.
		 * 
		 * @param message
		 *        See {@link Exception#Exception(String)}.
		 */
		public DropException(String message) {
			super(message);
		}

		/**
		 * Creates a {@link DropException}.
		 * 
		 * @param cause
		 *        See {@link Exception#Exception(Throwable)}.
		 */
		public DropException(Throwable cause) {
			super(cause);
		}
	}

	/**
	 * returns the id of this {@link Drop}
	 * 
	 * @return may be <code>null</code> if the Drop has no clientSide
	 *         representation.
	 */
	public String getID();

	/**
	 * @param value
	 *        the objects which was dragged
	 * @param dropInfo
	 *        Additional information sent form the client-side drop logic.
	 * 
	 * @throws DropException
	 *         if the business objects are not supported by this Drop, some information are missing
	 *         to handle the business objects, or some other problems completing the drop operation
	 *         occur.
	 */
	public void notifyDrop(T value, Object dropInfo) throws DropException;

}
