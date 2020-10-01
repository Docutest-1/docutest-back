package com.revature.classtest;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GenericEntityRepository extends JpaRepository<GenericEntity, Long> {

    public GenericEntity findOne(Long id);

    public GenericEntity save(GenericEntity genericEntity);
    
}
