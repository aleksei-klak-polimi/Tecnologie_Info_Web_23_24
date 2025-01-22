/**
 * 
 */

(function(){
	const imageDisplaySize = 5;
	
	window.AlbumPageManager = function(_manager){
		//ATTRIBUTES
		const manager = _manager;
		const albumContainer = document.getElementById("albumOuterContainer");
		let albumImages = [];
		let unsortedImages = [];
		let otherImages = [];
		let comments = new Map();
		
		let albumId = null;
		let albumOwner = false;
		let albumTitle = null;
		
		//COMPONENTS
		const albumDisplayComponent = new AlbumDisplay(this);
		const imageUploaderComponent = new ImageUploader(this);
		//OVERLAYS
		const imageDetailsComponent = new ImageView(this);
		const deleteImageComponent = new DeleteImage(this);
		const editAlbumPictures = new AddImages(this);
		const reorderAlbumPictures = new ReorderAlbumView(this);
		
		//GETTERS
		this.getAlbumImages = function(){
			return albumImages;
		}
		this.getUnsortedImages = function(){
			return unsortedImages;
		}
		this.getOtherImages = function(){
			return otherImages;
		}
		this.getComments = function(){
			return comments;
		}
		
		this.getAlbumId = function(){
			return albumId;
		}
		this.getAlbumTitle = function(){
			return albumTitle;
		}
		this.isAlbumOwner = function(){
			return albumOwner;
		}

		//METHODS
		this.init = function(){
			//Set initial component visibility
			albumDisplayComponent.show();
			if (albumOwner)
				imageUploaderComponent.show();
			else
				imageUploaderComponent.hide();
			imageDetailsComponent.hide();
			deleteImageComponent.hide();
			editAlbumPictures.hide();
			reorderAlbumPictures.hide();
		}
		
		this.registerEvents = function() {		
			albumDisplayComponent.registerEvents();
			imageUploaderComponent.registerEvents();
			imageDetailsComponent.registerEvents();
			deleteImageComponent.registerEvents();
			editAlbumPictures.registerEvents();
			reorderAlbumPictures.registerEvents();
		}


		this.update = function(_albumId) {
			if(_albumId){
				albumId = Number(_albumId);
			}
			
			if(albumId){
				let request = "GetAlbum?albumId=" + albumId;
				getRequest(request, x => (refreshImagesCallback(x)));

				function refreshImagesCallback(x) {
					if (x.readyState === XMLHttpRequest.DONE) {
						try {
							if (x.status === 200) {
								const response = JSON.parse(x.responseText);
								const recievedObjects = JSON.parse(response.data);

								//Retrieve and sort images according to user preference
								const album = JSON.parse(recievedObjects[0]);
								albumTitle = album.title;
								
								const user = JSON.parse(sessionStorage.getItem("user"));
								const username = user.username;
								
								if(album.owner === username){
									albumOwner = true;
									imageUploaderComponent.show();
								}
								else{
									albumOwner = false;
									imageUploaderComponent.hide();
								}
								
								unsortedImages = JSON.parse(recievedObjects[1]);
								let imageOrder = JSON.parse(recievedObjects[2]);
								albumImages = sortImages(unsortedImages, imageOrder);

								otherImages = JSON.parse(recievedObjects[3]);

								let commentList = JSON.parse(recievedObjects[4]);
								comments = new Map();

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

								//Refresh components
								albumDisplayComponent.update();
								imageDetailsComponent.refresh();
								editAlbumPictures.refresh();
								reorderAlbumPictures.refresh();

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
			}
		}


		this.show = function() {
			albumContainer.removeAttribute("style");
		}

		this.hide = function() {
			albumContainer.style.display = "none";
		}

		this.reset = function() {
			imageUploaderComponent.reset();
		}
		
		this.showImageDetails = function(pictureId){
			this.hideOverlays();
			
			//Open new overlay
			imageDetailsComponent.update(pictureId);
			imageDetailsComponent.show();
		}
		
		this.showDeleteImage = function(pictureId, albumMessage){
			this.hideOverlays();
			
			deleteImageComponent.update(pictureId, albumMessage);
			deleteImageComponent.show();
		};
		
		this.showOtherImages = function(){
			this.hideOverlays();
			
			editAlbumPictures.show();
		}
		
		this.showReorderAlbumPictures = function(){
			this.hideOverlays();
			
			reorderAlbumPictures.show();
		}
		
		this.hideOverlays = function(){
			imageDetailsComponent.hide();
			editAlbumPictures.hide();
			deleteImageComponent.hide();
			reorderAlbumPictures.hide();
			
			imageDetailsComponent.reset();
			editAlbumPictures.reset();
			reorderAlbumPictures.reset();
		}
		
		
		//HELPER METHODS
		function sortImages(albumImages, imageOrder){
			const sortedByPreference = [];
			const sortedByDefault = [];
			const sortingMap = new Map();
			
			//Populate map for o(1) access later in the sorting
			imageOrder.forEach((image) => {
				sortingMap.set(image.pictureId, image.pictureOrder);
			});
			
			//Sort pictures
			albumImages.forEach((image) =>{
				if(sortingMap.has(image.id)){
					const position = sortingMap.get(image.id);
					sortedByPreference[position] = image;
				}
				else
					sortedByDefault.push(image);
			});
			
			
			// Remove holes in sortedByPreference
			const cleanSortedByPreference = sortedByPreference.filter(Boolean);
			//Join and return the two arrays
			return sortedByDefault.concat(cleanSortedByPreference);
		}
	}

	
	
	
	
	function AlbumDisplay(_manager){
		const manager = _manager;
		let albumPage = 1;
		const imageDisplay = document.getElementById("imageDisplayContainer");
		const thumbnailSlotTemplate = document.getElementsByClassName("thumbnailSlot")[0];
		const emptySlotTemplate = document.getElementsByClassName("emptySlot")[0];
		
		const previousImgs = document.getElementById("previousImgs");
		const nextImgs = document.getElementById("nextImgs");
		const reorderImgBtn = document.getElementById("albumDisplayHeader").getElementsByTagName("img")[0];
		
		this.registerEvents = function() {
			//Listener logic for next and prev buttons
			previousImgs.addEventListener("click", (e) => {
				e.preventDefault();
				changeAlbumPage(-1);
				this.refresh();
			});
			nextImgs.addEventListener("click", (e) => {
				e.preventDefault();
				changeAlbumPage(1);
				this.refresh();
			});
			
			reorderImgBtn.addEventListener("click", (e) =>{
				e.preventDefault();
				manager.showReorderAlbumPictures();
			});

			function changeAlbumPage(delta) {
				albumPage += Number(delta);
				const imagesAmount = manager.getAlbumImages().lenght;

				if (albumPage < 1) {
					albumPage = 1;
				}
				else if (albumPage > Math.ceil(imagesAmount / imageDisplaySize)) {
					albumPage = Math.ceil(imagesAmount / imageDisplaySize);
				}
			}
		}
		
		this.show = function(){
			imageDisplay.removeAttribute("style");
		}
		
		this.hide = function(){
			imageDisplay.style.display = "none";
		}
		
		this.update = function(){
			const imageAmount = manager.getAlbumImages().length;
			if(imageAmount <=((albumPage -1)*imageDisplaySize)){
				if(albumPage > 1)
					albumPage--;
			}
			
			this.refresh();
		}
		
		this.refresh = function(){
			const images = manager.getAlbumImages();
			
			//Update Album Title
			const albumTitle = manager.getAlbumTitle() ? manager.getAlbumTitle() : "Missing album title";
			document.getElementById("albumTitle").textContent = albumTitle;

			//Clear old images
			while (imagesRow.firstChild) {
				imagesRow.removeChild(imagesRow.lastChild);
			}

			let imagesToSkip = (albumPage - 1) * imageDisplaySize;
			let imagesToDraw = images.slice(imagesToSkip, imagesToSkip + imageDisplaySize);

			imagesToDraw.forEach((image) => {
				const imageHtml = thumbnailSlotTemplate.cloneNode(true);
				imageHtml.getElementsByTagName("img")[0].setAttribute("src", getImageHost() + image.thumbnailPath);
				imageHtml.getElementsByTagName("img")[0].setAttribute("data-pictureId", image.id);
				imageHtml.getElementsByTagName("img")[0].addEventListener("click", (e) => {
					e.preventDefault();
					manager.showImageDetails(image.id);
				});

				imageHtml.getElementsByClassName("imageTitle")[0].textContent = image.title;
				imageHtml.getElementsByClassName("removeImageButton")[0].addEventListener("click", (e) => {
					e.preventDefault();
					removeImageFromAlbum(e);

					function removeImageFromAlbum(e) {
						let pictureId = e.target.parentNode.parentNode.parentNode.getElementsByTagName("img")[0].getAttribute("data-pictureId");
						let src = e.target.parentNode.parentNode.parentNode.getElementsByTagName("img")[0].getAttribute("src");

						let albumId = manager.getAlbumId();
						let requestPath = "RemoveFromAlbum?albumId=" + albumId + "&pictureId=" + pictureId;

						postRequest(requestPath, null, (x) => removeImageFromAlbumCallback(x, pictureId, src));
					}

					function removeImageFromAlbumCallback(x, pictureId, src) {
						if (x.readyState === XMLHttpRequest.DONE) {
							try {
								if (x.status === 200) {
									manager.update();
								}
								else if ([400, 402, 403, 500].includes(x.status)) {
									const response = JSON.parse(x.responseText);
									displayError(response.error);
								}
								else if (x.status === 401) {
									handleUnauthorized();
								}
								else if (x.status == 405) {
									alert("Last instance");
									manager.showDeleteImage(pictureId, true);
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
			
			//If user is not owner hide all "remove Image" buttons
			let editCommandsVisibility = manager.isAlbumOwner() === true ? "visible" : "hidden";

			const ownerContent = Array.from(imageDisplay.getElementsByClassName("forOwner"));
			for (let i = 0; i < ownerContent.length; i++) {
				ownerContent[i].style.visibility = editCommandsVisibility;
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
	
	
	
	
	function ImageUploader(_manager){
		//ATTRIBUTES
		const manager = _manager;
		let imageUploader = document.getElementById("imageUploaderContainer");
		let errorDiv = imageUploader.getElementsByClassName("errorText")[0];
		let errorParent = errorDiv.parentNode;
		const self = this;
		errorParent.removeChild(errorDiv);


		//METHODS
		this.registerEvents = function(parent) {
			//Listener logic for addExistingImage button
			document.getElementById("addExistingImage").addEventListener("click", (e) => {
				e.preventDefault();
				this.reset();
				manager.showOtherImages();
			});

			//Listener for submit button
			document.getElementById("uploadImage").addEventListener("click", (e) => {
				e.preventDefault();
				const form = e.target.closest("form");

				const albumId = manager.getAlbumId();
				const requestUrl = 'UploadImage?albumId=' + albumId;

				if (form.checkValidity()) {
					postRequest(requestUrl, e.target.closest("form"), uploadImageCallback);
				}
				else {
					form.reportValidity();
				}
			});

			function uploadImageCallback(x) {
				if (x.readyState === XMLHttpRequest.DONE) {
					try {
						if (x.status === 200) {
							self.reset();
							manager.update();
						}
						else if ([400, 402, 403, 500].includes(x.status)) {
							const response = JSON.parse(x.responseText);
							displayError(response.error);
						}
						else if (x.status === 401) {
							handleUnauthorized();
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

		this.reset = function() {
			imageUploader.getElementsByTagName("form")[0].reset();

			if (errorParent.contains(errorDiv))
				errorParent.removeChild(errorDiv);
		}

		this.show = function() {
			imageUploader.style.display = "";
		}

		this.hide = function() {
			imageUploader.style.display = "none";
		}
	}
	
	
	
	
	function AddImages(_manager){
		//ATTRIBUTES
		const manager = _manager;
		let albumEditOverlay = document.getElementById("albumEditOverlay");
		let errorDiv = albumEditOverlay.getElementsByClassName("errorText")[0];
		let errorParent = errorDiv.parentNode;
		const self = this;
		errorParent.removeChild(errorDiv);

		//METHODS
		this.registerEvents = function() {

			//Listener Logic for cancel button
			document.getElementById("cancelAddPicturesBtn").addEventListener("click", (e) => {
				e.preventDefault();
				this.hide();
				this.reset();
			});

			//Listener logic for submit button
			document.getElementById("addPicturesBtn").addEventListener("click", (e) => {
				e.preventDefault();

				const form = e.target.closest("form");
				const albumId = manager.getAlbumId();
				const requestUrl = 'AddToAlbum?albumId=' + albumId;

				if (form.checkValidity()) {
					postRequest(requestUrl, e.target.closest("form"), addPicturesCallback);
				}
				else {
					form.reportValidity();
				}
			});

			//Callback logic for addPicturesToAlbum
			function addPicturesCallback(x) {
				if (x.readyState === XMLHttpRequest.DONE) {
					try {
						if (x.status === 200) {
							self.hide();
							self.reset();
							manager.update();
						}
						else if ([400, 402, 403, 500].includes(x.status)) {
							const response = JSON.parse(x.responseText);
							displayError(response.error);
						}
						else if (x.status === 401) {
							handleUnauthorized();
						}
					} catch (e) {
						console.error("Error parsing JSON response: " + e.message);
					}
				}
			}

			function displayError(message) {
				errorDiv.textContent = message;
				errorParent.insertBefore(errorDiv, errorParent.children[1]);
				alert(message);
			}
		}

		this.show = function() {
			albumEditOverlay.classList.remove('hidden');
			albumEditOverlay.classList.add('active');
		}

		this.hide = function() {
			albumEditOverlay.classList.remove('active');
			albumEditOverlay.classList.add('hidden');
		}

		this.reset = function() {
			albumEditOverlay.getElementsByTagName("form")[0].reset();
			
			if(albumEditOverlay.getElementsByClassName("errorText")[0])
				errorParent.removeChild(errorDiv);
		}

		this.refresh = function() {
			const tableRowTemplate = albumEditOverlay.getElementsByTagName("tr")[0].cloneNode(true);

			//Remove old images
			const table = albumEditOverlay.getElementsByTagName("tbody")[0];
			while (table.firstChild) {
				table.removeChild(table.lastChild);
			}

			//Populate with new images
			manager.getOtherImages().forEach((image, index) => {
				const tableRow = tableRowTemplate.cloneNode(true);
				tableRow.className = index % 2 === 0 ? "odd" : "even";
				tableRow.getElementsByTagName("input")[0].setAttribute("value", image.id);
				tableRow.getElementsByTagName("img")[0].setAttribute("src", getImageHost() + image.thumbnailPath);
				tableRow.getElementsByTagName("p")[0].innerText = image.title;
				tableRow.getElementsByTagName("p")[1].innerText = image.uploadDate;

				table.appendChild(tableRow);
			});
		}
	}
	
	
	
	
	
	function DeleteImage(_manager) {
		//ATTRIBUTES
		const manager = _manager;
		const overlay = document.getElementById("deleteImageOverlay");
		const self = this;
		let pictureId;

		//METHODS
		this.registerEvents = function() {

			//Listener Logic for cancel button
			document.getElementById("cancelDeleteImage").addEventListener("click", (e) => {
				e.preventDefault();
				self.hide();
			});

			//Listener logic for submit button
			document.getElementById("confirmDeleteImage").addEventListener("click", (e) => {
				e.preventDefault();
				const requestUrl = 'DeleteImage?pictureId=' + pictureId;

				postRequest(requestUrl, e.target.closest("form"), deletePicturesCallback);

			});

			//Callback logic for addPicturesToAlbum
			function deletePicturesCallback(x) {
				if (x.readyState === XMLHttpRequest.DONE) {
					try {
						self.hide();
						if (x.status === 200) {
							manager.update();
						}
						else if ([400, 402, 403, 500].includes(x.status)) {
							const response = JSON.parse(x.responseText);
							alert(response.error);
						}
						else if (x.status === 401) {
							handleUnauthorized();
						}
					} catch (e) {
						console.error("Error parsing JSON response: " + e.message);
					}
				}
			}
		}

		this.show = function() {
			overlay.classList.remove('hidden');
			overlay.classList.add('active');
		}

		this.hide = function() {
			overlay.classList.remove('active');
			overlay.classList.add('hidden');
		}

		this.update = function(_pictureId, albumMessage) {
			pictureId = _pictureId;
			const picture = manager.getAlbumImages().find((element) => element.id == pictureId);
			const src = getImageHost() + picture.thumbnailPath;
			overlay.getElementsByTagName("img")[0].setAttribute("src", src);

			//TODO change text if last picture instance or simply delete
			if (albumMessage) {
				Array.prototype.forEach.call(overlay.getElementsByClassName("removeFromLastAlbumMessage"), function(element) {
					element.removeAttribute("style");
				});
				Array.prototype.forEach.call(overlay.getElementsByClassName("defaultDeleteMessage"), function(element) {
					element.style.display = "none";
				});
			}
			else {
				Array.prototype.forEach.call(overlay.getElementsByClassName("defaultDeleteMessage"), function(element) {
					element.removeAttribute("style");
				});
				Array.prototype.forEach.call(overlay.getElementsByClassName("removeFromLastAlbumMessage"), function(element) {
					element.style.display = "none";
				});
			}
		}
	}
	
	
	
	
	
	
	function ImageView(_manager) {
		//ATTRIBUTES
		let pictureId;
		const manager = _manager;
		const modal = document.getElementById("imageContainerOverlay");
		const imageDisplayComponent = new ImageDetailsView(_manager, this);
		const imageEditComponent = new EditImageView(_manager, this);
		imageEditComponent.hide();



		//METHODS
		this.registerEvents = function() {
			imageDisplayComponent.registerEvents();
			imageEditComponent.registerEvents();

			modal.getElementsByClassName("closeOverlayButton")[0].addEventListener("click", (e) => {
				e.preventDefault();
				this.hide();
			})
		}

		this.update = function(_pictureId) {
			if (_pictureId !== undefined) {
				pictureId = _pictureId;
				
				imageDisplayComponent.update(pictureId);
				imageEditComponent.update(pictureId);
				
				this.refresh();
			}

			this.reset();
		}

		this.refresh = function(){
			if(pictureId){
				const picture = manager.getAlbumImages().find((element) => element.id === pictureId);
				
				if(picture){
					modal.getElementsByTagName("img")[1].setAttribute("src", getImageHost() + picture.path);
					document.getElementById("imageTitle").innerText = picture.title;
					document.getElementById("imageDate").innerText = picture.uploadDate;

					document.getElementById("imageDescription").firstElementChild.innerText = picture.description;

					imageDisplayComponent.refresh();
					imageEditComponent.refresh();
				}
			}
		}
		
		this.show = function() {
			modal.classList.remove('hidden');
			modal.classList.add('active');
		}

		this.hide = function() {
			modal.classList.remove('active');
			modal.classList.add('hidden');
		}

		this.reset = function() {
			imageDisplayComponent.show();
			imageEditComponent.hide();
			
			//Scroll to the top of the modal
			modal.getElementsByClassName("imageContainer")[0].scrollTop = 0;
		}
		
		this.showImageDetails = function(){
			imageEditComponent.hide();
			imageEditComponent.reset();
			imageDisplayComponent.show();
		}
		
		this.showImageEdit = function(){
			imageDisplayComponent.hide();
			imageEditComponent.show();
		}
	}
	
	
	
	
	
	
	
	
	function ImageDetailsView(_manager, _parent){
		const parent = _parent;
		const manager = _manager;
		let pictureId;
		const imageDescription = document.getElementById("imageDescription");
		const editImageButtons = document.getElementById("editImageButtons");
		const commentSection = new CommentSectionView(_manager);


		this.registerEvents = function() {
			commentSection.registerEvents();

			editImageButtons.getElementsByTagName("a")[0].addEventListener("click", (e) => {
				e.preventDefault();
				this.reset();
				parent.showImageEdit();
			})

			editImageButtons.getElementsByTagName("a")[1].addEventListener("click", (e) => {
				e.preventDefault();
				this.reset();
				manager.showDeleteImage(pictureId, false);
			})
		}

		this.show = function() {
			imageDescription.removeAttribute("style");
			commentSection.show();

			if (manager.isAlbumOwner() && manager.isAlbumOwner() === true)
				editImageButtons.removeAttribute("style");
			else
				editImageButtons.style.display = "none";
		}

		this.hide = function() {
			imageDescription.style.display = "none";
			editImageButtons.style.display = "none";
			commentSection.hide();
		}

		this.update = function(_pictureId) {
			pictureId = _pictureId;
			commentSection.update(pictureId);
			this.refresh()
		}
		
		this.refresh = function(){
			const pictures = manager.getAlbumImages();
			const picture = pictures.find((element) => element.id === pictureId);
			imageDescription.firstElementChild.innerText = picture.description;
			commentSection.refresh();
		}
		
		this.reset = function() {
			commentSection.reset();
		}
	}
	
	
	
	
	
	
	
	
	function EditImageView(_manager, _parent) {
		const manager = _manager;
		const parent = _parent;
		const self = this;
		const editImageContainer = document.getElementById("editImageContainer");
		const form = editImageContainer.getElementsByTagName("form")[0];
		const errorDiv = editImageContainer.getElementsByClassName("errorText")[0];
		const errorParent = errorDiv.parentElement;
		let pictureId;

		errorParent.removeChild(errorDiv);


		this.registerEvents = function() {
			document.getElementById("cancelEditImage").addEventListener("click", (e) => {
				e.preventDefault();
				parent.showImageDetails();
			});

			document.getElementById("submitEditImage").addEventListener("click", (e) => {
				e.preventDefault();
				
				const pictures = manager.getAlbumImages();
				const picture = pictures.find((element) => element.id === pictureId);
				
				if (picture) {
					const request = "EditImage?pictureId=" + picture.id;

					if (form.checkValidity()) {
						const description = form.getElementsByTagName("textarea")[0].value.trim();
						if (description.length > 0) {
							if (!validDescription(description))
								return;
						}
						postRequest(request, form, (x) => editImageCallback(x));
					}
					else {
						form.reportValidity();
					}

					function editImageCallback(x) {
						if (x.readyState === XMLHttpRequest.DONE) {
							try {
								if (x.status === 200) {
									self.reset();
									manager.update();
									parent.showImageDetails();
								}
								else {
									const response = JSON.parse(x.responseText);
									displayError(response.error);
								}

							} catch (e) {
								console.error("Error parsing JSON response: " + e.message);
							}
						}
					}

					function validDescription(description) {
						const regex = new RegExp("^[a-zA-Z0-9(?:\\r\\n|\\r|\\n|\\xB0)\\x3F\\x21\\x40\\x23\\x24\\x25\\x5E\\x26\\x2A\\x28\\x29\\x5F\\x2B\\x2D\\x3D\\x5B\\x5D\\x7B\\x7D\\x7C\\x3B\\x3A\\x27\\x22\\x2C\\x2E\\x3C\\x3E\\x3F\\x2F\\x5C\\x7E\\x20]+$");

						if (description.length > 1023) {
							displayError("Description too long");
							return false;
						}
						else if (!regex.test(description)) {
							displayError("Description contains unsupported characters");
							return false;
						}

						return true;
					}

					function displayError(message) {
						errorDiv.textContent = message;
						errorParent.appendChild(errorDiv);
						alert(message);
					}
				}

			});
		}

		this.update = function(_pictureId) {
			pictureId = _pictureId;
		}

		this.hide = function() {
			editImageContainer.style.display = "none";
		}

		this.show = function() {
			editImageContainer.removeAttribute("style");
		}
		
		this.refresh = function(){
			const pictures = manager.getAlbumImages();
			const picture = pictures.find((element) => element.id === pictureId);
			
			if(picture){
				document.getElementById("editedImageTitle").value = picture.title;
				document.getElementById("editedImageDescription").value = picture.description;
				document.getElementById("editedImageDate").value = picture.uploadDate;

				if (errorParent.lastElementChild === errorDiv)
					errorParent.removeChild(errorDiv);
			}
		}
		
		this.reset = function() {
			if(pictureId)
				this.refresh();
			else
				form.reset();
		}
	}
	
	
	
	
	
	
	
	
	function CommentSectionView(_manager){
		const manager = _manager;
		const self = this;
		const commentForm = document.getElementById("commentForm");
		const commentSection = document.getElementById("commentSection");
		const commentTemplate = commentSection.getElementsByClassName("comment")[0].cloneNode(true);
		const errorDiv = commentForm.getElementsByClassName("errorText")[0];
		const errorParent = errorDiv.parentElement;
		let pictureId;

		errorParent.removeChild(errorDiv);

		this.registerEvents = function(parent) {
			document.getElementById("submitComment").addEventListener("click", e => {
				e.preventDefault();
				const form = e.target.closest("form");
				if (form.checkValidity()) {
					if (validForm(form)) {
						const request = "PostComment?pictureId=" + pictureId;
						postRequest(request, form, (x) => postCommentCallback(x, parent));
					}
				}
				else {
					commentForm.reportValidity();
				}

				function validForm(form) {
					const regex = new RegExp("^[a-zA-Z0-9(?:\\r\\n|\\r|\\n|\\xB0)\\x3F\\x21\\x40\\x23\\x24\\x25\\x5E\\x26\\x2A\\x28\\x29\\x5F\\x2B\\x2D\\x3D\\x5B\\x5D\\x7B\\x7D\\x7C\\x3B\\x3A\\x27\\x22\\x2C\\x2E\\x3C\\x3E\\x3F\\x2F\\x5C\\x7E\\x20]+$");
					const textContent = form.getElementsByTagName("textarea")[0].value.trim();

					if (textContent.length === 0) {
						displayError("Comment body can't be empty");
						return false;
					}
					else if (textContent.length > 1023) {
						displayError("Comment body too long");
						return false;
					}
					else if (!regex.test(textContent)) {
						displayError("Comment contains unsupported characters");
						return false;
					}

					return true;
				}

				function postCommentCallback(x, parent) {
					if (x.readyState === XMLHttpRequest.DONE) {
						try {
							if (x.status === 200) {
								self.reset();
								manager.update();
							}
							else {
								const response = JSON.parse(x.responseText);
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

			});
		}

		this.update = function(_pictureId) {
			pictureId = _pictureId;
			this.refresh();
		}
		
		this.refresh = function(){
			//Update comment header
			const comments = manager.getComments().get(pictureId);
			const commentSection = document.getElementById("commentSection");
			const commentSectionHeader = document.getElementById("commentSectionHeader");
			const commentNumber = comments === undefined ? 0 : comments.length;

			commentSectionHeader.firstElementChild.innerText = commentNumber;
			commentSectionHeader.lastElementChild.innerText = commentNumber != 1 ? "Comments" : "Comment";

			//Clear old comments
			while (commentSection.lastChild != commentSectionHeader) {
				commentSection.removeChild(commentSection.lastChild);
			}

			//Add new comments
			if (commentNumber > 0) {
				comments.forEach((comment) => {
					const commentHtml = commentTemplate.cloneNode(true);
					commentHtml.getElementsByClassName("commentAuthor")[0].innerText = comment.author;
					commentHtml.getElementsByClassName("commentBody")[0].innerText = comment.body;
					commentHtml.getElementsByClassName("commentDate")[0].innerText = comment.postDate;

					commentSection.appendChild(commentHtml);
				});
			}
		}

		this.show = function() {
			commentForm.removeAttribute("style");
			commentSection.removeAttribute("style");
		}

		this.hide = function() {
			commentForm.style.display = "none";
			commentSection.style.display = "none";
		}

		this.reset = function() {
			commentForm.reset();
		}
	}
	
	
	
	
	
	function ReorderAlbumView(_manager){
		const manager = _manager;
		const modal = document.getElementById("albumReorderOverlay");
		const rowTemplate = modal.getElementsByTagName("li")[0].cloneNode(true);
		const list = modal.getElementsByTagName("ul")[0];
		const errorDiv = modal.getElementsByClassName("errorText")[0];
		const errorParent = errorDiv.parentElement;
		const self = this
		let draggedItem;
		let defaultOrdering = false;
		
		errorParent.removeChild(errorDiv);
		
		this.registerEvents = function(){
			document.getElementById("cancelReorderPicturesBtn").addEventListener("click", (e) => {
				e.preventDefault();
				this.hide();
				this.reset();
			});
			
			document.getElementById("resetPictureOrdering").addEventListener("click", (e) => {
				e.preventDefault();
				const images = manager.getUnsortedImages();
				drawImages(images);
				defaultOrdering = true;
			})
			
			document.getElementById("confirmReorderPicturesBtn").addEventListener("click", (e) => {
				e.preventDefault();
				const formData = extractPictureOrderToFormData();
				
				//Check that form is not empty
				if(!formData.entries().next().done){
					const albumId = manager.getAlbumId();
					
					if(!albumId)
						return;
					
					//Check if user wants to return to use default ordering
					//or update the custom ordering
					if(!defaultOrdering){
						const requestPath = "UpdatePictureOrder?albumId="+albumId;
						postRequest(requestPath, formData, updatePictureOrderCallback);
					}
					else{
						const requestPath = "ResetPictureOrder?albumId="+albumId;
						postRequest(requestPath, formData, updatePictureOrderCallback);
					}
					
				}
				
				
				
				function updatePictureOrderCallback(x){
					if (x.readyState == XMLHttpRequest.DONE) {
						if (x.status == 200){
							manager.update();
							self.hide();
						}
						else {
							try{
								if(x.status === 401){
									handleUnauthorized();
								}
								else{
									errorDiv.textContent = message;
									errorParent.insertBefore(errorParent.lastElementChild);
									alert(message);
								}
							} 
							catch (e) {
								console.error("Error parsing JSON response: " + e.message);
							}
						}
					}
				}
			});
			
			list.addEventListener('dragstart', handleDragStart);
			list.addEventListener('dragover', handleDragOver);
			list.addEventListener('drop', handleDrop);
		}
		
		this.refresh = function(){
			const images = manager.getAlbumImages();
			drawImages(images);
		}
		
		function drawImages(images){
			//Remove old images
			while (list.firstChild) {
				list.removeChild(list.lastChild);
			}
			//Populate with new images
			images.forEach((image) => {
				const row = rowTemplate.cloneNode(true);
				row.setAttribute("data-pictureId", image.id);
				row.setAttribute("data-uploadDate", image.uploadDate);
				row.setAttribute("draggable", true);
				row.getElementsByTagName("img")[0].setAttribute("src", getImageHost() + image.thumbnailPath);
				row.getElementsByTagName("p")[0].innerText = image.title;
				row.getElementsByTagName("p")[1].innerText = image.uploadDate;

				list.appendChild(row);
			});
		}
		
		this.reset = function(){
			draggedItem = null;
			this.refresh();
			
			if(errorDiv.parentNode && errorDiv.parentNode.isEqualNode(errorParent))
				errorParent.removeChild(errorDiv);
		}
		
		this.show = function() {
			modal.classList.remove('hidden');
			modal.classList.add('active');
		}

		this.hide = function() {
			modal.classList.remove('active');
			modal.classList.add('hidden');
		}
		
		
		
		//DRAG-TO-REORDER-LOGIC
		// Drag start event handler
		function handleDragStart(event) {
			draggedItem = event.target.closest("li");

			// Calculate the offset of the cursor for the dragImage to match the 
			//list item
			const rect = draggedItem.getBoundingClientRect();
			const offsetX = event.clientX - rect.left;
			const offsetY = event.clientY - rect.top;
			
			event.dataTransfer.setDragImage(draggedItem, offsetX, offsetY);
			event.dataTransfer.effectAllowed = 'move';
		}
		
		// Drag over event handler
		function handleDragOver(event) {
			event.preventDefault();
			event.dataTransfer.dropEffect = 'move';
			const targetItem = event.target.closest("li");
			
			if (targetItem !== draggedItem && targetItem.classList.contains('draggable')) {
				const boundingRect = targetItem.getBoundingClientRect();
				const offset = boundingRect.y + (boundingRect.height / 2);
				if (event.clientY - offset > 0 && (!targetItem.nextSibling || !targetItem.nextSibling.isEqualNode(draggedItem))){
					targetItem.parentNode.insertBefore(draggedItem, targetItem.nextSibling);
					defaultOrdering = false;
				} 
				else if(event.clientY - offset <= 0 && (!targetItem.previousSibling || !targetItem.previousSibling.isEqualNode(draggedItem))){
					targetItem.parentNode.insertBefore(draggedItem, targetItem);
					defaultOrdering = false;
				}
			}
		}
		
		// Drop event handler
		function handleDrop(event) {
			event.preventDefault();
			draggedItem = null;
		}
		
		
		//Helper functions
		function extractPictureOrderToFormData() {
			const formData = new FormData();
			const items = Array.from(list.getElementsByTagName("li"));
			
			items.forEach((item, index) => {
				const pictureId = item.getAttribute("data-pictureid");
				if (pictureId) {
					formData.append(String(pictureId), String(index));
				} else {
					console.warn(`List item at position ${index} does not have a "data-pictureid" attribute.`);
				}
			});
			return formData;
		}
		
	}
	
	
})();














