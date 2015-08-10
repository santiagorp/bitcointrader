-- TABLES
CREATE TABLE TBLSYMBOL
(
    symbolId    INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    name        VARCHAR
);

CREATE TABLE TBLORDER
(
    orderId     INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    symbolId    INTEGER,
    ref         VARCHAR,
    orderType   INTEGER,
    price       VARCHAR,
    amount      VARCHAR,
    FOREIGN KEY(symbolId) REFERENCES tblSymbol(symbolId)
);

CREATE TABLE TBLOPERATION
(
	operationId        INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
	askId 		       INTEGER NOT NULL,
	bidId 		       INTEGER NOT NULL,
    operationType      INTEGER NOT NULL,
    symbolId           INTEGER,
    FOREIGN KEY(askId) REFERENCES tblOrder(orderId),
    FOREIGN KEY(bidId) REFERENCES tblOrder(orderId),
    FOREIGN KEY(symbolId) REFERENCES tblSymbol(symbolId)
);

-- History of the order
-- Status enum values:
-- 0: Unknown
-- 1: Pending
-- 2: Finished
-- 3: Cancelled
CREATE TABLE TBLORDERHISTORY
(
	orderHistoryId	    INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    orderId 		    INTEGER NOT NULL,
	time				DATETIME,
	status 				INTEGER,
	FOREIGN KEY(orderId) REFERENCES tblOrder(orderId)
);

-- VIEWS

-- Last status of orders
CREATE VIEW VWLASTORDERSTATUS AS
SELECT
    orderId,
    status,
    max(time) as time
FROM
    TBlORDERHISTORY
GROUP BY orderId;


-- Operations summary
CREATE VIEW VWOPERATIONSUMMARY as
SELECT
    op.operationId,
    a.price as ask,
    a.amount as askAmount,
    ah.status as askStatus,
    b.price as bid,
    b.amount as bidAmount,
    bh.status as bidStatus,
    s.name as symbol
FROM
    tblOperation op
JOIN
    tblSymbol s on op.symbolId = s.symbolId,
    tblOrder a on op.askId = a.orderId,
    tblOrder b on op.bidId = b.orderId,
    vwLastOrderStatus ah on ah.orderId = op.askId,
    vwLastOrderStatus bh on bh.orderId = op.bidId;

-- Order summary
CREATE VIEW VWORDERSUMMARY AS
SELECT
    o.orderId,
    o.orderType,
    o.price,
    o.amount,
    datetime(cd.time/ 1000, 'unixepoch', 'localtime') as created,
    datetime(oh.time/ 1000, 'unixepoch', 'localtime') as finished,
    ROUND((oh.time/1000 - cd.time/1000)  / 60.0, 2) as minutes
FROM
    TBLORDER o,
    (SELECT orderId, min(time) as time FROM TBlORDERHISTORY GROUP BY orderId) cd,
    VWLASTORDERSTATUS oh
WHERE
    o.orderId = oh.orderId
    AND oh.status = 2
    AND o.orderId = cd.orderId;

-- FILL BASIC DATA
INSERT INTO TBLSYMBOL(name) VALUES('BTCUSD');
INSERT INTO TBLSYMBOL(name) VALUES('BTCEUR');
INSERT INTO TBLSYMBOL(name) VALUES('BTCCNY');
INSERT INTO TBLSYMBOL(name) VALUES('LTCBTC');
INSERT INTO TBLSYMBOL(name) VALUES('LTCUSD');
