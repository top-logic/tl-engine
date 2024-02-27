var services, PlaceDialog;
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
        
        buttonDrop: function (button) {
            var ddBoxOriginal = button.nextElementSibling;
            var ddBox = this.getDDBox();
            
            var onGlobalChange = function () {
                if (ddBox.contains(document.activeElement)) {
                    services.form.DropDownControl.buttonDrop(button);
                }
            }

            var prevActive = button.parentElement.classList.contains(this.activeCl);
            button.parentElement.classList.toggle(this.activeCl);
            
            if (prevActive) {
                this.closeDD(button, ddBox);
                if (this.mutObserver) {
                    this.mutObserver.disconnect();
                    this.mutObserver = null;
                }
            } else {
                var outerDocument = document.body.firstElementChild;
                ddBox = ddBoxOriginal.cloneNode(true);
                outerDocument.append(ddBox);
                var ddList = ddBox.querySelector("." + this.listCl);
                var activeItem = this.getActiveItem(button, ddList);
                
                this.positionDD(button, ddBox);
                if (activeItem) {
                    this.setItemActive(activeItem, true, false);
                    this.addScrollEvents(button, onGlobalChange);
                }
                
                var dialog = button.closest(".dlgWindow");
                if (dialog) {
                    this.setMutationObserver(dialog, button);
                }
            }
        },
        
        getDDBox: function () {
            return document.body.firstElementChild.querySelector(":scope > ." + this.boxCl);
        },
        
        getButton: function (ddBox) {
            return document.body.firstElementChild.querySelector("#" + ddBox.dataset.ctrlid + " ." + this.buttonCl);
        },
        
        closeDD: function (button, ddBox) {
            // reset chevron to default (right)
            button.classList.remove("down");
            button.classList.remove("up");
            
            // reset & hide search
            var search = ddBox.querySelector("." + this.searchCl);
            search.value = "";
            search.classList.add(this.hideCl);
            
            PlaceDialog.closeCurrentTooltip(document.body.firstElementChild);
            
            this.cancelScrollEvents(button);
            
            var ddBoxAct = ddBox.contains(document.activeElement);
            
            // hide DropDown
            ddBox.remove();
            
            if (ddBoxAct) {
                button.focus();
            }
        },
        
        setMutationObserver: function (dialog, button) {
            var _this = this;
            // Options for the observer (which mutations to observe)
            var config = { attributeFilter: ["style"], attributeOldValue: true };
            
            // Callback function to execute when mutations are observed
            var callback = function (mutationList) {
                mutationList.forEach(function (mutation) {
                    if (mutation.type === "attributes") {
                        _this.buttonDrop(button);
                    }
                });
            };
            this.mutObserver = new MutationObserver(callback);
            this.mutObserver.observe(dialog, config);
        },
        cancelScrollEvents: function (button) {
            if (button.controller) {
                button.controller.abort();
                delete button.controller;
            }
        },
        getActiveItem: function (button, ddList) {
            // search for previous active item
            var activeItem = ddList.querySelector(":scope > ." + this.actItemCl);
            // if there was no previous active item
            if (!activeItem) {
                for (var _i = 0, _a = ddList.children; _i < _a.length; _i++) {
                    var item = _a[_i];
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
        addScrollEvents: function (button, onGlobalChange) {
            button.controller = new AbortController();
            var signal = button.controller.signal;
            window.addEventListener("resize", onGlobalChange, { once: true, signal: signal });
            var scrollParent = button;
            while (scrollParent.parentElement) {
                scrollParent = this.getClosestParent(scrollParent, "overflow", ["auto", "scroll"]);
                if (scrollParent) {
                    scrollParent.addEventListener("scroll", onGlobalChange, { once: true, signal: signal });
                }
                else {
                    break;
                }
            }
        },
        getClosestParent: function (element, property, values) {
            var parent = element.parentElement, propVal = window.getComputedStyle(parent).getPropertyValue(property);
            if (values.some(function (value) { return propVal.includes(value); })) {
                return parent;
            }
            else {
                if (parent.parentElement) {
                    return this.getClosestParent(parent, property, values);
                }
                else {
                    return null;
                }
            }
        },
        positionDD: function (button, ddBox) {
            ddBox.style.left = 0;
            ddBox.style.top = 0;
            var btnPos = button.getBoundingClientRect(), ddBoxPos = ddBox.getBoundingClientRect(), hWindow = window.innerHeight;
            var bottomSpace = hWindow - btnPos.bottom, ddMaxHeight = bottomSpace;
            if ((bottomSpace >= ddBoxPos.height) || (bottomSpace >= btnPos.top)) {
                this.openDown(button, ddBox);
            }
            else {
                ddMaxHeight = btnPos.top;
                this.openUp(button, ddBox);
            }
            this.setDimensions(btnPos, ddBox, ddMaxHeight);
        },
        openDown: function (button, ddBox) {
            var btnPos = button.getBoundingClientRect();
            button.classList.add("down");
            ddBox.style.removeProperty("bottom");
            ddBox.style.setProperty("top", btnPos.bottom + "px");
            ddBox.style.setProperty("flex-direction", "column");
        },
        openUp: function (button, ddBox) {
            var btnPos = button.getBoundingClientRect();
            button.classList.add("up");
            ddBox.style.removeProperty("top");
            ddBox.style.setProperty("bottom", (window.innerHeight - btnPos.top) + "px");
            ddBox.style.setProperty("flex-direction", "column-reverse");
        },
        setDimensions: function (btnPos, ddBox, ddMaxHeight) {
            var search = ddBox.querySelector(":scope > ." + this.searchCl), ddList = ddBox.querySelector(":scope > ." + this.listCl), incrWidth = window.getComputedStyle(ddBox).getPropertyValue("width");
            ddBox.style.removeProperty("right");
            ddBox.style.setProperty("left", btnPos.left + "px");
            ddBox.style.setProperty("min-width", btnPos.width + "px");
            ddBox.style.setProperty("max-height", ddMaxHeight + "px");
            var scrollbarW = ddList.offsetWidth - ddList.clientWidth;
            if (btnPos.width < (parseFloat(incrWidth) + scrollbarW)) {
                incrWidth = parseFloat(incrWidth) + scrollbarW + "px";
                ddList.style.setProperty("width", incrWidth);
                if (parseFloat(incrWidth) > (window.innerWidth - btnPos.left)) {
                    ddBox.style.removeProperty("left");
                    ddBox.style.setProperty("right", (window.innerWidth - btnPos.right) + "px");
                }
            }
            var searchW = ddList.getBoundingClientRect().width + "px";
            search.style.setProperty("width", searchW);
            search.focus();
        },
        setItemActive: function (item, scroll, mouse) {
            var ddList = item.parentElement;
            var previousActive = ddList.querySelector(":scope > ." + this.actItemCl);
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
            var mouseoverEvent = new Event('mouseover', { 'bubbles': true });
            item.dispatchEvent(mouseoverEvent);
        },
        setItemInactive: function (item) {
            item.classList.remove(this.actItemCl);
            var mouseleaveEvent = new Event('mouseleave', { 'bubbles': true });
            var mouseoutEvent = new Event('mouseout', { 'bubbles': true });
            item.dispatchEvent(mouseleaveEvent);
            item.dispatchEvent(mouseoutEvent);
        },
        lostFocus: function () {
            var _this = this;
            var ddBox = this.getDDBox();
            var button = this.getButton(ddBox);
            var ddList = ddBox.querySelector("." + this.listCl);
            var itemList = ddList.children;
            setTimeout(function () {
                if (!ddBox.contains(document.activeElement)) {
                    for (var _i = 0, itemList_1 = itemList; _i < itemList_1.length; _i++) {
                        var item = itemList_1[_i];
                        if (item.classList.contains(_this.actItemCl)) {
                            _this.setItemInactive(item);
                        }
                    }
                    if (button.parentElement.classList.contains(_this.activeCl)) {
                        _this.buttonDrop(button);
                    }
                }
            }, 150);
        },
        keyPressed: function (event, multi) {
            var ddBox = this.getDDBox();
            var button = event.target;
            if (ddBox) {
                button = this.getButton(ddBox);
            }
            else {
                if (multi) {
                    this.buttonDrop(button);
                    ddBox = this.getDDBox();
                    button = this.getButton(ddBox);
                }
                else {
                    ddBox = button.nextElementSibling;
                }
            }
            var ddList = ddBox.querySelector("." + this.listCl);
            var sourceBtn = (event.target == button);
            var activeItem = this.getActiveItem(button, ddList);
            switch (event.key) {
                // UP ARROW was pressed
                case "ArrowUp":
                    // get previous navigation item for this element
                    var previous = activeItem.previousElementSibling;
                    // previous is not an item or is a selected item
                    while (!previous || !this.isDisplayedItem(previous)) {
                        // a previous element is not defined
                        if (!previous) {
                            // wrap to last element
                            previous = ddList.lastElementChild;
                        }
                        else {
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
                    }
                    else {
                        // set previous item active
                        this.setItemActive(previous, true, false);
                        break;
                    }
                // DOWN ARROW was pressed
                case "ArrowDown":
                    // get next navigation item for this element
                    var next = activeItem.nextElementSibling;
                    // next is not an item or is a selected item
                    while (!next || !this.isDisplayedItem(next)) {
                        // a next element is not defined
                        if (!next) {
                            // wrap to first element
                            next = ddList.querySelector(":scope > ." + this.itemCl + ":not(." + this.selItemCl + ")");
                        }
                        else {
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
                    }
                    else {
                        // set next item active
                        this.setItemActive(next, true, false);
                        break;
                    }
                // [HOME|POS1] key was pressed
                case "Home":
                    // get first displayed item
                    var first = ddList.querySelector(":scope > ." + this.itemCl + ":not(." + this.selItemCl + ")");
                    // if event source is the button
                    if (sourceBtn && !multi) {
                        // set first item as selected
                        this.selectItem(first);
                        event.preventDefault();
                        return;
                    }
                    else {
                        // set first item active
                        this.setItemActive(first, true, false);
                        break;
                    }
                // [PageUp|PgUp|Bild ^] was pressed
                case "PageUp":
                // [PageDown|PgDn|Bild v] was pressed
                case "PageDown":
                    var tempList = ddList, tempActive = activeItem;
                    if (sourceBtn && !multi) {
                        this.buttonDrop(button);
                        ddList = this.getDDBox().querySelector("." + this.listCl);
                        activeItem = this.getActiveItem(button, ddList);
                    }
                    var pageH = ddList.getBoundingClientRect().height, itemH = activeItem.getBoundingClientRect().height;
                    if (sourceBtn && !multi) {
                        this.buttonDrop(button);
                        ddList = tempList;
                        activeItem = tempActive;
                    }
                    var itemCount = Math.trunc(pageH / itemH), scrollH = itemCount * itemH;
                    var i = 0, pageItem = activeItem;
                    while (i < itemCount) {
                        if (event.key == "PageUp") {
                            pageItem = pageItem.previousElementSibling;
                        }
                        else {
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
                        scrollH = -scrollH;
                    }
                    // if event source is the button
                    if (sourceBtn && !multi) {
                        // set page up/down item as selected
                        this.selectItem(activeItem);
                        event.preventDefault();
                        return;
                    }
                    else {
                        ddList.scrollBy({ top: scrollH, behavior: "smooth" });
                        this.setItemActive(activeItem, false, false);
                        return;
                    }
                // [END|ENDE] was Pressed
                case "End":
                    // get last item
                    var last = ddList.lastElementChild;
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
                    }
                    else {
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
            var search = this.getDDBox().querySelector(":scope > ." + this.searchCl);
            if (event.target != search) {
                search.focus();
            }
        },
        isDisplayedItem: function (item) {
            return (item.classList.contains(this.itemCl) && !(item.classList.contains(this.selItemCl))
                && !(item.style.display == "none"));
        },
        search: function (search) {
            var ddList = search.parentElement.querySelector("." + this.listCl), itemList = ddList.children, inputStr = search.value.toLowerCase(), firstItem = false;
            search.classList.remove(this.hideCl);
            for (var _i = 0, itemList_2 = itemList; _i < itemList_2.length; _i++) {
                var item = itemList_2[_i];
                this.setItemInactive(item);
                item.style.display = "";
                if (this.isDisplayedItem(item)) {
                    var label = item.querySelector("." + this.itemLabelCl).textContent.toLowerCase();
                    if (label.includes(inputStr)) {
                        if (!firstItem) {
                            firstItem = item;
                        }
                    }
                    else {
                        item.style.display = "none";
                    }
                }
            }
            if (firstItem) {
                this.setItemActive(firstItem, true, false);
            }
        },
        selectItem: function (item) {
            var ddBox = item.parentElement.parentElement;
            var button = this.getButton(ddBox);
            var ctrlID = ddBox.dataset.ctrlid;
            if (button.parentElement.classList.contains(this.activeCl)) {
                this.buttonDrop(button);
            }
            services.ajax.execute("dispatchControlCommand", {
                controlCommand: "ddItemSelected",
                controlID: ctrlID,
                itemID: item.id
            });
        },
        changeSelectedState: function (item) {
            item.classList.toggle(this.selItemCl);
        },
        /**
         * @deprecated since Icons were added (realized via Java)
         */
        setSelectedLabel: function (button, selection) {
            button.firstElementChild.textContent = selection;
        },
        removeTag: function (tag, itemID) {
            var item = document.getElementById(itemID);
            if (!item) {
                item = tag;
                item.id = itemID;
            }
            this.selectItem(item);
        }
    },
};
