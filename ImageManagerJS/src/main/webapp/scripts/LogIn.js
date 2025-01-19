/**
 * LogIn logic management
 */

(function (){
	const authPageManager = new AuthPageManager();
	authPageManager.registerEvents();
	
	
	function AuthPageManager(){
		const logInView = new LogInView(this);
		const signUpView = new SignUpView(this);
		
		signUpView.hide();
		
		
		this.registerEvents = function(){
			logInView.registerEvents();
			signUpView.registerEvents();
		}
		
		
		this.showLogIn = function(){
			signUpView.hide();
			signUpView.reset();
			logInView.show();
		}
		
		this.showSignUp = function() {
			logInView.hide();
			logInView.reset();
			signUpView.show();
		}
	}
	
	
	function LogInView(_manager){
		const manager = _manager;
		const logInContainer = document.getElementById("logInContainer");
		const form = logInContainer.getElementsByTagName("form")[0];
		const errorDiv = logInContainer.getElementsByClassName("errorText")[0];
		const errorParent = errorDiv.parentNode;
		errorParent.removeChild(errorDiv);
		
		
		this.registerEvents = function(){
			document.getElementById("logInBtn").addEventListener("click", (e) => handleLogIn(e));
			document.getElementById("redirectToSignUp").addEventListener("click", (e) => redirectToSignUp(e));
		
			function redirectToSignUp(e) {
				e.preventDefault();
				manager.showSignUp();
			}
			
			function handleLogIn(e) {
				e.preventDefault();
				const form = e.target.closest("form");

				if (form.checkValidity()) {
					postRequest('LogIn', e.target.closest("form"), handleLogInCallback);
				}
				else {
					form.reportValidity();
				}
			}
			
			function handleLogInCallback(x) {
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
			
			function displayError(message) {
				errorDiv.textContent = message;
				errorParent.appendChild(errorDiv);
				alert(message);
			}
		}
		
		this.hide = function(){
			logInContainer.style.display = "none";
		}
		
		this.show = function(){
			logInContainer.style.display = "";
		}
		
		this.reset = function(){
			form.reset();
			
			if (errorParent.contains(errorDiv))
				errorParent.removeChild(errorDiv);
		}
	}
	
	
	function SignUpView(_manager) {
		const manager = _manager;
		const signUpContainer = document.getElementById("signUpContainer");
		const form = signUpContainer.getElementsByTagName("form")[0];
		const errorDiv = signUpContainer.getElementsByClassName("errorText")[0];
		const errorParent = errorDiv.parentNode;
		errorParent.removeChild(errorDiv);


		this.registerEvents = function() {
			document.getElementById("signUpBtn").addEventListener("click", (e) => handleSignUp(e));
			document.getElementById("redirectToLogIn").addEventListener("click", (e) => redirectToLogIn(e));

			function redirectToLogIn(e) {
				e.preventDefault();
				manager.showLogIn();
			}

			function handleSignUp(e) {
				e.preventDefault();
				const form = e.target.closest("form");

				if (form.checkValidity()) {
					if (matchingPasswords()) {
						postRequest('SignUp', e.target.closest("form"), handleSignUpCallback);
					}
					else {
						displayError("Passwords don't match.");
					}
				}
				else {
					form.reportValidity();
				}
			}

			function handleSignUpCallback(x) {
				if (x.readyState == XMLHttpRequest.DONE) {
					try {
						const response = JSON.parse(x.responseText);

						if (x.status == 200) {
							if (response.redirect) {
								window.location.href = response.redirect;
							}
						}
						else if ([400, 401, 402].includes(x.status)) {
							displayError(response.error);
						}
					}
					catch (e) {
						console.error("Error parsing JSON response: " + e.message);
					}
				}
			}
			
			function matchingPasswords() {
				var pwd = document.getElementById("password").textContent;
				var repeatPwd = document.getElementById("repeatPassword").textContent;

				return (pwd === repeatPwd)
			}

			function displayError(message) {
				errorDiv.textContent = message;
				errorParent.appendChild(errorDiv);
				alert(message);
			}
		}

		this.hide = function() {
			signUpContainer.style.display = "none";
		}

		this.show = function() {
			signUpContainer.style.display = "";
		}

		this.reset = function() {
			form.reset();

			if (errorParent.contains(errorDiv))
				errorParent.removeChild(errorDiv);
		}
	}
	
	
})();






