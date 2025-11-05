package fr.pmevent.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "guests")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GuestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String firstname;

    @Email(message = "Invalid email format")
    private String email;
    private String phone;

    @Column(nullable = false)
    private Integer number_places;
    private String comment;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, foreignKey = @ForeignKey(
            name = "fk_guest_event",
            foreignKeyDefinition = "FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE"
    ))
    private EventEntity event;

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
