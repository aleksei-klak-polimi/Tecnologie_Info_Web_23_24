/**
 * Contains logic for displaying existing and creating new albums
 */



(function(){
	
	window.AlbumsPageManager = function(_manager){
		const manager = _manager;
		const container = document.getElementById("albumsContainer");
		const self = this;
		const form = container.getElementsByTagName("form")[0];
		const errorDiv = container.getElementsByClassName("errorText")[0];
		const errorParent = errorDiv.parentNode;
		const myAlbums = document.getElementById("myAlbums");
		const otherAlbums = document.getElementById("otherAlbums");
		
		errorParent.removeChild(errorDiv);
		
		
		this.registerEvents = function(){
			document.getElementById("submitCreateAlbum").addEventListener("click", (e) => handleCreateAlbum(e));
		
			
			function handleCreateAlbum(e) {
				e.preventDefault();
				const form = e.target.closest("form");

				if (validateForm(form)) {
					postRequest("CreateAlbum", form, handleCreateAlbumCallback, true);
				}
			}
			
			function validateForm(form) {
				const albumTitle = form.querySelector("[name='albumTitle']").value;

				if (!isValidTitle(albumTitle)) {
					displayError("provided album title is not valid.", true);
					return false;
				}
				return true;
			}
			
			function handleCreateAlbumCallback(x) {
				if (x.readyState === XMLHttpRequest.DONE) {
					if (x.status === 200) {
						form.reset();
						if(errorParent.contains(errorDiv))
							errorParent.removeChild(errorDiv);
						
						self.update();
					} else if (x.status === 401) {
						handleUnauthorized();
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
			
			
			function displayError(message, dontAlert) {
				errorDiv.textContent = message;
				errorParent.appendChild(errorDiv);
				if(!dontAlert)
					alert(message);
			}
		}
		
		this.update = function(){
			getRequest('GetAlbums', x => (refreshAlbumsCallback(x)));
			
			function refreshAlbumsCallback(x) {
				if (x.readyState === XMLHttpRequest.DONE) {
					try {
						if (x.status === 200) {
							const response = JSON.parse(x.responseText);
							if (response.data) {
								clearAlbums(myAlbums);
								clearAlbums(otherAlbums);
								
								const albums = JSON.parse(response.data);
								
								const myAlbumsList = [];
								const otherAlbumsList = [];
								
								const user = JSON.parse(sessionStorage.getItem("user"));
								const username = user.username;
								
								albums.forEach((album) => {
									if(album.owner === username)
										myAlbumsList.push(album);
									else
										otherAlbumsList.push(album);
								})
								
								populateAlbums(myAlbums, myAlbumsList, false);
								populateAlbums(otherAlbums, otherAlbumsList, true);
							}
							else {
								console.warn("Server did not send data in response.");
							}
						}
						else if (x.status == 401) {
							handleUnauthorized();
						}
						else {
							const response = JSON.parse(x.responseText);
							handleError(response, x.status);
						}
					}
					catch (e) {
						console.error("Error parsing JSON response:", e.message);
					}
				}
			}
			
			function clearAlbums(tbody) {
				while (tbody.firstChild) {
					tbody.removeChild(tbody.lastChild);
				}
			}
			
			function populateAlbums(tbody, albums, showAuthor) {
				clearAlbums(tbody);

				albums.forEach((album, index) => {
					const tr = document.createElement("tr");
					tr.className = index % 2 === 0 ? "odd" : "even";

					const albumName = document.createElement("td");

					const linkToAlbum = document.createElement("a");
					linkToAlbum.setAttribute("data-albumId", album.id);
					linkToAlbum.addEventListener("click", e => (handleViewAlbum(e)));
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
			
			function handleViewAlbum(e) {
				e.preventDefault();

				//Load into session storge the albumId for access by other scripts
				let albumId = e.target.getAttribute("data-albumId");

				manager.showAlbum(albumId);
			}
		}
		
		
		this.show = function(){
			container.style.display = "";
		}
		
		this.hide = function(){
			container.style.display = "none";
		}
		
		this.reset = function(){
			form.reset();

			if (errorParent.contains(errorDiv))
				errorParent.removeChild(errorDiv);
		}
	}
})();















