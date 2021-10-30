/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.icare.core.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.openmrs.Order;
import org.openmrs.Visit;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.module.icare.billing.models.ItemPrice;
import org.openmrs.module.icare.billing.models.Prescription;
import org.openmrs.module.icare.core.Item;
import org.openmrs.module.icare.store.models.OrderStatus;

import javax.persistence.EntityManager;
import java.util.Calendar;
import java.util.List;

public class ICareDao extends BaseDAO<Item> {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	public Item saveItem(Item item) {
		getSession().saveOrUpdate(item);
		return item;
	}
	
	public Item getItemByConceptId(Integer id) {
		DbSession session = getSession();
		List<Item> items = session.createQuery("SELECT a FROM Item a WHERE a.concept='" + id.toString() + "'").list();
		
		if (items.size() > 0) {
			return items.get(0);
		} else {
			return null;
		}
	}
	
	public ItemPrice getItemPriceByConceptId(Integer serviceConceptId, Integer paymentSchemeConceptId,
	        Integer paymentTypeConceptId) {
		DbSession session = getSession();
		String queryStr = "SELECT ip FROM ItemPrice ip WHERE\n" + "ip.id.item.concept.id = :serviceConceptId AND\n"
		        + "ip.id.paymentScheme.id = :paymentSchemeConceptId AND \n" + "ip.id.paymentType.id = :paymentTypeConceptId";
		Query query = session.createQuery(queryStr);
		query.setParameter("serviceConceptId", serviceConceptId);
		query.setParameter("paymentTypeConceptId", paymentTypeConceptId);
		query.setParameter("paymentSchemeConceptId", paymentSchemeConceptId);
		log.info("QUERY:" + query.getQueryString());
		List<ItemPrice> itemPrices = query.list();
		if (itemPrices.size() > 0) {
			return itemPrices.get(0);
		} else {
			return null;
		}
	}
	
	public ItemPrice getItemPriceByDrugId(Integer drugId, Integer paymentSchemeConceptId, Integer paymentTypeConceptId) {
		DbSession session = getSession();
		String queryStr = "SELECT ip FROM ItemPrice ip WHERE\n" + "ip.id.item.drug.id = :drugId AND\n"
		        + "ip.id.paymentScheme.id = :paymentSchemeConceptId AND \n" + "ip.id.paymentType.id = :paymentTypeConceptId";
		Query query = session.createQuery(queryStr);
		query.setParameter("drugId", drugId);
		query.setParameter("paymentTypeConceptId", paymentTypeConceptId);
		query.setParameter("paymentSchemeConceptId", paymentSchemeConceptId);
		log.info("QUERY:" + query.getQueryString());
		List<ItemPrice> itemPrices = query.list();
		if (itemPrices.size() > 0) {
			return itemPrices.get(0);
		} else {
			return null;
		}
	}
	
	public List<ItemPrice> getItemPrices() {
		DbSession session = getSession();
		String queryStr = "SELECT ip FROM ItemPrice ip";
		Query query = session.createQuery(queryStr);
		log.info("QUERY:" + query.getQueryString());
		return query.list();
	}
	
	public List<ItemPrice> getItemPrices(Integer limit, Integer startIndex) {
		DbSession session = getSession();
		String queryStr = "SELECT ip FROM ItemPrice ip ";
		Query query = session.createQuery(queryStr);
		query.setFirstResult(startIndex);
		query.setMaxResults(limit);
		return query.list();
	}
	
	public List<ItemPrice> getItemPricesByPaymentType(String paymentType, Integer limit, Integer startIndex) {
		DbSession session = getSession();
		String queryStr = "SELECT ip FROM ItemPrice ip WHERE ip.paymentType.uuid =:paymentType";
		Query query = session.createQuery(queryStr);
		query.setParameter("paymentType", paymentType);
		query.setFirstResult(startIndex);
		query.setMaxResults(limit);
		return query.list();
	}
	
