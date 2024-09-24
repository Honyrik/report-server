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
import ru.tehnobear.essence.dao.dto.EDFormatType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * The persistent class for the "t_d_format" database table.
 * 
 */
@Entity
@Table(name="\"t_d_format\"")
@NamedQuery(name="TDFormat.findAll", query="SELECT t FROM TDFormat t")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class TDFormat extends Audit implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final String formatPostgres =
			"INSERT INTO ${user.table}.t_d_format\n" +
					"(ck_id, cv_name, cv_extension, cv_content_type, ck_user, ct_change, ct_create, cl_deleted, cct_parameter, cr_type, cv_plugin)\n" +
					"VALUES(%s, %s, %s, %s, %s, %s::timestamp, %s::timestamp, %s, %s, %s, %s)\n" +
					"ON CONFLICT (ck_id)\n" +
					"DO NOTHING;\n";

	@Id
	@Column(name="\"ck_id\"", nullable = false)
	private String ckId;

	@Column(name="\"cv_content_type\"", nullable = false)
	private String cvContentType;

	@Column(name="\"cv_extension\"", nullable = false)
	private String cvExtension;

	@Column(name="\"cv_name\"", nullable = false)
	private String cvName;

	@Column(name="\"cct_parameter\"")
	@JdbcTypeCode(SqlTypes.JSON)
	@Builder.Default
	private Map<String, Object> cctParameter = new HashMap<>();

	@Column(name="\"cr_type\"", nullable = false)
	@Enumerated(EnumType.STRING)
	private EDFormatType crType;

	@Column(name="\"cv_plugin\"")
	private String cvPlugin;

	@Override
	public String toPostgresPatch() {
		return String.format(
				formatPostgres,
				toStringPostgres(ckId),
				toStringPostgres(cvName),
				toStringPostgres(cvExtension),
				toStringPostgres(cvContentType),
				toStringPostgres(getCkUser()),
				toStringPostgres(getCtChange()),
				toStringPostgres(getCtCreate()),
				toStringPostgres(isClDeleted()),
				toStringPostgres(cctParameter),
				toStringPostgres(crType),
				toStringPostgres(cvPlugin)
		);
	}
}