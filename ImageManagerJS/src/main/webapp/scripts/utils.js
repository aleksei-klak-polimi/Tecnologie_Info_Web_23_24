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

function getImageHost(){
	const url = window.location.href;
	
	//Regular expression to extract everything between http:// (or https://) and the next "/"
	const domainRegex = /^(https?:\/\/[^\/]+)/;
	const match = url.match(domainRegex);
	
	if (match && match[1]) {
		const serverDomain = match[1]; // Extracted server domain
		// Append the image host to the server domain
		const imageHost = serverDomain + "/ImageManager";
		return imageHost;
	}
	else {
		console.error("Server domain not found in the URL.");
		return null;
	}
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










