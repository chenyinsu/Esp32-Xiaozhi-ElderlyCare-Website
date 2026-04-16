package com.example.esp32_robot.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsResponse {
    private Long totalUsers;
    private Long elderlyCount;
    private Long communityStaffCount;
    private Long staffCount;
    private Long childCount;
    private Long hospitalCount;
    private Long activeUsers;
    private Long inactiveUsers;
    private Long usersWithDevice;
    private Map<String, Long> usersByGender;
    private Map<String, Double> averageAgeByRole;
}
