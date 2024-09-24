package ru.tehnobear.essence.dao.entries;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.tehnobear.essence.dao.dto.Audit;

import java.io.Serializable;


/**
 * The persistent class for the "t_d_global_setting" database table.
 * 
 */
@Entity
@Table(name="\"t_d_global_setting\"")
@NamedQuery(name="TDGlobalSetting.findAll", query="SELECT t FROM TDGlobalSetting t")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class TDGlobalSetting extends Audit implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="\"ck_id\"", nullable = false)
	private String ckId;

	@Column(name="\"cv_description\"")
	private String cvDescription;

	@Column(name="\"cv_value\"")
	private String cvValue;

	@Override
	public String toPostgresPatch() {
		return "";
	}
}