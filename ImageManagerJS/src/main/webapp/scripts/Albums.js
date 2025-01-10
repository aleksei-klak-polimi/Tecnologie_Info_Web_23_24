/**
 * Contains logic for displaying old and creating new albums
 */

(function(){
	const errorDiv = document.getElementsByClassName("errorText")[0];
	const errorParent = errorDiv.parentNode;
	const myAlbums = document.getElementById("myAlbums");
	const otherAlbums = document.getElementById("otherAlbums");
	
	// Event listener for album creation
	document.getElementById("submitCreateAlbum")
		.addEventListener("click", (e) => handleCreateAlbum(e));
	
	//Clean-up place-holder html
	errorParent.removeChild(errorDiv);
	clearAlbums(myAlbums);
	clearAlbums(otherAlbums);
	
	//Load albums from server
	refreshAlbums();
	
	
	/**
	 * Removes all children from a given tbody element.
	 * @param {HTMLElement} tbody - The table body to clear.
	 */
	function clearAlbums(tbody) {
		while (tbody.firstChild) {
			tbody.removeChild(tbody.lastChild);
		}
	}
	
	/**
	 * Fetches albums from the server and populates the tables.
	 */
	function refreshAlbums() {
		getRequest('GetUserAlbums', x => (refreshAlbumsCallback(myAlbums, x, false)));
		getRequest('GetOtherAlbums', x => (refreshAlbumsCallback(otherAlbums, x, true)));
	}
	
	
	/**
	 * Callback for album refresh API response.
	 * @param {HTMLElement} tbody - The table body for which the albums are being fetched.
	 * @param {XMLHttpRequest} x - The XMLHttpRequest object.
	 * @param {boolean} showAuthor - Whether to include the author column when rendering the table.
	 */
	function refreshAlbumsCallback(tbody, x, showAuthor){
		if (x.readyState === XMLHttpRequest.DONE){
			try{
				const response = JSON.parse(x.responseText);
				
				if (x.status === 200) {
					if (response.data) {
						const albums = JSON.parse(response.data);
						populateAlbums(tbody, albums, showAuthor);
					}
					else {
						console.warn("Server did not send data in response.");
					}
				}
				else if(x.status == 401){
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
	 * Populates a table body with provided albums.
	 * @param {HTMLElement} tbody - The table body to populate.
	 * @param {JSON} albums - The albums json object to display in the table body.
	 * @param {boolean} showAuthor - Whether to include the author column.
	 */
	function populateAlbums(tbody, albums, showAuthor) {
		clearAlbums(tbody);

		albums.forEach((album, index) => {
			const tr = document.createElement("tr");
			tr.className = index % 2 === 0 ? "odd" : "even";

			const albumName = document.createElement("td");
			const linkToAlbum = document.createElement("a");
			linkToAlbum.href = `/Album?albumid=${album.id}`;
			linkToAlbum.innerText = album.title;
			albumName.appendChild(linkToAlbum);
			tr.appendChild(albumName);

			if (showAuthor) {
				const albumAuthor = document.createElement("td");
				albumAuthor.innerText = album.owner;
				tr.appendChild(albumAuthor);
			}

			const albumDate = document.createElement("td");
			albumDate.innerText = album.creationDate;
			tr.appendChild(albumDate);

			tbody.appendChild(tr);
		});
	} 
	
	
	
	/**
	 * Handles album creation form submission.
	 * @param {Event} e - The event object.
	 */
	function handleCreateAlbum(e) {
		e.preventDefault();
		const form = e.target.closest("form");

		if (form.checkValidity()) {
			postRequest("CreateAlbum", form, handleCreateAlbumCallback, true);
		} else {
			form.reportValidity();
		}
	}
	
	
	/**
	 * Callback for album creation API response.
	 * @param {XMLHttpRequest} x - The XMLHttpRequest object.
	 */
	function handleCreateAlbumCallback(x) {
		if (x.readyState === XMLHttpRequest.DONE) {
			if (x.status === 200) {
				refreshAlbums();
			} else if (x.status === 401) {
				const response = JSON.parse(x.responseText);
				handleUnauthorized(response);
			} else {
				try {
					const response = JSON.parse(x.responseText);
					displayError(response.error);
				} catch (e) {
					console.error("Error parsing JSON response:", e.message);
				}
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















