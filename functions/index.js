'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions

/*exports.helloWorld = functions.database.ref().onWrite(event=> {
 response.send("Hello from Firebase!");
  console.log("ciaooo");
});*/
exports.sendNotification = functions.database.ref('/location/{uid}/status').onUpdate((change,context)=> {
	const uid = context.params.uid;
	//const status = context.params.status;
	//console.log ("the user status is:", status);
	//const stat = event.data.val().status;
	console.log ('doslo je do promjene', uid);

	const deviceToken = admin.database().ref(`/Users/${uid}/deviceToken`).once('value');
	return deviceToken.then(result =>{

		const token_id = result.val();
		const payload ={
			notification:{
				title: "Prihvacena voznja",
				body: "klik za detalje"
			}
		};
		return admin.messaging().sendToDevice(token_id,payload).then (response => {
			return console.log('this is notif feature');
		});

	});
});