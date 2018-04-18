/**
 * ErpServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.elan.erp.service;

public class ErpServiceLocator extends org.apache.axis.client.Service implements com.elan.erp.service.ErpService {

    public ErpServiceLocator() {
    }


    public ErpServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public ErpServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for ERPSaleOrderImplPort
    private java.lang.String ERPSaleOrderImplPort_address = "http://192.168.131.246:8080/elan/ws/erpService";

    public java.lang.String getERPSaleOrderImplPortAddress() {
        return ERPSaleOrderImplPort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String ERPSaleOrderImplPortWSDDServiceName = "ERPSaleOrderImplPort";

    public java.lang.String getERPSaleOrderImplPortWSDDServiceName() {
        return ERPSaleOrderImplPortWSDDServiceName;
    }

    public void setERPSaleOrderImplPortWSDDServiceName(java.lang.String name) {
        ERPSaleOrderImplPortWSDDServiceName = name;
    }

    public com.elan.erp.service.ErpWebService getERPSaleOrderImplPort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(ERPSaleOrderImplPort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getERPSaleOrderImplPort(endpoint);
    }

    public com.elan.erp.service.ErpWebService getERPSaleOrderImplPort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.elan.erp.service.ErpServiceSoapBindingStub _stub = new com.elan.erp.service.ErpServiceSoapBindingStub(portAddress, this);
            _stub.setPortName(getERPSaleOrderImplPortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setERPSaleOrderImplPortEndpointAddress(java.lang.String address) {
        ERPSaleOrderImplPort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.elan.erp.service.ErpWebService.class.isAssignableFrom(serviceEndpointInterface)) {
                com.elan.erp.service.ErpServiceSoapBindingStub _stub = new com.elan.erp.service.ErpServiceSoapBindingStub(new java.net.URL(ERPSaleOrderImplPort_address), this);
                _stub.setPortName(getERPSaleOrderImplPortWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("ERPSaleOrderImplPort".equals(inputPortName)) {
            return getERPSaleOrderImplPort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://service.erp.elan.com", "erpService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://service.erp.elan.com", "ERPSaleOrderImplPort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("ERPSaleOrderImplPort".equals(portName)) {
            setERPSaleOrderImplPortEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
