# Borrow

Have you ever waited in line at the cashier for 15 minutes only to realise that you have no cash? We have, so we made an app for that.

Borrow lets you securely borrow and lend money from/to your friends. The transactions are created collaboratively and signed by both parties, so nobody can fake transactions. Both sides keep copies of the same blockchain locally, so no centralised server is needed.

For the actual transaction, the app uses a novel QR code protocol to exchange data between phones. While facing each other's screen, the phones take turns displaying QR codes and reading from the other side with the front-facing camera. This ensures that no internet connection is required to perform the transaction and prevents compatibility problems across brands.

This projects uses:
* [QRCodeReaderView](https://github.com/dlazaro66/QRCodeReaderView) for reading QR codes;
