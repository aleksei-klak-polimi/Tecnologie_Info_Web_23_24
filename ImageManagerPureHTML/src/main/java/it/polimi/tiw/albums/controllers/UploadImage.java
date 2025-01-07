package it.polimi.tiw.albums.controllers;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import it.polimi.tiw.albums.beans.User;
import it.polimi.tiw.albums.controllers.helpers.DBConnector;
import it.polimi.tiw.albums.daos.AlbumDAO;
import it.polimi.tiw.albums.daos.PictureDAO;
import it.polimi.tiw.albums.utils.InputSanitizer;
import it.polimi.tiw.albums.utils.MediaManager;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

@WebServlet("/UploadImage")
@MultipartConfig(
 fileSizeThreshold = 1024 * 1024 * 1, // 1 MB
 maxFileSize = 1024 * 1024 * 10,      // 10 MB
 maxRequestSize = 1024 * 1024 * 100   // 100 MB
)
public class UploadImage extends HttpServlet {
	// ATTRIBUTES
	private static final long serialVersionUID = 1L;
	private Connection conn;

	
	// CONSTRUCTOR
	public UploadImage() {
		super();
	}

	
	
	// SERVLET METHODS
	@Override
	public void init() throws ServletException {
		try {
			conn = DBConnector.getConnection(getServletContext());

		} catch (ClassNotFoundException e) {
			throw new UnavailableException("Can't load database driver");
		} catch (SQLException e) {
			throw new UnavailableException("Couldn't get db connection");
		}
	}

	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String imageName = "";
		try {
			HttpSession session = request.getSession();
			User user = (User) session.getAttribute("user");
			int uploader = user.getId();

			int albumId = validateAndRetrieveAlbumId(request, response, uploader);
			if (albumId == -1) return;

			String title = request.getParameter("title").trim();
			String description = request.getParameter("description").trim();
			Part filePart = request.getPart("image");

			if(!validateInputs(request, response, albumId, title, description, filePart)) return;
        
			//Inputs were valid, proceed to save image to the filesystem
			ServletContext context = getServletContext();
			String uploadImagePath = context.getInitParameter("uploadImagePath");
			String uploadThumbnailPath = context.getInitParameter("uploadThumbnailPath");
			
			//Save image
			imageName = MediaManager.saveImageToSystem(filePart, request.getServletContext().getRealPath("/").concat(uploadImagePath));
			//Generate thumbnail
			File f = new File(request.getServletContext().getRealPath("/").concat(uploadImagePath).concat(imageName));
			MediaManager.generateThumbnailFromFile(f, imageName, request.getServletContext().getRealPath("/").concat(uploadThumbnailPath));
			
			String imagePath = context.getInitParameter("ImagePath").concat(imageName);
			String thumbnailPath = context.getInitParameter("ThumbnailPath").concat(imageName);
			
			//Add image to database
			saveImageToDatabase(albumId, uploader, imagePath, thumbnailPath, title, description, new Date());
			redirectToAlbum(request, response, albumId);
        
		}
		catch (SQLException e) {
			deleteImageFiles(request, response, imageName);
			e.printStackTrace(); // for debugging
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database access failed");
		}
		
	}
	
	
	
	//HELPER METHODS
	private int validateAndRetrieveAlbumId(HttpServletRequest request, HttpServletResponse response, int userId) throws IOException, SQLException {
        String albumIdString = request.getParameter("albumId");
        if (!InputSanitizer.isValidId(albumIdString)) {
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid parameter albumId");
            return -1;
        }

        int albumId = Integer.parseInt(albumIdString);
        AlbumDAO albumDao = new AlbumDAO(conn);
        
        //Check if album exists and if user is the owner
        if(!albumDao.albumExists(albumId)) {
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No album found with provided id.");
        	return -1;
        }
        if (!albumDao.albumBelongsToUser(albumId, userId)) {
        	response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Album does not belong to user.");
            return -1;
        }

        return albumId;
    }
	
	private boolean validateInputs(HttpServletRequest request, HttpServletResponse response, int albumId, String title, String description, Part filePart) throws IOException, SQLException {
		String error = "";
		
        if (!InputSanitizer.isValidTitle(title)) {
            error = "Missing or malformed Image Title.";
            redirectToAlbumWithError(request, response, albumId, error, "", "");
            return false;
        }

        if (!InputSanitizer.isValidImageDescription(description)) {
            error = "Malformed Image description.";
            redirectToAlbumWithError(request, response, albumId, error, title, "");
            return false;
        }

        if (!InputSanitizer.isValidImageFile(filePart)) {
            error = "Malformed Image file.";
            redirectToAlbumWithError(request, response, albumId, error, title, description);
            return false;
        }

        return true;
    }
	
	private int validateAndRetrieveAlbumPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
	     HttpSession s = request.getSession();
	     //Checking for null pointer
	     Integer albumPageInteger = (Integer) s.getAttribute("albumPage");
	     if(albumPageInteger == null || albumPageInteger.intValue() <= 0) {
	         return 1;
	     }
	     
	     int albumPage = albumPageInteger;
	     return albumPage;
	}
	
	private void redirectToAlbumWithError(HttpServletRequest request, HttpServletResponse response, int albumId, String error, String title, String description) throws IOException {
        
		int albumPage = validateAndRetrieveAlbumPage(request, response);
		String paramString = String.format("?error=%s&title=%s&description=%s&albumId=%d&albumPage=%d",
                                           error, title, description, albumId, albumPage);
        String albumPath = request.getServletContext().getContextPath() + "/Album" + paramString;
        response.sendRedirect(albumPath);
    }
	
	private void deleteImageFiles(HttpServletRequest request, HttpServletResponse response, String imageName) {
		ServletContext context = getServletContext();
		String uploadImagePath = context.getInitParameter("uploadImagePath");
		String uploadThumbnailPath = context.getInitParameter("uploadThumbnailPath");
		
		File pictureFile = new File(request.getServletContext().getRealPath("/").concat(uploadImagePath).concat(imageName));
		File thumbNailFile = new File(request.getServletContext().getRealPath("/").concat(uploadThumbnailPath).concat(imageName));
		
		if(!pictureFile.delete()) {
			System.out.println("Couldn't deleted the image: " + pictureFile.getName());
			System.out.println("Full file path: " + request.getServletContext().getRealPath("/").concat(uploadImagePath).concat(imageName));
		}
		if(!thumbNailFile.delete()) {
			System.out.println("Couldn't deleted the thumbnail: " + pictureFile.getName());
			System.out.println("Full file path: " + request.getServletContext().getRealPath("/").concat(uploadThumbnailPath).concat(imageName));
		}
	}
	
	private void saveImageToDatabase(int albumId, int uploader, String imagePath, String thumbnailPath, String title, String description, Date uploadDate) throws SQLException {
        PictureDAO pictureDao = new PictureDAO(conn);
        pictureDao.createPicture(albumId, uploader, imagePath, thumbnailPath, title, description, uploadDate);
    }

    private void redirectToAlbum(HttpServletRequest request, HttpServletResponse response, int albumId) throws IOException {
        String albumPath = request.getServletContext().getContextPath() + "/Album?albumId=" + albumId;
        response.sendRedirect(albumPath);
    }
}




