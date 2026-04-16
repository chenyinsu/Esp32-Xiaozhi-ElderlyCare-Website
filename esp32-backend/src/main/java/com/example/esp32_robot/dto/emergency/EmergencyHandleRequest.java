package com.example.esp32_robot.dto.emergency;

import com.example.esp32_robot.entity.Emergency;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmergencyHandleRequest {

    @NotNull(message = "处理状态不能为空")
    private Emergency.Status status;

    @NotNull(message = "处理人ID不能为空")
    private Long handledById;

    private String resolutionNote;
}