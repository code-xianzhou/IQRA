package com.iqra.workflow;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Simple intent recognizer based on keywords
 */
@Component
public class IntentRecognizer {

    private static final Pattern LEAVE_PATTERN = Pattern.compile("请假|休假|年假|事假|病假|调休");
    private static final Pattern REIMBURSE_PATTERN = Pattern.compile("报销|差旅|补贴|发票");
    private static final Pattern ATTENDANCE_PATTERN = Pattern.compile("考勤|打卡|迟到|早退|加班");
    private static final Pattern SALARY_PATTERN = Pattern.compile("薪资|工资|薪水|薪酬|社保");
    private static final Pattern ONBOARD_PATTERN = Pattern.compile("入职|报到|新员工|办理");

    public String recognize(String question) {
        if (LEAVE_PATTERN.matcher(question).find()) {
            return "LEAVE_PROCESS";
        }
        if (REIMBURSE_PATTERN.matcher(question).find()) {
            return "REIMBURSE_PROCESS";
        }
        if (ATTENDANCE_PATTERN.matcher(question).find()) {
            return "ATTENDANCE_QUERY";
        }
        if (SALARY_PATTERN.matcher(question).find()) {
            return "SALARY_QUERY";
        }
        if (ONBOARD_PATTERN.matcher(question).find()) {
            return "ONBOARD_PROCESS";
        }
        return "DOCUMENT_QA";
    }
}
