package com.board.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Abstract base entity providing common fields for all JPA entities.
 * Includes ID generation, audit timestamps, and soft delete functionality.
 *
 * <p>
 * All entities should extend this class to inherit:
 * <ul>
 * <li>Auto-generated Long ID</li>
 * <li>Created/Updated timestamps (auto-managed by JPA auditing)</li>
 * <li>Soft delete support via deletedAt field</li>
 * </ul>
 *
 * <p>
 * Requires {@code @EnableJpaAuditing} in Spring configuration.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Checks if this entity is active (not soft deleted).
     *
     * @return true if the entity is active (deletedAt is null), false otherwise
     */
    public boolean isActive() {
        return deletedAt == null;
    }

    /**
     * Soft deletes this entity by setting the deletedAt timestamp to now.
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Restores a soft-deleted entity by clearing the deletedAt timestamp.
     */
    @SuppressWarnings("PMD.NullAssignment") // Intentional: clearing deletedAt restores the entity
    public void restore() {
        this.deletedAt = null;
    }
}
