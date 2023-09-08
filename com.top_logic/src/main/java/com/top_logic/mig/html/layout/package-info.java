/**
 * Layouts help managing Complex ensembles of HTML pages.
 * <p>
 * A Layout consists of a Number of {@link com.top_logic.mig.html.layout.LayoutComponent}s. A
 * Structuring Component is the the Layout that represents a Framset or a HTML-Table. The Top-Level
 * component can be a {@link com.top_logic.mig.html.layout.MainLayout}. It has a set of static
 * Functions that help manage the layout as a whole.
 * </p>
 * <p>
 * <h2>Usage</h2> Simple JSP or static HTML pages can be integrated with the
 * <code>PageComponent</code>. JSP pages in this context can access their <code>PageComponent</code>
 * and through it their model and other components using the <code>layout</code> tag library.The
 * <code>html</code> tag starts the Layout-aware part. The <code>action</code> tag is the right
 * position to manipulate the model. In the MVC-concept it represents the controller. Model changes
 * should only occure if the request is valid using
 * {@link com.top_logic.mig.html.layout.LayoutComponent#getSubmitNumber()}. The <code>head</code>
 * tag marks the position at which default head data is printed. The <code>body</code> tag marks the
 * position at which the body is printed. <br/>
 * <b>Note:</b> All links to {@link com.top_logic.mig.html.layout.LayoutComponent}s including links
 * to the same component/JSP must include the layout id (at the
 * <code>LayoutConstants.PARAM_LAYOUT</code> parameter) and should contain the submit number (at the
 * <code>LayoutConstants.PARAM_SUBMIT_NUMBER</code> parameter). If forms are submitted it is
 * clearest to use <code>LayoutConstants.PARAM_SUBMIT</code> for forms that use only one submit
 * button and <code>LayoutConstants.PARAM_SUBMIT + "." + somevalue</code> for forms that have more
 * than one submit button. Consider using the <code>writeSubmitLink</code> or the
 * <code>getFrameURL</code> method to generate links.
 * <h2>Extending</h2> In some cases it will be necessary to subclass
 * {@link com.top_logic.mig.html.layout.LayoutComponent}. In this case, besides the things necessary
 * for XML-Serialization the basic methods for rendering the component must be overwritten. These
 * are:
 * <ul>
 * <li><code>getModel</code> is an abstract method. You should have your own getter and setter
 * methods for your model. <code>getModel</code> should simply return your accessor method.</li>
 * <li><code>getOnLoadFunctionCalls</code> to call javascript functions that are declared in the
 * header when the page has been loaded completely.</li>
 * <li><code>getOnLoadFunctionCalls</code> to call javascript functions that are declared in the
 * header when the page is replaced by another page.</li>
 * <li><code>isCompleteRenderer</code>, if you don't want the component to write the the html
 * structure. Consider using a JSP instead.</li>
 * <li><code>isFrameSet</code>, if the page is a frame set. Consider sublcassing -- or maybe just
 * using -- the class <code>Layout</code>.</li>
 * <li><code>writeHeader</code>, to write the contents of the html header -- just the contents, not
 * the html-tag</li>
 * <li><code>writeBody</code>, to write the contents of the html body -- just the contents, not the
 * body-tag</li>
 * </ul>
 * 
 * Also the <code>prepare</code> methods can be overwriten. Here the controller function of the MVC
 * concept is implemented.
 * 
 * <h2>XML-Serlialization</h2>
 * <h3>Usage of the XML-Serialization</h3> Layouts can be serialized to an XML-File, by calling the
 * static method <code>MainLayout.writeLayoutAsXML</code>. A file, containing xml of the expected
 * form, can be deserialized to a Layout by calling <code>MainLayout.getLayoutFromXML</code>. The
 * state of the {@link com.top_logic.mig.html.layout.LayoutComponent}'s models is not written to
 * file. Thus after deserializing the model is <code>null</code> or the component's defaultmodel in
 * the default state.<br/>
 * <h3>Design of the XML-Serialization</h3> Each
 * {@link com.top_logic.mig.html.layout.LayoutComponent} must overwride the following methods to be
 * XML-serializable
 * <ul>
 * <li>A constructor taking <code>org.xml.sax.Attributes</code>. Must call the super constructor and
 * read in all its directly implemented fields from the attributes.</li>
 * <li><code>writeFieldsAsXML</code>. Must call the super method and write out all its non-transient
 * fields as attributes using <code>TagWriter.writeAttribute</code>.</li>
 * <li><code>writeInnerCollectionsAsXML</code>. Must call the super method and write all
 * non-transient inner Sets or Lists using <code>writeListAsXML</code>. The set-version is not
 * implemented yet.</li>
 * <li><code>findList</code>. Must return the list, that was serialized by the given name or call
 * the super method if the name is unknown.</li>
 * <li><code>setSelectedXMLList</code>. If specific actions need to be taken on begin or ending the
 * deserialization of a list.</li>
 * <li><code>postLoad</code>. If actions need to be taken after all components have been
 * deserialized</li>
 * </ul>
 * List elements are expected to either be {@link com.top_logic.mig.html.layout.LayoutComponent}s --
 * which is necesary to write the the subclasses of Layout and shouldn't be used anywhere else --
 * implement XMLSerializable or contain their value within their <code>toString</code> method and
 * have a string constructor.
 * </p>
 */
package com.top_logic.mig.html.layout;