	public ItemPrice saveItemPrice(ItemPrice itemPrice) {
		getSession().saveOrUpdate(itemPrice);
		return itemPrice;
	}
	
	public List<Item> getItems() {
		DbSession session = getSession();
		String queryStr = "SELECT ip FROM Item ip";
		Query query = session.createQuery(queryStr);
		log.info("QUERY:" + query.getQueryString());
		return query.list();
	}
	
	public List<Item> getItems(String search, Integer limit, Integer startIndex) {
		DbSession session = getSession();
		String queryStr = "SELECT ip FROM Item ip";
		if (search != null) {
			queryStr = "SELECT ip FROM Item ip " + "LEFT JOIN ip.concept as c " + "LEFT JOIN c.names cn "
			        + "LEFT JOIN ip.drug as d " + "LEFT JOIN d.concept as c1 " + "LEFT JOIN c1.names cn1 "
			        + "WHERE lower(cn.name) like :search OR lower(cn1.name) like :search";
		}
		Query query = session.createQuery(queryStr);
		query.setFirstResult(startIndex);
		query.setMaxResults(limit);
		if (search != null) {
			query.setParameter("search", "%" + search + "%");
		}
		return query.list();
	}
	
	public List<OrderStatus> getOrderStatusByOrderUuid(String orderUuid) {
		DbSession session = this.getSession();
		//String queryStr = "SELECT item FROM Item item \n"
		//        + "WHERE item.stockable = true AND item.uuid NOT IN(SELECT stock.item.uuid FROM Stock stock WHERE stock.location.uuid =:locationUuid)";
		//String queryStr = "SELECT item FROM Item item, Stock stock WHERE item.stockable = true AND stock.item=item AND stock.location.uuid =:locationUuid";
		String queryStr = "SELECT os FROM OrderStatus os\n" + "INNER JOIN os.order o\n" + "WHERE o.uuid=:orderUuid";
		
		Query query = session.createQuery(queryStr);
		query.setParameter("orderUuid", orderUuid);
		return query.list();
	}
	
	public long getVisitSerialNumber(Visit visit) {
		DbSession session = getSession();
		String queryStr = "SELECT COUNT(visit) FROM Visit visit WHERE YEAR(visit.startDatetime) = :year AND MONTH(visit.startDatetime) = :month AND visit.startDatetime <= :startDate ORDER BY visit.startDatetime";
		Query query = session.createQuery(queryStr);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(visit.getStartDatetime());
		query.setParameter("year", calendar.get(Calendar.YEAR));
		query.setParameter("startDate", visit.getStartDatetime());
		query.setParameter("month", calendar.get(Calendar.MONTH) + 1);
		return (long) query.list().get(0);
	}
	
	public Item getItemByConceptUuid(String uuid) {
		DbSession session = getSession();
		
		String queryStr = "SELECT a FROM Item a WHERE a.concept.uuid= :uuid";
		Query query = session.createQuery(queryStr);
		query.setParameter("uuid", uuid);
		List<Item> items = query.list();
		
		if (items.size() > 0) {
			return items.get(0);
		} else {
			return null;
		}
	}
	
	public Item getItemByDrugUuid(String uuid) {
		DbSession session = getSession();
		
		String queryStr = "SELECT a FROM Item a WHERE a.drug.uuid= :uuid";
		Query query = session.createQuery(queryStr);
		query.setParameter("uuid", uuid);
		List<Item> items = query.list();
		
		if (items.size() > 0) {
			return items.get(0);
		} else {
			return null;
		}
	}
	
	public List<Visit> getOpenVisit() {
		DbSession session = getSession();
		String queryStr = "SELECT visit FROM Visit visit WHERE visit.stopDatetime IS NULL";
		Query query = session.createQuery(queryStr);
		return query.list();
	}
	
