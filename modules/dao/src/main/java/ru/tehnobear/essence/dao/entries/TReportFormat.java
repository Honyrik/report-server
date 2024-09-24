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
 * The persistent class for the "t_report_format" database table.
 * 
 */
@Entity
@Table(name="\"t_report_format\"")
@NamedQuery(name="TReportFormat.findAll", query="SELECT t FROM TReportFormat t")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class TReportFormat extends Audit implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final String formatPostgres =
			"INSERT INTO ${user.table}.t_report_format\n" +
					"(ck_id, ck_report, ck_d_format, ck_asset, ck_user, ct_change, ct_create, ck_source, cl_deleted)\n" +
					"VALUES(%s::uuid, %s::uuid, %s, %s::uuid, %s, %s::timestamp, %s::timestamp, %s, %s)\n" +
					"ON CONFLICT (ck_report, ck_d_format)\n" +
					"DO UPDATE \n" +
					"SET ck_id=EXCLUDED.ck_id, ck_asset=EXCLUDED.ck_asset, ck_user=EXCLUDED.ck_user, ct_change=EXCLUDED.ct_change, ct_create=EXCLUDED.ct_create, ck_source=EXCLUDED.ck_source, cl_deleted=EXCLUDED.cl_deleted;\n";
	@Id
	@GeneratedValue(strategy=GenerationType.UUID)
	@Column(name="\"ck_id\"", nullable = false)
	private UUID ckId;

	//bi-directional many-to-one association to TAsset
	@ManyToOne
	@JoinColumn(name="\"ck_asset\"", nullable = false)
	@SQLJoinTableRestriction("not clDeleted")
	private TAsset asset;

	//bi-directional many-to-one association to TDFormat
	@ManyToOne
	@JoinColumn(name="\"ck_d_format\"", nullable = false)
	@SQLJoinTableRestriction("not clDeleted")
	private TDFormat format;

	//bi-directional many-to-one association to TReport
	@ManyToOne
	@JoinColumn(name="\"ck_report\"", nullable = false)
	@SQLJoinTableRestriction("not clDeleted")
	private TReport report;

	//bi-directional many-to-one association to TReport
	@ManyToOne
	@JoinColumn(name="\"ck_source\"", nullable = false)
	@SQLJoinTableRestriction("not clDeleted")
	private TSource source;

	@Override
	public String toPostgresPatch() {
		return String.format(
				formatPostgres,
				toStringPostgres(ckId),
				toStringPostgres(report == null ? null : report.getCkId()),
				toStringPostgres(format == null ? null : format.getCkId()),
				toStringPostgres(asset == null ? null : asset.getCkId()),
				toStringPostgres(getCkUser()),
				toStringPostgres(getCtChange()),
				toStringPostgres(getCtCreate()),
				toStringPostgres(source == null ? null : source.getCkId()),
				toStringPostgres(isClDeleted())
		);
	}
}