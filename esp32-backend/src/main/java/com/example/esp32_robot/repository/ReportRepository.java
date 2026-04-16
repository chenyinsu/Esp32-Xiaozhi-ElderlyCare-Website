package com.example.esp32_robot.repository;

import com.example.esp32_robot.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    // 根据设备查询报告
    List<Report> findByDeviceIdOrderByReportDateDesc(Long deviceId);

    // 根据用户查询报告
    List<Report> findByUserIdOrderByReportDateDesc(Long userId);

    // 根据报告类型查询
    List<Report> findByReportTypeOrderByReportDateDesc(Report.ReportType type);

    // 根据健康状况评级查询
    List<Report> findByHealthStatusOrderByReportDateDesc(Report.HealthStatus healthStatus);

    // 根据生成者查询
    List<Report> findByGeneratedByOrderByReportDateDesc(Long generatedBy);

    // 根据是否已读查询
    List<Report> findByIsReadOrderByReportDateDesc(Boolean isRead);

    // 根据时间段查询
    List<Report> findByReportDateBetweenOrderByReportDateDesc(
            LocalDate startDate,
            LocalDate endDate
    );

    // 根据创建时间查询
    List<Report> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    // 查询用户的每日健康报告
    List<Report> findByUserIdAndReportTypeOrderByReportDateDesc(
            Long userId,
            Report.ReportType reportType
    );

    // 统计各类报告数量
    long countByReportType(Report.ReportType type);
    long countByHealthStatus(Report.HealthStatus healthStatus);

    // 统计用户的报告数量
    long countByUserId(Long userId);

    // 获取用户的最新报告
    @Query("SELECT r FROM Report r WHERE r.user.id = :userId ORDER BY r.reportDate DESC LIMIT 1")
    Report findLatestByUser(@Param("userId") Long userId);

    // 获取用户的健康趋势数据
    @Query("SELECT r.reportDate, r.heartRate, r.bloodPressureSys, r.bloodPressureDia, " +
            "r.bodyTemperature, r.bloodOxygen FROM Report r " +
            "WHERE r.user.id = :userId AND r.reportType = 'DAILY_HEALTH' " +
            "AND r.reportDate >= :startDate ORDER BY r.reportDate ASC")
    List<Object[]> findHealthTrendData(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate
    );

    // 获取用户的月度统计
    @Query("SELECT FUNCTION('MONTH', r.reportDate), FUNCTION('YEAR', r.reportDate), " +
            "AVG(r.heartRate), AVG(r.bloodPressureSys), AVG(r.bloodPressureDia), " +
            "SUM(r.medicationTaken), SUM(r.medicationMissed), SUM(r.stepCount) " +
            "FROM Report r WHERE r.user.id = :userId AND r.reportType = 'DAILY_HEALTH' " +
            "AND r.reportDate >= :startDate " +
            "GROUP BY FUNCTION('MONTH', r.reportDate), FUNCTION('YEAR', r.reportDate)")
    List<Object[]> getMonthlyStatistics(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate
    );

    // 获取健康异常报告
    @Query("SELECT r FROM Report r WHERE r.healthStatus IN ('POOR', 'CRITICAL') " +
            "ORDER BY r.reportDate DESC")
    List<Report> findHealthAlertReports();

    // 批量标记报告为已读
    @Query("UPDATE Report r SET r.isRead = true, r.readBy = :readBy, r.readTime = :readTime " +
            "WHERE r.id IN :ids")
    int markAsRead(
            @Param("ids") List<Long> ids,
            @Param("readBy") Long readBy,
            @Param("readTime") LocalDateTime readTime
    );

    // 搜索报告
    @Query("SELECT r FROM Report r WHERE " +
            "(:userId IS NULL OR r.user.id = :userId) AND " +
            "(:type IS NULL OR r.reportType = :type) AND " +
            "(:healthStatus IS NULL OR r.healthStatus = :healthStatus) AND " +
            "(r.reportDate BETWEEN :startDate AND :endDate)")
    List<Report> searchReports(
            @Param("userId") Long userId,
            @Param("type") Report.ReportType type,
            @Param("healthStatus") Report.HealthStatus healthStatus,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}