package com.iqra.functioncall;

import com.iqra.workflow.WorkflowContext;
import org.springframework.stereotype.Component;

/**
 * Query leave process (simulated)
 */
@Component
public class GetLeaveProcessExecutor implements FunctionExecutor {

    @Override
    public String execute(WorkflowContext context) {
        return "请假流程:\n\n1. 登录OA系统\n2. 进入\"我的审批\" -> \"请假申请\"\n3. 填写请假类型（年假/事假/病假/调休）\n4. 填写请假起止时间\n5. 填写请假事由\n6. 选择审批人（直接上级）\n7. 提交申请\n\n注意事项:\n- 年假需提前3天申请\n- 病假需提供医院证明\n- 3天以上请假需部门经理审批\n- 1天以内请假直接上级审批即可";
    }
}
