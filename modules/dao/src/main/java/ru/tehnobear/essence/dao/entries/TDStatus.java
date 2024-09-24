package ru.tehnobear.essence.dao.entries;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.tehnobear.essence.dao.dto.Audit;
import ru.tehnobear.essence.dao.dto.EStatus;

import java.io.Serializable;


/**
 * The persistent class for the "t_d_status" database table.
 * 
 */
@Entity
@Table(name="\"t_d_status\"")
@NamedQuery(name="TDStatus.findAll", query="SELECT t FROM TDStatus t")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class TDStatus extends Audit implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final String formatPostgres =
			"INSERT INTO ${user.table}.t_d_status\n" +
					"(ck_id, cv_name, ck_user, ct_change, ct_create, cl_deleted)\n" +
					"VALUES(%s, %s, %s, %s::timestamp, %s::timestamp, %s)\n" +
					"ON CONFLICT (ck_id)\n" +
					"DO NOTHING;\n";

	@Id
	@Column(name="\"ck_id\"", nullable = false)
	@Enumerated(EnumType.STRING)
	private EStatus ckId;

	@Column(name="\"cv_name\"", nullable = false)
	private String cvName;

	@Override
	public String toPostgresPatch() {
		return String.format(
				formatPostgres,
				toStringPostgres(ckId),
				toStringPostgres(cvName),
				toStringPostgres(getCkUser()),
				toStringPostgres(getCtChange()),
				toStringPostgres(getCtCreate()),
				toStringPostgres(isClDeleted())
		);
	}
}