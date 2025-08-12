package com.ec2.user.entity;

import lombok.*;
import jakarta.persistence.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users") // 'user'는 예약어일 수 있으므로 'users' 사용
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String password;

    @Builder
    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }
}