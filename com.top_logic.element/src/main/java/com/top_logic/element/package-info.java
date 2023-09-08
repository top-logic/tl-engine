/**
 * <p>
 * Elements for building up a structure (like trees).
 * </p>
 * <p>
 * Using the package one can easily create complex data structures of
 * {@link com.top_logic.element.structured.StructuredElement StructuredElements}. These structures
 * have a root element, which has no parent. Every other element within this structure belongs to a
 * root element.
 * </p>
 * <p>
 * For getting a root element one can use the
 * {@link com.top_logic.element.structured.StructuredElementFactory factory}, which will be defined
 * in the configuration. The factory will provide a method for requesting a root element of a
 * specific type. This is needed, because an application can have several structures which cannot be
 * compared to each other.
 * </p>
 */
package com.top_logic.element;
