// Firebase Configuration
// This file configures the Firebase client SDK for the application

// Initialize Firebase with your project configuration
const firebaseConfig = {
  apiKey: "AIzaSyAqWeuU1FjDBH6UWBZ58u2lU5GvY2P4TqA",
  authDomain: "agriconnect-lite-wbo1k.firebaseapp.com",
  projectId: "agriconnect-lite-wbo1k",
  storageBucket: "agriconnect-lite-wbo1k.firebasestorage.app",
  messagingSenderId: "112746459207",
  appId: "1:112746459207:web:44b9aeb8af8f1bb7238162"
};

// Initialize Firebase app
firebase.initializeApp(firebaseConfig);
// Create references to Firebase services
const db = firebase.firestore();
const storage = firebase.storage();
const auth = firebase.auth();

console.log("Firebase initialized successfully");
