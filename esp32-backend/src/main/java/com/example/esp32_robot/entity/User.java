package com.example.esp32_robot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户实体类
 * 包含老年人、社区工作人员、子女、医院等角色
 */
@Entity
@Table(name = "users")
@Data
@ToString(exclude = {"managedElders", "emergencyContacts"})
@EqualsAndHashCode(exclude = {"managedElders", "emergencyContacts"})
public class User {

    public enum Role {
        ELDERLY,        // 老年人
        COMMUNITY,      // 社区工作人员
        STAFF,          // 管理员
        CHILD,          // 子女
        HOSPITAL        // 医院联系人
    }

    public enum Gender {
        MALE, FEMALE, OTHER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;        // 用户名

    @Column(nullable = false)
    private String password;        // 密码

    @Column(nullable = false)
    private String name;            // 真实姓名

    @Column(unique = true, nullable = false)
    private String phone;           // 手机号

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;              // 角色

    @Enumerated(EnumType.STRING)
    private Gender gender;          // 性别

    private Integer age;            // 年龄

    @Column(unique = true)
    private String idCard;          // 身份证号

    private String address;         // 住址

    @Column(name = "emergency_phone")
    private String emergencyPhone;  // 备用紧急电话

    @Column(name = "medical_history")
    private String medicalHistory;  // 病史

    @Column(name = "health_condition")
    private String healthCondition; // 健康状况

    @Column(name = "device_id")
    private Long deviceId;          // 关联的设备ID（老年人专用）

    // 社区工作人员管理的老年人列表
    @ManyToMany
    @JoinTable(
            name = "community_elder_relation",
            joinColumns = @JoinColumn(name = "community_staff_id"),
            inverseJoinColumns = @JoinColumn(name = "elder_id")
    )
    @JsonIgnore
    private List<User> managedElders = new ArrayList<>();

    // 紧急联系人列表（子女、医院等）
    @ManyToMany
    @JoinTable(
            name = "user_emergency_contact",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "contact_id")
    )
    @JsonIgnore
    private List<User> emergencyContacts = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "is_active")
    private Boolean isActive = true;  // 账户是否激活

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}