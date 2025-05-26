// Firebase Authentication handling for login/signup

// Wait for the DOM content to be fully loaded
document.addEventListener('DOMContentLoaded', function() {
    console.log('Auth.js loaded');
    
    // Reference to the signup form
    const signupForm = document.querySelector('.sign-up-container form');
    
    // Handle signup form submission
    if (signupForm) {
        console.log('Signup form found');
        signupForm.addEventListener('submit', function(e) {
            console.log('Signup form submitted');
            // Form validation is handled in the inline script
        });
    }
    
    // Reference to the login form
    const loginForm = document.querySelector('.sign-in-container form');
    
    // Handle login form submission 
    if (loginForm) {
        console.log('Login form found');
        loginForm.addEventListener('submit', function(e) {
            console.log('Login form submitted');
            // Allow the form to submit - the server will handle authentication
        });
    }
});
