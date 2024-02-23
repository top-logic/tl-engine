import Editor from '@toast-ui/editor';
//import codeSyntaxHighlight from '@toast-ui/editor-plugin-code-syntax-highlight';
//import * as colorPicker from 'tui-color-picker';
//import * as colorSyntax from '@toast-ui/editor-plugin-color-syntax';
//import * as tableMergedCell from '@toast-ui/editor-plugin-table-merged-cell';
//import * as chart from '@toast-ui/editor-plugin-chart';
//import * as uml from '@toast-ui/editor-plugin-uml';
//import '@toast-ui/editor/dist/toastui-editor.css'; // Editor's Style

services.wysiwyg = {
	loadCkeditor: function(url, callback) {
		var loaderId = "CKEDITOR_LOADER";
		var script = document.getElementById(loaderId);
		
		if (script == null) {
			script = document.createElement("script");
			script.id = loaderId;
			script.type = "text/javascript";
			script.src = url;
			script.addEventListener("load", callback);
			
			document.body.appendChild(script);
		} else {
			script.addEventListener("load", callback);
		}
	},
	
	/**
	 * Lazy loading of CKEDITOR script.
	 */
	ckeditor: function(callback) {
		var lib = window["CKEDITOR"];
		if (lib === undefined) {
			this.loadCkeditor(WebService.prototype.CONTEXT_PATH + "/script/ckeditor/ckeditor.js", 
				function() {
					callback(CKEDITOR);
				}
			);
		} else {
			callback(lib);
		}
	},

	StructuredText: {
		
		init: function(controlId, contentId, configString, tlObjectClass, tlObjectWrapperClass, useComponentScrollPosition) {
			let content = document.getElementById(contentId);
//			const TUIEditor = toastui.Editor;
//			const { Editor } = toastui;
//			const { colorSyntax } = Editor.plugin;
			const { colorSyntax } = toastui.Editor.plugin;
			const editor = new Editor({
				el: content,
				height: '400px',
				previewStyle: 'vertical',
				previewHighlight: true,
				initialEditType: 'markdown', // wysiwyg
				autofocus: false,
				plugins: [colorSyntax],
			});
			
			editor.on('change', function(evt) {
//				var topLevelDoc = BAL.getTopLevelDocumentOf(content);
//				if (KEYBOARD_NAVIGATION.isAnyDialogOpen(topLevelDoc)) {
//					if (KEYBOARD_NAVIGATION.getDialog(content) != KEYBOARD_NAVIGATION.getTopmostDialog(topLevelDoc)) {
//						return;
//					} 
//				}
				// evt.editor.getData() has turned some spaces into non-breaking spaces. They have to be replaced by normal spaces.
//				data = data.replaceAll(String.fromCharCode(160), ' ');
//				data = data.replaceAll('&nbsp;', ' ');
				let data = editor.getHTML();
				let requestID = services.ajax.createLazyRequestID();
				services.ajax.executeOrUpdateLazy(requestID, "dispatchControlCommand", {
					controlCommand : "valueChanged",
					controlID : controlId,
					value : data
				});
		
			});
			
			editor.on('keydown', function(evt) {
				event.stopImmediatePropagation();
			});
//			var requestID = services.ajax.createLazyRequestID();
//			services.wysiwyg.ckeditor(function(ckeditor) {
//				var config = JSON.parse(configString);
//				var templateFiles = config.templates_files;
//				templateFiles.forEach(function (templateFile, index) {
//		 			templateFiles[index] = CKEDITOR.getUrl(templateFile);
//				});
//				
//				var contentRule = config.extraAllowedContent;
//				
//				// Allow embedded TL-Script expressions.
//				contentRule.script = {
//					match: function( element ) {
//						return element.attributes['type'] == "text/tlscript";
//					}
//				};
//				
//				services.wysiwyg.StructuredText.cleanup(contentId);
//				
//				var editor = ckeditor.replace(contentId, config);
//				
//				editor.on('change', function(evt) {
//					var topLevelDoc = BAL.getTopLevelDocumentOf(content);
//					if (KEYBOARD_NAVIGATION.isAnyDialogOpen(topLevelDoc)) {
//						if (KEYBOARD_NAVIGATION.getDialog(content) != KEYBOARD_NAVIGATION.getTopmostDialog(topLevelDoc)) {
//							return;
//						} 
//					}
//					var data = evt.editor.getData();
//					// evt.editor.getData() has turned some spaces into non-breaking spaces. They have to be replaced by normal spaces.
//					data = data.replaceAll(String.fromCharCode(160), ' ');
//					data = data.replaceAll('&nbsp;', ' ');
//					services.ajax.executeOrUpdateLazy(requestID, "dispatchControlCommand", {
//						controlCommand : "valueChanged",
//						controlID : controlId,
//						value : data
//					});
//			
//				 });
//				 editor.on('paste', function(evt) {
//				 	// evt.data.dataValue has turned some spaces into non-breaking spaces. They have to be replaced by normal spaces.
//				 	var clipboard = evt.data.dataValue.replaceAll(String.fromCharCode(160), ' ');
//					var doc = new DOMParser().parseFromString(clipboard, "text/html");
//
//					// Remove BaseURI from TLObject links
//					var links = doc.querySelectorAll("a." + tlObjectClass);
//				 	Array.prototype.forEach.call(links, function (link) {
//			 			link.setAttribute("href", link.href.substr(link.href.indexOf('?')));
//					});
//					
//					// Remove Icons of TLObject links because they are rendered automatically and should not be saved inside of the source code.
//					var icons = doc.querySelectorAll("span." + tlObjectWrapperClass);
//				 	Array.prototype.forEach.call(icons, function (icon) {
//			 			var anchor = icon.querySelector("a");
//			 			icon.parentNode.insertBefore(anchor, icon);
//			 			icon.remove();
//					});
//					
//					// Remove all spans cause they should never be copied. This has to be done after the removal of TLObjectLink icons because they are identified by the tlObjectWrapperClass in a span
//					var spans = doc.querySelectorAll("span");
//				 	Array.prototype.forEach.call(spans, function (span) {
//			 			var parent = span.parentNode;
//			 			while (span.firstChild) parent.insertBefore(span.firstChild, span);
//			 			parent.removeChild(span);
//					});
//					
//					// Replace b tags by strongs
//					var bs = doc.querySelectorAll("b");
//				 	Array.prototype.forEach.call(bs, function (b) {
//			 			var strong = document.createElement('strong');
//						strong.innerHTML = b.innerHTML;
//						while(b.firstChild) {
//					         strong.appendChild(b.firstChild);
//					     }
//						b.parentNode.replaceChild(strong, b);
//					});
//					
//				    evt.data.dataValue = doc.body.innerHTML;
//				    
//		         });
//		         editor.on( 'contentDom', function() {
//			         if(!useComponentScrollPosition) {
//			         	return;
//			         }
//		         	 var frame = document.querySelector('[title*="' + contentId + '"]');
//					 if(frame == null) {
//					 	return;
//					 }
//			    	 var scrollContainer = services.wysiwyg.StructuredText.findScrollContainer(frame);
//			    	 if(scrollContainer == null) {
//			    	 	return;
//			    	 }
//			    	 
//				 	 services.wysiwyg.StructuredText.initScrollPosition(editor, frame, scrollContainer);
//				 });
//				 CKEDITOR.on( 'dialogDefinition', function( ev ) {
//				    var dialogName = ev.data.name;
//				    var dialogDefinition = ev.data.definition;
//				
//				    if ( dialogName == 'table' ) {
//				        var advanced = dialogDefinition.getContents( 'advanced' );
//				        var info = dialogDefinition.getContents( 'info' );
//				
//				        advanced.get( 'advCSSClasses' )[ 'default' ] = 'tlDocTable';
//				        info.get( 'selHeaders' )[ 'default' ] = 'row';
//				        info.get( 'txtCellSpace' )[ 'default' ] = '';
//				        info.get( 'txtCellPad' )[ 'default' ] = '';
//				        info.get( 'txtBorder' )[ 'default' ] = '0';
//				    }
//				});
//			});
		},
		
		initView: function(controlID, isHighlightingSupported, tlObjectClass) {
			var content = document.getElementById(controlID);
			
			if (isHighlightingSupported) {
				services.wysiwyg.StructuredText.highlightContent(content);
			}
			services.wysiwyg.StructuredText.addTLObjectClickListener(content, tlObjectClass);
		},
		
		initScrollPosition: function(editor, frame, scrollContainer) {
			var documentHeight = frame.contentDocument.body.scrollHeight-frame.contentWindow.innerHeight;
			var xOffset = SaveScrollPosition.getLeftPosition(scrollContainer.id);
			var rawYOffset = SaveScrollPosition.getTopPosition(scrollContainer.id);
			
			if (typeof(xOffset) == "undefined") {
    			xOffset = 0;
    		}
	    	var yOffset;
	    	if (typeof(rawYOffset) == "undefined") {
		        yOffset = 0;
		    } else {
		    	yOffset = Math.round(rawYOffset * documentHeight);
		    }
	    	frame.contentWindow.scrollTo(xOffset, yOffset);
	    	
			services.wysiwyg.StructuredText.addScrollListener(editor, frame, scrollContainer, documentHeight);
		},
		
		addScrollListener: function(editor, frame, scrollContainer, documentHeight) {
			var editable = editor.editable();
			editable.attachListener( editable.getDocument(), 'scroll', function() {
				var documentWidth = frame.contentDocument.body.scrollWidth;
		    	var relativeTop = 1/documentHeight*frame.contentWindow.scrollY;
		    	var left = frame.contentWindow.scrollX;
			   	SaveScrollPosition.setTopPosition(scrollContainer.id, relativeTop);
			   	SaveScrollPosition.setLeftPosition(scrollContainer.id, left);
			});
		},
		
		findScrollContainer: function(el) {
			var cls = "fptBodyContent";
		    while (el = el.parentElement) {
		    	if(el.classList.contains(cls)) {
		    		return el;
		    	}
		    }
		    return null;
		},
		
		replaceContent: function(controlId, contentId, newContent) {
//			var editor = CKEDITOR.instances[contentId];
//			editor.setData(newContent);
			let editor = document.querySelector("#" + contentId + " > .toastui-editor-defaultUI");
			editor.setHTML(newContent, true);
		},
		
		cleanup: function(contentId) {
//			if(typeof CKEDITOR !== 'undefined' && CKEDITOR.instances[contentId]) {
//				CKEDITOR.instances[contentId].destroy(true);
//			}
			let editor = document.querySelector("#" + contentId + " > .toastui-editor-defaultUI");
			if (editor) {
				editor.destroy();
			}
		},
		
		addTLObjectClickListener: function(content, tlObjectClass) {
			BAL.addEventListener(content, 'click', function(event) {
				var target = event.target;
				
				if(target.classList.contains(tlObjectClass)) {
					event.preventDefault();
					var editor = services.wysiwyg.StructuredText.closestEditor(target, '.cStructuredText');
					services.wysiwyg.StructuredText.openTLObject(editor.id, target);
				}
			});
		},
		
		closestEditor: function(element, editorClass) {
			if(!Element.prototype.closest) {
				/**
				 * Polyfill for IE11. closest method don't exist, therefore one is created.
				 */
				if (!Element.prototype.matches) {
					Element.prototype.matches = Element.prototype.msMatchesSelector || Element.prototype.webkitMatchesSelector;
				}
				
				Element.prototype.closest = function(s) {
					var el = this;

				    do {
				      if (el.matches(s)) return el;
				      el = el.parentElement || el.parentNode;
				    } while (el !== null && el.nodeType === 1);
				    
				    return null;
				};
			}
			
			return element.closest(editorClass);
		},
		
		highlightContent: function(content) {
			var codes = content.querySelectorAll('code:not(.inlineCode)');
			var inlineCodes = content.querySelectorAll('code.inlineCode');
			
			Array.prototype.forEach.call(codes, function (code) {
				services.wysiwyg.StructuredText.highlightCodePart(code);
			});
			Array.prototype.forEach.call(inlineCodes, function (code) {
				services.wysiwyg.StructuredText.highlightInlineCode(code);
			});
		},
		
		highlightCodePart: function(code) {
			code.classList.add('hljs');
			
			var highlightedContent = hljs.highlightAuto(code.textContent);
			code.innerHTML = highlightedContent.value;
		},
		
		highlightInlineCode: function(code) {
			var originalCode = code.cloneNode(true);
			var originalChildren = services.wysiwyg.StructuredText.getLeafNodes(originalCode);
			code.classList.add('hljs');
			var highlightedContent = hljs.highlightAuto(code.textContent);
			code.innerHTML = highlightedContent.value;
			var children = services.wysiwyg.StructuredText.getLeafNodes(code)
			for ( child in children) {
				var codeChild = children[child];
				services.wysiwyg.StructuredText.wrapWithOriginalTags(originalChildren, codeChild);
			}
		},
		
		wrapWithOriginalTags: function(originalChildren, codeChild) {
			var orig = originalChildren[0];
			while (orig.length < codeChild.textContent.length) {
				var nextSibling = codeChild.splitText(orig.length);
				var change = nextSibling.previousSibling;
				services.wysiwyg.StructuredText.wrap(originalChildren, change);
				codeChild = nextSibling;
				orig = originalChildren[0];
			}
			services.wysiwyg.StructuredText.wrap(originalChildren, codeChild);
		},
		
		wrap: function(originalChildren, codeChild) {
			var firstChild = originalChildren[0];
			var parent = firstChild.parentNode;
			var wrapper = parent.cloneNode(true);
			wrapper.innerHTML = '';
			while ( wrapper.nodeName != "CODE") {
				codeChild.parentNode.insertBefore(wrapper, codeChild);
				wrapper.appendChild(codeChild);
				parent = parent.parentNode;
				wrapper = parent.cloneNode(true);
				wrapper.innerHTML = '';
			}
			firstChild.textContent = firstChild.textContent.substr(codeChild.textContent.length);
			if ( firstChild.textContent.length == 0) {
				originalChildren.shift();
			}
		},
		
		getLeafNodes: function(node, accum) {
		    var i;
		    accum = accum || [];
		    for (i = 0; i < node.childNodes.length; i++) {
			    if ( !node.childNodes[i].hasChildNodes() ) {
			        accum.push(node.childNodes[i])
			    }
		        services.wysiwyg.StructuredText.getLeafNodes(node.childNodes[i], accum);
		    }
		    return accum;
		},
	
		insertLink: function(controlId, contentId, link) {
			var content = document.getElementById(contentId);
			
			var editor = CKEDITOR.instances[contentId];
			var bookmarks = editor.element.getCustomData('bookmarks');
			var range = editor.createRange();
			range.moveToBookmark(bookmarks[0]);
			editor.insertHtml(link, 'text', range);
		},
	
		addTLObjectLink: function(command, ctrlId) {
			services.ajax.execute("dispatchControlCommand", {
				controlCommand : command,
				controlID : ctrlId
			});
		},
		
		openTLObject: function(controlId, object) {
			services.ajax.execute('dispatchControlCommand', {controlCommand: 'openTLObjectLink', controlID: controlId, object: object.href, section: object.dataset.section});
		},
		
		goto: function(locationId) {
			var element = document.getElementById(locationId);
			var topY = BAL.getElementY(element);
			var topX = BAL.getElementX(element);
			var offsetParent = element.offsetParent;
			BAL.setScrollLeftElement(offsetParent, topX);
			BAL.setScrollTopElement(offsetParent, topY);
		}
		
	}
	
};