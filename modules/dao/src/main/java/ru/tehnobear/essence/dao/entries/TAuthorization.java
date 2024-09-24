package ru.tehnobear.essence.dao.entries;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.tehnobear.essence.dao.dto.Audit;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * The persistent class for the "t_authorization" database table.
 * 
 */
@Entity
@Table(name="\"t_authorization\"")
@NamedQuery(name="TAuthorization.findAll", query="SELECT t FROM TAuthorization t")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class TAuthorization extends Audit implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final String formatPostgres =
			"INSERT INTO ${user.table}.t_authorization\n" +
					"(ck_id, cv_name, cv_plugin, cct_parameter, ck_user, ct_change, ct_create, cl_deleted)\n" +
					"VALUES(%s::uuid, %s, %s, %s, %s, %s::timestamp, %s::timestamp, %s)\n" +
					"ON CONFLICT (ck_id)\n" +
					"DO NOTHING;\n";
	@Id
	@GeneratedValue(strategy=GenerationType.UUID)
	@Column(name="\"ck_id\"", nullable = false)
	private UUID ckId;

	@Column(name="\"cct_parameter\"")
	@JdbcTypeCode(SqlTypes.JSON)
	@Builder.Default
	private Map<String, Object> cctParameter = new HashMap<>();

	@Column(name="\"cv_name\"", nullable = false)
	private String cvName;

	@Column(name="\"cv_plugin\"", nullable = false)
	private String cvPlugin;

	@Override
	public String toPostgresPatch() {
		return String.format(
				formatPostgres,
				toStringPostgres(ckId),
				toStringPostgres(cvName),
				toStringPostgres(cvPlugin),
				toStringPostgres(cctParameter),
				toStringPostgres(getCkUser()),
				toStringPostgres(getCtChange()),
				toStringPostgres(getCtCreate()),
				toStringPostgres(isClDeleted())
		);
	}
}