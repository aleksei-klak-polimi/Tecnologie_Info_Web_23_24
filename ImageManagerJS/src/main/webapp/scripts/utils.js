/**
 * Script to handle sending of xmlhttp form post requests to the server
 */

function postRequest(url, formElement, callBack, reset){
	let req = new XMLHttpRequest();
	req.onreadystatechange = function(){callBack(req)};
	req.open('POST', url);
	
	if(formElement){
		if(!(formElement instanceof FormData)){
			var formData = new FormData(formElement);	
			req.send(formData);
		}
		else
			req.send(formElement);
	}
	else
		req.send();

	if (formElement !== null && !formElement instanceof FormData && reset === true) {
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



function handleUnauthorized() {
	sessionStorage.clear();
	window.location.href = "Auth";
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










