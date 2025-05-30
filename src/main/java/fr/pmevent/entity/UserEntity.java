package fr.pmevent.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    private String firstname;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<UserEventRoleEntity> userEventRoles;

    @Column(updatable = false)
    private LocalDateTime create_date;

    private LocalDateTime update_date;

    @PrePersist
    public void onCreate() {
        this.create_date = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.update_date = LocalDateTime.now();
    }

}
