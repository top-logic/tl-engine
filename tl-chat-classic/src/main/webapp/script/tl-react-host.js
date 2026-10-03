/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */

/*
 * Boots the React view layer inside a classic LayoutServlet page, for ReactViewComponent.
 *
 * Temporary workaround for the classic-UI bridge (E1, E3 in tl-chat-classic/CLAUDE.md): the engine
 * offers no way to load the React client resources into a classic page, and its React stylesheets
 * and the classic theme have no style boundary between them. Remove this file when the engine
 * loads React resources into classic pages itself and scopes both stylesheets, or when the classic
 * UI is gone.
 *
 * What it does, once per page:
 *
 * 1. Installs the import map and imports the module scripts the server rendered into the page's
 *    head markup (ClientResources), in their order, then mounts the React controls present.
 * 2. Loads every stylesheet of that markup as a constructable sheet wrapped in
 *    "@layer tl-react { @scope (host, portal) { ... } }", so that no React rule reaches a classic
 *    element. Inside the scope ":root" means the scope root and "html"/"body" mean the host, which
 *    is where the React page's document-level rules belong when the document is not theirs. Both are
 *    written with ":scope", and a rule keyed on a theme attribute ("[data-theme=dark]") gets a
 *    ":scope" variant: the host carries those attributes, and a selector without ":scope" never
 *    matches the scope root.
 *    @font-face, @keyframes and the like cannot stand in @scope and are kept beside it.
 * 3. Adds one unlayered reset that reverts, inside the scope, every declaration of the unlayered
 *    (classic) stylesheets back to the React layer - so "button:focus" and similar rules of the
 *    classic theme no longer recolour React controls.
 * 4. Marks the elements React portals into <body> (tooltips, popups) as scope roots, since they
 *    are rendered outside the host.
 */
