# bitcointrader
This trading bot platform has a client/server architecture and is written in JAVA using the spring framework. 
It uses the bitcoin XChange library to access the exchange.

Multiple bots can be used at the same time and are loaded/unload dynamically as plugins.

### Libraries
The XChange version used at this time is quite old and needs to be updated.

It also uses Spring Framework and Hibernate to store the tradings. The DB used in the included plugins is SQLite.
Spring Roo is used to scaffold the classes.
 

### Clients
There is a command line client and a swing client.

### Plugins

The included plugins are basic scalping bots with different settings.

Parameters can be configured independently for each bot and exchange. Bots can also be started/stopped independently and load/unload new plugins without stopping the server.