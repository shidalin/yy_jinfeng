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
 * ���۷�Ʊ��˺���� �ش��������� ��Ʊ�ش��ӿڵ�ַ�� http://192.168.9.97:8080/elan/ws/erpService?wsdl
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
					// ���ȹ���
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
							.createElement("SaleListId"); // ���ݺ�
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
						saleListItemIdElement.setTextContent(bvo.getVfree1());// �ӱ�vdef1
						Element commodityNoElement = document
								.createElement("CommodityNo");
						itemElement.appendChild(commodityNoElement);
						commodityNoElement.setTextContent(pk2code.get(bvo
								.getCmaterialid()));// ���ϱ���
						Element amountElement = document
								.createElement("Amount");
						itemElement.appendChild(amountElement);
						amountElement
								.setTextContent(bvo.getNorigmny() == null ? "0"
										: bvo.getNorigmny().toString());// ����˰���ۿۺ�
						Element taxElement = document.createElement("Tax");
						itemElement.appendChild(taxElement);
						taxElement.setTextContent(bvo.getNtax() == null ? "0"
								: bvo.getNtax().toString());// ˰���ۿۺ�

						Element totalDiscountAmountElement = document
								.createElement("TotalDiscountAmount");
						itemElement.appendChild(totalDiscountAmountElement);
						// ˰��
						Double ntaxrate = bvo.getNtaxrate() == null ? 0 : bvo
								.getNtaxrate().doubleValue();
						if (ntaxrate > 1) {
							ntaxrate = ntaxrate / 100;
						}
						// ���ó�ֽ��=����˰���ۿ۲���˰���+�ۿ�˰��
						Double norigsubmny = bvo.getNorigdiscount() == null ? 0
								: bvo.getNorigsubmny().doubleValue();
						// �ۿ۲���˰���
						Double totalDiscountAmount = norigsubmny
								/ (1 + ntaxrate);
						// �ۿ�˰��
						Double totalDiscountTax = norigsubmny
								- totalDiscountAmount;

						totalDiscountAmountElement.setTextContent(scaleUtils
								.adjustGroupMnyScale(
										new UFDouble(totalDiscountAmount))
								.toString());// �ۿ۲���˰���

						Element totalDiscountTaxElement = document
								.createElement("TotalDiscountTax");

						itemElement.appendChild(totalDiscountTaxElement);
						totalDiscountTaxElement.setTextContent(scaleUtils
								.adjustGroupMnyScale(
										new UFDouble(totalDiscountTax))
								.toString());// �ۿ�˰��
						Element isGiftElement = document
								.createElement("IsGift");
						itemElement.appendChild(isGiftElement);
						Integer bflag = 0;
						if (bvo.getBlargessflag().booleanValue()) {
							bflag = 1;
						}
						isGiftElement.setTextContent(bflag.toString());// �Ƿ���Ʒ
																		// 0Ϊ����Ʒ��1Ϊ��Ʒ
					}
					// XMLת�ַ���
					TransformerFactory tf = TransformerFactory.newInstance();
					Transformer t = tf.newTransformer();
					t.setOutputProperty("encoding", "GB23121");// ����������⣬�Թ���GBK����
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					t.transform(new DOMSource(document), new StreamResult(bos));
					String xmlStr = bos.toString();
					// ��Ϊ���͵�xml��ʽ
					xmlStr = StringEscapeUtils.unescapeXml(xmlStr);
					// Զ�̵��� ��������
					java.util.logging.Logger logger = java.util.logging.Logger
							.getLogger("*****����NCϵͳ�ش�ERP�ٵ���˰��Ʊ�ӿ�-���۷�Ʊ*****\n");
					logger.info("*****��������*****" + xmlStr + "*****\n");
					logger.info("****���ķ��Ϳ�ʼ****" + System.currentTimeMillis()
							+ "ms\n");
					ErpWebServiceProxy proxy = new ErpWebServiceProxy();

					String result = proxy.sendSaleToElanERP(xmlStr);

					logger.info("*****���Ļ�ִ*****" + result + "*****\n");
					logger.info("****���ķ��ͽ���****" + System.currentTimeMillis()
							+ "ms\n");
				}
			} catch (Exception e) {
				ExceptionUtils.wrappException(e);
			}
		}
	}
}
