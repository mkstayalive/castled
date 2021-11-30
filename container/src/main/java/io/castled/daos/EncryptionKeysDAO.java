package io.castled.daos;

import io.castled.encryption.TeamEncryptionKey;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface EncryptionKeysDAO {

    @SqlUpdate("insert into encryption_keys(team_id, encryption_key) values(:encryptionKey.teamId, :encryptionKey.cipherKey)")
    @GetGeneratedKeys
    long createEncryptionKey(@BindBean("encryptionKey") TeamEncryptionKey encryptionKey);

    @SqlQuery("select encryption_key from encryption_keys where team_id = :teamId and is_deleted = 0")
    String getEncryptionKey(@Bind("teamId") Long teamId);

    @SqlUpdate("update encryption_keys set is_deleted = 1 where team_id = :teamId")
    void deleteEncryptionKey(@Bind("teamId") Long teamId);

}


