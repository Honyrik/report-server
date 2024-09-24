package ru.tehnobear.essence.dao.entries;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
 * The persistent class for the "t_asset" database table.
 * 
 */
@Entity
@Table(name="\"t_asset\"")
@NamedQuery(name="TAsset.findAll", query="SELECT t FROM TAsset t")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class TAsset extends Audit implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final String formatPostgres =
			"INSERT INTO ${user.table}.t_asset\n" +
					"(ck_id, cv_name, cb_asset, cct_parameter, ck_user, ct_change, ct_create, ck_d_type, cv_asset, cl_deleted, cv_plugin)\n" +
					"VALUES(%s::uuid, %s, %s, %s, %s, %s::timestamp, %s::timestamp, %s, %s, %s, %s)\n" +
					"ON CONFLICT (ck_id)\n" +
					"DO UPDATE \n" +
					"SET cv_name=EXCLUDED.cv_name, cb_asset=EXCLUDED.cb_asset, cct_parameter=EXCLUDED.cct_parameter, ck_user=EXCLUDED.ck_user, ct_change=EXCLUDED.ct_change, ct_create=EXCLUDED.ct_create, ck_d_type=EXCLUDED.ck_d_type, cv_asset=EXCLUDED.cv_asset, cl_deleted=EXCLUDED.cl_deleted, cv_plugin=EXCLUDED.cv_plugin;\n";

	@Id
	@GeneratedValue(strategy=GenerationType.UUID)
	@Column(name="\"ck_id\"", nullable = false)
	private UUID ckId;

	@Column(name="\"cb_asset\"")
	@JsonIgnore
	private byte[] cbAsset;

	@Column(name="\"cv_asset\"")
	private String cvAsset;

	@Column(name="\"cct_parameter\"")
	@JdbcTypeCode(SqlTypes.JSON)
	@Builder.Default
	private Map<String, Object> cctParameter = new HashMap<>();

	@Column(name="\"cv_name\"", nullable = false)
	private String cvName;

	@ManyToOne
	@JoinColumn(name = "\"ck_d_type\"", nullable = false)
	@SQLJoinTableRestriction("not clDeleted")
	private TDAssetType type;

	@Column(name="\"cv_plugin\"")
	private String cvPlugin;

	public String toPostgresPatch() {
		return String.format(
			formatPostgres,
			toStringPostgres(ckId),
			toStringPostgres(cvName),
			toStringPostgres(cbAsset),
			toStringPostgres(cctParameter),
			toStringPostgres(getCkUser()),
			toStringPostgres(getCtChange()),
			toStringPostgres(getCtCreate()),
			toStringPostgres(type.getCkId()),
			toStringPostgres(cvAsset),
			toStringPostgres(isClDeleted()),
			toStringPostgres(cvPlugin)
		);
	}
}