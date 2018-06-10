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

	//const status = context.params.status;
	//console.log ("the user status is:", status);
	//const stat = event.data.val().status;

exports.sendNotification = functions.database.ref('/location/{uid}/status').onUpdate((change,context)=> {
	const status = change.after.val();
	const uid = context.params.uid;
//	console.log ('doslo je do promjene', uid);
	console.log ('doslo je do promjene', status);
	const deviceToken = admin.database().ref(`/Users/${uid}/deviceToken`).once('value');
    if(status===2){
						return deviceToken.then(result =>{
							const token_id = result.val();
								const payload = {
									notification:{
										title: "Prihvacena voznja",
										body: "klik za detalje"
									}
								};
							return admin.messaging().sendToDevice(token_id,payload).then (response => {
								return console.log('this is notif feature');
					});

				});
	}
	if(status===1){
						return deviceToken.then(result =>{
							const token_id = result.val();
								const payload = {
									notification:{
										title: "Odbijena voznja",
										body: "klik za detalje"
									}
								};
							return admin.messaging().sendToDevice(token_id,payload).then (response => {
								return console.log('this is notif feature');
					});

				});
	}
});

exports.sendNotificationVozacu = functions.database.ref('/drives/{uid}/status').onUpdate((change,context)=> {
	const status2 = change.after.val();
	const uid2 = context.params.uid;
//	console.log ('doslo je do promjene', uid);
	console.log ('doslo je do promjene', status2);
	const deviceToken = admin.database().ref(`/Users/${uid2}/deviceToken`).once('value');
    if(status2===4){
						return deviceToken.then(result =>{
							const token_id = result.val();
								const payload = {
									notification:{
										title: "Imas drustvo",
										body: "klik za detalje"
									}
								};
							return admin.messaging().sendToDevice(token_id,payload).then (response => {
								return console.log('this is notif feature');
					});

				});
	}
	if(status2===3){
						return deviceToken.then(result =>{
							const token_id = result.val();
								const payload = {
									notification:{
										title: "Nazalost, netko je odbio voznju",
										body: "klik za detalje"
									}
								};
							return admin.messaging().sendToDevice(token_id,payload).then (response => {
								return console.log('this is notif feature');
					});

				});
	}
});