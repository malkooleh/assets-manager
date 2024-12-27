package com.userservice.model.db;

import com.userservice.repository.UserRepository;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Entity
@Table(name = "users")
@Audited
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @SequenceGenerator(name = UserRepository.USER_ID_SEQUENCE, sequenceName = UserRepository.USER_ID_SEQUENCE, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = UserRepository.USER_ID_SEQUENCE)
    private Integer userId;

    @Column
    private String name;

    @Column
    private String email;

    @Column
    @CreatedDate
    private LocalDateTime created;
}
