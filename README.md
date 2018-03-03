# BT-Headset-Controller
An Android application that demonstrates I/O with Bluetooth headsets through the hardware buttons, vibration motor, and speakers.  Input is handled by sniffing the btsnoop_hci.log file and output is taken care of by sending AT commands over a Bluetooth socket.

The headset used is a [Rymemo Bluetooth headset](https://www.amazon.com/Wireless-Bluetooth-Running-Neckband-Cellphone/dp/B06XVYV2NX/ref=pd_sbs_107_5?_encoding=UTF8&pd_rd_i=B06XVYV2NX&pd_rd_r=6KB83801Z7GFFNTFS6HD&pd_rd_w=FIbYU&pd_rd_wg=kHiFy&psc=1&refRID=6KB83801Z7GFFNTFS6HD) which features 6 hardware buttons (play/pause, call, forward, backward, volume up, volume down), a vibration motor for notifications, and earbuds.

On the Android device, developer options and Bluetooth HCI snoop log must be enabled as described [here](https://www.amazon.com/Wireless-Bluetooth-Running-Neckband-Cellphone/dp/B06XVYV2NX/ref=pd_sbs_107_5?_encoding=UTF8&pd_rd_i=B06XVYV2NX&pd_rd_r=6KB83801Z7GFFNTFS6HD&pd_rd_w=FIbYU&pd_rd_wg=kHiFy&psc=1&refRID=6KB83801Z7GFFNTFS6HD).  Since the snoop file is a binary log file, Wireshark was used to inspect it, and the following packets of relevance (i.e. related to the hardware buttons) were found:
 
| Button   | Action   | ACL Data |    | Data... | ...Length |    |    |    |    |    |    |    |    |    |    |    | Operation ID |    |
|----------|----------|----|----|----|----|----|----|----|----|----|----|----|----|----|----|----|----|----|
| Backward | Pushed   | 02 | 04 | 20 | 0C | 00 | 08 | 00 | 40 | 00 | 30 | 11 | 0E | 00 | 48 | 7C | 4C | 00 |
| Backward | Released | 02 | 04 | 20 | 0C | 00 | 08 | 00 | 40 | 00 | 40 | 11 | 0E | 00 | 48 | 7C | CC | 00 |
| Forward  | Pushed   | 02 | 04 | 20 | 0C | 00 | 08 | 00 | 40 | 00 | 10 | 11 | 0E | 00 | 48 | 7C | 4B | 00 |
| Forward  | Released | 02 | 04 | 20 | 0C | 00 | 08 | 00 | 40 | 00 | 20 | 11 | 0E | 00 | 48 | 7C | CB | 00 |
| Play     | Pushed   | 02 | 04 | 20 | 0C | 00 | 08 | 00 | 40 | 00 | 30 | 11 | 0E | 00 | 48 | 7C | 44 | 00 |
| Play     | Released | 02 | 04 | 20 | 0C | 00 | 08 | 00 | 40 | 00 | 40 | 11 | 0E | 00 | 48 | 7C | C4 | 00 |

Regex patterns were used to detect these sequences in the snoop file.  A similar process was taken to find the AT commands used to trigger the vibration motor and sound:

| Output    | Action | Command |   | Parameter |
|-----------|--------|---------|---|-----------|
| Vibration | Start  | CIEV    | 2 | 1         |
| Vibration | End    | CIEV    | 2 | 0         |

These AT commands are then sent over the socket as bytes.
