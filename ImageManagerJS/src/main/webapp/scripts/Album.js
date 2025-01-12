/**
 * Contains logic for displaying picture for a given album and adding new ones.
 */

(function(){
	const imageDisplaySize = 5;
	
	const errorDiv = document.getElementsByClassName("errorText")[0];
	const errorParent = errorDiv.parentNode;
	
	const imagesRow = document.getElementById("imagesRow");
	const previousImgs = document.getElementById("previousImgs");
	const nextImgs = document.getElementById("nextImgs");
	
	const uploadImage = document.getElementById("uploadImage");
	const addExistingImage = document.getElementById("addExistingImage");
	
	document.getElementById("Home")
			.addEventListener("click", (e) => handleRedirectHome(e));
			
	previousImgs.addEventListener("click", (e) => changeAlbumPage(e, -1));
	nextImgs.addEventListener("click", (e) => changeAlbumPage(e, 1));
	
	uploadImage.addEventListener("click", (e) => handleUploadImage(e));
	addExistingImage.addEventListener("click", (e) => handleAddExistingImage(e));
	
	checkIfOwner();
	const thumbnailSlotTemplate = document.getElementsByClassName("thumbnailSlot")[0];
	const emptySlotTemplate = document.getElementsByClassName("emptySlot")[0];
	
	
	let images = [];
	let comments = new Map();
	
	
	//Clean-up place-holder html
	errorParent.removeChild(errorDiv);
	clearImages();
	sessionStorage.setItem("albumPage", 1);
	
	const albumTitle = sessionStorage.getItem("albumTitle");
	document.getElementById("albumTitle").textContent = albumTitle;
	
	//Query the server to populate the page.
	refreshImages();
	
	function refreshImages(){
		let albumId = sessionStorage.getItem("albumId");
		let request = "GetImagesByAlbum?albumId="+albumId;
		getRequest(request, x => (refreshImagesCallback(x)));
	}
	
	function refreshImagesCallback(x){
		if (x.readyState === XMLHttpRequest.DONE) {
			try {
				const response = JSON.parse(x.responseText);
				
				if(x.status === 200){
					const recievedObjects = JSON.parse(response.data);
					
					images = JSON.parse(recievedObjects[0]);
					let commentList = JSON.parse(recievedObjects[1]);
					
					commentList.forEach((comment) => {
						if(!comment.pictureId){
							console.warn("Comment missing 'pictureId' field:", comment);
							return;
						}
						
						// Check if the id already exists in the map
						if(!comments.has(comment.pictureId)){
							comments.set(comment.pictureId, []);
						}
						
						// Add the object to the list corresponding to its id
						comments.get(comment.pictureId).push(comment);
					});
					
					clearImages();
					populateImages();
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
	
	
	function checkIfOwner(){
		if(!sessionStorage.getItem("albumOwner")){
			console.error("Missing parameter albumOwner in sessionStorage");
		}
		else if(sessionStorage.getItem("albumOwner") === "false"){
			const ownerContent = Array.from(document.getElementsByClassName("forOwner"));
			for(let i = 0; i < ownerContent.length; i++){
				ownerContent[i].parentNode.removeChild(ownerContent[i]);
			}
		}
	}
	
	
	/**
	 * Removes all images from the imagesRow element.
	 */
	function clearImages() {
		while (imagesRow.firstChild) {
			imagesRow.removeChild(imagesRow.lastChild);
		}
	}
	
	
	function populateImages(){
		if(!sessionStorage.getItem("albumPage")){
			sessionStorage.setItem("albumPage", "1");
		}
		
		let albumPage = sessionStorage.getItem("albumPage");
		
		let imagesToSkip = (albumPage -1) * imageDisplaySize;
		let imagesToDraw = images.slice(imagesToSkip, imagesToSkip+imageDisplaySize);
		
		imagesToDraw.forEach((image) => {
			const imageHtml = thumbnailSlotTemplate.cloneNode(true);
			imageHtml.getElementsByTagName("img")[0].setAttribute("src", getImageHost() + image.thumbnailPath)
			imageHtml.getElementsByClassName("imageTitle")[0].textContent = image.title;
			
			imagesRow.appendChild(imageHtml);
		});
		
		for(let i = 0; i < imageDisplaySize-imagesToDraw.length; i++){
			imagesRow.appendChild(emptySlotTemplate.cloneNode(true));
		}
		
		//Rendering logic for previousImgs button
		if(albumPage > 1){
			previousImgs.classList.remove('hidden');
			previousImgs.classList.add('active');
		}
		else{
			previousImgs.classList.remove('active');
			previousImgs.classList.add('hidden');
		}
		
		//Rendering logic for nextImgs button
		if(imagesToSkip+imageDisplaySize < images.length){
			nextImgs.classList.remove('hidden');
			nextImgs.classList.add('active');
		}
		else{
			nextImgs.classList.remove('active');
			nextImgs.classList.add('hidden');
		}
	}
	
	
	function changeAlbumPage(e, delta){
		e.preventDefault();
		
		let albumPage = Number(sessionStorage.getItem("albumPage"));
		
		albumPage += Number(delta);
		
		if(albumPage < 1){
			albumPage = 1;
		}
		else if(albumPage > Math.ceil(images.length/imageDisplaySize)){
			albumPage = Math.ceil(images.length/imageDisplaySize);
		}
		
		sessionStorage.setItem("albumPage", albumPage);
		
		clearImages();
		populateImages();
	}
	
	
	function handleUploadImage(e){
		e.preventDefault();
		const form = e.target.closest("form");
		
		const albumId = sessionStorage.getItem("albumId");
		const requestUrl = 'UploadImage?albumId='+albumId;
		
		if (form.checkValidity()) {
			postRequest(requestUrl, e.target.closest("form"), uploadImageCallback);
		}
		else {
			form.reportValidity();
		}
	}
	
	
	function uploadImageCallback(x){
		if (x.readyState === XMLHttpRequest.DONE) {
			try {
				if (x.status === 200) {
					console.log("About to reset images");
					resetForm();
					refreshImages();
				}
				else if ([400, 402, 500].includes(x.status)) {
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
	
	function handleAddExistingImage(e){
		//TODO
		alert("TODO");
	}
	
	function resetForm(){
		const form = uploadImage.closest("form");
		form.reset();
	}
	
	function displayError(message){
		errorDiv.textContent = message;
		errorParent.insertBefore(errorDiv, errorParent.firstChild);
		alert(message);
	}
	
	
	/**
	 * Handles the event of user clicking on one of the albums on the page.
	 * Initializes the context switch for the Album View.
	 */
	function handleRedirectHome(e) {
		e.preventDefault();
		
		//Removing event listener is not directly possible
		//Replace the target with a clone to discard the listeners.
		let newHomeBtn = e.target.cloneNode(true);
		e.target.parentNode.replaceChild(newHomeBtn, e.target);
		
		//Clear session storage
		sessionStorage.clear("albumPage");

		replaceHtml('static/pages/Albums.html');
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











