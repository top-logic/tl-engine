// saveScrollPosition.js
//
// Javscript to keep frame-scroll-positions persisten over page reload
//
// Used to keep the POS-Trees at the same position when changing tabbers.
//
// author MBA 05.12.2003
//
// Usage:
// 
// include this script in your jsp with
//
// 	 	 <basic:js name="saveScrollPosition"/>
//
// and overwrite 
//
//    getObjectID() {
//       return "<%= theObjectID %>";
//    }
// Make sure, theObjectID is a uniqe key for the view to save and most important:
// ensure, that theObjectID is a VALID JAVASCRIPT IDENTIFIER. So no ".-+*\:{}()[]' in the name. 
//
//
// add a call to 
//
//    popScrollPosition();
//
//    where you want the scroll position to be updated. 
//    (most of the time this will be the last step in onLoad())
//
// That's it!
//


	// infix for the position storage variables 
	// change, if you run into problems with duplicate names
	// -> x_Offset_Treeid = ...
	var OFFSET = "_Offset";

	// disables scrollEventCatching on loading/layouting
	var catchScrollEvent = false;
	
	// catches scroll events and uses own handler
	onscroll = pushScrollPosition;	

	// save the scroll offset into the top-frame or use another, fixed frame (Mainlayout?)
	function pushScrollPosition() {
		if (!catchScrollEvent) return;
		eval("top.x" + OFFSET + getObjectID() + "=" + getScrollLeft());
		eval("top.y" + OFFSET + getObjectID() + "=" + getScrollTop());		
		parent.status = 'scrolling ' + getObjectID()+ ' '+ eval("top.x" + OFFSET + getObjectID()) + ',' + eval("top.y" + OFFSET + getObjectID());
	}	

	// get the saved scroll offset (if any) from the top-frame
	function popScrollPosition() {
		var treeId  = getObjectID();
		var xOffset = "";
		var yOffset = "";
		eval("xOffset = top.x" + OFFSET + treeId);
		eval("yOffset = top.y" + OFFSET + treeId);
		catchScrollEvent = true;

		// Mozilla
		scrollTo(xOffset,yOffset);
		
		// IE
		document.body.scrollLeft = xOffset;
		document.body.scrollTop  = yOffset;
		//alert(getObjectID()+" xOffset: "+ xOffset +", yOffset: " + yOffset);
	}

	// extracts the x-/Left-scrollPositon 	
	function getScrollLeft() {
		var scrollLeft = 0;

		if (self.pageYOffset) { 
		    // all except Explorer
			scrollLeft = self.pageXOffset;
			
		} else if (document.documentElement && document.documentElement.scrollLeft) {
			// Explorer 6 Strict
			scrollLeft = document.documentElement.scrollLeft;
			
		} else if (document.body) { 
			// all other Explorers
			scrollLeft = document.body.scrollLeft;
		}

		return scrollLeft;
	}
	
	// extracts the y-/Top-scrollPositon 	
	function getScrollTop() {
		var scrollTop = 0;

		if (self.pageYOffset) { 
		    // all except Explorer
			scrollTop = self.pageYOffset;
			
		} else if (document.documentElement && document.documentElement.scrollTop) {
			// Explorer 6 Strict
			scrollTop = document.documentElement.scrollTop;
			
		} else if (document.body) { 
			// all other Explorers
			scrollTop = document.body.scrollTop;
		}

		return scrollTop;
	}
	
	// Overwrite this function and make sure, you get a unique
	// key for the view / object who´s scroll position you want 
	// to save. 
	// !! USE A VALID JAVASCRIPT IDENTIFIER AS RETURN VALUE !!
	// -> in RegExp that's just  [A-Za-z0-9_]+ 
	function getObjectID() {
		alert("saveScrollPosition.js: getObjectID() is not overwritten!");
		return "please_overwrite_getObjectID_";
	}
	
