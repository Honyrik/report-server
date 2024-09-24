package ru.tehnobear.essence.dao.entries;

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
 * The persistent class for the "t_queue_log" database table.
 * 
 */
@Entity
@Table(name="\"t_queue_log\"")
@NamedQuery(name="TQueueLog.findAll", query="SELECT t FROM TQueueLog t")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class TQueueLog extends Audit implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.UUID)
	@Column(name="\"ck_id\"", nullable = false)
	private UUID ckId;

	@Column(name="\"cv_error\"", nullable = false)
	private String cvError;

	@Column(name="\"cv_error_stacktrace\"", nullable = false)
	private String cvErrorStacktrace;

	//bi-directional many-to-one association to TQueue
	@ManyToOne
	@JoinColumn(name = "\"ck_queue\"", nullable = false)
	@SQLJoinTableRestriction("not clDeleted")
	private TQueue queue;

	@Override
	public String toPostgresPatch() {
		return "";
	}
}