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

				if (form.checkValidity()) {
					postRequest("CreateAlbum", form, handleCreateAlbumCallback, true);
				} else {
					form.reportValidity();
				}
			}
			
			
			function handleCreateAlbumCallback(x) {
				if (x.readyState === XMLHttpRequest.DONE) {
					if (x.status === 200) {
						self.update();
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
			
			
			function displayError(message) {
				errorDiv.textContent = message;
				errorParent.appendChild(errorDiv);
				alert(message);
			}
		}
		
		this.update = function(){
			getRequest('GetAlbums', x => (refreshAlbumsCallback(x)));
			
			function refreshAlbumsCallback(x) {
				if (x.readyState === XMLHttpRequest.DONE) {
					try {
						const response = JSON.parse(x.responseText);

						if (x.status === 200) {
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
							handleUnauthorized(response);
						}
						else {
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
					linkToAlbum.setAttribute("data-albumOwner", !showAuthor);
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
				let albumOwner = e.target.getAttribute("data-albumOwner");
				let albumTitle = e.target.textContent;

				manager.showAlbum(albumId, albumOwner, albumTitle);
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















