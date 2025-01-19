/**
 * Contains logic for loading the default html and for logout
 */

(function (){
	let homeManager;
	
	document.addEventListener("DOMContentLoaded", () =>{
		homeManager = new HomeManager();
		homeManager.registerEvents();
	});
	
	function HomeManager(){
		const albumsPageManager = new AlbumsPageManager(this);
		const albumPageManager = new AlbumPageManager(this);
		albumPageManager.init();
		const logOutOverlay = new LogOutOverlay(this);
		
		albumPageManager.hide();
		
		albumsPageManager.update();
		albumsPageManager.show();
		
		this.registerEvents = function(){
			logOutOverlay.registerEvents();
			albumPageManager.registerEvents();
			albumsPageManager.registerEvents();
			
			document.addEventListener('keydown', (e) => {
				if(e.key == "Escape"){
					logOutOverlay.hide();
					albumPageManager.hideOverlays();
				}
			});
			
			document.getElementById("logOut").addEventListener("click", (e) => {
				e.preventDefault();
				logOutOverlay.show();
			});
			
			document.getElementById("Home").addEventListener("click", (e) => {
				e.preventDefault();
				albumPageManager.hide();
				albumsPageManager.show();
				albumPageManager.reset();
			});
		}
		
		this.showAlbum = function(albumId, albumOwner, albumTitle){
			albumPageManager.update(albumId, albumOwner, albumTitle);
			albumsPageManager.hide();
			albumPageManager.show();
		}
	}
	
	
	
	
	
	
	function LogOutOverlay(_manager){
		const manager = _manager;
		const overlay = document.getElementById('logoutOverlay');
		
		
		
		this.registerEvents = function(){
			document.getElementById("cancelLogOut").addEventListener("click", (e) => {
				e.preventDefault();
				this.hide();
			});
			
			document.getElementById("confirmLogOut").addEventListener("click", (e) => handleLogOut(e));
			
			overlay.addEventListener("click", (e) => {
				if (e.target === overlay){
					e.preventDefault()
					this.hide();
				}
			});
			
			
			function handleLogOut(e) {
				e.preventDefault();
				overlay.classList.remove('active');
				overlay.classList.add('hidden');

				postRequest("LogOut", null, handleLogOutCallback);
			}
			
			function handleLogOutCallback(x) {
				if (x.readyState === XMLHttpRequest.DONE) {
					try {
						const response = JSON.parse(x.responseText);

						if (x.status === 200) {
							if (response.redirect)
								window.location.href = response.redirect;
							else {
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
					catch (e) {
						console.error("Error parsing JSON response:", e.message);
					}
				}
			}
		}
		
		this.hide = function(){
			overlay.classList.remove('active');
			overlay.classList.add('hidden');
		}
		
		this.show = function(){
			overlay.classList.remove('hidden');
			overlay.classList.add('active');
		}
	}
	
})();