(function() {
	if (window.TLReactHost) {
		return;
	}

	var HOST = 'tlReactHost';
	var PORTAL = 'tlReactPortal';
	var SCOPE = '@scope (.' + HOST + ', .' + PORTAL + ')';

	/** Copied from the host to a portal root, so that the scoped theme tokens apply there too. */
	var THEME_ATTRIBUTES = ['data-theme', 'data-tl-mode', 'data-tl-density'];

	/**
	 * Cuts the inheritance from the classic page at the scope roots. First in the layer and of zero
	 * specificity, so every React rule overrides it.
	 */
	var BASE_CSS =
		':where(.' + HOST + ', .' + PORTAL + ') { all: initial; display: block; font-size: medium; }';

	/**
	 * Gives every element in the scope the value of the React layer for every property, whatever an
	 * unlayered (classic) rule says. The id selector that never matches raises the specificity above
	 * any classic rule built from classes and elements.
	 */
	var RESET_CSS = SCOPE + ' {'
		+ ' :scope:not(#\\#), :not(#\\#), :not(#\\#)::before, :not(#\\#)::after,'
		+ ' :not(#\\#)::placeholder, :not(#\\#)::marker { all: revert-layer; }'
		+ ' }';

	/** Rules that cannot be nested in @scope; they are kept in the layer beside it. */
	function isHoisted(rule) {
		return rule instanceof CSSFontFaceRule
			|| rule instanceof CSSKeyframesRule
			|| (window.CSSPropertyRule && rule instanceof CSSPropertyRule)
			|| (window.CSSCounterStyleRule && rule instanceof CSSCounterStyleRule)
			|| (window.CSSFontFeatureValuesRule && rule instanceof CSSFontFeatureValuesRule)
			|| (window.CSSFontPaletteValuesRule && rule instanceof CSSFontPaletteValuesRule);
	}

	/**
	 * A selector that starts with one of the theme attributes. A React page sets them on <html>; in a
	 * classic page they stand on the host, the scoping root - and a selector inside @scope that names
	 * no :scope matches descendants of the root only, never the root itself. Without a :scope variant
	 * the dark theme's tokens ("[data-theme=dark] {...}") would never reach the host (#649).
	 */
	var THEME_SELECTOR = /^\[data-(theme|tl-mode|tl-density)\b/;

	/** The items of a selector list, split at the commas that are not inside a () or a []. */
	function selectorItems(selector) {
		var items = [];
		var depth = 0;
		var start = 0;
		for (var i = 0; i < selector.length; i++) {
			var c = selector.charAt(i);
			if (c === '(' || c === '[') {
				depth++;
			} else if (c === ')' || c === ']') {
				depth--;
			} else if (c === ',' && depth === 0) {
				items.push(selector.substring(start, i).trim());
				start = i + 1;
			}
		}
		items.push(selector.substring(start).trim());
		return items;
	}

	/**
	 * The selector a document-level React rule has in the scope.
	 *
	 * "html" and "body" become the host as scope root, ":scope.tlReactHost": a selector naming the
	 * host without ":scope" would again match hosts below the root only, and the page background, text
	 * colour and font of a React page's body would never reach the host (#649). Portal roots keep
	 * none of them - a portal is a popup, not a page.
	 */
	function scopedSelector(selector) {
		var scoped = selector
			.replace(/:root\b/g, ':scope')
			.replace(/(^|[\s,>+~(])(html|body)(?=$|[\s,.:#\[>+~)])/g, '$1:scope.' + HOST);
		var items = selectorItems(scoped);
		if (!items.some(function(item) { return THEME_SELECTOR.test(item); })) {
			return scoped;
		}
		return items.map(function(item) {
			return THEME_SELECTOR.test(item) ? ':scope' + item + ', ' + item : item;
		}).join(', ');
	}

	function rewriteSelectors(rules) {
		for (var i = 0; i < rules.length; i++) {
			var rule = rules[i];
			if (rule instanceof CSSStyleRule) {
				var scoped = scopedSelector(rule.selectorText);
				if (scoped !== rule.selectorText) {
					rule.selectorText = scoped;
				}
			}
			if (rule.cssRules) {
				rewriteSelectors(rule.cssRules);
			}
		}
	}

	/** One stylesheet of the React layer, scoped to the hosts and portals. */
	function scopedSheet(text, baseURL) {
		var parsed = new CSSStyleSheet({ baseURL: baseURL });
		parsed.replaceSync(text);
		var hoisted = [];
		var scoped = [];
		for (var i = 0; i < parsed.cssRules.length; i++) {
			var rule = parsed.cssRules[i];
			if (isHoisted(rule)) {
				hoisted.push(rule.cssText);
			} else {
				if (rule.cssRules || rule instanceof CSSStyleRule) {
					rewriteSelectors([rule]);
				}
				scoped.push(rule.cssText);
			}
		}
		var sheet = new CSSStyleSheet({ baseURL: baseURL });
		sheet.replaceSync('@layer tl-react { ' + hoisted.join('\n') + '\n' + SCOPE + ' {\n'
			+ scoped.join('\n') + '\n} }');
		return sheet;
	}

	function plainSheet(text) {
		var sheet = new CSSStyleSheet();
		sheet.replaceSync(text);
		return sheet;
	}

	/**
	 * The theme attributes of the first host, or null if there is none yet.
	 */
	function themeOf() {
		return document.querySelector('.' + HOST);
	}

	function markPortal(element) {
		element.classList.add(PORTAL);
		var host = themeOf();
		if (host) {
			THEME_ATTRIBUTES.forEach(function(name) {
				var value = host.getAttribute(name);
				if (value != null) {
					element.setAttribute(name, value);
				}
			});
		}
	}

	function isReactElement(element) {
		for (var key in element) {
			if (key.indexOf('__react') === 0) {
				return true;
			}
		}
		return element.id === 'tl-tooltip-host';
	}

	/**
	 * Marks the elements React adds directly to <body> - portals and the tooltip host - as scope
	 * roots. Runs before the first paint after the insertion.
	 */
	function observePortals() {
		new MutationObserver(function(mutations) {
			mutations.forEach(function(mutation) {
				mutation.addedNodes.forEach(function(node) {
					if (node.nodeType === 1 && !node.classList.contains(PORTAL) && isReactElement(node)) {
						markPortal(node);
					}
				});
			});
		}).observe(document.body, { childList: true });
	}

	/**
	 * Loads the stylesheets of the head markup in their order and adopts them as one block.
	 */
	function loadStyles(nodes) {
		var loads = nodes.map(function(node) {
			if (node.nodeName === 'STYLE') {
				return Promise.resolve(scopedSheet(node.textContent, document.baseURI));
			}
			var href = new URL(node.getAttribute('href'), document.baseURI).href;
			return fetch(href, { credentials: 'same-origin' })
				.then(function(response) {
					if (!response.ok) {
						throw new Error('Failed to load ' + href + ': ' + response.status);
					}
					return response.text();
				})
				.then(function(text) {
					return scopedSheet(text, href);
				});
		});
		return Promise.all(loads).then(function(sheets) {
			document.adoptedStyleSheets = document.adoptedStyleSheets
				.concat([plainSheet('@layer tl-react { ' + BASE_CSS + ' }')])
				.concat(sheets)
				.concat([plainSheet(RESET_CSS)]);
		});
	}

	/**
	 * Installs the import map and imports the module scripts as one module graph, in their order.
	 *
	 * <p>
	 * One graph, because the bridge starts watching the page for React controls when it is
	 * evaluated, and the controls bundle registers the components only when it is evaluated: in one
	 * graph both happen in the same task, so the bridge never sees a control it has no component for.
	 * </p>
	 */
	function loadScripts(nodes) {
		var modules = [];
		nodes.forEach(function(node) {
			if (node.getAttribute('type') === 'importmap') {
				var map = document.createElement('script');
				map.type = 'importmap';
				map.textContent = node.textContent;
				document.head.appendChild(map);
			} else if (node.getAttribute('type') === 'module' && node.getAttribute('src')) {
				modules.push(new URL(node.getAttribute('src'), document.baseURI).href);
			}
		});
		var graph = modules.map(function(src) {
			return 'import ' + JSON.stringify(src) + ';';
		}).join('\n');
		var url = URL.createObjectURL(new Blob([graph], { type: 'text/javascript' }));
		return import(url).finally(function() {
			URL.revokeObjectURL(url);
		});
	}

	var _state = null;

	window.TLReactHost = {
		/**
		 * Makes the React controls written into the page come alive.
		 *
		 * @param headMarkup The markup ClientResources and UIThemeService write into the <head> of a
		 *        React page. Read on the first call only.
		 */
		boot: function(headMarkup) {
			if (_state === 'booted') {
				window.TLReact.discoverAndMount();
				return;
			}
			if (_state === 'loading') {
				// The running boot mounts everything that is on the page when it completes.
				return;
			}
			_state = 'loading';
			observePortals();

			var template = document.createElement('template');
			template.innerHTML = headMarkup;
			var nodes = Array.prototype.slice.call(template.content.children);
			var styles = nodes.filter(function(node) {
				return node.nodeName === 'STYLE'
					|| (node.nodeName === 'LINK' && node.getAttribute('rel') === 'stylesheet');
			});
			var scripts = nodes.filter(function(node) {
				return node.nodeName === 'SCRIPT';
			});

			Promise.all([loadStyles(styles), loadScripts(scripts)])
				.then(function() {
					_state = 'booted';
					window.TLReact.discoverAndMount();
				})
				.catch(function(error) {
					_state = null;
					console.error('[TLReactHost] Failed to boot the React view layer.', error);
				});
		}
	};
})();
