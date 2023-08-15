package io.lsdconsulting.intercceptors.example.repository

import io.lsdconsulting.intercceptors.example.entity.Fish
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.transaction.Transactional

@Transactional
@Repository
interface FishRepositoryJpa : CrudRepository<Fish?, String?> {
    fun findFishByName(name: String?): Fish?
    fun deleteByName(name: String?)
}
