package net.sphuta.tms.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
public class TmsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // Add other fields as needed

    // Getters and setters
    public Long id() { return id; }
    public void setId(Long id) { this.id = id; }
}
