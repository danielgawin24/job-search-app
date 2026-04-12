package com.jsa.jobsearchapp.user;

import com.jsa.jobsearchapp.offer_history.History;
import com.jsa.jobsearchapp.userPref.UserPref;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "user")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_pref_id", referencedColumnName = "id", unique = true)
    private UserPref userPref;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @OneToMany(mappedBy = "userId")
    private Set<History> jobsHistory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('USER','ADMIN')")
    private UserRole role;

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
