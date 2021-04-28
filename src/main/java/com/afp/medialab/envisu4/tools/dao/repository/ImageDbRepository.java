package com.afp.medialab.envisu4.tools.dao.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.afp.medialab.envisu4.tools.dao.entities.Image;

public interface ImageDbRepository extends JpaRepository<Image, Long> {

	Optional<Image> findByMd5sum(String mdr5sum);
}
