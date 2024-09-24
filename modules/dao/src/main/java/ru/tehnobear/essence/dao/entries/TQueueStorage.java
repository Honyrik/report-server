package ru.tehnobear.essence.dao.entries;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLJoinTableRestriction;
import ru.tehnobear.essence.dao.dto.Audit;

import java.io.Serializable;
import java.util.UUID;


/**
 * The persistent class for the "t_queue_storage" database table.
 * 
 */
@Entity
@Table(name="\"t_queue_storage\"")
@NamedQuery(name="TQueueStorage.findAll", query="SELECT t FROM TQueueStorage t")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class TQueueStorage extends Audit implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.UUID)
	@Column(name="\"ck_id\"", nullable = false)
	private UUID ckId;

	@Column(name="\"cb_result\"", nullable = false)
	@JsonIgnore
	private byte[] cbResult;

	@Column(name="\"cv_name\"", nullable = false)
	private String cvName;

	@Column(name="\"cv_content_type\"", nullable = false)
	private String cvContentType;

	//bi-directional many-to-one association to TQueue
	@ManyToOne
	@JoinColumn(name = "\"ck_queue\"", nullable = false)
	@JsonIgnore
	@SQLJoinTableRestriction("not clDeleted")
	private TQueue queue;

	@Override
	public String toPostgresPatch() {
		return "";
	}
}