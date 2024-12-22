package it.polimi.tiw.albums.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tomcat.jakartaee.commons.compress.utils.FileNameUtils;

import jakarta.servlet.http.Part;

public final class InputSanitizer {
	//ATTRIBUTES
	private final static String uppercaseRegex = ".*[A-Z].*"; // At least one uppercase letter
	private final static String lowercaseRegex = ".*[a-z].*"; // At least one lowercase letter
	private final static String numberRegex = ".*[0-9].*";    // At least one numeric digit
	// !@#$%^&*()_+-=[]{}|;:'",.<>?/\~
    // \x21\x40\x23\x24\x25\x5E\x26\x2A\x28\x29\x5F\x2B\x2D\x3D\x5B\x5D\x7B\x7D\x7C\x3B\x3A\x27\x22\x2C\x2E\x3C\x3E\x3F\x2F\x5C\x7E
	private final static String specialCharRegex = ".*[\\x21\\x40\\x23\\x24\\x25\\x5E\\x26\\x2A\\x28\\x29\\x5F\\x2B\\x2D\\x3D\\x5B\\x5D\\x7B\\x7D\\x7C\\x3B\\x3A\\x27\\x22\\x2C\\x2E\\x3C\\x3E\\x3F\\x2F\\x5C\\x7E].*"; // At least one special character
	private final static String spaceRegex=".*[\\x20].*";
	
	
	// CONSTRUCTOR
	private InputSanitizer() {
	}

	public static boolean isVaildEmail(String email) {
		if (email == null || email.isEmpty()) {
			return false;
		}
		
		// Check if email exceeds maximum database allowed length
		if (email.length() > 44) {
			return false;
		}

		// Regex to validate email structure
		String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
		Pattern pattern = Pattern.compile(emailRegex);

		//Check if email contains spaces
		if(Pattern.matches(spaceRegex, email)) {
			return false;
		}
		
		// Check if the email matches the regex
		if (!pattern.matcher(email).matches()) {
			return false;
		}

		// Additional safety checks to avoid suspicious or strange characters
		String invalidChars = "[^a-zA-Z0-9@._%+-]";
		if (email.matches(".*" + invalidChars + ".*")) {
			return false;
		}

		// Email is valid
		return true;
	}

	
	
	public static boolean isValidUsername(String username) {
		if (username == null || username.isEmpty()) {
			return false;
		}

		// Check if username exceeds maximum database allowed length
		if (username.length() < 3 ||  username.length() > 44) {
			return false;
		}
		
		// Check if username contains spaces
		if (Pattern.matches(spaceRegex, username)) {
			return false;
		}

		// Regex to validate username structure (only alphanumerics allowed)
		String usernameRegex = "^[a-zA-Z0-9]+$";
		Pattern pattern = Pattern.compile(usernameRegex);

		// Check if the username matches the regex
		if (!pattern.matcher(username).matches()) {
			// Contains invalid characters
			return false;
		}

		// Username is valid
		return true;
	}
	
	
	public static boolean isValidPassword(String password) {
		if (password == null || password.isEmpty()) {
            return false;
        }

        if (password.length() < 8 || password.length() > 44) {
            return false;
        }
        
        boolean hasUppercase = Pattern.matches(uppercaseRegex, password);
        boolean hasLowercase = Pattern.matches(lowercaseRegex, password);
        boolean hasNumber = Pattern.matches(numberRegex, password);
        boolean hasSpecialChar = Pattern.matches(specialCharRegex, password);
        boolean doesntHaveSpace = !Pattern.matches(spaceRegex, password);

        // Password is valid only if all conditions are met
        return hasUppercase && hasLowercase && hasNumber && hasSpecialChar && doesntHaveSpace;
	}
	
	
	public static boolean isValidTitle(String title) {
		if (title == null || title.isEmpty()) {
            return false;
        }
		
		if (title.length() < 3 || title.length() > 127) {
            return false;
        }
		
		//Album title can contain alpha numerics, spaces and select special characters
		String albumTitleRegex = "^[a-zA-Z0-9\\x21\\x40\\x23\\x24\\x25\\x5E\\x26\\x2A\\x28\\x29\\x5F\\x2B\\x2D\\x3D\\x5B\\x5D\\x7B\\x7D\\x7C\\x3B\\x3A\\x27\\x22\\x2C\\x2E\\x3C\\x3E\\x3F\\x2F\\x5C\\x7E\\x20]+$";
		Pattern pattern = Pattern.compile(albumTitleRegex);

		// Check if the album Title matches the regex
		if (!pattern.matcher(title).matches()) {
			// Contains invalid characters
			return false;
		}

		// albumTitle is valid
		return true;
	}
	
	
	
