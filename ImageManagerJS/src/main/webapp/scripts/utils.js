/**
 * Script to handle sending of xmlhttp form post requests to the server
 */

function postRequest(url, formElement, callBack, reset){
	let req = new XMLHttpRequest();
	req.onreadystatechange = function(){callBack(req)};
	req.open('POST', url);
	
	if(formElement){
		var formData = new FormData(formElement);	
		req.send(formData);
	}
	else
		req.send();

	if (formElement !== null && reset === true) {
		formElement.reset();
	}
}

function getRequest(url, callBack){
	let req = new XMLHttpRequest();
	req.onreadystatechange = function(){callBack(req)};
	req.open('GET', url);
	req.send();
}

function getPage(url, callback){
	let req = new XMLHttpRequest();
	req.onload = function(){callback(req)};
	req.open('GET', url);
	req.responseType = 'document';
	req.send();
}

function getImageHost(){
	return "http://localhost:8080/ImageManager";
}


/**
 * Handles unauthorized responses.
 * @param {Object} response - The server response.
 */
function handleUnauthorized(response) {
	if (response.redirect) {
		window.location.href = response.redirect;
	} else {
		console.warn("Server responded 401 but provided no redirect.");
	}
}


/**
 * Handles generic errors from server responses.
 * @param {Object} response - The server response.
 * @param {number} status - The HTTP status code.
 */
function handleError(response, status) {
	console.error(`Status: ${status}\nMessage: ${response.error}`);
	alert(`Status: ${status}\nMessage: ${response.error}`);
}










