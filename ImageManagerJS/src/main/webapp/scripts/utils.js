/**
 * Script to handle sending of xmlhttp form post requests to the server
 */

function makeCall(method, url, formElement, callBack){
	var req = new XMLHttpRequest();
	req.onreadystatechange = function(){callBack(req)};
	
	var formData = new FormData(formElement);
	req.open(method, url);
	req.send(formData);
}