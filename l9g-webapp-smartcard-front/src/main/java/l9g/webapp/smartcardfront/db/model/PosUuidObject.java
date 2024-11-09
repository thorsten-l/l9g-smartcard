package l9g.webapp.smartcardfront.db.model;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.Getter;

//~--- JDK imports ------------------------------------------------------------
import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Objects;
import java.util.UUID;
import l9g.webapp.smartcardfront.json.View;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Dr. Thorsten Ludewig (t.ludewig@ostfalia.de)
 */
@MappedSuperclass
@NoArgsConstructor
@Slf4j
@Getter
@ToString
public class PosUuidObject implements Serializable
{
  private static final long serialVersionUID = 1575497541642600225l;

  //~--- constructors ---------------------------------------------------------
  public PosUuidObject(String createdBy, boolean immutable)
  {
    this.createdBy = this.modifiedBy = createdBy;
    this.id = UUID.randomUUID().toString();
    this.immutable = immutable;
  }

  public PosUuidObject(String createdBy)
  {
    this(createdBy, false);
  }

  @PrePersist
  public void prePersist()
  {
    log.debug("prePersist " + this.getClass().getCanonicalName());
    this.createTimestamp = this.modifyTimestamp = new Date();
  }

  /**
   * Method description
   *
   */
  @PreRemove
  public void preRemove()
  {
    log.debug("preRemove {} {}",
      this.getClass().getCanonicalName(), id);
    /*
    if(immutable)
    {
      log.error("Attempted to remove an immutable object: {} {}",
        this.getClass().getCanonicalName(), id);
      throw new IllegalStateException("Cannot remove an immutable object.");
    }
     */
    log.debug("done");
  }

  /**
   * Method description
   *
   */
  @PreUpdate
  public void preUpdate()
  {
    log.debug("preUpdate {} {}",
      this.getClass().getCanonicalName(), id);
    if(immutable)
    {
      log.error("Attempted to update an immutable object: {} {}",
        this.getClass().getCanonicalName(), id);
      throw new IllegalStateException("Cannot update an immutable object.");
    }
    this.modifyTimestamp = new Date();
  }

  //~--- methods --------------------------------------------------------------
  @Override
  public boolean equals(Object obj)
  {
    boolean same = false;

    if(this == obj)
    {
      return true;
    }

    if((obj != null) && (obj instanceof PosUuidObject))
    {
      same = this.getId().equals(((PosUuidObject)obj).getId());
    }

    return same;
  }

  @Override
  public int hashCode()
  {
    return Objects.hashCode(this.id);
  }

  public void setModifiedBy(String modifiedBy)
  {
    this.modifiedBy = modifiedBy;
    this.modifyTimestamp = new Date();
  }

  @Column(updatable = false)
  private String createdBy;

  private String modifiedBy;

  @JsonView(View.Base.class)
  @Temporal(TemporalType.TIMESTAMP)
  @Column(updatable = false)
  protected Date createTimestamp;

  @JsonView(View.Base.class)
  @Temporal(TemporalType.TIMESTAMP)
  protected Date modifyTimestamp;

  @JsonView(View.Base.class)
  private boolean immutable;

  @Setter
  @JsonView(View.Base.class)
  private boolean hidden;

  @Id
  @Column(length = 40, updatable = false)
  @JsonView(View.Base.class)
  private String id;

}
