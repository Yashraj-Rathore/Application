/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

/**
*const {onRequest} = require("firebase-functions/v2/https");
*const logger = require("firebase-functions/logger");
 *const {onObjectFinalized} = require("firebase-functions/v2/storage");
*const {initializeApp} = require("firebase-admin/app");
*const {getStorage} = require("firebase-admin/storage");
*const path = require("path");
*const sharp = require("sharp");
*/
const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();
exports.sendNotificationOnImageUpload = functions.storage.object().onFinalize(
    async (object) => {
      // Check if the file is uploaded to the "images" folder and is a JPEG
      if (object.contentType === "image/jpeg") {
        // Prepare the message for FCM
        const message = {
          notification: {
            title: "New Image Uploaded",
            body: "A new image has been added to the gallery:"+ object.name,
          },
          // Assuming all devices subscribe to this topic
          topic: "allDevices",
        };

        // Send a message to devices subscribed to the specified topic
        try {
          const response = await admin.messaging().send(message);
          console.log("Successfully sent message:", response);
        } catch (error) {
          console.error("Error sending message:", error);
        }
      }
    });
// Create and deploy your first functions
// https://firebase.google.com/docs/functions/get-started

// exports.helloWorld = onRequest((request, response) => {
//   logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });
