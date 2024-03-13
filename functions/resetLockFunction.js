const functions = require("firebase-functions");
const admin = require("firebase-admin");

exports.resetMLUpdateLockOnNewFile =
  functions.storage.object().onFinalize(async (object) => {
  // Specify the target bucket
    const targetBucket = "app-proj4000.appspot.com";
    if (object.bucket === targetBucket && object.name ===
      "processed_results.txt") {
    // Path to the lock variable in Firebase Realtime Database
      const lockRef = admin.database().ref("/ML_Update_Lock");
      // Reset the lock to false
      await lockRef.set(false);
      console.log("ML_Update_Lock reset to false due to new txt file");
    }
  });
