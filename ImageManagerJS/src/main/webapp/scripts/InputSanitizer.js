/**
 * Contains logic for input sanitation
 */

(function(){
	// Constants for regex patterns
	const uppercaseRegex = /.*[A-Z].*/; // At least one uppercase letter
	const lowercaseRegex = /.*[a-z].*/; // At least one lowercase letter
	const numberRegex = /.*[0-9].*/;  // At least one numeric digit
	const specialCharRegex = /.*[\x21\x40\x23\x24\x25\x5E\x26\x2A\x28\x29\x5F\x2B\x2D\x3D\x5B\x5D\x7B\x7D\x7C\x3B\x3A\x27\x22\x2C\x2E\x3C\x3E\x3F\x2F\x5C\x7E].*/; // At least one special character
	const spaceRegex = /.*[\x20].*/;
	
	
	// Email validation
	window.isValidEmail = function(email) {
	  if (!email || email.length > 44) return false;

	  const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
	  const invalidChars = /[^a-zA-Z0-9@._%+-]/;

	  return (
	    emailRegex.test(email) &&
	    !spaceRegex.test(email) &&
	    !invalidChars.test(email)
	  );
	}
	
	
	// Username validation
	window.isValidUsername = function(username) {
	  if (!username || username.length < 3 || username.length > 44) return false;

	  const usernameRegex = /^[a-zA-Z0-9]+$/;

	  return usernameRegex.test(username) && !spaceRegex.test(username);
	}
	
	
	
	// Password validation
	window.isValidPassword = function(password) {
	  if (!password || password.length < 8 || password.length > 44) return false;

	  return (
	    uppercaseRegex.test(password) &&
	    lowercaseRegex.test(password) &&
	    numberRegex.test(password) &&
	    specialCharRegex.test(password) &&
	    !spaceRegex.test(password)
	  );
	}
	
	
	
	// Title validation
	window.isValidTitle = function(title) {
	  if (!title || title.length < 3 || title.length > 127) return false;

	  const titleRegex = /^[a-zA-Z0-9\x21\x40\x23\x24\x25\x5E\x26\x2A\x28\x29\x5F\x2B\x2D\x3D\x5B\x5D\x7B\x7D\x7C\x3B\x3A\x27\x22\x2C\x2E\x3C\x3E\x3F\x2F\x5C\x7E\x20]+$/;

	  return titleRegex.test(title);
	}
	
	
	
	// Image description validation
	window.isValidImageDescription = function(description){
	  if (!description) return true;
	  if (description.length > 1023) return false;

	  const descriptionRegex = /^[a-zA-Z0-9\x20\xB0\x21\x40\x23\x24\x25\x5E\x26\x2A\x28\x29\x5F\x2B\x2D\x3D\x5B\x5D\x7B\x7D\x7C\x3B\x3A\x27\x22\x2C\x2E\x3C\x3E\x3F\x2F\x5C\x7E\r\n]+$/;

	  return descriptionRegex.test(description);
	}
	
	
	
	// Date validation
	window.isValidDate = function(date){
	  if (!date || !date.trim()) return false;

	  const datePattern = /^\d{4}-\d{2}-\d{2}$/;
	  if (!datePattern.test(date)) return false;

	  const parsedDate = new Date(date);
	  return !isNaN(parsedDate.getTime()) && parsedDate.toISOString().startsWith(date);
	}
	
	
	
	// ID validation
	window.isValidId = function(id) {
	  if (!id || id.length > 10) return false;

	  const idRegex = /^[0-9]+$/;
	  return idRegex.test(id) && Number.isSafeInteger(Number(id));
	}
	
	
	// Comment body validation
	window.isValidCommentBody = function(commentBody) {
	  if (!commentBody || commentBody.length > 1023) return false;

	  const commentBodyRegex = /^[a-zA-Z0-9\x20\xB0\x21\x40\x23\x24\x25\x5E\x26\x2A\x28\x29\x5F\x2B\x2D\x3D\x5B\x5D\x7B\x7D\x7C\x3B\x3A\x27\x22\x2C\x2E\x3C\x3E\x3F\x2F\x5C\x7E\r\n]+$/;

	  return commentBodyRegex.test(commentBody);
	}
	
})();














