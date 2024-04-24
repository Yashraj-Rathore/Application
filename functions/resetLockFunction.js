const functions = require("firebase-functions");
const admin = require("firebase-admin");

// Ensure the Firebase Admin SDK is initialized only once
if (!admin.apps.length) {
  admin.initializeApp(); // Initialize the default app, if needed
}

let secondaryApp = admin.apps.find((app) => app.name === "secondary");
if (!secondaryApp) {
  secondaryApp = admin.initializeApp({
    databaseURL: "https://eng4k-capstone-server-main2.firebaseio.com/",
  }, "secondary");
}

exports.resetMLUpdateLockOnNewFile = functions.storage
    .bucket("eng4k-capstone-server-712")
    .object()
    .onFinalize(async (object) => {
      const targetBucket = "eng4k-capstone-server-712";

      // Check if the object is in the target bucket and is the target file
      if (object.bucket === targetBucket &&
      object.contentType === "image/jpeg") {
        console.log("Target file identified:", object.name);

        const lockRef = secondaryApp.database().ref("/ML_Update_Lock");

        try {
        // Reset the lock to false
          await lockRef.set(false);
          console.log("ML_Update_Lock reset to false due to new txt file");
        } catch (error) {
          console.error("Error resetting ML_Update_Lock:", error);
        }
      } else {
        console.log("File does not match target criteria. Skipping...");
      }
    });
