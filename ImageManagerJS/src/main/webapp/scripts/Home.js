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
		
		//TODO call to LogOut servlet
	}
})();













