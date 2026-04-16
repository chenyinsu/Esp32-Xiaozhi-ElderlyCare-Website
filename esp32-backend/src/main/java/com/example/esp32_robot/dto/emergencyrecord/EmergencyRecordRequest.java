package com.example.esp32_robot.dto.emergencyrecord;

import com.example.esp32_robot.entity.EmergencyRecord;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmergencyRecordRequest {

    @NotNull(message = "处理动作类型不能为空")
    private EmergencyRecord.ActionType actionType;

    @NotBlank(message = "处理内容不能为空")
    private String content;

    @NotNull(message = "紧急事件ID不能为空")
    private Long emergencyId;

    @NotNull(message = "处理人ID不能为空")
    private Long userId;

    private String attachmentUrl;
}
