/**
 * SignUp logic management
 */

(function (){
	const errorDiv = document.getElementsByClassName("errorText")[0];
	const errorParent = errorDiv.parentNode;
	
	//Clean-up place-holder html
	errorParent.removeChild(errorDiv);
	
	//Event listener for form submission
	document.getElementById("signUpBtn").addEventListener("click", (e) => handleSignUp(e));
	
	//Event listener for switch to log-in
	document.getElementById("logInBtn").addEventListener("click", (e) => redirectToLogIn(e));
	
	/**
	 * Redirects to the log-in page.
	 * @param {Event} e - The event object.
	 */
	function redirectToLogIn(e) {
		e.preventDefault();
		window.location.href = "/ImageManagerJS/LogIn";
	}
	
	/**
	 * Handles the signUp form submission.
	 * @param {Event} e - The event object.
	 */
	function handleSignUp(e) {
		e.preventDefault();
		const form = e.target.closest("form");
		
		if(form.checkValidity()){
			if(matchingPasswords()){
				postForm('SignUp', e.target.closest("form"), handleSignUpCallback);
			}
			else{
				displayError("Passwords don't match.");
			}
		}
		else {
			form.reportValidity();
		}
	}
	
	
	/**
	 * Callback function for handling the server response to signUp.
	 * @param {XMLHttpRequest} x - The XMLHttpRequest object.
	 */
	function handleSignUpCallback(x){
		if (x.readyState == XMLHttpRequest.DONE){
			try{
				const response = JSON.parse(x.responseText);
				
				if(x.status == 200){
					if (response.redirect) {
						window.location.href = response.redirect;
					}
				}
				else if ([400, 401, 402].includes(x.status)) {
					displayError(response.error);
				}
			}
			catch(e) {
				console.error("Error parsing JSON response: " + e.message);
			}
		}
	}
	
	/**
	 * Checks if the fields password and repeatPassword match.
	 * @returns true/false boolean values
	 */
	function matchingPasswords() {
		var pwd = document.getElementById("password").textContent;
		var repeatPwd = document.getElementById("repeatPassword").textContent;

		return (pwd === repeatPwd) 
	}
	
	/**
	 * Displays an error message in the UI.
	 * @param {string} message - The error message to display.
	 */
	function displayError(message) {
		errorDiv.textContent = message;
		errorParent.appendChild(errorDiv);
		alert(message);
	}
})();






