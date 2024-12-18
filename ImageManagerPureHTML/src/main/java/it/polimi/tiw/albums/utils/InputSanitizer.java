package it.polimi.tiw.albums.utils;

import java.util.regex.Pattern;

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
	
	
	public static boolean isValidTitle(String albumTitle) {
		if (albumTitle == null || albumTitle.isEmpty()) {
            return false;
        }
		
		if (albumTitle.length() < 3 || albumTitle.length() > 127) {
            return false;
        }
		
		//Album title can contain alpha numerics, spaces and select special characters
		String albumTitleRegex = "^[a-zA-Z0-9\\x21\\x40\\x23\\x24\\x25\\x5E\\x26\\x2A\\x28\\x29\\x5F\\x2B\\x2D\\x3D\\x5B\\x5D\\x7B\\x7D\\x7C\\x3B\\x3A\\x27\\x22\\x2C\\x2E\\x3C\\x3E\\x3F\\x2F\\x5C\\x7E\\x20]+$";
		Pattern pattern = Pattern.compile(albumTitleRegex);

		// Check if the album Title matches the regex
		if (!pattern.matcher(albumTitle).matches()) {
			// Contains invalid characters
			return false;
		}

		// albumTitle is valid
		return true;
	}
}