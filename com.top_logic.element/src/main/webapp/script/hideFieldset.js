/** Stores the DOM node which is hidden. */
var spanRepository;

/** 
 * Toggle the visibility of the given DOM object.
 *
 * @param    aCheckbox    The checkbox to get the status from.
 * @param    aDomID       An id of a DOM object which is to be moved to the repository.
 * @param    aParentID    An id of a DOM object which servers as the parent of the object to be toggled.
 */
function setVisibility(aCheckbox, aDomID, aParentID) {
    var isChecked = aCheckbox.checked;

    if (isChecked == true) {
        setChildNode(aParentID,spanRepository);

        document.getElementById(aDomID).style.visibility= "visible";
    }
    else {
        var theSpan = document.getElementById(aDomID);

        theSpan.style.visibility= "hidden";
        removeDOMNode(aDomID, spanRepository);
    }
}

/**
 * Store a DOM node in the given variable.
 *
 * @param    aDomID         The identifier of the DOM node to remove from the model.
 * @param    aRepository    A variable to store the removed DOM node.
 */
function removeDOMNode(aDomID, aRepository) {
    var theNode   = document.getElementById(aDomID);
    var theParent = theNode.parentNode;

    spanRepository = theNode;
    theParent.removeChild(theNode);
}

/**
 * Set the node from the given repository as child of the given node.
 *
 * @param    aParentID      The id of the node to set the child into.
 * @param    aRepository    The variable to get the DOM node from.
 */
function setChildNode(aParentID, aRepository) {
    var theNode = document.getElementById(aParentID);

    if (aRepository) {
        theNode.appendChild(aRepository);
    }
}
