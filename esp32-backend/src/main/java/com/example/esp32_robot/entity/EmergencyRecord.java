package com.example.esp32_robot.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 紧急事件处理记录实体类
 * 记录紧急事件的详细处理过程
 */
@Entity
@Table(name = "emergency_record")
@Data
@ToString(exclude = {"emergency", "user"})
@EqualsAndHashCode(exclude = {"emergency", "user"})
public class EmergencyRecord {

    public enum ActionType {
        VIEW_CAMERA,        // 查看摄像头
        CALL_USER,          // 联系老人
        CALL_CHILD,         // 联系子女
        CALL_HOSPITAL,      // 联系医院
        ON_SITE,            // 现场处理
        DISMISS,            // 取消/误报
        OTHER               // 其他
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType actionType;  // 处理动作类型

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;         // 处理内容

    // 关联紧急事件
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emergency_id", nullable = false)
    private Emergency emergency;

    // 处理人
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "action_time", nullable = false)
    private LocalDateTime actionTime = LocalDateTime.now();  // 处理时间

    @Column(name = "attachment_url")
    private String attachmentUrl;   // 附件地址（如通话录音、截图等）

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}