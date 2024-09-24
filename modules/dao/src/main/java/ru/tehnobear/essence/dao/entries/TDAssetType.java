package ru.tehnobear.essence.dao.entries;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.tehnobear.essence.dao.dto.Audit;
import ru.tehnobear.essence.dao.dto.EAssetType;

import java.io.Serializable;


/**
 * The persistent class for the "t_d_asset_type" database table.
 * 
 */
@Entity
@Table(name="\"t_d_asset_type\"")
@NamedQuery(name="TDAssetType.findAll", query="SELECT t FROM TDAssetType t")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class TDAssetType extends Audit implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final String formatPostgres =
			"INSERT INTO ${user.table}.t_d_asset_type\n" +
					"(ck_id, cv_description, ct_create, ct_change, ck_user, cr_type, cl_deleted, cv_content_type, cv_extension)\n" +
					"VALUES(%s, %s, %s::timestamp, %s::timestamp, %s, %s, %s, %s, %s)\n" +
					"ON CONFLICT (ck_id)\n" +
					"DO NOTHING;\n";

	@Id
	@Column(name="\"ck_id\"", nullable = false)
	private String ckId;

	@Column(name="\"cv_description\"")
	private String cvDescription;

	@Column(name="\"cr_type\"", nullable = false)
	@Enumerated(EnumType.STRING)
	private EAssetType crType;

	@Column(name="\"cv_content_type\"", nullable = false)
	@Builder.Default
	private String cvContentType = "plain/text";

	@Column(name="\"cv_extension\"", nullable = false)
	@Builder.Default
	private String cvExtension = ".txt";

	@Override
	public String toPostgresPatch() {
		return String.format(
				formatPostgres,
				toStringPostgres(ckId),
				toStringPostgres(cvDescription),
				toStringPostgres(getCtCreate()),
				toStringPostgres(getCtChange()),
				toStringPostgres(getCkUser()),
				toStringPostgres(crType),
				toStringPostgres(isClDeleted()),
				toStringPostgres(cvContentType),
				toStringPostgres(cvExtension)
		);
	}
}