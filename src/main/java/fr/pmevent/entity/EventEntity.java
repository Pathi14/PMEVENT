package fr.pmevent.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Entity
@Table(name = "events")
@NoArgsConstructor
@AllArgsConstructor
public class EventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String location;
    private LocalDate start_date;
    private LocalDate end_date;
    private String description;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<GuestEntity> guests;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
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
