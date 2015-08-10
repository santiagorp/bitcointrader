package com.srp.trading.plugin.scalper;

import java.io.File;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.srp.trading.common.Util;
import com.srp.trading.domain.DomainBase;
import com.srp.trading.domain.SessionWrapper;
import com.srp.trading.plugin.PluginConfig;
import com.xeiam.xchange.dto.marketdata.Ticker;
import com.xeiam.xchange.dto.marketdata.Trade;
import com.xeiam.xchange.mtgox.v2.service.streaming.SocketMessageFactory;
import com.xeiam.xchange.service.streaming.ExchangeEvent;
import com.xeiam.xchange.service.streaming.ExchangeStreamingConfiguration;
import com.xeiam.xchange.service.streaming.StreamingExchangeService;

public class StreamingRunnable implements Runnable {
	static final Logger logger = Logger.getLogger(StreamingRunnable.class.getName());

	private final StreamingExchangeService streamingExchangeService;
	private final Settings settings;
	private PluginConfig config;

	public StreamingRunnable(Settings settings, PluginConfig config) {
		this.settings = settings;
		this.config = config;
		streamingExchangeService = settings.getExchange().getStreamingExchangeService(settings.getExchangeStreamConfig());
	}

	@Override
	public void run() {
		// Create session
		String path = new File(config.getDataPath(), settings.getExchangeSite() + ".sqlite").toString();
		Session session = null;

		streamingExchangeService.connect();
		SocketMessageFactory socketMsgFactory = new SocketMessageFactory(settings.getExchange().getExchangeSpecification().getApiKey(), settings.getExchange()
				.getExchangeSpecification().getSecretKey());
		long threadId = Thread.currentThread().getId();
		try {
			settings.updateTicker();
			session = DomainBase.createSession(path);
			SessionWrapper sw = new SessionWrapper(session);
			DomainBase.getActiveSessions().put(threadId, sw);

			while (true) {
				ExchangeEvent exchangeEvent = streamingExchangeService.getNextEvent();
				switch (exchangeEvent.getEventType()) {

				case CONNECT:
					System.out.println("Connected!");
					streamingExchangeService.send(socketMsgFactory.subscribeWithChannel("ticker.BTCUSD"));
					System.out.println("Subscribed to ticker channel.");
					streamingExchangeService.send(socketMsgFactory.subscribeWithChannel("trade.BTC"));
					System.out.println("Subscribed to trade channel");
					break;

				case TICKER:
					Ticker ticker = (Ticker) exchangeEvent.getPayload();
					if (ticker.getTradableIdentifier().equals(settings.getTradableIdentifier())) {
						settings.setLastTicker(ticker);
						String msg = "Ticker: " + ticker.toString();
						logger.debug(msg);
					}
					break;
				case TRADE:
					Trade trade = (Trade) exchangeEvent.getPayload();
					if (trade.getTradableIdentifier().equals(settings.getTradableIdentifier())) {
						String msg = "Trade: " + trade.toString();
						logger.debug(msg);

						ScalpLogic logic = new ScalpLogic(settings);
						logic.execute(true);
					}
					break;
				default:
					break;
				}

			}
		} catch (InterruptedException e) {
			String msg = "Stopped scalping!";
			Util.printOutput(logger, msg);
			logger.info(e);
			return;
		} catch (JsonProcessingException e) {
			System.out.println("JSON ERROR!!!");
			logger.info(e);
		} catch (Exception e) {
			String msg = "Error in runnable";
			Util.printOutput(logger, msg);
			logger.info(e);
		}
		if (session != null) {
			session.close();
		}
		if (DomainBase.getActiveSessions().containsKey(threadId)) {
			DomainBase.getActiveSessions().remove(threadId);
		}
	}
}
