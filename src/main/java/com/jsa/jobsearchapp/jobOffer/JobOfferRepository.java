package com.jsa.jobsearchapp.jobOffer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface JobOfferRepository extends JpaRepository<JobOffer, Integer> {

    Optional<JobOffer> findByUrl(String url);

    @NativeQuery(value = """
            SELECT o.url,
                   (
                       IF(o.seniority = :seniority, 1, 0) +
                       IF(o.salary_from >= :salaryFrom && o.salary_to <= :salaryTo, 1, 0) +
                       IF(o.employment_type = :employmentType, 1, 0) +
                       IF(o.type_of_contract = :typeOfContract, 1, 0) +
                       IF(o.is_remote = :isRemote && o.is_hybrid = :isHybrid && o.is_on_site = :isOnSite, 1, 0)
                       ) AS 'match_count'
            FROM offer o;
            """)
    List<OfferMatchProjectionOld> findAllByUserPrefOld(
            String seniority,
            Double salaryFrom,
            Double salaryTo,
            String employmentType,
            String typeOfContract,
            boolean isRemote,
            boolean isHybrid,
            boolean isOnSite,
            String locationsString
    );

    @Query(value = """
            WITH gateOneOffers
                     AS (
                    SELECT *
                    FROM (
                             SELECT O.id AS id,
                                    O.url AS url,
                                    O.seniority AS seniority,
                                    O.salary_from AS salaryFrom,
                                    O.is_remote AS isRemote,
                                    O.is_hybrid AS isHybrid,
                                    O.is_on_site AS isOnSite,
                                    (
                                        CASE
                                            WHEN ?2 = 1 THEN 1
                                            WHEN EXISTS (
                                                SELECT 1
                                                FROM offer_location OL
                                                         JOIN location L
                                                              ON OL.location_id = L.id
                                                WHERE ol.offer_id = O.id
                                                  AND L.alias_name = ?3
                                            ) THEN 1
                                            ELSE 0
                                            END
                                            + IF(O.seniority = ?4, 1, 0)
                                            + IF(O.salary_from >= ?5 AND O.salary_from > 0, 1, 0)
                                            + IF(O.is_remote = ?6, 1, 0)
                                            + IF(O.is_hybrid = ?7, 1, 0)
                                            + IF(O.is_on_site = ?8, 1, 0)
                                        ) AS Offer_Gate_One_Score
                             FROM offer O
                             WHERE NOT EXISTS (
                                 SELECT 1
                                 FROM offer_history OH
                                 WHERE OH.url = O.url
                                   AND user_id = ?9
                             )
                         ) t
                    WHERE t.Offer_Gate_One_Score / ?10 >= 0.4
                )
            
            SELECT
                id,
                url,
                seniority,
                salaryFrom,
                isRemote,
                isHybrid,
                isOnSite
            FROM gateOneOffers AS O
            WHERE (SELECT COUNT(1) FROM user_pref_skill WHERE user_pref_id = ?1) = 0 OR EXISTS (
                SELECT 1
                FROM skill S
                         JOIN offer_skill OS
                              ON S.id = OS.skill_id
                         JOIN user_pref_skill UPS
                              ON S.id = UPS.skill_id
                WHERE OS.offer_id = O.id AND UPS.user_pref_id = ?1
            )
              AND
                (?2 IS NULL OR CASE ?2
                                    WHEN 'city' THEN EXISTS (
                                        SELECT 1
                                        FROM offer_location OL
                                                 JOIN location L
                                                      ON OL.location_id = L.id
                                        WHERE ol.offer_id = O.id
                                          AND L.alias_name = ?3
                                    )
                                    WHEN 'seniority' THEN O.seniority = ?4
                                    WHEN 'salaryFrom' THEN O.salaryFrom >= ?5 AND O.salaryFrom > 0
                                    WHEN 'isRemote' THEN O.isRemote = ?6
                                    WHEN 'isHybrid' THEN O.isHybrid = ?7
                                    WHEN 'isOnSite' THEN O.isOnSite = ?8
                    END);""",
            nativeQuery = true)
    List<OfferMatchProjection> findAllUrlsByUserPref(
            int userPrefId,
            boolean isNoLocationPreference,
            String cityAsAliasKey,
            String seniority,
            double salaryFrom,
            boolean isRemote,
            boolean isHybrid,
            boolean isOnSite,
            int userId,
            int maxScore
    );

    List<JobOffer> findTop5By();
}
