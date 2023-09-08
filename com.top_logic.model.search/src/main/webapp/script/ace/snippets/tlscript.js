/**
 * Setup as a ACE module.
 */
ace.define("ace/snippets/tlscript",["require","exports","module"], function(require, exports, module) {
	"use strict";
	
	exports.snippetText = undefined;
	exports.scope = "tlscript";
});

(function() {
    ace.require(["ace/snippets/tlscript"], function(m) {
        if (typeof module == "object" && typeof exports == "object" && module) {
            module.exports = m;
        }
    });
})();