	public static boolean isValidImageDescription(String description) {
		if (description == null || description.isEmpty()) {
			//Images can be without descriptions
            return true;
        }
		
		if (description.length() > 1023) {
            return false;
        }
		
		//Image description can contain alpha numerics, spaces and select special characters
		String albumTitleRegex = "^[a-zA-Z0-9\\x21\\x40\\x23\\x24\\x25\\x5E\\x26\\x2A\\x28\\x29\\x5F\\x2B\\x2D\\x3D\\x5B\\x5D\\x7B\\x7D\\x7C\\x3B\\x3A\\x27\\x22\\x2C\\x2E\\x3C\\x3E\\x3F\\x2F\\x5C\\x7E\\x20]+$";
		Pattern pattern = Pattern.compile(albumTitleRegex);

		// Check if the album Title matches the regex
		if (!pattern.matcher(description).matches()) {
			// Contains invalid characters
			return false;
		}

		// albumTitle is valid
		return true;
	}
	
	
	
	public static boolean isValidId(String id) {
		if (id == null || id.isEmpty()) {
            return false;
        }
		
		if (id.length() > 10) {
			//INT type for id is 32bits so max number is 2,147,483,647, 10 digits
            return false;
        }
		
		// Id can only contain numeric chars
		String idRegex = "^[0-9]+$";
		Pattern pattern = Pattern.compile(idRegex);

		if (!pattern.matcher(id).matches()) {
			// Contains invalid characters
			return false;
		}
		
		try {
			Integer.valueOf(id);
		}
		catch(NumberFormatException e){
			//string can't be converted to int so is not a valid id
			return false;
		}
		
		return true;
	}
	
	
	public static boolean isValidAlbumPage(String page) {
		if (page == null || page.isEmpty()) {
            return false;
        }
		
		if (page.length() > 10) {
            return false;
        }
		
		// Id can only contain numeric chars
		String idRegex = "^[0-9]+$";
		Pattern pattern = Pattern.compile(idRegex);

		if (!pattern.matcher(page).matches()) {
			// Contains invalid characters
			return false;
		}
		
		try {
			Integer.valueOf(page);
		}
		catch(NumberFormatException e){
			//string can't be converted to int so is not a valid id
			return false;
		}
		
		return true;
	}
	
	
	public static boolean isValidImageFile(Part filePart) {
		if(filePart == null) {
			return false;
		}
		
		String fileName = filePart.getSubmittedFileName();
		//Check if valid extension
		String extension = FileNameUtils.getExtension(fileName);
		if(!extension.equals("jpg") && !extension.equals("jpeg") && !extension.equals("webp") && !extension.equals("png")) {
			return false;
		}
		
		//Assert file type with apache tika
		String fileType ="";
		String fileSubType ="";
		try {
			TikaConfig tika = new TikaConfig();
			InputStream iStream = filePart.getInputStream();
			MediaType mimetype = tika.getDetector().detect(iStream, new Metadata());
			
			fileType = mimetype.getType();
			fileSubType = mimetype.getSubtype();
		}
		catch(IOException | TikaException e) {
			e.printStackTrace();
			System.out.println("Exception caught in InputSanitizer using apache tika");
			return false;
		}
		if(!fileType.equals("image")) {
			return false;
		}
		else if(!fileSubType.equals("jpg") && !fileSubType.equals("jpeg") && !fileSubType.equals("webp") && !fileSubType.equals("png")) {
			return false;
		}
		
		if(!extension.equals(fileSubType)) {
			//jpeg and jpg different writings but still the same extension
			if((extension.equals("jpeg") && fileSubType.equals("jpg")) || (extension.equals("jpg") && fileSubType.equals("jpeg")))
				return true;
			
			return false;
		}
		//Check for viruses in the file
		if(VirusScanner.containsVirus(filePart)) {
			return false;
		}
		
		return true;
	}
	
	
	public static boolean isValidCommentBody(String commentBody) {
		if (commentBody == null || commentBody.isEmpty()) {
			//Images can be without descriptions
            return false;
        }
		
		if (commentBody.length() > 1023) {
            return false;
        }
		
		//Image description can contain alpha numerics, spaces and select special characters
		String albumTitleRegex = "^[a-zA-Z0-9\\x21\\x40\\x23\\x24\\x25\\x5E\\x26\\x2A\\x28\\x29\\x5F\\x2B\\x2D\\x3D\\x5B\\x5D\\x7B\\x7D\\x7C\\x3B\\x3A\\x27\\x22\\x2C\\x2E\\x3C\\x3E\\x3F\\x2F\\x5C\\x7E\\x20]+$";
		Pattern pattern = Pattern.compile(albumTitleRegex);

		// Check if the album Title matches the regex
		if (!pattern.matcher(commentBody).matches()) {
			// Contains invalid characters
			return false;
		}

		// albumTitle is valid
		return true;
	}
}





