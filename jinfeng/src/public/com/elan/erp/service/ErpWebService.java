/**
 * ErpWebService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.elan.erp.service;

public interface ErpWebService extends java.rmi.Remote {
    public com.elan.erp.service.UserBean[] test() throws java.rmi.RemoteException;
    public java.lang.String sendSaleToElanERP(java.lang.String salexml) throws java.rmi.RemoteException, com.elan.erp.service.XmlException;
    public java.lang.String getElanERPSale(java.lang.String salexml) throws java.rmi.RemoteException, com.elan.erp.service.Exception;
    public void testInt(int num) throws java.rmi.RemoteException;
}
