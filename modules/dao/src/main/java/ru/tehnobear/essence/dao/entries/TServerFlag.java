package ru.tehnobear.essence.dao.entries;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.Instant;


/**
 * The persistent class for the "t_server_flag" database table.
 * 
 */
@Entity
@Table(name="\"t_server_flag\"")
@NamedQuery(name="TServerFlag.findAll", query="SELECT t FROM TServerFlag t")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class TServerFlag implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="\"ck_id\"", nullable = false)
	private String ckId;

	@Column(name="\"ct_change\"", nullable = false)
	@UpdateTimestamp
	private Instant ctChange;
}