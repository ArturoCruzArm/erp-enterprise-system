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
@Table(name = "permissions")
public class Permission extends BaseEntity {
    
    @NotBlank
    @Size(max = 100)
    @Column(unique = true, nullable = false)
    private String name;
    
    @Size(max = 200)
    private String description;
    
    @NotBlank
    @Size(max = 50)
    private String module;
    
    @NotBlank
    @Size(max = 50)
    private String action;
    
    @ManyToMany(mappedBy = "permissions")
    private Set<Role> roles;
}