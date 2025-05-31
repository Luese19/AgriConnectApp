// fix-order-dates.js
const admin = require('firebase-admin');
admin.initializeApp({ credential: admin.credential.applicationDefault() });
const db = admin.firestore();

async function fixOrderDates() {
  const orders = await db.collection('orders').get();
  for (const doc of orders.docs) {
    const data = doc.data();
    if (data.orderDate && typeof data.orderDate !== 'string') {
      // Convert Firestore Timestamp or object to ISO string
      let newDate;
      if (data.orderDate.toDate) {
        newDate = data.orderDate.toDate().toISOString();
      } else if (data.orderDate.seconds) {
        newDate = new Date(data.orderDate.seconds * 1000).toISOString();
      } else {
        newDate = new Date().toISOString();
      }
      await doc.ref.update({ orderDate: newDate });
      console.log(`Fixed orderDate for doc ${doc.id}: ${newDate}`);
    }
  }
  console.log('Done!');
}
fixOrderDates();