	public Prescription updatePrescription(Prescription prescription) {
		DbSession session = getSession();
		
		String queryStr = "UPDATE prescription SET quantity=10 WHERE uuid=:uuid";
		
		SQLQuery query = session.createSQLQuery(queryStr);
		query.setParameter("uuid", prescription.getUuid());
		query.executeUpdate();
		return prescription;
	}
	
	public List<Visit> getVisitsByOrderType(String search, String orderTypeUuid, String locationUuid,
	        OrderStatus.OrderStatusCode orderStatusCode, Order.FulfillerStatus fulfillerStatus, Integer limit,
	        Integer startIndex) {
		DbSession session = this.getSession();
		String queryStr = "SELECT distinct v FROM Visit v"
				+ " INNER JOIN v.patient p"
				+ " INNER JOIN p.names pname"
				+ " INNER JOIN v.encounters e"
				+ " INNER JOIN e.orders o"
		        + " INNER JOIN o.orderType ot" + " WHERE ot.uuid=:orderTypeUuid " + " AND v.stopDatetime IS NULL ";
		if (fulfillerStatus != null) {
			queryStr += " AND o.fulfillerStatus=:fulfillerStatus";
		} else {
			queryStr += " AND o.fulfillerStatus IS NULL";
		}

		if (search != null) {
			queryStr += " AND lower(concat(pname.givenName,pname.middleName,pname.familyName)) LIKE lower(:search)";
		}
		if (orderStatusCode != null) {
			if (orderStatusCode == OrderStatus.OrderStatusCode.EMPTY) {
				queryStr += " AND o NOT IN (SELECT o2 FROM OrderStatus os" + "	INNER JOIN os.order o2)";
			} else {
				queryStr += " AND o IN (SELECT o2 FROM OrderStatus os"
				        + "	INNER JOIN os.order o2 WHERE os.status=:orderStatusCode)";
			}
		}
		if (locationUuid != null) {
			queryStr += " AND v.location.uuid=:locationUuid";
		}
		
		Query query = session.createQuery(queryStr);
		query.setParameter("orderTypeUuid", orderTypeUuid);
		if (fulfillerStatus != null) {
			query.setParameter("fulfillerStatus", fulfillerStatus);
		}
		if (locationUuid != null) {
			query.setParameter("locationUuid", locationUuid);
		}
		if (search != null) {
			query.setParameter("search", "%" + search.replace(" ","%") + "%");
		}
		if (orderStatusCode != null) {
			if (orderStatusCode == OrderStatus.OrderStatusCode.EMPTY) {
				
			} else {
				query.setParameter("orderStatusCode", orderStatusCode);
			}
		}
		//query.setParameter("fulfillerStatus", fulfillerStatus);
		query.setFirstResult(startIndex);
		query.setMaxResults(limit);
		return query.list();
	}
	
	public List<Order> getOrdersByVisitAndOrderType(String visitUuid, String orderTypeUuid,
	        Order.FulfillerStatus fulfillerStatus, Integer limit, Integer startIndex) {
		DbSession session = this.getSession();
		String queryStr = "SELECT distinct o FROM Visit v" + " INNER JOIN v.encounters e" + " INNER JOIN e.orders o"
		        + " INNER JOIN o.orderType ot" + " WHERE ot.uuid=:orderTypeUuid " + " AND v.stopDatetime IS NULL "
		        + " AND v.uuid=:visitUuid ";
		if (fulfillerStatus != null) {
			queryStr += " AND o.fulfillerStatus=:fulfillerStatus";
		} else {
			queryStr += " AND o.fulfillerStatus IS NULL";
		}
		
		Query query = session.createQuery(queryStr);
		query.setParameter("orderTypeUuid", orderTypeUuid);
		query.setParameter("visitUuid", visitUuid);
		if (fulfillerStatus != null) {
			query.setParameter("fulfillerStatus", fulfillerStatus);
		}
		//query.setParameter("fulfillerStatus", fulfillerStatus);
		query.setFirstResult(startIndex);
		query.setMaxResults(limit);
		return query.list();
	}
}
