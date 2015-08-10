/**
 * 
 */
package com.srp.trading.plugin;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
/**
 * Supported exchanges
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public enum DataExchange {
    MtGox ("mtgox"),
    BtcChina ("btcchina"),
    BTCe ("btce"),
    Bitstamp ("bitstamp");

    private final String name;       

    private DataExchange(String s) {
        name = s;
    }

    public boolean equalsName(String otherName){
        return (otherName == null)? false:name.equals(otherName);
    }

    public String toString(){
       return name;
    }
    
    /**
     * Create the enumeration from a string
     * @param str
     * @return
     */
    public static DataExchange fromString(String str) {
    	DataExchange de = null;
    	switch (str.toLowerCase()) {
    	case "bitstamp":
			de = DataExchange.Bitstamp;
			break;
		case "btce":
			de = DataExchange.BTCe;
			break;
		case "btcchina":
			de = DataExchange.BtcChina;
			break;
		case "mtgox":
			de = DataExchange.MtGox;
			break;
		default:
			de = null;
			break;
		}
    	return de;
    }
}
