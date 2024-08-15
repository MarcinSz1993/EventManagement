package com.marcinsz.eventmanagementsystem.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Data
@Entity
@Table(name = "users")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)

    private Long id;
    private String firstName;
    private String lastName;
    @Column(unique = true)
    private String email;
    @Column(unique = true)
    private String username;
    private String password;
    private LocalDate birthDate;
    @Enumerated(value = EnumType.STRING)
    private Role role;
    @Column(unique = true)
    private String phoneNumber;
    @Column(unique = true)
    private String accountNumber;
    private String accountStatus;


    @ManyToMany(mappedBy = "participants",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JsonBackReference
    private List<Event> events;

    @OneToMany(mappedBy = "organizer")
    @JsonManagedReference
    private List<Event> organizedEvents;

    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    private List<Review> reviews;

    public User(String firstName, String lastName, String email,String username,
                String password, LocalDate birthDate, Role role,
                String phoneNumber, String accountNumber,
                String accountStatus) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.password = password;
        this.birthDate = birthDate;
        this.role = role;
        this.phoneNumber = phoneNumber;
        this.accountNumber = accountNumber;
        this.accountStatus = accountStatus;
    }



    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
