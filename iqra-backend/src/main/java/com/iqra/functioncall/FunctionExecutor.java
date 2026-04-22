package com.iqra.functioncall;

import com.iqra.workflow.WorkflowContext;

/**
 * Function executor interface
 */
public interface FunctionExecutor {
    String execute(WorkflowContext context);
}
