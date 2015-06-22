The test is presenting two processors:

*Delegating processor* that simply delegates the posted in messages to frondend over websocket UPDATE notification.

*Cumulating processor* that processes messages by summing up all incoming currency amounts and is meant to track the revenue of the trading company. After a processing is done the frontend(s) are notified over websocket by UPDATE notification.

The concept of snaps is used to minimize the trafic between backend and frontend.

Used frameworks:

Backend - Jersey, Spring, Websocket (JSR365), Simple log facade
Frontend - jQuery