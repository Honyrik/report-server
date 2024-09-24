package ru.tehnobear.essence.dao.entries;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLJoinTableRestriction;
import org.hibernate.type.SqlTypes;
import ru.tehnobear.essence.dao.dto.Audit;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * The persistent class for the "t_report" database table.
 * 
 */
@Entity
@Table(name="\"t_report\"")
@NamedQuery(name="TReport.findAll", query="SELECT t FROM TReport t")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class TReport extends Audit implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final String formatPostgres =
			"INSERT INTO ${user.table}.t_report\n" +
					"(ck_id, cv_name, ck_d_queue, ck_authorization, cv_duration_expire_storage_online, cct_parameter, cn_priority, ck_user, ct_change, cv_duration_expire_storage_offline, ct_create, cl_deleted)\n" +
					"VALUES(%s::uuid, %s, %s, %s::uuid, %s, %s, %s, %s, %s::timestamp, %s, %s::timestamp, %s)\n" +
					"ON CONFLICT (cv_name)\n" +
					"DO UPDATE \n" +
					"SET ck_id=EXCLUDED.ck_id, ck_d_queue=EXCLUDED.ck_d_queue, ck_authorization=EXCLUDED.ck_authorization, cv_duration_expire_storage_online=EXCLUDED.cv_duration_expire_storage_online, cct_parameter=EXCLUDED.cct_parameter, cn_priority=EXCLUDED.cn_priority, ck_user=EXCLUDED.ck_user, ct_change=EXCLUDED.ct_change, cv_duration_expire_storage_offline=EXCLUDED.cv_duration_expire_storage_offline, ct_create=EXCLUDED.ct_create, cl_deleted=EXCLUDED.cl_deleted;\n";

	@Id
	@GeneratedValue(strategy=GenerationType.UUID)
	@Column(name="\"ck_id\"", nullable = false)
	private UUID ckId;

	@Column(name="\"cct_parameter\"")
	@JdbcTypeCode(SqlTypes.JSON)
	@Builder.Default
	private Map<String, Object> cctParameter = new HashMap<>();

	@Column(name="\"cv_duration_expire_storage_online\"")
	private String cvDurationExpireStorageOnline;

	@Column(name="\"cv_duration_expire_storage_offline\"")
	private String cvDurationExpireStorageOffline;

	@Column(name="\"cn_priority\"")
	@Builder.Default
	private int cnPriority = 100;

	@Column(name="\"cv_name\"", nullable = false)
	private String cvName;

	@ManyToOne
	@JoinColumn(name="\"ck_authorization\"", nullable = false)
	@SQLJoinTableRestriction("not clDeleted")
	private TAuthorization authorization;

	@ManyToOne
	@JoinColumn(name="\"ck_d_queue\"", nullable = false)
	@SQLJoinTableRestriction("not clDeleted")
	private TDQueue queue;

	@Override
	public String toPostgresPatch() {
		return String.format(
				formatPostgres,
				toStringPostgres(ckId),
				toStringPostgres(cvName),
				toStringPostgres(queue == null ? null : queue.getCkId()),
				toStringPostgres(authorization == null ? null : authorization.getCkId()),
				toStringPostgres(cvDurationExpireStorageOnline),
				toStringPostgres(cctParameter),
				toStringPostgres(cnPriority),
				toStringPostgres(getCkUser()),
				toStringPostgres(getCtChange()),
				toStringPostgres(cvDurationExpireStorageOffline),
				toStringPostgres(getCtCreate()),
				toStringPostgres(isClDeleted())
		);
	}
}