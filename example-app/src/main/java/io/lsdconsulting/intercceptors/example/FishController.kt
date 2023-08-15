package io.lsdconsulting.intercceptors.example

import io.lsdconsulting.intercceptors.example.entity.Fish
import io.lsdconsulting.intercceptors.example.repository.FishRepositoryEntityManager
import io.lsdconsulting.intercceptors.example.repository.FishRepositoryJpa
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class FishController(
    private val fishRepositoryJpa: FishRepositoryJpa,
    private val fishRepositoryEntityManager: FishRepositoryEntityManager
) {

    @PostMapping(value = ["/fish"])
    fun add(@RequestBody request: NewFishRequest) {
        fishRepositoryJpa.save(
            Fish(id = System.currentTimeMillis(), name = request.name)
        )
    }

    @DeleteMapping(value = ["/fish/{name}"])
    fun deleteFishWithName(@PathVariable name: String?) {
        fishRepositoryJpa.deleteByName(name)
    }

    @GetMapping(value = ["/fish/{name}"])
    fun getFishWithName(@PathVariable name: String?): String {
        val fishByName = fishRepositoryJpa.findFishByName(name)
        if (Objects.isNull(fishByName)) {
            throw FishNotFound()
        }
        return fishByName.toString()
    }

    @GetMapping(value = ["/fish"])
    fun getFishByName(@RequestParam name: String?): List<String> {
        val fishByName = fishRepositoryJpa.findFishByName(name)
        if (Objects.isNull(fishByName)) {
            throw FishNotFound()
        }
        return listOf(fishByName.toString())
    }

    @PostMapping(value = ["/fish/{id}/{name}"])
    fun createFish(@PathVariable id: Long, @PathVariable name: String?) {
        fishRepositoryEntityManager.persist(id, name)
    }
}
