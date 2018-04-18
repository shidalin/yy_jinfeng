package com.elan.erp.service;

public class ErpWebServiceProxy implements com.elan.erp.service.ErpWebService {
  private String _endpoint = null;
  private com.elan.erp.service.ErpWebService erpWebService = null;
  
  public ErpWebServiceProxy() {
    _initErpWebServiceProxy();
  }
  
  public ErpWebServiceProxy(String endpoint) {
    _endpoint = endpoint;
    _initErpWebServiceProxy();
  }
  
  private void _initErpWebServiceProxy() {
    try {
      erpWebService = (new com.elan.erp.service.ErpServiceLocator()).getERPSaleOrderImplPort();
      if (erpWebService != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)erpWebService)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)erpWebService)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (erpWebService != null)
      ((javax.xml.rpc.Stub)erpWebService)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public com.elan.erp.service.ErpWebService getErpWebService() {
    if (erpWebService == null)
      _initErpWebServiceProxy();
    return erpWebService;
  }
  
  public com.elan.erp.service.UserBean[] test() throws java.rmi.RemoteException{
    if (erpWebService == null)
      _initErpWebServiceProxy();
    return erpWebService.test();
  }
  
  public java.lang.String sendSaleToElanERP(java.lang.String salexml) throws java.rmi.RemoteException, com.elan.erp.service.XmlException{
    if (erpWebService == null)
      _initErpWebServiceProxy();
    return erpWebService.sendSaleToElanERP(salexml);
  }
  
  public java.lang.String getElanERPSale(java.lang.String salexml) throws java.rmi.RemoteException, com.elan.erp.service.Exception{
    if (erpWebService == null)
      _initErpWebServiceProxy();
    return erpWebService.getElanERPSale(salexml);
  }
  
  public void testInt(int num) throws java.rmi.RemoteException{
    if (erpWebService == null)
      _initErpWebServiceProxy();
    erpWebService.testInt(num);
  }
  
  
}