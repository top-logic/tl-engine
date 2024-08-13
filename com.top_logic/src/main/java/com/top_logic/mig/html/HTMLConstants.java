/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.servlet.ServletResponse;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Drag;
import com.top_logic.layout.Drop;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.contextmenu.control.ContextMenuOwner;
import com.top_logic.layout.messagebox.ProgressControl;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;

/**
 * Constants for XHTML element names, attribute names, and attributes values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface HTMLConstants {

	/** Constant for HTML5 DOCTYPE declaration. */
	public static final String DOCTYPE_HTML = "<!DOCTYPE html>";

	/**
	 * Namespace for XHTML.
	 * 
	 * <p>
	 * Only for legacy cases, new-style documents should use {@link #DOCTYPE_HTML}.
	 * </p>
	 * 
	 * @deprecated Use {@link #DOCTYPE_HTML}.
	 */
	@Deprecated
	public static final String XHTML_NS = "http://www.w3.org/1999/xhtml";

    /** Non-breaking space for things like empty table entries. */
    public static final String NBSP = "\u00A0";

    /** Arrow pointing to the left. */
    public static final String LEFT_ARROW = "\u2190";

    /** Arrow pointing to the right. */
    public static final String RIGHT_ARROW = "\u2192";

	/** Arrow pointing up. */
	public static final String UP_ARROW = "\u2191";

    /** Arrow pointing down. */
	public static final String DOWN_ARROW = "\u2193";

    /** Unicode character displaying a sum symbol. */
	public static final String SUM = "&#8721;";
	
    /** New line. */
    public static final String NL = "\r\n";

	/** Constant for the HTML attribute "id". */
	public static final String ID_ATTR = "id";

	/** Constant for the HTML attribute "name". */
	public static final String NAME_ATTR = "name";
	
	/** Constant for the HTML attribute "class". */
    public static final String CLASS_ATTR = "class";
    
    /**
	 * DOM property corresponding to the {@value #CLASS_ATTR} HTML attribute.
	 */
    public static final String CLASS_PROPERTY = "className";

    /** Constant for the HTML attribute "border". */
    public static final String BORDER_ATTR = "border";

    /** Constant for the HTML attribute "summary". */
    public static final String SUMMARY_ATTR = "summary";

	/**
	 * Constant for the HTML attribute "type".
	 * <p>
	 * Possible values:
	 * <ul>
	 * <li>{@value #TEXT_TYPE_VALUE}</li>
	 * <li>{@value #PASSWORD_TYPE_VALUE}</li>
	 * <li>{@value #IMAGE_TYPE_VALUE}</li>
	 * <li>{@value #CHECKBOX_TYPE_VALUE}</li>
	 * <li>{@value #RADIO_TYPE_VALUE}</li>
	 * <li>{@value #FILE_TYPE_VALUE}</li>
	 * <li>{@value #BUTTON_TYPE_VALUE}</li>
	 * <li>{@value #HIDDEN_TYPE_VALUE}</li>
	 * <li>{@value #RESET_TYPE_VALUE}</li>
	 * <li>{@value #SUBMIT_TYPE_VALUE}</li>
	 * </ul>
	 * </p>
	 * 
	 * @see #INPUT
	 */
    public static final String TYPE_ATTR = "type";

	/** Constant for the HTML attribute "value". */
    public static final String VALUE_ATTR = "value";

	/** Constant for the HTML attribute "accept". */
	public static final String ACCEPT_ATTR = "accept";

	/** Constant for the HTML attribute "size". */
    public static final String SIZE_ATTR = "size";

    /** Constant for the HTML attribute "maxlength". */
    public static final String MAXLENGTH_ATTR = "maxlength";
    
    /** Constant for the HTML attribute "cols". */
    public static final String COLS_ATTR = "cols";
    
    /** Constant for the HTML attribute "colspan". */
    public static final String COLSPAN_ATTR = "colspan";
    
    /** Constant for the HTML attribute "rowspan". */
    public static final String ROWSPAN_ATTR = "rowspan";

    /** Constant for the HTML attribute "rows". */
    public static final String ROWS_ATTR = "rows";
    
	/** Constant for the HTML attribute "tabindex". */
    public static final String TABINDEX_ATTR = "tabindex";
    
	/** Constant for the HTML attribute "for". */
    public static final String FOR_ATTR = "for";
    
	/** Constant for the HTML attribute "accesskey". */
    public static final String ACCESSKEY_ATTR = "accesskey";

	/** Constant for the HTML attribute "method". */
    public static final String METHOD_ATTR = "method";
    
    /** Constant for the HTML POST {@link #METHOD_ATTR method}. */
    public static final String POST_VALUE = "post";

    /** Constant for the HTML attribute "enctype". */
    public static final String ENCTYPE_ATTR = "enctype";
    
    /** Constant for the HTML multipart/form-data {@link #ENCTYPE_ATTR}. */
    public static final String MULTIPART_FORM_DATA_VALUE = "multipart/form-data";
    
	/** Constant for the HTML attribute "action". */
    public static final String ACTION_ATTR = "action";

    /** Constant for the HTML attribute "target". */
    public static final String TARGET_ATTR = "target";


	/** Constant for the HTML attribute "scope". */
	public static final String SCOPE_ATTR = "scope";

	/** Constant for the HTML attribute "dir". */
	public static final String DIR_ATTR = "dir";

	/** Constant for the HTML attribute "lang". */
	public static final String LANG_ATTR = "lang";

	/** Constant for the HTML attribute "longdesc". */
	public static final String LONGDESC_ATTR = "longdesc";

	/** Constant for the HTML attribute "src". */
    public static final String SRC_ATTR = "src";

	/** Constant for the HTML attribute "href". */
    public static final String HREF_ATTR = "href";

    /** Constant for the empty value of HTML attribute "href". */
    public static final String HREF_EMPTY_LINK = "#";

	/**
	 * Flags the element as potential drag source in a HTML5 drag-and-drop operation.
	 * 
	 * @see #DRAGGABLE_TRUE_VALUE
	 * @see #DRAGGABLE_FALSE_VALUE
	 */
	public static final String DRAGGABLE_ATTR = "draggable";

	/**
	 * Possible value for the {@link #DRAGGABLE_ATTR} attribute to enable dragging.
	 */
	public static final String DRAGGABLE_TRUE_VALUE = "true";

	/**
	 * Possible value for the {@link #DRAGGABLE_ATTR} attribute to disenable dragging.
	 */
	public static final String DRAGGABLE_FALSE_VALUE = "false";

	/** Constant for the HTML attribute ""onselectstart"". */
	public static final String ONSELECTSTART_ATTR = "onselectstart";

	/** DOM-Level 2: The event occurs when the user clicks on an element */
    public static final String ONCLICK_ATTR = "onclick";

	/** DOM-Level 2: The event occurs when the user double-clicks on an element */
    public static final String ONDBLCLICK_ATTR = "ondblclick";
    
	/**
	 * DOM-Level 2: The event occurs when a user moves the mouse pointer out of an element, or out
	 * of one of its children
	 */
    public static final String ONMOUSEOUT_ATTR = "onmouseout";
    
	/**
	 * DOM-Level 2: The event occurs when the pointer is moved onto an element, or onto one of its
	 * children
	 */
    public static final String ONMOUSEOVER_ATTR = "onmouseover";
    
	/** DOM-Level 2: The event occurs when the user presses a mouse button over an element */
    public static final String ONMOUSEDOWN_ATTR = "onmousedown";

	/** DOM-Level 2: The event occurs when a user releases a mouse button over an element */
    public static final String ONMOUSEUP_ATTR = "onmouseup";
    
	/**
	 * DOM-Level 2: The event occurs when the content of a form element, the selection, or the
	 * checked state have changed (for {@link #INPUT}, {@link #KEYGEN}, {@link #SELECT}, and
	 * {@link #TEXTAREA})
	 */
    public static final String ONCHANGE_ATTR = "onchange";
    
	/** DOM-Level 3: The event occurs when an element gets user input */
	public static final String ONINPUT_ATTR = "oninput";

	/** DOM-Level 2: The event occurs when an element gets focus */
    public static final String ONFOCUS_ATTR = "onfocus";

	/** DOM-Level 2: The event occurs when the user releases a key */
    public static final String ONKEYUP_ATTR = "onkeyup";
    
	/** DOM-Level 2: The event occurs when the user is pressing a key */
    public static final String ONKEYDOWN_ATTR = "onkeydown";

	/** DOM-Level 2: The event occurs when the user presses a key */
    public static final String ONKEYPRESS_ATTR = "onkeypress";
    
	/** DOM-Level 2: The event occurs when an element loses focus */
    public static final String ONBLUR_ATTR = "onblur";
    
	/** DOM-Level 2: The event occurs when an object has loaded */
    public static final String ONLOAD_ATTR = "onload";
    
	/** DOM-Level 2: The event occurs once a page has unloaded (for {@link #BODY}) */
    public static final String ONUNLOAD_ATTR = "onunload";
    
	/**
	 * DOM-Level 3: The event occurs when the user right-clicks on an element to open a context menu
	 */
	public static final String ONCONTEXTMENU_ATTR = "oncontextmenu";

	/** DOM-Level 2: The event occurs when the pointer is moved onto an element */
	public static final String ONMOUSEENTER_ATTR = "onmouseenter";

	/** DOM-Level 2: The event occurs when the pointer is moved out of an element */
	public static final String ONMOUSELEAVE_ATTR = "onmouseleave";

	/** DOM-Level 2: The event occurs when the pointer is moving while it is over an element */
	public static final String ONMOUSEMOVE_ATTR = "onmousemove";

    /** DOM-Level 2: The event occurs when the loading of a resource has been aborted */
	public static final String ONABORT_ATTR = "onabort";

	/** DOM-Level 2: The event occurs before the document is about to be unloaded */
	public static final String ONBEFOREUNLOAD_ATTR = "onbeforeunload";

	/** DOM-Level 2: The event occurs when an error occurs while loading an external file */
	public static final String ONERROR_ATTR = "onerror";

	/** DOM-Level 3: The event occurs when there has been changes to the anchor part of a URL */
	public static final String ONHASHCHANGE_ATTR = "onhashchange";

	/** DOM-Level 3: The event occurs when the user navigates to a webpage */
	public static final String ONPAGESHOW_ATTR = "onpageshow";

	/** DOM-Level 3: The event occurs when the user navigates away from a webpage */
	public static final String ONPAGEHIDE_ATTR = "onpagehide";

	/** DOM-Level 2: The event occurs when the document view is resized */
	public static final String ONRESIZE_ATTR = "onresize";

	/** DOM-Level 2: The event occurs when an element's scrollbar is being scrolled */
	public static final String ONSCROLL_ATTR = "onscroll";

	/** DOM-Level 2: The event occurs when an element is about to get focus */
	public static final String ONFOCUSIN_ATTR = "onfocusin";

	/** DOM-Level 2: The event occurs when an element is about to lose focus */
	public static final String ONFOCUSOUT_ATTR = "onfocusout";

	/** DOM-Level 3: The event occurs when an element is invalid */
	public static final String ONINVALID_ATTR = "oninvalid";

	/** DOM-Level 2: The event occurs when a form is reset */
	public static final String ONRESET_ATTR = "onreset";

	/**
	 * DOM-Level 3: The event occurs when the user writes something in a search field (for
	 * &lt;input="search">)
	 */
	public static final String ONSEARCH_ATTR = "onsearch";

	/**
	 * DOM-Level 2: The event occurs after the user selects some text (for {@link #INPUT} and
	 * {@link #TEXTAREA})
	 */
	public static final String ONSELECT_ATTR = "onselect";

	/** DOM-Level 2: The event occurs when a form is submitted */
	public static final String ONSUBMIT_ATTR = "onsubmit";

	/** DOM-Level 3: The event occurs when an element is being dragged */
	public static final String ONDRAG_ATTR = "ondrag";

	/** DOM-Level 3: The event occurs when the user has finished dragging an element */
	public static final String ONDRAGEND_ATTR = "ondragend";

	/** DOM-Level 3: The event occurs when the dragged element enters the drop target */
	public static final String ONDRAGENTER_ATTR = "ondragenter";

	/** DOM-Level 3: The event occurs when the dragged element leaves the drop target */
	public static final String ONDRAGLEAVE_ATTR = "ondragleave";

	/** DOM-Level 3: The event occurs when the dragged element is over the drop target */
	public static final String ONDRAGOVER_ATTR = "ondragover";

	/** DOM-Level 3: The event occurs when the user starts to drag an element */
	public static final String ONDRAGSTART_ATTR = "ondragstart";

	/** DOM-Level 3: The event occurs when the dragged element is dropped on the drop target */
	public static final String ONDROP_ATTR = "ondrop";

	/** The event occurs when the user copies the content of an element */
	public static final String ONCOPY_ATTR = "oncopy";

	/** The event occurs when the user cuts the content of an element */
	public static final String ONCUT_ATTR = "oncut";

	/** The event occurs when the user pastes some content in an element */
	public static final String ONPASTE_ATTR = "onpaste";

	/**
	 * DOM-Level 3: The event occurs when a page has started printing, or if the print dialogue box
	 * has been closed
	 */
	public static final String ONAFTERPRINT_ATTR = "onafterprint";

	/** DOM-Level 3: The event occurs when a page is about to be printed */
	public static final String ONBEFOREPRINT_ATTR = "onbeforeprint";

	/**
	 * DOM-Level 3: The event occurs when the browser can start playing the media (when it has
	 * buffered enough to begin)
	 */
	public static final String ONCANPLAY_ATTR = "oncanplay";

	/**
	 * DOM-Level 3: The event occurs when the browser can play through the media without stopping
	 * for buffering
	 */
	public static final String ONCANPLAYTHROUGH_ATTR = "oncanplaythrough";

	/** DOM-Level 3: The event occurs when the duration of the media is changed */
	public static final String ONDURATIONCHANGE_ATTR = "ondurationchange";

	/**
	 * DOM-Level 3: The event occurs when something bad happens and the media file is suddenly
	 * unavailable (like unexpectedly disconnects)
	 */
	public static final String ONEMPTIED_ATTR = "onemptied";

	/**
	 * DOM-Level 3: The event occurs when the media has reach the end (useful for messages like
	 * "thanks for listening")
	 */
	public static final String ONENDED_ATTR = "onended";

	/** DOM-Level 3: The event occurs when media data is loaded */
	public static final String ONLOADEDDATA_ATTR = "onloadeddata";

	/** DOM-Level 3: The event occurs when meta data (like dimensions and duration) are loaded */
	public static final String ONLOADEDMETADATA_ATTR = "onloadedmetadata";

	/** DOM-Level 3: The event occurs when the browser starts looking for the specified media */
	public static final String ONLOADSTART_ATTR = "onloadstart";

	/**
	 * DOM-Level 3: The event occurs when the media is paused either by the user or programmatically
	 */
	public static final String ONPAUSE_ATTR = "onpause";

	/** DOM-Level 3: The event occurs when the media has been started or is no longer paused */
	public static final String ONPLAY_ATTR = "onplay";

	/**
	 * DOM-Level 3: The event occurs when the media is playing after having been paused or stopped
	 * for buffering
	 */
	public static final String ONPLAYING_ATTR = "onplaying";

	/**
	 * DOM-Level 3: The event occurs when the browser is in the process of getting the media data
	 * (downloading the media)
	 */
	public static final String ONPROGRESS_ATTR = "onprogress";

	/** DOM-Level 3: The event occurs when the playing speed of the media is changed */
	public static final String ONRATECHANGE_ATTR = "onratechange";

	/**
	 * DOM-Level 3: The event occurs when the user is finished moving/skipping to a new position in
	 * the media
	 */
	public static final String ONSEEKED_ATTR = "onseeked";

	/**
	 * DOM-Level 3: The event occurs when the user starts moving/skipping to a new position in the
	 * media
	 */
	public static final String ONSEEKING_ATTR = "onseeking";

	/**
	 * DOM-Level 3: The event occurs when the browser is trying to get media data, but data is not
	 * available
	 */
	public static final String ONSTALLED_ATTR = "onstalled";

	/** DOM-Level 3: The event occurs when the browser is intentionally not getting media data */
	public static final String ONSUSPEND_ATTR = "onsuspend";

	/**
	 * DOM-Level 3: The event occurs when the playing position has changed (like when the user fast
	 * forwards to a different point in the media)
	 */
	public static final String ONTIMEUPDATE_ATTR = "ontimeupdate";

	/**
	 * DOM-Level 3: The event occurs when the volume of the media has changed (includes setting the
	 * volume to "mute")
	 */
	public static final String ONVOLUMECHANGE_ATTR = "onvolumechange";

	/**
	 * DOM-Level 3: The event occurs when the media has paused but is expected to resume (like when
	 * the media pauses to buffer more data)
	 */
	public static final String ONWAITING_ATTR = "onwaiting";

	/** DOM-Level 3: The event occurs when a CSS animation has completed */
	public static final String ANIMATIONEND_ATTR = "animationend";

	/** DOM-Level 3: The event occurs when a CSS animation is repeated */
	public static final String ANIMATIONITERATION_ATTR = "animationiteration";

	/** DOM-Level 3: The event occurs when a CSS animation has started */
	public static final String ANIMATIONSTART_ATTR = "animationstart";

	/** DOM-Level 3: The event occurs when a CSS transition has completed */
	public static final String TRANSITIONEND_ATTR = "transitionend";

	/**
	 * DOM-Level 3: The event occurs when a message is received through or from an object
	 * (WebSocket, Web Worker, Event Source or a child frame or a parent window)
	 */
	public static final String ONMESSAGE_ATTR = "onmessage";

	/** @deprecated Use the {@link #ONWHEEL_ATTR} event instead */
	@Deprecated
	public static final String ONMOUSEWHEEL_ATTR = "onmousewheel";

	/** DOM-Level 3: The event occurs when the browser starts to work online */
	public static final String ONONLINE_ATTR = "ononline";

	/** DOM-Level 3: The event occurs when the browser starts to work offline */
	public static final String ONOFFLINE_ATTR = "onoffline";

	/** DOM-Level 3: The event occurs when the window's history changes */
	public static final String ONPOPSTATE_ATTR = "onpopstate";

	/**
	 * DOM-Level 3: The event occurs when a <code>&lt;menu></code> element is shown as a context
	 * menu
	 */
	public static final String ONSHOW_ATTR = "onshow";

	/** DOM-Level 3: The event occurs when a Web Storage area is updated */
	public static final String ONSTORAGE_ATTR = "onstorage";

	/**
	 * DOM-Level 3: The event occurs when the user opens or closes the <code>&lt;details></code>
	 * element
	 */
	public static final String ONTOGGLE_ATTR = "ontoggle";

	/** DOM-Level 3: The event occurs when the mouse wheel rolls up or down over an element */
	public static final String ONWHEEL_ATTR = "onwheel";

	/** Constant for the HTML attribute "width". */
    public static final String WIDTH_ATTR = "width";
    
	/** Constant for the HTML attribute "height". */
    public static final String HEIGHT_ATTR = "height";

    /** Constant for the HTML attribute "alt". */
    public static final String ALT_ATTR = "alt";

    /** Constant for the HTML attribute "title". */
    public static final String TITLE_ATTR = "title";

	/** Constant for the HTML element "style". */
	public static final String STYLE_ELEMENT = "style";

    /** Constant for the HTML attribute "style". */
    public static final String STYLE_ATTR = "style";

	/** Constant for the HTML attribute "placeholder". */
	public static final String PLACEHOLDER_ATTR = "placeholder";

	/** Constant for the HTML attribute "checked". */
    public static final String CHECKED_ATTR = "checked";

	/** Constant for the HTML attribute "disabled". */
    public static final String DISABLED_ATTR = "disabled";

	/** Constant for the HTML attribute "readonly". */
    public static final String READONLY_ATTR = "readonly";

	/** Constant for the HTML attribute "selected". */
    public static final String SELECTED_ATTR = "selected";

	/** Constant for the HTML attribute "autofocus". */
	public static final String AUTOFOCUS_ATTR = "autofocus";

	/** Constant for the HTML attribute "formnovalidate". */
	public static final String FORMNOVALIDATE_ATTR = "formnovalidate";

	/** Constant for the HTML attribute "novalidate". */
	public static final String NOVALIDATE_ATTR = "novalidate";

	/** Constant for the HTML attribute "required". */
	public static final String REQUIRED_ATTR = "required";

	/** Constant for the HTML attribute "multiple". */
    public static final String MULTIPLE_ATTR = "multiple";
    
	/** Constant for the HTML attribute "http-equiv". */
	public static final String HTTP_EQUIV_ATTR = "http-equiv";
    
	/** Constant for the HTML attribute "content". */
	public static final String CONTENT_ATTR = "content";

	/** Constant for the "checked" value of the HTML "checked" attribute. */
	public static final String CHECKED_CHECKED_VALUE = "checked";

	/** Constant for the "disabled" value of the HTML "disabled" attribute. */
	public static final String DISABLED_DISABLED_VALUE = "disabled";

	/** Constant for the "readonly" value of the HTML "readonly" attribute. */
	public static final String READONLY_READONLY_VALUE = "readonly";

	/** Constant for the "selected" value of the HTML "selected" attribute. */
    public static final String SELECTED_SELECTED_VALUE = "selected";
	
	/** Constant for the "multiple" value of the HTML "multiple" attribute. */
    public static final String MULTIPLE_MULTIPLE_VALUE = "multiple";

    
	/**
	 * Constant for the "text" value of the HTML "type" attribute.
	 * 
	 * @see #TYPE_ATTR
	 * @see #INPUT
	 */
	public static final String TEXT_TYPE_VALUE = "text";
    
	/**
	 * Constant for the "password" value of the HTML "type" attribute.
	 * 
	 * @see #TYPE_ATTR
	 * @see #INPUT
	 */
	public static final String PASSWORD_TYPE_VALUE = "password";
    
	/**
	 * Constant for the "checkbox" value of the HTML "type" attribute.
	 * 
	 * @see #TYPE_ATTR
	 * @see #INPUT
	 */
	public static final String CHECKBOX_TYPE_VALUE = "checkbox";
    
	/**
	 * Constant for the "radio" value of the HTML "type" attribute.
	 * 
	 * @see #TYPE_ATTR
	 * @see #INPUT
	 */
	public static final String RADIO_TYPE_VALUE = "radio";
    
	/**
	 * Constant for the "submit" value of the HTML "type" attribute.
	 * 
	 * @see #TYPE_ATTR
	 * @see #INPUT
	 */
	public static final String SUBMIT_TYPE_VALUE = "submit";
    
	/**
	 * Constant for the "text" value of the HTML "type" attribute.
	 * 
	 * @see #TYPE_ATTR
	 * @see #INPUT
	 */
	public static final String RESET_TYPE_VALUE = "reset";
    
	/**
	 * Constant for the "file" value of the HTML "type" attribute.
	 * 
	 * @see #TYPE_ATTR
	 * @see #INPUT
	 */
	public static final String FILE_TYPE_VALUE = "file";
    
	/**
	 * Constant for the "hidden" value of the HTML "type" attribute.
	 * 
	 * @see #TYPE_ATTR
	 * @see #INPUT
	 */
	public static final String HIDDEN_TYPE_VALUE = "hidden";
    
	/**
	 * Constant for the "image" value of the HTML "type" attribute.
	 * 
	 * @see #TYPE_ATTR
	 * @see #INPUT
	 */
	public static final String IMAGE_TYPE_VALUE = "image";
    
	/**
	 * Constant for the "button" value of the HTML "type" attribute.
	 * 
	 * @see #TYPE_ATTR
	 * @see #INPUT
	 */
	public static final String BUTTON_TYPE_VALUE = "button";

	/**
	 * Constant for the "text/javascript" value of the HTML "type" attribute of {@link #SCRIPT_REF}
	 * elements.
	 */
	public static final String JAVASCRIPT_TYPE_VALUE = "text/javascript";
	
	/** Constant for the "text/css" value of the HTML "type" attribute of "link" elements. */
	public static final String CSS_TYPE_VALUE = "text/css";
	
	/** Constant for the HTML-tag "html". */
    public static final String HTML = "html";

    /** Constant for the HTML-tag "head". */
    public static final String HEAD = "head";
    
    /** Constant for the HTML tag "title". */
    public static final String TITLE = "title";

	/** Constant for the HTML tag "caption". */
	public static final String CAPTION = "caption";

	/** Constant for the HTML tag "address". */
	public static final String ADDRESS = "address";

	/** Constant for the HTML tag "blockquote". */
	public static final String BLOCKQUOTE = "blockquote";

    /** Constant for the HTML-tag "body". */
    public static final String BODY = "body";

    /** Constant for the HTML-tag "frame". */
    public static final String FRAME = "frame";
    
    /** Constant for the HTML attribute "frameborder". */
    public static final String FRAMEBORDER_ATTR = "frameborder";
    
    /** Constant for the HTML attribute "framespacing". */
    public static final String FRAMESPACING_ATTR = "framespacing";
    
    /** Constant for the HTML-tag "frameset". */
    public static final String FRAMESET = "frameset";
    
    /** Constant for the HTML-tag "iframe". */
    public static final String IFRAME = "iframe";
    
    /** Constants for the scrolling attribute of iframes */
	public static final String	SCROLLING = "scrolling";
	public static final String	SCROLLING_NO_VALUE = "no";
	public static final String	SCROLLING_AUTO_VALUE = "auto";
	public static final String	SCROLLING_YES_VALUE = "yes";

	/** Constant for the HTML-tag "h1". */
	public static final String H1 = "h1";

	/** Constant for the HTML-tag "h2". */
	public static final String H2 = "h2";

	/** Constant for the HTML-tag "h3". */
	public static final String H3 = "h3";

	/** Constant for the HTML-tag "h4". */
	public static final String H4 = "h4";

	/** Constant for the HTML-tag "h5". */
	public static final String H5 = "h5";

	/** Constant for the HTML-tag "h6". */
	public static final String H6 = "h6";
	
	/** Constant for the HTML-tag "paragraph" ("p"). */
    public static final String PARAGRAPH = "p";
	
	/** Constant for the HTML-tag "div". */
    public static final String DIV = "div";

	/** Constant for the HTML-tag "progress". */
	public static final String PROGRESS = "progress";

	/** Constant for the HTML-attribute "max" of e.g. {@link #PROGRESS}. */
	public static final String MAX_ATTR = "max";

	/** Constant for the HTML-tag "nav". */
	public static final String NAV = "nav";

	/** Constant for the HTML-tag "base". */
	public static final String BASE = "base";

	/** Constant for the HTML-tag "command". */
	public static final String COMMAND = "command";

	/** Constant for the HTML-tag "embed". */
	public static final String EMBED = "embed";

	/** Constant for the HTML-tag "keygen". */
	public static final String KEYGEN = "keygen";

	/** Constant for the HTML-tag "param". */
	public static final String PARAM = "param";

	/** Constant for the HTML-tag "source". */
	public static final String SOURCE = "source";

	/** Constant for the HTML-tag "track". */
	public static final String TRACK = "track";

	/** Constant for the HTML-tag "wbr". */
	public static final String WBR = "wbr";

    /** Constant for the HTML-tag "pre". */
    public static final String PRE = "pre";

	/** Constant for the HTML-tag "code". */
	public static final String CODE = "code";

	/** Constant for the HTML-tag "span". */
    public static final String SPAN = "span";

	/** Constant for the HTML-tag "br". */
    public static final String BR = "br";

	/** Constant for the HTML-tag "hr". */
	public static final String HR = "hr";

    /** Constant for the HTML-tag "ol". */
    public static final String OL = "ol";

	/** Constant for the HTML-tag "ul". */
    public static final String UL = "ul";

	/** Constant for the HTML-tag "li". */
    public static final String LI = "li";

    /** Constant for the HTML-tag "dl". */
    public static final String DL = "dl";

    /** Constant for the HTML-tag "dt". */
    public static final String DT = "dt";

    /** Constant for the HTML-tag "dd". */
    public static final String DD = "dd";
    
    /** Constant for the HTML-tag "form". */
    public static final String FORM     = "form";

    /** Constant for the HTML-tag "fieldset". */
    public static final String FIELDSET = "fieldset";

    /** Constant for the HTML-tag "legend". */
    public static final String LEGEND = "legend";

	/**
	 * Constant for the HTML-tag "script" embedding literal script contents.
	 * 
	 * @deprecated Use {@link TagWriter#beginScript()}, {@link TagWriter#writeScript(CharSequence)},
	 *             and {@link TagWriter#endScript()}, see Ticket #14079.
	 */
	@Deprecated
    public static final String SCRIPT = "script";

	/**
	 * Constant for the HTML-tag "script" tag referencing an external script.
	 */
	public static final String SCRIPT_REF = "script";

	/**
	 * Constant for the HTML-tag "img".
	 * 
	 * <p>
	 * Consider using {@link ThemeImage}s and
	 * {@link ThemeImage#write(com.top_logic.layout.DisplayContext, TagWriter)} instead.
	 * </p>
	 */
    public static final String IMG    = "img";

    /** Constant for the HTML-tag "map". */
    public static final String MAP    = "map";

    /** Constant for the HTML-tag "area". */
    public static final String AREA    = "area";

    /** Constant for the HTML-attribute "coords". */
    public static final String COORDS_ATTR    = "coords";

    /** Constant for the HTML-attribute "shape". */
    public static final String SHAPE_ATTR    = "shape";

    /** Constant for the HTML-attribute value "rect". */
    public static final String RECT_SHAPE_VALUE    = "rect";

    /** Constant for the HTML-attribute value "poly". */
    public static final String POLY_SHAPE_VALUE    = "poly";
    
    /** Constant for the HTML-attribute value "circle". */
    public static final String CIRCLE_SHAPE_VALUE    = "circle";

    /** Constant for the HTML attribute "usemap". */
    public static final String USEMAP_ATTR = "usemap";
    
    /** Constant for the HTML attribute "valign". */
    public static final String VALIGN_ATTR  = "valign";
    
    /** Constant for the HTML value "top". */
    public static final String TOP_VALUE  = "top";
    
    /** Constant for the HTML value "middle". */
    public static final String MIDDLE_VALUE  = "middle";
    
    /** Constant for the HTML value "bottom". */
    public static final String BOTTOM_VALUE  = "bottom";
    
    /** Constant for the HTML value "baseline". */
    public static final String BASELINE_VALUE  = "baseline";
    
    /** Constant for the HTML-tag "a". */
    public static final String ANCHOR = "a";

    /** Constant for the HTML-tag "table". */
    public static final String TABLE  = "table";
    
    public static final String CELLPADDING_ATTR = "cellpadding";
    public static final String CELLSPACING_ATTR = "cellspacing";
    
    /** Constant for the HTML-tag "colgroup". */
    public static final String COLGROUP = "colgroup";

    /** Constant for the HTML-tag "col". */
    public static final String COL     = "col";

    /** Constant for the HTML-tag "thead". */
    public static final String THEAD = "thead";

    /** Constant for the HTML-tag "tbody". */
    public static final String TBODY = "tbody";

    /** Constant for the HTML-tag "tfoot". */
    public static final String TFOOT = "tfoot";

    /** Constant for the HTML-tag "td". */
    public static final String TD     = "td";

    /** Constant for the HTML-tag "tr". */
    public static final String TR     = "tr";

    /** Constant for the HTML-tag "th". */
    public static final String TH     = "th";

    /** Constant for the HTML-tag "select". */
    public static final String SELECT = "select";

    /** Constant for the HTML-tag "optgroup". */
    public static final String OPTGROUP = "optgroup";

    /** Constant for the HTML-tag "option". */
    public static final String OPTION = "option";

	/**
	 * Constant for the HTML-tag "input".
	 * 
	 * @see #TYPE_ATTR
	 */
    public static final String INPUT  = "input";

    /** Constant for the HTML-tag "button". */
    public static final String BUTTON  = "button";
    
    /** Constant for the HTML-tag "lable". */
    public static final String LABEL  = "label";

    /** Constant for the HTML-tag "textarea". */
    public static final String TEXTAREA  = "textarea";
    
    /** Constant for the HTML-tag "b" for bold font. */
	public static final String BOLD = "b";
	
	/** Constant for the HTML-tag "strong" for bold font. */
	public static final String STRONG = "strong";

	/** Constant for the HTML-tag "em" for bold font. */
	public static final String EM = "em";

	/** Constant for the HTML-tag "i" for bold font. */
	public static final String ITALICS = "i";

	/** Constant for the HTML-tag "em" for bold font. */
	public static final String S = "s";

	/** Constant for the HTML-tag "u" for bold font. */
	public static final String U = "u";

	/** Constant for the HTML-tag "u" for bold font. */
	public static final String SUB = "sub";

	/** Constant for the HTML-tag "u" for bold font. */
	public static final String SUP = "sup";

	/** Constant for the HTML-tag "link". */
	public static final String LINK = "link";
	
	/** Constant for the HTML-tag "rel". */
	public static final String REL_ATTR = "rel";

	/** Constant for the HTML-tag "meta". */
	public static final String META = "meta";

	/** {@link #REL_ATTR} of stylesheet {@link #LINK}s */
	public static final String STYLESHEET_REL_VALUE = "stylesheet";
    
    /** Constant for the css align. */
    public static final String ALIGN_ATTR    = "align";
    /** Constant for the value of the text-align. */
    public static final String RIGHT_VALUE   = "right";
    /** Constant for the value of the text-align. */
    public static final String LEFT_VALUE    = "left";
    /** Constant for the value of the text-align. */
    public static final String CENTER_VALUE  = "center";
    /** Constant for the value of the text-align. */
    public static final String JUSTIFY_VALUE = "justify";

	/** Constant for the css attribute {@value}. */
	public static final String TEXT_ALIGN_ATTR = "text-align";

	/** Constant for value {@value}. */
	public static final String TEXT_ALIGN_RIGHT = TEXT_ALIGN_ATTR + ':' + RIGHT_VALUE + ';';

	/** Constant for value {@value}. */
	public static final String TEXT_ALIGN_CENTER = TEXT_ALIGN_ATTR + ':' + CENTER_VALUE + ';';

	/** Constant for value {@value}. */
	public static final String TEXT_ALIGN_LEFT = TEXT_ALIGN_ATTR + ':' + LEFT_VALUE + ';';

	/**
	 * Prefix for HTML5 data attributes (generic custom attributes not defined by the DTD).
	 */
	public static final String DATA_ATTRIBUTE_PREFIX = "data-";

	/**
	 * Special attribute annotating a tool-tip to its element.
	 * 
	 * <p>
	 * This attribute should not be used by application code. Use service methods in
	 * {@link OverlibTooltipFragmentGenerator} to write a tooltip.
	 * </p>
	 * 
	 * @see OverlibTooltipFragmentGenerator#writeTooltipAttributesPlain(DisplayContext, TagWriter,
	 *      String) Write a HTML tooltip.
	 * @see OverlibTooltipFragmentGenerator#writeTooltipAttributesPlain(DisplayContext, TagWriter,
	 *      String, String) Write a HTML tooltip with caption.
	 * @see OverlibTooltipFragmentGenerator#writeTooltipAttributes(DisplayContext, TagWriter,
	 *      String) Write a plain text tooltip.
	 * @see OverlibTooltipFragmentGenerator#writeTooltipAttributes(DisplayContext, TagWriter,
	 *      String, String) Write a plain text tooltip with caption.
	 */
	@FrameworkInternal
	public static final String TL_TOOLTIP_ATTR = DATA_ATTRIBUTE_PREFIX + "tooltip";
	
	/**
	 * The attribute value is a reference to the javascript object where the
	 * javascript functions for the element of this attribute exist.
	 */
	public static final String TL_TYPE_ATTR = DATA_ATTRIBUTE_PREFIX + "type";

	/**
	 * Attribute to activate &quot;Drag'n'Drop&quot; in a {@link Drag} source.
	 * 
	 * <p>
	 * The value must be a JSON formatted list of client-side IDs of elements that may serve as
	 * {@link Drop} targets in a &quot;Drag'n'Drop&quot; process. An example of a legal attribute
	 * value is e.g "(['c43','c7','c567'])" (if there are elements on the client with those ID's).
	 * </p>
	 */
	public static final String TL_DRAG_N_DROP = DATA_ATTRIBUTE_PREFIX + "dnd";

	/**
	 * Attribute to set the preview image of the element that is dragged.
	 * 
	 * <p>
	 * The client parses the specified text value as html and adds it to dom container element from
	 * which the actual drag image is created.
	 * </p>
	 */
	public static final String TL_DRAG_IMAGE = DATA_ATTRIBUTE_PREFIX + "drag-image";

	/**
	 * Custom attribute describing key press events that are required on server-side for a control.
	 * 
	 * <p>
	 * The attribute must only be used on a control element that also has the {@link #ID_ATTR} with
	 * the control ID of the control to dispatch the key event to.
	 * </p>
	 * 
	 * <p>
	 * The format of the attribute value must be a space separated list of key selectors. A key
	 * selector is built according to the following pattern: "KS?C?A?\d\d\d". The letters "S", "C"
	 * and "A" represent the modifiers shift, ctrl, and alt respectively. The scan code is the three
	 * digit numeric key code (zero padded).
	 * </p>
	 */
	public static final String TL_KEY_SELECTORS = DATA_ATTRIBUTE_PREFIX + "keys";

	/**
	 * Custom attribute, to flag a node as the root of a tabbing cycle tree.
	 */
	public static final String TL_TAB_ROOT = DATA_ATTRIBUTE_PREFIX + "tabroot";

	/**
	 * Identifier of the element where to start searching for the next focusable element in TAB
	 * order if no candidate has found within the element declaring this attribute.
	 * 
	 * @see #TL_TAB_PREV
	 */
	public static final String TL_TAB_NEXT = DATA_ATTRIBUTE_PREFIX + "tabnext";

	/**
	 * Like {@link #TL_TAB_NEXT} for inverse (SHIFT) tab order.
	 */
	public static final String TL_TAB_PREV = DATA_ATTRIBUTE_PREFIX + "tabprev";
	
	/**
	 * Custom attribute, to flag a node as the begin of a complex widget.
	 */
	public static final String TL_COMPLEX_WIDGET_ATTR = DATA_ATTRIBUTE_PREFIX + "complexwidget";

	/**
	 * Custom attribute, to flag a node as a frame, filled with external content.
	 */
	public static final String TL_EXTERNAL_FRAME_ATTR = DATA_ATTRIBUTE_PREFIX + "externalframe";

	/**
	 * Custom attribute for a frame's source reference.
	 */
	public static final String TL_FRAME_SOURCE_ATTR = DATA_ATTRIBUTE_PREFIX + "source";

	/**
	 * Custom attribute for a element's height.
	 */
	public static final String TL_HEIGHT_ATTR = DATA_ATTRIBUTE_PREFIX + "height";

	/**
	 * Custom attribute for a element's width.
	 */
	public static final String TL_WIDTH_ATTR = DATA_ATTRIBUTE_PREFIX + "width";

	/**
	 * Custom attribute for an element's context menu.
	 * 
	 * <p>
	 * The value of this attribute is passed to {@link ContextMenuOwner#createContextMenu(String)}
	 * when a context menu is opened on an HTML element that has this attribute.
	 * </p>
	 */
	public static final String TL_CONTEXT_MENU_ATTR = DATA_ATTRIBUTE_PREFIX + "context-menu";

	/**
	 * Custom attribute to open a browser context menu instead of a TL context menu.
	 * 
	 * <p>
	 * <code>true</code> if the browser menu should be opened. <code>false</code> if the TL context
	 * menu should be opened.
	 * </p>
	 */
	public static final String TL_BROWSER_MENU_ATTR = DATA_ATTRIBUTE_PREFIX + "browser-menu";

	/** Custom attribute to define when an action is complete. f.e. {@link ProgressControl} */
	public static final String COMPLETE_ATTR = DATA_ATTRIBUTE_PREFIX + "complete";

	/**
	 * Attribute value to denote an empty source reference (e.g. used in combination with
	 * {@link #SRC_ATTR}).
	 */
	public static final String EMPTY_SOURCE_VALUE = "about:blank";

	/**
	 * value to inform the {@link ServletResponse#setContentType(String) response} that the content
	 * consists of html.
	 */
	public static final String CONTENT_TYPE_TEXT_HTML = "text/html";


	/**
	 * value to inform the {@link ServletResponse#setContentType(String) response} that the content
	 * consists of 'text/html' and the character set is 'utf-8'.
	 */
	public static final String CONTENT_TYPE_TEXT_HTML_UTF_8 = "text/html;charset=" + StringServices.UTF8;

	/**
	 * Type of .ico files.
	 */
	public static final String TYPE_ICO = "image/x-icon";

	/**
	 * Type of .gif files.
	 */
	public static final String TYPE_GIF = "image/gif";

	/**
	 * Type of .png files.
	 */
	public static final String TYPE_PNG = "image/png";

	/**
	 * Type of .jpeg files.
	 */
	public static final String TYPE_JPEG = "image/jpeg";

	/**
	 * Type of .svg files.
	 */
	public static final String TYPE_SVG = "image/svg+xml";

	/**
	 * Tells the browser to open a new window or tab when the link is clicked.
	 */
	public static final String BLANK_VALUE = "_blank";

	/**
	 * HTTP is an application protocol for information systems.
	 */
	public static final String HTTP_VALUE = "http://";

	/**
	 * All void element whose content model never allows it to have contents under any circumstances.
	 * Only those elements must be written as XML empty tags in HTML5. Void elements can have
	 * attributes.
	 * 
	 * @see <a href=
	 *      "http://www.w3.org/TR/html-markup/syntax.html#syntax-elements">W3C-HTML5-Elementsyntaxspezifikation</a>
	 */
	public static final Set<String> VOID_ELEMENTS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
		AREA, BASE, BR, COL, COMMAND, EMBED, HR, IMG, INPUT, KEYGEN, LINK, META, PARAM, SOURCE, TRACK, WBR)));

	/**
	 * All boolean HTML attributes.
	 * 
	 * <p>
	 * A boolean attribute must either have the attribute name as its value (to represent the
	 * <code>true</code> value), or must not be set on the element (to represent the
	 * <code>false</code> value).
	 * </p>
	 */
	public static final Set<String> BOOLEAN_ATTRIBUTES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
		CHECKED_ATTR,
		READONLY_ATTR,
		REQUIRED_ATTR,
		MULTIPLE_ATTR,
		DISABLED_ATTR,
		SELECTED_ATTR,
		NOVALIDATE_ATTR,
		FORMNOVALIDATE_ATTR,
		AUTOFOCUS_ATTR)));

}
