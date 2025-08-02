package com.erp.system.user.entity;

import com.erp.system.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "roles")
public class Role extends BaseEntity {
    
    @NotBlank
    @Size(max = 50)
    @Column(unique = true, nullable = false)
    private String name;
    
    @Size(max = 200)
    private String description;
    
    @ManyToMany(mappedBy = "roles")
    private Set<User> users;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;
}