/**
 * 
 */
package com.srp.trading.domain;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.xeiam.xchange.dto.Order.OrderType;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = "/META-INF/spring/applicationContext-test.xml")
public class OperationUnitTest {
	@Autowired
	ApplicationContext applicationContext;

	public OperationUnitTest() {
		String testDBPath = new File(System.getProperty("user.dir"), "db").toString();
		String dbFile = new File(testDBPath, "domain_test.sqlite").toString();
		Session session = DomainBase.createSession(dbFile);
		long threadId = Thread.currentThread().getId();
		SessionWrapper sw = new SessionWrapper(session);		
		DomainBase.getActiveSessions().put(threadId, sw);
		Symbol s = Symbol.find("BTCEUR");
		sw.setSymbolId(s.getSymbolId());
	}
			
	/**
	 * Test method for
	 * {@link com.srp.trading.service.OperationService#Save(com.srp.trading.domain.Operation)}
	 * .
	 */
	@Test
	public final void testSave() {
		if (applicationContext.containsBean("logger")) {
			Logger logger = (Logger) applicationContext.getBean("logger");
			logger.info("Bean succesfully retrieved");
		}

		Operation op = createDummyOperation();
		boolean saved = op.SaveAll();

		assertTrue("Operation saved in the database", saved);
	}

	/**
	 * Create a dummy operation for testing purposes
	 * 
	 * @return
	 */
	public static Operation createDummyOperation() {
		BigDecimal askPrice = new BigDecimal("98.344");
		BigDecimal bidPrice = new BigDecimal("95.344");
		BigDecimal askAmount = new BigDecimal("1.0");
		BigDecimal bidAmount = new BigDecimal("1.0");
		
		Operation op = new Operation(OperationType.Scalping, askPrice, askAmount, bidPrice, bidAmount);
		
		return op;
	}

	/**
	 * Test method for
	 * {@link com.srp.trading.service.OperationService#find(java.lang.Long)}
	 * .
	 */
	@Test
	public final void testFindLong() {
		Operation dummyOp = createDummyOperation();
		if (dummyOp.SaveAll()) {
			int id = dummyOp.getOperationId();
			int bidId = dummyOp.getBid().getOrderId();
			int askId = dummyOp.getAsk().getOrderId();
			Operation op = Operation.find(id);
			Order bid = Order.find(bidId);
			Order ask = Order.find(askId);
			
			boolean found = op != null && bid != null && ask != null;			
			assertTrue("Operation and/or orders not found", found);
		} else {
			fail("Error saving dummy operation");
		}
	}

	/**
	 * Test method for
	 * {@link com.srp.trading.service.OperationService#find(java.lang.String)}
	 * .
	 */
	@Test
	public final void testFindString() {
		Operation dummyOp = createDummyOperation();
		String bidRef = UUID.randomUUID().toString();
		String askRef = UUID.randomUUID().toString();
		dummyOp.getBid().setRef(bidRef);
		dummyOp.getAsk().setRef(askRef);
		if (dummyOp.SaveAll()) {
			Order bid = Order.find(bidRef).get(0);
			Order ask = Order.find(askRef).get(0);
			
			boolean found = bid != null && ask != null;
			assertTrue("Orders not found", found);
		} else {
			fail("Error saving dummy operation");
		}
	}
	
	/**
	 * Test method for
	 * {@link com.srp.trading.service.OperationService#find(com.srp.trading.domain.OperationStatus)}  
	 */
	@Test
	public final void testFindOperationStatus() {
		Operation dummyOp = createDummyOperation();
		dummyOp.getBid().setRef(UUID.randomUUID().toString());
		if (!dummyOp.SaveAll())
			fail("Error saving dummy operation");
		
		String bidRef = dummyOp.getBid().getRef();
		Order o = Order.find(bidRef).get(0);
		
		if (!o.saveLatestStatus(OrderStatus.Pending))
			fail("Error saving status");
		
		if (!o.saveLatestStatus(OrderStatus.Finished))
			fail("Error saving status");
		
				
		List<Order> results = Order.find(OrderStatus.Finished, OrderType.BID);
		
		assertTrue("No operations found", results.size() > 0);
		
	}
	
