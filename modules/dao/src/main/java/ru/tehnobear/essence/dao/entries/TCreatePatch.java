package ru.tehnobear.essence.dao.entries;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;


/**
 * The persistent class for the "t_create_patch" database table.
 * 
 */
@Entity
@Table(name="\"t_create_patch\"")
@NamedQuery(name="TCreatePatch.findAll", query="SELECT t FROM TCreatePatch t")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TCreatePatch implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="\"ck_id\"")
	private UUID ckId;

	@Column(name="\"cct_parameter\"")
	private String cctParameter;

	@Column(name="\"ct_create\"")
	private Instant ctCreate;

	@Column(name="\"ck_user\"")
	private String ckUser;

	@Column(name="\"cn_size\"")
	private long cnSize;

	@Column(name="\"ct_change\"")
	private Instant ctChange;

	@Column(name="\"cv_file_name\"")
	private String cvFileName;
}