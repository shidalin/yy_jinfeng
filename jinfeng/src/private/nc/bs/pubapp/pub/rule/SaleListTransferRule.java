package nc.bs.pubapp.pub.rule;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import nc.bs.dao.BaseDAO;
import nc.impl.pubapp.pattern.rule.IRule;
import nc.jdbc.framework.processor.MapListProcessor;
import nc.vo.pub.ISuperVO;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pubapp.pattern.exception.ExceptionUtils;
import nc.vo.pubapp.scale.ScaleUtils;
import nc.vo.so.m32.entity.SaleInvoiceBVO;
import nc.vo.so.m32.entity.SaleInvoiceHVO;
import nc.vo.so.m32.entity.SaleInvoiceVO;

import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.elan.erp.service.ErpWebServiceProxy;

/**
 * 销售发票审核后规则 回传销售数据 发票回传接口地址： http://192.168.9.97:8080/elan/ws/erpService?wsdl
 * 
 * @author yymark
 * 
 */
public class SaleListTransferRule implements IRule<SaleInvoiceVO> {

	public void process(SaleInvoiceVO[] aggs) {
		if (aggs != null && aggs.length > 0) {
			try {
				ArrayList<String> materialPKList = new ArrayList<String>();
				for (SaleInvoiceVO agg : aggs) {
					ISuperVO[] bvos = agg.getChildren(SaleInvoiceBVO.class);
					for (ISuperVO isupervo : bvos) {
						SaleInvoiceBVO bvo = (SaleInvoiceBVO) isupervo;
						materialPKList.add(bvo.getCmaterialvid());
					}
				}
				HashMap<String, String> pk2code = new HashMap<String, String>();
				if (materialPKList.size() > 0) {
					String sql = "select t.code,t.pk_material from bd_material t where nvl(t.dr,0) =0 and ";
					String wheresql = nc.vo.pf.pub.util.SQLUtil.buildSqlForIn(
							"t.pk_material",
							materialPKList.toArray(new String[0]));
					sql += wheresql;
					BaseDAO dao = new BaseDAO();
					List list = (List) dao.executeQuery(sql,
							new MapListProcessor());
					for (Object obj : list) {
						Map map = (Map) obj;
						String pk_material = (String) map.get("pk_material");
						String code = (String) map.get("code");
						pk2code.put(pk_material, code);
					}
				}
				for (SaleInvoiceVO agg : aggs) {
					// 精度工具
					ScaleUtils scaleUtils = new nc.vo.pubapp.scale.ScaleUtils(
							agg.getParentVO().getPk_group());
					SaleInvoiceHVO hvo = agg.getParentVO();
					String vbillcode = hvo.getVbillcode();
					try {
						Integer test = Integer.parseInt(vbillcode);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						continue;
					}
					DocumentBuilderFactory factory = DocumentBuilderFactory
							.newInstance();
					DocumentBuilder builder = factory.newDocumentBuilder();
					Document document = builder.newDocument();
					document.setXmlVersion("1.0");
					document.setXmlStandalone(true);
					Element root = document.createElement("SaleList");
					document.appendChild(root);
					Element saleElement = document.createElement("Sale");
					root.appendChild(saleElement);
					Element headElement = document.createElement("Head");
					saleElement.appendChild(headElement);
					Element subShopNoElement = document
							.createElement("SubShopNo"); // vdef1
					Element saleListIdElement = document
							.createElement("SaleListId"); // 单据号
					subShopNoElement.setTextContent(hvo.getVdef1());
					saleListIdElement.setTextContent(hvo.getVbillcode());
					headElement.appendChild(subShopNoElement);
					headElement.appendChild(saleListIdElement);
					ISuperVO[] bvos = agg.getChildren(SaleInvoiceBVO.class);
					for (ISuperVO isupervo : bvos) {
						SaleInvoiceBVO bvo = (SaleInvoiceBVO) isupervo;
						Element subItemElement = document
								.createElement("SubItem");
						saleElement.appendChild(subItemElement);
						Element itemElement = document.createElement("Item");
						subItemElement.appendChild(itemElement);
						Element saleListItemIdElement = document
								.createElement("SaleListItemId");
						itemElement.appendChild(saleListItemIdElement);
						saleListItemIdElement.setTextContent(bvo.getVfree1());// 子表vdef1
						Element commodityNoElement = document
								.createElement("CommodityNo");
						itemElement.appendChild(commodityNoElement);
						commodityNoElement.setTextContent(pk2code.get(bvo
								.getCmaterialid()));// 物料编码
						Element amountElement = document
								.createElement("Amount");
						itemElement.appendChild(amountElement);
						amountElement
								.setTextContent(bvo.getNorigmny() == null ? "0"
										: bvo.getNorigmny().toString());// 不含税金额（折扣后）
						Element taxElement = document.createElement("Tax");
						itemElement.appendChild(taxElement);
						taxElement.setTextContent(bvo.getNtax() == null ? "0"
								: bvo.getNtax().toString());// 税金（折扣后）

						Element totalDiscountAmountElement = document
								.createElement("TotalDiscountAmount");
						itemElement.appendChild(totalDiscountAmountElement);
						// 税率
						Double ntaxrate = bvo.getNtaxrate() == null ? 0 : bvo
								.getNtaxrate().doubleValue();
						if (ntaxrate > 1) {
							ntaxrate = ntaxrate / 100;
						}
						// 费用冲抵金额=（含税金额）折扣不含税金额+折扣税金
						Double norigsubmny = bvo.getNorigdiscount() == null ? 0
								: bvo.getNorigsubmny().doubleValue();
						// 折扣不含税金额
						Double totalDiscountAmount = norigsubmny
								/ (1 + ntaxrate);
						// 折扣税金
						Double totalDiscountTax = norigsubmny
								- totalDiscountAmount;

						totalDiscountAmountElement.setTextContent(scaleUtils
								.adjustGroupMnyScale(
										new UFDouble(totalDiscountAmount))
								.toString());// 折扣不含税金额

						Element totalDiscountTaxElement = document
								.createElement("TotalDiscountTax");

						itemElement.appendChild(totalDiscountTaxElement);
						totalDiscountTaxElement.setTextContent(scaleUtils
								.adjustGroupMnyScale(
										new UFDouble(totalDiscountTax))
								.toString());// 折扣税金
						Element isGiftElement = document
								.createElement("IsGift");
						itemElement.appendChild(isGiftElement);
						Integer bflag = 0;
						if (bvo.getBlargessflag().booleanValue()) {
							bflag = 1;
						}
						isGiftElement.setTextContent(bflag.toString());// 是否赠品
																		// 0为非赠品，1为赠品
					}
					// XML转字符串
					TransformerFactory tf = TransformerFactory.newInstance();
					Transformer t = tf.newTransformer();
					t.setOutputProperty("encoding", "GB23121");// 解决中文问题，试过用GBK不行
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					t.transform(new DOMSource(document), new StreamResult(bos));
					String xmlStr = bos.toString();
					// 因为传送的xml格式
					xmlStr = StringEscapeUtils.unescapeXml(xmlStr);
					// 远程调用 传送数据
					java.util.logging.Logger logger = java.util.logging.Logger
							.getLogger("*****用友NC系统回传ERP再到金税开票接口-销售发票*****\n");
					logger.info("*****报文数据*****" + xmlStr + "*****\n");
					logger.info("****报文发送开始****" + System.currentTimeMillis()
							+ "ms\n");
					ErpWebServiceProxy proxy = new ErpWebServiceProxy();

					String result = proxy.sendSaleToElanERP(xmlStr);

					logger.info("*****报文回执*****" + result + "*****\n");
					logger.info("****报文发送结束****" + System.currentTimeMillis()
							+ "ms\n");
				}
			} catch (Exception e) {
				ExceptionUtils.wrappException(e);
			}
		}
	}
}
