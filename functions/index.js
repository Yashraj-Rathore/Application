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
const {resetMLUpdateLockOnNewFile} = require("./resetLockFunction");
exports.resetMLUpdateLockOnNewFile = resetMLUpdateLockOnNewFile;
exports.sendNotificationOnImageUpload = functions.storage.object().onFinalize(
    async (object) => {
      const targetBucket = "app-proj4000.appspot.com";
      // Check if the file is uploaded to the "images" folder and is a JPEG
      if (object.bucket === targetBucket && object.contentType==="image/jpeg") {
        // Prepare the message for FCM
        const timestamp = new Date().toISOString();
        const message = {
          data: {
            type: "imageUpload",
            title: "New Image Uploaded",
            message: "New image has been added to the gallery: " + object.name,
            timestamp: timestamp,
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
exports.sendNotificationOnNewCodePin = functions.database.ref("/codePin").
    onWrite(async (change, context) => {
      const before = change.before.val();
      const after = change.after.val();

      if (before !== after) {
        const timestamp = new Date().toISOString();
        const message = {
          data: {
            type: "codePin",
            title: "New Code Pin Set. Continue to Home to Verify.",
            message: "A new code pin has been set: " + after,
            timestamp: timestamp,
          },
          topic: "allDevices",
        };

        try {
          const response = await admin.messaging().send(message);
          console.log("Successfully sent code pin message:", response);
        } catch (error) {
          console.error("Error sending code pin message:", error);
        }
      }
    });
exports.checkIfFail = functions.database.ref("/iffail").
    onWrite(async (change, context) => {
      const after = change.after.val();
      const timestamp = new Date().toISOString();

      if (after === true) {
        // Set a timestamp for the ifFail event
        await change.after.ref.parent.child("ifFailTimestamp").set(timestamp);

        // Wait for 1 second
        await new Promise((resolve) => setTimeout(resolve, 1000));

        // Re-check ifFail and the timestamp
        const ifFailSnapshot = await change.after.ref.once("value");
        const ifFailTimestampSnapshot = await
        change.after.ref.parent.child("ifFailTimestamp").once("value");

        if (ifFailSnapshot.val() === true &&
    ifFailTimestampSnapshot.val()=== timestamp) {
          // Proceed to send the notification
          const message = {
            data: {
              type: "iffail",
              title: "Unauthorized Access Detected!",
              message: "Unauthorized access in vehicle! Check Dashboard!",
              timestamp: timestamp,
            },
            topic: "allDevices",
          };

          try {
            const response = await admin.messaging().send(message);
            console.log("Successfully sent ifFail notification:", response);
          } catch (error) {
            console.error("Error sending ifFail notification:", error);
          }
        }
      }
    });


// Create and deploy your first functions
// https://firebase.google.com/docs/functions/get-started

// exports.helloWorld = onRequest((request, response) => {
//   logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });
