package com.iqra.workflow;

import com.iqra.functioncall.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Function call router: determines if a function call is needed and executes it
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FunctionCallRouter {

    private final Map<String, FunctionExecutor> executorMap = new ConcurrentHashMap<>();

    public FunctionCallRouter(GetEmployeeInfoExecutor getEmployeeInfo,
                               GetAttendanceExecutor getAttendance,
                               GetLeaveProcessExecutor getLeaveProcess,
                               GetReimburseMaterialExecutor getReimburseMaterial) {
        executorMap.put("getEmployeeInfo", getEmployeeInfo);
        executorMap.put("getAttendance", getAttendance);
        executorMap.put("getLeaveProcess", getLeaveProcess);
        executorMap.put("getReimburseMaterial", getReimburseMaterial);
    }

    public boolean needFunctionCall(WorkflowContext context) {
        String intent = context.getIntent();
        // Check if intent matches a function call scenario
        return "ATTENDANCE_QUERY".equals(intent) ||
               "SALARY_QUERY".equals(intent) ||
               "LEAVE_PROCESS".equals(intent) ||
               "REIMBURSE_PROCESS".equals(intent);
    }

    public String execute(WorkflowContext context) {
        String intent = context.getIntent();
        String functionName = mapIntentToFunction(intent);

        FunctionExecutor executor = executorMap.get(functionName);
        if (executor == null) {
            return "抱歉，暂不支持此功能。";
        }

        try {
            return executor.execute(context);
        } catch (Exception e) {
            log.error("Function call execution failed: {}", functionName, e);
            return "功能调用失败: " + e.getMessage();
        }
    }

    private String mapIntentToFunction(String intent) {
        switch (intent) {
            case "ATTENDANCE_QUERY":
                return "getAttendance";
            case "SALARY_QUERY":
                return "getEmployeeInfo";
            case "LEAVE_PROCESS":
                return "getLeaveProcess";
            case "REIMBURSE_PROCESS":
                return "getReimburseMaterial";
            default:
                return null;
        }
    }
}
