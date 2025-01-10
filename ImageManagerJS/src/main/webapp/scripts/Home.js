/**
 * Contains logic for loading the default html and for logout
 */

(function (){
	const root = document.getElementsByTagName("body")[0];
	const overlay = document.getElementById('logoutOverlay');
	
	// Event listeners for logout
	overlay.addEventListener("click", (e) => {
		if(e.target === overlay)hideLogOut(e);
	});
	
	document.addEventListener('keydown', (e) => {
		if (e.key === 'Escape' && overlay.classList.contains('active')) {
			hideLogOut(e);
		}
	});
	
	document.getElementById("cancelLogOut").addEventListener("click", (e) => hideLogOut(e));
	document.getElementById("confirmLogOut").addEventListener("click", (e) => handleLogOut(e));
	document.getElementById("logOut").addEventListener("click", (e) => showLogOut(e));
	
	// Load the default HTML (Albums.html)
	getPage('static/pages/Albums.html', loadHtml);
	
	/**
	 * Callback to load HTML into the page.
	 * @param {XMLHttpRequest} x - The XMLHttpRequest object.
	 */
	function loadHtml(x) {
		if (x.readyState === XMLHttpRequest.DONE) {
			try {
				const html = x.response.getElementsByTagName("body")[0];
				const externalScripts = [];

				// Extract scripts from the loaded HTML
				const rawScripts = html.getElementsByTagName("script");
				for (const rawScript of rawScripts) {
					const newScript = document.createElement("script");
					newScript.src = rawScript.src;
					newScript.className = "external";
					externalScripts.push(newScript);
				}

				// Remove the scripts from the new HTML
				Array.from(rawScripts).forEach((script) => script.remove());

				// Append the new HTML content to the root
				Array.from(html.childNodes).forEach((node) => root.appendChild(node));

				// Append the extracted scripts to the document head
				externalScripts.forEach((script) => document.head.appendChild(script));
			} catch (error) {
				console.error("Error loading HTML:", error);
			}
		}
	}
	
	/**
	 * Displays the logout overlay.
	 * @param {Event} e - The event object.
	 */
	function showLogOut(e){
		e.preventDefault();
		overlay.classList.remove('hidden');
		overlay.classList.add('active');
	}
	
	/**
	 * Hides the logout overlay.
	 */
	function hideLogOut(e){
		e.preventDefault();
		overlay.classList.remove('active');
		overlay.classList.add('hidden');
	}
	
	/**
	 * Handles the logout action.
	 * @param {Event} e - The event object.
	 */
	function handleLogOut(e){
		e.preventDefault();
		overlay.classList.remove('active');
		overlay.classList.add('hidden');
		
		postRequest("LogOut", null, handleLogOutCallback);
	}
	
	
	/**
	 * Callback to logOut from site.
	 * @param {XMLHttpRequest} x - The XMLHttpRequest object.
	 */
	function handleLogOutCallback(x){
		if (x.readyState === XMLHttpRequest.DONE) {
			try{
				const response = JSON.parse(x.responseText);
				
				if (x.status === 200) {
					if(response.redirect)
						window.location.href = response.redirect;
					else{
						console.warn("Server responded 200 to LogOut but provided no redirect.");
					}
				}
				else if (x.status === 401) {
					handleUnauthorized(response);
				}
				else {
					handleError(response, x.status);
				}	
			}
			catch(e){
				console.error("Error parsing JSON response:", e.message);
			}
		}
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
})();













