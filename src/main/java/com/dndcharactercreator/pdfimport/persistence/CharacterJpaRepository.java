package com.dndcharactercreator.pdfimport.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data repository for {@link CharacterEntity}.
 *
 * <p>{@link JpaRepository} already provides {@code save}, {@code findById}, {@code findAll},
 * and {@code deleteById} - no custom query methods are needed yet, so this interface is
 * intentionally empty beyond declaring the entity/id types.
 *
 * @author Carter Ballard
 */
@Repository
public interface CharacterJpaRepository extends JpaRepository<CharacterEntity, UUID> {
}
