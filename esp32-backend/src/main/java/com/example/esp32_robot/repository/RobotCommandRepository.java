package com.example.esp32_robot.repository;

import com.example.esp32_robot.model.RobotCommand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RobotCommandRepository extends JpaRepository<RobotCommand, Long> {

    // 按状态查询指令
    List<RobotCommand> findByStatus(String status);

    // 查询指定时间段内的指令
    List<RobotCommand> findByCommandTimeBetween(LocalDateTime start, LocalDateTime end);

    // 查询最新的N条指令
    @Query(value = "SELECT * FROM robot_command ORDER BY command_time DESC LIMIT :limit", nativeQuery = true)
    List<RobotCommand> findLatestCommands(@Param("limit") int limit);

    // 统计今日指令数量
    @Query("SELECT COUNT(c) FROM RobotCommand c WHERE DATE(c.commandTime) = CURRENT_DATE")
    Long countTodayCommands();
}