package com.jsa.jobsearchapp.userPref;

import com.jsa.jobsearchapp.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPrefRepository extends JpaRepository<UserPref, Integer> {

    Optional<UserPref> findByUser(User user);

    @Query(value = """
            SELECT (
                   CASE
                       WHEN UP.is_no_location_pref = 1 THEN 0
                       WHEN UP.city IS NULL OR UP.city = '' THEN 0
                       ELSE 1
                       END +
                   IF(UP.salary_from IS NULL OR UP.salary_from <= 0, 0, 1) +
                   IF(UP.is_remote IS NULL OR UP.is_remote = 0, 0, 1) +
                   IF(UP.is_hybrid IS NULL OR UP.is_hybrid = 0, 0, 1) +
                   IF(UP.is_on_site IS NULL OR UP.is_on_site = 0, 0, 1)
                   )
            FROM user_pref UP
            WHERE UP.id = ?1""",
            nativeQuery = true)
    Integer calculateMaxScoreByUserPref(int userPrefId);
}
