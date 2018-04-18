/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package nc.impl.so.m32.action;

import nc.bs.pub.action.N_32_APPROVE;
import nc.bs.pubapp.pub.rule.SaleListTransferRule;
import nc.bs.so.m32.plugin.Action32PlugInPoint;
import nc.impl.pubapp.pattern.data.bill.BillQuery;
import nc.impl.pubapp.pattern.rule.processer.AroundProcesser;
import nc.impl.so.m32.action.rule.approve.BusiLog;
import nc.impl.so.m32.action.rule.approve.CheckAppEnableRule;
import nc.impl.so.m32.action.rule.approve.PushSquareRule;
import nc.impl.so.m32.action.rule.approve.ReWriteArsubAppRule;
import nc.vo.pubapp.pattern.exception.ExceptionUtils;
import nc.vo.pubapp.pattern.log.TimeLog;
import nc.vo.so.m32.entity.SaleInvoiceVO;

public class ApproveSaleInvoiceAction {
	public Object approve(N_32_APPROVE script) {
		Object ret = null;

		try {
			SaleInvoiceVO[] e = (SaleInvoiceVO[]) ((SaleInvoiceVO[]) script
					.getPfParameterVO().m_preValueVos);
			AroundProcesser processer = new AroundProcesser(
					Action32PlugInPoint.ApproveAction);
			this.addBeforeRule(processer);
			TimeLog.logStart();
			processer.before(e);
			TimeLog.info("调用审批流前执行业务规则");
			TimeLog.logStart();
			ret = script.procActionFlow(script.getPfParameterVO());
			TimeLog.info("走审批工作流处理，此处不允许进行修改");
			if (null == ret) {
				ret = this.queryNewVO(e);
				this.addAfterRule(processer);
				TimeLog.logStart();
				processer.after((SaleInvoiceVO[]) ((SaleInvoiceVO[]) ret));
				TimeLog.info("调用审批流后执行业务规则");
			}
		} catch (Exception arg4) {
			ExceptionUtils.wrappException(arg4);
		}

		return ret;
	}

	private void addAfterRule(AroundProcesser<SaleInvoiceVO> processer) {
		ReWriteArsubAppRule rule = new ReWriteArsubAppRule();
		processer.addAfterRule(rule);
		PushSquareRule rule1 = new PushSquareRule();
		processer.addAfterRule(rule1);
		SaleListTransferRule rule2 = new SaleListTransferRule();
		processer.addAfterRule(rule2);
	}

	private void addBeforeRule(AroundProcesser<SaleInvoiceVO> processer) {
		CheckAppEnableRule rule = new CheckAppEnableRule();
		processer.addBeforeRule(rule);
		BusiLog rule1 = new BusiLog();
		processer.addBeforeRule(rule1);
	}

	private SaleInvoiceVO[] queryNewVO(SaleInvoiceVO[] bills) {
		int ilength = bills.length;
		String[] ids = new String[ilength];

		for (int query = 0; query < ilength; ++query) {
			ids[query] = bills[query].getPrimaryKey();
		}

		BillQuery arg4 = new BillQuery(SaleInvoiceVO.class);
		return (SaleInvoiceVO[]) arg4.query(ids);
	}
}