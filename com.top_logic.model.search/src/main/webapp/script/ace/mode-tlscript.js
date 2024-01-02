ace.define('ace/mode/tlscript', function(require, exports, module) {
	var langTools = require('../ext/language_tools');
	
	// Completer object for TL-Script.
	var tlScriptCompleter = {
			
			/**
			 * Invoked if the identifier regular expression below is matched.
			 */
			getCompletions: function(editor, session, pos, prefix, callback) {
				if (editor.session.$modeId != "ace/mode/tlscript") {
					// Not a TL-Script editor.
					return;
				}
				ace.tlscriptCompletionCallback = callback;
				
				var editorID = editor.container.id;
				var controlID = editorID.substring(0, editorID.lastIndexOf('-editor'));
				
				var line = session.getLine(pos.row);
				var prefixLine = line.substring(0, pos.column);
				
				// Delegates the computation of suggestions to the server.
				services.ajax.execute('dispatchControlCommand', {controlCommand: 'tlScriptAutoCompletion', controlID: controlID, prefix: prefix, line: prefixLine });
			},
			
			/**
			 * Regular expression for an identifier deciding if completions should be computed and shown.
			 */
			identifierRegexps: [
				/[a-zA-Z0-9$]+/
			]
	};
	
	// Set the custom completer and removes all default completers.
	langTools.setCompleters([tlScriptCompleter]);
	
	var oop = require("ace/lib/oop");
	
	// Basic ACE mode.
	var TextMode = require("ace/mode/text").Mode;
	var TLScriptHighlightRules = require("ace/mode/tlscript_highlight_rules").TLScriptHighlightRules;
	
	// Create a ACE mode with the given highlighting rules.
	var Mode = function() {
		this.HighlightRules = TLScriptHighlightRules
	};
	
	// Realizes a javascript inheritance.
	oop.inherits(Mode, TextMode);
	
	// Makes it visible to the caller.
	exports.Mode = Mode;
});

/**
 * ACE Module for TL-Script syntax highlighting.
 */
ace.define('ace/mode/tlscript_highlight_rules', function(require, exports, module) {
	var oop = require("ace/lib/oop");
	
	// Basic text highlighting rules.
	var TextHighlightRules = require("ace/mode/text_highlight_rules").TextHighlightRules;
	
	// Identifier regular expression, i.e. begins with a raw character followed by any alphanumeric character.
	var identifierRegEx = "[a-zA-Z][a-zA-Z0-9]*";
	
	/** 
	 * TL-Script specific highlighting rules.
	 * 
	 * The names of the JSON object this.$rules defining modes. For instance: "start", "function", etc.
	 * "start" is the entry point for the syntax highlighting.
	 * 
	 * Then it tries to match tokens with the given regular expressions defined in the current mode, from top to bottom,
	 * as long as the mode does not change. 
	 * 
	 * The next keyword switches the mode.
	 * The push and pop keyword work together. The push keyword "switches" the mode but caches the mode that can be recovered
	 * by another mode with the switch to pop.
	 * 
	 * The token keyword identifies the matched text by the given regular expression with a highlighting class.
	 * All elements in a specific highlighting class are colored the same.
	 */
	var TLScriptHighlightRules = function() {
		this.$rules = {
			"start": [
				{
					include: "function"
				},
				{
					include: "core"
				}
			],
			"core": [
				{
					// Space
					token: "text",
					regex: "\\s+|^$"
				},
				{
					// Integer and floats
					token: "constant.numeric",
					regex: /(?:[1-9]\d*|0)(?:.\d+)?/
				}, 
				{
					token: "constant.language.boolean",
					regex: "true|false"
				}, 
				{
					token: "constant.language",
					regex: "null"
				},
				{
					// Resource key
					// #"my.res.key"
					// #'my.res.key'
					token: "support.variable",
					regex: /#(?:(?:"[\w\.]+")|(?:'[\w\.]+'))/
				}, 
				{
					// Literale Resourcen
					// #("Ein Text"@de, "Some text"@en, "Un texto"@es)
					// #('Ein Text'@de, 'Some text'@en, 'Un texto'@es)
					token: "support.variable",
					regex: /#\((?:(?:(?:"\w[\w\.\s]*")|(?:'\w[\w\.\s]*'))\@\w+)(?:\s*,\s*(?:(?:"\w[\w\.\s]*")|(?:'\w[\w\.\s]*'))\@\w+)*\)/
				},
				{
					// Literale Resourcen (one liner)
					// "some string"@de
					// 'some other string'@en
					token: "support.variable",
					regex: /(?:(?:"\w[\w\s]*")|(?:'\w[\w\s]*'))\@\w+/
				},
				{
					// Model constants
					// `DemoTypes:AChild#parent`
					token: "support.constant",
					regex: /`\w[\w\.]*(:\w[\w\.]*)?(#\w[\w\.]*)?`/
				},
				{
					// Single quoted strings
					// 'text'
					token: "string.single",
					regex: /'\w*'/
				},
				{
					// Double quoted strings
					// "text"
					token: "string.single",
					regex: /"\w*"/
				},
				{
					// Arithmetic operators
	                token : "keyword.operator",
	                regex : /\+|-|\*|\/|%/
				},
				{
					// Boolean logic operators
					token : "keyword.operator",
					regex : /&&|and|\|\||or|==|!=|!/
				},
				{
					// Ordered logic operators
					token : "keyword.operator",
					regex : />=|<=|>|</
				}, 
				{
					// Function call operator
					token: "punctuation.operator",
					regex: /\./
				},
				{
					// Left parenthesis
					token : "paren.lparen",
	                regex : /[({]/
				},
				{
					// Right parenthesis
	                token : "paren.rparen",
	                regex : /[)}]/
				}
			],
			"function": [
				{
					// Function begin
					token: ["support.function", "paren.lparen"],
					regex: "(\\w+)(\\()",
					push: "function"
				},
				{
					// Function end
					token: "paren.rparen",
					regex: /\)/,
					next: "pop"
				},
				{
					// Separator for function arguments
					token: "punctuation.operator",
					regex: /[, ]+/
				},
				{
					include: "lamda"
				},
				{
					// Function argument
					token: "variable.parameter",
					regex: "\\w+"
				},
				{
					include: "core"
				}
			],
			"lamda": [
				{
					// Lambda expression beginning
					// myVar -> 
					token: ["variable.parameter", "support.function"],
					regex: "(\\w+\\s*)(->)"
				},
				{
					// Variable dereference
					// $myVar
					token: "variable.other",
					regex: /\$\w+/
				}
			]
		};
		
	    this.normalizeRules();
	}
	
	// Realizes a javascript inheritance.
	oop.inherits(TLScriptHighlightRules, TextHighlightRules);
	
	// Makes it visible to the caller.
	exports.TLScriptHighlightRules = TLScriptHighlightRules;
});

services.tlscriptsearch = {
		
	/**
	 * Shows the given completions in a popup.
	 */
	showCompletions: function(completions) {
		ace.tlscriptCompletionCallback(null, JSON.parse(completions));
	}

}