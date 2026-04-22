package com.iqra.functioncall;

import com.iqra.workflow.WorkflowContext;
import org.springframework.stereotype.Component;

/**
 * Query attendance info (simulated)
 */
@Component
public class GetAttendanceExecutor implements FunctionExecutor {

    @Override
    public String execute(WorkflowContext context) {
        String userId = context.getUserId();
        return String.format("考勤查询结果 (工号: %s):\n\n今日打卡: 09:02 (正常)\n本月迟到: 1次\n本月早退: 0次\n本月缺勤: 0天\n加班时长: 8小时\n\n如有异常，请联系行政部。", userId);
    }
}
