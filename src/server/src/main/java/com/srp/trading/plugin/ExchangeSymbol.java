/**
 * 
 */
package com.srp.trading.plugin;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
/***
 * Supported exchange symbols in bitcoinwisdom
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public enum ExchangeSymbol {
    BTCUSD ("btcusd"),
    BTCCNY ("btccny"),
    BTCEUR ("btceur"),
    LTCUSD ("ltceur"),
    LTCBTC ("ltcbtc");   
    
    private final String name;       

    private ExchangeSymbol(String s) {
        name = s;
    }

    public boolean equalsName(String otherName){
        return (otherName == null)? false:name.equals(otherName);
    }

    public String toString(){
       return name;
    }
    
    /**
     * Create the enumeration from the suplied string
     * @param str
     * @return
     */
    public static ExchangeSymbol fromString(String str) {
    	ExchangeSymbol symbol = null;
    	switch (str.toLowerCase()) {	    	
		case "btcusd":
			symbol = ExchangeSymbol.BTCUSD;
			break;
		case "btceur":
			symbol = ExchangeSymbol.BTCEUR;
			break;
		case "btccny":
			symbol = ExchangeSymbol.BTCCNY;
			break;
		case "ltcbtc":
			symbol = ExchangeSymbol.LTCBTC;
			break;
		case "ltcusd":
			symbol = ExchangeSymbol.LTCUSD;
			break;
		default:
			symbol = null;
			break;
		}
    	return symbol;
    }
}