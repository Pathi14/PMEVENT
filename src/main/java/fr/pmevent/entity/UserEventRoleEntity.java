package fr.pmevent.entity;

import fr.pmevent.enums.EventRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "user_event_roles",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "event_id"})
        }
)
public class UserEventRoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, foreignKey = @ForeignKey(
            name = "fk_user_event_roles_event",
            foreignKeyDefinition = "FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE"
    ))
    private EventEntity event;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventRole role;
}
