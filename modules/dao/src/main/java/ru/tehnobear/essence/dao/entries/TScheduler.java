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
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * The persistent class for the "t_scheduler" database table.
 * 
 */
@Entity
@Table(name="\"t_scheduler\"")
@NamedQuery(name="TScheduler.findAll", query="SELECT t FROM TScheduler t")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class TScheduler extends Audit implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final String formatPostgres =
			"INSERT INTO ${user.table}.t_scheduler\n" +
					"(ck_id, cct_parameter, cn_priority, cv_unix_cron, ck_report, cv_report_name, ck_user, ct_change, ct_start_run_cron, cl_enable, ct_create, cl_deleted, ck_report_format)\n" +
					"VALUES(%s::uuid, %s, %s, %s, %s::uuid, %s, %s, %s::timestamp, %s::timestamp, %s, %s::timestamp, %s, %s::uuid)\n" +
					"ON CONFLICT (ck_id)\n" +
					"DO UPDATE \n" +
					"SET cct_parameter=EXCLUDED.cct_parameter, cn_priority=EXCLUDED.cn_priority, cv_unix_cron=EXCLUDED.cv_unix_cron, ck_report=EXCLUDED.ck_report, cv_report_name=EXCLUDED.cv_report_name, ck_user=EXCLUDED.ck_user, ct_change=EXCLUDED.ct_change, ct_start_run_cron=EXCLUDED.ct_start_run_cron, cl_enable=EXCLUDED.cl_enable, ct_create=EXCLUDED.ct_create, cl_deleted=EXCLUDED.cl_deleted, ck_report_format=EXCLUDED.ck_report_format;\n";

	@Id
	@GeneratedValue(strategy=GenerationType.UUID)
	@Column(name="\"ck_id\"", nullable = false)
	private UUID ckId;

	@Column(name="\"cct_parameter\"", nullable = false)
	@JdbcTypeCode(SqlTypes.JSON)
	@Builder.Default
	private Map<String, Object> cctParameter = new HashMap<>();

	@Column(name="\"cl_enable\"", nullable = false)
	@Convert(converter = NumericBooleanConverter.class)
	@Builder.Default
	private boolean clEnable = true;

	@Column(name="\"cn_priority\"", nullable = false)
	private Integer cnPriority;

	@Column(name="\"ct_next_run_cron\"")
	private Instant ctNextRunCron;

	@Column(name="\"ct_start_run_cron\"")
	@Builder.Default
	private Instant ctStartRunCron = Instant.now();

	@Column(name="\"cv_report_name\"")
	private String cvReportName;

	@Column(name="\"cv_unix_cron\"", nullable = false)
	private String cvUnixCron;

	//bi-directional many-to-one association to TDFormat
	@ManyToOne
	@JoinColumn(name = "\"ck_report_format\"", nullable = false)
	@SQLJoinTableRestriction("not clDeleted")
	private TReportFormat format;

	//bi-directional many-to-one association to TReport
	@ManyToOne
	@JoinColumn(name="\"ck_report\"", nullable = false)
	@SQLJoinTableRestriction("not clDeleted")
	private TReport report;

	@Override
	public String toPostgresPatch() {
		return String.format(
				formatPostgres,
				toStringPostgres(ckId),
				toStringPostgres(cctParameter),
				toStringPostgres(cnPriority),
				toStringPostgres(cvUnixCron),
				toStringPostgres(report == null ? null : report.getCkId()),
				toStringPostgres(cvReportName),
				toStringPostgres(getCkUser()),
				toStringPostgres(getCtChange()),
				toStringPostgres(ctStartRunCron),
				toStringPostgres(clEnable),
				toStringPostgres(getCtCreate()),
				toStringPostgres(isClDeleted()),
				toStringPostgres(format == null ? null : format.getCkId())
		);
	}
}