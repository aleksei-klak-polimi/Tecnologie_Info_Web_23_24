/**
 * Script to handle sending of xmlhttp form post requests to the server
 */

function postForm(url, formElement, callBack, reset){
	let req = new XMLHttpRequest();
	req.onreadystatechange = function(){callBack(req)};
	req.open('POST', url);
	
	var formData = new FormData(formElement);	
	req.send(formData);

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










