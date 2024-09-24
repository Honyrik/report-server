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
import java.util.List;


/**
 * The persistent class for the "t_d_queue" database table.
 * 
 */
@Entity
@Table(name="\"t_d_queue\"")
@NamedQuery(name="TDQueue.findAll", query="SELECT t FROM TDQueue t")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class TDQueue extends Audit implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final String formatPostgres =
			"INSERT INTO ${user.table}.t_d_queue\n" +
					"(ck_id, cv_runner_url, ck_parent, ck_user, ct_change, ct_create, cl_deleted)\n" +
					"VALUES(%s, %s, %s, %s, %s::timestamp, %s::timestamp, %s)\n" +
					"ON CONFLICT (ck_id)\n" +
					"DO NOTHING;\n";

	@Id
	@Column(name="\"ck_id\"", nullable = false)
	private String ckId;

	@Column(name="\"cv_runner_url\"")
	private String cvRunnerUrl;

	//bi-directional many-to-one association to TDQueue
	@ManyToOne
	@JoinColumn(name = "\"ck_parent\"")
	@SQLJoinTableRestriction("not clDeleted")
	private TDQueue parent;

	@OneToMany(mappedBy = "parent", fetch = FetchType.EAGER)
	@JsonIgnore
	private List<TDQueue> children;

	@Override
	public String toPostgresPatch() {
		return String.format(
				formatPostgres,
				toStringPostgres(ckId),
				toStringPostgres(cvRunnerUrl),
				toStringPostgres(parent != null ? parent.ckId : null),
				toStringPostgres(getCkUser()),
				toStringPostgres(getCtChange()),
				toStringPostgres(getCtCreate()),
				toStringPostgres(isClDeleted())
		);
	}
}