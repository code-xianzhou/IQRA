package com.iqra.functioncall;

import com.iqra.workflow.WorkflowContext;
import org.springframework.stereotype.Component;

/**
 * Query reimbursement material list (simulated)
 */
@Component
public class GetReimburseMaterialExecutor implements FunctionExecutor {

    @Override
    public String execute(WorkflowContext context) {
        return "报销所需材料清单:\n\n1. 报销申请表（OA系统下载）\n2. 发票原件（增值税发票需提供抵扣联）\n3. 付款凭证（银行转账记录）\n4. 出差审批表（差旅报销）\n5. 合同/协议（如有）\n\n报销流程:\n1. 准备上述材料\n2. 填写报销申请表\n3. 部门经理审批\n4. 财务部审核\n5. 总经理审批（金额>5000元）\n6. 财务打款\n\n报销时限: 审批通过后5个工作日内到账";
    }
}
