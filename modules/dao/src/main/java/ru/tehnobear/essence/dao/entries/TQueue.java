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
 * The persistent class for the "t_queue" database table.
 * 
 */
@Entity
@Table(name="\"t_queue\"")
@NamedQuery(name="TQueue.findAll", query="SELECT t FROM TQueue t")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class TQueue extends Audit implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.UUID)
	@Column(name="\"ck_id\"", nullable = false)
	private UUID ckId;

	@Column(name="\"cct_parameter\"", nullable = false)
	@JdbcTypeCode(SqlTypes.JSON)
	@Builder.Default
	private Map<String, Object> cctParameter = new HashMap<>();

	@Column(name="\"cn_priority\"", nullable = false)
	private Integer cnPriority;

	@Column(name="\"ct_cleaning\"")
	private Instant ctCleaning;

	@Column(name="\"ct_en\"")
	private Instant ctEn;

	@Column(name="\"ct_st\"")
	private Instant ctSt;

	@Column(name="\"cv_report_name\"")
	private String cvReportName;

	//bi-directional many-to-one association to TDFormat
	@ManyToOne
	@JoinColumn(name = "\"ck_report_format\"", nullable = false)
	@SQLJoinTableRestriction("not clDeleted")
	private TReportFormat format;

	//bi-directional many-to-one association to TDQueue
	@ManyToOne
	@JoinColumn(name = "\"ck_d_queue\"", nullable = false)
	@SQLJoinTableRestriction("not clDeleted")
	private TDQueue queue;

	//bi-directional many-to-one association to TDStatus
	@ManyToOne
	@JoinColumn(name = "\"ck_d_status\"", nullable = false)
	@SQLJoinTableRestriction("not clDeleted")
	private TDStatus status;

	//bi-directional many-to-one association to TReport
	@ManyToOne
	@JoinColumn(name = "\"ck_report\"", nullable = false)
	@SQLJoinTableRestriction("not clDeleted")
	private TReport report;

	//bi-directional many-to-one association to TScheduler
	@ManyToOne
	@JoinColumn(name = "\"ck_scheduler\"")
	@SQLJoinTableRestriction("not clDeleted")
	private TScheduler scheduler;

	//bi-directional many-to-one association to TScheduler
	@ManyToOne
	@JoinColumn(name = "\"ck_server\"")
	private TServerFlag server;

	@Column(name="\"cl_online\"", nullable = false)
	@Builder.Default
	@Convert(converter = NumericBooleanConverter.class)
	private boolean clOnline = false;

	@Override
	public String toPostgresPatch() {
		return "";
	}
}