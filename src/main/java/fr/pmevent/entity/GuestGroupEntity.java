package fr.pmevent.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "guest_groups",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_guest_group_event_name",
                columnNames = {"event_id", "name"}
        )
)
@AllArgsConstructor
@NoArgsConstructor
public class GuestGroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "max_people", nullable = false)
    private Integer maxPeople;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "event_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_guest_group_event",
                    foreignKeyDefinition = "FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE"
            )
    )
    private EventEntity event;
}
