package ru.tehnobear.essence.dao.entries;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLJoinTableRestriction;
import org.hibernate.type.NumericBooleanConverter;
import org.hibernate.type.SqlTypes;
import ru.tehnobear.essence.dao.dto.Audit;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * The persistent class for the "t_source" database table.
 * 
 */
@Entity
@Table(name="\"t_source\"")
@NamedQuery(name="TSource.findAll", query="SELECT t FROM TSource t")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class TSource extends Audit implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final String formatPostgres =
			"INSERT INTO ${user.table}.t_source\n" +
					"(ck_id, cct_parameter, cv_plugin, ck_d_source, ck_user, ct_change, cl_enable, ct_create, cl_deleted, cv_script)\n" +
					"VALUES(%s, %s, %s, %s, %s, %s::timestamp, %s::smallint, %s::timestamp, %s, %s)\n" +
					"ON CONFLICT (ck_id)\n" +
					"DO NOTHING;\n";

	@Id
	@Column(name="\"ck_id\"", nullable = false)
	private String ckId;

	@Column(name="\"cct_parameter\"", nullable = false)
	@JdbcTypeCode(SqlTypes.JSON)
	@Builder.Default
	private Map<String, Object> cctParameter = new HashMap<>();

	@Column(name="\"cl_enable\"", nullable = false)
	@Convert(converter = NumericBooleanConverter.class)
	private Boolean clEnable;

	@Column(name="\"cv_plugin\"")
	private String cvPlugin;

	@Column(name="\"cv_script\"")
	private String cvScript;

	//bi-directional many-to-one association to TDSourceType
	@ManyToOne
	@JoinColumn(name="\"ck_d_source\"", nullable = false)
	@SQLJoinTableRestriction("not clDeleted")
	private TDSourceType sourceType;

	@Override
	public String toPostgresPatch() {
		return String.format(
				formatPostgres,
				toStringPostgres(ckId),
				toStringPostgres(cctParameter),
				toStringPostgres(cvPlugin),
				toStringPostgres(sourceType == null ? null : sourceType.getCkId()),
				toStringPostgres(getCkUser()),
				toStringPostgres(getCtChange()),
				toStringPostgres(clEnable),
				toStringPostgres(getCtCreate()),
				toStringPostgres(isClDeleted()),
				toStringPostgres(cvScript)
		);
	}

}