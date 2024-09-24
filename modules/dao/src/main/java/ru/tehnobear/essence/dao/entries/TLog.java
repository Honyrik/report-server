package ru.tehnobear.essence.dao.entries;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;


/**
 * The persistent class for the "t_log" database table.
 * 
 */
@Entity
@Table(name="\"t_log\"")
@NamedQuery(name="TLog.findAll", query="SELECT t FROM TLog t")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TLog implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.UUID)
	@Column(name="\"ck_id\"", nullable = false)
	@Builder.Default
	private UUID ckId = UUID.randomUUID();

	@Column(name="\"cc_json\"")
	private String ccJson;

	@Column(name="\"ck_user\"", nullable = false)
	private String ckUser;

	@Column(name="\"ct_change\"", nullable = false)
	@Builder.Default
	private Instant ctChange = Instant.now();

	@Column(name="\"cv_action\"", nullable = false)
	private String cvAction;

	@Column(name="\"cv_error\"")
	private String cvError;

	@Column(name="\"cv_id\"")
	private String cvId;

	@Column(name="\"cv_table\"", nullable = false)
	private String cvTable;

}