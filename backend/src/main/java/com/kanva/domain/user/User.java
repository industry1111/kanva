package com.kanva.domain.user;

import com.kanva.domain.BaseEntity;
import com.kanva.domain.dailynote.DailyNote;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 100)
    private String email;

    @Column
    private String password;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(length = 500)
    private String picture;

    @OneToMany(mappedBy = "user")
    private List<DailyNote> dailyNotes = new ArrayList<>();

    @Builder
    public User(String email, String password, String name, Role role, String picture) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role != null ? role : Role.USER;
        this.picture = picture;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updatePicture(String picture) {
        this.picture = picture;
    }
}
