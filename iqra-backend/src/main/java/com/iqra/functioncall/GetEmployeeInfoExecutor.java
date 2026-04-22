package com.iqra.functioncall;

import com.iqra.workflow.WorkflowContext;
import org.springframework.stereotype.Component;

/**
 * Query employee basic info (simulated)
 */
@Component
public class GetEmployeeInfoExecutor implements FunctionExecutor {

    @Override
    public String execute(WorkflowContext context) {
        String userId = context.getUserId();
        // Simulated employee info
        return String.format("员工信息:\n姓名: 张三\n工号: %s\n部门: 技术部\n职位: 高级工程师\n入职日期: 2022-01-15\n\n如需更详细信息，请联系HR部门。", userId);
    }
}
