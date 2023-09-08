// Handle non-null checkbox JS

function changeHiddenValue(name,chkbox){
	var theElement = document.getElementById(name);
	if(chkbox.checked){
		theElement.value="true";	
	}else{
		theElement.value="false";
	}	
}