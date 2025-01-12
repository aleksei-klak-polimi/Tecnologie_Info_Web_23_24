/**
 * Contains logic for displaying picture for a given album and adding new ones.
 */

(function(){
	const imageDisplaySize = 5;
	
	let albumPageManager = new albumPage();
	albumPageManager.registerEvents();
	albumPageManager.update();
	albumPageManager.show();
	
	//albumPage class (page Manager)
	function albumPage(){
		//ATTRIBUTES
		const albumContainer = document.getElementById("albumOuterContainer");
		let albumImages = [];
		let otherImages = [];
		let comments = new Map();
		
		//COMPONENTS
		const imageDisplayComponent = new imageDisplayContainer(albumImages, this);
		const imageUploaderComponent = new imageUploader();
		
		
		//METHODS
		this.registerEvents = function(){
			document.getElementById("Home").addEventListener("click", (e) => handleRedirectHome(e));
			
			imageDisplayComponent.registerEvents(this);
			imageUploaderComponent.registerEvents(this);
			
			function handleRedirectHome(e) {
				e.preventDefault();

				//Removing event listener is not directly possible
				//Replace the target with a clone to discard the listeners.
				let newHomeBtn = e.target.cloneNode(true);
				e.target.parentNode.replaceChild(newHomeBtn, e.target);

				//Clear session storage
				sessionStorage.removeItem("albumPage");
				sessionStorage.removeItem("albumId");
				sessionStorage.removeItem("albumOwner");

				replaceHtml('static/pages/Albums.html');
			}
		}
		
		
		this.update = function() {
			let albumId = sessionStorage.getItem("albumId");
			let request = "GetImagesByAlbum?albumId=" + albumId;
			getRequest(request, x => (refreshImagesCallback(x)));

			function refreshImagesCallback(x) {
				if (x.readyState === XMLHttpRequest.DONE) {
					try {
						const response = JSON.parse(x.responseText);

						if (x.status === 200) {
							const recievedObjects = JSON.parse(response.data);

							albumImages = JSON.parse(recievedObjects[0]);
							otherImages = JSON.parse(recievedObjects[1]);
							
							let commentList = JSON.parse(recievedObjects[2]);

							commentList.forEach((comment) => {
								if (!comment.pictureId) {
									console.warn("Comment missing 'pictureId' field:", comment);
									return;
								}

								// Check if the id already exists in the map
								if (!comments.has(comment.pictureId)) {
									comments.set(comment.pictureId, []);
								}

								// Add the object to the list corresponding to its id
								comments.get(comment.pictureId).push(comment);
							});
							
							//Update components
							imageDisplayComponent.update(albumImages);
							imageUploaderComponent.update(otherImages);
							
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
		}
		
		
		this.show = function(){
			albumContainer.style.visibility = "visible";
			
			imageDisplayComponent.show();
			
			if(sessionStorage.getItem("albumOwner") && sessionStorage.getItem("albumOwner") === "true")
				imageUploaderComponent.show();
			else
			imageUploaderComponent.hide();
		}
		
		this.hide = function(){
			albumContainer.style.visibility = "hidden";
			imageDisplayComponent.hide();
			imageUploaderComponent.hide();
		}
		
		this.reset = function(){
			imageUploaderComponent.reset();
		}
	}
	
	
	//TODO change name
	//ImageDisplay class
	function imageDisplayContainer(images, parent){
		//ATTRIBUTES
		const parentReference = parent;
		let imagesLocal = images;
		let imageDisplay = document.getElementById("imageDisplayContainer");
		let isAlbumOwner = sessionStorage.getItem("albumOwner") === "true";
		const imagesRow = document.getElementById("imagesRow");
		const thumbnailSlotTemplate = document.getElementsByClassName("thumbnailSlot")[0];
		const emptySlotTemplate = document.getElementsByClassName("emptySlot")[0];
		const deleteLastInstanceComponent = new DeleteLastInstanceOverlay();
		
		//METHODS
		this.registerEvents = function(){
			deleteLastInstanceComponent.registerEvents(parentReference);
			
			const previousImgs = document.getElementById("previousImgs");
			const nextImgs = document.getElementById("nextImgs");
			
			//Listener logic for next and prev buttons
			previousImgs.addEventListener("click", (e) => {
				changeAlbumPage(e, -1);
				this.update(imagesLocal);
			});
			nextImgs.addEventListener("click", (e) => {
				changeAlbumPage(e, 1);
				this.update(imagesLocal);
			});
			
			function changeAlbumPage(e, delta) {
				e.preventDefault();
				
				if(!sessionStorage.getItem("albumPage"))
					sessionStorage.setItem("albumPage", 1);

				let albumPage = Number(sessionStorage.getItem("albumPage"));

				albumPage += Number(delta);

				if (albumPage < 1) {
					albumPage = 1;
				}
				else if (albumPage > Math.ceil(imagesLocal.lenght / imageDisplaySize)) {
					albumPage = Math.ceil(imagesLocal.lenght / imageDisplaySize);
				}

				sessionStorage.setItem("albumPage", albumPage);
			}
		}
		
		this.show = function(){
			//If user is not owner hide all "remove Image" buttons
			const ownerContent = Array.from(document.getElementsByClassName("forOwner"));
			for(let i = 0; i < ownerContent.length; i++){
				ownerContent[i].style.visibility = isAlbumOwner ? "visible" : "hidden";
			}
			
			imageDisplay.style.visibility = "visible";
		}
		
		this.hide = function(){
			imageDisplay.style.visibility = "hidden";
		}
		
		
		this.update = function(images){
			imagesLocal = images;
			//Check if user owns album
			if (!sessionStorage.getItem("albumOwner")) {
				console.error("Missing parameter albumOwner in sessionStorage");
			}
			else
				//Convert to boolean
				isAlbumOwner = sessionStorage.getItem("albumOwner") === "true";
			
			//Update Album Title
			const albumTitle = sessionStorage.getItem("albumTitle") ? sessionStorage.getItem("albumTitle") : "Missing album title";
			document.getElementById("albumTitle").textContent = albumTitle;
			
			//Clear old images
			while (imagesRow.firstChild) {
				imagesRow.removeChild(imagesRow.lastChild);
			}
			
			//Update with new images and albumPage
			if (!sessionStorage.getItem("albumPage")) {
				sessionStorage.setItem("albumPage", "1");
			}

			let albumPage = sessionStorage.getItem("albumPage");

			let imagesToSkip = (albumPage - 1) * imageDisplaySize;
			let imagesToDraw = images.slice(imagesToSkip, imagesToSkip + imageDisplaySize);
			
			imagesToDraw.forEach((image) => {
				const imageHtml = thumbnailSlotTemplate.cloneNode(true);
				imageHtml.getElementsByTagName("img")[0].setAttribute("src", getImageHost() + image.thumbnailPath)
				imageHtml.getElementsByTagName("img")[0].setAttribute("data-pictureId", image.id);
				imageHtml.getElementsByClassName("imageTitle")[0].textContent = image.title;
				imageHtml.getElementsByClassName("removeImageButton")[0].addEventListener("click", (e) => {
					e.preventDefault();
					removeImageFromAlbum(e);
					
					function removeImageFromAlbum(e) {
						let pictureId = e.target.parentNode.parentNode.parentNode.getElementsByTagName("img")[0].getAttribute("data-pictureId");
						let src = e.target.parentNode.parentNode.parentNode.getElementsByTagName("img")[0].getAttribute("src");
						
						let albumId = sessionStorage.getItem("albumId");
						let requestPath = "RemoveFromAlbum?albumId=" + albumId + "&pictureId=" + pictureId;

						postRequest(requestPath, null, (x) => removeImageFromAlbumCallback(x, pictureId, src));
					}
					
					function removeImageFromAlbumCallback(x, pictureId, src) {
						if (x.readyState === XMLHttpRequest.DONE) {
							try {
								if (x.status === 200) {
									parentReference.update();
								}
								else if ([400, 402, 403, 500].includes(x.status)) {
									const response = JSON.parse(x.responseText);
									displayError(response.error);
								}
								else if (x.status === 401) {
									const response = JSON.parse(x.responseText);
									handleUnauthorized(response);
								}
								else if (x.status == 405) {
									alert("Last instance");
									deleteLastInstanceComponent.update(src, pictureId);
									deleteLastInstanceComponent.show();
								}
							} catch (e) {
								console.error("Error parsing JSON response: " + e.message);
							}
						}
					}
				});
				
				imagesRow.appendChild(imageHtml);
			});

			for (let i = 0; i < imageDisplaySize - imagesToDraw.length; i++) {
				imagesRow.appendChild(emptySlotTemplate.cloneNode(true));
			}

			//Rendering logic for previousImgs button
			if (albumPage > 1) {
				previousImgs.classList.remove('hidden');
				previousImgs.classList.add('active');
			}
			else {
				previousImgs.classList.remove('active');
				previousImgs.classList.add('hidden');
			}

			//Rendering logic for nextImgs button
			if (imagesToSkip + imageDisplaySize < images.length) {
				nextImgs.classList.remove('hidden');
				nextImgs.classList.add('active');
			}
			else {
				nextImgs.classList.remove('active');
				nextImgs.classList.add('hidden');
			}
		}
	}
	
	
	
	
	//ImageUploader class
	function imageUploader(parent){
		//ATTRIBUTES
		let imageUploader = document.getElementById("imageUploaderContainer");
		let errorDiv = imageUploader.getElementsByClassName("errorText")[0];
		let errorParent = errorDiv.parentNode;
		const self = this;
		errorParent.removeChild(errorDiv);
		
		let albumEditOverlay = new albumEditOverlayTemp();
		
		
		//METHODS
		this.registerEvents = function(parent){
			albumEditOverlay.registerEvents(parent);
			
			//Listener logic for addExistingImage button
			document.getElementById("addExistingImage").addEventListener("click", (e) =>{
				e.preventDefault();
				this.reset();
				albumEditOverlay.show();
			});
			
			//Listener for submit button
			document.getElementById("uploadImage").addEventListener("click", (e)=>{
				e.preventDefault();
				const form = e.target.closest("form");

				const albumId = sessionStorage.getItem("albumId");
				const requestUrl = 'UploadImage?albumId=' + albumId;

				if (form.checkValidity()) {
					postRequest(requestUrl, e.target.closest("form"), uploadImageCallback);
				}
				else {
					form.reportValidity();
				}
			});
			
			function uploadImageCallback(x){
				if (x.readyState === XMLHttpRequest.DONE) {
					try {
						if (x.status === 200) {
							self.reset();
							parent.update();
						}
						else if ([400, 402, 403, 500].includes(x.status)) {
							const response = JSON.parse(x.responseText);
							displayError(response.error);
						}
						else if (x.status === 401) {
							const response = JSON.parse(x.responseText);
							handleUnauthorized(response);
						}
					} catch (e) {
						console.error("Error parsing JSON response: " + e.message);
					}
				}
			}
			
			function displayError(message) {
				errorDiv.textContent = message;
				errorParent.insertBefore(errorDiv, errorParent.firstChild);
				alert(message);
			}
		}
		
		this.reset = function(){
			albumEditOverlay.reset();
			imageUploader.getElementsByTagName("form")[0].reset();
			
			if(errorParent.contains(errorDiv))
				errorParent.removeChild(errorDiv);
		}
		
		this.show = function() {
			imageUploader.style.removeProperty("display");
			
		}
		
		this.hide = function(){
			imageUploader.style.display = "none";
		}
		
		this.update = function(otherImages){
			albumEditOverlay.update(otherImages);
		}
	}
	
	
	//TODO change function name
	//AlbumEditOverlay class
	function albumEditOverlayTemp(){
		//ATTRIBUTES
		let albumEditOverlay = document.getElementById("albumEditOverlay");
		let errorDiv = albumEditOverlay.getElementsByClassName("errorText")[0];
		let errorParent = errorDiv.parentNode;
		const self = this;
		errorParent.removeChild(errorDiv);
		
		//METHODS
		this.registerEvents = function(parent){
			
			//Listener Logic for cancel button
			document.getElementById("cancelAddPicturesBtn").addEventListener("click", (e)=>{
				e.preventDefault();
				albumEditOverlay.classList.remove('active');
				albumEditOverlay.classList.add('hidden');
			});
			
			//Listener logic for submit button
			document.getElementById("addPicturesBtn").addEventListener("click", (e) => {
				e.preventDefault();
				
				const form = e.target.closest("form");
				const albumId = sessionStorage.getItem("albumId");
				const requestUrl = 'AddToAlbum?albumId=' + albumId;

				if (form.checkValidity()) {
					postRequest(requestUrl, e.target.closest("form"), addPicturesCallback);
				}
				else {
					form.reportValidity();
				}
			});
			
			//Callback logic for addPicturesToAlbum
			function addPicturesCallback(x){
				if (x.readyState === XMLHttpRequest.DONE) {
					try {
						if (x.status === 200) {
							self.hide();
							self.reset();
							parent.update();
						}
						else if ([400, 402, 403, 500].includes(x.status)) {
							const response = JSON.parse(x.responseText);
							displayError(response.error);
						}
						else if (x.status === 401) {
							const response = JSON.parse(x.responseText);
							handleUnauthorized(response);
						}
					} catch (e) {
						console.error("Error parsing JSON response: " + e.message);
					}
				}
			}
			
			function displayError(message){
				errorDiv.textContent = message;
				errorParent.insertBefore(errorDiv, errorParent.children[1]);
				alert(message);
			}
		}
		
		this.show = function(){
			albumEditOverlay.classList.remove('hidden');
			albumEditOverlay.classList.add('active');
		}
		
		this.hide = function(){
			albumEditOverlay.classList.remove('active');
			albumEditOverlay.classList.add('hidden');
		}
		
		this.reset = function(){
			albumEditOverlay.getElementsByTagName("form")[0].reset();
		}
		
		this.update = function(otherImages){
			const tableRowTemplate = albumEditOverlay.getElementsByTagName("tr")[0].cloneNode(true);
			
			//Remove old images
			const table = albumEditOverlay.getElementsByTagName("tbody")[0];
			while(table.firstChild){
				table.removeChild(table.lastChild);
			}
			
			//Populate with new images
			otherImages.forEach((image, index) => {
				const tableRow = tableRowTemplate.cloneNode(true);
				tableRow.className = index % 2 === 0 ? "odd" : "even";
				tableRow.getElementsByTagName("input")[0].setAttribute("value", image.id);
				tableRow.getElementsByTagName("img")[0].setAttribute("src", getImageHost()+image.thumbnailPath);
				tableRow.getElementsByTagName("p")[0].innerText = image.title;
				tableRow.getElementsByTagName("p")[1].innerText = image.uploadDate;
				
				table.appendChild(tableRow);
			});
		}
	}
	
	//DeleteLastInstanceOverlay class
		function DeleteLastInstanceOverlay(){
			//ATTRIBUTES
			let overlay = document.getElementById("removeFromLastAlbumOverlay");
			const self = this;
			let pictureIdLocal;
			
			//METHODS
			this.registerEvents = function(parent){
				
				//Listener Logic for cancel button
				document.getElementById("cancelRemoveFromLastAlbum").addEventListener("click", (e)=>{
					e.preventDefault();
					self.hide();
				});
				
				//Listener logic for submit button
				document.getElementById("confirmRemoveFromLastAlbum").addEventListener("click", (e) => {
					e.preventDefault();
					const requestUrl = 'DeleteImage?pictureId=' + pictureIdLocal;

					postRequest(requestUrl, e.target.closest("form"), deletePicturesCallback);
					
				});
				
				//Callback logic for addPicturesToAlbum
				function deletePicturesCallback(x){
					if (x.readyState === XMLHttpRequest.DONE) {
						try {
							self.hide();
							if (x.status === 200) {
								parent.update();
							}
							else if ([400, 402, 403, 500].includes(x.status)) {
								const response = JSON.parse(x.responseText);
								alert(response.error);
							}
							else if (x.status === 401) {
								const response = JSON.parse(x.responseText);
								handleUnauthorized(response);
							}
						} catch (e) {
							console.error("Error parsing JSON response: " + e.message);
						}
					}
				}
			}
			
			this.show = function(){
				overlay.classList.remove('hidden');
				overlay.classList.add('active');
			}
			
			this.hide = function(){
				overlay.classList.remove('active');
				overlay.classList.add('hidden');
			}
			
			this.update = function(src, pictureId){
				pictureIdLocal = pictureId;
				overlay.getElementsByTagName("img")[0].setAttribute("src", src);
			}
		}
	
})();











