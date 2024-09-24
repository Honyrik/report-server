package ru.tehnobear.essence.dao.entries;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.tehnobear.essence.dao.dto.Audit;

import java.io.Serializable;


/**
 * The persistent class for the "t_d_source_type" database table.
 * 
 */
@Entity
@Table(name="\"t_d_source_type\"")
@NamedQuery(name="TDSourceType.findAll", query="SELECT t FROM TDSourceType t")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class TDSourceType extends Audit implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final String formatPostgres =
			"INSERT INTO ${user.table}.t_d_source_type\n" +
					"(ck_id, cv_description, ck_user, ct_change, ct_create, cl_deleted)\n" +
					"VALUES(%s, %s, %s, %s::timestamp, %s::timestamp, %s)\n" +
					"ON CONFLICT (ck_id)\n" +
					"DO NOTHING;\n";
	@Id
	@Column(name="\"ck_id\"", nullable = false)
	private String ckId;

	@Column(name="\"cv_description\"")
	private String cvDescription;

	@Override
	public String toPostgresPatch() {
		return String.format(
				formatPostgres,
				toStringPostgres(ckId),
				toStringPostgres(cvDescription),
				toStringPostgres(getCkUser()),
				toStringPostgres(getCtChange()),
				toStringPostgres(getCtCreate()),
				toStringPostgres(isClDeleted())
		);
	}
}