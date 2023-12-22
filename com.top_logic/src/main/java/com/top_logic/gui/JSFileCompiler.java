/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.gui;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.ResourceDeclaration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.io.FileCompiler;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.mig.html.HTMLUtil;

/**
 * Compile (most) TopLogic JS-Files into one big file.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public final class JSFileCompiler extends FileCompiler {

	/**
	 * Configuration for the {@link JSFileCompiler}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static interface Config extends FileCompiler.Config {

		/** Configuration name for the value {@link Config#isStripFile()}. */
		String STRIP_FILE_NAME = "strip-file";

		/** Configuration name for the value {@link Config#useOriginalNames()}. */
		String ORIGINAL_NAMES_NAME = "original-names";

		/** Configuration name for the value {@link Config#getAdditionalFiles()}. */
		String ADDITIONAL_FILES_NAME = "additional-files";

		@Override
		@StringDefault("script/top-logic.js")
		String getTarget();

		/** Whether the files shall be stripped */
		@Name(STRIP_FILE_NAME)
		@BooleanDefault(false)
		boolean isStripFile();

		/**
		 * Do not use single stripped results but the original ones , use for debugging (the smaller
		 * files) only.
		 */
		@Name(ORIGINAL_NAMES_NAME)
		boolean useOriginalNames();

		/**
		 * Additional JavaScript files which are not included into the compiled file.
		 */
		@Name(ADDITIONAL_FILES_NAME)
		@Key(ResourceDeclaration.RESOURCE_ATTRIBUTE)
		@EntryTag("file")
		List<ScriptResourceDeclaration> getAdditionalFiles();
	}

	private static final char WHITESPACE = ' ';
    
    private static final char NEWLINE = '\n';

	private Map<String, String> _typeByAdditionalFiles;

	/**
	 * ES6 module specifier mapped to a resource that can be dynamically loaded.
	 */
	private Map<String, String> _importMap;
    
     /**
	 * Creates a new {@link JSFileCompiler} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link JSFileCompiler}.
	 * 
	 */
	public JSFileCompiler(InstantiationContext context, Config config) {
		super(context,config);
	}

	private Config config() {
		return (Config) getConfig();
	}

	@Override
	protected void startUp(InstantiationContext context) {
		super.startUp(context);

		resolveAdditionalFilesReferences(context);
	}

	private void resolveAdditionalFilesReferences(InstantiationContext context) {
		_typeByAdditionalFiles = new LinkedHashMap<>();
		_importMap = new LinkedHashMap<>();

		for (ScriptResourceDeclaration resourceDeclaration : config().getAdditionalFiles()) {
			String resourcePath = resolveResourcePath(context, resourceDeclaration);

			if (isAccessable(context, resourcePath)) {
				String specifier = resourceDeclaration.getAs();
				if (specifier == null) {
					_typeByAdditionalFiles.put(resourcePath, resourceDeclaration.getType());
				} else {
					_importMap.put(specifier, resourcePath);
				}
			}
		}
	}

	@Override
	protected void shutDown() {
		_typeByAdditionalFiles = null;
		_importMap = null;
		super.shutDown();
	}

    @Override
	protected void compile(Reader in, Writer out, String aPath) throws IOException {
        out.write(";\n// Compiled from '" + aPath + "'\n");
        
		if (config().isStripFile()) {
        	strip(in, out);
        } else {
        	super.compile(in, out, aPath);
        }
        
    }
    
    /**
     * Strips the input stream into the output stream.
     */
    public void strip(Reader in, Writer out) throws IOException {
        int ch0 = -1;
        int ch1 = in.read();
        int ch2 = -1;

        while (ch1 != -1) {
            switch (ch1) {
                case '/':
                    ch2 = in.read();

                    switch (ch2) {
                        case '/':
                            ch1 = stripCPPComments(in, out);
                            break;

                        case '*':
                            ch1 = stripCComment(in, out);
                            break;

                        default:
                            out.write(ch1);
                            ch0 = ch1;
                            ch1 = preserveRegExp(in, out, ch2);
                            break;
                    }
                    break;

                case '\"':
                    out.write(ch1);
                    ch0 = ch1;
                    ch1 = preserveDoubleQuote(in, out);
                    break;

                case '\'':
                    out.write(ch1);
                    ch0 = ch1;
                    ch1 = preserveSingleQuote(in, out);
                    break;

                case '\n':
                case '\r':
                    // if (isAmbigousSep(ch0)) { // TODO needed ?
                        out.write(ch0 = NEWLINE);
                    // }
                    ch1 = stripWhitespace(in, out, /* newline */ true);
                    break;
                case ' ':
                case '\t':
                    if (isAmbigousSep(ch0)) { // TODO needed ?
                        ch1 = stripWhitespace(in, out, false);
                        if (!isSeperator(ch0) && !isSeperator(ch1)) {
                            out.write(ch0 = WHITESPACE);
                            
                        }
                    } else {
                        ch1 = stripWhitespace(in, out, true);
                    }
                    break;

                default:
                    out.write((char) ch1);
                    ch0 = ch1;
                    ch1 = in.read();
                    break;
            }
        }
    }

    /**
     * @param newline strip newline as whitespace, too
     */
    protected int stripWhitespace(Reader in, Writer out, boolean newline) throws IOException {
        int ch = in.read();

        while (ch != -1) {
            switch (ch) {
                case '\n':
                case '\r':
                    // some statements don't end in ; but \n
                    if (!newline) {
                        out.write(NEWLINE);
                        newline = true;
                    }
                case ' ':
                case '\t':
                    break;
                default:
                    return ch;
            }
            ch = in.read();
        }

        return ch;
    }

    /* Ignore Comments like this one */ 
    protected int stripCComment(Reader in, Writer out) throws IOException {
        int ch0 = in.read();

        if (ch0 == -1) {
            return -1;
        }
        // special msie conditional compilation
        if (ch0 == '@') {
            out.write("/*");
            return '@';
        }

        int ch1 = in.read();

        while (ch1 != -1) {
            if (ch1 == '/' && ch0 == '*') {
                ch1 = in.read();
                break;
            }

            ch0 = ch1;
            ch1 = in.read();
        }

        return ch1;
    }

    // Ignore Comments like this one 
    protected int stripCPPComments(Reader in, Writer out) throws IOException {
        int ch;
        
        do {
            ch = in.read();
        } while (ch != -1 && ch != '\n' && ch != '\r');

        return ch;
    }

    /** Keep "quoted Text" including "Escaped\tParts\"YouSee\"" */
    protected int preserveDoubleQuote(Reader in, Writer out) throws IOException {
        int ch = in.read();

        if (ch == -1) {
            return -1;
        }

        out.write(ch);

        if (ch == '\"') {
            return in.read();
        }

        boolean escaped = (ch == '\\');
        ch = in.read();

        while (ch != -1) {
            out.write((char) ch);

            if (ch == '\"' && !escaped) {
                ch = in.read();
                break;
            }

            // toggle escaped escape char
            escaped = (!escaped && ch == '\\');
            ch = in.read();
        }

        return ch;
    }

    /** Keep 'quoted Text' including 'Escaped\tParts\'YouSee\'' */
    protected int preserveSingleQuote(Reader in, Writer out) throws IOException {
        int ch = in.read();

        if (ch == -1) {
            return -1;
        }

        out.write(ch);

        if (ch == '\'') {
            return in.read();
        }

        boolean escaped = (ch == '\\');
        ch = in.read();

        while (ch != -1) {
            out.write((char) ch);

            if (ch == '\'' && !escaped) {
                ch = in.read();
                break;
            }

            // toggle escaped escape char
            escaped = (!escaped && ch == '\\');
            ch = in.read();
        }

        return ch;
    }

    /** Keep (rare) regula expressions */
    protected int preserveRegExp(Reader in, Writer out, int ch) throws IOException {
        out.write((char) ch);
        
        boolean escaped = (ch == '\\');
        ch = in.read();

        while (ch != -1) {
            out.write((char) ch);

            // actual a divide operator, not a regexp
            if (ch == '\n' || ch == '\r') {
                return ch;
            }
            
            if (ch == '/' && !escaped) {
                ch = in.read();
                break;
            }

            // toggle escaped escape char
            escaped = (!escaped && ch == '\\');
            ch = in.read();
        }

        return ch;
    }

    /**
     * Non-javascript-identifier characters.
     */
    protected boolean isSeperator(int ch) {
        // all 7-bit ASCII except alpha-numerics, $, _ (both legal JavaScript identifiers), and @ (used by MSIE conditional compilation)
        return ((ch < '0' || (ch > '9' && ch < '@') || (ch > 'Z' && ch < '_') || (ch > 'z' && ch < 0x7f)) && ch != '$');
    }
    

    /**
     * Non-javascript-identifier characters that, when at the end of a line, can indicate the end of a statement.
     */
    protected boolean isAmbigousSep(int aChar) {
        return !(isSeperator(aChar) && aChar != ')' && aChar != ']' && aChar != '}' && aChar != '/' && aChar != '\'' && aChar != '\"');
    }

    /** 
     * Write the (single or multiple) References to the JS-Files using relative aContextpath.
     */
    public final void writeJavascriptRef(TagWriter out, String aContextpath) throws IOException {
		String reloadSuffix = "?t=" + getLastUpdate();
		if (config().useOriginalNames()) {
			checkForUpdate();
			for (String baseName : getBaseNames()) {
				HTMLUtil.writeJavaScriptRef(out, aContextpath, baseName, reloadSuffix);
			}
		} else {
			HTMLUtil.writeJavaScriptRef(out, aContextpath, getTarget(), reloadSuffix);
        }

		// <script type="importmap">
		// {
		//   "imports": {
		//     "lodash/": "/node_modules/lodash-es/"
		//   }
		// }
		// </script>		
		if (!_importMap.isEmpty()) {
			out.beginBeginTag(SCRIPT);
			out.writeAttribute(TYPE_ATTR, "importmap");
			out.endBeginTag();
			
			// Do not close to prevent closing the underlying writer.
			@SuppressWarnings("resource")
			JsonWriter json = new JsonWriter(out);
			{
				json.beginObject();
				{
					json.name("imports");
					json.beginObject();
					{
						for (Entry<String, String> entry : _importMap.entrySet()) {
							json.name(entry.getKey());
							json.value(aContextpath + entry.getValue());
						}
					}
					json.endObject();
				}
				json.endObject();
			}
			
			out.endTag(SCRIPT);
		}

		for (Entry<String, String> typeByAdditionalFile : _typeByAdditionalFiles.entrySet()) {
			HTMLUtil.writeJavaScriptRef(out, aContextpath, typeByAdditionalFile.getKey(), reloadSuffix,
				typeByAdditionalFile.getValue());
		}
    }

    public static JSFileCompiler getInstance() {
    	return Module.INSTANCE.getImplementationInstance();
    }
    
	public static final class Module extends TypedRuntimeModule<JSFileCompiler> {

		public static final Module INSTANCE = new Module();

		@Override
		public Class<JSFileCompiler> getImplementation() {
			return JSFileCompiler.class;
		}

	}

}

