//
// Copyright (c) 2002 BOS GmbH. All Rights Reserved.
//
//
// formutils: A collection of JavaScript functions providing some tools to
//            handle HTML forms.
//
//


// Returns the number of selected elements in form aForm whose names match aPattern.
function countSelectedElements(aPattern, aForm) {
	var a = new Array();
	a = getElementsByPattern(aPattern, aForm);
	return countSelectedArrayElements(a);
}


// Returns the number of selected elements in array anArray.
function countSelectedArrayElements(anArray) {
	var j = 0;
	for (var i = 0; i < anArray.length; i++) {
		if (anArray[i].checked == true) j++;
	}
	return j;
}


// Sets the "disabled" flag of all checkboxes contained in the provided array
// to the specified value.
function setCheckboxDisabled(anArray, aValue) {
	for (var i = 0; i < anArray.length; i++) {
		anArray[i].disabled = aValue;
	}
}

// Returns an Array of all elements whose names match the
// specified RegExp pattern in the specified form.
function getElementsByPattern(aPattern, aForm) {

	var butArray = new Array();
	var len = aForm.elements.length;
	var j = 0;
	for (var i = 0; i < len; i++) {
		var theElement = aForm.elements[i];
		var elName = theElement.name;
		if ( elName.search(aPattern) > -1) {
			butArray[j++] = theElement;
		}
	}

	return butArray;
}


// Filters elements from array anArray using pattern aPattern.
// If parameter inverse is true, those elements that match aPattern
// will be added, otherwise those elements that do NOT match aPattern
// are added.
function filterArrayByPattern(anArray, aPattern, add) {
	//alert("START "+(add?"ADD":"REMOVE")+" with pattern " + aPattern);
	var result = new Array();
	var j = 0;
	for (var i = 0; i < anArray.length; i++) {
		var theElement = anArray[i];
		var theName = theElement.name;
		if ( (theName).search(aPattern) > -1 ) {
			// match
			if (add) {
				result[j++] = theElement;
			}
		} else {
			// no match
			if (!add) {
				result[j++] = theElement;
			}
		}
	}
	return result;
}

// Changes the state of all checkboxes with the name provided
// in aName in form aForm to the value aValue. If aName is null,
// all checkboxes in the form will be changed.
function toggleCheckBoxesInForm(aForm, aName, aValue){
	var len = aForm.elements.length;

	var i;
	for (i = 0; i < len; i++) {
		var theElement = aForm.elements[i];
		if (aName == null) {
				changeCheckboxState(theElement, aValue);
		} else if (theElement.name == aName) {
				changeCheckboxState(theElement, aValue);
		}
	}
}

// Changes the value of a given checkbox:
//
//     aValue == 0    =>   uncheck checkbox
//     aValue == 1    =>   check checkbox
//     aValue == -1   =>   invert checkbox state
//     
function changeCheckboxState(aCheckbox, aValue) {
	if (aValue == 0) {
		aCheckbox.checked = false;
	} else if (aValue == 1) {
		aCheckbox.checked = true;
	} else if (aValue == -1) {
		aCheckbox.checked = !aCheckbox.checked;
	}
}



// DEPRECATED METHODS FOLLOW BELOW

// DEPRECATED
function specifyValueAndSubmit (message, formname, inputname, additionalinputname, additionalvalue) {
	if (additionalinputname) {
		setValue (formname, additionalinputname, additionalvalue);
	}
	inputvalue = prompt (message, "");
	if ((inputvalue != null) && (inputvalue != "") && (inputvalue != "null")) {
		setValue (formname, inputname, inputvalue);
		document.forms[formname].submit ();
	}
}

// DEPRECATED
function checkActionAndSubmit (message, formname, inputname, inputvalue, additionalinputname, additionalvalue) {
	check = confirm (message);
	if (check) {
		if (additionalinputname) {
			setValue (formname, additionalinputname, additionalvalue);
		}
		setValue (formname, inputname, inputvalue);
		document.forms[formname].submit ();
	}
}

// DEPRECATED
function setValueAndSubmit (formname, inputname, inputvalue, additionalinputname, additionalvalue) {
	if (additionalinputname) {
		setValue (formname, additionalinputname, additionalvalue);				
	}
	setValue (formname, inputname, inputvalue);
	document.forms[formname].submit ();
}

// DEPRECATED
function setValue (formname, inputname, inputvalue) {
	theInputField = document.forms[formname].elements[inputname];
	if (!(theInputField.type == "select-one")) {
		theInputField.value = inputvalue;
	}
	else {
		for (i=0; i<theInputField.length; i++) {
			if (theInputField.options[i].value == inputvalue) {
				theInputField.options[i].selected = true;
			}
		}
	}
}
