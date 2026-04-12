package com.jsa.jobsearchapp.offer_history;

import com.jsa.jobsearchapp.user.User;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "offer_history")
@Data
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User userId;

    private String url;

}
