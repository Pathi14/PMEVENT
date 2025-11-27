package fr.pmevent.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private String imageUrl;

    @Column(nullable = false)
    private boolean publicEvent = false;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GuestEntity> guests = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserEventRoleEntity> userEventRoles = new ArrayList<>();

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
