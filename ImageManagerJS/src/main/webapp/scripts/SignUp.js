 /**
 * SignUp logic management
 */
(function (){
	//Clean-up place-holder values
	var errorDiv = document.getElementsByClassName("errorText")[0];
	var errorParent = errorDiv.parentNode;
	errorParent.removeChild(errorDiv);
	
	document.getElementById("signUpBtn").addEventListener("click", (e) => signUp(e));
	document.getElementById("logInBtn").addEventListener("click", (e) => redirectToLogIn(e));
	
	//Onclick function
	function redirectToLogIn(e){
		e.preventDefault();
		window.location.href = "/ImageManagerJS/LogIn";
	}
	
	function signUp(e) {
		e.preventDefault();
		var form = e.target.closest("form");
		if (form.checkValidity() && matchingPasswords()) {
			makeCall("POST", 'SignUp', e.target.closest("form"), callBackFunc);
		}
		else {
			form.reportValidity();
		}
	}
	
	
	
	function matchingPasswords(){
		var pwd = document.getElementById("password").textContent;
		var repeatPwd = document.getElementById("repeatPassword").textContent;
		
		if(pwd !== repeatPwd){
			//Passwords don't match
			errorDiv.textContent = "Passwords don't match.";
			errorParent.appendChild(errorDiv);
			return false;
		}
		return true;
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