	/**
	 * Test multiple operations:
	 * 1. Create and save operation
	 * 2. Change the bid and save
	 * 3. Change status of the operation
	 * 4. Search by status. Filter the operation with the first reference id
	 * 5. Change the status to BuyOrderCompleted
	 * 6. Search by status BuyOrderCompleted status. Filter the operation with the original ref
	 * 7. Change the ref and save. Update status to SellCreated.
	 * 8. Change the status of the operation to sell pending 
	 */
	@Test
	public final void testMulti() {
		Operation dummyOp = createDummyOperation();
		dummyOp.getAsk().setRef(UUID.randomUUID().toString());
		dummyOp.getBid().setRef(UUID.randomUUID().toString());
		
		// 1
		if (!dummyOp.SaveAll())
			fail("Error saving dummy operation");
		
		String bidRef = dummyOp.getBid().getRef();
		String askRef = dummyOp.getAsk().getRef();
		
		// 2
		dummyOp.getBid().setPrice(new BigDecimal("40.0"));
		dummyOp.Save();
		
		// 3
		dummyOp.getBid().saveLatestStatus(OrderStatus.Pending);
		
		// 4
		List<Order> allOrders = Order.find(OrderStatus.Pending, OrderType.BID);
		Order initial = getByRef(allOrders, bidRef);
		
		// 5		
		initial.saveLatestStatus(OrderStatus.Finished);
		
		// 6
		allOrders = Order.find(OrderStatus.Finished, OrderType.BID);
		initial = getByRef(allOrders, bidRef);
		
		// 7
		initial = dummyOp.getAsk();
		initial.saveLatestStatus(OrderStatus.Pending);
		
		// 8
		initial.saveLatestStatus(OrderStatus.Finished);
		
		Order o1 = Order.find(bidRef).get(0);
		Order o2 = Order.find(askRef).get(0);

		boolean success = o1 != null && o2 != null;
		
		assertTrue("Error in the operations management", success);	
	}
	
	/**
	 * Return an order by ref from the provided ones
	 * @param operations
	 * @param ref
	 * @return
	 */
	private Order getByRef(List<Order> orders, String ref) {
		for(Order o: orders) {
			if (o.getRef() != null && o.getRef().equals(ref)) {
				return o;
			}
		}
		return null;
	}
	
	/**
	 * Test saving history of an operation
	 */
	@Test
	public final void testManageHistory() {
		Operation dummyOp = createDummyOperation();
		dummyOp.getAsk().setRef(UUID.randomUUID().toString());
		if (!dummyOp.SaveAll())
			fail("Error saving dummy operation");
		
		String ref = dummyOp.getAsk().getRef();
		Order o = Order.find(ref).get(0);
		
		if (!o.saveLatestStatus(OrderStatus.Pending))
			fail("Error saving status");
		
		if (!o.saveLatestStatus(OrderStatus.Cancelled))
			fail("Error saving status");
		
		if (!o.saveLatestStatus(OrderStatus.Pending))
			fail("Error saving status");
		
		if (!o.saveLatestStatus(OrderStatus.Finished))
			fail("Error saving status");
		
		OrderStatus latest = o.getLatestStatus();
		
		boolean success = latest == OrderStatus.Finished && o.getHistory().size() == 5; // Unknown status + 4 status changes
			
		assertTrue("History not stored properly", success);	
	}
	
	@Test
	public final void testLastOrderStatus() {		
		Operation dummyOp = createDummyOperation();
		dummyOp.getAsk().setRef(UUID.randomUUID().toString());
		if (!dummyOp.SaveAll())
			fail("Error saving dummy operation");
		
		// Number of unknown orders should be >= 2
		Long n = LastOrderStatus.count(OrderStatus.Unknown);
		
		dummyOp.getAsk().saveLatestStatus(OrderStatus.Pending);
		dummyOp.getBid().saveLatestStatus(OrderStatus.Pending);
		
		Long n2 = LastOrderStatus.count(OrderStatus.Unknown);
		
		boolean success = n2 == n - 2;
		assertTrue("Last order status did not count properly", success);		
	}
	
	@Test
	public final void testFindOperationWithOrderByStatus() {
		// Get the number of operations in unknown state
		int numUnknownOp = Operation.find(OrderStatus.Unknown).size();
		
		// Create dummy operation
		Operation dummyOp = createDummyOperation();
		if (!dummyOp.SaveAll())
			fail("Error saving dummy operation");
		
		// Number of unknown operations should be n + 1
		int newUnknownOp = Operation.find(OrderStatus.Unknown).size();
		if (numUnknownOp + 1 != newUnknownOp)
			fail("Error getting operations in unknown state");
		
		// Change status of the bid
		dummyOp.getBid().saveLatestStatus(OrderStatus.Cancelled);
		
		// Number of unknown operations should be n + 1
		newUnknownOp = Operation.find(OrderStatus.Unknown).size();
		if (numUnknownOp + 1 != newUnknownOp)
			fail("Error getting operations in unknown state");
		
		// Change status of the ask
		dummyOp.getAsk().saveLatestStatus(OrderStatus.Cancelled);
		
		newUnknownOp = Operation.find(OrderStatus.Unknown).size();
		
		int numPendingOrCancelled = Operation.find(new OrderStatus[] { OrderStatus.Pending, OrderStatus.Cancelled }).size();
		
		boolean success = newUnknownOp == numUnknownOp && numPendingOrCancelled > 0;
		assertTrue("Find operations by order status did not work", success);		
	}
	@Test
	public final void testFindOperationWithOrderByStatuses() {
		// Get the number of operations in unknown state
		List<Operation> unknownOrPending = Operation.find(new OrderStatus[] { OrderStatus.Unknown, OrderStatus.Pending});
		assertTrue("Find operations by order statuses dummy test worked", true);
	}
}
