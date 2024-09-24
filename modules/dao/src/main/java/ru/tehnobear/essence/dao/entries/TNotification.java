package ru.tehnobear.essence.dao.entries;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.type.NumericBooleanConverter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;


/**
 * The persistent class for the "t_notification" database table.
 * 
 */
@Entity
@Table(name="\"t_notification\"")
@NamedQuery(name="TNotification.findAll", query="SELECT t FROM TNotification t")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TNotification implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.UUID)
	@Column(name="\"ck_id\"", nullable = false)
	@Builder.Default
	private UUID ckId = UUID.randomUUID();

	@Column(name="\"cd_en\"")
	private Instant cdEn;

	@Column(name="\"cd_st\"", nullable = false)
	@Builder.Default
	@Nonnull
	private Instant cdSt = Instant.now();

	@Column(name="\"ck_user\"", nullable = false)
	@Nonnull
	private String ckUser;

	@Column(name="\"cl_sent\"", nullable = false)
	@Builder.Default
	@Convert(converter = NumericBooleanConverter.class)
	private Boolean clSent = false;

	@Column(name="\"cl_read\"", nullable = false)
	@Builder.Default
	@Convert(converter = NumericBooleanConverter.class)
	private Boolean clRead = false;

	@Column(name="\"cv_message\"", nullable = false)
	@Nonnull
	private String cctMessage;

}