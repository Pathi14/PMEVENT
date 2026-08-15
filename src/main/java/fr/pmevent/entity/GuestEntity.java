package fr.pmevent.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "guests")
@AllArgsConstructor
@NoArgsConstructor
public class GuestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String firstname;

    @Email(message = "Invalid email format")
    private String email;
    private String phone;

    @Column(nullable = false)
    private Integer number_places;
    private String comment;
    private String photoUrl;
    private String photoPublicId;

    @Column(unique = true)
    private String qrCodeToken;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean present = false;

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
