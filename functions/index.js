const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();
const {resetMLUpdateLockOnNewFile} = require("./resetLockFunction");
exports.resetMLUpdateLockOnNewFile = resetMLUpdateLockOnNewFile;

exports.sendNotificationOnImageUpload = functions.storage
    .bucket("eng4k-capstone-server-712")
    .object()
    .onFinalize(async (object) => {
      const targetBucket = "eng4k-capstone-server-image-post";
      if (object.bucket === targetBucket &&
            object.contentType === "image/jpeg") {
        const timestamp = new Date().toISOString();
        const message = {
          data: {
            type: "imageUpload",
            title: "New Image Uploaded",
            message: "New image has been added to the gallery: " + object.name,
            timestamp: timestamp,
          },
          topic: "allDevices",
        };
        try {
          const response = await admin.messaging().send(message);
          console.log("Successfully sent message:", response);
        } catch (error) {
          console.error("Error sending message:", error);
        }
      }
    });
const message1 = "New code pin has been set, input code from LCD Screen";
exports.sendNotificationOnNewCodePin = functions.database
    .instance("eng4k-capstone-server-main2")
    .ref("/codePin")
    .onWrite(async (change, context) => {
      const before = change.before.val();
      const after = change.after.val();

      if (before !== after) {
        const timestamp = new Date().toISOString();
        const message = {
          data: {
            type: "codePin",
            title: "New Code Pin Set. Continue to Home to Verify.",
            message: message1,
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
exports.checkIfFail = functions.database
    .instance("eng4k-capstone-server-main2")
    .ref("/iffail")
    .onWrite(async (change, context) => {
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

exports.clearGalleryImages = functions.database
    .instance("eng4k-capstone-server-main2")
    .ref("/galleryClear")
    .onWrite(async (change, context) => {
      const before = change.before.val();
      const after = change.after.val();

      if (before !== after && after) {
        const bucket = admin.storage()
            .bucket("eng4k-capstone-server-712");

        try {
          const [files] = await bucket.getFiles();

          await Promise.all(files.map((file) => file.delete()));

          console.log("All images cleared from gallery.");
          return null;
        } catch (error) {
          return null;
        }
      }
      return null;
    });

const nonDefaultDb = admin.initializeApp({
  databaseURL: "https://eng4k-capstone-server-main2.firebaseio.com/",
}, "nonDefaultDb").database();

exports.resetEnrollEndToDefault = functions.database
    .instance("eng4k-capstone-server-main2")
    .ref("/enrollEnd")
    .onWrite(async (change, context) => {
      const before = change.before.val();
      const after = change.after.val();

      // Check if the value has changed to true
      if (before !== after && after === true) {
        try {
          // Wait for 5 seconds before resetting the node
          await new Promise((resolve) => setTimeout(resolve, 2000));

          // Reset the `/enrollEnd` node back to false
          await nonDefaultDb.ref("/enrollEnd").set(false);
          console.log("`/enrollEnd` node reset to false after 2 seconds.");

          return null;
        } catch (error) {
          console.error("Error resetting `/enrollEnd` node:", error);
          return null;
        }
      }
      return null;
    });

// enrollTrigger
exports.resetEnrollTriggerToDefault = functions.database
    .instance("eng4k-capstone-server-main2")
    .ref("/enrollTrigger")
    .onWrite(async (change, context) => {
      const before = change.before.val();
      const after = change.after.val();

      if (before !== after && after === true) {
        try {
          await new Promise((resolve) => setTimeout(resolve, 2000));
          await nonDefaultDb.ref("/enrollTrigger").set(false);
          console.log("`/enrollTrigger` node reset to false after 2 seconds.");
          return null;
        } catch (error) {
          console.error("Error resetting `/enrollTrigger` node:", error);
          return null;
        }
      }
      return null;
    });

// EnrollInit
exports.resetEnrollInitToDefault = functions.database
    .instance("eng4k-capstone-server-main2")
    .ref("/enrollInit")
    .onWrite(async (change, context) => {
      const before = change.before.val();
      const after = change.after.val();
      const timestamp = new Date().toISOString();

      // Check if the value has changed to true
      if (before !== after && after === true) {
        try {
          // Send the notification immediately when enrollEnd becomes true
          const message = {
            data: {
              type: "enrollInit",
              title: "Enrollment Notification",
              message: "Press button on MCU to begin Enrollment.",
              timestamp: timestamp,
            },
            topic: "allDevices",
          };

          await admin.messaging().send(message);
          console.log("Successfully sent enrollment notification.");

          // Wait for 2 seconds before resetting the node
          await new Promise((resolve) => setTimeout(resolve, 2000));

          // Reset the `/enrollEnd` node back to false
          await nonDefaultDb.ref("/enrollInit").set(false);
          console.log("`/enrollInit` node reset to false after 2 seconds.");

          return null;
        } catch (error) {
          console.error("Error in function execution:", error);
          return null;
        }
      }
      return null;
    });


// EnrollInit
exports.resetEnrollInitToDefault = functions.database
    .instance("eng4k-capstone-server-main2")
    .ref("/enrollInit")
    .onWrite(async (change, context) => {
      const before = change.before.val();
      const after = change.after.val();
      const timestamp = new Date().toISOString();

      // Check if the value has changed to true
      if (before !== after && after === true) {
        try {
          // Send the notification immediately when enrollEnd becomes true
          const message = {
            data: {
              type: "enrollInit",
              title: "Enrollment Notification",
              message: "Press button on MCU to begin Enrollment.",
              timestamp: timestamp,
            },
            topic: "allDevices",
          };

          await admin.messaging().send(message);
          console.log("Successfully sent enrollment notification.");

          // Wait for 2 seconds before resetting the node
          await new Promise((resolve) => setTimeout(resolve, 2000));

          // Reset the `/enrollEnd` node back to false
          await nonDefaultDb.ref("/enrollInit").set(false);
          console.log("`/enrollInit` node reset to false after 2 seconds.");

          return null;
        } catch (error) {
          console.error("Error in function execution:", error);
          return null;
        }
      }
      return null;
    });

