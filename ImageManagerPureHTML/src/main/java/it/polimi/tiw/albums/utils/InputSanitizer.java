package it.polimi.tiw.albums.utils;

import java.util.regex.Pattern;

public final class InputSanitizer {
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
        
        // !@#$%^&*()_+-=[]{}|;:'",.<>?/\~
        // \x21\x40\x23\x24\x25\x5E\x26\x2A\x28\x29\x5F\x2B\x2D\x3D\x5B\x5D\x7B\x7D\x7C\x3B\x3A\x27\x22\x2C\x2E\x3C\x3E\x3F\x2F\x5C\x7E

        String uppercaseRegex = ".*[A-Z].*"; // At least one uppercase letter
        String lowercaseRegex = ".*[a-z].*"; // At least one lowercase letter
        String numberRegex = ".*[0-9].*";    // At least one numeric digit
        String specialCharRegex = ".*[\\x21\\x40\\x23\\x24\\x25\\x5E\\x26\\x2A\\x28\\x29\\x5F\\x2B\\x2D\\x3D\\x5B\\x5D\\x7B\\x7D\\x7C\\x3B\\x3A\\x27\\x22\\x2C\\x2E\\x3C\\x3E\\x3F\\x2F\\x5C\\x7E].*"; // At least one special character

        boolean hasUppercase = Pattern.matches(uppercaseRegex, password);
        boolean hasLowercase = Pattern.matches(lowercaseRegex, password);
        boolean hasNumber = Pattern.matches(numberRegex, password);
        boolean hasSpecialChar = Pattern.matches(specialCharRegex, password);

        // Password is valid only if all conditions are met
        return hasUppercase && hasLowercase && hasNumber && hasSpecialChar;
	}
}