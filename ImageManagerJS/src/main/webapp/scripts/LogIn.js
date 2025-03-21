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
			//Ignore spaces
			form.addEventListener("keydown", (e)=>{
				if(e.key === ' ')
					e.preventDefault();
			});
			document.getElementById("logInBtn").addEventListener("click", (e) => handleLogIn(e));
			document.getElementById("redirectToSignUp").addEventListener("click", (e) => redirectToSignUp(e));
		
			function redirectToSignUp(e) {
				e.preventDefault();
				manager.showSignUp();
			}
			
			function handleLogIn(e) {
				e.preventDefault();
				const form = e.target.closest("form");

				if (validateForm(form)) {
					postRequest('LogIn', e.target.closest("form"), handleLogInCallback);
				}
			}
			
			function handleLogInCallback(x) {
				if (x.readyState === XMLHttpRequest.DONE) {
					try {
						if (x.status === 200) {
							const response = JSON.parse(x.responseText);
							if (response.data){
								const user = response.data
								sessionStorage.setItem("user", user);
								window.location.href = "Home";
							}
							else{
								console.warn("Server responded 200 but sent to user obj")
							}
						}
						else if(x.status === 401){
							handleUnauthorized();
						}
						else if ([400, 402].includes(x.status)) {
							const response = JSON.parse(x.responseText);
							displayError(response.error);
						}
					} catch (e) {
						console.error("Error parsing JSON response: " + e.message);
					}
				}
			}
			
			function validateForm(form){
				const username = form.querySelector("[name='username']").value;
				const password = form.querySelector("[name='password']").value;
				
				if(!isValidUsername(username)){
					displayError("provided username is not valid.", true);
					return false;
				}
				else if(!isValidPassword(password)){
					displayError("provided password is not valid.", true);
					return false;
				}
				return true;
			}
			
			function displayError(message, dontAlert) {
				errorDiv.textContent = message;
				errorParent.appendChild(errorDiv);
				if(!dontAlert)
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
			//Ignore spaces
			form.addEventListener("keydown", (e) => {
				if (e.key === ' ')
					e.preventDefault();
			});
			
			document.getElementById("signUpBtn").addEventListener("click", (e) => handleSignUp(e));
			document.getElementById("redirectToLogIn").addEventListener("click", (e) => redirectToLogIn(e));

			function redirectToLogIn(e) {
				e.preventDefault();
				manager.showLogIn();
			}

			function handleSignUp(e) {
				e.preventDefault();
				const form = e.target.closest("form");

				if (validateForm(form)) {
					postRequest('SignUp', e.target.closest("form"), handleSignUpCallback);
				}
			}

			function handleSignUpCallback(x) {
				if (x.readyState == XMLHttpRequest.DONE) {
					try {
						const response = JSON.parse(x.responseText);

						if (x.status == 200) {
							if (response.data) {
								const user = response.data
								sessionStorage.setItem("user", user);
								window.location.href = "Home";
							}
							else{
								console.warn("Server responded 200 but sent to user obj")
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
			
			function validateForm(form) {
				const username = form.querySelector("[name='username']").value;
				const email = form.querySelector("[name='email']").value;
				const password = form.querySelector("[name='password']").value;
				const repeatPassword = form.querySelector("[name='repeatPassword']").value;

				if (!isValidUsername(username)) {
					displayError("provided username is not valid.", true);
					return false;
				}
				else if (!isValidEmail(email)) {
					displayError("provided email is not valid.", true);
					return false;
				}
				else if (!isValidPassword(password)) {
					displayError("provided password is not valid.", true);
					return false;
				}
				else if(password !== repeatPassword){
					displayError("Passwords don't match.", true);
					return false;
				}
				return true;
			}

			function displayError(message, dontAlert) {
				errorDiv.textContent = message;
				errorParent.appendChild(errorDiv);
				if(!dontAlert)
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






