package de.birk.calory.adapter.secondary.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Persistence entity for refresh tokens.
 *
 * <p>Only the SHA-256 hash of a refresh token is ever persisted, never the
 * raw value handed out to the client.
 *
 * @author Marius Birk
 */
@Entity
@Table(name = "tab_calory_refresh_token")
public class RefreshTokenPersistence {

  @Id
  @Column
  private UUID id;

  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "token_hash")
  private String tokenHash;

  @Column(name = "expires_at")
  private Instant expiresAt;

  @Column
  private boolean revoked;

  @Column(name = "created_at")
  private Instant createdAt;

  public RefreshTokenPersistence() {
    //for JPA
  }

  /**
   * Constructor to create an instance of the object, that can be saved in the database.
   *
   * @param id the identifier
   * @param userId the identifier of the owning user
   * @param tokenHash the SHA-256 hash of the raw refresh token
   * @param expiresAt the point in time the refresh token expires
   * @param revoked whether the refresh token has already been used or revoked
   * @param createdAt the point in time the refresh token was issued
   */
  public RefreshTokenPersistence(
      UUID id,
      UUID userId,
      String tokenHash,
      Instant expiresAt,
      boolean revoked,
      Instant createdAt) {
    this.id = id;
    this.userId = userId;
    this.tokenHash = tokenHash;
    this.expiresAt = expiresAt;
    this.revoked = revoked;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getTokenHash() {
    return tokenHash;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public boolean isRevoked() {
    return revoked;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
