/**
 * LogIn logic management
 */

(function (){
	const errorDiv = document.getElementsByClassName("errorText")[0];
	const errorParent = errorDiv.parentNode;
	
	//Clean-up place-holder html
	errorParent.removeChild(errorDiv);
	
	//Event listener for form submission
	document.getElementById("logInBtn").addEventListener("click", (e) => handleLogIn(e))
	
	//Event listener for switch to sign-up
	document.getElementById("signUpBtn").addEventListener("click", (e) => redirectToSignUp(e));
	
	
	
	/**
	 * Redirects to the sign-up page.
	 * @param {Event} e - The event object.
	 */
	function redirectToSignUp(e) {
		e.preventDefault();
		window.location.href = "/ImageManagerJS/SignUp";
	}
	
	/**
	 * Handles the logIn form submission.
	 * @param {Event} e - The event object.
	 */
	function handleLogIn(e) {
		e.preventDefault();
		const form = e.target.closest("form");
		
		if (form.checkValidity()) {
			postForm('LogIn', e.target.closest("form"), handleLogInCallback);
		}
		else {
			form.reportValidity();
		}
	}
	
	
	/**
	 * Callback function for handling the server response to logIn.
	 * @param {XMLHttpRequest} x - The XMLHttpRequest object.
	 */
	function handleLogInCallback(x){
		if (x.readyState === XMLHttpRequest.DONE) {
			try {
				const response = JSON.parse(x.responseText);

				if (x.status === 200) {
					if (response.redirect) {
						window.location.href = response.redirect;
					}
				}
				else if ([400, 401, 402].includes(x.status)) {
					displayError(response.error);
				}
			} catch (e) {
				console.error("Error parsing JSON response: " + e.message);
			}
		}
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






