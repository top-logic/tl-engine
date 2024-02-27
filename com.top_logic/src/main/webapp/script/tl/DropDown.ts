let services, PlaceDialog;
services.form = {
	DropDownControl: {
		// CSS classes
		containerCl: "ddwttContainer",
		buttonCl: "ddwttDropBtn",
		activeCl: "ddwttDropBtnActive",
		searchCl: "ddwttSearch",
		hideCl: "ddwttHide",
		boxCl: "ddwttDDBox",
		listCl: "ddwttDDList",
		itemCl: "ddwttItem",
		selItemCl: "ddwttSelectedItem",
		actItemCl: "ddwttActiveItem",
		itemLabelCl: "ddwttItemLabel",
		mutObserver: null,

		buttonDrop: function(button: HTMLButtonElement) {
			const ddBoxOriginal = button.nextElementSibling;
			let ddBox = this.getDDBox();
			
			const onGlobalChange = function() {
				if (ddBox.contains(document.activeElement)) {
					services.form.DropDownControl.buttonDrop(button);
				}
			};

			let prevActive = button.parentElement!.classList.contains(this.activeCl);
			button.parentElement!.classList.toggle(this.activeCl);

			if (prevActive) {
				this.closeDD(button, ddBox);
				if (this.mutObserver) {
					this.mutObserver.disconnect();
					this.mutObserver = null;
				}
			} else {
				const outerDocument = document.body.firstElementChild;
				ddBox = ddBoxOriginal!.cloneNode(true);
				outerDocument!.append(ddBox);
				const ddList = ddBox.querySelector("." + this.listCl);
				let activeItem = this.getActiveItem(button, ddList);

				this.positionDD(button, ddBox);
				if (activeItem) {
					this.setItemActive(activeItem, true, false);
					this.addScrollEvents(button, onGlobalChange);
				}
				
				let dialog = button.closest(".dlgWindow");
				if (dialog) {
					this.setMutationObserver(dialog, button);
				}
			}
		},
		
		getDDBox: function() {
			return document.body.firstElementChild!.querySelector(":scope > ." + this.boxCl);
		},
		
		getButton: function(ddBox: HTMLElement) {
			return document.body.firstElementChild!.querySelector("#" + ddBox.dataset.ctrlid + " ." + this.buttonCl);
		},

		closeDD: function(button: HTMLElement, ddBox: HTMLElement) {
			// reset chevron to default (right)
			button.classList.remove("down");
			button.classList.remove("up");

			// reset & hide search
			const search: HTMLInputElement = ddBox.querySelector("." + this.searchCl)!;
			search.value = "";
			search.classList.add(this.hideCl);
			
			PlaceDialog.closeCurrentTooltip(document.body.firstElementChild);

			this.cancelScrollEvents(button);
			
			let ddBoxAct = ddBox.contains(document.activeElement);

			// hide DropDown
			ddBox.remove();
			
			if (ddBoxAct) {
				button.focus();
			}
		},
		
		setMutationObserver: function(dialog, button: HTMLElement) {
			// Options for the observer (which mutations to observe)
			const config = {attributeFilter: ["style"], attributeOldValue: true};
			
			// Callback function to execute when mutations are observed
			const callback = (mutationList) => {
				mutationList.forEach((mutation) => {
					if (mutation.type === "attributes") {
						this.buttonDrop(button);
					}
				});
			};
			this.mutObserver = new MutationObserver(callback);
			this.mutObserver.observe(dialog, config);
		},

		cancelScrollEvents: function(button) {
			if (button.controller) {
				button.controller.abort();
				delete button.controller;
			}
		},

		getActiveItem: function(button, ddList) {
			// search for previous active item
			let activeItem = ddList.querySelector(":scope > ." + this.actItemCl);
			// if there was no previous active item
			if (!activeItem) {
				for (let item of ddList.children) {
					if (this.isDisplayedItem(item)) {
						if (!activeItem) {
							// get first item that is displayed
							activeItem = item;
						}
						// if there is a selected item in single mode
						if (item.firstElementChild.textContent == button.textContent) {
							// return this instead
							return activeItem = item;
						}
					}
				}
			}
			return activeItem;
		},

		addScrollEvents: function(button, onGlobalChange) {
			button.controller = new AbortController();
			const signal = button.controller.signal;
			window.addEventListener("resize", onGlobalChange, { once: true, signal });
			let scrollParent = button;
			while (scrollParent.parentElement) {
				scrollParent = this.getClosestParent(scrollParent, "overflow", ["auto", "scroll"]);
				if (scrollParent) {
					scrollParent.addEventListener("scroll", onGlobalChange, { once: true, signal });
				} else {
					break;
				}
			}
		},

		getClosestParent: function(element: HTMLElement, property: string, values) {
			let parent = element.parentElement,
				propVal = window.getComputedStyle(parent!).getPropertyValue(property);

			if (values.some((value) => propVal.includes(value))) {
				return parent;
			} else {
				if (parent!.parentElement) {
					return this.getClosestParent(parent, property, values);
				} else {
					return null;
				}
			}
		},
		
		positionDD: function(button: HTMLElement, ddBox) {
			ddBox.style.left = 0;
			ddBox.style.top = 0;

			let btnPos = button.getBoundingClientRect(),
				ddBoxPos = ddBox.getBoundingClientRect(),
				hWindow = window.innerHeight;

			let bottomSpace = hWindow - btnPos.bottom,
				ddMaxHeight = bottomSpace;

			if ((bottomSpace >= ddBoxPos.height) || (bottomSpace >= btnPos.top)) {
				this.openDown(button, ddBox);
			} else {
				ddMaxHeight = btnPos.top;
				this.openUp(button, ddBox);
			}
			this.setDimensions(btnPos, ddBox, ddMaxHeight);
		},

		openDown: function(button: HTMLElement, ddBox: HTMLElement) {
			let btnPos = button.getBoundingClientRect();
			button.classList.add("down");
			ddBox.style.removeProperty("bottom");
			ddBox.style.setProperty("top", btnPos.bottom + "px");
			ddBox.style.setProperty("flex-direction", "column");
		},

		openUp: function(button: HTMLElement, ddBox: HTMLElement) {
			let btnPos = button.getBoundingClientRect();
			button.classList.add("up");
			ddBox.style.removeProperty("top");
			ddBox.style.setProperty("bottom", (window.innerHeight - btnPos.top) + "px");
			ddBox.style.setProperty("flex-direction", "column-reverse");
		},

		setDimensions: function(btnPos, ddBox, ddMaxHeight: number) {
			let search = ddBox.querySelector(":scope > ." + this.searchCl),
				ddList = ddBox.querySelector(":scope > ." + this.listCl),
				incrWidth = window.getComputedStyle(ddBox).getPropertyValue("width");
			
			ddBox.style.removeProperty("right");
			ddBox.style.setProperty("left", btnPos.left + "px");
			ddBox.style.setProperty("min-width", btnPos.width + "px");
			ddBox.style.setProperty("max-height", ddMaxHeight + "px");
			
			let scrollbarW = ddList.offsetWidth - ddList.clientWidth;
			if (btnPos.width < (parseFloat(incrWidth) + scrollbarW)) {
				incrWidth = parseFloat(incrWidth) + scrollbarW + "px";
				ddList.style.setProperty("width", incrWidth);
				if (parseFloat(incrWidth) > (window.innerWidth - btnPos.left)) {
					ddBox.style.removeProperty("left");
					ddBox.style.setProperty("right", (window.innerWidth - btnPos.right) + "px");
				}
			}
			
			let searchW = ddList.getBoundingClientRect().width + "px";
			search.style.setProperty("width", searchW);
			search.focus();
		},

		setItemActive: function(item: HTMLElement, scroll: boolean, mouse: boolean) {
			let ddList = item.parentElement;
			let previousActive = ddList!.querySelector(":scope > ." + this.actItemCl);
			if (previousActive) {
				if (previousActive == item) {
					return;
				}
				this.setItemInactive(previousActive);
			}
			item.classList.add(this.actItemCl);

			if (scroll) {
				item.scrollIntoView({ behavior: "smooth", block: "nearest", inline: "nearest" });
			}
			
			if (mouse) {
				return;
			}
			const mouseoverEvent = new Event('mouseover', { 'bubbles': true });
			item.dispatchEvent(mouseoverEvent);
		},
		
		setItemInactive: function(item: HTMLElement) {
			item.classList.remove(this.actItemCl);
			const mouseleaveEvent = new Event('mouseleave', { 'bubbles': true });
			const mouseoutEvent = new Event('mouseout', { 'bubbles': true });
			item.dispatchEvent(mouseleaveEvent);
			item.dispatchEvent(mouseoutEvent);
		},

		lostFocus: function() {
			const ddBox = this.getDDBox();
			const button = this.getButton(ddBox);
			const ddList = ddBox.querySelector("." + this.listCl);
			let itemList = ddList.children;

			setTimeout(() => {
				if (!ddBox.contains(document.activeElement)) {
					for (let item of itemList) {
						if (item.classList.contains(this.actItemCl)) {
							this.setItemInactive(item);
						}
					}
					if (button.parentElement.classList.contains(this.activeCl)) {
						this.buttonDrop(button);
					}
				}
			}, 150);
		},

		keyPressed: function(event: KeyboardEvent, multi: boolean) {
			let ddBox = this.getDDBox();
			let button = event.target as HTMLElement;
			if (ddBox) {
				button = this.getButton(ddBox);
			} else {
				if (multi) {
					this.buttonDrop(button);
					ddBox = this.getDDBox();
					button = this.getButton(ddBox);
				} else {
					ddBox = button.nextElementSibling;
				}
			}
			let ddList = ddBox.querySelector("." + this.listCl);
			let sourceBtn = (event.target == button);

			let activeItem = this.getActiveItem(button, ddList);

			switch (event.key) {
				// UP ARROW was pressed
				case "ArrowUp":
					// get previous navigation item for this element
					let previous = activeItem.previousElementSibling;

					// previous is not an item or is a selected item
					while (!previous || !this.isDisplayedItem(previous)) {
						// a previous element is not defined
						if (!previous) {
							// wrap to last element
							previous = ddList.lastElementChild;
						} else {
							// go one step up the list
							previous = previous.previousElementSibling;
						}
					}
					// if event source is the button
					if (sourceBtn && !multi) {
						// set previous item as selected
						this.selectItem(previous);
						event.preventDefault();
						return;
					} else {
						// set previous item active
						this.setItemActive(previous, true, false);
						break;
					}

				// DOWN ARROW was pressed
				case "ArrowDown":
					// get next navigation item for this element
					let next = activeItem.nextElementSibling;

					// next is not an item or is a selected item
					while (!next || !this.isDisplayedItem(next)) {
						// a next element is not defined
						if (!next) {
							// wrap to first element
							next = ddList.querySelector(":scope > ." + this.itemCl + ":not(." + this.selItemCl + ")");
						} else {
							// go one step down the list
							next = next.nextElementSibling;
						}
					}
					// if event source is the button
					if (sourceBtn && !multi) {
						// set next item as selected
						this.selectItem(next);
						event.preventDefault();
						return;
					} else {
						// set next item active
						this.setItemActive(next, true, false);
						break;
					}

				// [HOME|POS1] key was pressed
				case "Home":
					// get first displayed item
					let first = ddList.querySelector(":scope > ." + this.itemCl + ":not(." + this.selItemCl + ")");
					// if event source is the button
					if (sourceBtn && !multi) {
						// set first item as selected
						this.selectItem(first);
						event.preventDefault();
						return;
					} else {
						// set first item active
						this.setItemActive(first, true, false);
						break;
					}

				// [PageUp|PgUp|Bild ^] was pressed
				case "PageUp":
				// [PageDown|PgDn|Bild v] was pressed
				case "PageDown":
					let tempList = ddList,
						tempActive = activeItem;
						
					if (sourceBtn && !multi) {
						this.buttonDrop(button);
						ddList = this.getDDBox().querySelector("." + this.listCl);
						activeItem = this.getActiveItem(button, ddList);
					}
					
					let pageH = ddList.getBoundingClientRect().height,
						itemH = activeItem.getBoundingClientRect().height;
						
					if (sourceBtn && !multi) {
						this.buttonDrop(button);
						ddList = tempList;
						activeItem = tempActive;
					}
					
					let itemCount = Math.trunc(pageH / itemH),
						scrollH = itemCount * itemH;

					let i = 0,
						pageItem = activeItem;
					while (i < itemCount) {
						if (event.key == "PageUp") {
							pageItem = pageItem.previousElementSibling;
						} else {
							pageItem = pageItem.nextElementSibling;
						}

						if (!pageItem) {
							break;
						}

						if (this.isDisplayedItem(pageItem)) {
							activeItem = pageItem;
							i++;
						}
					}

					if (event.key == "PageUp") {
						scrollH = - scrollH;
					}

					// if event source is the button
					if (sourceBtn && !multi) {
						// set page up/down item as selected
						this.selectItem(activeItem);
						event.preventDefault();
						return;
					} else {
						ddList.scrollBy({ top: scrollH, behavior: "smooth" });
						this.setItemActive(activeItem, false, false);
						return;
					}

				// [END|ENDE] was Pressed
				case "End":
					// get last item
					let last = ddList.lastElementChild;
					// while last is no displayed item
					while (!this.isDisplayedItem(last)) {
						// go one step up the list
						last = last.previousElementSibling;
					}
					// if event source is the button
					if (sourceBtn && !multi) {
						// set last item as selected
						this.selectItem(last);
						event.preventDefault();
						return;
					} else {
						// set last item active
						this.setItemActive(last, true, false);
						break;
					}

				// [ENTER] was pressed
				case "Enter":
					if (!sourceBtn) {
						// set current item as selected
						this.selectItem(activeItem);
						event.preventDefault();
						event.stopPropagation();
						return;
					}
					event.stopImmediatePropagation();
					break;

				// [ESC] was pressed
				case "Escape":
					if (sourceBtn && !multi) {
						return;
					}
					this.buttonDrop(button);
					event.stopImmediatePropagation();
					return;
					
				// [TAB] was pressed
				case "Tab":
					if (multi || !sourceBtn) {
						this.buttonDrop(button);
					}
					return;

				// [Control|Ctrl|Ctl|STRG] + [C] was pressed
				case "c":
					if (event.ctrlKey && !sourceBtn) {
						return;
					}
					break;

				// any other key was pressed
				default:
					// the key was a printable character
					if (event.key.length == 1) {
						break;
					}
					// the key was no printable character
					return;
			}

			if (sourceBtn && !multi) {
				this.buttonDrop(button);
			}

			let search = this.getDDBox().querySelector(":scope > ." + this.searchCl);
			if (event.target != search) {
				search.focus();
			}
			
		},

		isDisplayedItem: function(item: HTMLElement) {
			return (item.classList.contains(this.itemCl) && !(item.classList.contains(this.selItemCl))
				&& !(item.style.display == "none"));
		},

		search: function(search) {
			let ddList = search.parentElement.querySelector("." + this.listCl),
				itemList = ddList.children,
				inputStr = search.value.toLowerCase(),
				firstItem = false;

			search.classList.remove(this.hideCl);

			for (let item of itemList) {
				this.setItemInactive(item);
				item.style.display = "";
				if (this.isDisplayedItem(item)) {
					let label = item.querySelector("." + this.itemLabelCl).textContent.toLowerCase();
					if (label.includes(inputStr)) {
						if (!firstItem) {
							firstItem = item;
						}
					} else {
						item.style.display = "none";
					}
				}
			}
			if (firstItem) {
				this.setItemActive(firstItem, true, false);
			}
		},

		selectItem: function(item: HTMLElement) {
			const ddBox = item.parentElement!.parentElement;
			const button = this.getButton(ddBox);
			let ctrlID = ddBox!.dataset.ctrlid;
			
			if (button.parentElement.classList.contains(this.activeCl)) {
				this.buttonDrop(button);
			}
	
			services.ajax.execute("dispatchControlCommand", {
				controlCommand: "ddItemSelected",
				controlID: ctrlID,
				itemID: item.id
			});
		},

		changeSelectedState: function(item: HTMLElement) {
			item.classList.toggle(this.selItemCl);
		},

		/**
		 * @deprecated since Icons were added (realized via Java)
		 */
		setSelectedLabel: function(button, selection) {
			button.firstElementChild.textContent = selection;
		},

		removeTag: function(tag: HTMLElement, itemID: string) {
			let item = document.getElementById(itemID);
			if (!item) {
				item = tag;
				item!.id = itemID;
			}
			this.selectItem(item);
		}
	},
};
