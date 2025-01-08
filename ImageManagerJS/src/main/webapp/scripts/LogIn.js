/**
 * LogIn logic management
 */

(function (){
	//Clean-up place-holder values
	var errorDiv = document.getElementsByClassName("errorText")[0];
	var errorParent = errorDiv.parentNode;
	errorParent.removeChild(errorDiv);
	
	document.getElementById("logInBtn").addEventListener("click", (e) => logIn(e))
	document.getElementById("signUpBtn").addEventListener("click", (e) => redirectToSignUp(e));
	
	//Onclick function
	function redirectToSignUp(e) {
		e.preventDefault();
		window.location.href = "/ImageManagerJS/SignUp";
	}
	
	function logIn(e) {
		e.preventDefault();
		var form = e.target.closest("form");
		if (form.checkValidity()) {
			makeCall("POST", 'LogIn', e.target.closest("form"), callBackFunc);
		}
		else {
			form.reportValidity();
		}
	}
	
	//CallBack function
	function callBackFunc(x){
		if (x.readyState == XMLHttpRequest.DONE){
			var message = x.responseText;
			if(x.status == 400 || x.status == 401 || x.status == 402){
				errorDiv.textContent = message;
				errorParent.appendChild(errorDiv);
				alert(message);
			}
		}
	}
})();